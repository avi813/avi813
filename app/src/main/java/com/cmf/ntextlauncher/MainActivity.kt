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
import android.hardware.camera2.CameraManager
import android.app.ActivityManager
import android.os.StatFs
import android.os.Environment
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
    private var homeIntentTrigger by mutableStateOf(0)

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
                onDismissKeyboardHard = { dismissKeyboardHard() },
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
    homeIntentTrigger: Int,
    onDismissKeyboardHard: () -> Unit,
    onToggleAndroidStatusBar: (Boolean) -> Unit,
    onExpandNotifications: () -> Unit,
    onRequestDefaultHomeRole: () -> Unit,
    onOpenSystemWallpaperPicker: () -> Unit,
    onRequestSystemUninstall: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
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
    var globeTheme by remember { mutableStateOf(prefs.getString("globe_theme", "retro_holo") ?: "retro_holo") }
    var globeWireframeColor by remember { mutableStateOf(prefs.getString("globe_wf_color", "#FF3B00") ?: "#FF3B00") }
    var blackHoleAccentColor by remember { mutableStateOf(prefs.getString("bh_accent_color", "#FF3B00") ?: "#FF3B00") }

    // Removable Widgets Order, Visibility & Sizing (Compact, Standard, Large)
    // Uses widgets_order_v5 so existing installs automatically get the resizable "globe" widget and can add/toggle "blackhole"!
    var activeWidgets by remember {
        val saved = prefs.getString("widgets_order_v5", "clock,weather,globe,blackhole") ?: "clock,weather,globe,blackhole"
        mutableStateOf(saved.split(",").filter { it.isNotBlank() && it != "telemetry" })
    }
    var widgetSizes by remember {
        val saved = prefs.getString("widget_sizes_v5", "clock:standard,weather:standard,globe:standard,blackhole:standard,torch:standard,pedometer:standard,storage:standard,quote:standard,scratchpad:standard,zen:standard") ?: ""
        val map = saved.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
        mutableStateOf(map)
    }
    var torchEnabled by remember { mutableStateOf(false) }
    var pedometerSteps by remember { mutableStateOf(6420) }
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
        prefs.edit().putString("widgets_order_v5", updated.joinToString(",")).apply()
    }

    fun saveWidgetSize(widgetId: String, size: String) {
        val newMap = widgetSizes.toMutableMap()
        newMap[widgetId] = size
        widgetSizes = newMap.toMap()
        val serialized = newMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        prefs.edit().putString("widget_sizes_v5", serialized).apply()
    }

    fun cycleWidgetSize(widgetId: String) {
        val current = widgetSizes[widgetId] ?: "standard"
        val next = when (current) {
            "compact" -> "standard"
            "standard" -> "large"
            else -> "compact"
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
        context.packageManager.getLaunchIntentForPackage(pkg)?.let {
            context.startActivity(it)
        }
    }

    // Whenever the user swipes up from the Android 14 navigation bar or returns from Recents,
    // immediately return from Settings / Widgets / Modals / Drawer to the clean Home Screen
    // and ensure the keyboard stays closed!
    LaunchedEffect(homeIntentTrigger) {
        if (homeIntentTrigger > 0) {
            clearAllFocusAndKeyboard()
            oledSleepActive = false
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
                        // 1. HOME SCREEN LAYER (Reads drawerProgress.value ONLY inside graphicsLayer -> 0 recompositions, 120 FPS!)
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
                                .pointerInput(doubleTapSleep) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            if (doubleTapSleep && drawerProgress.value < 0.1f) oledSleepActive = true
                                        },
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
                                    currentHourStr = currentHourStr,
                                    currentMinStr = currentMinStr,
                                    currentAmPmStr = currentAmPmStr,
                                    currentDateStr = currentDateStr,
                                    nextAlarmStr = nextAlarmStr,
                                    batteryPct = batteryPct,
                                    isCharging = isCharging,
                                    tempCelsius = tempCelsius,
                                    torchEnabled = torchEnabled,
                                    pedometerSteps = pedometerSteps,
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
                                    globeTheme = globeTheme,
                                    globeWireframeColor = globeWireframeColor,
                                    blackHoleAccentColor = blackHoleAccentColor,
                                    pinnedApps = installedApps.filter { it.isPinned && !it.isHidden },
                                    onToggle24h = {
                                        use24Hour = !use24Hour
                                        prefs.edit().putBoolean("use_24h_v4", use24Hour).apply()
                                    },
                                    onToggleTempUnit = {
                                        tempCelsius = !tempCelsius
                                        prefs.edit().putBoolean("temp_c", tempCelsius).apply()
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

                        // 2. APP DRAWER LAYER (Reads drawerProgress.value ONLY inside graphicsLayer -> locked 120 FPS!)
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
                                accentColor = accentColor,
                                niagaraWaveMode = niagaraWaveMode,
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
                            globeTheme = globeTheme,
                            globeWireframeColor = globeWireframeColor,
                            blackHoleAccentColor = blackHoleAccentColor,
                            onChangeGlobeTheme = {
                                globeTheme = it
                                prefs.edit().putString("globe_theme", it).apply()
                            },
                            onChangeGlobeWireframeColor = {
                                globeWireframeColor = it
                                prefs.edit().putString("globe_wf_color", it).apply()
                            },
                            onChangeBlackHoleColor = {
                                blackHoleAccentColor = it
                                prefs.edit().putString("bh_accent_color", it).apply()
                            },
                            onUpdateWidgets = { saveWidgetList(it) },
                            onCycleWidgetSize = { cycleWidgetSize(it) },
                            onMoveWidget = { id, dir -> moveWidget(id, dir) },
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
                            globeTheme = globeTheme,
                            globeWireframeColor = globeWireframeColor,
                            blackHoleAccentColor = blackHoleAccentColor,
                            onChangeGlobeTheme = {
                                globeTheme = it
                                prefs.edit().putString("globe_theme", it).apply()
                            },
                            onChangeGlobeWireframeColor = {
                                globeWireframeColor = it
                                prefs.edit().putString("globe_wf_color", it).apply()
                            },
                            onChangeBlackHoleColor = {
                                blackHoleAccentColor = it
                                prefs.edit().putString("bh_accent_color", it).apply()
                            },
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

                    // 5. Switch 3D Globe Theme (Retro Hologram vs Customizable Wireframe)
                    QuickMenuButton(
                        label = if (globeTheme == "retro_holo") "🌐 Globe: 80s Hologram (Green/Blue)" else "🌐 Globe: Wireframe ($globeWireframeColor)",
                        badge = "GLOBE",
                        accentColor = accentColor,
                        lomoFontFamily = lomoFontFamily,
                        onClick = {
                            val next = if (globeTheme == "retro_holo") "wireframe" else "retro_holo"
                            globeTheme = next
                            prefs.edit().putString("globe_theme", next).apply()
                            tickHaptic(heavy = false)
                        }
                    )

                    // 6. Launcher Settings
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
    widgetSizes: Map<String, String>,
    editMode: Boolean,
    currentHourStr: String,
    currentMinStr: String,
    currentAmPmStr: String,
    currentDateStr: String,
    nextAlarmStr: String,
    batteryPct: Int,
    isCharging: Boolean,
    tempCelsius: Boolean,
    torchEnabled: Boolean,
    pedometerSteps: Int,
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
    globeTheme: String,
    globeWireframeColor: String,
    blackHoleAccentColor: String,
    pinnedApps: List<AppEntry>,
    onToggle24h: () -> Unit,
    onToggleTempUnit: () -> Unit,
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
                                            // Compact Size: Single-line minimal clock with battery
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
                                                    text = "$currentDateStr · BAT $batteryPct%",
                                                    color = Color(0xFFA3A3A3),
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        "large" -> {
                                            // Large Size: Prominent Nothing Dot-Matrix Digital Display
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
                                                    text = "BAT $batteryPct%",
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
                                                Text(
                                                    text = if (isCharging) "CHG 33W" else "OLED SAVING",
                                                    color = if (isCharging) accentColor else Color(0xFF737373),
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        else -> {
                                            // Standard Size (The exact setup the user loves)
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
                                }
                            }
                            "weather" -> {
                                val temp = if (tempCelsius) "28°C" else "82°F"
                                when (sizeMode) {
                                    "compact" -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onToggleTempUnit() }
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "● $temp CLEAR SKY",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "AQI 42",
                                                color = Color(0xFFA3A3A3),
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    "large" -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onToggleTempUnit() }
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(7.dp)
                                                            .background(accentColor, CircleShape)
                                                    )
                                                    Text(
                                                        text = temp,
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "CLEAR SKY (H 31° / L 22°)",
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
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "HUMIDITY 48% · UV INDEX 2 LOW · BHOPAL",
                                                color = Color(0xFF737373),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    else -> {
                                        // Standard
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
                                                        text = temp,
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
                                }
                            }
                            "torch" -> {
                                // NEW WIDGET 1: CMF Torch / Glyph Flashlight Quick Toggle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleTorch() }
                                        .border(1.dp, if (torchEnabled) accentColor else Color(0xFF262626), RoundedCornerShape(8.dp))
                                        .background(if (torchEnabled) accentColor.copy(alpha = 0.12f) else Color.Transparent, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
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
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = if (sizeMode == "large") "CMF GLYPH TORCH // FLASHLIGHT" else "CMF TORCH",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = if (torchEnabled) "ACTIVE [ON]" else "READY [OFF]",
                                        color = if (torchEnabled) accentColor else Color(0xFF737373),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            "pedometer" -> {
                                // NEW WIDGET 2: Steps & Activity Counter (10,000 Step Goal)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    val km = "%.1f".format(Locale.US, pedometerSteps * 0.00075f)
                                    val pct = (pedometerSteps / 10000f).coerceIn(0f, 1f)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "STEPS // $pedometerSteps",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "$km KM · 10K GOAL",
                                            color = Color(0xFFA3A3A3),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    if (sizeMode != "compact") {
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .background(Color(0xFF141414), RoundedCornerShape(999.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(pct)
                                                    .background(accentColor, RoundedCornerShape(999.dp))
                                            )
                                        }
                                    }
                                }
                            }
                            "storage" -> {
                                // NEW WIDGET 3: OLED RAM & Storage Telemetry Bar
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "RAM 2.4/8 GB",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "STORAGE 64/128 GB",
                                            color = Color(0xFFA3A3A3),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    if (sizeMode != "compact") {
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(4.dp)
                                                    .background(Color(0xFF141414), RoundedCornerShape(999.dp))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(0.30f)
                                                        .background(accentColor, RoundedCornerShape(999.dp))
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(4.dp)
                                                    .background(Color(0xFF141414), RoundedCornerShape(999.dp))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(0.50f)
                                                        .background(Color(0xFF555555), RoundedCornerShape(999.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            "quote" -> {
                                // NEW WIDGET 4: Minimalist Thought / Design Ethos Quote
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "“Simplify, then add lightness.”",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontFamily = lomoFontFamily,
                                        lineHeight = 14.sp
                                    )
                                    if (sizeMode != "compact") {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "— COLIN CHAPMAN · NOTHING ETHOS",
                                            color = Color(0xFF737373),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
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
                                        text = if (sizeMode == "compact") "ZEN $mm:$ss" else "ZEN FOCUS · $mm:$ss",
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
                            "globe" -> {
                                val globeHeight = when (sizeMode) {
                                    "compact" -> 130.dp
                                    "large" -> 250.dp
                                    else -> 185.dp
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
                            "blackhole" -> {
                                val bhHeight = when (sizeMode) {
                                    "compact" -> 115.dp
                                    "large" -> 220.dp
                                    else -> 160.dp
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(bhHeight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GlowingBlackHoleWidgetView(
                                        accentHex = blackHoleAccentColor
                                    )
                                }
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
            val maxWaveShiftPx = with(density) { (if (niagaraWaveMode == "full") 32.dp else 16.dp).toPx() }
            val verticalSpreadPx = with(density) { 4.dp.toPx() }
            val pressHoverShiftPx = with(density) { 14.dp.toPx() }
            val maxDotWaveShiftPx = with(density) { 14.dp.toPx() }

            val activeFocusedIndex = remember(waveFloatIndex, pinnedApps.size) {
                val w = waveFloatIndex
                if (w == null || pinnedApps.isEmpty() || niagaraWaveMode == "off") {
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

                        val targetWaveFactor = if (waveFloatIndex != null && niagaraWaveMode != "off") {
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
                if (niagaraWaveMode != "off" && pinnedApps.isNotEmpty()) {
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
            // Larger globe radius (0.47f of available middle space) with zero text/shape clutter
            val radius = (min(w, h) * 0.47f).coerceAtLeast(36f)

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
// GARGANTUA ASTROPHYSICAL BLACK HOLE WIDGET (INTERSTELLAR RELATIVISTIC RENDERER)
// Redesigned from scratch to replicate the Kip Thorne / Double Negative gravitational
// lensing appearance of Gargantua in Interstellar:
// - Volumetric atmospheric bloom & gravitational light-bending corona
// - Warped upper & lower relativistic lensing domes (back of the accretion disk bent
//   90 degrees over the north and south poles of the black hole shadow)
// - Pure OLED black event horizon shadow surrounded by an incandescent white Einstein photon ring
// - Continuous multi-layered, optically thick equatorial accretion disk with relativistic
//   Doppler beaming (blindingly hot white on the approaching left side, custom glowing
//   corona color on the outer/receding side) and slow continuous relativistic shear rotation.
// - Non-interactive (passes all touches through to Home Screen) & Resizable (Compact/Standard/Large).
// ============================================================================
@Composable
fun GlowingBlackHoleWidgetView(
    accentHex: String
) {
    var spinPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = nanos
                spinPhase = (spinPhase + dt * 0.52f) % (2f * PI.toFloat())
            }
        }
    }

    val coronaColor = remember(accentHex) {
        when (accentHex.uppercase(Locale.US)) {
            "#FFFFFF" -> Color(0xFFD8E6FF) // Pure Starlight Silver-White
            "#00E5FF" -> Color(0xFF00E5FF) // Quasar Cyan
            "#A855F7" -> Color(0xFFA855F7) // Nebula Violet
            "#00FF66" -> Color(0xFF00FF66) // Emerald Plasma
            "#FFB000" -> Color(0xFFFFAA22) // Interstellar Gargantua Warm Gold
            "#D71921" -> Color(0xFFFF2A2A) // Crimson Redshift
            else -> Color(0xFFFF5511)      // CMF / Gargantua Relativistic Amber-Orange
        }
    }

    // Pure non-interactive Canvas so Home Screen swipes pass right through
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        // Event horizon shadow radius scaled cleanly to widget container
        val rShadow = (min(w * 0.42f, h * 0.56f) * 0.44f).coerceAtLeast(16f)
        val diskRx = rShadow * 2.85f
        val diskRy = rShadow * 0.28f
        val tiltDeg = -9.5f
        val tiltRad = (tiltDeg * PI / 180.0).toFloat()
        val cosT = cos(tiltRad)
        val sinT = sin(tiltRad)

        fun rot(x: Float, y: Float): Offset {
            return Offset(
                cx + x * cosT - y * sinT,
                cy + x * sinT + y * cosT
            )
        }

        // 1. DEEP SPACE GRAVITATIONAL BLOOM (Multi-stage radial atmospheric glow)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.26f),
                    coronaColor.copy(alpha = 0.18f),
                    coronaColor.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = rShadow * 2.95f
            ),
            radius = rShadow * 2.95f,
            center = Offset(cx, cy)
        )

        // Asymmetric Doppler Beaming Bloom on the approaching (left) wing of the disk
        val dopplerCenter = rot(-rShadow * 1.15f, 0f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.28f),
                    coronaColor.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = dopplerCenter,
                radius = rShadow * 1.75f
            ),
            radius = rShadow * 1.75f,
            center = dopplerCenter
        )

        // 2. GRAVITATIONALLY LENSED UPPER & LOWER DOMES (Light from the back of the accretion disk
        // bent over the top and under the bottom of the black hole shadow, merging into the equatorial plane)
        // We draw concentric lensed light sheets from outer corona inward to the white-hot core.
        val domeLayers = 14
        for (layer in domeLayers downTo 0) {
            val t = layer.toFloat() / domeLayers.toFloat() // 1.0 = outer soft corona, 0.0 = inner incandescent white
            val spread = t * rShadow * 0.34f
            val strokeW = (1.5f + t * 11.5f).dp.toPx()
            val layerColor = if (t < 0.38f) {
                Color.White
            } else {
                // Smooth blend from White to coronaColor
                val blend = ((t - 0.38f) / 0.62f).coerceIn(0f, 1f)
                Color(
                    red = 1f * (1f - blend) + coronaColor.red * blend,
                    green = 1f * (1f - blend) + coronaColor.green * blend,
                    blue = 1f * (1f - blend) + coronaColor.blue * blend,
                    alpha = 1f
                )
            }
            val baseAlpha = if (t < 0.25f) 0.55f else (1f - t * 0.72f) * 0.14f

            // Upper Lensed Arch (bends over the North pole of the shadow and flares into the disk wings)
            val upperPath = Path()
            val steps = 40
            for (i in 0..steps) {
                val u = (i.toFloat() / steps) * PI.toFloat() // 0 (right wing) to PI (left wing)
                val cosU = cos(u)
                val sinU = sin(u)
                // Flattened near the equator, arching to rShadow * 1.04 + spread over the pole
                val flareX = (rShadow * 1.06f + spread * 1.35f) * cosU * (1f + 0.32f * (1f - sinU))
                val archY = -(rShadow * 1.01f + spread * 0.92f) * (sinU * 0.92f + 0.08f)
                val pt = rot(flareX, archY)
                if (i == 0) upperPath.moveTo(pt.x, pt.y) else upperPath.lineTo(pt.x, pt.y)
            }
            drawPath(
                path = upperPath,
                color = layerColor.copy(alpha = (baseAlpha * 1.15f).coerceIn(0f, 0.95f)),
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Lower Lensed Under-Arch (Secondary image bent under the South pole of the shadow)
            val lowerPath = Path()
            for (i in 0..steps) {
                val u = (i.toFloat() / steps) * PI.toFloat()
                val cosU = cos(u)
                val sinU = sin(u)
                val flareX = (rShadow * 1.04f + spread * 1.15f) * cosU * (1f + 0.24f * (1f - sinU))
                val archY = (rShadow * 0.99f + spread * 0.74f) * (sinU * 0.90f + 0.10f)
                val pt = rot(flareX, archY)
                if (i == 0) lowerPath.moveTo(pt.x, pt.y) else lowerPath.lineTo(pt.x, pt.y)
            }
            drawPath(
                path = lowerPath,
                color = layerColor.copy(alpha = (baseAlpha * 0.82f).coerceIn(0f, 0.85f)),
                style = Stroke(width = strokeW * 0.88f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Subtle animated relativistic plasma caustics flowing along the upper & lower lensed arches
        for (cIdx in 0 until 8) {
            val phaseU = ((spinPhase * 0.65f + cIdx * (PI.toFloat() / 4f)) % PI.toFloat())
            val sweepU = 0.42f
            val uStart = (phaseU - sweepU * 0.5f).coerceIn(0.08f, PI.toFloat() - 0.08f)
            val uEnd = (phaseU + sweepU * 0.5f).coerceIn(0.08f, PI.toFloat() - 0.08f)
            if (uEnd > uStart) {
                val archCaustic = Path()
                val cSteps = 12
                val radialOff = rShadow * (1.03f + (cIdx % 3) * 0.065f)
                for (k in 0..cSteps) {
                    val u = uStart + (k.toFloat() / cSteps) * (uEnd - uStart)
                    val pt = rot(radialOff * 1.12f * cos(u), -radialOff * sin(u))
                    if (k == 0) archCaustic.moveTo(pt.x, pt.y) else archCaustic.lineTo(pt.x, pt.y)
                }
                val dopplerBoost = (0.55f - 0.45f * cos(phaseU)).coerceIn(0.2f, 1f)
                drawPath(
                    path = archCaustic,
                    color = Color.White.copy(alpha = 0.32f * dopplerBoost),
                    style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // 3. BACK HALF OF EQUATORIAL ACCRETION DISK (Behind the Black Hole Shadow)
        val diskBands = 22
        for (b in diskBands downTo 0) {
            val f = b.toFloat() / diskBands.toFloat() // 0 = innermost photon edge, 1 = outer disk edge
            val rx = rShadow * 1.05f + f * (diskRx - rShadow * 1.05f)
            val ry = rShadow * 0.09f + f * (diskRy - rShadow * 0.09f)
            val backPath = Path()
            val bSteps = 32
            for (k in 0..bSteps) {
                // Back half: angle from PI to 2*PI
                val a = PI.toFloat() + (k.toFloat() / bSteps) * PI.toFloat()
                // Warp the back disk slightly upward near the shadow due to gravitational lensing
                val rNorm = (rx * cos(a)) / diskRx
                val lensLift = -rShadow * 0.18f * exp(-rNorm * rNorm * 3.5f) * (1f - f * 0.5f)
                val pt = rot(rx * cos(a), ry * sin(a) + lensLift)
                if (k == 0) backPath.moveTo(pt.x, pt.y) else backPath.lineTo(pt.x, pt.y)
            }
            val col = if (f < 0.35f) {
                Color.White
            } else {
                val blend = ((f - 0.35f) / 0.65f).coerceIn(0f, 1f)
                Color(
                    red = 1f * (1f - blend) + coronaColor.red * blend,
                    green = 1f * (1f - blend) + coronaColor.green * blend,
                    blue = 1f * (1f - blend) + coronaColor.blue * blend,
                    alpha = 1f
                )
            }
            val alpha = ((1f - f * 0.78f) * 0.24f).coerceIn(0.03f, 0.35f)
            drawPath(
                path = backPath,
                color = col.copy(alpha = alpha),
                style = Stroke(width = (2.2f + (1f - f) * 3.5f).dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // 4. EVENT HORIZON SHADOW & EINSTEIN PHOTON RING (Pure #000000 Void + Blinding White Inner Ring)
        drawCircle(
            color = Color.Black,
            radius = rShadow * 0.99f,
            center = Offset(cx, cy)
        )
        // Outer soft photon halo
        drawCircle(
            color = coronaColor.copy(alpha = 0.48f),
            radius = rShadow * 1.02f,
            center = Offset(cx, cy),
            style = Stroke(width = 4.5.dp.toPx())
        )
        // Inner incandescent white photon ring (with slight left-side Doppler brightening)
        drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = rShadow * 0.995f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.6.dp.toPx())
        )

        // 5. FRONT EQUATORIAL ACCRETION DISK SHEET (Passing directly in front of the Black Hole Shadow)
        // First draw a soft volumetric glow across the entire front equatorial plane
        val leftWing = rot(-diskRx * 1.04f, 0f)
        val rightWing = rot(diskRx * 1.04f, 0f)
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    coronaColor.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.75f),
                    Color.White.copy(alpha = 0.65f),
                    coronaColor.copy(alpha = 0.28f),
                    Color.Transparent
                ),
                start = leftWing,
                end = rightWing
            ),
            start = leftWing,
            end = rightWing,
            strokeWidth = (rShadow * 0.42f).coerceAtLeast(8f),
            cap = StrokeCap.Round
        )

        // Draw continuous high-density concentric gas bands across the front half (angle 0 to PI)
        for (b in diskBands downTo 0) {
            val f = b.toFloat() / diskBands.toFloat()
            val rx = rShadow * 1.02f + f * (diskRx - rShadow * 1.02f)
            val ry = rShadow * 0.07f + f * (diskRy - rShadow * 0.07f)
            val fSteps = 36
            var prevPt: Offset? = null
            for (k in 0..fSteps) {
                val a = (k.toFloat() / fSteps) * PI.toFloat() // 0 (right) to PI (left)
                val cosA = cos(a)
                val sinA = sin(a)
                val pt = rot(rx * cosA, ry * sinA)
                if (prevPt != null) {
                    // Relativistic Doppler beaming: left side (cosA < 0, approaching observer) is intensely bright white;
                    // right side (cosA > 0, receding) is warmer/dimmer with stronger coronaColor tint.
                    val doppler = (1.0f - 0.36f * cosA).coerceIn(0.55f, 1.38f)
                    // Subtle spiral shear wave rotating slowly with spinPhase
                    val spiralWave = 0.84f + 0.16f * sin(a * 3f - spinPhase * (2.2f - f * 1.1f) + b * 0.45f)
                    val radialFalloff = (1f - f).let { it * it * 0.65f + it * 0.35f }
                    val segAlpha = (radialFalloff * doppler * spiralWave * 0.58f).coerceIn(0.04f, 0.98f)

                    val whiteWeight = ((1f - f * 0.78f) * (if (cosA < 0f) 1.15f else 0.82f)).coerceIn(0f, 1f)
                    val segColor = Color(
                        red = 1f * whiteWeight + coronaColor.red * (1f - whiteWeight),
                        green = 1f * whiteWeight + coronaColor.green * (1f - whiteWeight),
                        blue = 1f * whiteWeight + coronaColor.blue * (1f - whiteWeight),
                        alpha = segAlpha
                    )
                    drawLine(
                        color = segColor,
                        start = prevPt,
                        end = pt,
                        strokeWidth = (1.4f + (1f - f) * 2.8f).dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                prevPt = pt
            }
        }

        // Razor-sharp incandescent white inner equatorial core line crossing the black hole shadow
        val corePath = Path()
        val coreSteps = 32
        for (k in 0..coreSteps) {
            val a = (k.toFloat() / coreSteps) * PI.toFloat()
            val pt = rot((rShadow * 1.55f + (diskRx - rShadow * 1.55f) * 0.25f) * cos(a), (rShadow * 0.11f) * sin(a))
            if (k == 0) corePath.moveTo(pt.x, pt.y) else corePath.lineTo(pt.x, pt.y)
        }
        drawPath(
            path = corePath,
            color = Color.White.copy(alpha = 0.94f),
            style = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

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
    accentColor: Color,
    niagaraWaveMode: String,
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

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Ultra-clean 120Hz LazyColumn:
                // Tight 20.dp bottom padding so scrolling stops cleanly right at the bottommost app with no wide gap!
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
                // Keeps touch scrub state isolated so LazyColumn NEVER recomposes during A-Z scrubbing!
                AlphabetScrubberRail(
                    accentColor = accentColor,
                    niagaraWaveMode = niagaraWaveMode,
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
    niagaraWaveMode: String,
    lomoFontFamily: FontFamily,
    onSelectLetter: (Char) -> Unit
) {
    val density = LocalDensity.current
    val letters = remember { ('A'..'Z').toList() }
    var railHeightPx by remember { mutableStateOf(1f) }
    var scrubLocalY by remember { mutableStateOf<Float?>(null) }
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    val currentOnSelect by rememberUpdatedState(onSelectLetter)

    val letterWaveRadiusPx = with(density) { 95.dp.toPx() }
    val maxLetterShiftPx = with(density) { 18.dp.toPx() }

    fun updateScrubFromLocalY(localY: Float, currentRailHeight: Float) {
        val validHeight = currentRailHeight.coerceAtLeast(1f)
        railHeightPx = validHeight
        val clampedLocalY = localY.coerceIn(0f, validHeight)
        scrubLocalY = clampedLocalY

        val idx = ((clampedLocalY / validHeight) * letters.size).toInt().coerceIn(0, letters.lastIndex)
        val chosenLetter = letters[idx]

        if (activeLetter != chosenLetter) {
            activeLetter = chosenLetter
            currentOnSelect(chosenLetter)
        }
    }

    Box(contentAlignment = Alignment.CenterEnd) {
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
                val targetLetterFactor = if (currentScrub != null && niagaraWaveMode != "off") {
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
                                scaleX = 1f + animLetterFactor * 0.35f
                                scaleY = 1f + animLetterFactor * 0.35f
                            }
                        }
                        .padding(vertical = 0.5.dp)
                )
            }
        }

        if (activeLetter != null) {
            Box(
                modifier = Modifier
                    .padding(end = 44.dp)
                    .size(52.dp)
                    .border(1.dp, accentColor, RoundedCornerShape(12.dp))
                    .background(Color.Black, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeLetter.toString(),
                    color = Color.White,
                    fontSize = 26.sp,
                    fontFamily = lomoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// MODULAR OLED WIDGETS MANAGER (Relocate, Resize, Add/Remove)
// ============================================================================
@Composable
fun WidgetsManagerView(
    activeWidgets: List<String>,
    widgetSizes: Map<String, String>,
    accentColor: Color,
    lomoFontFamily: FontFamily,
    globeTheme: String,
    globeWireframeColor: String,
    blackHoleAccentColor: String,
    onChangeGlobeTheme: (String) -> Unit,
    onChangeGlobeWireframeColor: (String) -> Unit,
    onChangeBlackHoleColor: (String) -> Unit,
    onUpdateWidgets: (List<String>) -> Unit,
    onCycleWidgetSize: (String) -> Unit,
    onMoveWidget: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    val allWidgets = listOf(
        "clock" to "Dot-Matrix Clock, Date, Alarm & Battery Bar",
        "weather" to "Minimal Weather & AQI Pill",
        "globe" to "3D Interactive Spinning Globe (Resizable & Removable)",
        "blackhole" to "Glowing Relativistic Black Hole (Non-Interactive · Resizable)",
        "torch" to "CMF Flashlight / Glyph Torch Quick Toggle",
        "pedometer" to "Minimal Steps & Daily Pedometer Goal",
        "storage" to "OLED RAM & Storage Telemetry Bar",
        "zen" to "Zen Focus Timer (25m)",
        "quote" to "Minimalist Thought / Design Ethos Quote",
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

        Text(
            text = "Relocate, resize, or toggle widgets for the pure OLED home screen:",
            color = Color(0xFF737373),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allWidgets) { (id, title) ->
                val enabled = activeWidgets.contains(id)
                val currentSize = widgetSizes[id] ?: "standard"
                val idx = activeWidgets.indexOf(id)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (enabled) Color(0xFF333333) else Color(0xFF1A1A1A), RoundedCornerShape(10.dp))
                        .background(if (enabled) Color(0xFF0A0A0A) else Color.Transparent, RoundedCornerShape(10.dp))
                        .padding(12.dp)
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

                    if (enabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (idx > 0) {
                                    Text(
                                        text = "↑ UP",
                                        color = Color(0xFFA3A3A3),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
                                            .clickable { onMoveWidget(id, -1) }
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
                                            .clickable { onMoveWidget(id, 1) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "SIZE: ${currentSize.uppercase()}",
                                color = accentColor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .clickable { onCycleWidgetSize(id) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (id == "globe") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("retro_holo" to "80s HOLOGRAM", "wireframe" to "WIREFRAME").forEach { (tId, tLabel) ->
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
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        if (id == "blackhole") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ACCRETION RING ACCENT (MAIN BODY: WHITE)",
                                color = Color(0xFF8A8A8A),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    "#FFFFFF" to "WHT",
                                    "#FF3B00" to "CMF",
                                    "#00E5FF" to "CYN",
                                    "#A855F7" to "PRP",
                                    "#00FF66" to "GRN",
                                    "#FFB000" to "GLD",
                                    "#D71921" to "RED"
                                ).forEach { (hex, label) ->
                                    val isSel = blackHoleAccentColor.equals(hex, ignoreCase = true)
                                    Text(
                                        text = label,
                                        color = if (isSel) Color.Black else Color.White,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(if (isSel) Color.White else Color(0xFF191919), RoundedCornerShape(4.dp))
                                            .clickable { onChangeBlackHoleColor(hex) }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
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
    globeTheme: String,
    globeWireframeColor: String,
    blackHoleAccentColor: String,
    onChangeGlobeTheme: (String) -> Unit,
    onChangeGlobeWireframeColor: (String) -> Unit,
    onChangeBlackHoleColor: (String) -> Unit,
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

        // 3B. 3D Interactive Globe Theme & Wireframe Color Customizer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("3D Home Globe Theme", color = Color.White, fontSize = 13.sp, fontFamily = lomoFontFamily)
                        Text(
                            text = "Switch between 80s Green/Blue Hologram & Custom Wireframe",
                            color = Color(0xFF737373),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "retro_holo" to "80s HOLOGRAM (GREEN/BLUE)",
                        "wireframe" to "3D WIREFRAME"
                    ).forEach { (id, label) ->
                        val active = globeTheme == id
                        Text(
                            text = label,
                            color = if (active) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(if (active) Color.White else Color(0xFF141414), RoundedCornerShape(6.dp))
                                .clickable { onChangeGlobeTheme(id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                if (globeTheme == "wireframe") {
                    Text(
                        text = "WIREFRAME COLOR PALETTE",
                        color = Color(0xFFA3A3A3),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                    .padding(horizontal = 7.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(swatchColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
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
        }

        // 3C. Glowing Black Hole Widget Part Color Customizer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Black Hole Widget Ring Color", color = Color.White, fontSize = 13.sp, fontFamily = lomoFontFamily)
                Text(
                    text = "Main photon core is glowing white · Customize accretion streams & halo color",
                    color = Color(0xFF737373),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "#FFFFFF" to "WHT",
                        "#FF3B00" to "CMF",
                        "#00E5FF" to "CYN",
                        "#A855F7" to "PRP",
                        "#00FF66" to "GRN",
                        "#FFB000" to "GLD",
                        "#D71921" to "RED"
                    ).forEach { (hex, name) ->
                        val selected = blackHoleAccentColor.equals(hex, ignoreCase = true)
                        val swatchColor = when (hex) {
                            "#FFFFFF" -> Color(0xFFFFFFFF)
                            "#00E5FF" -> Color(0xFF00E5FF)
                            "#A855F7" -> Color(0xFFA855F7)
                            "#00FF66" -> Color(0xFF00FF66)
                            "#FFB000" -> Color(0xFFFFB000)
                            "#D71921" -> Color(0xFFD71921)
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
                                .clickable { onChangeBlackHoleColor(hex) }
                                .padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(swatchColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
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
