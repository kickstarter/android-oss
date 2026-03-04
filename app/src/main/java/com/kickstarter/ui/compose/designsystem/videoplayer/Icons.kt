package com.kickstarter.ui.compose.designsystem.videoplayer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val playButton: ImageVector
    get() {
        if (_iconButton != null) return _iconButton!!

        _iconButton = ImageVector.Builder(
            name = "playButton",
            defaultWidth = 65.dp,
            defaultHeight = 65.dp,
            viewportWidth = 65f,
            viewportHeight = 65f
        ).apply {
            path {
            }
            group {
                path(
                    fill = SolidColor(Color(0xFF2B2B2D))
                ) {
                    moveTo(32.38403f, 1.38394f)
                    horizontalLineTo(32.38403f)
                    arcTo(31f, 31f, 0f, false, true, 63.38403f, 32.38394f)
                    verticalLineTo(32.38394f)
                    arcTo(31f, 31f, 0f, false, true, 32.38403f, 63.38394f)
                    horizontalLineTo(32.38403f)
                    arcTo(31f, 31f, 0f, false, true, 1.38403f, 32.38394f)
                    verticalLineTo(32.38394f)
                    arcTo(31f, 31f, 0f, false, true, 32.38403f, 1.38394f)
                    close()
                }
            }
            group {
                path(
                    stroke = SolidColor(Color(0xFFFFFFFF)),
                    strokeLineWidth = 1.38393f
                ) {
                    moveTo(32.384018999999995f, 0.691977f)
                    horizontalLineTo(32.384019f)
                    arcTo(31.69195f, 31.69195f, 0f, false, true, 64.075969f, 32.383927f)
                    verticalLineTo(32.38392699999999f)
                    arcTo(31.69195f, 31.69195f, 0f, false, true, 32.384019f, 64.07587699999999f)
                    horizontalLineTo(32.384018999999995f)
                    arcTo(31.69195f, 31.69195f, 0f, false, true, 0.692069f, 32.38392699999999f)
                    verticalLineTo(32.383927f)
                    arcTo(31.69195f, 31.69195f, 0f, false, true, 32.384018999999995f, 0.691977f)
                    close()
                }
            }
            group {
                path(
                    fill = SolidColor(Color(0xFFFFFFFF))
                ) {
                    moveTo(24.8257f, 21.7344f)
                    curveTo(25.6334f, 21.2286f, 26.6449f, 21.174f, 27.5024f, 21.5898f)
                    lineTo(44.1099f, 29.6416f)
                    curveTo(45.0531f, 30.0991f, 45.6567f, 31.0504f, 45.6694f, 32.0986f)
                    curveTo(45.6821f, 33.147f, 45.1015f, 34.1126f, 44.1694f, 34.5928f)
                    lineTo(27.562f, 43.1484f)
                    curveTo(26.7041f, 43.5902f, 25.678f, 43.5531f, 24.854f, 43.0508f)
                    curveTo(24.0299f, 42.5483f, 23.5269f, 41.6527f, 23.5269f, 40.6875f)
                    verticalLineTo(24.0801f)
                    curveTo(23.527f, 23.1269f, 24.0179f, 22.2404f, 24.8257f, 21.7344f)
                    close()
                }
            }
        }.build()

        return _iconButton!!
    }

private var _iconButton: ImageVector? = null

val rewindIcon: ImageVector
    get() {
        if (_frame97545 != null) return _frame97545!!

        _frame97545 = ImageVector.Builder(
            name = "rewindIcon",
            defaultWidth = 38.dp,
            defaultHeight = 38.dp,
            viewportWidth = 38f,
            viewportHeight = 38f
        ).apply {
            path {
            }
            group {
                path(
                    fill = SolidColor(Color(0xFF2B2B2D))
                ) {
                    moveTo(1.00635f, 18.7208f)
                    curveTo(1.00635f, 8.93745f, 8.9373f, 1.0065f, 18.7206f, 1.0065f)
                    curveTo(28.504f, 1.0065f, 36.4349f, 8.93745f, 36.4349f, 18.7208f)
                    curveTo(36.4349f, 28.5041f, 28.504f, 36.4351f, 18.7206f, 36.4351f)
                    curveTo(8.9373f, 36.4351f, 1.00635f, 28.5041f, 1.00635f, 18.7208f)
                    close()
                }
            }
            group {
                path(
                    stroke = SolidColor(Color(0xFFFFFFFF)),
                    strokeLineWidth = 1.00649f
                ) {
                    moveTo(18.7202f, 0.503571f)
                    curveTo(28.7813f, 0.503571f, 36.9378f, 8.65929f, 36.938f, 18.7204f)
                    curveTo(36.938f, 28.7816f, 28.7815f, 36.9381f, 18.7202f, 36.9381f)
                    curveTo(8.65914f, 36.9379f, 0.503418f, 28.7815f, 0.503418f, 18.7204f)
                    curveTo(0.503644f, 8.65943f, 8.65928f, 0.503796f, 18.7202f, 0.503571f)
                    close()
                }
            }
            group {
                path {
                }
            }
            group {
                path(
                    fill = SolidColor(Color(0xFFFFFFFF))
                ) {
                    moveTo(18.6001f, 22.3784f)
                    curveTo(18.1551f, 22.3784f, 17.755f, 22.2931f, 17.3999f, 22.1227f)
                    curveTo(17.0471f, 21.9499f, 16.7654f, 21.7131f, 16.5547f, 21.4125f)
                    curveTo(16.344f, 21.1118f, 16.2315f, 20.7685f, 16.2173f, 20.3826f)
                    horizontalLineTo(17.2827f)
                    curveTo(17.3087f, 20.6951f, 17.4472f, 20.952f, 17.6982f, 21.1532f)
                    curveTo(17.9491f, 21.3545f, 18.2498f, 21.4551f, 18.6001f, 21.4551f)
                    curveTo(18.8795f, 21.4551f, 19.1269f, 21.3911f, 19.3423f, 21.2633f)
                    curveTo(19.5601f, 21.1331f, 19.7306f, 20.9544f, 19.8537f, 20.7271f)
                    curveTo(19.9792f, 20.4998f, 20.0419f, 20.2406f, 20.0419f, 19.9494f)
                    curveTo(20.0419f, 19.6535f, 19.978f, 19.3895f, 19.8501f, 19.1575f)
                    curveTo(19.7223f, 18.9255f, 19.5459f, 18.7432f, 19.321f, 18.6106f)
                    curveTo(19.0985f, 18.478f, 18.8428f, 18.4106f, 18.554f, 18.4082f)
                    curveTo(18.3338f, 18.4082f, 18.1125f, 18.4461f, 17.8899f, 18.5218f)
                    curveTo(17.6674f, 18.5976f, 17.4875f, 18.697f, 17.3501f, 18.8201f)
                    lineTo(16.3452f, 18.671f)
                    lineTo(16.7536f, 15.0062f)
                    horizontalLineTo(20.7521f)
                    verticalLineTo(15.9473f)
                    horizontalLineTo(17.6662f)
                    lineTo(17.4354f, 17.9821f)
                    horizontalLineTo(17.478f)
                    curveTo(17.62f, 17.8447f, 17.8082f, 17.7299f, 18.0426f, 17.6376f)
                    curveTo(18.2794f, 17.5453f, 18.5327f, 17.4991f, 18.8026f, 17.4991f)
                    curveTo(19.2453f, 17.4991f, 19.6394f, 17.6045f, 19.9851f, 17.8152f)
                    curveTo(20.3331f, 18.0259f, 20.6065f, 18.3135f, 20.8054f, 18.6781f)
                    curveTo(21.0066f, 19.0403f, 21.1061f, 19.457f, 21.1037f, 19.9281f)
                    curveTo(21.1061f, 20.3992f, 20.9995f, 20.8194f, 20.7841f, 21.1887f)
                    curveTo(20.571f, 21.558f, 20.2751f, 21.8492f, 19.8963f, 22.0623f)
                    curveTo(19.5199f, 22.273f, 19.0878f, 22.3784f, 18.6001f, 22.3784f)
                    close()
                }
            }
        }.build()

        return _frame97545!!
    }

private var _frame97545: ImageVector? = null

val forwardIcon: ImageVector
    get() {
        if (_forward != null) return _forward!!

        _forward = ImageVector.Builder(
            name = "forwardIcon",
            defaultWidth = 38.dp,
            defaultHeight = 38.dp,
            viewportWidth = 38f,
            viewportHeight = 38f
        ).apply {
            path {
            }
            group {
                path(
                    fill = SolidColor(Color(0xFF2B2B2D))
                ) {
                    moveTo(1.00659f, 18.7208f)
                    curveTo(1.00659f, 8.93745f, 8.93755f, 1.0065f, 18.7209f, 1.0065f)
                    curveTo(28.5042f, 1.0065f, 36.4352f, 8.93745f, 36.4352f, 18.7208f)
                    curveTo(36.4352f, 28.5041f, 28.5042f, 36.4351f, 18.7209f, 36.4351f)
                    curveTo(8.93755f, 36.4351f, 1.00659f, 28.5041f, 1.00659f, 18.7208f)
                    close()
                }
            }
            group {
                path(
                    stroke = SolidColor(Color(0xFFFFFFFF)),
                    strokeLineWidth = 1.00649f
                ) {
                    moveTo(18.7205f, 0.503571f)
                    curveTo(28.7816f, 0.503571f, 36.938f, 8.65929f, 36.9382f, 18.7204f)
                    curveTo(36.9382f, 28.7816f, 28.7817f, 36.9381f, 18.7205f, 36.9381f)
                    curveTo(8.65938f, 36.9379f, 0.503662f, 28.7815f, 0.503662f, 18.7204f)
                    curveTo(0.503888f, 8.65943f, 8.65952f, 0.503796f, 18.7205f, 0.503571f)
                    close()
                }
            }
            group {
                path {
                }
            }
            group {
                path(
                    fill = SolidColor(Color(0xFFFFFFFF))
                ) {
                    moveTo(18.6001f, 22.3781f)
                    curveTo(18.1551f, 22.3781f, 17.755f, 22.2929f, 17.3999f, 22.1224f)
                    curveTo(17.0471f, 21.9496f, 16.7654f, 21.7129f, 16.5547f, 21.4122f)
                    curveTo(16.344f, 21.1115f, 16.2315f, 20.7683f, 16.2173f, 20.3824f)
                    horizontalLineTo(17.2827f)
                    curveTo(17.3087f, 20.6949f, 17.4472f, 20.9517f, 17.6982f, 21.153f)
                    curveTo(17.9491f, 21.3542f, 18.2498f, 21.4548f, 18.6001f, 21.4548f)
                    curveTo(18.8795f, 21.4548f, 19.1269f, 21.3909f, 19.3423f, 21.2631f)
                    curveTo(19.5601f, 21.1329f, 19.7306f, 20.9541f, 19.8537f, 20.7268f)
                    curveTo(19.9792f, 20.4996f, 20.0419f, 20.2403f, 20.0419f, 19.9491f)
                    curveTo(20.0419f, 19.6532f, 19.978f, 19.3892f, 19.8501f, 19.1572f)
                    curveTo(19.7223f, 18.9252f, 19.5459f, 18.7429f, 19.321f, 18.6104f)
                    curveTo(19.0985f, 18.4778f, 18.8428f, 18.4103f, 18.554f, 18.4079f)
                    curveTo(18.3338f, 18.4079f, 18.1125f, 18.4458f, 17.8899f, 18.5216f)
                    curveTo(17.6674f, 18.5973f, 17.4875f, 18.6968f, 17.3501f, 18.8199f)
                    lineTo(16.3452f, 18.6707f)
                    lineTo(16.7536f, 15.006f)
                    horizontalLineTo(20.7521f)
                    verticalLineTo(15.947f)
                    horizontalLineTo(17.6662f)
                    lineTo(17.4354f, 17.9818f)
                    horizontalLineTo(17.478f)
                    curveTo(17.62f, 17.8445f, 17.8082f, 17.7297f, 18.0426f, 17.6374f)
                    curveTo(18.2794f, 17.545f, 18.5327f, 17.4989f, 18.8026f, 17.4989f)
                    curveTo(19.2453f, 17.4989f, 19.6394f, 17.6042f, 19.9851f, 17.8149f)
                    curveTo(20.3331f, 18.0256f, 20.6065f, 18.3133f, 20.8054f, 18.6778f)
                    curveTo(21.0066f, 19.0401f, 21.1061f, 19.4567f, 21.1037f, 19.9278f)
                    curveTo(21.1061f, 20.399f, 20.9995f, 20.8192f, 20.7841f, 21.1885f)
                    curveTo(20.571f, 21.5578f, 20.2751f, 21.849f, 19.8963f, 22.0621f)
                    curveTo(19.5199f, 22.2728f, 19.0878f, 22.3781f, 18.6001f, 22.3781f)
                    close()
                }
            }
        }.build()

        return _forward!!
    }

private var _forward: ImageVector? = null
