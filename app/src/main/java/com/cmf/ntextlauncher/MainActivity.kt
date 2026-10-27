package com.cmf.ntextlauncher

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.unit.Velocity
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
// EMBEDDED LOMO WEB PIXEL TRUETYPE FONT (SLENDER 1-PIXEL STROKE 9-ROW GRID)
// Self-contained inside MainActivity.kt so even single-file GitHub updates
// compile with zero external resource dependencies!
// ============================================================================
private const val LOMO_PIXEL_TTF_B64 =
    "AAEAAAAKAIAAAwAgT1MvMmeiavUAAAEoAAAAYGNtYXBNwt5LAAAC7AAAAHxnbHlmXaJ0EAAABBwAABRIaGVhZC8Bq6QAAACsAAAA" +
    "NmhoZWEE2gIKAAAA5AAAACRobXR4t8AAAAAAAYgAAAFkbG9jYd+w5OQAAANoAAAAtG1heHAAYgAmAAABCAAAACBuYW1lKj423QAA" +
    "GGQAAAFQcG9zdHNM/OUAABm0AAACpwABAAAAAQAArRVIGl8PPPUAAQPoAAAAAOcGNHIAAAAA5wY0cgAA/0AB4AKgAAAAAwACAAAA" +
    "AAAAAAEAAAL4/xAAAAJAAAAAYAHgAAEAAAAAAAAAAAAAAAAAAABZAAEAAABZACQACAAAAAAAAgAAAAAAAAAAAAAAAAAAAAAAAwIR" +
    "AZAABQAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAAAgAAAAAAAAAAAAPz8/PwAAACAluAL4/xAAAAL4APAA" +
    "AAAAAAAAAAHgAqAAAAAgAAAB4AAAAeAAAADAAAABgAAAAkAAAAJAAAACQAAAAMAAAAGAAAABgAAAAkAAAAJAAAABIAAAAeAAAAEg" +
    "AAACQAAAAkAAAAJAAAACQAAAAkAAAAJAAAACQAAAAkAAAAJAAAACQAAAAkAAAAGAAAAB4AAAAeAAAAJAAAACQAAAAkAAAAJAAAAC" +
    "QAAAAkAAAAJAAAACQAAAAkAAAAJAAAABgAAAAkAAAAJAAAACQAAAAkAAAAJAAAACQAAAAkAAAAJAAAACQAAAAkAAAAJAAAACQAAA" +
    "AkAAAAJAAAACQAAAAkAAAAJAAAABgAAAAYAAAAJAAAACQAAAAkAAAAJAAAACQAAAAkAAAAJAAAACQAAAAkAAAAGAAAAB4AAAAeAA" +
    "AAGAAAACQAAAAkAAAAJAAAACQAAAAkAAAAJAAAACQAAAAeAAAAJAAAACQAAAAkAAAAJAAAACQAAAAkAAAAHgAAABgAAAAeAAAAAA" +
    "AAIAAAADAAAAFAADAAEAAAAUAAQAaAAAABYAEAADAAYAIwA6ADwAWwBdAF8AegCwALcluP//AAAAIAAlADwAPgBdAF8AYQCwALcl" +
    "uP///+H/4P/f/97/3f/c/9v/pv+g2qAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAwAHwAxAFoAhACsALgA3QD8ASIBNwFI" +
    "AVYBYgGBAasBvwHoAhQCKQJLAnECjQK2At0C7wMaA0gDagOOA6oDygPtBAYEHQQyBFcEbgSEBJ8E0ATgBQEFHwU9BVUFgQWnBdMF" +
    "5QX9BhwGPAZvBpAGtAbEBtYG4gcBBxoHOgdTB3IHjweuB8cH4AgACCIINQhUCGgIhAieCLgI0gj2CRUJKglJCWsJpAnCCeMKAAoM" +
    "CiQAAQAAAAABgAKgAAMAADERIREBgAKg/WAAAgAAAAAAYAKgAAMABwAAExEjERE1MxVgYGACoP4gAeD9YGBgAAACAAAB4AEgAqAA" +
    "AwAHAAATMxUjJxUjNcBgYGBgAqDAwMDAAAACAAAAYAHgAqAAGwAfAAARNTM1MxUzNTMVMxUjFTMVIxUjNSMVIzUjNTM1FzUjFWBg" +
    "YGBgYGBgYGBgYGDAYAHgYGBgYGBgwGBgYGBgYMDAwMAABAAAAAAB4AKgAA8AEwAXABsAABMzNTM1MxUjFSMVIxUjNTMFIzUzATUz" +
    "FTczFSNgYGBgYGBgYGABgMDA/iDAwGBgASBgwMBgwGBgYMABIMDAwGAAAQAAAAAB4AKgAB8AACEjNSMVIzUzNSMVIzUzNSM1MxUz" +
    "NSM1MxUzFSMVMxUzAeBgYMDAwGBgYGDAwMBgYGBgYGBgwMDAYMDAwGBgwGDAAAABAAAB4ABgAqAAAwAAExUjNWBgAqDAwAAFAAAA" +
    "AAEgAqAAAwAHAAsADwATAAABIzUzBTMRIxMzFSMTIzUzFxUjNQEgYGD+4GBgYGBgYGBgYGACQGDA/uABgGD+gGBgYGAAAAIAAAAA" +
    "ASACoAAPABMAABMjNTMVMxEjFSMVIzUzNTMDMxUjwGBgYGBgYGBgwGBgAeBgYP7gYGBgYAHgYAAAAgAAAGAB4AJAABcAGwAAASMV" +
    "MxUjNSMVIzUzNSM1MzUjNTMVMxUzJyM1MwHgwGBgYGBgwMBgYGDAYGBgASBgYGBgYGBgYGBgYGBgAAEAAABgAeACQAALAAABIxUj" +
    "NSM1MzUzFTMB4MBgwMBgwAEgwMBgwMAAAAIAAP+gAMAAYAADAAcAADczFSMVIzUzYGBgYGBgYGBgAAABAAABIAGAAYAAAwAAASE1" +
    "IQGA/oABgAEgYAAAAQAAAAAAYABgAAMAADcVIzVgYGBgYAAAAgAAAAAB4AKgAA8AEwAAEzM1MzUzFSMVIxUjFSM1MwEzFSNgYGBg" +
    "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" +
    "AAAAAAAFAAMAOwABAAAAAAAGABQAEwADAAEECQABABgAPgADAAEECQACAA4AVgADAAEECQADACgAZAADAAEECQAEACgAjAADAAEE" +
    "CQAFAAYAtAADAAEECQAGACgAZExvbW9XZWJQaXhlbFJlZ3VsYXJMb21vV2ViUGl4ZWwtUmVndWxhckxvbW9XZWJQaXhlbCBSZWd1" +
    "bGFyMi4wAEwAbwBtAG8AVwBlAGIAUABpAHgAZQBsAFIAZQBnAHUAbABhAHIATABvAG0AbwBXAGUAYgBQAGkAeABlAGwALQBSAGUA" +
    "ZwB1AGwAYQByAEwAbwBtAG8AVwBlAGIAUABpAHgAZQBsACAAUgBlAGcAdQBsAGEAcgAyAC4AMAACAAAAAAAAAAAAAAAAAAAAAAAA" +
    "AAAAAAAAAAAAAAAAAFkAAAECAQMBBAEFAQYBBwEIAQkBCgELAQwBDQEOAQ8BEAERARIBEwEUARUBFgEXARgBGQEaARsBHAEdAR4B" +
    "HwEgASEBIgEjASQBJQEmAScBKAEpASoBKwEsAS0BLgEvATABMQEyATMBNAE1ATYBNwE4ATkBOgE7ATwBPQE+AT8BQAFBAUIBQwFE" +
    "AUUBRgFHAUgBSQFKAUsBTAFNAU4BTwFQAVEBUgFTAVQBVQFWAVcBWAFZBGdfMzIEZ18zMwRnXzM0BGdfMzUEZ18zNwRnXzM4BGdf" +
    "MzkEZ180MARnXzQxBGdfNDIEZ180MwRnXzQ0BGdfNDUEZ180NgRnXzQ3BGdfNDgEZ180OQRnXzUwBGdfNTEEZ181MgRnXzUzBGdf" +
    "NTQEZ181NQRnXzU2BGdfNTcEZ181OARnXzYwBGdfNjIEZ182MwRnXzY0BGdfNjUEZ182NgRnXzY3BGdfNjgEZ182OQRnXzcwBGdf" +
    "NzEEZ183MgRnXzczBGdfNzQEZ183NQRnXzc2BGdfNzcEZ183OARnXzc5BGdfODAEZ184MQRnXzgyBGdfODMEZ184NARnXzg1BGdf" +
    "ODYEZ184NwRnXzg4BGdfODkEZ185MARnXzkxBGdfOTMEZ185NQRnXzk3BGdfOTgEZ185OQVnXzEwMAVnXzEwMQVnXzEwMgVnXzEw" +
    "MwVnXzEwNAVnXzEwNQVnXzEwNgVnXzEwNwVnXzEwOAVnXzEwOQVnXzExMAVnXzExMQVnXzExMgVnXzExMwVnXzExNAVnXzExNQVn" +
    "XzExNgVnXzExNwVnXzExOAVnXzExOQVnXzEyMAVnXzEyMQVnXzEyMgVnXzE3NgVnXzE4MwZnXzk2NTYA"

private var cachedLomoFontFamily: FontFamily? = null

fun getLomoPixelFontFamily(context: Context): FontFamily {
    cachedLomoFontFamily?.let { return it }
    return try {
        val ttfFile = File(context.cacheDir, "lomo_web_pixel_v3.ttf")
        if (!ttfFile.exists() || ttfFile.length() == 0L) {
            val bytes = Base64.decode(LOMO_PIXEL_TTF_B64, Base64.DEFAULT)
            ttfFile.writeBytes(bytes)
        }
        val tf = Typeface.createFromFile(ttfFile)
        FontFamily(tf).also { cachedLomoFontFamily = it }
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
    val firstLetter: Char,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false
)

enum class LauncherView {
    HOME_AND_DRAWER, WIDGETS, SETTINGS
}

enum class PrebuiltFont(val id: String, val displayName: String, val uppercaseHome: Boolean) {
    LOMO_PIXEL("dot", "Lomo Web Pixel (Default · 1px Thin)", false),
    LOMO_CAPS("ndot", "Lomo Web Pixel (Uppercase)", true),
    TECH_MONO("mono", "JetBrains Tech Mono", false),
    SPACE_MONO("spacemono", "Space Mono Industrial", false),
    CLEAN_SANS("sans", "Clean Geometric Sans", false),
    EDITORIAL("editorial", "Minimal Editorial Serif", false);

    fun resolveFontFamily(lomoFamily: FontFamily): FontFamily = when (this) {
        LOMO_PIXEL, LOMO_CAPS -> lomoFamily
        TECH_MONO, SPACE_MONO -> FontFamily.Monospace
        CLEAN_SANS -> FontFamily.SansSerif
        EDITORIAL -> FontFamily.Serif
    }

    companion object {
        fun fromId(id: String): PrebuiltFont = values().find { it.id == id } ?: LOMO_PIXEL
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
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
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
                activity = this,
                prefs = prefs,
                packageRefreshTrigger = packageRefreshTrigger,
                onToggleAndroidStatusBar = { hide ->
                    hideDefaultAndroidStatusBar = hide
                    prefs.edit().putBoolean("hide_android_bar", hide).apply()
                    applySystemStatusBarVisibility(hide)
                },
                onExpandNotifications = { expandSystemNotificationsPanel() },
                onRequestDefaultHomeRole = { requestDefaultHomeLauncher() },
                onOpenSystemWallpaperPicker = { openSystemWallpaperChooser() },
                onRequestSystemUninstall = { pkg -> triggerSystemWideAppUninstall(pkg) }
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

    @SuppressLint("WrongConstant", "PrivateApi")
    private fun expandSystemNotificationsPanel() {
        // 1. Expand the Android System Notification Shade via StatusBarManager reflection
        try {
            val statusBarService = getSystemService("statusbar")
            if (statusBarService != null) {
                val statusBarManager = Class.forName("android.app.StatusBarManager")
                val expandMethod = statusBarManager.getMethod("expandNotificationsPanel")
                expandMethod.isAccessible = true
                expandMethod.invoke(statusBarService)
                return
            }
        } catch (_: Exception) {
        }
        // 2. Fallback if EXPAND_STATUS_BAR permission wasn't added to AndroidManifest.xml yet:
        // temporarily reveal the system status bar so the user can see/pull notifications
        try {
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.show(WindowInsetsCompat.Type.statusBars())
        } catch (_: Exception) {
        }
    }

    @Suppress("DEPRECATION")
    private fun triggerSystemWideAppUninstall(pkg: String) {
        try {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
            if (isSystemApp) {
                Toast.makeText(
                    this,
                    "System app cannot be uninstalled; opening App Info",
                    Toast.LENGTH_SHORT
                ).show()
                openSystemAppDetails(pkg)
                return
            }
        } catch (_: Exception) {
        }

        val hasDeletePerm = checkSelfPermission(android.Manifest.permission.REQUEST_DELETE_PACKAGES) ==
            PackageManager.PERMISSION_GRANTED

        if (hasDeletePerm) {
            try {
                val pkgUri = Uri.fromParts("package", pkg, null)
                val deleteIntent = Intent(Intent.ACTION_DELETE, pkgUri).apply {
                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                }
                startActivityForResult(deleteIntent, 2002)
                return
            } catch (_: Exception) {
            }
        }

        // If REQUEST_DELETE_PACKAGES is missing from AndroidManifest.xml on GitHub,
        // still try ACTION_DELETE and fall back to the system App Details screen where Uninstall lives
        try {
            val pkgUri = Uri.fromParts("package", pkg, null)
            val deleteIntent = Intent(Intent.ACTION_DELETE, pkgUri)
            startActivity(deleteIntent)
            if (!hasDeletePerm) {
                Toast.makeText(
                    this,
                    "Add REQUEST_DELETE_PACKAGES to AndroidManifest.xml if uninstall popup didn't appear",
                    Toast.LENGTH_LONG
                ).show()
                openSystemAppDetails(pkg)
            }
        } catch (_: Exception) {
            openSystemAppDetails(pkg)
        }
    }

    private fun openSystemAppDetails(pkg: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", pkg, null)
            }
            startActivity(intent)
        } catch (_: Exception) {
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
    activity: ComponentActivity,
    prefs: SharedPreferences,
    packageRefreshTrigger: Int,
    onToggleAndroidStatusBar: (Boolean) -> Unit,
    onExpandNotifications: () -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onOpenSystemWallpaperPicker: () -> Unit,
    onRequestSystemUninstall: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lomoFontFamily = remember { getLomoPixelFontFamily(context) }

    // Navigation & Edit Mode State
    var currentView by remember { mutableStateOf(LauncherView.HOME_AND_DRAWER) }
    // Smooth continuous App Drawer progress: 0f = Home Screen, 1f = App Drawer Open
    val drawerProgress = remember { Animatable(0f) }
    val isDrawerOpen by remember { derivedStateOf { drawerProgress.value > 0.5f } }
    var focusDrawerSearchOnOpen by remember { mutableStateOf(false) }

    var editMode by remember { mutableStateOf(false) }
    var oledSleepActive by remember { mutableStateOf(false) }

    // Long-tap on Home Screen opens the Home Quick Actions Modal
    var showHomeQuickMenu by remember { mutableStateOf(false) }

    // Long-press on an App opens the App Context Menu Modal: Pair(AppEntry, isFromHomeScreen)
    var contextMenuTarget by remember { mutableStateOf<Pair<AppEntry, Boolean>?>(null) }

    // Persisted Preferences (Default font = "dot" -> Lomo Web Pixel 1px thin font)
    var fontId by remember { mutableStateOf(prefs.getString("font_id", "dot") ?: "dot") }
    var textAlignMode by remember { mutableStateOf(prefs.getString("align", "left") ?: "left") }
    var accentHex by remember { mutableStateOf(prefs.getString("accent", "#FF3B00") ?: "#FF3B00") }
    var textSizeSp by remember { mutableStateOf(prefs.getInt("text_size", 20)) }
    // Default 12-hour clock ("4:22 PM") matching the user's uploaded prototype image
    var use24Hour by remember { mutableStateOf(prefs.getBoolean("use_24h_v4", false)) }
    var tempCelsius by remember { mutableStateOf(prefs.getBoolean("temp_c", true)) }
    var hideAndroidBar by remember { mutableStateOf(prefs.getBoolean("hide_android_bar", true)) }
    var niagaraWaveMode by remember { mutableStateOf(prefs.getString("wave_mode", "full") ?: "full") }
    var wallpaperPreset by remember { mutableStateOf(prefs.getString("wallpaper", "oled") ?: "oled") }
    var wallpaperDimPercent by remember { mutableStateOf(prefs.getInt("wp_dim", 50)) }
    var doubleTapSleep by remember { mutableStateOf(prefs.getBoolean("dbl_tap_sleep", true)) }

    // Removable Widgets Order & Visibility
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

    // Installed Apps Loader (Sorted alphabetically A-Z)
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
                val label = ri.loadLabel(pm).toString().trim()
                val firstCh = label.firstOrNull()?.uppercaseChar()?.let {
                    if (it in 'A'..'Z') it else '#'
                } ?: '#'
                AppEntry(
                    label = label,
                    packageName = pkg,
                    firstLetter = firstCh,
                    isPinned = pinnedPackages.contains(pkg),
                    isHidden = hiddenPackages.contains(pkg)
                )
            }
            .distinctBy { it.packageName }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })

        if (pinnedPackages.isEmpty() && loaded.isNotEmpty()) {
            val defaults = loaded.take(6).map { it.packageName }.toSet()
            pinnedPackages = defaults
            prefs.edit().putStringSet("pinned_pkgs", defaults).apply()
        }
        installedApps = loaded
    }

    // Live Clock, Date, Next Alarm & Battery Receiver (Matching image_169fbb.png)
    var currentHourStr by remember { mutableStateOf("4") }
    var currentMinStr by remember { mutableStateOf("22") }
    var currentAmPmStr by remember { mutableStateOf("PM") }
    var currentDateStr by remember { mutableStateOf("TUE · 27 OCT") }
    var nextAlarmStr by remember { mutableStateOf("ALARM 07:00") }
    var batteryPct by remember { mutableStateOf(86) }
    var isCharging by remember { mutableStateOf(false) }

    LaunchedEffect(use24Hour) {
        while (true) {
            val now = Date()
            if (use24Hour) {
                currentHourStr = SimpleDateFormat("HH", Locale.getDefault()).format(now)
                currentMinStr = SimpleDateFormat("mm", Locale.getDefault()).format(now)
                currentAmPmStr = ""
            } else {
                currentHourStr = SimpleDateFormat("h", Locale.getDefault()).format(now)
                currentMinStr = SimpleDateFormat("mm", Locale.getDefault()).format(now)
                currentAmPmStr = SimpleDateFormat("a", Locale.ENGLISH).format(now).uppercase(Locale.ENGLISH)
            }
            currentDateStr = SimpleDateFormat("EEE · dd MMM", Locale.ENGLISH).format(now).uppercase(Locale.ENGLISH)

            // Check system AlarmManager for next scheduled alarm, or show ALARM 07:00
            val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val nextAlarm = am?.nextAlarmClock
            nextAlarmStr = if (nextAlarm != null) {
                val alarmTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(nextAlarm.triggerTime))
                "ALARM $alarmTime"
            } else {
                "ALARM 07:00"
            }

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
    val selectedFont = remember(prebuiltFont, lomoFontFamily) {
        prebuiltFont.resolveFontFamily(lomoFontFamily)
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

    fun animateDrawerTo(target: Float, focusSearch: Boolean = false) {
        focusDrawerSearchOnOpen = focusSearch && target > 0.5f
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
            }
            activity.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    // System Back Button Handling (Android 14 3-Button & Gesture Navigation)
    val backDispatcher = activity.onBackPressedDispatcher
    DisposableEffect(backDispatcher, currentView, editMode, isDrawerOpen, contextMenuTarget, showHomeQuickMenu, oledSleepActive) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    oledSleepActive -> oledSleepActive = false
                    contextMenuTarget != null -> contextMenuTarget = null
                    showHomeQuickMenu -> showHomeQuickMenu = false
                    editMode -> editMode = false
                    currentView != LauncherView.HOME_AND_DRAWER -> currentView = LauncherView.HOME_AND_DRAWER
                    drawerProgress.value > 0.05f -> animateDrawerTo(0f)
                }
            }
        }
        backDispatcher.addCallback(callback)
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
                    text = "$currentHourStr:$currentMinStr",
                    color = Color(0xFF404040),
                    fontSize = 42.sp,
                    fontFamily = lomoFontFamily,
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

                        // 1. HOME SCREEN LAYER (Swipe UP -> App Drawer, Swipe DOWN -> Notifications, Long-Tap -> Quick Menu)
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
                                    .pointerInput(doubleTapSleep) {
                                        detectTapGestures(
                                            onDoubleTap = {
                                                if (doubleTapSleep) oledSleepActive = true
                                            },
                                            onLongPress = {
                                                tickHaptic(heavy = true)
                                                showHomeQuickMenu = true
                                            }
                                        )
                                    }
                                    .pointerInput(screenHeightPx) {
                                        var totalDy = 0f
                                        var notifTriggered = false
                                        detectVerticalDragGestures(
                                            onDragStart = {
                                                totalDy = 0f
                                                notifTriggered = false
                                            },
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                totalDy += dragAmount
                                                // If swiping up (or closing an already partially dragged drawer)
                                                if (totalDy < 0f || drawerProgress.value > 0.01f) {
                                                    val delta = -dragAmount / (screenHeightPx * 0.52f)
                                                    val next = (drawerProgress.value + delta).coerceIn(0f, 1f)
                                                    scope.launch { drawerProgress.snapTo(next) }
                                                } else if (totalDy > 110f && !notifTriggered && drawerProgress.value < 0.01f) {
                                                    // Swipe DOWN on Home Screen -> Reveal Android Notifications!
                                                    notifTriggered = true
                                                    tickHaptic(heavy = false)
                                                    onExpandNotifications()
                                                }
                                            },
                                            onDragEnd = {
                                                if (drawerProgress.value > 0.16f || totalDy < -90f) {
                                                    animateDrawerTo(1f)
                                                } else if (drawerProgress.value > 0f) {
                                                    animateDrawerTo(0f)
                                                }
                                            },
                                            onDragCancel = {
                                                if (drawerProgress.value > 0.5f) animateDrawerTo(1f)
                                                else if (drawerProgress.value > 0f) animateDrawerTo(0f)
                                            }
                                        )
                                    }
                            ) {
                                HomeScreenView(
                                    activeWidgets = activeWidgets,
                                    editMode = editMode,
                                    currentHourStr = currentHourStr,
                                    currentMinStr = currentMinStr,
                                    currentAmPmStr = currentAmPmStr,
                                    currentDateStr = currentDateStr,
                                    nextAlarmStr = nextAlarmStr,
                                    batteryPct = batteryPct,
                                    isCharging = isCharging,
                                    tempCelsius = tempCelsius,
                                    zenActive = zenActive,
                                    zenSeconds = zenSeconds,
                                    quickNote = quickNote,
                                    accentColor = accentColor,
                                    lomoFontFamily = lomoFontFamily,
                                    selectedFont = selectedFont,
                                    uppercaseHome = prebuiltFont.uppercaseHome,
                                    textAlign = textAlign,
                                    textSizeSp = textSizeSp,
                                    niagaraWaveMode = niagaraWaveMode,
                                    pinnedApps = installedApps.filter { it.isPinned && !it.isHidden },
                                    onToggle24h = {
                                        use24Hour = !use24Hour
                                        prefs.edit().putBoolean("use_24h_v4", use24Hour).apply()
                                    },
                                    onToggleTempUnit = {
                                        tempCelsius = !tempCelsius
                                        prefs.edit().putBoolean("temp_c", tempCelsius).apply()
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
                                    onWaveTick = { tickHaptic(heavy = false) }
                                )
                            }
                        }

                        // 2. APP DRAWER LAYER (Slides up smoothly + native 120Hz NestedScrollConnection)
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
                                    lomoFontFamily = lomoFontFamily,
                                    selectedFont = selectedFont,
                                    uppercaseHome = prebuiltFont.uppercaseHome,
                                    accentColor = accentColor,
                                    niagaraWaveMode = niagaraWaveMode,
                                    screenHeightPx = screenHeightPx,
                                    drawerProgressValue = progress,
                                    focusSearchOnOpen = focusDrawerSearchOnOpen,
                                    onConsumedSearchFocus = { focusDrawerSearchOnOpen = false },
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
                            lomoFontFamily = lomoFontFamily,
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
                            lomoFontFamily = lomoFontFamily,
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
                                prefs.edit().putBoolean("use_24h_v4", use24Hour).apply()
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
        // LONG-TAP HOME SCREEN QUICK ACTIONS MENU (Replaces Bottom Dock Text)
        // ====================================================================
        if (showHomeQuickMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.84f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showHomeQuickMenu = false }
                    .padding(horizontal = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(16.dp))
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
                        Column {
                            Text(
                                text = "N-TEXT // HOME MENU",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontFamily = lomoFontFamily
                            )
                            Text(
                                text = "Long-tap on Home Screen",
                                color = Color(0xFF737373),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "ESC",
                            color = Color(0xFF8A8A8A),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .border(1.dp, Color(0xFF262626), RoundedCornerShape(6.dp))
                                .clickable { showHomeQuickMenu = false }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF1F1F1F))
                    )

                    // 1. Search Apps
                    QuickMenuButton(
                        label = "/ Search Apps",
                        badge = "SEARCH",
                        accentColor = accentColor,
                        lomoFontFamily = lomoFontFamily,
                        onClick = {
                            showHomeQuickMenu = false
                            animateDrawerTo(1f, focusSearch = true)
                        }
                    )

                    // 2. All Apps Drawer
                    QuickMenuButton(
                        label = "↑ Open All Apps (${installedApps.count { !it.isHidden }})",
                        badge = "DRAWER",
                        accentColor = accentColor,
                        lomoFontFamily = lomoFontFamily,
                        onClick = {
                            showHomeQuickMenu = false
                            animateDrawerTo(1f, focusSearch = false)
                        }
                    )

                    // 3. Manage Widgets
                    QuickMenuButton(
                        label = "+ Widgets Manager",
                        badge = "WIDGETS",
                        accentColor = accentColor,
                        lomoFontFamily = lomoFontFamily,
                        onClick = {
                            showHomeQuickMenu = false
                            currentView = LauncherView.WIDGETS
                        }
                    )

                    // 4. Toggle Inline Edit Mode
                    QuickMenuButton(
                        label = if (editMode) "✓ Exit Widget Edit Mode" else "✎ Edit Home Widgets",
                        badge = "EDIT",
                        accentColor = accentColor,
                        lomoFontFamily = lomoFontFamily,
                        onClick = {
                            showHomeQuickMenu = false
                            editMode = !editMode
                        }
                    )

                    // 5. Launcher Settings
                    QuickMenuButton(
                        label = "⚙ Launcher Settings",
                        badge = "CFG",
                        accentColor = accentColor,
                        lomoFontFamily = lomoFontFamily,
                        onClick = {
                            showHomeQuickMenu = false
                            currentView = LauncherView.SETTINGS
                        }
                    )
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
                                text = targetApp.label,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = lomoFontFamily,
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
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
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
                        "Remove from Pinned Apps"
                    } else {
                        "Add to Home Screen"
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
                            fontSize = 14.sp,
                            fontFamily = lomoFontFamily
                        )
                        Text(
                            text = pinActionSymbol,
                            color = accentColor,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace
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
                            text = "App Info",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = lomoFontFamily
                        )
                        Text(
                            text = "INFO ↗",
                            color = Color(0xFF8A8A8A),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Option 3: System-Wide Uninstall App (Android Package Uninstaller)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF451A1A), RoundedCornerShape(10.dp))
                            .background(Color(0xFF140707), RoundedCornerShape(10.dp))
                            .clickable {
                                val pkg = targetApp.packageName
                                contextMenuTarget = null
                                onRequestSystemUninstall(pkg)
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Uninstall App",
                            color = Color(0xFFF87171),
                            fontSize = 14.sp,
                            fontFamily = lomoFontFamily
                        )
                        Text(
                            text = "DEL ×",
                            color = Color(0xFFF87171),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickMenuButton(
    label: String,
    badge: String,
    accentColor: Color,
    lomoFontFamily: FontFamily,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
            .background(Color.Black, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontFamily = lomoFontFamily
        )
        Text(
            text = badge,
            color = accentColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ============================================================================
// HOME SCREEN (EXACT PROTOTYPE WIDGETS SETUP + LOMO PIXEL APPS + NO BOTTOM DOCK)
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenView(
    activeWidgets: List<String>,
    editMode: Boolean,
    currentHourStr: String,
    currentMinStr: String,
    currentAmPmStr: String,
    currentDateStr: String,
    nextAlarmStr: String,
    batteryPct: Int,
    isCharging: Boolean,
    tempCelsius: Boolean,
    zenActive: Boolean,
    zenSeconds: Int,
    quickNote: String,
    accentColor: Color,
    lomoFontFamily: FontFamily,
    selectedFont: FontFamily,
    uppercaseHome: Boolean,
    textAlign: TextAlign,
    textSizeSp: Int,
    niagaraWaveMode: String,
    pinnedApps: List<AppEntry>,
    onToggle24h: () -> Unit,
    onToggleTempUnit: () -> Unit,
    onToggleZen: () -> Unit,
    onRemoveWidget: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    onLongPressApp: (AppEntry) -> Unit,
    onWaveTick: () -> Unit
) {
    val density = LocalDensity.current
    var waveTouchRootY by remember { mutableStateOf<Float?>(null) }
    var waveStripRootTopY by remember { mutableStateOf(0f) }
    var lastHoveredPkg by remember { mutableStateOf<String?>(null) }

    // Pulsing orange colon animation matching the prototype's .animate-dot
    val infiniteTransition = rememberInfiniteTransition(label = "colonPulse")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colonAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. EXACT PROTOTYPE WIDGETS STACK (Matching image_169fbb.png 1:1)
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
                                val horizontalAlign = when (textAlign) {
                                    TextAlign.Center -> Alignment.CenterHorizontally
                                    TextAlign.End -> Alignment.End
                                    else -> Alignment.Start
                                }
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = horizontalAlign
                                ) {
                                    // Line 1: 4:22 PM (with pulsing orange colon)
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        modifier = Modifier.clickable { onToggle24h() }
                                    ) {
                                        Text(
                                            text = currentHourStr,
                                            color = Color.White,
                                            fontSize = 42.sp,
                                            fontFamily = lomoFontFamily
                                        )
                                        Text(
                                            text = ":",
                                            color = accentColor.copy(alpha = colonAlpha),
                                            fontSize = 42.sp,
                                            fontFamily = lomoFontFamily
                                        )
                                        Text(
                                            text = currentMinStr,
                                            color = Color.White,
                                            fontSize = 42.sp,
                                            fontFamily = lomoFontFamily
                                        )
                                        if (currentAmPmStr.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = currentAmPmStr,
                                                color = Color(0xFF737373),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(bottom = 5.dp)
                                            )
                                        }
                                    }

                                    // Line 2: TUE · 27 OCT · ALARM 07:00
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = currentDateStr,
                                            color = Color(0xFFA3A3A3),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.2.sp
                                        )
                                        Text(
                                            text = "·",
                                            color = accentColor,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = nextAlarmStr,
                                            color = Color(0xFF737373),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.2.sp
                                        )
                                    }

                                    // Faint Horizontal Line
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFF171717))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Line 3: BAT 86% [━━━━━━━━━━░░] OLED
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "BAT $batteryPct%",
                                            color = Color(0xFFD4D4D4),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 0.8.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .border(1.dp, Color(0xFF262626), RoundedCornerShape(999.dp))
                                                .background(Color(0xFF141414), RoundedCornerShape(999.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth((batteryPct / 100f).coerceIn(0.03f, 1f))
                                                    .background(accentColor, RoundedCornerShape(999.dp))
                                            )
                                        }
                                        Text(
                                            text = if (isCharging) "CHG" else "OLED",
                                            color = if (isCharging) accentColor else Color(0xFF737373),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                            "weather" -> {
                                // Line 4: ● 28°C CLEAR SKY          [AQI 42 · GOOD]
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onToggleTempUnit() }
                                            .padding(vertical = 6.dp),
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
                                                text = if (tempCelsius) "28°C" else "82°F",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "CLEAR SKY",
                                                color = Color(0xFFA3A3A3),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF171717), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                                        ) {
                                            Text(
                                                text = "AQI 42 · GOOD",
                                                color = Color(0xFFA3A3A3),
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFF171717))
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

        // 2. PINNED HOME APPS WITH LOMO WEB PIXEL FONT + HOVER & NIAGARA WAVE ANIMATIONS
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
                        waveFactor > 0f -> 1f + waveFactor * 0.10f
                        isPressed -> 1.04f
                        else -> 1f
                    }

                    val animatedShiftX by animateFloatAsState(
                        targetValue = targetShiftX,
                        animationSpec = tween(durationMillis = 65)
                    )
                    val animatedScale by animateFloatAsState(
                        targetValue = targetScale,
                        animationSpec = tween(durationMillis = 65)
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
                                transformOrigin = TransformOrigin(0f, 0.5f)
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

            // Right-edge Niagara Wave Hover Strip on Home Screen
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
                        repeat(11) {
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
                                        if (animDotShift < -6f) accentColor else Color(0xFF333333),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Bottom dock text completely removed per user request!
        // (Long-tapping anywhere on the Home Screen opens Search, All Apps, Widgets & Settings)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

// ============================================================================
// APP DRAWER WITH ALPHABETICAL GROUPS, FAINT LINES & 120HZ NESTED SCROLLING
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerView(
    apps: List<AppEntry>,
    lomoFontFamily: FontFamily,
    selectedFont: FontFamily,
    uppercaseHome: Boolean,
    accentColor: Color,
    niagaraWaveMode: String,
    screenHeightPx: Float,
    drawerProgressValue: Float,
    focusSearchOnOpen: Boolean,
    onConsumedSearchFocus: () -> Unit,
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
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(focusSearchOnOpen, drawerProgressValue) {
        if (focusSearchOnOpen && drawerProgressValue > 0.95f) {
            try {
                searchFocusRequester.requestFocus()
            } catch (_: Exception) {
            }
            onConsumedSearchFocus()
        }
    }

    var drawerBoxRootTopY by remember { mutableStateOf(0f) }
    var drawerBoxHeightPx by remember { mutableStateOf(screenHeightPx) }
    var railRootTopY by remember { mutableStateOf(0f) }
    var railHeightPx by remember { mutableStateOf(1f) }
    var scrubRootY by remember { mutableStateOf<Float?>(null) }

    // Non-observable HashMaps prevent per-frame recomposition jank during normal LazyColumn fling scrolling!
    val letterCenterRootY = remember { HashMap<Char, Float>() }
    val rowCenterRootY = remember { HashMap<String, Float>() }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Strictly sorted alphabetically A-Z
    val filteredApps = remember(apps, query) {
        apps
            .filter { !it.isHidden && (query.isBlank() || it.label.contains(query, ignoreCase = true)) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    val letters = remember { ('A'..'Z').toList() }

    // Native 120Hz NestedScrollConnection: lets LazyColumn scroll/fling with zero gesture interference,
    // and smoothly pulls the App Drawer closed when pulling down at the top of the list!
    val drawerNestedScroll = remember(screenHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag && drawerProgressValue < 0.995f && available.y < 0f) {
                    onDragDrawerDelta(available.y)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.Drag && available.y > 0f) {
                    val atTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                    if (atTop) {
                        onDragDrawerDelta(available.y)
                        return Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (drawerProgressValue < 0.98f) {
                    onSettleDrawer(if (available.y > 500f) 140f else 0f)
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.97f))
            .nestedScroll(drawerNestedScroll)
            .onGloballyPositioned { coords ->
                drawerBoxRootTopY = coords.positionInRoot().y
                drawerBoxHeightPx = coords.size.height.toFloat().coerceAtLeast(1f)
            }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Swipe-Down Pull Handle Bar
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
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFF2E2E2E), RoundedCornerShape(2.dp))
                )
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
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(searchFocusRequester),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search apps...",
                                color = Color(0xFF525252),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        inner()
                    }
                )
                Text(
                    text = "ESC",
                    color = Color(0xFF737373),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable {
                        if (query.isNotEmpty()) query = "" else onClose()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App List + Niagara A-Z Wave Scrubber Rail
            val letterWaveRadiusPx = with(density) { 95.dp.toPx() }
            val maxLetterShiftPx = with(density) { (if (niagaraWaveMode == "full") 34.dp else 16.dp).toPx() }
            val rowWaveRadiusPx = with(density) { 105.dp.toPx() }
            val maxRowShiftPx = with(density) { (if (niagaraWaveMode == "full") 22.dp else 11.dp).toPx() }
            val pressShiftPx = with(density) { 10.dp.toPx() }

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(
                        items = filteredApps,
                        key = { _, app -> app.packageName }
                    ) { index, app ->
                        val prevLetter = if (index > 0) filteredApps[index - 1].firstLetter else null
                        val nextLetter = if (index < filteredApps.lastIndex) filteredApps[index + 1].firstLetter else null
                        val isFirstInAlphabetGroup = app.firstLetter != prevLetter
                        val isLastInAlphabetGroup = app.firstLetter != nextLetter

                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        val currentScrubY = scrubRootY
                        val rowWaveFactor = if (currentScrubY != null && niagaraWaveMode != "off") {
                            val rCenter = rowCenterRootY[app.packageName] ?: -1000f
                            val d = abs(currentScrubY - rCenter)
                            if (rCenter > 0f && d < rowWaveRadiusPx) {
                                cos((d / rowWaveRadiusPx) * (PI / 2)).toFloat().coerceIn(0f, 1f)
                            } else 0f
                        } else 0f

                        val isRowHighlighted = rowWaveFactor > 0.72f || isPressed
                        val displayLabel = if (uppercaseHome) {
                            app.label.uppercase(Locale.getDefault())
                        } else {
                            app.label
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Alphabet Group Section Header (Just like the web prototype)
                            if (isFirstInAlphabetGroup) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (index == 0) 4.dp else 10.dp, bottom = 2.dp)
                                ) {
                                    Text(
                                        text = app.firstLetter.toString(),
                                        color = accentColor,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFF1A1A1A))
                                    )
                                }
                            }

                            // App Item Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coords ->
                                        rowCenterRootY[app.packageName] =
                                            coords.positionInRoot().y + coords.size.height / 2f
                                    }
                                    .graphicsLayer {
                                        val shift = when {
                                            rowWaveFactor > 0f -> rowWaveFactor * rowWaveFactor * maxRowShiftPx
                                            isPressed -> pressShiftPx
                                            else -> 0f
                                        }
                                        val sc = when {
                                            rowWaveFactor > 0f -> 1f + rowWaveFactor * 0.06f
                                            isPressed -> 1.03f
                                            else -> 1f
                                        }
                                        translationX = shift
                                        scaleX = sc
                                        scaleY = sc
                                        transformOrigin = TransformOrigin(0f, 0.5f)
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
                                            text = "★",
                                            color = accentColor,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            // Faint Divider Line after each alphabet group (and subtle separator between apps)
                            if (isLastInAlphabetGroup) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp, bottom = 4.dp)
                                        .height(1.dp)
                                        .background(Color(0xFF222222))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFF0D0D0D))
                                )
                            }
                        }
                    }
                }

                // Niagara A-Z Wave Scrubber Rail
                fun updateScrubFromLocalY(localY: Float) {
                    val clampedLocalY = localY.coerceIn(0f, railHeightPx)
                    val currentRootY = railRootTopY + clampedLocalY
                    scrubRootY = currentRootY

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
                            it.firstLetter == chosenLetter
                        }
                        if (targetIdx >= 0) {
                            scope.launch { listState.scrollToItem(targetIdx) }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .width(32.dp)
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
                        val currentScrub = scrubRootY
                        val letterFactor = if (currentScrub != null && niagaraWaveMode != "off") {
                            val lCenterY = letterCenterRootY[ch] ?: 0f
                            val d = abs(currentScrub - lCenterY)
                            if (lCenterY > 0f && d < letterWaveRadiusPx) {
                                cos((d / letterWaveRadiusPx) * (PI / 2)).toFloat().coerceIn(0f, 1f)
                            } else 0f
                        } else 0f

                        Text(
                            text = ch.toString(),
                            color = if (isCurrent || letterFactor > 0.72f) accentColor else Color(0xFF737373),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .onGloballyPositioned { coords ->
                                    letterCenterRootY[ch] = coords.positionInRoot().y + coords.size.height / 2f
                                }
                                .graphicsLayer {
                                    translationX = -(letterFactor * letterFactor * maxLetterShiftPx)
                                    val sc = 1f + letterFactor * 0.42f
                                    scaleX = sc
                                    scaleY = sc
                                }
                        )
                    }
                }
            }
        }

        // Floating Niagara Wave Letter Bubble (Follows finger Y position smoothly!)
        if (activeLetter != null && scrubRootY != null) {
            val bubbleSizePx = with(density) { 48.dp.toPx() }
            val minBubbleY = with(density) { 56.dp.toPx() }
            val maxBubbleY = (drawerBoxHeightPx - bubbleSizePx - with(density) { 24.dp.toPx() }).coerceAtLeast(minBubbleY)
            val bubbleY = (scrubRootY!! - drawerBoxRootTopY - bubbleSizePx / 2f).coerceIn(minBubbleY, maxBubbleY)

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 54.dp)
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
                    fontFamily = lomoFontFamily,
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
    lomoFontFamily: FontFamily,
    onUpdateWidgets: (List<String>) -> Unit,
    onBack: () -> Unit
) {
    val allWidgets = listOf(
        "clock" to "Lomo Pixel Clock, Date, Alarm & Battery Bar",
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
                fontSize = 15.sp,
                fontFamily = lomoFontFamily,
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
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (enabled) "ACTIVE [×]" else "+ ADD",
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
    hideAndroidBar: Boolean,
    niagaraWaveMode: String,
    wallpaperPreset: String,
    lomoFontFamily: FontFamily,
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
                        fontSize = 16.sp,
                        fontFamily = lomoFontFamily,
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
                lomoFontFamily = lomoFontFamily,
                onClick = onRequestDefaultHomeRole
            )
        }

        // 2. Clock Format (12h PM/AM vs 24h)
        item {
            SettingCard(
                title = "Clock Format (12h PM / 24h)",
                subtitle = "Tap to switch between 4:22 PM and 16:22",
                actionLabel = if (use24Hour) "24-HOUR" else "12-HOUR (PM)",
                lomoFontFamily = lomoFontFamily,
                onClick = onToggle24h
            )
        }

        // 3. Hide Stock Android Notification Bar
        item {
            SettingCard(
                title = "Hide Android Status Bar",
                subtitle = "Swipe down on Home Screen anytime to view notifications",
                actionLabel = if (hideAndroidBar) "HIDDEN (ON)" else "SHOWN (OFF)",
                lomoFontFamily = lomoFontFamily,
                onClick = onToggleAndroidBar
            )
        }

        // 4. Wallpaper Presets + System Wallpaper Picker
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
                    Text("OLED Wallpaper Engine", color = Color.White, fontSize = 13.sp, fontFamily = lomoFontFamily)
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

        // 5. Prebuilt Font Style Selector (Lomo Web Pixel 1px Default, etc.)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Prebuilt Font Style", color = Color.White, fontSize = 13.sp, fontFamily = lomoFontFamily)
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
                            fontSize = 13.sp,
                            fontFamily = f.resolveFontFamily(lomoFontFamily)
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

        // 6. Niagara Wave Traversal Animation
        item {
            SettingCard(
                title = "Niagara Wave List Animation",
                subtitle = "Curved horizontal wave when scrubbing app lists",
                actionLabel = niagaraWaveMode.uppercase(Locale.getDefault()),
                lomoFontFamily = lomoFontFamily,
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

        // 7. Text Alignment & Accent Color
        item {
            SettingCard(
                title = "Home Text Alignment",
                subtitle = "Tap to cycle Left / Center / Right",
                actionLabel = textAlignMode.uppercase(Locale.getDefault()),
                lomoFontFamily = lomoFontFamily,
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
                lomoFontFamily = lomoFontFamily,
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

        // 8. Manage Home Screen Widgets
        item {
            SettingCard(
                title = "Removable Home Widgets",
                subtitle = "Add, reorder, or remove all widgets",
                actionLabel = "MANAGE →",
                lomoFontFamily = lomoFontFamily,
                onClick = onOpenWidgets
            )
        }

        // 9. Double-Tap OLED Sleep
        item {
            SettingCard(
                title = "Double-Tap OLED Sleep",
                subtitle = "Double-tap empty black area to lock screen",
                actionLabel = if (doubleTapSleep) "ON" else "OFF",
                lomoFontFamily = lomoFontFamily,
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
    lomoFontFamily: FontFamily,
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
            Text(text = title, color = Color.White, fontSize = 13.sp, fontFamily = lomoFontFamily)
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
