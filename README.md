# ZukuLock

[![GitHub release](https://img.shields.io/github/v/release/dwicao/zukulock?label=ZukuLock)](https://github.com/dwicao/zukulock/releases)


[![Download APK](https://img.shields.io/badge/Download-APK-green)](https://github.com/dwicao/zukulock/releases)



## Introduction


**ZukuLock** is a lightweight Android application that locks your screen with a single tap. The app utilizes **Shizuku** to execute the `input keyevent 223` (`KEYCODE_SLEEP`) command, which behaves exactly like pressing a dedicated screen-off button. This is particularly useful for reducing wear and tear on your power button or putting the screen to sleep from launchers and home-screen gestures.

This app was initially designed for **Google Pixel devices**, and it avoids **Accessibility permissions** on purpose. Many banking apps block usage when Accessibility is enabled, so **ZukuLock** offers a workaround that lets you use banking apps seamlessly.



----------


## Key Features


-  **One-tap screen lock**: Turns the screen off instantly — it never wakes the device.
-  **No accidental camera launches**: Uses `KEYCODE_SLEEP` (223) instead of `KEYCODE_POWER` (26), so rapid taps are never interpreted as "double-press power". A 2-second debounce provides an extra layer of protection.
-  **Shizuku Integration**: Leverages system-level permissions without requiring root access.
-  **No UI Interruption**: Simply tap the app icon to execute the action without launching any additional windows.
-  **Customizable Gestures**: Works with launchers like **Lawnchair** or **Nova Launcher** to enable double-tap gestures for locking the screen.


----------


## Requirements


-  **Android 7.0 or higher**.
-  **Shizuku** must be installed and properly configured before use.


----------


## How to Use


1. Install the ZukuLock application.
2. Ensure **Shizuku** is running and the app has been granted permission. On the first tap, ZukuLock will ask for the Shizuku permission and lock the screen as soon as it is granted.
3. Tap the app icon to lock the screen.
4. For double-tap-to-lock gestures, use launchers like **Lawnchair** or **Nova Launcher** to map the gesture to open ZukuLock.


----------


## Notes


- This app does not require root access but relies on **Shizuku** for executing system commands.
- The lock functionality is implemented using the `adb shell input keyevent 223` command, with a fallback to `26` (`KEYCODE_POWER`) on devices where `KEYCODE_SLEEP` is unsupported.
- Rapid repeat taps (within 2 seconds) are ignored on purpose.
- Avoids the need for Accessibility permissions, enabling smooth use of banking apps that restrict access when Accessibility is enabled.


----------


## Building


```sh
./gradlew assembleDebug          # debug APK
./gradlew testDebugUnitTest      # unit tests
./gradlew assembleRelease        # minified release APK (falls back to the debug key if no release keystore is configured)
```

Release builds are signed in CI using secrets (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`) and published to GitHub Releases on version tags. Pull requests and pushes to `main` run the CI workflow (tests + minified release build) automatically.


----------


## Contribution

If you have ideas or want to contribute to improving the app, feel free to provide feedback or open a pull request on this repository.


----------


## Contact

For any questions or support, please reach out via email: [tien.dnm@outlook.com].


----------

## License

This project is licensed under the [MIT License](LICENSE).
