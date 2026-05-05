package com.kickstarter.features.socialshare.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview @Composable private fun CopyLinkPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareCopyLink, null) } }
@Preview @Composable private fun InstagramPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareInstagram, null) } }
@Preview @Composable private fun XPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareX, null) } }
@Preview @Composable private fun FacebookPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareFacebook, null) } }
@Preview @Composable private fun WhatsAppPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareWhatsApp, null) } }
@Preview @Composable private fun MessagePreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareMessage, null) } }
@Preview @Composable private fun EmailPreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareEmail, null) } }
@Preview @Composable private fun MorePreview() { Box(Modifier.padding(12.dp)) { Image(SocialShareMore, null) } }

// ---------------------------------------------------------------------------
// Icons
// ---------------------------------------------------------------------------

val Vector91: ImageVector
    get() {
        if (_Vector91 != null) {
            return _Vector91!!
        }
        _Vector91 = ImageVector.Builder(
            name = "Vector91",
            defaultWidth = 393.dp,
            defaultHeight = 728.dp,
            viewportWidth = 393f,
            viewportHeight = 728f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF9BFF1D)),
                strokeLineWidth = 120f
            ) {
                moveTo(262.59f, 1498.33f)
                curveTo(500.69f, 1465.14f, 609.41f, 1189.46f, 211.07f, 991.26f)
                curveTo(586.58f, 1036.13f, 529.5f, 722.57f, 278.4f, 463.14f)
                curveTo(165.04f, 346.02f, -21.39f, 195.2f, 88.27f, 36.96f)
                curveTo(187.71f, -106.54f, 299.17f, 119.43f, 125.28f, 90.54f)
                curveTo(-21.18f, 66.2f, -151.93f, -139.73f, 10.72f, -446.05f)
            }
        }.build()

        return _Vector91!!
    }

@Suppress("ObjectPropertyName")
private var _Vector91: ImageVector? = null
@Preview
@Composable
private fun Vector91Preview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Vector91, contentDescription = null)
    }
}

val SocialShareMore: ImageVector
    get() {
        if (_SocialShareMore != null) return _SocialShareMore!!
        _SocialShareMore = ImageVector.Builder(
            name = "SocialShareMore",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            // Left dot
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 10f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
                reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
                reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
                close()
            }
            // Right dot
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 10f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
                reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
                reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
                close()
            }
            // Center dot
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 10f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
                reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
                reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
                close()
            }
        }.build()
        return _SocialShareMore!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareMore: ImageVector? = null

val SocialShareInstagram: ImageVector
    get() {
        if (_SocialShareInstagram != null) {
            return _SocialShareInstagram!!
        }
        _SocialShareInstagram = ImageVector.Builder(
            name = "SocialShareInstagram",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(24f)
                    verticalLineToRelative(24f)
                    horizontalLineToRelative(-24f)
                    close()
                }
            ) {
            }
            path(
                fill = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFDF497),
                        Color(0xFFFD5949),
                        Color(0xFFD6249F),
                        Color(0xFF285AE1)
                    ),
                    start = Offset(0f, 24f),
                    end = Offset(24f, 0f)
                ),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12f, 2.162f)
                curveTo(15.204f, 2.162f, 15.584f, 2.174f, 16.849f, 2.232f)
                curveTo(18.019f, 2.285f, 18.655f, 2.48f, 19.077f, 2.645f)
                curveTo(19.638f, 2.863f, 20.037f, 3.122f, 20.457f, 3.542f)
                curveTo(20.877f, 3.962f, 21.137f, 4.362f, 21.355f, 4.922f)
                curveTo(21.519f, 5.345f, 21.715f, 5.98f, 21.768f, 7.15f)
                curveTo(21.826f, 8.416f, 21.838f, 8.795f, 21.838f, 12f)
                curveTo(21.838f, 15.204f, 21.826f, 15.583f, 21.768f, 16.849f)
                curveTo(21.715f, 18.019f, 21.519f, 18.654f, 21.355f, 19.077f)
                curveTo(21.137f, 19.637f, 20.877f, 20.037f, 20.457f, 20.457f)
                curveTo(20.037f, 20.877f, 19.638f, 21.137f, 19.077f, 21.354f)
                curveTo(18.655f, 21.519f, 18.019f, 21.714f, 16.849f, 21.767f)
                curveTo(15.584f, 21.825f, 15.204f, 21.837f, 12f, 21.837f)
                curveTo(8.796f, 21.837f, 8.416f, 21.825f, 7.151f, 21.767f)
                curveTo(5.981f, 21.714f, 5.345f, 21.519f, 4.923f, 21.354f)
                curveTo(4.362f, 21.136f, 3.963f, 20.877f, 3.543f, 20.457f)
                curveTo(3.123f, 20.037f, 2.863f, 19.637f, 2.645f, 19.077f)
                curveTo(2.481f, 18.654f, 2.286f, 18.019f, 2.232f, 16.849f)
                curveTo(2.174f, 15.583f, 2.162f, 15.204f, 2.162f, 12f)
                curveTo(2.162f, 8.795f, 2.174f, 8.416f, 2.232f, 7.15f)
                curveTo(2.286f, 5.98f, 2.481f, 5.345f, 2.645f, 4.922f)
                curveTo(2.863f, 4.362f, 3.123f, 3.962f, 3.543f, 3.542f)
                curveTo(3.963f, 3.122f, 4.362f, 2.862f, 4.923f, 2.645f)
                curveTo(5.345f, 2.48f, 5.981f, 2.285f, 7.151f, 2.232f)
                curveTo(8.416f, 2.174f, 8.796f, 2.162f, 12f, 2.162f)
                close()
                moveTo(12f, 0f)
                curveTo(8.741f, 0f, 8.332f, 0.014f, 7.052f, 0.072f)
                curveTo(5.775f, 0.13f, 4.903f, 0.333f, 4.14f, 0.63f)
                curveTo(3.35f, 0.937f, 2.681f, 1.347f, 2.014f, 2.014f)
                curveTo(1.347f, 2.681f, 0.936f, 3.35f, 0.63f, 4.14f)
                curveTo(0.333f, 4.903f, 0.13f, 5.775f, 0.072f, 7.052f)
                curveTo(0.014f, 8.332f, 0f, 8.741f, 0f, 12f)
                curveTo(0f, 15.259f, 0.014f, 15.668f, 0.072f, 16.948f)
                curveTo(0.13f, 18.225f, 0.333f, 19.097f, 0.63f, 19.86f)
                curveTo(0.937f, 20.65f, 1.347f, 21.319f, 2.014f, 21.986f)
                curveTo(2.681f, 22.653f, 3.351f, 23.063f, 4.14f, 23.37f)
                curveTo(4.903f, 23.667f, 5.775f, 23.87f, 7.052f, 23.928f)
                curveTo(8.332f, 23.986f, 8.741f, 24f, 12f, 24f)
                curveTo(15.259f, 24f, 15.668f, 23.986f, 16.948f, 23.928f)
                curveTo(18.225f, 23.87f, 19.097f, 23.667f, 19.86f, 23.37f)
                curveTo(20.65f, 23.063f, 21.319f, 22.653f, 21.986f, 21.986f)
                curveTo(22.653f, 21.319f, 23.063f, 20.649f, 23.37f, 19.86f)
                curveTo(23.667f, 19.097f, 23.87f, 18.225f, 23.928f, 16.948f)
                curveTo(23.986f, 15.668f, 24f, 15.259f, 24f, 12f)
                curveTo(24f, 8.741f, 23.986f, 8.332f, 23.928f, 7.052f)
                curveTo(23.87f, 5.775f, 23.667f, 4.903f, 23.37f, 4.14f)
                curveTo(23.063f, 3.35f, 22.653f, 2.681f, 21.986f, 2.014f)
                curveTo(21.319f, 1.347f, 20.649f, 0.937f, 19.86f, 0.63f)
                curveTo(19.097f, 0.333f, 18.225f, 0.13f, 16.948f, 0.072f)
                curveTo(15.668f, 0.014f, 15.259f, 0f, 12f, 0f)
                close()
                moveTo(12f, 5.838f)
                curveTo(8.597f, 5.838f, 5.838f, 8.597f, 5.838f, 12f)
                curveTo(5.838f, 15.403f, 8.597f, 18.162f, 12f, 18.162f)
                curveTo(15.403f, 18.162f, 18.162f, 15.403f, 18.162f, 12f)
                curveTo(18.162f, 8.597f, 15.403f, 5.838f, 12f, 5.838f)
                close()
                moveTo(12f, 16f)
                curveTo(9.791f, 16f, 8f, 14.21f, 8f, 12f)
                curveTo(8f, 9.79f, 9.791f, 8f, 12f, 8f)
                curveTo(14.209f, 8f, 16f, 9.79f, 16f, 12f)
                curveTo(16f, 14.21f, 14.209f, 16f, 12f, 16f)
                close()
                moveTo(18.406f, 4.155f)
                curveTo(17.61f, 4.155f, 16.965f, 4.799f, 16.965f, 5.595f)
                curveTo(16.965f, 6.39f, 17.61f, 7.035f, 18.406f, 7.035f)
                curveTo(19.201f, 7.035f, 19.846f, 6.39f, 19.846f, 5.595f)
                curveTo(19.846f, 4.799f, 19.201f, 4.155f, 18.406f, 4.155f)
                close()
            }
        }.build()

        return _SocialShareInstagram!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareInstagram: ImageVector? = null

val SocialShareFacebook: ImageVector
    get() {
        if (_SocialShareFacebook != null) {
            return _SocialShareFacebook!!
        }
        _SocialShareFacebook = ImageVector.Builder(
            name = "ocialShareFacebook",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(24f)
                    verticalLineToRelative(24f)
                    horizontalLineToRelative(-24f)
                    close()
                }
            ) {
            }
            group(
                clipPathData = PathData {
                    moveTo(-4.8f, -4.8f)
                    horizontalLineTo(28.8f)
                    verticalLineTo(28.8f)
                    horizontalLineTo(-4.8f)
                    verticalLineTo(-4.8f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color(0xFF0866FF))) {
                    moveTo(24f, 12f)
                    curveTo(24f, 5.373f, 18.627f, -0f, 12f, -0f)
                    curveTo(5.373f, -0f, 0f, 5.373f, 0f, 12f)
                    curveTo(0f, 17.628f, 3.874f, 22.35f, 9.101f, 23.647f)
                    verticalLineTo(15.667f)
                    horizontalLineTo(6.627f)
                    verticalLineTo(12f)
                    horizontalLineTo(9.101f)
                    verticalLineTo(10.42f)
                    curveTo(9.101f, 6.335f, 10.95f, 4.442f, 14.959f, 4.442f)
                    curveTo(15.72f, 4.442f, 17.031f, 4.591f, 17.568f, 4.74f)
                    verticalLineTo(8.065f)
                    curveTo(17.285f, 8.035f, 16.793f, 8.02f, 16.182f, 8.02f)
                    curveTo(14.214f, 8.02f, 13.454f, 8.765f, 13.454f, 10.703f)
                    verticalLineTo(12f)
                    horizontalLineTo(17.373f)
                    lineTo(16.7f, 15.667f)
                    horizontalLineTo(13.454f)
                    verticalLineTo(23.912f)
                    curveTo(19.396f, 23.194f, 24f, 18.135f, 24f, 12f)
                    close()
                }
                path(fill = SolidColor(Color.White)) {
                    moveTo(16.7f, 15.667f)
                    lineTo(17.373f, 12f)
                    horizontalLineTo(13.454f)
                    verticalLineTo(10.703f)
                    curveTo(13.454f, 8.765f, 14.214f, 8.02f, 16.182f, 8.02f)
                    curveTo(16.793f, 8.02f, 17.285f, 8.035f, 17.568f, 8.065f)
                    verticalLineTo(4.74f)
                    curveTo(17.031f, 4.591f, 15.719f, 4.442f, 14.959f, 4.442f)
                    curveTo(10.949f, 4.442f, 9.101f, 6.335f, 9.101f, 10.42f)
                    verticalLineTo(12f)
                    horizontalLineTo(6.626f)
                    verticalLineTo(15.667f)
                    horizontalLineTo(9.101f)
                    verticalLineTo(23.647f)
                    curveTo(10.029f, 23.877f, 11f, 24f, 12f, 24f)
                    curveTo(12.492f, 24f, 12.977f, 23.97f, 13.454f, 23.912f)
                    verticalLineTo(15.667f)
                    horizontalLineTo(16.7f)
                    close()
                }
            }
        }.build()

        return _SocialShareFacebook!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareFacebook: ImageVector? = null

val SocialShareWhatsApp: ImageVector
    get() {
        if (_SocialShareWhatsApp != null) {
            return _SocialShareWhatsApp!!
        }
        _SocialShareWhatsApp = ImageVector.Builder(
            name = "SocialShareWhatsApp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF25D366)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(20.503f, 3.485f)
                curveTo(18.247f, 1.239f, 15.247f, 0.001f, 12.05f, 0f)
                curveTo(5.463f, 0f, 0.103f, 5.331f, 0.1f, 11.883f)
                curveTo(0.099f, 13.978f, 0.65f, 16.022f, 1.695f, 17.824f)
                lineTo(0f, 23.983f)
                lineTo(6.335f, 22.33f)
                curveTo(8.08f, 23.277f, 10.045f, 23.776f, 12.045f, 23.776f)
                horizontalLineTo(12.05f)
                curveTo(18.636f, 23.776f, 23.998f, 18.445f, 24f, 11.893f)
                curveTo(24.001f, 8.717f, 22.76f, 5.732f, 20.503f, 3.486f)
                verticalLineTo(3.485f)
                close()
                moveTo(12.05f, 21.769f)
                horizontalLineTo(12.046f)
                curveTo(10.264f, 21.769f, 8.516f, 21.292f, 6.991f, 20.393f)
                lineTo(6.628f, 20.178f)
                lineTo(2.869f, 21.159f)
                lineTo(3.872f, 17.514f)
                lineTo(3.636f, 17.14f)
                curveTo(2.642f, 15.567f, 2.117f, 13.75f, 2.118f, 11.884f)
                curveTo(2.12f, 6.438f, 6.576f, 2.007f, 12.054f, 2.007f)
                curveTo(14.707f, 2.008f, 17.201f, 3.037f, 19.076f, 4.904f)
                curveTo(20.952f, 6.771f, 21.984f, 9.253f, 21.983f, 11.892f)
                curveTo(21.98f, 17.338f, 17.525f, 21.769f, 12.05f, 21.769f)
                verticalLineTo(21.769f)
                close()
                moveTo(17.498f, 14.372f)
                curveTo(17.2f, 14.223f, 15.732f, 13.505f, 15.458f, 13.406f)
                curveTo(15.184f, 13.307f, 14.985f, 13.257f, 14.786f, 13.555f)
                curveTo(14.588f, 13.852f, 14.015f, 14.521f, 13.841f, 14.719f)
                curveTo(13.667f, 14.917f, 13.493f, 14.942f, 13.194f, 14.793f)
                curveTo(12.896f, 14.644f, 11.934f, 14.331f, 10.793f, 13.319f)
                curveTo(9.905f, 12.532f, 9.306f, 11.56f, 9.132f, 11.262f)
                curveTo(8.957f, 10.965f, 9.113f, 10.804f, 9.262f, 10.657f)
                curveTo(9.396f, 10.523f, 9.561f, 10.31f, 9.71f, 10.137f)
                curveTo(9.86f, 9.963f, 9.909f, 9.839f, 10.009f, 9.641f)
                curveTo(10.109f, 9.443f, 10.059f, 9.27f, 9.984f, 9.121f)
                curveTo(9.91f, 8.972f, 9.313f, 7.511f, 9.063f, 6.916f)
                curveTo(8.821f, 6.338f, 8.575f, 6.416f, 8.392f, 6.406f)
                curveTo(8.218f, 6.398f, 8.019f, 6.396f, 7.82f, 6.396f)
                curveTo(7.62f, 6.396f, 7.297f, 6.47f, 7.023f, 6.768f)
                curveTo(6.749f, 7.065f, 5.978f, 7.784f, 5.978f, 9.245f)
                curveTo(5.978f, 10.706f, 7.048f, 12.118f, 7.197f, 12.317f)
                curveTo(7.347f, 12.515f, 9.303f, 15.514f, 12.298f, 16.801f)
                curveTo(13.01f, 17.107f, 13.566f, 17.289f, 14f, 17.426f)
                curveTo(14.715f, 17.652f, 15.366f, 17.621f, 15.881f, 17.544f)
                curveTo(16.454f, 17.459f, 17.647f, 16.826f, 17.896f, 16.132f)
                curveTo(18.145f, 15.439f, 18.145f, 14.844f, 18.07f, 14.72f)
                curveTo(17.996f, 14.596f, 17.796f, 14.522f, 17.498f, 14.373f)
                lineTo(17.498f, 14.372f)
                close()
            }
        }.build()

        return _SocialShareWhatsApp!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareWhatsApp: ImageVector? = null

val SocialShareX: ImageVector
    get() {
        if (_SocialShareX != null) {
            return _SocialShareX!!
        }
        _SocialShareX = ImageVector.Builder(
            name = "SocialShareX",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(24f)
                    verticalLineToRelative(24f)
                    horizontalLineToRelative(-24f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color(0xFF3C3C3C))) {
                    moveTo(14.283f, 10.157f)
                    lineTo(23.218f, 0f)
                    horizontalLineTo(21.101f)
                    lineTo(13.343f, 8.819f)
                    lineTo(7.147f, 0f)
                    horizontalLineTo(0f)
                    lineTo(9.37f, 13.336f)
                    lineTo(0f, 23.988f)
                    horizontalLineTo(2.117f)
                    lineTo(10.31f, 14.674f)
                    lineTo(16.853f, 23.988f)
                    horizontalLineTo(24f)
                    lineTo(14.283f, 10.157f)
                    horizontalLineTo(14.283f)
                    close()
                    moveTo(11.383f, 13.454f)
                    lineTo(10.434f, 12.126f)
                    lineTo(2.88f, 1.559f)
                    horizontalLineTo(6.132f)
                    lineTo(12.228f, 10.087f)
                    lineTo(13.178f, 11.415f)
                    lineTo(21.102f, 22.5f)
                    horizontalLineTo(17.85f)
                    lineTo(11.383f, 13.454f)
                    verticalLineTo(13.454f)
                    close()
                }
            }
        }.build()

        return _SocialShareX!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareX: ImageVector? = null

val SocialShareEmail: ImageVector
    get() {
        if (_SocialShareEmail != null) {
            return _SocialShareEmail!!
        }
        _SocialShareEmail = ImageVector.Builder(
            name = "SocialShareEmail",
            defaultWidth = 20.dp,
            defaultHeight = 14.dp,
            viewportWidth = 20f,
            viewportHeight = 14f
        ).apply {
            path(fill = SolidColor(Color(0xFF3C3C3C))) {
                moveTo(17f, 0f)
                curveTo(18.657f, 0f, 20f, 1.343f, 20f, 3f)
                verticalLineTo(11f)
                curveTo(20f, 12.657f, 18.657f, 14f, 17f, 14f)
                horizontalLineTo(3f)
                curveTo(1.343f, 14f, 0f, 12.657f, 0f, 11f)
                verticalLineTo(3f)
                curveTo(0f, 1.343f, 1.343f, 0f, 3f, 0f)
                horizontalLineTo(17f)
                close()
                moveTo(10.636f, 9.771f)
                curveTo(10.267f, 10.076f, 9.734f, 10.076f, 9.364f, 9.771f)
                lineTo(2f, 3.706f)
                verticalLineTo(11f)
                curveTo(2f, 11.552f, 2.448f, 12f, 3f, 12f)
                horizontalLineTo(17f)
                curveTo(17.552f, 12f, 18f, 11.552f, 18f, 11f)
                verticalLineTo(3.706f)
                lineTo(10.636f, 9.771f)
                close()
                moveTo(10f, 7.704f)
                lineTo(16.928f, 2f)
                horizontalLineTo(3.072f)
                lineTo(10f, 7.704f)
                close()
            }
        }.build()

        return _SocialShareEmail!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareEmail: ImageVector? = null

val SocialShareMessage: ImageVector
    get() {
        if (_SocialShareMessage != null) {
            return _SocialShareMessage!!
        }
        _SocialShareMessage = ImageVector.Builder(
            name = "SocialShareMessage",
            defaultWidth = 27.dp,
            defaultHeight = 25.dp,
            viewportWidth = 27f,
            viewportHeight = 25f
        ).apply {
            path(fill = SolidColor(Color(0xFF3C3C3C))) {
                moveTo(5.086f, 24.551f)
                curveTo(4.68f, 24.551f, 4.371f, 24.453f, 4.16f, 24.258f)
                curveTo(3.949f, 24.07f, 3.84f, 23.832f, 3.832f, 23.543f)
                curveTo(3.824f, 23.262f, 3.922f, 22.984f, 4.125f, 22.711f)
                curveTo(4.305f, 22.484f, 4.52f, 22.191f, 4.77f, 21.832f)
                curveTo(5.02f, 21.48f, 5.27f, 21.109f, 5.52f, 20.719f)
                curveTo(5.77f, 20.328f, 5.984f, 19.961f, 6.164f, 19.617f)
                curveTo(4.93f, 19.047f, 3.848f, 18.305f, 2.918f, 17.391f)
                curveTo(1.996f, 16.477f, 1.277f, 15.445f, 0.762f, 14.297f)
                curveTo(0.254f, 13.148f, 0f, 11.934f, 0f, 10.652f)
                curveTo(0f, 9.176f, 0.34f, 7.793f, 1.02f, 6.504f)
                curveTo(1.707f, 5.215f, 2.656f, 4.086f, 3.867f, 3.117f)
                curveTo(5.086f, 2.141f, 6.5f, 1.379f, 8.109f, 0.832f)
                curveTo(9.719f, 0.277f, 11.445f, 0f, 13.289f, 0f)
                curveTo(15.133f, 0f, 16.859f, 0.277f, 18.469f, 0.832f)
                curveTo(20.078f, 1.379f, 21.488f, 2.141f, 22.699f, 3.117f)
                curveTo(23.918f, 4.086f, 24.867f, 5.215f, 25.547f, 6.504f)
                curveTo(26.234f, 7.793f, 26.578f, 9.176f, 26.578f, 10.652f)
                curveTo(26.578f, 11.879f, 26.344f, 13.039f, 25.875f, 14.133f)
                curveTo(25.414f, 15.219f, 24.754f, 16.207f, 23.895f, 17.098f)
                curveTo(23.043f, 17.988f, 22.023f, 18.754f, 20.836f, 19.395f)
                curveTo(19.656f, 20.027f, 18.344f, 20.508f, 16.898f, 20.836f)
                curveTo(15.453f, 21.164f, 13.914f, 21.305f, 12.281f, 21.258f)
                curveTo(11.492f, 21.828f, 10.641f, 22.363f, 9.727f, 22.863f)
                curveTo(8.813f, 23.363f, 7.941f, 23.77f, 7.113f, 24.082f)
                curveTo(6.285f, 24.395f, 5.609f, 24.551f, 5.086f, 24.551f)
                close()
                moveTo(6.316f, 22.605f)
                curveTo(6.668f, 22.457f, 7.113f, 22.23f, 7.652f, 21.926f)
                curveTo(8.191f, 21.621f, 8.75f, 21.281f, 9.328f, 20.906f)
                curveTo(9.906f, 20.539f, 10.434f, 20.184f, 10.91f, 19.84f)
                curveTo(11.129f, 19.668f, 11.34f, 19.543f, 11.543f, 19.465f)
                curveTo(11.754f, 19.379f, 11.981f, 19.336f, 12.223f, 19.336f)
                curveTo(12.457f, 19.344f, 12.664f, 19.352f, 12.844f, 19.359f)
                curveTo(13.023f, 19.359f, 13.172f, 19.359f, 13.289f, 19.359f)
                curveTo(14.867f, 19.359f, 16.344f, 19.133f, 17.719f, 18.68f)
                curveTo(19.094f, 18.227f, 20.297f, 17.602f, 21.328f, 16.805f)
                curveTo(22.367f, 16.008f, 23.18f, 15.082f, 23.766f, 14.027f)
                curveTo(24.352f, 12.973f, 24.645f, 11.848f, 24.645f, 10.652f)
                curveTo(24.645f, 9.449f, 24.352f, 8.324f, 23.766f, 7.277f)
                curveTo(23.18f, 6.223f, 22.367f, 5.297f, 21.328f, 4.5f)
                curveTo(20.297f, 3.695f, 19.094f, 3.066f, 17.719f, 2.613f)
                curveTo(16.344f, 2.16f, 14.867f, 1.934f, 13.289f, 1.934f)
                curveTo(11.711f, 1.934f, 10.234f, 2.16f, 8.859f, 2.613f)
                curveTo(7.484f, 3.066f, 6.277f, 3.695f, 5.238f, 4.5f)
                curveTo(4.207f, 5.297f, 3.398f, 6.223f, 2.813f, 7.277f)
                curveTo(2.234f, 8.324f, 1.945f, 9.449f, 1.945f, 10.652f)
                curveTo(1.945f, 11.691f, 2.172f, 12.68f, 2.625f, 13.617f)
                curveTo(3.078f, 14.555f, 3.734f, 15.414f, 4.594f, 16.195f)
                curveTo(5.461f, 16.969f, 6.508f, 17.641f, 7.734f, 18.211f)
                curveTo(8.109f, 18.391f, 8.328f, 18.609f, 8.391f, 18.867f)
                curveTo(8.453f, 19.125f, 8.402f, 19.406f, 8.238f, 19.711f)
                curveTo(8.035f, 20.086f, 7.734f, 20.539f, 7.336f, 21.07f)
                curveTo(6.938f, 21.609f, 6.574f, 22.082f, 6.246f, 22.488f)
                curveTo(6.215f, 22.527f, 6.207f, 22.559f, 6.223f, 22.582f)
                curveTo(6.238f, 22.613f, 6.27f, 22.621f, 6.316f, 22.605f)
                close()
            }
        }.build()

        return _SocialShareMessage!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareMessage: ImageVector? = null

val SocialShareCopyLink: ImageVector
    get() {
        if (_SocialShareCopyLink != null) {
            return _SocialShareCopyLink!!
        }
        _SocialShareCopyLink = ImageVector.Builder(
            name = "Link",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF3C3C3C)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(8.465f, 9.877f)
                curveTo(8.075f, 9.486f, 7.441f, 9.486f, 7.051f, 9.877f)
                lineTo(4.929f, 11.999f)
                curveTo(2.976f, 13.952f, 2.976f, 17.118f, 4.929f, 19.07f)
                curveTo(6.881f, 21.023f, 10.047f, 21.023f, 12f, 19.07f)
                lineTo(14.122f, 16.948f)
                curveTo(14.512f, 16.557f, 14.512f, 15.924f, 14.122f, 15.534f)
                curveTo(13.731f, 15.143f, 13.098f, 15.143f, 12.708f, 15.534f)
                lineTo(10.585f, 17.656f)
                curveTo(9.414f, 18.827f, 7.514f, 18.827f, 6.343f, 17.656f)
                curveTo(5.171f, 16.484f, 5.171f, 14.585f, 6.343f, 13.413f)
                lineTo(8.465f, 11.291f)
                curveTo(8.856f, 10.901f, 8.856f, 10.267f, 8.465f, 9.877f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF3C3C3C)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(15.535f, 14.121f)
                curveTo(15.925f, 14.512f, 16.558f, 14.512f, 16.949f, 14.121f)
                lineTo(19.071f, 11.999f)
                curveTo(21.024f, 10.046f, 21.024f, 6.881f, 19.071f, 4.928f)
                curveTo(17.119f, 2.975f, 13.953f, 2.975f, 12f, 4.928f)
                lineTo(9.878f, 7.05f)
                curveTo(9.487f, 7.441f, 9.487f, 8.074f, 9.878f, 8.464f)
                curveTo(10.268f, 8.855f, 10.902f, 8.855f, 11.292f, 8.464f)
                lineTo(13.414f, 6.342f)
                curveTo(14.586f, 5.171f, 16.486f, 5.171f, 17.657f, 6.342f)
                curveTo(18.829f, 7.514f, 18.829f, 9.413f, 17.657f, 10.585f)
                lineTo(15.535f, 12.707f)
                curveTo(15.144f, 13.098f, 15.144f, 13.731f, 15.535f, 14.121f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3C3C3C))) {
                moveTo(14.828f, 7.756f)
                curveTo(15.219f, 7.365f, 15.852f, 7.365f, 16.242f, 7.756f)
                curveTo(16.633f, 8.146f, 16.633f, 8.78f, 16.242f, 9.17f)
                lineTo(9.171f, 16.241f)
                curveTo(8.781f, 16.632f, 8.147f, 16.632f, 7.757f, 16.241f)
                curveTo(7.366f, 15.851f, 7.366f, 15.217f, 7.757f, 14.827f)
                lineTo(14.828f, 7.756f)
                close()
            }
        }.build()

        return _SocialShareCopyLink!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareCopyLink: ImageVector? = null
