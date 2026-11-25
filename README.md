# MTV Retro — Gradle-fixed Android TV starter

This project is pinned to **Gradle 8.13** using `gradle/wrapper/gradle-wrapper.properties`.
This avoids the Gradle 9.6.0 / Android Gradle Plugin 8.x internal API incompatibility.

## Open
1. Extract the ZIP.
2. In Android Studio, choose **File → Open** and select the `MTVRetro_Fixed` folder.
3. Allow Android Studio to download Gradle 8.13 and sync.
4. Select the `app` run configuration and an Android TV emulator/device.
5. Press Run.

If Android Studio still uses Gradle 9.6:
- Open **Settings/Preferences → Build, Execution, Deployment → Build Tools → Gradle**.
- Set **Gradle distribution** to **Wrapper** / **gradle-wrapper.properties**.
- Sync again.

Main source: `app/src/main/java/com/retro/tv/MainActivity.kt`
