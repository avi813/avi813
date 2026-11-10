# CMF // Nocturnaa Minimalist OLED Launcher v1.4 (Android 14 · Nothing OS 2.6)

This package contains the complete, lightweight Android 14 (`API 34`) source project for **Nocturnaa v1.4**, engineered for the **CMF by Nothing Phone 1**.

## What's New in v1.4
- **Lomo Web Pixel (1px Thin Stroke) Font**: Replaced the thick blocky glyphs with a slender 1-pixel stroke 9-row Lomo Web Pixel TrueType font (`LOMO_PIXEL_TTF_B64`) with proper lowercase descenders.
- **Exact Prototype Home Screen Widgets Setup**: Replicated the 12-hour `4:22 PM` clock with pulsing CMF Orange `:` colon, `TUE · 27 OCT · ALARM 07:00`, faint horizontal divider, pill-shaped `BAT 86%` progress bar with `OLED` tag, and `● 28°C CLEAR SKY` + `[AQI 42 · GOOD]` pill.
- **Alphabetical App Drawer with Group Headers & Faint Divider Lines**: Sorted A–Z with orange letter headers and faint horizontal divider lines after each alphabet group.
- **120Hz Fluid App Drawer Scrolling**: Replaced gesture-intercepting `pointerInput` and observable map writes with a native `NestedScrollConnection` and non-observable `HashMap` coordinate lookup.
- **Swipe Down on Home Screen for Notifications**: Pulling down on the Home Screen expands Android's notification shade via `StatusBarManager.expandNotificationsPanel()`.
- **Clean Bottom Bezel + Long-Tap Home Quick Menu**: Removed all bottom dock text (`/ search`, `all apps`, `+ widgets`, `cfg`) from the Home Screen; long-tapping anywhere on the Home Screen opens the Quick Menu modal.
- **System-Wide App Deletion (`REQUEST_DELETE_PACKAGES`)**: Triggers Android's native system uninstaller dialog when clicking **Uninstall App** from the long-press menu.
