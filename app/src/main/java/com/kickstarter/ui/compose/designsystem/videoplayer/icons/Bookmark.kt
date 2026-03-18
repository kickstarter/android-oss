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

val Bookmark: ImageVector
    get() {
        if (_Bookmark != null) {
            return _Bookmark!!
        }
        _Bookmark = ImageVector.Builder(
            name = "Bookmark",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(8.167f, 5.833f)
                verticalLineTo(20.985f)
                lineTo(11.893f, 18.176f)
                curveTo(13.14f, 17.235f, 14.86f, 17.235f, 16.107f, 18.176f)
                lineTo(19.833f, 20.985f)
                verticalLineTo(5.833f)
                horizontalLineTo(8.167f)
                close()
                moveTo(7f, 3.5f)
                curveTo(6.356f, 3.5f, 5.833f, 4.022f, 5.833f, 4.667f)
                verticalLineTo(23.326f)
                curveTo(5.833f, 24.289f, 6.934f, 24.837f, 7.702f, 24.257f)
                lineTo(13.298f, 20.039f)
                curveTo(13.713f, 19.725f, 14.286f, 19.725f, 14.702f, 20.039f)
                lineTo(20.298f, 24.257f)
                curveTo(21.066f, 24.837f, 22.167f, 24.289f, 22.167f, 23.326f)
                verticalLineTo(4.667f)
                curveTo(22.167f, 4.022f, 21.644f, 3.5f, 21f, 3.5f)
                horizontalLineTo(7f)
                close()
            }
        }.build()

        return _Bookmark!!
    }

@Suppress("ObjectPropertyName")
private var _Bookmark: ImageVector? = null

@Preview
@Composable
private fun BookmarkPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Bookmark, contentDescription = null)
    }
}
