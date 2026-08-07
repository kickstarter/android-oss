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
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun RewindPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Rewind, contentDescription = null)
    }
}

@Preview
@Composable
private fun PlayPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Play, contentDescription = null)
    }
}

@Preview
@Composable
private fun ForwardPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Forward, contentDescription = null)
    }
}

@Preview
@Composable
private fun BookmarkPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Bookmark, contentDescription = null)
    }
}

@Preview
@Composable
private fun CheckPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Check, contentDescription = null)
    }
}

@Preview
@Composable
private fun EllipsisPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Ellipsis, contentDescription = null)
    }
}

@Preview
@Composable
private fun ReplyPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Share, contentDescription = null)
    }
}

@Preview
@Composable
private fun ClosePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Close, contentDescription = null)
    }
}

@Preview
@Composable
private fun ExpandPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Expand, contentDescription = null)
    }
}

@Preview
@Composable
private fun CollapsePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Collapse, contentDescription = null)
    }
}

@Preview
@Composable
private fun MutePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Mute, contentDescription = null)
    }
}

@Preview
@Composable
private fun VolumeUpPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = VolumeUp, contentDescription = null)
    }
}

val Play: ImageVector
    get() {
        if (_Play != null) {
            return _Play!!
        }
        _Play = ImageVector.Builder(
            name = "Play",
            defaultWidth = 65.dp,
            defaultHeight = 65.dp,
            viewportWidth = 65f,
            viewportHeight = 65f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(24.826f, 21.734f)
                curveTo(25.633f, 21.229f, 26.645f, 21.174f, 27.502f, 21.59f)
                lineTo(44.11f, 29.642f)
                curveTo(45.053f, 30.099f, 45.657f, 31.05f, 45.669f, 32.099f)
                curveTo(45.682f, 33.147f, 45.102f, 34.113f, 44.169f, 34.593f)
                lineTo(27.562f, 43.148f)
                curveTo(26.704f, 43.59f, 25.678f, 43.553f, 24.854f, 43.051f)
                curveTo(24.03f, 42.548f, 23.527f, 41.653f, 23.527f, 40.688f)
                verticalLineTo(24.08f)
                curveTo(23.527f, 23.127f, 24.018f, 22.24f, 24.826f, 21.734f)
                close()
            }
        }.build()

        return _Play!!
    }

@Suppress("ObjectPropertyName")
private var _Play: ImageVector? = null

val Rewind: ImageVector
    get() {
        if (_Rewind != null) {
            return _Rewind!!
        }
        _Rewind = ImageVector.Builder(
            name = "Rewind",
            defaultWidth = 38.dp,
            defaultHeight = 38.dp,
            viewportWidth = 38f,
            viewportHeight = 38f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(33.214f, 4.227f)
                    lineToRelative(-28.987f, 0f)
                    lineToRelative(-0f, 28.987f)
                    lineToRelative(28.987f, 0f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color.White)) {
                    moveTo(9.308f, 24.158f)
                    curveTo(12.31f, 29.357f, 18.958f, 31.138f, 24.157f, 28.137f)
                    curveTo(29.356f, 25.135f, 31.137f, 18.487f, 28.136f, 13.288f)
                    curveTo(25.134f, 8.089f, 18.486f, 6.308f, 13.287f, 9.309f)
                    curveTo(11.945f, 10.084f, 10.831f, 11.101f, 9.968f, 12.273f)
                    lineTo(9.751f, 10.455f)
                    curveTo(9.672f, 9.793f, 9.071f, 9.32f, 8.409f, 9.399f)
                    curveTo(7.747f, 9.478f, 7.274f, 10.08f, 7.353f, 10.742f)
                    lineTo(7.898f, 15.309f)
                    lineTo(7.917f, 15.425f)
                    curveTo(8.043f, 15.999f, 8.571f, 16.407f, 9.169f, 16.371f)
                    lineTo(14.561f, 16.047f)
                    curveTo(15.226f, 16.007f, 15.734f, 15.435f, 15.694f, 14.769f)
                    curveTo(15.653f, 14.103f, 15.082f, 13.596f, 14.416f, 13.636f)
                    lineTo(11.853f, 13.789f)
                    curveTo(12.533f, 12.843f, 13.42f, 12.022f, 14.495f, 11.402f)
                    curveTo(18.538f, 9.067f, 23.709f, 10.452f, 26.044f, 14.495f)
                    curveTo(28.379f, 18.539f, 26.993f, 23.711f, 22.949f, 26.045f)
                    curveTo(18.906f, 28.38f, 13.735f, 26.994f, 11.4f, 22.95f)
                    curveTo(11.029f, 22.307f, 10.751f, 21.635f, 10.564f, 20.95f)
                    curveTo(10.44f, 20.498f, 10.106f, 20.123f, 9.653f, 20.002f)
                    lineTo(9.52f, 19.966f)
                    curveTo(8.717f, 19.752f, 7.946f, 20.396f, 8.136f, 21.205f)
                    curveTo(8.374f, 22.217f, 8.762f, 23.212f, 9.308f, 24.158f)
                    close()
                }
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(18.6f, 22.378f)
                curveTo(18.155f, 22.378f, 17.755f, 22.293f, 17.4f, 22.123f)
                curveTo(17.047f, 21.95f, 16.765f, 21.713f, 16.555f, 21.413f)
                curveTo(16.344f, 21.112f, 16.232f, 20.768f, 16.217f, 20.383f)
                horizontalLineTo(17.283f)
                curveTo(17.309f, 20.695f, 17.447f, 20.952f, 17.698f, 21.153f)
                curveTo(17.949f, 21.354f, 18.25f, 21.455f, 18.6f, 21.455f)
                curveTo(18.879f, 21.455f, 19.127f, 21.391f, 19.342f, 21.263f)
                curveTo(19.56f, 21.133f, 19.731f, 20.954f, 19.854f, 20.727f)
                curveTo(19.979f, 20.5f, 20.042f, 20.241f, 20.042f, 19.949f)
                curveTo(20.042f, 19.653f, 19.978f, 19.389f, 19.85f, 19.157f)
                curveTo(19.722f, 18.926f, 19.546f, 18.743f, 19.321f, 18.611f)
                curveTo(19.098f, 18.478f, 18.843f, 18.411f, 18.554f, 18.408f)
                curveTo(18.334f, 18.408f, 18.112f, 18.446f, 17.89f, 18.522f)
                curveTo(17.667f, 18.598f, 17.487f, 18.697f, 17.35f, 18.82f)
                lineTo(16.345f, 18.671f)
                lineTo(16.754f, 15.006f)
                horizontalLineTo(20.752f)
                verticalLineTo(15.947f)
                horizontalLineTo(17.666f)
                lineTo(17.435f, 17.982f)
                horizontalLineTo(17.478f)
                curveTo(17.62f, 17.845f, 17.808f, 17.73f, 18.043f, 17.638f)
                curveTo(18.279f, 17.545f, 18.533f, 17.499f, 18.803f, 17.499f)
                curveTo(19.245f, 17.499f, 19.639f, 17.604f, 19.985f, 17.815f)
                curveTo(20.333f, 18.026f, 20.607f, 18.313f, 20.805f, 18.678f)
                curveTo(21.007f, 19.04f, 21.106f, 19.457f, 21.104f, 19.928f)
                curveTo(21.106f, 20.399f, 21f, 20.819f, 20.784f, 21.189f)
                curveTo(20.571f, 21.558f, 20.275f, 21.849f, 19.896f, 22.062f)
                curveTo(19.52f, 22.273f, 19.088f, 22.378f, 18.6f, 22.378f)
                close()
            }
        }.build()

        return _Rewind!!
    }

@Suppress("ObjectPropertyName")
private var _Rewind: ImageVector? = null

val Forward: ImageVector
    get() {
        if (_Forward != null) {
            return _Forward!!
        }
        _Forward = ImageVector.Builder(
            name = "Forward",
            defaultWidth = 38.dp,
            defaultHeight = 38.dp,
            viewportWidth = 38f,
            viewportHeight = 38f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(4.227f, 4.227f)
                    horizontalLineToRelative(28.987f)
                    verticalLineToRelative(28.987f)
                    horizontalLineToRelative(-28.987f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color.White)) {
                    moveTo(28.133f, 24.158f)
                    curveTo(25.132f, 29.357f, 18.483f, 31.138f, 13.284f, 28.137f)
                    curveTo(8.085f, 25.135f, 6.304f, 18.487f, 9.306f, 13.288f)
                    curveTo(12.307f, 8.089f, 18.956f, 6.308f, 24.155f, 9.309f)
                    curveTo(25.496f, 10.084f, 26.611f, 11.101f, 27.474f, 12.273f)
                    lineTo(27.69f, 10.455f)
                    curveTo(27.769f, 9.793f, 28.371f, 9.32f, 29.033f, 9.399f)
                    curveTo(29.695f, 9.478f, 30.168f, 10.08f, 30.089f, 10.742f)
                    lineTo(29.544f, 15.309f)
                    lineTo(29.524f, 15.425f)
                    curveTo(29.398f, 15.999f, 28.871f, 16.407f, 28.272f, 16.371f)
                    lineTo(22.881f, 16.047f)
                    curveTo(22.215f, 16.007f, 21.708f, 15.435f, 21.748f, 14.769f)
                    curveTo(21.788f, 14.103f, 22.36f, 13.596f, 23.025f, 13.636f)
                    lineTo(25.588f, 13.789f)
                    curveTo(24.909f, 12.842f, 24.021f, 12.022f, 22.947f, 11.401f)
                    curveTo(18.903f, 9.067f, 13.732f, 10.452f, 11.397f, 14.495f)
                    curveTo(9.063f, 18.539f, 10.448f, 23.711f, 14.492f, 26.045f)
                    curveTo(18.536f, 28.38f, 23.706f, 26.994f, 26.041f, 22.95f)
                    curveTo(26.412f, 22.307f, 26.69f, 21.635f, 26.878f, 20.95f)
                    curveTo(27.001f, 20.498f, 27.335f, 20.123f, 27.788f, 20.002f)
                    lineTo(27.922f, 19.966f)
                    curveTo(28.724f, 19.751f, 29.495f, 20.396f, 29.305f, 21.205f)
                    curveTo(29.067f, 22.217f, 28.68f, 23.211f, 28.133f, 24.158f)
                    close()
                }
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(18.6f, 22.378f)
                curveTo(18.155f, 22.378f, 17.755f, 22.293f, 17.4f, 22.122f)
                curveTo(17.047f, 21.95f, 16.765f, 21.713f, 16.555f, 21.412f)
                curveTo(16.344f, 21.111f, 16.232f, 20.768f, 16.217f, 20.382f)
                horizontalLineTo(17.283f)
                curveTo(17.309f, 20.695f, 17.447f, 20.952f, 17.698f, 21.153f)
                curveTo(17.949f, 21.354f, 18.25f, 21.455f, 18.6f, 21.455f)
                curveTo(18.879f, 21.455f, 19.127f, 21.391f, 19.342f, 21.263f)
                curveTo(19.56f, 21.133f, 19.731f, 20.954f, 19.854f, 20.727f)
                curveTo(19.979f, 20.5f, 20.042f, 20.24f, 20.042f, 19.949f)
                curveTo(20.042f, 19.653f, 19.978f, 19.389f, 19.85f, 19.157f)
                curveTo(19.722f, 18.925f, 19.546f, 18.743f, 19.321f, 18.61f)
                curveTo(19.098f, 18.478f, 18.843f, 18.41f, 18.554f, 18.408f)
                curveTo(18.334f, 18.408f, 18.112f, 18.446f, 17.89f, 18.522f)
                curveTo(17.667f, 18.597f, 17.487f, 18.697f, 17.35f, 18.82f)
                lineTo(16.345f, 18.671f)
                lineTo(16.754f, 15.006f)
                horizontalLineTo(20.752f)
                verticalLineTo(15.947f)
                horizontalLineTo(17.666f)
                lineTo(17.435f, 17.982f)
                horizontalLineTo(17.478f)
                curveTo(17.62f, 17.844f, 17.808f, 17.73f, 18.043f, 17.637f)
                curveTo(18.279f, 17.545f, 18.533f, 17.499f, 18.803f, 17.499f)
                curveTo(19.245f, 17.499f, 19.639f, 17.604f, 19.985f, 17.815f)
                curveTo(20.333f, 18.026f, 20.607f, 18.313f, 20.805f, 18.678f)
                curveTo(21.007f, 19.04f, 21.106f, 19.457f, 21.104f, 19.928f)
                curveTo(21.106f, 20.399f, 21f, 20.819f, 20.784f, 21.188f)
                curveTo(20.571f, 21.558f, 20.275f, 21.849f, 19.896f, 22.062f)
                curveTo(19.52f, 22.273f, 19.088f, 22.378f, 18.6f, 22.378f)
                close()
            }
        }.build()

        return _Forward!!
    }

@Suppress("ObjectPropertyName")
private var _Forward: ImageVector? = null

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

val BookmarkFilled: ImageVector
    get() {
        if (_BookmarkFilled != null) {
            return _BookmarkFilled!!
        }
        _BookmarkFilled = ImageVector.Builder(
            name = "BookmarkFilled",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(fill = SolidColor(Color.White)) {
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

        return _BookmarkFilled!!
    }

@Suppress("ObjectPropertyName")
private var _BookmarkFilled: ImageVector? = null

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

val Close: ImageVector
    get() {
        if (_Close != null) {
            return _Close!!
        }
        _Close = ImageVector.Builder(
            name = "Close",
            defaultWidth = 45.dp,
            defaultHeight = 45.dp,
            viewportWidth = 45f,
            viewportHeight = 45f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(29.495f, 15.352f)
                curveTo(29.983f, 15.84f, 29.983f, 16.631f, 29.495f, 17.119f)
                lineTo(24.191f, 22.423f)
                lineTo(29.495f, 27.727f)
                curveTo(29.983f, 28.215f, 29.983f, 29.007f, 29.495f, 29.495f)
                curveTo(29.007f, 29.983f, 28.215f, 29.983f, 27.727f, 29.495f)
                lineTo(22.423f, 24.191f)
                lineTo(17.12f, 29.494f)
                curveTo(16.632f, 29.982f, 15.841f, 29.982f, 15.353f, 29.494f)
                curveTo(14.865f, 29.006f, 14.865f, 28.214f, 15.353f, 27.726f)
                lineTo(20.655f, 22.423f)
                lineTo(15.353f, 17.121f)
                curveTo(14.864f, 16.633f, 14.865f, 15.841f, 15.353f, 15.353f)
                curveTo(15.841f, 14.865f, 16.632f, 14.865f, 17.12f, 15.353f)
                lineTo(22.423f, 20.656f)
                lineTo(27.727f, 15.352f)
                curveTo(28.215f, 14.863f, 29.007f, 14.864f, 29.495f, 15.352f)
                close()
            }
        }.build()

        return _Close!!
    }

@Suppress("ObjectPropertyName")
private var _Close: ImageVector? = null

val Expand: ImageVector
    get() {
        if (_Expand != null) {
            return _Expand!!
        }
        _Expand = ImageVector.Builder(
            name = "Expand",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(20f, 12f)
                curveTo(19.448f, 12f, 19f, 11.552f, 19f, 11f)
                verticalLineTo(6.414f)
                lineTo(14.707f, 10.707f)
                curveTo(14.317f, 11.098f, 13.683f, 11.098f, 13.293f, 10.707f)
                curveTo(12.902f, 10.317f, 12.902f, 9.683f, 13.293f, 9.293f)
                lineTo(17.586f, 5f)
                lineTo(13f, 5f)
                curveTo(12.448f, 5f, 12f, 4.552f, 12f, 4f)
                curveTo(12f, 3.448f, 12.448f, 3f, 13f, 3f)
                horizontalLineTo(20f)
                curveTo(20.265f, 3f, 20.52f, 3.105f, 20.707f, 3.293f)
                curveTo(20.895f, 3.48f, 21f, 3.735f, 21f, 4f)
                lineTo(21f, 11f)
                curveTo(21f, 11.552f, 20.552f, 12f, 20f, 12f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(4f, 12f)
                curveTo(4.552f, 12f, 5f, 12.448f, 5f, 13f)
                verticalLineTo(17.586f)
                lineTo(9.293f, 13.293f)
                curveTo(9.683f, 12.902f, 10.317f, 12.902f, 10.707f, 13.293f)
                curveTo(11.098f, 13.683f, 11.098f, 14.317f, 10.707f, 14.707f)
                lineTo(6.414f, 19f)
                horizontalLineTo(11f)
                curveTo(11.552f, 19f, 12f, 19.448f, 12f, 20f)
                curveTo(12f, 20.552f, 11.552f, 21f, 11f, 21f)
                horizontalLineTo(4.001f)
                lineTo(3.997f, 21f)
                curveTo(3.862f, 21f, 3.734f, 20.973f, 3.617f, 20.924f)
                curveTo(3.499f, 20.875f, 3.389f, 20.803f, 3.293f, 20.707f)
                curveTo(3.197f, 20.611f, 3.125f, 20.501f, 3.076f, 20.383f)
                curveTo(3.027f, 20.265f, 3f, 20.136f, 3f, 20f)
                verticalLineTo(13f)
                curveTo(3f, 12.448f, 3.448f, 12f, 4f, 12f)
                close()
            }
        }.build()

        return _Expand!!
    }

@Suppress("ObjectPropertyName")
private var _Expand: ImageVector? = null

val Collapse: ImageVector
    get() {
        if (_Collapse != null) {
            return _Collapse!!
        }
        _Collapse = ImageVector.Builder(
            name = "Collapse",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(13f, 3f)
                curveTo(13.552f, 3f, 14f, 3.448f, 14f, 4f)
                verticalLineTo(8.586f)
                lineTo(18.293f, 4.293f)
                curveTo(18.683f, 3.902f, 19.317f, 3.902f, 19.707f, 4.293f)
                curveTo(20.098f, 4.683f, 20.098f, 5.317f, 19.707f, 5.707f)
                lineTo(15.414f, 10f)
                horizontalLineTo(20f)
                curveTo(20.552f, 10f, 21f, 10.448f, 21f, 11f)
                curveTo(21f, 11.552f, 20.552f, 12f, 20f, 12f)
                horizontalLineTo(13.001f)
                lineTo(12.997f, 12f)
                curveTo(12.863f, 12f, 12.734f, 11.973f, 12.617f, 11.924f)
                curveTo(12.499f, 11.875f, 12.389f, 11.803f, 12.293f, 11.707f)
                curveTo(12.197f, 11.611f, 12.125f, 11.501f, 12.076f, 11.383f)
                curveTo(12.027f, 11.265f, 12f, 11.136f, 12f, 11f)
                verticalLineTo(4f)
                curveTo(12f, 3.448f, 12.448f, 3f, 13f, 3f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(11f, 21f)
                curveTo(10.448f, 21f, 10f, 20.552f, 10f, 20f)
                lineTo(10f, 15.414f)
                lineTo(5.707f, 19.707f)
                curveTo(5.317f, 20.098f, 4.683f, 20.098f, 4.293f, 19.707f)
                curveTo(3.902f, 19.317f, 3.902f, 18.683f, 4.293f, 18.293f)
                lineTo(8.586f, 14f)
                lineTo(4f, 14f)
                curveTo(3.448f, 14f, 3f, 13.552f, 3f, 13f)
                curveTo(3f, 12.448f, 3.448f, 12f, 4f, 12f)
                lineTo(10.999f, 12f)
                lineTo(11.003f, 12f)
                curveTo(11.137f, 12f, 11.266f, 12.027f, 11.383f, 12.076f)
                curveTo(11.501f, 12.125f, 11.611f, 12.197f, 11.707f, 12.293f)
                curveTo(11.803f, 12.389f, 11.875f, 12.499f, 11.924f, 12.617f)
                curveTo(11.973f, 12.735f, 12f, 12.864f, 12f, 13f)
                lineTo(12f, 20f)
                curveTo(12f, 20.552f, 11.552f, 21f, 11f, 21f)
                close()
            }
        }.build()

        return _Collapse!!
    }

@Suppress("ObjectPropertyName")
private var _Collapse: ImageVector? = null

val Mute: ImageVector
    get() {
        if (_Mute != null) {
            return _Mute!!
        }
        _Mute = ImageVector.Builder(
            name = "Mute",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFE0E0E0)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(6f, 9f)
                horizontalLineTo(7.705f)
                curveTo(8.597f, 9f, 9.435f, 8.604f, 10f, 7.932f)
                lineTo(10f, 16.068f)
                curveTo(9.435f, 15.396f, 8.597f, 15f, 7.705f, 15f)
                horizontalLineTo(6f)
                lineTo(6f, 9f)
                close()
                moveTo(7.705f, 7f)
                curveTo(8.05f, 7f, 8.371f, 6.822f, 8.553f, 6.529f)
                lineTo(10.151f, 3.966f)
                curveTo(10.683f, 3.112f, 12f, 3.489f, 12f, 4.495f)
                lineTo(12f, 19.506f)
                curveTo(12f, 20.511f, 10.683f, 20.888f, 10.151f, 20.035f)
                lineTo(8.553f, 17.471f)
                curveTo(8.371f, 17.178f, 8.05f, 17f, 7.705f, 17f)
                horizontalLineTo(5f)
                curveTo(4.448f, 17f, 4f, 16.552f, 4f, 16f)
                lineTo(4f, 8f)
                curveTo(4f, 7.448f, 4.448f, 7f, 5f, 7f)
                horizontalLineTo(7.705f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE0E0E0))) {
                moveTo(18.414f, 14.828f)
                curveTo(18.805f, 15.219f, 19.438f, 15.219f, 19.828f, 14.828f)
                curveTo(20.219f, 14.438f, 20.219f, 13.804f, 19.828f, 13.414f)
                lineTo(15.586f, 9.171f)
                curveTo(15.195f, 8.781f, 14.562f, 8.781f, 14.171f, 9.171f)
                curveTo(13.781f, 9.562f, 13.781f, 10.195f, 14.171f, 10.585f)
                lineTo(18.414f, 14.828f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE0E0E0))) {
                moveTo(19.828f, 10.586f)
                curveTo(20.219f, 10.196f, 20.219f, 9.562f, 19.828f, 9.172f)
                curveTo(19.438f, 8.781f, 18.804f, 8.781f, 18.414f, 9.172f)
                lineTo(14.171f, 13.415f)
                curveTo(13.781f, 13.805f, 13.781f, 14.438f, 14.171f, 14.829f)
                curveTo(14.562f, 15.219f, 15.195f, 15.219f, 15.585f, 14.829f)
                lineTo(19.828f, 10.586f)
                close()
            }
        }.build()

        return _Mute!!
    }

@Suppress("ObjectPropertyName")
private var _Mute: ImageVector? = null

val VolumeUp: ImageVector
    get() {
        if (_VolumeUp != null) {
            return _VolumeUp!!
        }
        _VolumeUp = ImageVector.Builder(
            name = "VolumeUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFE0E0E0)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(6f, 9f)
                horizontalLineTo(7.705f)
                curveTo(8.597f, 9f, 9.435f, 8.604f, 10f, 7.932f)
                lineTo(10f, 16.068f)
                curveTo(9.435f, 15.396f, 8.597f, 15f, 7.705f, 15f)
                horizontalLineTo(6f)
                lineTo(6f, 9f)
                close()
                moveTo(7.705f, 7f)
                curveTo(8.05f, 7f, 8.371f, 6.822f, 8.553f, 6.529f)
                lineTo(10.151f, 3.966f)
                curveTo(10.683f, 3.112f, 12f, 3.489f, 12f, 4.495f)
                lineTo(12f, 19.506f)
                curveTo(12f, 20.511f, 10.683f, 20.888f, 10.151f, 20.035f)
                lineTo(8.553f, 17.471f)
                curveTo(8.371f, 17.178f, 8.05f, 17f, 7.705f, 17f)
                horizontalLineTo(5f)
                curveTo(4.448f, 17f, 4f, 16.552f, 4f, 16f)
                lineTo(4f, 8f)
                curveTo(4f, 7.448f, 4.448f, 7f, 5f, 7f)
                horizontalLineTo(7.705f)
                close()
                moveTo(16.491f, 18.861f)
                curveTo(16.966f, 19.142f, 17.579f, 18.985f, 17.861f, 18.509f)
                lineTo(17f, 18f)
                curveTo(17.861f, 18.509f, 17.861f, 18.509f, 17.861f, 18.509f)
                lineTo(17.864f, 18.504f)
                lineTo(17.869f, 18.495f)
                lineTo(17.889f, 18.461f)
                curveTo(17.905f, 18.432f, 17.929f, 18.391f, 17.958f, 18.339f)
                curveTo(18.017f, 18.234f, 18.1f, 18.083f, 18.198f, 17.895f)
                curveTo(18.395f, 17.52f, 18.657f, 16.991f, 18.92f, 16.375f)
                curveTo(19.432f, 15.173f, 20f, 13.519f, 20f, 12f)
                curveTo(20f, 10.481f, 19.432f, 8.827f, 18.92f, 7.625f)
                curveTo(18.657f, 7.009f, 18.395f, 6.48f, 18.198f, 6.105f)
                curveTo(18.1f, 5.917f, 18.017f, 5.766f, 17.958f, 5.661f)
                curveTo(17.929f, 5.609f, 17.905f, 5.568f, 17.889f, 5.539f)
                lineTo(17.869f, 5.505f)
                lineTo(17.864f, 5.496f)
                lineTo(17.861f, 5.492f)
                curveTo(17.861f, 5.492f, 17.861f, 5.491f, 17f, 6f)
                lineTo(17.861f, 5.492f)
                curveTo(17.58f, 5.016f, 16.966f, 4.858f, 16.491f, 5.139f)
                curveTo(16.016f, 5.421f, 15.858f, 6.034f, 16.139f, 6.509f)
                lineTo(16.142f, 6.514f)
                lineTo(16.156f, 6.538f)
                curveTo(16.169f, 6.56f, 16.188f, 6.594f, 16.214f, 6.639f)
                curveTo(16.264f, 6.73f, 16.338f, 6.864f, 16.427f, 7.033f)
                curveTo(16.605f, 7.373f, 16.843f, 7.853f, 17.08f, 8.41f)
                curveTo(17.568f, 9.554f, 18f, 10.9f, 18f, 12f)
                curveTo(18f, 13.1f, 17.568f, 14.446f, 17.08f, 15.59f)
                curveTo(16.843f, 16.147f, 16.605f, 16.627f, 16.427f, 16.967f)
                curveTo(16.338f, 17.136f, 16.264f, 17.271f, 16.214f, 17.361f)
                curveTo(16.188f, 17.406f, 16.169f, 17.44f, 16.156f, 17.462f)
                lineTo(16.142f, 17.486f)
                lineTo(16.139f, 17.491f)
                moveTo(16.491f, 18.861f)
                curveTo(16.016f, 18.58f, 15.858f, 17.966f, 16.139f, 17.491f)
                lineTo(16.491f, 18.861f)
                close()
                moveTo(13.468f, 15.847f)
                curveTo(13.936f, 16.141f, 14.553f, 16f, 14.847f, 15.532f)
                lineTo(14f, 15f)
                curveTo(14.847f, 15.532f, 14.847f, 15.532f, 14.847f, 15.532f)
                lineTo(14.848f, 15.53f)
                lineTo(14.849f, 15.528f)
                lineTo(14.853f, 15.522f)
                lineTo(14.865f, 15.503f)
                curveTo(14.875f, 15.487f, 14.888f, 15.465f, 14.904f, 15.438f)
                curveTo(14.936f, 15.383f, 14.981f, 15.306f, 15.033f, 15.21f)
                curveTo(15.138f, 15.018f, 15.277f, 14.748f, 15.417f, 14.427f)
                curveTo(15.686f, 13.81f, 16f, 12.91f, 16f, 12f)
                curveTo(16f, 11.09f, 15.686f, 10.19f, 15.417f, 9.573f)
                curveTo(15.277f, 9.252f, 15.138f, 8.982f, 15.033f, 8.791f)
                curveTo(14.981f, 8.694f, 14.936f, 8.617f, 14.904f, 8.563f)
                curveTo(14.888f, 8.535f, 14.875f, 8.513f, 14.865f, 8.497f)
                lineTo(14.853f, 8.478f)
                lineTo(14.849f, 8.472f)
                lineTo(14.847f, 8.469f)
                curveTo(14.847f, 8.469f, 14.847f, 8.468f, 14f, 9f)
                lineTo(14.847f, 8.469f)
                curveTo(14.554f, 8.001f, 13.936f, 7.859f, 13.468f, 8.153f)
                curveTo(13.001f, 8.447f, 12.86f, 9.063f, 13.152f, 9.531f)
                lineTo(13.153f, 9.532f)
                lineTo(13.158f, 9.539f)
                curveTo(13.163f, 9.547f, 13.171f, 9.561f, 13.182f, 9.58f)
                curveTo(13.204f, 9.618f, 13.238f, 9.676f, 13.279f, 9.751f)
                curveTo(13.362f, 9.902f, 13.473f, 10.118f, 13.583f, 10.371f)
                curveTo(13.814f, 10.902f, 14f, 11.502f, 14f, 12f)
                curveTo(14f, 12.498f, 13.814f, 13.098f, 13.583f, 13.629f)
                curveTo(13.473f, 13.882f, 13.362f, 14.098f, 13.279f, 14.249f)
                curveTo(13.238f, 14.324f, 13.204f, 14.382f, 13.182f, 14.42f)
                curveTo(13.171f, 14.439f, 13.163f, 14.453f, 13.158f, 14.461f)
                lineTo(13.153f, 14.468f)
                curveTo(12.86f, 14.936f, 13f, 15.553f, 13.468f, 15.847f)
                close()
            }
        }.build()

        return _VolumeUp!!
    }

@Suppress("ObjectPropertyName")
private var _VolumeUp: ImageVector? = null
