package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

enum class KSButtonType {
    FILLED,
    GREEN,
    FILLED_INVERTED,
    FILLED_DESTRUCTIVE,
    BORDERLESS,
    OUTLINED,
    OUTLINED_DESTRUCTIVE,
    BORDERLESS_DESTRUCTIVE,
    FACEBOOK,
}

@Composable
fun KSButton(
    modifier: Modifier = Modifier,
    type: KSButtonType,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    imageId: Int? = null,
    imageContentDescription: String? = null,
) {
    val colors = if (!isSystemInDarkTheme()) {
        KSDefaultButtonColors
    } else {
        KSDarkButtonColors
    }

    val buttonColors = when(type) {
        KSButtonType.FILLED -> filledButtonColors(colors)
        KSButtonType.GREEN -> greenButtonColors(colors)
        KSButtonType.FILLED_INVERTED -> filledInvertedButtonColors(colors)
        KSButtonType.FILLED_DESTRUCTIVE -> filledDestructiveButtonColors(colors)
        KSButtonType.BORDERLESS -> borderlessButtonColors(colors)
        KSButtonType.OUTLINED -> outlinedButtonColors(colors)
        KSButtonType.OUTLINED_DESTRUCTIVE -> outlinedDestructiveButtonColors(colors)
        KSButtonType.BORDERLESS_DESTRUCTIVE -> borderLessDestructiveButtonColors(colors)
        KSButtonType.FACEBOOK -> fbLoginButtonColors(colors)
    }

    BaseButton(
        modifier = modifier,
        onClickAction = onClickAction,
        text = text,
        buttonColors = buttonColors,
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed,
        isBorderless = type == KSButtonType.BORDERLESS || type == KSButtonType.BORDERLESS_DESTRUCTIVE
    )
}

@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    buttonColors: ButtonColorVariables,
    imageId: Int? = null,
    imageContentDescription: String? = null,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    isBorderless: Boolean = false,
) {
    val currentBackgroundColor = when {
        !isEnabled -> buttonColors.disabledColor
        isPressed -> buttonColors.pressedColor
        else -> buttonColors.backgroundColor
    }

    val currentTextColor = if (isEnabled) buttonColors.textColor else buttonColors.disabledTextColor

    Button(
        border = if (!isBorderless) {
            BorderStroke(
                width = dimensions.dividerThickness,
                color = when {
                    isPressed -> buttonColors.borderColorPressed
                    !isEnabled -> buttonColors.borderColorDisabled
                    isLoading -> buttonColors.borderColorDisabled
                    else -> buttonColors.borderColor
                },
            )
        } else BorderStroke(0.dp, Color.Transparent),
        modifier = modifier
            .defaultMinSize(minHeight = dimensions.minButtonHeight),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = currentBackgroundColor,
            disabledBackgroundColor = buttonColors.disabledColor
        ),
        onClick = { onClickAction.invoke() },
        enabled = isEnabled && !isLoading,
        shape = RoundedCornerShape(size = dimensions.radiusExtraSmall),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (imageId != null) {
                    Icon(
                        modifier = Modifier.size(dimensions.paddingMedium),
                        painter = painterResource(id = imageId),
                        contentDescription = imageContentDescription,
                        tint = if (!isEnabled || isLoading) buttonColors.iconDisabled else buttonColors.iconEnabled
                    )
                }
                Text(
                    text = text,
                    color = if (isLoading) buttonColors.disabledTextColor else currentTextColor,
                    style = typographyV2.buttonLabel,
                    modifier = Modifier.padding(start = if (imageId != null) dimensions.paddingSmall else dimensions.none)
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensions.loadingSpinnerSize),
                    color = buttonColors.loadingSpinnerColor,
                    strokeWidth = dimensions.strokeWidth
                )
            }
        }
    }
}

@Composable
fun FBLoginButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isPressed: Boolean = false,
) {
    val colors = fbLoginButtonColors(if (!isSystemInDarkTheme()) KSDefaultButtonColors else KSDarkButtonColors)
    val currentBackgroundColor =
        if (isPressed) colors.pressedColor else colors.backgroundColor

    Button(
        onClick = { onClickAction.invoke() },
        modifier = modifier
            .defaultMinSize(minHeight = dimensions.minButtonHeight),
        colors = ButtonDefaults.buttonColors(backgroundColor = currentBackgroundColor),
        shape = RoundedCornerShape(size = dimensions.radiusExtraSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.com_facebook_button_icon),
                contentDescription = "Facebook Logo",
                modifier = Modifier
                    .size(dimensions.imageSizeMedium)
            )
            Text(
                text = text,
                color = colors.textColor,
                style = typographyV2.buttonLabel,
                modifier = Modifier.padding(start = dimensions.paddingSmall)
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSFilledButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(color = colors.backgroundSurfacePrimary),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED,
                text = "Filled",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSGreenButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(color = colors.backgroundSurfacePrimary),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.GREEN,
                text = "Green",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.GREEN,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.GREEN,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.GREEN,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSFilledInvertedButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(color = colors.backgroundSurfacePrimary),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_INVERTED,
                text = "Inverted",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_INVERTED,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_INVERTED,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_INVERTED,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSFilledDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(color = colors.backgroundSurfacePrimary),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_DESTRUCTIVE,
                text = "Destructive",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_DESTRUCTIVE,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_DESTRUCTIVE,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_DESTRUCTIVE,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSBorderlessButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .background(color = colors.backgroundSurfacePrimary)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS,
                text = "Borderless",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSOutlinedButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .background(color = colors.backgroundSurfacePrimary)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED,
                text = "Outlined",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSOutlinedDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .background(color = colors.backgroundSurfacePrimary)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED_DESTRUCTIVE,
                text = "Outlined Destructive",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED_DESTRUCTIVE,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED_DESTRUCTIVE,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED_DESTRUCTIVE,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSBorderlessDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .background(color = colors.backgroundSurfacePrimary)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS_DESTRUCTIVE,
                text = "Borderless Destructive",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS_DESTRUCTIVE,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS_DESTRUCTIVE,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS_DESTRUCTIVE,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun FBLoginButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .background(colors.kds_white)
                .padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FBLoginButton(onClickAction = {}, text = "Continue with Facebook")
            FBLoginButton(onClickAction = {}, text = "Continue with Facebook", isPressed = true)
        }
    }
}
