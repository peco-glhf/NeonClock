// app/src/main/java/com/example/neonclock/MainActivity.kt
package com.example.neonclock

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ネオングリーンカラー定数
private val NeonGreen = Color(0xFF39FF14)
private val NeonGreenMid = Color(0xFF20CC00)
private val NeonGreenDim = Color(0xFF0D5500)

// Inter Bold フォントファミリー
private val InterFontFamily = FontFamily(
    Font(R.font.inter_bold, FontWeight.Bold)
)

// ベーステキストスタイル（TextMeasurer での計測・NeonText の両方で共用）
private val BaseTextStyle = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.sp,
    textAlign = TextAlign.Center,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val view = LocalView.current
            SideEffect {
                val controller = WindowInsetsControllerCompat(window, view)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            NeonClockScreen()
        }
    }
}

/**
 * 時計画面のルートComposable。
 * 日本時間（JST）の時刻と日付を表示する。
 * 時刻は画面横幅いっぱい、日付はその上に1/4サイズで表示。
 */
@Composable
fun NeonClockScreen() {
    val jstZone = remember { ZoneId.of("Asia/Tokyo") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy年 M月 d日", Locale.JAPANESE) }
    val dowFormatter = remember { DateTimeFormatter.ofPattern("E", Locale.JAPANESE) }

    fun formatDate(now: ZonedDateTime) = "${now.format(dateFormatter)}（${now.format(dowFormatter)}）"

    var timeText by remember { mutableStateOf(ZonedDateTime.now(jstZone).format(timeFormatter)) }
    var dateText by remember { mutableStateOf(formatDate(ZonedDateTime.now(jstZone))) }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                val now = ZonedDateTime.now(jstZone)
                timeText = now.format(timeFormatter)
                dateText = formatDate(now)
                val secondsRemaining = 60 - now.second
                val millisRemaining = secondsRemaining * 1000L - (now.nano / 1_000_000L)
                delay(millisRemaining.coerceAtLeast(100L))
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // 時刻: 幅94% かつ 高さ72% の両方に収まるフォントサイズを二分探索で計算
        val targetWidthPx  = with(density) { (maxWidth  * 0.94f).toPx() }
        val targetHeightPx = with(density) { (maxHeight * 0.72f).toPx() }
        val timeFontSize: TextUnit = remember(maxWidth, maxHeight, timeText) {
            var low = 10f
            var high = 800f
            repeat(20) {
                val mid = (low + high) / 2f
                val measured = textMeasurer.measure(
                    text = timeText,
                    style = BaseTextStyle.copy(fontSize = mid.sp)
                )
                if (measured.size.width < targetWidthPx && measured.size.height < targetHeightPx) {
                    low = mid
                } else {
                    high = mid
                }
            }
            low.sp
        }

        // 日付: 時刻と同じ幅になるフォントサイズを二分探索で計算
        val dateFontSize: TextUnit = remember(maxWidth, dateText) {
            var low = 5f
            var high = 300f
            repeat(20) {
                val mid = (low + high) / 2f
                val measured = textMeasurer.measure(
                    text = dateText,
                    style = BaseTextStyle.copy(fontSize = mid.sp)
                )
                if (measured.size.width < targetWidthPx) {
                    low = mid
                } else {
                    high = mid
                }
            }
            low.sp
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NeonText(
                text = dateText,
                fontSize = dateFontSize
            )
            Spacer(modifier = Modifier.height(12.dp))
            NeonText(
                text = timeText,
                fontSize = timeFontSize
            )
        }
    }
}

/**
 * ネオングロウエフェクト付きテキストComposable。
 * 3層のShadowを重ね、外側から内側へ収束するグロウを表現する。
 */
@Composable
fun NeonText(
    text: String,
    fontSize: TextUnit,
    letterSpacing: TextUnit = 0.sp
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // レイヤー1: 外側グロウ・タイト（装飾専用 → アクセシビリティツリーから除外）
        BasicText(
            text = text,
            modifier = Modifier.clearAndSetSemantics { },
            style = BaseTextStyle.copy(
                fontSize = fontSize,
                letterSpacing = letterSpacing,
                color = Color.Transparent,
                shadow = Shadow(
                    color = NeonGreenDim.copy(alpha = 0.20f),
                    offset = Offset.Zero,
                    blurRadius = 18f
                )
            )
        )
        // レイヤー2: 中間グロウ・タイト（装飾専用 → アクセシビリティツリーから除外）
        BasicText(
            text = text,
            modifier = Modifier.clearAndSetSemantics { },
            style = BaseTextStyle.copy(
                fontSize = fontSize,
                letterSpacing = letterSpacing,
                color = Color.Transparent,
                shadow = Shadow(
                    color = NeonGreenMid.copy(alpha = 0.45f),
                    offset = Offset.Zero,
                    blurRadius = 6f
                )
            )
        )
        // レイヤー3: メインテキスト + 極細グロウ（パキッとシャープに）
        BasicText(
            text = text,
            style = BaseTextStyle.copy(
                fontSize = fontSize,
                letterSpacing = letterSpacing,
                color = NeonGreen,
                shadow = Shadow(
                    color = NeonGreen.copy(alpha = 0.95f),
                    offset = Offset.Zero,
                    blurRadius = 2f
                )
            )
        )
    }
}
