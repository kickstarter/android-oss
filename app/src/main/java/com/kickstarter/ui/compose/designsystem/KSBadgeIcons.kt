package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun WhatshotPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Whatshot, contentDescription = null)
    }
}

val featuredRewardStar: ImageVector
    get() {
        if (_featuredRewardStar != null) return _featuredRewardStar!!

        _featuredRewardStar = ImageVector.Builder(
            name = "featuredRewardStar",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            group {
                path(
                    fill = SolidColor(purple_08),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(11.233f, 3.93451f)
                    lineTo(12.5116f, 3.91658f)
                    curveTo(15.415f, 3.87587f, 16.6769f, 7.57215f, 14.3547f, 9.31544f)
                    lineTo(13.1531f, 10.2175f)
                    lineTo(13.6067f, 11.6367f)
                    curveTo(14.509f, 14.4601f, 11.206f, 16.7425f, 8.88419f, 14.8999f)
                    lineTo(8.00001f, 14.1982f)
                    lineTo(7.11583f, 14.8999f)
                    curveTo(4.79398f, 16.7425f, 1.49098f, 14.4601f, 2.39335f, 11.6367f)
                    lineTo(2.8469f, 10.2175f)
                    lineTo(1.64528f, 9.31544f)
                    curveTo(-0.676839f, 7.57215f, 0.585059f, 3.87587f, 3.48846f, 3.91658f)
                    lineTo(4.76701f, 3.93451f)
                    lineTo(5.12037f, 2.72492f)
                    curveTo(5.96123f, -0.153424f, 10.0388f, -0.153426f, 10.8796f, 2.72492f)
                    lineTo(11.233f, 3.93451f)
                    close()
                    moveTo(8.95989f, 3.28575f)
                    curveTo(8.6796f, 2.3263f, 7.32042f, 2.3263f, 7.04013f, 3.28575f)
                    lineTo(6.47347f, 5.22549f)
                    curveTo(6.34743f, 5.65694f, 5.94901f, 5.95128f, 5.49957f, 5.94497f)
                    lineTo(3.46042f, 5.91638f)
                    curveTo(2.49262f, 5.90281f, 2.07199f, 7.13491f, 2.84603f, 7.716f)
                    lineTo(4.61627f, 9.04498f)
                    curveTo(4.9567f, 9.30055f, 5.09802f, 9.74364f, 4.96843f, 10.1491f)
                    lineTo(4.29842f, 12.2455f)
                    curveTo(3.99763f, 13.1867f, 5.09863f, 13.9475f, 5.87258f, 13.3333f)
                    lineTo(7.37839f, 12.1383f)
                    curveTo(7.74245f, 11.8494f, 8.25757f, 11.8494f, 8.62163f, 12.1383f)
                    lineTo(10.1274f, 13.3333f)
                    curveTo(10.9014f, 13.9475f, 12.0024f, 13.1867f, 11.7016f, 12.2455f)
                    lineTo(11.0316f, 10.1491f)
                    curveTo(10.902f, 9.74364f, 11.0433f, 9.30055f, 11.3838f, 9.04498f)
                    lineTo(13.154f, 7.716f)
                    curveTo(13.928f, 7.1349f, 13.5074f, 5.90281f, 12.5396f, 5.91638f)
                    lineTo(10.5005f, 5.94497f)
                    curveTo(10.051f, 5.95128f, 9.65259f, 5.65694f, 9.52655f, 5.22549f)
                    lineTo(8.95989f, 3.28575f)
                    close()
                }
            }
        }.build()

        return _featuredRewardStar!!
    }

private var _featuredRewardStar: ImageVector? = null

val secretRewardLock: ImageVector
    get() {
        if (_secretRewardLock != null) return _secretRewardLock!!

        _secretRewardLock = ImageVector.Builder(
            name = "secretRewardLock",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(green_06),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(8f, 1f)
                curveTo(5.79086f, 1f, 4f, 2.79086f, 4f, 5f)
                curveTo(4f, 5.05652f, 4.00469f, 5.11194f, 4.0137f, 5.1659f)
                curveTo(2.84136f, 5.57385f, 2f, 6.68867f, 2f, 8f)
                verticalLineTo(12f)
                curveTo(2f, 13.6569f, 3.34315f, 15f, 5f, 15f)
                horizontalLineTo(11f)
                curveTo(12.6569f, 15f, 14f, 13.6569f, 14f, 12f)
                verticalLineTo(8f)
                curveTo(14f, 6.68867f, 13.1586f, 5.57385f, 11.9863f, 5.1659f)
                curveTo(11.9953f, 5.11194f, 12f, 5.05652f, 12f, 5f)
                curveTo(12f, 2.79086f, 10.2091f, 1f, 8f, 1f)
                close()
                moveTo(8f, 3f)
                curveTo(9.10457f, 3f, 10f, 3.89543f, 10f, 5f)
                horizontalLineTo(6f)
                curveTo(6f, 3.89543f, 6.89543f, 3f, 8f, 3f)
                close()
                moveTo(5f, 7f)
                curveTo(4.44772f, 7f, 4f, 7.44772f, 4f, 8f)
                verticalLineTo(12f)
                curveTo(4f, 12.5523f, 4.44772f, 13f, 5f, 13f)
                horizontalLineTo(11f)
                curveTo(11.5523f, 13f, 12f, 12.5523f, 12f, 12f)
                verticalLineTo(8f)
                curveTo(12f, 7.44772f, 11.5523f, 7f, 11f, 7f)
                horizontalLineTo(5f)
                close()
                moveTo(9f, 9f)
                curveTo(9f, 8.44772f, 8.55228f, 8f, 8f, 8f)
                curveTo(7.44772f, 8f, 7f, 8.44772f, 7f, 9f)
                verticalLineTo(11f)
                curveTo(7f, 11.5523f, 7.44772f, 12f, 8f, 12f)
                curveTo(8.55228f, 12f, 9f, 11.5523f, 9f, 11f)
                verticalLineTo(9f)
                close()
            }
        }.build()

        return _secretRewardLock!!
    }

private var _secretRewardLock: ImageVector? = null

val projectWeLove: ImageVector
    get() {
        if (_projectWeLove != null) return _projectWeLove!!

        _projectWeLove = ImageVector.Builder(
            name = "projectWeLove",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(green_05),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(5.787f, 1.669f)
                curveTo(6.792f, 0.003f, 9.208f, 0.003f, 10.213f, 1.669f)
                curveTo(10.672f, 2.429f, 11.489f, 2.901f, 12.377f, 2.918f)
                curveTo(14.322f, 2.955f, 15.53f, 5.048f, 14.59f, 6.751f)
                curveTo(14.16f, 7.528f, 14.16f, 8.472f, 14.59f, 9.249f)
                curveTo(15.53f, 10.953f, 14.322f, 13.045f, 12.377f, 13.082f)
                curveTo(11.489f, 13.099f, 10.672f, 13.571f, 10.213f, 14.331f)
                curveTo(9.208f, 15.997f, 6.792f, 15.997f, 5.787f, 14.331f)
                curveTo(5.328f, 13.571f, 4.511f, 13.099f, 3.623f, 13.082f)
                curveTo(1.678f, 13.045f, 0.47f, 10.953f, 1.41f, 9.249f)
                curveTo(1.84f, 8.472f, 1.84f, 7.528f, 1.41f, 6.751f)
                curveTo(0.47f, 5.048f, 1.678f, 2.955f, 3.623f, 2.918f)
                curveTo(4.511f, 2.901f, 5.328f, 2.429f, 5.787f, 1.669f)
                close()
                moveTo(8.036f, 7.034f)
                curveTo(8.372f, 7.022f, 8.68f, 6.841f, 8.854f, 6.554f)
                curveTo(8.855f, 6.554f, 8.855f, 6.553f, 8.856f, 6.552f)
                curveTo(8.861f, 6.545f, 8.873f, 6.527f, 8.894f, 6.502f)
                curveTo(8.935f, 6.451f, 9.003f, 6.376f, 9.098f, 6.3f)
                curveTo(9.282f, 6.154f, 9.569f, 6f, 10f, 6f)
                curveTo(10.42f, 6f, 10.615f, 6.141f, 10.733f, 6.293f)
                curveTo(10.88f, 6.482f, 11f, 6.821f, 11f, 7.322f)
                curveTo(11f, 7.674f, 10.864f, 8.235f, 10.176f, 8.925f)
                lineTo(8f, 10.707f)
                lineTo(5.824f, 8.925f)
                curveTo(5.136f, 8.235f, 5f, 7.674f, 5f, 7.322f)
                curveTo(5f, 6.786f, 5.109f, 6.444f, 5.239f, 6.261f)
                curveTo(5.329f, 6.134f, 5.482f, 6f, 5.896f, 6f)
                curveTo(6.333f, 6f, 6.656f, 6.158f, 6.879f, 6.323f)
                curveTo(6.992f, 6.406f, 7.076f, 6.488f, 7.128f, 6.545f)
                curveTo(7.153f, 6.574f, 7.17f, 6.595f, 7.179f, 6.605f)
                curveTo(7.18f, 6.608f, 7.182f, 6.609f, 7.183f, 6.611f)
                curveTo(7.377f, 6.886f, 7.698f, 7.046f, 8.036f, 7.034f)
                close()
            }
        }.build()

        return _projectWeLove!!
    }

private var _projectWeLove: ImageVector? = null

val Whatshot: ImageVector
    get() {
        if (_Whatshot != null) {
            return _Whatshot!!
        }
        _Whatshot = ImageVector.Builder(
            name = "Whatshot",
            defaultWidth = 12.dp,
            defaultHeight = 15.dp,
            viewportWidth = 12f,
            viewportHeight = 15f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(6.074f, 0.083f)
                curveTo(5.432f, 0.546f, 4.908f, 1.155f, 4.548f, 1.86f)
                curveTo(4.187f, 2.565f, 3.999f, 3.346f, 4f, 4.138f)
                verticalLineTo(4.17f)
                curveTo(4.004f, 4.77f, 4.114f, 5.346f, 4.311f, 5.879f)
                curveTo(4.471f, 6.307f, 4.107f, 6.789f, 3.701f, 6.579f)
                curveTo(2.946f, 6.187f, 2.304f, 5.61f, 1.833f, 4.902f)
                curveTo(1.631f, 4.598f, 1.185f, 4.539f, 0.985f, 4.844f)
                curveTo(0.326f, 5.848f, -0.017f, 7.027f, 0.001f, 8.228f)
                curveTo(0.019f, 9.429f, 0.397f, 10.597f, 1.086f, 11.581f)
                curveTo(1.776f, 12.565f, 2.745f, 13.319f, 3.867f, 13.746f)
                curveTo(4.99f, 14.173f, 6.215f, 14.253f, 7.384f, 13.976f)
                curveTo(8.553f, 13.699f, 9.612f, 13.077f, 10.424f, 12.192f)
                curveTo(11.235f, 11.306f, 11.762f, 10.197f, 11.936f, 9.008f)
                curveTo(12.111f, 7.82f, 11.924f, 6.606f, 11.401f, 5.525f)
                curveTo(10.878f, 4.444f, 10.042f, 3.544f, 9.002f, 2.943f)
                lineTo(8.998f, 2.936f)
                curveTo(7.994f, 2.358f, 7.223f, 1.447f, 6.818f, 0.362f)
                curveTo(6.702f, 0.052f, 6.341f, -0.109f, 6.074f, 0.083f)
                close()
                moveTo(6.854f, 6.261f)
                curveTo(7.326f, 6.401f, 7.756f, 6.655f, 8.106f, 7f)
                curveTo(8.457f, 7.345f, 8.717f, 7.771f, 8.864f, 8.24f)
                curveTo(9.012f, 8.71f, 9.041f, 9.208f, 8.95f, 9.691f)
                curveTo(8.86f, 10.175f, 8.651f, 10.629f, 8.344f, 11.013f)
                curveTo(8.037f, 11.397f, 7.64f, 11.7f, 7.188f, 11.894f)
                curveTo(6.736f, 12.089f, 6.243f, 12.17f, 5.753f, 12.129f)
                curveTo(5.263f, 12.088f, 4.79f, 11.928f, 4.377f, 11.662f)
                curveTo(3.963f, 11.395f, 3.621f, 11.031f, 3.381f, 10.602f)
                curveTo(3.176f, 10.237f, 3.596f, 9.908f, 4.001f, 10.012f)
                curveTo(4.614f, 10.17f, 5.256f, 10.18f, 5.874f, 10.042f)
                curveTo(6.162f, 9.977f, 6.287f, 9.656f, 6.195f, 9.376f)
                curveTo(6.065f, 8.976f, 5.999f, 8.558f, 6f, 8.137f)
                curveTo(6f, 7.552f, 6.126f, 6.997f, 6.351f, 6.496f)
                curveTo(6.392f, 6.402f, 6.466f, 6.327f, 6.558f, 6.284f)
                curveTo(6.651f, 6.24f, 6.756f, 6.232f, 6.854f, 6.261f)
                close()
            }
        }.build()

        return _Whatshot!!
    }

@Suppress("ObjectPropertyName")
private var _Whatshot: ImageVector? = null
