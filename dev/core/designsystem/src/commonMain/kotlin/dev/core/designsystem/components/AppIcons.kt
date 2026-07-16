package dev.core.designsystem.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Lucide ikonalari — dizayndagi aynan SVG `d` yo'llari [PathParser] orqali
 * [ImageVector] ga aylantirilgan. Chiziqli (stroke) ikonalar `Icon(tint=...)` bilan
 * bo'yaladi; ko'p rangli logolar `Image` bilan chiziladi.
 */
private fun strokeIcon(name: String, vararg paths: String, viewport: Float = 24f): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = viewport,
        viewportHeight = viewport,
    ).apply {
        paths.forEach { d ->
            addPath(
                pathData = PathParser().parsePathString(d).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

object AppIcons {
    val GraduationCap = strokeIcon(
        "GraduationCap",
        "M22 10 12 5 2 10l10 5 10-5Z",
        "M6 12v5c0 1 2 3 6 3s6-2 6-3v-5",
    )
    val Phone = strokeIcon(
        "Phone",
        "M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.13.96.36 1.9.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.91.34 1.85.57 2.81.7A2 2 0 0 1 22 16.92Z",
    )
    val Mail = strokeIcon(
        "Mail",
        "M2 6a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2z",
        "m22 7-10 5L2 7",
    )
    val Lock = strokeIcon(
        "Lock",
        "M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2z",
        "M7 11V7a5 5 0 0 1 10 0v4",
    )
    val Eye = strokeIcon(
        "Eye",
        "M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z",
        "M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0z",
    )
    val EyeOff = strokeIcon(
        "EyeOff",
        "M9.88 9.88a3 3 0 0 0 4.24 4.24",
        "M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68",
        "M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61",
        "M2 2l20 20",
    )
    val ArrowRight = strokeIcon("ArrowRight", "M5 12h14", "m12 5 7 7-7 7")
    val ArrowLeft = strokeIcon("ArrowLeft", "M19 12H5", "m12 19-7-7 7-7")
    val Check = strokeIcon("Check", "M20 6 9 17l-5-5")
    val Clock = strokeIcon(
        "Clock",
        "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z",
        "M12 6v6l4 2",
    )
    val Calendar = strokeIcon(
        "Calendar",
        "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z",
        "M16 2v4",
        "M8 2v4",
        "M3 10h18",
    )
    val Search = strokeIcon(
        "Search",
        "M11 3a8 8 0 1 0 0 16 8 8 0 0 0 0-16z",
        "m21 21-4.3-4.3",
    )
    val ShieldCheck = strokeIcon(
        "ShieldCheck",
        "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z",
        "m9 12 2 2 4-4",
    )
    val ChevronDown = strokeIcon("ChevronDown", "m6 9 6 6 6-6")
    val Close = strokeIcon("Close", "M18 6 6 18", "M6 6l12 12")
    val MessageSquare = strokeIcon(
        "MessageSquare",
        "M7 8h10",
        "M7 12h6",
        "M4 4h16a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1h-6l-4 4v-4H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1Z",
    )
    val ScanFace = strokeIcon(
        "ScanFace",
        "M4 8V6a2 2 0 0 1 2-2h2",
        "M4 16v2a2 2 0 0 0 2 2h2",
        "M16 4h2a2 2 0 0 1 2 2v2",
        "M16 20h2a2 2 0 0 0 2-2v-2",
        "M8 14s1.5 2 4 2 4-2 4-2",
        "M9 9h.01",
        "M15 9h.01",
    )
    val Briefcase = strokeIcon(
        "Briefcase",
        "M4 7h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2z",
        "M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16",
    )
    val Store = strokeIcon(
        "Store",
        "M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z",
        "M3 6h18",
        "M16 10a4 4 0 0 1-8 0",
    )
    val Building = strokeIcon(
        "Building",
        "M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18",
        "M2 22h20",
        "M10 7h4",
        "M10 11h4",
        "M10 15h4",
    )
    val Home = strokeIcon(
        "Home",
        "M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z",
        "M9 21v-8h6v8",
    )
    val Bell = strokeIcon(
        "Bell",
        "M10.268 21a2 2 0 0 0 3.464 0",
        "M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326z",
    )
    val Tag = strokeIcon(
        "Tag",
        "M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z",
        "M7.5 7.5h.01",
    )
    val Users = strokeIcon(
        "Users",
        "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
        "M9 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z",
        "M22 21v-2a4 4 0 0 0-3-3.87",
        "M16 3.13a4 4 0 0 1 0 7.75",
    )
    val Plus = strokeIcon("Plus", "M5 12h14", "M12 5v14")
    val ChevronRight = strokeIcon("ChevronRight", "m9 18 6-6-6-6")
    val Bookmark = strokeIcon(
        "Bookmark",
        "m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z",
    )
    val Filter = strokeIcon("Filter", "M22 3H2l8 9.46V19l4 2v-8.54z")
    val Send = strokeIcon("Send", "M22 2 11 13", "M22 2 15 22l-4-9-9-4z")
    val Settings = strokeIcon(
        "Settings",
        "M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z",
        "M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z",
    )
    val LogOut = strokeIcon("LogOut", "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4", "m16 17 5-5-5-5", "M21 12H9")
    val FileText = strokeIcon(
        "FileText",
        "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z",
        "M14 2v6h6",
        "M9 13h6",
        "M9 17h6",
    )
    val Pencil = strokeIcon(
        "Pencil",
        "M12 20h9",
        "M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4z",
    )
    val Star = strokeIcon("Star", "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14l-5-4.87 6.91-1.01z")
    val Camera = strokeIcon(
        "Camera",
        "M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z",
        "M15 13a3 3 0 1 1-6 0 3 3 0 0 1 6 0z",
    )
    val ImageIcon = strokeIcon(
        "Image",
        "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z",
        "M8.5 10a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z",
        "m21 15-5-5L5 21",
    )
    val UserPlus = strokeIcon(
        "UserPlus",
        "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
        "M9 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z",
        "M19 8v6",
        "M22 11h-6",
    )

    /** Apple logo — bitta to'ldirilgan yo'l; `Icon(tint=...)` bilan bo'yaladi. */
    val Apple: ImageVector = ImageVector.Builder(
        "Apple", 24.dp, 24.dp, 24f, 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "M17.05 12.04c-.03-2.94 2.4-4.35 2.51-4.42-1.37-2-3.5-2.28-4.26-2.31-1.81-.18-3.53 1.07-4.45 1.07-.92 0-2.33-1.04-3.83-1.01-1.97.03-3.79 1.15-4.8 2.91-2.05 3.55-.52 8.8 1.47 11.68.97 1.41 2.13 2.99 3.64 2.93 1.46-.06 2.01-.94 3.78-.94 1.76 0 2.26.94 3.8.91 1.57-.03 2.56-1.43 3.52-2.85 1.11-1.63 1.57-3.21 1.59-3.29-.03-.02-3.05-1.17-3.08-4.66zM14.13 3.57c.81-.98 1.36-2.34 1.21-3.7-1.17.05-2.59.78-3.43 1.76-.75.87-1.41 2.26-1.23 3.59 1.3.1 2.64-.66 3.45-1.65z",
            ).toNodes(),
            fill = SolidColor(Color.Black),
        )
    }.build()

    /** Google 'G' — ko'p rangli, `Image` bilan chiziladi. */
    val Google: ImageVector = ImageVector.Builder(
        "Google", 24.dp, 24.dp, 48f, 48f,
    ).apply {
        addPath(
            PathParser().parsePathString("M43.6 20.1H42V20H24v8h11.3C33.7 32.7 29.3 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.8 1.2 7.9 3l5.7-5.7C34 6.1 29.3 4 24 4 13 4 4 13 4 24s9 20 20 20 20-9 20-20c0-1.3-.1-2.7-.4-3.9z").toNodes(),
            fill = SolidColor(Color(0xFFFFC107)),
        )
        addPath(
            PathParser().parsePathString("m6.3 14.7 6.6 4.8C14.7 15.1 19 12 24 12c3.1 0 5.8 1.2 7.9 3l5.7-5.7C34 6.1 29.3 4 24 4 16.3 4 9.7 8.3 6.3 14.7z").toNodes(),
            fill = SolidColor(Color(0xFFFF3D00)),
        )
        addPath(
            PathParser().parsePathString("M24 44c5.2 0 9.9-2 13.4-5.2l-6.2-5.2C29.2 35.1 26.7 36 24 36c-5.2 0-9.6-3.3-11.3-7.9l-6.5 5C9.5 39.6 16.2 44 24 44z").toNodes(),
            fill = SolidColor(Color(0xFF4CAF50)),
        )
        addPath(
            PathParser().parsePathString("M43.6 20.1H42V20H24v8h11.3c-.8 2.2-2.2 4.2-4.1 5.6l6.2 5.2C36.9 40.2 44 34 44 24c0-1.3-.1-2.7-.4-3.9z").toNodes(),
            fill = SolidColor(Color(0xFF1976D2)),
        )
    }.build()

    /** Telegram — ko'k doira + oq samolyot, `Image` bilan chiziladi. */
    val Telegram: ImageVector = ImageVector.Builder(
        "Telegram", 24.dp, 24.dp, 24f, 24f,
    ).apply {
        addPath(
            PathParser().parsePathString("M12 1a11 11 0 1 0 0 22 11 11 0 0 0 0-22z").toNodes(),
            fill = SolidColor(Color(0xFF229ED9)),
        )
        addPath(
            PathParser().parsePathString("M5.6 11.7l11-4.24c.51-.19.96.12.79.9l-1.87 8.82c-.13.6-.5.75-1 .47l-2.76-2.03-1.33 1.28c-.15.15-.27.27-.55.27l.2-2.8 5.12-4.62c.22-.2-.05-.31-.34-.11L9.9 13.5l-2.73-.85c-.59-.19-.6-.59.13-.87z").toNodes(),
            fill = SolidColor(Color.White),
        )
    }.build()
}
