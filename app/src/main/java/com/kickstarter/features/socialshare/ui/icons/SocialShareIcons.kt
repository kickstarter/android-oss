package com.kickstarter.features.socialshare.ui.icons

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

val SocialShareCopyLink: ImageVector
    get() {
        if (_SocialShareCopyLink != null) return _SocialShareCopyLink!!
        _SocialShareCopyLink = ImageVector.Builder(
            name = "SocialShareCopyLink",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            // Left half of the chain link
            path(fill = SolidColor(Color.Black)) {
                moveTo(3.9f, 12f)
                curveToRelative(0f, -1.71f, 1.39f, -3.1f, 3.1f, -3.1f)
                horizontalLineToRelative(4f)
                verticalLineTo(7f)
                horizontalLineTo(7f)
                curveToRelative(-2.76f, 0f, -5f, 2.24f, -5f, 5f)
                reflectiveCurveToRelative(2.24f, 5f, 5f, 5f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(-1.9f)
                horizontalLineTo(7f)
                curveToRelative(-1.71f, 0f, -3.1f, -1.39f, -3.1f, -3.1f)
                close()
            }
            // Middle bar
            path(fill = SolidColor(Color.Black)) {
                moveTo(8f, 13f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(-2f)
                horizontalLineTo(8f)
                verticalLineToRelative(2f)
                close()
            }
            // Right half of the chain link
            path(fill = SolidColor(Color.Black)) {
                moveTo(17f, 7f)
                horizontalLineToRelative(-4f)
                verticalLineToRelative(1.9f)
                horizontalLineToRelative(4f)
                curveToRelative(1.71f, 0f, 3.1f, 1.39f, 3.1f, 3.1f)
                reflectiveCurveToRelative(-1.39f, 3.1f, -3.1f, 3.1f)
                horizontalLineToRelative(-4f)
                verticalLineTo(17f)
                horizontalLineToRelative(4f)
                curveToRelative(2.76f, 0f, 5f, -2.24f, 5f, -5f)
                reflectiveCurveToRelative(-2.24f, -5f, -5f, -5f)
                close()
            }
        }.build()
        return _SocialShareCopyLink!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareCopyLink: ImageVector? = null

// ---------------------------------------------------------------------------

val SocialShareInstagram: ImageVector
    get() {
        if (_SocialShareInstagram != null) return _SocialShareInstagram!!
        _SocialShareInstagram = ImageVector.Builder(
            name = "SocialShareInstagram",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            // Inner rounded-rect highlight
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2.163f)
                curveToRelative(3.204f, 0f, 3.584f, 0.012f, 4.85f, 0.07f)
                curveToRelative(3.252f, 0.148f, 4.771f, 1.691f, 4.919f, 4.919f)
                curveToRelative(0.058f, 1.265f, 0.069f, 1.645f, 0.069f, 4.849f)
                curveToRelative(0f, 3.205f, -0.012f, 3.584f, -0.069f, 4.849f)
                curveToRelative(-0.149f, 3.225f, -1.664f, 4.771f, -4.919f, 4.919f)
                curveToRelative(-1.266f, 0.058f, -1.644f, 0.07f, -4.85f, 0.07f)
                curveToRelative(-3.204f, 0f, -3.584f, -0.012f, -4.849f, -0.07f)
                curveToRelative(-3.26f, -0.149f, -4.771f, -1.699f, -4.919f, -4.92f)
                curveToRelative(-0.058f, -1.265f, -0.07f, -1.644f, -0.07f, -4.849f)
                curveToRelative(0f, -3.204f, 0.013f, -3.583f, 0.07f, -4.849f)
                curveToRelative(0.149f, -3.227f, 1.664f, -4.771f, 4.919f, -4.919f)
                curveToRelative(1.266f, -0.057f, 1.645f, -0.069f, 4.849f, -0.069f)
                close()
            }
            // Outer rounded-rect border
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 0f)
                curveTo(8.741f, 0f, 8.333f, 0.014f, 7.053f, 0.072f)
                curveTo(2.695f, 0.272f, 0.273f, 2.69f, 0.073f, 7.052f)
                curveTo(0.014f, 8.333f, 0f, 8.741f, 0f, 12f)
                curveToRelative(0f, 3.259f, 0.014f, 3.668f, 0.072f, 4.948f)
                curveToRelative(0.2f, 4.358f, 2.618f, 6.78f, 6.98f, 6.98f)
                curveTo(8.333f, 23.986f, 8.741f, 24f, 12f, 24f)
                curveToRelative(3.259f, 0f, 3.668f, -0.014f, 4.948f, -0.072f)
                curveToRelative(4.354f, -0.2f, 6.782f, -2.618f, 6.979f, -6.98f)
                curveToRelative(0.059f, -1.28f, 0.073f, -1.689f, 0.073f, -4.948f)
                curveToRelative(0f, -3.259f, -0.014f, -3.667f, -0.072f, -4.947f)
                curveToRelative(-0.196f, -4.354f, -2.617f, -6.78f, -6.979f, -6.98f)
                curveTo(15.668f, 0.014f, 15.259f, 0f, 12f, 0f)
                close()
            }
            // Lens circle
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 5.838f)
                arcToRelative(6.162f, 6.162f, 0f, false, false, 0f, 12.324f)
                arcToRelative(6.162f, 6.162f, 0f, false, false, 0f, -12.324f)
                close()
            }
            // Lens fill circle
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 16f)
                arcToRelative(4f, 4f, 0f, true, true, 0f, -8f)
                arcToRelative(4f, 4f, 0f, false, true, 0f, 8f)
                close()
            }
            // Top-right dot
            path(fill = SolidColor(Color.Black)) {
                moveTo(18.406f, 4.155f)
                arcToRelative(1.44f, 1.44f, 0f, true, false, 0f, 2.881f)
                arcToRelative(1.44f, 1.44f, 0f, false, false, 0f, -2.881f)
                close()
            }
        }.build()
        return _SocialShareInstagram!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareInstagram: ImageVector? = null

// ---------------------------------------------------------------------------

val SocialShareX: ImageVector
    get() {
        if (_SocialShareX != null) return _SocialShareX!!
        _SocialShareX = ImageVector.Builder(
            name = "SocialShareX",
            defaultWidth = 20.dp, defaultHeight = 16.dp,
            viewportWidth = 20f, viewportHeight = 16f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(0f, 14.185f)
                curveTo(0.321f, 14.223f, 0.648f, 14.242f, 0.979f, 14.242f)
                curveTo(2.901f, 14.242f, 4.67f, 13.596f, 6.075f, 12.513f)
                curveTo(4.279f, 12.48f, 2.764f, 11.312f, 2.242f, 9.707f)
                curveTo(2.492f, 9.755f, 2.749f, 9.78f, 3.014f, 9.78f)
                curveTo(3.388f, 9.78f, 3.75f, 9.731f, 4.095f, 9.638f)
                curveTo(2.218f, 9.267f, 0.803f, 7.635f, 0.803f, 5.678f)
                curveTo(0.803f, 5.661f, 0.803f, 5.644f, 0.804f, 5.627f)
                curveTo(1.357f, 5.929f, 1.99f, 6.111f, 2.662f, 6.132f)
                curveTo(1.561f, 5.408f, 0.837f, 4.171f, 0.837f, 2.77f)
                curveTo(0.837f, 2.03f, 1.039f, 1.336f, 1.392f, 0.739f)
                curveTo(3.416f, 3.183f, 6.44f, 4.791f, 9.85f, 4.96f)
                curveTo(9.78f, 4.664f, 9.744f, 4.356f, 9.744f, 4.039f)
                curveTo(9.744f, 1.808f, 11.581f, 0f, 13.847f, 0f)
                curveTo(15.027f, 0f, 16.093f, 0.491f, 16.842f, 1.276f)
                curveTo(17.777f, 1.094f, 18.655f, 0.758f, 19.447f, 0.295f)
                curveTo(19.141f, 1.239f, 18.491f, 2.03f, 17.643f, 2.53f)
                curveTo(18.473f, 2.432f, 19.264f, 2.215f, 20f, 1.894f)
                curveTo(19.45f, 2.704f, 18.754f, 3.415f, 17.953f, 3.985f)
                curveTo(17.96f, 4.158f, 17.964f, 4.332f, 17.964f, 4.508f)
                curveTo(17.964f, 9.845f, 13.837f, 16f, 6.29f, 16f)
                curveTo(3.973f, 16f, 1.816f, 15.331f, 0f, 14.185f)
                close()
            }
        }.build()
        return _SocialShareX!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareX: ImageVector? = null

// ---------------------------------------------------------------------------

val SocialShareFacebook: ImageVector
    get() {
        if (_SocialShareFacebook != null) return _SocialShareFacebook!!
        _SocialShareFacebook = ImageVector.Builder(
            name = "SocialShareFacebook",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 1024f, viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(1024f, 515f)
                curveToRelative(0f, -282.77f, -229.23f, -512f, -512f, -512f)
                reflectiveCurveToRelative(-512f, 229.23f, -512f, 512f)
                curveToRelative(0f, 255.55f, 187.23f, 467.37f, 432f, 505.78f)
                verticalLineToRelative(-357.78f)
                horizontalLineToRelative(-130f)
                verticalLineToRelative(-148f)
                horizontalLineToRelative(130f)
                verticalLineToRelative(-112.8f)
                curveToRelative(0f, -128.32f, 76.438f, -199.2f, 193.39f, -199.2f)
                curveToRelative(56.017f, 0f, 114.61f, 10f, 114.61f, 10f)
                verticalLineToRelative(126f)
                horizontalLineToRelative(-64.562f)
                curveToRelative(-63.603f, 0f, -83.438f, 39.467f, -83.438f, 79.957f)
                verticalLineToRelative(96.043f)
                horizontalLineToRelative(142f)
                lineToRelative(-22.7f, 148f)
                horizontalLineToRelative(-119.3f)
                verticalLineToRelative(357.78f)
                curveToRelative(244.77f, -38.41f, 432f, -250.23f, 432f, -505.78f)
                close()
            }
        }.build()
        return _SocialShareFacebook!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareFacebook: ImageVector? = null

// ---------------------------------------------------------------------------

val SocialShareWhatsApp: ImageVector
    get() {
        if (_SocialShareWhatsApp != null) return _SocialShareWhatsApp!!
        _SocialShareWhatsApp = ImageVector.Builder(
            name = "SocialShareWhatsApp",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            // Speech-bubble phone body
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.472f, 14.382f)
                curveToRelative(-0.297f, -0.149f, -1.758f, -0.867f, -2.03f, -0.967f)
                curveToRelative(-0.273f, -0.099f, -0.471f, -0.148f, -0.67f, 0.149f)
                curveToRelative(-0.197f, 0.297f, -0.767f, 0.966f, -0.94f, 1.164f)
                curveToRelative(-0.173f, 0.199f, -0.347f, 0.223f, -0.644f, 0.074f)
                curveToRelative(-0.297f, -0.149f, -1.255f, -0.462f, -2.39f, -1.475f)
                curveToRelative(-0.883f, -0.788f, -1.48f, -1.761f, -1.653f, -2.059f)
                curveToRelative(-0.173f, -0.297f, -0.018f, -0.458f, 0.13f, -0.606f)
                curveToRelative(0.134f, -0.133f, 0.298f, -0.347f, 0.446f, -0.521f)
                curveToRelative(0.149f, -0.174f, 0.198f, -0.298f, 0.298f, -0.497f)
                curveToRelative(0.099f, -0.198f, 0.05f, -0.372f, -0.025f, -0.521f)
                curveToRelative(-0.075f, -0.148f, -0.669f, -1.611f, -0.916f, -2.206f)
                curveToRelative(-0.242f, -0.579f, -0.487f, -0.501f, -0.669f, -0.51f)
                curveToRelative(-0.173f, -0.008f, -0.371f, -0.01f, -0.57f, -0.01f)
                curveToRelative(-0.198f, 0f, -0.52f, 0.074f, -0.792f, 0.372f)
                curveToRelative(-0.272f, 0.297f, -1.04f, 1.016f, -1.04f, 2.479f)
                curveToRelative(0f, 1.462f, 1.065f, 2.875f, 1.213f, 3.074f)
                curveToRelative(0.149f, 0.198f, 2.096f, 3.2f, 5.077f, 4.487f)
                curveToRelative(0.709f, 0.306f, 1.263f, 0.489f, 1.694f, 0.626f)
                curveToRelative(0.712f, 0.226f, 1.36f, 0.194f, 1.872f, 0.118f)
                curveToRelative(0.571f, -0.085f, 1.758f, -0.719f, 2.006f, -1.413f)
                curveToRelative(0.248f, -0.694f, 0.248f, -1.289f, 0.173f, -1.413f)
                curveToRelative(-0.074f, -0.124f, -0.272f, -0.198f, -0.57f, -0.347f)
                close()
            }
            // Inner circle / chat tail
            path(fill = SolidColor(Color.Black)) {
                moveTo(12.05f, 21.785f)
                horizontalLineToRelative(-0.01f)
                curveToRelative(-1.784f, -0.001f, -3.535f, -0.48f, -5.065f, -1.388f)
                lineTo(6.6f, 20.15f)
                lineToRelative(-3.637f, 0.954f)
                lineToRelative(0.97f, -3.546f)
                lineToRelative(-0.346f, -0.559f)
                arcTo(9.886f, 9.886f, 0f, false, true, 2.085f, 12.05f)
                curveTo(2.085f, 6.555f, 6.554f, 2.085f, 12.05f, 2.085f)
                curveToRelative(2.664f, 0.001f, 5.165f, 1.04f, 7.049f, 2.926f)
                arcToRelative(9.928f, 9.928f, 0f, false, true, 2.921f, 7.051f)
                curveToRelative(-0.002f, 5.496f, -4.471f, 9.723f, -9.97f, 9.723f)
                close()
            }
            // Outer border / tail
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.52f, 3.449f)
                curveTo(18.24f, 1.245f, 15.24f, 0.001f, 12.05f, 0f)
                curveTo(5.495f, 0f, 0.16f, 5.335f, 0.157f, 11.892f)
                curveToRelative(-0.001f, 2.096f, 0.547f, 4.142f, 1.588f, 5.945f)
                lineTo(0.057f, 24f)
                lineToRelative(6.305f, -1.654f)
                arcToRelative(11.882f, 11.882f, 0f, false, false, 5.683f, 1.448f)
                horizontalLineToRelative(0.005f)
                curveToRelative(6.554f, 0f, 11.89f, -5.335f, 11.893f, -11.893f)
                arcToRelative(11.821f, 11.821f, 0f, false, false, -3.423f, -8.452f)
                close()
            }
        }.build()
        return _SocialShareWhatsApp!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareWhatsApp: ImageVector? = null

// ---------------------------------------------------------------------------

val SocialShareMessage: ImageVector
    get() {
        if (_SocialShareMessage != null) return _SocialShareMessage!!
        _SocialShareMessage = ImageVector.Builder(
            name = "SocialShareMessage",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            // Outer chat bubble with tail
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 2f)
                horizontalLineTo(4f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(18f)
                lineToRelative(4f, -4f)
                horizontalLineToRelative(14f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(4f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
            }
            // Inner bubble fill to create outline effect
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 16f)
                horizontalLineTo(6f)
                lineToRelative(-2f, 2f)
                verticalLineTo(4f)
                horizontalLineToRelative(16f)
                verticalLineToRelative(12f)
                close()
            }
        }.build()
        return _SocialShareMessage!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareMessage: ImageVector? = null

// ---------------------------------------------------------------------------

val SocialShareEmail: ImageVector
    get() {
        if (_SocialShareEmail != null) return _SocialShareEmail!!
        _SocialShareEmail = ImageVector.Builder(
            name = "SocialShareEmail",
            defaultWidth = 42.dp, defaultHeight = 30.dp,
            viewportWidth = 42f, viewportHeight = 30f
        ).apply {
            path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                // Top flap with V crease
                moveTo(38.5f, 1.3f)
                lineTo(21f, 16.1f)
                lineTo(3.5f, 1.3f)
                curveTo(3.2f, 1.1f, 3.4f, 0.6f, 3.8f, 0.6f)
                horizontalLineToRelative(34.5f)
                curveTo(38.6f, 0.6f, 38.8f, 1.1f, 38.5f, 1.3f)
                close()
                // Left side panel
                moveTo(0.2f, 3.6f)
                curveToRelative(0f, -0.3f, 0.4f, -0.5f, 0.7f, -0.3f)
                lineToRelative(13.4f, 11.3f)
                lineTo(0.9f, 26.6f)
                curveToRelative(-0.3f, 0.2f, -0.7f, 0f, -0.7f, -0.3f)
                verticalLineTo(3.6f)
                close()
                // Right side panel
                moveTo(41.1f, 3.3f)
                lineTo(27.7f, 14.6f)
                lineToRelative(13.4f, 12f)
                curveToRelative(0.3f, 0.2f, 0.7f, 0f, 0.7f, -0.3f)
                verticalLineTo(3.6f)
                curveTo(41.8f, 3.2f, 41.4f, 3f, 41.1f, 3.3f)
                close()
                // Bottom flap with V crease
                moveTo(22.5f, 19f)
                lineToRelative(2.7f, -2.3f)
                lineToRelative(13.4f, 12f)
                curveToRelative(0.3f, 0.2f, 0.1f, 0.7f, -0.3f, 0.7f)
                horizontalLineTo(3.6f)
                curveToRelative(-0.4f, 0f, -0.5f, -0.5f, -0.3f, -0.7f)
                lineToRelative(13.4f, -12f)
                lineToRelative(2.7f, 2.3f)
                curveToRelative(0.4f, 0.4f, 1f, 0.6f, 1.5f, 0.6f)
                curveTo(21.5f, 19.6f, 22.1f, 19.4f, 22.5f, 19f)
                close()
            }
        }.build()
        return _SocialShareEmail!!
    }

@Suppress("ObjectPropertyName")
private var _SocialShareEmail: ImageVector? = null

// ---------------------------------------------------------------------------

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
