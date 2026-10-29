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
import androidx.compose.ui.graphics.ImageBitmap
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

    // Navigation & Edit Mode State
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

    // 100% Offline Essential Widgets (Clock, Week Strip, Calendar, Chronograph, Globe, CRT World Map, etc.)
    var activeWidgets by remember {
        val saved = prefs.getString("widgets_order_v7", "clock,week_strip,globe") ?: "clock,week_strip,globe"
        mutableStateOf(
            saved.split(",").filter {
                it.isNotBlank() && it != "telemetry" && it != "blackhole" && it != "weather" && it != "pedometer"
            }
        )
    }
    var widgetSizes by remember {
        val saved = prefs.getString(
            "widget_sizes_v7",
            "clock:standard,week_strip:standard,retro_calendar:standard,year_matrix:standard,retro_flip_clock:standard,analog_chrono:standard,precision_chrono:standard,globe:standard,crt_world_map:standard,torch:standard,storage:standard,quote:standard,scratchpad:standard,zen:standard"
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
            it != "blackhole" && it != "telemetry" && it != "weather" && it != "pedometer"
        }
        activeWidgets = cleaned
        prefs.edit().putString("widgets_order_v7", cleaned.joinToString(",")).apply()
    }

    fun saveWidgetSize(widgetId: String, size: String) {
        val newMap = widgetSizes.toMutableMap()
        newMap[widgetId] = size
        widgetSizes = newMap.toMap()
        val serialized = newMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        prefs.edit().putString("widget_sizes_v7", serialized).apply()
    }

    fun cycleWidgetSize(widgetId: String) {
        val current = widgetSizes[widgetId] ?: "standard"
        val next = if (widgetId == "globe" || widgetId == "crt_world_map") {
            when (current) {
                "compact" -> "standard"
                "standard" -> "large"
                "large" -> "xlarge"
                "xlarge" -> "max"
                else -> "compact"
            }
        } else if (widgetId == "precision_chrono" || widgetId == "analog_chrono" || widgetId == "retro_flip_clock") {
            when (current) {
                "compact" -> "standard"
                "standard" -> "large"
                "large" -> "xlarge"
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

        // Full-Screen Edge-to-Edge Wallpaper Canvas (Zero inset gaps; dims Live, Static & Third-Party Wallpapers)
        OledWallpaperCanvas(
            preset = wallpaperPreset,
            accentColor = accentColor,
            dimPercent = wallpaperDimPercent,
            wpSpeed = wpSpeed,
            wpDensity = wpDensity,
            wpOpacity = wpOpacity,
            wpScale = wpScale,
            userWallpaperRefreshTrigger = userWallpaperRefreshTrigger
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
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
                                .pointerInput(screenHeightPx) {
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
                                    pinnedApps = installedApps.filter { it.isPinned && !it.isHidden },
                                    onToggle24h = {
                                        use24Hour = !use24Hour
                                        prefs.edit().putBoolean("use_24h_v4", use24Hour).apply()
                                    },
                                    onToggleTorch = { toggleTorchState() },
                                    onToggleZen = {
                                        zenActive = !zenActive
                                        if (!zenActive) zenSeconds = 1500
                                    },
                                    onCycleWidgetSize = { id -> cycleWidgetSize(id) },
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
                                lomoFontFamily = lomoFontFamily,
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
                            lomoFontFamily = lomoFontFamily,
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
                            lomoFontFamily = lomoFontFamily,
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
                            fontFamily = lomoFontFamily,
                            fontWeight = FontWeight.Bold
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
                        lomoFontFamily = lomoFontFamily,
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
                        lomoFontFamily = lomoFontFamily,
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
                        lomoFontFamily = lomoFontFamily,
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
                        lomoFontFamily = lomoFontFamily,
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
    widgetSizes: Map<String, String>,
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
    pinnedApps: List<AppEntry>,
    onToggle24h: () -> Unit,
    onToggleTorch: () -> Unit,
    onToggleZen: () -> Unit,
    onCycleWidgetSize: (String) -> Unit,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. MODULAR OLED WIDGETS STACK (Relocatable, Resizable: Compact/Standard/Large, Removable)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            activeWidgets.forEach { widgetId ->
                val sizeMode = widgetSizes[widgetId] ?: "standard"
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
                                                modifier = Modifier.clickable { onToggle24h() }
                                            ) {
                                                Text(
                                                    text = "$currentHourStr:$currentMinStr $currentAmPmStr".trim(),
                                                    color = Color.White,
                                                    fontSize = 24.sp,
                                                    fontFamily = lomoFontFamily
                                                )
                                                Text(
                                                    text = "·",
                                                    color = accentColor,
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Text(
                                                    text = "$currentDateStr · $batteryPct%",
                                                    color = Color(0xFFA3A3A3),
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        "large" -> {
                                            Row(
                                                verticalAlignment = Alignment.Bottom,
                                                modifier = Modifier.clickable { onToggle24h() }
                                            ) {
                                                Text(
                                                    text = currentHourStr,
                                                    color = Color.White,
                                                    fontSize = 58.sp,
                                                    fontFamily = lomoFontFamily
                                                )
                                                Text(
                                                    text = ":",
                                                    color = accentColor.copy(alpha = colonAlpha),
                                                    fontSize = 58.sp,
                                                    fontFamily = lomoFontFamily
                                                )
                                                Text(
                                                    text = currentMinStr,
                                                    color = Color.White,
                                                    fontSize = 58.sp,
                                                    fontFamily = lomoFontFamily
                                                )
                                                if (currentAmPmStr.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = currentAmPmStr,
                                                        color = Color(0xFF737373),
                                                        fontSize = 13.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        modifier = Modifier.padding(bottom = 7.dp)
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
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    letterSpacing = 1.2.sp
                                                )
                                                if (nextAlarmStr.isNotBlank()) {
                                                    Text(
                                                        text = "·",
                                                        color = accentColor,
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = nextAlarmStr,
                                                        color = Color(0xFF737373),
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
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
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    letterSpacing = 0.8.sp
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(8.dp)
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
                                                        fontSize = 10.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                        }
                                        else -> {
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
                                                if (nextAlarmStr.isNotBlank()) {
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
                                                if (isCharging) {
                                                    Text(
                                                        text = "⚡",
                                                        color = accentColor,
                                                        fontSize = 9.sp,
                                                        fontFamily = FontFamily.Monospace
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
                                    lomoFontFamily = lomoFontFamily,
                                    onToggle24h = onToggle24h
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
                                    lomoFontFamily = lomoFontFamily,
                                    onToggle24h = onToggle24h
                                )
                            }
                            "precision_chrono" -> {
                                PrecisionChronographWidget(
                                    sizeMode = sizeMode,
                                    accentColor = accentColor,
                                    lomoFontFamily = lomoFontFamily,
                                    onTick = onWaveTick
                                )
                            }
                            "retro_calendar" -> {
                                RetroDotMatrixCalendarWidget(
                                    currentTimeMillis = currentTimeMillis,
                                    sizeMode = sizeMode,
                                    accentColor = accentColor,
                                    lomoFontFamily = lomoFontFamily
                                )
                            }
                            "week_strip" -> {
                                RetroWeekTimelineStripWidget(
                                    currentTimeMillis = currentTimeMillis,
                                    sizeMode = sizeMode,
                                    accentColor = accentColor,
                                    lomoFontFamily = lomoFontFamily
                                )
                            }
                            "year_matrix" -> {
                                RetroYearDotMatrixWidget(
                                    currentTimeMillis = currentTimeMillis,
                                    sizeMode = sizeMode,
                                    accentColor = accentColor,
                                    lomoFontFamily = lomoFontFamily
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
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = if (torchEnabled) "ON" else "OFF",
                                        color = if (torchEnabled) accentColor else Color(0xFF737373),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
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
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = storageStatusText,
                                            color = Color(0xFFA3A3A3),
                                            fontSize = if (sizeMode == "large") 11.sp else 10.sp,
                                            fontFamily = FontFamily.Monospace
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
                                        fontFamily = lomoFontFamily,
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
                                        fontFamily = lomoFontFamily,
                                        fontWeight = FontWeight.Bold
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
                                        fontFamily = FontFamily.Monospace
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
                                        onTick = onWaveTick
                                    )
                                }
                            }
                            "crt_world_map" -> {
                                val mapHeight = when (sizeMode) {
                                    "compact" -> 115.dp
                                    "large" -> 215.dp
                                    "xlarge" -> 265.dp
                                    "max" -> 320.dp
                                    else -> 165.dp
                                }
                                RetroCrtFlatWorldMapWidget(
                                    currentTimeMillis = currentTimeMillis,
                                    mapHeight = mapHeight,
                                    globeTheme = globeTheme,
                                    globeWireframeColor = globeWireframeColor,
                                    accentColor = accentColor,
                                    onTick = onWaveTick
                                )
                            }
                        }

                        // Edit Mode Controls for Relocating, Resizing & Removing Widgets
                        if (editMode) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val idx = activeWidgets.indexOf(widgetId)
                                    if (idx > 0) {
                                        Text(
                                            text = "↑ UP",
                                            color = Color(0xFFA3A3A3),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier
                                                .border(1.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
                                                .clickable { onMoveWidget(widgetId, -1) }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (idx < activeWidgets.lastIndex) {
                                        Text(
                                            text = "↓ DOWN",
                                            color = Color(0xFFA3A3A3),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier
                                                .border(1.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
                                                .clickable { onMoveWidget(widgetId, 1) }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "SIZE: ${sizeMode.uppercase()}",
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            .clickable { onCycleWidgetSize(widgetId) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "× REMOVE",
                                    color = Color(0xFFEF4444),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .clickable { onRemoveWidget(widgetId) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. FLEXIBLE OLED SPACER (Keeps Pinned Apps anchored at the bottom while widgets are modular at the top/middle)
        Spacer(modifier = Modifier.weight(1f))

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
// HIGH-PRECISION 3D INTERACTIVE GLOBE (106 COUNTRY BORDERS · 2 THEMES)
// Theme 1 ("retro_holo"): Translucent Blue Ocean + Translucent Green Countries + Glowing Mint Borders
// Theme 2 ("wireframe"): Pure 3D Vector Wireframe with User-Customizable Color
// Pure floating globe with zero surrounding text or shapes, larger radius,
// slow autonomous spin, 360-degree omnidirectional finger spin, and spring posture recovery.
// ============================================================================
private val WORLD_COUNTRY_BORDERS: List<List<Pair<Float, Float>>> = listOf(
    listOf(
        49.0f to -123.1f, 49.0f to -119.6f, 49.0f to -116.1f, 49.0f to -112.6f, 49.0f to -109.2f, 49.0f to -105.7f, 49.0f to -102.2f, 49.0f to -98.7f,
        49.0f to -95.2f, 48.5f to -92.3f, 48.0f to -89.5f, 47.2f to -86.9f, 46.5f to -84.3f, 45.0f to -82.5f, 42.3f to -83.0f, 42.6f to -81.0f,
        42.9f to -78.9f, 44.0f to -76.8f, 45.0f to -74.7f, 45.0f to -71.5f, 47.4f to -69.2f, 45.1f to -67.1f, 44.9f to -65.3f, 44.6f to -63.5f,
        45.8f to -62.0f, 46.9f to -60.4f, 49.5f to -58.1f, 52.0f to -55.8f, 54.2f to -58.1f, 56.3f to -60.5f, 58.5f to -62.8f, 60.9f to -64.8f,
        59.8f to -66.6f, 58.8f to -68.4f, 60.0f to -71.5f, 61.3f to -74.7f, 62.5f to -77.8f, 60.1f to -79.3f, 57.6f to -80.8f, 55.2f to -82.3f,
        55.8f to -85.6f, 56.5f to -89.0f, 57.1f to -92.3f, 59.4f to -92.9f, 61.8f to -93.4f, 64.1f to -94.0f, 66.3f to -95.2f, 68.5f to -96.5f,
        68.7f to -99.6f, 68.8f to -102.7f, 69.0f to -105.8f, 69.2f to -108.8f, 69.3f to -111.9f, 69.5f to -115.0f, 69.6f to -117.8f, 69.7f to -120.6f,
        69.9f to -123.4f, 70.0f to -126.2f, 70.1f to -129.0f, 70.0f to -132.0f, 69.8f to -135.0f, 69.7f to -138.0f, 69.6f to -141.0f, 66.5f to -141.0f,
        63.4f to -141.0f, 60.3f to -141.0f, 58.9f to -138.4f, 57.5f to -135.8f, 56.1f to -133.2f, 54.7f to -130.6f, 52.8f to -129.5f, 50.8f to -128.4f,
        49.9f to -125.8f, 49.0f to -123.1f
    ),
    listOf(
        62.0f to -66.0f, 64.2f to -63.8f, 66.5f to -61.5f, 67.8f to -64.2f, 69.0f to -67.0f, 70.2f to -69.8f, 71.5f to -72.5f, 72.0f to -75.4f,
        72.6f to -78.2f, 73.2f to -81.1f, 73.7f to -84.0f, 71.8f to -85.0f, 70.0f to -86.0f, 68.3f to -83.3f, 66.7f to -80.7f, 65.0f to -78.0f,
        64.2f to -75.0f, 63.5f to -72.0f, 62.8f to -69.0f, 62.0f to -66.0f
    ),
    listOf(
        49.0f to -123.1f, 48.4f to -124.7f, 46.2f to -124.0f, 44.1f to -124.1f, 42.0f to -124.2f, 40.0f to -123.6f, 38.0f to -123.0f, 36.2f to -121.8f,
        34.5f to -120.5f, 33.5f to -118.8f, 32.5f to -117.1f, 31.9f to -114.0f, 31.3f to -111.0f, 31.6f to -108.8f, 31.8f to -106.5f, 30.4f to -104.8f,
        29.0f to -103.1f, 27.5f to -100.2f, 26.0f to -97.2f, 27.8f to -97.1f, 28.8f to -95.4f, 29.7f to -93.8f, 29.4f to -91.5f, 29.1f to -89.2f,
        30.2f to -88.0f, 30.1f to -86.0f, 30.0f to -84.0f, 28.0f to -82.8f, 26.6f to -81.6f, 25.1f to -80.4f, 28.5f to -80.6f, 30.7f to -81.4f,
        32.8f to -79.9f, 34.0f to -77.7f, 35.2f to -75.5f, 37.0f to -76.0f, 39.5f to -74.3f, 40.6f to -74.0f, 41.3f to -71.8f, 42.0f to -70.0f,
        43.6f to -70.2f, 44.2f to -68.6f, 44.8f to -66.9f, 47.4f to -69.2f, 45.0f to -71.5f, 45.0f to -74.7f, 44.0f to -76.8f, 42.9f to -78.9f,
        42.6f to -81.0f, 42.3f to -83.0f, 45.0f to -82.5f, 46.5f to -84.3f, 47.2f to -86.9f, 48.0f to -89.5f, 48.5f to -92.3f, 49.0f to -95.2f,
        49.0f to -98.7f, 49.0f to -102.2f, 49.0f to -105.7f, 49.0f to -109.2f, 49.0f to -112.6f, 49.0f to -116.1f, 49.0f to -119.6f, 49.0f to -123.1f
    ),
    listOf(
        60.3f to -141.0f, 63.4f to -141.0f, 66.5f to -141.0f, 69.6f to -141.0f, 69.9f to -144.2f, 70.3f to -147.3f, 70.6f to -150.5f, 71.0f to -153.6f,
        71.3f to -156.8f, 70.5f to -159.9f, 69.7f to -163.1f, 68.9f to -166.2f, 67.2f to -167.1f, 65.6f to -168.1f, 64.5f to -165.1f, 63.5f to -162.0f,
        61.8f to -163.8f, 60.0f to -165.6f, 59.3f to -163.0f, 58.7f to -160.3f, 58.0f to -157.7f, 56.4f to -160.6f, 54.8f to -163.4f, 55.8f to -160.7f,
        56.7f to -157.9f, 57.6f to -155.2f, 58.6f to -152.5f, 59.5f to -149.2f, 60.5f to -146.0f, 59.8f to -142.6f, 59.1f to -139.2f, 58.4f to -135.8f,
        56.5f to -133.2f, 54.7f to -130.6f, 56.1f to -133.2f, 57.5f to -135.8f, 58.9f to -138.4f, 60.3f to -141.0f
    ),
    listOf(
        32.5f to -117.1f, 30.1f to -116.0f, 27.8f to -114.9f, 26.2f to -113.2f, 24.5f to -111.6f, 22.9f to -109.9f, 24.7f to -110.7f, 26.5f to -111.5f,
        28.9f to -112.5f, 31.2f to -113.5f, 29.6f to -112.2f, 28.0f to -111.0f, 25.6f to -108.7f, 23.2f to -106.4f, 21.1f to -105.3f, 19.0f to -104.3f,
        17.9f to -102.1f, 16.9f to -99.9f, 16.4f to -98.2f, 15.8f to -96.5f, 15.2f to -94.3f, 14.5f to -92.2f, 16.1f to -91.6f, 17.8f to -91.0f,
        17.8f to -89.2f, 19.6f to -88.2f, 21.5f to -87.1f, 21.2f to -90.0f, 18.6f to -91.8f, 18.2f to -94.4f, 19.9f to -95.9f, 21.5f to -97.4f,
        23.8f to -97.3f, 26.0f to -97.2f, 27.5f to -100.2f, 29.0f to -103.1f, 30.4f to -104.8f, 31.8f to -106.5f, 31.6f to -108.8f, 31.3f to -111.0f,
        31.9f to -114.0f, 32.5f to -117.1f
    ),
    listOf(
        17.8f to -89.2f, 15.9f to -88.6f, 15.9f to -85.8f, 15.9f to -83.1f, 13.4f to -83.4f, 11.0f to -83.7f, 9.7f to -80.6f, 8.3f to -77.4f,
        7.2f to -77.9f, 7.7f to -80.4f, 8.1f to -82.9f, 9.5f to -84.3f, 10.9f to -85.7f, 13.0f to -87.6f, 13.8f to -89.9f, 14.5f to -92.2f,
        16.1f to -91.6f, 17.8f to -91.0f, 17.8f to -89.2f
    ),
    listOf(
        21.9f to -84.9f, 23.1f to -81.8f, 22.3f to -79.4f, 21.5f to -77.0f, 19.8f to -74.1f, 19.9f to -75.9f, 19.9f to -77.7f, 21.6f to -80.0f,
        21.8f to -82.5f, 21.9f to -84.9f
    ),
    listOf(
        19.9f to -73.4f, 19.8f to -71.7f, 19.7f to -69.9f, 18.4f to -68.4f, 18.0f to -71.7f, 18.4f to -74.4f, 19.9f to -73.4f
    ),
    listOf(
        60.0f to -43.5f, 61.9f to -41.4f, 63.7f to -39.3f, 65.6f to -37.2f, 66.6f to -34.2f, 67.5f to -31.1f, 68.5f to -28.1f, 69.4f to -25.0f,
        70.4f to -22.0f, 72.5f to -20.9f, 74.7f to -19.9f, 76.8f to -18.8f, 78.5f to -16.7f, 80.1f to -14.6f, 81.8f to -12.5f, 82.1f to -15.8f,
        82.4f to -19.0f, 82.7f to -22.2f, 83.0f to -25.5f, 83.3f to -28.8f, 83.6f to -32.0f, 83.3f to -35.3f, 83.1f to -38.7f, 82.8f to -42.0f,
        82.5f to -45.3f, 82.3f to -48.7f, 82.0f to -52.0f, 81.7f to -55.3f, 81.5f to -58.7f, 81.2f to -62.0f, 79.6f to -64.7f, 77.9f to -67.3f,
        76.3f to -70.0f, 75.6f to -67.2f, 74.9f to -64.4f, 74.2f to -61.6f, 73.5f to -58.8f, 72.8f to -56.0f, 70.4f to -54.8f, 68.0f to -53.5f,
        65.9f to -52.5f, 63.8f to -51.5f, 62.5f to -48.8f, 61.3f to -46.2f, 60.0f to -43.5f
    ),
    listOf(
        8.3f to -77.4f, 9.8f to -75.8f, 11.2f to -74.2f, 12.4f to -71.6f, 9.7f to -71.2f, 7.0f to -70.7f, 4.3f to -68.9f, 1.7f to -67.1f,
        -1.3f to -68.5f, -4.2f to -69.9f, -2.3f to -72.6f, -0.4f to -75.2f, 0.5f to -77.0f, 1.4f to -78.8f, 4.0f to -77.3f, 6.2f to -77.3f,
        8.3f to -77.4f
    ),
    listOf(
        12.4f to -71.6f, 11.5f to -69.2f, 10.6f to -66.9f, 10.6f to -64.2f, 10.6f to -61.6f, 8.4f to -59.8f, 7.2f to -57.5f, 6.0f to -55.2f,
        5.2f to -53.4f, 4.3f to -51.6f, 2.2f to -52.9f, 2.0f to -55.3f, 1.7f to -57.6f, 1.5f to -60.0f, 4.5f to -61.0f, 3.1f to -64.0f,
        1.7f to -67.1f, 4.3f to -68.9f, 7.0f to -70.7f, 9.7f to -71.2f, 12.4f to -71.6f
    ),
    listOf(
        1.4f to -78.8f, 0.5f to -77.0f, -0.4f to -75.2f, -2.3f to -72.6f, -4.2f to -69.9f, -5.8f to -71.9f, -7.4f to -73.9f, -9.2f to -71.7f,
        -11.0f to -69.5f, -13.8f to -69.2f, -16.5f to -69.0f, -18.3f to -70.4f, -16.9f to -72.4f, -15.4f to -74.3f, -14.0f to -76.3f, -11.1f to -77.7f,
        -8.1f to -79.1f, -6.4f to -80.2f, -4.7f to -81.3f, -2.2f to -81.0f, -0.4f to -79.9f, 1.4f to -78.8f
    ),
    listOf(
        4.3f to -51.6f, 2.1f to -50.5f, 0.0f to -49.5f, -1.2f to -46.9f, -2.5f to -44.3f, -3.1f to -41.4f, -3.7f to -38.5f, -4.8f to -36.9f,
        -5.8f to -35.2f, -8.0f to -34.9f, -10.5f to -36.7f, -13.0f to -38.5f, -15.5f to -39.0f, -18.0f to -39.5f, -20.5f to -41.4f, -23.0f to -43.2f,
        -24.2f to -45.9f, -25.5f to -48.5f, -27.8f to -49.4f, -30.0f to -50.2f, -31.9f to -51.8f, -33.7f to -53.4f, -32.0f to -55.5f, -30.2f to -57.6f,
        -27.9f to -56.1f, -25.6f to -54.6f, -23.9f to -56.3f, -22.1f to -58.0f, -20.1f to -57.8f, -18.0f to -57.6f, -15.8f to -59.0f, -13.6f to -60.4f,
        -12.2f to -62.9f, -10.8f to -65.4f, -10.9f to -67.5f, -11.0f to -69.5f, -9.2f to -71.7f, -7.4f to -73.9f, -5.8f to -71.9f, -4.2f to -69.9f,
        -1.2f to -68.5f, 1.7f to -67.1f, 3.1f to -64.0f, 4.5f to -61.0f, 1.5f to -60.0f, 1.7f to -57.6f, 2.0f to -55.3f, 2.2f to -52.9f,
        4.3f to -51.6f
    ),
    listOf(
        -11.0f to -69.5f, -10.9f to -67.5f, -10.8f to -65.4f, -12.2f to -62.9f, -13.6f to -60.4f, -15.8f to -59.0f, -18.0f to -57.6f, -20.1f to -57.8f,
        -22.1f to -58.0f, -23.9f to -56.3f, -25.6f to -54.6f, -26.5f to -56.6f, -27.3f to -58.6f, -25.1f to -60.7f, -22.9f to -62.7f, -22.9f to -65.2f,
        -22.9f to -67.8f, -19.7f to -68.4f, -16.5f to -69.0f, -13.8f to -69.2f, -11.0f to -69.5f
    ),
    listOf(
        -18.3f to -70.4f, -16.5f to -69.0f, -19.7f to -68.4f, -22.9f to -67.8f, -24.9f to -68.2f, -27.0f to -68.5f, -30.0f to -69.2f, -33.0f to -70.0f,
        -35.7f to -70.6f, -38.3f to -71.2f, -41.0f to -71.8f, -43.7f to -72.2f, -46.3f to -72.6f, -49.0f to -73.0f, -51.0f to -71.2f, -52.9f to -69.3f,
        -54.9f to -67.5f, -54.4f to -70.0f, -53.9f to -72.4f, -51.5f to -73.5f, -49.2f to -74.5f, -46.8f to -75.6f, -44.1f to -74.7f, -41.5f to -73.8f,
        -38.7f to -73.1f, -35.8f to -72.3f, -33.0f to -71.6f, -29.9f to -71.2f, -26.7f to -70.8f, -23.6f to -70.4f, -21.0f to -70.4f, -18.3f to -70.4f
    ),
    listOf(
        -22.9f to -67.8f, -22.9f to -65.2f, -22.9f to -62.7f, -25.1f to -60.7f, -27.3f to -58.6f, -26.5f to -56.6f, -25.6f to -54.6f, -27.9f to -56.1f,
        -30.2f to -57.6f, -32.0f to -55.5f, -33.7f to -53.4f, -35.0f to -54.9f, -34.9f to -57.9f, -38.0f to -57.5f, -39.5f to -60.1f, -41.0f to -62.8f,
        -43.5f to -65.2f, -46.0f to -67.5f, -49.2f to -68.0f, -52.4f to -68.4f, -54.9f to -67.5f, -52.9f to -69.3f, -51.0f to -71.2f, -49.0f to -73.0f,
        -46.3f to -72.6f, -43.7f to -72.2f, -41.0f to -71.8f, -38.3f to -71.2f, -35.7f to -70.6f, -33.0f to -70.0f, -30.0f to -69.2f, -27.0f to -68.5f,
        -24.9f to -68.2f, -22.9f to -67.8f
    ),
    listOf(
        50.1f to -5.2f, 50.5f to -2.5f, 50.8f to 0.2f, 51.4f to 1.4f, 52.7f to 1.7f, 53.6f to 0.1f, 55.8f to -2.1f, 57.7f to -1.8f,
        58.6f to -3.1f, 58.6f to -5.0f, 56.5f to -5.6f, 54.8f to -5.0f, 53.4f to -3.0f, 51.7f to -5.1f, 50.1f to -5.2f
    ),
    listOf(
        51.5f to -9.8f, 52.2f to -6.4f, 54.5f to -5.5f, 55.4f to -7.4f, 53.5f to -10.1f, 51.5f to -9.8f
    ),
    listOf(
        63.4f to -19.0f, 63.9f to -16.8f, 64.4f to -14.5f, 66.5f to -16.0f, 66.2f to -18.7f, 65.9f to -21.5f, 65.6f to -24.2f, 64.5f to -21.6f,
        63.4f to -19.0f
    ),
    listOf(
        36.0f to -5.6f, 37.0f to -8.9f, 38.8f to -9.5f, 41.9f to -8.9f, 43.0f to -9.3f, 43.2f to -6.2f, 43.4f to -3.0f, 43.3f to -1.8f,
        42.8f to 0.7f, 42.4f to 3.2f, 40.7f to 0.8f, 38.7f to 0.2f, 36.7f to -2.2f, 36.0f to -5.6f
    ),
    listOf(
        37.0f to -8.9f, 37.2f to -7.4f, 39.5f to -7.0f, 41.9f to -6.6f, 41.9f to -8.9f, 38.8f to -9.5f, 37.0f to -8.9f
    ),
    listOf(
        43.3f to -1.8f, 46.0f to -1.2f, 47.2f to -3.0f, 48.4f to -4.8f, 48.7f to -1.8f, 49.7f to -1.9f, 50.4f to 0.3f, 51.0f to 2.5f,
        50.0f to 5.3f, 49.0f to 8.2f, 47.6f to 7.6f, 45.8f to 6.8f, 43.8f to 7.5f, 43.1f to 5.9f, 43.3f to 3.5f, 42.4f to 3.2f,
        42.8f to 0.7f, 43.3f to -1.8f
    ),
    listOf(
        51.0f to 2.5f, 52.2f to 4.0f, 53.4f to 5.5f, 53.9f to 8.7f, 54.8f to 8.4f, 54.4f to 11.0f, 53.9f to 14.2f, 51.0f to 15.0f,
        49.0f to 13.8f, 47.6f to 13.0f, 46.8f to 11.0f, 46.0f to 9.0f, 46.2f to 6.1f, 47.6f to 7.6f, 49.0f to 8.2f, 50.0f to 5.3f,
        51.0f to 2.5f
    ),
    listOf(
        43.8f to 7.5f, 45.8f to 6.8f, 46.0f to 9.0f, 47.0f to 12.2f, 45.6f to 13.8f, 44.4f to 12.3f, 43.2f to 13.7f, 42.0f to 15.0f,
        41.0f to 16.8f, 40.1f to 18.5f, 39.8f to 16.5f, 38.0f to 15.6f, 39.0f to 16.0f, 40.8f to 14.2f, 41.8f to 12.2f, 43.1f to 10.6f,
        44.4f to 8.9f, 43.8f to 7.5f
    ),
    listOf(
        58.0f to 7.0f, 60.0f to 6.0f, 62.0f to 5.0f, 63.0f to 7.5f, 64.0f to 10.0f, 66.0f to 12.2f, 68.0f to 14.5f, 68.8f to 17.3f,
        69.5f to 20.1f, 70.3f to 23.0f, 71.1f to 25.8f, 70.5f to 27.8f, 69.9f to 29.7f, 69.6f to 26.7f, 69.3f to 23.8f, 69.0f to 20.8f,
        67.3f to 18.5f, 65.7f to 16.3f, 64.0f to 14.0f, 61.5f to 12.6f, 59.0f to 11.2f, 58.5f to 9.1f, 58.0f to 7.0f
    ),
    listOf(
        59.0f to 11.2f, 61.5f to 12.6f, 64.0f to 14.0f, 65.7f to 16.3f, 67.3f to 18.5f, 69.0f to 20.8f, 67.4f to 22.5f, 65.8f to 24.1f,
        64.7f to 22.1f, 63.5f to 20.0f, 61.9f to 19.4f, 60.2f to 18.8f, 58.1f to 17.4f, 56.0f to 16.0f, 55.4f to 13.0f, 57.7f to 11.9f,
        59.0f to 11.2f
    ),
    listOf(
        65.8f to 24.1f, 67.4f to 22.5f, 69.0f to 20.8f, 69.3f to 23.8f, 69.6f to 26.7f, 69.9f to 29.7f, 67.5f to 29.8f, 65.0f to 29.8f,
        62.8f to 28.8f, 60.5f to 27.8f, 60.1f to 25.4f, 59.8f to 23.0f, 61.5f to 21.5f, 63.6f to 22.8f, 65.8f to 24.1f
    ),
    listOf(
        53.9f to 14.2f, 54.3f to 16.3f, 54.8f to 18.4f, 55.9f to 21.1f, 57.0f to 22.8f, 58.0f to 24.4f, 59.4f to 24.7f, 59.4f to 28.0f,
        56.0f to 28.2f, 55.0f to 25.9f, 53.9f to 23.5f, 50.5f to 24.0f, 49.0f to 22.5f, 49.2f to 20.6f, 49.5f to 18.8f, 49.2f to 16.3f,
        49.0f to 13.8f, 51.0f to 15.0f, 53.9f to 14.2f
    ),
    listOf(
        53.9f to 23.5f, 55.0f to 25.9f, 56.0f to 28.2f, 54.8f to 30.4f, 53.5f to 32.5f, 52.1f to 34.0f, 51.0f to 36.1f, 49.8f to 38.2f,
        47.1f to 39.2f, 46.5f to 37.1f, 46.0f to 35.0f, 44.4f to 33.8f, 45.5f to 32.2f, 46.5f to 30.7f, 45.3f to 29.7f, 46.2f to 27.5f,
        47.1f to 25.2f, 48.0f to 23.0f, 50.5f to 24.0f, 53.9f to 23.5f
    ),
    listOf(
        45.6f to 13.8f, 47.0f to 17.0f, 47.5f to 20.0f, 48.0f to 23.0f, 47.1f to 25.2f, 46.2f to 27.5f, 45.3f to 29.7f, 43.6f to 28.9f,
        42.0f to 28.0f, 40.8f to 26.0f, 40.5f to 23.0f, 38.0f to 24.0f, 36.4f to 22.5f, 37.7f to 21.2f, 39.0f to 20.0f, 42.0f to 19.2f,
        43.8f to 16.5f, 45.6f to 13.8f
    ),
    listOf(
        59.4f to 28.0f, 60.5f to 27.8f, 62.8f to 28.8f, 65.0f to 29.8f, 67.5f to 29.8f, 69.9f to 29.7f, 69.5f to 33.1f, 69.0f to 36.5f,
        67.8f to 35.2f, 66.5f to 34.0f, 65.8f to 37.0f, 65.0f to 40.0f, 66.8f to 42.0f, 68.5f to 44.0f, 68.7f to 47.2f, 68.9f to 50.4f,
        69.1f to 53.6f, 69.3f to 56.8f, 69.5f to 60.0f, 70.4f to 62.5f, 71.2f to 65.0f, 72.1f to 67.5f, 73.0f to 70.0f, 73.1f to 72.6f,
        73.2f to 75.2f, 73.4f to 77.9f, 73.5f to 80.5f, 74.1f to 83.9f, 74.7f to 87.3f, 75.3f to 90.7f, 75.9f to 94.1f, 76.5f to 97.5f,
        77.1f to 100.9f, 77.7f to 104.3f, 76.7f to 106.7f, 75.6f to 109.2f, 74.5f to 111.6f, 73.5f to 114.0f, 73.2f to 117.1f, 72.9f to 120.3f,
        72.6f to 123.4f, 72.4f to 126.6f, 72.1f to 129.7f, 71.8f to 132.9f, 71.5f to 136.0f, 71.3f to 139.2f, 71.0f to 142.5f, 70.8f to 145.8f,
        70.5f to 149.0f, 70.3f to 152.2f, 70.1f to 155.5f, 69.8f to 158.8f, 69.6f to 162.0f, 69.0f to 164.9f, 68.4f to 167.8f, 67.8f to 170.8f,
        67.2f to 173.7f, 66.6f to 176.6f, 66.0f to 179.5f, 64.8f to 176.6f, 63.6f to 173.7f, 62.4f to 170.8f, 61.2f to 167.9f, 60.0f to 165.0f,
        57.8f to 162.9f, 55.5f to 160.8f, 53.2f to 158.8f, 51.0f to 156.7f, 54.0f to 156.1f, 57.0f to 155.6f, 60.0f to 155.0f, 59.0f to 152.0f,
        58.0f to 149.0f, 57.0f to 146.0f, 56.0f to 143.0f, 55.0f to 140.0f, 54.0f to 137.0f, 51.2f to 135.8f, 48.5f to 134.5f, 45.8f to 133.2f,
        43.0f to 132.0f, 45.8f to 133.5f, 48.5f to 135.0f, 49.5f to 132.2f, 50.4f to 129.4f, 51.4f to 126.6f, 52.3f to 123.8f, 53.3f to 121.0f,
        51.6f to 118.0f, 50.0f to 115.0f, 50.0f to 111.7f, 50.0f to 108.3f, 50.0f to 105.0f, 50.0f to 101.7f, 50.0f to 98.3f, 50.0f to 95.0f,
        49.7f to 92.4f, 49.3f to 89.9f, 49.0f to 87.3f, 50.2f to 84.2f, 51.5f to 81.2f, 52.8f to 78.1f, 54.0f to 75.0f, 53.4f to 72.0f,
        52.8f to 69.0f, 52.2f to 66.0f, 51.6f to 63.0f, 51.0f to 60.0f, 51.1f to 57.0f, 51.1f to 54.0f, 51.2f to 51.0f, 49.1f to 49.2f,
        47.0f to 47.5f, 45.0f to 47.5f, 43.0f to 47.5f, 43.2f to 45.0f, 43.3f to 42.5f, 43.5f to 40.0f, 45.3f to 39.6f, 47.1f to 39.2f,
        49.8f to 38.2f, 51.0f to 36.1f, 52.1f to 34.0f, 53.5f to 32.5f, 54.8f to 30.4f, 56.0f to 28.2f, 59.4f to 28.0f
    ),
    listOf(
        47.0f to 47.5f, 49.1f to 49.2f, 51.2f to 51.0f, 51.1f to 54.0f, 51.1f to 57.0f, 51.0f to 60.0f, 51.6f to 63.0f, 52.2f to 66.0f,
        52.8f to 69.0f, 53.4f to 72.0f, 54.0f to 75.0f, 52.8f to 78.1f, 51.5f to 81.2f, 50.2f to 84.2f, 49.0f to 87.3f, 47.0f to 84.9f,
        45.0f to 82.6f, 43.0f to 80.2f, 42.1f to 78.0f, 41.1f to 75.7f, 40.2f to 73.5f, 38.7f to 70.7f, 37.2f to 67.8f, 37.5f to 65.1f,
        37.9f to 62.4f, 38.2f to 59.7f, 38.5f to 57.0f, 37.4f to 54.0f, 39.7f to 53.2f, 42.0f to 52.5f, 44.5f to 50.3f, 45.8f to 48.9f,
        47.0f to 47.5f
    ),
    listOf(
        41.2f to 29.0f, 41.6f to 32.1f, 42.0f to 35.2f, 41.5f to 37.9f, 41.0f to 40.5f, 40.5f to 42.5f, 40.0f to 44.5f, 37.2f to 44.8f,
        37.1f to 41.9f, 36.9f to 39.1f, 36.8f to 36.2f, 36.5f to 34.4f, 36.2f to 32.5f, 36.5f to 29.9f, 36.8f to 27.4f, 39.5f to 26.2f,
        41.2f to 29.0f
    ),
    listOf(
        36.8f to 36.2f, 36.9f to 39.1f, 37.1f to 41.9f, 37.2f to 44.8f, 40.0f to 44.5f, 39.4f to 46.7f, 38.8f to 48.9f, 38.1f to 51.5f,
        37.4f to 54.0f, 38.5f to 57.0f, 37.0f to 59.1f, 35.6f to 61.2f, 32.8f to 61.0f, 29.9f to 60.9f, 27.5f to 61.2f, 25.2f to 61.6f,
        25.9f to 59.0f, 26.6f to 56.3f, 27.7f to 53.7f, 28.9f to 51.1f, 30.0f to 48.5f, 29.1f to 46.5f, 29.2f to 43.6f, 29.3f to 40.8f,
        29.4f to 37.9f, 29.5f to 35.0f, 31.5f to 34.5f, 34.1f to 35.4f, 36.8f to 36.2f
    ),
    listOf(
        29.5f to 35.0f, 29.4f to 37.9f, 29.3f to 40.8f, 29.2f to 43.6f, 29.1f to 46.5f, 30.0f to 48.5f, 28.1f to 49.5f, 26.2f to 50.6f,
        24.5f to 51.5f, 25.4f to 53.9f, 26.2f to 56.3f, 24.4f to 58.0f, 22.5f to 59.8f, 20.7f to 58.0f, 18.8f to 56.3f, 17.0f to 54.5f,
        15.8f to 51.9f, 14.5f to 49.2f, 13.6f to 46.4f, 12.7f to 43.5f, 14.6f to 43.1f, 16.5f to 42.8f, 19.0f to 41.0f, 21.5f to 39.1f,
        23.7f to 37.7f, 25.8f to 36.2f, 28.0f to 34.8f, 29.5f to 35.0f
    ),
    listOf(
        35.6f to 61.2f, 36.4f to 64.5f, 37.2f to 67.8f, 37.1f to 70.1f, 37.1f to 72.5f, 37.0f to 74.8f, 34.5f to 74.0f, 32.2f to 74.0f,
        30.0f to 74.0f, 28.0f to 72.3f, 26.0f to 70.5f, 24.0f to 68.8f, 24.8f to 67.0f, 25.0f to 64.3f, 25.2f to 61.6f, 27.5f to 61.2f,
        29.9f to 60.9f, 32.8f to 61.0f, 35.6f to 61.2f
    ),
    listOf(
        34.5f to 74.0f, 35.5f to 76.5f, 34.2f to 79.0f, 31.0f to 79.8f, 29.2f to 81.0f, 28.4f to 82.8f, 27.5f to 84.5f, 27.1f to 86.3f,
        26.7f to 88.2f, 27.8f to 88.8f, 27.8f to 91.7f, 28.1f to 94.1f, 28.3f to 96.5f, 27.0f to 97.1f, 25.5f to 95.7f, 24.0f to 94.2f,
        22.0f to 92.6f, 25.2f to 91.5f, 25.2f to 89.8f, 23.4f to 89.4f, 21.6f to 89.1f, 21.5f to 87.0f, 19.8f to 85.8f, 17.7f to 83.3f,
        15.9f to 80.8f, 13.1f to 80.3f, 10.3f to 79.8f, 8.1f to 77.5f, 9.9f to 76.2f, 12.9f to 74.8f, 15.4f to 73.8f, 17.2f to 73.3f,
        19.0f to 72.8f, 21.6f to 72.6f, 20.7f to 70.4f, 22.3f to 69.0f, 23.8f to 68.4f, 24.3f to 71.0f, 26.1f to 70.8f, 28.0f to 70.5f,
        29.0f to 72.2f, 30.0f to 74.0f, 32.2f to 74.0f, 34.5f to 74.0f
    ),
    listOf(
        9.8f to 80.2f, 8.6f to 81.2f, 6.0f to 80.2f, 7.5f to 79.8f, 9.8f to 80.2f
    ),
    listOf(
        49.0f to 87.3f, 49.3f to 89.9f, 49.7f to 92.4f, 50.0f to 95.0f, 50.0f to 98.3f, 50.0f to 101.7f, 50.0f to 105.0f, 50.0f to 108.3f,
        50.0f to 111.7f, 50.0f to 115.0f, 51.6f to 118.0f, 53.3f to 121.0f, 52.3f to 123.8f, 51.4f to 126.6f, 50.4f to 129.4f, 49.5f to 132.2f,
        48.5f to 135.0f, 45.8f to 133.0f, 43.0f to 131.0f, 42.0f to 128.7f, 41.0f to 126.5f, 40.0f to 124.2f, 38.8f to 121.2f, 38.9f to 119.5f,
        39.0f to 117.7f, 38.2f to 119.8f, 37.5f to 122.0f, 36.2f to 120.8f, 35.0f to 119.5f, 33.1f to 120.5f, 31.2f to 121.5f, 28.6f to 120.5f,
        26.0f to 119.6f, 24.2f to 116.9f, 22.5f to 114.2f, 22.0f to 111.6f, 21.5f to 109.0f, 21.5f to 108.0f, 22.1f to 105.8f, 22.8f to 103.5f,
        21.5f to 101.2f, 22.8f to 99.5f, 24.0f to 97.8f, 26.1f to 97.2f, 28.3f to 96.5f, 28.1f to 94.1f, 27.8f to 91.7f, 27.8f to 88.8f,
        28.3f to 86.2f, 28.7f to 83.6f, 29.2f to 81.0f, 31.0f to 79.8f, 34.2f to 79.0f, 35.6f to 76.9f, 37.0f to 74.8f, 40.2f to 73.5f,
        41.1f to 75.7f, 42.1f to 78.0f, 43.0f to 80.2f, 45.0f to 82.6f, 47.0f to 84.9f, 49.0f to 87.3f
    ),
    listOf(
        49.0f to 87.3f, 49.3f to 89.9f, 49.7f to 92.4f, 50.0f to 95.0f, 50.0f to 98.3f, 50.0f to 101.7f, 50.0f to 105.0f, 50.0f to 108.3f,
        50.0f to 111.7f, 50.0f to 115.0f, 48.5f to 117.2f, 47.0f to 119.5f, 45.5f to 116.5f, 44.0f to 113.5f, 42.5f to 110.5f, 42.6f to 107.6f,
        42.7f to 104.7f, 42.8f to 101.8f, 42.9f to 98.9f, 43.0f to 96.0f, 44.5f to 93.8f, 46.0f to 91.7f, 47.5f to 89.5f, 49.0f to 87.3f
    ),
    listOf(
        40.0f to 124.2f, 41.1f to 127.4f, 42.3f to 130.6f, 40.5f to 129.5f, 38.6f to 128.4f, 36.9f to 128.8f, 35.1f to 129.1f, 34.5f to 126.3f,
        37.5f to 126.6f, 40.0f to 124.2f
    ),
    listOf(
        41.5f to 140.9f, 42.5f to 143.2f, 43.4f to 145.6f, 44.5f to 143.8f, 45.5f to 141.9f, 43.5f to 141.4f, 41.5f to 140.9f
    ),
    listOf(
        41.0f to 140.6f, 39.0f to 141.9f, 35.7f to 140.8f, 34.7f to 138.9f, 33.5f to 135.8f, 33.8f to 133.4f, 34.0f to 130.9f, 35.5f to 133.2f,
        36.5f to 135.2f, 37.5f to 137.3f, 39.2f to 138.9f, 41.0f to 140.6f
    ),
    listOf(
        33.9f to 130.9f, 31.2f to 130.7f, 32.8f to 129.9f, 33.9f to 130.9f
    ),
    listOf(
        25.3f to 121.6f, 22.0f to 120.8f, 23.5f to 120.1f, 25.3f to 121.6f
    ),
    listOf(
        28.3f to 96.5f, 26.1f to 97.2f, 24.0f to 97.8f, 22.8f to 99.5f, 21.5f to 101.2f, 22.8f to 103.5f, 22.1f to 105.8f, 21.5f to 108.0f,
        18.8f to 108.1f, 16.0f to 108.2f, 13.9f to 108.7f, 11.9f to 109.2f, 10.2f to 107.0f, 8.6f to 104.8f, 10.6f to 103.5f, 12.1f to 102.0f,
        13.5f to 100.6f, 10.8f to 100.4f, 8.0f to 100.2f, 5.8f to 101.5f, 3.5f to 102.9f, 1.3f to 104.2f, 3.1f to 101.4f, 4.8f to 100.8f,
        6.5f to 100.1f, 8.2f to 99.3f, 9.9f to 98.6f, 13.2f to 98.1f, 16.5f to 97.6f, 16.0f to 94.3f, 18.1f to 93.6f, 20.1f to 92.9f,
        22.1f to 93.6f, 24.0f to 94.2f, 25.5f to 95.7f, 27.0f to 97.1f, 28.3f to 96.5f
    ),
    listOf(
        5.6f to 95.3f, 4.1f to 97.7f, 2.5f to 100.1f, 1.0f to 102.5f, -1.3f to 103.6f, -3.6f to 104.7f, -5.9f to 105.8f, -4.8f to 104.0f,
        -3.8f to 102.2f, -1.0f to 100.5f, 1.7f to 98.8f, 3.6f to 97.0f, 5.6f to 95.3f
    ),
    listOf(
        -6.0f to 106.0f, -6.5f to 109.2f, -6.9f to 112.5f, -8.4f to 114.4f, -8.1f to 111.7f, -7.8f to 109.0f, -6.0f to 106.0f
    ),
    listOf(
        7.0f to 116.8f, 4.2f to 117.9f, 1.5f to 117.3f, -1.2f to 116.8f, -3.5f to 114.5f, -3.2f to 112.3f, -2.9f to 110.2f, -0.9f to 109.6f,
        1.0f to 109.0f, 2.8f to 111.5f, 4.5f to 114.0f, 5.8f to 115.4f, 7.0f to 116.8f
    ),
    listOf(
        18.5f to 121.0f, 16.6f to 121.0f, 14.6f to 121.0f, 13.0f to 123.8f, 14.5f to 121.9f, 16.0f to 120.0f, 18.5f to 121.0f
    ),
    listOf(
        9.8f to 125.5f, 8.0f to 125.3f, 6.1f to 125.2f, 7.0f to 122.1f, 8.4f to 123.8f, 9.8f to 125.5f
    ),
    listOf(
        -0.8f to 131.3f, -1.7f to 134.5f, -2.6f to 137.8f, -3.5f to 141.0f, -4.5f to 143.3f, -5.5f to 145.5f, -6.5f to 147.8f, -8.5f to 149.3f,
        -10.5f to 150.8f, -9.8f to 147.5f, -9.2f to 144.3f, -8.5f to 141.0f, -7.2f to 139.0f, -5.8f to 137.0f, -4.5f to 135.0f, -2.6f to 133.2f,
        -0.8f to 131.3f
    ),
    listOf(
        35.8f to -5.8f, 35.5f to -4.0f, 35.1f to -2.2f, 32.0f to -1.2f, 30.8f to -3.7f, 29.7f to -6.2f, 28.5f to -8.7f, 26.7f to -10.8f,
        24.9f to -12.8f, 23.1f to -14.9f, 21.3f to -17.0f, 24.1f to -15.2f, 27.0f to -13.5f, 28.7f to -11.6f, 30.4f to -9.6f, 32.0f to -8.6f,
        33.6f to -7.6f, 35.8f to -5.8f
    ),
    listOf(
        35.1f to -2.2f, 36.0f to 0.5f, 36.8f to 3.1f, 37.0f to 6.5f, 37.3f to 9.8f, 35.2f to 10.7f, 33.2f to 11.5f, 31.7f to 10.5f,
        30.2f to 9.5f, 28.0f to 10.3f, 25.7f to 11.2f, 23.5f to 12.0f, 22.0f to 9.2f, 20.5f to 6.3f, 19.0f to 3.5f, 20.5f to 1.2f,
        22.0f to -1.0f, 23.5f to -3.2f, 25.0f to -5.5f, 26.8f to -7.1f, 28.5f to -8.7f, 29.7f to -6.2f, 30.8f to -3.7f, 32.0f to -1.2f,
        35.1f to -2.2f
    ),
    listOf(
        33.2f to 11.5f, 32.9f to 13.2f, 31.6f to 16.1f, 30.4f to 19.0f, 31.6f to 20.8f, 32.8f to 22.6f, 31.6f to 25.2f, 28.4f to 25.1f,
        25.2f to 25.1f, 22.0f to 25.0f, 19.5f to 24.0f, 20.5f to 21.0f, 21.5f to 18.0f, 22.5f to 15.0f, 23.5f to 12.0f, 25.7f to 11.2f,
        28.0f to 10.3f, 30.2f to 9.5f, 31.7f to 10.5f, 33.2f to 11.5f
    ),
    listOf(
        31.6f to 25.2f, 31.4f to 27.5f, 31.2f to 29.9f, 31.4f to 32.2f, 31.5f to 34.5f, 29.5f to 35.0f, 27.0f to 35.6f, 24.5f to 36.3f,
        22.0f to 36.9f, 22.0f to 33.9f, 22.0f to 30.9f, 22.0f to 28.0f, 22.0f to 25.0f, 25.2f to 25.1f, 28.4f to 25.1f, 31.6f to 25.2f
    ),
    listOf(
        22.0f to 25.0f, 22.0f to 28.0f, 22.0f to 30.9f, 22.0f to 33.9f, 22.0f to 36.9f, 20.0f to 37.8f, 18.0f to 38.6f, 15.0f to 37.3f,
        12.0f to 36.0f, 9.5f to 35.3f, 7.0f to 34.7f, 4.5f to 34.0f, 4.8f to 30.8f, 5.0f to 27.5f, 6.7f to 25.8f, 8.3f to 24.2f,
        10.0f to 22.5f, 13.2f to 23.0f, 16.3f to 23.5f, 19.5f to 24.0f, 22.0f to 25.0f
    ),
    listOf(
        21.3f to -17.0f, 23.1f to -14.9f, 24.9f to -12.8f, 26.7f to -10.8f, 28.5f to -8.7f, 26.8f to -7.1f, 25.0f to -5.5f, 23.5f to -3.2f,
        22.0f to -1.0f, 20.5f to 1.2f, 19.0f to 3.5f, 20.5f to 6.3f, 22.0f to 9.2f, 23.5f to 12.0f, 22.5f to 15.0f, 21.5f to 18.0f,
        20.5f to 21.0f, 19.5f to 24.0f, 16.3f to 23.5f, 13.2f to 23.0f, 10.0f to 22.5f, 8.8f to 19.9f, 7.6f to 17.2f, 6.4f to 14.6f,
        5.2f to 11.9f, 4.0f to 9.3f, 5.2f to 6.4f, 6.4f to 3.4f, 5.6f to 0.7f, 4.7f to -2.0f, 4.6f to -4.8f, 4.4f to -7.7f,
        6.5f to -10.4f, 8.5f to -13.2f, 10.6f to -14.6f, 12.6f to -16.0f, 14.7f to -17.4f, 18.0f to -17.2f, 21.3f to -17.0f
    ),
    listOf(
        18.0f to 38.6f, 16.2f to 40.2f, 14.4f to 41.8f, 12.6f to 43.4f, 12.3f to 46.0f, 12.1f to 48.7f, 11.8f to 51.3f, 9.4f to 50.2f,
        6.9f to 49.1f, 4.5f to 48.0f, 2.5f to 45.9f, 0.4f to 43.7f, -1.6f to 41.6f, -4.2f to 40.5f, -6.8f to 39.3f, -8.7f to 39.8f,
        -10.5f to 40.4f, -11.0f to 37.7f, -11.5f to 35.0f, -8.9f to 33.9f, -6.2f to 32.8f, -3.6f to 31.6f, -1.0f to 30.5f, 1.8f to 32.2f,
        4.5f to 34.0f, 7.0f to 34.7f, 9.5f to 35.3f, 12.0f to 36.0f, 15.0f to 37.3f, 18.0f to 38.6f
    ),
    listOf(
        4.0f to 9.3f, 5.2f to 11.9f, 6.4f to 14.6f, 7.6f to 17.2f, 8.8f to 19.9f, 10.0f to 22.5f, 8.3f to 24.2f, 6.7f to 25.8f,
        5.0f to 27.5f, 4.8f to 30.8f, 4.5f to 34.0f, 1.8f to 32.2f, -1.0f to 30.5f, -3.6f to 31.6f, -6.2f to 32.8f, -8.9f to 33.9f,
        -11.5f to 35.0f, -13.0f to 32.9f, -14.5f to 30.9f, -16.0f to 28.8f, -17.8f to 25.8f, -17.7f to 23.0f, -17.6f to 20.2f, -17.6f to 17.4f,
        -17.5f to 14.6f, -17.4f to 11.8f, -14.9f to 12.6f, -12.5f to 13.4f, -9.2f to 12.8f, -6.0f to 12.2f, -3.4f to 10.5f, -0.7f to 8.8f,
        1.7f to 9.1f, 4.0f to 9.3f
    ),
    listOf(
        -17.4f to 11.8f, -17.5f to 14.6f, -17.6f to 17.4f, -17.6f to 20.2f, -17.7f to 23.0f, -17.8f to 25.8f, -16.0f to 28.8f, -14.5f to 30.9f,
        -13.0f to 32.9f, -11.5f to 35.0f, -11.0f to 37.7f, -10.5f to 40.4f, -13.8f to 39.7f, -17.0f to 39.0f, -18.5f to 37.0f, -20.0f to 35.0f,
        -23.0f to 34.0f, -26.0f to 32.9f, -27.9f to 31.9f, -29.8f to 31.0f, -31.9f to 28.3f, -34.0f to 25.6f, -34.4f to 22.8f, -34.8f to 20.0f,
        -33.9f to 18.4f, -31.2f to 17.4f, -28.6f to 16.5f, -25.8f to 15.5f, -23.0f to 14.5f, -20.2f to 13.2f, -17.4f to 11.8f
    ),
    listOf(
        -12.0f to 49.3f, -15.3f to 50.4f, -18.5f to 49.3f, -21.8f to 48.1f, -25.0f to 47.0f, -25.5f to 45.2f, -22.8f to 44.8f, -20.0f to 44.3f,
        -17.9f to 45.3f, -15.7f to 46.3f, -13.8f to 47.8f, -12.0f to 49.3f
    ),
    listOf(
        -12.4f to 130.8f, -11.7f to 133.8f, -11.0f to 136.8f, -13.8f to 138.0f, -16.5f to 139.2f, -13.6f to 140.8f, -10.7f to 142.5f, -12.8f to 143.6f,
        -14.8f to 144.7f, -16.9f to 145.8f, -19.1f to 147.5f, -21.2f to 149.1f, -23.4f to 150.8f, -26.0f to 152.2f, -28.6f to 153.6f, -31.2f to 152.4f,
        -33.9f to 151.2f, -35.7f to 150.6f, -37.5f to 150.0f, -37.9f to 147.4f, -38.3f to 144.9f, -38.1f to 142.7f, -38.0f to 140.5f, -36.5f to 139.6f,
        -35.0f to 138.6f, -34.7f to 135.5f, -33.1f to 133.2f, -31.5f to 131.0f, -32.3f to 128.0f, -33.2f to 125.0f, -34.0f to 122.0f, -34.5f to 120.0f,
        -35.0f to 117.9f, -34.0f to 115.0f, -32.0f to 115.8f, -28.9f to 114.7f, -25.9f to 113.5f, -23.9f to 113.8f, -21.8f to 114.1f, -21.1f to 116.3f,
        -20.3f to 118.6f, -19.1f to 120.4f, -18.0f to 122.2f, -16.2f to 123.8f, -14.5f to 125.5f, -13.4f to 128.2f, -12.4f to 130.8f
    ),
    listOf(
        -40.8f to 144.7f, -40.9f to 146.5f, -41.0f to 148.3f, -43.6f to 146.9f, -42.2f to 145.2f, -40.8f to 144.7f
    ),
    listOf(
        -34.4f to 172.7f, -36.0f to 175.6f, -37.7f to 178.5f, -39.5f to 176.7f, -41.3f to 174.8f, -39.3f to 173.8f, -36.8f to 173.2f, -34.4f to 172.7f
    ),
    listOf(
        -40.5f to 172.7f, -42.4f to 173.7f, -44.5f to 171.0f, -46.6f to 168.3f, -45.8f to 166.5f, -44.0f to 168.6f, -42.3f to 170.6f, -40.5f to 172.7f
    ),
    listOf(
        17.8f to -91.0f, 17.8f to -89.2f, 15.9f to -88.6f, 13.8f to -90.1f, 14.5f to -92.2f, 16.1f to -91.6f, 17.8f to -91.0f
    ),
    listOf(
        15.9f to -88.6f, 15.9f to -85.8f, 15.9f to -83.1f, 14.4f to -85.3f, 13.0f to -87.6f, 13.8f to -90.1f, 15.9f to -88.6f
    ),
    listOf(
        15.9f to -83.1f, 13.4f to -83.4f, 11.0f to -83.7f, 10.9f to -85.7f, 13.0f to -87.6f, 14.4f to -85.3f, 15.9f to -83.1f
    ),
    listOf(
        1.4f to -78.8f, 0.7f to -77.0f, 0.0f to -75.2f, -1.5f to -76.5f, -3.0f to -77.8f, -3.9f to -79.5f, -4.7f to -81.3f, -2.2f to -81.0f,
        -0.4f to -79.9f, 1.4f to -78.8f
    ),
    listOf(
        -19.3f to -58.0f, -22.1f to -58.0f, -23.9f to -56.3f, -25.6f to -54.6f, -26.5f to -56.6f, -27.3f to -58.6f, -25.1f to -60.7f, -22.9f to -62.7f,
        -21.1f to -60.4f, -19.3f to -58.0f
    ),
    listOf(
        -30.2f to -57.6f, -32.0f to -55.5f, -33.7f to -53.4f, -35.0f to -54.9f, -34.4f to -57.9f, -32.3f to -57.8f, -30.2f to -57.6f
    ),
    listOf(
        8.4f to -59.8f, 7.2f to -58.5f, 6.0f to -57.1f, 3.8f to -57.8f, 1.5f to -58.5f, 1.5f to -60.0f, 3.4f to -60.4f, 5.2f to -60.7f,
        8.4f to -59.8f
    ),
    listOf(
        6.0f to -57.1f, 6.0f to -54.0f, 4.0f to -54.0f, 2.0f to -54.0f, 1.8f to -56.2f, 1.5f to -58.5f, 3.8f to -57.8f, 6.0f to -57.1f
    ),
    listOf(
        51.0f to 2.5f, 52.2f to 4.0f, 53.4f to 5.5f, 53.4f to 7.2f, 50.8f to 6.0f, 49.5f to 5.8f, 50.2f to 4.2f, 51.0f to 2.5f
    ),
    listOf(
        54.8f to 8.4f, 56.2f to 9.5f, 57.7f to 10.6f, 55.5f to 12.6f, 54.4f to 11.0f, 54.8f to 8.4f
    ),
    listOf(
        47.6f to 7.6f, 47.5f to 9.6f, 46.2f to 10.5f, 45.8f to 9.0f, 46.2f to 6.1f, 47.6f to 7.6f
    ),
    listOf(
        47.5f to 9.6f, 48.0f to 11.7f, 48.5f to 13.8f, 48.0f to 17.1f, 46.5f to 16.0f, 46.5f to 13.7f, 47.0f to 11.6f, 47.5f to 9.6f
    ),
    listOf(
        51.0f to 14.3f, 50.2f to 16.6f, 49.5f to 18.8f, 48.6f to 16.9f, 49.0f to 13.8f, 50.2f to 12.1f, 51.0f to 14.3f
    ),
    listOf(
        48.6f to 16.9f, 49.5f to 18.8f, 49.2f to 20.6f, 49.0f to 22.5f, 48.0f to 22.9f, 46.2f to 20.1f, 45.8f to 18.4f, 47.7f to 16.5f,
        48.6f to 16.9f
    ),
    listOf(
        48.0f to 22.9f, 48.1f to 24.8f, 48.2f to 26.7f, 46.8f to 28.2f, 45.3f to 29.7f, 43.7f to 28.6f, 43.9f to 25.8f, 44.0f to 22.9f,
        45.1f to 21.5f, 46.2f to 20.1f, 48.0f to 22.9f
    ),
    listOf(
        44.0f to 22.9f, 43.9f to 25.8f, 43.7f to 28.6f, 42.0f to 28.0f, 41.3f to 26.6f, 41.3f to 24.8f, 41.4f to 22.9f, 44.0f to 22.9f
    ),
    listOf(
        59.4f to 24.7f, 59.4f to 28.0f, 57.5f to 27.5f, 57.8f to 24.4f, 59.4f to 24.7f
    ),
    listOf(
        57.8f to 24.4f, 57.5f to 27.5f, 55.7f to 26.6f, 55.9f to 23.9f, 56.0f to 21.1f, 56.9f to 22.8f, 57.8f to 24.4f
    ),
    listOf(
        56.0f to 21.1f, 55.9f to 23.9f, 55.7f to 26.6f, 54.8f to 25.1f, 53.9f to 23.5f, 54.1f to 21.6f, 54.4f to 19.6f, 56.0f to 21.1f
    ),
    listOf(
        56.0f to 28.2f, 54.8f to 30.4f, 53.5f to 32.5f, 51.5f to 30.6f, 51.5f to 27.1f, 51.5f to 23.6f, 53.9f to 23.5f, 55.0f to 25.9f,
        56.0f to 28.2f
    ),
    listOf(
        37.2f to 42.4f, 35.4f to 44.1f, 33.5f to 45.9f, 31.8f to 47.2f, 30.0f to 48.5f, 29.1f to 46.5f, 30.1f to 44.1f, 31.0f to 41.6f,
        32.0f to 39.2f, 33.4f to 38.8f, 35.3f to 40.6f, 37.2f to 42.4f
    ),
    listOf(
        36.8f to 36.2f, 37.0f to 39.3f, 37.2f to 42.4f, 35.3f to 40.6f, 33.4f to 38.8f, 32.3f to 36.8f, 33.3f to 35.6f, 35.8f to 35.8f,
        36.8f to 36.2f
    ),
    listOf(
        17.0f to 54.5f, 15.8f to 51.9f, 14.5f to 49.2f, 13.6f to 46.4f, 12.7f to 43.5f, 14.6f to 43.1f, 16.5f to 42.8f, 17.3f to 45.9f,
        18.2f to 48.9f, 19.0f to 52.0f, 17.0f to 54.5f
    ),
    listOf(
        26.2f to 56.3f, 24.4f to 58.0f, 22.5f to 59.8f, 20.7f to 58.0f, 18.8f to 56.3f, 17.0f to 54.5f, 19.0f to 52.0f, 21.5f to 53.8f,
        24.0f to 55.5f, 26.2f to 56.3f
    ),
    listOf(
        35.6f to 61.2f, 36.4f to 64.5f, 37.2f to 67.8f, 37.1f to 70.1f, 37.1f to 72.5f, 37.0f to 74.8f, 35.5f to 72.4f, 34.0f to 70.0f,
        31.5f to 69.3f, 30.5f to 67.7f, 29.5f to 66.0f, 29.7f to 63.5f, 29.9f to 60.9f, 32.8f to 61.0f, 35.6f to 61.2f
    ),
    listOf(
        30.2f to 81.0f, 29.4f to 83.4f, 28.6f to 85.8f, 27.8f to 88.2f, 26.4f to 88.0f, 26.9f to 86.0f, 27.5f to 84.0f, 28.2f to 82.0f,
        29.0f to 80.1f, 30.2f to 81.0f
    ),
    listOf(
        26.6f to 88.4f, 25.2f to 89.8f, 25.2f to 91.5f, 22.0f to 92.6f, 21.8f to 90.8f, 21.6f to 89.1f, 24.5f to 88.0f, 26.6f to 88.4f
    ),
    listOf(
        28.3f to 96.5f, 26.1f to 97.2f, 24.0f to 97.8f, 22.8f to 99.5f, 21.5f to 101.2f, 20.4f to 100.1f, 18.4f to 99.3f, 16.5f to 98.5f,
        13.2f to 98.5f, 9.9f to 98.6f, 13.2f to 98.1f, 16.5f to 97.6f, 16.0f to 94.3f, 18.1f to 93.6f, 20.1f to 92.9f, 22.1f to 93.6f,
        24.0f to 94.2f, 25.5f to 95.7f, 27.0f to 97.1f, 28.3f to 96.5f
    ),
    listOf(
        20.4f to 100.1f, 19.2f to 102.4f, 18.0f to 104.8f, 15.0f to 105.6f, 13.5f to 102.5f, 12.0f to 102.5f, 13.5f to 100.6f, 10.8f to 100.4f,
        8.0f to 100.2f, 6.5f to 100.1f, 8.2f to 99.3f, 9.9f to 98.6f, 13.2f to 98.5f, 16.5f to 98.5f, 18.4f to 99.3f, 20.4f to 100.1f
    ),
    listOf(
        14.4f to 102.8f, 14.0f to 106.0f, 11.5f to 107.5f, 10.4f to 104.4f, 12.0f to 102.5f, 14.4f to 102.8f
    ),
    listOf(
        13.9f to 5.5f, 13.6f to 8.3f, 13.3f to 11.2f, 13.0f to 14.0f, 11.0f to 13.8f, 9.0f to 13.5f, 6.8f to 11.2f, 4.5f to 8.9f,
        4.3f to 6.0f, 6.4f to 3.4f, 8.9f to 3.5f, 11.5f to 3.6f, 13.9f to 5.5f
    ),
    listOf(
        13.0f to 14.0f, 10.0f to 15.5f, 7.4f to 15.7f, 4.8f to 15.8f, 2.2f to 16.0f, 2.2f to 12.9f, 2.2f to 9.8f, 4.5f to 8.9f,
        6.8f to 11.2f, 9.0f to 13.5f, 11.0f to 13.8f, 13.0f to 14.0f
    ),
    listOf(
        23.5f to 12.0f, 23.5f to 15.0f, 22.2f to 18.0f, 20.8f to 21.0f, 19.5f to 24.0f, 17.2f to 23.5f, 15.0f to 23.0f, 12.5f to 22.8f,
        10.0f to 22.5f, 9.3f to 20.2f, 8.7f to 17.8f, 8.0f to 15.5f, 10.5f to 14.8f, 13.0f to 14.0f, 13.3f to 11.2f, 13.6f to 8.3f,
        13.9f to 5.5f, 16.5f to 4.0f, 18.2f to 5.0f, 20.0f to 6.0f, 21.8f to 9.0f, 23.5f to 12.0f
    ),
    listOf(
        14.5f to 36.6f, 15.5f to 39.5f, 14.1f to 41.5f, 12.6f to 43.4f, 10.8f to 43.7f, 9.0f to 44.0f, 7.0f to 44.5f, 5.0f to 45.0f,
        4.0f to 41.9f, 3.8f to 40.0f, 3.5f to 38.0f, 4.5f to 35.9f, 7.0f to 35.0f, 9.5f to 34.0f, 12.0f to 35.3f, 14.5f to 36.6f
    ),
    listOf(
        4.5f to 35.9f, 3.5f to 38.0f, 3.8f to 40.0f, 4.0f to 41.9f, 1.2f to 41.8f, -1.6f to 41.6f, -3.2f to 40.4f, -4.7f to 39.2f,
        -2.9f to 36.6f, -1.0f to 34.0f, 1.0f to 34.5f, 2.8f to 35.2f, 4.5f to 35.9f
    ),
    listOf(
        -1.0f to 30.5f, -1.0f to 34.0f, -2.9f to 36.6f, -4.7f to 39.2f, -7.6f to 39.8f, -10.5f to 40.4f, -11.0f to 37.7f, -11.5f to 35.0f,
        -9.4f to 33.0f, -8.5f to 30.8f, -6.5f to 30.0f, -4.4f to 29.2f, -2.7f to 29.9f, -1.0f to 30.5f
    ),
    listOf(
        -6.0f to 12.2f, -6.4f to 15.1f, -6.9f to 17.9f, -7.3f to 20.8f, -9.2f to 22.4f, -11.0f to 24.0f, -13.0f to 22.0f, -15.4f to 22.5f,
        -17.8f to 23.0f, -17.7f to 20.2f, -17.6f to 17.4f, -17.5f to 14.6f, -17.4f to 11.8f, -14.9f to 12.6f, -12.5f to 13.4f, -9.2f to 12.8f,
        -6.0f to 12.2f
    ),
    listOf(
        -17.4f to 11.8f, -17.5f to 15.1f, -17.6f to 18.4f, -17.7f to 21.7f, -17.8f to 25.0f, -19.9f to 22.5f, -22.0f to 20.0f, -25.2f to 20.0f,
        -28.5f to 20.0f, -28.6f to 18.2f, -28.6f to 16.5f, -25.8f to 15.5f, -23.0f to 14.5f, -20.2f to 13.2f, -17.4f to 11.8f
    ),
    listOf(
        -17.8f to 25.0f, -20.0f to 27.2f, -22.2f to 29.4f, -23.4f to 27.7f, -24.7f to 26.0f, -25.8f to 23.3f, -26.8f to 20.6f, -24.4f to 20.3f,
        -22.0f to 20.0f, -19.9f to 22.5f, -17.8f to 25.0f
    ),
    listOf(
        -15.6f to 30.4f, -16.5f to 28.9f, -17.1f to 27.1f, -17.8f to 25.3f, -20.0f to 27.4f, -22.2f to 29.4f, -22.4f to 31.3f, -19.8f to 33.0f,
        -16.5f to 33.0f, -15.6f to 30.4f
    )
)

@Composable
fun RetroFuturisticGlobeView(
    globeTheme: String,
    globeWireframeColor: String,
    onTick: () -> Unit
) {
    // Default spinning posture constants
    val defaultPitch = 0.24f   // ~14 deg natural axial tilt
    val defaultRoll = -0.08f   // subtle orbital inclination
    val baseSpinSpeed = 0.36f  // smooth autonomous Y-axis spin (radians/sec)

    var yaw by remember { mutableStateOf(-1.25f) }
    var pitch by remember { mutableStateOf(defaultPitch) }
    var roll by remember { mutableStateOf(defaultRoll) }

    var velYaw by remember { mutableStateOf(baseSpinSpeed) }
    var velPitch by remember { mutableStateOf(0f) }
    var velRoll by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

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
                    // Advance rotation by angular velocity
                    yaw += velYaw * dt
                    pitch = (pitch + velPitch * dt).coerceIn(-1.45f, 1.45f)
                    roll = (roll + velRoll * dt).coerceIn(-0.95f, 0.95f)

                    // Smooth exponential decay of user flick velocity back to autonomous spin
                    val decay = exp(-3.1f * dt)
                    velYaw = baseSpinSpeed + (velYaw - baseSpinSpeed) * decay
                    velPitch *= decay
                    velRoll *= decay

                    // Restoring spring torque back to original spinning posture (defaultPitch, defaultRoll)
                    val restoreFactor = (1f - exp(-2.8f * dt)).coerceIn(0f, 1f)
                    pitch += (defaultPitch - pitch) * restoreFactor
                    roll += (defaultRoll - roll) * restoreFactor
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
            else -> Color(0xFFFF3B00) // CMF Orange default wireframe color
        }
    }

    val isWireframe = globeTheme == "wireframe"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var lastHapticStep = 0
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isDragging = true
                    velYaw = 0f
                    velPitch = 0f
                    velRoll = 0f
                    var prevPos = down.position
                    var prevTime = down.uptimeMillis

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            change.consume()
                            val curPos = change.position
                            val curTime = change.uptimeMillis
                            val dx = curPos.x - prevPos.x
                            val dy = curPos.y - prevPos.y
                            val dtMs = (curTime - prevTime).coerceAtLeast(1L)
                            val dtSec = dtMs / 1000f

                            val sens = 0.0102f
                            yaw += dx * sens
                            pitch = (pitch - dy * sens).coerceIn(-1.42f, 1.42f)
                            roll = (roll + (dx * dy) * 0.00016f).coerceIn(-0.85f, 0.85f)

                            val instVelYaw = (dx * sens) / dtSec
                            val instVelPitch = (-dy * sens) / dtSec
                            val instVelRoll = ((dx * dy) * 0.00016f) / dtSec
                            velYaw = (velYaw * 0.35f + instVelYaw * 0.65f).coerceIn(-14f, 14f)
                            velPitch = (velPitch * 0.35f + instVelPitch * 0.65f).coerceIn(-12f, 12f)
                            velRoll = (velRoll * 0.35f + instVelRoll * 0.65f).coerceIn(-8f, 8f)

                            val step = ((yaw * 6f).toInt())
                            if (step != lastHapticStep) {
                                lastHapticStep = step
                                onTick()
                            }

                            prevPos = curPos
                            prevTime = curTime
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
            // Maximum globe radius (0.492f of container) with zero text/shape clutter
            val radius = (min(w, h) * 0.492f).coerceAtLeast(36f)

            val cosY = cos(yaw)
            val sinY = sin(yaw)
            val cosP = cos(pitch)
            val sinP = sin(pitch)
            val cosR = cos(roll)
            val sinR = sin(roll)

            // Project (latDeg, lonDeg) onto 3D rotated unit sphere -> (screenX, screenY, normZ)
            fun projectLatLon(latDeg: Float, lonDeg: Float): Triple<Float, Float, Float> {
                val lat = (latDeg * PI / 180.0).toFloat()
                val lon = (lonDeg * PI / 180.0).toFloat()
                val cosLat = cos(lat)
                val x0 = cosLat * sin(lon)
                val y0 = -sin(lat)
                val z0 = cosLat * cos(lon)

                // 1. Rotate around Y-axis (yaw)
                val x1 = x0 * cosY + z0 * sinY
                val y1 = y0
                val z1 = -x0 * sinY + z0 * cosY

                // 2. Rotate around X-axis (pitch)
                val x2 = x1
                val y2 = y1 * cosP - z1 * sinP
                val z2 = y1 * sinP + z1 * cosP

                // 3. Rotate around Z-axis (roll)
                val x3 = x2 * cosR - y2 * sinR
                val y3 = x2 * sinR + y2 * cosR
                val z3 = z2

                return Triple(cx + x3 * radius, cy + y3 * radius, z3)
            }

            // 1. Sphere Ocean / Interior Background
            if (!isWireframe) {
                // "retro_holo": Translucent deep cyan-blue hologram ocean sphere matching image_d2e577.jpg
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0A6699).copy(alpha = 0.56f),
                            Color(0xFF064269).copy(alpha = 0.68f),
                            Color(0xFF03223A).copy(alpha = 0.80f)
                        ),
                        center = Offset(cx - radius * 0.18f, cy - radius * 0.18f),
                        radius = radius * 1.15f
                    ),
                    radius = radius,
                    center = Offset(cx, cy)
                )
            } else {
                // "wireframe": Subtle translucent core glow in user's wireframe color
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            wfColor.copy(alpha = 0.07f),
                            wfColor.copy(alpha = 0.02f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(cx, cy)
                )
            }

            // 2. Back-Hemisphere Latitude & Longitude Grid (z < 0) for 3D Transparency
            val latLevels = listOf(-75f, -60f, -45f, -30f, -15f, 0f, 15f, 30f, 45f, 60f, 75f)
            val backGridColor = if (isWireframe) wfColor.copy(alpha = 0.11f) else Color(0xFF1E96C8).copy(alpha = 0.14f)
            for (latDeg in latLevels) {
                var prevPt: Triple<Float, Float, Float>? = null
                for (lonStep in 0..360 step 10) {
                    val curPt = projectLatLon(latDeg, lonStep.toFloat())
                    if (prevPt != null) {
                        val zAvg = (prevPt.third + curPt.third) * 0.5f
                        if (zAvg < 0f) {
                            drawLine(
                                color = backGridColor,
                                start = Offset(prevPt.first, prevPt.second),
                                end = Offset(curPt.first, curPt.second),
                                strokeWidth = 0.65.dp.toPx()
                            )
                        }
                    }
                    prevPt = curPt
                }
            }
            for (lonDeg in 0 until 360 step 15) {
                var prevPt: Triple<Float, Float, Float>? = null
                for (latStep in -75..75 step 10) {
                    val curPt = projectLatLon(latStep.toFloat(), lonDeg.toFloat())
                    if (prevPt != null) {
                        val zAvg = (prevPt.third + curPt.third) * 0.5f
                        if (zAvg < 0f) {
                            drawLine(
                                color = backGridColor,
                                start = Offset(prevPt.first, prevPt.second),
                                end = Offset(curPt.first, curPt.second),
                                strokeWidth = 0.65.dp.toPx()
                            )
                        }
                    }
                    prevPt = curPt
                }
            }

            // 3. Back-Hemisphere Ghost Country Outlines (for 3D Transparent Depth)
            val backBorderColor = if (isWireframe) wfColor.copy(alpha = 0.14f) else Color(0xFF18B868).copy(alpha = 0.15f)
            for (poly in WORLD_COUNTRY_BORDERS) {
                var prevPt: Triple<Float, Float, Float>? = null
                for ((lat, lon) in poly) {
                    val curPt = projectLatLon(lat, lon)
                    if (prevPt != null && prevPt.third < 0f && curPt.third < 0f) {
                        drawLine(
                            color = backBorderColor,
                            start = Offset(prevPt.first, prevPt.second),
                            end = Offset(curPt.first, curPt.second),
                            strokeWidth = 0.65.dp.toPx()
                        )
                    }
                    prevPt = curPt
                }
            }

            // 4. Front-Hemisphere Latitude & Longitude Grid (z >= 0)
            for (latDeg in latLevels) {
                val isEquator = latDeg == 0f
                var prevPt: Triple<Float, Float, Float>? = null
                for (lonStep in 0..360 step 8) {
                    val curPt = projectLatLon(latDeg, lonStep.toFloat())
                    if (prevPt != null) {
                        val zAvg = (prevPt.third + curPt.third) * 0.5f
                        if (zAvg >= 0f) {
                            val gridAlpha = if (isEquator) (0.35f + 0.25f * zAvg) else (0.18f + 0.22f * zAvg)
                            val gridCol = if (isWireframe) {
                                wfColor.copy(alpha = gridAlpha.coerceIn(0f, 1f))
                            } else {
                                Color(0xFF28B8E8).copy(alpha = gridAlpha.coerceIn(0f, 1f))
                            }
                            drawLine(
                                color = gridCol,
                                start = Offset(prevPt.first, prevPt.second),
                                end = Offset(curPt.first, curPt.second),
                                strokeWidth = if (isEquator) 1.05.dp.toPx() else 0.72.dp.toPx()
                            )
                        }
                    }
                    prevPt = curPt
                }
            }
            for (lonDeg in 0 until 360 step 15) {
                var prevPt: Triple<Float, Float, Float>? = null
                for (latStep in -75..75 step 8) {
                    val curPt = projectLatLon(latStep.toFloat(), lonDeg.toFloat())
                    if (prevPt != null) {
                        val zAvg = (prevPt.third + curPt.third) * 0.5f
                        if (zAvg >= 0f) {
                            val gridAlpha = 0.18f + 0.22f * zAvg
                            val gridCol = if (isWireframe) {
                                wfColor.copy(alpha = gridAlpha.coerceIn(0f, 1f))
                            } else {
                                Color(0xFF28B8E8).copy(alpha = gridAlpha.coerceIn(0f, 1f))
                            }
                            drawLine(
                                color = gridCol,
                                start = Offset(prevPt.first, prevPt.second),
                                end = Offset(curPt.first, curPt.second),
                                strokeWidth = 0.72.dp.toPx()
                            )
                        }
                    }
                    prevPt = curPt
                }
            }

            // 5. Front-Hemisphere High-Detail Country Polygons (Fill + Precise Glowing Country Borders)
            for (poly in WORLD_COUNTRY_BORDERS) {
                val projected = poly.map { (lat, lon) -> projectLatLon(lat, lon) }
                val anyFront = projected.any { it.third > -0.02f }
                if (!anyFront) continue

                // In "retro_holo" theme, fill each visible country polygon with translucent emerald green
                // clamping any horizon-crossing vertex cleanly to the sphere's circular rim
                if (!isWireframe) {
                    val frontCount = projected.count { it.third > 0.0f }
                    if (frontCount >= 3) {
                        val fillPath = Path()
                        var started = false
                        for (pt in projected) {
                            val px: Float
                            val py: Float
                            if (pt.third >= 0f) {
                                px = pt.first
                                py = pt.second
                            } else {
                                val dx = pt.first - cx
                                val dy = pt.second - cy
                                val d = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                                px = cx + (dx / d) * radius * 0.992f
                                py = cy + (dy / d) * radius * 0.992f
                            }
                            if (!started) {
                                fillPath.moveTo(px, py)
                                started = true
                            } else {
                                fillPath.lineTo(px, py)
                            }
                        }
                        fillPath.close()
                        drawPath(
                            path = fillPath,
                            color = Color(0xFF0D944E).copy(alpha = 0.48f)
                        )
                    }
                }

                // Draw precise country border segments on the front hemisphere
                for (i in 0 until projected.lastIndex) {
                    val p1 = projected[i]
                    val p2 = projected[i + 1]
                    val zAvg = (p1.third + p2.third) * 0.5f
                    if (zAvg >= -0.03f) {
                        val vis = zAvg.coerceIn(0f, 1f)
                        val edgeAlpha = (0.45f + 0.55f * vis).coerceIn(0.25f, 1f)
                        if (!isWireframe) {
                            // "retro_holo": Outer emerald phosphor glow + crisp pale-mint/neon-green border
                            drawLine(
                                color = Color(0xFF1EFF6E).copy(alpha = edgeAlpha * 0.45f),
                                start = Offset(p1.first, p1.second),
                                end = Offset(p2.first, p2.second),
                                strokeWidth = 2.1.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color(0xFFD6FFCC).copy(alpha = edgeAlpha * 0.96f),
                                start = Offset(p1.first, p1.second),
                                end = Offset(p2.first, p2.second),
                                strokeWidth = 1.05.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        } else {
                            // "wireframe": Crisp customizable wireframe border + subtle white core highlight
                            drawLine(
                                color = wfColor.copy(alpha = edgeAlpha * 0.95f),
                                start = Offset(p1.first, p1.second),
                                end = Offset(p2.first, p2.second),
                                strokeWidth = 1.35.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            // 6. Clean Outer Sphere Horizon Rim (No extra rings, brackets, crosshairs, or text)
            val rimGlowColor = if (isWireframe) wfColor.copy(alpha = 0.28f) else Color(0xFF28E098).copy(alpha = 0.30f)
            val rimCoreColor = if (isWireframe) wfColor.copy(alpha = 0.88f) else Color(0xFF70FFBA).copy(alpha = 0.85f)
            drawCircle(
                color = rimGlowColor,
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 3.0.dp.toPx())
            )
            drawCircle(
                color = rimCoreColor,
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.3.dp.toPx())
            )
        }
    }
}

// ============================================================================
// ============================================================================
// ULTRA-LIGHT TEXT APP DRAWER (120Hz Pure Fluid Scroll + Top-Aligned A-Z Scrubber)
// Zero row highlighting, zero hover animations on drawer apps, zero recomposition!
// ============================================================================
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
            .background(Color.Black.copy(alpha = 0.97f))
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
                                fontFamily = FontFamily.Monospace
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
                        fontFamily = FontFamily.Monospace,
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

            var activeScrubFloatLetter by remember { mutableStateOf<Float?>(null) }
            val density = LocalDensity.current
            val maxRowWaveShiftPx = with(density) { (20.dp * drawerWaveIntensity).toPx() }
            val headerFontSp = (drawerTextSizeSp * 0.68f).coerceIn(10f, 16f).sp
            val starFontSp = (drawerTextSizeSp * 0.65f).coerceIn(9f, 15f).sp

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Ultra-clean 120Hz LazyColumn with configurable App Name Scale & Curved Horizontal Wave
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

                        // Curved horizontal wave on visible App Drawer rows during A-Z traversal
                        val currentFloatLetter = activeScrubFloatLetter
                        val targetRowWave = if (currentFloatLetter != null && waveScrubMode != "off" && drawerWaveIntensity > 0.01f) {
                            val appLetterIdx = (app.firstLetter.uppercaseChar() - 'A').coerceIn(0, 25).toFloat()
                            val distLetters = abs(currentFloatLetter - appLetterIdx)
                            val waveSpanLetters = (2.0f + 1.8f * drawerWaveIntensity).coerceAtLeast(1.0f)
                            if (distLetters < waveSpanLetters) {
                                cos((distLetters / waveSpanLetters) * (PI / 2)).toFloat().coerceIn(0f, 1f)
                            } else 0f
                        } else 0f

                        val animRowWave by animateFloatAsState(
                            targetValue = targetRowWave,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessHigh
                            ),
                            label = "drawerRowWave"
                        )

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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        if (animRowWave > 0.005f) {
                                            translationX = animRowWave * maxRowWaveShiftPx
                                            val sc = 1f + animRowWave * (0.05f * drawerWaveIntensity).coerceIn(0f, 0.14f)
                                            scaleX = sc
                                            scaleY = sc
                                        }
                                    }
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
                                        color = if (animRowWave > 0.55f) accentColor else Color(0xFFF5F5F5),
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
                                            fontFamily = FontFamily.Monospace
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

                // A-Z Scrubber Rail with Adjustable Curved Horizontal Wave Intensity
                AlphabetScrubberRail(
                    accentColor = accentColor,
                    waveScrubMode = waveScrubMode,
                    drawerWaveIntensity = drawerWaveIntensity,
                    lomoFontFamily = lomoFontFamily,
                    onScrubFloatProgress = { floatLetter ->
                        activeScrubFloatLetter = floatLetter
                    },
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
    onScrubFloatProgress: (Float?) -> Unit,
    onSelectLetter: (Char) -> Unit
) {
    val density = LocalDensity.current
    val letters = remember { ('A'..'Z').toList() }
    var railHeightPx by remember { mutableStateOf(1f) }
    var scrubLocalY by remember { mutableStateOf<Float?>(null) }
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    val currentOnSelect by rememberUpdatedState(onSelectLetter)
    val currentOnFloatProgress by rememberUpdatedState(onScrubFloatProgress)

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
        currentOnFloatProgress(floatIdx)
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
                        currentOnFloatProgress(null)
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
                    fontFamily = FontFamily.Monospace,
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
        "crt_world_map" to "Interactive Flat CRT World Map",
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
                    text = "100% offline widgets · Resize & customize (reorder in Edit Mode)",
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
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (enabled) "ACTIVE [×]" else "+ ADD",
                            color = if (enabled) accentColor else Color(0xFF8A8A8A),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
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
                        Spacer(modifier = Modifier.height(10.dp))
                        val sizeChoices = when (id) {
                            "globe", "crt_world_map" -> listOf(
                                "compact" to "S",
                                "standard" to "M",
                                "large" to "L",
                                "xlarge" to "XL",
                                "max" to "MAX"
                            )
                            "precision_chrono", "analog_chrono", "retro_flip_clock" -> listOf(
                                "compact" to "S",
                                "standard" to "M",
                                "large" to "L",
                                "xlarge" to "XL"
                            )
                            else -> listOf(
                                "compact" to "S",
                                "standard" to "M",
                                "large" to "L"
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WIDGET SCALE",
                                color = Color(0xFF8A8A8A),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                sizeChoices.forEach { (szId, szLbl) ->
                                    val isSz = currentSize == szId
                                    Text(
                                        text = szLbl,
                                        color = if (isSz) Color.Black else Color.White,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(if (isSz) accentColor else Color(0xFF191919), RoundedCornerShape(4.dp))
                                            .clickable { onSetWidgetSize(id, szId) }
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

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
                                    fontFamily = FontFamily.Monospace
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(false to "12H", true to "24H").forEach { (is24, lbl) ->
                                        val sel = use24Hour == is24
                                        Text(
                                            text = lbl,
                                            color = if (sel) Color.Black else Color.White,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
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

                        if (id == "globe" || id == "crt_world_map") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "VISUAL THEME",
                                color = Color(0xFF8A8A8A),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
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
                                        fontFamily = FontFamily.Monospace,
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
                                    fontFamily = FontFamily.Monospace
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
                                                fontFamily = FontFamily.Monospace
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
                                    fontFamily = FontFamily.Monospace
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
    lomoFontFamily: FontFamily,
    onChangeFont: (String) -> Unit,
    onChangeAlign: (String) -> Unit,
    onChangeAccent: (String) -> Unit,
    onChangeTextSize: (Int) -> Unit,
    onChangeDrawerTextSize: (Int) -> Unit,
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
    onOpenSystemWallpaperPicker: () -> Unit,
    onPickCustomWallpaperImage: () -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onBack: () -> Unit
) {
    var expandedSystem by remember { mutableStateOf(false) }
    var expandedTypography by remember { mutableStateOf(false) }
    var expandedWallpaper by remember { mutableStateOf(false) }
    var expandedMotion by remember { mutableStateOf(false) }

    // Separate collapsible sub-sections inside "Wallpapers and Settings"
    var expandedLiveWallpapers by remember { mutableStateOf(false) }
    var expandedStaticWallpapers by remember { mutableStateOf(false) }
    var expandedUserWallpapers by remember { mutableStateOf(false) }

    val accentColor = resolveLauncherAccentColor(accentHex)

    // 8 Minimal, Full-Screen AMOLED Live Wallpapers (including Glowing & Halftone Wave Live Wallpapers)
    val liveWallpaperOptions = listOf(
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold
                )
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

        item {
            CollapsibleSettingsSection(
                indexTag = "01",
                title = "System & Home Integration",
                subtitle = "Default launcher role & Android status bar",
                badge = if (hideAndroidBar) "IMMERSIVE" else "STANDARD",
                expanded = expandedSystem,
                accentColor = accentColor,
                lomoFontFamily = lomoFontFamily,
                onToggle = { expandedSystem = !expandedSystem }
            ) {
                SettingCard(
                    title = "Default Home Launcher",
                    subtitle = "Set N-TEXT as your primary Android 14 Home App",
                    actionLabel = "CHOOSE HOME",
                    lomoFontFamily = lomoFontFamily,
                    onClick = onRequestDefaultHomeRole
                )
                SettingCard(
                    title = "Hide Android Status Bar",
                    subtitle = "Swipe down on Home Screen anytime to view notifications",
                    actionLabel = if (hideAndroidBar) "HIDDEN (ON)" else "SHOWN (OFF)",
                    lomoFontFamily = lomoFontFamily,
                    onClick = onToggleAndroidBar
                )
            }
        }

        item {
            val activeFontName = PrebuiltFont.fromId(fontId).displayName.substringBefore(" (")
            CollapsibleSettingsSection(
                indexTag = "02",
                title = "Typography & Layout",
                subtitle = "Typeface, Home & App Drawer text scale, alignment",
                badge = "H:${textSizeSp} · D:${drawerTextSizeSp}SP",
                expanded = expandedTypography,
                accentColor = accentColor,
                lomoFontFamily = lomoFontFamily,
                onToggle = { expandedTypography = !expandedTypography }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
                        .background(Color(0xFF070707), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "TYPEFACE ($activeFontName)",
                        color = Color(0xFF8A8A8A),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
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
                            Text(
                                text = f.displayName,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = f.resolveFontFamily(lomoFontFamily)
                            )
                            Text(
                                text = if (selected) "●" else "○",
                                color = if (selected) accentColor else Color(0xFF525252),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
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
                        fontFamily = FontFamily.Monospace
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
                                fontFamily = FontFamily.Monospace,
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

                // App Drawer App Name Scale (Slider + Quick Presets)
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
                                fontFamily = FontFamily.Monospace,
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

                SettingCard(
                    title = "Home Text Alignment",
                    subtitle = "Left, Center, or Right aligned apps",
                    actionLabel = textAlignMode.uppercase(Locale.US),
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
                lomoFontFamily = lomoFontFamily,
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
                                fontFamily = FontFamily.Monospace,
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
                    lomoFontFamily = lomoFontFamily,
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
                                            fontFamily = FontFamily.Monospace,
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
                                        fontFamily = FontFamily.Monospace
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
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "RESET [1.0x]",
                                color = Color(0xFFA3A3A3),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
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
                            onSelect = onChangeWpSpeed
                        )

                        WallpaperTuningRow(
                            label = "ELEMENT DENSITY",
                            currentValue = wpDensity,
                            options = listOf(0.5f to "0.5x", 0.75f to "0.75x", 1.0f to "1.0x", 1.35f to "1.35x", 1.75f to "1.75x"),
                            accentColor = accentColor,
                            onSelect = onChangeWpDensity
                        )

                        WallpaperTuningRow(
                            label = "GLOW / OPACITY",
                            currentValue = wpOpacity,
                            options = listOf(0.4f to "40%", 0.7f to "70%", 1.0f to "100%", 1.35f to "135%", 1.75f to "175%"),
                            accentColor = accentColor,
                            onSelect = onChangeWpOpacity
                        )

                        WallpaperTuningRow(
                            label = "SCALE / REACH",
                            currentValue = wpScale,
                            options = listOf(0.65f to "0.65x", 0.85f to "0.85x", 1.0f to "1.0x", 1.25f to "1.25x", 1.5f to "1.5x"),
                            accentColor = accentColor,
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
                    lomoFontFamily = lomoFontFamily,
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
                                            fontFamily = FontFamily.Monospace,
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
                                        fontFamily = FontFamily.Monospace
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
                    lomoFontFamily = lomoFontFamily,
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
                        lomoFontFamily = lomoFontFamily,
                        onClick = onPickCustomWallpaperImage
                    )

                    if (hasCustomUserImage && wallpaperPreset != "user_image") {
                        SettingCard(
                            title = "Activate Saved Custom Photo",
                            subtitle = "Switch back to your previously imported custom wallpaper",
                            actionLabel = "USE SAVED",
                            lomoFontFamily = lomoFontFamily,
                            onClick = { onChangeWallpaperPreset("user_image") }
                        )
                    }

                    SettingCard(
                        title = "System / Third-Party Wallpaper Picker",
                        subtitle = "Pick a wallpaper from Android or any 3rd-party wallpaper app",
                        actionLabel = "OPEN PICKER",
                        lomoFontFamily = lomoFontFamily,
                        onClick = onOpenSystemWallpaperPicker
                    )

                    SettingCard(
                        title = "Use Active System / Third-Party Wallpaper",
                        subtitle = "Display your current system wallpaper behind the launcher with ${wallpaperDimPercent}% dimmer",
                        actionLabel = if (wallpaperPreset == "system_wallpaper") "ACTIVE (ON)" else "ENABLE",
                        lomoFontFamily = lomoFontFamily,
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
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = accentHex,
                            color = accentColor,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
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
                                        fontFamily = FontFamily.Monospace,
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
        // SECTION 04: Motion & Gesture Physics
        // Includes Slider to Increase or Decrease the App Drawer Curved Horizontal Wave
        // ====================================================================
        item {
            val wavePct = (drawerWaveIntensity * 100f).roundToInt()
            CollapsibleSettingsSection(
                indexTag = "04",
                title = "Motion & Gesture Physics",
                subtitle = "App drawer curved horizontal wave slider & tactile feedback",
                badge = "WAVE ${wavePct}%",
                expanded = expandedMotion,
                accentColor = accentColor,
                lomoFontFamily = lomoFontFamily,
                onToggle = { expandedMotion = !expandedMotion }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
                        .background(Color(0xFF070707), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinimalistSliderRow(
                        label = "APP DRAWER CURVED HORIZONTAL WAVE",
                        valueText = "${wavePct}%",
                        fraction = (drawerWaveIntensity / 2.0f).coerceIn(0f, 1f),
                        accentColor = accentColor,
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
                                fontFamily = FontFamily.Monospace,
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

                SettingCard(
                    title = "Fluid Wave Scrubbing Mode",
                    subtitle = "Toggle preset wave mode for Home dots & A–Z rail",
                    actionLabel = waveScrubMode.uppercase(Locale.US),
                    lomoFontFamily = lomoFontFamily,
                    onClick = {
                        val next = when (waveScrubMode) {
                            "full" -> "subtle"
                            "subtle" -> "off"
                            else -> "full"
                        }
                        onChangeWaveMode(next)
                        when (next) {
                            "full" -> onChangeDrawerWaveIntensity(1.0f)
                            "subtle" -> onChangeDrawerWaveIntensity(0.5f)
                            "off" -> onChangeDrawerWaveIntensity(0.0f)
                        }
                    }
                )
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
                    fontFamily = FontFamily.Monospace
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
                    fontFamily = FontFamily.Monospace,
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
                    fontFamily = FontFamily.Monospace
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
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = valueText,
                color = accentColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
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
                color = if (expanded) Color(0xFF383838) else Color(0xFF1F1F1F),
                shape = RoundedCornerShape(14.dp)
            )
            .background(Color(0xFF0A0A0A), RoundedCornerShape(14.dp))
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
                    fontFamily = FontFamily.Monospace,
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
                        fontFamily = FontFamily.Monospace
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
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(Color(0xFF171717), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
                Text(
                    text = if (expanded) "▾" else "▸",
                    color = if (expanded) accentColor else Color(0xFF737373),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
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
// INTERACTIVE WALLPAPER TUNING ROW COMPOSABLE
// ============================================================================
@Composable
fun WallpaperTuningRow(
    label: String,
    currentValue: Float,
    options: List<Pair<Float, String>>,
    accentColor: Color,
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
                fontFamily = FontFamily.Monospace
            )
            val activeLabel = options.minByOrNull { abs(it.first - currentValue) }?.second ?: "${currentValue}x"
            Text(
                text = activeLabel,
                color = accentColor,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
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
                    fontFamily = FontFamily.Monospace,
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
@Composable
fun OledWallpaperCanvas(
    preset: String,
    accentColor: Color,
    dimPercent: Int,
    wpSpeed: Float = 1.0f,
    wpDensity: Float = 1.0f,
    wpOpacity: Float = 1.0f,
    wpScale: Float = 1.0f,
    userWallpaperRefreshTrigger: Int = 0
) {
    val context = LocalContext.current
    val isLive = preset.startsWith("live_")
    var liveTimeSec by remember { mutableStateOf(0f) }
    val currentSpeed by rememberUpdatedState(wpSpeed.coerceIn(0.15f, 3.0f))

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

    if (isLive) {
        LaunchedEffect(preset) {
            var lastNanos = 0L
            while (true) {
                withFrameNanos { nanos ->
                    val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    lastNanos = nanos
                    liveTimeSec = (liveTimeSec + dt * 0.42f * currentSpeed) % 7200f
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
    onToggle24h: () -> Unit
) {
    val tileHeight = when (sizeMode) {
        "compact" -> 42.dp
        "large" -> 64.dp
        "xlarge" -> 76.dp
        else -> 52.dp
    }
    val digitFontSize = when (sizeMode) {
        "compact" -> 22.sp
        "large" -> 34.sp
        "xlarge" -> 40.sp
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
            .clickable { onToggle24h() }
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
                    fontFamily = FontFamily.Monospace
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
                fontFamily = FontFamily.Monospace,
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
    onToggle24h: () -> Unit
) {
    val dialSize = when (sizeMode) {
        "compact" -> 64.dp
        "large" -> 106.dp
        "xlarge" -> 128.dp
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
            .clickable { onToggle24h() }
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
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            Text(
                text = dateStr,
                color = Color(0xFF8A8A8A),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
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
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (running) "PAUSE" else "START",
                    color = if (running) Color.Black else Color.White,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
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
                    fontFamily = FontFamily.Monospace,
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
                fontFamily = FontFamily.Monospace,
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
                    fontFamily = FontFamily.Monospace,
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
                                fontFamily = FontFamily.Monospace,
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
                        fontFamily = FontFamily.Monospace,
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
                fontFamily = FontFamily.Monospace,
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
                fontFamily = FontFamily.Monospace,
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
                fontFamily = FontFamily.Monospace,
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

// ============================================================================
// INTERACTIVE FLAT CRT WORLD MAP WIDGET ("crt_world_map")
// Flat 2D equirectangular world map rendered like a vintage 80s-90s CRT screen:
// - 106 high-precision country borders from WORLD_COUNTRY_BORDERS
// - Interactive finger drag to pan across longitudes/latitudes + move target reticle (⊕)
// - Double-tap to cycle CRT zoom (1.0x -> 1.8x -> 2.8x)
// - Horizontal CRT phosphor scanlines, lat/lon graticule grid & live UTC solar terminator wave
// ============================================================================
@Composable
fun RetroCrtFlatWorldMapWidget(
    currentTimeMillis: Long,
    mapHeight: androidx.compose.ui.unit.Dp,
    globeTheme: String,
    globeWireframeColor: String,
    accentColor: Color,
    onTick: () -> Unit
) {
    var centerLon by remember { mutableStateOf(20f) }
    var centerLat by remember { mutableStateOf(15f) }
    var zoomLevel by remember { mutableStateOf(1.0f) }
    var reticleLon by remember { mutableStateOf(83.4f) } // Raigarh longitude
    var reticleLat by remember { mutableStateOf(21.9f) } // Raigarh latitude

    val isWireframe = globeTheme == "wireframe"
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

    val landFillColor = if (isWireframe) {
        wfColor.copy(alpha = 0.12f)
    } else {
        Color(0xFF00E676).copy(alpha = 0.22f)
    }
    val landBorderColor = if (isWireframe) {
        wfColor.copy(alpha = 0.88f)
    } else {
        Color(0xFF44FF99).copy(alpha = 0.92f)
    }
    val oceanBgColor = if (isWireframe) {
        Color(0xFF040404)
    } else {
        Color(0xFF030B14)
    }
    val gridColor = if (isWireframe) {
        wfColor.copy(alpha = 0.14f)
    } else {
        Color(0xFF00B4FF).copy(alpha = 0.16f)
    }

    val latHem = if (reticleLat >= 0f) "N" else "S"
    val lonHem = if (reticleLon >= 0f) "E" else "W"
    val coordReadout = String.format(
        Locale.US,
        "%.1f°%s %.1f°%s",
        abs(reticleLat),
        latHem,
        abs(reticleLon),
        lonHem
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(mapHeight)
            .border(1.dp, Color(0xFF282828), RoundedCornerShape(10.dp))
            .background(oceanBgColor, RoundedCornerShape(10.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onTick()
                        zoomLevel = when {
                            zoomLevel < 1.4f -> 1.8f
                            zoomLevel < 2.3f -> 2.8f
                            else -> 1.0f
                        }
                        if (zoomLevel == 1.0f) {
                            centerLat = 10f
                        }
                    },
                    onTap = { tapOffset ->
                        onTick()
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val h = size.height.toFloat().coerceAtLeast(1f)
                        val spanLon = 360f / zoomLevel
                        val spanLat = 165f / zoomLevel
                        val lon = centerLon + ((tapOffset.x / w) - 0.5f) * spanLon
                        val lat = centerLat - ((tapOffset.y / h) - 0.5f) * spanLat
                        reticleLon = (((lon + 180f) % 360f + 360f) % 360f) - 180f
                        reticleLat = lat.coerceIn(-80f, 80f)
                    }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var prevPos = down.position
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            val delta = change.position - prevPos
                            if (delta.getDistance() > 0.5f) {
                                change.consume()
                                val w = size.width.toFloat().coerceAtLeast(1f)
                                val h = size.height.toFloat().coerceAtLeast(1f)
                                val spanLon = 360f / zoomLevel
                                val spanLat = 165f / zoomLevel
                                centerLon = (((centerLon - (delta.x / w) * spanLon + 180f) % 360f + 360f) % 360f) - 180f
                                val maxLatPan = (82f - spanLat * 0.42f).coerceAtLeast(12f)
                                centerLat = (centerLat + (delta.y / h) * spanLat).coerceIn(-maxLatPan, maxLatPan)
                                reticleLon = centerLon
                                reticleLat = centerLat
                                prevPos = change.position
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val spanLon = 360f / zoomLevel
            val spanLat = 165f / zoomLevel

            fun mapToScreen(lat: Float, lon: Float): Offset {
                var dLon = lon - centerLon
                while (dLon > 180f) dLon -= 360f
                while (dLon < -180f) dLon += 360f
                val x = (0.5f + dLon / spanLon) * w
                val y = (0.5f - (lat - centerLat) / spanLat) * h
                return Offset(x, y)
            }

            // 1. CRT Lat/Lon Graticule Grid (every 30 deg)
            var lonGrid = -180f
            while (lonGrid <= 150f) {
                val pt = mapToScreen(0f, lonGrid)
                if (pt.x in 0f..w) {
                    val isPrime = abs(lonGrid) < 0.1f
                    drawLine(
                        color = if (isPrime) gridColor.copy(alpha = 0.30f) else gridColor,
                        start = Offset(pt.x, 0f),
                        end = Offset(pt.x, h),
                        strokeWidth = 1f,
                        pathEffect = if (isPrime) PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) else null
                    )
                }
                lonGrid += 30f
            }

            var latGrid = -60f
            while (latGrid <= 60f) {
                val pt = mapToScreen(latGrid, centerLon)
                if (pt.y in 0f..h) {
                    val isEquator = abs(latGrid) < 0.1f
                    drawLine(
                        color = if (isEquator) gridColor.copy(alpha = 0.32f) else gridColor,
                        start = Offset(0f, pt.y),
                        end = Offset(w, pt.y),
                        strokeWidth = 1f,
                        pathEffect = if (isEquator) PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) else null
                    )
                }
                latGrid += 30f
            }

            // 2. Flat 2D Plane Projection of all 106 Country Borders (with horizontal wrap handling)
            val borderStroke = Stroke(width = 1.15.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            for (poly in WORLD_COUNTRY_BORDERS) {
                if (poly.size < 3) continue
                val fillPath = Path()
                val strokePath = Path()
                var prevScreen: Offset? = null
                var hasDiscontinuity = false

                for (i in poly.indices) {
                    val (lat, lon) = poly[i]
                    val pt = mapToScreen(lat, lon)
                    if (prevScreen == null) {
                        fillPath.moveTo(pt.x, pt.y)
                        strokePath.moveTo(pt.x, pt.y)
                    } else {
                        // Avoid drawing horizontal wrap-around lines across the entire CRT screen
                        if (abs(pt.x - prevScreen.x) > w * 0.45f) {
                            hasDiscontinuity = true
                            strokePath.moveTo(pt.x, pt.y)
                            fillPath.moveTo(pt.x, pt.y)
                        } else {
                            strokePath.lineTo(pt.x, pt.y)
                            fillPath.lineTo(pt.x, pt.y)
                        }
                    }
                    prevScreen = pt
                }
                if (!hasDiscontinuity) {
                    fillPath.close()
                    drawPath(path = fillPath, color = landFillColor)
                }
                drawPath(path = strokePath, color = landBorderColor, style = borderStroke)
            }

            // 3. Live UTC Solar Terminator Wave across the flat CRT map
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = currentTimeMillis
            }
            val utcHours = utcCal.get(Calendar.HOUR_OF_DAY) + utcCal.get(Calendar.MINUTE) / 60f
            val subSolarLon = (12f - utcHours) * 15f
            val terminatorPath = Path()
            val tSteps = 48
            for (s in 0..tSteps) {
                val x = (s.toFloat() / tSteps) * w
                val lonAtX = centerLon + ((x / w) - 0.5f) * spanLon
                val hourAngleRad = Math.toRadians((lonAtX - subSolarLon).toDouble())
                val termLat = (23.4f * cos(hourAngleRad)).toFloat()
                val pt = mapToScreen(termLat, lonAtX)
                if (s == 0) terminatorPath.moveTo(x, pt.y.coerceIn(0f, h))
                else terminatorPath.lineTo(x, pt.y.coerceIn(0f, h))
            }
            drawPath(
                path = terminatorPath,
                color = accentColor.copy(alpha = 0.50f),
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                )
            )

            // 4. Interactive CRT Target Crosshair Reticle (⊕)
            val retPt = mapToScreen(reticleLat, reticleLon)
            if (retPt.x in 0f..w && retPt.y in 0f..h) {
                drawLine(
                    color = accentColor.copy(alpha = 0.35f),
                    start = Offset(retPt.x, 0f),
                    end = Offset(retPt.x, h),
                    strokeWidth = 1f
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.35f),
                    start = Offset(0f, retPt.y),
                    end = Offset(w, retPt.y),
                    strokeWidth = 1f
                )
                drawCircle(
                    color = accentColor,
                    radius = 5.5.dp.toPx(),
                    center = retPt,
                    style = Stroke(width = 1.2.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.8.dp.toPx(),
                    center = retPt
                )
            }

            // 5. Retro CRT Phosphor Horizontal Scanline Overlay & Corner Bezel Ticks
            val scanStep = 3.5.dp.toPx().coerceAtLeast(3f)
            var sy = 0f
            while (sy < h) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.28f),
                    start = Offset(0f, sy),
                    end = Offset(w, sy),
                    strokeWidth = 1f
                )
                sy += scanStep
            }

            // Inner CRT bezel frame
            val inset = 4.dp.toPx()
            drawRoundRect(
                color = landBorderColor.copy(alpha = 0.24f),
                topLeft = Offset(inset, inset),
                size = Size(w - inset * 2f, h - inset * 2f),
                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Minimal coordinate & zoom pill in bottom-right corner
        Text(
            text = if (zoomLevel > 1.05f) "$coordReadout · ${String.format(Locale.US, "%.1f", zoomLevel)}×" else coordReadout,
            color = Color(0xFFB0B0B0),
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 6.dp)
                .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}
