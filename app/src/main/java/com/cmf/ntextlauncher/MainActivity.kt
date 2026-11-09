package com.cmf.ntextlauncher

import android.app.Activity
import android.app.ActivityManager
import android.app.WallpaperManager
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.SystemClock
import android.provider.AlarmClock
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.*

// ============================================================================
// NOCTURNAA — ULTRA-MINIMALIST OLED TEXT LAUNCHER (v5.1.0-cmf)
// Engineered for CMF by Nothing Phone 1 (Android 14 · API 34 · Nothing OS 2.6)
// - 100% Offline, Zero Internet Permission, Pure #000000 AMOLED Black
// - System-Wide Themes:
//   1. Regular Minimalist OLED Mode
//   2. Old School Mode (Green Phosphor #9EFF78 & Amber Orange #FFB830)
//   3. Retro-Futuristic Mode (Glowing Cyan #26FFFF & Glowing Red #FF3B14 Holomap)
// - True Neon Phosphor Text & Box Glow (Shadow + BlurMaskFilter outer bloom)
// ============================================================================

val LocalRetro80sMode = compositionLocalOf { false }
// "green" = Green Phosphor, "amber" = Amber Orange, "holomap" = Glowing Red & Cyan Retro-Futuristic
val LocalRetro80sColorTheme = compositionLocalOf { "green" }

@Composable
fun rememberRetroPrimaryColor(fallbackAccent: Color = Color(0xFF9EFF78)): Color {
    val isRetro = LocalRetro80sMode.current
    val theme = LocalRetro80sColorTheme.current
    return when {
        !isRetro -> fallbackAccent
        theme == "holomap" -> Color(0xFF26FFFF) // Bright Glowing Cyan
        theme == "amber" -> Color(0xFFFFB830)   // Bright Glowing Amber
        else -> Color(0xFF9EFF78)               // Bright Glowing Green Phosphor
    }
}

@Composable
fun rememberRetroSecondaryColor(fallbackAccent: Color = Color(0xFF9EFF78)): Color {
    val isRetro = LocalRetro80sMode.current
    val theme = LocalRetro80sColorTheme.current
    return when {
        !isRetro -> fallbackAccent
        theme == "holomap" -> Color(0xFFFF3B14) // Bright Glowing Red-Orange
        theme == "amber" -> Color(0xFFFFB830)   // Bright Glowing Amber
        else -> Color(0xFF9EFF78)               // Bright Glowing Green Phosphor
    }
}

@Composable
fun glowTextStyle(
    color: Color,
    blurRadius: Float = 20f
): TextStyle {
    val isRetro = LocalRetro80sMode.current
    val theme = LocalRetro80sColorTheme.current
    if (!isRetro) return TextStyle.Default
    val haloColor = when {
        color == Color.Black || color.alpha < 0.2f -> Color.Transparent
        theme == "holomap" && color.red > color.blue -> Color(0xFFFF1E00)
        theme == "holomap" -> Color(0xFF00E5FF)
        theme == "amber" -> Color(0xFFFF8800)
        else -> Color(0xFF39FF14)
    }
    return if (haloColor == Color.Transparent) {
        TextStyle.Default
    } else {
        TextStyle(
            shadow = Shadow(
                color = haloColor,
                offset = Offset.Zero,
                blurRadius = blurRadius
            )
        )
    }
}

fun Modifier.retroNeonBorder(
    width: Dp,
    color: Color,
    shape: Shape = RectangleShape,
    glowEnabled: Boolean = false,
    glowRadiusDp: Dp = 7.dp
): Modifier = if (!glowEnabled) {
    this.border(width, color, shape)
} else {
    this
        .drawBehind {
            val strokePx = width.toPx().coerceAtLeast(1f)
            val glowPx = glowRadiusDp.toPx().coerceAtLeast(2f)
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = strokePx * 1.8f
                    this.color = android.graphics.Color.argb(
                        175,
                        (color.red * 255).toInt().coerceIn(0, 255),
                        (color.green * 255).toInt().coerceIn(0, 255),
                        (color.blue * 255).toInt().coerceIn(0, 255)
                    )
                    maskFilter = BlurMaskFilter(glowPx, BlurMaskFilter.Blur.NORMAL)
                }
                canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
            }
        }
        .border(width, color, shape)
}

data class AppEntry(
    val label: String,
    val packageName: String,
    val firstLetter: Char,
    val isPinned: Boolean = false,
    val isSystemApp: Boolean = false
)

enum class LauncherView {
    HOME_AND_DRAWER,
    SETTINGS,
    WIDGETS
}

enum class PrebuiltFont(
    val id: String,
    val displayName: String,
    val shortLabel: String,
    val uppercaseHome: Boolean
) {
    LOMO_PIXEL("lomo_pixel", "Lomo Pixel (5x7 Dot Matrix)", "LOMO", true),
    NDOT("ndot", "Nothing N-Dot (All-Caps Matrix)", "NDOT", true),
    DOT_MATRIX("dot_matrix", "Nothing Dot-Matrix (Mixed Case)", "DOT", false),
    IBM_CRT("ibm_crt", "IBM Retro CRT (80s Terminal)", "IBM", true),
    ARCADE_8BIT("arcade_8bit", "Arcade 8-Bit ( High Score )", "8BIT", true),
    VCR_OSD("vcr_osd", "VCR OSD Mono ( Camcorder )", "VCR", true),
    CASIO_LCD("casio_lcd", "Casio Digital LCD ( Quartz )", "LCD", true),
    CYBER_TERM("cyber_term", "Cyber Terminal ( Tactical )", "CYBR", true),
    SWISS_GROTESK("swiss_grotesk", "Swiss Grotesk ( Clean Sans )", "SWIS", false),
    BAUHAUS_GEO("bauhaus_geo", "Bauhaus Geometric ( Caps )", "BAUH", true),
    JETBRAINS_MONO("jetbrains_mono", "JetBrains Tech Mono ( Code )", "MONO", false),
    SPACE_MONO("space_mono", "Space Mono ( Telemetry )", "SPCE", true),
    EDITORIAL_SERIF("editorial_serif", "Minimal Editorial Serif", "SERF", false),
    ULTRA_THIN("ultra_thin", "Ultra Thin Sans ( Light )", "THIN", false);

    fun resolveFontFamily(lomoFontFamily: FontFamily): FontFamily = when (this) {
        LOMO_PIXEL, NDOT, DOT_MATRIX, IBM_CRT, ARCADE_8BIT, VCR_OSD, CASIO_LCD -> lomoFontFamily
        CYBER_TERM, JETBRAINS_MONO, SPACE_MONO -> FontFamily.Monospace
        EDITORIAL_SERIF -> FontFamily.Serif
        SWISS_GROTESK, BAUHAUS_GEO, ULTRA_THIN -> FontFamily.SansSerif
    }

    companion object {
        fun fromId(id: String?): PrebuiltFont = values().firstOrNull { it.id == id } ?: LOMO_PIXEL
    }
}

val LAUNCHER_ACCENT_PALETTE: List<Pair<String, String>> = listOf(
    "#FF3B00" to "CMF ORG",
    "#D71921" to "NTH RED",
    "#FFFFFF" to "PURE WHT",
    "#00FF66" to "CRT GRN",
    "#00E5FF" to "CYN HUD",
    "#FFB000" to "AMB CRT",
    "#A855F7" to "VIOLET",
    "#3B82F6" to "COBALT",
    "#EC4899" to "NEON PNK",
    "#A3A3A3" to "MONO GRY"
)

fun resolveLauncherAccentColor(hex: String): Color = when (hex.uppercase(Locale.US)) {
    "#FF3B00" -> Color(0xFFFF3B00)
    "#D71921" -> Color(0xFFD71921)
    "#FFFFFF" -> Color(0xFFFFFFFF)
    "#00FF66" -> Color(0xFF00FF66)
    "#00E5FF" -> Color(0xFF00E5FF)
    "#FFB000" -> Color(0xFFFFB000)
    "#A855F7" -> Color(0xFFA855F7)
    "#3B82F6" -> Color(0xFF3B82F6)
    "#EC4899" -> Color(0xFFEC4899)
    "#A3A3A3" -> Color(0xFFA3A3A3)
    "#7CFF5B", "#9EFF78" -> Color(0xFF9EFF78)
    "#FF8800", "#FFB830" -> Color(0xFFFFB830)
    "#26FFFF" -> Color(0xFF26FFFF)
    "#FF3B14" -> Color(0xFFFF3B14)
    else -> Color(0xFFFF3B00)
}

@Composable
fun rememberIosPopupProgress(
    isVisible: Boolean,
    label: String
): State<Float> {
    return animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (isVisible) {
            spring(
                dampingRatio = 0.78f,
                stiffness = Spring.StiffnessMediumLow
            )
        } else {
            tween(durationMillis = 170, easing = FastOutSlowInEasing)
        },
        label = label
    )
}

fun Modifier.iosPopupWindowLayer(
    progress: Float,
    initialScale: Float = 0.82f
): Modifier = this.graphicsLayer {
    val p = progress.coerceIn(0f, 1.08f)
    val scale = initialScale + (1f - initialScale) * p
    scaleX = scale
    scaleY = scale
    alpha = progress.coerceIn(0f, 1f)
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color.Black,
                    surface = Color.Black,
                    primary = Color.White,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    NTextLauncherApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NTextLauncherApp() {
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx().coerceAtLeast(1f) }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx().coerceAtLeast(1f) }
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("cmf_ntext_prefs", Context.MODE_PRIVATE) }

    val lomoFontFamily = remember {
        try {
            FontFamily(Font(R.font.lomo_pixel, FontWeight.Normal))
        } catch (_: Exception) {
            FontFamily.Monospace
        }
    }

    var currentView by remember { mutableStateOf(LauncherView.HOME_AND_DRAWER) }
    var editMode by remember { mutableStateOf(false) }
    var showHomeQuickMenu by remember { mutableStateOf(false) }
    var contextMenuTarget by remember { mutableStateOf<Pair<AppEntry, Boolean>?>(null) }
    var showNoSpacePopup by remember { mutableStateOf(false) }
    var noSpacePopupMessage by remember { mutableStateOf("") }

    fun triggerNoSpaceLeftPopup(msg: String) {
        noSpacePopupMessage = msg
        showNoSpacePopup = true
    }

    var fontId by remember { mutableStateOf(prefs.getString("font_id", "lomo_pixel") ?: "lomo_pixel") }
    var textAlignMode by remember { mutableStateOf(prefs.getString("align_mode", "left") ?: "left") }
    var accentHex by remember { mutableStateOf(prefs.getString("accent_hex", "#FF3B00") ?: "#FF3B00") }
    var regTextSizeSp by remember { mutableStateOf(prefs.getInt("text_size_sp", 20)) }
    var crtTextSizeSp by remember { mutableStateOf(prefs.getInt("crt80s_text_size_v1", 18)) }
    var drawerTextSizeSp by remember { mutableStateOf(prefs.getInt("drawer_text_size_sp", 20)) }
    var use24Hour by remember { mutableStateOf(prefs.getBoolean("use_24h", false)) }
    var hideAndroidBar by remember { mutableStateOf(prefs.getBoolean("hide_status_bar", true)) }
    var enableLeftPage by remember { mutableStateOf(prefs.getBoolean("enable_left_page", true)) }

    // Old School Mode & Theme ("green" = Green Phosphor, "amber" = Amber Orange, "holomap" = Glowing Red & Cyan)
    var retro80sMode by remember { mutableStateOf(prefs.getBoolean("retro_80s_green_mode", false)) }
    var retro80sColorTheme by remember { mutableStateOf(prefs.getString("retro_80s_color_theme", "green") ?: "green") }
    var batteryBarStyle by remember { mutableStateOf(prefs.getString("battery_bar_style", "retro_blocks") ?: "retro_blocks") }
    var pauseWhenAppOpen by remember { mutableStateOf(prefs.getBoolean("pause_when_app_open", true)) }
    var customPinnedOrderEnabled by remember { mutableStateOf(prefs.getBoolean("custom_pinned_order_enabled", true)) }
    var waveScrubMode by remember { mutableStateOf(prefs.getString("wave_mode", "full") ?: "full") }
    var drawerWaveIntensity by remember { mutableStateOf(prefs.getFloat("drawer_wave_intensity", 1.0f)) }

    val rawSavedWp = prefs.getString("wp_preset", "oled") ?: "oled"
    var wallpaperPreset by remember {
        mutableStateOf(if (rawSavedWp == "live_lissajous") "live_crt_scanlines" else rawSavedWp)
    }
    var wallpaperDimPercent by remember { mutableStateOf(prefs.getInt("wp_dim_pct", 0)) }
    var wpSpeed by remember { mutableStateOf(prefs.getFloat("wp_speed", 1.0f)) }
    var wpDensity by remember { mutableStateOf(prefs.getFloat("wp_density", 1.0f)) }
    var wpOpacity by remember { mutableStateOf(prefs.getFloat("wp_opacity", 1.0f)) }
    var wpScale by remember { mutableStateOf(prefs.getFloat("wp_scale", 1.0f)) }
    var settingsBgMode by remember { mutableStateOf(prefs.getString("settings_bg_mode", "opaque") ?: "opaque") }
    var settingsBgIntensity by remember { mutableStateOf(prefs.getInt("settings_bg_intensity", 70)) }
    var drawerBgMode by remember { mutableStateOf(prefs.getString("drawer_bg_mode", "opaque") ?: "opaque") }
    var drawerBgIntensity by remember { mutableStateOf(prefs.getInt("drawer_bg_intensity", 65)) }
    var quickNote by remember { mutableStateOf(prefs.getString("quick_note", "STAY MINIMAL.") ?: "STAY MINIMAL.") }
    var globeTheme by remember { mutableStateOf(prefs.getString("globe_theme", "retro_holo") ?: "retro_holo") }
    var globeWireframeColor by remember { mutableStateOf(prefs.getString("globe_wf_color", "#00FF66") ?: "#00FF66") }
    var globeTilt2dDeg by remember { mutableStateOf(prefs.getFloat("globe_tilt2d_deg", -3f)) }
    var globeRotXDeg by remember { mutableStateOf(prefs.getFloat("globe_rotx_deg", 14f)) }
    var globeRotYDeg by remember { mutableStateOf(prefs.getFloat("globe_roty_deg", -77f)) }

    // 1. REGULAR MODE WIDGET PROFILE
    var regActiveWidgets by remember {
        val saved = prefs.getString("widgets_order_v2", "clock,week_strip,pinned_apps") ?: "clock,week_strip,pinned_apps"
        mutableStateOf(saved.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct())
    }
    var regWidgetSizes by remember {
        val raw = prefs.getString("widget_sizes_v2", "clock:standard,week_strip:standard,pinned_apps:standard") ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }
    var regWidgetFonts by remember {
        val raw = prefs.getString("widget_fonts_v1", "") ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }
    var regWidgetStyles by remember {
        val raw = prefs.getString(
            "widget_styles_v1",
            "clock:default,battery_bar:80s_retro,week_strip:default,pinned_apps:default"
        ) ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }
    var regLeftPageWidgets by remember {
        val saved = prefs.getString("left_widgets_order_v1", "retro_calendar,storage,scratchpad,torch,zen") ?: "retro_calendar,storage,scratchpad,torch,zen"
        mutableStateOf(saved.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct())
    }
    var regLeftWidgetSizes by remember {
        val raw = prefs.getString("left_widget_sizes_v1", "retro_calendar:standard,storage:standard,scratchpad:standard") ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }
    var regLeftWidgetFonts by remember {
        val raw = prefs.getString("left_widget_fonts_v1", "") ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }
    var regLeftWidgetStyles by remember {
        val raw = prefs.getString("left_widget_styles_v1", "battery_bar:80s_retro") ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }

    // 2. OLD SCHOOL / RETRO-FUTURISTIC INDEPENDENT WIDGET PROFILE
    var crtActiveWidgets by remember {
        val saved = prefs.getString("crt80s_widgets_order_v1", "clock,week_strip,globe,pinned_apps") ?: "clock,week_strip,globe,pinned_apps"
        mutableStateOf(saved.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct())
    }
    var crtWidgetSizes by remember {
        val raw = prefs.getString("crt80s_widget_sizes_v1", "clock:standard,week_strip:standard,globe:standard,pinned_apps:standard") ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }
    var crtWidgetFonts by remember {
        val raw = prefs.getString("crt80s_widget_fonts_v1", "") ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }
    var crtWidgetStyles by remember {
        val raw = prefs.getString(
            "crt80s_widget_styles_v1",
            "clock:80s_retro,battery_bar:80s_retro,week_strip:80s_retro,pinned_apps:80s_retro,retro_calendar:80s_retro,year_matrix:80s_retro,retro_flip_clock:80s_retro,analog_chrono:80s_retro,precision_chrono:80s_retro,globe:80s_retro,torch:80s_retro,storage:80s_retro,zen:80s_retro,quote:80s_retro,scratchpad:80s_retro"
        ) ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }
    var crtLeftPageWidgets by remember {
        val saved = prefs.getString("crt80s_left_widgets_order_v1", "storage,retro_calendar,scratchpad,torch,zen") ?: "storage,retro_calendar,scratchpad,torch,zen"
        mutableStateOf(saved.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct())
    }
    var crtLeftWidgetSizes by remember {
        val raw = prefs.getString("crt80s_left_widget_sizes_v1", "storage:large,retro_calendar:standard") ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }
    var crtLeftWidgetFonts by remember {
        val raw = prefs.getString("crt80s_left_widget_fonts_v1", "") ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }
    var crtLeftWidgetStyles by remember {
        val raw = prefs.getString(
            "crt80s_left_widget_styles_v1",
            "clock:80s_retro,battery_bar:80s_retro,week_strip:80s_retro,pinned_apps:80s_retro,retro_calendar:80s_retro,year_matrix:80s_retro,retro_flip_clock:80s_retro,analog_chrono:80s_retro,precision_chrono:80s_retro,globe:80s_retro,torch:80s_retro,storage:80s_retro,zen:80s_retro,quote:80s_retro,scratchpad:80s_retro"
        ) ?: ""
        val map = raw.split(",").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        mutableStateOf(map)
    }

    val textSizeSp = if (retro80sMode) crtTextSizeSp else regTextSizeSp
    val activeWidgets = if (retro80sMode) crtActiveWidgets else regActiveWidgets
    val widgetSizes = if (retro80sMode) crtWidgetSizes else regWidgetSizes
    val widgetFonts = if (retro80sMode) crtWidgetFonts else regWidgetFonts
    val widgetStyles = if (retro80sMode) crtWidgetStyles else regWidgetStyles
    val leftPageWidgets = if (retro80sMode) crtLeftPageWidgets else regLeftPageWidgets
    val leftWidgetSizes = if (retro80sMode) crtLeftWidgetSizes else regLeftWidgetSizes
    val leftWidgetFonts = if (retro80sMode) crtLeftWidgetFonts else regLeftWidgetFonts
    val leftWidgetStyles = if (retro80sMode) crtLeftWidgetStyles else regLeftWidgetStyles

    // Bright, Glowing Theme Colors:
    // - In "holomap" (Red & Cyan): Primary = Glowing Cyan (#26FFFF), Secondary = Glowing Red (#FF3B14)
    // - In "amber": Glowing Amber (#FFB830)
    // - In "green": Glowing Green Phosphor (#9EFF78)
    val accentColor = remember(accentHex, retro80sMode, retro80sColorTheme) {
        when {
            retro80sMode && retro80sColorTheme == "holomap" -> Color(0xFF26FFFF)
            retro80sMode && retro80sColorTheme == "amber" -> Color(0xFFFFB830)
            retro80sMode -> Color(0xFF9EFF78)
            else -> resolveLauncherAccentColor(accentHex)
        }
    }
    val secondaryRetroColor = remember(accentHex, retro80sMode, retro80sColorTheme) {
        when {
            retro80sMode && retro80sColorTheme == "holomap" -> Color(0xFFFF3B14)
            retro80sMode && retro80sColorTheme == "amber" -> Color(0xFFFFB830)
            retro80sMode -> Color(0xFF9EFF78)
            else -> resolveLauncherAccentColor(accentHex)
        }
    }

    val activePrebuiltFont = remember(fontId) { PrebuiltFont.fromId(fontId) }
    val selectedFont = remember(activePrebuiltFont, lomoFontFamily) {
        activePrebuiltFont.resolveFontFamily(lomoFontFamily)
    }

    // Lifecycle & App Open Pause Tracking
    val lifecycleOwner = LocalLifecycleOwner.current
    var isLauncherResumed by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isLauncherResumed = when (event) {
                Lifecycle.Event.ON_RESUME -> true
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> false
                else -> isLauncherResumed
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val widgetsActive = !pauseWhenAppOpen || isLauncherResumed

    // Status Bar Visibility
    LaunchedEffect(hideAndroidBar) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, view)
        if (hideAndroidBar) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    // Custom User Wallpaper Image
    val customWallpaperFile = remember { File(context.filesDir, "custom_user_wallpaper.jpg") }
    var customWallpaperBitmap by remember {
        mutableStateOf<ImageBitmap?>(
            if (customWallpaperFile.exists()) {
                try {
                    BitmapFactory.decodeFile(customWallpaperFile.absolutePath)?.asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            } else null
        )
    }
    val pickWallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(customWallpaperFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    val bmp = BitmapFactory.decodeFile(customWallpaperFile.absolutePath)?.asImageBitmap()
                    withContext(Dispatchers.Main) {
                        if (bmp != null) {
                            customWallpaperBitmap = bmp
                            wallpaperPreset = "user_image"
                            prefs.edit().putString("wp_preset", "user_image").apply()
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    // Installed Apps & Pinned Apps
    var installedApps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var pinnedPackages by remember {
        mutableStateOf(prefs.getStringSet("pinned_pkgs", null) ?: emptySet())
    }
    var pinnedAppsOrder by remember {
        val raw = prefs.getString("pinned_apps_order_v1", "") ?: ""
        mutableStateOf(raw.split(",").map { it.trim() }.filter { it.isNotEmpty() })
    }

    fun reloadInstalledApps() {
        scope.launch(Dispatchers.IO) {
            try {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                val myPkg = context.packageName
                val result = mutableListOf<AppEntry>()
                if (launcherApps != null) {
                    val profiles = launcherApps.profiles
                    for (profile in profiles) {
                        val acts = launcherApps.getActivityList(null, profile)
                        for (info in acts) {
                            val pkg = info.applicationInfo.packageName
                            if (pkg == myPkg) continue
                            val label = info.label?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: pkg
                            val firstChar = label.firstOrNull()?.uppercaseChar() ?: '#'
                            val firstLetter = if (firstChar in 'A'..'Z') firstChar else '#'
                            val sysApp = (info.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                            result.add(
                                AppEntry(
                                    label = label,
                                    packageName = pkg,
                                    firstLetter = firstLetter,
                                    isPinned = false,
                                    isSystemApp = sysApp
                                )
                            )
                        }
                    }
                }
                val deduped = result.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) }
                withContext(Dispatchers.Main) {
                    installedApps = deduped
                    if (pinnedPackages.isEmpty() && deduped.isNotEmpty() && !prefs.contains("pinned_pkgs")) {
                        val defaults = deduped.take(6).map { it.packageName }
                        pinnedPackages = defaults.toSet()
                        pinnedAppsOrder = defaults
                        prefs.edit()
                            .putStringSet("pinned_pkgs", pinnedPackages)
                            .putString("pinned_apps_order_v1", defaults.joinToString(","))
                            .apply()
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    DisposableEffect(context) {
        reloadInstalledApps()
        val pkgReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                reloadInstalledApps()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(pkgReceiver, filter)
        onDispose {
            try {
                context.unregisterReceiver(pkgReceiver)
            } catch (_: Exception) {
            }
        }
    }

    val appsWithPinFlag = remember(installedApps, pinnedPackages) {
        installedApps.map { it.copy(isPinned = pinnedPackages.contains(it.packageName)) }
    }
    val orderedPinnedApps = remember(appsWithPinFlag, pinnedAppsOrder, customPinnedOrderEnabled) {
        val pinnedList = appsWithPinFlag.filter { it.isPinned }
        if (!customPinnedOrderEnabled) {
            pinnedList.sortedBy { it.label.lowercase(Locale.getDefault()) }
        } else {
            val orderMap = pinnedAppsOrder.withIndex().associate { it.value to it.index }
            pinnedList.sortedWith(
                compareBy<AppEntry> { orderMap[it.packageName] ?: Int.MAX_VALUE }
                    .thenBy { it.label.lowercase(Locale.getDefault()) }
            )
        }
    }

    // Real-Time Clock, Battery, RAM/ROM & Torch Telemetry
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var batteryPct by remember { mutableStateOf(80) }
    var isCharging by remember { mutableStateOf(false) }
    var ramStatusText by remember { mutableStateOf("RAM 4.2/8 GB") }
    var storageStatusText by remember { mutableStateOf("ROM 64/128 GB") }
    var ramUsageFraction by remember { mutableStateOf(0.52f) }
    var storageUsageFraction by remember { mutableStateOf(0.50f) }
    var torchEnabled by remember { mutableStateOf(false) }
    var zenActive by remember { mutableStateOf(false) }
    var zenSeconds by remember { mutableStateOf(25 * 60) }

    fun refreshRealtimeSystemTelemetry() {
        currentTimeMillis = System.currentTimeMillis()
        try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val lvl = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 80
            batteryPct = lvl.coerceIn(0, 100)
            isCharging = bm?.isCharging == true
        } catch (_: Exception) {
        }
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (am != null) {
                val mi = ActivityManager.MemoryInfo()
                am.getMemoryInfo(mi)
                val totalGb = (mi.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)).coerceAtLeast(1.0)
                val usedGb = ((mi.totalMem - mi.availMem).toDouble() / (1024.0 * 1024.0 * 1024.0)).coerceAtLeast(0.1)
                ramUsageFraction = (usedGb / totalGb).toFloat().coerceIn(0.05f, 1f)
                ramStatusText = String.format(Locale.US, "RAM %.1f/%.0fG", usedGb, totalGb)
            }
        } catch (_: Exception) {
        }
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes.toDouble().coerceAtLeast(1.0)
            val usedBytes = (stat.totalBytes - stat.availableBytes).toDouble().coerceAtLeast(0.0)
            val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
            val usedGb = usedBytes / (1024.0 * 1024.0 * 1024.0)
            storageUsageFraction = (usedBytes / totalBytes).toFloat().coerceIn(0.05f, 1f)
            storageStatusText = String.format(Locale.US, "ROM %.0f/%.0fG", usedGb, totalGb)
        } catch (_: Exception) {
        }
    }

    LaunchedEffect(widgetsActive) {
        refreshRealtimeSystemTelemetry()
        if (!widgetsActive) return@LaunchedEffect
        while (isActive && widgetsActive) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
            if ((currentTimeMillis / 1000L) % 10L == 0L) {
                refreshRealtimeSystemTelemetry()
            }
            if (zenActive) {
                if (zenSeconds > 0) zenSeconds -= 1 else zenActive = false
            }
        }
    }

    val currentHourStr = remember(currentTimeMillis, use24Hour) {
        SimpleDateFormat(if (use24Hour) "HH" else "hh", Locale.US).format(Date(currentTimeMillis))
    }
    val currentMinStr = remember(currentTimeMillis) {
        SimpleDateFormat("mm", Locale.US).format(Date(currentTimeMillis))
    }
    val currentSecStr = remember(currentTimeMillis) {
        SimpleDateFormat("ss", Locale.US).format(Date(currentTimeMillis))
    }
    val currentAmPmStr = remember(currentTimeMillis, use24Hour) {
        if (use24Hour) "" else SimpleDateFormat("a", Locale.US).format(Date(currentTimeMillis)).uppercase(Locale.US)
    }
    val currentDateStr = remember(currentTimeMillis) {
        SimpleDateFormat("EEE · dd MMM", Locale.US).format(Date(currentTimeMillis)).uppercase(Locale.US)
    }

    // Save helpers for Regular vs Old School / Retro-Futuristic Widget Profiles
    fun saveWidgetList(nextList: List<String>) {
        val cleaned = nextList.distinct()
        if (retro80sMode) {
            crtActiveWidgets = cleaned
            prefs.edit().putString("crt80s_widgets_order_v1", cleaned.joinToString(",")).apply()
        } else {
            regActiveWidgets = cleaned
            prefs.edit().putString("widgets_order_v2", cleaned.joinToString(",")).apply()
        }
    }
    fun saveWidgetSizesMap(nextMap: Map<String, String>) {
        val serialized = nextMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        if (retro80sMode) {
            crtWidgetSizes = nextMap
            prefs.edit().putString("crt80s_widget_sizes_v1", serialized).apply()
        } else {
            regWidgetSizes = nextMap
            prefs.edit().putString("widget_sizes_v2", serialized).apply()
        }
    }
    fun saveWidgetFontsMap(nextMap: Map<String, String>) {
        val serialized = nextMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        if (retro80sMode) {
            crtWidgetFonts = nextMap
            prefs.edit().putString("crt80s_widget_fonts_v1", serialized).apply()
        } else {
            regWidgetFonts = nextMap
            prefs.edit().putString("widget_fonts_v1", serialized).apply()
        }
    }
    fun saveWidgetStylesMap(nextMap: Map<String, String>) {
        val serialized = nextMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        if (retro80sMode) {
            crtWidgetStyles = nextMap
            prefs.edit().putString("crt80s_widget_styles_v1", serialized).apply()
        } else {
            regWidgetStyles = nextMap
            prefs.edit().putString("widget_styles_v1", serialized).apply()
        }
    }
    fun saveLeftWidgetList(nextList: List<String>) {
        val cleaned = nextList.distinct()
        if (retro80sMode) {
            crtLeftPageWidgets = cleaned
            prefs.edit().putString("crt80s_left_widgets_order_v1", cleaned.joinToString(",")).apply()
        } else {
            regLeftPageWidgets = cleaned
            prefs.edit().putString("left_widgets_order_v1", cleaned.joinToString(",")).apply()
        }
    }
    fun saveLeftWidgetSizesMap(nextMap: Map<String, String>) {
        val serialized = nextMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        if (retro80sMode) {
            crtLeftWidgetSizes = nextMap
            prefs.edit().putString("crt80s_left_widget_sizes_v1", serialized).apply()
        } else {
            regLeftWidgetSizes = nextMap
            prefs.edit().putString("left_widget_sizes_v1", serialized).apply()
        }
    }
    fun saveLeftWidgetFontsMap(nextMap: Map<String, String>) {
        val serialized = nextMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        if (retro80sMode) {
            crtLeftWidgetFonts = nextMap
            prefs.edit().putString("crt80s_left_widget_fonts_v1", serialized).apply()
        } else {
            regLeftWidgetFonts = nextMap
            prefs.edit().putString("left_widget_fonts_v1", serialized).apply()
        }
    }
    fun saveLeftWidgetStylesMap(nextMap: Map<String, String>) {
        val serialized = nextMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        if (retro80sMode) {
            crtLeftWidgetStyles = nextMap
            prefs.edit().putString("crt80s_left_widget_styles_v1", serialized).apply()
        } else {
            regLeftWidgetStyles = nextMap
            prefs.edit().putString("left_widget_styles_v1", serialized).apply()
        }
    }

    fun currentPinnedAppsCount(): Int = orderedPinnedApps.size

    fun estimateWidgetHeightDp(wId: String, sz: String, pinnedCount: Int, appSp: Int): Float {
        return when (wId) {
            "clock" -> when (sz) {
                "compact" -> 58f
                "large" -> 96f
                "xlarge" -> 114f
                "max" -> 132f
                else -> 74f
            }
            "battery_bar" -> when (sz) {
                "compact" -> 38f
                "large" -> 58f
                else -> 46f
            }
            "week_strip" -> when (sz) {
                "compact" -> 64f
                "large" -> 96f
                else -> 78f
            }
            "globe" -> when (sz) {
                "compact" -> 142f
                "large" -> 272f
                "xlarge" -> 338f
                "max" -> 402f
                else -> 202f
            }
            "pinned_apps" -> (pinnedCount.coerceAtLeast(1) * (appSp + 14f))
            "retro_calendar" -> 185f
            "year_matrix" -> 95f
            "retro_flip_clock", "analog_chrono", "precision_chrono" -> 105f
            else -> 48f
        }
    }

    fun canFitOnHomeScreen(
        candidateWidgets: List<String>,
        candidateSizes: Map<String, String>,
        pinnedCount: Int,
        appSp: Int
    ): Boolean {
        val totalDp = candidateWidgets.sumOf { wId ->
            val sz = candidateSizes[wId] ?: "standard"
            estimateWidgetHeightDp(wId, sz, pinnedCount, appSp).toDouble() + 12.0
        }
        val maxAllowedDp = (configuration.screenHeightDp - 64).coerceAtLeast(420)
        return totalDp <= maxAllowedDp
    }

    // Drawer & Left Page Slide Animation States (Pure horizontal slide for left page, zero fade!)
    var drawerProgress by remember { mutableStateOf(0f) }
    var leftPageProgress by remember { mutableStateOf(0f) }
    var isGlobeInteractionUnlocked by remember { mutableStateOf(false) }

    fun animateDrawerTo(target: Float) {
        scope.launch {
            animate(
                initialValue = drawerProgress,
                targetValue = target.coerceIn(0f, 1f),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) { value, _ ->
                drawerProgress = value
            }
        }
    }

    fun animateLeftPageTo(target: Float) {
        scope.launch {
            animate(
                initialValue = leftPageProgress,
                targetValue = target.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            ) { value, _ ->
                leftPageProgress = value
            }
        }
    }

    LaunchedEffect(enableLeftPage) {
        if (!enableLeftPage && leftPageProgress > 0f) {
            leftPageProgress = 0f
        }
    }

    val isDrawerNearClosed = drawerProgress < 0.04f
    val isOnLeftPage = enableLeftPage && leftPageProgress > 0.5f

    // Back Handler
    DisposableEffect(currentView, editMode, showHomeQuickMenu, contextMenuTarget, drawerProgress, leftPageProgress) {
        val activity = context as? ComponentActivity
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    contextMenuTarget != null -> contextMenuTarget = null
                    showHomeQuickMenu -> showHomeQuickMenu = false
                    showNoSpacePopup -> showNoSpacePopup = false
                    currentView != LauncherView.HOME_AND_DRAWER -> currentView = LauncherView.HOME_AND_DRAWER
                    editMode -> editMode = false
                    drawerProgress > 0.05f -> animateDrawerTo(0f)
                    leftPageProgress > 0.05f -> animateLeftPageTo(0f)
                }
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(callback)
        onDispose { callback.remove() }
    }

    fun launchPackage(pkg: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (_: Exception) {
        }
    }

    fun openClockApp() {
        try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
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

    fun onRequestSystemUninstall(pkg: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", pkg, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun toggleTorch() {
        try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
            val camId = cm.cameraIdList.firstOrNull() ?: return
            val next = !torchEnabled
            cm.setTorchMode(camId, next)
            torchEnabled = next
        } catch (_: Exception) {
        }
    }

    fun performWaveTick() {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } catch (_: Exception) {
        }
    }

    CompositionLocalProvider(
        LocalRetro80sMode provides retro80sMode,
        LocalRetro80sColorTheme provides retro80sColorTheme
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 1. Full-Screen AMOLED Wallpaper Layer
            WallpaperCanvasView(
                preset = wallpaperPreset,
                accentColor = accentColor,
                dimPercent = wallpaperDimPercent,
                customImageBitmap = customWallpaperBitmap,
                speedMult = wpSpeed,
                densityMult = wpDensity,
                opacityMult = wpOpacity,
                scaleMult = wpScale,
                animationsActive = widgetsActive
            )

            // 2. Subtle Background Overlay for Old School Scanlines or Red & Cyan Holomap Cyan Dot Matrix
            if (retro80sMode) {
                Retro80sCrtScreenEdgeGlowOverlay()
            }

            // 3. Main Foreground Content
            Column(modifier = Modifier.fillMaxSize()) {
                if (editMode && currentView == LauncherView.HOME_AND_DRAWER && isDrawerNearClosed) {
                    val topBtnShape = RoundedCornerShape(if (retro80sMode) 0.dp else 4.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
                            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 2.dp)
                            .retroNeonBorder(
                                if (retro80sMode) 1.5.dp else 1.dp,
                                if (retro80sMode) accentColor else Color(0xFF404040),
                                RoundedCornerShape(if (retro80sMode) 0.dp else 8.dp),
                                glowEnabled = retro80sMode
                            )
                            .background(
                                if (retro80sMode) Color(0xFF050505) else Color(0xFF0A0A0A),
                                RoundedCornerShape(if (retro80sMode) 0.dp else 8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (enableLeftPage) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "◀ LEFT PAGE",
                                    color = if (isOnLeftPage) Color.Black else if (retro80sMode) accentColor else Color.White,
                                    fontSize = 9.sp,
                                    fontFamily = selectedFont,
                                    fontWeight = FontWeight.Bold,
                                    style = if (isOnLeftPage) TextStyle.Default else glowTextStyle(accentColor, 14f),
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            if (isOnLeftPage) secondaryRetroColor else if (retro80sMode) accentColor.copy(alpha = 0.6f) else Color(0xFF282828),
                                            topBtnShape
                                        )
                                        .background(if (isOnLeftPage) secondaryRetroColor else if (retro80sMode) Color(0xFF080808) else Color(0xFF1A1A1A), topBtnShape)
                                        .clickable { animateLeftPageTo(1f) }
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                                Text(
                                    text = "MAIN PAGE ▶",
                                    color = if (!isOnLeftPage) Color.Black else if (retro80sMode) accentColor else Color.White,
                                    fontSize = 9.sp,
                                    fontFamily = selectedFont,
                                    fontWeight = FontWeight.Bold,
                                    style = if (!isOnLeftPage) TextStyle.Default else glowTextStyle(accentColor, 14f),
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            if (!isOnLeftPage) secondaryRetroColor else if (retro80sMode) accentColor.copy(alpha = 0.6f) else Color(0xFF282828),
                                            topBtnShape
                                        )
                                        .background(if (!isOnLeftPage) secondaryRetroColor else if (retro80sMode) Color(0xFF080808) else Color(0xFF1A1A1A), topBtnShape)
                                        .clickable { animateLeftPageTo(0f) }
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "EDIT MODE",
                                color = accentColor,
                                fontSize = 10.sp,
                                fontFamily = selectedFont,
                                fontWeight = FontWeight.Bold,
                                style = glowTextStyle(accentColor)
                            )
                        }
                        Text(
                            text = "DONE",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontFamily = selectedFont,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .retroNeonBorder(1.dp, if (retro80sMode) secondaryRetroColor else Color.White, topBtnShape, glowEnabled = retro80sMode)
                                .background(if (retro80sMode) secondaryRetroColor else Color.White, topBtnShape)
                                .clickable { editMode = false }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (currentView) {
                        LauncherView.HOME_AND_DRAWER -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(enableLeftPage, isDrawerNearClosed, isGlobeInteractionUnlocked) {
                                        if (!isDrawerNearClosed || isGlobeInteractionUnlocked) return@pointerInput
                                        var totalDx = 0f
                                        var totalDy = 0f
                                        detectDragGestures(
                                            onDragStart = {
                                                totalDx = 0f
                                                totalDy = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                totalDx += dragAmount.x
                                                totalDy += dragAmount.y
                                                if (abs(totalDx) > abs(totalDy) * 1.15f && enableLeftPage) {
                                                    change.consume()
                                                    val deltaFrac = dragAmount.x / screenWidthPx
                                                    leftPageProgress = (leftPageProgress + deltaFrac).coerceIn(0f, 1f)
                                                } else if (abs(totalDy) > abs(totalDx) * 1.15f && totalDy < -12f) {
                                                    change.consume()
                                                    val deltaFrac = -dragAmount.y / (screenHeightPx * 0.55f)
                                                    drawerProgress = (drawerProgress + deltaFrac).coerceIn(0f, 1f)
                                                }
                                            },
                                            onDragEnd = {
                                                if (drawerProgress > 0.02f) {
                                                    animateDrawerTo(if (drawerProgress > 0.22f) 1f else 0f)
                                                } else if (enableLeftPage) {
                                                    val target = when {
                                                        totalDx > 95f -> 1f
                                                        totalDx < -95f -> 0f
                                                        leftPageProgress >= 0.5f -> 1f
                                                        else -> 0f
                                                    }
                                                    animateLeftPageTo(target)
                                                }
                                            }
                                        )
                                    }
                            ) {
                                // Additional Left Page (Pure Horizontal Slide, ZERO Fade!)
                                if (enableLeftPage && leftPageProgress > 0.001f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                translationX = (-1f + leftPageProgress) * screenWidthPx
                                            }
                                    ) {
                                        HomeScreenView(
                                            pinnedApps = orderedPinnedApps,
                                            activeWidgets = leftPageWidgets,
                                            widgetSizes = leftWidgetSizes,
                                            widgetFonts = leftWidgetFonts,
                                            widgetStyles = leftWidgetStyles,
                                            selectedFont = selectedFont,
                                            lomoFontFamily = lomoFontFamily,
                                            uppercaseHome = activePrebuiltFont.uppercaseHome,
                                            textAlignMode = textAlignMode,
                                            textSizeSp = textSizeSp,
                                            accentColor = accentColor,
                                            secondaryRetroColor = secondaryRetroColor,
                                            retro80sMode = retro80sMode,
                                            retro80sColorTheme = retro80sColorTheme,
                                            batteryBarStyle = batteryBarStyle,
                                            onChangeBatteryBarStyle = { st ->
                                                batteryBarStyle = st
                                                prefs.edit().putString("battery_bar_style", st).apply()
                                            },
                                            waveScrubMode = waveScrubMode,
                                            currentHourStr = currentHourStr,
                                            currentMinStr = currentMinStr,
                                            currentSecStr = currentSecStr,
                                            currentAmPmStr = currentAmPmStr,
                                            currentDateStr = currentDateStr,
                                            currentTimeMillis = currentTimeMillis,
                                            batteryPct = batteryPct,
                                            isCharging = isCharging,
                                            ramStatusText = ramStatusText,
                                            storageStatusText = storageStatusText,
                                            ramUsageFraction = ramUsageFraction,
                                            storageUsageFraction = storageUsageFraction,
                                            torchEnabled = torchEnabled,
                                            zenActive = zenActive,
                                            zenSeconds = zenSeconds,
                                            quickNote = quickNote,
                                            globeTheme = if (retro80sMode) "wireframe" else globeTheme,
                                            globeWireframeColor = if (retro80sMode) {
                                                when (retro80sColorTheme) {
                                                    "holomap" -> "#26FFFF"
                                                    "amber" -> "#FFB830"
                                                    else -> "#9EFF78"
                                                }
                                            } else globeWireframeColor,
                                            globeTilt2dDeg = globeTilt2dDeg,
                                            globeRotXDeg = globeRotXDeg,
                                            globeRotYDeg = globeRotYDeg,
                                            editMode = editMode,
                                            customPinnedOrderEnabled = customPinnedOrderEnabled,
                                            widgetsActive = widgetsActive && isDrawerNearClosed,
                                            onMovePinnedApp = { pkg, dir ->
                                                val cur = orderedPinnedApps.map { it.packageName }.toMutableList()
                                                val idx = cur.indexOf(pkg)
                                                val nxt = idx + dir
                                                if (idx >= 0 && nxt in cur.indices) {
                                                    val item = cur.removeAt(idx)
                                                    cur.add(nxt, item)
                                                    pinnedAppsOrder = cur
                                                    prefs.edit().putString("pinned_apps_order_v1", cur.joinToString(",")).apply()
                                                }
                                            },
                                            onSetWidgetSize = { wId, sz ->
                                                saveLeftWidgetSizesMap(leftWidgetSizes + (wId to sz))
                                            },
                                            onSetWidgetFont = { wId, fId ->
                                                saveLeftWidgetFontsMap(leftWidgetFonts + (wId to fId))
                                            },
                                            onSetWidgetStyle = { wId, stId ->
                                                saveLeftWidgetStylesMap(leftWidgetStyles + (wId to stId))
                                            },
                                            onMoveWidget = { wId, dir ->
                                                val cur = leftPageWidgets.toMutableList()
                                                val idx = cur.indexOf(wId)
                                                val nxt = idx + dir
                                                if (idx >= 0 && nxt in cur.indices) {
                                                    val item = cur.removeAt(idx)
                                                    cur.add(nxt, item)
                                                    saveLeftWidgetList(cur)
                                                }
                                            },
                                            onRemoveWidget = { wId ->
                                                saveLeftWidgetList(leftPageWidgets - wId)
                                            },
                                            onUnpinApp = { pkg ->
                                                pinnedPackages = pinnedPackages - pkg
                                                prefs.edit().putStringSet("pinned_pkgs", pinnedPackages).apply()
                                            },
                                            onChangeGlobeTilt2d = { deg ->
                                                globeTilt2dDeg = deg
                                                prefs.edit().putFloat("globe_tilt2d_deg", deg).apply()
                                            },
                                            onChangeGlobeRotX = { deg ->
                                                globeRotXDeg = deg
                                                prefs.edit().putFloat("globe_rotx_deg", deg).apply()
                                            },
                                            onChangeGlobeRotY = { deg ->
                                                globeRotYDeg = deg
                                                prefs.edit().putFloat("globe_roty_deg", deg).apply()
                                            },
                                            onLaunchApp = { pkg -> launchPackage(pkg) },
                                            onLongPressApp = { app -> contextMenuTarget = app to true },
                                            onLongPressHome = { showHomeQuickMenu = true },
                                            onOpenClockApp = { openClockApp() },
                                            onToggleTorch = { toggleTorch() },
                                            onToggleZen = {
                                                if (zenActive) {
                                                    zenActive = false
                                                    zenSeconds = 25 * 60
                                                } else {
                                                    zenSeconds = 25 * 60
                                                    zenActive = true
                                                }
                                            },
                                            onWaveTick = { performWaveTick() },
                                            onGlobeInteractionChanged = { unlocked ->
                                                isGlobeInteractionUnlocked = unlocked
                                            }
                                        )
                                    }
                                }

                                // Main Home Screen Page (Pure Horizontal Slide, ZERO Fade!)
                                if (leftPageProgress < 0.999f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                translationX = leftPageProgress * screenWidthPx
                                            }
                                    ) {
                                        HomeScreenView(
                                            pinnedApps = orderedPinnedApps,
                                            activeWidgets = activeWidgets,
                                            widgetSizes = widgetSizes,
                                            widgetFonts = widgetFonts,
                                            widgetStyles = widgetStyles,
                                            selectedFont = selectedFont,
                                            lomoFontFamily = lomoFontFamily,
                                            uppercaseHome = activePrebuiltFont.uppercaseHome,
                                            textAlignMode = textAlignMode,
                                            textSizeSp = textSizeSp,
                                            accentColor = accentColor,
                                            secondaryRetroColor = secondaryRetroColor,
                                            retro80sMode = retro80sMode,
                                            retro80sColorTheme = retro80sColorTheme,
                                            batteryBarStyle = batteryBarStyle,
                                            onChangeBatteryBarStyle = { st ->
                                                batteryBarStyle = st
                                                prefs.edit().putString("battery_bar_style", st).apply()
                                            },
                                            waveScrubMode = waveScrubMode,
                                            currentHourStr = currentHourStr,
                                            currentMinStr = currentMinStr,
                                            currentSecStr = currentSecStr,
                                            currentAmPmStr = currentAmPmStr,
                                            currentDateStr = currentDateStr,
                                            currentTimeMillis = currentTimeMillis,
                                            batteryPct = batteryPct,
                                            isCharging = isCharging,
                                            ramStatusText = ramStatusText,
                                            storageStatusText = storageStatusText,
                                            ramUsageFraction = ramUsageFraction,
                                            storageUsageFraction = storageUsageFraction,
                                            torchEnabled = torchEnabled,
                                            zenActive = zenActive,
                                            zenSeconds = zenSeconds,
                                            quickNote = quickNote,
                                            globeTheme = if (retro80sMode) "wireframe" else globeTheme,
                                            globeWireframeColor = if (retro80sMode) {
                                                when (retro80sColorTheme) {
                                                    "holomap" -> "#26FFFF"
                                                    "amber" -> "#FFB830"
                                                    else -> "#9EFF78"
                                                }
                                            } else globeWireframeColor,
                                            globeTilt2dDeg = globeTilt2dDeg,
                                            globeRotXDeg = globeRotXDeg,
                                            globeRotYDeg = globeRotYDeg,
                                            editMode = editMode,
                                            customPinnedOrderEnabled = customPinnedOrderEnabled,
                                            widgetsActive = widgetsActive && isDrawerNearClosed,
                                            onMovePinnedApp = { pkg, dir ->
                                                val cur = orderedPinnedApps.map { it.packageName }.toMutableList()
                                                val idx = cur.indexOf(pkg)
                                                val nxt = idx + dir
                                                if (idx >= 0 && nxt in cur.indices) {
                                                    val item = cur.removeAt(idx)
                                                    cur.add(nxt, item)
                                                    pinnedAppsOrder = cur
                                                    prefs.edit().putString("pinned_apps_order_v1", cur.joinToString(",")).apply()
                                                }
                                            },
                                            onSetWidgetSize = { wId, sz ->
                                                saveWidgetSizesMap(widgetSizes + (wId to sz))
                                            },
                                            onSetWidgetFont = { wId, fId ->
                                                saveWidgetFontsMap(widgetFonts + (wId to fId))
                                            },
                                            onSetWidgetStyle = { wId, stId ->
                                                saveWidgetStylesMap(widgetStyles + (wId to stId))
                                            },
                                            onMoveWidget = { wId, dir ->
                                                val cur = activeWidgets.toMutableList()
                                                val idx = cur.indexOf(wId)
                                                val nxt = idx + dir
                                                if (idx >= 0 && nxt in cur.indices) {
                                                    val item = cur.removeAt(idx)
                                                    cur.add(nxt, item)
                                                    saveWidgetList(cur)
                                                }
                                            },
                                            onRemoveWidget = { wId ->
                                                saveWidgetList(activeWidgets - wId)
                                            },
                                            onUnpinApp = { pkg ->
                                                pinnedPackages = pinnedPackages - pkg
                                                prefs.edit().putStringSet("pinned_pkgs", pinnedPackages).apply()
                                            },
                                            onChangeGlobeTilt2d = { deg ->
                                                globeTilt2dDeg = deg
                                                prefs.edit().putFloat("globe_tilt2d_deg", deg).apply()
                                            },
                                            onChangeGlobeRotX = { deg ->
                                                globeRotXDeg = deg
                                                prefs.edit().putFloat("globe_rotx_deg", deg).apply()
                                            },
                                            onChangeGlobeRotY = { deg ->
                                                globeRotYDeg = deg
                                                prefs.edit().putFloat("globe_roty_deg", deg).apply()
                                            },
                                            onLaunchApp = { pkg -> launchPackage(pkg) },
                                            onLongPressApp = { app -> contextMenuTarget = app to true },
                                            onLongPressHome = { showHomeQuickMenu = true },
                                            onOpenClockApp = { openClockApp() },
                                            onToggleTorch = { toggleTorch() },
                                            onToggleZen = {
                                                if (zenActive) {
                                                    zenActive = false
                                                    zenSeconds = 25 * 60
                                                } else {
                                                    zenSeconds = 25 * 60
                                                    zenActive = true
                                                }
                                            },
                                            onWaveTick = { performWaveTick() },
                                            onGlobeInteractionChanged = { unlocked ->
                                                isGlobeInteractionUnlocked = unlocked
                                            }
                                        )
                                    }
                                }

                                // App Drawer Overlay
                                if (drawerProgress > 0.001f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                translationY = (1f - drawerProgress) * screenHeightPx
                                                alpha = (drawerProgress * 1.35f).coerceIn(0f, 1f)
                                            }
                                    ) {
                                        AppDrawerView(
                                            apps = appsWithPinFlag,
                                            selectedFont = selectedFont,
                                            lomoFontFamily = lomoFontFamily,
                                            uppercaseHome = activePrebuiltFont.uppercaseHome,
                                            drawerTextSizeSp = drawerTextSizeSp,
                                            accentColor = accentColor,
                                            secondaryRetroColor = secondaryRetroColor,
                                            waveScrubMode = waveScrubMode,
                                            drawerWaveIntensity = drawerWaveIntensity,
                                            drawerBgMode = drawerBgMode,
                                            drawerBgIntensity = drawerBgIntensity,
                                            isDrawerOpenNow = drawerProgress > 0.85f,
                                            onDragDelta = { dy ->
                                                val deltaFrac = dy / (screenHeightPx * 0.55f)
                                                drawerProgress = (drawerProgress - deltaFrac).coerceIn(0f, 1f)
                                            },
                                            onSettle = { pulledPx ->
                                                animateDrawerTo(if (pulledPx > 95f) 0f else 1f)
                                            },
                                            onLaunchApp = { pkg ->
                                                animateDrawerTo(0f)
                                                launchPackage(pkg)
                                            },
                                            onLongPressApp = { app -> contextMenuTarget = app to false },
                                            onWaveTick = { performWaveTick() }
                                        )
                                    }
                                }
                            }
                        }

                        LauncherView.WIDGETS -> {
                            WidgetsManagerView(
                                activeWidgets = activeWidgets,
                                widgetSizes = widgetSizes,
                                widgetStyles = widgetStyles,
                                leftPageEnabled = enableLeftPage,
                                initialTargetLeftPage = isOnLeftPage,
                                leftPageWidgets = leftPageWidgets,
                                leftWidgetSizes = leftWidgetSizes,
                                leftWidgetStyles = leftWidgetStyles,
                                accentColor = accentColor,
                                secondaryRetroColor = secondaryRetroColor,
                                lomoFontFamily = selectedFont,
                                use24Hour = use24Hour,
                                batteryBarStyle = batteryBarStyle,
                                onChangeBatteryBarStyle = { st ->
                                    batteryBarStyle = st
                                    prefs.edit().putString("battery_bar_style", st).apply()
                                },
                                customPinnedOrderEnabled = customPinnedOrderEnabled,
                                quickNote = quickNote,
                                globeTheme = globeTheme,
                                globeWireframeColor = globeWireframeColor,
                                onToggleCustomPinnedOrder = {
                                    customPinnedOrderEnabled = !customPinnedOrderEnabled
                                    prefs.edit().putBoolean("custom_pinned_order_enabled", customPinnedOrderEnabled).apply()
                                },
                                onToggle24h = {
                                    use24Hour = !use24Hour
                                    prefs.edit().putBoolean("use_24h", use24Hour).apply()
                                },
                                onChangeQuickNote = { note ->
                                    quickNote = note
                                    prefs.edit().putString("quick_note", note).apply()
                                },
                                onChangeGlobeTheme = { th ->
                                    globeTheme = th
                                    prefs.edit().putString("globe_theme", th).apply()
                                },
                                onChangeGlobeWireframeColor = { hex ->
                                    globeWireframeColor = hex
                                    prefs.edit().putString("globe_wf_color", hex).apply()
                                },
                                onUpdateWidgets = { nextList -> saveWidgetList(nextList) },
                                onUpdateLeftWidgets = { nextList -> saveLeftWidgetList(nextList) },
                                onSetWidgetStyle = { wId, stId -> saveWidgetStylesMap(widgetStyles + (wId to stId)) },
                                onSetLeftWidgetStyle = { wId, stId -> saveLeftWidgetStylesMap(leftWidgetStyles + (wId to stId)) },
                                onCycleWidgetSize = {},
                                onSetWidgetSize = { wId, sz -> saveWidgetSizesMap(widgetSizes + (wId to sz)) },
                                onMoveWidget = { _, _ -> },
                                onBack = { currentView = LauncherView.HOME_AND_DRAWER }
                            )
                        }

                        LauncherView.SETTINGS -> {
                            LauncherSettingsView(
                                fontId = fontId,
                                textAlignMode = textAlignMode,
                                accentHex = accentHex,
                                textSizeSp = textSizeSp,
                                drawerTextSizeSp = drawerTextSizeSp,
                                use24Hour = use24Hour,
                                hideAndroidBar = hideAndroidBar,
                                enableLeftPage = enableLeftPage,
                                retro80sMode = retro80sMode,
                                retro80sColorTheme = retro80sColorTheme,
                                onChangeRetro80sColorTheme = { themeId ->
                                    retro80sColorTheme = themeId
                                    prefs.edit().putString("retro_80s_color_theme", themeId).apply()
                                },
                                onActivateHolomapTheme = {
                                    if (retro80sMode && retro80sColorTheme == "holomap") {
                                        retro80sMode = false
                                        prefs.edit().putBoolean("retro_80s_green_mode", false).apply()
                                    } else {
                                        retro80sMode = true
                                        retro80sColorTheme = "holomap"
                                        prefs.edit()
                                            .putBoolean("retro_80s_green_mode", true)
                                            .putString("retro_80s_color_theme", "holomap")
                                            .apply()
                                    }
                                },
                                batteryBarStyle = batteryBarStyle,
                                pauseWhenAppOpen = pauseWhenAppOpen,
                                customPinnedOrderEnabled = customPinnedOrderEnabled,
                                waveScrubMode = waveScrubMode,
                                drawerWaveIntensity = drawerWaveIntensity,
                                wallpaperPreset = wallpaperPreset,
                                wallpaperDimPercent = wallpaperDimPercent,
                                hasCustomUserImage = customWallpaperBitmap != null,
                                wpSpeed = wpSpeed,
                                wpDensity = wpDensity,
                                wpOpacity = wpOpacity,
                                wpScale = wpScale,
                                settingsBgMode = settingsBgMode,
                                settingsBgIntensity = settingsBgIntensity,
                                drawerBgMode = drawerBgMode,
                                drawerBgIntensity = drawerBgIntensity,
                                lomoFontFamily = lomoFontFamily,
                                launcherFont = selectedFont,
                                onChangeFont = { newFontId ->
                                    fontId = newFontId
                                    prefs.edit().putString("font_id", newFontId).apply()
                                },
                                onChangeAlign = { newAlign ->
                                    textAlignMode = newAlign
                                    prefs.edit().putString("align_mode", newAlign).apply()
                                },
                                onChangeAccent = { newHex ->
                                    accentHex = newHex
                                    prefs.edit().putString("accent_hex", newHex).apply()
                                },
                                onChangeTextSize = { newSp ->
                                    if (retro80sMode) {
                                        crtTextSizeSp = newSp
                                        prefs.edit().putInt("crt80s_text_size_v1", newSp).apply()
                                    } else {
                                        regTextSizeSp = newSp
                                        prefs.edit().putInt("text_size_sp", newSp).apply()
                                    }
                                },
                                onChangeDrawerTextSize = { newSp ->
                                    drawerTextSizeSp = newSp
                                    prefs.edit().putInt("drawer_text_size_sp", newSp).apply()
                                },
                                onToggle24h = {
                                    use24Hour = !use24Hour
                                    prefs.edit().putBoolean("use_24h", use24Hour).apply()
                                },
                                onToggleAndroidBar = {
                                    hideAndroidBar = !hideAndroidBar
                                    prefs.edit().putBoolean("hide_status_bar", hideAndroidBar).apply()
                                },
                                onToggleLeftPage = {
                                    enableLeftPage = !enableLeftPage
                                    prefs.edit().putBoolean("enable_left_page", enableLeftPage).apply()
                                },
                                onToggleRetro80sMode = {
                                    retro80sMode = !retro80sMode
                                    prefs.edit().putBoolean("retro_80s_green_mode", retro80sMode).apply()
                                },
                                onChangeBatteryBarStyle = { st ->
                                    batteryBarStyle = st
                                    prefs.edit().putString("battery_bar_style", st).apply()
                                },
                                onTogglePauseWhenAppOpen = {
                                    pauseWhenAppOpen = !pauseWhenAppOpen
                                    prefs.edit().putBoolean("pause_when_app_open", pauseWhenAppOpen).apply()
                                },
                                onToggleCustomPinnedOrder = {
                                    customPinnedOrderEnabled = !customPinnedOrderEnabled
                                    prefs.edit().putBoolean("custom_pinned_order_enabled", customPinnedOrderEnabled).apply()
                                },
                                onChangeWaveMode = { newMode ->
                                    waveScrubMode = newMode
                                    prefs.edit().putString("wave_mode", newMode).apply()
                                },
                                onChangeDrawerWaveIntensity = { nextVal ->
                                    drawerWaveIntensity = nextVal
                                    prefs.edit().putFloat("drawer_wave_intensity", nextVal).apply()
                                },
                                onChangeWallpaperPreset = { preset ->
                                    wallpaperPreset = preset
                                    prefs.edit().putString("wp_preset", preset).apply()
                                },
                                onChangeWallpaperDim = { dim ->
                                    wallpaperDimPercent = dim
                                    prefs.edit().putInt("wp_dim_pct", dim).apply()
                                },
                                onChangeWpSpeed = { v ->
                                    wpSpeed = v
                                    prefs.edit().putFloat("wp_speed", v).apply()
                                },
                                onChangeWpDensity = { v ->
                                    wpDensity = v
                                    prefs.edit().putFloat("wp_density", v).apply()
                                },
                                onChangeWpOpacity = { v ->
                                    wpOpacity = v
                                    prefs.edit().putFloat("wp_opacity", v).apply()
                                },
                                onChangeWpScale = { v ->
                                    wpScale = v
                                    prefs.edit().putFloat("wp_scale", v).apply()
                                },
                                onResetWpTuning = {
                                    wpSpeed = 1.0f
                                    wpDensity = 1.0f
                                    wpOpacity = 1.0f
                                    wpScale = 1.0f
                                    prefs.edit()
                                        .putFloat("wp_speed", 1.0f)
                                        .putFloat("wp_density", 1.0f)
                                        .putFloat("wp_opacity", 1.0f)
                                        .putFloat("wp_scale", 1.0f)
                                        .apply()
                                },
                                onChangeSettingsBgMode = { mode ->
                                    settingsBgMode = mode
                                    prefs.edit().putString("settings_bg_mode", mode).apply()
                                },
                                onChangeSettingsBgIntensity = { pct ->
                                    settingsBgIntensity = pct
                                    prefs.edit().putInt("settings_bg_intensity", pct).apply()
                                },
                                onChangeDrawerBgMode = { mode ->
                                    drawerBgMode = mode
                                    prefs.edit().putString("drawer_bg_mode", mode).apply()
                                },
                                onChangeDrawerBgIntensity = { pct ->
                                    drawerBgIntensity = pct
                                    prefs.edit().putInt("drawer_bg_intensity", pct).apply()
                                },
                                onOpenSystemWallpaperPicker = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_SET_WALLPAPER))
                                    } catch (_: Exception) {
                                    }
                                },
                                onPickCustomWallpaperImage = {
                                    try {
                                        pickWallpaperLauncher.launch("image/*")
                                    } catch (_: Exception) {
                                    }
                                },
                                onRequestDefaultHomeRole = {
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            val rm = context.getSystemService(RoleManager::class.java)
                                            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
                                                val intent = rm.createRequestRoleIntent(RoleManager.ROLE_HOME)
                                                (context as? Activity)?.startActivityForResult(intent, 9001)
                                            } else {
                                                context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                                            }
                                        } else {
                                            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                                        }
                                    } catch (_: Exception) {
                                    }
                                },
                                onBack = { currentView = LauncherView.HOME_AND_DRAWER }
                            )
                        }
                    }
                }
            }

            // ====================================================================
            // FLUID SPRING-ANIMATED LONG-TAP HOME SCREEN "ACTIONS" MENU
            // ====================================================================
            val quickMenuProgress by rememberIosPopupProgress(
                isVisible = showHomeQuickMenu,
                label = "iosQuickMenuSpring"
            )
            if (showHomeQuickMenu || quickMenuProgress > 0.002f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (0.84f * quickMenuProgress.coerceIn(0f, 1f)).coerceIn(0f, 0.84f)))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showHomeQuickMenu = false }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val modalShape = RoundedCornerShape(if (retro80sMode) 0.dp else 16.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .iosPopupWindowLayer(progress = quickMenuProgress, initialScale = 0.80f)
                            .retroNeonBorder(
                                if (retro80sMode) 1.5.dp else 1.dp,
                                if (retro80sMode) accentColor else Color(0xFF2E2E2E),
                                modalShape,
                                glowEnabled = retro80sMode
                            )
                            .background(if (retro80sMode) Color(0xFF040404) else Color(0xFF0A0A0A), modalShape)
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
                            Text(
                                text = "Actions",
                                color = if (retro80sMode) accentColor else Color.White,
                                fontSize = 16.sp,
                                fontFamily = selectedFont,
                                fontWeight = FontWeight.Bold,
                                style = glowTextStyle(accentColor)
                            )
                            Text(
                                text = "DONE",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontFamily = selectedFont,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .retroNeonBorder(
                                        1.dp,
                                        if (retro80sMode) secondaryRetroColor else Color.White,
                                        RoundedCornerShape(if (retro80sMode) 0.dp else 6.dp),
                                        glowEnabled = retro80sMode
                                    )
                                    .background(
                                        if (retro80sMode) secondaryRetroColor else Color.White,
                                        RoundedCornerShape(if (retro80sMode) 0.dp else 6.dp)
                                    )
                                    .clickable { showHomeQuickMenu = false }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(if (retro80sMode) accentColor.copy(alpha = 0.5f) else Color(0xFF1F1F1F))
                        )

                        QuickMenuButton(
                            label = if (retro80sMode) "Widget Configuration" else "Widget Manager",
                            badge = "",
                            accentColor = accentColor,
                            lomoFontFamily = selectedFont,
                            onClick = {
                                showHomeQuickMenu = false
                                currentView = LauncherView.WIDGETS
                            }
                        )

                        QuickMenuButton(
                            label = if (editMode) "Exit Edit Mode" else "Edit Mode",
                            badge = "",
                            accentColor = accentColor,
                            lomoFontFamily = selectedFont,
                            onClick = {
                                showHomeQuickMenu = false
                                editMode = !editMode
                            }
                        )

                        QuickMenuButton(
                            label = if (retro80sMode) "Nocturna Configuration" else "Nocturnaa Settings",
                            badge = "",
                            accentColor = accentColor,
                            lomoFontFamily = selectedFont,
                            onClick = {
                                showHomeQuickMenu = false
                                currentView = LauncherView.SETTINGS
                            }
                        )

                        QuickMenuButton(
                            label = "Phone Settings",
                            badge = "",
                            accentColor = accentColor,
                            lomoFontFamily = selectedFont,
                            onClick = {
                                showHomeQuickMenu = false
                                try {
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                } catch (_: Exception) {
                                }
                            }
                        )
                    }
                }
            }

            // ====================================================================
            // FLUID SPRING-ANIMATED LONG-PRESS APP CONTEXT MENU MODAL
            // ====================================================================
            var lastContextMenuTarget by remember { mutableStateOf<Pair<AppEntry, Boolean>?>(null) }
            if (contextMenuTarget != null) {
                lastContextMenuTarget = contextMenuTarget
            }
            val appMenuProgress by rememberIosPopupProgress(
                isVisible = contextMenuTarget != null,
                label = "iosAppMenuSpring"
            )
            val activeTargetPair = contextMenuTarget ?: if (appMenuProgress > 0.002f) lastContextMenuTarget else null
            activeTargetPair?.let { (targetApp, isFromHome) ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (0.84f * appMenuProgress.coerceIn(0f, 1f)).coerceIn(0f, 0.84f)))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { contextMenuTarget = null }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val appModalShape = RoundedCornerShape(if (retro80sMode) 0.dp else 16.dp)
                    val currentlyPinned = pinnedPackages.contains(targetApp.packageName)
                    val pinActionLabel = if (isFromHome || currentlyPinned) {
                        "Remove from Pinned Apps"
                    } else {
                        "Add to Home Screen"
                    }
                    val pinActionSymbol = if (isFromHome || currentlyPinned) "−" else "+"

                    val onPinToggleClick: () -> Unit = {
                        if (isFromHome || currentlyPinned) {
                            pinnedPackages = pinnedPackages - targetApp.packageName
                            pinnedAppsOrder = pinnedAppsOrder.filter { it != targetApp.packageName }
                            prefs.edit()
                                .putStringSet("pinned_pkgs", pinnedPackages)
                                .putString("pinned_apps_order_v1", pinnedAppsOrder.joinToString(","))
                                .apply()
                            contextMenuTarget = null
                        } else {
                            val candidateWidgetsList = if (activeWidgets.contains("pinned_apps")) activeWidgets else activeWidgets + "pinned_apps"
                            val newPinnedCount = currentPinnedAppsCount() + 1
                            if (candidateWidgetsList.isNotEmpty() && !canFitOnHomeScreen(candidateWidgetsList, widgetSizes, newPinnedCount, textSizeSp)) {
                                contextMenuTarget = null
                                triggerNoSpaceLeftPopup(
                                    "No space left on the Home Screen to pin another app with your current widgets and ${textSizeSp}SP app scale."
                                )
                            } else {
                                pinnedPackages = pinnedPackages + targetApp.packageName
                                val nextOrder = orderedPinnedApps.map { it.packageName }.filter { it != targetApp.packageName } + targetApp.packageName
                                pinnedAppsOrder = nextOrder
                                if (!activeWidgets.contains("pinned_apps")) {
                                    saveWidgetList(candidateWidgetsList)
                                }
                                prefs.edit()
                                    .putStringSet("pinned_pkgs", pinnedPackages)
                                    .putString("pinned_apps_order_v1", nextOrder.joinToString(","))
                                    .apply()
                                contextMenuTarget = null
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .iosPopupWindowLayer(progress = appMenuProgress, initialScale = 0.80f)
                            .retroNeonBorder(
                                if (retro80sMode) 1.5.dp else 1.dp,
                                if (retro80sMode) accentColor else Color(0xFF333333),
                                appModalShape,
                                glowEnabled = retro80sMode
                            )
                            .background(if (retro80sMode) Color(0xFF040404) else Color(0xFF0A0A0A), appModalShape)
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
                            Text(
                                text = targetApp.label,
                                color = if (retro80sMode) accentColor else Color.White,
                                fontSize = 16.sp,
                                fontFamily = selectedFont,
                                fontWeight = FontWeight.Bold,
                                style = glowTextStyle(accentColor),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Text(
                                text = "DONE",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontFamily = selectedFont,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .retroNeonBorder(
                                        1.dp,
                                        if (retro80sMode) secondaryRetroColor else Color.White,
                                        RoundedCornerShape(if (retro80sMode) 0.dp else 6.dp),
                                        glowEnabled = retro80sMode
                                    )
                                    .background(
                                        if (retro80sMode) secondaryRetroColor else Color.White,
                                        RoundedCornerShape(if (retro80sMode) 0.dp else 6.dp)
                                    )
                                    .clickable { contextMenuTarget = null }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(if (retro80sMode) accentColor.copy(alpha = 0.5f) else Color(0xFF1F1F1F))
                        )

                        val rowShape = RoundedCornerShape(if (retro80sMode) 0.dp else 10.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .retroNeonBorder(1.dp, if (retro80sMode) accentColor else Color(0xFF262626), rowShape, glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                                .background(Color.Black, rowShape)
                                .clickable { onPinToggleClick() }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pinActionLabel,
                                color = if (retro80sMode) accentColor else Color.White,
                                fontSize = 13.sp,
                                fontFamily = selectedFont,
                                style = glowTextStyle(accentColor, 14f)
                            )
                            Text(
                                text = pinActionSymbol,
                                color = secondaryRetroColor,
                                fontSize = 15.sp,
                                fontFamily = selectedFont,
                                fontWeight = FontWeight.Bold,
                                style = glowTextStyle(secondaryRetroColor, 14f)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .retroNeonBorder(1.dp, if (retro80sMode) accentColor else Color(0xFF262626), rowShape, glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                                .background(Color.Black, rowShape)
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
                                color = if (retro80sMode) accentColor else Color.White,
                                fontSize = 13.sp,
                                fontFamily = selectedFont,
                                style = glowTextStyle(accentColor, 14f)
                            )
                            Text(
                                text = "↗",
                                color = secondaryRetroColor,
                                fontSize = 13.sp,
                                fontFamily = selectedFont,
                                fontWeight = FontWeight.Bold,
                                style = glowTextStyle(secondaryRetroColor, 14f)
                            )
                        }

                        if (!targetApp.isSystemApp) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .retroNeonBorder(1.dp, if (retro80sMode) secondaryRetroColor else Color(0xFF451A1A), rowShape, glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                                    .background(if (retro80sMode) Color.Black else Color(0xFF140707), rowShape)
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
                                    color = if (retro80sMode) secondaryRetroColor else Color(0xFFF87171),
                                    fontSize = 13.sp,
                                    fontFamily = selectedFont,
                                    style = glowTextStyle(secondaryRetroColor, 14f)
                                )
                                Text(
                                    text = "×",
                                    color = if (retro80sMode) secondaryRetroColor else Color(0xFFF87171),
                                    fontSize = 14.sp,
                                    fontFamily = selectedFont,
                                    fontWeight = FontWeight.Bold,
                                    style = glowTextStyle(secondaryRetroColor, 14f)
                                )
                            }
                        }
                    }
                }
            }

            // ====================================================================
            // "NO SPACE LEFT" BOUNDARY POPUP MODAL
            // ====================================================================
            val noSpaceProgress by rememberIosPopupProgress(
                isVisible = showNoSpacePopup,
                label = "iosNoSpaceAlertSpring"
            )
            if (showNoSpacePopup || noSpaceProgress > 0.002f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (0.84f * noSpaceProgress.coerceIn(0f, 1f)).coerceIn(0f, 0.84f)))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showNoSpacePopup = false }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val alertShape = RoundedCornerShape(if (retro80sMode) 0.dp else 16.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .iosPopupWindowLayer(progress = noSpaceProgress, initialScale = 0.80f)
                            .retroNeonBorder(if (retro80sMode) 1.5.dp else 1.dp, accentColor, alertShape, glowEnabled = retro80sMode)
                            .background(if (retro80sMode) Color(0xFF040404) else Color(0xFF0A0A0A), alertShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* consume inside modal */ }
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "No Space Left",
                            color = accentColor,
                            fontSize = 15.sp,
                            fontFamily = selectedFont,
                            fontWeight = FontWeight.Bold,
                            style = glowTextStyle(accentColor)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(if (retro80sMode) accentColor.copy(alpha = 0.5f) else Color(0xFF222222))
                        )

                        Text(
                            text = noSpacePopupMessage,
                            color = if (retro80sMode) accentColor else Color(0xFFE5E5E5),
                            fontSize = 12.sp,
                            fontFamily = selectedFont,
                            lineHeight = 17.sp,
                            style = glowTextStyle(accentColor, 12f)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .retroNeonBorder(1.dp, secondaryRetroColor, RoundedCornerShape(if (retro80sMode) 0.dp else 8.dp), glowEnabled = retro80sMode)
                                .background(if (retro80sMode) secondaryRetroColor else Color(0xFF141414), RoundedCornerShape(if (retro80sMode) 0.dp else 8.dp))
                                .clickable { showNoSpacePopup = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "OK",
                                color = if (retro80sMode) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontFamily = selectedFont,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
    lomoFontFamily: FontFamily = FontFamily.Monospace,
    onClick: () -> Unit
) {
    val is80s = LocalRetro80sMode.current
    val secondaryCol = rememberRetroSecondaryColor(accentColor)
    val shape = RoundedCornerShape(if (is80s) 0.dp else 10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .retroNeonBorder(1.dp, if (is80s) accentColor else Color(0xFF262626), shape, glowEnabled = is80s, glowRadiusDp = 4.dp)
            .background(Color.Black, shape)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (is80s) accentColor else Color.White,
            fontSize = 13.sp,
            fontFamily = lomoFontFamily,
            fontWeight = if (is80s) FontWeight.Bold else FontWeight.Normal,
            style = glowTextStyle(accentColor, 16f)
        )
        Text(
            text = if (is80s) "[+]" else "▸",
            color = secondaryCol,
            fontSize = 12.sp,
            fontFamily = lomoFontFamily,
            fontWeight = FontWeight.Bold,
            style = glowTextStyle(secondaryCol, 16f)
        )
    }
}

@Composable
fun Retro80sCrtScreenEdgeGlowOverlay() {
    val theme = LocalRetro80sColorTheme.current
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (theme == "holomap") {
            // Subtle Glowing Cyan Dot-Matrix Grid in the background (exact match to 2nd reference image!)
            val gridStep = 18.dp.toPx().coerceAtLeast(12f)
            val dotRadius = 1.05.dp.toPx()
            val glowRadius = 2.4.dp.toPx()
            var y = gridStep * 0.5f
            while (y < h) {
                var x = gridStep * 0.5f
                while (x < w) {
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                        radius = glowRadius,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color(0xFF26FFFF).copy(alpha = 0.42f),
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                    x += gridStep
                }
                y += gridStep
            }
        } else {
            // Subtle additive phosphor raster lines that never dim foreground text
            val scanStep = 4.dp.toPx().coerceAtLeast(3f)
            val tint = if (theme == "amber") Color(0xFFFF9900).copy(alpha = 0.035f) else Color(0xFF55FF33).copy(alpha = 0.035f)
            var y = 0f
            while (y < h) {
                drawLine(
                    color = tint,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
                y += scanStep
            }
        }
    }
}

fun getWidgetStyleChoices(widgetId: String): List<Pair<String, String>> = when (widgetId) {
    "clock" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "MODERN DOT", "cyber_hud" to "HOLOMAP")
    "battery_bar" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "CLEAN BAR", "cyber_hud" to "HOLOMAP")
    "week_strip" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "7-DAY STRIP", "cyber_hud" to "HOLOMAP")
    "retro_calendar" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "DOT MATRIX", "cyber_hud" to "HOLOMAP")
    "year_matrix" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "365 DOTS", "cyber_hud" to "HOLOMAP")
    "retro_flip_clock" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "SPLIT-FLAP", "cyber_hud" to "HOLOMAP")
    "analog_chrono" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "WATCH DIAL", "cyber_hud" to "HOLOMAP")
    "precision_chrono" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "SPLIT-LAP", "cyber_hud" to "HOLOMAP")
    "globe" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "HOLO GLOBE", "cyber_hud" to "HOLOMAP")
    "torch" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "CLEAN PILL", "cyber_hud" to "HOLOMAP")
    "storage" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "DUAL BARS", "cyber_hud" to "HOLOMAP")
    "zen" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "MINIMAL", "cyber_hud" to "HOLOMAP")
    "quote" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "EDITORIAL", "cyber_hud" to "HOLOMAP")
    "scratchpad" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "MINIMAL >", "cyber_hud" to "HOLOMAP")
    "pinned_apps" -> listOf("80s_retro" to "OLD SCHOOL", "default" to "CLEAN LIST", "cyber_hud" to "HOLOMAP")
    else -> listOf("80s_retro" to "OLD SCHOOL", "default" to "DEFAULT", "cyber_hud" to "HOLOMAP")
}

val BATTERY_BAR_STYLES: List<Pair<String, String>> = listOf(
    "retro_blocks" to "OLD SCHOOL",
    "holomap_slash" to "HOLOMAP ///",
    "segmented_led" to "LED CELLS",
    "pill" to "SMOOTH PILL",
    "ruler_ticks" to "TICKS"
)

@Composable
fun RetroBatteryProgressBar(
    batteryPct: Int,
    barStyle: String,
    accentColor: Color,
    retro80sMode: Boolean = false,
    sizeMode: String = "standard",
    modifier: Modifier = Modifier
) {
    val theme = LocalRetro80sColorTheme.current
    val isHolomap = (retro80sMode && theme == "holomap") || barStyle == "holomap_slash"
    val barColor = if (isHolomap) Color(0xFFFF3B14) else accentColor
    val barHeight = when (sizeMode) {
        "compact" -> 14.dp
        "large" -> 26.dp
        "xlarge" -> 30.dp
        "max" -> 34.dp
        else -> 20.dp
    }
    val clampedPct = batteryPct.coerceIn(0, 100)
    val effectiveStyle = if (retro80sMode && theme == "holomap") "holomap_slash" else barStyle

    Canvas(
        modifier = modifier
            .height(barHeight)
    ) {
        val w = size.width
        val h = size.height
        val frac = (clampedPct / 100f).coerceIn(0f, 1f)

        when (effectiveStyle) {
            "holomap_slash" -> {
                // GLOWING RED DIAGONAL-HATCHED BAR (Exact match to PEAKS/VALLIES/ROCKS bars in 2nd reference image!)
                val fillW = (w * frac).coerceAtLeast(4f)
                // Soft outer red neon glow
                drawIntoCanvas { canvas ->
                    val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        style = android.graphics.Paint.Style.FILL
                        color = android.graphics.Color.argb(115, 255, 30, 0)
                        maskFilter = BlurMaskFilter(8.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.nativeCanvas.drawRect(0f, 1.dp.toPx(), fillW, h - 1.dp.toPx(), glowPaint)
                }
                // Dark red base track for the filled region
                drawRect(
                    color = Color(0xFF3A0800),
                    topLeft = Offset(0f, 1.dp.toPx()),
                    size = Size(fillW, h - 2.dp.toPx())
                )
                // Faint unlit track for remaining region
                if (fillW < w) {
                    drawRect(
                        color = Color(0xFF1A0500),
                        topLeft = Offset(fillW, 1.dp.toPx()),
                        size = Size(w - fillW, h - 2.dp.toPx())
                    )
                }
                // Crisp glowing red diagonal hatch stripes (///) across the filled region
                val stripeStep = 5.2.dp.toPx().coerceAtLeast(4f)
                val slant = h * 0.55f
                val strokeW = 2.3.dp.toPx().coerceAtLeast(1.8f)
                var x = -slant
                while (x < fillW) {
                    val xStart = x.coerceIn(0f, fillW)
                    val xEnd = (x + slant).coerceIn(0f, fillW)
                    if (xEnd > xStart) {
                        drawLine(
                            color = barColor,
                            start = Offset(xStart, h - 1.5.dp.toPx()),
                            end = Offset(xEnd, 1.5.dp.toPx()),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Square
                        )
                    }
                    x += stripeStep
                }
            }

            "retro_blocks" -> {
                // Framed Old School Blocky Progress Bar with Dithered Unlit Blocks
                val strokePx = 1.4.dp.toPx()
                if (retro80sMode) {
                    drawIntoCanvas { canvas ->
                        val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = strokePx * 2f
                            color = android.graphics.Color.argb(
                                150,
                                (barColor.red * 255).toInt(),
                                (barColor.green * 255).toInt(),
                                (barColor.blue * 255).toInt()
                            )
                            maskFilter = BlurMaskFilter(6.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                        }
                        canvas.nativeCanvas.drawRect(0f, 0f, w, h, glowPaint)
                    }
                }
                drawRect(
                    color = barColor,
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    style = Stroke(width = strokePx)
                )
                val pad = 3.2.dp.toPx()
                val innerW = (w - pad * 2f).coerceAtLeast(1f)
                val innerH = (h - pad * 2f).coerceAtLeast(1f)
                val totalBlocks = 18
                val gap = 2.2.dp.toPx()
                val blockW = ((innerW - gap * (totalBlocks - 1)) / totalBlocks.toFloat()).coerceAtLeast(2f)
                val exactBlocks = frac * totalBlocks

                for (i in 0 until totalBlocks) {
                    val bx = pad + i * (blockW + gap)
                    val by = pad
                    if (i < exactBlocks.toInt()) {
                        drawRect(
                            color = barColor,
                            topLeft = Offset(bx, by),
                            size = Size(blockW, innerH)
                        )
                    } else if (i == exactBlocks.toInt() && (exactBlocks - i) > 0.25f) {
                        // Half-dithered transition block
                        val dotStep = 2.2.dp.toPx().coerceAtLeast(1.5f)
                        var dy = by
                        var r = 0
                        while (dy < by + innerH) {
                            var dx = bx
                            var c = 0
                            while (dx < bx + blockW) {
                                if ((r + c) % 2 == 0) {
                                    drawRect(
                                        color = barColor.copy(alpha = 0.78f),
                                        topLeft = Offset(dx, dy),
                                        size = Size(dotStep * 0.75f, dotStep * 0.75f)
                                    )
                                }
                                dx += dotStep
                                c++
                            }
                            dy += dotStep
                            r++
                        }
                    } else {
                        // Unlit stippled dithered block
                        val dotStep = 2.6.dp.toPx().coerceAtLeast(1.8f)
                        var dy = by
                        var r = 0
                        while (dy < by + innerH) {
                            var dx = bx
                            var c = 0
                            while (dx < bx + blockW) {
                                if ((r + c) % 2 == 0) {
                                    drawRect(
                                        color = barColor.copy(alpha = 0.22f),
                                        topLeft = Offset(dx, dy),
                                        size = Size(dotStep * 0.55f, dotStep * 0.55f)
                                    )
                                }
                                dx += dotStep
                                c++
                            }
                            dy += dotStep
                            r++
                        }
                    }
                }
            }

            "segmented_led" -> {
                val cells = 24
                val gap = 2.dp.toPx()
                val cellW = ((w - gap * (cells - 1)) / cells.toFloat()).coerceAtLeast(2f)
                val litCount = (frac * cells).roundToInt()
                for (i in 0 until cells) {
                    val cx = i * (cellW + gap)
                    drawRect(
                        color = if (i < litCount) barColor else barColor.copy(alpha = 0.16f),
                        topLeft = Offset(cx, h * 0.15f),
                        size = Size(cellW, h * 0.70f)
                    )
                }
            }

            "ruler_ticks" -> {
                val ticks = 40
                val step = w / ticks.toFloat()
                val litTicks = (frac * ticks).roundToInt()
                for (i in 0..ticks) {
                    val x = (i * step).coerceIn(0f, w)
                    val isMajor = i % 5 == 0
                    val th = if (isMajor) h * 0.85f else h * 0.48f
                    drawLine(
                        color = if (i <= litTicks) barColor else barColor.copy(alpha = 0.20f),
                        start = Offset(x, (h - th) * 0.5f),
                        end = Offset(x, (h + th) * 0.5f),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                    )
                }
            }

            else -> {
                // Smooth Pill
                val trackH = (h * 0.45f).coerceAtLeast(6.dp.toPx())
                val cy = h * 0.5f
                drawRoundRect(
                    color = barColor.copy(alpha = 0.18f),
                    topLeft = Offset(0f, cy - trackH * 0.5f),
                    size = Size(w, trackH),
                    cornerRadius = CornerRadius(trackH * 0.5f, trackH * 0.5f)
                )
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(0f, cy - trackH * 0.5f),
                    size = Size((w * frac).coerceAtLeast(trackH), trackH),
                    cornerRadius = CornerRadius(trackH * 0.5f, trackH * 0.5f)
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenView(
    pinnedApps: List<AppEntry>,
    activeWidgets: List<String>,
    widgetSizes: Map<String, String>,
    widgetFonts: Map<String, String>,
    widgetStyles: Map<String, String> = emptyMap(),
    selectedFont: FontFamily,
    lomoFontFamily: FontFamily,
    uppercaseHome: Boolean,
    textAlignMode: String,
    textSizeSp: Int,
    accentColor: Color,
    secondaryRetroColor: Color = accentColor,
    retro80sMode: Boolean = false,
    retro80sColorTheme: String = "green",
    batteryBarStyle: String = "retro_blocks",
    onChangeBatteryBarStyle: (String) -> Unit = {},
    waveScrubMode: String,
    currentHourStr: String,
    currentMinStr: String,
    currentSecStr: String,
    currentAmPmStr: String,
    currentDateStr: String,
    currentTimeMillis: Long,
    batteryPct: Int,
    isCharging: Boolean,
    ramStatusText: String,
    storageStatusText: String,
    ramUsageFraction: Float,
    storageUsageFraction: Float,
    torchEnabled: Boolean,
    zenActive: Boolean,
    zenSeconds: Int,
    quickNote: String,
    globeTheme: String,
    globeWireframeColor: String,
    globeTilt2dDeg: Float,
    globeRotXDeg: Float,
    globeRotYDeg: Float,
    editMode: Boolean,
    customPinnedOrderEnabled: Boolean = true,
    widgetsActive: Boolean = true,
    onMovePinnedApp: (String, Int) -> Unit = { _, _ -> },
    onSetWidgetSize: (String, String) -> Unit,
    onSetWidgetFont: (String, String) -> Unit,
    onSetWidgetStyle: (String, String) -> Unit = { _, _ -> },
    onMoveWidget: (String, Int) -> Unit,
    onRemoveWidget: (String) -> Unit,
    onUnpinApp: (String) -> Unit,
    onChangeGlobeTilt2d: (Float) -> Unit,
    onChangeGlobeRotX: (Float) -> Unit,
    onChangeGlobeRotY: (Float) -> Unit,
    onLaunchApp: (String) -> Unit,
    onLongPressApp: (AppEntry) -> Unit,
    onLongPressHome: () -> Unit,
    onOpenClockApp: () -> Unit,
    onToggleTorch: () -> Unit,
    onToggleZen: () -> Unit,
    onWaveTick: () -> Unit,
    onGlobeInteractionChanged: (Boolean) -> Unit = {}
) {
    val density = LocalDensity.current
    var waveFloatIndex by remember { mutableStateOf<Float?>(null) }
    var lastHoveredIndex by remember { mutableStateOf<Int?>(null) }
    var expandedFontWidgetIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scrollState = rememberScrollState()

    val isHolomapMode = retro80sMode && retro80sColorTheme == "holomap"
    val primaryCol = accentColor // Glowing Cyan in holomap, Green in green, Amber in amber
    val secondaryCol = secondaryRetroColor // Glowing Red in holomap

    val textAlign = when (textAlignMode) {
        "center" -> TextAlign.Center
        "right" -> TextAlign.End
        else -> TextAlign.Start
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = { onLongPressHome() }
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (editMode) Modifier.verticalScroll(scrollState) else Modifier),
            verticalArrangement = if (editMode) Arrangement.spacedBy(12.dp) else Arrangement.SpaceBetween
        ) {
            activeWidgets.forEach { widgetId ->
                val sizeMode = widgetSizes[widgetId] ?: "standard"
                val wFontId = widgetFonts[widgetId] ?: "lomo_pixel"
                val wPrebuiltFont = remember(wFontId) { PrebuiltFont.fromId(wFontId) }
                val wFontFamily = remember(wPrebuiltFont, lomoFontFamily) {
                    wPrebuiltFont.resolveFontFamily(lomoFontFamily)
                }
                val rawWStyle = widgetStyles[widgetId] ?: if (widgetId == "battery_bar") "80s_retro" else "default"
                val wStyle = when {
                    isHolomapMode -> "cyber_hud"
                    retro80sMode && rawWStyle == "default" -> "80s_retro"
                    else -> rawWStyle
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (editMode) {
                                val outerEditShape = RoundedCornerShape(if (retro80sMode) 0.dp else 8.dp)
                                Modifier
                                    .retroNeonBorder(
                                        if (retro80sMode) 1.4.dp else 1.dp,
                                        if (retro80sMode) primaryCol else Color(0xFF383838),
                                        outerEditShape,
                                        glowEnabled = retro80sMode,
                                        glowRadiusDp = 5.dp
                                    )
                                    .background(
                                        if (retro80sMode) Color(0xFF040404) else Color(0xFF0A0A0A),
                                        outerEditShape
                                    )
                                    .padding(8.dp)
                            } else {
                                Modifier.combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {},
                                    onLongClick = { onLongPressHome() }
                                )
                            }
                        )
                ) {
                    Column {
                        if (editMode) {
                            val editBtnShape = RoundedCornerShape(if (retro80sMode) 0.dp else 4.dp)
                            val editBoxShape = RoundedCornerShape(if (retro80sMode) 0.dp else 5.dp)
                            val sizeChoices = when (widgetId) {
                                "clock", "pinned_apps", "retro_flip_clock", "analog_chrono", "precision_chrono", "globe" -> listOf(
                                    "compact" to "S",
                                    "standard" to "M",
                                    "large" to "L",
                                    "xlarge" to "XL",
                                    "max" to "MAX"
                                )
                                else -> listOf(
                                    "compact" to "S",
                                    "standard" to "M",
                                    "large" to "L"
                                )
                            }
                            val allWidgetFonts = remember { PrebuiltFont.values().toList() }
                            val activeWidgetFont = remember(wFontId) { PrebuiltFont.fromId(wFontId) }
                            val isWidgetFontMenuExpanded = expandedFontWidgetIds.contains(widgetId)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Row 1: Reorder (↑ / ↓) + Direct Size Pills + Remove (×)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val idx = activeWidgets.indexOf(widgetId)
                                        if (idx > 0) {
                                            Text(
                                                text = "↑",
                                                color = if (retro80sMode) primaryCol else Color(0xFFD4D4D4),
                                                fontSize = 10.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(primaryCol, 12f),
                                                modifier = Modifier
                                                    .border(1.dp, if (retro80sMode) primaryCol else Color(0xFF333333), editBtnShape)
                                                    .background(if (retro80sMode) Color(0xFF080808) else Color(0xFF141414), editBtnShape)
                                                    .clickable { onMoveWidget(widgetId, -1) }
                                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                        if (idx < activeWidgets.lastIndex) {
                                            Text(
                                                text = "↓",
                                                color = if (retro80sMode) primaryCol else Color(0xFFD4D4D4),
                                                fontSize = 10.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(primaryCol, 12f),
                                                modifier = Modifier
                                                    .border(1.dp, if (retro80sMode) primaryCol else Color(0xFF333333), editBtnShape)
                                                    .background(if (retro80sMode) Color(0xFF080808) else Color(0xFF141414), editBtnShape)
                                                    .clickable { onMoveWidget(widgetId, 1) }
                                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                        sizeChoices.forEach { (szId, szLbl) ->
                                            val isSz = sizeMode == szId
                                            Text(
                                                text = szLbl,
                                                color = if (isSz) Color.Black else if (retro80sMode) primaryCol else Color.White,
                                                fontSize = 9.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold,
                                                style = if (isSz) TextStyle.Default else glowTextStyle(primaryCol, 12f),
                                                modifier = Modifier
                                                    .border(
                                                        1.dp,
                                                        if (isSz) secondaryCol else if (retro80sMode) primaryCol.copy(alpha = 0.55f) else Color(0xFF2E2E2E),
                                                        editBtnShape
                                                    )
                                                    .background(
                                                        if (isSz) secondaryCol else if (retro80sMode) Color(0xFF080808) else Color(0xFF151515),
                                                        editBtnShape
                                                    )
                                                    .clickable { onSetWidgetSize(widgetId, szId) }
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "×",
                                        color = if (retro80sMode) secondaryCol else Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        fontFamily = selectedFont,
                                        fontWeight = FontWeight.Bold,
                                        style = glowTextStyle(secondaryCol, 12f),
                                        modifier = Modifier
                                            .border(1.dp, if (retro80sMode) secondaryCol else Color(0xFFEF4444).copy(alpha = 0.5f), editBtnShape)
                                            .background(if (retro80sMode) Color(0xFF080808) else Color(0xFF1A0808), editBtnShape)
                                            .clickable { onRemoveWidget(widgetId) }
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }

                                // Per-Widget Visual Style Selector
                                val styleChoices = remember(widgetId) { getWidgetStyleChoices(widgetId) }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            if (retro80sMode) primaryCol.copy(alpha = 0.65f) else Color(0xFF262626),
                                            editBoxShape
                                        )
                                        .background(if (retro80sMode) Color(0xFF060606) else Color(0xFF101010), editBoxShape)
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "STYLE",
                                        color = if (retro80sMode) primaryCol else Color(0xFF8A8A8A),
                                        fontSize = 8.sp,
                                        fontFamily = selectedFont,
                                        fontWeight = FontWeight.Bold,
                                        style = glowTextStyle(primaryCol, 12f)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        styleChoices.forEach { (stId, stLbl) ->
                                            val isSt = rawWStyle == stId
                                            Text(
                                                text = stLbl,
                                                color = if (isSt) Color.Black else if (retro80sMode) primaryCol else Color.White,
                                                fontSize = 8.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                style = if (isSt) TextStyle.Default else glowTextStyle(primaryCol, 12f),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(
                                                        1.dp,
                                                        if (isSt) secondaryCol else if (retro80sMode) primaryCol.copy(alpha = 0.45f) else Color(0xFF282828),
                                                        editBtnShape
                                                    )
                                                    .background(
                                                        if (isSt) secondaryCol else if (retro80sMode) Color(0xFF080808) else Color(0xFF151515),
                                                        editBtnShape
                                                    )
                                                    .clickable { onSetWidgetStyle(widgetId, stId) }
                                                    .padding(vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // Battery Progress Bar Style Selector in Edit Mode
                                if (widgetId == "clock" || widgetId == "battery_bar") {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                1.dp,
                                                if (retro80sMode) primaryCol.copy(alpha = 0.65f) else Color(0xFF262626),
                                                editBoxShape
                                            )
                                            .background(if (retro80sMode) Color(0xFF060606) else Color(0xFF101010), editBoxShape)
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "BATTERY BAR STYLE",
                                            color = if (retro80sMode) primaryCol else Color(0xFF8A8A8A),
                                            fontSize = 8.sp,
                                            fontFamily = selectedFont,
                                            fontWeight = FontWeight.Bold,
                                            style = glowTextStyle(primaryCol, 12f)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            BATTERY_BAR_STYLES.forEach { (stId, stLbl) ->
                                                val isSt = batteryBarStyle == stId
                                                Text(
                                                    text = stLbl,
                                                    color = if (isSt) Color.Black else if (retro80sMode) primaryCol else Color.White,
                                                    fontSize = 7.sp,
                                                    fontFamily = selectedFont,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    style = if (isSt) TextStyle.Default else glowTextStyle(primaryCol, 12f),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .border(
                                                            1.dp,
                                                            if (isSt) secondaryCol else if (retro80sMode) primaryCol.copy(alpha = 0.45f) else Color(0xFF282828),
                                                            editBtnShape
                                                        )
                                                        .background(
                                                            if (isSt) secondaryCol else if (retro80sMode) Color(0xFF080808) else Color(0xFF151515),
                                                            editBtnShape
                                                        )
                                                        .clickable { onChangeBatteryBarStyle(stId) }
                                                        .padding(vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Row 2: Collapsible Per-Widget Font Style Menu
                                if (widgetId != "globe") {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    if (retro80sMode) primaryCol.copy(alpha = 0.65f) else if (isWidgetFontMenuExpanded) primaryCol.copy(alpha = 0.6f) else Color(0xFF282828),
                                                    editBoxShape
                                                )
                                                .background(if (retro80sMode) Color(0xFF060606) else Color(0xFF111111), editBoxShape)
                                                .clickable {
                                                    expandedFontWidgetIds = if (isWidgetFontMenuExpanded) {
                                                        expandedFontWidgetIds - widgetId
                                                    } else {
                                                        expandedFontWidgetIds + widgetId
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 5.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "FONT",
                                                    color = if (retro80sMode) primaryCol else Color(0xFF8A8A8A),
                                                    fontSize = 8.sp,
                                                    fontFamily = selectedFont,
                                                    fontWeight = FontWeight.Bold,
                                                    style = glowTextStyle(primaryCol, 12f)
                                                )
                                                Text(
                                                    text = activeWidgetFont.displayName.substringBefore(" ("),
                                                    color = if (retro80sMode) secondaryCol else Color.White,
                                                    fontSize = 9.sp,
                                                    fontFamily = wFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    style = glowTextStyle(secondaryCol, 12f)
                                                )
                                            }
                                            Text(
                                                text = if (retro80sMode) {
                                                    if (isWidgetFontMenuExpanded) "[-]" else "[+]"
                                                } else {
                                                    if (isWidgetFontMenuExpanded) "▾" else "▸"
                                                },
                                                color = secondaryCol,
                                                fontSize = 10.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(secondaryCol, 12f)
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = isWidgetFontMenuExpanded,
                                            enter = expandVertically() + fadeIn(),
                                            exit = shrinkVertically() + fadeOut()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 2.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                allWidgetFonts.chunked(5).forEach { rowFonts ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        rowFonts.forEach { f ->
                                                            val isF = wFontId == f.id
                                                            val previewFamily = f.resolveFontFamily(lomoFontFamily)
                                                            Text(
                                                                text = f.shortLabel,
                                                                color = if (isF) Color.Black else if (retro80sMode) primaryCol else Color(0xFFD4D4D4),
                                                                fontSize = 8.sp,
                                                                fontFamily = previewFamily,
                                                                fontWeight = if (isF) FontWeight.Bold else FontWeight.Normal,
                                                                textAlign = TextAlign.Center,
                                                                style = if (isF) TextStyle.Default else glowTextStyle(primaryCol, 10f),
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .border(
                                                                        1.dp,
                                                                        if (isF) (if (retro80sMode) secondaryCol else Color.White) else if (retro80sMode) primaryCol.copy(alpha = 0.45f) else Color(0xFF282828),
                                                                        editBtnShape
                                                                    )
                                                                    .background(
                                                                        if (isF) (if (retro80sMode) secondaryCol else Color.White) else if (retro80sMode) Color(0xFF080808) else Color(0xFF121212),
                                                                        editBtnShape
                                                                    )
                                                                    .clickable { onSetWidgetFont(widgetId, f.id) }
                                                                    .padding(vertical = 4.dp)
                                                            )
                                                        }
                                                        repeat(5 - rowFonts.size) {
                                                            Spacer(modifier = Modifier.weight(1f))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Dedicated X-Axis & Y-Axis Rotation Controls for the 3D Globe Widget in Edit Mode
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                1.dp,
                                                if (retro80sMode) primaryCol.copy(alpha = 0.65f) else Color(0xFF262626),
                                                editBoxShape
                                            )
                                            .background(if (retro80sMode) Color(0xFF060606) else Color(0xFF101010), editBoxShape)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "GLOBE AXIS",
                                                color = if (retro80sMode) primaryCol else Color(0xFFA3A3A3),
                                                fontSize = 8.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(primaryCol, 12f)
                                            )
                                            Text(
                                                text = "RESET",
                                                color = secondaryCol,
                                                fontSize = 8.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(secondaryCol, 12f),
                                                modifier = Modifier
                                                    .border(1.dp, secondaryCol.copy(alpha = 0.6f), editBtnShape)
                                                    .background(if (retro80sMode) Color(0xFF080808) else Color(0xFF181818), editBtnShape)
                                                    .clickable {
                                                        onChangeGlobeTilt2d(-3f)
                                                        onChangeGlobeRotX(14f)
                                                        onChangeGlobeRotY(-77f)
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        GlobeAxisControlRow(
                                            axisLabel = "X-AXIS (TILT)",
                                            degreeValue = globeTilt2dDeg,
                                            minDeg = -180f,
                                            maxDeg = 180f,
                                            stepDeg = 5f,
                                            accentColor = primaryCol,
                                            fontFamily = selectedFont,
                                            onChangeDeg = onChangeGlobeTilt2d
                                        )

                                        GlobeAxisControlRow(
                                            axisLabel = "Y-AXIS (SPIN)",
                                            degreeValue = globeRotYDeg,
                                            minDeg = -180f,
                                            maxDeg = 180f,
                                            stepDeg = 10f,
                                            accentColor = primaryCol,
                                            fontFamily = selectedFont,
                                            onChangeDeg = onChangeGlobeRotY
                                        )

                                        GlobeAxisControlRow(
                                            axisLabel = "POLAR (NOD)",
                                            degreeValue = globeRotXDeg,
                                            minDeg = -85f,
                                            maxDeg = 85f,
                                            stepDeg = 5f,
                                            accentColor = primaryCol,
                                            fontFamily = selectedFont,
                                            onChangeDeg = onChangeGlobeRotX
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(if (retro80sMode) primaryCol.copy(alpha = 0.45f) else Color(0xFF1F1F1F))
                                )
                            }
                        }

                        // Widget Rendering Switch
                        when (widgetId) {
                            "clock" -> {
                                val horizontalAlign = when (textAlign) {
                                    TextAlign.Center -> Alignment.CenterHorizontally
                                    TextAlign.End -> Alignment.End
                                    else -> Alignment.Start
                                }
                                val timePart = if (currentAmPmStr.isNotEmpty()) {
                                    "$currentHourStr:$currentMinStr $currentAmPmStr"
                                } else {
                                    "$currentHourStr:$currentMinStr"
                                }
                                val dateClean = currentDateStr.replace("·", "-")
                                val hdrSp = when (sizeMode) {
                                    "max" -> 18.sp
                                    "xlarge" -> 16.sp
                                    "large" -> 14.sp
                                    "compact" -> 11.sp
                                    else -> 13.sp
                                }

                                if (isHolomapMode || wStyle == "cyber_hud") {
                                    // RETRO-FUTURISTIC HOLOMAP CLOCK (Glowing Cyan Header + Glowing Red <<< TIME >>> + Red Diagonal Slash Bar!)
                                    val cyanCol = if (isHolomapMode) Color(0xFF26FFFF) else primaryCol
                                    val redCol = if (isHolomapMode) Color(0xFFFF3B14) else secondaryCol
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = { onOpenClockApp() },
                                                onLongClick = { onLongPressHome() }
                                            ),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "HOLOMAP CHRONO",
                                                color = cyanCol,
                                                fontSize = (hdrSp.value * 0.90f).sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.0.sp,
                                                style = glowTextStyle(cyanCol, 18f)
                                            )
                                            Text(
                                                text = "[${batteryPct}%]",
                                                color = cyanCol,
                                                fontSize = (hdrSp.value * 0.90f).sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(cyanCol, 18f)
                                            )
                                        }
                                        Text(
                                            text = "<<< $timePart · $dateClean >>>",
                                            color = redCol,
                                            fontSize = hdrSp,
                                            fontFamily = wFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            style = glowTextStyle(redCol, 18f)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "POWER",
                                                color = cyanCol,
                                                fontSize = 10.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(cyanCol, 16f)
                                            )
                                            Text(
                                                text = "[${batteryPct}%]",
                                                color = cyanCol,
                                                fontSize = 10.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(cyanCol, 16f)
                                            )
                                            RetroBatteryProgressBar(
                                                batteryPct = batteryPct,
                                                barStyle = "holomap_slash",
                                                accentColor = redCol,
                                                retro80sMode = true,
                                                sizeMode = sizeMode,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                } else if (wStyle == "80s_retro") {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = { onOpenClockApp() },
                                                onLongClick = { onLongPressHome() }
                                            ),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$timePart $dateClean",
                                                color = primaryCol,
                                                fontSize = hdrSp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.0.sp,
                                                style = glowTextStyle(primaryCol, 18f)
                                            )
                                            Text(
                                                text = if (isCharging) "$batteryPct% ⚡" else "$batteryPct%",
                                                color = primaryCol,
                                                fontSize = hdrSp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(primaryCol, 18f)
                                            )
                                        }
                                        RetroBatteryProgressBar(
                                            batteryPct = batteryPct,
                                            barStyle = batteryBarStyle,
                                            accentColor = primaryCol,
                                            retro80sMode = retro80sMode,
                                            sizeMode = sizeMode,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    // Standard Modern Dot-Matrix Clock
                                    val clockSp = when (sizeMode) {
                                        "compact" -> 34.sp
                                        "large" -> 54.sp
                                        "xlarge" -> 64.sp
                                        "max" -> 76.sp
                                        else -> 44.sp
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = { onOpenClockApp() },
                                                onLongClick = { onLongPressHome() }
                                            ),
                                        horizontalAlignment = horizontalAlign,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "$currentHourStr:$currentMinStr",
                                                color = Color.White,
                                                fontSize = clockSp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (currentAmPmStr.isNotEmpty()) {
                                                Text(
                                                    text = currentAmPmStr,
                                                    color = primaryCol,
                                                    fontSize = 12.sp,
                                                    fontFamily = wFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(bottom = 6.dp)
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = currentDateStr,
                                                color = Color(0xFFA3A3A3),
                                                fontSize = 11.sp,
                                                fontFamily = wFontFamily
                                            )
                                            Text(
                                                text = if (isCharging) "$batteryPct% ⚡" else "$batteryPct%",
                                                color = primaryCol,
                                                fontSize = 11.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        RetroBatteryProgressBar(
                                            batteryPct = batteryPct,
                                            barStyle = batteryBarStyle,
                                            accentColor = primaryCol,
                                            retro80sMode = retro80sMode,
                                            sizeMode = "compact",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            "battery_bar" -> {
                                val headerSp = when (sizeMode) {
                                    "compact" -> 10.sp
                                    "large" -> 14.sp
                                    else -> 12.sp
                                }
                                val timePart = if (currentAmPmStr.isNotEmpty()) {
                                    "$currentHourStr:$currentMinStr $currentAmPmStr"
                                } else {
                                    "$currentHourStr:$currentMinStr"
                                }
                                val dateClean = currentDateStr.replace("·", "-")
                                val effectiveBarStyle = when {
                                    isHolomapMode || wStyle == "cyber_hud" -> "holomap_slash"
                                    wStyle == "80s_retro" -> "retro_blocks"
                                    else -> batteryBarStyle
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { onOpenClockApp() },
                                            onLongClick = { onLongPressHome() }
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isHolomapMode || wStyle == "cyber_hud") "ENERGY CELL // $timePart" else "$timePart $dateClean",
                                            color = if (retro80sMode || wStyle == "80s_retro" || wStyle == "cyber_hud") primaryCol else Color.White,
                                            fontSize = headerSp,
                                            fontFamily = wFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            style = glowTextStyle(primaryCol, 16f)
                                        )
                                        Text(
                                            text = if (isHolomapMode || wStyle == "cyber_hud") "[${batteryPct}%]" else if (isCharging) "$batteryPct% ⚡" else "$batteryPct%",
                                            color = primaryCol,
                                            fontSize = headerSp,
                                            fontFamily = wFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            style = glowTextStyle(primaryCol, 16f)
                                        )
                                    }
                                    RetroBatteryProgressBar(
                                        batteryPct = batteryPct,
                                        barStyle = effectiveBarStyle,
                                        accentColor = secondaryCol,
                                        retro80sMode = retro80sMode,
                                        sizeMode = sizeMode,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            "retro_flip_clock" -> {
                                RetroSplitFlapClockWidget(
                                    hourStr = currentHourStr,
                                    minStr = currentMinStr,
                                    secStr = currentSecStr,
                                    amPmStr = currentAmPmStr,
                                    dateStr = currentDateStr,
                                    sizeMode = sizeMode,
                                    widgetStyle = wStyle,
                                    accentColor = primaryCol,
                                    secondaryColor = secondaryCol,
                                    lomoFontFamily = wFontFamily,
                                    onOpenClockApp = onOpenClockApp,
                                    onLongPressHome = onLongPressHome
                                )
                            }

                            "analog_chrono" -> {
                                RetroAnalogChronoWidget(
                                    currentTimeMillis = currentTimeMillis,
                                    hourStr = currentHourStr,
                                    minStr = currentMinStr,
                                    secStr = currentSecStr,
                                    amPmStr = currentAmPmStr,
                                    dateStr = currentDateStr,
                                    sizeMode = sizeMode,
                                    widgetStyle = wStyle,
                                    accentColor = primaryCol,
                                    secondaryColor = secondaryCol,
                                    lomoFontFamily = wFontFamily,
                                    widgetsActive = widgetsActive,
                                    onOpenClockApp = onOpenClockApp,
                                    onLongPressHome = onLongPressHome
                                )
                            }

                            "precision_chrono" -> {
                                PrecisionChronographWidget(
                                    sizeMode = sizeMode,
                                    widgetStyle = wStyle,
                                    accentColor = primaryCol,
                                    secondaryColor = secondaryCol,
                                    lomoFontFamily = wFontFamily,
                                    widgetsActive = widgetsActive,
                                    onLongPressHome = onLongPressHome,
                                    onTick = onWaveTick
                                )
                            }

                            "retro_calendar" -> {
                                RetroDotMatrixCalendarWidget(
                                    currentTimeMillis = currentTimeMillis,
                                    sizeMode = sizeMode,
                                    widgetStyle = wStyle,
                                    accentColor = primaryCol,
                                    secondaryColor = secondaryCol,
                                    lomoFontFamily = wFontFamily
                                )
                            }

                            "week_strip" -> {
                                val timeHeader = if (currentAmPmStr.isNotEmpty()) {
                                    "$currentHourStr:$currentMinStr $currentAmPmStr"
                                } else {
                                    "$currentHourStr:$currentMinStr"
                                }
                                RetroWeekTimelineStripWidget(
                                    currentTimeMillis = currentTimeMillis,
                                    sizeMode = sizeMode,
                                    widgetStyle = wStyle,
                                    timeHeaderStr = timeHeader,
                                    batteryPct = batteryPct,
                                    accentColor = primaryCol,
                                    secondaryColor = secondaryCol,
                                    lomoFontFamily = wFontFamily
                                )
                            }

                            "year_matrix" -> {
                                RetroYearDotMatrixWidget(
                                    currentTimeMillis = currentTimeMillis,
                                    sizeMode = sizeMode,
                                    widgetStyle = wStyle,
                                    accentColor = primaryCol,
                                    secondaryColor = secondaryCol,
                                    lomoFontFamily = wFontFamily
                                )
                            }

                            "torch" -> {
                                val padV = when (sizeMode) {
                                    "compact" -> 5.dp
                                    "large" -> 11.dp
                                    else -> 7.dp
                                }
                                when (wStyle) {
                                    "80s_retro", "cyber_hud" -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { onToggleTorch() },
                                                    onLongClick = { onLongPressHome() }
                                                )
                                                .retroNeonBorder(1.2.dp, primaryCol, RoundedCornerShape(0.dp), glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                                                .background(Color(0xFF040606), RoundedCornerShape(0.dp))
                                                .padding(horizontal = 10.dp, vertical = padV),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ILLUMINATOR",
                                                color = primaryCol,
                                                fontSize = if (sizeMode == "large") 12.sp else 10.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(primaryCol, 16f)
                                            )
                                            Text(
                                                text = if (torchEnabled) "<<< ACTIVE >>>" else "<<< STANDBY >>>",
                                                color = if (torchEnabled) secondaryCol else primaryCol.copy(alpha = 0.65f),
                                                fontSize = if (sizeMode == "large") 11.sp else 10.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(secondaryCol, 16f)
                                            )
                                        }
                                    }
                                    else -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { onToggleTorch() },
                                                    onLongClick = { onLongPressHome() }
                                                )
                                                .border(1.dp, if (torchEnabled) primaryCol else Color(0xFF242424), RoundedCornerShape(8.dp))
                                                .background(if (torchEnabled) primaryCol.copy(alpha = 0.12f) else Color(0xFF060606), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = padV),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = if (torchEnabled) "⚡" else "☼",
                                                    color = if (torchEnabled) primaryCol else Color(0xFFA3A3A3),
                                                    fontSize = if (sizeMode == "large") 14.sp else 12.sp
                                                )
                                                Text(
                                                    text = "TORCH",
                                                    color = Color.White,
                                                    fontSize = if (sizeMode == "large") 12.sp else 11.sp,
                                                    fontFamily = wFontFamily,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Text(
                                                text = if (torchEnabled) "ON" else "OFF",
                                                color = if (torchEnabled) primaryCol else Color(0xFF737373),
                                                fontSize = 10.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            "storage" -> {
                                val ramPct = (ramUsageFraction * 100f).roundToInt().coerceIn(5, 100)
                                val romPct = (storageUsageFraction * 100f).roundToInt().coerceIn(5, 100)
                                when (wStyle) {
                                    "cyber_hud" -> {
                                        // EXACT MATCH TO 2ND REFERENCE IMAGE (TERRAIN PROPERTIES -> SYSTEM PROPERTIES):
                                        // Glowing Cyan Header + Rows with Glowing Cyan Label, Cyan [81%] & Glowing Red Diagonal-Hatched Bar!
                                        val cyanCol = if (isHolomapMode) Color(0xFF26FFFF) else primaryCol
                                        val redCol = if (isHolomapMode) Color(0xFFFF3B14) else secondaryCol
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "SYSTEM PROPERTIES",
                                                color = cyanCol,
                                                fontSize = if (sizeMode == "large") 13.sp else 11.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.0.sp,
                                                style = glowTextStyle(cyanCol, 18f)
                                            )
                                            listOf(
                                                Triple("MEMORY", ramPct, ramStatusText),
                                                Triple("STORAGE", romPct, storageStatusText),
                                                Triple("BATTERY", batteryPct, "${batteryPct}%")
                                            ).forEach { (lbl, pctVal, _) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = lbl,
                                                        color = cyanCol,
                                                        fontSize = if (sizeMode == "large") 11.sp else 10.sp,
                                                        fontFamily = wFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        style = glowTextStyle(cyanCol, 16f),
                                                        modifier = Modifier.width(76.dp)
                                                    )
                                                    Text(
                                                        text = "[${pctVal}%]",
                                                        color = cyanCol,
                                                        fontSize = if (sizeMode == "large") 10.sp else 9.sp,
                                                        fontFamily = wFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        style = glowTextStyle(cyanCol, 16f),
                                                        modifier = Modifier.width(42.dp)
                                                    )
                                                    RetroBatteryProgressBar(
                                                        batteryPct = pctVal,
                                                        barStyle = "holomap_slash",
                                                        accentColor = redCol,
                                                        retro80sMode = true,
                                                        sizeMode = "compact",
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    "80s_retro" -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .retroNeonBorder(1.2.dp, primaryCol, RoundedCornerShape(0.dp), glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                                                .background(Color(0xFF040804), RoundedCornerShape(0.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = ramStatusText,
                                                    color = primaryCol,
                                                    fontSize = if (sizeMode == "large") 11.sp else 9.sp,
                                                    fontFamily = wFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    style = glowTextStyle(primaryCol, 16f)
                                                )
                                                Text(
                                                    text = storageStatusText,
                                                    color = primaryCol,
                                                    fontSize = if (sizeMode == "large") 11.sp else 9.sp,
                                                    fontFamily = wFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    style = glowTextStyle(primaryCol, 16f)
                                                )
                                            }
                                            if (sizeMode != "compact") {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    RetroBatteryProgressBar(
                                                        batteryPct = ramPct,
                                                        barStyle = "retro_blocks",
                                                        accentColor = secondaryCol,
                                                        retro80sMode = retro80sMode,
                                                        sizeMode = "compact",
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    RetroBatteryProgressBar(
                                                        batteryPct = romPct,
                                                        barStyle = "retro_blocks",
                                                        accentColor = secondaryCol,
                                                        retro80sMode = retro80sMode,
                                                        sizeMode = "compact",
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
                                                .background(Color(0xFF060606), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = ramStatusText,
                                                    color = Color.White,
                                                    fontSize = if (sizeMode == "large") 11.sp else 10.sp,
                                                    fontFamily = wFontFamily
                                                )
                                                Text(
                                                    text = storageStatusText,
                                                    color = Color(0xFFA3A3A3),
                                                    fontSize = if (sizeMode == "large") 11.sp else 10.sp,
                                                    fontFamily = wFontFamily
                                                )
                                            }
                                            if (sizeMode != "compact") {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(5.dp)
                                                            .background(Color(0xFF141414), RoundedCornerShape(999.dp))
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxHeight()
                                                                .fillMaxWidth(ramUsageFraction.coerceIn(0.05f, 1f))
                                                                .background(primaryCol, RoundedCornerShape(999.dp))
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(5.dp)
                                                            .background(Color(0xFF141414), RoundedCornerShape(999.dp))
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxHeight()
                                                                .fillMaxWidth(storageUsageFraction.coerceIn(0.05f, 1f))
                                                                .background(Color(0xFF555555), RoundedCornerShape(999.dp))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "quote" -> {
                                when (wStyle) {
                                    "80s_retro", "cyber_hud" -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .retroNeonBorder(1.2.dp, primaryCol, RoundedCornerShape(0.dp), glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                                                .background(Color(0xFF040606), RoundedCornerShape(0.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = "<<< Simplify, then add lightness. >>>",
                                                color = secondaryCol,
                                                fontSize = if (sizeMode == "large") 13.sp else 11.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(secondaryCol, 16f)
                                            )
                                        }
                                    }
                                    else -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                                .background(Color(0xFF060606), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = "“Simplify, then add lightness.”",
                                                color = Color.White,
                                                fontSize = if (sizeMode == "large") 13.sp else 11.sp,
                                                fontFamily = wFontFamily,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }

                            "zen" -> {
                                val mm = (zenSeconds / 60).toString().padStart(2, '0')
                                val ss = (zenSeconds % 60).toString().padStart(2, '0')
                                val zenPct = ((zenSeconds / 1500f) * 100f).roundToInt().coerceIn(1, 100)
                                when (wStyle) {
                                    "80s_retro", "cyber_hud" -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .retroNeonBorder(1.2.dp, primaryCol, RoundedCornerShape(0.dp), glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                                                .background(Color(0xFF040606), RoundedCornerShape(0.dp))
                                                .padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "FOCUS // $mm:$ss",
                                                    color = primaryCol,
                                                    fontSize = if (sizeMode == "large") 14.sp else 11.sp,
                                                    fontFamily = wFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    style = glowTextStyle(primaryCol, 16f)
                                                )
                                                Text(
                                                    text = if (zenActive) "[STOP]" else "[START]",
                                                    color = secondaryCol,
                                                    fontSize = 10.sp,
                                                    fontFamily = wFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    style = glowTextStyle(secondaryCol, 16f),
                                                    modifier = Modifier.combinedClickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = { onToggleZen() },
                                                        onLongClick = { onLongPressHome() }
                                                    )
                                                )
                                            }
                                            RetroBatteryProgressBar(
                                                batteryPct = zenPct,
                                                barStyle = if (isHolomapMode || wStyle == "cyber_hud") "holomap_slash" else "retro_blocks",
                                                accentColor = secondaryCol,
                                                retro80sMode = retro80sMode,
                                                sizeMode = "compact",
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                    else -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Color(0xFF262626), RoundedCornerShape(8.dp))
                                                .background(Color(0xFF060606), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 7.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$mm:$ss",
                                                color = Color.White,
                                                fontSize = if (sizeMode == "large") 15.sp else 12.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (zenActive) "STOP" else "START",
                                                color = primaryCol,
                                                fontSize = 10.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.combinedClickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { onToggleZen() },
                                                    onLongClick = { onLongPressHome() }
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            "scratchpad" -> {
                                when (wStyle) {
                                    "80s_retro", "cyber_hud" -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .retroNeonBorder(1.2.dp, primaryCol, RoundedCornerShape(0.dp), glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                                                .background(Color(0xFF040606), RoundedCornerShape(0.dp))
                                                .padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = if (isHolomapMode || wStyle == "cyber_hud") "<<< $quickNote >>>" else "$ $quickNote █",
                                                color = secondaryCol,
                                                fontSize = if (sizeMode == "large") 13.sp else 11.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                style = glowTextStyle(secondaryCol, 16f)
                                            )
                                        }
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
                                                .background(Color(0xFF060606), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = "> $quickNote",
                                                color = Color(0xFFD4D4D4),
                                                fontSize = if (sizeMode == "large") 13.sp else 11.sp,
                                                fontFamily = wFontFamily
                                            )
                                        }
                                    }
                                }
                            }

                            "globe" -> {
                                val globeHeight = when (sizeMode) {
                                    "compact" -> 135.dp
                                    "large" -> 265.dp
                                    "xlarge" -> 330.dp
                                    "max" -> 395.dp
                                    else -> 195.dp
                                }
                                val effectiveGlobeThemeForWidget = when {
                                    retro80sMode || wStyle == "80s_retro" || wStyle == "cyber_hud" -> "wireframe"
                                    else -> globeTheme
                                }
                                val effectiveGlobeColorForWidget = when {
                                    isHolomapMode -> "#26FFFF"
                                    retro80sMode && retro80sColorTheme == "amber" -> "#FFB830"
                                    retro80sMode || wStyle == "80s_retro" -> "#9EFF78"
                                    else -> globeWireframeColor
                                }
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isHolomapMode || wStyle == "cyber_hud") {
                                        // Exact match to top of 2nd reference image:
                                        // HOLOMAP TERRAIN + X <<< ... >>>, Y <<< [..] [..] >>>, Z <<< ... >>>
                                        val cyanCol = Color(0xFF26FFFF)
                                        val redCol = Color(0xFFFF3B14)
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            Text(
                                                text = "HOLOMAP TERRAIN",
                                                color = cyanCol,
                                                fontSize = 11.sp,
                                                fontFamily = wFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.0.sp,
                                                style = glowTextStyle(cyanCol, 18f)
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text("X", color = cyanCol, fontSize = 9.sp, fontFamily = wFontFamily, fontWeight = FontWeight.Bold, style = glowTextStyle(cyanCol, 14f))
                                                Text(
                                                    text = String.format(Locale.US, "<<< %.5f >>>", 50.66667f + globeRotXDeg * 0.1f),
                                                    color = redCol,
                                                    fontSize = 9.sp,
                                                    fontFamily = wFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    style = glowTextStyle(redCol, 14f)
                                                )
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text("Y", color = cyanCol, fontSize = 9.sp, fontFamily = wFontFamily, fontWeight = FontWeight.Bold, style = glowTextStyle(cyanCol, 14f))
                                                Text(
                                                    text = String.format(Locale.US, "<<< [%.1f] [%.1f] >>>", 38.5f + globeRotYDeg * 0.1f, 15.4f + globeTilt2dDeg),
                                                    color = redCol,
                                                    fontSize = 9.sp,
                                                    fontFamily = wFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    style = glowTextStyle(redCol, 14f)
                                                )
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text("Z", color = cyanCol, fontSize = 9.sp, fontFamily = wFontFamily, fontWeight = FontWeight.Bold, style = glowTextStyle(cyanCol, 14f))
                                                Text(
                                                    text = "<<< 891.80000 >>>",
                                                    color = redCol,
                                                    fontSize = 9.sp,
                                                    fontFamily = wFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    style = glowTextStyle(redCol, 14f)
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(globeHeight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        RetroFuturisticGlobeView(
                                            globeTheme = effectiveGlobeThemeForWidget,
                                            globeWireframeColor = effectiveGlobeColorForWidget,
                                            hudOverrideColor = if (isHolomapMode || wStyle == "cyber_hud") Color(0xFFFF3B14) else null,
                                            widgetStyle = wStyle,
                                            axisTilt2dDeg = globeTilt2dDeg,
                                            axisRotXDeg = globeRotXDeg,
                                            axisRotYDeg = globeRotYDeg,
                                            interactive = !editMode,
                                            widgetsActive = widgetsActive,
                                            onLongPressHome = onLongPressHome,
                                            onInteractionModeChanged = onGlobeInteractionChanged,
                                            onTick = onWaveTick
                                        )
                                    }
                                }
                            }

                            "pinned_apps" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 2.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    val waveRadiusRows = 2.25f
                                    val maxWaveShiftPx = with(density) { (if (waveScrubMode == "full") 32.dp else 16.dp).toPx() }
                                    val verticalSpreadPx = with(density) { 4.dp.toPx() }
                                    val pressHoverShiftPx = with(density) { 14.dp.toPx() }
                                    val maxDotWaveShiftPx = with(density) { 14.dp.toPx() }

                                    val activeFocusedIndex = remember(waveFloatIndex, pinnedApps.size, editMode) {
                                        val w = waveFloatIndex
                                        if (editMode || w == null || pinnedApps.isEmpty() || waveScrubMode == "off") {
                                            null
                                        } else {
                                            w.toInt().coerceIn(0, pinnedApps.lastIndex)
                                        }
                                    }

                                    LaunchedEffect(activeFocusedIndex) {
                                        if (activeFocusedIndex != null && activeFocusedIndex != lastHoveredIndex) {
                                            lastHoveredIndex = activeFocusedIndex
                                            onWaveTick()
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.Bottom
                                        ) {
                                            if (editMode) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "PINNED APPS",
                                                        color = if (retro80sMode) primaryCol else Color(0xFFA3A3A3),
                                                        fontSize = 8.sp,
                                                        fontFamily = selectedFont,
                                                        fontWeight = FontWeight.Bold,
                                                        style = glowTextStyle(primaryCol, 12f)
                                                    )
                                                    Text(
                                                        text = if (customPinnedOrderEnabled) "CUSTOM (↑/↓)" else "A–Z AUTO",
                                                        color = if (customPinnedOrderEnabled) secondaryCol else Color(0xFF737373),
                                                        fontSize = 8.sp,
                                                        fontFamily = selectedFont,
                                                        fontWeight = FontWeight.Bold,
                                                        style = glowTextStyle(secondaryCol, 12f)
                                                    )
                                                }
                                            }

                                            pinnedApps.forEachIndexed { index, app ->
                                                val interactionSource = remember { MutableInteractionSource() }
                                                val isPressed by interactionSource.collectIsPressedAsState()

                                                val targetWaveFactor = if (!editMode && waveFloatIndex != null && waveScrubMode != "off") {
                                                    val distRows = abs(waveFloatIndex!! - (index + 0.5f))
                                                    if (distRows < waveRadiusRows) {
                                                        val c = cos((distRows / waveRadiusRows) * (PI / 2)).toFloat().coerceIn(0f, 1f)
                                                        c * c * (3f - 2f * c)
                                                    } else 0f
                                                } else 0f

                                                val animWave by animateFloatAsState(
                                                    targetValue = targetWaveFactor,
                                                    animationSpec = spring(
                                                        dampingRatio = 0.78f,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    ),
                                                    label = "homeAppWave"
                                                )

                                                val isWaveFocused = activeFocusedIndex == index
                                                val isHighlighted = isWaveFocused || isPressed

                                                val pressAnim by animateFloatAsState(
                                                    targetValue = if (isPressed) 1f else 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                    ),
                                                    label = "homeAppPress"
                                                )

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .graphicsLayer {
                                                            val shiftX = (animWave * maxWaveShiftPx) + (pressAnim * pressHoverShiftPx)
                                                            val shiftY = if (waveFloatIndex != null && animWave > 0.01f) {
                                                                ((index + 0.5f) - waveFloatIndex!!) * verticalSpreadPx * animWave
                                                            } else 0f
                                                            val sc = 1f + (animWave * 0.14f) + (pressAnim * 0.04f)
                                                            translationX = shiftX
                                                            translationY = shiftY
                                                            scaleX = sc
                                                            scaleY = sc
                                                            transformOrigin = TransformOrigin(0f, 0.5f)
                                                        }
                                                        .combinedClickable(
                                                            interactionSource = interactionSource,
                                                            indication = null,
                                                            onClick = {
                                                                if (!editMode) onLaunchApp(app.packageName)
                                                            },
                                                            onLongClick = { onLongPressApp(app) }
                                                        )
                                                        .padding(vertical = 6.dp),
                                                    horizontalArrangement = if (editMode) {
                                                        Arrangement.SpaceBetween
                                                    } else {
                                                        when (textAlign) {
                                                            TextAlign.Center -> Arrangement.Center
                                                            TextAlign.End -> Arrangement.End
                                                            else -> Arrangement.Start
                                                        }
                                                    },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val displayLabel = if (wPrebuiltFont.uppercaseHome || uppercaseHome) {
                                                        app.label.uppercase(Locale.getDefault())
                                                    } else {
                                                        app.label
                                                    }
                                                    val is80sApps = (retro80sMode || wStyle == "80s_retro" || wStyle == "cyber_hud") && !editMode
                                                    val styledAppLabel = when {
                                                        isHighlighted && !editMode && isHolomapMode -> "<<< $displayLabel >>>"
                                                        isHighlighted && !editMode -> "▸ $displayLabel"
                                                        isHolomapMode && !editMode -> "> $displayLabel"
                                                        is80sApps -> "> $displayLabel"
                                                        else -> displayLabel
                                                    }
                                                    val appCol = when {
                                                        isHighlighted && !editMode -> secondaryCol
                                                        is80sApps -> primaryCol
                                                        else -> Color(0xFFF5F5F5)
                                                    }
                                                    Text(
                                                        text = styledAppLabel,
                                                        color = appCol,
                                                        fontSize = textSizeSp.sp,
                                                        fontFamily = wFontFamily,
                                                        style = glowTextStyle(appCol, 18f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = if (editMode) Modifier.weight(1f).padding(end = 8.dp) else Modifier
                                                    )

                                                    if (editMode) {
                                                        val pinBtnShape = RoundedCornerShape(if (retro80sMode) 0.dp else 4.dp)
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            if (customPinnedOrderEnabled) {
                                                                if (index > 0) {
                                                                    Text(
                                                                        text = "↑",
                                                                        color = if (retro80sMode) primaryCol else Color.White,
                                                                        fontSize = 10.sp,
                                                                        fontFamily = selectedFont,
                                                                        fontWeight = FontWeight.Bold,
                                                                        style = glowTextStyle(primaryCol, 12f),
                                                                        modifier = Modifier
                                                                            .border(1.dp, if (retro80sMode) primaryCol else Color(0xFF333333), pinBtnShape)
                                                                            .background(if (retro80sMode) Color(0xFF080808) else Color(0xFF161616), pinBtnShape)
                                                                            .clickable { onMovePinnedApp(app.packageName, -1) }
                                                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                                                    )
                                                                }
                                                                if (index < pinnedApps.lastIndex) {
                                                                    Text(
                                                                        text = "↓",
                                                                        color = if (retro80sMode) primaryCol else Color.White,
                                                                        fontSize = 10.sp,
                                                                        fontFamily = selectedFont,
                                                                        fontWeight = FontWeight.Bold,
                                                                        style = glowTextStyle(primaryCol, 12f),
                                                                        modifier = Modifier
                                                                            .border(1.dp, if (retro80sMode) primaryCol else Color(0xFF333333), pinBtnShape)
                                                                            .background(if (retro80sMode) Color(0xFF080808) else Color(0xFF161616), pinBtnShape)
                                                                            .clickable { onMovePinnedApp(app.packageName, 1) }
                                                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                                                    )
                                                                }
                                                            }
                                                            Text(
                                                                text = "×",
                                                                color = if (retro80sMode) secondaryCol else Color(0xFFEF4444),
                                                                fontSize = 10.sp,
                                                                fontFamily = selectedFont,
                                                                fontWeight = FontWeight.Bold,
                                                                style = glowTextStyle(secondaryCol, 12f),
                                                                modifier = Modifier
                                                                    .border(1.dp, if (retro80sMode) secondaryCol else Color(0xFFEF4444).copy(alpha = 0.45f), pinBtnShape)
                                                                    .background(if (retro80sMode) Color(0xFF080808) else Color(0xFF180808), pinBtnShape)
                                                                    .clickable { onUnpinApp(app.packageName) }
                                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Right-Side Dots Scroller
                                        if (!editMode && waveScrubMode != "off" && pinnedApps.isNotEmpty()) {
                                            Column(
                                                modifier = Modifier
                                                    .width(34.dp)
                                                    .fillMaxHeight()
                                                    .pointerInput(pinnedApps) {
                                                        awaitEachGesture {
                                                            val down = awaitFirstDown(requireUnconsumed = false)
                                                            down.consume()
                                                            val h0 = size.height.toFloat().coerceAtLeast(1f)
                                                            var latestWaveIdx: Float? = (down.position.y.coerceIn(0f, h0) / h0) * pinnedApps.size
                                                            waveFloatIndex = latestWaveIdx
                                                            var isFingerOutsideDots = false
                                                            val cancelMarginX = 36.dp.toPx()
                                                            val cancelMarginY = 28.dp.toPx()

                                                            do {
                                                                val event = awaitPointerEvent()
                                                                val change = event.changes.firstOrNull()
                                                                if (change != null) {
                                                                    val w = size.width.toFloat().coerceAtLeast(1f)
                                                                    val h = size.height.toFloat().coerceAtLeast(1f)
                                                                    val pos = change.position
                                                                    val outsideNow = pos.x < -cancelMarginX ||
                                                                        pos.x > w + cancelMarginX ||
                                                                        pos.y < -cancelMarginY ||
                                                                        pos.y > h + cancelMarginY

                                                                    if (change.pressed) {
                                                                        change.consume()
                                                                        isFingerOutsideDots = outsideNow
                                                                        if (outsideNow) {
                                                                            latestWaveIdx = null
                                                                            waveFloatIndex = null
                                                                            lastHoveredIndex = null
                                                                        } else {
                                                                            val idx = (pos.y.coerceIn(0f, h) / h) * pinnedApps.size
                                                                            latestWaveIdx = idx
                                                                            waveFloatIndex = idx
                                                                        }
                                                                    } else {
                                                                        isFingerOutsideDots = outsideNow || change.isConsumed
                                                                    }
                                                                }
                                                            } while (event.changes.any { it.pressed })

                                                            val targetIdx = latestWaveIdx?.toInt()?.coerceIn(0, pinnedApps.lastIndex)
                                                            waveFloatIndex = null
                                                            lastHoveredIndex = null

                                                            if (!isFingerOutsideDots && targetIdx != null && pinnedApps.isNotEmpty()) {
                                                                onWaveTick()
                                                                onLaunchApp(pinnedApps[targetIdx].packageName)
                                                            }
                                                        }
                                                    },
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.SpaceAround
                                            ) {
                                                pinnedApps.forEachIndexed { dotIdx, _ ->
                                                    val isDotFocused = activeFocusedIndex == dotIdx
                                                    val targetDotWave = if (waveFloatIndex != null) {
                                                        val dist = abs(waveFloatIndex!! - (dotIdx + 0.5f))
                                                        if (dist < waveRadiusRows) {
                                                            cos((dist / waveRadiusRows) * (PI / 2)).toFloat().coerceIn(0f, 1f)
                                                        } else 0f
                                                    } else 0f

                                                    val animDotWave by animateFloatAsState(
                                                        targetValue = targetDotWave,
                                                        animationSpec = spring(
                                                            dampingRatio = 0.75f,
                                                            stiffness = Spring.StiffnessMedium
                                                        ),
                                                        label = "homeDotWave"
                                                    )

                                                    Box(
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .graphicsLayer {
                                                                translationX = -animDotWave * maxDotWaveShiftPx
                                                                val sc = 1f + animDotWave * 0.9f
                                                                scaleX = sc
                                                                scaleY = sc
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(if (isDotFocused) 6.dp else 4.dp)
                                                                .background(
                                                                    color = when {
                                                                        isDotFocused -> secondaryCol
                                                                        animDotWave > 0.35f -> primaryCol
                                                                        retro80sMode -> primaryCol.copy(alpha = 0.55f)
                                                                        else -> Color(0xFF525252)
                                                                    },
                                                                    shape = if (retro80sMode) RoundedCornerShape(0.dp) else CircleShape
                                                                )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


private val WORLD_MAP_RAW_POLYGONS: Array<String> = arrayOf(
    "-16.07,180.00;-16.56,180.00;-16.80,179.36;-17.01,178.73;-16.64,178.60;-16.43,179.10;-16.38,179.41;-16.07,180.00",
    "-17.50,178.13;-17.34,178.37;-17.63,178.72;-18.15,178.55;-18.29,177.93;-18.16,177.38;-17.72,177.29;-17.38,177.67;-17.50,178.13",
    "49.00,-122.84;54.29,-130.51;60.31,-141.00;69.71,-140.99;70.19,-129.79;68.40,-113.90;68.02,-104.34;71.92,-95.21;69.88,-85.52;66.41,-83.34;63.61,-90.70;58.95,-94.68;56.85,-89.04;52.16,-81.40;58.05,-77.30;62.55,-77.41;61.06,-69.59;58.77,-66.20;54.95,-57.98;51.42,-57.13;50.23,-66.40;46.82,-71.10;45.27,-61.04;43.55,-65.36;45.14,-67.14;47.45,-69.24;45.01,-71.51;45.00,-74.87;43.63,-78.72;42.37,-80.25;41.68,-82.69;45.35,-82.55;46.44,-84.60;48.30,-88.38;49.00,-95.16;49.00,-122.84",
    "49.00,-122.84;49.00,-95.16;48.30,-88.38;46.44,-84.60;45.35,-82.55;41.68,-82.69;43.63,-78.72;45.00,-74.87;45.01,-71.51;47.45,-69.24;44.81,-66.96;43.09,-70.65;41.81,-70.49;40.63,-73.98;38.94,-74.91;36.97,-76.26;35.55,-75.73;32.51,-80.30;30.04,-81.31;25.21,-80.38;25.87,-81.71;28.55,-82.65;30.09,-84.10;30.27,-87.53;29.15,-90.88;29.71,-93.85;27.83,-97.14;25.87,-97.14;27.54,-99.52;29.76,-102.48;28.97,-103.11;31.75,-106.51;31.33,-111.02;32.53,-114.81;32.54,-117.13;34.45,-120.37;37.78,-122.51;40.31,-124.40;44.62,-124.02;48.38,-124.57;49.00,-122.84",
    "32.54,-117.13;31.33,-111.02;31.75,-106.51;28.97,-103.11;25.87,-97.14;22.20,-97.80;19.20,-96.10;18.20,-94.40;18.60,-91.80;21.50,-87.10;18.20,-87.80;15.80,-88.20;15.90,-85.90;15.00,-83.10;11.00,-83.70;8.50,-77.40;7.20,-77.90;8.10,-80.50;8.50,-82.90;10.90,-85.70;13.00,-87.60;13.80,-90.10;14.50,-92.20;16.20,-95.20;16.90,-99.90;18.00,-102.20;20.40,-105.70;23.20,-106.40;27.90,-110.90;31.30,-113.50;24.00,-109.90;22.90,-109.90;27.80,-114.90;32.54,-117.13",
    "11.20,-74.20;12.20,-71.20;10.60,-66.90;10.40,-62.40;8.50,-60.00;6.90,-58.10;5.90,-55.10;4.90,-52.30;1.80,-50.10;-0.20,-48.50;-2.50,-44.30;-3.70,-38.50;-5.80,-35.20;-8.10,-34.90;-13.00,-38.50;-18.00,-39.50;-23.00,-43.20;-25.50,-48.50;-30.00,-50.20;-33.70,-53.40;-35.00,-54.90;-34.60,-58.40;-38.00,-57.50;-41.00,-62.80;-42.80,-64.40;-46.00,-67.50;-50.00,-68.00;-52.40,-68.40;-54.80,-65.20;-55.10,-68.60;-53.20,-73.70;-48.70,-75.60;-43.20,-74.30;-37.20,-73.60;-33.00,-71.60;-27.00,-70.80;-21.40,-70.10;-18.30,-70.40;-14.00,-76.30;-8.10,-79.10;-5.10,-81.10;-2.20,-80.90;0.90,-79.70;4.00,-77.30;7.20,-77.90;8.50,-77.40;9.50,-75.60;11.20,-74.20",
    "71.60,-22.00;76.80,-18.80;81.60,-12.20;83.60,-30.80;82.00,-52.00;78.20,-72.60;76.00,-66.00;73.00,-56.00;68.50,-53.50;64.20,-51.70;60.00,-43.40;63.50,-40.80;65.60,-37.60;68.20,-28.00;71.60,-22.00",
    "63.80,-22.40;64.40,-14.50;66.20,-14.80;66.50,-22.50;65.40,-24.40;63.80,-22.40",
    "58.60,-3.10;57.70,-1.80;55.00,-1.40;52.80,1.70;51.20,1.40;50.70,-1.10;50.10,-5.20;51.50,-5.10;53.40,-3.10;54.80,-4.90;56.50,-5.60;58.50,-5.00;58.60,-3.10",
    "55.20,-6.20;53.40,-6.10;52.20,-6.40;51.50,-9.80;53.30,-9.90;54.50,-8.50;55.20,-6.20",
    "71.10,25.80;70.00,29.80;68.00,39.80;66.20,40.00;64.50,40.50;66.50,45.00;68.80,50.00;69.50,60.00;73.20,70.00;73.50,80.50;76.00,95.00;77.70,104.30;74.00,112.00;73.20,126.00;71.50,136.00;70.50,150.00;69.60,162.00;68.90,179.00;66.00,-169.80;64.40,-172.80;60.50,166.00;59.50,151.00;54.00,138.00;48.50,140.20;43.10,131.90;39.80,127.60;35.10,129.00;34.60,126.20;37.60,126.60;39.90,124.30;38.90,121.20;40.00,119.80;37.80,119.20;37.40,122.60;35.00,119.30;31.20,121.80;28.00,121.00;24.50,118.10;22.30,114.20;21.50,109.60;20.20,106.60;16.10,108.20;11.90,109.20;8.60,104.80;10.50,103.50;13.50,100.60;8.00,98.30;1.30,103.80;3.10,101.40;6.50,100.10;12.00,98.50;16.00,94.30;21.50,92.00;22.20,89.10;20.30,86.70;16.20,81.80;13.10,80.30;8.10,77.50;10.00,76.20;15.40,73.80;19.10,72.80;21.60,69.60;23.00,68.80;24.90,67.00;25.20,62.30;25.40,57.80;27.10,56.30;29.90,48.60;26.20,50.20;24.50,54.40;26.30,56.40;22.50,59.80;17.00,54.10;14.50,49.10;12.80,45.00;12.60,43.40;16.90,42.50;21.50,39.10;28.00,34.50;29.90,32.50;31.30,32.30;32.80,35.00;35.50,35.80;36.80,36.20;36.90,30.70;38.40,27.10;40.00,26.20;41.00,29.00;41.20,36.30;41.60,41.60;44.60,37.80;46.50,30.70;45.20,29.70;42.00,28.00;40.60,22.90;38.00,23.70;36.40,22.50;39.60,19.90;42.40,18.50;45.40,13.60;44.40,12.30;41.20,16.80;38.00,15.60;41.20,13.60;44.40,8.90;43.30,5.40;42.50,3.10;39.50,-0.30;36.70,-4.40;36.00,-5.60;37.00,-8.90;38.70,-9.20;41.20,-8.70;43.40,-8.40;43.30,-1.80;44.80,-1.20;47.20,-2.20;48.40,-4.50;49.60,-1.60;51.00,1.90;51.90,4.10;53.30,5.10;53.90,8.70;57.70,10.60;54.50,10.20;54.00,14.20;54.40,18.70;55.70,21.10;57.80,22.60;59.40,24.70;60.20,29.80;60.20,24.90;63.80,22.70;65.80,24.10;63.80,20.30;59.30,18.10;56.20,15.60;55.40,13.00;57.70,11.90;59.90,10.70;58.00,7.00;60.40,5.30;63.40,10.40;68.40,14.40;70.00,19.00;71.10,25.80",
    "35.80,-5.30;35.90,-0.60;36.80,3.10;37.30,9.80;33.90,10.10;32.90,13.20;31.20,16.60;32.10,20.10;31.60,25.20;31.20,29.90;31.30,32.30;29.90,32.50;24.00,35.50;19.60,37.20;15.60,39.50;11.60,43.10;10.40,45.00;11.80,51.30;6.00,49.00;2.00,45.30;-1.70,41.60;-4.00,39.70;-6.80,39.30;-10.30,40.30;-15.00,40.70;-19.80,34.90;-23.90,35.50;-25.90,32.60;-29.90,31.00;-33.00,27.90;-34.00,25.60;-34.80,20.00;-33.90,18.40;-28.60,16.50;-22.90,14.50;-17.30,11.80;-12.40,13.40;-8.80,13.20;-5.90,12.20;-4.00,11.20;-0.70,8.80;4.00,9.70;4.50,7.10;6.40,3.40;5.60,-0.20;4.80,-2.00;5.20,-4.00;4.40,-7.70;6.30,-10.80;8.50,-13.20;10.90,-15.10;12.30,-16.70;14.70,-17.40;18.10,-16.00;20.90,-17.10;23.70,-15.90;27.70,-13.20;30.40,-9.60;33.60,-7.60;35.80,-5.30",
    "-12.00,49.30;-15.30,50.50;-18.10,49.40;-22.10,48.00;-25.00,47.00;-25.60,45.20;-23.40,43.70;-19.00,44.20;-16.00,45.30;-13.50,47.90;-12.00,49.30",
    "45.40,141.90;43.30,145.60;42.00,143.20;42.30,139.80;43.50,141.30;45.40,141.90",
    "41.50,140.90;39.60,142.10;35.70,140.80;34.70,139.00;34.30,136.80;33.50,135.80;34.60,135.40;34.40,133.40;33.90,130.90;35.50,133.20;35.70,136.00;37.50,137.30;39.80,140.00;41.50,140.90",
    "33.90,130.90;32.80,129.80;31.20,130.60;31.40,131.40;33.20,131.70;33.90,130.90",
    "25.30,121.50;24.50,121.80;21.90,120.80;22.60,120.30;24.20,120.50;25.30,121.50",
    "18.50,122.20;16.00,121.60;13.50,123.80;12.60,124.10;14.60,121.00;16.10,119.80;18.50,120.60;18.50,122.20",
    "9.80,125.50;7.10,126.50;6.10,125.20;7.00,122.10;8.50,123.30;9.80,125.50",
    "9.80,80.20;8.60,81.20;6.80,81.80;6.00,80.20;7.00,79.90;9.00,79.90;9.80,80.20",
    "5.50,95.30;3.60,98.70;1.00,102.50;-1.00,104.40;-3.00,106.10;-5.90,105.80;-5.00,103.90;-2.80,101.40;0.20,99.30;3.30,97.20;5.50,95.30",
    "-6.00,106.00;-6.80,108.60;-7.30,112.80;-8.40,114.40;-8.70,114.00;-8.20,111.00;-7.70,108.50;-6.80,105.40;-6.00,106.00",
    "7.00,116.80;5.00,119.00;1.50,117.80;-1.20,116.80;-3.50,116.00;-4.10,114.80;-3.00,111.70;-1.50,110.10;1.00,109.00;2.00,109.70;3.20,113.00;5.00,115.00;7.00,116.80",
    "-0.80,131.30;-2.60,141.00;-6.10,147.60;-9.50,149.30;-10.60,150.70;-9.50,147.10;-8.00,144.00;-9.10,141.00;-8.30,138.90;-5.40,137.90;-4.50,135.20;-2.50,133.10;-0.80,131.30",
    "-12.40,130.80;-12.20,136.80;-14.80,135.60;-16.50,138.00;-17.70,140.80;-12.20,141.80;-10.70,142.50;-14.50,144.60;-16.90,145.80;-19.30,146.80;-23.40,150.80;-27.50,153.00;-33.90,151.20;-37.50,149.90;-38.40,144.90;-38.10,140.60;-35.00,138.60;-35.60,136.80;-32.50,134.00;-31.70,129.00;-33.90,121.90;-35.00,117.90;-33.90,115.00;-32.00,115.80;-27.70,114.20;-21.90,114.10;-20.30,118.60;-18.00,122.20;-14.80,125.00;-14.00,127.50;-12.40,130.80",
    "-40.80,144.70;-41.10,148.00;-43.60,146.90;-42.80,145.30;-40.80,144.70",
    "-34.40,172.70;-36.90,174.80;-37.70,178.50;-39.60,176.90;-41.30,174.80;-39.30,173.80;-36.90,174.50;-34.40,172.70",
    "-40.50,172.70;-41.50,174.00;-43.60,172.70;-46.40,168.40;-45.80,166.50;-43.00,170.20;-40.50,172.70"
)

private class GlobePolygonRing(
    val vx: FloatArray,
    val vy: FloatArray,
    val vz: FloatArray,
    val nx: Float,
    val ny: Float,
    val nz: Float
)

private val PRECOMPUTED_GLOBE_RINGS: List<GlobePolygonRing> by lazy {
    val rings = mutableListOf<GlobePolygonRing>()
    for (raw in WORLD_MAP_RAW_POLYGONS) {
        val pts = raw.split(";")
        if (pts.size < 3) continue
        val n = pts.size
        val vx = FloatArray(n)
        val vy = FloatArray(n)
        val vz = FloatArray(n)
        var sumX = 0f
        var sumY = 0f
        var sumZ = 0f
        for (i in 0 until n) {
            val pair = pts[i].split(",")
            val latRad = Math.toRadians(pair[0].toDouble()).toFloat()
            val lonRad = Math.toRadians(pair[1].toDouble()).toFloat()
            val cosLat = cos(latRad)
            val x = cosLat * sin(lonRad)
            val y = -sin(latRad)
            val z = cosLat * cos(lonRad)
            vx[i] = x
            vy[i] = y
            vz[i] = z
            sumX += x
            sumY += y
            sumZ += z
        }
        val len = sqrt(sumX * sumX + sumY * sumY + sumZ * sumZ).coerceAtLeast(1e-4f)
        rings.add(GlobePolygonRing(vx, vy, vz, sumX / len, sumY / len, sumZ / len))
    }
    rings
}

private class GlobeInteractivePoseState {
    var yaw: Float = 0f
    var pitch: Float = 0f
    var roll: Float = 0f
    var velYaw: Float = 0f
    var velPitch: Float = 0f
    var velRoll: Float = 0f
    var isDragging: Boolean = false
}

@Composable
fun GlobeAxisControlRow(
    axisLabel: String,
    degreeValue: Float,
    minDeg: Float,
    maxDeg: Float,
    stepDeg: Float,
    accentColor: Color,
    fontFamily: FontFamily,
    onChangeDeg: (Float) -> Unit
) {
    val is80s = LocalRetro80sMode.current
    val secondaryCol = rememberRetroSecondaryColor(accentColor)
    val btnShape = RoundedCornerShape(if (is80s) 0.dp else 4.dp)
    val trackShape = RoundedCornerShape(if (is80s) 0.dp else 999.dp)
    val currentOnChange by rememberUpdatedState(onChangeDeg)
    val span = (maxDeg - minDeg).coerceAtLeast(1f)
    val fraction = ((degreeValue - minDeg) / span).coerceIn(0f, 1f)
    val degInt = degreeValue.roundToInt()
    val formattedDeg = if (degInt > 0) "+$degInt°" else "$degInt°"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "$axisLabel $formattedDeg",
            color = if (is80s) accentColor else Color.White,
            fontSize = 8.sp,
            fontFamily = fontFamily,
            style = glowTextStyle(accentColor, 12f),
            modifier = Modifier.width(88.dp)
        )

        Text(
            text = "−",
            color = if (is80s) accentColor else Color(0xFFD4D4D4),
            fontSize = 10.sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            style = glowTextStyle(accentColor, 12f),
            modifier = Modifier
                .border(1.dp, if (is80s) accentColor else Color(0xFF303030), btnShape)
                .background(if (is80s) Color(0xFF080808) else Color(0xFF181818), btnShape)
                .clickable { currentOnChange((degreeValue - stepDeg).coerceIn(minDeg, maxDeg)) }
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(18.dp)
                .pointerInput(minDeg, maxDeg) {
                    detectTapGestures { offset ->
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val f = (offset.x / w).coerceIn(0f, 1f)
                        currentOnChange(minDeg + f * (maxDeg - minDeg))
                    }
                }
                .pointerInput(minDeg, maxDeg) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val f = (change.position.x / w).coerceIn(0f, 1f)
                        currentOnChange(minDeg + f * (maxDeg - minDeg))
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .border(1.dp, if (is80s) accentColor.copy(alpha = 0.55f) else Color(0xFF2E2E2E), trackShape)
                    .background(if (is80s) Color(0xFF060606) else Color(0xFF161616), trackShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .background(secondaryCol, trackShape)
                )
            }
        }

        Text(
            text = "+",
            color = if (is80s) accentColor else Color(0xFFD4D4D4),
            fontSize = 10.sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            style = glowTextStyle(accentColor, 12f),
            modifier = Modifier
                .border(1.dp, if (is80s) accentColor else Color(0xFF303030), btnShape)
                .background(if (is80s) Color(0xFF080808) else Color(0xFF181818), btnShape)
                .clickable { currentOnChange((degreeValue + stepDeg).coerceIn(minDeg, maxDeg)) }
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private class CountryGeoEntry(
    val name: String,
    val minLat: Float,
    val maxLat: Float,
    val minLon: Float,
    val maxLon: Float,
    val centerLat: Float,
    val centerLon: Float
)

private val WORLD_COUNTRY_GEO_TABLE: List<CountryGeoEntry> = listOf(
    CountryGeoEntry("INDIA", 6.7f, 35.7f, 68.1f, 97.4f, 22.0f, 79.0f),
    CountryGeoEntry("UNITED STATES", 24.5f, 49.0f, -124.8f, -66.9f, 39.8f, -98.6f),
    CountryGeoEntry("CANADA", 42.0f, 83.1f, -141.0f, -52.6f, 60.0f, -106.0f),
    CountryGeoEntry("MEXICO", 14.5f, 32.7f, -117.1f, -86.7f, 23.6f, -102.5f),
    CountryGeoEntry("BRAZIL", -33.7f, 5.3f, -73.9f, -34.8f, -14.2f, -51.9f),
    CountryGeoEntry("ARGENTINA", -55.0f, -21.8f, -73.6f, -53.6f, -38.4f, -63.6f),
    CountryGeoEntry("CHILE", -55.9f, -17.5f, -75.6f, -66.4f, -35.7f, -71.5f),
    CountryGeoEntry("PERU", -18.3f, -0.0f, -81.3f, -68.7f, -9.2f, -75.0f),
    CountryGeoEntry("COLOMBIA", -4.2f, 12.5f, -79.0f, -66.9f, 4.6f, -74.3f),
    CountryGeoEntry("VENEZUELA", 0.6f, 12.2f, -73.4f, -59.8f, 6.4f, -66.6f),
    CountryGeoEntry("UNITED KINGDOM", 49.9f, 58.7f, -8.2f, 1.8f, 54.0f, -2.0f),
    CountryGeoEntry("FRANCE", 42.3f, 51.1f, -4.8f, 8.2f, 46.2f, 2.2f),
    CountryGeoEntry("GERMANY", 47.3f, 55.1f, 5.9f, 15.0f, 51.2f, 10.4f),
    CountryGeoEntry("SPAIN", 36.0f, 43.8f, -9.3f, 3.3f, 40.5f, -3.7f),
    CountryGeoEntry("ITALY", 36.6f, 47.1f, 6.6f, 18.5f, 41.9f, 12.6f),
    CountryGeoEntry("NORWAY", 58.0f, 71.2f, 4.6f, 31.1f, 64.5f, 12.0f),
    CountryGeoEntry("SWEDEN", 55.3f, 69.1f, 11.1f, 24.2f, 62.0f, 15.0f),
    CountryGeoEntry("FINLAND", 59.8f, 70.1f, 20.6f, 31.6f, 64.0f, 26.0f),
    CountryGeoEntry("POLAND", 49.0f, 54.8f, 14.1f, 24.1f, 52.0f, 19.1f),
    CountryGeoEntry("UKRAINE", 44.4f, 52.4f, 22.1f, 40.2f, 48.4f, 31.2f),
    CountryGeoEntry("TURKEY", 35.8f, 42.1f, 26.0f, 44.8f, 39.0f, 35.2f),
    CountryGeoEntry("RUSSIA", 41.2f, 77.7f, 27.0f, 180.0f, 61.5f, 96.0f),
    CountryGeoEntry("CHINA", 18.2f, 53.6f, 73.5f, 134.8f, 35.9f, 104.2f),
    CountryGeoEntry("JAPAN", 30.0f, 45.5f, 129.5f, 145.8f, 36.2f, 138.3f),
    CountryGeoEntry("SOUTH KOREA", 33.1f, 38.6f, 126.1f, 129.6f, 35.9f, 127.8f),
    CountryGeoEntry("INDONESIA", -11.0f, 6.0f, 95.0f, 141.0f, -2.5f, 118.0f),
    CountryGeoEntry("AUSTRALIA", -43.6f, -10.7f, 113.2f, 153.6f, -25.3f, 133.8f),
    CountryGeoEntry("NEW ZEALAND", -47.3f, -34.4f, 166.4f, 178.6f, -40.9f, 174.9f),
    CountryGeoEntry("SAUDI ARABIA", 16.4f, 32.2f, 34.5f, 55.7f, 23.9f, 45.1f),
    CountryGeoEntry("IRAN", 25.1f, 39.8f, 44.0f, 63.3f, 32.4f, 53.7f),
    CountryGeoEntry("EGYPT", 22.0f, 31.7f, 24.7f, 36.9f, 26.8f, 30.8f),
    CountryGeoEntry("ALGERIA", 19.0f, 37.1f, -8.7f, 12.0f, 28.0f, 1.7f),
    CountryGeoEntry("MOROCCO", 27.7f, 35.9f, -13.2f, -1.0f, 31.8f, -7.1f),
    CountryGeoEntry("NIGERIA", 4.3f, 13.9f, 2.7f, 14.7f, 9.1f, 8.7f),
    CountryGeoEntry("KENYA", -4.7f, 5.0f, 33.9f, 41.9f, 0.0f, 37.9f),
    CountryGeoEntry("SOUTH AFRICA", -34.8f, -22.1f, 16.5f, 32.9f, -30.6f, 22.9f),
    CountryGeoEntry("MADAGASCAR", -25.6f, -12.0f, 43.2f, 50.5f, -18.8f, 46.9f),
    CountryGeoEntry("GREENLAND", 59.8f, 83.6f, -73.0f, -12.0f, 71.7f, -42.6f)
)

private fun resolveCountryAtGlobeCrosshair(latDeg: Float, lonDeg: Float): String {
    var bestMatch: CountryGeoEntry? = null
    var bestDistSq = Float.MAX_VALUE
    for (entry in WORLD_COUNTRY_GEO_TABLE) {
        if (latDeg in entry.minLat..entry.maxLat && lonDeg in entry.minLon..entry.maxLon) {
            val dLat = latDeg - entry.centerLat
            val dLon = lonDeg - entry.centerLon
            val d2 = dLat * dLat + dLon * dLon
            if (d2 < bestDistSq) {
                bestDistSq = d2
                bestMatch = entry
            }
        }
    }
    if (bestMatch != null) return bestMatch.name
    return when {
        latDeg > 66f -> "ARCTIC OCEAN"
        latDeg < -60f -> "ANTARCTIC SECTOR"
        lonDeg in -70f..-10f && latDeg > 0f -> "NORTH ATLANTIC"
        lonDeg in -45f..15f && latDeg <= 0f -> "SOUTH ATLANTIC"
        lonDeg in 45f..105f && latDeg < 20f -> "INDIAN OCEAN"
        else -> "PACIFIC OCEAN"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RetroFuturisticGlobeView(
    globeTheme: String,
    globeWireframeColor: String,
    hudOverrideColor: Color? = null,
    widgetStyle: String = "default",
    axisTilt2dDeg: Float = -3f,
    axisRotXDeg: Float = 14f,
    axisRotYDeg: Float = -77f,
    interactive: Boolean = true,
    widgetsActive: Boolean = true,
    onLongPressHome: () -> Unit = {},
    onInteractionModeChanged: (Boolean) -> Unit = {},
    onTick: () -> Unit = {}
) {
    val rings = remember { PRECOMPUTED_GLOBE_RINGS }
    val pose = remember {
        GlobeInteractivePoseState().apply {
            yaw = Math.toRadians(axisRotYDeg.toDouble()).toFloat()
            pitch = Math.toRadians(axisRotXDeg.toDouble()).toFloat()
            roll = Math.toRadians(axisTilt2dDeg.toDouble()).toFloat()
        }
    }
    var isInteractionUnlocked by remember { mutableStateOf(false) }
    var autoSpinPhase by remember { mutableStateOf(0f) }
    var frameInvalidateTick by remember { mutableStateOf(0L) }

    LaunchedEffect(axisRotYDeg, axisRotXDeg, axisTilt2dDeg) {
        if (!isInteractionUnlocked) {
            pose.yaw = Math.toRadians(axisRotYDeg.toDouble()).toFloat()
            pose.pitch = Math.toRadians(axisRotXDeg.toDouble()).toFloat()
            pose.roll = Math.toRadians(axisTilt2dDeg.toDouble()).toFloat()
        }
    }

    LaunchedEffect(widgetsActive, isInteractionUnlocked) {
        if (!widgetsActive) return@LaunchedEffect
        var lastNs = System.nanoTime()
        while (isActive && widgetsActive) {
            withFrameNanos { nowNs ->
                val dt = ((nowNs - lastNs) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNs = nowNs
                if (!pose.isDragging) {
                    if (isInteractionUnlocked) {
                        pose.yaw += pose.velYaw * dt
                        pose.pitch = (pose.pitch + pose.velPitch * dt).coerceIn(-1.45f, 1.45f)
                        pose.velYaw *= 0.92f
                        pose.velPitch *= 0.92f
                    } else {
                        autoSpinPhase = (autoSpinPhase + dt * 0.28f) % (2f * PI.toFloat())
                    }
                }
                frameInvalidateTick = nowNs
            }
        }
    }

    val wfColor = remember(globeWireframeColor) {
        resolveLauncherAccentColor(globeWireframeColor)
    }
    val isWireframe = globeTheme == "wireframe" || widgetStyle == "80s_retro" || widgetStyle == "cyber_hud"

    val sharedRx = remember { FloatArray(512) }
    val sharedRy = remember { FloatArray(512) }
    val sharedRz = remember { FloatArray(512) }
    val sharedFillPath = remember { Path() }
    val sharedBorderPath = remember { Path() }

    val hudDegreePaint = remember {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
            isFakeBoldText = true
        }
    }
    val hudCountryPaint = remember {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
            isFakeBoldText = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(interactive, isInteractionUnlocked) {
                if (!interactive) return@pointerInput
                detectTapGestures(
                    onDoubleTap = {
                        val next = !isInteractionUnlocked
                        if (next) {
                            pose.yaw = Math.toRadians(axisRotYDeg.toDouble()).toFloat() + autoSpinPhase
                            autoSpinPhase = 0f
                        }
                        isInteractionUnlocked = next
                        onInteractionModeChanged(next)
                        onTick()
                    },
                    onLongPress = { onLongPressHome() }
                )
            }
            .pointerInput(interactive, isInteractionUnlocked) {
                if (!interactive || !isInteractionUnlocked) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        pose.isDragging = true
                        pose.velYaw = 0f
                        pose.velPitch = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val sens = 0.0075f
                        pose.yaw += dragAmount.x * sens
                        pose.pitch = (pose.pitch - dragAmount.y * sens).coerceIn(-1.45f, 1.45f)
                        pose.velYaw = dragAmount.x * sens * 18f
                        pose.velPitch = -dragAmount.y * sens * 18f
                    },
                    onDragEnd = { pose.isDragging = false },
                    onDragCancel = { pose.isDragging = false }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tickRead = frameInvalidateTick
            if (tickRead < 0L) return@Canvas
            val w = size.width
            val h = size.height
            val cx = w * 0.5f
            val cy = h * 0.49f
            // Keep full-size globe radius (0.415f) even when interaction HUD is unlocked!
            val radius = (min(w, h) * 0.415f).coerceAtLeast(24f)

            val effectiveYaw = if (isInteractionUnlocked) pose.yaw else (Math.toRadians(axisRotYDeg.toDouble()).toFloat() + autoSpinPhase)
            val cosY = cos(effectiveYaw)
            val sinY = sin(effectiveYaw)
            val cosX = cos(pose.pitch)
            val sinX = sin(pose.pitch)
            val cosZ = cos(pose.roll)
            val sinZ = sin(pose.roll)

            // 1. Ocean Sphere Base
            drawCircle(
                color = if (isWireframe) Color(0xFF020604) else Color(0xFF041424),
                radius = radius,
                center = Offset(cx, cy)
            )

            // 2. Graticule Latitude / Longitude Wireframe Grid
            val gridColor = if (isWireframe) wfColor.copy(alpha = 0.16f) else Color(0xFF1E90FF).copy(alpha = 0.18f)
            val gridStroke = 0.75.dp.toPx()
            for (latDeg in -60..60 step 30) {
                val latR = Math.toRadians(latDeg.toDouble()).toFloat()
                val cosLat = cos(latR)
                val y0 = -sin(latR)
                sharedBorderPath.reset()
                var pen = false
                for (lonDeg in -180..180 step 10) {
                    val lonR = Math.toRadians(lonDeg.toDouble()).toFloat()
                    val x0 = cosLat * sin(lonR)
                    val z0 = cosLat * cos(lonR)
                    val x1 = x0 * cosY + z0 * sinY
                    val z1 = -x0 * sinY + z0 * cosY
                    val y2 = y0 * cosX - z1 * sinX
                    val z2 = y0 * sinX + z1 * cosX
                    val rx = x1 * cosZ - y2 * sinZ
                    val ry = x1 * sinZ + y2 * cosZ
                    if (z2 >= 0f) {
                        val sx = cx + rx * radius
                        val sy = cy + ry * radius
                        if (!pen) {
                            sharedBorderPath.moveTo(sx, sy)
                            pen = true
                        } else {
                            sharedBorderPath.lineTo(sx, sy)
                        }
                    } else {
                        pen = false
                    }
                }
                drawPath(sharedBorderPath, color = gridColor, style = Stroke(width = gridStroke))
            }

            // 3. Continents + Triangulated Geodesic Land Mesh
            val wfStroke = Stroke(width = 1.15.dp.toPx())
            val triStrokePx = 0.65.dp.toPx()
            val triMeshColor = wfColor.copy(alpha = 0.36f)

            for (ring in rings) {
                val nx1 = ring.nx * cosY + ring.nz * sinY
                val nz1 = -ring.nx * sinY + ring.nz * cosY
                val ny2 = ring.ny * cosX - nz1 * sinX
                val rnz = ring.ny * sinX + nz1 * cosX
                if (rnz < -0.35f) continue

                val n = min(ring.vx.size, sharedRx.size)
                var anyFront = false
                for (i in 0 until n) {
                    val x0 = ring.vx[i]
                    val y0 = ring.vy[i]
                    val z0 = ring.vz[i]
                    val x1 = x0 * cosY + z0 * sinY
                    val z1 = -x0 * sinY + z0 * cosY
                    val y2 = y0 * cosX - z1 * sinX
                    val z2 = y0 * sinX + z1 * cosX
                    sharedRx[i] = x1 * cosZ - y2 * sinZ
                    sharedRy[i] = x1 * sinZ + y2 * cosZ
                    sharedRz[i] = z2
                    if (z2 >= 0f) anyFront = true
                }
                if (!anyFront) continue

                val lambert = (nx1 * -0.25f + ny2 * -0.25f + rnz * 0.93f).coerceIn(0f, 1f)
                sharedFillPath.reset()
                var started = false
                for (i in 0 until n) {
                    val rx = sharedRx[i]
                    val ry = sharedRy[i]
                    val rz = sharedRz[i]
                    val px: Float
                    val py: Float
                    if (rz >= 0f) {
                        px = cx + rx * radius
                        py = cy + ry * radius
                    } else {
                        val normXY = hypot(rx, ry).coerceAtLeast(1e-4f)
                        px = cx + (rx / normXY) * radius * 0.996f
                        py = cy + (ry / normXY) * radius * 0.996f
                    }
                    if (!started) {
                        sharedFillPath.moveTo(px, py)
                        started = true
                    } else {
                        sharedFillPath.lineTo(px, py)
                    }
                }
                if (started) {
                    sharedFillPath.close()
                    drawPath(
                        path = sharedFillPath,
                        color = if (!isWireframe) {
                            Color(0xFF0E9F54).copy(alpha = (0.38f + 0.24f * lambert).coerceIn(0.32f, 0.64f))
                        } else {
                            wfColor.copy(alpha = (0.08f + 0.10f * lambert).coerceIn(0.06f, 0.18f))
                        }
                    )
                }

                // Triangulated Geodesic Land Mesh Chords
                if (n >= 8) {
                    val stride = (n / 12).coerceAtLeast(2)
                    var idxA = 0
                    while (idxA < n) {
                        val idxB = (idxA + stride * 2) % n
                        if (sharedRz[idxA] > 0.04f && sharedRz[idxB] > 0.04f) {
                            val ax = sharedRx[idxA]
                            val ay = sharedRy[idxA]
                            val bx = sharedRx[idxB]
                            val by = sharedRy[idxB]
                            if (hypot(ax - bx, ay - by) < 0.45f) {
                                drawLine(
                                    color = triMeshColor,
                                    start = Offset(cx + ax * radius, cy + ay * radius),
                                    end = Offset(cx + bx * radius, cy + by * radius),
                                    strokeWidth = triStrokePx
                                )
                            }
                        }
                        idxA += stride
                    }
                }

                sharedBorderPath.reset()
                var penDown = false
                for (i in 0 until n) {
                    val rx1 = sharedRx[i]
                    val ry1 = sharedRy[i]
                    val rz1 = sharedRz[i]
                    if (rz1 >= 0f) {
                        val sx = cx + rx1 * radius
                        val sy = cy + ry1 * radius
                        if (!penDown) {
                            sharedBorderPath.moveTo(sx, sy)
                            penDown = true
                        } else {
                            sharedBorderPath.lineTo(sx, sy)
                        }
                    } else {
                        penDown = false
                    }
                }
                drawPath(
                    path = sharedBorderPath,
                    color = wfColor.copy(alpha = 0.95f),
                    style = wfStroke
                )
            }

            // 4. Outer Planetary Horizon Rim with Neon Bloom
            drawCircle(
                color = wfColor.copy(alpha = 0.35f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 3.8.dp.toPx())
            )
            drawCircle(
                color = wfColor,
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.4.dp.toPx())
            )

            // 5. Outer 360° Compass Angle Ring & Double-Tap Targeting Crosshair HUD
            val hudColor = hudOverrideColor ?: wfColor
            val showCompassRing = isInteractionUnlocked || widgetStyle == "80s_retro" || widgetStyle == "cyber_hud"
            if (showCompassRing) {
                val tickBaseR = radius * 1.015f
                val minorTickLen = 2.5.dp.toPx()
                val medTickLen = 4.5.dp.toPx()
                val majorTickLen = 6.5.dp.toPx()
                val labelRadius = radius * 1.105f
                val yawDegOffset = ((effectiveYaw * 180.0 / PI).toInt() % 360 + 360) % 360

                hudDegreePaint.textSize = 7.2.dp.toPx()
                hudDegreePaint.color = android.graphics.Color.argb(
                    235,
                    (hudColor.red * 255).toInt(),
                    (hudColor.green * 255).toInt(),
                    (hudColor.blue * 255).toInt()
                )

                for (deg in 0 until 360 step 2) {
                    val rad = ((deg - 90.0) * PI / 180.0).toFloat()
                    val c = cos(rad)
                    val s = sin(rad)
                    val isMajor = deg % 30 == 0
                    val isMed = deg % 10 == 0
                    val tLen = when {
                        isMajor -> majorTickLen
                        isMed -> medTickLen
                        else -> minorTickLen
                    }
                    val tickAlpha = when {
                        isMajor -> 0.92f
                        isMed -> 0.74f
                        else -> 0.40f
                    }
                    drawLine(
                        color = hudColor.copy(alpha = tickAlpha),
                        start = Offset(cx + c * tickBaseR, cy + s * tickBaseR),
                        end = Offset(cx + c * (tickBaseR + tLen), cy + s * (tickBaseR + tLen)),
                        strokeWidth = if (isMajor) 1.1.dp.toPx() else 0.7.dp.toPx()
                    )

                    if (deg % 15 == 0 && deg != 0 && deg != 180) {
                        val displayAngle = ((deg + yawDegOffset) % 360)
                        val lx = cx + c * labelRadius
                        val ly = cy + s * labelRadius + (hudDegreePaint.textSize * 0.35f)
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText("${displayAngle}°", lx, ly, hudDegreePaint)
                        }
                    }
                }
            }

            if (isInteractionUnlocked) {
                val crossHalfSpan = radius * 1.14f
                val crossCoreStroke = 0.9.dp.toPx()
                val crossGlowStroke = 2.8.dp.toPx()

                drawLine(
                    color = hudColor.copy(alpha = 0.32f),
                    start = Offset(cx - crossHalfSpan, cy),
                    end = Offset(cx + crossHalfSpan, cy),
                    strokeWidth = crossGlowStroke
                )
                drawLine(
                    color = hudColor.copy(alpha = 0.32f),
                    start = Offset(cx, cy - crossHalfSpan),
                    end = Offset(cx, cy + crossHalfSpan),
                    strokeWidth = crossGlowStroke
                )
                drawLine(
                    color = hudColor,
                    start = Offset(cx - crossHalfSpan, cy),
                    end = Offset(cx + crossHalfSpan, cy),
                    strokeWidth = crossCoreStroke
                )
                drawLine(
                    color = hudColor,
                    start = Offset(cx, cy - crossHalfSpan),
                    end = Offset(cx, cy + crossHalfSpan),
                    strokeWidth = crossCoreStroke
                )

                val reticleTickHalf = 4.2.dp.toPx()
                val reticleFractions = floatArrayOf(-0.62f, -0.32f, 0.32f, 0.62f)
                for (rf in reticleFractions) {
                    val offsetPx = rf * radius
                    drawLine(
                        color = hudColor,
                        start = Offset(cx - reticleTickHalf, cy + offsetPx),
                        end = Offset(cx + reticleTickHalf, cy + offsetPx),
                        strokeWidth = crossCoreStroke
                    )
                    drawLine(
                        color = hudColor,
                        start = Offset(cx + offsetPx, cy - reticleTickHalf),
                        end = Offset(cx + offsetPx, cy + reticleTickHalf),
                        strokeWidth = crossCoreStroke
                    )
                }

                drawCircle(
                    color = hudColor,
                    radius = 2.4.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 0.9.dp.toPx())
                )

                val centerLatDeg = (-(pose.pitch * 180.0 / PI).toFloat()).coerceIn(-90f, 90f)
                var centerLonDeg = ((-effectiveYaw * 180.0 / PI).toFloat()) % 360f
                if (centerLonDeg > 180f) centerLonDeg -= 360f
                if (centerLonDeg < -180f) centerLonDeg += 360f

                val countryName = resolveCountryAtGlobeCrosshair(centerLatDeg, centerLonDeg)
                val latHem = if (centerLatDeg >= 0f) "N" else "S"
                val lonHem = if (centerLonDeg >= 0f) "E" else "W"
                val latInt = abs(centerLatDeg).roundToInt()
                val lonInt = abs(centerLonDeg).roundToInt()
                val bottomReadout = "<<< $countryName [$latInt°$latHem $lonInt°$lonHem] >>>"

                hudCountryPaint.textSize = 10.5.dp.toPx()
                hudCountryPaint.color = android.graphics.Color.argb(
                    255,
                    (hudColor.red * 255).toInt(),
                    (hudColor.green * 255).toInt(),
                    (hudColor.blue * 255).toInt()
                )
                val pillY = (h - 6.dp.toPx()).coerceAtLeast(cy + radius * 0.88f)
                val textWidth = hudCountryPaint.measureText(bottomReadout)
                val boxW = textWidth + 20.dp.toPx()
                val boxH = 19.dp.toPx()
                val boxLeft = cx - boxW * 0.5f
                val boxTop = pillY - boxH * 0.74f

                drawRect(
                    color = Color(0xFF050505).copy(alpha = 0.92f),
                    topLeft = Offset(boxLeft, boxTop),
                    size = Size(boxW, boxH)
                )
                drawRect(
                    color = hudColor,
                    topLeft = Offset(boxLeft, boxTop),
                    size = Size(boxW, boxH),
                    style = Stroke(width = 1.2.dp.toPx())
                )
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(bottomReadout, cx, pillY - 2.dp.toPx(), hudCountryPaint)
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerView(
    apps: List<AppEntry>,
    selectedFont: FontFamily,
    lomoFontFamily: FontFamily,
    uppercaseHome: Boolean,
    drawerTextSizeSp: Int,
    accentColor: Color,
    secondaryRetroColor: Color = accentColor,
    waveScrubMode: String,
    drawerWaveIntensity: Float,
    drawerBgMode: String = "opaque",
    drawerBgIntensity: Int = 65,
    isDrawerOpenNow: Boolean = true,
    onDragDelta: (Float) -> Unit,
    onSettle: (Float) -> Unit,
    onLaunchApp: (String) -> Unit,
    onLongPressApp: (AppEntry) -> Unit,
    onWaveTick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchFocusRequester = remember { FocusRequester() }
    var query by remember { mutableStateOf("") }

    val filteredApps = remember(apps, query) {
        val q = query.trim()
        if (q.isEmpty()) apps else apps.filter { it.label.contains(q, ignoreCase = true) }
    }

    val currentOnDragDelta by rememberUpdatedState(onDragDelta)
    val currentOnSettle by rememberUpdatedState(onSettle)
    var isPullingDrawerClosed by remember { mutableStateOf(false) }
    var totalPullDownPx by remember { mutableStateOf(0f) }

    val drawerNestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isPullingDrawerClosed && source == NestedScrollSource.Drag) {
                    totalPullDownPx = (totalPullDownPx + available.y).coerceAtLeast(0f)
                    currentOnDragDelta(available.y)
                    if (totalPullDownPx <= 0f) {
                        isPullingDrawerClosed = false
                    }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.Drag && available.y > 0f && consumed.y == 0f) {
                    val atTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                    if (atTop) {
                        isPullingDrawerClosed = true
                        totalPullDownPx += available.y
                        currentOnDragDelta(available.y)
                        return Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (isPullingDrawerClosed) {
                    val pulled = totalPullDownPx
                    isPullingDrawerClosed = false
                    totalPullDownPx = 0f
                    currentOnSettle(if (available.y > 400f) 150f else pulled)
                    return available
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (isPullingDrawerClosed) {
                    val pulled = totalPullDownPx
                    isPullingDrawerClosed = false
                    totalPullDownPx = 0f
                    currentOnSettle(pulled)
                }
                return Velocity.Zero
            }
        }
    }

    val normDrawerBg = (drawerBgIntensity.coerceIn(0, 100) / 100f)
    val drawerRootBgColor = when (drawerBgMode) {
        "transparent" -> Color.Black.copy(alpha = (1f - normDrawerBg * 0.92f).coerceIn(0.06f, 0.96f))
        "blurry" -> Color(0xFF050505).copy(alpha = (0.76f - normDrawerBg * 0.56f).coerceIn(0.16f, 0.82f))
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(drawerRootBgColor)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .nestedScroll(drawerNestedScroll)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val is80sDrawer = LocalRetro80sMode.current
            // Search Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroNeonBorder(
                        if (is80sDrawer) 1.4.dp else 1.dp,
                        if (is80sDrawer) accentColor else Color(0xFF262626),
                        RoundedCornerShape(if (is80sDrawer) 0.dp else 8.dp),
                        glowEnabled = is80sDrawer
                    )
                    .background(Color(0xFF080808), RoundedCornerShape(if (is80sDrawer) 0.dp else 8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (is80sDrawer) "> " else "/ ",
                    color = secondaryRetroColor,
                    fontSize = if (is80sDrawer) 11.sp else 13.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = glowTextStyle(secondaryRetroColor, 16f)
                )
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = if (is80sDrawer) accentColor else Color.White,
                        fontSize = 13.sp,
                        fontFamily = lomoFontFamily
                    ).merge(glowTextStyle(accentColor, 14f)),
                    cursorBrush = SolidColor(secondaryRetroColor),
                    enabled = isDrawerOpenNow,
                    modifier = Modifier
                        .weight(1f)
                        .focusProperties { canFocus = isDrawerOpenNow }
                        .focusRequester(searchFocusRequester),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search apps...",
                                color = if (is80sDrawer) accentColor.copy(alpha = 0.55f) else Color(0xFF525252),
                                fontSize = 12.sp,
                                fontFamily = lomoFontFamily,
                                style = glowTextStyle(accentColor, 10f)
                            )
                        }
                        inner()
                    }
                )
                if (query.isNotEmpty()) {
                    Text(
                        text = "×",
                        color = secondaryRetroColor,
                        fontSize = 15.sp,
                        fontFamily = lomoFontFamily,
                        style = glowTextStyle(secondaryRetroColor, 14f),
                        modifier = Modifier
                            .clickable {
                                query = ""
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val headerFontSp = (drawerTextSizeSp * 0.68f).coerceIn(10f, 16f).sp
            val starFontSp = (drawerTextSizeSp * 0.65f).coerceIn(9f, 15f).sp

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 20.dp)
                ) {
                    itemsIndexed(
                        items = filteredApps,
                        key = { _, app -> app.packageName },
                        contentType = { _, _ -> "app_row" }
                    ) { index, app ->
                        val prevLetter = if (index > 0) filteredApps[index - 1].firstLetter else null
                        val nextLetter = if (index < filteredApps.lastIndex) filteredApps[index + 1].firstLetter else null
                        val isFirstInAlphabetGroup = app.firstLetter != prevLetter
                        val isLastInAlphabetGroup = app.firstLetter != nextLetter

                        val displayLabel = if (uppercaseHome) {
                            app.label.uppercase(Locale.getDefault())
                        } else {
                            app.label
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (isFirstInAlphabetGroup) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (index == 0) 4.dp else 10.dp, bottom = 2.dp)
                                ) {
                                    Text(
                                        text = app.firstLetter.toString(),
                                        color = secondaryRetroColor,
                                        fontSize = headerFontSp,
                                        fontFamily = lomoFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        style = glowTextStyle(secondaryRetroColor, 16f)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(if (is80sDrawer) accentColor.copy(alpha = 0.4f) else Color(0xFF1A1A1A))
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            query = ""
                                            focusManager.clearFocus(force = true)
                                            keyboardController?.hide()
                                            onLaunchApp(app.packageName)
                                        },
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
                                        text = displayLabel,
                                        color = if (is80sDrawer) accentColor else Color(0xFFF5F5F5),
                                        fontSize = drawerTextSizeSp.sp,
                                        fontFamily = selectedFont,
                                        style = glowTextStyle(accentColor, 16f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (app.isPinned) {
                                        Text(
                                            text = "★",
                                            color = secondaryRetroColor,
                                            fontSize = starFontSp,
                                            fontFamily = lomoFontFamily,
                                            style = glowTextStyle(secondaryRetroColor, 16f)
                                        )
                                    }
                                }
                            }

                            if (isLastInAlphabetGroup) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp, bottom = 4.dp)
                                        .height(1.dp)
                                        .background(if (is80sDrawer) accentColor.copy(alpha = 0.35f) else Color(0xFF222222))
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

                AlphabetScrubberRail(
                    accentColor = secondaryRetroColor,
                    waveScrubMode = waveScrubMode,
                    drawerWaveIntensity = drawerWaveIntensity,
                    lomoFontFamily = lomoFontFamily,
                    onSelectLetter = { chosenLetter ->
                        onWaveTick()
                        if (filteredApps.isNotEmpty()) {
                            val exactIdx = filteredApps.indexOfFirst { it.firstLetter == chosenLetter }
                            val targetIdx = if (exactIdx >= 0) {
                                exactIdx
                            } else {
                                val nextIdx = filteredApps.indexOfFirst { it.firstLetter >= chosenLetter }
                                if (nextIdx >= 0) nextIdx else filteredApps.lastIndex
                            }
                            scope.launch {
                                listState.scrollToItem(targetIdx, scrollOffset = 0)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AlphabetScrubberRail(
    accentColor: Color,
    waveScrubMode: String,
    drawerWaveIntensity: Float,
    lomoFontFamily: FontFamily,
    onSelectLetter: (Char) -> Unit
) {
    val is80s = LocalRetro80sMode.current
    val primaryCol = rememberRetroPrimaryColor(accentColor)
    val density = LocalDensity.current
    val letters = remember { ('A'..'Z').toList() }
    var railHeightPx by remember { mutableStateOf(1f) }
    var scrubLocalY by remember { mutableStateOf<Float?>(null) }
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    val currentOnSelect by rememberUpdatedState(onSelectLetter)

    val effectiveWaveMult = if (waveScrubMode == "off") 0f else drawerWaveIntensity.coerceIn(0f, 2.0f)
    val letterWaveRadiusPx = with(density) { (65.dp + 55.dp * effectiveWaveMult).toPx() }
    val maxLetterShiftPx = with(density) { (22.dp * effectiveWaveMult).toPx() }
    val voxelBoxSizeDp = 54.dp
    val voxelBoxSizePx = with(density) { voxelBoxSizeDp.toPx() }

    fun updateScrubFromLocalY(localY: Float, currentRailHeight: Float) {
        val validHeight = currentRailHeight.coerceAtLeast(1f)
        railHeightPx = validHeight
        val clampedLocalY = localY.coerceIn(0f, validHeight)
        scrubLocalY = clampedLocalY

        val floatIdx = ((clampedLocalY / validHeight) * letters.size).coerceIn(0f, (letters.size - 1).toFloat())
        val idx = floatIdx.toInt().coerceIn(0, letters.lastIndex)
        val chosenLetter = letters[idx]

        if (activeLetter != chosenLetter) {
            activeLetter = chosenLetter
            currentOnSelect(chosenLetter)
        }
    }

    val targetIndicatorY = remember(scrubLocalY, railHeightPx, voxelBoxSizePx) {
        val sy = scrubLocalY ?: (railHeightPx * 0.5f)
        (sy - voxelBoxSizePx * 0.5f).coerceIn(0f, (railHeightPx - voxelBoxSizePx).coerceAtLeast(0f))
    }
    val animatedIndicatorY by animateFloatAsState(
        targetValue = targetIndicatorY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "voxelBubbleY"
    )

    Box(
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight()
                .onGloballyPositioned { coords ->
                    railHeightPx = coords.size.height.toFloat().coerceAtLeast(1f)
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        updateScrubFromLocalY(down.position.y, size.height.toFloat())
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null && change.pressed) {
                                change.consume()
                                updateScrubFromLocalY(change.position.y, size.height.toFloat())
                            }
                        } while (event.changes.any { it.pressed })
                        scrubLocalY = null
                        activeLetter = null
                    }
                },
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            letters.forEachIndexed { lIdx, ch ->
                val isCurrent = activeLetter == ch
                val currentScrub = scrubLocalY
                val targetLetterFactor = if (currentScrub != null && effectiveWaveMult > 0.01f) {
                    val lCenterY = ((lIdx + 0.5f) / letters.size.toFloat()) * railHeightPx
                    val d = abs(currentScrub - lCenterY)
                    if (d < letterWaveRadiusPx) {
                        cos((d / letterWaveRadiusPx) * (PI / 2)).toFloat().coerceIn(0f, 1f)
                    } else 0f
                } else 0f

                val animLetterFactor by animateFloatAsState(
                    targetValue = targetLetterFactor,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessHigh
                    ),
                    label = "letterWave"
                )

                val chColor = when {
                    isCurrent -> accentColor
                    animLetterFactor > 0.4f -> if (is80s) primaryCol else Color.White
                    is80s -> primaryCol.copy(alpha = 0.65f)
                    else -> Color(0xFF555555)
                }
                Text(
                    text = ch.toString(),
                    color = chColor,
                    fontSize = if (isCurrent) 12.sp else 9.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    style = glowTextStyle(chColor, 14f),
                    modifier = Modifier
                        .graphicsLayer {
                            if (animLetterFactor > 0.005f) {
                                translationX = -animLetterFactor * maxLetterShiftPx
                                val sc = 1f + animLetterFactor * (0.35f * effectiveWaveMult.coerceIn(0.4f, 1.5f))
                                scaleX = sc
                                scaleY = sc
                            }
                        }
                        .padding(vertical = 0.5.dp)
                )
            }
        }

        if (activeLetter != null && scrubLocalY != null) {
            Box(
                modifier = Modifier
                    .padding(end = 42.dp)
                    .offset { IntOffset(x = 0, y = animatedIndicatorY.roundToInt()) }
                    .size(46.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val step = 4.dp.toPx()
                    val strokePx = 1.2.dp.toPx()
                    val half = strokePx * 0.5f

                    val boxPath = Path().apply {
                        moveTo(step, half)
                        lineTo(w - step, half)
                        lineTo(w - step, step)
                        lineTo(w - half, step)
                        lineTo(w - half, h - step)
                        lineTo(w - step, h - step)
                        lineTo(w - step, h - half)
                        lineTo(step, h - half)
                        lineTo(step, h - step)
                        lineTo(half, h - step)
                        lineTo(half, step)
                        lineTo(step, step)
                        close()
                    }
                    drawPath(path = boxPath, color = Color.Black)
                    drawPath(
                        path = boxPath,
                        color = accentColor,
                        style = Stroke(width = strokePx)
                    )
                }

                Text(
                    text = activeLetter.toString(),
                    color = if (is80s) accentColor else Color.White,
                    fontSize = 20.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = glowTextStyle(accentColor, 18f)
                )
            }
        }
    }
}

@Composable
fun WidgetsManagerView(
    activeWidgets: List<String>,
    widgetSizes: Map<String, String>,
    widgetStyles: Map<String, String> = emptyMap(),
    leftPageEnabled: Boolean = true,
    initialTargetLeftPage: Boolean = false,
    leftPageWidgets: List<String> = emptyList(),
    leftWidgetSizes: Map<String, String> = emptyMap(),
    leftWidgetStyles: Map<String, String> = emptyMap(),
    accentColor: Color,
    secondaryRetroColor: Color = accentColor,
    lomoFontFamily: FontFamily,
    use24Hour: Boolean,
    batteryBarStyle: String = "retro_blocks",
    onChangeBatteryBarStyle: (String) -> Unit = {},
    customPinnedOrderEnabled: Boolean = true,
    quickNote: String,
    globeTheme: String,
    globeWireframeColor: String,
    onToggleCustomPinnedOrder: () -> Unit = {},
    onToggle24h: () -> Unit,
    onChangeQuickNote: (String) -> Unit,
    onChangeGlobeTheme: (String) -> Unit,
    onChangeGlobeWireframeColor: (String) -> Unit,
    onUpdateWidgets: (List<String>) -> Unit,
    onUpdateLeftWidgets: (List<String>) -> Unit = {},
    onSetWidgetStyle: (String, String) -> Unit = { _, _ -> },
    onSetLeftWidgetStyle: (String, String) -> Unit = { _, _ -> },
    onCycleWidgetSize: (String) -> Unit,
    onSetWidgetSize: (String, String) -> Unit,
    onMoveWidget: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    val is80sWm = LocalRetro80sMode.current
    val wmBtnShape = RoundedCornerShape(if (is80sWm) 0.dp else 4.dp)
    val wmPillShape = RoundedCornerShape(if (is80sWm) 0.dp else 6.dp)
    var editingLeftPage by remember { mutableStateOf(leftPageEnabled && initialTargetLeftPage) }
    val targetWidgetsList = if (leftPageEnabled && editingLeftPage) leftPageWidgets else activeWidgets
    val targetStylesMap = if (leftPageEnabled && editingLeftPage) leftWidgetStyles else widgetStyles
    val onUpdateTargetWidgets: (List<String>) -> Unit = if (leftPageEnabled && editingLeftPage) onUpdateLeftWidgets else onUpdateWidgets
    val onUpdateTargetStyle: (String, String) -> Unit = if (leftPageEnabled && editingLeftPage) onSetLeftWidgetStyle else onSetWidgetStyle
    val allWidgets = listOf(
        "clock" to "Dot-Matrix Clock, Date & Battery Bar",
        "battery_bar" to "Old School Blocky Battery & Time Header",
        "week_strip" to "7-Day Strip & Month Days Block Bar",
        "pinned_apps" to "Pinned Home Apps List",
        "retro_calendar" to "Dot-Matrix Month Calendar Grid",
        "year_matrix" to "365-Day Dot-Matrix Year Progress",
        "retro_flip_clock" to "Split-Flap Mechanical Clock",
        "analog_chrono" to "Vector Analog Watch Dial",
        "precision_chrono" to "Interactive Split-Lap Chronograph",
        "globe" to "3D Interactive Spinning Globe",
        "torch" to "CMF Flashlight Quick Toggle",
        "storage" to "Live RAM & ROM Telemetry Bar",
        "zen" to "Zen Focus Timer (25m)",
        "quote" to "Minimalist Design Quote",
        "scratchpad" to "One-Line Terminal Scratchpad"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .pointerInput(Unit) {
                var totalDy = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDy = 0f },
                    onVerticalDrag = { _, dragAmount -> totalDy += dragAmount },
                    onDragEnd = {
                        if (totalDy < -85f) onBack()
                    }
                )
            }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (is80sWm) "Widget Configuration" else "Widget Manager",
                color = if (is80sWm) accentColor else Color.White,
                fontSize = 16.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(accentColor, 20f)
            )
            Text(
                text = "DONE",
                color = Color.Black,
                fontSize = 11.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .retroNeonBorder(1.dp, if (is80sWm) secondaryRetroColor else Color.White, wmPillShape, glowEnabled = is80sWm)
                    .background(if (is80sWm) secondaryRetroColor else Color.White, wmPillShape)
                    .clickable { onBack() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        if (leftPageEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroNeonBorder(
                        1.dp,
                        if (is80sWm) accentColor else Color(0xFF282828),
                        RoundedCornerShape(if (is80sWm) 0.dp else 8.dp),
                        glowEnabled = is80sWm,
                        glowRadiusDp = 4.dp
                    )
                    .background(Color(0xFF0A0A0A), RoundedCornerShape(if (is80sWm) 0.dp else 8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    false to "MAIN PAGE",
                    true to "LEFT PAGE"
                ).forEach { (isLeft, tabLabel) ->
                    val selected = editingLeftPage == isLeft
                    Text(
                        text = tabLabel,
                        color = if (selected) Color.Black else if (is80sWm) accentColor else Color.White,
                        fontSize = 10.sp,
                        fontFamily = lomoFontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = if (selected) TextStyle.Default else glowTextStyle(accentColor, 14f),
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selected) secondaryRetroColor else Color(0xFF141414), wmPillShape)
                            .clickable { editingLeftPage = isLeft }
                            .padding(vertical = 7.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(allWidgets) { (id, title) ->
                val enabled = targetWidgetsList.contains(id)
                val wmCardShape = RoundedCornerShape(if (is80sWm) 0.dp else 12.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .retroNeonBorder(
                            if (is80sWm) 1.4.dp else 1.dp,
                            if (is80sWm) (if (enabled) accentColor else accentColor.copy(alpha = 0.55f)) else (if (enabled) Color(0xFF333333) else Color(0xFF1A1A1A)),
                            wmCardShape,
                            glowEnabled = is80sWm && enabled,
                            glowRadiusDp = 5.dp
                        )
                        .background(if (enabled) Color(0xFF080808) else Color(0xFF040404), wmCardShape)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val titleCol = when {
                            is80sWm && enabled -> accentColor
                            is80sWm -> accentColor.copy(alpha = 0.72f)
                            enabled -> Color.White
                            else -> Color(0xFF737373)
                        }
                        Text(
                            text = title,
                            color = titleCol,
                            fontSize = 12.sp,
                            fontFamily = lomoFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = glowTextStyle(titleCol, 16f),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (enabled) "ACTIVE [×]" else "+ ADD",
                            color = if (enabled) secondaryRetroColor else if (is80sWm) accentColor else Color(0xFF8A8A8A),
                            fontSize = 11.sp,
                            fontFamily = lomoFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = glowTextStyle(if (enabled) secondaryRetroColor else accentColor, 14f),
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (enabled) secondaryRetroColor else if (is80sWm) accentColor.copy(alpha = 0.6f) else Color(0xFF262626),
                                    wmPillShape
                                )
                                .clickable {
                                    val next = if (enabled) targetWidgetsList - id else targetWidgetsList + id
                                    onUpdateTargetWidgets(next)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (enabled) {
                        val currentStyle = targetStylesMap[id] ?: if (id == "battery_bar") "80s_retro" else "default"
                        val styleChoices = getWidgetStyleChoices(id)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "STYLE",
                            color = if (is80sWm) accentColor else Color(0xFF8A8A8A),
                            fontSize = 9.sp,
                            fontFamily = lomoFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = glowTextStyle(accentColor, 14f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            styleChoices.forEach { (stId, stLbl) ->
                                val isSel = currentStyle == stId
                                Text(
                                    text = stLbl,
                                    color = if (isSel) Color.Black else if (is80sWm) accentColor else Color.White,
                                    fontSize = 8.sp,
                                    fontFamily = lomoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    style = if (isSel) TextStyle.Default else glowTextStyle(accentColor, 12f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            if (isSel) secondaryRetroColor else if (is80sWm) accentColor.copy(alpha = 0.55f) else Color(0xFF282828),
                                            wmBtnShape
                                        )
                                        .background(if (isSel) secondaryRetroColor else if (is80sWm) Color(0xFF0A0A0A) else Color(0xFF191919), wmBtnShape)
                                        .clickable { onUpdateTargetStyle(id, stId) }
                                        .padding(vertical = 5.dp)
                                )
                            }
                        }

                        if (id == "clock" || id == "battery_bar") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "BATTERY BAR STYLE",
                                color = if (is80sWm) accentColor else Color(0xFF8A8A8A),
                                fontSize = 9.sp,
                                fontFamily = lomoFontFamily,
                                fontWeight = FontWeight.Bold,
                                style = glowTextStyle(accentColor, 14f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                BATTERY_BAR_STYLES.forEach { (stId, stLbl) ->
                                    val isSel = batteryBarStyle == stId
                                    Text(
                                        text = stLbl,
                                        color = if (isSel) Color.Black else if (is80sWm) accentColor else Color.White,
                                        fontSize = 8.sp,
                                        fontFamily = lomoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        style = if (isSel) TextStyle.Default else glowTextStyle(accentColor, 12f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                1.dp,
                                                if (isSel) secondaryRetroColor else if (is80sWm) accentColor.copy(alpha = 0.55f) else Color(0xFF282828),
                                                wmBtnShape
                                            )
                                            .background(if (isSel) secondaryRetroColor else if (is80sWm) Color(0xFF0A0A0A) else Color(0xFF191919), wmBtnShape)
                                            .clickable { onChangeBatteryBarStyle(stId) }
                                            .padding(vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        if (id == "clock" || id == "battery_bar" || id == "retro_flip_clock" || id == "analog_chrono") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CLOCK FORMAT",
                                    color = if (is80sWm) accentColor else Color(0xFF8A8A8A),
                                    fontSize = 9.sp,
                                    fontFamily = lomoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    style = glowTextStyle(accentColor, 14f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(false to "12H", true to "24H").forEach { (is24, lbl) ->
                                        val sel = use24Hour == is24
                                        val activeBg = if (is80sWm) secondaryRetroColor else Color.White
                                        Text(
                                            text = lbl,
                                            color = if (sel) Color.Black else if (is80sWm) accentColor else Color.White,
                                            fontSize = 9.sp,
                                            fontFamily = lomoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            style = if (sel) TextStyle.Default else glowTextStyle(accentColor, 12f),
                                            modifier = Modifier
                                                .border(
                                                    1.dp,
                                                    if (sel) activeBg else if (is80sWm) accentColor.copy(alpha = 0.55f) else Color(0xFF282828),
                                                    wmBtnShape
                                                )
                                                .background(if (sel) activeBg else if (is80sWm) Color(0xFF0A0A0A) else Color(0xFF191919), wmBtnShape)
                                                .clickable { if (use24Hour != is24) onToggle24h() }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (id == "globe") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "VISUAL THEME",
                                color = if (is80sWm) accentColor else Color(0xFF8A8A8A),
                                fontSize = 9.sp,
                                fontFamily = lomoFontFamily,
                                fontWeight = FontWeight.Bold,
                                style = glowTextStyle(accentColor, 14f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    "retro_holo" to "HOLOGRAM (GRN/BLU)",
                                    "wireframe" to "MONOCHROME VECTOR"
                                ).forEach { (tId, tLabel) ->
                                    val isSel = globeTheme == tId
                                    val activeBg = if (is80sWm) secondaryRetroColor else Color.White
                                    Text(
                                        text = tLabel,
                                        color = if (isSel) Color.Black else if (is80sWm) accentColor else Color.White,
                                        fontSize = 9.sp,
                                        fontFamily = lomoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        style = if (isSel) TextStyle.Default else glowTextStyle(accentColor, 12f),
                                        modifier = Modifier
                                            .border(
                                                1.dp,
                                                if (isSel) activeBg else if (is80sWm) accentColor.copy(alpha = 0.55f) else Color(0xFF282828),
                                                wmBtnShape
                                            )
                                            .background(if (isSel) activeBg else if (is80sWm) Color(0xFF0A0A0A) else Color(0xFF191919), wmBtnShape)
                                            .clickable { onChangeGlobeTheme(tId) }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        if (id == "pinned_apps") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SEQUENCE ORDER",
                                    color = if (is80sWm) accentColor else Color(0xFF8A8A8A),
                                    fontSize = 9.sp,
                                    fontFamily = lomoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    style = glowTextStyle(accentColor, 14f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(true to "CUSTOM (↑/↓)", false to "A–Z AUTO").forEach { (isCust, lbl) ->
                                        val sel = customPinnedOrderEnabled == isCust
                                        val activeBg = if (is80sWm) secondaryRetroColor else Color.White
                                        Text(
                                            text = lbl,
                                            color = if (sel) Color.Black else if (is80sWm) accentColor else Color.White,
                                            fontSize = 9.sp,
                                            fontFamily = lomoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            style = if (sel) TextStyle.Default else glowTextStyle(accentColor, 12f),
                                            modifier = Modifier
                                                .border(
                                                    1.dp,
                                                    if (sel) activeBg else if (is80sWm) accentColor.copy(alpha = 0.55f) else Color(0xFF282828),
                                                    wmBtnShape
                                                )
                                                .background(if (sel) activeBg else if (is80sWm) Color(0xFF0A0A0A) else Color(0xFF191919), wmBtnShape)
                                                .clickable { if (customPinnedOrderEnabled != isCust) onToggleCustomPinnedOrder() }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (id == "scratchpad") {
                            Spacer(modifier = Modifier.height(10.dp))
                            BasicTextField(
                                value = quickNote,
                                onValueChange = { onChangeQuickNote(it) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = if (is80sWm) secondaryRetroColor else Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = lomoFontFamily
                                ).merge(glowTextStyle(secondaryRetroColor, 14f)),
                                cursorBrush = SolidColor(secondaryRetroColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (is80sWm) accentColor else Color(0xFF262626), wmPillShape)
                                    .background(Color(0xFF111111), wmPillShape)
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// CLEAN & GLOWING LAUNCHER SETTINGS WITH COLLAPSIBLE ACCORDION MENUS (v5.1.0-cmf)
// - Every text element & border genuinely glows in Old School & Red/Cyan Holomap modes
// - Removed Battery Bar Style from Section 02 (Typography & Layout)
// - Removed Phase Thread live wallpaper
// - Added system-wide Retro-Futuristic (Glowing Red & Glowing Cyan) theme
// ============================================================================
@Composable
fun LauncherSettingsView(
    fontId: String,
    textAlignMode: String,
    accentHex: String,
    textSizeSp: Int,
    drawerTextSizeSp: Int,
    use24Hour: Boolean,
    hideAndroidBar: Boolean,
    enableLeftPage: Boolean = true,
    retro80sMode: Boolean = false,
    retro80sColorTheme: String = "green",
    onChangeRetro80sColorTheme: (String) -> Unit = {},
    onActivateHolomapTheme: () -> Unit = {},
    batteryBarStyle: String = "retro_blocks",
    pauseWhenAppOpen: Boolean = true,
    customPinnedOrderEnabled: Boolean = true,
    waveScrubMode: String,
    drawerWaveIntensity: Float,
    wallpaperPreset: String,
    wallpaperDimPercent: Int,
    hasCustomUserImage: Boolean,
    wpSpeed: Float,
    wpDensity: Float,
    wpOpacity: Float,
    wpScale: Float,
    settingsBgMode: String,
    settingsBgIntensity: Int,
    drawerBgMode: String = "opaque",
    drawerBgIntensity: Int = 65,
    lomoFontFamily: FontFamily,
    launcherFont: FontFamily,
    onChangeFont: (String) -> Unit,
    onChangeAlign: (String) -> Unit,
    onChangeAccent: (String) -> Unit,
    onChangeTextSize: (Int) -> Unit,
    onChangeDrawerTextSize: (Int) -> Unit,
    onToggle24h: () -> Unit,
    onToggleAndroidBar: () -> Unit,
    onToggleLeftPage: () -> Unit = {},
    onToggleRetro80sMode: () -> Unit = {},
    onChangeBatteryBarStyle: (String) -> Unit = {},
    onTogglePauseWhenAppOpen: () -> Unit = {},
    onToggleCustomPinnedOrder: () -> Unit = {},
    onChangeWaveMode: (String) -> Unit,
    onChangeDrawerWaveIntensity: (Float) -> Unit,
    onChangeWallpaperPreset: (String) -> Unit,
    onChangeWallpaperDim: (Int) -> Unit,
    onChangeWpSpeed: (Float) -> Unit,
    onChangeWpDensity: (Float) -> Unit,
    onChangeWpOpacity: (Float) -> Unit,
    onChangeWpScale: (Float) -> Unit,
    onResetWpTuning: () -> Unit,
    onChangeSettingsBgMode: (String) -> Unit,
    onChangeSettingsBgIntensity: (Int) -> Unit,
    onChangeDrawerBgMode: (String) -> Unit = {},
    onChangeDrawerBgIntensity: (Int) -> Unit = {},
    onOpenSystemWallpaperPicker: () -> Unit,
    onPickCustomWallpaperImage: () -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onBack: () -> Unit
) {
    var expandedSystem by remember { mutableStateOf(false) }
    var expandedTypography by remember { mutableStateOf(false) }
    var expandedFontMenu by remember { mutableStateOf(false) }
    var expandedWallpaper by remember { mutableStateOf(false) }
    var expandedMotion by remember { mutableStateOf(false) }

    var expandedLiveWallpapers by remember { mutableStateOf(false) }
    var expandedStaticWallpapers by remember { mutableStateOf(false) }
    var expandedUserWallpapers by remember { mutableStateOf(false) }

    val primaryCol = rememberRetroPrimaryColor(resolveLauncherAccentColor(accentHex))
    val secondaryCol = rememberRetroSecondaryColor(resolveLauncherAccentColor(accentHex))
    val innerCardShape = RoundedCornerShape(if (retro80sMode) 0.dp else 10.dp)
    val innerSubCardShape = RoundedCornerShape(if (retro80sMode) 0.dp else 8.dp)
    val innerBtnShape = RoundedCornerShape(if (retro80sMode) 0.dp else 6.dp)
    val innerCardBorderColor = if (retro80sMode) primaryCol else Color(0xFF222222)

    // Phase Thread ("live_lissajous") removed as requested!
    val liveWallpaperOptions = listOf(
        Triple("live_crt_scanlines", "OLD SCHOOL SCANLINES", "Faint Scanlines & Refresh Beam"),
        Triple("live_starfield_3d", "3D STARFIELD", "1,200-Star 3D Night Sky"),
        Triple("live_constellation", "ORBITAL NODES", "Full-Screen Drifting Stars"),
        Triple("live_embers", "DEEP EMBERS", "Full-Screen Rising Dust"),
        Triple("live_starlight", "STARLIGHT BREATH", "Calm Twinkling Field"),
        Triple("live_kepler", "KEPLER ORBITS", "Planetary Ellipses"),
        Triple("live_quantum", "MICRO LATTICE", "Breathing Dot Matrix"),
        Triple("live_glow_horizon", "AURORA GLOW FADE", "Breathing Ambient Fade"),
        Triple("live_halftone_pulse", "HALFTONE WAVE", "Glowing Halftone Pulse")
    )

    val staticWallpaperOptions = listOf(
        Triple("oled", "PURE AMOLED", "True #000000"),
        Triple("dots", "DOT MATRIX", "Full-Screen Dot Grid"),
        Triple("blueprint", "CMF GRID", "Full-Screen Schematic"),
        Triple("circuit", "GLYPH PCB", "Full-Screen Traces"),
        Triple("static_halftone", "RADIAL HALFTONE", "Full-Screen Dot Gradient"),
        Triple("static_halftone_vertical", "VERTICAL HALFTONE", "Bottom-to-Top Dot Fade"),
        Triple("static_halftone_diagonal", "DIAGONAL HALFTONE", "45° Halftone Matrix"),
        Triple("static_halftone_duotone", "DUOTONE HALFTONE", "Interleaved Accent Dots"),
        Triple("static_halftone_wave", "SINUSOID HALFTONE", "Harmonic Wave Dots"),
        Triple("static_glow_horizon", "HORIZON GLOW", "Rising Atmospheric Glow"),
        Triple("static_glow_nebula", "NEBULA GLOW", "Dual-Core Soft Aura"),
        Triple("static_glow_edge", "PERIMETER GLOW", "Edge-Lit Border Aura"),
        Triple("static_glow_pillar", "BEAM GLOW", "Vertical Light Column"),
        Triple("static_fade_monolith", "MONOLITH FADE", "Top-to-Bottom Ombré"),
        Triple("static_fade_twilight", "DIAGONAL FADE", "Silk Gradient & Rules"),
        Triple("static_fade_vignette", "SPOTLIGHT FADE", "Center Radial Fade"),
        Triple("static_fade_prism", "PRISM BANDS FADE", "Stepped Horizon Fade"),
        Triple("static_crosshair", "RETICLE HUD", "Full-Screen Crosshairs"),
        Triple("static_topo", "TOPO CONTOUR", "Full-Screen Isolines"),
        Triple("static_isometric", "ISO LATTICE", "60° Axonometric Grid"),
        Triple("static_star_chart", "CELESTIAL MAP", "Declination Star Map"),
        Triple("static_eclipse", "SOLAR ECLIPSE", "Coronal Rim & Rings"),
        Triple("static_vinyl", "MICRO GROOVES", "Full-Screen Fine Arcs"),
        Triple("static_bauhaus", "BAUHAUS GEO", "Full-Screen Line Balance"),
        Triple("static_horizon_lines", "SCANLINE FADE", "Full-Screen Scanlines"),
        Triple("static_glyph_arcs", "GLYPH ARCS", "Segmented Light Rings"),
        Triple("static_polar", "POLAR RADAR", "360° Azimuth Dial"),
        Triple("static_diagonal_hatch", "FINE HATCH", "45° Precision Lines"),
        Triple("static_golden_spiral", "FIBONACCI RATIO", "Golden Section Arc"),
        Triple("static_sine_interference", "MOIRÉ WAVES", "Harmonic Sine Field"),
        Triple("static_corner_frame", "VIEWFINDER", "Rule-of-Thirds & Corners"),
        Triple("static_hex_honeycomb", "HEX LATTICE", "Full-Screen Honeycomb"),
        Triple("static_constellation_fixed", "DEEP SKY MAP", "Fixed Star Lines"),
        Triple("static_vertical_flutes", "ARCHITECTURAL", "Vertical Pinstripes"),
        Triple("static_zen_enso", "ZEN CIRCLE", "Minimalist Open Ring"),
        Triple("static_micro_plus", "SWISS CROSSES", "Full-Screen + Matrix")
    )

    val normBg = (settingsBgIntensity.coerceIn(0, 100) / 100f)
    val rootBgColor = when (settingsBgMode) {
        "transparent" -> Color.Black.copy(alpha = (1f - normBg * 0.90f).coerceIn(0.08f, 0.96f))
        "blurry" -> Color(0xFF060606).copy(alpha = (0.78f - normBg * 0.52f).coerceIn(0.20f, 0.82f))
        else -> Color.Black
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(rootBgColor)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Clean Settings Header with Luminous Neon Glow
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (retro80sMode) "Nocturna Configuration" else "Nocturnaa Settings",
                    color = if (retro80sMode) primaryCol else Color.White,
                    fontSize = 16.sp,
                    fontFamily = launcherFont,
                    fontWeight = FontWeight.Bold,
                    style = glowTextStyle(primaryCol, 22f)
                )
                Text(
                    text = "DONE",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontFamily = launcherFont,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .retroNeonBorder(
                            1.dp,
                            if (retro80sMode) secondaryCol else Color.White,
                            innerBtnShape,
                            glowEnabled = retro80sMode
                        )
                        .background(if (retro80sMode) secondaryCol else Color.White, innerBtnShape)
                        .clickable { onBack() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // SECTION 01: System & Home Integration
        item {
            CollapsibleSettingsSection(
                indexTag = "01",
                title = "System & Home Integration",
                expanded = expandedSystem,
                accentColor = primaryCol,
                lomoFontFamily = launcherFont,
                onToggle = { expandedSystem = !expandedSystem }
            ) {
                SettingCard(
                    title = "Default Home Launcher",
                    actionLabel = "CHOOSE HOME",
                    lomoFontFamily = launcherFont,
                    onClick = onRequestDefaultHomeRole
                )
                SettingCard(
                    title = "Old School Mode",
                    actionLabel = if (retro80sMode) "ON" else "OFF",
                    lomoFontFamily = launcherFont,
                    onClick = onToggleRetro80sMode
                )
                SettingCard(
                    title = "Retro-Futuristic Mode (Red & Cyan)",
                    actionLabel = if (retro80sMode && retro80sColorTheme == "holomap") "ON" else "OFF",
                    lomoFontFamily = launcherFont,
                    onClick = onActivateHolomapTheme
                )
                // System-Wide Theme Palette Selector (Green Phosphor, Amber Orange, Red & Cyan Holomap)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .retroNeonBorder(1.dp, innerCardBorderColor, innerCardShape, glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                        .background(Color(0xFF050505), innerCardShape)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "OLD SCHOOL / RETRO-FUTURISTIC PALETTE",
                        color = if (retro80sMode) primaryCol else Color(0xFF8A8A8A),
                        fontSize = 9.sp,
                        fontFamily = launcherFont,
                        fontWeight = FontWeight.Bold,
                        style = glowTextStyle(primaryCol, 16f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "green" to "GREEN",
                            "amber" to "AMBER",
                            "holomap" to "RED & CYAN"
                        ).forEach { (thId, thLbl) ->
                            val isSel = retro80sColorTheme == thId
                            val activeBg = if (retro80sMode) secondaryCol else Color.White
                            Text(
                                text = thLbl,
                                color = if (isSel) Color.Black else if (retro80sMode) primaryCol else Color.White,
                                fontSize = 9.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = if (isSel) TextStyle.Default else glowTextStyle(primaryCol, 14f),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        1.dp,
                                        if (isSel) activeBg else if (retro80sMode) primaryCol.copy(alpha = 0.6f) else Color(0xFF282828),
                                        innerBtnShape
                                    )
                                    .background(
                                        if (isSel) activeBg else if (retro80sMode) Color(0xFF0A0A0A) else Color(0xFF141414),
                                        innerBtnShape
                                    )
                                    .clickable { onChangeRetro80sColorTheme(thId) }
                                    .padding(vertical = 7.dp)
                            )
                        }
                    }
                }
                SettingCard(
                    title = "Additional Left Home Page",
                    actionLabel = if (enableLeftPage) "ON" else "OFF",
                    lomoFontFamily = launcherFont,
                    onClick = onToggleLeftPage
                )
                SettingCard(
                    title = "Hide Android Status Bar",
                    actionLabel = if (hideAndroidBar) "ON" else "OFF",
                    lomoFontFamily = launcherFont,
                    onClick = onToggleAndroidBar
                )
                SettingCard(
                    title = "Pause Wallpapers & Widgets in Apps",
                    actionLabel = if (pauseWhenAppOpen) "ON" else "OFF",
                    lomoFontFamily = launcherFont,
                    onClick = onTogglePauseWhenAppOpen
                )

                SettingsBackdropControlCard(
                    cardTitle = "SETTINGS BACKGROUND",
                    settingsBgMode = settingsBgMode,
                    settingsBgIntensity = settingsBgIntensity,
                    accentColor = primaryCol,
                    lomoFontFamily = launcherFont,
                    onChangeMode = onChangeSettingsBgMode,
                    onChangeIntensity = onChangeSettingsBgIntensity
                )
            }
        }

        // SECTION 02: Typography & Layout (Battery Bar Style removed from here as requested!)
        item {
            val activeFontName = PrebuiltFont.fromId(fontId).displayName.substringBefore(" (")
            CollapsibleSettingsSection(
                indexTag = "02",
                title = "Typography & Layout",
                expanded = expandedTypography,
                accentColor = primaryCol,
                lomoFontFamily = launcherFont,
                onToggle = { expandedTypography = !expandedTypography }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .retroNeonBorder(1.dp, innerCardBorderColor, innerCardShape, glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                        .background(Color(0xFF070707), innerCardShape)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedFontMenu = !expandedFontMenu },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "LAUNCHER FONT",
                                color = if (retro80sMode) primaryCol else Color(0xFF8A8A8A),
                                fontSize = 9.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                style = glowTextStyle(primaryCol, 16f)
                            )
                            Text(
                                text = activeFontName,
                                color = if (retro80sMode) secondaryCol else Color.White,
                                fontSize = 13.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                style = glowTextStyle(secondaryCol, 16f)
                            )
                        }
                        Text(
                            text = if (retro80sMode) {
                                if (expandedFontMenu) "[-]" else "[+]"
                            } else {
                                if (expandedFontMenu) "▾" else "▸"
                            },
                            color = secondaryCol,
                            fontSize = if (retro80sMode) 11.sp else 14.sp,
                            fontFamily = launcherFont,
                            fontWeight = FontWeight.Bold,
                            style = glowTextStyle(secondaryCol, 16f)
                        )
                    }

                    AnimatedVisibility(
                        visible = expandedFontMenu,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PrebuiltFont.values().forEach { f ->
                                val selected = fontId == f.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            if (selected) secondaryCol else if (retro80sMode) primaryCol.copy(alpha = 0.45f) else Color(0xFF171717),
                                            innerSubCardShape
                                        )
                                        .background(
                                            if (selected) Color(0xFF121212) else Color.Transparent,
                                            innerSubCardShape
                                        )
                                        .clickable { onChangeFont(f.id) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val rowTextCol = when {
                                        selected && retro80sMode -> secondaryCol
                                        retro80sMode -> primaryCol
                                        else -> Color.White
                                    }
                                    Text(
                                        text = f.displayName.substringBefore(" ("),
                                        color = rowTextCol,
                                        fontSize = 12.sp,
                                        fontFamily = f.resolveFontFamily(lomoFontFamily),
                                        style = glowTextStyle(rowTextCol, 14f),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = if (retro80sMode) {
                                            if (selected) "[×]" else "[ ]"
                                        } else {
                                            if (selected) "●" else "○"
                                        },
                                        color = if (selected) secondaryCol else if (retro80sMode) primaryCol.copy(alpha = 0.6f) else Color(0xFF525252),
                                        fontSize = 10.sp,
                                        fontFamily = launcherFont,
                                        fontWeight = FontWeight.Bold,
                                        style = glowTextStyle(if (selected) secondaryCol else primaryCol, 14f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Home Screen App Font Size
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .retroNeonBorder(1.dp, innerCardBorderColor, innerCardShape, glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                        .background(Color(0xFF070707), innerCardShape)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HOME APP FONT SIZE",
                        color = if (retro80sMode) primaryCol else Color(0xFF8A8A8A),
                        fontSize = 9.sp,
                        fontFamily = launcherFont,
                        fontWeight = FontWeight.Bold,
                        style = glowTextStyle(primaryCol, 16f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(16, 18, 20, 22, 24).forEach { spVal ->
                            val sel = textSizeSp == spVal
                            val activeBg = if (retro80sMode) secondaryCol else Color.White
                            Text(
                                text = "${spVal}SP",
                                color = if (sel) Color.Black else if (retro80sMode) primaryCol else Color.White,
                                fontSize = 10.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = if (sel) TextStyle.Default else glowTextStyle(primaryCol, 14f),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        1.dp,
                                        if (sel) activeBg else if (retro80sMode) primaryCol.copy(alpha = 0.55f) else Color(0xFF242424),
                                        innerBtnShape
                                    )
                                    .background(if (sel) activeBg else if (retro80sMode) Color(0xFF0A0A0A) else Color(0xFF161616), innerBtnShape)
                                    .clickable { onChangeTextSize(spVal) }
                                    .padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                SettingCard(
                    title = "Clock Time Format",
                    actionLabel = if (use24Hour) "24-HOUR" else "12-HOUR",
                    lomoFontFamily = launcherFont,
                    onClick = onToggle24h
                )

                SettingCard(
                    title = "Home Text Alignment",
                    actionLabel = textAlignMode.uppercase(Locale.US),
                    lomoFontFamily = launcherFont,
                    onClick = {
                        val next = when (textAlignMode) {
                            "left" -> "center"
                            "center" -> "right"
                            else -> "left"
                        }
                        onChangeAlign(next)
                    }
                )

                SettingCard(
                    title = "Custom Pinned Apps Order",
                    actionLabel = if (customPinnedOrderEnabled) "ON" else "OFF",
                    lomoFontFamily = launcherFont,
                    onClick = onToggleCustomPinnedOrder
                )
            }
        }

        // SECTION 03: Wallpapers and Settings
        item {
            CollapsibleSettingsSection(
                indexTag = "03",
                title = "Wallpapers and Settings",
                expanded = expandedWallpaper,
                accentColor = primaryCol,
                lomoFontFamily = launcherFont,
                onToggle = { expandedWallpaper = !expandedWallpaper }
            ) {
                // 0. Universal Wallpaper Dimmer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .retroNeonBorder(1.dp, innerCardBorderColor, innerCardShape, glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                        .background(Color(0xFF070707), innerCardShape)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinimalistSliderRow(
                        label = "WALLPAPER DIMMER",
                        valueText = "${wallpaperDimPercent}%",
                        fraction = (wallpaperDimPercent / 90f).coerceIn(0f, 1f),
                        accentColor = primaryCol,
                        lomoFontFamily = launcherFont,
                        onFractionChange = { frac ->
                            val nextDim = (frac * 90f).roundToInt().coerceIn(0, 90)
                            if (nextDim != wallpaperDimPercent) {
                                onChangeWallpaperDim(nextDim)
                            }
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0, 20, 40, 60, 75, 90).forEach { dimVal ->
                            val sel = wallpaperDimPercent == dimVal
                            Text(
                                text = "${dimVal}%",
                                color = if (sel) Color.Black else if (retro80sMode) primaryCol else Color(0xFFD4D4D4),
                                fontSize = 9.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = if (sel) TextStyle.Default else glowTextStyle(primaryCol, 14f),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        1.dp,
                                        if (sel) secondaryCol else if (retro80sMode) primaryCol.copy(alpha = 0.55f) else Color(0xFF242424),
                                        innerBtnShape
                                    )
                                    .background(if (sel) secondaryCol else if (retro80sMode) Color(0xFF0A0A0A) else Color(0xFF151515), innerBtnShape)
                                    .clickable { onChangeWallpaperDim(dimVal) }
                                    .padding(vertical = 5.dp)
                            )
                        }
                    }
                }

                // 1. Live Wallpapers (Phase Thread removed!)
                CollapsibleSubSection(
                    title = "Live Wallpapers",
                    expanded = expandedLiveWallpapers,
                    accentColor = primaryCol,
                    lomoFontFamily = launcherFont,
                    onToggle = { expandedLiveWallpapers = !expandedLiveWallpapers }
                ) {
                    liveWallpaperOptions.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (id, title, _) ->
                                val active = wallpaperPreset == id
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            if (active) secondaryCol else if (retro80sMode) primaryCol.copy(alpha = 0.5f) else Color(0xFF242424),
                                            innerSubCardShape
                                        )
                                        .background(
                                            if (active) Color(0xFF141414) else Color(0xFF0A0A0A),
                                            innerSubCardShape
                                        )
                                        .clickable { onChangeWallpaperPreset(id) }
                                        .padding(horizontal = 10.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val wCol = when {
                                            active && retro80sMode -> secondaryCol
                                            retro80sMode -> primaryCol
                                            active -> Color.White
                                            else -> Color(0xFFD4D4D4)
                                        }
                                        Text(
                                            text = title,
                                            color = wCol,
                                            fontSize = 10.sp,
                                            fontFamily = launcherFont,
                                            fontWeight = FontWeight.Bold,
                                            style = glowTextStyle(wCol, 14f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    if (active) secondaryCol else Color(0xFF383838),
                                                    if (retro80sMode) RoundedCornerShape(0.dp) else CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, innerCardBorderColor, innerSubCardShape)
                            .background(Color(0xFF090909), innerSubCardShape)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE WALLPAPER TUNING",
                                color = primaryCol,
                                fontSize = 9.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                style = glowTextStyle(primaryCol, 16f)
                            )
                            Text(
                                text = "RESET",
                                color = if (retro80sMode) secondaryCol else Color(0xFFA3A3A3),
                                fontSize = 8.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                style = glowTextStyle(secondaryCol, 14f),
                                modifier = Modifier
                                    .border(1.dp, if (retro80sMode) secondaryCol else Color(0xFF2E2E2E), innerBtnShape)
                                    .background(Color(0xFF121212), innerBtnShape)
                                    .clickable { onResetWpTuning() }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        WallpaperTuningRow(
                            label = "SPEED",
                            currentValue = wpSpeed,
                            options = listOf(0.25f to "0.25x", 0.5f to "0.5x", 1.0f to "1.0x", 1.5f to "1.5x", 2.2f to "2.2x"),
                            accentColor = primaryCol,
                            lomoFontFamily = launcherFont,
                            onSelect = onChangeWpSpeed
                        )

                        WallpaperTuningRow(
                            label = "DENSITY",
                            currentValue = wpDensity,
                            options = listOf(0.5f to "0.5x", 0.75f to "0.75x", 1.0f to "1.0x", 1.35f to "1.35x", 1.75f to "1.75x"),
                            accentColor = primaryCol,
                            lomoFontFamily = launcherFont,
                            onSelect = onChangeWpDensity
                        )

                        WallpaperTuningRow(
                            label = "OPACITY",
                            currentValue = wpOpacity,
                            options = listOf(0.4f to "40%", 0.7f to "70%", 1.0f to "100%", 1.35f to "135%", 1.75f to "175%"),
                            accentColor = primaryCol,
                            lomoFontFamily = launcherFont,
                            onSelect = onChangeWpOpacity
                        )

                        WallpaperTuningRow(
                            label = "SCALE",
                            currentValue = wpScale,
                            options = listOf(0.65f to "0.65x", 0.85f to "0.85x", 1.0f to "1.0x", 1.25f to "1.25x", 1.5f to "1.5x"),
                            accentColor = primaryCol,
                            lomoFontFamily = launcherFont,
                            onSelect = onChangeWpScale
                        )
                    }
                }

                // 2. Static Wallpapers
                CollapsibleSubSection(
                    title = "Static Wallpapers",
                    expanded = expandedStaticWallpapers,
                    accentColor = primaryCol,
                    lomoFontFamily = launcherFont,
                    onToggle = { expandedStaticWallpapers = !expandedStaticWallpapers }
                ) {
                    staticWallpaperOptions.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (id, title, _) ->
                                val active = wallpaperPreset == id
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            if (active) (if (retro80sMode) secondaryCol else Color.White) else if (retro80sMode) primaryCol.copy(alpha = 0.5f) else Color(0xFF242424),
                                            innerSubCardShape
                                        )
                                        .background(
                                            if (active) Color(0xFF141414) else Color(0xFF0A0A0A),
                                            innerSubCardShape
                                        )
                                        .clickable { onChangeWallpaperPreset(id) }
                                        .padding(horizontal = 10.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val wCol = when {
                                            active && retro80sMode -> secondaryCol
                                            retro80sMode -> primaryCol
                                            active -> Color.White
                                            else -> Color(0xFFD4D4D4)
                                        }
                                        Text(
                                            text = title,
                                            color = wCol,
                                            fontSize = 10.sp,
                                            fontFamily = launcherFont,
                                            fontWeight = FontWeight.Bold,
                                            style = glowTextStyle(wCol, 14f)
                                        )
                                        if (active) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(
                                                        secondaryCol,
                                                        if (retro80sMode) RoundedCornerShape(0.dp) else CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. User Wallpapers
                CollapsibleSubSection(
                    title = "User Wallpapers",
                    expanded = expandedUserWallpapers,
                    accentColor = primaryCol,
                    lomoFontFamily = launcherFont,
                    onToggle = { expandedUserWallpapers = !expandedUserWallpapers }
                ) {
                    SettingCard(
                        title = "Import Photo from Gallery",
                        actionLabel = if (wallpaperPreset == "user_image") "CHANGE" else "PICK",
                        lomoFontFamily = launcherFont,
                        onClick = onPickCustomWallpaperImage
                    )

                    if (hasCustomUserImage && wallpaperPreset != "user_image") {
                        SettingCard(
                            title = "Use Saved Custom Photo",
                            actionLabel = "APPLY",
                            lomoFontFamily = launcherFont,
                            onClick = { onChangeWallpaperPreset("user_image") }
                        )
                    }

                    SettingCard(
                        title = "System Wallpaper Picker",
                        actionLabel = "OPEN",
                        lomoFontFamily = launcherFont,
                        onClick = onOpenSystemWallpaperPicker
                    )

                    SettingCard(
                        title = "Use System Wallpaper",
                        actionLabel = if (wallpaperPreset == "system_wallpaper") "ON" else "ENABLE",
                        lomoFontFamily = launcherFont,
                        onClick = { onChangeWallpaperPreset("system_wallpaper") }
                    )
                }

                // 4. Accent Color Picker
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .retroNeonBorder(1.dp, innerCardBorderColor, innerCardShape, glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                        .background(Color(0xFF070707), innerCardShape)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACCENT COLOR",
                            color = if (retro80sMode) primaryCol else Color(0xFF8A8A8A),
                            fontSize = 9.sp,
                            fontFamily = launcherFont,
                            fontWeight = FontWeight.Bold,
                            style = glowTextStyle(primaryCol, 16f)
                        )
                        Text(
                            text = accentHex,
                            color = secondaryCol,
                            fontSize = 9.sp,
                            fontFamily = launcherFont,
                            fontWeight = FontWeight.Bold,
                            style = glowTextStyle(secondaryCol, 16f)
                        )
                    }

                    LAUNCHER_ACCENT_PALETTE.chunked(5).forEach { paletteRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            paletteRow.forEach { (hex, label) ->
                                val isSel = accentHex.equals(hex, ignoreCase = true)
                                val swatchCol = resolveLauncherAccentColor(hex)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (isSel) 1.5.dp else 1.dp,
                                            color = if (isSel) (if (retro80sMode) secondaryCol else Color.White) else if (retro80sMode) primaryCol.copy(alpha = 0.5f) else Color(0xFF242424),
                                            shape = innerSubCardShape
                                        )
                                        .background(
                                            if (isSel) Color(0xFF181818) else Color(0xFF0D0D0D),
                                            innerSubCardShape
                                        )
                                        .clickable { onChangeAccent(hex) }
                                        .padding(vertical = 7.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(swatchCol, if (retro80sMode) RoundedCornerShape(0.dp) else CircleShape)
                                    )
                                    Text(
                                        text = label,
                                        color = if (isSel) (if (retro80sMode) secondaryCol else Color.White) else if (retro80sMode) primaryCol else Color(0xFF8A8A8A),
                                        fontSize = 8.sp,
                                        fontFamily = launcherFont,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        style = glowTextStyle(if (isSel) secondaryCol else primaryCol, 12f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 04: App Drawer Settings
        item {
            val wavePct = (drawerWaveIntensity * 100f).roundToInt()
            CollapsibleSettingsSection(
                indexTag = "04",
                title = "App Drawer Settings",
                expanded = expandedMotion,
                accentColor = primaryCol,
                lomoFontFamily = launcherFont,
                onToggle = { expandedMotion = !expandedMotion }
            ) {
                // 1. App Drawer App Name Scale
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .retroNeonBorder(1.dp, innerCardBorderColor, innerCardShape, glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                        .background(Color(0xFF070707), innerCardShape)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinimalistSliderRow(
                        label = "APP DRAWER TEXT SIZE",
                        valueText = "${drawerTextSizeSp} SP",
                        fraction = ((drawerTextSizeSp - 12f) / 14f).coerceIn(0f, 1f),
                        accentColor = primaryCol,
                        lomoFontFamily = launcherFont,
                        onFractionChange = { frac ->
                            val nextSp = (12f + frac * 14f).roundToInt().coerceIn(12, 26)
                            if (nextSp != drawerTextSizeSp) {
                                onChangeDrawerTextSize(nextSp)
                            }
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(12, 14, 16, 18, 20, 22, 24).forEach { spVal ->
                            val sel = drawerTextSizeSp == spVal
                            Text(
                                text = "${spVal}",
                                color = if (sel) Color.Black else if (retro80sMode) primaryCol else Color.White,
                                fontSize = 9.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = if (sel) TextStyle.Default else glowTextStyle(primaryCol, 14f),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        1.dp,
                                        if (sel) secondaryCol else if (retro80sMode) primaryCol.copy(alpha = 0.55f) else Color(0xFF242424),
                                        innerBtnShape
                                    )
                                    .background(if (sel) secondaryCol else if (retro80sMode) Color(0xFF0A0A0A) else Color(0xFF161616), innerBtnShape)
                                    .clickable { onChangeDrawerTextSize(spVal) }
                                    .padding(vertical = 5.dp)
                            )
                        }
                    }
                }

                // 2. Alphabet Scroller Wave Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .retroNeonBorder(1.dp, innerCardBorderColor, innerCardShape, glowEnabled = retro80sMode, glowRadiusDp = 4.dp)
                        .background(Color(0xFF070707), innerCardShape)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinimalistSliderRow(
                        label = "ALPHABET SCROLLER WAVE",
                        valueText = "${wavePct}%",
                        fraction = (drawerWaveIntensity / 2.0f).coerceIn(0f, 1f),
                        accentColor = primaryCol,
                        lomoFontFamily = launcherFont,
                        onFractionChange = { frac ->
                            val nextVal = ((frac * 200f).roundToInt() / 100f).coerceIn(0f, 2.0f)
                            onChangeDrawerWaveIntensity(nextVal)
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0f to "OFF", 0.5f to "50%", 1.0f to "100%", 1.5f to "150%", 2.0f to "200%").forEach { (wVal, wLbl) ->
                            val sel = abs(drawerWaveIntensity - wVal) < 0.08f
                            Text(
                                text = wLbl,
                                color = if (sel) Color.Black else if (retro80sMode) primaryCol else Color.White,
                                fontSize = 9.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = if (sel) TextStyle.Default else glowTextStyle(primaryCol, 14f),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        1.dp,
                                        if (sel) secondaryCol else if (retro80sMode) primaryCol.copy(alpha = 0.55f) else Color(0xFF242424),
                                        innerBtnShape
                                    )
                                    .background(if (sel) secondaryCol else if (retro80sMode) Color(0xFF0A0A0A) else Color(0xFF161616), innerBtnShape)
                                    .clickable { onChangeDrawerWaveIntensity(wVal) }
                                    .padding(vertical = 5.dp)
                            )
                        }
                    }
                }

                // 3. App Drawer Background Style
                SettingsBackdropControlCard(
                    cardTitle = "APP DRAWER BACKGROUND",
                    settingsBgMode = drawerBgMode,
                    settingsBgIntensity = drawerBgIntensity,
                    accentColor = primaryCol,
                    lomoFontFamily = launcherFont,
                    onChangeMode = onChangeDrawerBgMode,
                    onChangeIntensity = onChangeDrawerBgIntensity
                )
            }
        }
    }
}

@Composable
fun CollapsibleSubSection(
    title: String,
    subtitle: String = "",
    badge: String = "",
    expanded: Boolean,
    accentColor: Color,
    lomoFontFamily: FontFamily,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val is80s = LocalRetro80sMode.current
    val secondaryCol = rememberRetroSecondaryColor(accentColor)
    val subShape = RoundedCornerShape(if (is80s) 0.dp else 10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .retroNeonBorder(
                width = if (is80s) 1.3.dp else 1.dp,
                color = if (is80s) accentColor else (if (expanded) accentColor.copy(alpha = 0.55f) else Color(0xFF222222)),
                shape = subShape,
                glowEnabled = is80s,
                glowRadiusDp = 5.dp
            )
            .background(Color(0xFF070707), subShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (is80s) title.uppercase(Locale.US) else title,
                color = if (is80s) accentColor else Color.White,
                fontSize = 13.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(accentColor, 20f),
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Text(
                text = if (is80s) {
                    if (expanded) "[-]" else "[+]"
                } else {
                    if (expanded) "▾" else "▸"
                },
                color = if (is80s) secondaryCol else if (expanded) accentColor else Color(0xFF737373),
                fontSize = if (is80s) 11.sp else 13.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(secondaryCol, 20f)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (is80s) accentColor.copy(alpha = 0.45f) else Color(0xFF1A1A1A))
                )
                content()
            }
        }
    }
}

@Composable
fun MinimalistSliderRow(
    label: String,
    valueText: String,
    fraction: Float,
    accentColor: Color,
    lomoFontFamily: FontFamily = FontFamily.Monospace,
    onFractionChange: (Float) -> Unit
) {
    val is80s = LocalRetro80sMode.current
    val secondaryCol = rememberRetroSecondaryColor(accentColor)
    val currentOnChange by rememberUpdatedState(onFractionChange)
    val clamped = fraction.coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (is80s) accentColor else Color(0xFF9E9E9E),
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(accentColor, 16f)
            )
            Text(
                text = if (is80s) "[$valueText]" else valueText,
                color = secondaryCol,
                fontSize = 10.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(secondaryCol, 16f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        currentOnChange((offset.x / w).coerceIn(0f, 1f))
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        currentOnChange((change.position.x / w).coerceIn(0f, 1f))
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackH = if (is80s) 7.dp.toPx() else 4.dp.toPx()
                val cy = size.height * 0.5f
                val thumbX = (clamped * size.width).coerceIn(6.dp.toPx(), size.width - 6.dp.toPx())
                val cornerR = if (is80s) CornerRadius.Zero else CornerRadius(trackH * 0.5f, trackH * 0.5f)

                drawRoundRect(
                    color = if (is80s) Color(0xFF0A0A0A) else Color(0xFF222222),
                    topLeft = Offset(0f, cy - trackH * 0.5f),
                    size = Size(size.width, trackH),
                    cornerRadius = cornerR
                )
                if (is80s) {
                    drawRect(
                        color = accentColor.copy(alpha = 0.6f),
                        topLeft = Offset(0f, cy - trackH * 0.5f),
                        size = Size(size.width, trackH),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                drawRoundRect(
                    color = secondaryCol,
                    topLeft = Offset(0f, cy - trackH * 0.5f),
                    size = Size(thumbX, trackH),
                    cornerRadius = cornerR
                )
                val thumbSize = 12.dp.toPx()
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(thumbX - thumbSize * 0.5f, cy - thumbSize * 0.5f),
                    size = Size(thumbSize, thumbSize)
                )
                drawRect(
                    color = secondaryCol,
                    topLeft = Offset(thumbX - thumbSize * 0.5f, cy - thumbSize * 0.5f),
                    size = Size(thumbSize, thumbSize),
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun SettingsBackdropControlCard(
    cardTitle: String = "SETTINGS BACKGROUND",
    settingsBgMode: String,
    settingsBgIntensity: Int,
    accentColor: Color,
    lomoFontFamily: FontFamily = FontFamily.Monospace,
    onChangeMode: (String) -> Unit,
    onChangeIntensity: (Int) -> Unit
) {
    val is80s = LocalRetro80sMode.current
    val secondaryCol = rememberRetroSecondaryColor(accentColor)
    val cardShape = RoundedCornerShape(if (is80s) 0.dp else 10.dp)
    val btnShape = RoundedCornerShape(if (is80s) 0.dp else 6.dp)
    val pctShape = RoundedCornerShape(if (is80s) 0.dp else 5.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .retroNeonBorder(1.dp, if (is80s) accentColor else Color(0xFF282828), cardShape, glowEnabled = is80s, glowRadiusDp = 4.dp)
            .background(Color(0xFF070707).copy(alpha = 0.82f), cardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cardTitle,
                color = if (is80s) accentColor else Color(0xFF9E9E9E),
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(accentColor, 16f)
            )
            Text(
                text = settingsBgMode.uppercase(Locale.US),
                color = secondaryCol,
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(secondaryCol, 16f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "opaque" to "OPAQUE",
                "transparent" to "TRANSPARENT",
                "blurry" to "BLURRY"
            ).forEach { (modeId, modeLabel) ->
                val selected = settingsBgMode == modeId
                Text(
                    text = modeLabel,
                    color = if (selected) Color.Black else if (is80s) accentColor else Color.White,
                    fontSize = 9.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = if (selected) TextStyle.Default else glowTextStyle(accentColor, 14f),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (selected) secondaryCol else if (is80s) accentColor.copy(alpha = 0.55f) else Color(0xFF262626),
                            btnShape
                        )
                        .background(
                            if (selected) secondaryCol else if (is80s) Color(0xFF0A0A0A) else Color(0xFF141414),
                            btnShape
                        )
                        .clickable { onChangeMode(modeId) }
                        .padding(vertical = 6.dp)
                )
            }
        }

        val sliderLabel = when (settingsBgMode) {
            "transparent" -> "TRANSPARENCY"
            "blurry" -> "BLUR INTENSITY"
            else -> "INTENSITY"
        }

        MinimalistSliderRow(
            label = sliderLabel,
            valueText = "${settingsBgIntensity}%",
            fraction = (settingsBgIntensity / 100f).coerceIn(0f, 1f),
            accentColor = accentColor,
            lomoFontFamily = lomoFontFamily,
            onFractionChange = { frac ->
                val nextVal = (frac * 100f).roundToInt().coerceIn(0, 100)
                if (nextVal != settingsBgIntensity) {
                    onChangeIntensity(nextVal)
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(15, 35, 50, 70, 85, 100).forEach { pctVal ->
                val sel = settingsBgIntensity == pctVal
                Text(
                    text = "${pctVal}%",
                    color = if (sel) Color.Black else if (is80s) accentColor else Color(0xFFD4D4D4),
                    fontSize = 8.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = if (sel) TextStyle.Default else glowTextStyle(accentColor, 12f),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (sel) secondaryCol else if (is80s) accentColor.copy(alpha = 0.55f) else Color(0xFF242424),
                            pctShape
                        )
                        .background(if (sel) secondaryCol else if (is80s) Color(0xFF0A0A0A) else Color(0xFF151515), pctShape)
                        .clickable { onChangeIntensity(pctVal) }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CollapsibleSettingsSection(
    indexTag: String,
    title: String,
    subtitle: String = "",
    badge: String = "",
    expanded: Boolean,
    accentColor: Color,
    lomoFontFamily: FontFamily,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val is80s = LocalRetro80sMode.current
    val secondaryCol = rememberRetroSecondaryColor(accentColor)
    val secShape = RoundedCornerShape(if (is80s) 0.dp else 14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .retroNeonBorder(
                width = if (is80s) 1.5.dp else 1.dp,
                color = if (is80s) accentColor else (if (expanded) Color(0xFF383838) else Color(0xFF222222)),
                shape = secShape,
                glowEnabled = is80s,
                glowRadiusDp = 7.dp
            )
            .background(if (is80s) Color(0xFF030503) else Color(0xFF0A0A0A).copy(alpha = 0.72f), secShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (is80s) "[$indexTag]" else indexTag,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = glowTextStyle(accentColor, 20f),
                    modifier = if (is80s) Modifier else Modifier
                        .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Text(
                    text = if (is80s) title.uppercase(Locale.US) else title,
                    color = if (is80s) accentColor else Color.White,
                    fontSize = if (is80s) 13.sp else 14.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = glowTextStyle(accentColor, 20f),
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = if (is80s) {
                    if (expanded) "[-]" else "[+]"
                } else {
                    if (expanded) "▾" else "▸"
                },
                color = if (is80s) secondaryCol else if (expanded) accentColor else Color(0xFF737373),
                fontSize = if (is80s) 11.sp else 14.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(secondaryCol, 20f)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (is80s) accentColor.copy(alpha = 0.55f) else Color(0xFF1C1C1C))
                )
                content()
            }
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    subtitle: String = "",
    actionLabel: String,
    lomoFontFamily: FontFamily,
    onClick: () -> Unit
) {
    val is80s = LocalRetro80sMode.current
    val primaryCol = rememberRetroPrimaryColor()
    val secondaryCol = rememberRetroSecondaryColor()
    val cardShape = RoundedCornerShape(if (is80s) 0.dp else 10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .retroNeonBorder(
                1.dp,
                if (is80s) primaryCol else Color(0xFF242424),
                cardShape,
                glowEnabled = is80s,
                glowRadiusDp = 4.dp
            )
            .background(Color(0xFF050505), cardShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (is80s) primaryCol else Color.White,
            fontSize = 12.sp,
            fontFamily = lomoFontFamily,
            fontWeight = if (is80s) FontWeight.Bold else FontWeight.Normal,
            style = glowTextStyle(primaryCol, 18f),
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
        Text(
            text = actionLabel,
            color = Color.Black,
            fontSize = 10.sp,
            fontFamily = lomoFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .retroNeonBorder(
                    1.dp,
                    if (is80s) secondaryCol else Color.White,
                    RoundedCornerShape(if (is80s) 0.dp else 6.dp),
                    glowEnabled = is80s,
                    glowRadiusDp = 4.dp
                )
                .background(if (is80s) secondaryCol else Color.White, RoundedCornerShape(if (is80s) 0.dp else 6.dp))
                .padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun WallpaperTuningRow(
    label: String,
    currentValue: Float,
    options: List<Pair<Float, String>>,
    accentColor: Color,
    lomoFontFamily: FontFamily = FontFamily.Monospace,
    onSelect: (Float) -> Unit
) {
    val is80s = LocalRetro80sMode.current
    val secondaryCol = rememberRetroSecondaryColor(accentColor)
    val btnShape = RoundedCornerShape(if (is80s) 0.dp else 5.dp)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (is80s) accentColor else Color(0xFF9E9E9E),
                fontSize = 8.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(accentColor, 14f)
            )
            val activeLabel = options.minByOrNull { abs(it.first - currentValue) }?.second ?: "${currentValue}x"
            Text(
                text = activeLabel,
                color = secondaryCol,
                fontSize = 8.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(secondaryCol, 14f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            options.forEach { (value, textLabel) ->
                val selected = abs(currentValue - value) < 0.04f
                Text(
                    text = textLabel,
                    color = if (selected) Color.Black else if (is80s) accentColor else Color(0xFFD4D4D4),
                    fontSize = 8.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    style = if (selected) TextStyle.Default else glowTextStyle(accentColor, 12f),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (selected) secondaryCol else if (is80s) accentColor.copy(alpha = 0.55f) else Color(0xFF242424),
                            btnShape
                        )
                        .background(
                            if (selected) secondaryCol else if (is80s) Color(0xFF0A0A0A) else Color(0xFF121212),
                            btnShape
                        )
                        .clickable { onSelect(value) }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}


class DeepSpaceStarfieldSimulation {
    companion object {
        const val MIN_STAR_COUNT = 1000
        const val DEFAULT_STAR_COUNT = 1200
        const val MAX_STAR_COUNT = 1800
    }

    private val starX = FloatArray(MAX_STAR_COUNT)
    private val starY = FloatArray(MAX_STAR_COUNT)
    private val starZ = FloatArray(MAX_STAR_COUNT)
    private val starBaseRadiusDp = FloatArray(MAX_STAR_COUNT)
    private val starBaseAlpha = FloatArray(MAX_STAR_COUNT)
    private val starTwinklePhase = FloatArray(MAX_STAR_COUNT)
    private val starTwinkleFreq = FloatArray(MAX_STAR_COUNT)
    private val starRgbNoAlpha = IntArray(MAX_STAR_COUNT)

    private val starPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
    }

    private var rngState: Int = 0x6D2B79F5
    private var twinkleClockSec: Float = 0f

    init {
        for (i in 0 until MAX_STAR_COUNT) {
            initStar(i, randomDepth = true)
        }
    }

    private fun nextFloat01(): Float {
        var x = rngState
        x = x xor (x shl 13)
        x = x xor (x ushr 17)
        x = x xor (x shl 5)
        rngState = x
        return ((x ushr 8) and 0x00FFFFFF).toFloat() / 16777216.0f
    }

    private fun initStar(index: Int, randomDepth: Boolean) {
        starX[index] = (nextFloat01() * 2f - 1f)
        starY[index] = (nextFloat01() * 2f - 1f)
        starZ[index] = if (randomDepth) 0.04f + nextFloat01() * 0.96f else 0.90f + nextFloat01() * 0.10f
        val sizeRoll = nextFloat01()
        starBaseRadiusDp[index] = when {
            sizeRoll < 0.82f -> 0.36f + nextFloat01() * 0.36f
            sizeRoll < 0.96f -> 0.72f + nextFloat01() * 0.36f
            else -> 1.08f + nextFloat01() * 0.34f
        }
        starBaseAlpha[index] = 0.22f + nextFloat01() * 0.76f
        starTwinklePhase[index] = nextFloat01() * 6.2831855f
        starTwinkleFreq[index] = 0.65f + nextFloat01() * 2.15f
        val colorRoll = nextFloat01()
        starRgbNoAlpha[index] = when {
            colorRoll < 0.78f -> 0x00FFFFFF
            colorRoll < 0.91f -> 0x00D8ECFF
            else -> 0x00FFE6C8
        }
    }

    fun step(dtSec: Float, speedMult: Float) {
        val dt = dtSec.coerceIn(0f, 0.05f)
        twinkleClockSec += dt * (0.6f + 0.4f * speedMult)
        val zStep = dt * 0.032f * speedMult.coerceIn(0.2f, 2.5f)
        for (i in 0 until MAX_STAR_COUNT) {
            var z = starZ[i] - zStep
            if (z <= 0.035f) {
                initStar(i, randomDepth = false)
            } else {
                starZ[i] = z
            }
        }
    }

    fun draw(
        nativeCanvas: android.graphics.Canvas,
        widthPx: Float,
        heightPx: Float,
        densityPxPerDp: Float,
        densityMult: Float,
        opacityMult: Float,
        scaleMult: Float
    ) {
        val count = (DEFAULT_STAR_COUNT * densityMult).toInt().coerceIn(MIN_STAR_COUNT, MAX_STAR_COUNT)
        val cx = widthPx * 0.5f
        val cy = heightPx * 0.5f
        val focal = min(widthPx, heightPx) * 0.58f * scaleMult.coerceIn(0.5f, 1.8f)

        for (i in 0 until count) {
            val z = starZ[i].coerceAtLeast(0.035f)
            val invZ = 1f / z
            val sx = cx + starX[i] * focal * invZ
            val sy = cy + starY[i] * focal * invZ
            if (sx < 0f || sx > widthPx || sy < 0f || sy > heightPx) continue

            val depthBoost = ((1.05f - z) * 1.15f).coerceIn(0.25f, 1.35f)
            val twinkle = 0.72f + 0.28f * sin(twinkleClockSec * starTwinkleFreq[i] + starTwinklePhase[i])
            val alphaFloat = (starBaseAlpha[i] * depthBoost * twinkle * opacityMult).coerceIn(0.05f, 1f)
            val alphaInt = (alphaFloat * 255f).toInt().coerceIn(12, 255)
            val rPx = (starBaseRadiusDp[i] * densityPxPerDp * (0.65f + 0.55f * depthBoost)).coerceIn(0.55f, 3.8f)

            starPaint.color = (alphaInt shl 24) or starRgbNoAlpha[i]
            nativeCanvas.drawCircle(sx, sy, rPx, starPaint)
        }
    }
}

@Composable
fun WallpaperCanvasView(
    preset: String,
    accentColor: Color,
    dimPercent: Int = 0,
    customImageBitmap: ImageBitmap? = null,
    speedMult: Float = 1.0f,
    densityMult: Float = 1.0f,
    opacityMult: Float = 1.0f,
    scaleMult: Float = 1.0f,
    animationsActive: Boolean = true
) {
    if (preset == "system_wallpaper") {
        if (dimPercent > 0) {
            val dimAlpha = (dimPercent.coerceIn(0, 90) / 100f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimAlpha))
            )
        }
        return
    }

    if (preset == "user_image" && customImageBitmap != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                bitmap = customImageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (dimPercent > 0) {
                val dimAlpha = (dimPercent.coerceIn(0, 90) / 100f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = dimAlpha))
                )
            }
        }
        return
    }

    val isLive = preset.startsWith("live_")
    val starfieldSim = remember { DeepSpaceStarfieldSimulation() }
    var animTimeSec by remember { mutableStateOf(0f) }

    LaunchedEffect(isLive, preset, animationsActive, speedMult) {
        if (!isLive || !animationsActive) return@LaunchedEffect
        var lastNs = System.nanoTime()
        while (isActive && animationsActive) {
            withFrameNanos { nowNs ->
                val dt = ((nowNs - lastNs) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNs = nowNs
                animTimeSec += dt * speedMult
                if (preset == "live_starfield_3d") {
                    starfieldSim.step(dt, speedMult)
                }
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val minDim = min(w, h)
        val fullDiag = hypot(w, h)

        drawRect(color = Color.Black, size = Size(w, h))

        when (preset) {
            "oled" -> {
                // Pure #000000 AMOLED Black
            }

            "live_crt_scanlines" -> {
                val step = (3.6.dp.toPx() / densityMult.coerceIn(0.5f, 1.75f)).coerceAtLeast(2f)
                val lineAlpha = (0.14f * opacityMult).coerceIn(0.04f, 0.38f)
                var y = 0f
                while (y < h) {
                    drawLine(
                        color = accentColor.copy(alpha = lineAlpha),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                    y += step
                }
                val beamY = ((animTimeSec * 0.14f) % 1f) * h
                val beamH = (h * 0.16f * scaleMult).coerceAtLeast(36f)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            accentColor.copy(alpha = (0.16f * opacityMult).coerceIn(0.04f, 0.36f)),
                            Color.Transparent
                        ),
                        startY = beamY - beamH * 0.5f,
                        endY = beamY + beamH * 0.5f
                    ),
                    topLeft = Offset(0f, (beamY - beamH * 0.5f).coerceAtLeast(0f)),
                    size = Size(w, beamH)
                )
            }

            "live_starfield_3d" -> {
                val tRead = animTimeSec
                if (tRead >= 0f) {
                    drawIntoCanvas { canvas ->
                        starfieldSim.draw(
                            nativeCanvas = canvas.nativeCanvas,
                            widthPx = w,
                            heightPx = h,
                            densityPxPerDp = 1.dp.toPx(),
                            densityMult = densityMult,
                            opacityMult = opacityMult,
                            scaleMult = scaleMult
                        )
                    }
                }
            }

            "live_constellation" -> {
                val nodeCount = (26 * densityMult).toInt().coerceIn(12, 48)
                val connectDist = minDim * 0.28f * scaleMult
                val pts = Array(nodeCount) { idx ->
                    val seed = idx * 1.618034f
                    val px = ((sin(seed * 7.1f + animTimeSec * 0.18f) * 0.5f + 0.5f) * w)
                    val py = ((cos(seed * 5.3f + animTimeSec * 0.14f) * 0.5f + 0.5f) * h)
                    Offset(px, py)
                }
                for (i in 0 until nodeCount) {
                    for (j in i + 1 until nodeCount) {
                        val d = hypot(pts[i].x - pts[j].x, pts[i].y - pts[j].y)
                        if (d < connectDist) {
                            val a = ((1f - d / connectDist) * 0.25f * opacityMult).coerceIn(0.02f, 0.65f)
                            drawLine(accentColor.copy(alpha = a), pts[i], pts[j], strokeWidth = 0.85.dp.toPx())
                        }
                    }
                    drawCircle(
                        color = Color.White.copy(alpha = (0.65f * opacityMult).coerceIn(0.15f, 0.95f)),
                        radius = 1.8.dp.toPx(),
                        center = pts[i]
                    )
                }
            }

            "live_embers" -> {
                val count = (42 * densityMult).toInt().coerceIn(18, 80)
                for (i in 0 until count) {
                    val seed = i * 2.39996f
                    val x = ((sin(seed * 11.3f) * 0.5f + 0.5f) * w + sin(animTimeSec * 0.6f + i) * 14.dp.toPx())
                    val rawY = ((seed * 97.1f - animTimeSec * 28f) % h + h) % h
                    val r = (1.2f + (i % 4) * 0.55f).dp.toPx() * scaleMult
                    val a = ((0.18f + 0.35f * sin(animTimeSec + i).absoluteValue) * opacityMult).coerceIn(0.05f, 0.85f)
                    drawCircle(
                        color = if (i % 3 == 0) accentColor.copy(alpha = a) else Color.White.copy(alpha = a * 0.7f),
                        radius = r,
                        center = Offset(x.coerceIn(0f, w), rawY)
                    )
                }
            }

            "live_starlight" -> {
                val count = (75 * densityMult).toInt().coerceIn(30, 140)
                for (i in 0 until count) {
                    val sx = ((sin(i * 12.9898f) * 43758.5453f).let { it - floor(it) }) * w
                    val sy = ((sin(i * 78.233f) * 19341.123f).let { it - floor(it) }) * h
                    val tw = 0.5f + 0.5f * sin(animTimeSec * (0.9f + (i % 5) * 0.3f) + i)
                    val a = (tw * 0.65f * opacityMult).coerceIn(0.05f, 0.95f)
                    drawCircle(
                        color = if (i % 7 == 0) accentColor.copy(alpha = a) else Color.White.copy(alpha = a),
                        radius = (0.8f + (i % 3) * 0.5f).dp.toPx() * scaleMult,
                        center = Offset(sx, sy)
                    )
                }
            }

            "live_kepler" -> {
                val center = Offset(w * 0.5f, h * 0.5f)
                val orbits = 5
                for (o in 1..orbits) {
                    val rx = minDim * (0.12f * o) * scaleMult
                    val ry = rx * 0.62f
                    drawOval(
                        color = Color.White.copy(alpha = (0.14f * opacityMult).coerceIn(0.03f, 0.45f)),
                        topLeft = Offset(center.x - rx, center.y - ry),
                        size = Size(rx * 2f, ry * 2f),
                        style = Stroke(width = 0.8.dp.toPx())
                    )
                    val ang = animTimeSec * (0.55f / o) + o * 1.2f
                    val px = center.x + cos(ang) * rx
                    val py = center.y + sin(ang) * ry
                    drawCircle(
                        color = if (o % 2 == 1) accentColor else Color.White,
                        radius = 2.8.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }

            "live_quantum" -> {
                val step = (26.dp.toPx() / densityMult.coerceIn(0.6f, 1.6f)).coerceAtLeast(14.dp.toPx())
                var y = step * 0.5f
                while (y < h) {
                    var x = step * 0.5f
                    while (x < w) {
                        val d = hypot(x - w * 0.5f, y - h * 0.5f) / fullDiag
                        val wave = 0.5f + 0.5f * sin(d * 18f - animTimeSec * 1.8f)
                        val r = (0.8f + wave * 1.8f).dp.toPx() * scaleMult
                        drawCircle(
                            color = accentColor.copy(alpha = (wave * 0.42f * opacityMult).coerceIn(0.04f, 0.82f)),
                            radius = r,
                            center = Offset(x, y)
                        )
                        x += step
                    }
                    y += step
                }
            }

            "live_glow_horizon" -> {
                val pulse = 0.75f + 0.25f * sin(animTimeSec * 0.9f)
                val glowAlpha = (0.36f * pulse * opacityMult).coerceIn(0.06f, 0.82f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = glowAlpha),
                            accentColor.copy(alpha = glowAlpha * 0.32f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.88f),
                        radius = fullDiag * 0.62f * scaleMult
                    ),
                    size = Size(w, h)
                )
            }

            "live_halftone_pulse" -> {
                val step = (22.dp.toPx() / densityMult.coerceIn(0.6f, 1.6f)).coerceAtLeast(12.dp.toPx())
                var y = step * 0.5f
                while (y < h) {
                    var x = step * 0.5f
                    while (x < w) {
                        val wave = 0.5f + 0.5f * sin((x / w) * 6f + (y / h) * 8f - animTimeSec * 1.6f)
                        val r = (wave * 3.2.dp.toPx() * scaleMult).coerceAtLeast(0.6.dp.toPx())
                        drawCircle(
                            color = accentColor.copy(alpha = ((0.12f + wave * 0.35f) * opacityMult).coerceIn(0.04f, 0.78f)),
                            radius = r,
                            center = Offset(x, y)
                        )
                        x += step
                    }
                    y += step
                }
            }

            "dots" -> {
                val step = (24.dp.toPx() / densityMult.coerceIn(0.5f, 1.75f)).coerceAtLeast(12.dp.toPx())
                val a = (0.24f * opacityMult).coerceIn(0.05f, 0.75f)
                var y = step * 0.5f
                while (y < h) {
                    var x = step * 0.5f
                    while (x < w) {
                        drawCircle(Color.White.copy(alpha = a), radius = 1.1.dp.toPx() * scaleMult, center = Offset(x, y))
                        x += step
                    }
                    y += step
                }
            }

            "blueprint" -> {
                val step = (32.dp.toPx() * scaleMult).coerceAtLeast(16.dp.toPx())
                val a = (0.14f * opacityMult).coerceIn(0.04f, 0.55f)
                var x = 0f
                while (x <= w) {
                    drawLine(accentColor.copy(alpha = a), Offset(x, 0f), Offset(x, h), strokeWidth = 0.75.dp.toPx())
                    x += step
                }
                var y = 0f
                while (y <= h) {
                    drawLine(accentColor.copy(alpha = a), Offset(0f, y), Offset(w, y), strokeWidth = 0.75.dp.toPx())
                    y += step
                }
            }

            else -> {
                val glowAlpha = (0.24f * opacityMult).coerceIn(0.05f, 0.68f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = glowAlpha), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = fullDiag * 0.55f * scaleMult
                    ),
                    size = Size(w, h)
                )
            }
        }

        if (dimPercent > 0) {
            val dimAlpha = (dimPercent.coerceIn(0, 90) / 100f)
            drawRect(
                color = Color.Black.copy(alpha = dimAlpha),
                topLeft = Offset.Zero,
                size = Size(w, h)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RetroSplitFlapClockWidget(
    hourStr: String,
    minStr: String,
    secStr: String,
    amPmStr: String,
    dateStr: String,
    sizeMode: String,
    widgetStyle: String = "default",
    accentColor: Color,
    secondaryColor: Color = accentColor,
    lomoFontFamily: FontFamily,
    onOpenClockApp: () -> Unit,
    onLongPressHome: () -> Unit = {}
) {
    val tileHeight = when (sizeMode) {
        "compact" -> 42.dp
        "large" -> 64.dp
        "xlarge" -> 78.dp
        "max" -> 94.dp
        else -> 52.dp
    }
    val digitFontSize = when (sizeMode) {
        "compact" -> 22.sp
        "large" -> 34.sp
        "xlarge" -> 42.sp
        "max" -> 52.sp
        else -> 28.sp
    }
    val paddedHour = hourStr.padStart(2, '0')
    val paddedMin = minStr.padStart(2, '0')
    val paddedSec = secStr.padStart(2, '0')

    val is80s = widgetStyle == "80s_retro" || LocalRetro80sMode.current
    val isCyber = widgetStyle == "cyber_hud"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .retroNeonBorder(
                width = if (is80s || isCyber) 1.4.dp else 1.dp,
                color = if (is80s || isCyber) accentColor else Color(0xFF222222),
                shape = RoundedCornerShape(if (is80s || isCyber) 0.dp else 10.dp),
                glowEnabled = is80s || isCyber,
                glowRadiusDp = 5.dp
            )
            .background(if (is80s || isCyber) Color(0xFF030606) else Color(0xFF060606), RoundedCornerShape(if (is80s || isCyber) 0.dp else 10.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onOpenClockApp() },
                onLongClick = { onLongPressHome() }
            )
            .padding(horizontal = 12.dp, vertical = if (sizeMode == "compact") 8.dp else 10.dp)
    ) {
        if (is80s || isCyber) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCyber) "HOLOMAP // CHRONO" else "OLD SCHOOL // CHRONO",
                    color = accentColor,
                    fontSize = 8.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = glowTextStyle(accentColor, 16f)
                )
                Text(
                    text = dateStr,
                    color = secondaryColor,
                    fontSize = 8.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = glowTextStyle(secondaryColor, 16f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SplitFlapMechanicalTile(
                valueText = paddedHour,
                subLabel = if (is80s || isCyber) "HR" else "",
                tileHeight = tileHeight,
                fontSize = digitFontSize,
                textColor = if (is80s || isCyber) secondaryColor else Color.White,
                borderColor = if (is80s || isCyber) accentColor.copy(alpha = 0.75f) else Color(0xFF262626),
                sharpCorners = is80s || isCyber,
                lomoFontFamily = lomoFontFamily,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = ":",
                color = accentColor,
                fontSize = 20.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(accentColor, 16f)
            )

            SplitFlapMechanicalTile(
                valueText = paddedMin,
                subLabel = if (is80s || isCyber) "MN" else "",
                tileHeight = tileHeight,
                fontSize = digitFontSize,
                textColor = if (is80s || isCyber) secondaryColor else Color.White,
                borderColor = if (is80s || isCyber) accentColor.copy(alpha = 0.75f) else Color(0xFF262626),
                sharpCorners = is80s || isCyber,
                lomoFontFamily = lomoFontFamily,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = ":",
                color = if (is80s || isCyber) accentColor else Color(0xFF454545),
                fontSize = 18.sp,
                fontFamily = lomoFontFamily,
                style = glowTextStyle(accentColor, 16f)
            )

            SplitFlapMechanicalTile(
                valueText = paddedSec,
                subLabel = amPmStr,
                tileHeight = tileHeight,
                fontSize = (digitFontSize.value * 0.78f).sp,
                textColor = secondaryColor,
                borderColor = if (is80s || isCyber) accentColor.copy(alpha = 0.75f) else Color(0xFF262626),
                sharpCorners = is80s || isCyber,
                lomoFontFamily = lomoFontFamily,
                modifier = Modifier.weight(0.85f)
            )
        }
    }
}

@Composable
private fun SplitFlapMechanicalTile(
    valueText: String,
    subLabel: String,
    tileHeight: Dp,
    fontSize: TextUnit,
    textColor: Color,
    borderColor: Color = Color(0xFF262626),
    sharpCorners: Boolean = false,
    lomoFontFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(if (sharpCorners) 0.dp else 7.dp)
    Box(
        modifier = modifier
            .height(tileHeight)
            .border(1.dp, borderColor, shape)
            .background(Color(0xFF0A0A0A), shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = valueText,
            color = textColor,
            fontSize = fontSize,
            fontFamily = lomoFontFamily,
            fontWeight = FontWeight.Bold,
            style = glowTextStyle(textColor, 18f)
        )
        if (subLabel.isNotEmpty()) {
            Text(
                text = subLabel,
                color = borderColor,
                fontSize = 7.sp,
                fontFamily = lomoFontFamily,
                style = glowTextStyle(borderColor, 12f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 5.dp, bottom = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RetroAnalogChronoWidget(
    currentTimeMillis: Long,
    hourStr: String,
    minStr: String,
    secStr: String,
    amPmStr: String,
    dateStr: String,
    sizeMode: String,
    widgetStyle: String = "default",
    accentColor: Color,
    secondaryColor: Color = accentColor,
    lomoFontFamily: FontFamily,
    widgetsActive: Boolean = true,
    onOpenClockApp: () -> Unit,
    onLongPressHome: () -> Unit = {}
) {
    val dialSize = when (sizeMode) {
        "compact" -> 64.dp
        "large" -> 106.dp
        "xlarge" -> 128.dp
        "max" -> 154.dp
        else -> 84.dp
    }
    val cal = remember(currentTimeMillis / 1000L) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val hour12 = cal.get(Calendar.HOUR)
    val minute = cal.get(Calendar.MINUTE)
    val second = cal.get(Calendar.SECOND)

    val is80s = widgetStyle == "80s_retro" || LocalRetro80sMode.current
    val isCyber = widgetStyle == "cyber_hud"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .retroNeonBorder(
                width = if (is80s || isCyber) 1.4.dp else 1.dp,
                color = if (is80s || isCyber) accentColor else Color(0xFF222222),
                shape = RoundedCornerShape(if (is80s || isCyber) 0.dp else 10.dp),
                glowEnabled = is80s || isCyber,
                glowRadiusDp = 5.dp
            )
            .background(Color(0xFF050606), RoundedCornerShape(if (is80s || isCyber) 0.dp else 10.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onOpenClockApp() },
                onLongClick = { onLongPressHome() }
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(dialSize)) {
            val r = size.minDimension * 0.46f
            val c = Offset(size.width * 0.5f, size.height * 0.5f)
            drawCircle(
                color = if (is80s || isCyber) accentColor else Color(0xFF333333),
                radius = r,
                center = c,
                style = Stroke(width = 1.4.dp.toPx())
            )
            for (t in 0 until 12) {
                val ang = Math.toRadians((t * 30 - 90).toDouble()).toFloat()
                val inR = if (t % 3 == 0) r * 0.78f else r * 0.86f
                drawLine(
                    color = if (is80s || isCyber) accentColor else Color.White,
                    start = Offset(c.x + cos(ang) * inR, c.y + sin(ang) * inR),
                    end = Offset(c.x + cos(ang) * r, c.y + sin(ang) * r),
                    strokeWidth = if (t % 3 == 0) 1.6.dp.toPx() else 0.9.dp.toPx()
                )
            }
            val hrAng = Math.toRadians(((hour12 + minute / 60f) * 30f - 90f).toDouble()).toFloat()
            val mnAng = Math.toRadians(((minute + second / 60f) * 6f - 90f).toDouble()).toFloat()
            val scAng = Math.toRadians((second * 6f - 90f).toDouble()).toFloat()
            drawLine(
                color = if (is80s || isCyber) accentColor else Color.White,
                start = c,
                end = Offset(c.x + cos(hrAng) * r * 0.52f, c.y + sin(hrAng) * r * 0.52f),
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = if (is80s || isCyber) accentColor else Color(0xFFE5E5E5),
                start = c,
                end = Offset(c.x + cos(mnAng) * r * 0.74f, c.y + sin(mnAng) * r * 0.74f),
                strokeWidth = 1.7.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = secondaryColor,
                start = c,
                end = Offset(c.x + cos(scAng) * r * 0.84f, c.y + sin(scAng) * r * 0.84f),
                strokeWidth = 1.1.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = secondaryColor, radius = 2.6.dp.toPx(), center = c)
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (is80s || isCyber) {
                Text(
                    text = if (isCyber) "HOLOMAP // RADAR" else "OLD SCHOOL // RADAR",
                    color = accentColor,
                    fontSize = 8.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = glowTextStyle(accentColor, 14f)
                )
            }
            Text(
                text = if (isCyber) "<<< $hourStr:$minStr:$secStr >>>" else "$hourStr:$minStr:$secStr",
                color = if (is80s || isCyber) secondaryColor else Color.White,
                fontSize = 16.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(secondaryColor, 18f)
            )
            Text(
                text = dateStr,
                color = if (is80s || isCyber) accentColor else Color(0xFF8A8A8A),
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                style = glowTextStyle(accentColor, 14f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrecisionChronographWidget(
    sizeMode: String,
    widgetStyle: String = "default",
    accentColor: Color,
    secondaryColor: Color = accentColor,
    lomoFontFamily: FontFamily,
    widgetsActive: Boolean = true,
    onLongPressHome: () -> Unit = {},
    onTick: () -> Unit = {}
) {
    var running by remember { mutableStateOf(false) }
    var accumulatedMs by remember { mutableStateOf(0L) }
    var startRealtimeMs by remember { mutableStateOf(0L) }
    var displayMs by remember { mutableStateOf(0L) }

    LaunchedEffect(running, widgetsActive) {
        if (!running || !widgetsActive) return@LaunchedEffect
        while (isActive && running && widgetsActive) {
            withFrameNanos {
                displayMs = accumulatedMs + (SystemClock.elapsedRealtime() - startRealtimeMs)
            }
        }
    }

    val totalSec = displayMs / 1000L
    val mins = (totalSec / 60L).toString().padStart(2, '0')
    val secs = (totalSec % 60L).toString().padStart(2, '0')
    val centis = ((displayMs % 1000L) / 10L).toString().padStart(2, '0')
    val is80s = widgetStyle == "80s_retro" || LocalRetro80sMode.current
    val isCyber = widgetStyle == "cyber_hud"
    val btnShape = RoundedCornerShape(if (is80s || isCyber) 0.dp else 5.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .retroNeonBorder(
                width = if (is80s || isCyber) 1.4.dp else 1.dp,
                color = if (is80s || isCyber) accentColor else Color(0xFF222222),
                shape = RoundedCornerShape(if (is80s || isCyber) 0.dp else 10.dp),
                glowEnabled = is80s || isCyber,
                glowRadiusDp = 5.dp
            )
            .background(Color(0xFF050606), RoundedCornerShape(if (is80s || isCyber) 0.dp else 10.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = { onLongPressHome() }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (isCyber) "HOLOMAP // TIMER" else "OLD SCHOOL // TIMER",
                color = accentColor,
                fontSize = 8.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(accentColor, 14f)
            )
            Text(
                text = if (isCyber) "<<< $mins:$secs.$centis >>>" else "$mins:$secs.$centis",
                color = if (is80s || isCyber) secondaryColor else Color.White,
                fontSize = if (sizeMode == "large") 20.sp else 16.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(secondaryColor, 18f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (running) "PAUSE" else "START",
                color = if (running) Color.Black else if (is80s || isCyber) secondaryColor else Color.White,
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = if (running) TextStyle.Default else glowTextStyle(secondaryColor, 14f),
                modifier = Modifier
                    .border(1.dp, secondaryColor, btnShape)
                    .background(if (running) secondaryColor else Color(0xFF101010), btnShape)
                    .clickable {
                        onTick()
                        if (running) {
                            accumulatedMs += SystemClock.elapsedRealtime() - startRealtimeMs
                            displayMs = accumulatedMs
                            running = false
                        } else {
                            startRealtimeMs = SystemClock.elapsedRealtime()
                            running = true
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )
            Text(
                text = "RESET",
                color = if (is80s || isCyber) accentColor else Color(0xFFA3A3A3),
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(accentColor, 14f),
                modifier = Modifier
                    .border(1.dp, if (is80s || isCyber) accentColor else Color(0xFF282828), btnShape)
                    .background(Color(0xFF101010), btnShape)
                    .clickable {
                        onTick()
                        running = false
                        accumulatedMs = 0L
                        displayMs = 0L
                    }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
fun RetroDotMatrixCalendarWidget(
    currentTimeMillis: Long,
    sizeMode: String,
    widgetStyle: String = "default",
    accentColor: Color,
    secondaryColor: Color = accentColor,
    lomoFontFamily: FontFamily
) {
    val cal = remember(currentTimeMillis / 60_000L) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val todayDay = cal.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthTitle = remember(currentTimeMillis / 60_000L) {
        SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(currentTimeMillis)).uppercase(Locale.US)
    }
    val firstDayOffset = remember(currentTimeMillis / 60_000L) {
        val temp = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        ((temp.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) + 7) % 7
    }
    val totalCells = firstDayOffset + daysInMonth
    val rows = (totalCells + 6) / 7
    val is80s = widgetStyle == "80s_retro" || LocalRetro80sMode.current
    val isCyber = widgetStyle == "cyber_hud"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .retroNeonBorder(
                width = if (is80s || isCyber) 1.4.dp else 1.dp,
                color = if (is80s || isCyber) accentColor else Color(0xFF222222),
                shape = RoundedCornerShape(if (is80s || isCyber) 0.dp else 10.dp),
                glowEnabled = is80s || isCyber,
                glowRadiusDp = 5.dp
            )
            .background(Color(0xFF040606), RoundedCornerShape(if (is80s || isCyber) 0.dp else 10.dp))
            .padding(horizontal = 12.dp, vertical = if (sizeMode == "compact") 8.dp else 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthTitle,
                color = if (is80s || isCyber) accentColor else Color.White,
                fontSize = 11.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(accentColor, 16f)
            )
            Text(
                text = "[${todayDay.toString().padStart(2, '0')}/${daysInMonth}]",
                color = secondaryColor,
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(secondaryColor, 16f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU").forEachIndexed { idx, dName ->
                Text(
                    text = dName,
                    color = if (idx >= 5) secondaryColor else if (is80s || isCyber) accentColor else Color(0xFF737373),
                    fontSize = 8.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = glowTextStyle(if (idx >= 5) secondaryColor else accentColor, 12f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(if (is80s || isCyber) accentColor.copy(alpha = 0.6f) else Color(0xFF1A1A1A))
        )
        Spacer(modifier = Modifier.height(4.dp))

        Column(verticalArrangement = Arrangement.spacedBy(if (sizeMode == "large") 5.dp else 3.dp)) {
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                        val cellIdx = r * 7 + c
                        val dayNum = cellIdx - firstDayOffset + 1
                        val isValidDay = dayNum in 1..daysInMonth
                        val isToday = isValidDay && dayNum == todayDay
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (isToday) {
                                        Modifier.background(secondaryColor, RoundedCornerShape(if (is80s || isCyber) 0.dp else 4.dp))
                                    } else Modifier
                                )
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val dCol = when {
                                isToday -> Color.Black
                                is80s || isCyber -> accentColor
                                else -> Color(0xFFD4D4D4)
                            }
                            Text(
                                text = if (isValidDay) dayNum.toString().padStart(2, '0') else "  ",
                                color = dCol,
                                fontSize = if (sizeMode == "large") 10.sp else 9.sp,
                                fontFamily = lomoFontFamily,
                                fontWeight = if (isToday || is80s || isCyber) FontWeight.Bold else FontWeight.Normal,
                                style = if (isToday) TextStyle.Default else glowTextStyle(dCol, 12f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RetroWeekTimelineStripWidget(
    currentTimeMillis: Long,
    sizeMode: String,
    widgetStyle: String = "default",
    timeHeaderStr: String = "",
    batteryPct: Int = 80,
    accentColor: Color,
    secondaryColor: Color = accentColor,
    lomoFontFamily: FontFamily
) {
    val cal = remember(currentTimeMillis / 60_000L) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val todayDayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val shortMonth = remember(currentTimeMillis / 60_000L) {
        SimpleDateFormat("MMM", Locale.US).format(Date(currentTimeMillis)).uppercase(Locale.US)
    }
    val shortDow = remember(currentTimeMillis / 60_000L) {
        SimpleDateFormat("EEE", Locale.US).format(Date(currentTimeMillis)).uppercase(Locale.US)
    }

    if (widgetStyle == "80s_retro" || widgetStyle == "cyber_hud" || LocalRetro80sMode.current) {
        // 4x2 Bordered Table Grid with Today Highlighted in Secondary Color (Glowing Red in Holomap mode, Green/Amber in Old School)
        val eightDays = remember(currentTimeMillis / 60_000L) {
            val temp = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
            temp.add(Calendar.DAY_OF_MONTH, -5)
            (-5..2).map { offset ->
                val dow = temp.get(Calendar.DAY_OF_WEEK)
                val lbl = when (dow) {
                    Calendar.MONDAY -> "MO"
                    Calendar.TUESDAY -> "TU"
                    Calendar.WEDNESDAY -> "WE"
                    Calendar.THURSDAY -> "TH"
                    Calendar.FRIDAY -> "FR"
                    Calendar.SATURDAY -> "SA"
                    else -> "SU"
                }
                val dNum = temp.get(Calendar.DAY_OF_MONTH)
                val isToday = offset == 0
                temp.add(Calendar.DAY_OF_MONTH, 1)
                Triple(lbl, dNum, isToday)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (timeHeaderStr.isNotEmpty()) "$timeHeaderStr $shortDow - $batteryPct%" else "$shortDow · $shortMonth ${todayDayOfMonth.toString().padStart(2, '0')}",
                    color = accentColor,
                    fontSize = if (sizeMode == "large") 12.sp else 10.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = glowTextStyle(accentColor, 16f)
                )
                Text(
                    text = "[$shortMonth ${todayDayOfMonth.toString().padStart(2, '0')}/$daysInMonth]",
                    color = secondaryColor,
                    fontSize = 9.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = glowTextStyle(secondaryColor, 16f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroNeonBorder(1.4.dp, accentColor, RoundedCornerShape(0.dp), glowEnabled = true, glowRadiusDp = 5.dp)
                    .background(Color(0xFF030606), RoundedCornerShape(0.dp))
            ) {
                eightDays.chunked(4).forEachIndexed { rIdx, rowItems ->
                    if (rIdx > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.2.dp)
                                .background(accentColor)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        rowItems.forEachIndexed { cIdx, (dayLbl, dayNum, isToday) ->
                            if (cIdx > 0) {
                                Box(
                                    modifier = Modifier
                                        .width(1.2.dp)
                                        .fillMaxHeight()
                                        .background(accentColor)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isToday) secondaryColor else Color.Transparent)
                                    .padding(vertical = if (sizeMode == "large") 8.dp else 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = dayLbl,
                                    color = if (isToday) Color.Black else accentColor,
                                    fontSize = if (sizeMode == "large") 12.sp else 10.sp,
                                    fontFamily = lomoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    style = if (isToday) TextStyle.Default else glowTextStyle(accentColor, 14f)
                                )
                                Text(
                                    text = dayNum.toString().padStart(2, '0'),
                                    color = if (isToday) Color.Black else accentColor,
                                    fontSize = if (sizeMode == "large") 14.sp else 12.sp,
                                    fontFamily = lomoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    style = if (isToday) TextStyle.Default else glowTextStyle(accentColor, 14f)
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    val weekDays = remember(currentTimeMillis / 60_000L) {
        val temp = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        temp.add(Calendar.DAY_OF_MONTH, -3)
        (-3..3).map { offset ->
            val dow = temp.get(Calendar.DAY_OF_WEEK)
            val lbl = when (dow) {
                Calendar.MONDAY -> "MO"
                Calendar.TUESDAY -> "TU"
                Calendar.WEDNESDAY -> "WE"
                Calendar.THURSDAY -> "TH"
                Calendar.FRIDAY -> "FR"
                Calendar.SATURDAY -> "SA"
                else -> "SU"
            }
            val dNum = temp.get(Calendar.DAY_OF_MONTH)
            val isToday = offset == 0
            temp.add(Calendar.DAY_OF_MONTH, 1)
            Triple(lbl, dNum, isToday)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
            .background(Color(0xFF060606), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = if (sizeMode == "compact") 8.dp else 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            weekDays.forEach { (dayLbl, dayNum, isToday) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (isToday) accentColor else Color(0xFF1F1F1F), RoundedCornerShape(6.dp))
                        .background(if (isToday) accentColor.copy(alpha = 0.16f) else Color(0xFF0C0C0C), RoundedCornerShape(6.dp))
                        .padding(vertical = if (sizeMode == "large") 8.dp else 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = dayLbl,
                        color = if (isToday) accentColor else Color(0xFF737373),
                        fontSize = 8.sp,
                        fontFamily = lomoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dayNum.toString().padStart(2, '0'),
                        color = Color.White,
                        fontSize = if (sizeMode == "large") 14.sp else 12.sp,
                        fontFamily = lomoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RetroYearDotMatrixWidget(
    currentTimeMillis: Long,
    sizeMode: String,
    widgetStyle: String = "default",
    accentColor: Color,
    secondaryColor: Color = accentColor,
    lomoFontFamily: FontFamily
) {
    val cal = remember(currentTimeMillis / 60_000L) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val yearNum = cal.get(Calendar.YEAR)
    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    val totalDaysInYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
    val yearPct = ((dayOfYear.toFloat() / totalDaysInYear.toFloat()) * 100f).roundToInt()
    val canvasHeight = when (sizeMode) {
        "compact" -> 44.dp
        "large" -> 82.dp
        else -> 62.dp
    }
    val is80s = widgetStyle == "80s_retro" || LocalRetro80sMode.current
    val isCyber = widgetStyle == "cyber_hud"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .retroNeonBorder(
                width = if (is80s || isCyber) 1.4.dp else 1.dp,
                color = if (is80s || isCyber) accentColor else Color(0xFF222222),
                shape = RoundedCornerShape(if (is80s || isCyber) 0.dp else 10.dp),
                glowEnabled = is80s || isCyber,
                glowRadiusDp = 5.dp
            )
            .background(Color(0xFF030606), RoundedCornerShape(if (is80s || isCyber) 0.dp else 10.dp))
            .padding(horizontal = 12.dp, vertical = if (sizeMode == "compact") 8.dp else 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$yearNum SECTORS",
                color = if (is80s || isCyber) accentColor else Color.White,
                fontSize = 11.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(accentColor, 16f)
            )
            Text(
                text = "[$dayOfYear/$totalDaysInYear · $yearPct%]",
                color = secondaryColor,
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                style = glowTextStyle(secondaryColor, 16f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeight)
        ) {
            val cols = 37
            val rows = 10
            val cellW = size.width / cols.toFloat()
            val cellH = size.height / rows.toFloat()
            val dotR = (min(cellW, cellH) * 0.28f).coerceAtLeast(1.4f)

            for (d in 1..totalDaysInYear) {
                val idx = d - 1
                val c = idx % cols
                val r = idx / cols
                val cx = c * cellW + cellW * 0.5f
                val cy = r * cellH + cellH * 0.5f
                if (is80s || isCyber) {
                    val bw = (cellW * 0.72f).coerceAtLeast(2f)
                    val bh = (cellH * 0.72f).coerceAtLeast(2f)
                    val blockCol = when {
                        d == dayOfYear -> secondaryColor
                        d < dayOfYear -> accentColor.copy(alpha = 0.85f)
                        else -> accentColor.copy(alpha = 0.16f)
                    }
                    drawRect(
                        color = blockCol,
                        topLeft = Offset(cx - bw * 0.5f, cy - bh * 0.5f),
                        size = Size(bw, bh)
                    )
                } else {
                    val col = when {
                        d == dayOfYear -> accentColor
                        d < dayOfYear -> Color.White.copy(alpha = 0.78f)
                        else -> Color(0xFF222222)
                    }
                    drawCircle(
                        color = col,
                        radius = if (d == dayOfYear) dotR * 1.35f else dotR,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }
}
