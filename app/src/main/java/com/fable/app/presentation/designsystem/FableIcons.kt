package com.fable.app.presentation.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Fable's own line-icon set, drawn on a 24-unit grid with a consistent 1.7 stroke and rounded
 * joins. Keeping icons in-house gives every screen one coherent iconographic voice and avoids
 * pulling in the large material-icons-extended dependency.
 */
object FableIcons {
    val Home = stroke("home", "M4 10.5 12 4l8 6.5V19.5a1 1 0 0 1-1 1h-4.5v-6h-5v6H5a1 1 0 0 1-1-1z")
    val Compass = stroke(
        "compass",
        "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18z",
        "M15.6 8.4 13.5 13.5 8.4 15.6 10.5 10.5z",
    )
    val Search = stroke("search", "M11 4a7 7 0 1 0 0 14a7 7 0 1 0 0-14z", "M16.2 16.2 20.5 20.5")
    val Library = stroke(
        "library",
        "M4.5 4h3.5v16H4.5z",
        "M10 4h3.5v16H10z",
        "M15.6 5.3l3-0.8 3.6 15-3 0.8z",
    )
    val Back = stroke("back", "M15 5 8 12l7 7")
    val ArrowRight = stroke("arrow-right", "M5 12h14", "M13 6l6 6-6 6")
    val Close = stroke("close", "M6 6l12 12", "M18 6 6 18")
    val Check = stroke("check", "M5 12.5 10 17.5 19 7")
    val Plus = stroke("plus", "M12 5v14", "M5 12h14")
    val StarOutline = stroke("star-outline", STAR)
    val Star = fill("star", STAR)
    val Heart = stroke("heart", "M12 20s-7.5-4.6-7.5-10.2A4.2 4.2 0 0 1 12 7.3a4.2 4.2 0 0 1 7.5 2.5C19.5 15.4 12 20 12 20z")
    val Bookmark = stroke("bookmark", "M7 4h10v16l-5-3.6L7 20z")
    val User = stroke("user", "M12 4a4 4 0 1 0 0 8a4 4 0 1 0 0-8z", "M4.5 20.5a7.5 7.5 0 0 1 15 0")
    val Users = stroke(
        "users",
        "M9 5a3.5 3.5 0 1 0 0 7a3.5 3.5 0 1 0 0-7z",
        "M2.5 20a6.5 6.5 0 0 1 13 0",
        "M16 5.2a3.2 3.2 0 0 1 0 6.3",
        "M18 14.2a6 6 0 0 1 3.5 5.8",
    )
    val UserPlus = stroke(
        "user-plus",
        "M10 4a4 4 0 1 0 0 8a4 4 0 1 0 0-8z",
        "M3 20.5a7 7 0 0 1 14 0",
        "M19 8v6",
        "M16 11h6",
    )
    val Palette = stroke(
        "palette",
        "M12 3a9 9 0 0 0 0 18c1.2 0 1.8-0.8 1.8-1.7 0-1.2-1-1.6-1-2.6 0-1 0.8-1.7 1.8-1.7H17a4 4 0 0 0 4-4C21 6.6 17 3 12 3z",
        "M7.5 11.5h0.01",
        "M9.5 7.5h0.01",
        "M14.5 7.5h0.01",
    )
    val Bell = stroke("bell", "M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15z", "M10 20.5a2 2 0 0 0 4 0")
    val ChevronRight = stroke("chevron-right", "M9 5l7 7-7 7")
    val ChevronDown = stroke("chevron-down", "M5 9l7 7 7-7")
    val Sparkle = stroke("sparkle", "M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z", "M19 16.5v4", "M17 18.5h4")
    val Eye = stroke("eye", "M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12z", "M12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6z")
    val EyeOff = stroke(
        "eye-off",
        "M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12z",
        "M12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6z",
        "M4 4l16 16",
    )
    val Chart = stroke("chart", "M4 20h16", "M7 16v-5", "M12 16V7", "M17 16v-8")
    val SignOut = stroke("sign-out", "M10 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h4", "M14 8l4 4-4 4", "M18 12H9")
    val Edit = stroke("edit", "M4 20h4L19 9l-4-4L4 16z", "M13.5 6.5l4 4")
    val Trash = stroke("trash", "M5 7h14", "M10 4h4", "M7 7l1 13h8l1-13")
    val Activity = stroke("activity", "M3 12h4l3-7 4 14 3-7h4")
    val Mail = stroke("mail", "M4 6h16v12H4z", "M4 7l8 6 8-6")
    val Lock = stroke("lock", "M6 11h12v9H6z", "M8.5 11V8a3.5 3.5 0 0 1 7 0v3")
    val Sort = stroke("sort", "M4 7h11", "M4 12h8", "M4 17h5", "M18 6v13", "M15.5 16.5 18 19l2.5-2.5")
    val Clock = stroke("clock", "M12 4a8 8 0 1 0 0 16a8 8 0 1 0 0-16z", "M12 8v4.5l3 1.8")
    val External = stroke(
        "external",
        "M14 4h6v6",
        "M20 4l-9 9",
        "M18 14v5a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h5",
    )
    val BookOpen = stroke("book-open", "M3 5.5c3-1 6-1 9 1 3-2 6-2 9-1V19c-3-1-6-1-9 1-3-2-6-2-9-1z", "M12 6.5V20")
    val Grid = stroke("grid", "M4 4h7v7H4z", "M13 4h7v7h-7z", "M4 13h7v7H4z", "M13 13h7v7h-7z")
    val Rows = stroke("rows", "M4 6h16", "M4 12h16", "M4 18h16")
    val More = stroke("more", "M12 5h0.01", "M12 12h0.01", "M12 19h0.01", width = 3f)
    val Trophy = stroke(
        "trophy",
        "M8 4h8v5a4 4 0 0 1-8 0z",
        "M8 6H5a3 3 0 0 0 3 4",
        "M16 6h3a3 3 0 0 1-3 4",
        "M12 13v4",
        "M8.5 20h7",
    )
    val Refresh = stroke("refresh", "M20 11a8 8 0 1 0-2.3 5.7", "M20 5v6h-6")
    val Feather = stroke("feather", "M20 4C11 4 6 9 6 18l-2 2", "M6 18c5 0 9-2 11-6", "M9 13h6")
    val Flag = stroke("flag", "M5 21V4", "M5 4h11l-2 4 2 4H5")
    val Swap = stroke("swap", "M7 7h12", "M16 4l3 3-3 3", "M17 17H5", "M8 14l-3 3 3 3")
    val Send = stroke("send", "M4 12 20 4l-6 16-3-7z", "M11 13l9-9")
    val Info = stroke("info", "M12 4a8 8 0 1 0 0 16a8 8 0 1 0 0-16z", "M12 11v5", "M12 8h0.01")
    val Alert = stroke("alert", "M12 4 21 19H3z", "M12 10v4", "M12 16.8h0.01")
    val Globe = stroke(
        "globe",
        "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18z",
        "M3 12h18",
        "M12 3c2.5 2.6 3.7 5.6 3.7 9s-1.2 6.4-3.7 9c-2.5-2.6-3.7-5.6-3.7-9S9.5 5.6 12 3z",
    )
}

private const val STAR = "M12 3.6l2.5 5.2 5.7 0.8-4.1 4 1 5.7L12 16.6l-5.1 2.7 1-5.7-4.1-4 5.7-0.8z"

private fun stroke(name: String, vararg paths: String, width: Float = 1.7f): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    paths.forEach { data ->
        builder.addPath(
            pathData = addPathNodes(data),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    return builder.build()
}

private fun fill(name: String, vararg paths: String): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    paths.forEach { data ->
        builder.addPath(
            pathData = addPathNodes(data),
            fill = SolidColor(Color.Black),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.2f,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    return builder.build()
}
