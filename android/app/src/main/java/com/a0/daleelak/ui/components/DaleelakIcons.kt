package com.a0.daleelak.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Native vector icons; no additional icon dependency. */
object DaleelakIcons {
    val Home = outline("Home") {
        moveTo(3f, 10f); lineTo(12f, 3f); lineTo(21f, 10f)
        moveTo(5f, 9f); lineTo(5f, 21f); lineTo(10f, 21f)
        lineTo(10f, 14f); lineTo(14f, 14f); lineTo(14f, 21f)
        lineTo(19f, 21f); lineTo(19f, 9f)
    }
    val History = outline("History") {
        moveTo(5f, 3f); lineTo(19f, 3f); lineTo(19f, 21f); lineTo(5f, 21f); close()
        moveTo(9f, 8f); lineTo(15f, 8f)
        moveTo(9f, 12f); lineTo(15f, 12f)
        moveTo(9f, 16f); lineTo(13f, 16f)
    }
    val Current = outline("Current") {
        circle(); moveTo(12f, 7f); lineTo(12f, 12f); lineTo(15f, 14f)
    }
    val Finished = outline("Finished") {
        circle(); moveTo(8f, 12f); lineTo(11f, 15f); lineTo(16f, 9f)
    }
    val Add = outline("Add") {
        moveTo(12f, 5f); lineTo(12f, 19f); moveTo(5f, 12f); lineTo(19f, 12f)
    }
    val Places = outline("Places") {
        moveTo(12f, 21f)
        curveTo(9f, 17f, 5f, 13f, 5f, 9f); curveTo(5f, 5f, 8f, 3f, 12f, 3f)
        curveTo(16f, 3f, 19f, 5f, 19f, 9f); curveTo(19f, 13f, 15f, 17f, 12f, 21f); close()
        moveTo(14f, 9f); curveTo(14f, 12f, 10f, 12f, 10f, 9f)
        curveTo(10f, 6f, 14f, 6f, 14f, 9f); close()
    }
    val Microphone = outline("Microphone") {
        moveTo(9f, 6f); curveTo(9f, 2f, 15f, 2f, 15f, 6f)
        lineTo(15f, 11f); curveTo(15f, 15f, 9f, 15f, 9f, 11f); close()
        moveTo(6f, 10f); curveTo(6f, 19f, 18f, 19f, 18f, 10f)
        moveTo(12f, 17f); lineTo(12f, 21f); moveTo(8f, 21f); lineTo(16f, 21f)
    }
    val Keyboard = outline("Keyboard") {
        moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 19f); lineTo(3f, 19f); close()
        moveTo(7f, 9f); lineTo(8f, 9f); moveTo(11f, 9f); lineTo(12f, 9f)
        moveTo(15f, 9f); lineTo(17f, 9f); moveTo(7f, 12f); lineTo(8f, 12f)
        moveTo(11f, 12f); lineTo(12f, 12f); moveTo(15f, 12f); lineTo(17f, 12f)
        moveTo(8f, 16f); lineTo(16f, 16f)
    }
    val Speaker = outline("Speaker") {
        moveTo(3f, 9f); lineTo(7f, 9f); lineTo(12f, 5f); lineTo(12f, 19f)
        lineTo(7f, 15f); lineTo(3f, 15f); close()
        moveTo(16f, 8f); curveTo(19f, 10f, 19f, 14f, 16f, 16f)
        moveTo(19f, 5f); curveTo(24f, 9f, 24f, 15f, 19f, 19f)
    }

    private fun outline(name: String, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathBuilder = block)
        }.build()

    private fun PathBuilder.circle() {
        moveTo(21f, 12f); curveTo(21f, 17f, 17f, 21f, 12f, 21f)
        curveTo(7f, 21f, 3f, 17f, 3f, 12f); curveTo(3f, 7f, 7f, 3f, 12f, 3f)
        curveTo(17f, 3f, 21f, 7f, 21f, 12f); close()
    }
}
