package com.retro.tv

import android.content.Context
import org.json.JSONArray

data class Clip(
    val id: String,
    val title: String,
    val artist: String,
    val decade: String,
    val isAd: Boolean
)

object ClipRepo {
    private var cache: List<Clip>? = null

    fun load(ctx: Context): List<Clip> {
        cache?.let { return it }
        val json = ctx.assets.open("clips.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        val list = (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            Clip(
                id = o.getString("id"),
                title = o.getString("title"),
                artist = o.optString("artist"),
                decade = o.getString("decade"),
                isAd = o.getString("type") == "ad"
            )
        }.filter { !it.id.startsWith("REPLACE") }   // skip unfilled placeholder ids
        cache = list
        return list
    }
}

/** Plays every item once in random order, then reshuffles (never the same item twice in a row). */
class ShuffleBag<T>(private val items: List<T>) {
    private var bag = ArrayDeque<T>()
    private var last: T? = null

    fun next(): T {
        if (bag.isEmpty()) {
            var s = items.shuffled()
            if (s.size > 1 && s.first() == last) s = s.drop(1) + s.first()
            bag = ArrayDeque(s)
        }
        return bag.removeFirst().also { last = it }
    }
}

/** Endless channel: songs, with a commercial break every [adEvery] songs. */
class Channel(songs: List<Clip>, ads: List<Clip>, private val adEvery: Int = 4) {
    private val songBag = ShuffleBag(songs)
    private val adBag = if (ads.isNotEmpty()) ShuffleBag(ads) else null
    private var count = 0

    fun next(): Clip {
        if (adBag != null && count > 0 && count % adEvery == 0) {
            count++
            return adBag.next()
        }
        count++
        return songBag.next()
    }
}

sealed class Mode {
    object Live : Mode()
    data class DecadeChannel(val decade: String) : Mode()
    data class Top30(val decade: String) : Mode()
}