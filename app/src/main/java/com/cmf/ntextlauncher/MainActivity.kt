package com.cmf.ntextlauncher

import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

// ============================================================================
// EMBEDDED NOTHING BLOCKY PIXEL TRUETYPE FONT (SILKSCREEN 5-ROW PIXEL GRID)
// Self-contained inside MainActivity.kt so even single-file GitHub updates
// compile with zero external resource dependencies!
// ============================================================================
private const val NOTHING_BLOCKY_TTF_B64 =
    "AAEAAAAKAIAAAwAgT1MvMkdYRs4AAAEoAAAAYGNtYXABeAIcAAAC0AAAAGRnbHlmEUBRuQAAA9wAABAMaGVhZDBqaXAAAACsAAAA" +
    "NmhoZWEGQgOaAAAA5AAAACRobXR42YAAAAAAAYgAAAFIbG9jYaRjqHsAAAM0AAAAqG1heHAAWwAmAAABCAAAACBuYW1lvyYQyQAA" +
    "E+gAAAFZcG9zdEimK2IAABVEAAACeQABAAAAAQAAeAiE2V8PPPUAAwPoAAAAAOcGExwAAAAA5wYTHAAAAAADSAJYAAAAAwACAAAA" +
    "AAAAAAEAAAL4/xAAAAPAAAAAeANIAAEAAAAAAAAAAAAAAAAAAABRAAEAAABTACQABwAAAAAAAgAAAAAAAAAAAAAAAAAAAAAAAwKw" +
    "AZAABQAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAPz8/PwAAACAAegL4/xAAAAL4APAA" +
    "AAAAAAAAAAJYAlgAAAAgAAACWAAAAeAAAAFoAAAB4AAAAtAAAALQAAAC0AAAAWgAAAHgAAAB4AAAAtAAAAFoAAACWAAAAWgAAALQ" +
    "AAAC0AAAAlgAAALQAAAC0AAAAtAAAALQAAAC0AAAAtAAAALQAAAC0AAAAWgAAALQAAAC0AAAAtAAAALQAAAC0AAAAtAAAALQAAAC" +
    "0AAAAtAAAALQAAACWAAAAtAAAALQAAAC0AAAA8AAAANIAAAC0AAAAtAAAALQAAAC0AAAAtAAAANIAAAC0AAAAtAAAANIAAAC0AAA" +
    "AtAAAALQAAAB4AAAAeAAAALQAAAC0AAAAtAAAALQAAAC0AAAAtAAAALQAAAC0AAAAtAAAAJYAAAC0AAAAtAAAALQAAADwAAAA0gA" +
    "AALQAAAC0AAAAtAAAALQAAAC0AAAA0gAAALQAAAC0AAAA0gAAALQAAAAAAAAAAAAAgAAAAMAAAAUAAMAAQAAABQABABQAAAAEAAQ" +
    "AAMAAAAjACkAOgBbAF0AXwB6//8AAAAgACUAKwA/AF0AXwBh////4f/g/9//2//a/9n/2AABAAAAAAAAAAAAAAAAAAAAAAAAAAwA" +
    "DAAeADAAWgCKALsAxwDcAPEBBQEVASIBLgFIAWMBeAGUAbIByQHjAgQCHAJDAmECcwKPArECygLoAv8DFgMtA0EDWwNxA4cDnwPB" +
    "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" +
    "ETM1IRUzAlh48PD+mHh4AWh4eAFo/ph4eAFoeHgAAAEAAAAAAlgCWAANAAABIxUjFSMRIRUjFTM1MwJYePDwAeDw8HgBaHjwAlh4" +
    "eHgAAAEAAAAAAlgCWAAPAAAhITUjETM1IRUjETM1MzUzAlj+IHh4AWjweHh4eAFoeHj+mHjwAAACAAAAAAJYAlgADwATAAAhIzUj" +
    "NSMVIxEhFSMVMxUzNSM1MwJYeHh48AHg8PB4eHh4ePACWHh48PB4AAABAAAAAAJYAlgAEwAANzUjNTM1IRUhFTMVMxUjFSE1ITV4" +
    "eHgB4P6Y8Hh4/iABaPB4eHh4eHh4eHh4AAABAAAAAALQAlgABwAAETUhFSMRIxEC0PDwAeB4eP4gAeAAAQAAAAACWAJYAAkAAAEz" +
    "ESE1IxEzETMB4Hj+IHjw8AJY/ah4AeD+IAAAAQAAAAACWAJYAA0AAAEzESMVIzUjNSMRMxEzAeB4ePB4ePDwAlj+IHh4eAFo/pgA" +
    "AQAAAAAC0AJYABMAACEjNSMVIzUjETMRMzUzFTMRMxEjAlh4ePB48Hh4eHh4eHh4AeD+mHh4AWj+IAAAAQAAAAACWAJYABcAACEj" +
    "NSMVIzUzNTM1IzUjNTMVMzUzFSMVMwJYePDweHh4ePDweHh4eHh4eHh4eHh48HgAAAEAAAAAAlgCWAANAAABMxEjFSM1IzUjNTMV" +
    "MwHgeHjweHjw8AJY/pjw8Hjw8AABAAAAAAJYAlgADwAANzUzNSE1IRUjFSMVIRUhNXjw/pgCWPB4AWj9qPB4eHjweHh48AAAAAAM" +
    "AJYAAQAAAAAAAQANAAAAAQAAAAAAAgAHAA0AAQAAAAAAAwAVABQAAQAAAAAABAAVACkAAQAAAAAABQADAD4AAQAAAAAABgAVABQA" +
    "AwABBAkAAQAaAEEAAwABBAkAAgAOAFsAAwABBAkAAwAqAGkAAwABBAkABAAqAJMAAwABBAkABQAGAL0AAwABBAkABgAqAGlOb3Ro" +
    "aW5nQmxvY2t5UmVndWxhck5vdGhpbmdCbG9ja3ktUmVndWxhck5vdGhpbmdCbG9ja3kgUmVndWxhcjEuMABOAG8AdABoAGkAbgBn" +
    "AEIAbABvAGMAawB5AFIAZQBnAHUAbABhAHIATgBvAHQAaABpAG4AZwBCAGwAbwBjAGsAeQAtAFIAZQBnAHUAbABhAHIATgBvAHQA" +
    "aABpAG4AZwBCAGwAbwBjAGsAeQAgAFIAZQBnAHUAbABhAHIAMQAuADAAAAAAAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
    "AABTAAABAgEDAQQBBQEGAQcBCAEJAQoBCwEMAQ0BDgEPARABEQESARMBFAEVARYBFwEYARkBGgEbARwBHQEeAR8BIAEhASIBIwEk" +
    "ASUBJgEnASgBKQEqASsBLAEtAS4BLwEwATEBMgEzATQBNQE2ATcBOAE5AToBOwE8AT0BPgE/AUABQQFCAUMBRAFFAUYBRwFIAUkB" +
    "SgFLAUwBTQFOAU8BUAFRAVIBUwRnXzMyBGdfMzMEZ18zNARnXzM1BGdfMzcEZ18zOARnXzM5BGdfNDAEZ180MQRnXzQzBGdfNDQE" +
    "Z180NQRnXzQ2BGdfNDcEZ180OARnXzQ5BGdfNTAEZ181MQRnXzUyBGdfNTMEZ181NARnXzU1BGdfNTYEZ181NwRnXzU4BGdfNjME" +
    "Z182NARnXzY1BGdfNjYEZ182NwRnXzY4BGdfNjkEZ183MARnXzcxBGdfNzIEZ183MwRnXzc0BGdfNzUEZ183NgRnXzc3BGdfNzgE" +
    "Z183OQRnXzgwBGdfODEEZ184MgRnXzgzBGdfODQEZ184NQRnXzg2BGdfODcEZ184OARnXzg5BGdfOTAEZ185MQRnXzkzBGdfOTUE" +
    "Z185NwRnXzk4BGdfOTkFZ18xMDAFZ18xMDEFZ18xMDIFZ18xMDMFZ18xMDQFZ18xMDUFZ18xMDYFZ18xMDcFZ18xMDgFZ18xMDkF" +
    "Z18xMTAFZ18xMTEFZ18xMTIFZ18xMTMFZ18xMTQFZ18xMTUFZ18xMTYFZ18xMTcFZ18xMTgFZ18xMTkFZ18xMjAFZ18xMjEFZ18x" +
    "MjIAAAA="

private var cachedBlockyFontFamily: FontFamily? = null

fun getNothingBlockyFontFamily(context: Context): FontFamily {
    cachedBlockyFontFamily?.let { return it }
    return try {
        val ttfFile = File(context.cacheDir, "nothing_blocky_v2.ttf")
        if (!ttfFile.exists() || ttfFile.length() == 0L) {
            val bytes = Base64.decode(NOTHING_BLOCKY_TTF_B64, Base64.DEFAULT)
            ttfFile.writeBytes(bytes)
        }
        val tf = Typeface.createFromFile(ttfFile)
        FontFamily(tf).also { cachedBlockyFontFamily = it }
    } catch (e: Exception) {
        FontFamily.Monospace
    }
}

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
    HOME_AND_DRAWER, WIDGETS, SETTINGS
}

enum class PrebuiltFont(val id: String, val displayName: String, val uppercaseHome: Boolean) {
    NDOT("dot", "Nothing Blocky Pixel (Default)", true),
    DOT_MATRIX("ndot", "Nothing Dot-Matrix", true),
    TECH_MONO("mono", "JetBrains Tech Mono", false),
    SPACE_MONO("spacemono", "Space Mono Industrial", false),
    CLEAN_SANS("sans", "Clean Geometric Sans", false),
    EDITORIAL("editorial", "Minimal Editorial Serif", false);

    fun resolveFontFamily(blockyFamily: FontFamily): FontFamily = when (this) {
        NDOT, DOT_MATRIX -> blockyFamily
        TECH_MONO, SPACE_MONO -> FontFamily.Monospace
        CLEAN_SANS -> FontFamily.SansSerif
        EDITORIAL -> FontFamily.Serif
    }

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
    private var packageRefreshTrigger by mutableStateOf(0)

    private val packageBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            packageRefreshTrigger++
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("cmf_ntext_prefs", Context.MODE_PRIVATE)
        hideDefaultAndroidStatusBar = prefs.getBoolean("hide_android_bar", true)

        // Listen for app installs/uninstalls so the app list updates immediately
        val pkgFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(packageBroadcastReceiver, pkgFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(packageBroadcastReceiver, pkgFilter)
        }

        // Configure edge-to-edge WindowInsets for Android 14 System Navigation
        WindowCompat.setDecorFitsSystemWindows(window, false)
        applySystemStatusBarVisibility(hideDefaultAndroidStatusBar)

        setContent {
            NTextLauncherApp(
                prefs = prefs,
                packageRefreshTrigger = packageRefreshTrigger,
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
        packageRefreshTrigger++
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(packageBroadcastReceiver)
        } catch (_: Exception) {
        }
    }

    private fun applySystemStatusBarVisibility(hideStockBar: Boolean) {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (hideStockBar) {
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
    packageRefreshTrigger: Int,
    onToggleAndroidStatusBar: (Boolean) -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onOpenSystemWallpaperPicker: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val blockyFontFamily = remember { getNothingBlockyFontFamily(context) }

    // Navigation & Edit Mode State
    var currentView by remember { mutableStateOf(LauncherView.HOME_AND_DRAWER) }
    // Smooth continuous App Drawer progress: 0f = Home Screen, 1f = App Drawer Open
    val drawerProgress = remember { Animatable(0f) }
    val isDrawerOpen by remember { derivedStateOf { drawerProgress.value > 0.5f } }

    var editMode by remember { mutableStateOf(false) }
    var oledSleepActive by remember { mutableStateOf(false) }

    // Long-press App Context Menu Target: Pair(AppEntry, isFromHomeScreen)
    var contextMenuTarget by remember { mutableStateOf<Pair<AppEntry, Boolean>?>(null) }

    // Persisted Preferences (Default font = "dot" -> Nothing Blocky Pixel Font)
    var fontId by remember { mutableStateOf(prefs.getString("font_id", "dot") ?: "dot") }
    var textAlignMode by remember { mutableStateOf(prefs.getString("align", "left") ?: "left") }
    var accentHex by remember { mutableStateOf(prefs.getString("accent", "#FF3B00") ?: "#FF3B00") }
    var textSizeSp by remember { mutableStateOf(prefs.getInt("text_size", 20)) }
    var use24Hour by remember { mutableStateOf(prefs.getBoolean("use_24h", true)) }
    var hideAndroidBar by remember { mutableStateOf(prefs.getBoolean("hide_android_bar", true)) }
    var niagaraWaveMode by remember { mutableStateOf(prefs.getString("wave_mode", "full") ?: "full") }
    var wallpaperPreset by remember { mutableStateOf(prefs.getString("wallpaper", "oled") ?: "oled") }
    var wallpaperDimPercent by remember { mutableStateOf(prefs.getInt("wp_dim", 50)) }
    var autoKeyboard by remember { mutableStateOf(prefs.getBoolean("auto_kb", false)) }
    var doubleTapSleep by remember { mutableStateOf(prefs.getBoolean("dbl_tap_sleep", true)) }

    // Removable Widgets Order & Visibility (RAM/telemetry widget removed; battery bar is inside clock widget)
    var activeWidgets by remember {
        val saved = prefs.getString("widgets_order", "clock,weather") ?: "clock,weather"
        mutableStateOf(saved.split(",").filter { it.isNotBlank() && it != "telemetry" })
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
                    "com.brave.browser",
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
    LaunchedEffect(pinnedPackages, hiddenPackages, packageRefreshTrigger) {
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
    var isCharging by remember { mutableStateOf(false) }

    LaunchedEffect(use24Hour) {
        while (true) {
            val now = Date()
            val timePattern = if (use24Hour) "HH:mm" else "hh:mm a"
            currentTimeStr = SimpleDateFormat(timePattern, Locale.getDefault()).format(now)
            currentDateStr = SimpleDateFormat("EEE · dd MMM", Locale.getDefault()).format(now).uppercase(Locale.getDefault())

            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            if (bm != null) {
                batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(1, 100)
                isCharging = bm.isCharging
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
    val prebuiltFont = remember(fontId) { PrebuiltFont.fromId(fontId) }
    val selectedFont = remember(prebuiltFont, blockyFontFamily) {
        prebuiltFont.resolveFontFamily(blockyFontFamily)
    }
    val textAlign = remember(textAlignMode) {
        when (textAlignMode) {
            "center" -> TextAlign.Center
            "right" -> TextAlign.End
            else -> TextAlign.Start
        }
    }

    fun tickHaptic(heavy: Boolean = false) {
        val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        val effect = if (heavy) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        } else {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
        }
        vib?.vibrate(effect)
    }

    fun saveWidgetList(updated: List<String>) {
        activeWidgets = updated
        prefs.edit().putString("widgets_order", updated.joinToString(",")).apply()
    }

    fun animateDrawerTo(target: Float) {
        scope.launch {
            drawerProgress.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    fun launchApp(pkg: String) {
        context.packageManager.getLaunchIntentForPackage(pkg)?.let {
            context.startActivity(it)
        }
    }

    fun openAppInfo(pkg: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", pkg, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun uninstallApp(pkg: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", pkg, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openAppInfo(pkg)
        }
    }

    // System Back Button Handling (Android 14 3-Button & Gesture Navigation)
    val backDispatcher = (context as? ComponentActivity)?.onBackPressedDispatcher
    DisposableEffect(backDispatcher, currentView, editMode, isDrawerOpen, contextMenuTarget, oledSleepActive) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    oledSleepActive -> oledSleepActive = false
                    contextMenuTarget != null -> contextMenuTarget = null
                    editMode -> editMode = false
                    currentView != LauncherView.HOME_AND_DRAWER -> currentView = LauncherView.HOME_AND_DRAWER
                    drawerProgress.value > 0.05f -> animateDrawerTo(0f)
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
                    fontFamily = blockyFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CMF OLED LOCK · TAP TO WAKE",
                    color = Color(0xFF262626),
                    fontSize = 11.sp,
                    fontFamily = blockyFontFamily
                )
            }
        }
        return
    }

    // Root Surface with OLED Wallpaper Engine + System Navigation Bar Insets
    // (Note: Launcher's custom notification/status bar has been completely removed per user request)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val density = LocalDensity.current
        val screenHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }

        // Wallpaper Background Canvas (OLED Black / Nothing Dot-Matrix / CMF Blueprint / Glow)
        OledWallpaperCanvas(preset = wallpaperPreset, accentColor = accentColor, dimPercent = wallpaperDimPercent)

        Column(modifier = Modifier.fillMaxSize()) {
            // EDIT MODE BANNER
            if (editMode && currentView == LauncherView.HOME_AND_DRAWER && drawerProgress.value < 0.2f) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
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
            // VIEW SWITCHER (SMOOTH SWIPE HOME + DRAWER / WIDGETS / SETTINGS)
            // ================================================================
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (currentView) {
                    LauncherView.HOME_AND_DRAWER -> {
                        val progress = drawerProgress.value

                        // 1. HOME SCREEN LAYER (Fades & scales smoothly as App Drawer slides up)
                        if (progress < 0.99f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        alpha = (1f - progress * 1.25f).coerceIn(0f, 1f)
                                        translationY = -progress * 88.dp.toPx()
                                        val s = 1f - progress * 0.04f
                                        scaleX = s
                                        scaleY = s
                                    }
                                    .pointerInput(doubleTapSleep, screenHeightPx) {
                                        detectTapGestures(
                                            onDoubleTap = {
                                                if (doubleTapSleep) oledSleepActive = true
                                            },
                                            onLongPress = {
                                                editMode = !editMode
                                                tickHaptic(heavy = true)
                                            }
                                        )
                                    }
                                    .pointerInput(screenHeightPx) {
                                        var totalDy = 0f
                                        detectVerticalDragGestures(
                                            onDragStart = { totalDy = 0f },
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                totalDy += dragAmount
                                                val delta = -dragAmount / (screenHeightPx * 0.52f)
                                                val next = (drawerProgress.value + delta).coerceIn(0f, 1f)
                                                scope.launch { drawerProgress.snapTo(next) }
                                            },
                                            onDragEnd = {
                                                if (drawerProgress.value > 0.16f || totalDy < -90f) {
                                                    animateDrawerTo(1f)
                                                } else {
                                                    animateDrawerTo(0f)
                                                }
                                            },
                                            onDragCancel = {
                                                if (drawerProgress.value > 0.5f) animateDrawerTo(1f)
                                                else animateDrawerTo(0f)
                                            }
                                        )
                                    }
                            ) {
                                HomeScreenView(
                                    activeWidgets = activeWidgets,
                                    editMode = editMode,
                                    currentTimeStr = currentTimeStr,
                                    currentDateStr = currentDateStr,
                                    batteryPct = batteryPct,
                                    isCharging = isCharging,
                                    zenActive = zenActive,
                                    zenSeconds = zenSeconds,
                                    quickNote = quickNote,
                                    accentColor = accentColor,
                                    blockyFontFamily = blockyFontFamily,
                                    selectedFont = selectedFont,
                                    uppercaseHome = prebuiltFont.uppercaseHome,
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
                                    onLaunchApp = { pkg -> launchApp(pkg) },
                                    onLongPressApp = { app ->
                                        tickHaptic(heavy = true)
                                        contextMenuTarget = app to true
                                    },
                                    onOpenDrawer = { animateDrawerTo(1f) },
                                    onOpenWidgets = { currentView = LauncherView.WIDGETS },
                                    onOpenSettings = { currentView = LauncherView.SETTINGS },
                                    onWaveTick = { tickHaptic(heavy = false) }
                                )
                            }
                        }

                        // 2. APP DRAWER LAYER (Slides up smoothly with finger / spring animation)
                        if (progress > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        alpha = (progress * 1.35f).coerceIn(0f, 1f)
                                        translationY = (1f - progress) * (screenHeightPx * 0.42f)
                                    }
                            ) {
                                AppDrawerView(
                                    apps = installedApps,
                                    blockyFontFamily = blockyFontFamily,
                                    selectedFont = selectedFont,
                                    uppercaseHome = prebuiltFont.uppercaseHome,
                                    accentColor = accentColor,
                                    niagaraWaveMode = niagaraWaveMode,
                                    screenHeightPx = screenHeightPx,
                                    onDragDrawerDelta = { dy ->
                                        val delta = -dy / (screenHeightPx * 0.52f)
                                        val next = (drawerProgress.value + delta).coerceIn(0f, 1f)
                                        scope.launch { drawerProgress.snapTo(next) }
                                    },
                                    onSettleDrawer = { totalDy ->
                                        if (drawerProgress.value < 0.82f || totalDy > 95f) {
                                            animateDrawerTo(0f)
                                        } else {
                                            animateDrawerTo(1f)
                                        }
                                    },
                                    onLaunchApp = { pkg ->
                                        launchApp(pkg)
                                        animateDrawerTo(0f)
                                    },
                                    onLongPressApp = { app ->
                                        tickHaptic(heavy = true)
                                        contextMenuTarget = app to false
                                    },
                                    onClose = { animateDrawerTo(0f) },
                                    onWaveTick = { tickHaptic(heavy = false) }
                                )
                            }
                        }
                    }

                    LauncherView.WIDGETS -> {
                        WidgetsManagerView(
                            activeWidgets = activeWidgets,
                            accentColor = accentColor,
                            blockyFontFamily = blockyFontFamily,
                            onUpdateWidgets = { saveWidgetList(it) },
                            onBack = { currentView = LauncherView.HOME_AND_DRAWER }
                        )
                    }

                    LauncherView.SETTINGS -> {
                        LauncherSettingsView(
                            fontId = fontId,
                            textAlignMode = textAlignMode,
                            accentHex = accentHex,
                            textSizeSp = textSizeSp,
                            use24Hour = use24Hour,
                            hideAndroidBar = hideAndroidBar,
                            niagaraWaveMode = niagaraWaveMode,
                            wallpaperPreset = wallpaperPreset,
                            blockyFontFamily = blockyFontFamily,
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
                            onToggleDoubleTapSleep = {
                                doubleTapSleep = !doubleTapSleep
                                prefs.edit().putBoolean("dbl_tap_sleep", doubleTapSleep).apply()
                            },
                            onBack = { currentView = LauncherView.HOME_AND_DRAWER }
                        )
                    }
                }
            }
        }

        // ====================================================================
        // LONG-PRESS APP OPTIONS CONTEXT MENU MODAL (HOME & APP DRAWER)
        // ====================================================================
        contextMenuTarget?.let { (targetApp, isFromHome) ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { contextMenuTarget = null }
                    .padding(horizontal = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                        .background(Color(0xFF0A0A0A), RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* consume inside modal */ }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = targetApp.label.uppercase(Locale.getDefault()),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = blockyFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = targetApp.packageName,
                                color = Color(0xFF737373),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "ESC",
                            color = Color(0xFF8A8A8A),
                            fontSize = 11.sp,
                            fontFamily = blockyFontFamily,
                            modifier = Modifier
                                .border(1.dp, Color(0xFF262626), RoundedCornerShape(6.dp))
                                .clickable { contextMenuTarget = null }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF1F1F1F))
                    )

                    // Option 1: Remove from Pinned Apps (for Home Screen) OR Add to Home Screen (for App Drawer)
                    val currentlyPinned = pinnedPackages.contains(targetApp.packageName)
                    val pinActionLabel = if (isFromHome || currentlyPinned) {
                        "REMOVE FROM PINNED APPS"
                    } else {
                        "ADD TO HOME SCREEN"
                    }
                    val pinActionSymbol = if (isFromHome || currentlyPinned) "−" else "+"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                            .background(Color.Black, RoundedCornerShape(10.dp))
                            .clickable {
                                pinnedPackages = if (isFromHome || currentlyPinned) {
                                    pinnedPackages - targetApp.packageName
                                } else {
                                    pinnedPackages + targetApp.packageName
                                }
                                prefs.edit().putStringSet("pinned_pkgs", pinnedPackages).apply()
                                contextMenuTarget = null
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pinActionLabel,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = blockyFontFamily
                        )
                        Text(
                            text = pinActionSymbol,
                            color = accentColor,
                            fontSize = 16.sp,
                            fontFamily = blockyFontFamily
                        )
                    }

                    // Option 2: App Info (Android System Application Details)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                            .background(Color.Black, RoundedCornerShape(10.dp))
                            .clickable {
                                val pkg = targetApp.packageName
                                contextMenuTarget = null
                                openAppInfo(pkg)
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "APP INFO",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = blockyFontFamily
                        )
                        Text(
                            text = "INFO ↗",
                            color = Color(0xFF8A8A8A),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Option 3: Uninstall App (Android Package Uninstaller)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF451A1A), RoundedCornerShape(10.dp))
                            .background(Color(0xFF140707), RoundedCornerShape(10.dp))
                            .clickable {
                                val pkg = targetApp.packageName
                                contextMenuTarget = null
                                uninstallApp(pkg)
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UNINSTALL",
                            color = Color(0xFFF87171),
                            fontSize = 13.sp,
                            fontFamily = blockyFontFamily
                        )
                        Text(
                            text = "DEL ×",
                            color = Color(0xFFF87171),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// HOME SCREEN WITH BATTERY PROGRESS BAR & HOVER / NIAGARA WAVE ANIMATION
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenView(
    activeWidgets: List<String>,
    editMode: Boolean,
    currentTimeStr: String,
    currentDateStr: String,
    batteryPct: Int,
    isCharging: Boolean,
    zenActive: Boolean,
    zenSeconds: Int,
    quickNote: String,
    accentColor: Color,
    blockyFontFamily: FontFamily,
    selectedFont: FontFamily,
    uppercaseHome: Boolean,
    textAlign: TextAlign,
    textSizeSp: Int,
    niagaraWaveMode: String,
    pinnedApps: List<AppEntry>,
    totalAppsCount: Int,
    onToggle24h: () -> Unit,
    onToggleZen: () -> Unit,
    onRemoveWidget: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    onLongPressApp: (AppEntry) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenWidgets: () -> Unit,
    onOpenSettings: () -> Unit,
    onWaveTick: () -> Unit
) {
    val density = LocalDensity.current
    // Root window Y coordinate when sliding along the Home Wave Strip or hovering apps
    var waveTouchRootY by remember { mutableStateOf<Float?>(null) }
    var waveStripRootTopY by remember { mutableStateOf(0f) }
    var lastHoveredPkg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. REMOVABLE WIDGETS STACK (Clock widget includes integrated Battery Progress Bar below it)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                        fontSize = 42.sp,
                                        fontFamily = blockyFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$currentDateStr · CMF PHONE 1",
                                        color = Color(0xFF8A8A8A),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    // INTEGRATED BATTERY PROGRESS BAR JUST BELOW TIME WIDGET
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "BAT $batteryPct%",
                                            color = Color(0xFFD4D4D4),
                                            fontSize = 11.sp,
                                            fontFamily = blockyFontFamily
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(3.dp))
                                                .background(Color(0xFF121212), RoundedCornerShape(3.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth((batteryPct / 100f).coerceIn(0.03f, 1f))
                                                    .background(accentColor, RoundedCornerShape(3.dp))
                                            )
                                        }
                                        if (isCharging) {
                                            Text(
                                                text = "CHG",
                                                color = accentColor,
                                                fontSize = 10.sp,
                                                fontFamily = blockyFontFamily
                                            )
                                        }
                                    }
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
                                            fontFamily = blockyFontFamily
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
                                        fontFamily = blockyFontFamily
                                    )
                                    Text(
                                        text = if (zenActive) "STOP" else "START",
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontFamily = blockyFontFamily,
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

        // 2. PINNED HOME APPS WITH BLOCKY NOTHING FONT + HOVER & NIAGARA WAVE ANIMATIONS
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val waveRadiusPx = with(density) { 105.dp.toPx() }
            val maxWaveShiftPx = with(density) { (if (niagaraWaveMode == "full") 26.dp else 13.dp).toPx() }
            val pressHoverShiftPx = with(density) { 14.dp.toPx() }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                pinnedApps.forEach { app ->
                    var itemCenterRootY by remember { mutableStateOf(0f) }
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val waveFactor = remember(waveTouchRootY, itemCenterRootY, niagaraWaveMode) {
                        if (waveTouchRootY == null || niagaraWaveMode == "off") 0f
                        else {
                            val dist = abs(waveTouchRootY!! - itemCenterRootY)
                            if (dist < waveRadiusPx) {
                                cos((dist / waveRadiusPx) * (PI / 2)).toFloat().coerceIn(0f, 1f)
                            } else 0f
                        }
                    }

                    val isWaveFocused = waveFactor > 0.74f
                    LaunchedEffect(isWaveFocused) {
                        if (isWaveFocused && lastHoveredPkg != app.packageName) {
                            lastHoveredPkg = app.packageName
                            onWaveTick()
                        }
                    }

                    val targetShiftX = when {
                        waveFactor > 0f -> waveFactor * waveFactor * maxWaveShiftPx
                        isPressed -> pressHoverShiftPx
                        else -> 0f
                    }
                    val targetScale = when {
                        waveFactor > 0f -> 1f + waveFactor * 0.12f
                        isPressed -> 1.05f
                        else -> 1f
                    }

                    val animatedShiftX by animateFloatAsState(
                        targetValue = targetShiftX,
                        animationSpec = tween(durationMillis = 70)
                    )
                    val animatedScale by animateFloatAsState(
                        targetValue = targetScale,
                        animationSpec = tween(durationMillis = 70)
                    )
                    val isHighlighted = isWaveFocused || isPressed

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                itemCenterRootY = coords.positionInRoot().y + coords.size.height / 2f
                            }
                            .graphicsLayer {
                                translationX = animatedShiftX
                                scaleX = animatedScale
                                scaleY = animatedScale
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                            }
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onLaunchApp(app.packageName) },
                                onLongClick = { onLongPressApp(app) }
                            )
                            .padding(vertical = 8.dp),
                        horizontalArrangement = when (textAlign) {
                            TextAlign.Center -> Arrangement.Center
                            TextAlign.End -> Arrangement.End
                            else -> Arrangement.Start
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayLabel = if (uppercaseHome) {
                            app.label.uppercase(Locale.getDefault())
                        } else {
                            app.label
                        }
                        Text(
                            text = if (isHighlighted) "▸ $displayLabel" else displayLabel,
                            color = if (isHighlighted) accentColor else Color(0xFFF5F5F5),
                            fontSize = textSizeSp.sp,
                            fontFamily = selectedFont,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Right-edge Niagara Wave Hover Strip on Home Screen (uses exact Root Y coordinates!)
            if (niagaraWaveMode != "off") {
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .fillMaxHeight(0.68f)
                        .onGloballyPositioned { coords ->
                            waveStripRootTopY = coords.positionInRoot().y
                        }
                        .pointerInput(pinnedApps) {
                            detectVerticalDragGestures(
                                onDragStart = { offset ->
                                    waveTouchRootY = waveStripRootTopY + offset.y
                                    onWaveTick()
                                },
                                onVerticalDrag = { change, _ ->
                                    change.consume()
                                    waveTouchRootY = waveStripRootTopY + change.position.y
                                },
                                onDragEnd = {
                                    waveTouchRootY = null
                                    lastHoveredPkg = null
                                },
                                onDragCancel = {
                                    waveTouchRootY = null
                                    lastHoveredPkg = null
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        repeat(11) { idx ->
                            var dotRootY by remember { mutableStateOf(0f) }
                            val dotShift = remember(waveTouchRootY, dotRootY) {
                                if (waveTouchRootY == null) 0f
                                else {
                                    val d = abs(waveTouchRootY!! - dotRootY)
                                    val r = with(density) { 85.dp.toPx() }
                                    if (d < r) -cos((d / r) * (PI / 2)).toFloat() * with(density) { 14.dp.toPx() }
                                    else 0f
                                }
                            }
                            val animDotShift by animateFloatAsState(dotShift, tween(60))
                            Box(
                                modifier = Modifier
                                    .onGloballyPositioned { c ->
                                        dotRootY = c.positionInRoot().y + c.size.height / 2f
                                    }
                                    .graphicsLayer { translationX = animDotShift }
                                    .size(if (animDotShift < -6f) 5.dp else 3.dp)
                                    .background(
                                        if (animDotShift < -6f) accentColor else Color(0xFF404040),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }

        // 3. BOTTOM MINIMAL DOCK (Swipe Up Anywhere or Tap to Open App Drawer)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "/ SEARCH",
                color = Color(0xFF737373),
                fontSize = 11.sp,
                fontFamily = blockyFontFamily,
                modifier = Modifier.clickable { onOpenDrawer() }
            )
            Text(
                text = "SWIPE UP · ALL APPS ($totalAppsCount) ↑",
                color = Color(0xFF9E9E9E),
                fontSize = 10.sp,
                fontFamily = blockyFontFamily,
                modifier = Modifier.clickable { onOpenDrawer() }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "+ WIDGETS",
                    color = Color(0xFF737373),
                    fontSize = 10.sp,
                    fontFamily = blockyFontFamily,
                    modifier = Modifier.clickable { onOpenWidgets() }
                )
                Text(
                    text = "CFG",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontFamily = blockyFontFamily,
                    modifier = Modifier.clickable { onOpenSettings() }
                )
            }
        }
    }
}

// ============================================================================
// APP DRAWER WITH EXACT WEB-PROTOTYPE NIAGARA A-Z WAVE TRAVERSAL & SWIPE-DOWN
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerView(
    apps: List<AppEntry>,
    blockyFontFamily: FontFamily,
    selectedFont: FontFamily,
    uppercaseHome: Boolean,
    accentColor: Color,
    niagaraWaveMode: String,
    screenHeightPx: Float,
    onDragDrawerDelta: (Float) -> Unit,
    onSettleDrawer: (Float) -> Unit,
    onLaunchApp: (String) -> Unit,
    onLongPressApp: (AppEntry) -> Unit,
    onClose: () -> Unit,
    onWaveTick: () -> Unit
) {
    val density = LocalDensity.current
    var query by remember { mutableStateOf("") }
    var activeLetter by remember { mutableStateOf<Char?>(null) }

    // Exact Root Y coordinates so A-Z Rail, Floating Bubble, and App Rows align to the pixel!
    var drawerBoxRootTopY by remember { mutableStateOf(0f) }
    var drawerBoxHeightPx by remember { mutableStateOf(screenHeightPx) }
    var railRootTopY by remember { mutableStateOf(0f) }
    var railHeightPx by remember { mutableStateOf(1f) }
    var scrubRootY by remember { mutableStateOf<Float?>(null) }

    val letterCenterRootY = remember { mutableStateMapOf<Char, Float>() }
    val rowCenterRootY = remember { mutableStateMapOf<String, Float>() }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val filteredApps = remember(apps, query) {
        apps.filter { !it.isHidden && (query.isBlank() || it.label.contains(query, ignoreCase = true)) }
    }

    val letters = remember { ('A'..'Z').toList() }

    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.96f))
            .onGloballyPositioned { coords ->
                drawerBoxRootTopY = coords.positionInRoot().y
                drawerBoxHeightPx = coords.size.height.toFloat().coerceAtLeast(1f)
            }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Swipe-Down Pull Handle Bar (Smoothly drags the App Drawer closed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .pointerInput(Unit) {
                        var totalDy = 0f
                        detectVerticalDragGestures(
                            onDragStart = { totalDy = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                totalDy += dragAmount
                                onDragDrawerDelta(dragAmount)
                            },
                            onDragEnd = { onSettleDrawer(totalDy) },
                            onDragCancel = { onSettleDrawer(totalDy) }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(4.dp)
                            .background(Color(0xFF333333), RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SWIPE DOWN TO CLOSE ↓",
                        color = Color(0xFF525252),
                        fontSize = 9.sp,
                        fontFamily = blockyFontFamily
                    )
                }
            }

            // Search Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(8.dp))
                    .background(Color(0xFF080808), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "/ ",
                    color = accentColor,
                    fontSize = 13.sp,
                    fontFamily = blockyFontFamily,
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
                                text = "SEARCH APPS (LONG-PRESS APP FOR OPTIONS)...",
                                color = Color(0xFF525252),
                                fontSize = 10.sp,
                                fontFamily = blockyFontFamily
                            )
                        }
                        inner()
                    }
                )
                Text(
                    text = "ESC",
                    color = Color(0xFF8A8A8A),
                    fontSize = 11.sp,
                    fontFamily = blockyFontFamily,
                    modifier = Modifier.clickable {
                        if (query.isNotEmpty()) query = "" else onClose()
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App List + Niagara A-Z Wave Scrubber Rail
            val letterWaveRadiusPx = with(density) { 95.dp.toPx() }
            val maxLetterShiftPx = with(density) { (if (niagaraWaveMode == "full") 34.dp else 16.dp).toPx() }
            val rowWaveRadiusPx = with(density) { 105.dp.toPx() }
            val maxRowShiftPx = with(density) { (if (niagaraWaveMode == "full") 22.dp else 11.dp).toPx() }

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(isAtTop) {
                            if (isAtTop) {
                                var totalDy = 0f
                                detectVerticalDragGestures(
                                    onDragStart = { totalDy = 0f },
                                    onVerticalDrag = { change, dragAmount ->
                                        if (dragAmount > 0f || totalDy > 0f) {
                                            change.consume()
                                            totalDy += dragAmount
                                            onDragDrawerDelta(dragAmount)
                                        }
                                    },
                                    onDragEnd = {
                                        if (totalDy > 0f) onSettleDrawer(totalDy)
                                    },
                                    onDragCancel = {
                                        if (totalDy > 0f) onSettleDrawer(totalDy)
                                    }
                                )
                            }
                        },
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        val rowCenterY = rowCenterRootY[app.packageName] ?: -1000f
                        val rowWaveFactor = remember(scrubRootY, rowCenterY, niagaraWaveMode) {
                            if (scrubRootY == null || niagaraWaveMode == "off" || rowCenterY < 0f) 0f
                            else {
                                val d = abs(scrubRootY!! - rowCenterY)
                                if (d < rowWaveRadiusPx) {
                                    cos((d / rowWaveRadiusPx) * (PI / 2)).toFloat().coerceIn(0f, 1f)
                                } else 0f
                            }
                        }

                        val targetRowShiftX = when {
                            rowWaveFactor > 0f -> rowWaveFactor * rowWaveFactor * maxRowShiftPx
                            isPressed -> with(density) { 12.dp.toPx() }
                            else -> 0f
                        }
                        val targetRowScale = when {
                            rowWaveFactor > 0f -> 1f + rowWaveFactor * 0.08f
                            isPressed -> 1.04f
                            else -> 1f
                        }
                        val animRowShiftX by animateFloatAsState(targetRowShiftX, tween(65))
                        val animRowScale by animateFloatAsState(targetRowScale, tween(65))
                        val isRowHighlighted = rowWaveFactor > 0.72f || isPressed

                        val displayLabel = if (uppercaseHome) {
                            app.label.uppercase(Locale.getDefault())
                        } else {
                            app.label
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coords ->
                                    rowCenterRootY[app.packageName] =
                                        coords.positionInRoot().y + coords.size.height / 2f
                                }
                                .graphicsLayer {
                                    translationX = animRowShiftX
                                    scaleX = animRowScale
                                    scaleY = animRowScale
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                }
                                .combinedClickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { onLaunchApp(app.packageName) },
                                    onLongClick = { onLongPressApp(app) }
                                )
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isRowHighlighted) "▸ $displayLabel" else displayLabel,
                                    color = if (isRowHighlighted) accentColor else Color(0xFFF5F5F5),
                                    fontSize = 16.sp,
                                    fontFamily = selectedFont,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (app.isPinned) {
                                    Text(
                                        text = "[PIN]",
                                        color = accentColor,
                                        fontSize = 9.sp,
                                        fontFamily = blockyFontFamily
                                    )
                                }
                            }
                        }
                    }
                }

                // Niagara A-Z Wave Scrubber Rail (Curves outward to the LEFT while app rows curve RIGHT)
                fun updateScrubFromLocalY(localY: Float) {
                    val clampedLocalY = localY.coerceIn(0f, railHeightPx)
                    val currentRootY = railRootTopY + clampedLocalY
                    scrubRootY = currentRootY

                    // Find closest letter by measured root Y center (or proportional index fallback)
                    val chosenLetter = if (letterCenterRootY.isNotEmpty()) {
                        letterCenterRootY.minByOrNull { (_, cy) -> abs(cy - currentRootY) }?.key
                    } else {
                        val idx = ((clampedLocalY / railHeightPx) * letters.size).toInt().coerceIn(0, letters.lastIndex)
                        letters[idx]
                    } ?: 'A'

                    if (activeLetter != chosenLetter) {
                        activeLetter = chosenLetter
                        onWaveTick()
                        val targetIdx = filteredApps.indexOfFirst {
                            it.label.startsWith(chosenLetter, ignoreCase = true)
                        }
                        if (targetIdx >= 0) {
                            scope.launch { listState.scrollToItem(targetIdx) }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .width(34.dp)
                        .fillMaxHeight()
                        .onGloballyPositioned { coords ->
                            railRootTopY = coords.positionInRoot().y
                            railHeightPx = coords.size.height.toFloat().coerceAtLeast(1f)
                        }
                        .pointerInput(filteredApps) {
                            detectTapGestures(
                                onPress = { offset ->
                                    updateScrubFromLocalY(offset.y)
                                    tryAwaitRelease()
                                    scrubRootY = null
                                    activeLetter = null
                                }
                            )
                        }
                        .pointerInput(filteredApps) {
                            detectVerticalDragGestures(
                                onDragStart = { offset ->
                                    updateScrubFromLocalY(offset.y)
                                },
                                onVerticalDrag = { change, _ ->
                                    change.consume()
                                    updateScrubFromLocalY(change.position.y)
                                },
                                onDragEnd = {
                                    scrubRootY = null
                                    activeLetter = null
                                },
                                onDragCancel = {
                                    scrubRootY = null
                                    activeLetter = null
                                }
                            )
                        },
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    letters.forEach { ch ->
                        val isCurrent = activeLetter == ch
                        val lCenterY = letterCenterRootY[ch] ?: 0f

                        val letterFactor = remember(scrubRootY, lCenterY, niagaraWaveMode) {
                            if (scrubRootY == null || niagaraWaveMode == "off" || lCenterY == 0f) 0f
                            else {
                                val d = abs(scrubRootY!! - lCenterY)
                                if (d < letterWaveRadiusPx) {
                                    cos((d / letterWaveRadiusPx) * (PI / 2)).toFloat().coerceIn(0f, 1f)
                                } else 0f
                            }
                        }

                        val targetShiftX = -(letterFactor * letterFactor * maxLetterShiftPx)
                        val targetScale = 1f + letterFactor * 0.42f
                        val animShiftX by animateFloatAsState(targetShiftX, tween(60))
                        val animScale by animateFloatAsState(targetScale, tween(60))

                        Text(
                            text = ch.toString(),
                            color = if (isCurrent || letterFactor > 0.72f) accentColor else Color(0xFF737373),
                            fontSize = 10.sp,
                            fontFamily = blockyFontFamily,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .onGloballyPositioned { coords ->
                                    letterCenterRootY[ch] = coords.positionInRoot().y + coords.size.height / 2f
                                }
                                .graphicsLayer {
                                    translationX = animShiftX
                                    scaleX = animScale
                                    scaleY = animScale
                                }
                        )
                    }
                }
            }
        }

        // Floating Niagara Wave Letter Bubble (Follows finger Y position smoothly!)
        if (activeLetter != null && scrubRootY != null) {
            val bubbleSizePx = with(density) { 48.dp.toPx() }
            val minBubbleY = with(density) { 64.dp.toPx() }
            val maxBubbleY = (drawerBoxHeightPx - bubbleSizePx - with(density) { 24.dp.toPx() }).coerceAtLeast(minBubbleY)
            val bubbleY = (scrubRootY!! - drawerBoxRootTopY - bubbleSizePx / 2f).coerceIn(minBubbleY, maxBubbleY)

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 56.dp)
                    .offset { IntOffset(0, bubbleY.roundToInt()) }
                    .size(48.dp)
                    .border(1.5.dp, accentColor, RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A0A0A), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeLetter.toString(),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontFamily = blockyFontFamily,
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
    blockyFontFamily: FontFamily,
    onUpdateWidgets: (List<String>) -> Unit,
    onBack: () -> Unit
) {
    val allWidgets = listOf(
        "clock" to "Blocky Clock, Date & Battery Bar",
        "weather" to "Minimal Weather & AQI Pill",
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
                fontSize = 14.sp,
                fontFamily = blockyFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "← BACK",
                color = accentColor,
                fontSize = 11.sp,
                fontFamily = blockyFontFamily,
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
                    fontFamily = blockyFontFamily,
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
    hideAndroidBar: Boolean,
    niagaraWaveMode: String,
    wallpaperPreset: String,
    blockyFontFamily: FontFamily,
    doubleTapSleep: Boolean,
    onChangeFont: (String) -> Unit,
    onChangeAlign: (String) -> Unit,
    onChangeAccent: (String) -> Unit,
    onChangeTextSize: (Int) -> Unit,
    onToggle24h: () -> Unit,
    onToggleAndroidBar: () -> Unit,
    onChangeWaveMode: (String) -> Unit,
    onChangeWallpaperPreset: (String) -> Unit,
    onOpenSystemWallpaperPicker: () -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onOpenWidgets: () -> Unit,
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
                        text = "N-TEXT // SETTINGS",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = blockyFontFamily,
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
                    fontFamily = blockyFontFamily,
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
                blockyFontFamily = blockyFontFamily,
                onClick = onRequestDefaultHomeRole
            )
        }

        // 2. Hide Stock Android Notification Bar
        item {
            SettingCard(
                title = "Hide Android Status Bar",
                subtitle = "100% clean top OLED bezel (no status bars)",
                actionLabel = if (hideAndroidBar) "HIDDEN (ON)" else "SHOWN (OFF)",
                blockyFontFamily = blockyFontFamily,
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
                    Text("OLED Wallpaper Engine", color = Color.White, fontSize = 12.sp, fontFamily = blockyFontFamily)
                    Text(
                        text = "SYSTEM GALLERY →",
                        color = Color(0xFFFF3B00),
                        fontSize = 10.sp,
                        fontFamily = blockyFontFamily,
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
                            fontFamily = blockyFontFamily,
                            modifier = Modifier
                                .background(if (active) Color.White else Color(0xFF141414), RoundedCornerShape(6.dp))
                                .clickable { onChangeWallpaperPreset(id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 4. Prebuilt Font Style Selector (Nothing Blocky Pixel Default, Tech Mono, etc.)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Prebuilt Font Style", color = Color.White, fontSize = 12.sp, fontFamily = blockyFontFamily)
                PrebuiltFont.values().forEach { f ->
                    val selected = fontId == f.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChangeFont(f.id) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = f.displayName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = f.resolveFontFamily(blockyFontFamily)
                        )
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
                actionLabel = niagaraWaveMode.uppercase(Locale.getDefault()),
                blockyFontFamily = blockyFontFamily,
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
                actionLabel = textAlignMode.uppercase(Locale.getDefault()),
                blockyFontFamily = blockyFontFamily,
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
                blockyFontFamily = blockyFontFamily,
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
                blockyFontFamily = blockyFontFamily,
                onClick = onOpenWidgets
            )
        }

        // 8. Double-Tap OLED Sleep
        item {
            SettingCard(
                title = "Double-Tap OLED Sleep",
                subtitle = "Double-tap empty black area to lock screen",
                actionLabel = if (doubleTapSleep) "ON" else "OFF",
                blockyFontFamily = blockyFontFamily,
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
    blockyFontFamily: FontFamily,
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
            Text(text = title, color = Color.White, fontSize = 12.sp, fontFamily = blockyFontFamily)
            Text(text = subtitle, color = Color(0xFF737373), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        Text(
            text = actionLabel,
            color = Color.Black,
            fontSize = 10.sp,
            fontFamily = blockyFontFamily,
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
