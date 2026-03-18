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

val Check: ImageVector
    get() {
        if (_Check != null) {
            return _Check!!
        }
        _Check = ImageVector.Builder(
            name = "Check",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(19.669f, 6.257f)
                curveTo(20.08f, 6.626f, 20.113f, 7.258f, 19.743f, 7.669f)
                lineTo(10.743f, 17.669f)
                curveTo(10.56f, 17.873f, 10.3f, 17.993f, 10.026f, 18f)
                curveTo(9.752f, 18.007f, 9.487f, 17.901f, 9.293f, 17.707f)
                lineTo(4.293f, 12.707f)
                curveTo(3.902f, 12.317f, 3.902f, 11.683f, 4.293f, 11.293f)
                curveTo(4.683f, 10.902f, 5.317f, 10.902f, 5.707f, 11.293f)
                lineTo(9.962f, 15.548f)
                lineTo(18.257f, 6.331f)
                curveTo(18.626f, 5.921f, 19.258f, 5.887f, 19.669f, 6.257f)
                close()
            }
        }.build()

        return _Check!!
    }

@Suppress("ObjectPropertyName")
private var _Check: ImageVector? = null

@Preview
@Composable
private fun CheckPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Check, contentDescription = null)
    }
}
