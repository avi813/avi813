# CMF // N-TEXT Minimalist OLED Launcher (Android 14 · Nothing OS 2.6)

This package contains the complete, lightweight Android 14 (`API 34`) source project for **N-TEXT Launcher**, engineered specifically for the **CMF by Nothing Phone 1**.

## Key Features Included
- **Pure `#000000` OLED Minimalist Text Design** with 6 prebuilt font styles (**Nothing N-Dot**, **Nothing Dot-Matrix**, **JetBrains Tech Mono**, **Space Mono**, **Clean Geometric Sans**, and **Minimal Editorial Serif**).
- **Niagara Launcher-Style Wave Traversal** on both the Home Screen and the A–Z App Drawer rail (`60Hz/120Hz` smooth cosine wave displacement + haptic tick + floating letter bubble).
- **Hide Default Android Notification Bar** (`WindowInsetsControllerCompat.hide(WindowInsetsCompat.Type.statusBars())`) while keeping the custom **N-TEXT Minimal Top Bar** (`12:30`, `ZEN`, `5G`, `[86%]`).
- **Android 14 System Navigation Support** (`WindowInsets.navigationBars`) for both **3-Button System Navigation (`◁ ○ ▢`)** and **Gesture Navigation**.
- **Removable & Reorderable Home Widgets** (`Dot-Matrix Clock & Date`, `Weather & AQI Pill`, `CMF Battery & RAM Telemetry`, `25m Zen Focus Timer`, and `Terminal Scratchpad`).
- **Default Home Launcher Selector** (`RoleManager.ROLE_HOME`) & **OLED Wallpaper Engine** (built-in OLED presets + Android System Wallpaper picker).

## How to Compile the Installable `.apk`

### Method 1: Zero-Setup Cloud APK Build via GitHub Actions (Recommended — No PC Setup Needed)
1. Create a free repository on GitHub and upload the contents of `CMF_NText_Launcher_Android14_Project.zip`.
2. Go to the **Actions** tab in your GitHub repository — the included `.github/workflows/build-apk.yml` workflow will automatically run on push (or click **Run workflow**).
3. In ~90 seconds, download `CMF-NText-Launcher-APK.zip` from the workflow's **Artifacts** section, extract `app-release.apk` (or `app-debug.apk`), and install it on your CMF Phone 1.

### Method 2: Compile Locally in Android Studio or Command Line
1. Extract `CMF_NText_Launcher_Android14_Project.zip` and open the folder in **Android Studio (Iguana / Jellyfish or newer)**.
2. Let Gradle sync, then select **Build → Build Bundle(s) / APK(s) → Build APK(s)** (or run `./gradlew assembleRelease` in the terminal).
3. Install on your CMF Phone 1 via USB Debugging:
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```
4. Press the Home button (`○`) or open **N-TEXT Settings → Default Home Launcher** to set N-TEXT as your default Nothing OS 2.6 launcher.
