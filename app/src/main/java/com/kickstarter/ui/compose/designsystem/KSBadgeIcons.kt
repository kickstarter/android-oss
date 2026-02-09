package com.kickstarter.ui.compose.designsystem

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

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
