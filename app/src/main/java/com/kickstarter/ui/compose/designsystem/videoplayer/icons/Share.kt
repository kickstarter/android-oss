package com.kickstarter.ui.compose.designsystem.videoplayer.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Share: ImageVector
    get() {
        if (_Share != null) {
            return _Share!!
        }
        _Share = ImageVector.Builder(
            name = "Share",
            defaultWidth = 19.dp,
            defaultHeight = 16.dp,
            viewportWidth = 19f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(10.258f, 0.342f)
                curveTo(10.714f, -0.114f, 11.453f, -0.114f, 11.908f, 0.342f)
                lineTo(18.325f, 6.758f)
                curveTo(18.781f, 7.214f, 18.781f, 7.953f, 18.325f, 8.408f)
                lineTo(11.908f, 14.825f)
                curveTo(11.453f, 15.281f, 10.714f, 15.281f, 10.258f, 14.825f)
                curveTo(9.803f, 14.369f, 9.803f, 13.631f, 10.258f, 13.175f)
                lineTo(14.683f, 8.75f)
                horizontalLineTo(5.833f)
                curveTo(3.9f, 8.75f, 2.333f, 10.317f, 2.333f, 12.25f)
                verticalLineTo(14f)
                curveTo(2.333f, 14.644f, 1.811f, 15.167f, 1.167f, 15.167f)
                curveTo(0.522f, 15.167f, 0f, 14.644f, 0f, 14f)
                verticalLineTo(12.25f)
                curveTo(0f, 9.028f, 2.612f, 6.417f, 5.833f, 6.417f)
                horizontalLineTo(14.683f)
                lineTo(10.258f, 1.992f)
                curveTo(9.803f, 1.536f, 9.803f, 0.797f, 10.258f, 0.342f)
                close()
            }
        }.build()

        return _Share!!
    }

@Suppress("ObjectPropertyName")
private var _Share: ImageVector? = null

@Preview
@Composable
private fun SharePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Share, contentDescription = null)
    }
}
