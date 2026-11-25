package com.retro.tv

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

// YouTube requires embedders to identify themselves (error 153 otherwise).
// Use your app's package name as an https origin.
private const val APP_ORIGIN = "https://com.retro.tv"

// Makes YouTube believe VP9/AV1 are unsupported so it serves H.264 instead (safer on TVs).
private const val FORCE_H264_JS = """(function(){try{var ms=window.MediaSource;if(ms&&ms.isTypeSupported){var o=ms.isTypeSupported.bind(ms);ms.isTypeSupported=function(t){return /vp9|vp09|av01/i.test(t)?false:o(t);};}var p=HTMLMediaElement.prototype,c=p.canPlayType;p.canPlayType=function(t){return /vp9|vp09|av01/i.test(t)?'':c.call(this,t);};}catch(e){}})();"""

private const val PLAYER_HTML = """
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="referrer" content="strict-origin-when-cross-origin">
<style>html,body{margin:0;width:100%;height:100%;background:#000;overflow:hidden}#p,iframe{position:fixed;top:0;left:0;width:100vw;height:100vh;border:0}</style>
</head>
<body>
<div id="p"></div>
<script>
window.pendingId = '__VID__';
var player, ready = false;
window.onerror = function (m) { Android.log('JS error: ' + m); };
Android.log('page loaded, first id=' + window.pendingId);
function onYouTubeIframeAPIReady() {
  player = new YT.Player('p', {
    width: '100%', height: '100%',
    playerVars: { controls: 0, disablekb: 1, fs: 0, modestbranding: 1, rel: 0,
                  iv_load_policy: 3, playsinline: 1, autoplay: 1,
                  origin: '__ORIGIN__' },
    events: {
      onReady: function () { ready = true; Android.log('player ready, loading ' + window.pendingId); if (window.pendingId) player.loadVideoById(window.pendingId); },
      onStateChange: function (e) { Android.log('state ' + e.data); if (e.data === 0) Android.onEnded(); },
      onError: function (e) { Android.log('error ' + e.data); Android.onError(e.data); }
    }
  });
}
function play(id) { Android.log('play ' + id + ' ready=' + ready); window.pendingId = id; if (ready) player.loadVideoById(id); }
</script>
<script src="https://www.youtube.com/iframe_api"></script>
</body>
</html>
"""

private fun Context.findActivity(): Activity {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    error("YouTubePlayer needs to be inside an Activity")
}

/**
 * The WebView is NOT hosted inside Compose (that is what made the picture disappear on this TV).
 * It is added to the Activity as a normal view BEHIND the Compose UI, so the Compose screen on top
 * must be transparent while this is shown (MainActivity does that for the player screen).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayer(
    videoId: String,
    onEnded: () -> Unit,
    onError: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val endedCb by rememberUpdatedState(onEnded)
    val errorCb by rememberUpdatedState(onError)
    val main = remember { Handler(Looper.getMainLooper()) }
    val holder = remember { arrayOfNulls<WebView>(1) }
    val firstId = remember { videoId }

    DisposableEffect(Unit) {
        val activity = ctx.findActivity()
        val parent = activity.findViewById<ViewGroup>(android.R.id.content)

        val wv = WebView(activity).apply {
            isFocusable = false              // remote keys stay with Compose
            isFocusableInTouchMode = false
            setBackgroundColor(Color.BLACK)
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            addJavascriptInterface(object {
                @JavascriptInterface
                fun log(msg: String) { android.util.Log.d("RETROTV", msg) }

                @JavascriptInterface
                fun onEnded() { main.post { endedCb() } }

                @JavascriptInterface
                fun onError(code: Int) { main.post { errorCb(code) } }
            }, "Android")
            if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                WebViewCompat.addDocumentStartJavaScript(
                    this, FORCE_H264_JS,
                    setOf("https://www.youtube.com", APP_ORIGIN)
                )
            }
            tag = firstId
            loadDataWithBaseURL(
                APP_ORIGIN,
                PLAYER_HTML.replace("__VID__", firstId).replace("__ORIGIN__", APP_ORIGIN),
                "text/html", "utf-8", null
            )
        }
        // index 0 = behind the Compose view
        parent.addView(
            wv, 0,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        holder[0] = wv

        onDispose {
            holder[0] = null
            parent.removeView(wv)
            wv.stopLoading()
            wv.loadUrl("about:blank")
            wv.destroy()
        }
    }

    LaunchedEffect(videoId) {
        holder[0]?.let { wv ->
            if (wv.tag != videoId) {
                wv.tag = videoId
                wv.evaluateJavascript("play('$videoId')", null)
            }
        }
    }

    // Transparent placeholder so the Compose layout keeps its shape.
    Box(modifier)
}