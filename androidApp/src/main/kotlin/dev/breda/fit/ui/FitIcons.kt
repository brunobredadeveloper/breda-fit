package dev.breda.fit.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Small original vector set: no bundled font or thousands of unused glyphs. */
object FitIcons {
    private fun glyph(name: String, draw: PathBuilder.() -> Unit): ImageVector = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { draw() }
    }.build()
    val Home = glyph("Home") { moveTo(3f, 11f); lineTo(12f, 3f); lineTo(21f, 11f); moveTo(5f, 10f); lineTo(5f, 21f); lineTo(10f, 21f); lineTo(10f, 15f); lineTo(14f, 15f); lineTo(14f, 21f); lineTo(19f, 21f); lineTo(19f, 10f) }
    val Fitness = glyph("Fitness") { moveTo(8f, 12f); lineTo(16f, 12f); moveTo(5f, 6f); lineTo(5f, 18f); moveTo(8f, 8f); lineTo(8f, 16f); moveTo(16f, 8f); lineTo(16f, 16f); moveTo(19f, 6f); lineTo(19f, 18f); moveTo(2f, 10f); lineTo(2f, 14f); moveTo(22f, 10f); lineTo(22f, 14f) }
    val Chart = glyph("Chart") { moveTo(4f, 3f); lineTo(4f, 20f); lineTo(21f, 20f); moveTo(7f, 15f); lineTo(11f, 11f); lineTo(15f, 13f); lineTo(20f, 6f) }
    val Settings = glyph("Settings") { moveTo(3f, 6f); lineTo(7f, 6f); moveTo(11f, 6f); lineTo(21f, 6f); moveTo(7f, 4f); lineTo(11f, 4f); lineTo(11f, 8f); lineTo(7f, 8f); close(); moveTo(3f, 12f); lineTo(14f, 12f); moveTo(18f, 12f); lineTo(21f, 12f); moveTo(14f, 10f); lineTo(18f, 10f); lineTo(18f, 14f); lineTo(14f, 14f); close(); moveTo(3f, 18f); lineTo(8f, 18f); moveTo(12f, 18f); lineTo(21f, 18f); moveTo(8f, 16f); lineTo(12f, 16f); lineTo(12f, 20f); lineTo(8f, 20f); close() }
    val Play = glyph("Play") { moveTo(8f, 5f); lineTo(20f, 12f); lineTo(8f, 19f); close() }
    val Pause = glyph("Pause") { moveTo(8f, 5f); lineTo(8f, 19f); moveTo(16f, 5f); lineTo(16f, 19f) }
    val Check = glyph("Check") { moveTo(8f, 3f); lineTo(16f, 3f); lineTo(21f, 8f); lineTo(21f, 16f); lineTo(16f, 21f); lineTo(8f, 21f); lineTo(3f, 16f); lineTo(3f, 8f); close(); moveTo(7f, 12f); lineTo(10f, 15f); lineTo(17f, 8f) }
}
