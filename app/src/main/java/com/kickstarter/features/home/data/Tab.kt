package com.kickstarter.features.home.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

sealed class Tab(val route: String, val icon: ImageVector) {
    data object Home : Tab("home", home)
    data object Search : Tab("search", search)
    data object Profile : Tab("profile", human)
}

// Hardcoded for now, could try to potentially load configuration from backend (SDUI approach or easiest version try remote config configuration)
val tabs = listOf(Tab.Home, Tab.Search, Tab.Profile)

val home: ImageVector
    get() {
        if (_home != null) return _home!!

        _home = ImageVector.Builder(
            name = "home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF454545)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(4.00005f, 9f)
                horizontalLineTo(20f)
                verticalLineTo(19f)
                curveTo(20f, 20.6569f, 18.6569f, 22f, 17f, 22f)
                horizontalLineTo(7.00005f)
                curveTo(5.34319f, 22f, 4.00005f, 20.6569f, 4.00005f, 19f)
                verticalLineTo(9f)
                close()
                moveTo(6.00005f, 11f)
                verticalLineTo(19f)
                curveTo(6.00005f, 19.5523f, 6.44776f, 20f, 7.00005f, 20f)
                horizontalLineTo(17f)
                curveTo(17.5523f, 20f, 18f, 19.5523f, 18f, 19f)
                verticalLineTo(11f)
                horizontalLineTo(6.00005f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF454545)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(11.3861f, 2.21065f)
                curveTo(11.7472f, 1.92978f, 12.2529f, 1.92978f, 12.614f, 2.21065f)
                lineTo(21.614f, 9.21065f)
                curveTo(22.0499f, 9.54972f, 22.1285f, 10.178f, 21.7894f, 10.6139f)
                curveTo(21.4503f, 11.0499f, 20.8221f, 11.1284f, 20.3861f, 10.7894f)
                lineTo(12f, 4.26686f)
                lineTo(3.61399f, 10.7894f)
                curveTo(3.17804f, 11.1284f, 2.54976f, 11.0499f, 2.21069f, 10.6139f)
                curveTo(1.87162f, 10.178f, 1.95016f, 9.54972f, 2.38611f, 9.21065f)
                lineTo(11.3861f, 2.21065f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF454545))
            ) {
                moveTo(2.00005f, 10f)
                curveTo(2.00005f, 9.44772f, 2.44776f, 9f, 3.00005f, 9f)
                horizontalLineTo(21f)
                curveTo(21.5523f, 9f, 22f, 9.44772f, 22f, 10f)
                curveTo(22f, 10.5523f, 21.5523f, 11f, 21f, 11f)
                horizontalLineTo(3.00005f)
                curveTo(2.44776f, 11f, 2.00005f, 10.5523f, 2.00005f, 10f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF454545)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(16f, 22f)
                horizontalLineTo(8.00005f)
                verticalLineTo(15f)
                curveTo(8.00005f, 13.3431f, 9.34319f, 12f, 11f, 12f)
                horizontalLineTo(13f)
                curveTo(14.6569f, 12f, 16f, 13.3431f, 16f, 15f)
                verticalLineTo(22f)
                close()
                moveTo(11f, 14f)
                curveTo(10.4478f, 14f, 10f, 14.4477f, 10f, 15f)
                verticalLineTo(20f)
                horizontalLineTo(14f)
                verticalLineTo(15f)
                curveTo(14f, 14.4477f, 13.5523f, 14f, 13f, 14f)
                horizontalLineTo(11f)
                close()
            }
        }.build()

        return _home!!
    }

private var _home: ImageVector? = null

val human: ImageVector
    get() {
        if (_human != null) return _human!!

        _human = ImageVector.Builder(
            name = "human",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF454545))
            ) {
                moveTo(12.0004f, 4f)
                curveTo(14.2093f, 4.0002f, 16.0004f, 5.79099f, 16.0004f, 8f)
                curveTo(16.0004f, 9.36305f, 15.3169f, 10.5649f, 14.2758f, 11.2871f)
                curveTo(17.4541f, 12.0996f, 20.097f, 14.5775f, 20.097f, 18.001f)
                curveTo(20.097f, 18.1127f, 20.0943f, 18.2242f, 20.0883f, 18.335f)
                curveTo(19.9932f, 20.0902f, 18.4626f, 21f, 17.1927f, 21f)
                horizontalLineTo(6.80701f)
                curveTo(5.53716f, 20.9999f, 4.00654f, 20.0901f, 3.9115f, 18.335f)
                curveTo(3.9055f, 18.2242f, 3.90271f, 18.1127f, 3.90271f, 18.001f)
                curveTo(3.90277f, 14.5779f, 6.54517f, 12.0999f, 9.72302f, 11.2871f)
                curveTo(8.68235f, 10.5649f, 8.00037f, 9.36269f, 8.00037f, 8f)
                curveTo(8.00037f, 5.79086f, 9.79123f, 4f, 12.0004f, 4f)
                close()
                moveTo(12.0004f, 13f)
                curveTo(8.63298f, 13f, 5.90279f, 15.239f, 5.90271f, 18.001f)
                curveTo(5.90271f, 18.0765f, 5.90453f, 18.1518f, 5.90857f, 18.2266f)
                curveTo(5.93343f, 18.6857f, 6.34725f, 18.9999f, 6.80701f, 19f)
                horizontalLineTo(17.1927f)
                curveTo(17.6526f, 19f, 18.0663f, 18.6857f, 18.0912f, 18.2266f)
                curveTo(18.0952f, 18.1518f, 18.097f, 18.0765f, 18.097f, 18.001f)
                curveTo(18.097f, 15.2391f, 15.3676f, 13.0002f, 12.0004f, 13f)
                close()
                moveTo(12.0004f, 6f)
                curveTo(10.8958f, 6f, 10.0004f, 6.89543f, 10.0004f, 8f)
                curveTo(10.0004f, 9.10457f, 10.8958f, 10f, 12.0004f, 10f)
                curveTo(13.1048f, 9.9998f, 14.0004f, 9.10444f, 14.0004f, 8f)
                curveTo(14.0004f, 6.89556f, 13.1048f, 6.0002f, 12.0004f, 6f)
                close()
            }
        }.build()

        return _human!!
    }

private var _human: ImageVector? = null

val search: ImageVector
    get() {
        if (_search != null) return _search!!

        _search = ImageVector.Builder(
            name = "search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF454545))
            ) {
                moveTo(10.5f, 3f)
                curveTo(14.6421f, 3f, 18f, 6.35786f, 18f, 10.5f)
                curveTo(18f, 12.3231f, 17.3481f, 13.9932f, 16.2666f, 15.293f)
                lineTo(20.6699f, 19.2559f)
                curveTo(21.0804f, 19.6253f, 21.1136f, 20.2575f, 20.7441f, 20.668f)
                curveTo(20.3747f, 21.0785f, 19.7425f, 21.1116f, 19.332f, 20.7422f)
                lineTo(14.7871f, 16.6514f)
                curveTo(13.5717f, 17.5f, 12.0948f, 18f, 10.5f, 18f)
                curveTo(6.35786f, 18f, 3f, 14.6421f, 3f, 10.5f)
                curveTo(3f, 6.35786f, 6.35786f, 3f, 10.5f, 3f)
                close()
                moveTo(10.5f, 5f)
                curveTo(7.46243f, 5f, 5f, 7.46243f, 5f, 10.5f)
                curveTo(5f, 13.5376f, 7.46243f, 16f, 10.5f, 16f)
                curveTo(13.5376f, 16f, 16f, 13.5376f, 16f, 10.5f)
                curveTo(16f, 7.46243f, 13.5376f, 5f, 10.5f, 5f)
                close()
            }
        }.build()

        return _search!!
    }

private var _search: ImageVector? = null
