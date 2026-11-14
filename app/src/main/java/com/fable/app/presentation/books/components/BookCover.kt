package com.fable.app.presentation.books.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.fable.app.presentation.designsystem.drawCoverMotif
import com.fable.app.presentation.designsystem.sharedBookCover
import com.fable.app.presentation.designsystem.shimmer
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.Genre
import com.fable.app.presentation.theme.genreColors

/** Book-like silhouette: a tight spine edge and a softer fore-edge. */
val BookShape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 6.dp, bottomEnd = 6.dp)

/**
 * The book cover — Fable's most repeated visual element.
 *
 * Real artwork is loaded with a crossfade over a genre-tinted shimmer. When a book has no
 * artwork (or it fails to load), a typographic cover is composed from the book's own title,
 * author and genre identity, so the fallback still looks deliberate. A spine shade and
 * genre-coloured shadow give every cover physical presence.
 */
@Composable
fun BookCover(
    url: String?,
    title: String,
    modifier: Modifier = Modifier,
    author: String? = null,
    genre: Genre? = null,
    sharedKey: String? = null,
    elevation: Dp = 8.dp,
) {
    val colors = genreColors(genre)
    val shadowColor = colors.coverGradient.last().copy(alpha = 0.55f)
    Box(
        modifier
            .sharedBookCover(sharedKey)
            .shadow(elevation, BookShape, clip = false, ambientColor = shadowColor, spotColor = shadowColor)
            .clip(BookShape)
            .semantics { contentDescription = "Cover of $title" },
    ) {
        if (url.isNullOrBlank()) {
            TypographicCover(title, author, genre, Modifier.fillMaxSize())
        } else {
            var state by remember(url) { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
            when (state) {
                is AsyncImagePainter.State.Error -> TypographicCover(title, author, genre, Modifier.fillMaxSize())
                is AsyncImagePainter.State.Success -> Unit
                else -> Box(Modifier.fillMaxSize().background(colors.coverGradient.first().copy(alpha = 0.35f)).shimmer(BookShape))
            }
            val context = LocalContext.current
            val request = remember(url) { ImageRequest.Builder(context).data(url).crossfade(260).build() }
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onState = { state = it },
                modifier = Modifier.fillMaxSize(),
            )
        }
        SpineShade(Modifier.fillMaxSize())
    }
}

/** A thin spine crease and page sheen drawn over the artwork. */
@Composable
private fun SpineShade(modifier: Modifier) {
    Box(
        modifier.drawWithContent {
            drawContent()
            val spine = size.width * 0.045f
            drawRect(
                Brush.horizontalGradient(
                    0f to Color.Black.copy(alpha = 0.32f),
                    0.6f to Color.White.copy(alpha = 0.10f),
                    1f to Color.Transparent,
                    startX = 0f,
                    endX = spine * 2.2f,
                ),
                size = Size(spine * 2.2f, size.height),
            )
            drawRect(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.Transparent, Color.Black.copy(alpha = 0.12f))),
            )
        },
    )
}

/**
 * Editorial typographic cover for books without artwork. It uses the genre's deep cover
 * gradient, its signature motif, and the real title/author — never invented content.
 */
@Composable
fun TypographicCover(title: String, author: String?, genre: Genre?, modifier: Modifier = Modifier) {
    val colors = genreColors(genre)
    val tokens = FableThemeTokens.current
    val seed = remember(title) { title.hashCode() }
    BoxWithConstraints(
        modifier
            .background(colors.coverBrush)
            .drawBehind { drawCoverMotif(colors.motif, colors.onCover.copy(alpha = 0.14f), colors.accent, seed) },
    ) {
        val scale = (maxWidth.value / 140f).coerceIn(0.45f, 2.2f)
        Column(
            Modifier.fillMaxSize().padding(horizontal = (12 * scale).dp, vertical = (14 * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(colors.ornament, color = colors.accent, fontSize = (14 * scale).sp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    title,
                    color = colors.onCover,
                    fontFamily = tokens.bookTitleFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = (17 * scale).sp,
                    lineHeight = (20 * scale).sp,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height((8 * scale).dp))
                Box(Modifier.width((26 * scale).dp).height(1.dp).background(colors.accent.copy(alpha = 0.8f)))
            }
            Text(
                (author ?: genre?.label ?: "Fable").uppercase(),
                color = colors.onCover.copy(alpha = 0.85f),
                fontSize = (8.5f * scale).sp,
                letterSpacing = (1.4f * scale).sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Soft genre-coloured pool of light beneath a cover — part of the genre signature. */
fun Modifier.coverGlow(color: Color, strength: Float = 1f): Modifier = this.drawBehind {
    val tint = color.copy(alpha = (color.alpha * strength).coerceIn(0f, 1f))
    val center = Offset(size.width / 2, size.height * 0.96f)
    val radius = size.width * 0.62f
    // A circle squashed into a flat ellipse: the gradient reaches full transparency at the
    // rim, so the light pools softly instead of ending in a hard edge.
    scale(scaleX = 1f, scaleY = 0.2f, pivot = center) {
        drawCircle(Brush.radialGradient(listOf(tint, tint.copy(alpha = 0f)), center = center, radius = radius), radius, center)
    }
}

/** Convenience to keep cover aspect ratios consistent across the app. */
@Composable
fun CoverBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { content() }
}
