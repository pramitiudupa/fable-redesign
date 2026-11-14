package com.fable.app.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fable.app.presentation.theme.FableMotion
import com.fable.app.presentation.designsystem.pastelBackdrop
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

// Pastel opening palette: deep plum ink and warm rose-gold on a soft multi-colour gradient.
private val Glow = Color(0xFFFFFFFF)
private val GlowEdge = Color(0xFFFFF4FA)
private val Gold = Color(0xFFB07A4F)
private val Ivory = Color(0xFF2E2A45)

/**
 * The opening moment: on a soft pastel gradient an open book is drawn in rose-gold line, the
 * wordmark rises into place and motes drift through the light. Shown while the session restores.
 */
@Composable
fun FableSplash(modifier: Modifier = Modifier, statusText: String = "Opening your library…") {
    val draw = remember { Animatable(0f) }
    val word = remember { Animatable(0f) }
    val tagline = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { draw.animateTo(1f, tween(1300, easing = FableMotion.Emphasized)) }
        launch { word.animateTo(1f, tween(900, delayMillis = 420, easing = FableMotion.EmphasizedDecelerate)) }
        launch { tagline.animateTo(1f, tween(800, delayMillis = 900, easing = FableMotion.EmphasizedDecelerate)) }
    }
    val ambient = rememberInfiniteTransition(label = "splashAmbient")
    val drift by ambient.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "drift")
    val flicker by ambient.animateFloat(0.85f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "flicker")
    val sweep by ambient.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "sweep")
    val motes = remember {
        val random = Random(7)
        List(28) { Triple(random.nextFloat(), random.nextFloat(), 0.4f + random.nextFloat()) }
    }

    Box(
        modifier
            .fillMaxSize()
            .pastelBackdrop()
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        listOf(Glow.copy(alpha = 0.7f * flicker), GlowEdge.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(size.width / 2, size.height * 0.42f),
                        radius = max(size.width, size.height) * 0.65f,
                    ),
                )
                motes.forEach { (x, y, s) ->
                    val py = ((y - drift * s * 0.35f) % 1f + 1f) % 1f
                    drawCircle(
                        Gold.copy(alpha = 0.22f * s * flicker),
                        radius = 1.4.dp.toPx() * s,
                        center = Offset(size.width * x, size.height * py),
                    )
                }
            }
            .semantics { contentDescription = "Fable. $statusText" },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            OpenBookMark(progress = draw.value, modifier = Modifier.size(width = 112.dp, height = 74.dp))
            Text(
                "Fable",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 64.sp,
                letterSpacing = (8 * (1f - word.value) + 1).sp,
                color = Ivory,
                modifier = Modifier.graphicsLayer {
                    alpha = word.value
                    translationY = (1f - word.value) * 28.dp.toPx()
                },
            )
            Text(
                "A reading life, beautifully kept.",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 17.sp,
                color = Ivory.copy(alpha = 0.72f),
                modifier = Modifier.graphicsLayer {
                    alpha = tagline.value
                    translationY = (1f - tagline.value) * 12.dp.toPx()
                },
            )
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 48.dp)
                .graphicsLayer { alpha = tagline.value },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .width(120.dp)
                    .height(1.dp)
                    .drawBehind {
                        drawRect(Gold.copy(alpha = 0.18f))
                        val x = size.width * sweep
                        drawRect(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Gold, Color.Transparent),
                                startX = x - size.width * 0.4f,
                                endX = x,
                            ),
                        )
                    },
            )
            Text(statusText.uppercase(), fontSize = 10.sp, letterSpacing = 2.4.sp, color = Ivory.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Gold line drawing of an open book, traced progressively by [progress]. */
@Composable
private fun OpenBookMark(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            // Left page
            moveTo(w * 0.5f, h * 0.18f)
            cubicTo(w * 0.36f, h * 0.04f, w * 0.16f, h * 0.06f, w * 0.02f, h * 0.14f)
            lineTo(w * 0.02f, h * 0.9f)
            cubicTo(w * 0.16f, h * 0.82f, w * 0.36f, h * 0.82f, w * 0.5f, h * 0.96f)
            // Right page
            cubicTo(w * 0.64f, h * 0.82f, w * 0.84f, h * 0.82f, w * 0.98f, h * 0.9f)
            lineTo(w * 0.98f, h * 0.14f)
            cubicTo(w * 0.84f, h * 0.06f, w * 0.64f, h * 0.04f, w * 0.5f, h * 0.18f)
            lineTo(w * 0.5f, h * 0.96f)
        }
        val measure = PathMeasure().apply { setPath(path, false) }
        val segment = Path()
        measure.getSegment(0f, measure.length * progress, segment, true)
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawPath(segment, Gold, style = stroke)
        // Text lines on the pages fade in once the outline is drawn.
        val linesAlpha = ((progress - 0.6f) / 0.4f).coerceIn(0f, 1f)
        if (linesAlpha > 0f) {
            for (i in 0 until 4) {
                val y = h * (0.32f + i * 0.13f)
                drawLine(Gold.copy(alpha = 0.45f * linesAlpha), Offset(w * 0.12f, y), Offset(w * 0.4f, y - h * 0.02f), strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Gold.copy(alpha = 0.45f * linesAlpha), Offset(w * 0.6f, y - h * 0.02f), Offset(w * 0.88f, y), strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
            }
        }
    }
}
