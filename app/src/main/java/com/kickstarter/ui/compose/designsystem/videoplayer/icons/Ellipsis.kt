package com.kickstarter.ui.compose.designsystem.videoplayer.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Ellipsis: ImageVector
    get() {
        if (_Ellipsis != null) {
            return _Ellipsis!!
        }
        _Ellipsis = ImageVector.Builder(
            name = "Ellipsis",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(7f, 11.667f)
                curveTo(8.288f, 11.667f, 9.334f, 12.711f, 9.334f, 14f)
                curveTo(9.334f, 15.288f, 8.288f, 16.334f, 7f, 16.334f)
                curveTo(5.711f, 16.333f, 4.667f, 15.288f, 4.667f, 14f)
                curveTo(4.667f, 12.711f, 5.711f, 11.667f, 7f, 11.667f)
                close()
                moveTo(14f, 11.667f)
                curveTo(15.288f, 11.667f, 16.334f, 12.711f, 16.334f, 14f)
                curveTo(16.334f, 15.288f, 15.288f, 16.334f, 14f, 16.334f)
                curveTo(12.711f, 16.333f, 11.667f, 15.288f, 11.667f, 14f)
                curveTo(11.667f, 12.711f, 12.711f, 11.667f, 14f, 11.667f)
                close()
                moveTo(21f, 11.667f)
                curveTo(22.288f, 11.667f, 23.334f, 12.711f, 23.334f, 14f)
                curveTo(23.334f, 15.288f, 22.288f, 16.334f, 21f, 16.334f)
                curveTo(19.711f, 16.333f, 18.667f, 15.288f, 18.667f, 14f)
                curveTo(18.667f, 12.711f, 19.711f, 11.667f, 21f, 11.667f)
                close()
            }
        }.build()

        return _Ellipsis!!
    }

@Suppress("ObjectPropertyName")
private var _Ellipsis: ImageVector? = null

@Preview
@Composable
private fun EllipsisPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Ellipsis, contentDescription = null)
    }
}
