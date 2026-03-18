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
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(14.925f, 6.758f)
                curveTo(15.381f, 6.303f, 16.119f, 6.303f, 16.575f, 6.758f)
                lineTo(22.992f, 13.175f)
                curveTo(23.447f, 13.631f, 23.447f, 14.369f, 22.992f, 14.825f)
                lineTo(16.575f, 21.242f)
                curveTo(16.119f, 21.697f, 15.381f, 21.697f, 14.925f, 21.242f)
                curveTo(14.469f, 20.786f, 14.469f, 20.047f, 14.925f, 19.592f)
                lineTo(19.35f, 15.167f)
                horizontalLineTo(10.5f)
                curveTo(8.567f, 15.167f, 7f, 16.734f, 7f, 18.667f)
                verticalLineTo(20.417f)
                curveTo(7f, 21.061f, 6.478f, 21.583f, 5.833f, 21.583f)
                curveTo(5.189f, 21.583f, 4.667f, 21.061f, 4.667f, 20.417f)
                verticalLineTo(18.667f)
                curveTo(4.667f, 15.445f, 7.278f, 12.833f, 10.5f, 12.833f)
                horizontalLineTo(19.35f)
                lineTo(14.925f, 8.408f)
                curveTo(14.469f, 7.953f, 14.469f, 7.214f, 14.925f, 6.758f)
                close()
            }
        }.build()

        return _Share!!
    }

@Suppress("ObjectPropertyName")
private var _Share: ImageVector? = null

@Preview
@Composable
private fun ReplyPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Share, contentDescription = null)
    }
}
