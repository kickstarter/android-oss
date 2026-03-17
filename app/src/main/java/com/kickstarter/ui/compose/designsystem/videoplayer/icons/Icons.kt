package com.kickstarter.ui.compose.designsystem.videoplayer.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
