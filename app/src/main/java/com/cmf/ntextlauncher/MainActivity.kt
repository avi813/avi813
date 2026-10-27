package com.cmf.ntextlauncher

import android.app.Activity
import android.app.WallpaperManager
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.AlarmClock
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

// ============================================================================
// DATA MODELS
// ============================================================================
data class AppEntry(
    val label: String,
    val packageName: String,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false
)

enum class LauncherView {
    HOME, DRAWER, WIDGETS, SETTINGS
}

enum class PrebuiltFont(val id: String, val displayName: String, val family: FontFamily) {
    NDOT("dot", "Nothing N-Dot", FontFamily.Monospace),
    DOT_MATRIX("ndot", "Nothing Dot-Matrix", FontFamily.Monospace),
    TECH_MONO("mono", "JetBrains Tech Mono", FontFamily.Monospace),
    SPACE_MONO("spacemono", "Space Mono", FontFamily.Monospace),
    CLEAN_SANS("sans", "Clean Geometric Sans", FontFamily.SansSerif),
    EDITORIAL("editorial", "Minimal Editorial Serif", FontFamily.Serif);

    companion object {
        fun fromId(id: String): PrebuiltFont = values().find { it.id == id } ?: NDOT
    }
}

// ============================================================================
// MAIN LAUNCHER ACTIVITY (CMF by Nothing Phone 1 · Android 14 / Nothing OS 2.6)
// ============================================================================
class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private var hideDefaultAndroidStatusBar by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("cmf_ntext_prefs", Context.MODE_PRIVATE)
        hideDefaultAndroidStatusBar = prefs.getBoolean("hide_android_bar", true)

        // Configure edge-to-edge WindowInsets for Android 14 System Navigation
        WindowCompat.setDecorFitsSystemWindows(window, false)
        applySystemStatusBarVisibility(hideDefaultAndroidStatusBar)

        setContent {
            NTextLauncherApp(
                prefs = prefs,
                onToggleAndroidStatusBar = { hide ->
                    hideDefaultAndroidStatusBar = hide
                    prefs.edit().putBoolean("hide_android_bar", hide).apply()
                    applySystemStatusBarVisibility(hide)
                },
                onRequestDefaultHomeRole = { requestDefaultHomeLauncher() },
                onOpenSystemWallpaperPicker = { openSystemWallpaperChooser() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        applySystemStatusBarVisibility(hideDefaultAndroidStatusBar)
    }

    private fun applySystemStatusBarVisibility(hideStockBar: Boolean) {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (hideStockBar) {
            // Hides the cluttered default Android status/notification bar while keeping System Navigation bar visible
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    private fun requestDefaultHomeLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                    startActivityForResult(intent, 1001)
                    return
                }
            }
        }
        startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
    }

    private fun openSystemWallpaperChooser() {
        val intent = Intent(Intent.ACTION_SET_WALLPAPER)
        startActivity(Intent.createChooser(intent, "Select OLED Wallpaper"))
    }
}

// ============================================================================
// MAIN COMPOSABLE ROOT
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NTextLauncherApp(
    prefs: SharedPreferences,
    onToggleAndroidStatusBar: (Boolean) -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onOpenSystemWallpaperPicker: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Navigation & Edit Mode State
    var currentView by remember { mutableStateOf(LauncherView.HOME) }
    var editMode by remember { mutableStateOf(false) }
    var oledSleepActive by remember { mutableStateOf(false) }

    // Persisted Preferences
    var fontId by remember { mutableStateOf(prefs.getString("font_id", "dot") ?: "dot") }
    var textAlignMode by remember { mutableStateOf(prefs.getString("align", "left") ?: "left") }
    var accentHex by remember { mutableStateOf(prefs.getString("accent", "#FF3B00") ?: "#FF3B00") }
    var textSizeSp by remember { mutableStateOf(prefs.getInt("text_size", 20)) }
    var use24Hour by remember { mutableStateOf(prefs.getBoolean("use_24h", true)) }
    var showCustomTopBar by remember { mutableStateOf(prefs.getBoolean("show_custom_bar", true)) }
    var hideAndroidBar by remember { mutableStateOf(prefs.getBoolean("hide_android_bar", true)) }
    var niagaraWaveMode by remember { mutableStateOf(prefs.getString("wave_mode", "full") ?: "full") }
    var wallpaperPreset by remember { mutableStateOf(prefs.getString("wallpaper", "oled") ?: "oled") }
    var wallpaperDimPercent by remember { mutableStateOf(prefs.getInt("wp_dim", 50)) }
    var autoKeyboard by remember { mutableStateOf(prefs.getBoolean("auto_kb", true)) }
    var doubleTapSleep by remember { mutableStateOf(prefs.getBoolean("dbl_tap_sleep", true)) }

    // Removable Widgets Order & Visibility
    var activeWidgets by remember {
        val saved = prefs.getString("widgets_order", "clock,weather,telemetry") ?: "clock,weather,telemetry"
        mutableStateOf(saved.split(",").filter { it.isNotBlank() })
    }
    var zenActive by remember { mutableStateOf(false) }
    var zenSeconds by remember { mutableStateOf(1500) }
    var quickNote by remember { mutableStateOf(prefs.getString("quick_note", "CMF Minimal Focus") ?: "CMF Minimal Focus") }

    // Pinned & Hidden Package Names
    var pinnedPackages by remember {
        mutableStateOf(
            prefs.getStringSet(
                "pinned_pkgs",
                setOf(
                    "com.google.android.dialer",
                    "com.google.android.apps.messaging",
                    "com.android.chrome",
                    "com.nothing.camera",
                    "com.android.settings"
                )
            )?.toSet() ?: emptySet()
        )
    }
    var hiddenPackages by remember {
        mutableStateOf(prefs.getStringSet("hidden_pkgs", emptySet())?.toSet() ?: emptySet())
    }

    // Installed Apps Loader
    var installedApps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    LaunchedEffect(pinnedPackages, hiddenPackages) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val loaded = resolveInfos
            .mapNotNull { ri ->
                val pkg = ri.activityInfo.packageName
                if (pkg == context.packageName) return@mapNotNull null
                val label = ri.loadLabel(pm).toString()
                AppEntry(
                    label = label,
                    packageName = pkg,
                    isPinned = pinnedPackages.contains(pkg),
                    isHidden = hiddenPackages.contains(pkg)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }

        // If user hasn't pinned anything yet, auto-pin first 6 essential apps
        if (pinnedPackages.isEmpty() && loaded.isNotEmpty()) {
            val defaults = loaded.take(6).map { it.packageName }.toSet()
            pinnedPackages = defaults
            prefs.edit().putStringSet("pinned_pkgs", defaults).apply()
        }
        installedApps = loaded
    }

    // Live Clock & Battery Receiver
    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }
    var batteryPct by remember { mutableStateOf(86) }

    LaunchedEffect(use24Hour) {
        while (true) {
            val now = Date()
            val timePattern = if (use24Hour) "HH:mm" else "hh:mm a"
            currentTimeStr = SimpleDateFormat(timePattern, Locale.getDefault()).format(now)
            currentDateStr = SimpleDateFormat("EEE · dd MMM", Locale.getDefault()).format(now).uppercase()

            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            if (bm != null) {
                batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(1, 100)
            }
            delay(10_000)
        }
    }

    // Zen Focus Countdown
    LaunchedEffect(zenActive) {
        while (zenActive && zenSeconds > 0) {
            delay(1000)
            zenSeconds -= 1
            if (zenSeconds <= 0) zenActive = false
        }
    }

    val accentColor = remember(accentHex) {
        when (accentHex) {
            "#FFFFFF" -> Color.White
            "#D71921" -> Color(0xFFD71921)
            else -> Color(0xFFFF3B00) // CMF International Orange
        }
    }
    val selectedFont = remember(fontId) { PrebuiltFont.fromId(fontId).family }
    val textAlign = remember(textAlignMode) {
        when (textAlignMode) {
            "center" -> TextAlign.Center
            "right" -> TextAlign.End
            else -> TextAlign.Start
        }
    }

    // Helper for subtle haptics on Niagara wave scrubbing
    fun tickHaptic() {
        val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vib?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
    }

    fun saveWidgetList(updated: List<String>) {
        activeWidgets = updated
        prefs.edit().putString("widgets_order", updated.joinToString(",")).apply()
    }

    // System Back Button Handling (Android 14 3-Button & Gesture Navigation)
    val backDispatcher = (context as? ComponentActivity)?.onBackPressedDispatcher
    DisposableEffect(backDispatcher, currentView, editMode) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    oledSleepActive -> oledSleepActive = false
                    editMode -> editMode = false
                    currentView != LauncherView.HOME -> currentView = LauncherView.HOME
                }
            }
        }
        backDispatcher?.addCallback(callback)
        onDispose { callback.remove() }
    }

    // Always-On OLED Sleep Overlay
    if (oledSleepActive) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { oledSleepActive = false },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentTimeStr,
                    color = Color(0xFF404040),
                    fontSize = 42.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CMF OLED LOCK · TAP TO WAKE",
                    color = Color(0xFF262626),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        return
    }

    // Root Surface with OLED Wallpaper Engine + System Navigation Bar Insets
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .pointerInput(doubleTapSleep, currentView) {
                if (doubleTapSleep && currentView == LauncherView.HOME) {
                    detectTapGestures(
                        onDoubleTap = { oledSleepActive = true },
                        onLongPress = {
                            editMode = !editMode
                            tickHaptic()
                        }
                    )
                }
            }
    ) {
        // Wallpaper Background Canvas (OLED Black / Nothing Dot-Matrix / CMF Blueprint / Glow)
        OledWallpaperCanvas(preset = wallpaperPreset, accentColor = accentColor, dimPercent = wallpaperDimPercent)

        Column(modifier = Modifier.fillMaxSize()) {
            // CUSTOM N-TEXT MINIMAL TOP BAR (Replaces the hidden default Android notification bar)
            if (showCustomTopBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = currentTimeStr,
                            color = Color(0xFFD4D4D4),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                        if (zenActive) {
                            Text(
                                text = "ZEN",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .background(accentColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "5G",
                            color = Color(0xFF737373),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$batteryPct%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .border(1.dp, Color(0xFF404040), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // EDIT MODE BANNER
            if (editMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .border(1.dp, Color(0xFF404040), RoundedCornerShape(8.dp))
                        .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EDIT MODE: TAP [×] TO REMOVE WIDGETS",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "DONE",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .clickable { editMode = false }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // ================================================================
            // VIEW SWITCHER (HOME / DRAWER / WIDGETS / SETTINGS)
            // ================================================================
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (currentView) {
                    LauncherView.HOME -> {
                        HomeScreenView(
                            activeWidgets = activeWidgets,
                            editMode = editMode,
                            currentTimeStr = currentTimeStr,
                            currentDateStr = currentDateStr,
                            batteryPct = batteryPct,
                            zenActive = zenActive,
                            zenSeconds = zenSeconds,
                            quickNote = quickNote,
                            accentColor = accentColor,
                            selectedFont = selectedFont,
                            textAlign = textAlign,
                            textSizeSp = textSizeSp,
                            niagaraWaveMode = niagaraWaveMode,
                            pinnedApps = installedApps.filter { it.isPinned && !it.isHidden },
                            totalAppsCount = installedApps.count { !it.isHidden },
                            onToggle24h = {
                                use24Hour = !use24Hour
                                prefs.edit().putBoolean("use_24h", use24Hour).apply()
                            },
                            onToggleZen = {
                                zenActive = !zenActive
                                if (!zenActive) zenSeconds = 1500
                            },
                            onRemoveWidget = { id ->
                                saveWidgetList(activeWidgets.filter { it != id })
                            },
                            onLaunchApp = { pkg ->
                                context.packageManager.getLaunchIntentForPackage(pkg)?.let {
                                    context.startActivity(it)
                                }
                            },
                            onUnpinApp = { pkg ->
                                pinnedPackages = pinnedPackages - pkg
                                prefs.edit().putStringSet("pinned_pkgs", pinnedPackages).apply()
                            },
                            onOpenDrawer = { currentView = LauncherView.DRAWER },
                            onOpenWidgets = { currentView = LauncherView.WIDGETS },
                            onOpenSettings = { currentView = LauncherView.SETTINGS },
                            onWaveTick = { tickHaptic() }
                        )
                    }

                    LauncherView.DRAWER -> {
                        AppDrawerView(
                            apps = installedApps,
                            selectedFont = selectedFont,
                            accentColor = accentColor,
                            niagaraWaveMode = niagaraWaveMode,
                            onLaunchApp = { pkg ->
                                context.packageManager.getLaunchIntentForPackage(pkg)?.let {
                                    context.startActivity(it)
                                }
                                currentView = LauncherView.HOME
                            },
                            onTogglePin = { pkg ->
                                pinnedPackages = if (pinnedPackages.contains(pkg)) {
                                    pinnedPackages - pkg
                                } else {
                                    pinnedPackages + pkg
                                }
                                prefs.edit().putStringSet("pinned_pkgs", pinnedPackages).apply()
                            },
                            onToggleHide = { pkg ->
                                hiddenPackages = if (hiddenPackages.contains(pkg)) {
                                    hiddenPackages - pkg
                                } else {
                                    hiddenPackages + pkg
                                }
                                prefs.edit().putStringSet("hidden_pkgs", hiddenPackages).apply()
                            },
                            onClose = { currentView = LauncherView.HOME },
                            onWaveTick = { tickHaptic() }
                        )
                    }

                    LauncherView.WIDGETS -> {
                        WidgetsManagerView(
                            activeWidgets = activeWidgets,
                            accentColor = accentColor,
                            onUpdateWidgets = { saveWidgetList(it) },
                            onBack = { currentView = LauncherView.HOME }
                        )
                    }

                    LauncherView.SETTINGS -> {
                        LauncherSettingsView(
                            fontId = fontId,
                            textAlignMode = textAlignMode,
                            accentHex = accentHex,
                            textSizeSp = textSizeSp,
                            use24Hour = use24Hour,
                            showCustomTopBar = showCustomTopBar,
                            hideAndroidBar = hideAndroidBar,
                            niagaraWaveMode = niagaraWaveMode,
                            wallpaperPreset = wallpaperPreset,
                            wallpaperDimPercent = wallpaperDimPercent,
                            autoKeyboard = autoKeyboard,
                            doubleTapSleep = doubleTapSleep,
                            onChangeFont = {
                                fontId = it
                                prefs.edit().putString("font_id", it).apply()
                            },
                            onChangeAlign = {
                                textAlignMode = it
                                prefs.edit().putString("align", it).apply()
                            },
                            onChangeAccent = {
                                accentHex = it
                                prefs.edit().putString("accent", it).apply()
                            },
                            onChangeTextSize = {
                                textSizeSp = it
                                prefs.edit().putInt("text_size", it).apply()
                            },
                            onToggle24h = {
                                use24Hour = !use24Hour
                                prefs.edit().putBoolean("use_24h", use24Hour).apply()
                            },
                            onToggleCustomTopBar = {
                                showCustomTopBar = !showCustomTopBar
                                prefs.edit().putBoolean("show_custom_bar", showCustomTopBar).apply()
                            },
                            onToggleAndroidBar = {
                                val next = !hideAndroidBar
                                hideAndroidBar = next
                                onToggleAndroidStatusBar(next)
                            },
                            onChangeWaveMode = {
                                niagaraWaveMode = it
                                prefs.edit().putString("wave_mode", it).apply()
                            },
                            onChangeWallpaperPreset = {
                                wallpaperPreset = it
                                prefs.edit().putString("wallpaper", it).apply()
                            },
                            onOpenSystemWallpaperPicker = onOpenSystemWallpaperPicker,
                            onRequestDefaultHomeRole = onRequestDefaultHomeRole,
                            onOpenWidgets = { currentView = LauncherView.WIDGETS },
                            onToggleAutoKeyboard = {
                                autoKeyboard = !autoKeyboard
                                prefs.edit().putBoolean("auto_kb", autoKeyboard).apply()
                            },
                            onToggleDoubleTapSleep = {
                                doubleTapSleep = !doubleTapSleep
                                prefs.edit().putBoolean("dbl_tap_sleep", doubleTapSleep).apply()
                            },
                            onBack = { currentView = LauncherView.HOME }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// HOME SCREEN WITH REMOVABLE WIDGETS & NIAGARA WAVE TRAVERSAL
// ============================================================================
@Composable
fun HomeScreenView(
    activeWidgets: List<String>,
    editMode: Boolean,
    currentTimeStr: String,
    currentDateStr: String,
    batteryPct: Int,
    zenActive: Boolean,
    zenSeconds: Int,
    quickNote: String,
    accentColor: Color,
    selectedFont: FontFamily,
    textAlign: TextAlign,
    textSizeSp: Int,
    niagaraWaveMode: String,
    pinnedApps: List<AppEntry>,
    totalAppsCount: Int,
    onToggle24h: () -> Unit,
    onToggleZen: () -> Unit,
    onRemoveWidget: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    onUnpinApp: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenWidgets: () -> Unit,
    onOpenSettings: () -> Unit,
    onWaveTick: () -> Unit
) {
    var touchY by remember { mutableStateOf<Float?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. REMOVABLE WIDGETS STACK
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            activeWidgets.forEach { widgetId ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (editMode) {
                                Modifier
                                    .border(1.dp, Color(0xFF404040), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            } else Modifier
                        )
                ) {
                    Column {
                        when (widgetId) {
                            "clock" -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggle24h() }
                                ) {
                                    Text(
                                        text = currentTimeStr,
                                        color = Color.White,
                                        fontSize = 38.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$currentDateStr · CMF PHONE 1",
                                        color = Color(0xFF8A8A8A),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            "weather" -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(accentColor, CircleShape)
                                        )
                                        Text(
                                            text = "28°C · CLEAR SKY",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Text(
                                        text = "AQI 42 · GOOD",
                                        color = Color(0xFF8A8A8A),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            "telemetry" -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "BAT $batteryPct% · OLED PURE",
                                        color = Color(0xFFA3A3A3),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "RAM 8.4 MB",
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            "zen" -> {
                                val mm = (zenSeconds / 60).toString().padStart(2, '0')
                                val ss = (zenSeconds % 60).toString().padStart(2, '0')
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF262626), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ZEN FOCUS · $mm:$ss",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (zenActive) "STOP" else "START",
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { onToggleZen() }
                                    )
                                }
                            }
                            "scratchpad" -> {
                                Text(
                                    text = "> $quickNote",
                                    color = Color(0xFFD4D4D4),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (editMode) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "[× REMOVE WIDGET]",
                                    color = Color(0xFFEF4444),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.clickable { onRemoveWidget(widgetId) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. PINNED HOME APPS WITH NIAGARA WAVE TRAVERSING ANIMATION
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                pinnedApps.forEach { app ->
                    var itemCenterY by remember { mutableStateOf(0f) }
                    val waveOffset = remember(touchY, itemCenterY, niagaraWaveMode) {
                        if (touchY == null || niagaraWaveMode == "off") 0f
                        else {
                            val dist = abs(touchY!! - itemCenterY)
                            val radius = 240f
                            if (dist < radius) {
                                val factor = cos((dist / radius) * (PI / 2)).toFloat()
                                val maxShift = if (niagaraWaveMode == "full") 48f else 22f
                                factor * factor * maxShift
                            } else 0f
                        }
                    }
                    val animatedShift by animateFloatAsState(targetValue = waveOffset, animationSpec = tween(90))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                itemCenterY = coords.positionInRoot().y + coords.size.height / 2f
                            }
                            .graphicsLayer {
                                translationX = animatedShift
                                val scale = 1f + (animatedShift / 320f)
                                scaleX = scale
                                scaleY = scale
                            }
                            .clickable { onLaunchApp(app.packageName) }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = when (textAlign) {
                            TextAlign.Center -> Arrangement.Center
                            TextAlign.End -> Arrangement.End
                            else -> Arrangement.SpaceBetween
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.label,
                            color = if (animatedShift > 24f) accentColor else Color.White,
                            fontSize = textSizeSp.sp,
                            fontFamily = selectedFont,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (editMode) {
                            Text(
                                text = "unpin",
                                color = Color(0xFF8A8A8A),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .clickable { onUnpinApp(app.packageName) }
                            )
                        }
                    }
                }
            }

            // Right-edge Niagara Wave Sensor Strip on Home Screen
            if (niagaraWaveMode != "off") {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight(0.65f)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = { offset ->
                                    touchY = offset.y
                                    onWaveTick()
                                },
                                onVerticalDrag = { change, _ ->
                                    touchY = change.position.y
                                },
                                onDragEnd = { touchY = null },
                                onDragCancel = { touchY = null }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        repeat(9) {
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .background(Color(0xFF404040), CircleShape)
                            )
                        }
                    }
                }
            }
        }

        // 3. BOTTOM MINIMAL DOCK
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "/ search",
                color = Color(0xFF737373),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable { onOpenDrawer() }
            )
            Text(
                text = "all apps ($totalAppsCount) ↑",
                color = Color(0xFF9E9E9E),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable { onOpenDrawer() }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "+ widgets",
                    color = Color(0xFF737373),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { onOpenWidgets() }
                )
                Text(
                    text = "⚙ settings",
                    color = Color(0xFF737373),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { onOpenSettings() }
                )
            }
        }
    }
}

// ============================================================================
// APP DRAWER WITH NIAGARA A-Z WAVE SCRUBBER & INSTANT SEARCH
// ============================================================================
@Composable
fun AppDrawerView(
    apps: List<AppEntry>,
    selectedFont: FontFamily,
    accentColor: Color,
    niagaraWaveMode: String,
    onLaunchApp: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleHide: (String) -> Unit,
    onClose: () -> Unit,
    onWaveTick: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    var scrubY by remember { mutableStateOf<Float?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val filteredApps = remember(apps, query) {
        apps.filter { !it.isHidden && (query.isBlank() || it.label.contains(query, ignoreCase = true)) }
    }

    val letters = remember { ('A'..'Z').toList() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "/ ",
                    color = accentColor,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search apps...",
                                color = Color(0xFF525252),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        inner()
                    }
                )
                Text(
                    text = "ESC",
                    color = Color(0xFF737373),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable {
                        if (query.isNotEmpty()) query = "" else onClose()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App List + A-Z Wave Rail
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLaunchApp(app.packageName) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.label,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontFamily = selectedFont,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = if (app.isPinned) "★ pinned" else "☆ pin",
                                    color = if (app.isPinned) accentColor else Color(0xFF525252),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.clickable { onTogglePin(app.packageName) }
                                )
                                Text(
                                    text = "hide",
                                    color = Color(0xFF525252),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.clickable { onToggleHide(app.packageName) }
                                )
                            }
                        }
                    }
                }

                // Niagara A-Z Wave Scrubber Rail
                Column(
                    modifier = Modifier
                        .width(26.dp)
                        .fillMaxHeight()
                        .pointerInput(filteredApps) {
                            detectVerticalDragGestures(
                                onDragStart = { offset ->
                                    scrubY = offset.y
                                    onWaveTick()
                                },
                                onVerticalDrag = { change, _ ->
                                    val y = change.position.y.coerceIn(0f, size.height.toFloat())
                                    scrubY = y
                                    val idx = ((y / size.height.toFloat()) * letters.size)
                                        .toInt()
                                        .coerceIn(0, letters.lastIndex)
                                    val letter = letters[idx]
                                    if (activeLetter != letter) {
                                        activeLetter = letter
                                        onWaveTick()
                                        val targetIdx = filteredApps.indexOfFirst {
                                            it.label.startsWith(letter, ignoreCase = true)
                                        }
                                        if (targetIdx >= 0) {
                                            scope.launch { listState.scrollToItem(targetIdx) }
                                        }
                                    }
                                },
                                onDragEnd = {
                                    scrubY = null
                                    activeLetter = null
                                },
                                onDragCancel = {
                                    scrubY = null
                                    activeLetter = null
                                }
                            )
                        },
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    letters.forEachIndexed { idx, ch ->
                        val isCurrent = activeLetter == ch
                        val shiftX = if (scrubY != null && niagaraWaveMode != "off") {
                            val activeIdx = letters.indexOf(activeLetter ?: 'A')
                            val dist = abs(idx - activeIdx)
                            if (dist <= 4) {
                                -cos((dist / 4f) * (PI / 2)).toFloat() * 24f
                            } else 0f
                        } else 0f

                        Text(
                            text = ch.toString(),
                            color = if (isCurrent) accentColor else Color(0xFF737373),
                            fontSize = if (isCurrent) 12.sp else 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.graphicsLayer { translationX = shiftX }
                        )
                    }
                }
            }
        }

        // Floating Niagara Wave Letter Bubble
        if (activeLetter != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 48.dp)
                    .size(52.dp)
                    .border(1.dp, accentColor, RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A0A0A), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeLetter.toString(),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// MODULAR WIDGETS MANAGER (IN-PHONE)
// ============================================================================
@Composable
fun WidgetsManagerView(
    activeWidgets: List<String>,
    accentColor: Color,
    onUpdateWidgets: (List<String>) -> Unit,
    onBack: () -> Unit
) {
    val allWidgets = listOf(
        "clock" to "Dot-Matrix Clock & Date",
        "weather" to "Minimal Weather & AQI Pill",
        "telemetry" to "CMF Battery & RAM Telemetry",
        "zen" to "Zen Focus Timer (25m)",
        "scratchpad" to "One-Line Terminal Scratchpad"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MODULAR OLED WIDGETS",
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "← BACK",
                color = accentColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable { onBack() }
            )
        }

        allWidgets.forEach { (id, title) ->
            val enabled = activeWidgets.contains(id)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (enabled) "ACTIVE [× REMOVE]" else "+ ADD TO HOME",
                    color = if (enabled) accentColor else Color(0xFF8A8A8A),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        val next = if (enabled) activeWidgets - id else activeWidgets + id
                        onUpdateWidgets(next)
                    }
                )
            }
        }
    }
}

// ============================================================================
// IN-PHONE LAUNCHER SETTINGS SCREEN (100% SELF-CONTAINED IN APK)
// ============================================================================
@Composable
fun LauncherSettingsView(
    fontId: String,
    textAlignMode: String,
    accentHex: String,
    textSizeSp: Int,
    use24Hour: Boolean,
    showCustomTopBar: Boolean,
    hideAndroidBar: Boolean,
    niagaraWaveMode: String,
    wallpaperPreset: String,
    wallpaperDimPercent: Int,
    autoKeyboard: Boolean,
    doubleTapSleep: Boolean,
    onChangeFont: (String) -> Unit,
    onChangeAlign: (String) -> Unit,
    onChangeAccent: (String) -> Unit,
    onChangeTextSize: (Int) -> Unit,
    onToggle24h: () -> Unit,
    onToggleCustomTopBar: () -> Unit,
    onToggleAndroidBar: () -> Unit,
    onChangeWaveMode: (String) -> Unit,
    onChangeWallpaperPreset: (String) -> Unit,
    onOpenSystemWallpaperPicker: () -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onOpenWidgets: () -> Unit,
    onToggleAutoKeyboard: () -> Unit,
    onToggleDoubleTapSleep: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "N-TEXT // LAUNCHER SETTINGS",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "CMF Phone 1 · Nothing OS 2.6 · Android 14",
                        color = Color(0xFF737373),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "DONE",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .clickable { onBack() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // 1. Choose Default Home Launcher
        item {
            SettingCard(
                title = "Default Home Launcher",
                subtitle = "Set N-TEXT as your default Android 14 Home App",
                actionLabel = "CHOOSE HOME",
                onClick = onRequestDefaultHomeRole
            )
        }

        // 2. Hide Default Android Notification Bar
        item {
            SettingCard(
                title = "Hide Default Android Notification Bar",
                subtitle = "Keep only the custom N-TEXT minimal top bar",
                actionLabel = if (hideAndroidBar) "HIDDEN (ON)" else "SHOWN (OFF)",
                onClick = onToggleAndroidBar
            )
        }

        // 3. Wallpaper Presets + System Wallpaper Picker
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("OLED Wallpaper Engine", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text(
                        text = "SYSTEM GALLERY →",
                        color = Color(0xFFFF3B00),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { onOpenSystemWallpaperPicker() }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("oled" to "OLED", "dots" to "Dots", "blueprint" to "CMF", "mesh" to "Glow").forEach { (id, label) ->
                        val active = wallpaperPreset == id
                        Text(
                            text = label,
                            color = if (active) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(if (active) Color.White else Color(0xFF141414), RoundedCornerShape(6.dp))
                                .clickable { onChangeWallpaperPreset(id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 4. Prebuilt Font Style Selector (Nothing Dot-Matrix, Tech Mono, Space Mono, etc.)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Prebuilt Font Style", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                PrebuiltFont.values().forEach { f ->
                    val selected = fontId == f.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChangeFont(f.id) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(f.displayName, color = Color.White, fontSize = 12.sp, fontFamily = f.family)
                        Text(
                            text = if (selected) "● ACTIVE" else "○",
                            color = if (selected) Color(0xFFFF3B00) else Color(0xFF525252),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 5. Niagara Wave Traversal Animation
        item {
            SettingCard(
                title = "Niagara Wave List Animation",
                subtitle = "Curved horizontal wave when scrubbing app lists",
                actionLabel = niagaraWaveMode.uppercase(),
                onClick = {
                    val next = when (niagaraWaveMode) {
                        "full" -> "subtle"
                        "subtle" -> "off"
                        else -> "full"
                    }
                    onChangeWaveMode(next)
                }
            )
        }

        // 6. Text Alignment & Accent Color
        item {
            SettingCard(
                title = "Home Text Alignment",
                subtitle = "Tap to cycle Left / Center / Right",
                actionLabel = textAlignMode.uppercase(),
                onClick = {
                    val next = when (textAlignMode) {
                        "left" -> "center"
                        "center" -> "right"
                        else -> "left"
                    }
                    onChangeAlign(next)
                }
            )
        }

        item {
            SettingCard(
                title = "OLED Accent Color",
                subtitle = "CMF Orange (#FF3B00), Pure B&W (#FFFFFF), or Nothing Red",
                actionLabel = accentHex,
                onClick = {
                    val next = when (accentHex) {
                        "#FF3B00" -> "#FFFFFF"
                        "#FFFFFF" -> "#D71921"
                        else -> "#FF3B00"
                    }
                    onChangeAccent(next)
                }
            )
        }

        // 7. Manage Home Screen Widgets
        item {
            SettingCard(
                title = "Removable Home Widgets",
                subtitle = "Add, reorder, or remove all widgets",
                actionLabel = "MANAGE →",
                onClick = onOpenWidgets
            )
        }

        // 8. Double-Tap OLED Sleep
        item {
            SettingCard(
                title = "Double-Tap OLED Sleep",
                subtitle = "Double-tap empty black area to lock screen",
                actionLabel = if (doubleTapSleep) "ON" else "OFF",
                onClick = onToggleDoubleTapSleep
            )
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(text = title, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Text(text = subtitle, color = Color(0xFF737373), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        Text(
            text = actionLabel,
            color = Color.Black,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

// ============================================================================
// OLED WALLPAPER ENGINE CANVAS
// ============================================================================
@Composable
fun OledWallpaperCanvas(preset: String, accentColor: Color, dimPercent: Int) {
    if (preset == "oled") return
    Canvas(modifier = Modifier.fillMaxSize()) {
        when (preset) {
            "dots" -> {
                val step = 18.dp.toPx()
                var x = step
                while (x < size.width) {
                    var y = step
                    while (y < size.height) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.14f),
                            radius = 1.3.dp.toPx(),
                            center = Offset(x, y)
                        )
                        y += step
                    }
                    x += step
                }
            }
            "blueprint" -> {
                val step = 32.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = accentColor.copy(alpha = 0.08f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = accentColor.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += step
                }
            }
            "mesh" -> {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.8f),
                        radius = size.width * 0.7f
                    )
                )
            }
        }
    }
}
