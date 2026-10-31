package com.cmf.ntextlauncher

import android.annotation.SuppressLint
import android.app.ActivityOptions
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
import android.hardware.camera2.CameraManager
import android.app.ActivityManager
import android.os.StatFs
import android.os.Environment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import android.provider.AlarmClock
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
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.Shader as AndroidShader
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
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
    "YGBgYGABIGBgASBgwMBgwGBgAkBgAAADAAAAAAHgAqAABwAXABsAACUjESM1MzUzITMRMzUzFSMVIxUhFSE1IxM1IRUB4GBgYGD+" +
    "IGBgYGBgASD+4GBgASBgASBgYP7gYGBgYGBgAeBgYAABAGAAAAGAAqAACwAAMzUzESM1MzUzETMVYGBgYGBgYAGAYGD9wGAABAAA" +
    "AAAB4AKgAAsAEQAVABkAADc1MzUzNTMVIxUjFQUhNTMVIQE1MxU1NSEVYGDAYGDAASD+IGABgP4gYAEgwGBgwMBgYMDAYAGAYGBg" +
    "YGAAAAUAAAAAAeACoAALAA8AEwAXABsAACUjNSM1MzUzFSMVMyU1MxU1NSEVESE1ISE1MxUB4GDAwGBgYP4gYAEg/uABIP6AYGDA" +
    "YMDAYMBgYGBgYP3AYGBgAAABAAAAAAHgAqAACQAAISMRIREzESERMwHgYP6AYAEgYAEgAYD+4AEgAAADAAAAAAHgAqAACwAPABMA" +
    "ABEhFSEVIRUzFSM1IRchFSEnNTMVAeD+gAEgYGD+gGABIP7gYGACoGDAYMDAwGBgYGAAAAIAAAAAAeACoAAPABcAACUjNSEVIRUh" +
    "NSMRMxUhFTMBNSEVMxUjNQHgYP7gASD+4GBgASBg/oABIGBgYMDAYGAB4MBgASBgYGBgAAABAAAAAAHgAqAAEQAAEzM1MzUzNSE1" +
    "IRUjFSMVIxEjYGBgYP6AAeBgYGBgASBgYGBgwGBg/uAAAAIAAAAAAeACoAAXABsAACUjNSEVIRUhNSM1MzUjNTMVITUzFSMVMwE1" +
    "IRUB4GD+4AEg/uBgYGBgASBgYGD+gAEgYMDAYGDAYMDAwMBgASBgYAAEAAAAAAHgAqAACwAPABMAFwAAJSM1ITUjNTMVITUzITUh" +
    "FREhNSEhNTMVAeBg/uBgYAEgYP6AASD+4AEg/oBgYMBgwMDAYGD9wGBgYAACAGAAYADAAkAAAwAHAAATIzUzERUjNcBgYGAB4GD+" +
    "gGBgAAAFAAAAAAGAAqAABwALABMAFwAbAAA3NTMVMxUjNScVIzU3MzUzFSMVIwEjNTMRFSM1YGBgYGBgYGBgYGABIGBgYMBgYGBg" +
    "wGBgYGBgYP6AYAJAYGAAAAcAAAAAAYACoAADAAcACwAPABMAFwAbAAATMxUjERUjNREzFSMTIzUzExUjNRcjNTMHMxUjYGBgYGBg" +
    "wGBgYGDAYGDAYGACQGD+gGBgAkBg/iBgASBgYMBgYGAABAAAAAAB4AKgAAMACwARABUAABE1MxU1NSEVMxUjNQczFSMVIxcVIzVg" +
    "ASBgYMDAYGBgYAHgYGBgYGDAwMBgYGBgYAADAAAAAAHgAqAABwARABUAABEzESEVITUjEzUhFTMRIREzNRUjFTNgASD+4GBgASBg" +
    "/uDAYGACQP4gYGAB4GBg/oABIGDAYAACAAAAAAHgAqAACwAPAAAhIxEhESMRMxUhNTMhNSEVAeBg/uBgYAEgYP6AASABIP7gAkDA" +
    "wGBgAAEAAAAAAeACoAATAAAlIzUhFSEVIREhFTMVIzUhFSEVMwHgYP7gASD+gAGAYGD+4AEgYGDAwGACoGDAwMBgAAADAAAAAAHg" +
    "AqAAAwALABMAACUVIzUBMxEhFSE1IxM1IRUzFSM1AeBg/oBgASD+4GBgASBgYMBgYAGA/iBgYAHgYGBgYAAAAgAAAAAB4AKgAAMA" +
    "CwAAJSMRMyUhFSERIRUhAeBgYP4gAYD+4AEg/oBgAeBgYP4gYAABAAAAAAHgAqAACwAAISERIRUhFSEVIRUhAeD+IAHg/oABIP7g" +
    "AYACoGDAYMAAAQAAAAAB4AKgAAkAAAEVIREjESEVIRUBgP7gYAHg/oABgGD+4AKgYMAAAwAAAAAB4AKgAAUADQAVAAAlIzUjNSEl" +
    "MxEhFSE1IxM1IRUzFSM1AeBgwAEg/iBgASD+4GBgASBgYGDAYMD+IGBgAeBgYGBgAAABAAAAAAHgAqAACwAAISMRIREjETMRIREz" +
    "AeBg/uBgYAEgYAEg/uACoP7gASAAAQAAAAABIAKgAAsAABMjNSEVIxEzFSE1M2BgASBgYP7gYAJAYGD+IGBgAAACAAAAAAHgAqAA" +
    "CwAPAAABIzUhFSMRIxUjNTMhNTMVASBgASBgYMDA/uBgAkBgYP4gYGBgYAAFAAAAAAHgAqAABwAPABcAGwAfAAAhIzUjNTMVMwEz" +
    "ETMVIxEjASMVIzUzNTM1MxUjAzMVIwHgYGBgYP4gYGBgYAGAYGBgYGBgwGBgYGBgAkD+4GD+4AHgYGBgYGD+4GAAAAEAAAAAAeAC" +
    "oAAFAAAhIREzESEB4P4gYAGAAqD9wAAAAwAAAAAB4AKgAAcADwATAAAhIxEjNTM1MyEzFTMVIxEjASM1MwHgYGBgYP4gYGBgYAEg" +
    "YGAB4GBgYGD+IAEgwAAAAQAAAAAB4AKgABMAACEjESM1IzUjESMRMxUzFTMVMxEzAeBgYGBgYGBgYGBgASBgYP4gAqBgYGABIAAA" +
    "AwAAAAAB4AKgAAMACwAPAAAlIxEzITMRIRUhNSMTNSEVAeBgYP4gYAEg/uBgYAEgYAHg/iBgYAHgYGAAAQAAAAAB4AKgAA0AABEh" +
    "FTMVIzUhFSEVIREjAYBgYP7gASD+4GACoGDAwMBg/uAABAAAAAAB4AKgAAsADwAXABsAACEjNSMVIzUzNTMVMwEzESMTNSEVMxEj" +
    "EQMzFSMB4GBgwMBgYP4gYGBgASBgYMBgYGBgYGBgAeD+IAHgYGD+gAGA/uBgAAIAAAAAAeACoAAHABkAACEjNSM1MxUzASEVMxUj" +
    "NSEVIRUjFSM1IxEjAeBgYGBg/iABgGBg/uABIGBgYGBgYGACQGDAwMBgYGD+4AAEAAAAAAHgAqAACwATABcAGwAAJSM1ITUjNTMV" +
    "IRUzATUhFTMVIzURITUhITUzFQHgYP7gYGABIGD+gAEgYGD+4AEg/oBgYMBgwMBgASBgYGBg/cBgYGAAAQAAAAAB4AKgAAcAAAEV" +
    "IxEjESM1AeDAYMACoGD9wAJAYAAAAgAAAAAB4AKgAAMACwAAJSMRMyEzESEVITUjAeBgYP4gYAEg/uBgYAJA/cBgYAAAAgAAAAAB" +
    "4AKgAAMAEwAAETMRIwEzESMVIxUjNSM1MxUzNTNgYAGAYGBgYGBgYGACoP4gAeD+IGBgYGBgYAADAAAAAAHgAqAABwAPABMAACEj" +
    "NSM1MxEzITMRMxUjFSMTMxUjAeBgYGBg/iBgYGBgwGBgYGAB4P4gYGABgMAABQAAAAAB4AKgABMAFwAbAB8AIwAANzUzNSM1MxUz" +
    "NTM1MxUjFSMVIxUFIzUzATUzFREjNTMhIzUzYGBgYGBgYGBgYAEgYGD+IGBgYAEgYGDAYGBgYGDAwGBgYMDAASDAwP4gwGAABAAA" +
    "AAAB4AKgAAcACwAPABMAABE1MxUzFSM1JTMVIwc1MxUjMxEjYGBgASBgYGBgwGBgAeDAwGBgwMBgYGD+gAAAAgAAAAAB4AKgABEA" +
    "FwAANzUzNTM1MzUhNSEVIxUjFSMVBSE1MxUhYGBgYP6AAeBgYGABIP4gYAGAwGBgYGBgwGBgYMDAYAAAAQAAAAABIAKgAAcAADER" +
    "IRUjETMVASDAwAKgYP4gYAABAAAAAAEgAqAABwAAEyM1IREhNTPAwAEg/uDAAkBg/WBgAAABAAD/oAHgAAAAAwAAFTUhFQHgYGBg" +
    "AAACAAAAAAHgAeAADQARAAA3NSE1ITUhFTMRITUhNQU1MxVgASD+4AEgYP6AASD+gGDAYGBgYP6AYGBgYGAAAAEAAAAAAeACoAAN" +
    "AAARMxUhFTMRIxEhESEVIWABIGBg/uABIP6AAqDAYP7gASD+4GAAAQAAAAAB4AHgABMAAAEjNSERITUzFSMVITUjETM1IRUzAeBg" +
    "/uABIGBg/uBgYAEgYAEgYP7gYGBgYAEgYGAAAAEAAAAAAeACoAANAAAhITUhESERIxEzNSE1MwHg/oABIP7gYGABIGBgASD+4AEg" +
    "YMAAAgAAAAAB4AHgAAMAEQAAExUhNSUhFTMVIRUhFSE1IxEzYAEg/uABIGD+gAEg/uBgYAGAYGBgYMBgYGABIAABAAAAAAHgAqAA" +
    "EwAAEyM1MzUzNTMVMxUjNSMVMxUjESNgYGBgwGBgwMDAYAEgYMBgYGBgwGD+4AAAAQAA/0AB4AHgABEAAAUhNSE1ITUhESERIxEz" +
    "NSERIwGA/uABIP7gASD+4GBgAYBgwGBgYAEg/uABIGD9wAACAAAAAAHgAqAAAwALAAABMxEjATMVIRUhESMBgGBg/oBgASD+4GAB" +
    "gP6AAqDAYP6AAAIAAAAAASACoAAJAA0AABMjNTMRMxUhNTMTIzUzYGDAYP7gYGBgYAGAYP6AYGAB4GAABAAA/0ABIAKgAAMACQAN" +
    "ABEAABcjNTMXESM1MxERIzUzAyM1M2BgYGBgwGBgYGBgYGBgAeBg/cACoGD8oGAAAAIAAAAAAYACoAATABcAABEzETM1MzUzFSMV" +
    "IxUzFSM1IxUjISM1M2BgYGBgYGBgYGABgGBgAqD+gGBgYGBgYGDAYAAAAQAAAAABIAKgAAkAABMjNTMRMxUhNTNgYMBg/uBgAkBg" +
    "/cBgYAADAAAAAAHgAeAABwANABEAACEjESM1MxUzJTMVIxEjEzMVIwHgYGBgYP4gwGBgwGBgAYBgYGBg/oABgMAAAAEAAAAAAeAB" +
    "4AAJAAAhIxEhESMRIRUzAeBg/uBgAYBgAYD+gAHgYAABAAAAAAHgAeAADwAAJSMRIREhFSE1IxEzNSEVMwHgYP7gASD+4GBgASBg" +
    "YAEg/uBgYAEgYGAAAAEAAP9AAeAB4AANAAAlIxEhESEVIRUjESEVMwHgYP7gASD+4GABgGBgASD+4GDAAqBgAAABAAD/QAHgAeAA" +
    "DQAABSM1ITUhESERIxEzNSEB4GD+4AEg/uBgYAGAwMBgASD+4AEgYAAAAQAAAAAB4AHgAA8AAAEjNSMVIxEjETMVMzUzFTMB4GDA" +
    "YGBgYMBgASBgYP7gAeBgYGAAAAQAAAAAAeAB4AAHAAsADwATAAA3NSEVMxUjNSUVIzU3IRUhASE1IWABIGBg/uBgYAGA/oABIP6A" +
    "AYDAYGBgYMBgYGBg/oBgAAIAAAAAAYACoAALABMAABMjNTM1MxUzFSMRIyUVIxUjNTM1YGBgYMDAYAEgYGBgAYBgwMBg/uBgYGBg" +
    "YAAAAgAAAAAB4AHgAAUACQAAISE1IREzITMRIwHg/oABIGD+IGBgYAGA/oAAAgAAAAAB4AHgAAMAEwAAETMRIwEzESMVIxUjNSM1" +
    "MxUzNTNgYAGAYGBgYGBgYGAB4P7gASD+4GBgYGBgYAADAAAAAAHgAeAAAwAPABMAACUjETMhMxEzETMRIxUjNSMFIzUzAeBgYP4g" +
    "YGBgYGBgAYBgYGABgP6AASD+4GBgYGAAAAgAAAAAAeAB4AAHAAsADwATABcAGwAfACMAACEjNSM1MxUzATUzFREVIzUBNTMVASM1" +
    "MzcjNTMHIzUzFTMVIwHgYGBgYP4gYGABgGD+4GBgwGBgwGBgYGBgYGABIGBg/uBgYAEgYGD+4GBgYGBgYGAAAAIAAP9AAeAB4AAL" +
    "AA8AAAUhNSE1ITUhETMRIwEzESMBgP7gASD+4AEgYGD+gGBgwGBgYAGA/cACQP6AAAMAAAAAAeAB4AAHAA8AEwAAISE1MzUzFSEB" +
    "NSEVIxUjNQczFSMB4P4gYGABIP4gAeBgYGBgYGBgYAEgYGBgYGBgAAQAAAEgAYACoAADAAcACwAPAAARMxUjJSM1MxE1MxUFNTMV" +
    "YGABIMDAYP7gwAJAwMBg/uDAwGBgYAABAGABIADAAYAAAwAAExUjNcBgAYBgYAABAAAAAAGAAqAADwAAETMVMxUzFTMVIxUjFSMV" +
    "I2BgYGBgYGBgAqBgYGBgYGBgAAAAAAwAlgABAAAAAAABAAwAAAABAAAAAAACAAcADAABAAAAAAADABQAEwABAAAAAAAEABQAJwAB" +
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
    val isHidden: Boolean = false,
    val isSystemApp: Boolean = false
)

enum class LauncherView {
    HOME_AND_DRAWER, WIDGETS, SETTINGS
}

private val LAUNCHER_ACCENT_PALETTE: List<Pair<String, String>> = listOf(
    "#FF3B00" to "CMF",
    "#FFFFFF" to "WHT",
    "#D71921" to "RED",
    "#00FF66" to "GRN",
    "#00E5FF" to "CYN",
    "#FFB000" to "AMB",
    "#A855F7" to "VLT",
    "#3B82F6" to "BLU",
    "#FF2A85" to "MAG",
    "#10B981" to "MNT"
)

private fun resolveLauncherAccentColor(hex: String): Color = when (hex.uppercase(Locale.US)) {
    "#FFFFFF" -> Color(0xFFFFFFFF)
    "#D71921" -> Color(0xFFD71921)
    "#00FF66" -> Color(0xFF00FF66)
    "#00E5FF" -> Color(0xFF00E5FF)
    "#FFB000" -> Color(0xFFFFB000)
    "#A855F7" -> Color(0xFFA855F7)
    "#3B82F6" -> Color(0xFF3B82F6)
    "#FF2A85" -> Color(0xFFFF2A85)
    "#10B981" -> Color(0xFF10B981)
    else -> Color(0xFFFF3B00)
}

enum class PrebuiltFont(
    val id: String,
    val shortLabel: String,
    val displayName: String,
    val uppercaseHome: Boolean
) {
    LOMO_PIXEL("dot", "PIXEL", "Lomo Web Pixel (Default · 1px Thin)", false),
    LOMO_CAPS("ndot", "CAPS", "Lomo Web Pixel (Uppercase)", true),
    TECH_MONO("mono", "MONO", "JetBrains Tech Mono", false),
    SPACE_MONO("spacemono", "TECH", "Space Mono Industrial Bold", false),
    CLEAN_SANS("sans", "SANS", "Clean Geometric Sans", false),
    EDITORIAL("editorial", "SERIF", "Minimal Editorial Serif", false),
    CONDENSED_SANS("condensed", "COND", "Cyber Condensed Sans", false),
    CONDENSED_BOLD("cond_bold", "CND-B", "Industrial Condensed Caps", true),
    ULTRA_THIN("thin", "THIN", "Ultra-Thin Hairline Sans", false),
    LIGHT_SANS("light", "LIGHT", "Swiss Light Minimal", false),
    SMALL_CAPS("smallcaps", "SCAPS", "Architectural Small Caps", false),
    HEAVY_BLACK("black", "HEAVY", "Brutalist Heavy Black", false),
    SLAB_MONO("serif_mono", "TYPE", "Typewriter Slab Mono", false),
    SCRIPT_CURSIVE("cursive", "SCRPT", "Minimalist Script Cursive", false);

    fun resolveFontFamily(lomoFamily: FontFamily): FontFamily = when (this) {
        LOMO_PIXEL, LOMO_CAPS -> lomoFamily
        TECH_MONO -> FontFamily.Monospace
        SPACE_MONO -> resolveSystemTypeface("monospace", Typeface.BOLD)
        CLEAN_SANS -> FontFamily.SansSerif
        EDITORIAL -> FontFamily.Serif
        CONDENSED_SANS -> resolveSystemTypeface("sans-serif-condensed", Typeface.NORMAL)
        CONDENSED_BOLD -> resolveSystemTypeface("sans-serif-condensed", Typeface.BOLD)
        ULTRA_THIN -> resolveSystemTypeface("sans-serif-thin", Typeface.NORMAL)
        LIGHT_SANS -> resolveSystemTypeface("sans-serif-light", Typeface.NORMAL)
        SMALL_CAPS -> resolveSystemTypeface("sans-serif-smallcaps", Typeface.NORMAL)
        HEAVY_BLACK -> resolveSystemTypeface("sans-serif-black", Typeface.NORMAL)
        SLAB_MONO -> resolveSystemTypeface("serif-monospace", Typeface.NORMAL)
        SCRIPT_CURSIVE -> resolveSystemTypeface("cursive", Typeface.NORMAL)
    }

    companion object {
        private val systemTypefaceCache = mutableMapOf<String, FontFamily>()

        private fun resolveSystemTypeface(familyName: String, style: Int): FontFamily {
            val key = "$familyName:$style"
            return systemTypefaceCache.getOrPut(key) {
                try {
                    FontFamily(Typeface.create(familyName, style))
                } catch (_: Exception) {
                    FontFamily.SansSerif
                }
            }
        }

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
    private var homeIntentTrigger by mutableStateOf(0)
    private var userWallpaperRefreshTrigger by mutableStateOf(0)

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
        dismissKeyboardHard()

        setContent {
            NTextLauncherApp(
                activity = this,
                prefs = prefs,
                packageRefreshTrigger = packageRefreshTrigger,
                homeIntentTrigger = homeIntentTrigger,
                userWallpaperRefreshTrigger = userWallpaperRefreshTrigger,
                onDismissKeyboardHard = { dismissKeyboardHard() },
                onToggleAndroidStatusBar = { hide ->
                    hideDefaultAndroidStatusBar = hide
                    prefs.edit().putBoolean("hide_android_bar", hide).apply()
                    applySystemStatusBarVisibility(hide)
                },
                onExpandNotifications = { expandSystemNotificationsPanel() },
                onRequestDefaultHomeRole = { requestDefaultHomeLauncher() },
                onOpenSystemWallpaperPicker = { openSystemWallpaperChooser() },
                onPickCustomWallpaperImage = { pickCustomUserWallpaperFromGallery() },
                onRequestSystemUninstall = { pkg -> triggerSystemWideAppUninstall(pkg) }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Swiping up from the Android 14 navigation bar or pressing Home dispatches ACTION_MAIN / CATEGORY_HOME
        homeIntentTrigger++
        dismissKeyboardHard()
    }

    override fun onResume() {
        super.onResume()
        applySystemStatusBarVisibility(hideDefaultAndroidStatusBar)
        // Prevent the Android 14 IME from popping up when returning from Recents or closing an app
        dismissKeyboardHard()
        packageRefreshTrigger++
    }

    private fun dismissKeyboardHard() {
        try {
            currentFocus?.clearFocus()
            window.decorView.clearFocus()
            WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(packageBroadcastReceiver)
        } catch (_: Exception) {
        }
    }

    @Suppress("DEPRECATION")
    private fun applySystemStatusBarVisibility(hideStockBar: Boolean) {
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.isNavigationBarContrastEnforced = false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.attributes = window.attributes.apply {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes = window.attributes.apply {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        } catch (_: Exception) {
        }

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
        startActivity(Intent.createChooser(intent, "Select Wallpaper"))
    }

    @Suppress("DEPRECATION")
    private fun pickCustomUserWallpaperFromGallery() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, 3003)
        } catch (_: Exception) {
            try {
                val fallback = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                startActivityForResult(Intent.createChooser(fallback, "Select Wallpaper Image"), 3003)
            } catch (_: Exception) {
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3003 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            try {
                // Decode with bounds check so large camera photos fit smoothly in memory
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOpts)
                }
                var sampleSize = 1
                val maxSide = max(boundsOpts.outWidth, boundsOpts.outHeight)
                while (maxSide / sampleSize > 2400) {
                    sampleSize *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val bmp = contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                }
                if (bmp != null) {
                    val outFile = File(filesDir, "user_custom_wallpaper.jpg")
                    outFile.outputStream().use { out ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    }
                    prefs.edit().putString("wallpaper", "user_image").apply()
                    userWallpaperRefreshTrigger++
                    Toast.makeText(this, "Custom wallpaper applied", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(this, "Could not load selected image", Toast.LENGTH_SHORT).show()
            }
        }
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
    homeIntentTrigger: Int,
    userWallpaperRefreshTrigger: Int,
    onDismissKeyboardHard: () -> Unit,
    onToggleAndroidStatusBar: (Boolean) -> Unit,
    onExpandNotifications: () -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onOpenSystemWallpaperPicker: () -> Unit,
    onPickCustomWallpaperImage: () -> Unit,
    onRequestSystemUninstall: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val lomoFontFamily = remember { getLomoPixelFontFamily(context) }

    // Navigation & App Drawer State (Launcher Settings opened via Long-Tap Menu)
    var currentView by remember { mutableStateOf(LauncherView.HOME_AND_DRAWER) }
    val drawerProgress = remember { Animatable(0f) }
    val isDrawerOpen by remember { derivedStateOf { drawerProgress.value > 0.5f } }
    var focusDrawerSearchOnOpen by remember { mutableStateOf(false) }

    var editMode by remember { mutableStateOf(false) }

    // Long-tap on Home Screen opens the Home Quick Actions Modal
    var showHomeQuickMenu by remember { mutableStateOf(false) }

    // Long-press on an App opens the App Context Menu Modal: Pair(AppEntry, isFromHomeScreen)
    var contextMenuTarget by remember { mutableStateOf<Pair<AppEntry, Boolean>?>(null) }

    // Persisted Preferences (100% Offline · Zero Internet or Unnecessary Permissions)
    var fontId by remember { mutableStateOf(prefs.getString("font_id", "dot") ?: "dot") }
    var textAlignMode by remember { mutableStateOf(prefs.getString("align", "left") ?: "left") }
    var accentHex by remember { mutableStateOf(prefs.getString("accent", "#FF3B00") ?: "#FF3B00") }
    var textSizeSp by remember { mutableStateOf(prefs.getInt("text_size", 20)) }
    var drawerTextSizeSp by remember { mutableStateOf(prefs.getInt("drawer_text_size", 16)) }
    var use24Hour by remember { mutableStateOf(prefs.getBoolean("use_24h_v4", false)) }
    var hideAndroidBar by remember { mutableStateOf(prefs.getBoolean("hide_android_bar", true)) }
    var waveScrubMode by remember { mutableStateOf(prefs.getString("wave_mode", "full") ?: "full") }
    var drawerWaveIntensity by remember { mutableStateOf(prefs.getFloat("drawer_wave_intensity", 1.0f)) }
    var wallpaperPreset by remember {
        val rawWp = prefs.getString("wallpaper", "oled") ?: "oled"
        val migratedWp = when (rawWp) {
            "live_aurora" -> "live_embers"
            "live_rings" -> "live_kepler"
            "live_matrix_pulse" -> "live_starlight"
            "live_ripples" -> "live_quantum"
            "live_synth_grid" -> "live_lissajous"
            else -> rawWp
        }
        mutableStateOf(migratedWp)
    }
    var wallpaperDimPercent by remember { mutableStateOf(prefs.getInt("wp_dim", 0)) }
    // Live Wallpaper Interactive Tuning Variables (Speed, Density, Glow/Opacity, Scale)
    var wpSpeed by remember { mutableStateOf(prefs.getFloat("wp_speed", 1.0f)) }
    var wpDensity by remember { mutableStateOf(prefs.getFloat("wp_density", 1.0f)) }
    var wpOpacity by remember { mutableStateOf(prefs.getFloat("wp_opacity", 1.0f)) }
    var wpScale by remember { mutableStateOf(prefs.getFloat("wp_scale", 1.0f)) }
    // Settings Menu Background Style ("opaque", "transparent", "blurry") & Intensity (0%..100%)
    var settingsBgMode by remember { mutableStateOf(prefs.getString("settings_bg_mode", "opaque") ?: "opaque") }
    var settingsBgIntensity by remember { mutableStateOf(prefs.getInt("settings_bg_intensity", 65)) }

    // Sync wallpaperPreset when user imports a custom image from gallery
    LaunchedEffect(userWallpaperRefreshTrigger) {
        if (userWallpaperRefreshTrigger > 0) {
            wallpaperPreset = prefs.getString("wallpaper", "user_image") ?: "user_image"
        }
    }

    // Toggle Android Window FLAG_SHOW_WALLPAPER when "system_wallpaper" (Third-Party / System Wallpaper) is active
    LaunchedEffect(wallpaperPreset) {
        try {
            if (wallpaperPreset == "system_wallpaper") {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                activity.window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                activity.window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.BLACK))
            }
        } catch (_: Exception) {
        }
    }
    var globeTheme by remember { mutableStateOf(prefs.getString("globe_theme", "retro_holo") ?: "retro_holo") }
    var globeWireframeColor by remember { mutableStateOf(prefs.getString("globe_wf_color", "#FF3B00") ?: "#FF3B00") }
    var globeTilt2dDeg by remember { mutableStateOf(prefs.getFloat("globe_tilt_2d_deg", -3f)) }
    var globeRotXDeg by remember { mutableStateOf(prefs.getFloat("globe_rot_x_deg", 14f)) }
    var globeRotYDeg by remember { mutableStateOf(prefs.getFloat("globe_rot_y_deg", -77f)) }

    // 100% Offline Essential Widgets (Default on Home Screen: Clock & Week Strip; Globe can be added from Widget Manager)
    var activeWidgets by remember {
        val saved = prefs.getString("widgets_order_v8", "clock,week_strip") ?: "clock,week_strip"
        mutableStateOf(
            saved.split(",").filter {
                it.isNotBlank() && it != "telemetry" && it != "blackhole" && it != "weather" && it != "pedometer" && it != "crt_world_map"
            }
        )
    }
    var widgetSizes by remember {
        val saved = prefs.getString(
            "widget_sizes_v7",
            "clock:standard,week_strip:standard,retro_calendar:standard,year_matrix:standard,retro_flip_clock:standard,analog_chrono:standard,precision_chrono:standard,globe:standard,torch:standard,storage:standard,quote:standard,scratchpad:standard,zen:standard"
        ) ?: ""
        val map = saved.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
        mutableStateOf(map)
    }
    // Per-Widget Independent Font Style (changeable per widget in Edit Mode)
    var widgetFonts by remember {
        val saved = prefs.getString(
            "widget_fonts_v1",
            "clock:dot,week_strip:dot,retro_calendar:dot,year_matrix:dot,retro_flip_clock:dot,analog_chrono:dot,precision_chrono:dot,torch:dot,storage:dot,quote:dot,scratchpad:dot,zen:dot"
        ) ?: ""
        val map = saved.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
        mutableStateOf(map)
    }
    var torchEnabled by remember { mutableStateOf(false) }
    var zenActive by remember { mutableStateOf(false) }
    var zenSeconds by remember { mutableStateOf(1500) }
    var quickNote by remember { mutableStateOf(prefs.getString("quick_note", "CMF Minimal Focus") ?: "CMF Minimal Focus") }

    // Offline Hardware RAM & Storage Telemetry State
    var ramStatusText by remember { mutableStateOf("RAM --/-- GB") }
    var storageStatusText by remember { mutableStateOf("ROM --/-- GB") }
    var ramUsageFraction by remember { mutableStateOf(0.35f) }
    var storageUsageFraction by remember { mutableStateOf(0.50f) }

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
                val appFlags = ri.activityInfo?.applicationInfo?.flags ?: 0
                val isSysApp = (appFlags and ApplicationInfo.FLAG_SYSTEM) != 0
                AppEntry(
                    label = label,
                    packageName = pkg,
                    firstLetter = firstCh,
                    isPinned = pinnedPackages.contains(pkg),
                    isHidden = hiddenPackages.contains(pkg),
                    isSystemApp = isSysApp
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

    // Real-Time Offline Clock, Date, Next Alarm, Battery, RAM & ROM
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var currentHourStr by remember { mutableStateOf("4") }
    var currentMinStr by remember { mutableStateOf("22") }
    var currentSecStr by remember { mutableStateOf("00") }
    var currentAmPmStr by remember { mutableStateOf("PM") }
    var currentDateStr by remember { mutableStateOf("THU · 29 OCT") }
    var nextAlarmStr by remember { mutableStateOf("") }
    var batteryPct by remember { mutableStateOf(86) }
    var isCharging by remember { mutableStateOf(false) }

    fun refreshRealtimeSystemTelemetry() {
        val nowMs = System.currentTimeMillis()
        currentTimeMillis = nowMs
        val now = Date(nowMs)
        if (use24Hour) {
            currentHourStr = SimpleDateFormat("HH", Locale.getDefault()).format(now)
            currentMinStr = SimpleDateFormat("mm", Locale.getDefault()).format(now)
            currentAmPmStr = ""
        } else {
            currentHourStr = SimpleDateFormat("h", Locale.getDefault()).format(now)
            currentMinStr = SimpleDateFormat("mm", Locale.getDefault()).format(now)
            currentAmPmStr = SimpleDateFormat("a", Locale.ENGLISH).format(now).uppercase(Locale.ENGLISH)
        }
        currentSecStr = SimpleDateFormat("ss", Locale.getDefault()).format(now)
        currentDateStr = SimpleDateFormat("EEE · dd MMM", Locale.ENGLISH).format(now).uppercase(Locale.ENGLISH)

        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val nextAlarm = am?.nextAlarmClock
        nextAlarmStr = if (nextAlarm != null) {
            val fmt = if (use24Hour) "HH:mm" else "h:mm a"
            val alarmTime = SimpleDateFormat(fmt, Locale.ENGLISH).format(Date(nextAlarm.triggerTime)).uppercase(Locale.ENGLISH)
            "ALARM $alarmTime"
        } else {
            ""
        }

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        if (bm != null) {
            val cap = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            if (cap in 1..100) batteryPct = cap
            isCharging = bm.isCharging
        }

        try {
            val actMgr = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (actMgr != null) {
                val memInfo = ActivityManager.MemoryInfo()
                actMgr.getMemoryInfo(memInfo)
                val totalGb = memInfo.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
                val usedGb = (memInfo.totalMem - memInfo.availMem).toDouble() / (1024.0 * 1024.0 * 1024.0)
                ramStatusText = String.format(Locale.US, "RAM %.1f/%.0f GB", usedGb, totalGb)
                ramUsageFraction = (usedGb / totalGb.coerceAtLeast(1.0)).toFloat().coerceIn(0.05f, 1f)
            }
        } catch (_: Exception) {
        }

        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes.toDouble()
            val availBytes = stat.availableBytes.toDouble()
            val usedBytes = (totalBytes - availBytes).coerceAtLeast(0.0)
            val totalGb = totalBytes / (1000.0 * 1000.0 * 1000.0)
            val usedGb = usedBytes / (1000.0 * 1000.0 * 1000.0)
            storageStatusText = String.format(Locale.US, "ROM %.0f/%.0f GB", usedGb, totalGb)
            storageUsageFraction = (usedBytes / totalBytes.coerceAtLeast(1.0)).toFloat().coerceIn(0.05f, 1f)
        } catch (_: Exception) {
        }
    }

    LaunchedEffect(use24Hour, packageRefreshTrigger) {
        while (true) {
            refreshRealtimeSystemTelemetry()
            delay(1_000L)
        }
    }

    DisposableEffect(context, use24Hour) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        batteryPct = ((level * 100f) / scale).roundToInt().coerceIn(1, 100)
                    }
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
                }
                refreshRealtimeSystemTelemetry()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
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
        resolveLauncherAccentColor(accentHex)
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
        val cleaned = updated.filter {
            it != "blackhole" && it != "telemetry" && it != "weather" && it != "pedometer" && it != "crt_world_map"
        }
        activeWidgets = cleaned
        prefs.edit().putString("widgets_order_v8", cleaned.joinToString(",")).apply()
    }

    fun saveWidgetSize(widgetId: String, size: String) {
        val newMap = widgetSizes.toMutableMap()
        newMap[widgetId] = size
        widgetSizes = newMap.toMap()
        val serialized = newMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        prefs.edit().putString("widget_sizes_v7", serialized).apply()
    }

    fun saveWidgetFont(widgetId: String, wFontId: String) {
        val newMap = widgetFonts.toMutableMap()
        newMap[widgetId] = wFontId
        widgetFonts = newMap.toMap()
        val serialized = newMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        prefs.edit().putString("widget_fonts_v1", serialized).apply()
    }

    fun cycleWidgetSize(widgetId: String) {
        val current = widgetSizes[widgetId] ?: "standard"
        val next = if (
            widgetId == "clock" ||
            widgetId == "retro_flip_clock" ||
            widgetId == "analog_chrono" ||
            widgetId == "precision_chrono" ||
            widgetId == "globe"
        ) {
            when (current) {
                "compact" -> "standard"
                "standard" -> "large"
                "large" -> "xlarge"
                "xlarge" -> "max"
                else -> "compact"
            }
        } else {
            when (current) {
                "compact" -> "standard"
                "standard" -> "large"
                else -> "compact"
            }
        }
        saveWidgetSize(widgetId, next)
    }

    fun moveWidget(widgetId: String, direction: Int) {
        val list = activeWidgets.toMutableList()
        val idx = list.indexOf(widgetId)
        val targetIdx = idx + direction
        if (idx in list.indices && targetIdx in list.indices) {
            val item = list.removeAt(idx)
            list.add(targetIdx, item)
            saveWidgetList(list)
        }
    }

    fun toggleTorchState() {
        torchEnabled = !torchEnabled
        try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val camId = cm?.cameraIdList?.firstOrNull()
            if (camId != null) {
                cm.setTorchMode(camId, torchEnabled)
            }
        } catch (_: Exception) {
        }
    }

    var drawerSearchResetToken by remember { mutableStateOf(0) }

    fun clearAllFocusAndKeyboard() {
        focusDrawerSearchOnOpen = false
        try {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        } catch (_: Exception) {
        }
        onDismissKeyboardHard()
    }

    fun animateDrawerTo(target: Float, initialVelocity: Float = 0f, focusSearch: Boolean = false) {
        if (target <= 0.5f) {
            clearAllFocusAndKeyboard()
            drawerSearchResetToken++
        } else {
            focusDrawerSearchOnOpen = focusSearch
        }
        scope.launch {
            drawerProgress.animateTo(
                targetValue = target,
                initialVelocity = initialVelocity,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow,
                    visibilityThreshold = 0.001f
                )
            )
            drawerProgress.snapTo(target)
            if (target <= 0.05f) {
                clearAllFocusAndKeyboard()
            }
        }
    }

    fun launchApp(pkg: String) {
        clearAllFocusAndKeyboard()
        drawerSearchResetToken++
        context.packageManager.getLaunchIntentForPackage(pkg)?.let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                val dv = activity.window.decorView
                val w = dv.width.coerceAtLeast(1)
                val h = dv.height.coerceAtLeast(1)
                val opts = ActivityOptions.makeScaleUpAnimation(
                    dv,
                    w / 2,
                    h / 2,
                    w / 6,
                    h / 6
                ).toBundle()
                activity.startActivity(intent, opts)
            } catch (_: Exception) {
                context.startActivity(intent)
            }
        }
    }

    fun openClockApp() {
        tickHaptic(heavy = false)
        // 1. Check installed launcher apps for the system / Google / Nothing Clock app
        val clockApp = installedApps.firstOrNull {
            it.packageName.equals("com.google.android.deskclock", ignoreCase = true) ||
                it.packageName.equals("com.android.deskclock", ignoreCase = true) ||
                it.packageName.contains("deskclock", ignoreCase = true) ||
                it.label.equals("Clock", ignoreCase = true) ||
                it.packageName.endsWith(".clock", ignoreCase = true)
        }
        if (clockApp != null) {
            launchApp(clockApp.packageName)
            return
        }
        // 2. Fallback to Android's standard AlarmClock intent
        try {
            val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(alarmIntent)
        } catch (_: Exception) {
        }
    }

    LaunchedEffect(homeIntentTrigger) {
        if (homeIntentTrigger > 0) {
            clearAllFocusAndKeyboard()
            contextMenuTarget = null
            showHomeQuickMenu = false
            editMode = false
            currentView = LauncherView.HOME_AND_DRAWER
            drawerSearchResetToken++
            if (drawerProgress.value > 0f) {
                drawerProgress.snapTo(0f)
            }
        }
    }

    LaunchedEffect(packageRefreshTrigger, currentView) {
        if (currentView != LauncherView.HOME_AND_DRAWER || drawerProgress.value < 0.5f) {
            clearAllFocusAndKeyboard()
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

    val backDispatcher = activity.onBackPressedDispatcher
    DisposableEffect(backDispatcher, currentView, editMode, isDrawerOpen, contextMenuTarget, showHomeQuickMenu) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
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

    // Root Surface with Full-Bleed Edge-to-Edge Wallpaper Engine + Universal Wallpaper Dimmer
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(if (wallpaperPreset == "system_wallpaper") Color.Transparent else Color.Black)
    ) {
        val density = LocalDensity.current
        val screenHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }

        // Full-Screen Edge-to-Edge Wallpaper Canvas
        // When Launcher Settings is open, ONLY the currently applied wallpaper is visible in the background
        // (blurred when settingsBgMode == "blurry", visible when "transparent", or covered when "opaque")!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (currentView == LauncherView.SETTINGS && settingsBgMode == "blurry" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val blurRadiusPx = ((settingsBgIntensity.coerceIn(4, 100) / 100f) * 38.dp.toPx()).coerceAtLeast(1f)
                        renderEffect = AndroidRenderEffect.createBlurEffect(
                            blurRadiusPx,
                            blurRadiusPx,
                            AndroidShader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                }
        ) {
            val isWallpaperScreenVisible = when (currentView) {
                LauncherView.HOME_AND_DRAWER -> drawerProgress.value < 0.995f
                LauncherView.WIDGETS -> false
                LauncherView.SETTINGS -> settingsBgMode != "opaque"
            }
            OledWallpaperCanvas(
                preset = wallpaperPreset,
                accentColor = accentColor,
                dimPercent = wallpaperDimPercent,
                wpSpeed = wpSpeed,
                wpDensity = wpDensity,
                wpOpacity = wpOpacity,
                wpScale = wpScale,
                isScreenVisible = isWallpaperScreenVisible,
                userWallpaperRefreshTrigger = userWallpaperRefreshTrigger
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (editMode && currentView == LauncherView.HOME_AND_DRAWER && drawerProgress.value < 0.2f) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 2.dp)
                        .border(1.dp, Color(0xFF404040), RoundedCornerShape(8.dp))
                        .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EDIT MODE: CUSTOMIZE SIZE, FONT & ORDER",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontFamily = selectedFont
                    )
                    Text(
                        text = "DONE",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontFamily = selectedFont,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(4.dp))
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
                                .graphicsLayer {
                                    val p = drawerProgress.value
                                    if (p >= 0.995f) {
                                        alpha = 0f
                                        translationY = -screenHeightPx * 2f
                                    } else {
                                        alpha = (1f - p * 1.15f).coerceIn(0f, 1f)
                                        translationY = -p * 110.dp.toPx()
                                        val s = 1f - p * 0.045f
                                        scaleX = s
                                        scaleY = s
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            if (drawerProgress.value < 0.1f) {
                                                tickHaptic(heavy = true)
                                                showHomeQuickMenu = true
                                            }
                                        }
                                    )
                                }
                                .pointerInput(screenHeightPx, editMode) {
                                    if (editMode) return@pointerInput
                                    var totalDy = 0f
                                    var notifTriggered = false
                                    val velocityTracker = VelocityTracker()
                                    val dragDistancePx = (screenHeightPx * 0.58f).coerceAtLeast(1f)
                                    detectVerticalDragGestures(
                                        onDragStart = {
                                            totalDy = 0f
                                            notifTriggered = false
                                            velocityTracker.resetTracking()
                                        },
                                        onVerticalDrag = { change, dragAmount ->
                                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                                            change.consume()
                                            totalDy += dragAmount
                                            if (totalDy < 0f || drawerProgress.value > 0.005f) {
                                                val delta = -dragAmount / dragDistancePx
                                                val next = (drawerProgress.value + delta).coerceIn(0f, 1f)
                                                scope.launch { drawerProgress.snapTo(next) }
                                            } else if (totalDy > 95f && !notifTriggered && drawerProgress.value < 0.01f) {
                                                notifTriggered = true
                                                tickHaptic(heavy = false)
                                                onExpandNotifications()
                                            }
                                        },
                                        onDragEnd = {
                                            val vy = velocityTracker.calculateVelocity().y
                                            val normVel = (-vy / dragDistancePx).coerceIn(-6f, 6f)
                                            if (drawerProgress.value > 0.14f || totalDy < -75f || vy < -550f) {
                                                animateDrawerTo(1f, initialVelocity = normVel)
                                            } else if (drawerProgress.value > 0f) {
                                                animateDrawerTo(0f, initialVelocity = normVel)
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
                                widgetSizes = widgetSizes,
                                widgetFonts = widgetFonts,
                                editMode = editMode,
                                currentTimeMillis = currentTimeMillis,
                                currentHourStr = currentHourStr,
                                currentMinStr = currentMinStr,
                                currentSecStr = currentSecStr,
                                currentAmPmStr = currentAmPmStr,
                                currentDateStr = currentDateStr,
                                nextAlarmStr = nextAlarmStr,
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
                                accentColor = accentColor,
                                lomoFontFamily = lomoFontFamily,
                                selectedFont = selectedFont,
                                uppercaseHome = prebuiltFont.uppercaseHome,
                                textAlign = textAlign,
                                textSizeSp = textSizeSp,
                                waveScrubMode = waveScrubMode,
                                globeTheme = globeTheme,
                                globeWireframeColor = globeWireframeColor,
                                globeTilt2dDeg = globeTilt2dDeg,
                                globeRotXDeg = globeRotXDeg,
                                globeRotYDeg = globeRotYDeg,
                                onChangeGlobeTilt2d = { deg ->
                                    val clamped = deg.coerceIn(-180f, 180f)
                                    globeTilt2dDeg = clamped
                                    prefs.edit().putFloat("globe_tilt_2d_deg", clamped).apply()
                                },
                                onChangeGlobeRotX = { deg ->
                                    val clamped = deg.coerceIn(-85f, 85f)
                                    globeRotXDeg = clamped
                                    prefs.edit().putFloat("globe_rot_x_deg", clamped).apply()
                                },
                                onChangeGlobeRotY = { deg ->
                                    val clamped = deg.coerceIn(-180f, 180f)
                                    globeRotYDeg = clamped
                                    prefs.edit().putFloat("globe_rot_y_deg", clamped).apply()
                                },
                                pinnedApps = installedApps.filter { it.isPinned && !it.isHidden },
                                onOpenClockApp = { openClockApp() },
                                onToggleTorch = { toggleTorchState() },
                                onToggleZen = {
                                    zenActive = !zenActive
                                    if (!zenActive) zenSeconds = 1500
                                },
                                onCycleWidgetSize = { id -> cycleWidgetSize(id) },
                                onSetWidgetSize = { id, sz -> saveWidgetSize(id, sz) },
                                onSetWidgetFont = { id, fId -> saveWidgetFont(id, fId) },
                                onMoveWidget = { id, dir -> moveWidget(id, dir) },
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

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val p = drawerProgress.value
                                    if (p <= 0.005f) {
                                        alpha = 0f
                                        translationY = screenHeightPx * 2f
                                    } else {
                                        alpha = (p * 1.25f).coerceIn(0f, 1f)
                                        translationY = (1f - p) * (screenHeightPx * 0.48f)
                                    }
                                }
                        ) {
                            AppDrawerView(
                                apps = installedApps,
                                lomoFontFamily = selectedFont,
                                selectedFont = selectedFont,
                                uppercaseHome = prebuiltFont.uppercaseHome,
                                drawerTextSizeSp = drawerTextSizeSp,
                                accentColor = accentColor,
                                waveScrubMode = waveScrubMode,
                                drawerWaveIntensity = drawerWaveIntensity,
                                screenHeightPx = screenHeightPx,
                                getDrawerProgress = { drawerProgress.value },
                                searchResetToken = drawerSearchResetToken,
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

                    LauncherView.WIDGETS -> {
                        WidgetsManagerView(
                            activeWidgets = activeWidgets,
                            widgetSizes = widgetSizes,
                            accentColor = accentColor,
                            lomoFontFamily = selectedFont,
                            use24Hour = use24Hour,
                            quickNote = quickNote,
                            globeTheme = globeTheme,
                            globeWireframeColor = globeWireframeColor,
                            onToggle24h = {
                                use24Hour = !use24Hour
                                prefs.edit().putBoolean("use_24h_v4", use24Hour).apply()
                            },
                            onChangeQuickNote = {
                                quickNote = it
                                prefs.edit().putString("quick_note", it).apply()
                            },
                            onChangeGlobeTheme = {
                                globeTheme = it
                                prefs.edit().putString("globe_theme", it).apply()
                            },
                            onChangeGlobeWireframeColor = {
                                globeWireframeColor = it
                                prefs.edit().putString("globe_wf_color", it).apply()
                            },
                            onUpdateWidgets = { saveWidgetList(it) },
                            onCycleWidgetSize = { cycleWidgetSize(it) },
                            onSetWidgetSize = { id, sz -> saveWidgetSize(id, sz) },
                            onMoveWidget = { id, dir -> moveWidget(id, dir) },
                            onBack = { currentView = LauncherView.HOME_AND_DRAWER }
                        )
                    }

                    LauncherView.SETTINGS -> {
                        val hasCustomImageFile = remember(userWallpaperRefreshTrigger, wallpaperPreset) {
                            File(context.filesDir, "user_custom_wallpaper.jpg").exists()
                        }
                        LauncherSettingsView(
                            fontId = fontId,
                            textAlignMode = textAlignMode,
                            accentHex = accentHex,
                            textSizeSp = textSizeSp,
                            drawerTextSizeSp = drawerTextSizeSp,
                            use24Hour = use24Hour,
                            hideAndroidBar = hideAndroidBar,
                            waveScrubMode = waveScrubMode,
                            drawerWaveIntensity = drawerWaveIntensity,
                            wallpaperPreset = wallpaperPreset,
                            wallpaperDimPercent = wallpaperDimPercent,
                            hasCustomUserImage = hasCustomImageFile,
                            wpSpeed = wpSpeed,
                            wpDensity = wpDensity,
                            wpOpacity = wpOpacity,
                            wpScale = wpScale,
                            settingsBgMode = settingsBgMode,
                            settingsBgIntensity = settingsBgIntensity,
                            lomoFontFamily = lomoFontFamily,
                            launcherFont = selectedFont,
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
                            onChangeDrawerTextSize = {
                                drawerTextSizeSp = it
                                prefs.edit().putInt("drawer_text_size", it).apply()
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
                                waveScrubMode = it
                                prefs.edit().putString("wave_mode", it).apply()
                            },
                            onChangeDrawerWaveIntensity = {
                                drawerWaveIntensity = it
                                prefs.edit().putFloat("drawer_wave_intensity", it).apply()
                                if (it <= 0.02f && waveScrubMode != "off") {
                                    waveScrubMode = "off"
                                    prefs.edit().putString("wave_mode", "off").apply()
                                } else if (it > 0.02f && waveScrubMode == "off") {
                                    waveScrubMode = "full"
                                    prefs.edit().putString("wave_mode", "full").apply()
                                }
                            },
                            onChangeWallpaperPreset = {
                                wallpaperPreset = it
                                prefs.edit().putString("wallpaper", it).apply()
                            },
                            onChangeWallpaperDim = {
                                wallpaperDimPercent = it
                                prefs.edit().putInt("wp_dim", it).apply()
                            },
                            onChangeWpSpeed = {
                                wpSpeed = it
                                prefs.edit().putFloat("wp_speed", it).apply()
                            },
                            onChangeWpDensity = {
                                wpDensity = it
                                prefs.edit().putFloat("wp_density", it).apply()
                            },
                            onChangeWpOpacity = {
                                wpOpacity = it
                                prefs.edit().putFloat("wp_opacity", it).apply()
                            },
                            onChangeWpScale = {
                                wpScale = it
                                prefs.edit().putFloat("wp_scale", it).apply()
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
                            onChangeSettingsBgMode = {
                                settingsBgMode = it
                                prefs.edit().putString("settings_bg_mode", it).apply()
                            },
                            onChangeSettingsBgIntensity = {
                                settingsBgIntensity = it
                                prefs.edit().putInt("settings_bg_intensity", it).apply()
                            },
                            onOpenSystemWallpaperPicker = {
                                wallpaperPreset = "system_wallpaper"
                                prefs.edit().putString("wallpaper", "system_wallpaper").apply()
                                onOpenSystemWallpaperPicker()
                            },
                            onPickCustomWallpaperImage = onPickCustomWallpaperImage,
                            onRequestDefaultHomeRole = onRequestDefaultHomeRole,
                            onBack = { currentView = LauncherView.HOME_AND_DRAWER }
                        )
                    }
                }
            }
        }

        // ====================================================================
        // FLUID SPRING-ANIMATED LONG-TAP HOME SCREEN "ACTIONS" MENU
        // Smooth 120Hz scale + vertical glide + backdrop fade on BOTH open and close
        // ====================================================================
        val quickMenuProgress by animateFloatAsState(
            targetValue = if (showHomeQuickMenu) 1f else 0f,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "quickMenuFluidAnim"
        )
        if (showHomeQuickMenu || quickMenuProgress > 0.005f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = (0.84f * quickMenuProgress).coerceIn(0f, 0.84f)))
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
                        .graphicsLayer {
                            val p = quickMenuProgress.coerceIn(0f, 1f)
                            alpha = p
                            val s = 0.86f + 0.14f * p
                            scaleX = s
                            scaleY = s
                            translationY = (1f - p) * 26.dp.toPx()
                        }
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
                        Text(
                            text = "Actions",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = selectedFont,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "DONE",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontFamily = selectedFont,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(6.dp))
                                .clickable { showHomeQuickMenu = false }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF1F1F1F))
                    )

                    // 1. Widget Manager
                    QuickMenuButton(
                        label = "Widget Manager",
                        badge = "WIDGETS",
                        accentColor = accentColor,
                        lomoFontFamily = selectedFont,
                        onClick = {
                            showHomeQuickMenu = false
                            currentView = LauncherView.WIDGETS
                        }
                    )

                    // 2. Edit Mode
                    QuickMenuButton(
                        label = if (editMode) "Exit Edit Mode" else "Edit Mode",
                        badge = "EDIT",
                        accentColor = accentColor,
                        lomoFontFamily = selectedFont,
                        onClick = {
                            showHomeQuickMenu = false
                            editMode = !editMode
                        }
                    )

                    // 3. Launcher Settings
                    QuickMenuButton(
                        label = "Launcher Settings",
                        badge = "LAUNCHER",
                        accentColor = accentColor,
                        lomoFontFamily = selectedFont,
                        onClick = {
                            showHomeQuickMenu = false
                            currentView = LauncherView.SETTINGS
                        }
                    )

                    // 4. Phone Settings
                    QuickMenuButton(
                        label = "Phone Settings",
                        badge = "SYSTEM",
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
        val appMenuProgress by animateFloatAsState(
            targetValue = if (contextMenuTarget != null) 1f else 0f,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "appMenuFluidAnim"
        )
        val activeTargetPair = contextMenuTarget ?: if (appMenuProgress > 0.005f) lastContextMenuTarget else null
        activeTargetPair?.let { (targetApp, isFromHome) ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = (0.82f * appMenuProgress).coerceIn(0f, 0.82f)))
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
                        .graphicsLayer {
                            val p = appMenuProgress.coerceIn(0f, 1f)
                            alpha = p
                            val s = 0.86f + 0.14f * p
                            scaleX = s
                            scaleY = s
                            translationY = (1f - p) * 24.dp.toPx()
                        }
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
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = targetApp.label,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = selectedFont,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = targetApp.packageName,
                                color = Color(0xFF737373),
                                fontSize = 10.sp,
                                fontFamily = selectedFont,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "DONE",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontFamily = selectedFont,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(6.dp))
                                .clickable { contextMenuTarget = null }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
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
                            fontFamily = selectedFont
                        )
                        Text(
                            text = pinActionSymbol,
                            color = accentColor,
                            fontSize = 16.sp,
                            fontFamily = selectedFont
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
                            fontFamily = selectedFont
                        )
                        Text(
                            text = "INFO ↗",
                            color = Color(0xFF8A8A8A),
                            fontSize = 10.sp,
                            fontFamily = selectedFont
                        )
                    }

                    // Option 3: Uninstall App (Only shown for user-installed apps; hidden for all system apps)
                    if (!targetApp.isSystemApp) {
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
                                fontFamily = selectedFont
                            )
                            Text(
                                text = "DEL ×",
                                color = Color(0xFFF87171),
                                fontSize = 10.sp,
                                fontFamily = selectedFont
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
            fontFamily = lomoFontFamily
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
    widgetSizes: Map<String, String>,
    widgetFonts: Map<String, String>,
    editMode: Boolean,
    currentTimeMillis: Long,
    currentHourStr: String,
    currentMinStr: String,
    currentSecStr: String,
    currentAmPmStr: String,
    currentDateStr: String,
    nextAlarmStr: String,
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
    accentColor: Color,
    lomoFontFamily: FontFamily,
    selectedFont: FontFamily,
    uppercaseHome: Boolean,
    textAlign: TextAlign,
    textSizeSp: Int,
    waveScrubMode: String,
    globeTheme: String,
    globeWireframeColor: String,
    globeTilt2dDeg: Float,
    globeRotXDeg: Float,
    globeRotYDeg: Float,
    onChangeGlobeTilt2d: (Float) -> Unit,
    onChangeGlobeRotX: (Float) -> Unit,
    onChangeGlobeRotY: (Float) -> Unit,
    pinnedApps: List<AppEntry>,
    onOpenClockApp: () -> Unit,
    onToggleTorch: () -> Unit,
    onToggleZen: () -> Unit,
    onCycleWidgetSize: (String) -> Unit,
    onSetWidgetSize: (String, String) -> Unit,
    onSetWidgetFont: (String, String) -> Unit,
    onMoveWidget: (String, Int) -> Unit,
    onRemoveWidget: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    onLongPressApp: (AppEntry) -> Unit,
    onWaveTick: () -> Unit
) {
    val density = LocalDensity.current
    var waveFloatIndex by remember { mutableStateOf<Float?>(null) }
    var lastHoveredIndex by remember { mutableStateOf<Int?>(null) }

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

    val homeEditScrollState = rememberScrollState()
    val homeWidgetsScrollState = rememberScrollState()
    var expandedFontWidgetIds by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (editMode) {
                    // Edit Mode banner above already consumes statusBars & displayCutout top insets;
                    // use a clean 8.dp top spacing so there is no wide gap below the Edit Mode header,
                    // and enable smooth vertical scrolling across all widgets & pinned apps!
                    Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .verticalScroll(homeEditScrollState)
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 28.dp)
                } else {
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                }
            ),
        verticalArrangement = if (editMode) Arrangement.Top else Arrangement.SpaceBetween
    ) {
        // 1. MODULAR OLED WIDGETS STACK (Scrollable when overflowing; in Edit Mode the entire page scrolls + top control bar per widget)
        Column(
            modifier = if (editMode) {
                Modifier.fillMaxWidth()
            } else {
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(homeWidgetsScrollState)
            },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            activeWidgets.forEach { widgetId ->
                val sizeMode = widgetSizes[widgetId] ?: "standard"
                val wFontId = widgetFonts[widgetId] ?: "dot"
                val wFontFamily = remember(wFontId, lomoFontFamily) {
                    PrebuiltFont.fromId(wFontId).resolveFontFamily(lomoFontFamily)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (editMode) {
                                Modifier
                                    .border(1.dp, Color(0xFF383838), RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            } else Modifier
                        )
                ) {
                    Column {
                        // Top-Anchored Edit Mode Controls: Always accessible at the top of each widget card
                        // so increasing a widget's size (S -> M -> L -> XL -> MAX) never pushes its controls off-screen!
                        if (editMode) {
                            val sizeChoices = when (widgetId) {
                                "clock", "retro_flip_clock", "analog_chrono", "precision_chrono", "globe" -> listOf(
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
                                // Row 1: Reorder (↑ / ↓) + Direct Size Pills (S, M, L, XL, MAX) + Remove (×)
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
                                                color = Color(0xFFD4D4D4),
                                                fontSize = 10.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF141414), RoundedCornerShape(4.dp))
                                                    .clickable { onMoveWidget(widgetId, -1) }
                                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                        if (idx < activeWidgets.lastIndex) {
                                            Text(
                                                text = "↓",
                                                color = Color(0xFFD4D4D4),
                                                fontSize = 10.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF141414), RoundedCornerShape(4.dp))
                                                    .clickable { onMoveWidget(widgetId, 1) }
                                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                        // Direct S, M, L, XL, MAX Size Selector Pills
                                        sizeChoices.forEach { (szId, szLbl) ->
                                            val isSz = sizeMode == szId
                                            Text(
                                                text = szLbl,
                                                color = if (isSz) Color.Black else Color.White,
                                                fontSize = 9.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .border(
                                                        1.dp,
                                                        if (isSz) accentColor else Color(0xFF2E2E2E),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .background(
                                                        if (isSz) accentColor else Color(0xFF151515),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .clickable { onSetWidgetSize(widgetId, szId) }
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "×",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        fontFamily = selectedFont,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            .background(Color(0xFF1A0808), RoundedCornerShape(4.dp))
                                            .clickable { onRemoveWidget(widgetId) }
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }

                                // Row 2: Collapsible Per-Widget Font Style Menu (14 Fonts for all widgets with text)
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
                                                    if (isWidgetFontMenuExpanded) accentColor.copy(alpha = 0.6f) else Color(0xFF282828),
                                                    RoundedCornerShape(5.dp)
                                                )
                                                .background(Color(0xFF111111), RoundedCornerShape(5.dp))
                                                .clickable {
                                                    expandedFontWidgetIds = if (isWidgetFontMenuExpanded) {
                                                        expandedFontWidgetIds - widgetId
                                                    } else {
                                                        expandedFontWidgetIds + widgetId
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "FONT",
                                                    color = Color(0xFF8A8A8A),
                                                    fontSize = 8.sp,
                                                    fontFamily = selectedFont
                                                )
                                                Text(
                                                    text = activeWidgetFont.shortLabel + " · " + activeWidgetFont.displayName.substringBefore(" ("),
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontFamily = wFontFamily,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = if (isWidgetFontMenuExpanded) "HIDE ▲" else "${allWidgetFonts.size} FONTS ▼",
                                                color = accentColor,
                                                fontSize = 8.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold
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
                                                                color = if (isF) Color.Black else Color(0xFFD4D4D4),
                                                                fontSize = 8.sp,
                                                                fontFamily = previewFamily,
                                                                fontWeight = if (isF) FontWeight.Bold else FontWeight.Normal,
                                                                textAlign = TextAlign.Center,
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .border(
                                                                        1.dp,
                                                                        if (isF) Color.White else Color(0xFF282828),
                                                                        RoundedCornerShape(4.dp)
                                                                    )
                                                                    .background(
                                                                        if (isF) Color.White else Color(0xFF121212),
                                                                        RoundedCornerShape(4.dp)
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
                                    // Dedicated Separate X-Axis & Y-Axis Rotation Controls for the 3D Globe Widget in Edit Mode
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFF262626), RoundedCornerShape(6.dp))
                                            .background(Color(0xFF101010), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "GLOBE AXIS TILT & ROTATION",
                                                color = Color(0xFFA3A3A3),
                                                fontSize = 8.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "RESET AXIS",
                                                color = accentColor,
                                                fontSize = 8.sp,
                                                fontFamily = selectedFont,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF181818), RoundedCornerShape(4.dp))
                                                    .clickable {
                                                        onChangeGlobeTilt2d(-3f)
                                                        onChangeGlobeRotX(14f)
                                                        onChangeGlobeRotY(-77f)
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        // X-Axis (2D Screen-Plane Axial Tilt: tilts the globe's spin axis sideways -180° to +180°)
                                        GlobeAxisControlRow(
                                            axisLabel = "X-AXIS (TILT)",
                                            degreeValue = globeTilt2dDeg,
                                            minDeg = -180f,
                                            maxDeg = 180f,
                                            stepDeg = 5f,
                                            accentColor = accentColor,
                                            fontFamily = selectedFont,
                                            onChangeDeg = onChangeGlobeTilt2d
                                        )

                                        // Y-Axis Rotation Control (-180° to +180° Yaw / Longitude)
                                        GlobeAxisControlRow(
                                            axisLabel = "Y-AXIS (SPIN)",
                                            degreeValue = globeRotYDeg,
                                            minDeg = -180f,
                                            maxDeg = 180f,
                                            stepDeg = 10f,
                                            accentColor = accentColor,
                                            fontFamily = selectedFont,
                                            onChangeDeg = onChangeGlobeRotY
                                        )

                                        // Polar Inclination Control (-85° to +85° North/South Pitch)
                                        GlobeAxisControlRow(
                                            axisLabel = "POLAR (NOD)",
                                            degreeValue = globeRotXDeg,
                                            minDeg = -85f,
                                            maxDeg = 85f,
                                            stepDeg = 5f,
                                            accentColor = accentColor,
                                            fontFamily = selectedFont,
                                            onChangeDeg = onChangeGlobeRotX
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFF1F1F1F))
                                )
                            }
                        }

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
                                    when (sizeMode) {
                                        "compact" -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.clickable { onOpenClockApp() }
                                            ) {
                                                Text(
                                                    text = "$currentHourStr:$currentMinStr $currentAmPmStr".trim(),
                                                    color = Color.White,
                                                    fontSize = 24.sp,
                                                    fontFamily = wFontFamily
                                                )
                                                Text(
                                                    text = "·",
                                                    color = accentColor,
                                                    fontSize = 12.sp,
                                                    fontFamily = wFontFamily
                                                )
                                                Text(
                                                    text = "$currentDateStr · $batteryPct%",
                                                    color = Color(0xFFA3A3A3),
                                                    fontSize = 11.sp,
                                                    fontFamily = wFontFamily
                                                )
                                            }
                                        }
                                        "large", "xlarge", "max" -> {
                                            val digitSp = when (sizeMode) {
                                                "max" -> 96.sp
                                                "xlarge" -> 76.sp
                                                else -> 58.sp
                                            }
                                            val amPmSp = when (sizeMode) {
                                                "max" -> 17.sp
                                                "xlarge" -> 15.sp
                                                else -> 13.sp
                                            }
                                            val amPmBottomPad = when (sizeMode) {
                                                "max" -> 12.dp
                                                "xlarge" -> 9.dp
                                                else -> 7.dp
                                            }
                                            val metaSp = when (sizeMode) {
                                                "max" -> 13.sp
                                                "xlarge" -> 12.sp
                                                else -> 11.sp
                                            }
                                            val barHeight = when (sizeMode) {
                                                "max" -> 10.dp
                                                "xlarge" -> 9.dp
                                                else -> 8.dp
                                            }
                                            Row(
                                                verticalAlignment = Alignment.Bottom,
                                                modifier = Modifier.clickable { onOpenClockApp() }
                                            ) {
                                                Text(
                                                    text = currentHourStr,
                                                    color = Color.White,
                                                    fontSize = digitSp,
                                                    fontFamily = wFontFamily
                                                )
                                                Text(
                                                    text = ":",
                                                    color = accentColor.copy(alpha = colonAlpha),
                                                    fontSize = digitSp,
                                                    fontFamily = wFontFamily
                                                )
                                                Text(
                                                    text = currentMinStr,
                                                    color = Color.White,
                                                    fontSize = digitSp,
                                                    fontFamily = wFontFamily
                                                )
                                                if (currentAmPmStr.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = currentAmPmStr,
                                                        color = Color(0xFF737373),
                                                        fontSize = amPmSp,
                                                        fontFamily = wFontFamily,
                                                        modifier = Modifier.padding(bottom = amPmBottomPad)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = currentDateStr,
                                                    color = Color(0xFFA3A3A3),
                                                    fontSize = metaSp,
                                                    fontFamily = wFontFamily,
                                                    letterSpacing = 1.2.sp
                                                )
                                                if (nextAlarmStr.isNotBlank()) {
                                                    Text(
                                                        text = "·",
                                                        color = accentColor,
                                                        fontSize = metaSp,
                                                        fontFamily = wFontFamily,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = nextAlarmStr,
                                                        color = Color(0xFF737373),
                                                        fontSize = metaSp,
                                                        fontFamily = wFontFamily,
                                                        letterSpacing = 1.2.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(Color(0xFF171717))
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(
                                                    text = "$batteryPct%",
                                                    color = Color(0xFFD4D4D4),
                                                    fontSize = metaSp,
                                                    fontFamily = wFontFamily,
                                                    letterSpacing = 0.8.sp
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(barHeight)
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
                                                if (isCharging) {
                                                    Text(
                                                        text = "⚡",
                                                        color = accentColor,
                                                        fontSize = metaSp,
                                                        fontFamily = wFontFamily
                                                    )
                                                }
                                            }
                                        }
                                        else -> {
                                            Row(
                                                verticalAlignment = Alignment.Bottom,
                                                modifier = Modifier.clickable { onOpenClockApp() }
                                            ) {
                                                Text(
                                                    text = currentHourStr,
                                                    color = Color.White,
                                                    fontSize = 42.sp,
                                                    fontFamily = wFontFamily
                                                )
                                                Text(
                                                    text = ":",
                                                    color = accentColor.copy(alpha = colonAlpha),
                                                    fontSize = 42.sp,
                                                    fontFamily = wFontFamily
                                                )
                                                Text(
                                                    text = currentMinStr,
                                                    color = Color.White,
                                                    fontSize = 42.sp,
                                                    fontFamily = wFontFamily
                                                )
                                                if (currentAmPmStr.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = currentAmPmStr,
                                                        color = Color(0xFF737373),
                                                        fontSize = 11.sp,
                                                        fontFamily = wFontFamily,
                                                        modifier = Modifier.padding(bottom = 5.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = currentDateStr,
                                                    color = Color(0xFFA3A3A3),
                                                    fontSize = 10.sp,
                                                    fontFamily = wFontFamily,
                                                    letterSpacing = 1.2.sp
                                                )
                                                if (nextAlarmStr.isNotBlank()) {
                                                    Text(
                                                        text = "·",
                                                        color = accentColor,
                                                        fontSize = 10.sp,
                                                        fontFamily = wFontFamily,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = nextAlarmStr,
                                                        color = Color(0xFF737373),
                                                        fontSize = 10.sp,
                                                        fontFamily = wFontFamily,
                                                        letterSpacing = 1.2.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(Color(0xFF171717))
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(
                                                    text = "$batteryPct%",
                                                    color = Color(0xFFD4D4D4),
                                                    fontSize = 10.sp,
                                                    fontFamily = wFontFamily,
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
                                                if (isCharging) {
                                                    Text(
                                                        text = "⚡",
                                                        color = accentColor,
                                                        fontSize = 9.sp,
                                                        fontFamily = wFontFamily
                                                    )
                                                }
                                            }
                                        }
                                    }
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
                                    accentColor = accentColor,
                                    lomoFontFamily = wFontFamily,
                                    onOpenClockApp = onOpenClockApp
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
                                    accentColor = accentColor,
                                    lomoFontFamily = wFontFamily,
                                    onOpenClockApp = onOpenClockApp
                                )
                            }
                            "precision_chrono" -> {
                                PrecisionChronographWidget(
                                    sizeMode = sizeMode,
                                    accentColor = accentColor,
                                    lomoFontFamily = wFontFamily,
                                    onTick = onWaveTick
                                )
                            }
                            "retro_calendar" -> {
                                RetroDotMatrixCalendarWidget(
                                    currentTimeMillis = currentTimeMillis,
                                    sizeMode = sizeMode,
                                    accentColor = accentColor,
                                    lomoFontFamily = wFontFamily
                                )
                            }
                            "week_strip" -> {
                                RetroWeekTimelineStripWidget(
                                    currentTimeMillis = currentTimeMillis,
                                    sizeMode = sizeMode,
                                    accentColor = accentColor,
                                    lomoFontFamily = wFontFamily
                                )
                            }
                            "year_matrix" -> {
                                RetroYearDotMatrixWidget(
                                    currentTimeMillis = currentTimeMillis,
                                    sizeMode = sizeMode,
                                    accentColor = accentColor,
                                    lomoFontFamily = wFontFamily
                                )
                            }
                            "torch" -> {
                                val padV = when (sizeMode) {
                                    "compact" -> 5.dp
                                    "large" -> 11.dp
                                    else -> 7.dp
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleTorch() }
                                        .border(1.dp, if (torchEnabled) accentColor else Color(0xFF242424), RoundedCornerShape(8.dp))
                                        .background(if (torchEnabled) accentColor.copy(alpha = 0.12f) else Color(0xFF060606), RoundedCornerShape(8.dp))
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
                                            color = if (torchEnabled) accentColor else Color(0xFFA3A3A3),
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
                                        color = if (torchEnabled) accentColor else Color(0xFF737373),
                                        fontSize = 10.sp,
                                        fontFamily = wFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            "storage" -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
                                        .background(Color(0xFF060606), RoundedCornerShape(8.dp))
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = when (sizeMode) {
                                                "compact" -> 5.dp
                                                "large" -> 10.dp
                                                else -> 7.dp
                                            }
                                        )
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
                                        Spacer(modifier = Modifier.height(if (sizeMode == "large") 7.dp else 5.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(if (sizeMode == "large") 6.dp else 4.dp)
                                                    .background(Color(0xFF141414), RoundedCornerShape(999.dp))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(ramUsageFraction.coerceIn(0.05f, 1f))
                                                        .background(accentColor, RoundedCornerShape(999.dp))
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(if (sizeMode == "large") 6.dp else 4.dp)
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
                            "quote" -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                        .background(Color(0xFF060606), RoundedCornerShape(8.dp))
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = when (sizeMode) {
                                                "compact" -> 5.dp
                                                "large" -> 10.dp
                                                else -> 7.dp
                                            }
                                        )
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
                            "zen" -> {
                                val mm = (zenSeconds / 60).toString().padStart(2, '0')
                                val ss = (zenSeconds % 60).toString().padStart(2, '0')
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF262626), RoundedCornerShape(8.dp))
                                        .background(Color(0xFF060606), RoundedCornerShape(8.dp))
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = when (sizeMode) {
                                                "compact" -> 5.dp
                                                "large" -> 10.dp
                                                else -> 7.dp
                                            }
                                        ),
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
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontFamily = wFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { onToggleZen() }
                                    )
                                }
                            }
                            "scratchpad" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
                                        .background(Color(0xFF060606), RoundedCornerShape(8.dp))
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = when (sizeMode) {
                                                "compact" -> 5.dp
                                                "large" -> 10.dp
                                                else -> 7.dp
                                            }
                                        )
                                ) {
                                    Text(
                                        text = "> $quickNote",
                                        color = Color(0xFFD4D4D4),
                                        fontSize = if (sizeMode == "large") 13.sp else 11.sp,
                                        fontFamily = wFontFamily
                                    )
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(globeHeight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    RetroFuturisticGlobeView(
                                        globeTheme = globeTheme,
                                        globeWireframeColor = globeWireframeColor,
                                        axisTilt2dDeg = globeTilt2dDeg,
                                        axisRotXDeg = globeRotXDeg,
                                        axisRotYDeg = globeRotYDeg,
                                        interactive = !editMode,
                                        onTick = onWaveTick
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. SPACER BETWEEN WIDGETS AND PINNED APPS
        if (editMode) {
            Spacer(modifier = Modifier.height(20.dp))
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. PINNED HOME APPS + 1:1 MATCHED RIGHT-SIDE DOTS WAVE SCROLLER (Anchored at Bottom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            val waveRadiusRows = 2.25f
            val maxWaveShiftPx = with(density) { (if (waveScrubMode == "full") 32.dp else 16.dp).toPx() }
            val verticalSpreadPx = with(density) { 4.dp.toPx() }
            val pressHoverShiftPx = with(density) { 14.dp.toPx() }
            val maxDotWaveShiftPx = with(density) { 14.dp.toPx() }

            val activeFocusedIndex = remember(waveFloatIndex, pinnedApps.size) {
                val w = waveFloatIndex
                if (w == null || pinnedApps.isEmpty() || waveScrubMode == "off") {
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
                // Left/Main Column: Pinned Home Screen Apps
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    pinnedApps.forEachIndexed { index, app ->
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        val targetWaveFactor = if (waveFloatIndex != null && waveScrubMode != "off") {
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
                                color = if (isHighlighted) accentColor else if (animWave > 0.35f) Color.White else Color(0xFFF5F5F5),
                                fontSize = textSizeSp.sp,
                                fontFamily = selectedFont,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Right-Side Dots Scroller: Exact same count as pinnedApps.size, aligned 1-to-1 with each app row!
                if (waveScrubMode != "off" && pinnedApps.isNotEmpty()) {
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
                                                    // Finger moved away from the dots strip -> collapse wave & cancel app launch!
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
                                                isDotFocused -> accentColor
                                                animDotWave > 0.35f -> Color.White
                                                else -> Color(0xFF525252)
                                            },
                                            shape = CircleShape
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

// ============================================================================
// ULTRA-HIGH-PRECISION 3D EARTH MODEL & CARTOGRAPHIC DATASET (v3.3.0-cmf)
// - 360+ Exact Polygon Rings (11,700+ Satellite-Derived Natural Earth + Survey of India
//   + Island Archipelagos & Great Lakes Coordinates at 0.01° Precision)
// - Precomputed 3D Unit-Sphere Mesh Vectors (FloatArray) for 120Hz Zero-Trig Projection
// - Exact 3D Spherical Horizon Limb Clipping + Lambertian Surface Normal Shading
// - Interactive 360° Trackball Spin + Double-Tap Zoom (1.0x -> 1.75x -> 2.65x)
// ============================================================================
private val WORLD_MAP_RAW_POLYGONS: Array<String> = arrayOf(
    "-16.07,180.00;-16.56,180.00;-16.80,179.36;-17.01,178.73;-16.64,178.60;-16.43,179.10;-16.38,179.41;-16.07,180.00",
    "-17.50,178.13;-17.34,178.37;-17.63,178.72;-18.15,178.55;-18.29,177.93;-18.16,177.38;-17.72,177.29;-17.38,177.67;-17.50,178.13",
    "-16.02,-179.79;-16.50,-179.92;-16.56,-180.00;-16.07,-180.00;-16.02,-179.79",
    "-0.95,33.90;-1.06,34.07;-3.10,37.70;-3.68,37.77;-4.68,39.20;-5.91,38.74;-6.48,38.80;-6.84,39.44;-7.10,39.47;-7.70,39.19;-8.01,39.25;-8.49,39.19;-9.11,39.54;-10.10,39.95;-10.32,40.32;-10.32,40.32;-10.90,39.52;-11.29,38.43;-11.27,37.83;-11.57,37.47;-11.59,36.78;-11.72,36.51;-11.44,35.31;-11.52,34.56;-10.16,34.28;-9.69,33.94;-9.42,33.74;-9.23,32.76;-8.93,32.19;-8.76,31.56;-8.59,31.16;-8.34,30.74;-8.34,30.74;-7.08,30.20;-6.52,29.62;-5.94,29.42;-5.42,29.52;-4.50,29.34;-4.45,29.75;-4.09,30.12;-3.57,30.51;-3.36,30.75;-3.03,30.74;-2.81,30.53;-2.41,30.47;-2.41,30.47;-2.29,30.76;-1.70,30.82;-1.13,30.42;-1.01,30.77;-1.03,31.87;-0.95,33.90",
    "27.66,-8.67;27.59,-8.67;27.40,-8.68;25.88,-8.69;25.93,-11.97;23.37,-11.94;23.28,-12.87;22.77,-13.12;21.33,-12.93;21.33,-16.85;21.00,-17.06;21.42,-17.02;21.42,-17.00;21.50,-14.75;21.86,-14.63;22.31,-14.22;23.69,-13.89;24.77,-12.50;26.03,-12.03;26.10,-11.72;26.88,-11.39;26.99,-10.55;26.86,-10.19;26.86,-9.74;27.09,-9.41;27.12,-8.79;27.66,-8.82;27.66,-8.67",
    "49.00,-122.84;49.00,-122.97;49.98,-124.91;50.42,-125.62;50.83,-127.44;51.72,-127.99;52.33,-127.85;52.76,-129.13;53.56,-129.31;54.29,-130.51;54.80,-130.54;54.80,-130.54;55.29,-129.98;55.92,-130.01;56.55,-131.71;57.69,-132.73;58.41,-133.36;58.86,-134.27;59.27,-134.95;59.79,-135.48;59.46,-136.48;58.91,-137.45;59.56,-138.34;60.00,-139.04;60.28,-140.01;60.31,-141.00;66.00,-140.99;69.71,-140.99;69.71,-140.99;69.47,-139.12;68.99,-137.55;68.90,-136.50;69.32,-135.63;69.63,-134.41;69.51,-132.93;69.94,-131.43;70.19,-129.79;69.78,-129.11;70.01,-128.36;70.48,-128.14;70.38,-127.45;69.48,-125.76;70.16,-124.42;69.40,-124.29;69.56,-123.06;69.86,-122.68;69.80,-121.47;69.38,-119.94;69.01,-117.60;68.84,-116.23;68.91,-115.25;68.40,-113.90;67.90,-115.30;67.69,-113.50;67.81,-110.80;67.98,-109.95;67.38,-108.88;67.89,-107.79;68.31,-108.81;68.65,-108.17;68.70,-106.95;68.80,-106.15;68.56,-105.34;68.02,-104.34;68.10,-103.22;67.65,-101.45;67.81,-99.90;67.78,-98.44;68.40,-98.56;68.58,-97.67;68.24,-96.12;67.29,-96.13;68.09,-95.49;68.06,-94.69;69.07,-94.23;69.69,-95.30;70.09,-96.47;71.19,-96.39;71.92,-95.21;71.76,-93.89;71.32,-92.88;70.19,-91.52;69.70,-92.41;69.50,-90.55;68.47,-90.55;69.26,-89.22;68.62,-88.02;67.87,-88.32;67.20,-87.35;67.92,-86.31;68.78,-85.58;69.88,-85.52;69.81,-84.10;69.66,-82.62;69.16,-81.28;68.67,-81.22;68.13,-81.96;67.60,-81.26;67.11,-81.39;66.41,-83.34;66.26,-84.74;66.56,-85.77;66.06,-86.07;65.21,-87.03;64.78,-87.32;64.10,-88.48;64.03,-89.91;63.61,-90.70;62.96,-90.77;62.84,-91.93;62.02,-93.16;60.90,-94.24;60.11,-94.63;58.95,-94.68;58.78,-93.22;57.85,-92.76;57.09,-92.30;57.28,-90.90;56.85,-89.04;56.47,-88.04;56.00,-87.32;55.72,-86.07;55.30,-85.01;55.24,-83.36;55.15,-82.27;54.28,-82.44;53.28,-82.13;52.16,-81.40;51.21,-79.91;51.53,-79.14;52.56,-78.60;54.14,-79.12;54.67,-79.83;55.14,-78.23;55.84,-77.10;56.53,-76.54;57.20,-76.62;58.05,-77.30;58.80,-78.52;59.85,-77.34;60.76,-77.77;62.32,-78.11;62.55,-77.41;62.28,-75.70;62.18,-74.67;62.44,-73.84;62.11,-72.91;61.53,-71.68;61.14,-71.37;61.06,-69.59;60.22,-69.62;58.96,-69.29;58.80,-68.37;58.21,-67.65;58.77,-66.20;59.87,-65.25;60.34,-64.58;59.44,-63.80;58.17,-62.50;56.97,-61.40;56.34,-61.80;55.78,-60.47;55.20,-59.57;54.95,-57.98;54.63,-57.33;53.78,-56.94;53.65,-56.16;53.27,-55.76;52.15,-55.68;51.77,-56.41;51.42,-57.13;51.06,-58.77;50.24,-60.03;50.08,-61.72;50.29,-63.86;50.30,-65.36;50.23,-66.40;49.51,-67.24;49.07,-68.51;47.74,-69.95;46.82,-71.10;46.99,-70.26;48.30,-68.65;49.13,-66.55;49.23,-65.06;48.74,-64.17;48.07,-65.12;46.99,-64.80;46.24,-64.47;45.74,-63.17;45.88,-61.52;47.01,-60.52;46.28,-60.45;45.92,-59.80;45.27,-61.04;44.67,-63.25;44.27,-64.25;43.55,-65.36;43.62,-66.12;44.47,-66.16;45.29,-64.43;45.26,-66.03;45.14,-67.14;45.70,-67.79;47.07,-67.79;47.35,-68.23;47.19,-68.91;47.45,-69.24;46.69,-70.00;45.91,-70.31;45.46,-70.66;45.31,-71.08;45.25,-71.41;45.01,-71.51;45.01,-73.35;45.00,-74.87;44.82,-75.32;44.10,-76.38;44.02,-76.50;43.63,-76.82;43.63,-77.74;43.63,-78.72;43.47,-79.17;43.27,-79.01;42.96,-78.92;42.86,-78.94;42.37,-80.25;42.21,-81.28;41.68,-82.44;41.68,-82.69;41.83,-83.03;41.98,-83.14;42.08,-83.12;42.43,-82.90;42.98,-82.43;43.57,-82.14;44.44,-82.34;45.35,-82.55;45.82,-83.59;45.99,-83.47;46.12,-83.62;46.12,-83.89;46.28,-84.09;46.51,-84.14;46.41,-84.34;46.44,-84.60;46.54,-84.54;46.64,-84.78;46.90,-84.88;47.22,-85.65;47.55,-86.46;47.94,-87.44;48.30,-88.38;48.02,-89.27;48.01,-89.60;48.27,-90.83;48.14,-91.64;48.45,-92.61;48.61,-93.63;48.67,-94.33;48.84,-94.64;49.39,-94.82;49.38,-95.16;49.00,-95.16;49.00,-97.23;49.00,-100.65;49.00,-104.05;49.00,-107.05;49.00,-110.05;49.00,-113.00;49.00,-116.05;49.00,-117.03;49.00,-120.00;49.00,-122.84",
    "62.45,-83.99;62.91,-83.25;62.90,-81.88;62.71,-81.90;62.16,-83.07;62.18,-83.77;62.45,-83.99",
    "72.80,-79.78;73.33,-80.88;73.69,-80.83;73.76,-80.35;73.65,-78.06;73.10,-76.34;72.83,-76.25;72.86,-77.31;72.88,-78.39;72.74,-79.49;72.80,-79.78",
    "62.09,-80.32;62.39,-79.93;62.36,-79.52;62.16,-79.27;61.63,-79.66;61.72,-80.10;62.02,-80.36;62.09,-80.32",
    "74.98,-93.61;74.59,-94.16;74.67,-95.61;74.93,-96.82;75.38,-96.29;75.65,-94.85;75.30,-93.98;74.98,-93.61",
    "77.52,-93.84;77.49,-94.30;77.56,-96.17;77.83,-96.44;77.82,-94.42;77.63,-93.72;77.52,-93.84",
    "78.77,-96.75;78.42,-95.56;78.06,-95.83;77.85,-97.31;78.08,-98.12;78.46,-98.55;78.87,-98.63;78.83,-97.34;78.77,-96.75",
    "74.39,-88.15;74.52,-89.76;74.84,-92.42;75.39,-92.77;75.88,-92.89;76.32,-93.89;76.44,-95.96;76.75,-97.12;77.16,-96.75;77.10,-94.68;76.78,-93.57;76.78,-91.61;76.45,-90.74;76.07,-90.97;75.85,-89.82;75.61,-89.19;75.57,-87.84;75.48,-86.38;75.70,-84.79;75.78,-82.75;75.71,-81.13;75.34,-80.06;74.92,-79.83;74.66,-80.46;74.44,-81.95;74.56,-83.23;74.41,-86.10;74.39,-88.15",
    "78.15,-111.26;78.00,-109.85;77.70,-110.19;77.41,-112.05;77.73,-113.53;78.05,-112.72;78.15,-111.26",
    "78.80,-110.96;78.60,-109.66;78.41,-110.88;78.41,-112.54;78.55,-112.53;78.85,-111.50;78.80,-110.96",
    "51.32,-55.60;50.69,-56.13;49.81,-56.80;50.15,-56.14;49.94,-55.47;49.59,-55.82;49.31,-54.94;49.56,-54.47;49.25,-53.48;48.52,-53.79;48.69,-53.09;48.16,-52.96;47.54,-52.65;46.66,-53.07;46.62,-53.52;46.81,-54.18;47.63,-53.96;47.75,-54.24;46.88,-55.40;46.92,-56.00;47.39,-55.29;47.63,-56.25;47.57,-57.33;47.60,-59.27;47.90,-59.42;48.25,-58.80;48.52,-59.23;49.13,-58.39;50.72,-57.36;51.29,-56.74;51.63,-55.87;51.59,-55.41;51.32,-55.60",
    "65.11,-83.88;64.77,-82.79;64.46,-81.64;63.98,-81.55;64.06,-80.82;63.73,-80.10;63.41,-80.99;63.65,-82.55;64.10,-83.11;63.57,-84.10;63.05,-85.52;63.64,-85.87;63.54,-87.22;64.04,-86.35;64.82,-86.22;65.74,-85.88;65.66,-85.16;65.22,-84.98;65.37,-84.46;65.11,-83.88",
    "72.35,-78.77;72.75,-77.82;72.24,-75.61;71.77,-74.23;71.33,-74.10;71.56,-72.24;70.92,-71.20;70.53,-68.79;70.12,-67.91;69.19,-66.97;68.72,-68.81;68.07,-66.45;67.85,-64.86;66.93,-63.42;66.86,-61.85;66.16,-62.16;65.00,-63.92;65.43,-65.15;66.39,-66.72;66.26,-68.02;65.69,-68.14;65.11,-67.09;64.65,-65.73;64.38,-65.32;63.39,-64.67;62.67,-65.01;62.95,-66.28;63.75,-68.78;62.88,-67.37;62.28,-66.33;61.93,-66.17;62.33,-68.88;62.91,-71.02;63.40,-72.24;63.68,-71.89;64.19,-73.38;64.68,-74.83;64.39,-74.82;64.23,-77.71;64.57,-78.56;65.31,-77.90;65.33,-76.02;65.45,-73.96;65.81,-74.29;66.31,-73.94;67.28,-72.65;67.73,-72.93;68.07,-73.31;68.55,-74.84;68.89,-76.87;69.15,-76.23;69.77,-77.29;69.83,-78.17;70.17,-78.96;69.87,-79.49;69.74,-81.31;69.97,-84.94;70.26,-87.06;70.41,-88.68;70.76,-89.51;71.22,-88.47;71.22,-89.89;72.24,-90.21;73.13,-89.44;73.54,-88.41;73.80,-85.83;73.16,-86.56;72.53,-85.77;73.34,-84.85;73.75,-82.32;72.72,-80.60;72.06,-80.75;72.35,-78.77",
    "74.13,-94.50;74.10,-92.42;73.86,-90.51;72.97,-92.00;72.77,-93.20;72.02,-94.27;72.06,-95.41;72.94,-96.03;73.44,-96.02;73.86,-95.50;74.13,-94.50",
    "76.12,-122.85;76.12,-122.85;76.86,-121.16;77.51,-119.10;77.50,-117.57;77.65,-116.20;76.88,-116.34;76.53,-117.11;76.48,-118.04;76.05,-119.90;75.90,-121.50;76.12,-122.85",
    "54.04,-132.71;54.12,-131.75;52.98,-132.05;52.18,-131.18;52.18,-131.58;52.64,-132.18;53.10,-132.55;53.41,-133.05;53.85,-133.24;54.17,-133.18;54.04,-132.71",
    "79.30,-105.49;79.17,-103.53;78.80,-100.83;78.32,-100.06;77.91,-99.67;78.02,-101.30;78.34,-102.95;78.38,-105.18;78.68,-104.21;78.92,-105.42;79.30,-105.49",
    "48.51,-123.51;48.37,-124.01;48.83,-125.66;49.18,-125.95;49.53,-126.85;49.81,-127.03;49.99,-128.06;50.54,-128.44;50.77,-128.36;50.55,-127.31;50.40,-126.70;50.30,-125.76;49.95,-125.42;49.48,-124.92;49.06,-123.92;48.51,-123.51",
    "74.45,-121.54;74.24,-120.11;74.19,-117.56;73.90,-116.58;73.48,-115.51;73.22,-116.77;72.52,-119.22;71.82,-120.46;71.38,-120.46;70.90,-123.09;71.34,-123.62;71.87,-125.93;72.29,-125.50;73.02,-124.81;73.68,-123.94;74.29,-124.92;74.45,-121.54",
    "75.85,-107.82;76.01,-106.93;75.97,-105.88;75.48,-105.70;75.01,-106.31;74.85,-109.70;74.42,-112.22;74.39,-113.74;74.72,-113.87;75.16,-111.79;75.04,-116.31;75.22,-117.71;76.20,-116.35;76.48,-115.40;76.14,-112.59;75.55,-110.81;75.47,-109.07;76.43,-110.50;76.79,-109.58;76.68,-108.55;76.20,-108.21;75.85,-107.82",
    "73.08,-106.52;72.67,-105.40;71.70,-104.77;70.99,-104.46;70.50,-102.79;70.02,-100.98;69.58,-101.09;69.50,-102.73;69.12,-102.09;68.75,-102.43;68.91,-104.24;69.18,-105.96;69.12,-107.12;68.78,-109.00;68.63,-111.53;68.54,-113.31;69.01,-113.85;69.28,-115.22;69.17,-116.11;69.96,-117.34;70.07,-116.67;70.24,-115.13;70.19,-113.72;70.37,-112.42;70.60,-114.35;70.52,-116.49;70.54,-117.90;70.91,-118.43;71.31,-116.11;71.30,-117.66;71.56,-119.40;72.31,-118.56;72.71,-117.87;73.31,-115.19;73.12,-114.17;72.65,-114.67;72.96,-112.44;72.45,-111.05;72.96,-109.92;72.63,-109.01;71.65,-108.19;72.07,-107.69;73.09,-108.40;73.24,-107.52;73.08,-106.52",
    "72.71,-100.44;73.36,-101.54;73.84,-100.36;73.63,-99.16;73.76,-97.38;73.47,-97.12;72.99,-98.05;72.56,-96.54;71.66,-96.72;71.27,-98.36;71.36,-99.32;71.74,-100.01;72.51,-102.50;72.83,-102.48;72.71,-100.44",
    "73.60,-106.60;73.64,-105.26;73.42,-104.50;72.76,-105.38;73.46,-106.94;73.60,-106.60",
    "76.72,-98.50;76.26,-97.74;75.74,-97.70;75.00,-98.16;74.90,-99.81;75.06,-100.88;75.64,-100.86;75.56,-102.50;76.34,-102.57;76.31,-101.49;76.65,-99.98;76.59,-98.58;76.72,-98.50",
    "80.60,-96.02;80.91,-95.32;80.98,-94.30;81.21,-94.74;81.26,-92.41;80.72,-91.13;80.51,-89.45;80.32,-87.81;79.66,-87.02;79.34,-85.81;79.04,-87.19;78.29,-89.04;78.22,-90.80;78.34,-92.88;78.75,-93.95;79.11,-93.94;79.38,-93.15;79.37,-94.97;79.71,-96.08;80.16,-96.71;80.60,-96.02",
    "81.89,-91.59;82.09,-90.10;82.12,-88.93;82.28,-86.97;82.65,-85.50;82.60,-84.26;82.32,-83.18;82.86,-82.42;83.02,-81.10;83.13,-79.31;83.17,-76.25;83.06,-75.72;83.23,-72.83;83.17,-70.67;83.11,-68.50;83.03,-65.83;82.90,-63.68;82.63,-61.85;82.36,-61.89;81.93,-64.33;81.73,-66.75;81.50,-67.66;81.51,-65.48;80.90,-67.84;80.62,-69.47;79.80,-71.18;79.63,-73.24;79.43,-73.88;79.32,-76.91;79.20,-75.53;79.02,-76.22;78.53,-75.39;78.18,-76.34;77.90,-77.89;77.51,-78.36;77.21,-79.76;76.98,-79.62;77.02,-77.91;76.78,-77.89;76.18,-80.56;76.45,-83.17;76.30,-86.11;76.42,-87.60;76.47,-89.49;76.95,-89.62;77.18,-87.77;77.90,-88.26;77.97,-87.65;77.54,-84.98;78.18,-86.34;78.37,-87.96;78.76,-87.15;79.00,-85.38;79.35,-85.09;79.74,-86.51;80.25,-86.93;80.21,-84.20;80.10,-83.41;80.46,-81.85;80.58,-84.10;80.52,-87.60;80.86,-89.37;81.26,-90.20;81.55,-91.37;81.89,-91.59",
    "67.44,-75.22;67.15,-75.87;67.10,-76.99;67.59,-77.24;68.15,-76.81;68.29,-75.90;68.01,-75.11;67.58,-75.10;67.44,-75.22",
    "69.49,-96.26;69.11,-95.65;68.76,-96.27;69.06,-97.62;68.95,-98.43;69.40,-99.80;69.71,-98.92;70.14,-98.22;69.86,-97.16;69.68,-96.56;69.49,-96.26",
    "49.87,-64.52;49.96,-64.17;49.71,-62.86;49.29,-61.84;49.11,-61.81;49.09,-62.29;49.40,-63.59;49.87,-64.52",
    "47.04,-64.01;46.55,-63.66;46.42,-62.94;46.44,-62.01;46.03,-62.50;45.97,-62.87;46.39,-64.14;46.73,-64.39;47.04,-64.01",
    "49.00,-122.84;49.00,-120.00;49.00,-117.03;49.00,-116.05;49.00,-113.00;49.00,-110.05;49.00,-107.05;49.00,-104.05;49.00,-100.65;49.00,-97.23;49.00,-95.16;49.38,-95.16;49.39,-94.82;48.84,-94.64;48.67,-94.33;48.61,-93.63;48.45,-92.61;48.14,-91.64;48.27,-90.83;48.01,-89.60;48.02,-89.27;48.30,-88.38;47.94,-87.44;47.55,-86.46;47.22,-85.65;46.90,-84.88;46.64,-84.78;46.54,-84.54;46.44,-84.60;46.41,-84.34;46.51,-84.14;46.28,-84.09;46.12,-83.89;46.12,-83.62;45.99,-83.47;45.82,-83.59;45.35,-82.55;44.44,-82.34;43.57,-82.14;42.98,-82.43;42.43,-82.90;42.08,-83.12;41.98,-83.14;41.83,-83.03;41.68,-82.69;41.68,-82.44;42.21,-81.28;42.37,-80.25;42.86,-78.94;42.96,-78.92;43.27,-79.01;43.47,-79.17;43.63,-78.72;43.63,-77.74;43.63,-76.82;44.02,-76.50;44.10,-76.38;44.82,-75.32;45.00,-74.87;45.01,-73.35;45.01,-71.51;45.25,-71.41;45.31,-71.08;45.46,-70.66;45.91,-70.31;46.69,-70.00;47.45,-69.24;47.19,-68.91;47.35,-68.23;47.07,-67.79;45.70,-67.79;45.14,-67.14;44.81,-66.96;44.33,-68.03;43.98,-69.06;43.68,-70.12;43.09,-70.65;42.87,-70.81;42.33,-70.82;41.81,-70.49;41.78,-70.08;42.15,-70.19;41.92,-69.88;41.64,-69.97;41.48,-70.64;41.49,-71.12;41.32,-71.86;41.27,-72.30;41.22,-72.88;40.93,-73.71;41.12,-72.24;40.93,-71.94;40.63,-73.34;40.63,-73.98;40.75,-73.95;40.47,-74.26;40.43,-73.96;39.71,-74.18;38.94,-74.91;39.20,-74.98;39.25,-75.20;39.50,-75.53;38.96,-75.32;38.78,-75.07;38.40,-75.06;38.02,-75.38;37.22,-75.94;37.26,-76.03;37.94,-75.72;38.32,-76.23;39.15,-76.35;38.72,-76.54;38.08,-76.33;38.24,-76.99;37.92,-76.30;36.97,-76.26;36.90,-75.97;36.55,-75.87;35.55,-75.73;34.81,-76.36;34.51,-77.40;33.93,-78.05;33.86,-78.55;33.49,-79.06;33.16,-79.20;32.51,-80.30;32.03,-80.86;31.44,-81.34;30.73,-81.49;30.04,-81.31;29.18,-80.98;28.47,-80.54;28.04,-80.53;26.88,-80.06;26.21,-80.09;25.82,-80.13;25.21,-80.38;25.08,-80.68;25.20,-81.17;25.64,-81.33;25.87,-81.71;26.73,-82.24;27.50,-82.71;27.89,-82.86;28.55,-82.65;29.10,-82.93;29.94,-83.71;30.09,-84.10;29.64,-85.11;29.69,-85.29;30.15,-85.77;30.40,-86.40;30.27,-87.53;30.38,-88.42;30.32,-89.18;30.16,-89.59;29.89,-89.41;29.49,-89.43;29.29,-89.22;29.16,-89.41;29.31,-89.78;29.12,-90.15;29.15,-90.88;29.68,-91.63;29.55,-92.50;29.78,-93.23;29.71,-93.85;29.48,-94.69;28.74,-95.60;28.31,-96.59;27.83,-97.14;27.38,-97.37;26.69,-97.38;26.21,-97.33;25.87,-97.14;25.84,-97.53;26.06,-98.24;26.37,-99.02;26.84,-99.30;27.54,-99.52;28.11,-100.11;28.70,-100.46;29.38,-100.96;29.78,-101.66;29.76,-102.48;28.97,-103.11;29.27,-103.94;29.57,-104.46;30.12,-104.71;30.64,-105.04;31.08,-105.63;31.40,-106.14;31.75,-106.51;31.75,-108.24;31.34,-108.24;31.34,-109.04;31.33,-111.02;32.04,-113.30;32.53,-114.81;32.72,-114.72;32.61,-115.99;32.54,-117.13;33.05,-117.30;33.62,-117.94;33.74,-118.41;34.03,-118.52;34.08,-119.08;34.35,-119.44;34.45,-120.37;34.61,-120.62;35.16,-120.74;36.16,-121.71;37.55,-122.55;37.78,-122.51;38.11,-122.95;38.95,-123.73;39.77,-123.87;40.31,-124.40;41.14,-124.18;42.00,-124.21;42.77,-124.53;43.71,-124.14;44.62,-124.02;45.52,-123.90;46.86,-124.08;47.72,-124.40;48.18,-124.69;48.38,-124.57;48.04,-123.12;47.10,-122.59;47.36,-122.34;48.18,-122.50;49.00,-122.84",
    "20.08,-155.40;19.99,-155.22;19.86,-155.06;19.51,-154.81;19.45,-154.83;19.24,-155.22;19.08,-155.54;18.92,-155.69;19.06,-155.94;19.34,-155.91;19.70,-156.07;19.81,-156.02;19.98,-155.85;20.17,-155.92;20.27,-155.86;20.25,-155.79;20.08,-155.40",
    "20.76,-156.00;20.64,-156.08;20.57,-156.41;20.78,-156.59;20.86,-156.70;20.93,-156.71;21.01,-156.61;20.92,-156.26;20.76,-156.00",
    "21.18,-156.76;21.07,-156.79;21.10,-157.33;21.22,-157.25;21.18,-156.76",
    "21.72,-158.03;21.65,-157.94;21.32,-157.65;21.26,-157.71;21.28,-157.78;21.31,-158.13;21.54,-158.25;21.58,-158.29;21.72,-158.03",
    "22.21,-159.37;21.98,-159.35;21.88,-159.46;22.07,-159.80;22.14,-159.75;22.24,-159.60;22.21,-159.37",
    "60.38,-166.47;60.29,-165.67;59.91,-165.58;59.75,-166.19;59.94,-166.85;60.21,-167.46;60.38,-166.47",
    "57.97,-153.23;57.90,-152.56;57.59,-152.14;57.12,-153.01;56.73,-154.01;56.99,-154.52;57.46,-154.67;57.82,-153.76;57.97,-153.23",
    "69.71,-140.99;69.71,-140.99;66.00,-140.99;60.31,-141.00;60.28,-140.01;60.00,-139.04;59.56,-138.34;58.91,-137.45;59.46,-136.48;59.79,-135.48;59.27,-134.95;58.86,-134.27;58.41,-133.36;57.69,-132.73;56.55,-131.71;55.92,-130.01;55.29,-129.98;54.80,-130.54;54.80,-130.54;54.80,-130.54;55.18,-131.09;55.50,-131.97;56.37,-132.25;57.18,-133.54;58.12,-134.08;58.19,-135.04;58.21,-136.63;58.50,-137.80;59.54,-139.87;59.73,-140.83;60.08,-142.57;60.00,-143.96;60.46,-145.93;60.88,-147.11;60.67,-148.22;59.98,-148.02;59.91,-148.57;59.71,-149.73;59.37,-150.61;59.16,-151.72;59.74,-151.86;60.73,-151.41;61.03,-150.35;61.28,-150.62;60.73,-151.90;60.06,-152.58;59.35,-154.02;58.86,-153.29;58.15,-154.23;57.73,-155.31;57.42,-156.31;56.98,-156.56;56.46,-158.12;55.99,-158.43;55.57,-159.60;55.64,-160.29;55.36,-161.22;55.02,-162.24;54.69,-163.07;54.40,-164.79;54.57,-164.94;55.04,-163.85;55.35,-162.87;55.89,-161.80;56.01,-160.56;56.42,-160.07;57.02,-158.68;57.22,-158.46;57.57,-157.72;58.33,-157.55;58.92,-157.04;58.62,-158.19;58.79,-158.52;58.42,-159.06;58.93,-159.71;58.57,-159.98;59.07,-160.36;58.67,-161.36;58.67,-161.97;59.27,-162.05;59.63,-161.87;59.99,-162.52;59.80,-163.82;60.27,-164.66;60.51,-165.35;61.07,-165.35;61.50,-166.12;62.07,-165.73;62.63,-164.92;63.15,-164.56;63.22,-163.75;63.06,-163.07;63.54,-162.26;63.46,-161.53;63.77,-160.77;64.22,-160.96;64.40,-161.52;64.79,-160.78;64.78,-161.39;64.56,-162.45;64.34,-162.76;64.56,-163.55;64.45,-164.96;64.69,-166.43;65.09,-166.85;65.67,-168.11;66.09,-166.71;66.58,-164.47;66.58,-163.65;66.08,-163.79;66.12,-161.68;66.74,-162.49;67.12,-163.72;67.62,-164.43;68.04,-165.39;68.36,-166.76;68.88,-166.20;68.92,-164.43;69.37,-163.17;69.86,-162.93;70.33,-161.91;70.45,-160.93;70.89,-159.04;70.82,-158.12;71.36,-156.58;71.15,-155.07;70.70,-154.34;70.89,-153.90;70.83,-152.21;70.60,-152.27;70.43,-150.74;70.53,-149.72;70.21,-147.61;70.12,-145.69;69.99,-144.92;70.15,-143.59;69.85,-142.07;69.71,-140.99;69.71,-140.99",
    "63.78,-171.73;63.59,-171.11;63.69,-170.49;63.43,-169.68;63.30,-168.69;63.19,-168.77;62.98,-169.53;63.19,-170.29;63.38,-170.67;63.32,-171.55;63.41,-171.79;63.78,-171.73",
    "49.21,87.36;48.55,86.60;48.46,85.77;47.45,85.72;47.00,85.16;47.33,83.18;45.54,82.46;45.32,81.95;44.92,79.97;43.18,80.87;42.92,80.18;42.35,80.26;42.50,79.64;42.86,79.14;42.96,77.66;42.99,76.00;42.88,75.64;43.30,74.21;43.09,73.65;42.50,73.49;42.85,71.84;42.70,71.19;42.27,70.96;42.08,70.39;41.38,69.07;40.67,68.63;40.66,68.26;41.14,67.99;41.17,66.71;41.99,66.51;41.99,66.02;43.00,66.10;43.73,64.90;43.65,63.19;43.50,62.01;44.41,61.06;44.78,60.24;45.50,58.69;45.59,58.50;45.00,55.93;41.31,55.97;41.26,55.46;42.04,54.76;42.32,54.08;42.12,52.94;41.78,52.50;42.03,52.45;42.44,52.69;42.79,52.50;43.13,51.34;44.03,50.89;44.28,50.34;44.61,50.31;44.51,51.28;45.25,51.32;45.41,52.17;45.26,53.04;46.23,53.22;46.85,53.04;46.80,52.04;47.05,51.19;46.61,50.03;46.40,49.10;46.56,48.59;47.08,48.69;47.74,48.06;47.72,47.32;48.39,46.47;49.15,47.04;49.36,46.75;50.45,47.55;49.87,48.58;50.61,48.70;51.69,50.77;51.72,52.33;51.03,54.53;50.62,55.72;51.04,56.78;51.06,58.36;50.55,59.64;50.84,59.93;50.80,61.34;51.27,61.59;51.96,59.97;52.45,60.93;52.72,60.74;52.98,61.70;53.66,60.98;54.01,61.44;54.35,65.18;54.60,65.67;54.97,68.17;55.39,69.07;55.17,70.87;54.13,71.18;54.38,72.22;54.04,73.51;53.49,73.43;53.55,74.38;54.49,76.89;54.18,76.53;53.40,77.80;50.86,80.04;51.39,80.57;50.81,81.95;51.07,83.38;50.89,83.94;50.31,84.42;50.12,85.12;49.69,85.54;49.83,86.83;49.21,87.36",
    "41.31,55.97;45.00,55.93;45.59,58.50;45.50,58.69;44.78,60.24;44.41,61.06;43.50,62.01;43.65,63.19;43.73,64.90;43.00,66.10;41.99,66.02;41.99,66.51;41.17,66.71;41.14,67.99;40.66,68.26;40.67,68.63;41.38,69.07;42.08,70.39;42.27,70.96;42.17,71.26;41.52,70.42;41.14,71.16;41.39,71.87;40.87,73.06;40.15,71.77;40.24,71.01;40.22,70.60;40.50,70.46;40.96,70.67;40.73,69.33;40.09,69.01;39.53,68.54;39.58,67.70;39.14,67.44;38.90,68.18;38.16,68.39;37.14,67.83;37.36,67.08;37.36,66.52;37.97,66.55;38.40,65.22;38.89,64.17;39.36,63.52;40.05,62.37;41.08,61.88;41.27,61.55;41.22,60.47;41.43,60.08;42.22,59.98;42.75,58.63;42.17,57.79;41.83,56.93;41.32,57.10;41.31,55.97",
    "-2.60,141.00;-3.29,142.74;-3.86,144.58;-4.37,145.27;-4.88,145.83;-5.47,145.98;-6.08,147.65;-6.61,147.89;-6.72,146.97;-7.39,147.19;-8.04,148.08;-9.10,148.73;-9.07,149.31;-9.51,149.27;-9.68,150.04;-9.87,149.74;-10.29,150.80;-10.58,150.69;-10.65,150.03;-10.39,149.78;-10.28,148.92;-10.13,147.91;-9.49,147.14;-8.94,146.57;-8.07,146.05;-7.63,144.74;-7.92,143.90;-8.25,143.29;-8.98,143.41;-9.33,142.63;-9.16,142.07;-9.12,141.03;-5.86,141.02;-2.60,141.00",
    "-3.66,152.64;-3.98,153.02;-4.50,153.14;-4.77,152.83;-4.18,152.64;-3.79,152.41;-3.46,151.95;-3.04,151.38;-2.74,150.66;-2.50,150.94;-2.78,151.48;-3.00,151.82;-3.24,152.24;-3.66,152.64",
    "-5.84,151.30;-6.08,150.75;-6.32,150.24;-6.32,149.71;-6.03,148.89;-5.75,148.32;-5.44,148.40;-5.58,149.30;-5.51,149.85;-5.03,150.00;-5.00,150.14;-5.53,150.24;-5.46,150.81;-5.11,151.09;-4.76,151.65;-4.17,151.54;-4.15,152.14;-4.31,152.34;-4.87,152.32;-5.48,151.98;-5.56,151.46;-5.84,151.30",
    "-5.34,154.76;-5.57,155.06;-6.20,155.55;-6.54,156.02;-6.82,155.88;-6.92,155.60;-6.54,155.17;-5.90,154.73;-5.14,154.51;-5.04,154.65;-5.34,154.76",
    "-2.60,141.00;-5.86,141.02;-9.12,141.03;-8.30,140.14;-8.10,139.13;-8.38,138.88;-8.41,137.61;-7.60,138.04;-7.32,138.67;-6.23,138.41;-5.39,137.93;-4.55,135.99;-4.46,135.16;-3.54,133.66;-4.02,133.37;-4.11,132.98;-3.75,132.76;-3.31,132.75;-2.82,131.99;-2.46,133.07;-2.48,133.78;-2.21,133.70;-2.21,132.23;-1.62,131.84;-1.43,130.94;-0.94,130.52;-0.70,131.87;-0.37,132.38;-0.78,133.99;-1.15,134.14;-2.77,134.42;-3.37,135.46;-2.31,136.29;-1.70,137.44;-1.70,138.33;-2.05,139.18;-2.41,139.93;-2.60,141.00",
    "-8.89,124.97;-9.09,125.07;-9.39,125.09;-10.14,124.44;-10.36,123.58;-10.24,123.46;-9.90,123.55;-9.29,123.98;-8.89,124.97",
    "-6.90,134.21;-6.14,134.11;-5.78,134.29;-5.45,134.50;-5.74,134.73;-6.21,134.72;-6.90,134.21",
    "4.14,117.88;3.23,117.31;2.29,118.05;1.83,117.88;0.90,119.00;0.78,117.81;0.10,117.48;-0.80,117.52;-1.49,116.56;-2.48,116.53;-4.01,116.15;-3.66,116.00;-4.11,114.86;-3.50,114.47;-3.44,113.76;-3.12,113.26;-3.48,112.07;-2.99,111.70;-3.05,111.05;-2.93,110.22;-1.59,110.07;-1.31,109.57;-0.46,109.09;0.42,108.95;1.34,109.07;2.01,109.66;1.34,109.83;0.77,110.51;0.98,111.16;0.90,111.80;1.41,112.38;1.50,112.86;1.22,113.81;1.43,114.62;2.82,115.13;3.17,115.52;4.31,115.87;4.31,117.02;4.14,117.88",
    "-2.80,129.37;-3.09,130.47;-3.86,130.83;-3.45,129.99;-3.36,129.16;-3.43,128.59;-3.39,127.90;-2.84,128.14;-2.80,129.37",
    "-3.79,126.87;-3.61,126.18;-3.18,125.99;-3.13,127.00;-3.46,127.25;-3.79,126.87",
    "2.17,127.93;1.63,128.00;1.54,128.59;1.13,128.69;0.26,128.64;0.36,128.12;-0.25,127.97;-0.78,128.38;-0.90,128.10;-0.27,127.70;1.01,127.40;1.81,127.60;2.17,127.93",
    "0.88,122.93;0.92,124.08;1.64,125.07;1.42,125.24;0.43,124.44;0.24,123.69;0.43,122.72;0.38,121.06;0.24,120.18;-0.52,120.04;-1.41,120.94;-0.96,121.48;-0.62,123.34;-1.08,123.26;-0.93,122.82;-1.52,122.39;-1.90,121.51;-3.19,122.45;-3.53,122.27;-4.68,123.17;-5.34,123.16;-5.63,122.63;-5.28,122.24;-4.46,122.72;-4.85,121.74;-4.57,121.49;-4.19,121.62;-3.60,120.90;-2.63,120.97;-2.93,120.31;-4.10,120.39;-5.53,120.43;-5.67,119.80;-5.38,119.37;-4.46,119.65;-3.49,119.50;-3.49,119.08;-2.80,118.77;-2.15,119.18;-1.35,119.32;0.15,119.83;0.57,120.04;1.31,120.89;1.01,121.67;0.88,122.93",
    "-10.26,120.30;-9.56,118.97;-9.36,119.90;-9.67,120.43;-9.97,120.78;-10.24,120.72;-10.26,120.30",
    "-8.54,121.34;-8.46,122.01;-8.09,122.90;-8.65,122.76;-8.93,121.25;-8.81,119.92;-8.44,119.92;-8.24,120.72;-8.54,121.34",
    "-8.36,118.26;-8.28,118.88;-8.71,119.13;-8.91,117.97;-9.04,117.28;-9.03,116.74;-8.46,117.08;-8.45,117.63;-8.10,117.90;-8.36,118.26",
    "-6.42,108.49;-6.78,108.62;-6.88,110.54;-6.47,110.76;-6.95,112.61;-7.59,112.98;-7.78,114.48;-8.37,115.71;-8.75,114.56;-8.35,113.46;-8.38,112.56;-8.30,111.52;-8.12,110.59;-7.74,109.43;-7.64,108.69;-7.77,108.28;-7.35,106.45;-6.92,106.28;-6.85,105.37;-5.90,106.05;-5.95,107.27;-6.35,108.07;-6.42,108.49",
    "-1.08,104.37;-1.78,104.54;-2.34,104.89;-2.43,105.62;-3.06,106.11;-4.31,105.86;-5.85,105.82;-5.87,104.71;-5.04,103.87;-4.22,102.58;-3.61,102.16;-2.80,101.40;-2.05,100.90;-0.65,100.14;0.18,99.26;1.04,98.97;1.82,98.60;2.45,97.70;3.31,97.18;3.87,96.42;4.97,95.38;5.48,95.29;5.44,95.94;5.25,97.48;4.27,98.37;3.59,99.14;3.17,99.69;2.10,100.64;2.08,101.66;1.40,102.50;0.56,103.08;0.10,103.84;-0.71,103.44;-1.06,104.01;-1.08,104.37",
    "-52.64,-68.63;-53.10,-68.25;-53.85,-67.75;-54.45,-66.45;-54.70,-65.05;-55.20,-65.50;-55.25,-66.45;-54.90,-66.96;-54.87,-67.56;-54.87,-68.63;-52.64,-68.63",
    "-30.22,-57.63;-31.02,-57.87;-32.04,-58.14;-33.04,-58.13;-33.26,-58.35;-33.91,-58.43;-34.43,-58.50;-35.29,-57.23;-35.98,-57.36;-36.41,-56.74;-36.90,-56.79;-38.18,-57.75;-38.72,-59.23;-38.93,-61.24;-38.83,-62.34;-39.42,-62.13;-40.17,-62.33;-40.68,-62.15;-41.03,-62.75;-41.17,-63.77;-40.80,-64.73;-41.06,-65.12;-42.06,-64.98;-42.36,-64.30;-42.04,-63.76;-42.56,-63.46;-42.87,-64.38;-43.50,-65.18;-44.50,-65.33;-45.04,-65.57;-45.04,-66.51;-45.55,-67.29;-46.30,-67.58;-47.03,-66.60;-47.24,-65.64;-48.13,-65.99;-48.70,-67.17;-49.87,-67.82;-50.26,-68.73;-50.73,-69.14;-51.77,-68.82;-52.35,-68.15;-52.30,-68.57;-52.14,-69.50;-52.01,-71.91;-51.43,-72.33;-50.68,-72.31;-50.74,-72.98;-50.38,-73.33;-49.32,-73.42;-48.88,-72.65;-48.24,-72.33;-47.74,-72.45;-46.88,-71.92;-45.56,-71.55;-44.97,-71.66;-44.78,-71.22;-44.41,-71.33;-44.21,-71.79;-43.79,-71.46;-43.41,-71.92;-42.25,-72.15;-42.05,-71.75;-40.83,-71.92;-39.81,-71.68;-38.92,-71.41;-38.55,-70.81;-37.58,-71.12;-36.66,-71.12;-36.01,-70.36;-35.17,-70.39;-34.19,-69.82;-33.27,-69.81;-33.09,-70.07;-31.37,-70.54;-30.34,-69.92;-29.37,-70.01;-28.46,-69.66;-27.52,-69.00;-26.90,-68.30;-26.51,-68.59;-26.19,-68.39;-24.52,-68.42;-24.03,-67.33;-22.99,-66.99;-22.74,-67.11;-21.83,-66.27;-22.08,-64.96;-22.80,-64.38;-21.99,-63.99;-22.03,-62.85;-22.25,-62.69;-23.88,-60.85;-24.03,-60.03;-24.77,-58.81;-25.16,-57.78;-25.60,-57.63;-27.12,-58.62;-27.40,-57.61;-27.55,-56.49;-27.39,-55.70;-26.62,-54.79;-25.74,-54.63;-25.55,-54.13;-26.12,-53.63;-26.92,-53.65;-27.47,-54.49;-27.88,-55.16;-28.85,-56.29;-30.22,-57.63",
    "-52.64,-68.63;-54.87,-68.63;-54.87,-67.56;-54.90,-66.96;-55.30,-67.29;-55.61,-68.15;-55.58,-68.64;-55.50,-69.23;-55.20,-69.96;-55.05,-71.01;-54.50,-72.26;-53.96,-73.29;-52.84,-74.66;-53.05,-73.84;-53.72,-72.43;-54.07,-71.11;-53.62,-70.59;-52.93,-70.27;-52.52,-69.35;-52.64,-68.63",
    "-17.58,-69.59;-18.26,-69.10;-18.98,-68.97;-19.41,-68.44;-20.37,-68.76;-21.49,-68.22;-22.87,-67.83;-22.74,-67.11;-22.99,-66.99;-24.03,-67.33;-24.52,-68.42;-26.19,-68.39;-26.51,-68.59;-26.90,-68.30;-27.52,-69.00;-28.46,-69.66;-29.37,-70.01;-30.34,-69.92;-31.37,-70.54;-33.09,-70.07;-33.27,-69.81;-34.19,-69.82;-35.17,-70.39;-36.01,-70.36;-36.66,-71.12;-37.58,-71.12;-38.55,-70.81;-38.92,-71.41;-39.81,-71.68;-40.83,-71.92;-42.05,-71.75;-42.25,-72.15;-43.41,-71.92;-43.79,-71.46;-44.21,-71.79;-44.41,-71.33;-44.78,-71.22;-44.97,-71.66;-45.56,-71.55;-46.88,-71.92;-47.74,-72.45;-48.24,-72.33;-48.88,-72.65;-49.32,-73.42;-50.38,-73.33;-50.74,-72.98;-50.68,-72.31;-51.43,-72.33;-52.01,-71.91;-52.14,-69.50;-52.30,-68.57;-52.29,-69.46;-52.54,-69.94;-52.90,-70.85;-53.83,-71.01;-53.86,-71.43;-53.53,-72.56;-52.84,-73.70;-52.84,-73.70;-52.26,-74.95;-51.63,-75.26;-51.04,-74.98;-50.38,-75.48;-48.67,-75.61;-47.71,-75.18;-46.94,-74.13;-46.65,-75.64;-45.76,-74.69;-44.10,-74.35;-44.45,-73.24;-42.38,-72.72;-42.12,-73.39;-43.37,-73.70;-43.22,-74.33;-41.79,-74.02;-39.94,-73.68;-39.26,-73.22;-38.28,-73.51;-37.16,-73.59;-37.12,-73.17;-35.51,-72.55;-33.91,-71.86;-32.42,-71.44;-30.92,-71.67;-30.10,-71.37;-28.86,-71.49;-27.64,-70.91;-25.71,-70.72;-23.63,-70.40;-21.39,-70.09;-19.76,-70.16;-18.35,-70.37;-18.09,-69.86;-17.58,-69.59",
    "-4.50,29.34;-5.42,29.52;-5.94,29.42;-6.52,29.62;-7.08,30.20;-8.34,30.74;-8.34,30.74;-8.24,30.35;-8.41,29.00;-8.53,28.73;-9.16,28.45;-9.61,28.67;-10.79,28.50;-11.79,28.37;-11.97,28.64;-12.36,29.34;-12.18,29.62;-13.26,29.70;-13.25,28.93;-12.70,28.52;-12.27,28.16;-12.13,27.39;-11.61,27.16;-11.92,26.55;-11.78,25.75;-11.33,25.42;-11.24,24.78;-11.26,24.31;-10.95,24.26;-10.93,23.91;-10.87,23.46;-11.02,22.84;-10.99,22.40;-11.08,22.16;-9.89,22.21;-9.52,21.88;-8.91,21.80;-8.31,21.95;-7.92,21.75;-7.29,21.73;-7.30,20.51;-6.94,20.60;-6.94,20.09;-7.12,20.04;-7.16,19.42;-7.74,19.17;-7.99,19.02;-7.85,18.46;-7.99,18.13;-8.07,17.47;-7.55,17.09;-7.22,16.86;-6.62,16.57;-5.88,16.33;-5.86,13.38;-5.98,13.02;-5.97,12.74;-6.10,12.32;-5.79,12.18;-5.68,12.44;-5.25,12.47;-4.99,12.63;-4.78,13.00;-4.88,13.26;-4.50,13.60;-4.51,14.14;-4.79,14.21;-4.97,14.58;-4.34,15.17;-3.86,15.75;-3.54,16.01;-2.71,15.97;-1.74,16.41;-1.23,16.87;-0.74,17.52;-0.42,17.64;-0.06,17.66;0.29,17.83;0.86,17.77;1.74,17.90;2.37,18.09;2.90,18.39;3.50,18.45;4.20,18.54;4.71,18.93;5.03,19.47;4.69,20.29;4.32,20.93;4.22,21.66;4.03,22.41;4.63,22.70;4.71,22.84;4.61,23.30;5.11,24.41;4.90,24.81;4.93,25.13;5.17,25.28;5.26,25.65;5.15,26.40;5.13,27.04;5.23,27.37;4.41,27.98;4.29,28.43;4.46,28.70;4.39,29.16;4.60,29.72;4.17,29.95;3.51,30.83;3.51,30.83;2.34,30.77;2.20,31.17;1.85,30.85;1.58,30.47;1.06,30.09;0.60,29.88;-0.21,29.82;-0.59,29.59;-1.34,29.58;-1.62,29.29;-2.22,29.25;-2.29,29.12;-2.84,29.02;-3.29,29.28;-4.50,29.34",
    "-1.68,41.59;-0.86,40.99;2.78,40.98;3.92,41.86;4.23,42.13;4.25,42.77;4.96,43.66;5.00,44.96;8.00,47.79;8.84,48.49;9.45,48.94;9.97,48.94;10.98,48.94;11.39,48.94;11.41,48.95;11.41,48.95;11.43,49.27;11.58,49.73;11.68,50.26;12.02,50.73;12.02,51.11;11.75,51.13;11.17,51.04;10.64,51.05;10.28,50.83;9.20,50.55;8.08,50.07;6.80,49.45;5.34,48.59;4.22,47.74;2.86,46.56;2.05,45.56;1.05,44.07;0.29,43.14;-0.92,42.04;-1.45,41.81;-1.68,41.59",
    "-4.68,39.20;-3.68,37.77;-3.10,37.70;-1.06,34.07;-0.95,33.90;0.11,33.89;0.52,34.18;1.18,34.67;1.91,35.04;3.05,34.60;3.56,34.48;4.25,34.01;4.85,34.62;5.51,35.30;5.34,35.82;4.78,35.82;4.45,36.16;4.45,36.86;3.60,38.12;3.59,38.44;3.62,38.67;3.50,38.89;3.42,39.56;3.84,39.85;4.26,40.77;3.92,41.17;3.92,41.86;2.78,40.98;-0.86,40.99;-1.68,41.59;-2.08,40.88;-2.50,40.64;-2.57,40.26;-3.28,40.12;-3.68,39.80;-4.35,39.60;-4.68,39.20",
    "8.23,24.57;8.67,23.81;8.95,23.46;9.27,23.39;9.68,23.56;10.09,23.55;10.71,22.98;11.14,22.86;11.38,22.88;11.68,22.51;12.26,22.50;12.65,22.29;12.59,21.94;12.96,22.04;13.37,22.30;13.79,22.18;14.09,22.51;14.33,22.30;14.94,22.57;15.68,23.02;15.61,23.89;19.58,23.84;20.00,23.85;20.00,25.00;22.00,25.00;22.00,29.02;22.00,32.90;22.00,36.87;21.02,37.19;20.84,36.97;19.81,37.11;18.61,37.48;18.37,37.86;18.00,38.41;17.43,37.90;17.26,37.17;16.96,36.85;16.29,36.75;14.82,36.32;14.42,36.43;13.56,36.27;12.58,35.86;12.08,35.26;11.32,34.83;10.91,34.73;10.63,34.26;9.58,33.96;8.68,33.97;9.46,33.96;9.48,33.82;9.98,33.84;10.33,33.72;10.72,33.21;11.44,33.09;12.18,33.21;12.25,32.74;12.02,32.67;11.97,32.07;11.68,32.31;11.08,32.40;10.53,31.85;9.81,31.35;9.71,30.84;10.29,30.00;10.08,29.62;9.79,29.52;9.60,29.00;9.40,28.97;9.40,27.97;9.60,27.83;9.64,27.11;9.47,26.75;9.55,26.48;10.14,25.96;10.41,25.79;10.27,25.07;9.81,24.79;8.92,24.54;8.73,24.19;8.62,23.89;8.23,24.57",
    "19.58,23.84;15.61,23.89;15.68,23.02;14.94,22.57;14.33,22.30;14.09,22.51;13.79,22.18;13.37,22.30;12.96,22.04;12.59,21.94;12.65,22.29;12.26,22.50;11.68,22.51;11.38,22.88;11.14,22.86;10.97,22.23;10.57,21.72;9.48,21.00;9.01,20.06;9.07,19.09;8.98,18.81;8.63,18.91;8.28,18.39;7.89,17.96;7.51,16.71;7.73,16.46;7.75,16.29;7.50,16.11;7.42,15.28;7.69,15.44;8.38,15.12;8.80,14.98;8.97,14.54;9.55,13.95;10.02,14.17;9.92,14.63;9.99,14.91;9.98,15.47;10.89,14.92;11.56,14.96;12.22,14.89;12.86,14.50;13.33,14.60;13.35,13.95;14.00,13.96;14.37,13.54;15.68,13.97;16.63,15.25;17.93,15.30;19.96,15.69;20.39,15.90;20.73,15.49;21.05,15.47;21.31,15.10;22.86,14.85;23.41,15.86;21.50,19.85;19.58,23.84",
    "19.71,-71.71;19.17,-71.62;18.79,-71.70;18.62,-71.95;18.32,-71.69;18.04,-71.71;18.21,-72.37;18.15,-72.84;18.22,-73.45;18.03,-73.92;18.34,-74.46;18.66,-74.37;18.53,-73.45;18.45,-72.69;18.67,-72.33;19.10,-72.79;19.48,-72.78;19.64,-73.42;19.92,-73.19;19.87,-72.58;19.71,-71.71",
    "18.04,-71.71;18.32,-71.69;18.62,-71.95;18.79,-71.70;19.17,-71.62;19.71,-71.71;19.88,-71.59;19.88,-70.81;19.62,-70.21;19.65,-69.95;19.29,-69.77;19.31,-69.22;19.02,-69.25;18.98,-68.81;18.61,-68.32;18.21,-68.69;18.42,-69.16;18.38,-69.62;18.43,-69.95;18.25,-70.13;18.18,-70.52;18.43,-70.67;18.28,-71.00;17.60,-71.40;17.76,-71.66;18.04,-71.71",
    "71.10,178.73;71.52,180.00;70.83,180.00;70.78,178.90;71.10,178.73",
    "46.40,49.10;45.81,48.65;45.64,47.68;44.61,46.68;43.66,47.59;42.99,47.49;41.81,48.58;41.81,48.58;41.41,47.99;41.15,47.82;41.22,47.37;41.83,46.69;41.86,46.40;42.09,45.78;42.50,45.47;42.71,44.54;42.55,43.93;42.74,43.76;43.22,42.39;43.38,40.92;43.55,40.08;43.43,39.96;44.28,38.68;44.66,37.54;45.24,36.68;45.40,37.40;46.24,38.23;46.64,37.67;47.04,39.15;47.26,39.12;47.10,38.22;47.55,38.26;47.83,38.77;47.90,39.74;48.23,39.90;48.78,39.67;49.31,40.08;49.60,40.07;49.93,38.59;49.92,38.01;50.38,37.39;50.23,36.63;50.58,35.36;50.77,35.38;51.21,35.02;51.26,34.22;51.57,34.14;51.77,34.39;52.34,33.75;52.24,32.72;52.29,32.41;52.06,32.16;52.10,31.79;52.10,31.79;52.74,31.54;53.07,31.31;53.17,31.50;53.13,32.30;53.35,32.69;53.62,32.41;53.79,31.73;53.97,31.79;54.16,31.38;54.81,30.76;55.08,30.97;55.55,30.87;55.79,29.90;55.67,29.37;55.92,29.23;56.17,28.18;56.76,27.86;57.24,27.77;57.47,27.29;57.79,27.72;58.72,27.42;59.30,28.13;59.48,27.98;59.48,27.98;60.03,29.12;60.50,28.07;60.50,28.07;61.78,30.21;62.36,31.14;62.87,31.52;63.55,30.04;64.20,30.44;64.95,29.54;65.81,30.22;66.94,29.05;67.70,29.98;68.36,28.45;69.06,28.59;69.16,29.40;69.56,31.10;69.56,31.10;69.91,32.13;69.30,33.78;69.06,36.51;67.93,40.29;67.46,41.06;66.79,41.13;66.27,40.02;66.00,38.38;66.76,33.92;66.63,33.18;65.90,34.81;65.44,34.88;64.41,34.94;64.11,36.23;63.85,37.01;64.33,37.14;64.76,36.54;65.14,37.18;64.52,39.59;64.76,40.44;65.50,39.76;66.48,42.09;66.42,43.02;66.07,43.95;66.76,44.53;67.35,43.70;67.95,44.19;68.57,43.45;68.25,46.25;67.69,46.82;67.57,45.56;67.01,45.56;66.67,46.35;66.88,47.89;67.52,48.14;68.00,50.23;68.86,53.72;68.81,54.47;68.20,53.49;68.10,54.73;68.44,55.44;68.47,57.32;68.88,58.80;68.28,59.94;68.94,61.08;69.52,60.03;69.85,60.55;69.55,63.50;69.23,64.89;68.09,68.51;68.62,69.18;69.14,68.16;69.36,68.14;69.45,66.93;69.93,67.26;70.71,66.72;71.03,66.69;71.93,68.54;72.84,69.20;73.04,69.94;72.78,72.59;72.22,72.80;71.41,71.85;71.09,72.47;70.39,72.79;69.02,72.56;68.41,73.67;67.74,73.24;66.32,71.28;66.17,72.42;66.53,72.82;66.79,73.92;67.28,74.19;67.76,75.05;68.33,74.47;68.99,74.94;69.07,73.84;69.63,73.60;70.63,74.40;71.45,73.10;72.12,74.89;72.83,74.66;72.85,75.16;72.30,75.68;71.34,75.29;71.15,76.36;71.87,75.90;72.27,77.58;72.32,79.65;71.75,81.50;72.58,80.61;73.65,80.51;73.85,82.25;73.81,84.66;73.94,86.82;74.46,86.01;75.12,87.17;75.14,88.32;75.64,90.26;75.77,92.90;76.05,93.23;76.14,95.86;75.92,96.68;76.45,98.92;76.43,100.76;76.86,101.04;77.29,101.99;77.70,104.35;77.37,106.07;77.13,104.71;76.97,106.97;76.48,107.24;76.72,108.15;76.71,111.08;76.22,113.33;75.85,114.13;75.33,113.89;75.03,112.78;74.48,110.15;74.18,109.40;74.04,110.64;73.79,112.12;73.98,113.02;73.34,113.53;73.59,113.97;73.75,115.57;73.59,118.78;73.12,119.02;72.97,123.20;73.74,123.26;73.56,125.38;73.57,126.98;73.04,128.59;72.40,129.05;71.98,128.46;71.19,129.72;70.79,131.29;71.84,132.25;71.39,133.86;71.66,135.56;71.35,137.50;71.63,138.23;71.49,139.87;72.42,139.15;72.85,140.47;72.20,149.50;71.61,150.35;70.84,152.97;71.03,157.01;70.87,159.00;70.45,159.83;69.72,159.71;69.44,160.94;69.64,162.28;69.67,164.05;69.47,165.94;69.58,167.84;68.69,169.58;69.01,170.82;69.65,170.01;70.10,170.45;69.82,173.64;69.88,175.72;69.40,178.60;68.96,180.00;64.98,180.00;64.97,179.99;64.53,178.71;64.61,177.41;64.08,178.31;63.25,178.91;62.98,179.37;62.57,179.49;62.30,179.23;62.52,177.36;61.77,174.57;61.65,173.68;60.95,172.15;60.34,170.70;59.88,170.33;60.57,168.90;59.79,166.29;60.16,165.84;59.73,164.88;59.87,163.54;59.21,163.22;58.24,162.02;57.84,162.05;57.62,163.19;56.16,163.06;56.12,162.13;55.29,161.70;54.86,162.12;54.34,160.37;53.20,160.02;52.96,158.53;51.94,158.23;51.01,156.79;51.70,156.42;53.16,155.99;55.38,155.43;56.77,155.91;57.36,156.76;57.83,156.81;58.06,158.36;59.31,160.15;60.34,161.87;61.14,163.67;62.55,164.47;62.47,163.26;61.64,162.66;60.54,160.12;61.77,159.30;61.43,156.72;59.76,154.22;59.14,155.04;58.88,152.81;58.78,151.27;59.50,151.34;59.66,149.78;59.16,148.54;59.34,145.49;59.04,142.20;57.09,138.96;54.73,135.13;54.60,136.70;53.98,137.19;53.76,138.16;54.25,138.80;54.19,139.90;53.09,141.35;52.24,141.38;51.24,140.60;50.05,140.51;48.45,140.06;47.00,138.55;46.31,138.22;45.14,136.86;43.99,135.52;43.40,134.87;42.81,133.54;42.80,132.91;43.28,132.28;42.55,130.94;42.22,130.78;42.22,130.78;42.22,130.78;42.22,130.78;42.40,130.64;42.40,130.64;42.90,130.63;42.93,131.14;44.11,131.29;44.97,131.03;45.32,131.88;45.14,133.10;46.12,133.77;47.21,134.11;47.58,134.50;48.48,135.03;48.18,133.37;47.79,132.51;47.79,130.99;48.73,130.58;49.44,129.40;49.76,127.66;50.74,127.29;51.35,126.94;51.78,126.56;52.79,125.95;53.16,125.07;53.46,123.57;53.43,122.25;53.25,121.00;52.75,120.18;52.52,120.73;51.96,120.74;51.64,120.18;50.58,119.28;50.14,119.29;49.51,117.88;49.89,116.68;49.81,115.49;50.14,114.96;50.25,114.36;49.54,112.90;49.38,111.58;49.13,110.66;49.29,109.40;49.28,108.48;49.79,107.87;50.27,106.89;50.41,105.89;50.28,104.62;50.09,103.68;50.51,102.26;51.26,102.07;51.52,100.89;51.63,99.98;52.05,98.86;51.01,97.83;50.42,98.23;49.73,97.26;49.98,95.81;50.01,94.82;50.48,94.15;50.50,93.10;50.80,92.23;50.33,90.71;49.47,88.81;49.30,87.75;49.21,87.36;49.83,86.83;49.69,85.54;50.12,85.12;50.31,84.42;50.89,83.94;51.07,83.38;50.81,81.95;51.39,80.57;50.86,80.04;53.40,77.80;54.18,76.53;54.49,76.89;53.55,74.38;53.49,73.43;54.04,73.51;54.38,72.22;54.13,71.18;55.17,70.87;55.39,69.07;54.97,68.17;54.60,65.67;54.35,65.18;54.01,61.44;53.66,60.98;52.98,61.70;52.72,60.74;52.45,60.93;51.96,59.97;51.27,61.59;50.80,61.34;50.84,59.93;50.55,59.64;51.06,58.36;51.04,56.78;50.62,55.72;51.03,54.53;51.72,52.33;51.69,50.77;50.61,48.70;49.87,48.58;50.45,47.55;49.36,46.75;49.15,47.04;48.39,46.47;47.72,47.32;47.74,48.06;47.08,48.69;46.56,48.59;46.40,49.10",
    "81.02,93.78;81.25,95.94;80.75,97.88;79.78,100.19;78.88,99.94;78.76,97.76;79.04,94.97;79.43,93.31;80.14,92.55;80.34,91.18;81.02,93.78",
    "79.28,102.84;78.71,105.37;78.31,105.08;77.92,99.44;79.23,101.26;79.35,102.09;79.28,102.84",
    "76.14,138.83;76.09,141.47;75.56,145.09;74.82,144.30;74.85,140.61;74.61,138.96;75.26,136.97;75.95,137.51;76.14,138.83",
    "75.35,148.22;75.08,150.73;74.69,149.58;74.78,147.98;75.17,146.12;75.50,146.36;75.35,148.22",
    "73.37,139.86;73.77,140.81;73.86,142.06;73.48,143.48;73.21,143.60;73.21,142.09;73.32,140.04;73.37,139.86",
    "80.59,44.85;80.77,46.80;80.78,48.32;80.51,48.52;80.75,49.10;80.92,50.04;80.70,51.52;80.55,51.14;80.42,49.79;80.34,48.89;80.18,48.75;80.01,47.59;80.25,46.50;80.56,47.07;80.59,44.85",
    "54.33,22.73;54.31,20.89;54.43,19.66;54.87,19.89;55.19,21.27;55.02,22.32;54.86,22.76;54.58,22.65;54.33,22.73",
    "73.75,53.51;74.63,55.90;75.08,55.63;75.61,57.87;76.25,61.17;76.44,64.50;76.81,66.21;76.94,68.16;76.54,68.85;76.23,68.18;75.74,64.64;75.26,61.58;74.31,58.48;73.33,56.99;72.37,55.42;71.54,55.62;70.72,57.54;70.63,56.94;70.76,53.68;71.21,53.41;71.47,51.60;72.01,51.46;72.23,52.48;72.77,52.44;73.63,54.43;73.75,53.51",
    "53.70,142.91;52.74,143.26;51.76,143.24;50.75,143.65;48.98,144.65;49.31,143.17;47.86,142.56;46.84,143.53;46.14,143.51;46.74,142.75;45.97,142.09;46.81,141.91;47.78,142.02;48.86,141.90;49.62,142.14;50.95,142.18;51.94,141.59;53.30,141.68;53.76,142.61;54.23,142.21;54.37,142.65;53.70,142.91",
    "67.21,-174.93;66.58,-175.01;66.34,-174.34;67.06,-174.57;66.91,-171.86;65.98,-169.90;65.54,-170.89;65.44,-172.53;64.46,-172.56;64.25,-172.96;64.28,-173.89;64.63,-174.65;64.92,-175.98;65.36,-176.21;65.52,-177.22;65.39,-178.36;65.74,-178.90;66.11,-178.69;65.87,-179.88;65.40,-179.43;64.98,-180.00;68.96,-180.00;68.20,-177.55;67.21,-174.93",
    "70.89,-178.69;70.83,-180.00;71.52,-180.00;71.56,-179.87;71.56,-179.02;71.27,-177.58;71.13,-177.66;70.89,-178.69",
    "45.97,33.44;46.22,33.70;46.01,34.41;45.97,34.73;45.77,34.86;45.74,35.01;45.65,35.02;45.41,35.51;45.47,36.53;45.11,36.33;44.94,35.24;44.36,33.88;44.56,33.33;45.03,33.55;45.33,32.45;45.52,32.63;45.85,33.59;45.97,33.44",
    "26.79,-78.98;26.87,-78.51;26.84,-77.85;26.58,-77.82;26.42,-78.91;26.79,-78.98",
    "27.04,-77.79;26.59,-77.00;25.88,-77.17;26.01,-77.36;26.53,-77.34;26.93,-77.79;27.04,-77.79",
    "25.21,-78.19;25.17,-77.89;24.34,-77.54;23.76,-77.53;23.71,-77.78;24.29,-78.03;24.58,-78.41;25.21,-78.19",
    "-51.85,-61.20;-51.25,-60.00;-51.50,-59.15;-51.10,-58.55;-51.55,-57.75;-51.90,-58.05;-52.20,-59.40;-51.85,-59.85;-52.30,-60.70;-51.85,-61.20",
    "79.67,15.14;80.02,15.52;80.05,16.99;79.70,18.25;78.96,21.54;78.56,19.03;77.83,18.47;77.64,17.59;76.81,17.12;76.77,15.91;77.38,13.76;77.74,14.67;78.02,13.17;78.87,11.22;79.65,10.44;80.01,13.17;79.66,13.72;79.67,15.14",
    "69.56,31.10;69.16,29.40;69.06,28.59;69.77,29.02;70.16,27.73;69.83,26.18;69.09,25.69;68.65,24.74;68.89,23.66;68.84,22.36;69.37,21.24;69.11,20.65;69.07,20.03;68.41,19.88;68.57,17.99;68.01,17.73;68.01,16.77;67.30,16.11;66.19,15.11;64.79,13.56;64.45,13.92;64.05,13.57;64.07,12.58;63.13,11.93;61.80,11.99;61.29,12.63;60.12,12.30;59.43,11.47;58.86,11.03;59.47,10.36;58.31,8.38;58.08,7.05;58.59,5.67;59.66,5.31;61.97,4.99;62.61,5.91;63.45,8.55;64.49,10.53;65.88,12.36;67.81,14.76;68.56,16.44;69.82,19.18;70.26,21.38;70.20,23.02;71.03,24.55;70.99,26.37;71.19,28.17;70.45,31.29;70.19,30.01;69.56,31.10",
    "80.06,27.41;79.52,25.92;79.40,23.02;79.57,20.08;79.84,19.90;79.86,18.46;80.32,17.37;80.60,20.46;80.36,21.91;80.66,22.92;80.41,25.45;80.06,27.41",
    "77.85,24.72;77.44,22.49;77.68,20.73;77.94,21.42;78.25,20.81;78.45,22.88;78.08,23.28;77.85,24.72",
    "82.63,-46.76;83.23,-43.41;83.18,-39.90;83.55,-38.62;83.65,-35.09;83.52,-27.10;82.73,-20.85;82.34,-22.69;82.30,-26.52;82.20,-31.90;82.02,-31.40;82.13,-27.86;81.79,-24.84;82.09,-22.90;81.73,-22.07;81.15,-23.17;81.52,-20.62;81.91,-15.77;81.72,-12.77;81.29,-12.21;80.58,-16.29;80.35,-16.85;80.18,-20.05;80.13,-17.73;79.40,-18.90;78.75,-19.70;77.64,-19.67;76.99,-18.47;76.94,-20.04;76.63,-21.68;76.10,-19.83;75.25,-19.60;75.16,-20.67;74.30,-19.37;74.22,-21.59;73.82,-20.43;73.46,-20.76;73.31,-22.17;73.31,-23.57;72.63,-22.31;72.18,-22.30;72.60,-24.28;72.33,-24.79;72.08,-23.44;71.47,-22.13;70.66,-21.75;70.47,-23.54;70.86,-24.31;71.43,-25.54;70.75,-25.20;70.23,-26.36;70.18,-23.73;70.13,-22.35;69.26,-25.03;68.47,-27.75;68.13,-30.67;68.12,-31.78;67.74,-32.81;66.68,-34.20;65.98,-36.35;65.94,-37.04;65.69,-38.38;65.46,-39.81;64.84,-40.67;64.14,-40.68;63.48,-41.19;62.68,-42.82;61.90,-42.42;61.07,-42.87;60.10,-43.38;60.04,-44.79;60.85,-46.26;60.86,-48.26;61.41,-49.23;62.38,-49.90;63.63,-51.63;64.28,-52.14;65.18,-52.28;66.10,-53.66;66.84,-53.30;67.19,-53.97;68.36,-52.98;68.73,-51.48;69.15,-51.08;69.93,-50.87;69.57,-52.01;69.43,-52.56;69.28,-53.46;69.61,-54.68;70.29,-54.75;70.82,-54.36;70.84,-53.43;70.57,-51.39;71.20,-53.11;71.55,-54.00;71.41,-55.00;71.65,-55.83;72.59,-54.72;72.96,-55.33;73.65,-56.12;74.71,-57.32;75.10,-58.60;75.52,-58.59;76.10,-61.27;76.18,-63.39;76.13,-66.06;76.06,-68.50;76.38,-69.66;77.01,-71.40;77.32,-68.78;77.38,-66.76;77.64,-71.04;78.04,-73.30;78.43,-73.16;78.91,-69.37;79.39,-65.71;79.76,-65.32;80.12,-68.02;80.52,-67.15;81.21,-63.69;81.32,-62.23;81.77,-62.65;82.03,-60.28;82.19,-57.21;82.20,-54.13;81.89,-53.04;82.44,-50.39;82.06,-48.00;81.99,-46.60;81.66,-44.52;82.20,-46.90;82.63,-46.76",
    "-48.63,68.94;-48.94,69.58;-49.07,70.53;-49.26,70.56;-49.71,70.28;-49.78,68.75;-49.24,68.72;-48.83,68.87;-48.63,68.94",
    "-8.89,124.97;-8.66,125.09;-8.43,125.95;-8.40,126.64;-8.27,126.96;-8.40,127.34;-8.67,126.97;-9.11,125.93;-9.39,125.09;-9.09,125.07;-8.89,124.97",
    "-28.58,16.34;-28.08,16.82;-28.36,17.22;-28.78,17.39;-28.86,17.84;-29.05,18.46;-28.97,19.00;-28.46,19.89;-24.77,19.90;-24.92,20.17;-25.87,20.76;-26.48,20.67;-26.83,20.89;-26.73,21.61;-26.28,22.11;-25.98,22.58;-25.50,22.82;-25.27,23.31;-25.39,23.73;-25.67,24.21;-25.72,25.03;-25.49,25.66;-25.17,25.77;-24.70,25.94;-24.62,26.49;-24.24,26.79;-23.57,27.12;-22.83,28.02;-22.09,29.43;-22.10,29.84;-22.27,30.32;-22.15,30.66;-22.25,31.19;-23.66,31.67;-24.37,31.93;-25.48,31.75;-25.84,31.84;-25.66,31.33;-25.73,31.04;-26.02,30.95;-26.40,30.68;-26.74,30.69;-27.29,31.28;-27.18,31.87;-26.73,32.07;-26.74,32.83;-27.47,32.58;-28.30,32.46;-28.75,32.20;-29.26,31.52;-29.40,31.33;-29.91,30.90;-30.42,30.62;-31.14,30.06;-32.17,28.93;-32.77,28.22;-33.23,27.46;-33.61,26.42;-33.67,25.91;-33.94,25.78;-33.80,25.17;-33.99,24.68;-33.79,23.59;-33.92,22.99;-33.86,22.57;-34.26,21.54;-34.42,20.69;-34.80,20.07;-34.82,19.62;-34.46,19.19;-34.44,18.86;-34.00,18.42;-34.14,18.38;-33.87,18.24;-33.28,18.25;-32.61,17.93;-32.43,18.25;-31.66,18.22;-30.73,17.57;-29.88,17.06;-29.88,17.06;-28.58,16.34",
    "-28.96,28.98;-28.65,28.54;-28.85,28.07;-29.24,27.53;-29.88,27.00;-30.65,27.75;-30.55,28.11;-30.23,28.29;-30.07,28.85;-29.74,29.02;-29.26,29.33;-28.96,28.98",
    "-28.96,28.98;-29.26,29.33;-29.74,29.02;-30.07,28.85;-30.23,28.29;-30.55,28.11;-30.65,27.75;-29.88,27.00;-29.24,27.53;-28.85,28.07;-28.65,28.54;-28.96,28.98",
    "32.54,-117.13;32.61,-115.99;32.72,-114.72;32.53,-114.81;32.04,-113.30;31.33,-111.02;31.34,-109.04;31.34,-108.24;31.75,-108.24;31.75,-106.51;31.40,-106.14;31.08,-105.63;30.64,-105.04;30.12,-104.71;29.57,-104.46;29.27,-103.94;28.97,-103.11;29.76,-102.48;29.78,-101.66;29.38,-100.96;28.70,-100.46;28.11,-100.11;27.54,-99.52;26.84,-99.30;26.37,-99.02;26.06,-98.24;25.84,-97.53;25.87,-97.14;24.99,-97.53;24.27,-97.70;22.93,-97.78;22.44,-97.87;21.90,-97.70;21.41,-97.39;20.64,-97.19;19.89,-96.53;19.32,-96.29;18.83,-95.90;18.56,-94.84;18.14,-94.43;18.42,-93.55;18.52,-92.79;18.70,-92.04;18.88,-91.41;19.28,-90.77;19.87,-90.53;20.71,-90.45;21.00,-90.28;21.26,-89.60;21.49,-88.54;21.46,-87.66;21.54,-87.05;21.33,-86.81;20.85,-86.85;20.26,-87.38;19.65,-87.62;19.47,-87.44;19.04,-87.59;18.26,-87.84;18.52,-88.09;18.50,-88.30;18.49,-88.49;17.88,-88.85;18.00,-89.03;17.96,-89.15;17.81,-89.14;17.82,-90.07;17.82,-91.00;17.25,-91.00;17.25,-91.45;16.92,-91.08;16.69,-90.71;16.47,-90.60;16.41,-90.44;16.07,-90.46;16.07,-91.75;15.25,-92.23;15.06,-92.09;14.83,-92.20;14.54,-92.23;15.62,-93.36;15.94,-93.88;16.20,-94.69;16.13,-95.25;15.75,-96.05;15.65,-96.56;15.92,-97.26;16.11,-98.01;16.57,-98.95;16.71,-99.70;17.17,-100.83;17.65,-101.67;17.92,-101.92;17.98,-102.48;18.29,-103.50;18.75,-103.92;19.32,-104.99;19.95,-105.49;20.43,-105.73;20.53,-105.40;20.82,-105.50;21.08,-105.27;21.42,-105.27;21.87,-105.60;22.27,-105.69;22.77,-106.03;23.77,-106.91;24.55,-107.92;25.17,-108.40;25.58,-109.26;25.82,-109.44;26.44,-109.29;26.68,-109.80;27.16,-110.39;27.86,-110.64;27.94,-111.18;28.47,-111.76;28.95,-112.23;29.27,-112.27;30.02,-112.81;30.79,-113.16;31.17,-113.15;31.57,-113.87;31.52,-114.21;31.80,-114.78;31.39,-114.94;30.91,-114.77;30.16,-114.67;29.75,-114.33;29.06,-113.59;28.83,-113.42;28.75,-113.27;28.41,-113.14;28.43,-112.96;27.78,-112.76;27.53,-112.46;27.17,-112.24;26.66,-111.62;25.73,-111.28;25.29,-110.99;24.83,-110.71;24.30,-110.66;24.27,-110.17;23.81,-109.77;23.36,-109.41;23.19,-109.43;22.82,-109.85;22.82,-110.03;23.43,-110.30;24.00,-110.95;24.48,-111.67;24.74,-112.18;25.47,-112.15;26.01,-112.30;26.32,-112.78;26.77,-113.46;26.64,-113.60;26.90,-113.85;27.14,-114.47;27.72,-115.06;27.80,-114.98;27.74,-114.57;28.12,-114.20;28.57,-114.16;29.28,-114.93;29.56,-115.52;30.18,-115.89;30.84,-116.26;31.64,-116.72;32.54,-117.13",
    "-30.22,-57.63;-30.11,-56.98;-30.88,-55.97;-30.85,-55.60;-31.49,-54.57;-32.05,-53.79;-32.73,-53.21;-33.20,-53.65;-33.77,-53.37;-34.40,-53.81;-34.95,-54.94;-34.75,-55.67;-34.86,-56.22;-34.43,-57.14;-34.46,-57.82;-33.91,-58.43;-33.26,-58.35;-33.04,-58.13;-32.04,-58.14;-31.02,-57.87;-30.22,-57.63",
    "-33.77,-53.37;-33.20,-53.65;-32.73,-53.21;-32.05,-53.79;-31.49,-54.57;-30.85,-55.60;-30.88,-55.97;-30.11,-56.98;-30.22,-57.63;-28.85,-56.29;-27.88,-55.16;-27.47,-54.49;-26.92,-53.65;-26.12,-53.63;-25.55,-54.13;-25.74,-54.63;-25.16,-54.43;-24.57,-54.29;-24.02,-54.29;-23.84,-54.65;-24.00,-55.03;-23.96,-55.40;-23.57,-55.52;-22.66,-55.61;-22.36,-55.80;-22.09,-56.47;-22.28,-56.88;-22.09,-57.94;-20.73,-57.87;-20.18,-58.17;-19.97,-57.85;-19.40,-57.95;-18.96,-57.68;-18.17,-57.50;-17.55,-57.73;-17.27,-58.28;-16.88,-58.39;-16.30,-58.24;-16.26,-60.16;-15.09,-60.54;-15.08,-60.25;-14.65,-60.26;-14.35,-60.46;-13.78,-60.50;-13.48,-61.08;-13.49,-61.71;-13.20,-62.13;-13.00,-62.80;-12.63,-63.20;-12.46,-64.32;-11.57,-65.40;-10.90,-65.32;-10.51,-65.44;-9.76,-65.34;-9.93,-66.65;-10.31,-67.17;-10.71,-68.05;-11.01,-68.27;-11.04,-68.79;-10.95,-69.53;-11.12,-70.09;-11.01,-70.55;-9.49,-70.48;-10.08,-71.30;-10.05,-72.18;-9.52,-72.56;-9.46,-73.23;-9.03,-73.02;-8.42,-73.57;-7.52,-73.99;-7.34,-73.72;-6.92,-73.72;-6.63,-73.12;-6.09,-73.22;-5.74,-72.96;-5.27,-72.89;-4.59,-71.75;-4.40,-70.93;-4.25,-70.79;-4.30,-69.89;-1.56,-69.44;-1.12,-69.42;-0.55,-69.58;-0.19,-70.02;0.54,-70.02;0.71,-69.45;0.60,-69.25;0.99,-69.22;1.09,-69.80;1.71,-69.82;1.69,-67.87;2.04,-67.54;1.72,-67.26;1.13,-67.07;1.25,-66.88;0.72,-66.33;0.79,-65.55;1.10,-65.35;1.33,-64.61;1.49,-64.20;1.92,-64.08;2.20,-63.37;2.41,-63.42;2.50,-64.27;3.13,-64.41;3.80,-64.37;4.06,-64.82;4.15,-64.63;4.02,-63.89;3.77,-63.09;4.01,-62.80;4.16,-62.09;4.54,-60.97;4.92,-60.60;5.20,-60.73;5.24,-60.21;5.01,-59.98;4.57,-60.11;4.42,-59.77;3.96,-59.54;3.61,-59.82;2.76,-59.97;2.25,-59.72;1.79,-59.65;1.32,-59.03;1.27,-58.54;1.46,-58.43;1.51,-58.11;1.68,-57.66;1.95,-57.34;1.86,-56.78;1.90,-56.54;1.82,-56.00;2.02,-55.91;2.22,-56.07;2.51,-55.97;2.42,-55.57;2.52,-55.10;2.31,-54.52;2.11,-54.09;2.38,-53.78;2.33,-53.55;2.05,-53.42;2.12,-52.94;2.50,-52.56;3.24,-52.25;4.16,-51.66;4.20,-51.32;3.65,-51.07;1.90,-50.51;1.74,-49.97;1.05,-49.95;0.22,-50.70;-0.08,-50.39;-0.24,-48.62;-1.24,-48.58;-0.58,-47.82;-0.94,-46.57;-1.55,-44.91;-2.14,-44.42;-2.69,-44.58;-2.38,-43.42;-2.91,-41.47;-2.87,-39.98;-3.70,-38.50;-4.82,-37.22;-5.11,-36.45;-5.15,-35.60;-5.46,-35.24;-6.74,-34.90;-7.34,-34.73;-9.00,-35.13;-9.65,-35.64;-11.04,-37.05;-12.17,-37.68;-13.04,-38.42;-13.06,-38.67;-13.79,-38.95;-15.67,-38.88;-17.21,-39.16;-17.87,-39.27;-18.26,-39.58;-19.60,-39.76;-20.90,-40.77;-21.94,-40.94;-22.37,-41.75;-22.97,-41.99;-22.97,-43.07;-23.35,-44.65;-23.80,-45.35;-24.09,-46.47;-24.89,-47.65;-25.88,-48.50;-26.62,-48.64;-27.18,-48.47;-28.19,-48.66;-28.67,-48.89;-29.22,-49.59;-30.98,-50.70;-31.78,-51.58;-32.25,-52.26;-33.20,-52.71;-33.77,-53.37",
    "-10.95,-69.53;-11.04,-68.79;-11.01,-68.27;-10.71,-68.05;-10.31,-67.17;-9.93,-66.65;-9.76,-65.34;-10.51,-65.44;-10.90,-65.32;-11.57,-65.40;-12.46,-64.32;-12.63,-63.20;-13.00,-62.80;-13.20,-62.13;-13.49,-61.71;-13.48,-61.08;-13.78,-60.50;-14.35,-60.46;-14.65,-60.26;-15.08,-60.25;-15.09,-60.54;-16.26,-60.16;-16.30,-58.24;-16.88,-58.39;-17.27,-58.28;-17.55,-57.73;-18.17,-57.50;-18.96,-57.68;-19.40,-57.95;-19.97,-57.85;-20.18,-58.17;-19.87,-58.18;-19.36,-59.12;-19.34,-60.04;-19.63,-61.79;-20.51,-62.27;-21.05,-62.29;-22.25,-62.69;-22.03,-62.85;-21.99,-63.99;-22.80,-64.38;-22.08,-64.96;-21.83,-66.27;-22.74,-67.11;-22.87,-67.83;-21.49,-68.22;-20.37,-68.76;-19.41,-68.44;-18.98,-68.97;-18.26,-69.10;-17.58,-69.59;-16.50,-68.96;-15.66,-69.39;-15.32,-69.16;-14.95,-69.34;-14.45,-68.95;-13.60,-68.93;-12.90,-68.88;-12.56,-68.67;-10.95,-69.53",
    "-4.30,-69.89;-4.25,-70.79;-4.40,-70.93;-4.59,-71.75;-5.27,-72.89;-5.74,-72.96;-6.09,-73.22;-6.63,-73.12;-6.92,-73.72;-7.34,-73.72;-7.52,-73.99;-8.42,-73.57;-9.03,-73.02;-9.46,-73.23;-9.52,-72.56;-10.05,-72.18;-10.08,-71.30;-9.49,-70.48;-11.01,-70.55;-11.12,-70.09;-10.95,-69.53;-12.56,-68.67;-12.90,-68.88;-13.60,-68.93;-14.45,-68.95;-14.95,-69.34;-15.32,-69.16;-15.66,-69.39;-16.50,-68.96;-17.58,-69.59;-18.09,-69.86;-18.35,-70.37;-17.77,-71.38;-17.36,-71.46;-16.36,-73.44;-15.27,-75.24;-14.65,-76.01;-13.82,-76.42;-13.54,-76.26;-12.22,-77.11;-10.38,-78.09;-8.39,-79.04;-7.93,-79.45;-7.19,-79.76;-6.54,-80.54;-6.14,-81.25;-5.69,-80.93;-4.74,-81.41;-4.04,-81.10;-3.40,-80.30;-3.82,-80.18;-4.06,-80.47;-4.43,-80.44;-4.35,-80.03;-4.45,-79.62;-4.96,-79.21;-4.55,-78.64;-3.87,-78.45;-3.00,-77.84;-2.61,-76.64;-1.56,-75.54;-0.91,-75.23;-0.15,-75.37;-0.06,-75.11;-0.53,-74.44;-1.00,-74.12;-1.26,-73.66;-2.31,-73.07;-2.43,-72.33;-2.17,-71.77;-2.34,-71.41;-2.26,-70.81;-2.73,-70.05;-3.74,-70.69;-3.77,-70.39;-4.30,-69.89",
    "1.25,-66.88;1.13,-67.07;1.72,-67.26;2.04,-67.54;1.69,-67.87;1.71,-69.82;1.09,-69.80;0.99,-69.22;0.60,-69.25;0.71,-69.45;0.54,-70.02;-0.19,-70.02;-0.55,-69.58;-1.12,-69.42;-1.56,-69.44;-4.30,-69.89;-3.77,-70.39;-3.74,-70.69;-2.73,-70.05;-2.26,-70.81;-2.34,-71.41;-2.17,-71.77;-2.43,-72.33;-2.31,-73.07;-1.26,-73.66;-1.00,-74.12;-0.53,-74.44;-0.06,-75.11;-0.15,-75.37;0.08,-75.80;0.42,-76.29;0.26,-76.58;0.40,-77.42;0.83,-77.67;0.81,-77.86;1.38,-78.86;1.69,-78.99;1.77,-78.62;2.27,-78.66;2.63,-78.43;2.70,-77.93;3.33,-77.51;3.85,-77.13;4.09,-77.50;4.67,-77.31;5.58,-77.53;5.85,-77.32;6.69,-77.48;7.22,-77.88;7.71,-77.75;7.64,-77.43;7.94,-77.24;8.52,-77.47;8.67,-77.35;8.64,-76.84;9.34,-76.09;9.44,-75.67;9.77,-75.66;10.62,-75.48;11.08,-74.91;11.10,-74.28;11.31,-74.20;11.23,-73.41;11.73,-72.63;11.96,-72.24;12.44,-71.75;12.38,-71.40;12.11,-71.14;11.78,-71.33;11.61,-71.97;11.11,-72.23;10.82,-72.61;10.45,-72.91;9.74,-73.03;9.15,-73.30;9.09,-72.79;8.63,-72.66;8.41,-72.44;8.00,-72.36;7.63,-72.48;7.42,-72.44;7.34,-72.20;6.99,-71.96;7.09,-70.67;6.96,-70.09;6.10,-69.39;6.21,-68.99;6.15,-68.27;6.27,-67.70;6.10,-67.34;5.56,-67.52;5.22,-67.74;4.50,-67.82;3.84,-67.62;3.54,-67.34;3.32,-67.30;2.82,-67.81;2.60,-67.45;2.25,-67.18;1.25,-66.88",
    "8.67,-77.35;8.52,-77.47;7.94,-77.24;7.64,-77.43;7.71,-77.75;7.22,-77.88;7.51,-78.21;8.05,-78.43;8.32,-78.18;8.39,-78.44;8.72,-78.62;9.00,-79.12;8.93,-79.56;8.58,-79.76;8.33,-80.16;8.30,-80.38;8.09,-80.48;7.55,-80.00;7.42,-80.28;7.27,-80.42;7.22,-80.89;7.82,-81.06;7.65,-81.19;7.71,-81.52;8.11,-81.72;8.18,-82.13;8.29,-82.39;8.29,-82.82;8.07,-82.85;8.23,-82.97;8.42,-82.91;8.63,-82.83;8.81,-82.87;8.93,-82.72;9.07,-82.93;9.48,-82.93;9.57,-82.55;9.21,-82.19;9.00,-82.21;8.95,-81.81;9.03,-81.71;8.79,-81.44;8.86,-80.95;9.11,-80.52;9.31,-79.91;9.61,-79.57;9.55,-79.02;9.45,-79.06;9.42,-78.50;9.25,-78.06;8.95,-77.73;8.67,-77.35",
    "9.57,-82.55;9.48,-82.93;9.07,-82.93;8.93,-82.72;8.81,-82.87;8.63,-82.83;8.42,-82.91;8.23,-82.97;8.45,-83.51;8.66,-83.71;8.83,-83.60;9.05,-83.63;9.29,-83.91;9.49,-84.30;9.62,-84.65;9.91,-84.71;10.09,-84.98;9.80,-84.91;9.56,-85.11;9.83,-85.34;9.93,-85.66;10.13,-85.80;10.44,-85.79;10.75,-85.66;10.90,-85.94;11.09,-85.71;11.22,-85.56;10.95,-84.90;11.08,-84.67;11.00,-84.36;10.79,-84.19;10.73,-83.90;10.94,-83.66;10.40,-83.40;9.99,-83.02;9.57,-82.55",
    "10.94,-83.66;10.73,-83.90;10.79,-84.19;11.00,-84.36;11.08,-84.67;10.95,-84.90;11.22,-85.56;11.09,-85.71;11.40,-86.06;11.81,-86.53;12.14,-86.75;12.46,-87.17;12.91,-87.67;13.06,-87.56;12.91,-87.39;12.98,-87.32;13.03,-87.01;13.25,-86.88;13.26,-86.73;13.75,-86.76;13.78,-86.52;13.77,-86.31;14.04,-86.10;13.84,-85.80;13.96,-85.70;14.08,-85.51;14.35,-85.17;14.56,-85.15;14.55,-85.05;14.79,-84.92;14.82,-84.82;14.67,-84.65;14.62,-84.45;14.75,-84.23;14.75,-83.98;14.88,-83.63;15.02,-83.49;15.00,-83.15;14.90,-83.23;14.68,-83.28;14.31,-83.18;13.97,-83.41;13.57,-83.52;13.13,-83.55;12.87,-83.50;12.42,-83.47;12.32,-83.63;11.89,-83.72;11.63,-83.65;11.37,-83.86;11.10,-83.81;10.94,-83.66",
    "15.00,-83.15;15.02,-83.49;14.88,-83.63;14.75,-83.98;14.75,-84.23;14.62,-84.45;14.67,-84.65;14.82,-84.82;14.79,-84.92;14.55,-85.05;14.56,-85.15;14.35,-85.17;14.08,-85.51;13.96,-85.70;13.84,-85.80;14.04,-86.10;13.77,-86.31;13.78,-86.52;13.75,-86.76;13.26,-86.73;13.25,-86.88;13.03,-87.01;12.98,-87.32;13.30,-87.49;13.38,-87.79;13.79,-87.72;13.89,-87.86;13.96,-88.07;13.85,-88.50;13.98,-88.54;14.14,-88.84;14.34,-89.06;14.42,-89.35;14.68,-89.15;14.87,-89.23;15.07,-89.15;15.35,-88.68;15.73,-88.23;15.69,-88.12;15.86,-87.90;15.88,-87.62;15.80,-87.52;15.85,-87.37;15.76,-86.90;15.78,-86.44;15.89,-86.12;16.01,-86.00;15.95,-85.68;15.89,-85.44;15.91,-85.18;16.00,-84.98;15.86,-84.53;15.84,-84.37;15.65,-84.06;15.42,-83.77;15.27,-83.41;15.00,-83.15",
    "14.42,-89.35;14.34,-89.06;14.14,-88.84;13.98,-88.54;13.85,-88.50;13.96,-88.07;13.89,-87.86;13.79,-87.72;13.38,-87.79;13.15,-87.90;13.16,-88.48;13.26,-88.84;13.46,-89.26;13.52,-89.81;13.74,-90.10;13.88,-90.06;14.13,-89.72;14.24,-89.53;14.36,-89.59;14.42,-89.35",
    "14.54,-92.23;14.83,-92.20;15.06,-92.09;15.25,-92.23;16.07,-91.75;16.07,-90.46;16.41,-90.44;16.47,-90.60;16.69,-90.71;16.92,-91.08;17.25,-91.45;17.25,-91.00;17.82,-91.00;17.82,-90.07;17.81,-89.14;17.02,-89.15;15.89,-89.23;15.89,-88.93;15.71,-88.60;15.86,-88.52;15.73,-88.23;15.35,-88.68;15.07,-89.15;14.87,-89.23;14.68,-89.15;14.42,-89.35;14.36,-89.59;14.24,-89.53;14.13,-89.72;13.88,-90.06;13.74,-90.10;13.91,-90.61;13.93,-91.23;14.13,-91.69;14.54,-92.23",
    "17.81,-89.14;17.96,-89.15;18.00,-89.03;17.88,-88.85;18.49,-88.49;18.50,-88.30;18.35,-88.30;18.35,-88.11;18.08,-88.12;17.64,-88.29;17.49,-88.20;17.13,-88.30;17.04,-88.24;16.53,-88.36;16.27,-88.55;16.23,-88.73;15.89,-88.93;15.89,-89.23;17.02,-89.15;17.81,-89.14",
    "5.20,-60.73;4.92,-60.60;4.54,-60.97;4.16,-62.09;4.01,-62.80;3.77,-63.09;4.02,-63.89;4.15,-64.63;4.06,-64.82;3.80,-64.37;3.13,-64.41;2.50,-64.27;2.41,-63.42;2.20,-63.37;1.92,-64.08;1.49,-64.20;1.33,-64.61;1.10,-65.35;0.79,-65.55;0.72,-66.33;1.25,-66.88;2.25,-67.18;2.60,-67.45;2.82,-67.81;3.32,-67.30;3.54,-67.34;3.84,-67.62;4.50,-67.82;5.22,-67.74;5.56,-67.52;6.10,-67.34;6.27,-67.70;6.15,-68.27;6.21,-68.99;6.10,-69.39;6.96,-70.09;7.09,-70.67;6.99,-71.96;7.34,-72.20;7.42,-72.44;7.63,-72.48;8.00,-72.36;8.41,-72.44;8.63,-72.66;9.09,-72.79;9.15,-73.30;9.74,-73.03;10.45,-72.91;10.82,-72.61;11.11,-72.23;11.61,-71.97;11.78,-71.33;11.54,-71.36;11.42,-71.95;10.97,-71.62;10.45,-71.63;9.87,-72.07;9.07,-71.70;9.14,-71.26;9.86,-71.04;10.21,-71.35;10.97,-71.40;11.38,-70.16;11.85,-70.29;12.16,-69.94;11.46,-69.58;11.44,-68.88;10.89,-68.23;10.55,-68.19;10.55,-67.30;10.65,-66.23;10.20,-65.66;10.08,-64.89;10.39,-64.33;10.64,-64.32;10.70,-63.08;10.72,-61.88;10.42,-62.73;9.95,-62.39;9.87,-61.59;9.38,-60.83;8.58,-60.67;8.60,-60.15;8.37,-59.76;7.78,-60.55;7.41,-60.64;7.04,-60.30;6.86,-60.54;6.70,-61.16;6.23,-61.14;5.96,-61.41;5.20,-60.73",
    "1.90,-56.54;1.86,-56.78;1.95,-57.34;1.68,-57.66;1.51,-58.11;1.46,-58.43;1.27,-58.54;1.32,-59.03;1.79,-59.65;2.25,-59.72;2.76,-59.97;3.61,-59.82;3.96,-59.54;4.42,-59.77;4.57,-60.11;5.01,-59.98;5.24,-60.21;5.20,-60.73;5.96,-61.41;6.23,-61.14;6.70,-61.16;6.86,-60.54;7.04,-60.30;7.41,-60.64;7.78,-60.55;8.37,-59.76;8.00,-59.10;7.35,-58.48;6.83,-58.45;6.81,-58.08;6.32,-57.54;5.97,-57.15;5.07,-57.31;4.81,-57.91;4.58,-57.86;4.06,-58.04;3.33,-57.60;3.33,-57.28;2.77,-57.15;1.90,-56.54",
    "2.31,-54.52;2.52,-55.10;2.42,-55.57;2.51,-55.97;2.22,-56.07;2.02,-55.91;1.82,-56.00;1.90,-56.54;2.77,-57.15;3.33,-57.28;3.33,-57.60;4.06,-58.04;4.58,-57.86;4.81,-57.91;5.07,-57.31;5.97,-57.15;5.77,-55.95;5.95,-55.84;6.03,-55.03;5.76,-53.96;4.90,-54.48;4.21,-54.40;3.62,-54.01;3.19,-54.18;2.73,-54.27;2.31,-54.52",
    "4.16,-51.66;3.24,-52.25;2.50,-52.56;2.12,-52.94;2.05,-53.42;2.33,-53.55;2.38,-53.78;2.11,-54.09;2.31,-54.52;2.73,-54.27;3.19,-54.18;3.62,-54.01;4.21,-54.40;4.90,-54.48;5.76,-53.96;5.65,-53.62;5.41,-52.88;4.57,-51.82;4.16,-51.66",
    "49.46,6.19;49.20,6.66;49.02,8.10;48.33,7.59;47.62,7.47;47.45,7.19;47.54,6.74;47.29,6.77;46.73,6.04;46.27,6.02;46.43,6.50;45.99,6.84;45.71,6.80;45.33,7.10;45.03,6.75;44.25,7.01;44.13,7.55;43.69,7.44;43.13,6.53;43.40,4.56;43.08,3.10;42.47,2.99;42.34,1.83;42.80,0.70;42.58,0.34;43.03,-1.50;43.42,-1.90;44.02,-1.38;46.01,-1.19;47.06,-2.23;47.57,-2.96;47.95,-4.49;48.68,-4.59;48.90,-3.30;48.64,-1.62;49.78,-1.93;49.35,-0.99;50.13,1.34;50.95,1.64;51.15,2.51;50.80,2.66;50.78,3.12;50.38,3.59;49.91,4.29;49.99,4.80;49.53,5.67;49.44,5.90;49.46,6.19",
    "42.63,8.75;43.01,9.39;42.15,9.56;41.38,9.23;41.58,8.78;42.26,8.54;42.63,8.75",
    "-0.15,-75.37;-0.91,-75.23;-1.56,-75.54;-2.61,-76.64;-3.00,-77.84;-3.87,-78.45;-4.55,-78.64;-4.96,-79.21;-4.45,-79.62;-4.35,-80.03;-4.43,-80.44;-4.06,-80.47;-3.82,-80.18;-3.40,-80.30;-2.66,-79.77;-2.22,-79.99;-2.69,-80.37;-2.25,-80.97;-1.97,-80.76;-1.06,-80.93;-0.91,-80.58;-0.28,-80.40;0.36,-80.02;0.77,-80.09;0.98,-79.54;1.38,-78.86;0.81,-77.86;0.83,-77.67;0.40,-77.42;0.26,-76.58;0.42,-76.29;0.08,-75.80;-0.15,-75.37",
    "18.51,-66.28;18.43,-65.77;18.23,-65.59;17.98,-65.85;17.98,-66.60;17.95,-67.18;18.37,-67.24;18.52,-67.10;18.51,-66.28",
    "18.49,-77.57;18.40,-76.90;18.16,-76.37;17.89,-76.20;17.87,-76.90;17.70,-77.21;17.86,-77.77;18.23,-78.34;18.45,-78.22;18.52,-77.80;18.49,-77.57",
    "23.19,-82.27;23.12,-81.40;23.11,-80.62;22.77,-79.68;22.40,-79.28;22.51,-78.35;22.28,-77.99;21.66,-77.15;21.21,-76.52;21.22,-76.19;21.02,-75.60;20.74,-75.67;20.69,-74.93;20.28,-74.18;20.05,-74.30;19.92,-74.96;19.87,-75.63;19.95,-76.32;19.86,-77.76;20.41,-77.09;20.67,-77.49;20.74,-78.14;21.03,-78.48;21.60,-78.72;21.56,-79.28;21.83,-80.22;22.04,-80.52;22.19,-81.82;22.39,-82.17;22.64,-81.80;22.69,-82.78;22.17,-83.49;22.15,-83.91;21.91,-84.05;21.80,-84.55;21.90,-84.97;22.20,-84.45;22.57,-84.23;22.79,-83.78;22.98,-83.27;23.08,-82.51;23.19,-82.27",
    "-22.25,31.19;-22.15,30.66;-22.27,30.32;-22.10,29.84;-22.09,29.43;-21.64,28.79;-21.49,28.02;-20.85,27.73;-20.50,27.72;-20.39,27.30;-19.29,26.16;-18.71,25.85;-18.54,25.65;-17.74,25.26;-17.85,26.38;-17.96,26.71;-17.94,27.04;-17.29,27.60;-16.47,28.47;-16.39,28.83;-16.04,28.95;-15.64,29.52;-15.51,30.27;-15.88,30.34;-15.86,31.17;-16.07,31.64;-16.32,31.85;-16.39,32.33;-16.71,32.85;-17.98,32.85;-18.67,32.65;-19.42,32.61;-19.72,32.77;-20.30,32.66;-20.40,32.51;-21.12,32.24;-22.25,31.19",
    "-22.09,29.43;-22.83,28.02;-23.57,27.12;-24.24,26.79;-24.62,26.49;-24.70,25.94;-25.17,25.77;-25.49,25.66;-25.72,25.03;-25.67,24.21;-25.39,23.73;-25.27,23.31;-25.50,22.82;-25.98,22.58;-26.28,22.11;-26.73,21.61;-26.83,20.89;-26.48,20.67;-25.87,20.76;-24.92,20.17;-24.77,19.90;-21.85,19.90;-21.81,20.88;-18.25,20.91;-18.22,21.66;-17.87,23.20;-18.28,23.58;-17.89,24.22;-17.89,24.52;-17.66,25.08;-17.74,25.26;-18.54,25.65;-18.71,25.85;-19.29,26.16;-20.39,27.30;-20.50,27.72;-20.85,27.73;-21.49,28.02;-21.64,28.79;-22.09,29.43",
    "-24.77,19.90;-28.46,19.89;-28.97,19.00;-29.05,18.46;-28.86,17.84;-28.78,17.39;-28.36,17.22;-28.08,16.82;-28.58,16.34;-27.82,15.60;-27.09,15.21;-26.12,14.99;-25.39,14.74;-23.85,14.41;-22.66,14.39;-22.11,14.26;-21.70,13.87;-20.87,13.35;-19.67,12.83;-19.05,12.61;-18.07,11.79;-17.30,11.73;-17.11,12.22;-16.94,12.81;-16.97,13.46;-17.42,14.06;-17.35,14.21;-17.31,18.26;-17.79,18.96;-17.93,21.38;-17.52,23.22;-17.30,24.03;-17.35,24.68;-17.58,25.08;-17.66,25.08;-17.89,24.52;-17.89,24.22;-18.28,23.58;-17.87,23.20;-18.22,21.66;-18.25,20.91;-21.81,20.88;-21.85,19.90;-24.77,19.90",
    "13.59,-16.71;14.37,-17.13;14.73,-17.63;14.92,-17.19;15.62,-16.70;16.14,-16.46;16.46,-16.12;16.37,-15.62;16.59,-15.14;16.60,-14.58;16.30,-14.10;16.04,-13.44;15.30,-12.83;14.62,-12.17;13.99,-12.12;13.42,-11.93;13.14,-11.55;12.75,-11.47;12.44,-11.51;12.39,-11.66;12.47,-12.20;12.35,-12.28;12.33,-12.50;12.58,-13.22;12.59,-13.70;12.63,-15.55;12.52,-15.82;12.55,-16.15;12.38,-16.68;13.15,-16.84;13.13,-15.93;13.27,-15.69;13.28,-15.51;13.51,-15.14;13.30,-14.71;13.28,-14.28;13.51,-13.84;13.79,-14.05;13.63,-14.38;13.63,-14.69;13.88,-15.08;13.86,-15.40;13.62,-15.62;13.59,-16.71",
    "12.44,-11.51;12.75,-11.47;13.14,-11.55;13.42,-11.93;13.99,-12.12;14.62,-12.17;14.80,-11.83;15.39,-11.67;15.41,-11.35;15.13,-10.65;15.33,-10.09;15.26,-9.70;15.49,-9.55;15.50,-5.54;16.20,-5.32;16.33,-5.49;20.64,-5.97;24.96,-6.45;24.97,-4.92;22.79,-1.55;20.61,1.82;20.14,2.06;19.86,2.68;19.69,3.15;19.06,3.16;19.16,4.27;16.85,4.27;16.18,3.72;15.57,3.64;15.41,2.75;15.32,1.39;14.97,1.02;14.93,0.37;14.92,-0.27;15.12,-0.52;14.97,-1.07;14.56,-2.00;14.25,-2.19;13.80,-2.97;13.54,-3.10;13.34,-3.52;13.47,-4.01;13.23,-4.28;12.54,-4.43;11.71,-5.22;11.38,-5.20;10.95,-5.47;10.37,-5.40;10.22,-5.82;10.10,-6.05;10.52,-6.21;10.41,-6.49;10.43,-6.67;10.14,-6.85;10.15,-7.62;10.30,-7.90;10.21,-8.03;10.49,-8.34;10.79,-8.28;10.91,-8.41;10.81,-8.62;11.14,-8.58;11.39,-8.38;11.81,-8.79;12.09,-8.91;12.31,-9.13;12.33,-9.33;12.19,-9.57;12.06,-9.89;11.84,-10.17;11.92,-10.59;12.18,-10.87;12.21,-11.04;12.08,-11.30;12.08,-11.46;12.44,-11.51",
    "21.00,-17.06;21.33,-16.85;21.33,-12.93;22.77,-13.12;23.28,-12.87;23.37,-11.94;25.93,-11.97;25.88,-8.69;27.40,-8.68;24.97,-4.92;24.96,-6.45;20.64,-5.97;16.33,-5.49;16.20,-5.32;15.50,-5.54;15.49,-9.55;15.26,-9.70;15.33,-10.09;15.13,-10.65;15.41,-11.35;15.39,-11.67;14.80,-11.83;14.62,-12.17;15.30,-12.83;16.04,-13.44;16.30,-14.10;16.60,-14.58;16.59,-15.14;16.37,-15.62;16.46,-16.12;16.14,-16.46;16.67,-16.55;17.17,-16.27;18.11,-16.15;19.10,-16.26;19.59,-16.38;20.09,-16.28;20.57,-16.54;21.00,-17.06",
    "6.26,2.69;6.14,1.87;6.83,1.62;9.13,1.66;9.33,1.46;9.83,1.43;10.18,1.08;10.47,0.77;11.00,0.90;11.11,1.24;11.55,1.45;11.64,1.94;11.94,2.15;12.23,2.49;12.24,2.85;11.66,3.61;11.33,3.57;10.73,3.80;10.33,3.60;10.06,3.71;9.44,3.22;9.14,2.91;8.51,2.72;7.87,2.75;6.26,2.69",
    "22.86,14.85;21.31,15.10;21.05,15.47;20.73,15.49;20.39,15.90;19.96,15.69;17.93,15.30;16.63,15.25;15.68,13.97;14.37,13.54;14.00,13.96;13.35,13.95;13.33,14.60;12.86,14.50;12.80,14.21;12.48,14.18;12.46,14.00;13.56,13.32;13.60,13.08;13.04,12.30;13.33,11.53;13.39,10.99;13.25,10.70;13.28,10.11;12.85,9.52;12.83,9.01;13.34,7.80;13.10,7.33;13.12,6.82;13.49,6.45;13.87,5.44;13.75,4.37;13.53,4.11;12.96,3.97;12.55,3.68;11.66,3.61;12.24,2.85;12.23,2.49;11.94,2.15;12.63,2.18;12.85,1.02;13.34,0.99;13.99,0.43;14.44,0.30;14.93,0.37;14.97,1.02;15.32,1.39;15.41,2.75;15.57,3.64;16.18,3.72;16.85,4.27;19.16,4.27;19.60,5.68;21.57,8.57;23.47,12.00;23.04,13.58;22.49,14.14;22.86,14.85",
    "6.26,2.69;7.87,2.75;8.51,2.72;9.14,2.91;9.44,3.22;10.06,3.71;10.33,3.60;10.73,3.80;11.33,3.57;11.66,3.61;12.55,3.68;12.96,3.97;13.53,4.11;13.75,4.37;13.87,5.44;13.49,6.45;13.12,6.82;13.10,7.33;13.34,7.80;12.83,9.01;12.85,9.52;13.28,10.11;13.25,10.70;13.39,10.99;13.33,11.53;13.04,12.30;13.60,13.08;13.56,13.32;12.46,14.00;12.48,14.18;12.09,14.58;11.90,14.47;11.57,14.42;10.80,13.57;10.16,13.31;9.64,13.17;9.42,12.96;8.72,12.75;8.31,12.22;7.80,12.06;7.40,11.84;6.98,11.75;6.64,11.06;7.06,10.50;7.04,10.12;6.45,9.52;6.44,9.23;5.48,8.76;4.77,8.50;4.41,7.46;4.46,7.08;4.24,6.70;4.26,5.90;4.89,5.36;5.61,5.03;6.27,4.33;6.26,3.57;6.26,2.69",
    "12.86,14.50;12.22,14.89;11.56,14.96;10.89,14.92;9.98,15.47;9.99,14.91;9.92,14.63;10.02,14.17;9.55,13.95;8.97,14.54;8.80,14.98;8.38,15.12;7.69,15.44;7.42,15.28;6.41,14.78;6.23,14.54;5.45,14.46;5.03,14.56;4.73,14.48;4.21,14.95;3.85,15.04;3.34,15.41;3.01,15.86;2.56,15.91;2.27,16.01;1.73,15.94;1.96,15.15;2.23,14.34;2.27,13.08;2.32,12.95;2.19,12.36;2.33,11.75;2.26,11.28;2.28,9.65;3.07,9.80;3.73,9.40;3.90,8.95;4.35,8.74;4.50,8.49;4.77,8.50;5.48,8.76;6.44,9.23;6.45,9.52;7.04,10.12;7.06,10.50;6.64,11.06;6.98,11.75;7.40,11.84;7.80,12.06;8.31,12.22;8.72,12.75;9.42,12.96;9.64,13.17;10.16,13.31;10.80,13.57;11.57,14.42;11.90,14.47;12.09,14.58;12.48,14.18;12.80,14.21;12.86,14.50",
    "11.00,0.90;10.47,0.77;10.18,1.08;9.83,1.43;9.33,1.46;9.13,1.66;6.83,1.62;6.14,1.87;5.93,1.06;6.28,0.84;6.91,0.57;7.41,0.49;8.31,0.71;8.68,0.46;9.47,0.37;10.19,0.37;10.71,-0.05;11.02,0.02;11.00,0.90",
    "11.02,0.02;10.71,-0.05;10.19,0.37;9.47,0.37;8.68,0.46;8.31,0.71;7.41,0.49;6.91,0.57;6.28,0.84;5.93,1.06;5.34,-0.51;5.00,-1.06;4.71,-1.96;4.99,-2.86;5.39,-2.81;6.25,-3.24;7.38,-2.98;8.22,-2.56;9.64,-2.83;10.40,-2.96;10.96,-2.94;11.01,-1.20;10.94,-0.76;11.10,-0.44;11.02,0.02",
    "10.21,-8.03;10.30,-7.90;10.15,-7.62;10.14,-6.85;10.43,-6.67;10.41,-6.49;10.52,-6.21;10.10,-6.05;10.22,-5.82;10.37,-5.40;10.15,-4.95;9.82,-4.78;9.61,-4.33;9.86,-3.98;9.90,-3.51;9.64,-2.83;8.22,-2.56;7.38,-2.98;6.25,-3.24;5.39,-2.81;4.99,-2.86;4.98,-3.31;5.18,-4.01;5.17,-4.65;4.99,-5.83;4.71,-6.53;4.34,-7.52;4.36,-7.71;5.19,-7.64;5.31,-7.54;5.71,-7.57;6.13,-7.99;6.19,-8.31;6.47,-8.60;6.91,-8.39;7.40,-8.49;7.69,-8.44;7.69,-8.28;8.12,-8.22;8.32,-8.30;8.46,-8.20;8.58,-7.83;9.38,-8.08;9.79,-8.31;10.13,-8.23;10.21,-8.03",
    "12.59,-13.70;12.58,-13.22;12.33,-12.50;12.35,-12.28;12.47,-12.20;12.39,-11.66;12.44,-11.51;12.08,-11.46;12.08,-11.30;12.21,-11.04;12.18,-10.87;11.92,-10.59;11.84,-10.17;12.06,-9.89;12.19,-9.57;12.33,-9.33;12.31,-9.13;12.09,-8.91;11.81,-8.79;11.39,-8.38;11.14,-8.58;10.81,-8.62;10.91,-8.41;10.79,-8.28;10.49,-8.34;10.21,-8.03;10.13,-8.23;9.79,-8.31;9.38,-8.08;8.58,-7.83;8.46,-8.20;8.32,-8.30;8.12,-8.22;7.69,-8.28;7.69,-8.44;7.71,-8.72;7.31,-8.93;7.31,-9.21;7.53,-9.40;7.93,-9.34;8.54,-9.76;8.43,-10.02;8.41,-10.23;8.35,-10.51;8.72,-10.49;8.98,-10.65;9.27,-10.62;9.69,-10.84;10.05,-11.12;10.05,-11.92;9.86,-12.15;9.84,-12.43;9.62,-12.60;9.34,-12.71;8.90,-13.25;9.49,-13.69;9.89,-14.07;10.02,-14.33;10.21,-14.58;10.66,-14.69;10.88,-14.84;11.04,-15.13;11.53,-14.69;11.51,-14.38;11.68,-14.12;11.68,-13.90;11.81,-13.74;12.14,-13.83;12.25,-13.72;12.59,-13.70",
    "12.38,-16.68;12.55,-16.15;12.52,-15.82;12.63,-15.55;12.59,-13.70;12.25,-13.72;12.14,-13.83;11.81,-13.74;11.68,-13.90;11.68,-14.12;11.51,-14.38;11.53,-14.69;11.04,-15.13;11.46,-15.66;11.52,-16.09;11.81,-16.31;11.96,-16.31;12.17,-16.61;12.38,-16.68",
    "7.69,-8.44;7.40,-8.49;6.91,-8.39;6.47,-8.60;6.19,-8.31;6.13,-7.99;5.71,-7.57;5.31,-7.54;5.19,-7.64;4.36,-7.71;4.36,-7.97;4.83,-9.00;5.59,-9.91;6.14,-10.77;6.79,-11.44;7.11,-11.20;7.40,-11.15;7.94,-10.70;8.41,-10.23;8.43,-10.02;8.54,-9.76;7.93,-9.34;7.53,-9.40;7.31,-9.21;7.31,-8.93;7.71,-8.72;7.69,-8.44",
    "8.90,-13.25;9.34,-12.71;9.62,-12.60;9.84,-12.43;9.86,-12.15;10.05,-11.92;10.05,-11.12;9.69,-10.84;9.27,-10.62;8.98,-10.65;8.72,-10.49;8.35,-10.51;8.41,-10.23;7.94,-10.70;7.40,-11.15;7.11,-11.20;6.79,-11.44;6.86,-11.71;7.26,-12.43;7.80,-12.95;8.16,-13.12;8.90,-13.25",
    "10.37,-5.40;10.95,-5.47;11.38,-5.20;11.71,-5.22;12.54,-4.43;13.23,-4.28;13.47,-4.01;13.34,-3.52;13.54,-3.10;13.80,-2.97;14.25,-2.19;14.56,-2.00;14.97,-1.07;15.12,-0.52;14.92,-0.27;14.93,0.37;14.44,0.30;13.99,0.43;13.34,0.99;12.85,1.02;12.63,2.18;11.94,2.15;11.64,1.94;11.55,1.45;11.11,1.24;11.00,0.90;11.02,0.02;11.10,-0.44;10.94,-0.76;11.01,-1.20;10.96,-2.94;10.40,-2.96;9.64,-2.83;9.90,-3.51;9.86,-3.98;9.61,-4.33;9.82,-4.78;10.15,-4.95;10.37,-5.40",
    "5.23,27.37;5.13,27.04;5.15,26.40;5.26,25.65;5.17,25.28;4.93,25.13;4.90,24.81;5.11,24.41;4.61,23.30;4.71,22.84;4.63,22.70;4.03,22.41;4.22,21.66;4.32,20.93;4.69,20.29;5.03,19.47;4.71,18.93;4.20,18.54;3.50,18.45;3.56,17.81;3.73,17.13;3.20,16.54;2.27,16.01;2.56,15.91;3.01,15.86;3.34,15.41;3.85,15.04;4.21,14.95;4.73,14.48;5.03,14.56;5.45,14.46;6.23,14.54;6.41,14.78;7.42,15.28;7.50,16.11;7.75,16.29;7.73,16.46;7.51,16.71;7.89,17.96;8.28,18.39;8.63,18.91;8.98,18.81;9.07,19.09;9.01,20.06;9.48,21.00;10.57,21.72;10.97,22.23;11.14,22.86;10.71,22.98;10.09,23.55;9.68,23.56;9.27,23.39;8.95,23.46;8.67,23.81;8.23,24.57;7.83,25.11;7.50,25.12;6.98,25.80;6.55,26.21;5.95,26.47;5.55,27.21;5.23,27.37",
    "3.50,18.45;2.90,18.39;2.37,18.09;1.74,17.90;0.86,17.77;0.29,17.83;-0.06,17.66;-0.42,17.64;-0.74,17.52;-1.23,16.87;-1.74,16.41;-2.71,15.97;-3.54,16.01;-3.86,15.75;-4.34,15.17;-4.97,14.58;-4.79,14.21;-4.51,14.14;-4.50,13.60;-4.88,13.26;-4.78,13.00;-4.44,12.62;-4.61,12.32;-5.04,11.91;-3.98,11.09;-3.43,11.86;-2.77,11.48;-2.51,11.82;-2.39,12.50;-1.95,12.58;-2.43,13.11;-2.47,13.99;-2.00,14.30;-1.33,14.43;-0.55,14.32;0.04,13.84;1.20,14.28;1.40,14.03;1.31,13.28;1.83,13.00;2.27,13.08;2.23,14.34;1.96,15.15;1.73,15.94;2.27,16.01;3.20,16.54;3.73,17.13;3.56,17.81;3.50,18.45",
    "2.26,11.28;2.33,11.75;2.19,12.36;2.32,12.95;2.27,13.08;1.83,13.00;1.31,13.28;1.40,14.03;1.20,14.28;0.04,13.84;-0.55,14.32;-1.33,14.43;-2.00,14.30;-2.47,13.99;-2.43,13.11;-1.95,12.58;-2.39,12.50;-2.51,11.82;-2.77,11.48;-3.43,11.86;-3.98,11.09;-2.97,10.07;-2.14,9.41;-1.11,8.80;-0.78,8.83;-0.46,9.05;0.27,9.29;1.01,9.49;1.07,9.83;1.06,11.29;2.26,11.28",
    "2.28,9.65;2.26,11.28;1.06,11.29;1.07,9.83;1.01,9.49;1.16,9.31;2.28,9.65",
    "-8.34,30.74;-8.59,31.16;-8.76,31.56;-8.93,32.19;-9.23,32.76;-9.68,33.23;-10.53,33.49;-10.80,33.32;-11.61,33.11;-12.44,33.31;-12.78,32.99;-13.71,32.69;-13.97,33.21;-14.80,30.18;-15.51,30.27;-15.64,29.52;-16.04,28.95;-16.39,28.83;-16.47,28.47;-17.29,27.60;-17.94,27.04;-17.96,26.71;-17.85,26.38;-17.74,25.26;-17.66,25.08;-17.58,25.08;-17.35,24.68;-17.30,24.03;-17.52,23.22;-16.90,22.56;-16.08,21.89;-12.90,21.93;-12.91,24.02;-12.57,23.93;-12.19,24.08;-11.72,23.90;-11.24,24.02;-10.93,23.91;-10.95,24.26;-11.26,24.31;-11.24,24.78;-11.33,25.42;-11.78,25.75;-11.92,26.55;-11.61,27.16;-12.13,27.39;-12.27,28.16;-12.70,28.52;-13.25,28.93;-13.26,29.70;-12.18,29.62;-12.36,29.34;-11.97,28.64;-11.79,28.37;-10.79,28.50;-9.61,28.67;-9.16,28.45;-8.53,28.73;-8.41,29.00;-8.24,30.35;-8.34,30.74",
    "-9.23,32.76;-9.42,33.74;-9.69,33.94;-10.16,34.28;-11.52,34.56;-12.28,34.28;-13.58,34.56;-13.57,34.91;-13.89,35.27;-14.61,35.69;-15.90,35.77;-16.11,35.34;-16.80,35.03;-16.18,34.38;-15.48,34.31;-15.01,34.52;-14.61,34.46;-14.36,34.06;-14.45,33.79;-13.97,33.21;-13.71,32.69;-12.78,32.99;-12.44,33.31;-11.61,33.11;-10.80,33.32;-10.53,33.49;-9.68,33.23;-9.23,32.76",
    "-11.52,34.56;-11.44,35.31;-11.72,36.51;-11.59,36.78;-11.57,37.47;-11.27,37.83;-11.29,38.43;-10.90,39.52;-10.32,40.32;-10.32,40.32;-10.32,40.32;-10.77,40.48;-11.76,40.44;-12.64,40.56;-14.20,40.60;-14.69,40.78;-15.41,40.48;-16.10,40.09;-16.72,39.45;-17.10,38.54;-17.59,37.41;-18.66,36.28;-18.84,35.90;-19.55,35.20;-19.78,34.79;-20.50,34.70;-21.25,35.18;-21.84,35.37;-22.14,35.39;-22.09,35.56;-23.07,35.53;-23.54,35.37;-23.71,35.61;-24.12,35.46;-24.48,35.04;-24.82,34.22;-25.36,33.01;-25.73,32.57;-26.15,32.66;-26.22,32.92;-26.74,32.83;-26.73,32.07;-26.29,31.99;-25.84,31.84;-25.48,31.75;-24.37,31.93;-23.66,31.67;-22.25,31.19;-21.12,32.24;-20.40,32.51;-20.30,32.66;-19.72,32.77;-19.42,32.61;-18.67,32.65;-17.98,32.85;-16.71,32.85;-16.39,32.33;-16.32,31.85;-16.07,31.64;-15.86,31.17;-15.88,30.34;-15.51,30.27;-14.80,30.18;-13.97,33.21;-14.45,33.79;-14.36,34.06;-14.61,34.46;-15.01,34.52;-15.48,34.31;-16.18,34.38;-16.80,35.03;-16.11,35.34;-15.90,35.77;-14.61,35.69;-13.89,35.27;-13.57,34.91;-13.58,34.56;-12.28,34.28;-11.52,34.56",
    "-26.73,32.07;-27.18,31.87;-27.29,31.28;-26.74,30.69;-26.40,30.68;-26.02,30.95;-25.73,31.04;-25.66,31.33;-25.84,31.84;-26.29,31.99;-26.73,32.07",
    "-4.78,13.00;-4.99,12.63;-5.25,12.47;-5.68,12.44;-5.79,12.18;-5.04,11.91;-4.61,12.32;-4.44,12.62;-4.78,13.00",
    "-6.10,12.32;-5.97,12.74;-5.98,13.02;-5.86,13.38;-5.88,16.33;-6.62,16.57;-7.22,16.86;-7.55,17.09;-8.07,17.47;-7.99,18.13;-7.85,18.46;-7.99,19.02;-7.74,19.17;-7.16,19.42;-7.12,20.04;-6.94,20.09;-6.94,20.60;-7.30,20.51;-7.29,21.73;-7.92,21.75;-8.31,21.95;-8.91,21.80;-9.52,21.88;-9.89,22.21;-11.08,22.16;-10.99,22.40;-11.02,22.84;-10.87,23.46;-10.93,23.91;-11.24,24.02;-11.72,23.90;-12.19,24.08;-12.57,23.93;-12.91,24.02;-12.90,21.93;-16.08,21.89;-16.90,22.56;-17.52,23.22;-17.93,21.38;-17.79,18.96;-17.31,18.26;-17.35,14.21;-17.42,14.06;-16.97,13.46;-16.94,12.81;-17.11,12.22;-17.30,11.73;-16.67,11.64;-15.79,11.78;-14.88,12.12;-14.45,12.18;-13.55,12.50;-13.14,12.74;-12.48,13.31;-12.04,13.63;-11.30,13.74;-10.73,13.69;-10.37,13.39;-9.77,13.12;-9.17,12.88;-8.96,12.93;-8.56,13.24;-7.60,12.93;-6.93,12.73;-6.29,12.23;-6.10,12.32",
    "-2.41,30.47;-2.81,30.53;-3.03,30.74;-3.36,30.75;-3.57,30.51;-4.09,30.12;-4.45,29.75;-4.50,29.34;-3.29,29.28;-2.84,29.02;-2.92,29.63;-2.35,29.94;-2.41,30.47",
    "32.71,35.72;32.39,35.55;32.53,35.18;31.87,34.97;31.75,35.23;31.62,34.97;31.35,34.93;31.49,35.40;31.10,35.42;29.50,34.92;29.76,34.82;31.22,34.27;31.22,34.27;31.22,34.27;31.55,34.56;31.61,34.49;32.07,34.75;32.83,34.96;33.08,35.10;33.09,35.13;33.09,35.46;33.26,35.55;33.28,35.82;32.87,35.84;32.72,35.70;32.71,35.72",
    "33.28,35.82;33.26,35.55;33.09,35.46;33.09,35.13;33.91,35.48;34.61,35.98;34.64,36.00;34.59,36.45;34.20,36.61;33.82,36.07;33.28,35.82",
    "-12.47,49.54;-12.90,49.81;-13.56,50.06;-14.76,50.22;-15.23,50.48;-15.71,50.38;-16.00,50.20;-15.41,49.86;-15.71,49.67;-16.45,49.86;-16.88,49.77;-17.11,49.50;-17.95,49.44;-19.12,49.04;-20.50,48.55;-22.39,47.93;-23.78,47.55;-24.94,47.10;-25.18,46.28;-25.60,45.41;-25.35,44.83;-24.99,44.04;-24.46,43.76;-23.57,43.70;-22.78,43.35;-22.06,43.25;-21.34,43.43;-21.16,43.89;-20.83,43.90;-20.07,44.37;-19.44,44.46;-18.96,44.23;-18.33,44.04;-17.41,43.96;-16.85,44.31;-16.22,44.45;-16.18,44.94;-15.97,45.50;-15.79,45.87;-15.78,46.31;-15.21,46.88;-14.59,47.71;-14.09,48.01;-13.66,47.87;-13.78,48.29;-13.09,48.85;-12.49,48.86;-12.04,49.19;-12.47,49.54",
    "31.49,35.40;31.35,34.93;31.62,34.97;31.75,35.23;31.87,34.97;32.53,35.18;32.39,35.55;31.78,35.55;31.49,35.40",
    "13.59,-16.71;13.62,-15.62;13.86,-15.40;13.88,-15.08;13.63,-14.69;13.63,-14.38;13.79,-14.05;13.51,-13.84;13.28,-14.28;13.30,-14.71;13.51,-15.14;13.28,-15.51;13.27,-15.69;13.13,-15.93;13.15,-16.84;13.59,-16.71",
    "30.31,9.48;32.10,9.06;32.51,8.44;32.75,8.43;33.34,7.61;34.10,7.52;34.66,8.14;35.48,8.38;36.43,8.22;36.95,8.42;37.35,9.51;37.23,10.21;36.72,10.18;37.09,11.03;36.90,11.10;36.41,10.60;35.95,10.59;35.70,10.94;34.83,10.81;34.33,10.15;33.79,10.34;33.77,10.86;33.29,11.11;33.14,11.49;32.37,11.43;32.08,10.94;31.76,10.64;31.38,9.95;30.96,10.06;30.54,9.97;30.31,9.48",
    "27.40,-8.68;27.59,-8.67;27.66,-8.67;28.84,-8.67;29.58,-7.06;29.73,-6.06;30.00,-5.24;30.50,-4.86;30.90,-3.69;31.64,-3.65;31.72,-3.07;32.09,-2.62;32.26,-1.31;32.65,-1.12;32.86,-1.39;33.92,-1.73;34.53,-1.79;35.17,-2.17;35.71,-1.21;35.89,-0.13;36.30,0.50;36.61,1.47;36.78,3.16;36.87,4.82;36.72,5.32;37.11,6.26;37.12,7.33;36.89,7.74;36.95,8.42;36.43,8.22;35.48,8.38;34.66,8.14;34.10,7.52;33.34,7.61;32.75,8.43;32.51,8.44;32.10,9.06;30.31,9.48;29.42,9.81;28.96,9.86;28.14,9.68;27.69,9.76;27.14,9.63;26.51,9.72;26.09,9.32;25.37,9.91;24.94,9.95;24.38,10.30;24.56,10.77;24.10,11.56;23.47,12.00;21.57,8.57;19.60,5.68;19.16,4.27;19.06,3.16;19.69,3.15;19.86,2.68;20.14,2.06;20.61,1.82;22.79,-1.55;24.97,-4.92;27.40,-8.68",
    "32.39,35.55;32.71,35.72;32.31,36.83;33.38,38.79;32.16,39.20;32.01,39.00;31.51,37.00;30.51,38.00;30.34,37.67;30.00,37.50;29.87,36.74;29.51,36.50;29.20,36.07;29.36,34.96;29.50,34.92;31.10,35.42;31.49,35.40;31.78,35.55;32.39,35.55",
    "24.25,51.58;24.29,51.76;24.02,51.79;24.18,52.58;24.15,53.40;24.12,54.01;24.80,54.69;25.44,55.44;26.06,56.07;25.71,56.26;24.92,56.40;24.92,55.89;24.27,55.80;24.13,55.98;23.93,55.53;23.52,55.53;23.11,55.23;22.71,55.21;22.50,55.01;23.00,52.00;24.01,51.62;24.25,51.58",
    "24.75,50.81;25.48,50.74;26.01,51.01;26.11,51.29;25.80,51.59;25.22,51.61;24.63,51.39;24.56,51.11;24.75,50.81",
    "29.98,47.97;29.53,48.18;29.31,48.09;28.55,48.42;28.53,47.71;29.00,47.46;29.10,46.57;30.06,47.30;29.98,47.97",
    "32.16,39.20;33.38,38.79;34.42,41.01;35.63,41.38;36.36,41.29;36.61,41.84;37.23,42.35;37.39,42.78;37.26,43.94;37.00,44.29;37.17,44.77;35.98,45.42;35.68,46.08;35.09,46.15;34.75,45.65;33.97,45.42;33.02,46.11;32.47,47.33;31.71,47.85;30.98,47.69;30.99,48.00;30.45,48.01;29.93,48.57;29.98,47.97;30.06,47.30;29.10,46.57;29.18,44.71;31.19,41.89;31.89,40.40;32.16,39.20",
    "22.71,55.21;23.11,55.23;23.52,55.53;23.93,55.53;24.13,55.98;24.27,55.80;24.92,55.89;24.92,56.40;24.24,56.85;23.88,57.40;23.75,58.14;23.57,58.73;22.99,59.18;22.66,59.45;22.53,59.81;22.31,59.81;21.71,59.44;21.43,59.28;21.11,58.86;20.43,58.49;20.48,58.03;20.24,57.83;19.74,57.67;19.07,57.79;18.94,57.69;18.95,57.23;18.57,56.61;18.09,56.51;17.88,56.28;17.88,55.66;17.63,55.27;17.23,55.27;16.95,54.79;17.04,54.24;16.71,53.57;16.65,53.11;17.35,52.78;19.00,52.00;20.00,55.00;22.00,55.67;22.71,55.21",
    "25.71,56.26;26.06,56.07;26.40,56.36;26.31,56.49;25.90,56.39;25.71,56.26",
    "-15.89,167.22;-16.47,167.84;-16.60,167.52;-16.16,167.18;-15.89,167.22",
    "-15.67,166.79;-15.39,166.65;-14.63,166.63;-14.93,167.11;-15.74,167.27;-15.61,167.00;-15.67,166.79",
    "12.19,102.58;13.39,102.35;14.23,102.99;14.42,104.28;14.27,105.22;13.88,106.04;14.57,106.50;14.20,107.38;13.54,107.61;12.34,107.49;11.57,105.81;10.96,106.25;10.89,105.20;10.49,104.33;10.63,103.50;11.15,103.09;12.19,102.58",
    "14.27,105.22;14.42,104.28;14.23,102.99;13.39,102.35;12.19,102.58;12.65,101.69;12.63,100.83;13.41,100.98;13.41,100.10;12.31,100.02;10.85,99.48;9.96,99.15;9.24,99.22;9.21,99.87;8.30,100.28;7.43,100.46;6.86,101.02;6.74,101.62;6.22,102.14;5.81,101.81;5.69,101.15;6.20,101.08;6.64,100.26;6.46,100.09;6.85,99.69;7.34,99.52;7.91,98.99;8.38,98.50;7.79,98.34;8.35,98.15;8.97,98.26;9.93,98.55;10.96,99.04;11.89,99.59;12.80,99.20;13.27,99.21;13.83,99.10;14.62,98.43;15.12,98.19;15.31,98.54;16.18,98.90;16.84,98.49;17.57,97.86;18.45,97.38;18.63,97.80;19.71,98.25;19.75,98.96;20.19,99.54;20.42,100.12;20.11,100.55;19.51,100.61;19.46,101.28;18.41,101.04;17.51,101.06;18.11,102.11;17.93,102.41;17.96,103.00;18.31,103.20;18.24,103.96;17.43,104.72;16.44,104.78;15.57,105.59;14.72,105.54;14.27,105.22",
    "14.20,107.38;14.57,106.50;13.88,106.04;14.27,105.22;14.72,105.54;15.57,105.59;16.44,104.78;17.43,104.72;18.24,103.96;18.31,103.20;17.96,103.00;17.93,102.41;18.11,102.11;17.51,101.06;18.41,101.04;19.46,101.28;19.51,100.61;20.11,100.55;20.42,100.12;20.79,100.33;21.44,101.18;21.20,101.27;21.17,101.80;22.32,101.65;22.46,102.17;21.68,102.75;20.77,103.20;20.76,104.44;19.89,104.82;19.62,104.18;19.27,103.90;18.67,105.09;17.49,105.93;16.60,106.56;15.91,107.31;15.20,107.56;14.20,107.38",
    "20.42,100.12;20.19,99.54;19.75,98.96;19.71,98.25;18.63,97.80;18.45,97.38;17.57,97.86;16.84,98.49;16.18,98.90;15.31,98.54;15.12,98.19;14.62,98.43;13.83,99.10;13.27,99.21;12.80,99.20;11.89,99.59;10.96,99.04;9.93,98.55;10.68,98.46;11.44,98.76;12.03,98.43;13.12,98.51;13.64,98.10;14.84,97.78;16.10,97.60;16.93,97.16;16.43,96.51;15.71,95.37;15.80,94.81;16.04,94.19;17.28,94.53;18.21,94.32;19.37,93.54;19.73,93.66;19.86,93.08;20.67,92.37;21.48,92.30;21.32,92.65;22.04,92.67;22.28,93.17;22.70,93.06;23.04,93.29;24.08,93.33;23.85,94.11;24.68,94.55;25.16,94.60;26.00,95.16;26.57,95.12;27.26,96.42;27.08,97.13;27.70,97.05;27.88,97.40;28.26,97.33;28.34,97.91;27.75,98.25;27.51,98.68;26.74,98.71;25.92,98.67;25.08,97.72;23.90,97.60;24.06,98.66;23.14,98.90;22.95,99.53;22.12,99.24;21.74,99.98;21.56,100.42;21.85,101.15;21.44,101.18;20.79,100.33;20.42,100.12",
    "10.49,104.33;10.89,105.20;10.96,106.25;11.57,105.81;12.34,107.49;13.54,107.61;14.20,107.38;15.20,107.56;15.91,107.31;16.60,106.56;17.49,105.93;18.67,105.09;19.27,103.90;19.62,104.18;19.89,104.82;20.76,104.44;20.77,103.20;21.68,102.75;22.46,102.17;22.71,102.71;22.70,103.50;22.82,104.48;23.35,105.33;22.98,105.81;22.79,106.73;22.22,106.57;21.81,107.04;21.55,108.05;20.70,106.72;19.75,105.88;19.06,105.66;18.00,106.43;16.70,107.36;16.08,108.27;15.28,108.88;13.43,109.34;11.67,109.20;11.01,108.37;10.36,107.22;9.53,106.41;8.60,105.16;9.24,104.80;9.92,105.08;10.49,104.33",
    "42.22,130.78;42.22,130.78;42.22,130.78;42.22,130.78",
    "42.40,130.64;42.40,130.64;42.22,130.78;42.28,130.40;41.94,129.97;41.60,129.67;40.88,129.71;40.66,129.19;40.49,129.01;40.19,128.63;40.03,127.97;39.76,127.53;39.32,127.50;39.21,127.39;39.05,127.78;38.61,128.35;38.37,128.21;38.30,127.78;38.26,127.07;37.80,126.68;37.84,126.24;37.75,126.17;37.94,125.69;37.75,125.57;37.67,125.28;37.86,125.24;37.95,124.98;38.11,124.71;38.55,124.99;38.67,125.22;38.85,125.13;39.39,125.39;39.55,125.32;39.66,124.74;39.93,124.27;40.57,125.08;41.11,126.18;41.82,126.87;41.50,127.34;41.47,128.21;41.99,128.05;42.42,129.60;42.99,129.99;42.40,130.64",
    "37.75,126.17;37.84,126.24;37.80,126.68;38.26,127.07;38.30,127.78;38.37,128.21;38.61,128.35;37.43,129.21;36.78,129.46;35.63,129.47;35.08,129.09;34.89,128.19;34.48,127.39;34.39,126.49;34.93,126.37;35.68,126.56;36.73,126.12;36.89,126.86;37.75,126.17",
    "49.30,87.75;49.47,88.81;50.33,90.71;50.80,92.23;50.50,93.10;50.48,94.15;50.01,94.82;49.98,95.81;49.73,97.26;50.42,98.23;51.01,97.83;52.05,98.86;51.63,99.98;51.52,100.89;51.26,102.07;50.51,102.26;50.09,103.68;50.28,104.62;50.41,105.89;50.27,106.89;49.79,107.87;49.28,108.48;49.29,109.40;49.13,110.66;49.38,111.58;49.54,112.90;50.25,114.36;50.14,114.96;49.81,115.49;49.89,116.68;49.13,116.19;48.14,115.49;47.73,115.74;47.85,116.31;47.70,117.30;48.07,118.06;47.75,118.87;47.05,119.77;46.69,119.66;46.81,118.87;46.67,117.42;46.39,116.72;45.73,115.99;45.34,114.46;44.81,113.46;45.01,112.44;45.10,111.87;44.46,111.35;44.07,111.67;43.74,111.83;43.41,111.13;42.87,110.41;42.52,109.24;42.48,107.74;42.13,106.13;41.60,104.96;41.91,104.52;41.91,103.31;42.51,101.83;42.66,100.85;42.52,99.52;42.75,97.45;42.73,96.35;43.32,95.76;44.24,95.31;44.35,94.69;44.98,93.48;45.12,92.13;45.29,90.95;45.72,90.59;46.89,90.97;47.69,90.28;48.07,88.85;48.60,88.01;49.30,87.75",
    "27.88,97.40;27.70,97.05;27.08,97.13;27.26,96.42;26.57,95.12;26.00,95.16;25.16,94.60;24.68,94.55;23.85,94.11;24.08,93.33;23.04,93.29;22.70,93.06;22.28,93.17;22.04,92.67;23.63,92.15;23.62,91.87;22.99,91.71;23.50,91.16;24.07,91.47;24.13,91.92;24.98,92.38;25.15,91.80;25.13,90.87;25.27,89.92;25.97,89.83;26.01,89.36;26.45,88.56;25.77,88.21;25.24,88.93;24.87,88.31;24.50,88.08;24.23,88.70;23.63,88.53;22.88,88.88;22.06,89.03;21.69,88.89;21.70,88.21;21.50,86.98;20.74,87.03;20.15,86.50;19.48,85.06;18.30,83.94;17.67,83.19;17.02,82.19;16.56,82.19;16.31,81.69;15.95,80.79;15.90,80.32;15.14,80.03;13.84,80.23;13.01,80.29;12.06,79.86;10.36,79.86;10.31,79.34;9.55,78.89;9.22,79.19;8.93,78.28;8.25,77.94;7.97,77.54;8.90,76.59;10.30,76.13;11.31,75.75;11.78,75.40;12.74,74.86;13.99,74.62;14.62,74.44;15.99,73.53;17.93,73.12;19.21,72.82;20.42,72.82;21.36,72.63;20.76,71.18;20.88,70.47;22.09,69.16;22.45,69.64;22.84,69.35;23.69,68.18;24.36,68.84;24.36,71.04;25.22,70.84;25.72,70.28;26.49,70.17;26.94,69.51;27.99,70.62;27.91,71.78;28.96,72.82;29.98,73.45;30.98,74.42;31.69,74.41;31.69,74.41;32.45,73.85;33.25,73.55;34.15,73.35;34.85,73.45;35.35,73.15;35.85,72.75;36.75,72.88;36.88,74.07;37.06,74.58;37.16,75.18;36.70,75.92;35.95,76.22;35.52,77.84;35.95,78.42;36.05,79.25;35.95,79.95;35.35,80.38;34.55,79.90;34.32,78.95;33.51,78.85;32.99,79.25;32.48,79.20;31.51,78.75;30.88,79.72;30.18,81.11;29.73,80.48;28.79,80.09;28.42,81.06;27.93,82.00;27.36,83.30;27.23,84.68;26.73,85.25;26.63,86.02;26.40,87.23;26.41,88.06;26.81,88.17;27.45,88.04;27.88,88.12;28.09,88.73;27.30,88.81;27.10,88.84;26.72,89.74;26.88,90.37;26.81,91.22;26.84,92.03;27.45,92.10;27.77,91.70;27.90,92.50;28.64,93.41;29.28,94.57;29.03,95.40;29.45,96.12;28.83,96.59;28.41,96.25;28.26,97.33;27.88,97.40",
    "22.04,92.67;21.32,92.65;21.48,92.30;20.67,92.37;21.19,92.08;21.70,92.03;22.18,91.83;22.77,91.42;22.81,90.50;22.39,90.59;21.84,90.27;22.04,89.85;21.86,89.70;21.97,89.42;22.06,89.03;22.88,88.88;23.63,88.53;24.23,88.70;24.50,88.08;24.87,88.31;25.24,88.93;25.77,88.21;26.45,88.56;26.01,89.36;25.97,89.83;25.27,89.92;25.13,90.87;25.15,91.80;24.98,92.38;24.13,91.92;24.07,91.47;23.50,91.16;22.99,91.71;23.62,91.87;23.63,92.15;22.04,92.67",
    "27.77,91.70;27.45,92.10;26.84,92.03;26.81,91.22;26.88,90.37;26.72,89.74;27.10,88.84;27.30,88.81;28.04,89.48;28.30,90.02;28.06,90.73;28.04,91.26;27.77,91.70",
    "27.88,88.12;27.45,88.04;26.81,88.17;26.41,88.06;26.40,87.23;26.63,86.02;26.73,85.25;27.23,84.68;27.36,83.30;27.93,82.00;28.42,81.06;28.79,80.09;29.73,80.48;30.18,81.11;30.42,81.53;30.12,82.33;29.46,83.34;29.32,83.90;28.84,84.23;28.64,85.01;28.20,85.82;27.97,86.95;27.88,88.12",
    "23.94,67.44;24.66,67.15;25.43,66.37;25.24,64.53;25.22,62.91;25.08,61.50;26.24,61.87;26.76,63.32;27.22,63.23;27.38,62.76;28.26,62.73;28.70,61.77;29.30,61.37;29.83,60.87;29.32,62.55;29.47,63.55;29.34,64.15;29.56,64.35;29.47,65.05;29.89,66.35;30.74,66.38;31.30,66.94;31.30,67.68;31.58,67.79;31.71,68.56;31.62,68.93;31.90,69.32;32.50,69.26;33.11,69.69;33.36,70.32;34.02,69.93;33.99,70.88;34.35,71.16;34.73,71.12;35.15,71.61;35.65,71.50;36.07,71.26;36.51,71.85;36.71,72.86;35.85,72.74;35.85,72.74;35.85,72.74;35.85,72.74;35.85,72.74;35.85,72.74;35.85,72.74;35.84,72.74;35.84,72.74;35.34,73.14;34.85,73.44;34.15,73.34;34.15,73.34;34.15,73.34;34.15,73.34;34.15,73.34;33.25,73.54;33.25,73.54;32.45,73.84;32.45,73.84;32.44,73.84;32.44,73.84;31.69,74.40;30.98,74.41;29.98,73.44;29.98,73.44;29.98,73.44;28.97,72.82;27.92,71.77;28.00,70.62;28.00,70.62;28.00,70.62;28.00,70.61;28.00,70.61;28.00,70.61;28.00,70.61;28.00,70.61;28.00,70.61;26.95,69.51;26.95,69.51;26.95,69.51;26.95,69.51;26.94,69.51;26.94,69.50;26.94,69.50;26.94,69.50;26.94,69.50;26.94,69.50;26.94,69.50;26.94,69.50;26.94,69.51;26.94,69.51;26.94,69.51;26.93,69.51;26.93,69.51;26.93,69.51;26.93,69.51;26.49,70.16;25.72,70.27;25.72,70.27;25.72,70.27;25.72,70.27;25.72,70.27;25.72,70.27;25.72,70.28;25.71,70.28;25.21,70.84;24.37,71.03;24.37,68.84;24.37,68.84;24.37,68.84;24.37,68.84;24.37,68.84;24.37,68.84;24.37,68.84;24.37,68.84;24.37,68.84;23.70,68.17;23.70,68.17;23.70,68.17;23.70,68.17;23.70,68.17;23.70,68.17;23.94,67.44",
    "37.36,67.08;37.14,67.83;37.02,68.14;37.34,68.86;37.15,69.20;37.61,69.52;37.59,70.12;37.74,70.27;38.14,70.38;38.49,70.81;38.26,71.35;37.95,71.24;37.91,71.54;37.07,71.45;36.74,71.84;36.95,72.19;37.05,72.64;37.50,73.26;37.42,73.95;37.42,74.98;37.16,75.14;37.07,74.58;37.07,74.58;37.07,74.58;36.89,74.07;36.76,72.88;36.76,72.88;36.76,72.88;36.76,72.88;36.76,72.88;36.76,72.87;36.76,72.87;36.76,72.87;36.76,72.87;36.76,72.87;36.75,72.87;36.75,72.87;36.75,72.87;36.75,72.87;36.71,72.86;36.51,71.85;36.07,71.26;35.65,71.50;35.15,71.61;34.73,71.12;34.35,71.16;33.99,70.88;34.02,69.93;33.36,70.32;33.11,69.69;32.50,69.26;31.90,69.32;31.62,68.93;31.71,68.56;31.58,67.79;31.30,67.68;31.30,66.94;30.74,66.38;29.89,66.35;29.47,65.05;29.56,64.35;29.34,64.15;29.47,63.55;29.32,62.55;29.83,60.87;30.74,61.78;31.38,61.70;31.55,60.94;32.18,60.86;32.98,60.54;33.53,60.96;33.68,60.53;34.40,60.80;35.65,61.21;35.27,62.23;35.40,62.98;35.86,63.19;36.01,63.98;36.31,64.55;37.11,64.75;37.31,65.59;37.66,65.75;37.39,66.22;37.36,66.52;37.36,67.08",
    "37.14,67.83;38.16,68.39;38.90,68.18;39.14,67.44;39.58,67.70;39.53,68.54;40.09,69.01;40.73,69.33;40.96,70.67;40.50,70.46;40.22,70.60;40.24,71.01;39.94,70.65;40.10,69.56;39.53,69.46;39.60,70.55;39.28,71.78;39.43,73.68;38.51,73.93;38.61,74.26;38.38,74.86;37.99,74.83;37.42,74.98;37.42,73.95;37.50,73.26;37.05,72.64;36.95,72.19;36.74,71.84;37.07,71.45;37.91,71.54;37.95,71.24;38.26,71.35;38.49,70.81;38.14,70.38;37.74,70.27;37.59,70.12;37.61,69.52;37.15,69.20;37.34,68.86;37.02,68.14;37.14,67.83",
    "42.27,70.96;42.70,71.19;42.85,71.84;42.50,73.49;43.09,73.65;43.30,74.21;42.88,75.64;42.99,76.00;42.96,77.66;42.86,79.14;42.50,79.64;42.35,80.26;42.12,80.12;41.58,78.54;41.19,78.19;41.07,76.90;40.43,76.53;40.56,75.47;40.37,74.78;39.89,73.82;39.66,73.96;39.43,73.68;39.28,71.78;39.60,70.55;39.53,69.46;40.10,69.56;39.94,70.65;40.24,71.01;40.15,71.77;40.87,73.06;41.39,71.87;41.14,71.16;41.52,70.42;42.17,71.26;42.27,70.96",
    "41.78,52.50;42.12,52.94;42.32,54.08;42.04,54.76;41.26,55.46;41.31,55.97;41.32,57.10;41.83,56.93;42.17,57.79;42.75,58.63;42.22,59.98;41.43,60.08;41.22,60.47;41.27,61.55;41.08,61.88;40.05,62.37;39.36,63.52;38.89,64.17;38.40,65.22;37.97,66.55;37.36,66.52;37.39,66.22;37.66,65.75;37.31,65.59;37.11,64.75;36.31,64.55;36.01,63.98;35.86,63.19;35.40,62.98;35.27,62.23;35.65,61.21;36.49,61.12;36.53,60.38;37.41,59.23;37.52,58.44;38.03,57.33;38.12,56.62;37.94,56.18;37.96,55.51;37.39,54.80;37.20,53.92;37.91,53.74;38.95,53.88;39.29,53.10;39.98,53.36;40.03,52.69;40.88,52.92;40.63,53.86;40.95,54.74;41.55,54.01;42.12,53.72;41.87,52.92;41.14,52.81;41.78,52.50",
    "29.93,48.57;30.45,48.01;30.99,48.00;30.98,47.69;31.71,47.85;32.47,47.33;33.02,46.11;33.97,45.42;34.75,45.65;35.09,46.15;35.68,46.08;35.98,45.42;37.17,44.77;37.17,44.77;37.97,44.23;38.28,44.42;39.43,44.11;39.71,44.79;39.34,44.95;38.87,45.46;38.74,46.14;38.77,46.51;39.51,47.69;39.58,48.06;39.29,48.36;38.79,48.01;38.27,48.63;38.32,48.88;37.58,49.20;37.37,50.15;36.87,50.84;36.70,52.26;36.97,53.83;37.20,53.92;37.39,54.80;37.96,55.51;37.94,56.18;38.12,56.62;38.03,57.33;37.52,58.44;37.41,59.23;36.53,60.38;36.49,61.12;35.65,61.21;34.40,60.80;33.68,60.53;33.53,60.96;32.98,60.54;32.18,60.86;31.55,60.94;31.38,61.70;30.74,61.78;29.83,60.87;29.30,61.37;28.70,61.77;28.26,62.73;27.38,62.76;27.22,63.23;26.76,63.32;26.24,61.87;25.08,61.50;25.38,59.62;25.61,58.53;25.74,57.40;26.97,56.97;27.14,56.49;26.96,55.72;26.48,54.72;26.81,53.49;27.58,52.48;27.87,51.52;28.81,50.85;30.15,50.12;29.99,49.58;30.32,48.94;29.93,48.57",
    "32.71,35.72;32.72,35.70;32.87,35.84;33.28,35.82;33.82,36.07;34.20,36.61;34.59,36.45;34.64,36.00;35.41,35.91;35.82,36.15;36.04,36.42;36.26,36.69;36.82,36.74;36.62,37.07;36.90,38.17;36.71,38.70;36.72,39.52;37.09,40.67;37.07,41.21;37.23,42.35;36.61,41.84;36.36,41.29;35.63,41.38;34.42,41.01;33.38,38.79;32.31,36.83;32.71,35.72",
    "38.77,46.51;38.74,46.14;39.32,45.74;39.47,45.74;39.47,45.30;39.74,45.00;39.71,44.79;40.01,44.40;40.25,43.66;40.74,43.75;41.09,43.58;41.25,44.97;40.99,45.18;40.81,45.56;40.56,45.36;40.22,45.89;39.90,45.61;39.63,46.03;39.46,46.48;38.77,46.51",
    "58.86,11.03;59.43,11.47;60.12,12.30;61.29,12.63;61.80,11.99;63.13,11.93;64.07,12.58;64.05,13.57;64.45,13.92;64.79,13.56;66.19,15.11;67.30,16.11;68.01,16.77;68.01,17.73;68.57,17.99;68.41,19.88;69.07,20.03;69.11,20.65;68.62,21.98;67.94,23.54;66.40,23.57;66.01,23.90;65.72,22.18;65.03,21.21;64.41,21.37;63.61,19.78;62.75,17.85;61.34,17.12;60.64,17.83;60.08,18.79;58.95,17.87;58.72,16.83;57.04,16.45;56.10,15.88;56.20,14.67;55.41,14.10;55.36,12.94;56.31,12.63;57.44,11.79;58.86,11.03",
    "56.17,28.18;55.92,29.23;55.67,29.37;55.79,29.90;55.55,30.87;55.08,30.97;54.81,30.76;54.16,31.38;53.97,31.79;53.79,31.73;53.62,32.41;53.35,32.69;53.13,32.30;53.17,31.50;53.07,31.31;52.74,31.54;52.10,31.79;52.10,31.79;52.04,30.93;51.82,30.62;51.32,30.56;51.42,30.16;51.37,29.25;51.60,28.99;51.43,28.62;51.57,28.24;51.59,27.45;51.83,26.34;51.91,25.33;51.89,24.55;51.62,24.01;51.58,23.53;52.02,23.51;52.49,23.20;52.69,23.80;53.09,23.80;53.47,23.53;53.91,23.48;53.91,24.45;54.28,25.54;54.85,25.77;55.17,26.59;55.62,26.49;55.78,27.10;56.17,28.18",
    "52.10,31.79;52.06,32.16;52.29,32.41;52.24,32.72;52.34,33.75;51.77,34.39;51.57,34.14;51.26,34.22;51.21,35.02;50.77,35.38;50.58,35.36;50.23,36.63;50.38,37.39;49.92,38.01;49.93,38.59;49.60,40.07;49.31,40.08;48.78,39.67;48.23,39.90;47.90,39.74;47.83,38.77;47.55,38.26;47.10,38.22;47.02,37.43;46.70,36.76;46.65,35.82;46.27,34.96;45.74,35.01;45.77,34.86;45.97,34.73;46.01,34.41;46.22,33.70;45.97,33.44;46.08,33.30;46.33,31.74;46.71,31.68;46.58,30.75;46.03,30.38;45.29,29.60;45.46,29.15;45.30,28.68;45.49,28.23;45.60,28.49;45.94,28.66;46.26,28.93;46.44,28.86;46.52,29.07;46.38,29.17;46.35,29.76;46.42,30.02;46.53,29.84;46.67,29.91;46.93,29.56;47.35,29.42;47.51,29.05;47.85,29.12;48.12,28.67;48.16,28.26;48.47,27.52;48.37,26.86;48.22,26.62;48.22,26.20;47.99,25.95;47.89,25.21;47.74,24.87;47.98,24.40;47.99,23.76;48.10,23.14;47.88,22.71;48.15,22.64;48.42,22.09;48.83,22.28;49.09,22.56;49.03,22.78;49.48,22.52;50.31,23.43;50.42,23.92;50.71,24.03;51.58,23.53;51.62,24.01;51.89,24.55;51.91,25.33;51.83,26.34;51.59,27.45;51.57,28.24;51.43,28.62;51.60,28.99;51.37,29.25;51.42,30.16;51.32,30.56;51.82,30.62;52.04,30.93;52.10,31.79",
    "53.91,23.48;53.47,23.53;53.09,23.80;52.69,23.80;52.49,23.20;52.02,23.51;51.58,23.53;50.71,24.03;50.42,23.92;50.31,23.43;49.48,22.52;49.03,22.78;49.09,22.56;49.47,21.61;49.33,20.89;49.43,20.42;49.22,19.83;49.57,19.32;49.44,18.91;49.50,18.85;49.99,18.39;50.05,17.65;50.36,17.55;50.47,16.87;50.22,16.72;50.42,16.18;50.70,16.24;50.78,15.49;51.11,15.02;51.75,14.61;52.09,14.69;52.62,14.44;52.98,14.07;53.25,14.35;53.76,14.12;54.05,14.80;54.51,16.36;54.85,17.62;54.68,18.62;54.44,18.70;54.43,19.66;54.31,20.89;54.33,22.73;54.22,23.24;53.91,23.48",
    "48.12,16.98;47.71,16.90;47.71,16.34;47.50,16.53;46.85,16.20;46.68,16.01;46.66,15.14;46.43,14.63;46.51,13.81;46.77,12.38;47.12,12.15;46.94,11.16;46.75,11.05;46.89,10.44;46.92,9.93;47.10,9.48;47.35,9.63;47.53,9.59;47.58,9.90;47.30,10.40;47.57,10.54;47.52,11.43;47.70,12.14;47.67,12.62;47.47,12.93;47.64,13.03;48.29,12.88;48.42,13.24;48.88,13.60;48.56,14.34;48.96,14.90;49.04,15.25;48.73,16.03;48.79,16.50;48.60,16.96;48.47,16.88;48.12,16.98",
    "48.42,22.09;48.15,22.64;47.88,22.71;47.67,22.10;46.99,21.63;46.32,21.02;46.13,20.22;46.17,19.60;45.91,18.83;45.91,18.83;45.76,18.46;45.95,17.63;46.38,16.88;46.50,16.56;46.84,16.37;46.85,16.20;47.50,16.53;47.71,16.34;47.71,16.90;48.12,16.98;47.87,17.49;47.76,17.86;47.88,18.70;48.08,18.78;48.11,19.17;48.27,19.66;48.20,19.77;48.33,20.24;48.56,20.47;48.62,20.80;48.32,21.87;48.42,22.09",
    "48.22,26.62;48.37,26.86;48.47,27.52;48.16,28.26;48.12,28.67;47.85,29.12;47.51,29.05;47.35,29.42;46.93,29.56;46.67,29.91;46.53,29.84;46.42,30.02;46.35,29.76;46.38,29.17;46.52,29.07;46.44,28.86;46.26,28.93;45.94,28.66;45.60,28.49;45.49,28.23;45.94,28.05;46.37,28.16;46.81,28.13;47.41,27.55;47.83,27.23;48.12,26.92;48.22,26.62",
    "45.49,28.23;45.30,28.68;45.46,29.15;45.29,29.60;45.04,29.63;44.82,29.14;44.91,28.84;43.71,28.56;43.81,27.97;44.18,27.24;43.94,26.07;43.69,25.57;43.74,24.10;43.90,23.33;43.82,22.94;44.23,22.66;44.41,22.47;44.58,22.71;44.70,22.46;44.48,22.15;44.77,21.56;45.18,21.48;45.42,20.87;45.73,20.76;46.13,20.22;46.32,21.02;46.99,21.63;47.67,22.10;47.88,22.71;48.10,23.14;47.99,23.76;47.98,24.40;47.74,24.87;47.89,25.21;47.99,25.95;48.22,26.20;48.22,26.62;48.12,26.92;47.83,27.23;47.41,27.55;46.81,28.13;46.37,28.16;45.94,28.05;45.49,28.23",
    "55.62,26.49;55.17,26.59;54.85,25.77;54.28,25.54;53.91,24.45;53.91,23.48;54.22,23.24;54.33,22.73;54.58,22.65;54.86,22.76;55.02,22.32;55.19,21.27;56.03,21.06;56.34,22.20;56.27,23.88;56.37,24.86;56.16,25.00;56.10,25.53;55.62,26.49",
    "57.47,27.29;57.24,27.77;56.76,27.86;56.17,28.18;55.78,27.10;55.62,26.49;56.10,25.53;56.16,25.00;56.37,24.86;56.27,23.88;56.34,22.20;56.03,21.06;56.78,21.09;57.41,21.58;57.75,22.52;57.01,23.32;57.03,24.12;57.79,24.31;57.97,25.16;57.85,25.60;57.48,26.46;57.47,27.29",
    "59.48,27.98;59.48,27.98;59.30,28.13;58.72,27.42;57.79,27.72;57.47,27.29;57.48,26.46;57.85,25.60;57.97,25.16;57.79,24.31;58.38,24.43;58.26,24.06;58.61,23.43;59.19,23.34;59.47,24.60;59.61,25.86;59.45,26.95;59.48,27.98;59.48,27.98",
    "53.76,14.12;53.25,14.35;52.98,14.07;52.62,14.44;52.09,14.69;51.75,14.61;51.11,15.02;51.00,14.57;51.12,14.31;50.93,14.06;50.73,13.34;50.48,12.97;50.27,12.24;49.97,12.42;49.55,12.52;49.31,13.03;48.88,13.60;48.42,13.24;48.29,12.88;47.64,13.03;47.47,12.93;47.67,12.62;47.70,12.14;47.52,11.43;47.57,10.54;47.30,10.40;47.58,9.90;47.53,9.59;47.83,8.52;47.61,8.32;47.62,7.47;48.33,7.59;49.02,8.10;49.20,6.66;49.46,6.19;49.90,6.24;50.13,6.04;50.80,6.16;51.85,5.99;51.85,6.59;52.23,6.84;53.14,7.09;53.48,6.91;53.69,7.10;53.75,7.94;53.53,8.12;54.02,8.80;54.40,8.57;54.96,8.53;54.83,9.28;54.98,9.92;54.60,9.94;54.36,10.95;54.01,10.94;54.20,11.96;54.47,12.52;54.08,13.65;53.76,14.12",
    "44.23,22.66;43.82,22.94;43.90,23.33;43.74,24.10;43.69,25.57;43.94,26.07;44.18,27.24;43.81,27.97;43.71,28.56;43.29,28.04;42.58,27.67;42.01,28.00;42.14,27.14;41.83,26.12;41.33,26.11;41.23,25.20;41.58,24.49;41.31,23.69;41.34,22.95;42.00,22.88;42.32,22.38;42.46,22.55;42.58,22.44;42.90,22.60;43.21,22.99;43.64,22.50;44.01,22.41;44.23,22.66",
    "35.30,26.29;35.00,26.16;34.92,24.72;35.08,24.74;35.28,23.51;35.71,23.70;35.37,24.25;35.42,25.03;35.35,25.77;35.18,25.75;35.30,26.29",
    "41.34,22.95;41.31,23.69;41.58,24.49;41.23,25.20;41.33,26.11;41.83,26.12;41.56,26.60;40.94,26.29;40.82,26.06;40.85,25.45;40.95,24.93;40.69,23.71;40.12,24.41;39.96,23.90;39.96,23.34;40.48,22.81;40.26,22.63;39.66,22.85;39.19,23.35;38.97,22.97;38.51,23.53;38.22,24.03;37.66,24.04;37.92,23.12;37.41,23.41;37.31,22.77;36.42,23.15;36.41,22.49;36.84,21.67;37.64,21.30;38.31,21.12;38.77,20.73;39.34,20.22;39.62,20.15;40.11,20.62;40.43,20.67;40.58,21.00;40.84,21.02;40.93,21.67;41.15,22.06;41.13,22.60;41.30,22.76;41.34,22.95",
    "37.17,44.77;37.00,44.29;37.26,43.94;37.39,42.78;37.23,42.35;37.07,41.21;37.09,40.67;36.72,39.52;36.71,38.70;36.90,38.17;36.62,37.07;36.82,36.74;36.26,36.69;36.04,36.42;35.82,36.15;36.27,35.78;36.65,36.16;36.57,35.55;36.80,34.71;36.22,34.03;36.11,32.51;36.64,31.70;36.68,30.62;36.26,30.39;36.14,29.70;36.68,28.73;36.66,27.64;37.65,27.05;38.21,26.32;38.99,26.80;39.46,26.17;40.42,27.28;40.46,28.82;41.22,29.24;41.09,31.15;41.74,32.35;42.02,33.51;42.04,35.17;41.34,36.91;40.95,38.35;41.10,39.51;41.01,40.37;41.54,41.55;41.58,42.62;41.09,43.58;40.74,43.75;40.25,43.66;40.01,44.40;39.71,44.79;39.43,44.11;38.28,44.42;37.97,44.23;37.17,44.77;37.17,44.77",
    "41.83,26.12;42.14,27.14;42.01,28.00;41.62,28.12;41.30,28.99;41.05,28.81;41.00,27.62;40.69,27.19;40.15,26.36;40.62,26.04;40.82,26.06;40.94,26.29;41.56,26.60;41.83,26.12",
    "40.84,21.02;40.58,21.00;40.43,20.67;40.11,20.62;39.62,20.15;39.69,19.98;39.92,19.96;40.25,19.41;40.73,19.32;41.41,19.40;41.72,19.54;41.88,19.37;41.88,19.37;42.20,19.30;42.69,19.74;42.50,19.80;42.59,20.07;42.32,20.28;42.22,20.52;41.86,20.59;41.86,20.59;41.52,20.46;41.09,20.61;40.84,21.02",
    "46.50,16.56;46.38,16.88;45.95,17.63;45.76,18.46;45.91,18.83;45.52,19.07;45.24,19.39;44.86,19.01;45.08,18.55;45.07,17.86;45.23,17.00;45.21,16.53;45.00,16.32;45.23,15.96;44.82,15.75;44.35,16.24;44.04,16.46;43.67,16.92;43.45,17.30;43.03,17.67;42.65,18.56;42.48,18.45;42.48,18.45;42.85,17.51;43.21,16.93;43.51,16.02;44.24,15.17;44.32,15.38;44.74,14.92;45.08,14.90;45.23,14.26;44.80,13.95;45.14,13.66;45.48,13.68;45.50,13.72;45.47,14.41;45.63,14.60;45.47,14.94;45.45,15.33;45.73,15.32;45.83,15.67;46.24,15.77;46.50,16.56",
    "47.53,9.59;47.35,9.63;47.10,9.48;46.92,9.93;46.89,10.44;46.48,10.36;46.31,9.92;46.44,9.18;46.04,8.97;46.01,8.49;46.16,8.32;45.82,7.76;45.78,7.27;45.99,6.84;46.43,6.50;46.27,6.02;46.73,6.04;47.29,6.77;47.54,6.74;47.45,7.19;47.62,7.47;47.61,8.32;47.83,8.52;47.53,9.59",
    "50.13,6.04;49.90,6.24;49.46,6.19;49.44,5.90;49.53,5.67;50.09,5.78;50.13,6.04",
    "50.80,6.16;50.13,6.04;50.09,5.78;49.53,5.67;49.99,4.80;49.91,4.29;50.38,3.59;50.78,3.12;50.80,2.66;51.15,2.51;51.35,3.31;51.35,3.32;51.35,3.31;51.27,4.05;51.48,4.97;51.04,5.61;50.80,6.16",
    "53.48,6.91;53.14,7.09;52.23,6.84;51.85,6.59;51.85,5.99;50.80,6.16;51.04,5.61;51.48,4.97;51.27,4.05;51.35,3.31;51.35,3.32;51.62,3.83;53.09,4.71;53.51,6.07;53.48,6.91",
    "41.88,-9.03;42.13,-8.67;42.28,-8.26;41.79,-8.01;41.79,-7.42;41.92,-7.25;41.88,-6.67;41.38,-6.39;41.11,-6.85;40.33,-6.86;40.18,-7.03;39.71,-7.07;39.63,-7.50;39.03,-7.10;38.37,-7.37;38.08,-7.03;37.80,-7.17;37.43,-7.54;37.10,-7.45;36.84,-7.86;36.98,-8.38;36.87,-8.90;37.65,-8.75;38.27,-8.84;38.36,-9.29;38.74,-9.53;39.39,-9.45;39.76,-9.05;40.16,-8.98;40.76,-8.77;41.18,-8.79;41.54,-8.99;41.88,-9.03",
    "37.10,-7.45;37.43,-7.54;37.80,-7.17;38.08,-7.03;38.37,-7.37;39.03,-7.10;39.63,-7.50;39.71,-7.07;40.18,-7.03;40.33,-6.86;41.11,-6.85;41.38,-6.39;41.88,-6.67;41.92,-7.25;41.79,-7.42;41.79,-8.01;42.28,-8.26;42.13,-8.67;41.88,-9.03;42.59,-8.98;43.03,-9.39;43.75,-7.98;43.57,-6.75;43.57,-5.41;43.40,-4.35;43.46,-3.52;43.42,-1.90;43.03,-1.50;42.58,0.34;42.80,0.70;42.34,1.83;42.47,2.99;41.89,3.04;41.23,2.09;41.01,0.81;40.68,0.72;40.12,0.11;39.31,-0.28;38.74,0.11;38.29,-0.47;37.64,-0.68;37.44,-1.44;36.67,-2.15;36.66,-3.42;36.68,-4.37;36.32,-5.00;35.95,-5.38;36.03,-5.87;36.37,-6.24;36.94,-6.52;37.10,-7.45",
    "53.87,-6.20;53.15,-6.03;52.26,-6.79;51.67,-8.56;51.82,-9.98;52.86,-9.17;53.88,-9.69;54.66,-8.33;55.13,-7.57;54.60,-7.37;54.06,-7.57;54.07,-6.95;53.87,-6.20",
    "-21.08,165.78;-21.70,166.60;-22.16,167.12;-22.40,166.74;-22.13,166.19;-21.68,165.47;-21.15,164.83;-20.44,164.17;-20.11,164.03;-20.12,164.46;-20.46,165.02;-20.80,165.46;-21.08,165.78",
    "-10.48,162.12;-10.83,162.40;-10.82,161.70;-10.20,161.32;-10.45,161.92;-10.48,162.12",
    "-9.60,161.68;-9.78,161.53;-8.92,160.79;-8.32,160.58;-8.32,160.92;-9.12,161.28;-9.60,161.68",
    "-9.87,160.85;-9.90,160.46;-9.79,159.85;-9.64,159.64;-9.24,159.70;-9.40,160.36;-9.61,160.69;-9.87,160.85",
    "-8.02,159.64;-8.34,159.88;-8.54,159.92;-8.11,159.13;-7.75,158.59;-7.42,158.21;-7.32,158.36;-7.56,158.82;-8.02,159.64",
    "-7.02,157.14;-7.35,157.54;-7.40,157.34;-7.18,156.90;-6.77,156.49;-6.60,156.54;-7.02,157.14",
    "-40.07,176.89;-40.60,176.51;-41.29,176.01;-41.69,175.24;-41.43,175.07;-41.28,174.65;-40.46,175.23;-39.91,174.90;-39.51,173.82;-39.15,173.85;-38.80,174.57;-38.03,174.74;-37.38,174.70;-36.71,174.29;-36.53,174.32;-36.12,173.84;-35.24,173.05;-34.53,172.64;-34.45,173.01;-35.01,173.55;-35.27,174.33;-36.16,174.61;-37.21,175.34;-36.53,175.36;-36.80,175.81;-37.56,175.96;-37.88,176.76;-37.96,177.44;-37.58,178.01;-37.70,178.52;-38.58,178.27;-39.17,177.97;-39.15,177.21;-39.45,176.94;-39.88,177.03;-40.07,176.89",
    "-43.56,169.67;-43.03,170.52;-42.51,171.13;-41.77,171.57;-41.51,171.95;-40.96,172.10;-40.49,172.80;-40.92,173.02;-41.33,173.25;-40.93,173.96;-41.35,174.25;-41.77,174.25;-42.23,173.88;-42.97,173.22;-43.37,172.71;-43.85,173.08;-43.87,172.31;-44.24,171.45;-44.90,171.19;-45.91,170.62;-46.36,169.83;-46.64,169.33;-46.62,168.41;-46.29,167.76;-46.22,166.68;-45.85,166.51;-45.11,167.05;-44.12,168.30;-43.94,168.95;-43.56,169.67",
    "-40.81,147.69;-40.88,148.29;-42.06,148.36;-42.41,148.02;-43.21,147.91;-42.94,147.56;-43.63,146.87;-43.58,146.66;-43.55,146.05;-42.69,145.43;-42.03,145.30;-41.16,144.72;-40.70,144.74;-40.79,145.40;-41.14,146.36;-41.00,146.91;-40.81,147.69",
    "-32.22,126.15;-32.73,125.09;-32.96,124.22;-33.48,124.03;-33.89,123.66;-33.91,122.81;-34.00,122.18;-33.82,121.30;-33.93,120.58;-33.98,119.89;-34.51,119.30;-34.46,119.01;-34.75,118.51;-35.06,118.02;-35.03,117.30;-35.03,116.63;-34.39,115.56;-34.20,115.03;-33.62,115.05;-33.49,115.55;-33.26,115.71;-32.90,115.68;-32.21,115.80;-31.61,115.69;-30.60,115.16;-30.03,115.00;-29.46,115.04;-28.81,114.64;-28.52,114.62;-28.12,114.17;-27.33,114.05;-26.54,113.48;-26.12,113.34;-26.55,113.78;-25.62,113.44;-25.91,113.94;-26.30,114.23;-25.79,114.22;-25.00,113.72;-24.68,113.63;-24.38,113.39;-23.81,113.50;-23.56,113.71;-23.06,113.84;-22.48,113.74;-21.76,114.15;-22.52,114.23;-21.83,114.65;-21.50,115.46;-21.07,115.95;-20.70,116.71;-20.62,117.17;-20.75,117.44;-20.37,118.23;-20.26,118.84;-20.04,118.99;-19.95,119.25;-19.98,119.81;-19.68,120.86;-19.24,121.40;-18.71,121.66;-18.20,122.24;-17.80,122.29;-17.25,122.31;-16.41,123.01;-17.27,123.43;-17.07,123.86;-16.60,123.50;-16.11,123.82;-16.33,124.26;-15.57,124.38;-15.08,124.93;-14.68,125.17;-14.51,125.67;-14.23,125.69;-14.35,126.13;-14.10,126.14;-13.95,126.58;-13.82,127.07;-14.28,127.80;-14.87,128.36;-14.88,128.99;-14.97,129.62;-14.42,129.41;-13.62,129.89;-13.36,130.34;-13.11,130.18;-12.54,130.62;-12.18,131.22;-12.30,131.74;-12.11,132.58;-11.60,132.56;-11.27,131.82;-11.13,132.36;-11.38,133.02;-11.79,133.55;-12.04,134.39;-11.94,134.68;-12.25,135.30;-11.96,135.88;-12.05,136.26;-11.86,136.49;-12.35,136.95;-12.89,136.69;-13.29,136.31;-13.32,135.96;-13.72,136.08;-14.22,135.78;-14.72,135.43;-15.00,135.50;-15.55,136.30;-15.87,137.07;-16.22,137.58;-16.81,138.30;-16.81,138.59;-17.06,139.11;-17.37,139.26;-17.71,140.22;-17.37,140.88;-16.83,141.07;-16.39,141.27;-15.84,141.40;-15.04,141.70;-14.56,141.56;-14.27,141.64;-13.70,141.52;-12.94,141.65;-12.74,141.84;-12.41,141.69;-11.88,141.93;-11.33,142.12;-11.04,142.14;-10.67,142.52;-11.16,142.80;-11.78,142.87;-11.91,143.12;-12.33,143.16;-12.83,143.52;-13.40,143.60;-13.76,143.56;-14.55,143.92;-14.17,144.56;-14.59,144.89;-14.98,145.37;-15.43,145.27;-16.29,145.49;-16.78,145.64;-16.91,145.89;-17.76,146.16;-18.28,146.06;-18.96,146.39;-19.48,147.47;-19.96,148.18;-20.39,148.85;-20.63,148.72;-21.26,149.29;-22.34,149.68;-22.12,150.08;-22.56,150.48;-22.40,150.73;-23.46,150.90;-24.08,151.61;-24.46,152.07;-25.27,152.86;-26.07,153.14;-26.64,153.16;-27.26,153.09;-28.11,153.57;-29.00,153.51;-29.46,153.34;-30.35,153.07;-30.92,153.09;-31.64,152.89;-32.55,152.45;-33.04,151.71;-33.82,151.34;-34.31,151.01;-35.17,150.71;-35.67,150.33;-36.42,150.08;-37.11,149.95;-37.43,150.00;-37.77,149.42;-37.81,148.30;-38.22,147.38;-38.61,146.92;-39.04,146.32;-38.59,145.49;-38.42,144.88;-37.90,145.03;-38.09,144.49;-38.81,143.61;-38.54,142.75;-38.38,142.18;-38.31,141.61;-38.02,140.64;-37.40,139.99;-36.64,139.81;-36.14,139.57;-35.73,139.08;-35.61,138.12;-35.13,138.45;-34.38,138.21;-35.08,137.72;-35.26,136.83;-34.71,137.35;-34.13,137.50;-33.64,137.89;-32.90,137.81;-33.75,137.00;-34.09,136.37;-34.89,135.99;-34.48,135.21;-33.95,135.24;-33.22,134.61;-32.85,134.09;-32.62,134.27;-32.01,132.99;-31.98,132.29;-31.50,131.33;-31.59,129.54;-31.95,128.24;-32.28,127.10;-32.22,126.15",
    "7.52,81.79;6.48,81.64;6.20,81.22;5.97,80.35;6.76,79.87;8.20,79.70;9.82,80.15;9.27,80.84;8.56,81.30;7.52,81.79",
    "42.92,80.18;43.18,80.87;44.92,79.97;45.32,81.95;45.54,82.46;47.33,83.18;47.00,85.16;47.45,85.72;48.46,85.77;48.55,86.60;49.21,87.36;49.30,87.75;48.60,88.01;48.07,88.85;47.69,90.28;46.89,90.97;45.72,90.59;45.29,90.95;45.12,92.13;44.98,93.48;44.35,94.69;44.24,95.31;43.32,95.76;42.73,96.35;42.75,97.45;42.52,99.52;42.66,100.85;42.51,101.83;41.91,103.31;41.91,104.52;41.60,104.96;42.13,106.13;42.48,107.74;42.52,109.24;42.87,110.41;43.41,111.13;43.74,111.83;44.07,111.67;44.46,111.35;45.10,111.87;45.01,112.44;44.81,113.46;45.34,114.46;45.73,115.99;46.39,116.72;46.67,117.42;46.81,118.87;46.69,119.66;47.05,119.77;47.75,118.87;48.07,118.06;47.70,117.30;47.85,116.31;47.73,115.74;48.14,115.49;49.13,116.19;49.89,116.68;49.51,117.88;50.14,119.29;50.58,119.28;51.64,120.18;51.96,120.74;52.52,120.73;52.75,120.18;53.25,121.00;53.43,122.25;53.46,123.57;53.16,125.07;52.79,125.95;51.78,126.56;51.35,126.94;50.74,127.29;49.76,127.66;49.44,129.40;48.73,130.58;47.79,130.99;47.79,132.51;48.18,133.37;48.48,135.03;47.58,134.50;47.21,134.11;46.12,133.77;45.14,133.10;45.32,131.88;44.97,131.03;44.11,131.29;42.93,131.14;42.90,130.63;42.40,130.64;42.99,129.99;42.42,129.60;41.99,128.05;41.47,128.21;41.50,127.34;41.82,126.87;41.11,126.18;40.57,125.08;39.93,124.27;39.64,122.87;39.17,122.13;38.90,121.05;39.36,121.59;39.75,121.38;40.42,122.17;40.95,121.64;40.59,120.77;39.90,119.64;39.25,119.02;39.20,118.04;38.74,117.53;38.06,118.06;37.90,118.88;37.45,118.91;37.16,119.70;37.87,120.82;37.48,121.71;37.45,122.36;36.93,122.52;36.65,121.10;36.11,120.64;35.61,119.66;34.91,119.15;34.36,120.23;33.38,120.62;32.46,121.23;31.69,121.91;30.95,121.89;30.68,121.26;30.14,121.50;29.83,122.09;29.02,121.94;28.23,121.68;28.14,121.13;27.05,120.40;25.74,119.59;24.55,118.66;23.62,117.28;22.78,115.89;22.67,114.76;22.22,114.15;22.55,113.81;22.05,113.24;21.55,111.84;21.40,110.79;20.34,110.44;20.28,109.89;21.01,109.63;21.40,109.86;21.72,108.52;21.55,108.05;21.81,107.04;22.22,106.57;22.79,106.73;22.98,105.81;23.35,105.33;22.82,104.48;22.70,103.50;22.71,102.71;22.46,102.17;22.32,101.65;21.17,101.80;21.20,101.27;21.44,101.18;21.85,101.15;21.56,100.42;21.74,99.98;22.12,99.24;22.95,99.53;23.14,98.90;24.06,98.66;23.90,97.60;25.08,97.72;25.92,98.67;26.74,98.71;27.51,98.68;27.75,98.25;28.34,97.91;28.26,97.34;28.26,97.34;28.26,97.34;28.27,97.34;28.27,97.34;28.27,97.34;28.27,97.33;28.27,97.33;28.27,97.33;28.27,97.33;28.27,97.33;28.27,97.33;28.27,97.33;28.27,97.33;28.27,97.33;28.42,96.27;28.82,96.59;28.83,96.59;28.83,96.60;28.83,96.60;28.83,96.60;28.83,96.60;28.83,96.60;28.83,96.60;28.83,96.60;28.83,96.60;28.83,96.60;28.84,96.60;28.84,96.60;28.84,96.59;29.46,96.13;29.46,96.13;29.46,96.12;29.46,96.12;29.46,96.12;29.46,96.12;29.46,96.12;29.46,96.12;29.46,96.12;29.46,96.12;29.46,96.12;29.46,96.12;29.46,96.12;29.46,96.11;29.46,96.11;29.46,96.11;29.04,95.40;29.29,94.57;29.29,94.57;29.29,94.57;29.29,94.57;29.29,94.56;29.29,94.56;29.29,94.56;29.29,94.56;29.29,94.56;28.65,93.41;28.65,93.41;28.65,93.41;27.91,92.50;27.78,91.70;27.78,91.69;27.78,91.69;27.78,91.69;27.78,91.69;27.78,91.69;27.78,91.69;27.78,91.69;27.78,91.69;27.78,91.69;28.04,91.26;28.06,90.73;28.30,90.02;28.04,89.48;27.31,88.82;28.09,88.74;28.09,88.74;28.09,88.74;28.09,88.74;28.09,88.74;28.09,88.74;28.09,88.74;28.09,88.74;28.09,88.74;28.10,88.74;28.10,88.73;28.10,88.73;28.10,88.73;28.10,88.73;28.10,88.73;28.10,88.73;28.10,88.73;28.10,88.73;28.10,88.73;27.89,88.12;27.89,88.12;27.89,88.12;27.88,88.11;27.88,88.11;27.88,88.11;27.88,88.11;27.88,88.11;27.88,88.11;27.88,88.11;27.88,88.11;27.88,88.11;27.88,88.11;27.97,86.95;28.20,85.82;28.64,85.01;28.84,84.23;29.32,83.90;29.46,83.34;30.12,82.33;30.42,81.53;30.19,81.12;30.19,81.12;30.19,81.12;30.19,81.12;30.19,81.12;30.19,81.12;30.19,81.12;30.89,79.73;31.51,78.77;32.48,79.21;32.48,79.21;32.48,79.21;32.48,79.21;32.99,79.26;32.99,79.26;32.99,79.26;32.99,79.26;32.99,79.26;32.99,79.26;32.99,79.26;33.00,79.26;33.00,79.26;33.51,78.86;34.31,78.96;34.54,79.90;34.54,79.90;34.54,79.90;34.54,79.91;34.54,79.91;34.54,79.91;34.54,79.91;34.54,79.91;34.54,79.91;35.34,80.39;35.35,80.39;35.35,80.39;35.35,80.39;35.35,80.39;35.35,80.39;35.35,80.39;35.35,80.39;35.35,80.39;35.35,80.39;35.35,80.39;35.36,80.39;35.36,80.39;35.96,79.96;35.96,79.96;35.96,79.96;35.96,79.96;35.96,79.96;35.96,79.95;35.96,79.95;35.96,79.95;35.96,79.95;36.06,79.25;36.06,79.25;36.06,79.25;36.06,79.25;35.96,78.42;35.96,78.42;35.96,78.42;35.96,78.42;35.96,78.41;35.96,78.41;35.53,77.84;35.96,76.23;36.70,75.93;36.70,75.93;36.71,75.93;36.71,75.93;36.71,75.93;36.71,75.93;36.71,75.93;37.17,75.19;37.17,75.18;37.17,75.18;37.17,75.18;37.17,75.18;37.17,75.18;37.17,75.18;37.17,75.18;37.16,75.14;37.42,74.98;37.99,74.83;38.38,74.86;38.61,74.26;38.51,73.93;39.43,73.68;39.66,73.96;39.89,73.82;40.37,74.78;40.56,75.47;40.43,76.53;41.07,76.90;41.19,78.19;41.58,78.54;42.12,80.12;42.35,80.26;42.92,80.18",
    "18.51,108.66;19.37,108.63;19.82,109.12;20.10,110.21;20.08,110.79;19.70,111.01;19.26,110.57;18.68,110.34;18.20,109.48;18.51,108.66",
    "24.39,121.78;22.79,121.18;21.97,120.75;22.81,120.22;23.56,120.11;24.54,120.69;25.30,121.50;25.00,121.95;24.39,121.78",
    "46.89,10.44;46.75,11.05;46.94,11.16;47.12,12.15;46.77,12.38;46.51,13.81;46.02,13.70;45.59,13.94;45.74,13.14;45.38,12.33;44.89,12.38;44.60,12.26;44.09,12.59;43.59,13.53;42.76,14.03;41.96,15.14;41.96,15.93;41.74,16.17;41.54,15.89;41.18,16.79;40.88,17.52;40.36,18.38;40.17,18.48;39.81,18.29;40.28,17.74;40.44,16.87;39.80,16.45;39.42,17.17;38.90,17.05;38.84,16.64;37.99,16.10;37.91,15.68;38.21,15.69;38.75,15.89;38.96,16.11;39.54,15.72;40.05,15.41;40.17,15.00;40.60,14.70;40.79,14.06;41.19,13.63;41.25,12.89;41.70,12.11;42.36,11.19;42.93,10.51;43.92,10.20;44.04,9.70;44.37,8.89;44.23,8.43;43.77,7.85;43.69,7.44;44.13,7.55;44.25,7.01;45.03,6.75;45.33,7.10;45.71,6.80;45.99,6.84;45.78,7.27;45.82,7.76;46.16,8.32;46.01,8.49;46.04,8.97;46.44,9.18;46.31,9.92;46.48,10.36;46.89,10.44",
    "38.14,14.76;38.23,15.52;37.44,15.16;37.13,15.31;36.62,15.10;37.00,14.34;37.10,13.83;37.61,12.43;38.13,12.57;38.03,13.74;38.14,14.76",
    "40.90,8.71;41.21,9.21;40.50,9.81;39.18,9.67;39.24,9.21;38.91,8.81;39.17,8.43;40.38,8.39;40.95,8.16;40.90,8.71",
    "54.98,9.92;54.83,9.28;54.96,8.53;55.52,8.12;56.54,8.09;56.81,8.26;57.11,8.54;57.17,9.42;57.45,9.78;57.73,10.58;57.22,10.55;56.89,10.25;56.61,10.37;56.46,10.91;56.08,10.67;56.19,10.37;55.47,9.65;54.98,9.92",
    "56.11,12.37;55.61,12.69;54.80,12.09;55.36,11.04;55.78,10.90;56.11,12.37",
    "53.87,-6.20;54.07,-6.95;54.06,-7.57;54.60,-7.37;55.13,-7.57;55.17,-6.73;54.55,-5.66;53.87,-6.20",
    "53.40,-3.09;53.40,-3.09;53.98,-2.95;54.60,-3.61;54.62,-3.63;54.79,-4.84;55.06,-5.08;55.51,-4.72;55.78,-5.05;55.31,-5.59;56.28,-5.64;56.79,-6.15;57.82,-5.79;58.63,-5.01;58.55,-4.21;58.64,-3.01;57.55,-4.07;57.69,-3.06;57.68,-1.96;56.87,-2.22;55.97,-3.12;55.91,-2.09;55.80,-2.01;54.62,-1.11;54.46,-0.43;53.33,0.18;52.93,0.47;52.74,1.68;52.10,1.56;51.81,1.05;51.29,1.45;50.77,0.55;50.77,-0.79;50.50,-2.49;50.70,-2.96;50.23,-3.62;50.34,-4.54;49.96,-5.25;50.16,-5.78;51.21,-4.31;51.43,-3.41;51.43,-3.42;51.59,-4.98;51.99,-5.27;52.30,-4.22;52.84,-4.77;53.50,-4.58;53.40,-3.09",
    "66.46,-14.51;65.81,-14.74;65.13,-13.61;64.36,-14.91;63.68,-17.79;63.50,-18.66;63.64,-19.97;63.96,-22.76;64.40,-21.78;64.89,-23.96;65.08,-22.18;65.38,-22.23;65.61,-24.33;66.26,-23.65;66.41,-22.13;65.73,-20.58;66.28,-19.06;65.99,-17.80;66.53,-16.17;66.46,-14.51",
    "41.86,46.40;41.83,46.69;41.22,47.37;41.15,47.82;41.41,47.99;41.81,48.58;41.28,49.11;40.57,49.62;40.53,50.08;40.26,50.39;40.18,49.57;39.40,49.40;39.05,49.22;38.82,48.86;38.32,48.88;38.27,48.63;38.79,48.01;39.29,48.36;39.58,48.06;39.51,47.69;38.77,46.51;39.46,46.48;39.63,46.03;39.90,45.61;40.22,45.89;40.56,45.36;40.81,45.56;40.99,45.18;41.25,44.97;41.41,45.22;41.12,45.96;41.06,46.50;41.18,46.64;41.72,46.15;41.86,46.40",
    "38.74,46.14;38.87,45.46;39.34,44.95;39.71,44.79;39.74,45.00;39.47,45.30;39.47,45.74;39.32,45.74;38.74,46.14",
    "43.43,39.96;43.55,40.08;43.38,40.92;43.22,42.39;42.74,43.76;42.55,43.93;42.71,44.54;42.50,45.47;42.09,45.78;41.86,46.40;41.72,46.15;41.18,46.64;41.06,46.50;41.12,45.96;41.41,45.22;41.25,44.97;41.09,43.58;41.58,42.62;41.54,41.55;41.96,41.70;42.65,41.45;43.01,40.88;43.13,40.32;43.43,39.96",
    "12.70,120.83;13.47,120.32;13.43,121.18;13.07,121.53;12.21,121.26;12.70,120.83",
    "9.98,122.59;10.26,122.84;10.88,122.95;10.94,123.50;10.27,123.34;11.23,124.08;10.28,123.98;9.95,123.62;9.32,123.31;9.02,123.00;9.71,122.38;9.98,122.59",
    "8.41,126.38;7.75,126.48;7.19,126.54;6.27,126.20;7.29,125.83;6.79,125.36;6.05,125.68;5.58,125.40;6.16,124.22;6.89,123.94;7.36,124.24;7.83,123.61;7.42,123.30;7.46,122.83;6.90,122.09;7.19,121.92;8.03,122.31;8.32,122.94;8.69,123.49;8.24,123.84;8.51,124.60;8.96,124.76;8.99,125.47;9.76,125.41;9.29,126.22;8.78,126.31;8.41,126.38",
    "9.32,118.50;8.37,117.17;9.07,117.66;9.68,118.39;10.38,118.99;11.37,119.51;10.55,119.69;10.00,119.03;9.32,118.50",
    "18.22,122.34;17.81,122.17;17.09,122.52;16.26,122.25;15.93,121.66;15.12,121.51;14.33,121.73;14.22,122.26;14.34,122.70;13.78,123.95;13.24,123.86;13.00,124.18;12.54,124.08;13.03,123.30;13.55,122.93;13.19,122.67;13.78,122.03;13.64,121.13;13.86,120.63;14.27,120.68;14.53,120.99;14.76,120.69;14.40,120.56;14.97,120.07;15.41,119.92;16.36,119.88;16.03,120.29;17.60,120.39;18.51,120.72;18.50,121.32;18.22,121.94;18.48,122.25;18.22,122.34",
    "11.42,122.04;11.89,121.88;11.58,122.48;11.58,123.12;11.17,123.10;10.74,122.64;10.44,122.00;10.91,121.97;11.42,122.04",
    "12.16,125.50;11.05,125.78;11.31,125.01;10.98,125.03;10.36,125.28;10.13,124.80;10.84,124.76;10.89,124.46;11.50,124.30;11.42,124.89;11.79,124.88;12.56,124.27;12.54,125.23;12.16,125.50",
    "6.46,100.09;6.64,100.26;6.20,101.08;5.69,101.15;5.81,101.81;6.22,102.14;6.13,102.37;5.52,102.96;4.86,103.38;4.18,103.44;3.73,103.33;3.38,103.43;2.79,103.50;2.52,103.85;1.63,104.25;1.29,104.23;1.23,103.52;1.97,102.57;2.76,101.39;3.27,101.27;3.94,100.70;4.77,100.56;5.31,100.20;6.04,100.31;6.46,100.09",
    "4.14,117.88;4.31,117.02;4.31,115.87;3.17,115.52;2.82,115.13;1.43,114.62;1.22,113.81;1.50,112.86;1.41,112.38;0.90,111.80;0.98,111.16;0.77,110.51;1.34,109.83;2.01,109.66;1.66,110.40;1.85,111.17;2.70,111.37;2.89,111.80;3.10,113.00;3.89,113.71;4.53,114.20;4.01,114.66;4.35,114.87;4.32,115.35;4.96,115.41;5.45,115.45;6.14,116.22;6.92,116.73;6.93,117.13;6.42,117.64;5.99,117.69;5.71,118.35;5.41,119.18;5.02,119.11;4.97,118.44;4.48,118.62;4.14,117.88",
    "5.45,115.45;4.96,115.41;4.32,115.35;4.35,114.87;4.01,114.66;4.53,114.20;4.90,114.60;5.45,115.45",
    "46.51,13.81;46.43,14.63;46.66,15.14;46.68,16.01;46.85,16.20;46.84,16.37;46.50,16.56;46.24,15.77;45.83,15.67;45.73,15.32;45.45,15.33;45.47,14.94;45.63,14.60;45.47,14.41;45.50,13.72;45.59,13.94;46.02,13.70;46.51,13.81",
    "69.06,28.59;68.36,28.45;67.70,29.98;66.94,29.05;65.81,30.22;64.95,29.54;64.20,30.44;63.55,30.04;62.87,31.52;62.36,31.14;61.78,30.21;60.50,28.07;60.50,28.07;60.50,28.07;60.42,26.26;60.06,24.50;59.85,22.87;60.39,22.29;60.72,21.32;61.71,21.54;62.61,21.06;63.19,21.54;63.82,22.44;64.90,24.73;65.11,25.40;65.53,25.29;66.01,23.90;66.40,23.57;67.94,23.54;68.62,21.98;69.11,20.65;69.37,21.24;68.84,22.36;68.89,23.66;68.65,24.74;69.09,25.69;69.83,26.18;70.16,27.73;69.77,29.02;69.06,28.59",
    "49.09,22.56;48.83,22.28;48.42,22.09;48.32,21.87;48.62,20.80;48.56,20.47;48.33,20.24;48.20,19.77;48.27,19.66;48.11,19.17;48.08,18.78;47.88,18.70;47.76,17.86;47.87,17.49;48.12,16.98;48.47,16.88;48.60,16.96;48.82,17.10;48.80,17.55;48.90,17.89;49.00,17.91;49.04,18.10;49.27,18.17;49.32,18.40;49.50,18.55;49.50,18.85;49.44,18.91;49.57,19.32;49.22,19.83;49.43,20.42;49.33,20.89;49.47,21.61;49.09,22.56",
    "51.11,15.02;50.78,15.49;50.70,16.24;50.42,16.18;50.22,16.72;50.47,16.87;50.36,17.55;50.05,17.65;49.99,18.39;49.50,18.85;49.50,18.55;49.32,18.40;49.27,18.17;49.04,18.10;49.00,17.91;48.90,17.89;48.80,17.55;48.82,17.10;48.60,16.96;48.79,16.50;48.73,16.03;49.04,15.25;48.96,14.90;48.56,14.34;48.88,13.60;49.31,13.03;49.55,12.52;49.97,12.42;50.27,12.24;50.48,12.97;50.73,13.34;50.93,14.06;51.12,14.31;51.00,14.57;51.11,15.02",
    "14.42,36.43;14.82,36.32;16.29,36.75;16.96,36.85;17.26,37.17;17.43,37.90;18.00,38.41;16.84,38.99;15.92,39.27;15.44,39.81;14.49,41.18;13.92,41.73;13.34,42.28;13.00,42.59;12.70,43.08;12.46,42.78;12.54,42.35;12.87,42.01;13.45,41.60;13.77,41.16;14.12,40.90;14.52,40.03;14.53,39.34;14.74,39.10;14.51,38.51;14.96,37.91;14.21,37.59;14.42,36.43",
    "39.18,141.88;38.17,140.96;37.14,140.98;36.34,140.60;35.84,140.77;35.14,140.25;34.67,138.98;34.61,137.22;33.46,135.79;33.85,135.12;34.60,135.08;34.38,133.34;33.90,132.16;33.89,130.99;33.15,132.00;31.45,131.33;31.03,130.69;31.42,130.20;32.32,130.45;32.61,129.81;33.30,129.41;33.60,130.35;34.23,130.88;34.75,131.88;35.43,132.62;35.73,134.61;35.53,135.68;37.30,136.72;36.83,137.39;37.83,138.86;38.22,139.43;39.44,140.05;40.56,139.88;41.20,140.31;41.38,141.37;39.99,141.91;39.18,141.88",
    "43.96,144.61;44.38,145.32;43.26,145.54;42.99,144.06;42.00,143.18;42.68,141.61;41.58,141.07;41.57,139.96;42.56,139.82;43.33,140.31;43.39,141.38;44.77,141.67;45.55,141.97;44.51,143.14;44.17,143.91;43.96,144.61",
    "33.46,132.37;34.06,132.92;33.94,133.49;34.36,133.90;34.15,134.64;33.81,134.77;33.20,134.20;33.52,133.79;33.29,133.28;32.70,133.01;32.99,132.36;33.46,132.37",
    "-20.18,-58.17;-20.73,-57.87;-22.09,-57.94;-22.28,-56.88;-22.09,-56.47;-22.36,-55.80;-22.66,-55.61;-23.57,-55.52;-23.96,-55.40;-24.00,-55.03;-23.84,-54.65;-24.02,-54.29;-24.57,-54.29;-25.16,-54.43;-25.74,-54.63;-26.62,-54.79;-27.39,-55.70;-27.55,-56.49;-27.40,-57.61;-27.12,-58.62;-25.60,-57.63;-25.16,-57.78;-24.77,-58.81;-24.03,-60.03;-23.88,-60.85;-22.25,-62.69;-21.05,-62.29;-20.51,-62.27;-19.63,-61.79;-19.34,-60.04;-19.36,-59.12;-19.87,-58.18;-20.18,-58.17",
    "19.00,52.00;17.35,52.78;16.65,53.11;16.38,52.39;15.94,52.19;15.60,52.17;15.18,51.17;14.71,49.57;14.00,48.68;13.95,48.24;14.01,47.94;13.59,47.35;13.40,46.72;13.35,45.88;13.29,45.63;13.03,45.41;12.95,45.14;12.70,44.99;12.72,44.49;12.59,44.18;12.64,43.48;13.22,43.22;13.77,43.25;14.06,43.09;14.80,42.89;15.21,42.60;15.26,42.81;15.72,42.70;15.91,42.82;16.35,42.78;16.67,43.22;17.09,43.12;17.58,43.38;17.32,43.79;17.41,44.06;17.43,45.22;17.33,45.40;17.23,46.37;17.28,46.75;16.95,47.00;17.12,47.47;18.17,48.18;18.62,49.12;19.00,52.00",
    "29.36,34.96;29.20,36.07;29.51,36.50;29.87,36.74;30.00,37.50;30.34,37.67;30.51,38.00;31.51,37.00;32.01,39.00;32.16,39.20;31.89,40.40;31.19,41.89;29.18,44.71;29.10,46.57;29.00,47.46;28.53,47.71;28.55,48.42;27.69,48.81;27.46,49.30;27.11,49.47;26.69,50.15;26.28,50.21;25.94,50.11;25.61,50.24;25.33,50.53;25.00,50.66;24.75,50.81;24.56,51.11;24.63,51.39;24.25,51.58;24.01,51.62;23.00,52.00;22.50,55.01;22.71,55.21;22.00,55.67;20.00,55.00;19.00,52.00;18.62,49.12;18.17,48.18;17.12,47.47;16.95,47.00;17.28,46.75;17.23,46.37;17.33,45.40;17.43,45.22;17.41,44.06;17.32,43.79;17.58,43.38;17.09,43.12;16.67,43.22;16.35,42.78;16.77,42.65;17.08,42.35;17.47,42.27;17.83,41.75;18.67,41.22;19.49,40.94;20.17,40.25;20.34,39.80;21.29,39.14;21.99,39.02;22.58,39.07;23.69,38.49;24.08,38.02;24.29,37.48;24.86,37.15;25.08,37.21;25.60,36.93;25.83,36.64;26.57,36.25;27.38,35.64;28.06,35.13;28.06,34.63;28.61,34.79;28.96,34.83;29.36,34.96",
    "-78.05,-48.66;-78.05,-48.15;-77.83,-46.66;-78.05,-45.15;-78.48,-43.92;-79.09,-43.49;-79.52,-43.37;-80.03,-43.33;-80.34,-44.88;-80.59,-46.51;-80.83,-48.39;-81.03,-50.48;-80.97,-52.85;-80.63,-54.16;-80.22,-53.99;-79.95,-51.85;-79.61,-50.99;-79.18,-50.36;-78.81,-49.91;-78.46,-49.31;-78.05,-48.66;-78.05,-48.66",
    "-80.26,-66.29;-80.29,-64.04;-80.39,-61.88;-79.98,-61.14;-79.63,-60.61;-80.04,-59.57;-80.55,-59.87;-81.00,-60.16;-80.86,-62.26;-80.92,-64.49;-80.59,-65.74;-80.55,-65.74;-80.26,-66.29",
    "-71.27,-73.92;-71.27,-73.92;-71.15,-73.23;-71.19,-72.07;-70.68,-71.78;-70.31,-71.72;-69.51,-71.74;-69.04,-71.17;-68.88,-70.25;-69.25,-69.72;-69.62,-69.49;-70.07,-69.06;-70.51,-68.73;-70.96,-68.45;-71.41,-68.33;-71.80,-68.51;-72.17,-68.78;-72.31,-69.96;-72.50,-71.08;-72.48,-72.39;-72.09,-71.90;-72.23,-73.07;-72.37,-74.19;-72.07,-74.95;-71.66,-75.01;-71.27,-73.92",
    "-71.89,-102.33;-71.89,-102.33;-71.72,-101.70;-71.85,-100.43;-71.93,-98.98;-72.07,-97.88;-71.95,-96.79;-72.52,-96.20;-72.44,-96.98;-72.48,-98.20;-72.44,-99.43;-72.50,-100.78;-72.31,-101.80;-71.89,-102.33",
    "-73.66,-122.62;-73.66,-122.62;-73.32,-122.41;-73.50,-121.21;-73.66,-119.92;-73.48,-118.72;-73.83,-119.29;-74.09,-120.23;-74.01,-121.62;-73.66,-122.62",
    "-73.46,-127.28;-73.46,-127.28;-73.25,-126.56;-73.48,-125.56;-73.87,-124.03;-73.83,-124.62;-73.74,-125.91;-73.46,-127.28",
    "-78.60,-163.71;-78.60,-163.71;-78.22,-163.11;-78.38,-161.25;-78.69,-160.25;-79.05,-159.48;-79.50,-159.21;-79.63,-161.13;-79.28,-162.44;-78.93,-163.03;-78.87,-163.07;-78.60,-163.71",
    "-84.71,180.00;-90.00,180.00;-90.00,-180.00;-84.71,-180.00;-84.72,-179.94;-84.14,-179.06;-84.45,-177.26;-84.42,-177.14;-84.10,-176.08;-84.11,-175.95;-84.12,-175.83;-84.53,-174.38;-84.12,-173.12;-84.06,-172.89;-83.88,-169.95;-84.12,-169.00;-84.24,-168.53;-84.57,-167.02;-84.83,-164.18;-85.14,-161.93;-85.37,-158.07;-85.10,-155.19;-85.30,-150.94;-85.61,-148.53;-85.32,-145.89;-85.04,-143.11;-84.57,-142.89;-84.53,-146.83;-84.30,-150.06;-83.90,-150.90;-83.69,-153.59;-83.24,-153.41;-82.83,-153.04;-82.45,-152.67;-82.04,-152.86;-81.77,-154.53;-81.42,-155.29;-81.10,-156.84;-81.16,-154.41;-81.00,-152.10;-81.34,-150.65;-81.04,-148.87;-80.67,-147.22;-80.34,-146.42;-79.93,-146.77;-79.65,-148.06;-79.36,-149.53;-79.30,-151.59;-79.16,-153.39;-79.06,-155.33;-78.69,-155.98;-78.38,-157.27;-78.03,-158.05;-76.89,-158.37;-76.99,-157.88;-77.30,-156.97;-77.20,-155.33;-77.07,-153.74;-77.50,-152.92;-77.40,-151.33;-77.18,-150.00;-76.91,-148.75;-76.58,-147.61;-76.48,-146.10;-76.11,-146.14;-75.73,-146.50;-75.38,-146.20;-75.20,-144.91;-75.54,-144.32;-75.34,-142.79;-75.09,-141.64;-75.07,-140.21;-74.97,-138.86;-74.73,-137.51;-74.52,-136.43;-74.30,-135.21;-74.36,-134.43;-74.44,-133.75;-74.30,-132.26;-74.48,-130.93;-74.46,-129.55;-74.32,-128.24;-74.42,-126.89;-74.52,-125.40;-74.48,-124.01;-74.50,-122.56;-74.52,-121.07;-74.48,-119.70;-74.19,-118.68;-74.03,-117.47;-74.24,-116.22;-74.07,-115.02;-73.71,-113.94;-74.03,-113.30;-74.38,-112.95;-74.71,-112.30;-74.42,-111.26;-74.79,-110.07;-74.91,-108.71;-75.18,-107.56;-75.13,-106.15;-74.95,-104.88;-74.99,-103.37;-75.13,-102.02;-75.30,-100.65;-74.87,-100.12;-74.54,-100.76;-74.19,-101.25;-74.11,-102.55;-73.73,-103.11;-73.36,-103.33;-72.62,-103.68;-72.75,-102.92;-72.81,-101.61;-72.75,-100.31;-72.91,-99.14;-73.21,-98.12;-73.56,-97.69;-73.62,-96.34;-73.48,-95.04;-73.28,-93.67;-73.17,-92.44;-73.40,-91.42;-73.32,-90.09;-72.56,-89.23;-73.01,-88.42;-73.19,-87.27;-73.09,-86.01;-73.48,-85.19;-73.52,-83.88;-73.64,-82.67;-73.85,-81.47;-73.48,-80.69;-73.13,-80.30;-73.52,-79.30;-73.42,-77.93;-73.64,-76.91;-73.97,-76.22;-73.87,-74.89;-73.66,-73.85;-73.40,-72.83;-73.26,-71.62;-73.15,-70.21;-73.01,-68.94;-72.79,-67.96;-72.48,-67.37;-72.05,-67.13;-71.64,-67.25;-71.25,-67.56;-70.85,-67.92;-70.46,-68.23;-70.11,-68.49;-69.72,-68.54;-69.33,-68.45;-68.95,-67.98;-68.54,-67.58;-68.15,-67.43;-67.72,-67.62;-67.33,-67.74;-66.88,-67.25;-66.58,-66.70;-66.21,-66.06;-65.90,-65.37;-65.60,-64.57;-65.17,-64.18;-64.90,-63.63;-64.64,-63.00;-64.58,-62.04;-64.27,-61.41;-64.07,-60.71;-63.96,-59.89;-63.70,-59.16;-63.39,-58.59;-63.27,-57.81;-63.53,-57.22;-63.86,-57.60;-64.15,-58.61;-64.37,-59.05;-64.21,-59.79;-64.31,-60.61;-64.54,-61.30;-64.80,-62.02;-65.09,-62.51;-65.48,-62.65;-65.86,-62.59;-66.19,-62.12;-66.43,-62.81;-66.50,-63.75;-66.84,-64.29;-67.15,-64.88;-67.58,-65.51;-67.95,-65.67;-68.37,-65.31;-68.68,-64.78;-68.91,-63.96;-69.23,-63.20;-69.62,-62.79;-69.99,-62.57;-70.38,-62.28;-70.72,-61.81;-71.09,-61.51;-72.01,-61.38;-72.38,-61.08;-72.77,-61.00;-73.17,-60.69;-73.70,-60.83;-74.11,-61.38;-74.44,-61.96;-74.58,-63.30;-74.93,-63.75;-75.26,-64.35;-75.64,-65.86;-75.79,-67.19;-76.01,-68.45;-76.22,-69.80;-76.63,-70.60;-76.67,-72.21;-76.63,-73.97;-76.71,-75.56;-76.71,-77.24;-77.10,-76.93;-77.28,-75.40;-77.56,-74.28;-77.91,-73.66;-78.22,-74.77;-78.12,-76.50;-78.38,-77.93;-78.79,-77.98;-79.18,-78.02;-79.51,-76.85;-79.89,-76.63;-80.26,-75.36;-80.42,-73.24;-80.69,-71.44;-81.00,-70.01;-81.32,-68.19;-81.47,-65.70;-81.75,-63.26;-82.04,-61.55;-82.38,-59.69;-82.85,-58.71;-83.22,-58.22;-82.87,-57.01;-82.57,-55.36;-82.26,-53.62;-82.00,-51.54;-81.73,-49.76;-81.71,-47.27;-81.85,-44.83;-82.08,-42.81;-81.65,-42.16;-81.36,-40.77;-81.34,-38.24;-81.12,-36.27;-80.91,-34.39;-80.77,-32.31;-80.59,-30.10;-80.34,-28.55;-79.99,-29.25;-79.63,-29.69;-79.26,-29.69;-79.30,-31.62;-79.46,-33.68;-79.46,-35.64;-79.08,-35.91;-78.34,-35.78;-78.12,-35.33;-77.89,-33.90;-77.65,-32.21;-77.36,-31.00;-77.07,-29.78;-76.67,-28.88;-76.50,-27.51;-76.36,-26.16;-76.28,-25.47;-76.24,-23.93;-76.11,-22.46;-75.91,-21.22;-75.67,-20.01;-75.44,-18.91;-75.13,-17.52;-74.79,-16.64;-74.50,-15.70;-74.11,-15.41;-73.87,-16.47;-73.46,-16.11;-73.15,-15.45;-72.95,-14.41;-72.72,-13.31;-72.40,-12.29;-72.01,-11.51;-71.54,-11.02;-71.27,-10.30;-71.32,-9.10;-71.66,-8.61;-71.70,-7.42;-71.32,-7.38;-70.93,-6.87;-71.03,-5.79;-71.40,-5.54;-71.46,-4.34;-71.29,-3.05;-71.17,-1.80;-71.23,-0.66;-71.64,-0.23;-71.30,0.87;-71.13,1.89;-70.99,3.02;-70.85,4.14;-70.62,5.16;-70.46,6.27;-70.25,7.14;-69.89,7.74;-70.15,8.49;-70.01,9.53;-70.48,10.25;-70.83,10.82;-70.64,11.95;-70.25,12.40;-69.97,13.42;-70.03,14.73;-70.40,15.13;-70.03,15.95;-69.91,17.03;-69.87,18.20;-69.89,19.26;-70.01,20.38;-70.07,21.45;-70.40,21.92;-70.70,22.57;-70.52,23.67;-70.48,24.84;-70.48,25.98;-70.46,27.09;-70.32,28.09;-70.21,29.15;-69.93,30.03;-69.76,30.97;-69.66,31.99;-69.38,32.75;-68.84,33.30;-68.50,33.87;-68.66,34.91;-69.01,35.30;-69.25,36.16;-69.17,37.20;-69.52,37.91;-69.78,38.65;-69.54,39.67;-69.11,40.02;-68.93,40.92;-68.60,41.96;-68.46,42.94;-68.27,44.11;-68.05,44.90;-67.82,45.72;-67.60,46.50;-67.72,47.44;-67.37,48.34;-67.09,48.99;-67.11,49.93;-66.88,50.75;-66.52,50.95;-66.25,51.79;-66.05,52.61;-65.90,53.61;-65.82,54.53;-65.88,55.41;-65.97,56.36;-66.25,57.16;-66.68,57.26;-67.01,58.14;-67.29,58.74;-67.41,59.94;-67.68,60.61;-67.95,61.43;-68.01,62.39;-67.82,63.19;-67.41,64.05;-67.62,64.99;-67.74,65.97;-67.86,66.91;-67.93,67.89;-67.93,68.89;-68.97,69.71;-69.23,69.67;-69.68,69.56;-69.93,68.60;-70.31,67.81;-70.70,67.95;-70.68,69.07;-71.07,68.93;-71.44,68.42;-71.85,67.95;-72.17,68.71;-72.26,69.87;-72.09,71.02;-71.70,71.57;-71.32,71.91;-71.01,72.45;-70.72,73.08;-70.36,73.34;-69.87,73.86;-69.78,74.49;-69.74,75.63;-69.62,76.63;-69.46,77.64;-69.07,78.13;-68.70,78.43;-68.33,79.11;-68.07,80.09;-67.88,80.94;-67.54,81.48;-67.37,82.05;-67.21,82.78;-67.31,83.78;-67.21,84.68;-67.09,85.66;-67.15,86.75;-66.88,87.48;-66.21,87.99;-66.48,88.36;-66.95,88.83;-67.15,89.67;-67.23,90.63;-67.11,91.59;-67.19,92.61;-67.21,93.55;-67.11,94.18;-67.17,95.02;-67.39,95.78;-67.25,96.68;-67.25,97.76;-67.11,98.68;-67.25,99.72;-66.92,100.38;-66.58,100.89;-66.31,101.58;-65.56,102.83;-65.70,103.48;-65.97,104.24;-66.33,104.91;-66.93,106.18;-66.95,107.16;-66.95,108.08;-66.84,109.16;-66.70,110.24;-66.43,111.06;-66.13,111.74;-66.09,112.86;-65.88,113.60;-66.07,114.39;-66.39,114.90;-66.70,115.60;-66.66,116.70;-66.92,117.38;-67.17,118.58;-67.27,119.83;-67.19,120.87;-66.88,121.65;-66.56,122.32;-66.48,123.22;-66.62,124.12;-66.72,125.16;-66.56,126.10;-66.56,127.00;-66.66,127.88;-66.76,128.80;-66.58,129.70;-66.43,130.78;-66.39,131.80;-66.39,132.94;-66.29,133.86;-66.21,134.76;-65.72,135.03;-65.31,135.07;-65.58,135.70;-66.03,135.87;-66.45,136.21;-66.78,136.62;-66.95,137.46;-66.90,138.60;-66.88,139.91;-66.82,140.81;-66.82,142.12;-66.80,143.06;-66.84,144.37;-66.92,145.49;-67.23,146.20;-67.60,146.00;-67.90,146.65;-68.13,147.72;-68.39,148.84;-68.56,150.13;-68.72,151.48;-68.87,152.50;-68.89,153.64;-68.56,154.28;-68.84,155.17;-69.15,155.93;-69.38,156.81;-69.48,158.03;-69.60,159.18;-69.99,159.67;-70.23,160.81;-70.58,161.57;-70.74,162.69;-70.72,163.84;-70.78,164.92;-70.76,166.11;-70.83,167.31;-70.97,168.43;-71.21,169.46;-71.40,170.50;-71.70,171.21;-72.09,171.09;-72.44,170.56;-72.89,170.11;-73.24,169.76;-73.66,169.29;-73.81,167.98;-74.17,167.39;-74.38,166.09;-74.77,165.64;-75.15,164.96;-75.46,164.23;-75.87,163.82;-76.24,163.57;-76.69,163.47;-77.07,163.49;-77.46,164.06;-77.83,164.27;-78.18,164.74;-78.32,166.60;-78.75,167.00;-78.91,165.19;-79.12,163.67;-79.16,161.77;-79.73,160.92;-80.20,160.75;-80.57,160.32;-80.95,159.79;-81.28,161.12;-81.69,161.63;-82.06,162.49;-82.40,163.71;-82.71,165.10;-83.02,166.60;-83.34,168.90;-83.83,169.40;-84.04,172.28;-84.12,172.48;-84.41,173.22;-84.16,175.99;-84.47,178.28;-84.71,180.00",
    "35.14,32.73;35.15,32.80;35.39,32.95;35.37,33.67;35.67,34.58;35.25,33.90;35.06,33.97;35.09,33.87;35.02,33.68;35.04,33.53;35.00,33.48;35.10,33.46;35.16,33.38;35.17,33.19;35.09,32.92;35.14,32.73",
    "35.14,32.73;35.09,32.92;35.17,33.19;35.16,33.38;35.10,33.46;35.00,33.48;35.04,33.53;35.02,33.68;35.09,33.87;35.06,33.97;34.98,34.00;34.57,32.98;34.70,32.49;35.10,32.26;35.14,32.73",
    "35.17,-2.17;34.53,-1.79;33.92,-1.73;32.86,-1.39;32.65,-1.12;32.26,-1.31;32.09,-2.62;31.72,-3.07;31.64,-3.65;30.90,-3.69;30.50,-4.86;30.00,-5.24;29.73,-6.06;29.58,-7.06;28.84,-8.67;27.66,-8.67;27.66,-8.82;27.12,-8.79;27.09,-9.41;26.86,-9.74;26.86,-10.19;26.99,-10.55;26.88,-11.39;26.10,-11.72;26.03,-12.03;24.77,-12.50;23.69,-13.89;22.31,-14.22;21.86,-14.63;21.50,-14.75;21.42,-17.00;21.42,-17.02;21.89,-16.97;22.16,-16.59;22.68,-16.26;23.02,-16.33;23.72,-15.98;24.36,-15.43;24.52,-15.09;25.10,-14.82;25.64,-14.80;26.25,-14.44;26.62,-13.77;27.64,-13.14;27.65,-13.12;28.04,-12.62;28.15,-11.69;28.83,-10.90;29.10,-10.40;29.93,-9.56;31.18,-9.81;32.04,-9.43;32.56,-9.30;33.24,-8.66;33.70,-7.65;34.11,-6.91;35.15,-6.24;35.76,-5.93;35.76,-5.19;35.33,-4.59;35.40,-3.64;35.18,-2.60;35.17,-2.17",
    "22.00,36.87;22.00,32.90;22.00,29.02;22.00,25.00;25.68,25.00;29.24,25.00;30.04,24.70;30.66,24.96;31.09,24.80;31.57,25.16;31.59,26.50;31.32,27.46;31.03,28.45;30.87,28.91;31.19,29.68;31.47,30.10;31.56,30.98;31.43,31.69;30.93,31.96;31.26,32.19;31.02,32.99;30.97,33.77;31.22,34.27;31.22,34.27;29.76,34.82;29.50,34.92;29.10,34.64;28.34,34.43;27.82,34.15;27.65,33.92;27.97,33.59;28.42,33.14;29.85,32.42;29.76,32.32;28.71,32.73;27.70,33.35;26.14,34.10;25.60,34.47;25.03,34.80;23.93,35.69;23.75,35.49;23.10,35.53;22.20,36.69;22.00,36.87",
    "22.00,25.00;20.00,25.00;20.00,23.85;19.58,23.84;21.50,19.85;23.41,15.86;22.86,14.85;22.49,14.14;23.04,13.58;23.47,12.00;24.10,11.56;24.56,10.77;24.38,10.30;24.94,9.95;25.37,9.91;26.09,9.32;26.51,9.72;27.14,9.63;27.69,9.76;28.14,9.68;28.96,9.86;29.42,9.81;30.31,9.48;30.54,9.97;30.96,10.06;31.38,9.95;31.76,10.64;32.08,10.94;32.37,11.43;33.14,11.49;32.79,12.66;32.88,13.08;32.71,13.92;32.27,15.25;31.38,15.71;31.18,16.61;30.76,18.02;30.27,19.09;30.53,19.57;30.99,20.05;31.75,19.82;32.24,20.13;32.71,20.85;32.84,21.54;32.64,22.90;32.19,23.24;32.19,23.61;32.02,23.93;31.90,24.92;31.57,25.16;31.09,24.80;30.66,24.96;30.04,24.70;29.24,25.00;25.68,25.00;22.00,25.00",
    "8.00,47.79;5.00,44.96;4.96,43.66;4.25,42.77;4.23,42.13;3.92,41.86;3.92,41.17;4.26,40.77;3.84,39.85;3.42,39.56;3.50,38.89;3.62,38.67;3.59,38.44;3.60,38.12;4.45,36.86;4.45,36.16;4.78,35.82;5.34,35.82;5.51,35.30;6.59,34.71;6.83,34.25;7.23,34.08;7.71,33.57;7.78,32.95;8.35,33.29;8.38,33.83;8.68,33.97;9.58,33.96;10.63,34.26;10.91,34.73;11.32,34.83;12.08,35.26;12.58,35.86;13.56,36.27;14.42,36.43;14.21,37.59;14.96,37.91;14.51,38.51;14.74,39.10;14.53,39.34;14.52,40.03;14.12,40.90;13.77,41.16;13.45,41.60;12.87,42.01;12.54,42.35;12.10,42.00;11.63,41.66;11.36,41.74;11.05,41.76;11.03,42.31;11.11,42.55;10.93,42.78;10.57,42.56;10.02,42.93;9.54,43.30;9.18,43.68;8.00,46.95;8.00,47.79",
    "12.54,42.35;12.46,42.78;12.70,43.08;12.39,43.32;11.97,43.29;11.74,42.72;11.46,43.15;10.93,42.78;11.11,42.55;11.03,42.31;11.05,41.76;11.36,41.74;11.63,41.66;12.10,42.00;12.54,42.35",
    "11.41,48.95;11.41,48.95;11.39,48.94;10.98,48.94;9.97,48.94;9.45,48.94;8.84,48.49;8.00,47.79;8.00,46.95;9.18,43.68;9.54,43.30;10.02,42.93;10.57,42.56;10.93,42.78;11.46,43.15;11.28,43.47;10.86,43.67;10.45,44.12;10.44,44.61;10.70,45.56;10.82,46.65;11.13,47.53;11.19,48.02;11.38,48.38;11.41,48.95;11.41,48.95",
    "-0.95,33.90;-1.03,31.87;-1.01,30.77;-1.13,30.42;-1.44,29.82;-1.34,29.58;-0.59,29.59;-0.21,29.82;0.60,29.88;1.06,30.09;1.58,30.47;1.85,30.85;2.20,31.17;2.34,30.77;3.51,30.83;3.51,30.83;3.78,31.25;3.56,31.88;3.79,32.69;3.79,33.39;4.25,34.01;3.56,34.48;3.05,34.60;1.91,35.04;1.18,34.67;0.52,34.18;0.11,33.89;-0.95,33.90",
    "-1.13,30.42;-1.70,30.82;-2.29,30.76;-2.41,30.47;-2.41,30.47;-2.35,29.94;-2.92,29.63;-2.84,29.02;-2.29,29.12;-2.22,29.25;-1.62,29.29;-1.34,29.58;-1.44,29.82;-1.13,30.42",
    "42.65,18.56;43.03,17.67;43.45,17.30;43.67,16.92;44.04,16.46;44.35,16.24;44.82,15.75;45.23,15.96;45.00,16.32;45.21,16.53;45.23,17.00;45.07,17.86;45.08,18.55;44.86,19.01;44.86,19.01;44.86,19.37;44.42,19.12;44.04,19.60;43.57,19.45;43.52,19.22;43.43,19.03;43.20,18.71;42.65,18.56",
    "42.32,22.38;42.00,22.88;41.34,22.95;41.30,22.76;41.13,22.60;41.15,22.06;40.93,21.67;40.84,21.02;41.09,20.61;41.52,20.46;41.86,20.59;41.86,20.59;41.85,20.72;42.05,20.76;42.21,21.35;42.25,21.58;42.30,21.92;42.32,22.38",
    "45.91,18.83;45.91,18.83;46.17,19.60;46.13,20.22;45.73,20.76;45.42,20.87;45.18,21.48;44.77,21.56;44.48,22.15;44.70,22.46;44.58,22.71;44.41,22.47;44.23,22.66;44.01,22.41;43.64,22.50;43.21,22.99;42.90,22.60;42.58,22.44;42.46,22.55;42.32,22.38;42.30,21.92;42.25,21.58;42.32,21.54;42.44,21.66;42.68,21.78;42.68,21.63;42.86,21.44;42.91,21.27;43.07,21.14;43.13,20.96;43.27,20.81;43.22,20.64;42.88,20.50;42.81,20.26;42.90,20.34;43.11,19.96;43.21,19.63;43.35,19.48;43.52,19.22;43.57,19.45;44.04,19.60;44.42,19.12;44.86,19.37;44.86,19.01;44.86,19.01;45.24,19.39;45.52,19.07;45.91,18.83",
    "42.59,20.07;42.50,19.80;42.69,19.74;42.20,19.30;41.88,19.37;41.96,19.16;42.28,18.88;42.48,18.45;42.65,18.56;43.20,18.71;43.43,19.03;43.52,19.22;43.35,19.48;43.21,19.63;43.11,19.96;42.90,20.34;42.81,20.26;42.59,20.07",
    "41.86,20.59;42.22,20.52;42.32,20.28;42.59,20.07;42.81,20.26;42.88,20.50;43.22,20.64;43.27,20.81;43.13,20.96;43.07,21.14;42.91,21.27;42.86,21.44;42.68,21.63;42.68,21.78;42.44,21.66;42.32,21.54;42.25,21.58;42.21,21.35;42.05,20.76;41.85,20.72;41.86,20.59",
    "10.76,-61.68;10.89,-61.11;10.86,-60.90;10.11,-60.94;10.00,-61.77;10.09,-61.95;10.37,-61.66;10.76,-61.68",
    "3.51,30.83;4.17,29.95;4.60,29.72;4.39,29.16;4.46,28.70;4.29,28.43;4.41,27.98;5.23,27.37;5.55,27.21;5.95,26.47;6.55,26.21;6.98,25.80;7.50,25.12;7.83,25.11;8.23,24.57;8.62,23.89;8.73,24.19;8.92,24.54;9.81,24.79;10.27,25.07;10.41,25.79;10.14,25.96;9.55,26.48;9.47,26.75;9.64,27.11;9.60,27.83;9.40,27.97;9.40,28.97;9.60,29.00;9.79,29.52;10.08,29.62;10.29,30.00;9.71,30.84;9.81,31.35;10.53,31.85;11.08,32.40;11.68,32.31;11.97,32.07;12.02,32.67;12.25,32.74;12.18,33.21;11.44,33.09;10.72,33.21;10.33,33.72;9.98,33.84;9.48,33.82;9.46,33.96;8.68,33.97;8.38,33.83;8.35,33.29;7.78,32.95;7.71,33.57;7.23,34.08;6.83,34.25;6.59,34.71;5.51,35.30;4.85,34.62;4.25,34.01;3.79,33.39;3.79,32.69;3.56,31.88;3.78,31.25;3.51,30.83",
    "13.52,92.98;13.35,93.08;12.92,92.95;12.55,92.92;12.15,92.82;11.68,92.76;11.52,92.62;12.12,92.68;12.88,92.75;13.42,92.85;13.52,92.98",
    "10.88,92.58;10.55,92.58;10.52,92.38;10.85,92.42;10.88,92.58",
    "9.25,92.82;9.12,92.82;9.12,92.70;9.25,92.72;9.25,92.82",
    "8.35,93.55;7.92,93.58;7.88,93.35;8.32,93.38;8.35,93.55",
    "7.38,93.88;6.75,93.92;6.75,93.68;7.35,93.68;7.38,93.88",
    "11.62,72.78;11.12,72.72;10.55,72.65;10.05,72.32;10.58,72.18;11.22,72.52;11.62,72.78",
    "6.95,73.22;5.82,73.42;4.22,73.55;2.15,73.38;0.28,73.18;-0.65,73.15;0.35,72.95;2.25,73.05;4.35,73.22;5.92,73.12;6.95,73.22",
    "-20.02,57.58;-20.48,57.75;-20.52,57.32;-20.05,57.42;-20.02,57.58",
    "-20.88,55.45;-21.38,55.82;-21.35,55.22;-20.92,55.22;-20.88,55.45",
    "-4.58,55.45;-4.82,55.55;-4.78,55.38;-4.58,55.45",
    "-11.38,43.35;-11.92,43.52;-12.38,44.52;-12.22,44.22;-11.42,43.22;-11.38,43.35",
    "12.68,54.48;12.45,54.52;12.32,53.35;12.62,53.38;12.68,54.48",
    "-5.75,39.32;-6.45,39.55;-6.42,39.18;-5.78,39.20;-5.75,39.32",
    "38.28,15.65;37.52,15.12;36.68,15.08;37.15,13.58;37.75,12.42;38.18,12.75;38.22,14.25;38.28,15.65",
    "41.25,9.25;40.42,9.78;39.15,9.58;38.88,8.85;39.22,8.38;40.58,8.18;41.15,8.25;41.25,9.25",
    "43.02,9.42;42.15,9.55;41.38,9.22;41.58,8.78;42.55,8.58;43.02,9.42",
    "35.58,23.58;35.38,24.48;35.22,26.28;34.98,25.85;34.95,24.75;35.22,23.52;35.58,23.58",
    "36.45,28.22;35.88,27.78;36.15,27.72;36.45,28.22",
    "36.08,14.22;35.82,14.58;35.80,14.32;36.08,14.22",
    "39.95,3.18;39.65,3.48;39.28,2.98;39.55,2.35;39.95,3.18",
    "40.08,4.05;39.82,4.32;39.92,3.82;40.08,4.05",
    "39.12,1.58;38.85,1.42;38.92,1.22;39.12,1.58",
    "28.58,-16.12;28.02,-16.72;28.32,-16.92;28.58,-16.12",
    "28.18,-15.38;27.75,-15.62;27.95,-15.85;28.18,-15.38",
    "29.22,-13.42;28.05,-14.35;28.42,-14.02;29.22,-13.42",
    "32.88,-16.68;32.62,-17.25;32.85,-17.22;32.88,-16.68",
    "37.92,-25.15;37.70,-25.88;38.55,-28.62;38.78,-27.05;37.92,-25.15",
    "17.22,-25.05;16.58,-24.25;15.15,-23.15;14.82,-24.52;16.05,-25.15;17.22,-25.05",
    "57.95,19.15;57.35,18.78;56.92,18.18;57.45,18.12;57.95,19.15",
    "57.35,17.12;56.20,16.40;56.65,16.38;57.35,17.12",
    "58.62,22.85;57.92,22.05;58.38,21.82;58.62,22.85",
    "62.42,-6.52;61.38,-6.68;62.12,-7.45;62.42,-6.52",
    "71.15,-8.05;70.82,-8.95;71.08,-8.52;71.15,-8.05",
    "81.25,58.50;80.15,62.50;79.85,50.20;80.85,47.50;81.25,58.50",
    "81.20,96.50;79.50,102.50;77.95,104.20;78.50,94.50;80.40,91.50;81.20,96.50",
    "71.55,-178.50;70.85,-178.90;70.92,178.60;71.52,179.20;71.55,-178.50",
    "54.42,142.65;51.85,143.25;49.25,144.25;46.62,143.45;45.95,141.95;48.55,142.05;51.55,141.95;53.85,142.25;54.42,142.65",
    "50.75,156.45;49.25,154.65;46.85,151.85;45.25,148.45;44.15,146.15;44.85,147.55;47.25,152.35;50.75,156.45",
    "33.55,126.85;33.22,126.55;33.30,126.18;33.52,126.45;33.55,126.85",
    "26.88,128.32;26.08,127.68;26.45,127.78;26.88,128.32",
    "28.52,129.72;27.75,128.88;28.25,129.15;28.52,129.72",
    "24.48,124.32;24.05,123.75;24.35,123.95;24.48,124.32",
    "1.46,103.85;1.32,104.05;1.24,103.65;1.42,103.68;1.46,103.85",
    "8.15,98.35;7.75,98.32;7.95,98.25;8.15,98.35",
    "-8.08,115.12;-8.45,115.72;-8.82,115.18;-8.42,114.45;-8.08,115.12",
    "-8.22,116.45;-8.88,116.58;-8.85,115.98;-8.28,116.05;-8.22,116.45",
    "-8.15,117.85;-8.62,119.05;-9.02,118.42;-9.05,116.75;-8.42,116.82;-8.15,117.85",
    "-9.35,119.85;-10.15,120.82;-10.32,120.15;-9.65,118.95;-9.35,119.85",
    "-8.22,122.95;-8.68,122.88;-8.85,121.15;-8.72,119.82;-8.35,120.45;-8.22,122.95",
    "2.18,127.92;1.45,128.68;0.25,128.75;-0.82,128.15;0.35,127.45;1.65,127.48;2.18,127.92",
    "-2.82,129.52;-3.45,130.85;-3.52,129.25;-3.32,127.92;-2.85,128.25;-2.82,129.52",
    "-3.08,126.85;-3.82,127.22;-3.78,126.05;-3.12,125.98;-3.08,126.85",
    "-1.55,105.85;-2.55,106.52;-3.08,106.45;-2.38,105.65;-1.68,105.18;-1.55,105.85",
    "-2.55,107.92;-3.22,108.25;-3.20,107.55;-2.58,107.58;-2.55,107.92",
    "11.42,119.55;9.95,118.92;8.42,117.22;8.92,117.52;10.45,118.88;11.42,119.55",
    "13.52,120.85;12.25,121.38;12.52,120.78;13.45,120.32;13.52,120.85",
    "11.88,122.15;11.42,123.18;10.45,122.18;11.15,121.95;11.88,122.15",
    "10.95,123.45;9.12,123.18;9.65,122.42;10.82,122.95;10.95,123.45",
    "11.28,124.05;9.42,123.35;10.25,123.48;11.28,124.05",
    "12.68,125.12;11.15,125.72;10.05,125.18;10.82,124.62;12.25,124.32;12.68,125.12",
    "20.25,-155.82;19.52,-154.82;18.92,-155.68;19.68,-156.05;20.25,-155.82",
    "21.02,-156.45;20.58,-156.02;20.78,-156.68;21.02,-156.45",
    "21.72,-157.95;21.25,-157.68;21.32,-158.12;21.58,-158.28;21.72,-157.95",
    "22.22,-159.35;21.88,-159.38;22.02,-159.78;22.22,-159.35",
    "-17.48,-149.42;-17.88,-149.12;-17.78,-149.62;-17.48,-149.42",
    "-13.45,-172.18;-13.85,-171.42;-14.05,-171.85;-13.68,-172.78;-13.45,-172.18",
    "-9.25,159.65;-9.88,160.82;-9.92,159.95;-9.38,159.58;-9.25,159.65",
    "57.92,-152.35;56.88,-154.15;57.38,-154.68;57.92,-152.35",
    "54.85,-163.15;53.45,-166.85;52.15,-174.15;52.95,-171.25;54.25,-165.15;54.85,-163.15",
    "54.15,-131.75;52.15,-131.12;52.95,-132.45;54.05,-133.05;54.15,-131.75",
    "-41.82,-73.52;-43.38,-73.68;-43.42,-74.38;-41.88,-74.05;-41.82,-73.52",
    "-0.02,-91.42;-0.98,-90.95;-1.02,-91.55;-0.25,-91.62;-0.02,-91.42",
    "47.05,-64.02;46.42,-62.02;45.98,-62.52;46.45,-63.92;47.05,-64.02",
    "47.05,-60.42;45.95,-59.82;45.55,-61.25;46.42,-61.18;47.05,-60.42",
    "49.95,-64.15;49.12,-61.72;49.08,-62.65;49.75,-64.52;49.95,-64.15",
    "48.88,-87.85;47.65,-84.85;46.52,-84.42;46.52,-88.25;46.78,-92.08;47.95,-89.85;48.88,-87.85",
    "46.02,-85.25;44.15,-86.35;41.68,-87.15;42.55,-87.85;44.85,-87.55;45.82,-86.55;46.02,-85.25",
    "46.18,-83.85;45.25,-81.35;43.15,-82.42;43.95,-83.85;45.65,-84.35;46.18,-83.85",
    "42.88,-78.95;42.15,-80.25;41.48,-82.85;41.95,-83.35;42.68,-81.15;42.88,-78.95",
    "44.22,-76.35;43.55,-76.22;43.25,-79.75;43.88,-78.15;44.22,-76.35",
    "66.85,-119.50;65.25,-118.85;65.15,-123.45;66.55,-121.85;66.85,-119.50",
    "62.85,-110.25;61.25,-113.55;60.95,-116.45;62.35,-115.25;62.85,-110.25",
    "53.85,-97.95;51.15,-96.55;50.42,-96.85;53.25,-99.15;53.85,-97.95",
    "55.75,109.65;53.55,108.25;51.65,104.25;51.95,105.65;54.15,109.15;55.75,109.65",
    "46.85,79.15;45.85,77.25;45.15,74.15;46.25,74.85;46.75,77.55;46.85,79.15",
    "0.42,33.25;-1.15,33.95;-2.68,32.85;-2.35,31.65;-0.45,31.75;0.42,33.25",
    "-3.38,29.35;-5.85,29.85;-8.75,31.15;-8.45,30.55;-5.65,29.25;-3.38,29.35",
    "-9.55,33.95;-11.95,34.85;-14.35,35.15;-13.85,34.45;-10.85,34.15;-9.55,33.95",
    "-15.25,-69.85;-16.55,-68.65;-16.25,-69.45;-15.25,-69.85",
    "61.65,30.85;60.15,32.45;60.05,31.05;61.25,30.15;61.65,30.85"
)

private data class SpherePolygon3D(
    val latLonPairs: List<Pair<Float, Float>>,
    val xyzPacked: FloatArray, // [x0, y0, z0, x1, y1, z1, ...] on unit sphere
    val normalX: Float,
    val normalY: Float,
    val normalZ: Float
)

private val WORLD_SPHERE_POLYGONS_3D: List<SpherePolygon3D> by lazy {
    WORLD_MAP_RAW_POLYGONS.mapNotNull { rawStr ->
        val rawPts = rawStr.split(';')
        if (rawPts.size < 3) return@mapNotNull null

        // Subdivide any segment longer than 2.4 degrees so long borders follow the true 3D spherical curvature!
        val latLonList = ArrayList<Pair<Float, Float>>(rawPts.size * 2)
        var prevLat = 0f
        var prevLon = 0f
        var hasPrev = false

        for (token in rawPts) {
            val comma = token.indexOf(',')
            if (comma <= 0) continue
            val lat = token.substring(0, comma).toFloatOrNull() ?: continue
            val lon = token.substring(comma + 1).toFloatOrNull() ?: continue
            if (hasPrev) {
                val dLat = lat - prevLat
                var dLon = lon - prevLon
                if (abs(dLon) < 180f) {
                    val distDeg = hypot(dLat, dLon)
                    if (distDeg > 2.4f) {
                        val steps = (distDeg / 2.0f).toInt().coerceIn(2, 16)
                        for (s in 1 until steps) {
                            val f = s.toFloat() / steps.toFloat()
                            latLonList.add((prevLat + dLat * f) to (prevLon + dLon * f))
                        }
                    }
                }
            }
            latLonList.add(lat to lon)
            prevLat = lat
            prevLon = lon
            hasPrev = true
        }

        val n = latLonList.size
        if (n < 3) return@mapNotNull null

        val xyz = FloatArray(n * 3)
        var sumX = 0f
        var sumY = 0f
        var sumZ = 0f
        for (i in 0 until n) {
            val (latDeg, lonDeg) = latLonList[i]
            val latRad = (latDeg * PI / 180.0).toFloat()
            val lonRad = (lonDeg * PI / 180.0).toFloat()
            val cosLat = cos(latRad)
            val x0 = cosLat * sin(lonRad)
            val y0 = -sin(latRad)
            val z0 = cosLat * cos(lonRad)
            val base = i * 3
            xyz[base] = x0
            xyz[base + 1] = y0
            xyz[base + 2] = z0
            sumX += x0
            sumY += y0
            sumZ += z0
        }
        val normLen = sqrt(sumX * sumX + sumY * sumY + sumZ * sumZ).coerceAtLeast(1e-4f)
        SpherePolygon3D(
            latLonPairs = latLonList,
            xyzPacked = xyz,
            normalX = sumX / normLen,
            normalY = sumY / normLen,
            normalZ = sumZ / normLen
        )
    }
}

private val WORLD_COUNTRY_BORDERS: List<List<Pair<Float, Float>>> by lazy {
    WORLD_SPHERE_POLYGONS_3D.map { it.latLonPairs }
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
            color = Color.White,
            fontSize = 8.sp,
            fontFamily = fontFamily,
            modifier = Modifier.width(88.dp)
        )

        Text(
            text = "−",
            color = Color(0xFFD4D4D4),
            fontSize = 10.sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .border(1.dp, Color(0xFF303030), RoundedCornerShape(4.dp))
                .background(Color(0xFF181818), RoundedCornerShape(4.dp))
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
                    .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(999.dp))
                    .background(Color(0xFF161616), RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .background(accentColor, RoundedCornerShape(999.dp))
                )
            }
        }

        Text(
            text = "+",
            color = Color(0xFFD4D4D4),
            fontSize = 10.sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .border(1.dp, Color(0xFF303030), RoundedCornerShape(4.dp))
                .background(Color(0xFF181818), RoundedCornerShape(4.dp))
                .clickable { currentOnChange((degreeValue + stepDeg).coerceIn(minDeg, maxDeg)) }
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun RetroFuturisticGlobeView(
    globeTheme: String,
    globeWireframeColor: String,
    axisTilt2dDeg: Float = -3f,
    axisRotXDeg: Float = 14f,
    axisRotYDeg: Float = -77f,
    interactive: Boolean = true,
    onTick: () -> Unit
) {
    val targetRollRad = ((axisTilt2dDeg.coerceIn(-180f, 180f)) * PI / 180.0).toFloat()
    val targetPitchRad = ((axisRotXDeg.coerceIn(-85f, 85f)) * PI / 180.0).toFloat()
    val baseYawOffsetRad = ((axisRotYDeg.coerceIn(-180f, 180f)) * PI / 180.0).toFloat()
    val currentTargetRoll by rememberUpdatedState(targetRollRad)
    val currentTargetPitch by rememberUpdatedState(targetPitchRad)
    val currentBaseYawOffset by rememberUpdatedState(baseYawOffsetRad)
    val baseSpinSpeed = 0.28f  // calm, precise autonomous Y-axis spin (radians/sec)

    var spinYawDelta by remember { mutableStateOf(0f) }
    var pitch by remember { mutableStateOf(targetPitchRad) }
    var roll by remember { mutableStateOf(targetRollRad) }

    // Immediately reflect 2D Axial Tilt (roll) and Polar Pitch adjustments from Edit Mode
    LaunchedEffect(targetRollRad) {
        roll = targetRollRad
    }
    LaunchedEffect(targetPitchRad) {
        pitch = targetPitchRad
    }

    var velYaw by remember { mutableStateOf(baseSpinSpeed) }
    var velPitch by remember { mutableStateOf(0f) }
    var velRoll by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Interactive 3D Globe Zoom (Double-tap to cycle 1.0x -> 1.75x -> 2.65x)
    var targetGlobeZoom by remember { mutableStateOf(1.0f) }
    val animatedGlobeZoom by animateFloatAsState(
        targetValue = targetGlobeZoom,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "globeZoomAnim"
    )

    // 120Hz physics & smooth spring posture recovery loop
    LaunchedEffect(Unit) {
        var lastFrameNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                val dt = if (lastFrameNanos == 0L) {
                    0.016f
                } else {
                    ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                }
                lastFrameNanos = frameNanos

                if (!isDragging) {
                    val zoomSlowdown = 1f / animatedGlobeZoom.coerceAtLeast(1f)
                    spinYawDelta += velYaw * dt * zoomSlowdown
                    pitch = (pitch + velPitch * dt).coerceIn(-1.48f, 1.48f)
                    roll = (roll + velRoll * dt).coerceIn(-3.1416f, 3.1416f)

                    val decay = exp(-3.1f * dt)
                    velYaw = baseSpinSpeed + (velYaw - baseSpinSpeed) * decay
                    velPitch *= decay
                    velRoll *= decay

                    val restoreFactor = (1f - exp(-2.6f * dt)).coerceIn(0f, 1f)
                    pitch += (currentTargetPitch - pitch) * restoreFactor
                    roll += (currentTargetRoll - roll) * restoreFactor
                }
            }
        }
    }

    val wfColor = remember(globeWireframeColor) {
        when (globeWireframeColor.uppercase(Locale.US)) {
            "#00FF66" -> Color(0xFF00FF66)
            "#00E5FF" -> Color(0xFF00E5FF)
            "#FFFFFF" -> Color(0xFFFFFFFF)
            "#D71921" -> Color(0xFFD71921)
            "#FFB000" -> Color(0xFFFFB000)
            "#3B82F6" -> Color(0xFF3B82F6)
            else -> Color(0xFFFF3B00)
        }
    }

    val isWireframe = globeTheme == "wireframe"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(interactive) {
                if (!interactive) return@pointerInput
                detectTapGestures(
                    onDoubleTap = {
                        onTick()
                        targetGlobeZoom = when {
                            targetGlobeZoom < 1.35f -> 1.75f
                            targetGlobeZoom < 2.2f -> 2.65f
                            else -> 1.0f
                        }
                    }
                )
            }
            .pointerInput(interactive) {
                if (!interactive) return@pointerInput
                var lastHapticStep = 0
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var prevPos = down.position
                    var prevTime = down.uptimeMillis
                    var movedEnough = false

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            val curPos = change.position
                            val curTime = change.uptimeMillis
                            val dx = curPos.x - prevPos.x
                            val dy = curPos.y - prevPos.y
                            if (!movedEnough && hypot(dx, dy) > 2.5f) {
                                movedEnough = true
                                isDragging = true
                                velYaw = 0f
                                velPitch = 0f
                                velRoll = 0f
                            }
                            if (movedEnough) {
                                change.consume()
                                val dtMs = (curTime - prevTime).coerceAtLeast(1L)
                                val dtSec = dtMs / 1000f

                                val sens = 0.0095f / animatedGlobeZoom.coerceAtLeast(1f)
                                spinYawDelta += dx * sens
                                pitch = (pitch - dy * sens).coerceIn(-1.45f, 1.45f)
                                roll = (roll + (dx * dy) * 0.00014f).coerceIn(-0.85f, 0.85f)

                                val instVelYaw = (dx * sens) / dtSec
                                val instVelPitch = (-dy * sens) / dtSec
                                val instVelRoll = ((dx * dy) * 0.00014f) / dtSec
                                velYaw = (velYaw * 0.35f + instVelYaw * 0.65f).coerceIn(-14f, 14f)
                                velPitch = (velPitch * 0.35f + instVelPitch * 0.65f).coerceIn(-12f, 12f)
                                velRoll = (velRoll * 0.35f + instVelRoll * 0.65f).coerceIn(-8f, 8f)

                                val step = (((currentBaseYawOffset + spinYawDelta) * 7f).toInt())
                                if (step != lastHapticStep) {
                                    lastHapticStep = step
                                    onTick()
                                }
                                prevPos = curPos
                                prevTime = curTime
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    isDragging = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val baseRadius = (min(w, h) * 0.492f).coerceAtLeast(36f)
            val radius = baseRadius * animatedGlobeZoom

            val effectiveYaw = currentBaseYawOffset + spinYawDelta
            val cosY = cos(effectiveYaw)
            val sinY = sin(effectiveYaw)
            val cosP = cos(pitch)
            val sinP = sin(pitch)
            val cosR = cos(roll)
            val sinR = sin(roll)

            // Fast 3D matrix rotation for unit-sphere vector (x0, y0, z0)
            fun rotateUnitVec(x0: Float, y0: Float, z0: Float): Triple<Float, Float, Float> {
                val x1 = x0 * cosY + z0 * sinY
                val z1 = -x0 * sinY + z0 * cosY
                val y2 = y0 * cosP - z1 * sinP
                val z2 = y0 * sinP + z1 * cosP
                val x3 = x1 * cosR - y2 * sinR
                val y3 = x1 * sinR + y2 * cosR
                return Triple(x3, y3, z2)
            }

            fun projectLatLon(latDeg: Float, lonDeg: Float): Triple<Float, Float, Float> {
                val lat = (latDeg * PI / 180.0).toFloat()
                val lon = (lonDeg * PI / 180.0).toFloat()
                val cosLat = cos(lat)
                val (x3, y3, z3) = rotateUnitVec(
                    cosLat * sin(lon),
                    -sin(lat),
                    cosLat * cos(lon)
                )
                return Triple(cx + x3 * radius, cy + y3 * radius, z3)
            }

            // 1. 3D Ocean Sphere with Bathymetric Depth Gradient & Specular Solar Highlight
            if (!isWireframe) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1085BE).copy(alpha = 0.62f),
                            Color(0xFF07507D).copy(alpha = 0.74f),
                            Color(0xFF032844).copy(alpha = 0.88f),
                            Color(0xFF011324).copy(alpha = 0.95f)
                        ),
                        center = Offset(cx - radius * 0.24f, cy - radius * 0.24f),
                        radius = radius * 1.28f
                    ),
                    radius = radius,
                    center = Offset(cx, cy)
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            wfColor.copy(alpha = 0.08f),
                            wfColor.copy(alpha = 0.03f),
                            Color(0xFF040404).copy(alpha = 0.85f)
                        ),
                        center = Offset(cx - radius * 0.18f, cy - radius * 0.18f),
                        radius = radius * 1.15f
                    ),
                    radius = radius,
                    center = Offset(cx, cy)
                )
            }

            // 2. Back-Hemisphere Geodesic Graticule Grid (z < 0)
            val latLevels = listOf(-75f, -60f, -45f, -30f, -15f, 0f, 15f, 30f, 45f, 60f, 75f)
            val backGridColor = if (isWireframe) wfColor.copy(alpha = 0.09f) else Color(0xFF1E96C8).copy(alpha = 0.12f)
            for (latDeg in latLevels) {
                var prevPt: Triple<Float, Float, Float>? = null
                for (lonStep in 0..360 step 12) {
                    val curPt = projectLatLon(latDeg, lonStep.toFloat())
                    if (prevPt != null) {
                        val zAvg = (prevPt.third + curPt.third) * 0.5f
                        if (zAvg < 0f) {
                            drawLine(
                                color = backGridColor,
                                start = Offset(prevPt.first, prevPt.second),
                                end = Offset(curPt.first, curPt.second),
                                strokeWidth = 0.55.dp.toPx()
                            )
                        }
                    }
                    prevPt = curPt
                }
            }
            for (lonDeg in 0 until 360 step 15) {
                var prevPt: Triple<Float, Float, Float>? = null
                for (latStep in -75..75 step 12) {
                    val curPt = projectLatLon(latStep.toFloat(), lonDeg.toFloat())
                    if (prevPt != null) {
                        val zAvg = (prevPt.third + curPt.third) * 0.5f
                        if (zAvg < 0f) {
                            drawLine(
                                color = backGridColor,
                                start = Offset(prevPt.first, prevPt.second),
                                end = Offset(curPt.first, curPt.second),
                                strokeWidth = 0.55.dp.toPx()
                            )
                        }
                    }
                    prevPt = curPt
                }
            }

            // 3. Back-Hemisphere Ghost Country Outlines (z < 0 for 3D Holographic Depth)
            val backBorderColor = if (isWireframe) wfColor.copy(alpha = 0.12f) else Color(0xFF18B868).copy(alpha = 0.13f)
            for (poly3d in WORLD_SPHERE_POLYGONS_3D) {
                // Quick back-hemisphere check using rotated polygon normal
                val (_, _, nz) = rotateUnitVec(poly3d.normalX, poly3d.normalY, poly3d.normalZ)
                if (nz > 0.45f) continue
                val xyz = poly3d.xyzPacked
                val count = xyz.size / 3
                var prevX = 0f
                var prevY = 0f
                var prevZ = 1f
                for (i in 0 until count) {
                    val base = i * 3
                    val (rx, ry, rz) = rotateUnitVec(xyz[base], xyz[base + 1], xyz[base + 2])
                    val sx = cx + rx * radius
                    val sy = cy + ry * radius
                    if (i > 0 && prevZ < 0f && rz < 0f) {
                        drawLine(
                            color = backBorderColor,
                            start = Offset(prevX, prevY),
                            end = Offset(sx, sy),
                            strokeWidth = 0.55.dp.toPx()
                        )
                    }
                    prevX = sx
                    prevY = sy
                    prevZ = rz
                }
            }

            // 4. Front-Hemisphere Geodesic Graticule Grid (z >= 0)
            for (latDeg in latLevels) {
                val isEquator = latDeg == 0f
                var prevPt: Triple<Float, Float, Float>? = null
                for (lonStep in 0..360 step 6) {
                    val curPt = projectLatLon(latDeg, lonStep.toFloat())
                    if (prevPt != null) {
                        val zAvg = (prevPt.third + curPt.third) * 0.5f
                        if (zAvg >= 0f) {
                            val gridAlpha = if (isEquator) (0.32f + 0.25f * zAvg) else (0.14f + 0.20f * zAvg)
                            val gridCol = if (isWireframe) {
                                wfColor.copy(alpha = gridAlpha.coerceIn(0f, 1f))
                            } else {
                                Color(0xFF28B8E8).copy(alpha = gridAlpha.coerceIn(0f, 1f))
                            }
                            drawLine(
                                color = gridCol,
                                start = Offset(prevPt.first, prevPt.second),
                                end = Offset(curPt.first, curPt.second),
                                strokeWidth = if (isEquator) 0.95.dp.toPx() else 0.62.dp.toPx()
                            )
                        }
                    }
                    prevPt = curPt
                }
            }
            for (lonDeg in 0 until 360 step 15) {
                var prevPt: Triple<Float, Float, Float>? = null
                for (latStep in -75..75 step 6) {
                    val curPt = projectLatLon(latStep.toFloat(), lonDeg.toFloat())
                    if (prevPt != null) {
                        val zAvg = (prevPt.third + curPt.third) * 0.5f
                        if (zAvg >= 0f) {
                            val gridAlpha = 0.14f + 0.20f * zAvg
                            val gridCol = if (isWireframe) {
                                wfColor.copy(alpha = gridAlpha.coerceIn(0f, 1f))
                            } else {
                                Color(0xFF28B8E8).copy(alpha = gridAlpha.coerceIn(0f, 1f))
                            }
                            drawLine(
                                color = gridCol,
                                start = Offset(prevPt.first, prevPt.second),
                                end = Offset(curPt.first, curPt.second),
                                strokeWidth = 0.62.dp.toPx()
                            )
                        }
                    }
                    prevPt = curPt
                }
            }

            // 5. Front-Hemisphere Ultra-Accurate 3D Country Mesh (Exact Horizon Clipping + 3D Lambertian Shading)
            // Directional light source from upper-left front: L = (-0.38, -0.44, 0.81)
            val lightX = -0.38f
            val lightY = -0.44f
            val lightZ = 0.81f

            for (poly3d in WORLD_SPHERE_POLYGONS_3D) {
                val (rnx, rny, rnz) = rotateUnitVec(poly3d.normalX, poly3d.normalY, poly3d.normalZ)
                // Cull polygons completely on the far back side of the sphere
                if (rnz < -0.68f) continue

                val xyz = poly3d.xyzPacked
                val n = xyz.size / 3
                var anyFront = false

                // Rotate vertices & clip against the 3D horizon plane (z = 0)
                val rxArr = FloatArray(n)
                val ryArr = FloatArray(n)
                val rzArr = FloatArray(n)
                for (i in 0 until n) {
                    val base = i * 3
                    val (rx, ry, rz) = rotateUnitVec(xyz[base], xyz[base + 1], xyz[base + 2])
                    rxArr[i] = rx
                    ryArr[i] = ry
                    rzArr[i] = rz
                    if (rz >= -0.02f) anyFront = true
                }
                if (!anyFront) continue

                // 3D Lambertian illumination factor from surface normal
                val lambert = (rnx * lightX + rny * lightY + rnz * lightZ).coerceIn(0f, 1f)

                if (!isWireframe) {
                    val fillPath = Path()
                    var started = false
                    for (i in 0 until n) {
                        val rx = rxArr[i]
                        val ry = ryArr[i]
                        val rz = rzArr[i]
                        val px: Float
                        val py: Float
                        if (rz >= 0f) {
                            px = cx + rx * radius
                            py = cy + ry * radius
                        } else {
                            // Project horizon-crossing points cleanly onto the 3D sphere's circular limb
                            val normXY = hypot(rx, ry).coerceAtLeast(1e-4f)
                            px = cx + (rx / normXY) * radius * 0.996f
                            py = cy + (ry / normXY) * radius * 0.996f
                        }
                        if (!started) {
                            fillPath.moveTo(px, py)
                            started = true
                        } else {
                            fillPath.lineTo(px, py)
                        }
                    }
                    if (started) {
                        fillPath.close()
                        val landAlpha = (0.38f + 0.24f * lambert).coerceIn(0.32f, 0.64f)
                        drawPath(
                            path = fillPath,
                            color = Color(0xFF0E9F54).copy(alpha = landAlpha)
                        )
                    }
                } else {
                    // In wireframe mode, give front continents a subtle 3D shaded vector fill
                    val fillPath = Path()
                    var started = false
                    for (i in 0 until n) {
                        val rx = rxArr[i]
                        val ry = ryArr[i]
                        val rz = rzArr[i]
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
                            fillPath.moveTo(px, py)
                            started = true
                        } else {
                            fillPath.lineTo(px, py)
                        }
                    }
                    if (started) {
                        fillPath.close()
                        drawPath(
                            path = fillPath,
                            color = wfColor.copy(alpha = (0.06f + 0.08f * lambert).coerceIn(0.04f, 0.15f))
                        )
                    }
                }

                // Draw ultra-crisp, high-precision sovereign borders & coastlines on the front hemisphere
                // with exact z = 0 horizon interpolation!
                val borderPath = Path()
                var penDown = false
                for (i in 0 until n) {
                    val rx1 = rxArr[i]
                    val ry1 = ryArr[i]
                    val rz1 = rzArr[i]
                    if (rz1 >= 0f) {
                        val sx = cx + rx1 * radius
                        val sy = cy + ry1 * radius
                        if (!penDown) {
                            borderPath.moveTo(sx, sy)
                            penDown = true
                        } else {
                            borderPath.lineTo(sx, sy)
                        }
                    } else if (i > 0 && rzArr[i - 1] >= 0f) {
                        // Interpolate exact horizon crossing point where z = 0
                        val prevZ = rzArr[i - 1]
                        val tCross = (prevZ / (prevZ - rz1)).coerceIn(0f, 1f)
                        val hx = rxArr[i - 1] + (rx1 - rxArr[i - 1]) * tCross
                        val hy = ryArr[i - 1] + (ry1 - ryArr[i - 1]) * tCross
                        borderPath.lineTo(cx + hx * radius, cy + hy * radius)
                        penDown = false
                    } else {
                        penDown = false
                    }
                }

                val depthVis = rnz.coerceIn(0.15f, 1f)
                val edgeAlpha = (0.50f + 0.50f * depthVis).coerceIn(0.35f, 1f)
                if (!isWireframe) {
                    drawPath(
                        path = borderPath,
                        color = Color(0xFF1EFF6E).copy(alpha = edgeAlpha * 0.42f),
                        style = Stroke(width = 1.85.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        path = borderPath,
                        color = Color(0xFFDCFFD4).copy(alpha = edgeAlpha * 0.96f),
                        style = Stroke(width = 0.92.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                } else {
                    drawPath(
                        path = borderPath,
                        color = wfColor.copy(alpha = edgeAlpha * 0.96f),
                        style = Stroke(width = 1.15.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            // 6. 3D Atmospheric Fresnel Rim Halo & Outer Planetary Horizon Ring
            val rimGlowColor = if (isWireframe) wfColor.copy(alpha = 0.28f) else Color(0xFF28E098).copy(alpha = 0.32f)
            val rimCoreColor = if (isWireframe) wfColor.copy(alpha = 0.88f) else Color(0xFF70FFBA).copy(alpha = 0.88f)
            drawCircle(
                color = rimGlowColor,
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 3.2.dp.toPx())
            )
            drawCircle(
                color = rimCoreColor,
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.15.dp.toPx())
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerView(
    apps: List<AppEntry>,
    lomoFontFamily: FontFamily,
    selectedFont: FontFamily,
    uppercaseHome: Boolean,
    drawerTextSizeSp: Int,
    accentColor: Color,
    waveScrubMode: String,
    drawerWaveIntensity: Float,
    screenHeightPx: Float,
    getDrawerProgress: () -> Float,
    searchResetToken: Int,
    focusSearchOnOpen: Boolean,
    onConsumedSearchFocus: () -> Unit,
    onDragDrawerDelta: (Float) -> Unit,
    onSettleDrawer: (Float) -> Unit,
    onLaunchApp: (String) -> Unit,
    onLongPressApp: (AppEntry) -> Unit,
    onClose: () -> Unit,
    onWaveTick: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val isDrawerOpenNow by remember { derivedStateOf { getDrawerProgress() > 0.5f } }

    // Automatically clear search text, reset scroll position, and dismiss keyboard
    // whenever an app is opened or the app drawer is closed!
    LaunchedEffect(isDrawerOpenNow, searchResetToken) {
        if (!isDrawerOpenNow || searchResetToken > 0) {
            if (!isDrawerOpenNow) {
                if (query.isNotEmpty()) query = ""
                try {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                } catch (_: Exception) {
                }
                if (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0) {
                    listState.scrollToItem(0, 0)
                }
            }
        }
    }

    LaunchedEffect(searchResetToken) {
        if (searchResetToken > 0) {
            if (query.isNotEmpty()) query = ""
            try {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(focusSearchOnOpen, isDrawerOpenNow) {
        if (focusSearchOnOpen && isDrawerOpenNow) {
            delay(160)
            try {
                searchFocusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {
            }
            onConsumedSearchFocus()
        }
    }

    val filteredApps = remember(apps, query) {
        apps
            .filter { !it.isHidden && (query.isBlank() || it.label.contains(query, ignoreCase = true)) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    val currentOnDragDelta by rememberUpdatedState(onDragDrawerDelta)
    val currentOnSettle by rememberUpdatedState(onSettleDrawer)

    val drawerNestedScroll = remember {
        var isPullingDrawerClosed = false
        var totalPullDownPx = 0f
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag && isPullingDrawerClosed && available.y < 0f) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.98f))
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .nestedScroll(drawerNestedScroll)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Input Bar (Top grab handle removed for a cleaner minimal header)
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
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = lomoFontFamily
                    ),
                    cursorBrush = SolidColor(accentColor),
                    enabled = isDrawerOpenNow,
                    modifier = Modifier
                        .weight(1f)
                        .focusProperties { canFocus = isDrawerOpenNow }
                        .focusRequester(searchFocusRequester),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search apps...",
                                color = Color(0xFF525252),
                                fontSize = 12.sp,
                                fontFamily = lomoFontFamily
                            )
                        }
                        inner()
                    }
                )
                if (query.isNotEmpty()) {
                    Text(
                        text = "×",
                        color = Color(0xFF8A8A8A),
                        fontSize = 15.sp,
                        fontFamily = lomoFontFamily,
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
                // Ultra-clean 120Hz LazyColumn:
                // Zero app name highlighting & zero hover animation on app rows!
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
                                        color = accentColor,
                                        fontSize = headerFontSp,
                                        fontFamily = lomoFontFamily,
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
                                        color = Color(0xFFF5F5F5),
                                        fontSize = drawerTextSizeSp.sp,
                                        fontFamily = selectedFont,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (app.isPinned) {
                                        Text(
                                            text = "★",
                                            color = accentColor,
                                            fontSize = starFontSp,
                                            fontFamily = lomoFontFamily
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

                // Isolated A-Z Scrubber Rail Composable:
                // Only the A-Z rail letters curve horizontally; app rows never highlight or hover-animate!
                AlphabetScrubberRail(
                    accentColor = accentColor,
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

    // Smooth vertical finger-tracking offset for the Voxel Alphabet Box
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

                Text(
                    text = ch.toString(),
                    color = if (isCurrent) accentColor else if (animLetterFactor > 0.4f) Color.White else Color(0xFF555555),
                    fontSize = if (isCurrent) 12.sp else 9.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
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

        // Minimalist Black & White Stepped Alphabet Indicator with Razor-Thin 1.dp Accent Stroke
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
                    val strokePx = 1.dp.toPx()
                    val half = strokePx * 0.5f

                    // Stepped-corner minimal box path: pure #000000 black fill + razor-thin 1.dp accentColor stroke
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
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// MODULAR OLED WIDGETS MANAGER (Relocate, Resize, Add/Remove & Full Widget Customization)
// All widget customization controls shifted here from Launcher Settings!
// ============================================================================
@Composable
fun WidgetsManagerView(
    activeWidgets: List<String>,
    widgetSizes: Map<String, String>,
    accentColor: Color,
    lomoFontFamily: FontFamily,
    use24Hour: Boolean,
    quickNote: String,
    globeTheme: String,
    globeWireframeColor: String,
    onToggle24h: () -> Unit,
    onChangeQuickNote: (String) -> Unit,
    onChangeGlobeTheme: (String) -> Unit,
    onChangeGlobeWireframeColor: (String) -> Unit,
    onUpdateWidgets: (List<String>) -> Unit,
    onCycleWidgetSize: (String) -> Unit,
    onSetWidgetSize: (String, String) -> Unit,
    onMoveWidget: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    val allWidgets = listOf(
        "clock" to "Dot-Matrix Clock, Date & Battery Bar",
        "week_strip" to "7-Day Strip & Month Days Block Bar",
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
            Column {
                Text(
                    text = "WIDGET MANAGER",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "100% offline widgets · Add/remove & configure (resize, font & order in Edit Mode)",
                    color = Color(0xFF737373),
                    fontSize = 10.sp,
                    fontFamily = lomoFontFamily
                )
            }
            Text(
                text = "DONE",
                color = Color.Black,
                fontSize = 11.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .clickable { onBack() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(allWidgets) { (id, title) ->
                val enabled = activeWidgets.contains(id)
                val currentSize = widgetSizes[id] ?: "standard"
                val idx = activeWidgets.indexOf(id)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (enabled) Color(0xFF333333) else Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                        .background(if (enabled) Color(0xFF0A0A0A) else Color(0xFF040404), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            color = if (enabled) Color.White else Color(0xFF737373),
                            fontSize = 12.sp,
                            fontFamily = lomoFontFamily,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (enabled) "ACTIVE [×]" else "+ ADD",
                            color = if (enabled) accentColor else Color(0xFF8A8A8A),
                            fontSize = 11.sp,
                            fontFamily = lomoFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (enabled) accentColor.copy(alpha = 0.5f) else Color(0xFF262626),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    val next = if (enabled) activeWidgets - id else activeWidgets + id
                                    onUpdateWidgets(next)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (enabled) {

                        if (id == "clock" || id == "retro_flip_clock" || id == "analog_chrono") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CLOCK FORMAT",
                                    color = Color(0xFF8A8A8A),
                                    fontSize = 9.sp,
                                    fontFamily = lomoFontFamily
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(false to "12H", true to "24H").forEach { (is24, lbl) ->
                                        val sel = use24Hour == is24
                                        Text(
                                            text = lbl,
                                            color = if (sel) Color.Black else Color.White,
                                            fontSize = 9.sp,
                                            fontFamily = lomoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(if (sel) Color.White else Color(0xFF191919), RoundedCornerShape(4.dp))
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
                                color = Color(0xFF8A8A8A),
                                fontSize = 9.sp,
                                fontFamily = lomoFontFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    "retro_holo" to "CRT HOLOGRAM (GRN/BLU)",
                                    "wireframe" to "MONOCHROME VECTOR"
                                ).forEach { (tId, tLabel) ->
                                    val isSel = globeTheme == tId
                                    Text(
                                        text = tLabel,
                                        color = if (isSel) Color.Black else Color.White,
                                        fontSize = 9.sp,
                                        fontFamily = lomoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(if (isSel) Color.White else Color(0xFF191919), RoundedCornerShape(4.dp))
                                            .clickable { onChangeGlobeTheme(tId) }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            if (globeTheme == "wireframe") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "PHOSPHOR / VECTOR COLOR",
                                    color = Color(0xFF8A8A8A),
                                    fontSize = 9.sp,
                                    fontFamily = lomoFontFamily
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "#FF3B00" to "CMF",
                                        "#00FF66" to "GRN",
                                        "#00E5FF" to "CYN",
                                        "#FFFFFF" to "WHT",
                                        "#D71921" to "RED",
                                        "#FFB000" to "AMB",
                                        "#3B82F6" to "BLU"
                                    ).forEach { (hex, name) ->
                                        val selected = globeWireframeColor.equals(hex, ignoreCase = true)
                                        val swatchColor = when (hex) {
                                            "#00FF66" -> Color(0xFF00FF66)
                                            "#00E5FF" -> Color(0xFF00E5FF)
                                            "#FFFFFF" -> Color(0xFFFFFFFF)
                                            "#D71921" -> Color(0xFFD71921)
                                            "#FFB000" -> Color(0xFFFFB000)
                                            "#3B82F6" -> Color(0xFF3B82F6)
                                            else -> Color(0xFFFF3B00)
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .border(
                                                    width = if (selected) 1.5.dp else 1.dp,
                                                    color = if (selected) Color.White else Color(0xFF262626),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .background(Color(0xFF111111), RoundedCornerShape(6.dp))
                                                .clickable { onChangeGlobeWireframeColor(hex) }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(swatchColor, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = name,
                                                color = if (selected) Color.White else Color(0xFF737373),
                                                fontSize = 8.sp,
                                                fontFamily = lomoFontFamily
                                            )
                                        }
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
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = lomoFontFamily
                                ),
                                cursorBrush = SolidColor(accentColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(6.dp))
                                    .background(Color(0xFF111111), RoundedCornerShape(6.dp))
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
// PREMIUM LAUNCHER SETTINGS WITH COLLAPSIBLE ACCORDION MENUS (v3.1.0-cmf)
// - Section 02: Home App Font Size + App Drawer App Name Scale (Slider + Presets)
// - Section 03: "Wallpapers and Settings" with Universal Wallpaper Dimmer (0%-90% for
//   Live, Static & Third-Party/User Wallpapers) + 3 Separate Collapsible Sub-Sections:
//   (1) Live Wallpapers, (2) Static Wallpapers, (3) User Wallpapers
// - Section 04: App Drawer Traverse Curved Horizontal Wave Slider (0%-200%)
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
    lomoFontFamily: FontFamily,
    launcherFont: FontFamily,
    onChangeFont: (String) -> Unit,
    onChangeAlign: (String) -> Unit,
    onChangeAccent: (String) -> Unit,
    onChangeTextSize: (Int) -> Unit,
    onChangeDrawerTextSize: (Int) -> Unit,
    onToggle24h: () -> Unit,
    onToggleAndroidBar: () -> Unit,
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

    // Separate collapsible sub-sections inside "Wallpapers and Settings"
    var expandedLiveWallpapers by remember { mutableStateOf(false) }
    var expandedStaticWallpapers by remember { mutableStateOf(false) }
    var expandedUserWallpapers by remember { mutableStateOf(false) }

    val accentColor = resolveLauncherAccentColor(accentHex)

    // 8 Minimal, Full-Screen AMOLED Live Wallpapers (including Glowing & Halftone Wave Live Wallpapers)
    val liveWallpaperOptions = listOf(
        Triple("live_starfield_3d", "3D STARFIELD", "1,200-Star 3D Night Sky"),
        Triple("live_constellation", "ORBITAL NODES", "Full-Screen Drifting Stars"),
        Triple("live_embers", "DEEP EMBERS", "Full-Screen Rising Dust"),
        Triple("live_starlight", "STARLIGHT BREATH", "Calm Twinkling Field"),
        Triple("live_kepler", "KEPLER ORBITS", "Planetary Ellipses"),
        Triple("live_quantum", "MICRO LATTICE", "Breathing Dot Matrix"),
        Triple("live_lissajous", "PHASE THREAD", "Slow Harmonic Curve"),
        Triple("live_glow_horizon", "AURORA GLOW FADE", "Breathing Ambient Fade"),
        Triple("live_halftone_pulse", "HALFTONE WAVE", "Glowing Halftone Pulse")
    )

    // 36 Full-Screen Static AMOLED Wallpapers (Original 24 + 12 New Halftone, Glowing & Fade Wallpapers)
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
        Triple("static_glyph_arcs", "GLYPH STRIPS", "Segmented Light Arcs"),
        Triple("static_polar", "POLAR RADAR", "Full-Screen Range Rings"),
        Triple("static_diagonal_hatch", "CARBON WEAVE", "Fine 45° Micro Hatch"),
        Triple("static_golden_spiral", "GOLDEN RATIO", "Fibonacci Subdivision"),
        Triple("static_sine_interference", "MOIRÉ WAVES", "Full-Screen Sine Phase"),
        Triple("static_corner_frame", "VIEWFINDER", "Rule-of-Thirds & Frame"),
        Triple("static_hex_honeycomb", "HEX MATRIX", "Full-Screen Hex Mesh"),
        Triple("static_constellation_fixed", "DEEP SKY ATLAS", "Full-Screen Star Mesh"),
        Triple("static_vertical_flutes", "MONOLITH RIBS", "Full-Height Fine Rules"),
        Triple("static_zen_enso", "ZEN CIRCLE", "Tapered Ring & Aura"),
        Triple("static_micro_plus", "SWISS PLUS GRID", "Full-Screen + Markers")
    )

    val normBgIntensity = (settingsBgIntensity.coerceIn(0, 100) / 100f)
    val settingsRootBgColor = when (settingsBgMode) {
        "transparent" -> Color.Black.copy(alpha = (1f - normBgIntensity * 0.92f).coerceIn(0.06f, 0.96f))
        "blurry" -> Color(0xFF050505).copy(alpha = (0.76f - normBgIntensity * 0.56f).coerceIn(0.18f, 0.82f))
        else -> Color.Black
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(settingsRootBgColor)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAUNCHER SETTINGS",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = launcherFont,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "DONE",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontFamily = launcherFont,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .clickable { onBack() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        item {
            CollapsibleSettingsSection(
                indexTag = "01",
                title = "System & Home Integration",
                subtitle = "Default launcher role & Android status bar",
                badge = if (hideAndroidBar) "IMMERSIVE" else "STANDARD",
                expanded = expandedSystem,
                accentColor = accentColor,
                lomoFontFamily = launcherFont,
                onToggle = { expandedSystem = !expandedSystem }
            ) {
                SettingCard(
                    title = "Default Home Launcher",
                    subtitle = "Set N-TEXT as your primary Android 14 Home App",
                    actionLabel = "CHOOSE HOME",
                    lomoFontFamily = launcherFont,
                    onClick = onRequestDefaultHomeRole
                )
                SettingCard(
                    title = "Hide Android Status Bar",
                    subtitle = "Swipe down on Home Screen anytime to view notifications",
                    actionLabel = if (hideAndroidBar) "HIDDEN (ON)" else "SHOWN (OFF)",
                    lomoFontFamily = launcherFont,
                    onClick = onToggleAndroidBar
                )

                // Settings Menu Background Style (Opaque, Transparent, Blurry) + Intensity Slider
                SettingsBackdropControlCard(
                    settingsBgMode = settingsBgMode,
                    settingsBgIntensity = settingsBgIntensity,
                    accentColor = accentColor,
                    lomoFontFamily = launcherFont,
                    onChangeMode = onChangeSettingsBgMode,
                    onChangeIntensity = onChangeSettingsBgIntensity
                )
            }
        }

        item {
            val activeFontName = PrebuiltFont.fromId(fontId).displayName.substringBefore(" (")
            CollapsibleSettingsSection(
                indexTag = "02",
                title = "Typography & Layout",
                subtitle = "Typeface, Home app text scale, clock format & alignment",
                badge = "${textSizeSp}SP · ${textAlignMode.uppercase(Locale.US)}",
                expanded = expandedTypography,
                accentColor = accentColor,
                lomoFontFamily = launcherFont,
                onToggle = { expandedTypography = !expandedTypography }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (expandedFontMenu) accentColor.copy(alpha = 0.45f) else Color(0xFF222222),
                            RoundedCornerShape(10.dp)
                        )
                        .background(Color(0xFF070707), RoundedCornerShape(10.dp))
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
                                text = "LAUNCHER FONT MENU (COLLAPSIBLE)",
                                color = Color(0xFF8A8A8A),
                                fontSize = 9.sp,
                                fontFamily = launcherFont
                            )
                            Text(
                                text = activeFontName,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (expandedFontMenu) "HIDE ▲" else "${PrebuiltFont.values().size} FONTS ▼",
                            color = accentColor,
                            fontSize = 9.sp,
                            fontFamily = launcherFont,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .border(1.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                .background(Color(0xFF121212), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
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
                                            if (selected) accentColor.copy(alpha = 0.6f) else Color(0xFF171717),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            if (selected) Color(0xFF121212) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onChangeFont(f.id) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = f.displayName,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontFamily = f.resolveFontFamily(lomoFontFamily)
                                        )
                                        Text(
                                            text = "TAG: ${f.shortLabel}" + if (f.uppercaseHome) " · ALL-CAPS" else "",
                                            color = Color(0xFF737373),
                                            fontSize = 8.sp,
                                            fontFamily = launcherFont
                                        )
                                    }
                                    Text(
                                        text = if (selected) "●" else "○",
                                        color = if (selected) accentColor else Color(0xFF525252),
                                        fontSize = 10.sp,
                                        fontFamily = launcherFont
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
                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
                        .background(Color(0xFF070707), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HOME SCREEN APP FONT SIZE (${textSizeSp} SP)",
                        color = Color(0xFF8A8A8A),
                        fontSize = 9.sp,
                        fontFamily = launcherFont
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(16, 18, 20, 22, 24).forEach { spVal ->
                            val sel = textSizeSp == spVal
                            Text(
                                text = "${spVal}SP",
                                color = if (sel) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (sel) Color.White else Color(0xFF161616), RoundedCornerShape(6.dp))
                                    .clickable { onChangeTextSize(spVal) }
                                    .padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                SettingCard(
                    title = "Clock Time Format",
                    subtitle = "Switch between 12-Hour (AM/PM) and 24-Hour time (tapping Clock opens Clock app)",
                    actionLabel = if (use24Hour) "24-HOUR" else "12-HOUR",
                    lomoFontFamily = launcherFont,
                    onClick = onToggle24h
                )

                SettingCard(
                    title = "Home Text Alignment",
                    subtitle = "Left, Center, or Right aligned apps",
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
            }
        }

        // ====================================================================
        // SECTION 03: "Wallpapers and Settings"
        // Includes Universal Wallpaper Dimmer (0%-90% for Live, Static & User/Third-Party)
        // + 3 Separate Collapsible Sections: Live Wallpapers, Static Wallpapers, User Wallpapers
        // ====================================================================
        item {
            CollapsibleSettingsSection(
                indexTag = "03",
                title = "Wallpapers and Settings",
                subtitle = "Wallpaper dimmer, live, static & user wallpapers, accent colors",
                badge = "DIM ${wallpaperDimPercent}%",
                expanded = expandedWallpaper,
                accentColor = accentColor,
                lomoFontFamily = launcherFont,
                onToggle = { expandedWallpaper = !expandedWallpaper }
            ) {
                // 0. UNIVERSAL WALLPAPER DIMMER (Works on Live, Static & User/Third-Party Wallpapers)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                        .background(Color(0xFF070707), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinimalistSliderRow(
                        label = "UNIVERSAL WALLPAPER DIMMER (LIVE / STATIC / 3RD-PARTY)",
                        valueText = "${wallpaperDimPercent}%",
                        fraction = (wallpaperDimPercent / 90f).coerceIn(0f, 1f),
                        accentColor = accentColor,
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
                                color = if (sel) Color.Black else Color(0xFFD4D4D4),
                                fontSize = 9.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (sel) accentColor else Color(0xFF151515), RoundedCornerShape(6.dp))
                                    .clickable { onChangeWallpaperDim(dimVal) }
                                    .padding(vertical = 5.dp)
                            )
                        }
                    }
                }

                // 1. COLLAPSIBLE SUB-SECTION: LIVE WALLPAPERS
                CollapsibleSubSection(
                    title = "Live Wallpapers",
                    subtitle = "8 minimal full-screen AMOLED live animations & tuning lab",
                    badge = if (wallpaperPreset.startsWith("live_")) "ACTIVE" else "${liveWallpaperOptions.size} STYLES",
                    expanded = expandedLiveWallpapers,
                    accentColor = accentColor,
                    lomoFontFamily = launcherFont,
                    onToggle = { expandedLiveWallpapers = !expandedLiveWallpapers }
                ) {
                    liveWallpaperOptions.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (id, title, desc) ->
                                val active = wallpaperPreset == id
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            if (active) accentColor else Color(0xFF242424),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            if (active) Color(0xFF161616) else Color(0xFF0C0C0C),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onChangeWallpaperPreset(id) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = title,
                                            color = if (active) Color.White else Color(0xFFD4D4D4),
                                            fontSize = 10.sp,
                                            fontFamily = launcherFont,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    if (active) accentColor else Color(0xFF383838),
                                                    CircleShape
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc,
                                        color = Color(0xFF737373),
                                        fontSize = 8.sp,
                                        fontFamily = launcherFont
                                    )
                                }
                            }
                        }
                    }

                    // Interactive Live Wallpaper Tuning Variables Lab
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF242424), RoundedCornerShape(8.dp))
                            .background(Color(0xFF0B0B0B), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE WALLPAPER TUNING VARIABLES",
                                color = accentColor,
                                fontSize = 9.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "RESET [1.0x]",
                                color = Color(0xFFA3A3A3),
                                fontSize = 8.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(4.dp))
                                    .background(Color(0xFF141414), RoundedCornerShape(4.dp))
                                    .clickable { onResetWpTuning() }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        WallpaperTuningRow(
                            label = "MOTION SPEED",
                            currentValue = wpSpeed,
                            options = listOf(0.25f to "0.25x", 0.5f to "0.5x", 1.0f to "1.0x", 1.5f to "1.5x", 2.2f to "2.2x"),
                            accentColor = accentColor,
                            lomoFontFamily = launcherFont,
                            onSelect = onChangeWpSpeed
                        )

                        WallpaperTuningRow(
                            label = "ELEMENT DENSITY",
                            currentValue = wpDensity,
                            options = listOf(0.5f to "0.5x", 0.75f to "0.75x", 1.0f to "1.0x", 1.35f to "1.35x", 1.75f to "1.75x"),
                            accentColor = accentColor,
                            lomoFontFamily = launcherFont,
                            onSelect = onChangeWpDensity
                        )

                        WallpaperTuningRow(
                            label = "GLOW / OPACITY",
                            currentValue = wpOpacity,
                            options = listOf(0.4f to "40%", 0.7f to "70%", 1.0f to "100%", 1.35f to "135%", 1.75f to "175%"),
                            accentColor = accentColor,
                            lomoFontFamily = launcherFont,
                            onSelect = onChangeWpOpacity
                        )

                        WallpaperTuningRow(
                            label = "SCALE / REACH",
                            currentValue = wpScale,
                            options = listOf(0.65f to "0.65x", 0.85f to "0.85x", 1.0f to "1.0x", 1.25f to "1.25x", 1.5f to "1.5x"),
                            accentColor = accentColor,
                            lomoFontFamily = launcherFont,
                            onSelect = onChangeWpScale
                        )
                    }
                }

                // 2. COLLAPSIBLE SUB-SECTION: STATIC WALLPAPERS
                CollapsibleSubSection(
                    title = "Static Wallpapers",
                    subtitle = "36 full-screen AMOLED backdrops (halftone, glowing, fade & geometric)",
                    badge = if (!wallpaperPreset.startsWith("live_") && wallpaperPreset != "system_wallpaper" && wallpaperPreset != "user_image") "ACTIVE" else "${staticWallpaperOptions.size} STYLES",
                    expanded = expandedStaticWallpapers,
                    accentColor = accentColor,
                    lomoFontFamily = launcherFont,
                    onToggle = { expandedStaticWallpapers = !expandedStaticWallpapers }
                ) {
                    staticWallpaperOptions.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (id, title, desc) ->
                                val active = wallpaperPreset == id
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            if (active) Color.White else Color(0xFF242424),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            if (active) Color(0xFF161616) else Color(0xFF0C0C0C),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onChangeWallpaperPreset(id) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = title,
                                            color = if (active) Color.White else Color(0xFFD4D4D4),
                                            fontSize = 10.sp,
                                            fontFamily = launcherFont,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (active) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(accentColor, CircleShape)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc,
                                        color = Color(0xFF737373),
                                        fontSize = 8.sp,
                                        fontFamily = launcherFont
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. COLLAPSIBLE SUB-SECTION: USER WALLPAPERS (Custom Gallery Photo & System / Third-Party Wallpapers)
                CollapsibleSubSection(
                    title = "User Wallpapers",
                    subtitle = "Custom gallery photos & third-party / system live or static wallpapers",
                    badge = if (wallpaperPreset == "system_wallpaper" || wallpaperPreset == "user_image") "ACTIVE" else "CUSTOM",
                    expanded = expandedUserWallpapers,
                    accentColor = accentColor,
                    lomoFontFamily = launcherFont,
                    onToggle = { expandedUserWallpapers = !expandedUserWallpapers }
                ) {
                    SettingCard(
                        title = "Import Custom Photo from Gallery",
                        subtitle = if (wallpaperPreset == "user_image") {
                            "ACTIVE · Full-screen custom image with ${wallpaperDimPercent}% dimmer"
                        } else if (hasCustomUserImage) {
                            "Pick a new image from device storage (dimmer supported)"
                        } else {
                            "Choose any image from your phone to use as a full-screen wallpaper"
                        },
                        actionLabel = if (wallpaperPreset == "user_image") "CHANGE PHOTO" else "PICK PHOTO",
                        lomoFontFamily = launcherFont,
                        onClick = onPickCustomWallpaperImage
                    )

                    if (hasCustomUserImage && wallpaperPreset != "user_image") {
                        SettingCard(
                            title = "Activate Saved Custom Photo",
                            subtitle = "Switch back to your previously imported custom wallpaper",
                            actionLabel = "USE SAVED",
                            lomoFontFamily = launcherFont,
                            onClick = { onChangeWallpaperPreset("user_image") }
                        )
                    }

                    SettingCard(
                        title = "System / Third-Party Wallpaper Picker",
                        subtitle = "Pick a wallpaper from Android or any 3rd-party wallpaper app",
                        actionLabel = "OPEN PICKER",
                        lomoFontFamily = launcherFont,
                        onClick = onOpenSystemWallpaperPicker
                    )

                    SettingCard(
                        title = "Use Active System / Third-Party Wallpaper",
                        subtitle = "Display your current system wallpaper behind the launcher with ${wallpaperDimPercent}% dimmer",
                        actionLabel = if (wallpaperPreset == "system_wallpaper") "ACTIVE (ON)" else "ENABLE",
                        lomoFontFamily = launcherFont,
                        onClick = { onChangeWallpaperPreset("system_wallpaper") }
                    )
                }

                // 4. Expanded 10-Swatch AMOLED Accent Color Picker
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
                        .background(Color(0xFF070707), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LAUNCHER ACCENT COLOR (10 PALETTES)",
                            color = Color(0xFF8A8A8A),
                            fontSize = 9.sp,
                            fontFamily = launcherFont
                        )
                        Text(
                            text = accentHex,
                            color = accentColor,
                            fontSize = 9.sp,
                            fontFamily = launcherFont,
                            fontWeight = FontWeight.Bold
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
                                            color = if (isSel) Color.White else Color(0xFF242424),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            if (isSel) Color(0xFF181818) else Color(0xFF0D0D0D),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onChangeAccent(hex) }
                                        .padding(vertical = 7.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(swatchCol, CircleShape)
                                    )
                                    Text(
                                        text = label,
                                        color = if (isSel) Color.White else Color(0xFF8A8A8A),
                                        fontSize = 8.sp,
                                        fontFamily = launcherFont,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================
        // SECTION 04: "App Drawer Settings"
        // Houses App Drawer App Name Scale + Alphabet Scroller Curved Horizontal Wave Slider
        // ====================================================================
        item {
            val wavePct = (drawerWaveIntensity * 100f).roundToInt()
            CollapsibleSettingsSection(
                indexTag = "04",
                title = "App Drawer Settings",
                subtitle = "App drawer app name scale & alphabet scroller wave",
                badge = "${drawerTextSizeSp}SP · ${wavePct}%",
                expanded = expandedMotion,
                accentColor = accentColor,
                lomoFontFamily = launcherFont,
                onToggle = { expandedMotion = !expandedMotion }
            ) {
                // 1. App Drawer App Name Scale (Shifted here from Typography & Layout)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
                        .background(Color(0xFF070707), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinimalistSliderRow(
                        label = "APP DRAWER APP NAME SCALE",
                        valueText = "${drawerTextSizeSp} SP",
                        fraction = ((drawerTextSizeSp - 12f) / 14f).coerceIn(0f, 1f),
                        accentColor = accentColor,
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
                                color = if (sel) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (sel) accentColor else Color(0xFF161616), RoundedCornerShape(6.dp))
                                    .clickable { onChangeDrawerTextSize(spVal) }
                                    .padding(vertical = 5.dp)
                            )
                        }
                    }
                }

                // 2. Alphabet Scroller Curved Horizontal Wave Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
                        .background(Color(0xFF070707), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinimalistSliderRow(
                        label = "ALPHABET SCROLLER CURVED WAVE",
                        valueText = "${wavePct}%",
                        fraction = (drawerWaveIntensity / 2.0f).coerceIn(0f, 1f),
                        accentColor = accentColor,
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
                                color = if (sel) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontFamily = launcherFont,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (sel) accentColor else Color(0xFF161616), RoundedCornerShape(6.dp))
                                    .clickable { onChangeDrawerWaveIntensity(wVal) }
                                    .padding(vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollapsibleSubSection(
    title: String,
    subtitle: String,
    badge: String,
    expanded: Boolean,
    accentColor: Color,
    lomoFontFamily: FontFamily,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (expanded) accentColor.copy(alpha = 0.55f) else Color(0xFF222222),
                shape = RoundedCornerShape(10.dp)
            )
            .background(Color(0xFF070707), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF737373),
                    fontSize = 9.sp,
                    fontFamily = lomoFontFamily
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = badge,
                    color = if (badge == "ACTIVE") Color.Black else Color(0xFFA3A3A3),
                    fontSize = 8.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            if (badge == "ACTIVE") accentColor else Color(0xFF171717),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
                Text(
                    text = if (expanded) "▾" else "▸",
                    color = if (expanded) accentColor else Color(0xFF737373),
                    fontSize = 13.sp,
                    fontFamily = lomoFontFamily
                )
            }
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
                        .background(Color(0xFF1A1A1A))
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
                color = Color(0xFF9E9E9E),
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = valueText,
                color = accentColor,
                fontSize = 10.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
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
                val trackH = 4.dp.toPx()
                val cy = size.height * 0.5f
                val thumbX = (clamped * size.width).coerceIn(6.dp.toPx(), size.width - 6.dp.toPx())

                // Inactive background track
                drawRoundRect(
                    color = Color(0xFF222222),
                    topLeft = Offset(0f, cy - trackH * 0.5f),
                    size = Size(size.width, trackH),
                    cornerRadius = CornerRadius(trackH * 0.5f, trackH * 0.5f)
                )
                // Active filled track
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(0f, cy - trackH * 0.5f),
                    size = Size(thumbX, trackH),
                    cornerRadius = CornerRadius(trackH * 0.5f, trackH * 0.5f)
                )
                // Minimalist blocky thumb with pure black center & thin accent stroke
                val thumbSize = 12.dp.toPx()
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(thumbX - thumbSize * 0.5f, cy - thumbSize * 0.5f),
                    size = Size(thumbSize, thumbSize)
                )
                drawRect(
                    color = accentColor,
                    topLeft = Offset(thumbX - thumbSize * 0.5f, cy - thumbSize * 0.5f),
                    size = Size(thumbSize, thumbSize),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun SettingsBackdropControlCard(
    settingsBgMode: String,
    settingsBgIntensity: Int,
    accentColor: Color,
    lomoFontFamily: FontFamily = FontFamily.Monospace,
    onChangeMode: (String) -> Unit,
    onChangeIntensity: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF282828), RoundedCornerShape(10.dp))
            .background(Color(0xFF070707).copy(alpha = 0.82f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SETTINGS MENU BACKGROUND STYLE",
                color = Color(0xFF9E9E9E),
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = settingsBgMode.uppercase(Locale.US),
                color = accentColor,
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
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
                    color = if (selected) Color.Black else Color.White,
                    fontSize = 9.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (selected) accentColor else Color(0xFF262626),
                            RoundedCornerShape(6.dp)
                        )
                        .background(
                            if (selected) accentColor else Color(0xFF141414),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onChangeMode(modeId) }
                        .padding(vertical = 6.dp)
                )
            }
        }

        val sliderLabel = when (settingsBgMode) {
            "transparent" -> "TRANSPARENCY LEVEL"
            "blurry" -> "BLURRINESS / FROST INTENSITY"
            else -> "TRANSPARENCY / BLURRINESS"
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
                    color = if (sel) Color.Black else Color(0xFFD4D4D4),
                    fontSize = 8.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .background(if (sel) accentColor else Color(0xFF151515), RoundedCornerShape(5.dp))
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
    subtitle: String,
    badge: String,
    expanded: Boolean,
    accentColor: Color,
    lomoFontFamily: FontFamily,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (expanded) Color(0xFF383838) else Color(0xFF222222),
                shape = RoundedCornerShape(14.dp)
            )
            .background(Color(0xFF0A0A0A).copy(alpha = 0.72f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = indexTag,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = lomoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = Color(0xFF737373),
                        fontSize = 10.sp,
                        fontFamily = lomoFontFamily
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = badge,
                    color = Color(0xFFA3A3A3),
                    fontSize = 9.sp,
                    fontFamily = lomoFontFamily,
                    modifier = Modifier
                        .background(Color(0xFF171717), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
                Text(
                    text = if (expanded) "▾" else "▸",
                    color = if (expanded) accentColor else Color(0xFF737373),
                    fontSize = 14.sp,
                    fontFamily = lomoFontFamily
                )
            }
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
                        .background(Color(0xFF1C1C1C))
                )
                content()
            }
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
            .border(1.dp, Color(0xFF242424), RoundedCornerShape(10.dp))
            .background(Color(0xFF050505), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontFamily = lomoFontFamily)
            Text(text = subtitle, color = Color(0xFF737373), fontSize = 10.sp, fontFamily = lomoFontFamily)
        }
        Text(
            text = actionLabel,
            color = Color.Black,
            fontSize = 10.sp,
            fontFamily = lomoFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

// ============================================================================
// INTERACTIVE WALLPAPER TUNING ROW COMPOSABLE
// ============================================================================
@Composable
fun WallpaperTuningRow(
    label: String,
    currentValue: Float,
    options: List<Pair<Float, String>>,
    accentColor: Color,
    lomoFontFamily: FontFamily = FontFamily.Monospace,
    onSelect: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color(0xFF9E9E9E),
                fontSize = 8.sp,
                fontFamily = lomoFontFamily
            )
            val activeLabel = options.minByOrNull { abs(it.first - currentValue) }?.second ?: "${currentValue}x"
            Text(
                text = activeLabel,
                color = accentColor,
                fontSize = 8.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
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
                    color = if (selected) Color.Black else Color(0xFFD4D4D4),
                    fontSize = 8.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (selected) accentColor else Color(0xFF242424),
                            RoundedCornerShape(5.dp)
                        )
                        .background(
                            if (selected) accentColor else Color(0xFF121212),
                            RoundedCornerShape(5.dp)
                        )
                        .clickable { onSelect(value) }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

// ============================================================================
// FULL-BLEED AMOLED LIVE, STATIC & USER WALLPAPER ENGINE CANVAS (v3.1.0-cmf)
// - Every single Live & Static wallpaper fills 100% of the screen (0..w, 0..h) without gaps
// - Universal Wallpaper Dimmer (dimPercent: 0%..90%) dims Live, Static, Custom Photo & 3rd-Party Wallpapers
// - 8 Live Wallpapers + 36 Static Wallpapers (including Halftone, Glowing & Fade series)
// ============================================================================
// ============================================================================
// ZERO-GC 1,200-STAR 3D NIGHT SKY SIMULATION ENGINE (v3.9.1-cmf)
// - Pre-allocates all primitive FloatArray/IntArray buffers and a single native Paint once
// - Zero object allocations inside step() or draw() -> 0 bytes/frame GC overhead
// - Features >= 1,000 stars (1,200 default, up to 1,800 with density slider)
// - Varied tiny pinpoint sizes, distinct baseline opacities, realistic spectral color tints
//   (~78% pure/crisp stellar white, ~13% faint O/B celestial blue, ~9% soft K/M orange/amber)
// - Gentle autonomous sinusoidal twinkling & slow continuous 3D forward drift (ignores all touch/user inputs)
// - Clean pause() and resume() methods that completely freeze animation (0% CPU/GPU) when screen is hidden
// ============================================================================
class DeepSpaceStarfieldSimulation {
    companion object {
        const val MIN_STAR_COUNT = 1000
        const val DEFAULT_STAR_COUNT = 1200
        const val MAX_STAR_COUNT = 1800
    }

    // Flat primitive arrays allocated once (Zero GC during animation loop)
    private val starX = FloatArray(MAX_STAR_COUNT)
    private val starY = FloatArray(MAX_STAR_COUNT)
    private val starZ = FloatArray(MAX_STAR_COUNT)
    private val starBaseRadiusDp = FloatArray(MAX_STAR_COUNT)
    private val starBaseAlpha = FloatArray(MAX_STAR_COUNT)
    private val starTwinklePhase = FloatArray(MAX_STAR_COUNT)
    private val starTwinkleFreq = FloatArray(MAX_STAR_COUNT)
    private val starRgbNoAlpha = IntArray(MAX_STAR_COUNT)

    // Single reusable native Paint (Zero GC during Canvas rendering)
    private val starPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
    }

    private var rngState: Int = 0x6D2B79F5
    private var twinkleClockSec: Float = 0f

    var isPaused: Boolean = false
        private set

    init {
        for (i in 0 until MAX_STAR_COUNT) {
            initStar(i, randomDepth = true)
        }
    }

    // Fast allocation-free Xorshift32 PRNG returning [0f, 1f)
    private fun nextFloat01(): Float {
        var x = rngState
        x = x xor (x shl 13)
        x = x xor (x ushr 17)
        x = x xor (x shl 5)
        rngState = x
        return ((x ushr 8) and 0x00FFFFFF).toFloat() / 16777216.0f
    }

    private fun initStar(index: Int, randomDepth: Boolean) {
        // Spread across 3D view frustum [-1f, 1f]
        starX[index] = (nextFloat01() * 2f - 1f)
        starY[index] = (nextFloat01() * 2f - 1f)
        starZ[index] = if (randomDepth) {
            0.04f + nextFloat01() * 0.96f
        } else {
            0.90f + nextFloat01() * 0.10f
        }

        // Varied tiny sizes: 82% micro pinpoints (0.36..0.72dp), 14% subtle mid-stars (0.72..1.08dp), 4% crisp bright stars (1.08..1.42dp)
        val sizeRoll = nextFloat01()
        starBaseRadiusDp[index] = when {
            sizeRoll < 0.82f -> 0.36f + nextFloat01() * 0.36f
            sizeRoll < 0.96f -> 0.72f + nextFloat01() * 0.36f
            else -> 1.08f + nextFloat01() * 0.34f
        }

        // Distinct baseline opacities (0.22..0.98)
        starBaseAlpha[index] = 0.22f + nextFloat01() * 0.76f

        // Gentle autonomous ambient twinkling parameters
        starTwinklePhase[index] = nextFloat01() * 6.2831855f
        starTwinkleFreq[index] = 0.55f + nextFloat01() * 1.75f

        // Realistic stellar spectral color tints:
        // ~78% white, ~13% faint celestial blue, ~9% soft warm orange
        val tintRoll = nextFloat01()
        starRgbNoAlpha[index] = when {
            tintRoll < 0.52f -> 0x00FFFFFF // Pure Stellar White
            tintRoll < 0.78f -> 0x00F4F7FB // Crisp Ice-White
            tintRoll < 0.85f -> 0x00BFDBFE // Faint O-Type Celestial Blue
            tintRoll < 0.91f -> 0x0093C5FD // Soft B-Type Sky Blue
            tintRoll < 0.96f -> 0x00FED7AA // Soft K-Type Warm Orange
            else -> 0x00FDE68A             // Gentle Amber-Orange Tint
        }
    }

    /**
     * Completely freezes the 3D starfield simulation when the screen is hidden or paused.
     */
    fun pause() {
        isPaused = true
    }

    /**
     * Cleanly resumes the 3D starfield simulation when the screen becomes visible again.
     */
    fun resume() {
        isPaused = false
    }

    /**
     * Advances the slow 3D forward drift and ambient twinkling by dtSec (Zero allocations).
     */
    fun step(dtSec: Float, speedMultiplier: Float, activeCount: Int) {
        if (isPaused) return
        val clampedCount = activeCount.coerceIn(MIN_STAR_COUNT, MAX_STAR_COUNT)
        // Slow, calm default forward drift / gentle warp speed along the Z depth axis
        val forwardDrift = dtSec * 0.038f * speedMultiplier
        twinkleClockSec = (twinkleClockSec + dtSec) % 3600f

        for (i in 0 until clampedCount) {
            val nextZ = starZ[i] - forwardDrift
            if (nextZ <= 0.025f) {
                initStar(i, randomDepth = false)
            } else {
                starZ[i] = nextZ
            }
        }
    }

    /**
     * Renders all >= 1,000 3D stars directly onto the hardware-accelerated native Canvas with zero GC overhead.
     */
    fun draw(
        nativeCanvas: android.graphics.Canvas,
        width: Float,
        height: Float,
        densityPxPerDp: Float,
        activeCount: Int,
        opacityMultiplier: Float,
        scaleMultiplier: Float
    ) {
        if (width <= 0f || height <= 0f) return
        val clampedCount = activeCount.coerceIn(MIN_STAR_COUNT, MAX_STAR_COUNT)
        val cx = width * 0.5f
        val cy = height * 0.5f
        val halfSpan = max(width, height) * 0.62f
        val t = twinkleClockSec
        val sizeScale = densityPxPerDp * (0.75f + 0.25f * scaleMultiplier)

        for (i in 0 until clampedCount) {
            val z = starZ[i]
            val invZ = 1.0f / z
            val sx = cx + starX[i] * invZ * halfSpan
            val sy = cy + starY[i] * invZ * halfSpan

            // If perspective projection moves the star outside the screen bounds, recycle it in the far field
            if (sx < -4f || sx > width + 4f || sy < -4f || sy > height + 4f) {
                if (!isPaused) {
                    initStar(i, randomDepth = false)
                }
                continue
            }

            // Depth-attenuated tiny radius (closer stars appear slightly crisper, distant stars are sub-pixel pinpoints)
            val depthScale = (1.18f - z * 0.68f).coerceIn(0.42f, 1.35f)
            val radiusPx = (starBaseRadiusDp[i] * depthScale * sizeScale).coerceAtLeast(0.65f)

            // Gentle, automatic ambient sinusoidal twinkling
            val twinkle = 0.74f + 0.26f * sin(t * starTwinkleFreq[i] + starTwinklePhase[i])
            val depthAlpha = (1.12f - z * 0.65f).coerceIn(0.18f, 1.0f)
            val alphaFloat = (starBaseAlpha[i] * twinkle * depthAlpha * opacityMultiplier).coerceIn(0.05f, 1.0f)
            val alphaByte = (alphaFloat * 255f).toInt().coerceIn(12, 255)

            starPaint.color = (alphaByte shl 24) or starRgbNoAlpha[i]
            nativeCanvas.drawCircle(sx, sy, radiusPx, starPaint)
        }
    }
}

@Composable
fun OledWallpaperCanvas(
    preset: String,
    accentColor: Color,
    dimPercent: Int,
    wpSpeed: Float = 1.0f,
    wpDensity: Float = 1.0f,
    wpOpacity: Float = 1.0f,
    wpScale: Float = 1.0f,
    isScreenVisible: Boolean = true,
    userWallpaperRefreshTrigger: Int = 0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isLive = preset.startsWith("live_")
    var liveTimeSec by remember { mutableStateOf(0f) }
    var starfieldFrameTick by remember { mutableStateOf(0) }
    val currentSpeed by rememberUpdatedState(wpSpeed.coerceIn(0.15f, 3.0f))
    val currentDensity by rememberUpdatedState(wpDensity.coerceIn(0.4f, 2.0f))

    // Dedicated Zero-GC 3D Starfield Simulation Engine with pause() and resume()
    val starfieldSimulation = remember { DeepSpaceStarfieldSimulation() }
    var isLifecycleResumed by remember { mutableStateOf(true) }

    // Automatically call pause() and resume() when the Activity lifecycle or screen visibility changes
    DisposableEffect(lifecycleOwner, isScreenVisible) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME, Lifecycle.Event.ON_START -> {
                    isLifecycleResumed = true
                    if (isScreenVisible) starfieldSimulation.resume()
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    isLifecycleResumed = false
                    starfieldSimulation.pause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (isScreenVisible && isLifecycleResumed) {
            starfieldSimulation.resume()
        } else {
            starfieldSimulation.pause()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            starfieldSimulation.pause()
        }
    }

    // Load custom imported user wallpaper bitmap when preset == "user_image"
    val customUserBitmap: ImageBitmap? = remember(preset, userWallpaperRefreshTrigger) {
        if (preset == "user_image") {
            try {
                val file = File(context.filesDir, "user_custom_wallpaper.jpg")
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else null
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val shouldAnimateLive = isLive && isScreenVisible && isLifecycleResumed
    if (shouldAnimateLive) {
        LaunchedEffect(preset, shouldAnimateLive) {
            var lastNanos = 0L
            while (true) {
                withFrameNanos { nanos ->
                    val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    lastNanos = nanos
                    if (preset == "live_starfield_3d") {
                        val targetStars = (DeepSpaceStarfieldSimulation.DEFAULT_STAR_COUNT * currentDensity)
                            .roundToInt()
                            .coerceIn(DeepSpaceStarfieldSimulation.MIN_STAR_COUNT, DeepSpaceStarfieldSimulation.MAX_STAR_COUNT)
                        starfieldSimulation.step(dt, currentSpeed, targetStars)
                        starfieldFrameTick = (starfieldFrameTick + 1) and 0x7FFFFFFF
                    } else {
                        liveTimeSec = (liveTimeSec + dt * 0.42f * currentSpeed) % 7200f
                    }
                }
            }
        }
    }

    val densityMult = wpDensity.coerceIn(0.4f, 2.0f)
    val opacityMult = wpOpacity.coerceIn(0.25f, 2.0f)
    val scaleMult = wpScale.coerceIn(0.5f, 1.8f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val minDim = min(w, h)
        val maxDim = max(w, h)
        val fullDiag = hypot(w, h)
        val t = liveTimeSec

        when (preset) {
            // ================================================================
            // 0. REALISTIC 1,200-STAR 3D NIGHT SKY SIMULATION (Zero-GC Native Loop)
            // ================================================================
            "live_starfield_3d" -> {
                // Read starfieldFrameTick to subscribe Canvas draw phase to frame updates without allocating
                if (starfieldFrameTick >= 0) {
                    val activeStars = (DeepSpaceStarfieldSimulation.DEFAULT_STAR_COUNT * densityMult)
                        .roundToInt()
                        .coerceIn(DeepSpaceStarfieldSimulation.MIN_STAR_COUNT, DeepSpaceStarfieldSimulation.MAX_STAR_COUNT)
                    drawIntoCanvas { composeCanvas ->
                        starfieldSimulation.draw(
                            nativeCanvas = composeCanvas.nativeCanvas,
                            width = w,
                            height = h,
                            densityPxPerDp = 1.dp.toPx(),
                            activeCount = activeStars,
                            opacityMultiplier = opacityMult,
                            scaleMultiplier = scaleMult
                        )
                    }
                }
            }
            "oled", "system_wallpaper" -> {
                // "oled" stays pure #000000; "system_wallpaper" lets Android's FLAG_SHOW_WALLPAPER shine through
                // and then applies the universal dimmer overlay below!
            }

            "user_image" -> {
                val img = customUserBitmap
                if (img != null && img.width > 0 && img.height > 0) {
                    // Center-crop full-screen edge-to-edge rendering without leaving any gaps
                    val srcRatio = img.width.toFloat() / img.height.toFloat()
                    val dstRatio = w / h.coerceAtLeast(1f)
                    val srcW: Int
                    val srcH: Int
                    val srcX: Int
                    val srcY: Int
                    if (srcRatio > dstRatio) {
                        srcH = img.height
                        srcW = (img.height * dstRatio).roundToInt().coerceIn(1, img.width)
                        srcX = ((img.width - srcW) / 2).coerceAtLeast(0)
                        srcY = 0
                    } else {
                        srcW = img.width
                        srcH = (img.width / dstRatio).roundToInt().coerceIn(1, img.height)
                        srcX = 0
                        srcY = ((img.height - srcH) / 2).coerceAtLeast(0)
                    }
                    drawImage(
                        image = img,
                        srcOffset = IntOffset(srcX, srcY),
                        srcSize = IntSize(srcW, srcH),
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(w.roundToInt(), h.roundToInt())
                    )
                }
            }

            // ================================================================
            // 1. ORBITAL NODES (Full-Screen Edge-to-Edge 0..w, 0..h)
            // ================================================================
            "live_constellation" -> {
                val nodeCount = (32 * densityMult).roundToInt().coerceIn(12, 64)
                val pts = ArrayList<Offset>(nodeCount)
                val driftSpanX = (0.14f * scaleMult).coerceIn(0.05f, 0.28f)
                val driftSpanY = (0.12f * scaleMult).coerceIn(0.05f, 0.24f)
                for (i in 0 until nodeCount) {
                    val seedX = ((i * 73 + 11) % 100) / 100f
                    val seedY = ((i * 139 + 23) % 100) / 100f
                    val speedX = 0.16f + (i % 5) * 0.04f
                    val speedY = 0.13f + (i % 7) * 0.03f
                    val nx = (((seedX + sin(t * speedX + i * 1.7f) * driftSpanX) % 1f) + 1f) % 1f
                    val ny = (((seedY + cos(t * speedY + i * 2.3f) * driftSpanY) % 1f) + 1f) % 1f
                    pts.add(Offset(nx * w, ny * h))
                }
                val connectRadius = minDim * (0.28f * scaleMult).coerceIn(0.15f, 0.48f)
                for (i in 0 until nodeCount) {
                    for (j in i + 1 until nodeCount) {
                        val d = (pts[i] - pts[j]).getDistance()
                        if (d < connectRadius) {
                            val lineAlpha = ((1f - d / connectRadius) * 0.34f * opacityMult).coerceIn(0f, 0.78f)
                            drawLine(
                                color = if ((i + j) % 3 == 0) accentColor.copy(alpha = lineAlpha) else Color.White.copy(alpha = lineAlpha * 0.8f),
                                start = pts[i],
                                end = pts[j],
                                strokeWidth = 0.9.dp.toPx()
                            )
                        }
                    }
                }
                for (i in 0 until nodeCount) {
                    val isAccent = i % 4 == 0
                    val dotAlpha = ((if (isAccent) 0.78f else 0.52f) * opacityMult).coerceIn(0.08f, 0.98f)
                    drawCircle(
                        color = if (isAccent) accentColor.copy(alpha = dotAlpha) else Color.White.copy(alpha = dotAlpha),
                        radius = (if (isAccent) 2.2.dp.toPx() else 1.5.dp.toPx()) * (0.8f + 0.2f * scaleMult),
                        center = pts[i]
                    )
                }
            }

            // ================================================================
            // 2. DEEP EMBERS (Full-Screen Edge-to-Edge Rising Micro Dust)
            // ================================================================
            "live_embers" -> {
                val emberCount = (34 * densityMult).roundToInt().coerceIn(14, 68)
                val swaySpan = w * 0.045f * scaleMult
                for (i in 0 until emberCount) {
                    val baseX = ((i * 61 + 7) % 100) / 100f * w
                    val riseRate = 0.032f + (i % 6) * 0.009f
                    val phaseOffset = i * 0.37f
                    val cyc = ((t * riseRate + phaseOffset) % 1f)
                    val y = h * (1.0f - cyc)
                    val x = (((baseX + sin(t * (0.35f + (i % 4) * 0.08f) + i * 1.9f) * swaySpan) % w) + w) % w

                    val envelope = sin(cyc * PI.toFloat()).coerceIn(0f, 1f)
                    val isAccent = i % 3 == 0
                    val coreAlpha = (envelope * (if (isAccent) 0.70f else 0.46f) * opacityMult).coerceIn(0f, 0.94f)
                    val rPx = (1.1f + (i % 3) * 0.45f).dp.toPx() * scaleMult

                    if (isAccent && coreAlpha > 0.12f) {
                        drawCircle(
                            color = accentColor.copy(alpha = (coreAlpha * 0.22f).coerceIn(0f, 0.38f)),
                            radius = rPx * 2.9f,
                            center = Offset(x, y)
                        )
                    }
                    drawCircle(
                        color = if (isAccent) accentColor.copy(alpha = coreAlpha) else Color.White.copy(alpha = coreAlpha),
                        radius = rPx,
                        center = Offset(x, y)
                    )
                }
            }

            // ================================================================
            // 3. STARLIGHT BREATH (Full-Screen Edge-to-Edge Twinkling Starfield)
            // ================================================================
            "live_starlight" -> {
                val starCount = (46 * densityMult).roundToInt().coerceIn(18, 90)
                for (i in 0 until starCount) {
                    val gx = ((i * 0.6180339887f + 0.01f) % 1f)
                    val gy = (((i * 0.3819660112f) + (i % 7) * 0.071f + 0.01f) % 1f)
                    val x = gx * w
                    val y = gy * h

                    val breathSpeed = 0.55f + (i % 7) * 0.16f
                    val breath = (0.5f + 0.5f * sin(t * breathSpeed + i * 2.17f)).coerceIn(0.05f, 1f)
                    val isAccent = i % 5 == 0
                    val alpha = (breath * (if (isAccent) 0.74f else 0.50f) * opacityMult).coerceIn(0.03f, 0.96f)
                    val rad = (0.9f + (i % 3) * 0.35f).dp.toPx() * scaleMult

                    drawCircle(
                        color = if (isAccent) accentColor.copy(alpha = alpha) else Color.White.copy(alpha = alpha),
                        radius = rad,
                        center = Offset(x, y)
                    )

                    if (i % 6 == 0 && breath > 0.78f) {
                        val glintFactor = ((breath - 0.78f) / 0.22f).coerceIn(0f, 1f)
                        val arm = (4.5f * glintFactor * scaleMult).dp.toPx()
                        val glintCol = (if (isAccent) accentColor else Color.White).copy(alpha = (alpha * 0.55f * glintFactor).coerceIn(0f, 0.65f))
                        drawLine(glintCol, Offset(x - arm, y), Offset(x + arm, y), strokeWidth = 0.7.dp.toPx())
                        drawLine(glintCol, Offset(x, y - arm), Offset(x, y + arm), strokeWidth = 0.7.dp.toPx())
                    }
                }
            }

            // ================================================================
            // 4. KEPLER ORBITS (Full-Screen Elliptical Planetary Tracks)
            // ================================================================
            "live_kepler" -> {
                val center = Offset(w * 0.5f, h * 0.5f)
                val orbitCount = (6 * densityMult).roundToInt().coerceIn(3, 10)
                drawCircle(
                    color = accentColor.copy(alpha = (0.68f * opacityMult).coerceIn(0.1f, 0.95f)),
                    radius = 2.4.dp.toPx() * scaleMult,
                    center = center
                )
                for (k in 1..orbitCount) {
                    val frac = k.toFloat() / orbitCount.toFloat()
                    val rx = maxDim * (0.08f + frac * 0.48f) * scaleMult
                    val ry = rx * (0.48f + (k % 3) * 0.10f)
                    val tiltDeg = -24f + k * 11f
                    val isAccentOrbit = k % 2 == 1
                    val trackAlpha = ((0.14f + (1f - frac) * 0.08f) * opacityMult).coerceIn(0.04f, 0.48f)

                    val steps = 72
                    val tiltRad = Math.toRadians(tiltDeg.toDouble())
                    val cosT = cos(tiltRad).toFloat()
                    val sinT = sin(tiltRad).toFloat()
                    val orbitPath = Path()
                    for (s in 0..steps) {
                        val ang = (s.toFloat() / steps) * (2f * PI.toFloat())
                        val ex = rx * cos(ang)
                        val ey = ry * sin(ang)
                        val px = center.x + (ex * cosT - ey * sinT)
                        val py = center.y + (ex * sinT + ey * cosT)
                        if (s == 0) orbitPath.moveTo(px, py) else orbitPath.lineTo(px, py)
                    }
                    drawPath(
                        path = orbitPath,
                        color = if (isAccentOrbit) accentColor.copy(alpha = trackAlpha) else Color.White.copy(alpha = trackAlpha),
                        style = Stroke(width = 0.75.dp.toPx())
                    )

                    val dir = if (k % 2 == 0) -1f else 1f
                    val satAng = t * (0.38f / sqrt(k.toFloat())) * dir + k * 1.4f
                    val sx = rx * cos(satAng)
                    val sy = ry * sin(satAng)
                    val satPt = Offset(
                        center.x + (sx * cosT - sy * sinT),
                        center.y + (sx * sinT + sy * cosT)
                    )
                    val satAlpha = ((if (isAccentOrbit) 0.82f else 0.64f) * opacityMult).coerceIn(0.1f, 1f)
                    drawCircle(
                        color = if (isAccentOrbit) accentColor.copy(alpha = satAlpha) else Color.White.copy(alpha = satAlpha),
                        radius = (if (isAccentOrbit) 2.3.dp.toPx() else 1.8.dp.toPx()) * (0.85f + 0.15f * scaleMult),
                        center = satPt
                    )
                }
            }

            // ================================================================
            // 5. MICRO LATTICE (Full-Screen Edge-to-Edge Breathing Dot Field)
            // ================================================================
            "live_quantum" -> {
                val cols = (11 * densityMult).roundToInt().coerceIn(6, 20)
                val rows = (22 * densityMult).roundToInt().coerceIn(12, 38)
                val spacingX = w / cols.toFloat()
                val spacingY = h / rows.toFloat()
                val waveCenter = Offset(
                    w * (0.5f + 0.25f * sin(t * 0.28f)),
                    h * (0.5f + 0.25f * cos(t * 0.22f))
                )
                val waveLength = minDim * 0.42f * scaleMult

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val pt = Offset((c + 0.5f) * spacingX, (r + 0.5f) * spacingY)
                        val dist = (pt - waveCenter).getDistance()
                        val wave = 0.5f + 0.5f * cos((dist / waveLength) * PI.toFloat() * 2f - t * 1.15f)
                        val smoothWave = (wave * wave * wave).coerceIn(0f, 1f)
                        val isAccentDot = (r + c) % 4 == 0 && smoothWave > 0.55f
                        val alpha = ((0.09f + smoothWave * 0.44f) * opacityMult).coerceIn(0.03f, 0.90f)
                        val radiusPx = (0.9f + smoothWave * 1.25f).dp.toPx() * (0.8f + 0.2f * scaleMult)

                        drawCircle(
                            color = if (isAccentDot) accentColor.copy(alpha = alpha) else Color.White.copy(alpha = alpha * 0.85f),
                            radius = radiusPx,
                            center = pt
                        )
                    }
                }
            }

            // ================================================================
            // 6. PHASE THREAD (Full-Screen Harmonic Lissajous Ribbons)
            // ================================================================
            "live_lissajous" -> {
                val center = Offset(w * 0.5f, h * 0.5f)
                val ampX = w * 0.46f * scaleMult.coerceIn(0.5f, 1.1f)
                val ampY = h * 0.44f * scaleMult.coerceIn(0.5f, 1.1f)
                val segments = (110 * densityMult).roundToInt().coerceIn(55, 180)
                val tailSpan = 3.2f
                var prevPt: Offset? = null

                for (s in 0..segments) {
                    val f = s.toFloat() / segments.toFloat()
                    val tau = t * 0.42f - (1f - f) * tailSpan
                    val px = center.x + sin(tau * 1.0f + sin(t * 0.12f)) * ampX * cos(tau * 0.22f)
                    val py = center.y + sin(tau * 1.5f + PI.toFloat() * 0.25f) * ampY
                    val curPt = Offset(px, py)
                    if (prevPt != null) {
                        val segAlpha = (f * f * 0.48f * opacityMult).coerceIn(0.02f, 0.88f)
                        drawLine(
                            color = if (f > 0.62f) accentColor.copy(alpha = segAlpha) else Color.White.copy(alpha = segAlpha * 0.75f),
                            start = prevPt,
                            end = curPt,
                            strokeWidth = (0.7f + f * 0.65f).dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    prevPt = curPt
                }
                prevPt?.let { head ->
                    drawCircle(
                        color = accentColor.copy(alpha = (0.88f * opacityMult).coerceIn(0.15f, 1f)),
                        radius = 2.5.dp.toPx() * scaleMult,
                        center = head
                    )
                }
            }

            // ================================================================
            // 7. AURORA GLOW FADE (Slow-Breathing Full-Screen Glowing Atmospheric Fade)
            // ================================================================
            "live_glow_horizon" -> {
                val c1 = Offset(
                    w * (0.5f + 0.24f * sin(t * 0.32f)),
                    h * (0.78f + 0.08f * cos(t * 0.26f))
                )
                val c2 = Offset(
                    w * (0.5f - 0.22f * cos(t * 0.24f)),
                    h * (0.24f + 0.08f * sin(t * 0.30f))
                )
                val a1 = ((0.26f + 0.08f * sin(t * 0.5f)) * opacityMult).coerceIn(0.05f, 0.68f)
                val a2 = ((0.16f + 0.06f * cos(t * 0.4f)) * opacityMult).coerceIn(0.04f, 0.52f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = a1), accentColor.copy(alpha = a1 * 0.35f), Color.Transparent),
                        center = c1,
                        radius = fullDiag * 0.58f * scaleMult
                    ),
                    size = Size(w, h)
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = a2), Color.Transparent),
                        center = c2,
                        radius = fullDiag * 0.48f * scaleMult
                    ),
                    size = Size(w, h)
                )
            }

            // ================================================================
            // 8. HALFTONE WAVE (Full-Screen Glowing Halftone Pulse Matrix)
            // ================================================================
            "live_halftone_pulse" -> {
                val step = (20.dp.toPx() / densityMult.coerceIn(0.6f, 1.5f)).coerceAtLeast(11.dp.toPx())
                val cols = (w / step).toInt().coerceAtLeast(1)
                val rows = (h / step).toInt().coerceAtLeast(1)
                val cellW = w / cols.toFloat()
                val cellH = h / rows.toFloat()

                for (r in 0 until rows) {
                    val ny = (r + 0.5f) / rows.toFloat()
                    for (c in 0 until cols) {
                        val nx = (c + 0.5f) / cols.toFloat()
                        val phase = (nx * 2.4f + ny * 3.6f) * PI.toFloat() - t * 1.1f
                        val wave = (0.5f + 0.5f * sin(phase)).coerceIn(0f, 1f)
                        val dotR = (0.55f + wave * 2.15f).dp.toPx() * scaleMult
                        val dotA = ((0.06f + wave * 0.36f) * opacityMult).coerceIn(0.02f, 0.85f)
                        drawCircle(
                            color = if (wave > 0.62f) accentColor.copy(alpha = dotA) else Color.White.copy(alpha = dotA * 0.8f),
                            radius = dotR,
                            center = Offset((c + 0.5f) * cellW, (r + 0.5f) * cellH)
                        )
                    }
                }
            }

            // ================================================================
            // 36 FULL-BLEED STATIC AMOLED WALLPAPERS (0..w, 0..h WITH ZERO GAPS)
            // ================================================================
            "dots" -> {
                val step = (18.dp.toPx() * scaleMult).coerceAtLeast(10.dp.toPx())
                val dotAlpha = (0.22f * opacityMult).coerceIn(0.05f, 0.75f)
                val startX = (w % step) * 0.5f
                val startY = (h % step) * 0.5f
                var x = startX
                while (x <= w) {
                    var y = startY
                    while (y <= h) {
                        drawCircle(
                            color = Color.White.copy(alpha = dotAlpha),
                            radius = 1.3.dp.toPx(),
                            center = Offset(x, y)
                        )
                        y += step
                    }
                    x += step
                }
            }

            "blueprint" -> {
                val step = (32.dp.toPx() * scaleMult).coerceAtLeast(16.dp.toPx())
                val lineAlpha = (0.18f * opacityMult).coerceIn(0.05f, 0.65f)
                var x = (w % step) * 0.5f
                while (x <= w) {
                    drawLine(accentColor.copy(alpha = lineAlpha), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    x += step
                }
                var y = (h % step) * 0.5f
                while (y <= h) {
                    drawLine(accentColor.copy(alpha = lineAlpha), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                    y += step
                }
            }

            "circuit" -> {
                val stroke = 1.3.dp.toPx()
                val traceColor = Color.White.copy(alpha = (0.18f * opacityMult).coerceIn(0.05f, 0.65f))
                val nodeColor = accentColor.copy(alpha = (0.48f * opacityMult).coerceIn(0.10f, 0.88f))
                val paths = listOf(
                    listOf(Offset(0f, h * 0.08f), Offset(w * 0.28f, h * 0.08f), Offset(w * 0.42f, h * 0.18f), Offset(w * 0.42f, h * 0.44f)),
                    listOf(Offset(w, h * 0.14f), Offset(w * 0.70f, h * 0.14f), Offset(w * 0.56f, h * 0.26f), Offset(w * 0.56f, h * 0.58f)),
                    listOf(Offset(0f, h * 0.52f), Offset(w * 0.22f, h * 0.52f), Offset(w * 0.36f, h * 0.64f), Offset(w * 0.36f, h)),
                    listOf(Offset(w, h * 0.68f), Offset(w * 0.74f, h * 0.68f), Offset(w * 0.60f, h * 0.80f), Offset(w * 0.60f, h)),
                    listOf(Offset(w * 0.18f, 0f), Offset(w * 0.18f, h * 0.24f), Offset(w * 0.30f, h * 0.34f)),
                    listOf(Offset(w * 0.82f, 0f), Offset(w * 0.82f, h * 0.38f), Offset(w * 0.68f, h * 0.48f))
                )
                for (pts in paths) {
                    for (k in 0 until pts.lastIndex) {
                        drawLine(traceColor, pts[k], pts[k + 1], strokeWidth = stroke, cap = StrokeCap.Round)
                    }
                    drawCircle(color = nodeColor, radius = 3.0.dp.toPx(), center = pts.last())
                }
            }

            // ================================================================
            // NEW HALFTONE, GLOWING & FADE WALLPAPERS (FULL SCREEN EDGE-TO-EDGE)
            // ================================================================
            "static_halftone" -> {
                // Full-Screen Radial Halftone Dot Gradient (reaches all 4 corners)
                val step = (19.dp.toPx() * scaleMult).coerceAtLeast(11.dp.toPx())
                val center = Offset(w * 0.5f, h * 0.5f)
                val maxDist = hypot(w * 0.5f, h * 0.5f) * 1.05f
                var y = (h % step) * 0.5f
                while (y <= h) {
                    var x = (w % step) * 0.5f
                    while (x <= w) {
                        val d = (Offset(x, y) - center).getDistance()
                        val norm = (1f - (d / maxDist)).coerceIn(0.08f, 1f)
                        val r = (0.55f + norm * 2.4f).dp.toPx()
                        val a = ((0.06f + norm * 0.34f) * opacityMult).coerceIn(0.03f, 0.82f)
                        drawCircle(
                            color = if (norm > 0.62f) accentColor.copy(alpha = a) else Color.White.copy(alpha = a),
                            radius = r,
                            center = Offset(x, y)
                        )
                        x += step
                    }
                    y += step
                }
            }

            "static_halftone_vertical" -> {
                // Full-Screen Vertical Halftone Dot Fade (Bottom Horizon to Top Edge)
                val step = (18.dp.toPx() * scaleMult).coerceAtLeast(10.dp.toPx())
                var y = (h % step) * 0.5f
                while (y <= h) {
                    val fy = (y / h.coerceAtLeast(1f)).coerceIn(0f, 1f)
                    val r = (0.5f + fy * fy * 2.8f).dp.toPx()
                    val a = ((0.05f + fy * 0.38f) * opacityMult).coerceIn(0.02f, 0.85f)
                    var x = (w % step) * 0.5f
                    while (x <= w) {
                        drawCircle(
                            color = if (fy > 0.58f) accentColor.copy(alpha = a) else Color.White.copy(alpha = a * 0.85f),
                            radius = r,
                            center = Offset(x, y)
                        )
                        x += step
                    }
                    y += step
                }
            }

            "static_halftone_diagonal" -> {
                // Full-Screen 45° Diagonal Halftone Gradient Matrix
                val step = (18.dp.toPx() * scaleMult).coerceAtLeast(10.dp.toPx())
                var y = (h % step) * 0.5f
                while (y <= h) {
                    val ny = y / h.coerceAtLeast(1f)
                    var x = (w % step) * 0.5f
                    while (x <= w) {
                        val nx = x / w.coerceAtLeast(1f)
                        val diag = ((nx + ny) * 0.5f).coerceIn(0f, 1f)
                        val r = (0.5f + diag * 2.6f).dp.toPx()
                        val a = ((0.06f + diag * 0.36f) * opacityMult).coerceIn(0.03f, 0.85f)
                        drawCircle(
                            color = if (diag > 0.55f) accentColor.copy(alpha = a) else Color.White.copy(alpha = a * 0.8f),
                            radius = r,
                            center = Offset(x, y)
                        )
                        x += step
                    }
                    y += step
                }
            }

            "static_halftone_duotone" -> {
                // Full-Screen Interleaved Dual-Grid Duotone Halftone
                val step = (22.dp.toPx() * scaleMult).coerceAtLeast(12.dp.toPx())
                var y = (h % step) * 0.5f
                while (y <= h) {
                    val ny = y / h.coerceAtLeast(1f)
                    var x = (w % step) * 0.5f
                    while (x <= w) {
                        val nx = x / w.coerceAtLeast(1f)
                        val w1 = (1f - ny).coerceIn(0.1f, 1f)
                        val w2 = ny.coerceIn(0.1f, 1f)
                        drawCircle(
                            color = Color.White.copy(alpha = (0.24f * w1 * opacityMult).coerceIn(0.03f, 0.75f)),
                            radius = (0.6f + w1 * 1.8f).dp.toPx(),
                            center = Offset(x, y)
                        )
                        val ox = (x + step * 0.5f).coerceAtMost(w)
                        val oy = (y + step * 0.5f).coerceAtMost(h)
                        drawCircle(
                            color = accentColor.copy(alpha = (0.32f * w2 * opacityMult).coerceIn(0.04f, 0.82f)),
                            radius = (0.6f + w2 * 2.0f).dp.toPx(),
                            center = Offset(ox, oy)
                        )
                        x += step
                    }
                    y += step
                }
            }

            "static_halftone_wave" -> {
                // Full-Screen Sinusoidal Wave Halftone Matrix
                val step = (18.dp.toPx() * scaleMult).coerceAtLeast(10.dp.toPx())
                var y = (h % step) * 0.5f
                while (y <= h) {
                    val ny = y / h.coerceAtLeast(1f)
                    var x = (w % step) * 0.5f
                    while (x <= w) {
                        val nx = x / w.coerceAtLeast(1f)
                        val wave = 0.5f + 0.5f * sin((nx * 2.5f + sin(ny * PI.toFloat() * 2f) * 0.6f) * PI.toFloat() * 2f)
                        val r = (0.55f + wave * 2.25f).dp.toPx()
                        val a = ((0.06f + wave * 0.34f) * opacityMult).coerceIn(0.03f, 0.82f)
                        drawCircle(
                            color = if (wave > 0.62f) accentColor.copy(alpha = a) else Color.White.copy(alpha = a * 0.8f),
                            radius = r,
                            center = Offset(x, y)
                        )
                        x += step
                    }
                    y += step
                }
            }

            "static_glow_horizon" -> {
                // Full-Screen Rising Horizon Glow Fade with Subtle Scanline Texture
                val aGlow = (0.42f * opacityMult).coerceIn(0.08f, 0.88f)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Black,
                            accentColor.copy(alpha = aGlow * 0.32f),
                            accentColor.copy(alpha = aGlow)
                        ),
                        startY = 0f,
                        endY = h
                    ),
                    size = Size(w, h)
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = aGlow * 0.35f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.96f),
                        radius = w * 0.85f * scaleMult
                    ),
                    size = Size(w, h)
                )
            }

            "static_glow_nebula" -> {
                // Full-Screen Dual-Core Soft Atmospheric Nebula Glow
                val a1 = (0.34f * opacityMult).coerceIn(0.06f, 0.80f)
                val a2 = (0.22f * opacityMult).coerceIn(0.04f, 0.65f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = a1), accentColor.copy(alpha = a1 * 0.25f), Color.Transparent),
                        center = Offset(w * 0.28f, h * 0.32f),
                        radius = fullDiag * 0.52f * scaleMult
                    ),
                    size = Size(w, h)
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = a2), accentColor.copy(alpha = a2 * 0.4f), Color.Transparent),
                        center = Offset(w * 0.74f, h * 0.72f),
                        radius = fullDiag * 0.55f * scaleMult
                    ),
                    size = Size(w, h)
                )
            }

            "static_glow_edge" -> {
                // Full-Screen Edge-Lit Border Aura (Pure #000000 center, glowing edges)
                val edgeAlpha = (0.36f * opacityMult).coerceIn(0.08f, 0.82f)
                val borderDepth = minDim * 0.28f * scaleMult
                // Top glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = edgeAlpha), Color.Transparent),
                        startY = 0f,
                        endY = borderDepth
                    ),
                    size = Size(w, borderDepth)
                )
                // Bottom glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(alpha = edgeAlpha)),
                        startY = h - borderDepth,
                        endY = h
                    ),
                    topLeft = Offset(0f, h - borderDepth),
                    size = Size(w, borderDepth)
                )
                // Left glow
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(accentColor.copy(alpha = edgeAlpha * 0.75f), Color.Transparent),
                        startX = 0f,
                        endX = borderDepth * 0.75f
                    ),
                    size = Size(borderDepth * 0.75f, h)
                )
                // Right glow
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(alpha = edgeAlpha * 0.75f)),
                        startX = w - borderDepth * 0.75f,
                        endX = w
                    ),
                    topLeft = Offset(w - borderDepth * 0.75f, 0f),
                    size = Size(borderDepth * 0.75f, h)
                )
            }

            "static_glow_pillar" -> {
                // Full-Screen Vertical Light Column Glow
                val a = (0.32f * opacityMult).coerceIn(0.06f, 0.78f)
                val beamCenter = w * 0.5f
                val halfSpan = w * 0.42f * scaleMult
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            accentColor.copy(alpha = a * 0.45f),
                            Color.White.copy(alpha = a * 0.65f),
                            accentColor.copy(alpha = a * 0.45f),
                            Color.Transparent
                        ),
                        startX = (beamCenter - halfSpan).coerceAtLeast(0f),
                        endX = (beamCenter + halfSpan).coerceAtMost(w)
                    ),
                    size = Size(w, h)
                )
                drawLine(
                    color = accentColor.copy(alpha = (a * 1.2f).coerceIn(0.1f, 0.9f)),
                    start = Offset(beamCenter, 0f),
                    end = Offset(beamCenter, h),
                    strokeWidth = 1.dp.toPx()
                )
            }

            "static_fade_monolith" -> {
                // Full-Screen Top-to-Bottom Monolith Ombré Fade
                val a = (0.38f * opacityMult).coerceIn(0.08f, 0.85f)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = a),
                            Color.White.copy(alpha = a * 0.22f),
                            Color.Black
                        ),
                        startY = 0f,
                        endY = h
                    ),
                    size = Size(w, h)
                )
            }

            "static_fade_twilight" -> {
                // Full-Screen Diagonal Silk Gradient Fade & Fine Rules
                val a = (0.36f * opacityMult).coerceIn(0.08f, 0.82f)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = a),
                            Color.Black,
                            Color.White.copy(alpha = a * 0.45f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    ),
                    size = Size(w, h)
                )
            }

            "static_fade_vignette" -> {
                // Full-Screen Radial Spotlight Fade
                val a = (0.34f * opacityMult).coerceIn(0.08f, 0.80f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = a),
                            Color.White.copy(alpha = a * 0.25f),
                            Color.Black
                        ),
                        center = Offset(w * 0.5f, h * 0.48f),
                        radius = fullDiag * 0.52f * scaleMult
                    ),
                    size = Size(w, h)
                )
            }

            "static_fade_prism" -> {
                // Full-Screen Stepped Atmospheric Horizon Bands Fade
                val bands = 16
                val bandH = h / bands.toFloat()
                for (b in 0 until bands) {
                    val f = (b + 1).toFloat() / bands.toFloat()
                    val a = ((f * f * 0.34f) * opacityMult).coerceIn(0.01f, 0.78f)
                    drawRect(
                        color = if (b % 3 == 0) accentColor.copy(alpha = a) else Color.White.copy(alpha = a * 0.55f),
                        topLeft = Offset(0f, b * bandH),
                        size = Size(w, bandH + 1f)
                    )
                }
            }

            "static_crosshair" -> {
                val c = Offset(w * 0.5f, h * 0.5f)
                val aWhite = (0.18f * opacityMult).coerceIn(0.05f, 0.65f)
                val aAccent = (0.38f * opacityMult).coerceIn(0.08f, 0.80f)
                for (ring in 1..6) {
                    val r = (fullDiag * 0.09f * ring) * scaleMult
                    drawCircle(
                        color = if (ring % 2 == 0) accentColor.copy(alpha = aAccent) else Color.White.copy(alpha = aWhite),
                        radius = r,
                        center = c,
                        style = Stroke(
                            width = 0.9.dp.toPx(),
                            pathEffect = if (ring % 2 == 0) PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f) else null
                        )
                    )
                }
                drawLine(Color.White.copy(alpha = aWhite * 0.8f), Offset(0f, c.y), Offset(w, c.y), strokeWidth = 0.8.dp.toPx())
                drawLine(Color.White.copy(alpha = aWhite * 0.8f), Offset(c.x, 0f), Offset(c.x, h), strokeWidth = 0.8.dp.toPx())
                drawCircle(accentColor.copy(alpha = aAccent), radius = 2.5.dp.toPx(), center = c)
            }

            "static_topo" -> {
                val center = Offset(w * 0.5f, h * 0.5f)
                val rings = 14
                for (i in 1..rings) {
                    val baseR = (fullDiag * 0.042f * i) * scaleMult
                    val path = Path()
                    val steps = 80
                    for (s in 0..steps) {
                        val ang = (s.toFloat() / steps) * 2f * PI.toFloat()
                        val warp = 1f + 0.14f * sin(ang * 3f + i * 0.7f) + 0.08f * cos(ang * 5f - i * 1.1f)
                        val px = center.x + cos(ang) * baseR * warp
                        val py = center.y + sin(ang) * baseR * warp * 1.25f
                        if (s == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    val isIndexContour = i % 3 == 0
                    val alpha = ((if (isIndexContour) 0.30f else 0.15f) * opacityMult).coerceIn(0.04f, 0.75f)
                    drawPath(
                        path = path,
                        color = if (isIndexContour) accentColor.copy(alpha = alpha) else Color.White.copy(alpha = alpha),
                        style = Stroke(width = if (isIndexContour) 1.1.dp.toPx() else 0.75.dp.toPx())
                    )
                }
            }

            "static_isometric" -> {
                val step = (36.dp.toPx() * scaleMult).coerceAtLeast(18.dp.toPx())
                val a = (0.14f * opacityMult).coerceIn(0.04f, 0.55f)
                val tan30 = 0.57735f
                var y = -h
                while (y <= h * 2f) {
                    drawLine(Color.White.copy(alpha = a), Offset(0f, y), Offset(w, y + w * tan30), strokeWidth = 0.8.dp.toPx())
                    drawLine(Color.White.copy(alpha = a), Offset(0f, y + w * tan30), Offset(w, y), strokeWidth = 0.8.dp.toPx())
                    y += step
                }
                var x = 0f
                while (x <= w) {
                    drawLine(accentColor.copy(alpha = a * 0.85f), Offset(x, 0f), Offset(x, h), strokeWidth = 0.7.dp.toPx())
                    x += step * 0.866f
                }
            }

            "static_star_chart" -> {
                val pole = Offset(w * 0.5f, h * 0.45f)
                val aLine = (0.16f * opacityMult).coerceIn(0.04f, 0.58f)
                for (rIdx in 1..10) {
                    drawCircle(
                        color = Color.White.copy(alpha = aLine),
                        radius = fullDiag * 0.065f * rIdx * scaleMult,
                        center = pole,
                        style = Stroke(width = 0.75.dp.toPx())
                    )
                }
                for (angIdx in 0 until 12) {
                    val rad = Math.toRadians((angIdx * 30).toDouble())
                    val rInner = minDim * 0.06f * scaleMult
                    val rOuter = fullDiag * 0.75f
                    drawLine(
                        color = if (angIdx % 3 == 0) accentColor.copy(alpha = aLine * 1.6f) else Color.White.copy(alpha = aLine * 0.8f),
                        start = Offset(pole.x + cos(rad).toFloat() * rInner, pole.y + sin(rad).toFloat() * rInner),
                        end = Offset(pole.x + cos(rad).toFloat() * rOuter, pole.y + sin(rad).toFloat() * rOuter),
                        strokeWidth = 0.75.dp.toPx()
                    )
                }
            }

            "static_eclipse" -> {
                val c = Offset(w * 0.5f, h * 0.46f)
                val r = minDim * 0.28f * scaleMult
                val glowAlpha = (0.32f * opacityMult).coerceIn(0.06f, 0.78f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(alpha = glowAlpha), accentColor.copy(alpha = glowAlpha * 0.18f), Color.Transparent),
                        center = c,
                        radius = fullDiag * 0.55f * scaleMult
                    ),
                    size = Size(w, h)
                )
                drawCircle(Color.Black, radius = r, center = c)
                drawCircle(
                    color = Color.White.copy(alpha = (0.58f * opacityMult).coerceIn(0.12f, 0.95f)),
                    radius = r,
                    center = c,
                    style = Stroke(width = 1.1.dp.toPx())
                )
                for (outer in 2..5) {
                    drawCircle(
                        color = Color.White.copy(alpha = (0.10f * opacityMult).coerceIn(0.03f, 0.35f)),
                        radius = r * (1f + outer * 0.45f),
                        center = c,
                        style = Stroke(width = 0.7.dp.toPx())
                    )
                }
            }

            "static_vinyl" -> {
                val c = Offset(w * 0.5f, h * 0.5f)
                for (i in 2..32) {
                    val r = fullDiag * 0.02f * i * scaleMult
                    val isAccent = i % 7 == 0
                    val a = ((if (isAccent) 0.34f else 0.13f) * opacityMult).coerceIn(0.03f, 0.75f)
                    drawCircle(
                        color = if (isAccent) accentColor.copy(alpha = a) else Color.White.copy(alpha = a),
                        radius = r,
                        center = c,
                        style = Stroke(width = 0.75.dp.toPx())
                    )
                }
            }

            "static_bauhaus" -> {
                val aWhite = (0.20f * opacityMult).coerceIn(0.05f, 0.65f)
                val aAccent = (0.42f * opacityMult).coerceIn(0.08f, 0.85f)
                val xRule1 = w * 0.28f
                val xRule2 = w * 0.76f
                val yRule1 = h * 0.32f
                val yRule2 = h * 0.68f
                drawLine(Color.White.copy(alpha = aWhite), Offset(xRule1, 0f), Offset(xRule1, h), strokeWidth = 1.dp.toPx())
                drawLine(Color.White.copy(alpha = aWhite * 0.6f), Offset(xRule2, 0f), Offset(xRule2, h), strokeWidth = 0.8.dp.toPx())
                drawLine(Color.White.copy(alpha = aWhite), Offset(0f, yRule2), Offset(w, yRule2), strokeWidth = 1.dp.toPx())
                drawLine(Color.White.copy(alpha = aWhite * 0.6f), Offset(0f, yRule1), Offset(w, yRule1), strokeWidth = 0.8.dp.toPx())
                drawCircle(
                    color = accentColor.copy(alpha = aAccent),
                    radius = minDim * 0.22f * scaleMult,
                    center = Offset(xRule1, yRule2),
                    style = Stroke(width = 1.3.dp.toPx())
                )
                drawRect(
                    color = Color.White.copy(alpha = aWhite),
                    topLeft = Offset(xRule1, yRule1),
                    size = Size(xRule2 - xRule1, yRule2 - yRule1),
                    style = Stroke(width = 0.9.dp.toPx())
                )
            }

            "static_horizon_lines" -> {
                val lines = 32
                for (i in 0 until lines) {
                    val f = i.toFloat() / (lines - 1).toFloat()
                    val y = f * h
                    val a = ((0.06f + f * 0.30f) * opacityMult).coerceIn(0.03f, 0.78f)
                    drawLine(
                        color = if (i % 5 == 0) accentColor.copy(alpha = a) else Color.White.copy(alpha = a * 0.8f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 0.85.dp.toPx()
                    )
                }
            }

            "static_glyph_arcs" -> {
                val c = Offset(w * 0.5f, h * 0.5f)
                val aAccent = (0.48f * opacityMult).coerceIn(0.10f, 0.90f)
                val aWhite = (0.28f * opacityMult).coerceIn(0.06f, 0.75f)
                val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                for (ring in 1..3) {
                    val r = minDim * (0.20f * ring) * scaleMult
                    drawArc(accentColor.copy(alpha = aAccent), startAngle = -75f + ring * 20f, sweepAngle = 52f, useCenter = false, topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2f, r * 2f), style = stroke)
                    drawArc(Color.White.copy(alpha = aWhite), startAngle = 25f + ring * 15f, sweepAngle = 68f, useCenter = false, topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2f, r * 2f), style = stroke)
                    drawArc(Color.White.copy(alpha = aWhite), startAngle = 135f + ring * 10f, sweepAngle = 55f, useCenter = false, topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2f, r * 2f), style = stroke)
                    drawArc(accentColor.copy(alpha = aAccent), startAngle = 220f + ring * 15f, sweepAngle = 45f, useCenter = false, topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2f, r * 2f), style = stroke)
                }
                drawLine(Color.White.copy(alpha = aWhite), Offset(c.x, 0f), Offset(c.x, h * 0.14f), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color.White.copy(alpha = aWhite), Offset(c.x, h * 0.86f), Offset(c.x, h), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
            }

            "static_polar" -> {
                val c = Offset(w * 0.5f, h * 0.5f)
                val a = (0.18f * opacityMult).coerceIn(0.04f, 0.65f)
                for (ring in 1..8) {
                    val r = fullDiag * 0.075f * ring * scaleMult
                    drawCircle(
                        color = if (ring % 3 == 0) accentColor.copy(alpha = a * 1.5f) else Color.White.copy(alpha = a),
                        radius = r,
                        center = c,
                        style = Stroke(width = 0.85.dp.toPx())
                    )
                }
                for (deg in 0 until 360 step 15) {
                    val rad = Math.toRadians(deg.toDouble())
                    val innerR = minDim * 0.08f * scaleMult
                    val outerR = fullDiag * 0.65f
                    drawLine(
                        color = if (deg % 90 == 0) accentColor.copy(alpha = a * 1.6f) else Color.White.copy(alpha = a * 0.85f),
                        start = Offset(c.x + cos(rad).toFloat() * innerR, c.y + sin(rad).toFloat() * innerR),
                        end = Offset(c.x + cos(rad).toFloat() * outerR, c.y + sin(rad).toFloat() * outerR),
                        strokeWidth = 0.8.dp.toPx()
                    )
                }
            }

            "static_diagonal_hatch" -> {
                val step = (24.dp.toPx() * scaleMult).coerceAtLeast(12.dp.toPx())
                val a = (0.14f * opacityMult).coerceIn(0.04f, 0.55f)
                var offset = -h
                var idx = 0
                while (offset <= w + h) {
                    val isAccent = idx % 7 == 0
                    drawLine(
                        color = if (isAccent) accentColor.copy(alpha = a * 1.4f) else Color.White.copy(alpha = a),
                        start = Offset(offset, 0f),
                        end = Offset(offset + h, h),
                        strokeWidth = 0.75.dp.toPx()
                    )
                    offset += step
                    idx++
                }
            }

            "static_golden_spiral" -> {
                val aWhite = (0.18f * opacityMult).coerceIn(0.04f, 0.65f)
                val aAccent = (0.38f * opacityMult).coerceIn(0.08f, 0.85f)
                val splitY = h * 0.618f
                val splitX = w * 0.618f
                drawLine(Color.White.copy(alpha = aWhite), Offset(0f, splitY), Offset(w, splitY), strokeWidth = 0.9.dp.toPx())
                drawLine(Color.White.copy(alpha = aWhite), Offset(splitX, splitY), Offset(splitX, h), strokeWidth = 0.9.dp.toPx())
                drawLine(Color.White.copy(alpha = aWhite), Offset(splitX, splitY + (h - splitY) * 0.618f), Offset(w, splitY + (h - splitY) * 0.618f), strokeWidth = 0.9.dp.toPx())
                drawArc(
                    color = accentColor.copy(alpha = aAccent),
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(0f, 0f),
                    size = Size(w * 2f, splitY * 2f),
                    style = Stroke(width = 1.2.dp.toPx())
                )
                drawArc(
                    color = accentColor.copy(alpha = aAccent),
                    startAngle = 270f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(splitX * 2f - w, splitY),
                    size = Size((w - splitX) * 2f, (h - splitY) * 2f),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            "static_sine_interference" -> {
                val waves = 20
                val steps = 64
                for (wIdx in 0 until waves) {
                    val f = wIdx.toFloat() / (waves - 1).toFloat()
                    val baseY = f * h
                    val path = Path()
                    for (s in 0..steps) {
                        val xr = s.toFloat() / steps.toFloat()
                        val x = xr * w
                        val y = baseY + sin(xr * PI.toFloat() * 3f + wIdx * 0.38f) * (minDim * 0.045f * scaleMult)
                        if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    val isAccent = wIdx % 5 == 0
                    val a = ((if (isAccent) 0.36f else 0.15f) * opacityMult).coerceIn(0.04f, 0.75f)
                    drawPath(
                        path = path,
                        color = if (isAccent) accentColor.copy(alpha = a) else Color.White.copy(alpha = a),
                        style = Stroke(width = if (isAccent) 1.1.dp.toPx() else 0.75.dp.toPx())
                    )
                }
            }

            "static_corner_frame" -> {
                val aAccent = (0.45f * opacityMult).coerceIn(0.10f, 0.85f)
                val aWhite = (0.14f * opacityMult).coerceIn(0.04f, 0.55f)
                // Full-screen rule-of-thirds grid lines
                drawLine(Color.White.copy(alpha = aWhite), Offset(w / 3f, 0f), Offset(w / 3f, h), 0.75.dp.toPx())
                drawLine(Color.White.copy(alpha = aWhite), Offset(w * 2f / 3f, 0f), Offset(w * 2f / 3f, h), 0.75.dp.toPx())
                drawLine(Color.White.copy(alpha = aWhite), Offset(0f, h / 3f), Offset(w, h / 3f), 0.75.dp.toPx())
                drawLine(Color.White.copy(alpha = aWhite), Offset(0f, h * 2f / 3f), Offset(w, h * 2f / 3f), 0.75.dp.toPx())

                val padX = 12.dp.toPx()
                val padY = 12.dp.toPx()
                val arm = 26.dp.toPx() * scaleMult
                val sPx = 1.4.dp.toPx()
                drawLine(accentColor.copy(alpha = aAccent), Offset(padX, padY), Offset(padX + arm, padY), sPx)
                drawLine(accentColor.copy(alpha = aAccent), Offset(padX, padY), Offset(padX, padY + arm), sPx)
                drawLine(accentColor.copy(alpha = aAccent), Offset(w - padX, padY), Offset(w - padX - arm, padY), sPx)
                drawLine(accentColor.copy(alpha = aAccent), Offset(w - padX, padY), Offset(w - padX, padY + arm), sPx)
                drawLine(accentColor.copy(alpha = aAccent), Offset(padX, h - padY), Offset(padX + arm, h - padY), sPx)
                drawLine(accentColor.copy(alpha = aAccent), Offset(padX, h - padY), Offset(padX, h - padY - arm), sPx)
                drawLine(accentColor.copy(alpha = aAccent), Offset(w - padX, h - padY), Offset(w - padX - arm, h - padY), sPx)
                drawLine(accentColor.copy(alpha = aAccent), Offset(w - padX, h - padY), Offset(w - padX, h - padY - arm), sPx)
            }

            "static_hex_honeycomb" -> {
                val r = (24.dp.toPx() * scaleMult).coerceAtLeast(14.dp.toPx())
                val dx = r * 1.5f
                val dy = r * sqrt(3f)
                val aWhite = (0.15f * opacityMult).coerceIn(0.04f, 0.55f)
                val aAccent = (0.32f * opacityMult).coerceIn(0.08f, 0.78f)
                var col = 0
                var x = 0f
                while (x <= w + r) {
                    var row = 0
                    var y = if (col % 2 == 0) 0f else dy * 0.5f
                    while (y <= h + dy) {
                        if ((col + row) % 2 == 0) {
                            val hexPath = Path()
                            for (v in 0..5) {
                                val ang = Math.toRadians((v * 60).toDouble())
                                val vx = x + cos(ang).toFloat() * (r * 0.90f)
                                val vy = y + sin(ang).toFloat() * (r * 0.90f)
                                if (v == 0) hexPath.moveTo(vx, vy) else hexPath.lineTo(vx, vy)
                            }
                            hexPath.close()
                            val isHighlight = (col * 3 + row) % 9 == 0
                            drawPath(
                                path = hexPath,
                                color = if (isHighlight) accentColor.copy(alpha = aAccent) else Color.White.copy(alpha = aWhite),
                                style = Stroke(width = 0.8.dp.toPx())
                            )
                        }
                        y += dy
                        row++
                    }
                    x += dx
                    col++
                }
            }

            "static_constellation_fixed" -> {
                val coords = listOf(
                    Offset(0.08f, 0.06f), Offset(0.32f, 0.12f), Offset(0.62f, 0.08f), Offset(0.90f, 0.14f),
                    Offset(0.16f, 0.28f), Offset(0.46f, 0.26f), Offset(0.76f, 0.32f), Offset(0.92f, 0.48f),
                    Offset(0.10f, 0.54f), Offset(0.38f, 0.50f), Offset(0.64f, 0.58f), Offset(0.86f, 0.72f),
                    Offset(0.18f, 0.78f), Offset(0.48f, 0.82f), Offset(0.74f, 0.92f), Offset(0.12f, 0.95f)
                ).map { Offset(it.x * w, it.y * h) }
                val links = listOf(
                    0 to 1, 1 to 2, 2 to 3, 0 to 4, 1 to 5, 2 to 6, 4 to 5, 5 to 6, 6 to 7,
                    4 to 8, 5 to 9, 9 to 10, 10 to 11, 8 to 12, 9 to 13, 12 to 13, 13 to 14, 12 to 15
                )
                val aLine = (0.20f * opacityMult).coerceIn(0.05f, 0.65f)
                for ((i, j) in links) {
                    drawLine(Color.White.copy(alpha = aLine), coords[i], coords[j], strokeWidth = 0.85.dp.toPx())
                }
                coords.forEachIndexed { idx, pt ->
                    val isMajor = idx % 3 == 0
                    drawCircle(
                        color = if (isMajor) accentColor.copy(alpha = (0.75f * opacityMult).coerceIn(0.15f, 0.95f)) else Color.White.copy(alpha = (0.50f * opacityMult).coerceIn(0.10f, 0.85f)),
                        radius = (if (isMajor) 2.5.dp.toPx() else 1.6.dp.toPx()) * scaleMult,
                        center = pt
                    )
                }
            }

            "static_vertical_flutes" -> {
                val count = 18
                val step = w / count.toFloat()
                for (i in 0..count) {
                    val x = i * step
                    val isAccent = i == count / 2
                    val a = ((if (isAccent) 0.32f else 0.13f) * opacityMult).coerceIn(0.03f, 0.72f)
                    drawLine(
                        color = if (isAccent) accentColor.copy(alpha = a) else Color.White.copy(alpha = a),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 0.85.dp.toPx()
                    )
                }
            }

            "static_zen_enso" -> {
                val c = Offset(w * 0.5f, h * 0.5f)
                val r = minDim * 0.34f * scaleMult
                val aAccent = (0.42f * opacityMult).coerceIn(0.08f, 0.85f)
                val aWhite = (0.20f * opacityMult).coerceIn(0.05f, 0.65f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = aAccent * 0.25f), Color.Transparent),
                        center = c,
                        radius = fullDiag * 0.5f * scaleMult
                    ),
                    size = Size(w, h)
                )
                drawArc(
                    color = accentColor.copy(alpha = aAccent),
                    startAngle = -40f,
                    sweepAngle = 295f,
                    useCenter = false,
                    topLeft = Offset(c.x - r, c.y - r),
                    size = Size(r * 2f, r * 2f),
                    style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.White.copy(alpha = aWhite),
                    startAngle = -25f,
                    sweepAngle = 250f,
                    useCenter = false,
                    topLeft = Offset(c.x - r * 0.92f, c.y - r * 0.92f),
                    size = Size(r * 1.84f, r * 1.84f),
                    style = Stroke(width = 0.75.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            "static_micro_plus" -> {
                val step = (32.dp.toPx() * scaleMult).coerceAtLeast(18.dp.toPx())
                val arm = 3.2.dp.toPx()
                val aWhite = (0.20f * opacityMult).coerceIn(0.05f, 0.65f)
                val aAccent = (0.45f * opacityMult).coerceIn(0.10f, 0.85f)
                var row = 0
                var y = (h % step) * 0.5f
                while (y <= h) {
                    var col = 0
                    var x = (w % step) * 0.5f
                    while (x <= w) {
                        val isAccent = (row + col) % 5 == 0
                        val colStroke = if (isAccent) accentColor.copy(alpha = aAccent) else Color.White.copy(alpha = aWhite)
                        drawLine(colStroke, Offset(x - arm, y), Offset(x + arm, y), strokeWidth = 0.85.dp.toPx())
                        drawLine(colStroke, Offset(x, y - arm), Offset(x, y + arm), strokeWidth = 0.85.dp.toPx())
                        x += step
                        col++
                    }
                    y += step
                    row++
                }
            }

            else -> {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = (0.22f * opacityMult).coerceIn(0.05f, 0.65f)), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = fullDiag * 0.55f * scaleMult
                    ),
                    size = Size(w, h)
                )
            }
        }

        // ====================================================================
        // UNIVERSAL WALLPAPER DIMMER OVERLAY
        // Smoothly dims Built-In Live, Built-In Static, Custom Photo & 3rd-Party Wallpapers!
        // ====================================================================
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

// ============================================================================
// PREMIUM, ACCURATE, MINIMALIST RETRO CLOCK, CALENDAR & CRT WORLD MAP WIDGETS
// Zero filler labels · 100% live-accurate · All resizable
// ============================================================================

@Composable
fun RetroSplitFlapClockWidget(
    hourStr: String,
    minStr: String,
    secStr: String,
    amPmStr: String,
    dateStr: String,
    sizeMode: String,
    accentColor: Color,
    lomoFontFamily: FontFamily,
    onOpenClockApp: () -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
            .background(Color(0xFF060606), RoundedCornerShape(10.dp))
            .clickable { onOpenClockApp() }
            .padding(horizontal = 12.dp, vertical = if (sizeMode == "compact") 8.dp else 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SplitFlapMechanicalTile(
                valueText = paddedHour,
                subLabel = "",
                tileHeight = tileHeight,
                fontSize = digitFontSize,
                textColor = Color.White,
                lomoFontFamily = lomoFontFamily,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = ":",
                color = accentColor,
                fontSize = 20.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
            )

            SplitFlapMechanicalTile(
                valueText = paddedMin,
                subLabel = "",
                tileHeight = tileHeight,
                fontSize = digitFontSize,
                textColor = Color.White,
                lomoFontFamily = lomoFontFamily,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = ":",
                color = Color(0xFF454545),
                fontSize = 18.sp,
                fontFamily = lomoFontFamily
            )

            SplitFlapMechanicalTile(
                valueText = paddedSec,
                subLabel = amPmStr,
                tileHeight = tileHeight,
                fontSize = (digitFontSize.value * 0.78f).sp,
                textColor = accentColor,
                lomoFontFamily = lomoFontFamily,
                modifier = Modifier.weight(0.85f)
            )
        }

        if (sizeMode != "compact") {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = dateStr,
                    color = Color(0xFF8A8A8A),
                    fontSize = 9.sp,
                    fontFamily = lomoFontFamily
                )
            }
        }
    }
}

@Composable
private fun SplitFlapMechanicalTile(
    valueText: String,
    subLabel: String,
    tileHeight: androidx.compose.ui.unit.Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    textColor: Color,
    lomoFontFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(tileHeight)
            .border(1.dp, Color(0xFF262626), RoundedCornerShape(7.dp))
            .background(Color(0xFF0D0D0D), RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = valueText,
            color = textColor,
            fontSize = fontSize,
            fontFamily = lomoFontFamily,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.2.dp)
                .background(Color.Black.copy(alpha = 0.9f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(width = 3.dp, height = 6.dp)
                .background(Color(0xFF2C2C2C))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 3.dp, height = 6.dp)
                .background(Color(0xFF2C2C2C))
        )
        if (subLabel.isNotEmpty()) {
            Text(
                text = subLabel,
                color = Color(0xFF737373),
                fontSize = 7.sp,
                fontFamily = lomoFontFamily,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 5.dp, bottom = 2.dp)
            )
        }
    }
}

@Composable
fun RetroAnalogChronoWidget(
    currentTimeMillis: Long,
    hourStr: String,
    minStr: String,
    secStr: String,
    amPmStr: String,
    dateStr: String,
    sizeMode: String,
    accentColor: Color,
    lomoFontFamily: FontFamily,
    onOpenClockApp: () -> Unit
) {
    val dialSize = when (sizeMode) {
        "compact" -> 64.dp
        "large" -> 106.dp
        "xlarge" -> 128.dp
        "max" -> 154.dp
        else -> 84.dp
    }

    // Smooth sub-second live sweep for 100% accurate mechanical/quartz analog hands
    var liveNowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                liveNowMs = System.currentTimeMillis()
            }
        }
    }

    val cal = remember(liveNowMs / 100L) {
        Calendar.getInstance().apply { timeInMillis = liveNowMs }
    }
    val hour12 = cal.get(Calendar.HOUR)
    val minute = cal.get(Calendar.MINUTE)
    val second = cal.get(Calendar.SECOND)
    val millis = cal.get(Calendar.MILLISECOND)
    val exactSec = second + millis / 1000f
    val exactMin = minute + exactSec / 60f
    val exactHour = hour12 + exactMin / 60f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
            .background(Color(0xFF060606), RoundedCornerShape(10.dp))
            .clickable { onOpenClockApp() }
            .padding(horizontal = 14.dp, vertical = if (sizeMode == "compact") 8.dp else 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(dialSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = min(cx, cy) - 2.dp.toPx()

                drawCircle(
                    color = Color(0xFF2C2C2C),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.4.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF0A0A0A),
                    radius = radius - 1.5.dp.toPx(),
                    center = Offset(cx, cy)
                )

                for (i in 0 until 60) {
                    val angleRad = Math.toRadians((i * 6 - 90).toDouble())
                    val isCardinal = i % 15 == 0
                    val isHour = i % 5 == 0
                    val tickLen = when {
                        isCardinal -> radius * 0.19f
                        isHour -> radius * 0.12f
                        else -> radius * 0.05f
                    }
                    val rOuter = radius * 0.92f
                    val rInner = rOuter - tickLen
                    drawLine(
                        color = when {
                            i == 0 -> accentColor
                            isCardinal -> Color.White
                            isHour -> Color(0xFFA3A3A3)
                            else -> Color(0xFF303030)
                        },
                        start = Offset(
                            cx + (rInner * cos(angleRad)).toFloat(),
                            cy + (rInner * sin(angleRad)).toFloat()
                        ),
                        end = Offset(
                            cx + (rOuter * cos(angleRad)).toFloat(),
                            cy + (rOuter * sin(angleRad)).toFloat()
                        ),
                        strokeWidth = if (isHour) 1.5.dp.toPx() else 0.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Hour hand
                val hourAngle = Math.toRadians((exactHour * 30f - 90f).toDouble())
                drawLine(
                    color = Color.White,
                    start = Offset(cx, cy),
                    end = Offset(
                        cx + (radius * 0.50f * cos(hourAngle)).toFloat(),
                        cy + (radius * 0.50f * sin(hourAngle)).toFloat()
                    ),
                    strokeWidth = 2.6.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Minute hand
                val minAngle = Math.toRadians((exactMin * 6f - 90f).toDouble())
                drawLine(
                    color = Color(0xFFE5E5E5),
                    start = Offset(cx, cy),
                    end = Offset(
                        cx + (radius * 0.74f * cos(minAngle)).toFloat(),
                        cy + (radius * 0.74f * sin(minAngle)).toFloat()
                    ),
                    strokeWidth = 1.9.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Smooth sweeping second hand
                val secAngle = Math.toRadians((exactSec * 6f - 90f).toDouble())
                drawLine(
                    color = accentColor,
                    start = Offset(
                        cx - (radius * 0.18f * cos(secAngle)).toFloat(),
                        cy - (radius * 0.18f * sin(secAngle)).toFloat()
                    ),
                    end = Offset(
                        cx + (radius * 0.82f * cos(secAngle)).toFloat(),
                        cy + (radius * 0.82f * sin(secAngle)).toFloat()
                    ),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                drawCircle(color = accentColor, radius = 3.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = Color.Black, radius = 1.1.dp.toPx(), center = Offset(cx, cy))
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "${hourStr.padStart(2, '0')}:${minStr.padStart(2, '0')}:${secStr.padStart(2, '0')}",
                    color = Color.White,
                    fontSize = when (sizeMode) {
                        "xlarge" -> 26.sp
                        "large" -> 22.sp
                        "compact" -> 16.sp
                        else -> 19.sp
                    },
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                if (amPmStr.isNotEmpty()) {
                    Text(
                        text = amPmStr,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontFamily = lomoFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            Text(
                text = dateStr,
                color = Color(0xFF8A8A8A),
                fontSize = 10.sp,
                fontFamily = lomoFontFamily
            )
        }
    }
}

// ============================================================================
// IMPROVED INTERACTIVE PRECISION CHRONOGRAPH WIDGET ("precision_chrono")
// Dual sub-dial vector chronograph (60s sweep + 30m register + 1/10s dial)
// with tactile RUN/PAUSE and LAP/RST controls & 120Hz frame-accurate stopwatch
// ============================================================================
@Composable
fun PrecisionChronographWidget(
    sizeMode: String,
    accentColor: Color,
    lomoFontFamily: FontFamily,
    onTick: () -> Unit
) {
    var running by remember { mutableStateOf(false) }
    var accumulatedMs by remember { mutableStateOf(0L) }
    var startRealtimeMs by remember { mutableStateOf(0L) }
    var displayElapsedMs by remember { mutableStateOf(0L) }
    var lastLapMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(running) {
        if (running) {
            startRealtimeMs = System.currentTimeMillis()
            while (running) {
                withFrameNanos {
                    displayElapsedMs = accumulatedMs + (System.currentTimeMillis() - startRealtimeMs)
                }
            }
        } else {
            displayElapsedMs = accumulatedMs
        }
    }

    val dialSize = when (sizeMode) {
        "compact" -> 66.dp
        "large" -> 112.dp
        "xlarge" -> 136.dp
        else -> 88.dp
    }

    val totalSec = displayElapsedMs / 1000f
    val mins = ((displayElapsedMs / 60000L) % 60L).toInt()
    val secs = ((displayElapsedMs / 1000L) % 60L).toInt()
    val centis = ((displayElapsedMs % 1000L) / 10L).toInt()
    val formattedChrono = String.format(Locale.US, "%02d:%02d.%02d", mins, secs, centis)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF242424), RoundedCornerShape(10.dp))
            .background(Color(0xFF060606), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = if (sizeMode == "compact") 8.dp else 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dual Sub-Dial Precision Vector Chronograph Face
        Box(
            modifier = Modifier
                .size(dialSize)
                .clickable {
                    onTick()
                    if (running) {
                        accumulatedMs += System.currentTimeMillis() - startRealtimeMs
                        running = false
                    } else {
                        startRealtimeMs = System.currentTimeMillis()
                        running = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = min(cx, cy) - 2.dp.toPx()

                // Outer tachymeter bezel
                drawCircle(
                    color = Color(0xFF2E2E2E),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.4.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF0A0A0A),
                    radius = radius - 1.5.dp.toPx(),
                    center = Offset(cx, cy)
                )

                // 60 precision ticks
                for (i in 0 until 60) {
                    val angleRad = Math.toRadians((i * 6 - 90).toDouble())
                    val isFive = i % 5 == 0
                    val tickLen = if (isFive) radius * 0.14f else radius * 0.06f
                    val rOut = radius * 0.92f
                    val rIn = rOut - tickLen
                    drawLine(
                        color = if (i == 0) accentColor else if (isFive) Color.White else Color(0xFF333333),
                        start = Offset(
                            cx + (rIn * cos(angleRad)).toFloat(),
                            cy + (rIn * sin(angleRad)).toFloat()
                        ),
                        end = Offset(
                            cx + (rOut * cos(angleRad)).toFloat(),
                            cy + (rOut * sin(angleRad)).toFloat()
                        ),
                        strokeWidth = if (isFive) 1.4.dp.toPx() else 0.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Upper sub-register (30-minute accumulator)
                val subR = radius * 0.24f
                val topSubCenter = Offset(cx, cy - radius * 0.36f)
                drawCircle(
                    color = Color(0xFF262626),
                    radius = subR,
                    center = topSubCenter,
                    style = Stroke(width = 1.dp.toPx())
                )
                val minSubAngle = Math.toRadians((((totalSec / 60f) % 30f) * 12f - 90f).toDouble())
                drawLine(
                    color = Color.White,
                    start = topSubCenter,
                    end = Offset(
                        topSubCenter.x + (subR * 0.78f * cos(minSubAngle)).toFloat(),
                        topSubCenter.y + (subR * 0.78f * sin(minSubAngle)).toFloat()
                    ),
                    strokeWidth = 1.3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Lower sub-register (1/10s high-speed sweep)
                val botSubCenter = Offset(cx, cy + radius * 0.36f)
                drawCircle(
                    color = Color(0xFF262626),
                    radius = subR,
                    center = botSubCenter,
                    style = Stroke(width = 1.dp.toPx())
                )
                val tenthAngle = Math.toRadians((((displayElapsedMs % 1000L) / 1000f) * 360f - 90f).toDouble())
                drawLine(
                    color = accentColor,
                    start = botSubCenter,
                    end = Offset(
                        botSubCenter.x + (subR * 0.78f * cos(tenthAngle)).toFloat(),
                        botSubCenter.y + (subR * 0.78f * sin(tenthAngle)).toFloat()
                    ),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Main 60s chronograph sweep hand
                val mainSecAngle = Math.toRadians(((totalSec % 60f) * 6f - 90f).toDouble())
                drawLine(
                    color = accentColor,
                    start = Offset(
                        cx - (radius * 0.18f * cos(mainSecAngle)).toFloat(),
                        cy - (radius * 0.18f * sin(mainSecAngle)).toFloat()
                    ),
                    end = Offset(
                        cx + (radius * 0.82f * cos(mainSecAngle)).toFloat(),
                        cy + (radius * 0.82f * sin(mainSecAngle)).toFloat()
                    ),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                drawCircle(color = Color.White, radius = 2.8.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = accentColor, radius = 1.2.dp.toPx(), center = Offset(cx, cy))
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Right side: Digital centisecond readout + Tactile RUN/LAP/RST controls
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = formattedChrono,
                color = Color.White,
                fontSize = when (sizeMode) {
                    "xlarge" -> 26.sp
                    "large" -> 22.sp
                    "compact" -> 16.sp
                    else -> 19.sp
                },
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
            )

            if (lastLapMs != null && sizeMode != "compact") {
                val lm = ((lastLapMs!! / 60000L) % 60L).toInt()
                val ls = ((lastLapMs!! / 1000L) % 60L).toInt()
                val lc = ((lastLapMs!! % 1000L) / 10L).toInt()
                Text(
                    text = String.format(Locale.US, "LAP %02d:%02d.%02d", lm, ls, lc),
                    color = Color(0xFF737373),
                    fontSize = 9.sp,
                    fontFamily = lomoFontFamily
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (running) "PAUSE" else "START",
                    color = if (running) Color.Black else Color.White,
                    fontSize = 9.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            if (running) accentColor else Color(0xFF171717),
                            RoundedCornerShape(5.dp)
                        )
                        .border(
                            1.dp,
                            if (running) accentColor else Color(0xFF2E2E2E),
                            RoundedCornerShape(5.dp)
                        )
                        .clickable {
                            onTick()
                            if (running) {
                                accumulatedMs += System.currentTimeMillis() - startRealtimeMs
                                running = false
                            } else {
                                startRealtimeMs = System.currentTimeMillis()
                                running = true
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )

                Text(
                    text = if (running) "LAP" else "RESET",
                    color = Color(0xFFA3A3A3),
                    fontSize = 9.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .border(1.dp, Color(0xFF282828), RoundedCornerShape(5.dp))
                        .background(Color(0xFF101010), RoundedCornerShape(5.dp))
                        .clickable {
                            onTick()
                            if (running) {
                                lastLapMs = displayElapsedMs
                            } else {
                                accumulatedMs = 0L
                                displayElapsedMs = 0L
                                lastLapMs = null
                            }
                        }
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ============================================================================
// ACCURATE DOT-MATRIX MONTH CALENDAR WIDGET ("retro_calendar")
// Live Calendar.getInstance() · Minimal header · Resizable
// ============================================================================
@Composable
fun RetroDotMatrixCalendarWidget(
    currentTimeMillis: Long,
    sizeMode: String,
    accentColor: Color,
    lomoFontFamily: FontFamily
) {
    val cal = remember(currentTimeMillis / 60_000L) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val todayDay = cal.get(Calendar.DAY_OF_MONTH)
    val monthTitle = remember(currentTimeMillis / 60_000L) {
        SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(cal.time).uppercase(Locale.ENGLISH)
    }

    val (leadingBlanks, daysInMonth) = remember(currentTimeMillis / 60_000L) {
        val temp = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val dow = temp.get(Calendar.DAY_OF_WEEK)
        val mondayOffset = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
        val maxDay = temp.getActualMaximum(Calendar.DAY_OF_MONTH)
        mondayOffset to maxDay
    }

    val totalCells = ((leadingBlanks + daysInMonth + 6) / 7) * 7
    val dayHeaders = listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
            .background(Color(0xFF050505), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = if (sizeMode == "compact") 8.dp else 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthTitle,
                color = Color.White,
                fontSize = if (sizeMode == "large") 13.sp else 11.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${todayDay.toString().padStart(2, '0')} / $daysInMonth",
                color = accentColor,
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEachIndexed { idx, dName ->
                Text(
                    text = dName,
                    color = if (idx >= 5) accentColor.copy(alpha = 0.85f) else Color(0xFF737373),
                    fontSize = 9.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF1A1A1A))
        )
        Spacer(modifier = Modifier.height(6.dp))

        val rows = (0 until totalCells).chunked(7)
        Column(verticalArrangement = Arrangement.spacedBy(if (sizeMode == "large") 6.dp else 3.dp)) {
            rows.forEach { weekCells ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekCells.forEachIndexed { colIdx, cellIndex ->
                        val dayNum = cellIndex - leadingBlanks + 1
                        val isValidDay = dayNum in 1..daysInMonth
                        val isToday = isValidDay && dayNum == todayDay
                        val isWeekend = colIdx >= 5

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(if (sizeMode == "large") 26.dp else if (sizeMode == "compact") 18.dp else 21.dp)
                                .then(
                                    if (isToday) {
                                        Modifier.background(accentColor, RoundedCornerShape(4.dp))
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isValidDay) dayNum.toString().padStart(2, '0') else "··",
                                color = when {
                                    isToday -> Color.Black
                                    !isValidDay -> Color(0xFF222222)
                                    isWeekend -> Color(0xFFA3A3A3)
                                    else -> Color.White
                                },
                                fontSize = if (sizeMode == "large") 11.sp else 10.sp,
                                fontFamily = lomoFontFamily,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 7-DAY TIMELINE STRIP + EXACT MONTH DAYS PROGRESS BLOCKS ("week_strip")
// Renders the 7-day Monday-Sunday strip AND the exact number of days in the
// current month (28/29, 30, or 31) as thin vertical progress blocks!
// ============================================================================
@Composable
fun RetroWeekTimelineStripWidget(
    currentTimeMillis: Long,
    sizeMode: String,
    accentColor: Color,
    lomoFontFamily: FontFamily
) {
    val cal = remember(currentTimeMillis / 60_000L) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val todayDayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    val todayYear = cal.get(Calendar.YEAR)
    val todayDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH) // Exact 28, 29, 30, or 31 days!
    val shortMonth = remember(currentTimeMillis / 60_000L) {
        SimpleDateFormat("MMM", Locale.ENGLISH).format(cal.time).uppercase(Locale.ENGLISH)
    }

    val weekDays = remember(currentTimeMillis / 60_000L) {
        val temp = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val currentDow = temp.get(Calendar.DAY_OF_WEEK)
        val deltaToMonday = if (currentDow == Calendar.SUNDAY) -6 else Calendar.MONDAY - currentDow
        temp.add(Calendar.DAY_OF_MONTH, deltaToMonday)
        val labels = listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU")
        labels.map { lbl ->
            val dNum = temp.get(Calendar.DAY_OF_MONTH)
            val isToday = temp.get(Calendar.YEAR) == todayYear &&
                temp.get(Calendar.DAY_OF_YEAR) == todayDayOfYear
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
        // 7-Day Monday-Sunday Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            weekDays.forEach { (dayLbl, dayNum, isToday) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (isToday) accentColor else Color(0xFF1F1F1F),
                            RoundedCornerShape(6.dp)
                        )
                        .background(
                            if (isToday) accentColor.copy(alpha = 0.16f) else Color(0xFF0C0C0C),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(vertical = if (sizeMode == "large") 8.dp else if (sizeMode == "compact") 4.dp else 6.dp),
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

        Spacer(modifier = Modifier.height(8.dp))

        // Exact Month Days Thin Progress Blocks (28/29, 30, or 31 blocks)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = shortMonth,
                color = Color(0xFF8A8A8A),
                fontSize = 8.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (dayIdx in 1..daysInMonth) {
                    val isPast = dayIdx < todayDayOfMonth
                    val isCurrent = dayIdx == todayDayOfMonth
                    val blockHeight = when {
                        sizeMode == "large" && isCurrent -> 11.dp
                        sizeMode == "large" -> 8.dp
                        isCurrent -> 8.dp
                        else -> 5.dp
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(blockHeight)
                            .background(
                                color = when {
                                    isCurrent -> accentColor
                                    isPast -> Color.White.copy(alpha = 0.80f)
                                    else -> Color(0xFF1D1D1D)
                                },
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }
            }

            Text(
                text = "${todayDayOfMonth.toString().padStart(2, '0')}/$daysInMonth",
                color = accentColor,
                fontSize = 8.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// 365-DAY DOT-MATRIX YEAR PROGRESS WIDGET ("year_matrix")
// Leap-year accurate (365 or 366 dots) · Minimalist · Resizable
// ============================================================================
@Composable
fun RetroYearDotMatrixWidget(
    currentTimeMillis: Long,
    sizeMode: String,
    accentColor: Color,
    lomoFontFamily: FontFamily
) {
    val cal = remember(currentTimeMillis / 60_000L) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val yearNum = cal.get(Calendar.YEAR)
    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    val totalDaysInYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR) // 365 or 366

    val canvasHeight = when (sizeMode) {
        "compact" -> 44.dp
        "large" -> 82.dp
        else -> 62.dp
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = yearNum.toString(),
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$dayOfYear / $totalDaysInYear",
                color = accentColor,
                fontSize = 9.sp,
                fontFamily = lomoFontFamily,
                fontWeight = FontWeight.Bold
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
