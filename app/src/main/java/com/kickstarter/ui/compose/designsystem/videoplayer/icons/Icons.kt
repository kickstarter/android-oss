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
