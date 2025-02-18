package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

object ButtonVariables {
    val ButtonCornerRadius: Dp = 4.dp
}

object FilledButtonVariables {
    val ColorBackgroundAction: Color = Color(0xFF171717)
    val ColorBackgroundActionPressed: Color = Color(0xFF3C3C3C)
    val ColorBackgroundActionDisabled: Color = Color(0xFFB3B3B3)
    val ColorTextInverseDisabled: Color = Color(0xFFF2F2F2)
    val ColorLoadingSpinner: Color = Color(0xFF3C3C3C)
}

object GreenButtonVariables {
    val ColorBackgroundAccentGreenBold: Color = Color(0xFF037242)
    val ColorBackgroundAccentGreenBoldPressed: Color = Color(0xFF024629)
    val ColorBackgroundAccentGreenDisabled: Color = Color(0xFFEBFEF6)
    val ColorTextDisabled: Color = Color(0xFFB3B3B3)
    val ColorLoadingSpinner: Color = Color(0xFF025A34)
}

object FilledInvertedButtonVariables {
    val ColorBackgroundSurfacePrimary: Color = Color(0xFFFFFFFF)
    val ColorBackgroundInversePressed: Color = Color(0xFFE0E0E0)
    val ColorBackgroundInverseDisabled: Color = Color(0xFFF2F2F2)
    val ColorTextPrimary: Color = Color(0xFF171717)
    val ColorTextDisabled: Color = Color(0xFFB3B3B3)
    val ColorLoadingSpinner: Color = Color(0xFF3C3C3C)
}

object FilledDestructiveButtonVariables {
    val ColorBackgroundDangerBold: Color = Color(0xFFB81F14)
    val ColorBackgroundDangerDisabled: Color = Color(0xFFF7BBB7)
    val ColorTextInversePrimary: Color = Color(0xFFFFFFFF)
    val ColorTextAccentRedInverseDisabled: Color = Color(0xFFFEF2F1)
    val ColorLoadingSpinner: Color = Color(0xFF931910)
}

object BorderlessButtonVariables {
    val ColorTextPrimary: Color = Color(0xFF171717)
    val ColorBackgroundInversePressed: Color = Color(0xFFE0E0E0)
    val ColorTextDisabled: Color = Color(0xFFB3B3B3)
    val ColorLoadingSpinner: Color = Color(0xFF3C3C3C)
}

object OutlinedButtonVariables {
    val ColorBorderBold: Color = Color(0xFFC9C9C9)
    val ColorBackgroundInversePressed: Color = Color(0xFFE0E0E0)
    val ColorBorderSubtle: Color = Color(0xFFE0E0E0)
    val ColorTextPrimary: Color = Color(0xFF171717)
    val ColorTextDisabled: Color = Color(0xFFB3B3B3)
    val ColorLoadingSpinner: Color = Color(0xFF3C3C3C)
}

object OutlinedDestructiveButtonVariables {
    val ColorBackgroundDangerBold: Color = Color(0xFFB81F14)
    val ColorTextAccentRed: Color = Color(0xFF73140D)
    val ColorBackgroundAccentRedSubtle: Color = Color(0xFFFEF2F1)
    val ButtonCornerRadius: Dp = 4.dp
    val ColorBorderDangerBold: Color = Color(0xFF73140D)
    val ColorTextAccentRedDisabled: Color = Color(0xFFF7BBB7)
    val ColorLoadingSpinner: Color = Color(0xFF931910)
}

object BorderlessDestructiveButtonVariables {
    val ColorTextAccentRed: Color = Color(0xFFB81F14)
    val ColorTextAccentRedBolder: Color = Color(0xFF73140D)
    val ColorTextAccentRedDisabled: Color = Color(0xFFF7BBB7)
    val ColorBackgroundAccentRedSubtle: Color = Color(0xFFFEF2F1)
    val ButtonCornerRadius: Dp = 4.dp
    val ColorLoadingSpinner: Color = Color(0xFF931910)
}

object FBLoginButtonVariables {
    val ButtonCornerRadius: Dp = 4.dp
    val ButtonBackgroundColor: Color = Color(0xFF1877F2)
    val ButtonTextColor: Color = Color.White
    val ButtonPressedColor: Color = Color(0xFF135ABE)
}

data class ButtonColors(
    val backgroundColor: Color,
    val pressedColor: Color,
    val disabledColor: Color,
    val textColor: Color,
    val disabledTextColor: Color,
    val loadingSpinnerColor: Color
)


@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    backgroundColor: Color,
    pressedColor: Color,
    disabledColor: Color,
    textColor: Color,
    disabledTextColor: Color,
    loadingSpinnerColor: Color,
    imageId: Int? = null,
    imageContentDescription: String? = null,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    isBorderless: Boolean = false  // New parameter to detect borderless buttons
) {
    val currentBackgroundColor = when {
        !isEnabled -> disabledColor
        isPressed -> pressedColor
        else -> backgroundColor
    }

    val currentTextColor = if (isEnabled) textColor else disabledTextColor

    Button(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimensions.minButtonHeight),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = currentBackgroundColor,
            disabledBackgroundColor = disabledColor
        ),
        onClick = { onClickAction.invoke() },
        enabled = isEnabled && !isLoading,
        shape = RoundedCornerShape(size = ButtonVariables.ButtonCornerRadius),
        elevation = if (isBorderless) ButtonDefaults.elevation(0.dp) else ButtonDefaults.elevation()  // Removes shadow if borderless
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (imageId != null) {
                    Image(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(id = imageId),
                        contentDescription = imageContentDescription
                    )
                }
                Text(
                    text = text,
                    color = currentTextColor,
                    style = typographyV2.buttonLabel,
                    modifier = Modifier.padding(start = if (imageId != null) 8.dp else 0.dp)
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = loadingSpinnerColor,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}


@Composable
fun CreateButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    colors: ButtonColors,
    imageId: Int? = null,
    imageContentDescription: String? = null,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    isBorderless: Boolean = false
) {
    BaseButton(
        modifier = modifier,
        onClickAction = onClickAction,
        text = text,
        backgroundColor = colors.backgroundColor,
        pressedColor = colors.pressedColor,
        disabledColor = colors.disabledColor,
        textColor = colors.textColor,
        disabledTextColor = colors.disabledTextColor,
        loadingSpinnerColor = colors.loadingSpinnerColor,
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed,
        isBorderless = isBorderless
    )
}


@Composable
fun KSFilledButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    imageId: Int? = null,
    imageContentDescription: String? = null
) {
    CreateButton(
        modifier = modifier,
        onClickAction = onClickAction,
        text = text,
        colors = ButtonColors(
            backgroundColor = FilledButtonVariables.ColorBackgroundAction,
            pressedColor = FilledButtonVariables.ColorBackgroundActionPressed,
            disabledColor = FilledButtonVariables.ColorBackgroundActionDisabled,
            textColor = Color.White,
            disabledTextColor = FilledButtonVariables.ColorTextInverseDisabled,
            loadingSpinnerColor = FilledButtonVariables.ColorLoadingSpinner
        ),
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed
    )
}


@Composable
fun KSGreenButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    imageId: Int? = null,
    imageContentDescription: String? = null
) {
    CreateButton(
        modifier = modifier,
        onClickAction = onClickAction,
        text = text,
        colors = ButtonColors(
            backgroundColor = GreenButtonVariables.ColorBackgroundAccentGreenBold,
            pressedColor = GreenButtonVariables.ColorBackgroundAccentGreenBoldPressed,
            disabledColor = GreenButtonVariables.ColorBackgroundAccentGreenDisabled,
            textColor = Color.White,
            disabledTextColor = GreenButtonVariables.ColorTextDisabled,
            loadingSpinnerColor = GreenButtonVariables.ColorLoadingSpinner
        ),
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed
    )
}


@Composable
fun KSFilledInvertedButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    imageId: Int? = null,
    imageContentDescription: String? = null
) {
    CreateButton(
        modifier = modifier,
        onClickAction = onClickAction,
        text = text,
        colors = ButtonColors(
            backgroundColor = FilledInvertedButtonVariables.ColorBackgroundSurfacePrimary,
            pressedColor = FilledInvertedButtonVariables.ColorBackgroundInversePressed,
            disabledColor = FilledInvertedButtonVariables.ColorBackgroundInverseDisabled,
            textColor = FilledInvertedButtonVariables.ColorTextPrimary,
            disabledTextColor = FilledInvertedButtonVariables.ColorTextDisabled,
            loadingSpinnerColor = FilledInvertedButtonVariables.ColorLoadingSpinner
        ),
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed
    )
}


@Composable
fun KSFilledDestructiveButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    imageId: Int? = null,
    imageContentDescription: String? = null
) {
    CreateButton(
        modifier = modifier,
        onClickAction = onClickAction,
        text = text,
        colors = ButtonColors(
            backgroundColor = FilledDestructiveButtonVariables.ColorBackgroundDangerBold,
            pressedColor = FilledDestructiveButtonVariables.ColorBackgroundDangerBold,
            disabledColor = FilledDestructiveButtonVariables.ColorBackgroundDangerDisabled,
            textColor = FilledDestructiveButtonVariables.ColorTextInversePrimary,
            disabledTextColor = FilledDestructiveButtonVariables.ColorTextAccentRedInverseDisabled,
            loadingSpinnerColor = FilledDestructiveButtonVariables.ColorLoadingSpinner
        ),
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed
    )
}


@Composable
fun KSBorderlessButton(
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    imageId: Int? = null,
    imageContentDescription: String? = null
) {
    CreateButton(
        onClickAction = onClickAction,
        text = text,
        colors = ButtonColors(
            backgroundColor = Color.Transparent,
            pressedColor = BorderlessButtonVariables.ColorBackgroundInversePressed,
            disabledColor = Color.Transparent,
            textColor = BorderlessButtonVariables.ColorTextPrimary,
            disabledTextColor = BorderlessButtonVariables.ColorTextDisabled,
            loadingSpinnerColor = BorderlessButtonVariables.ColorLoadingSpinner
        ),
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed,
        isBorderless = true
    )
}


@Composable
fun KSOutlinedButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    imageId: Int? = null,
    imageContentDescription: String? = null
) {
    CreateButton(
        modifier = modifier
            .border(
                width = 1.dp,
                color = when {
                    isPressed -> OutlinedButtonVariables.ColorBackgroundInversePressed
                    !isEnabled -> OutlinedButtonVariables.ColorBorderSubtle
                    else -> OutlinedButtonVariables.ColorBorderBold
                },
                shape = RoundedCornerShape(size = ButtonVariables.ButtonCornerRadius)
            ),
        onClickAction = onClickAction,
        text = text,
        colors = ButtonColors(
            backgroundColor = Color.Transparent,
            pressedColor = OutlinedButtonVariables.ColorBackgroundInversePressed,
            disabledColor = Color.Transparent,
            textColor = OutlinedButtonVariables.ColorTextPrimary,
            disabledTextColor = OutlinedButtonVariables.ColorTextDisabled,
            loadingSpinnerColor = OutlinedButtonVariables.ColorLoadingSpinner
        ),
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed,
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isBorderless = true
    )
}

@Composable
fun KSOutlinedDestructiveButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    imageId: Int? = null,
    imageContentDescription: String? = null
) {
    CreateButton(
        modifier = modifier
            .border(
                width = 1.dp,
                color = when {
                    isPressed -> OutlinedDestructiveButtonVariables.ColorBorderDangerBold
                    !isEnabled -> OutlinedDestructiveButtonVariables.ColorTextAccentRedDisabled
                    else -> OutlinedDestructiveButtonVariables.ColorBackgroundDangerBold
                },
                shape = RoundedCornerShape(size = OutlinedDestructiveButtonVariables.ButtonCornerRadius)
            )
            .background(
                color = if (isPressed) OutlinedDestructiveButtonVariables.ColorBackgroundAccentRedSubtle else Color.Transparent,
                shape = RoundedCornerShape(size = OutlinedDestructiveButtonVariables.ButtonCornerRadius)
            ),
        onClickAction = onClickAction,
        text = text,
        colors = ButtonColors(
            backgroundColor = Color.Transparent,
            pressedColor = OutlinedDestructiveButtonVariables.ColorBackgroundAccentRedSubtle,
            disabledColor = Color.Transparent,
            textColor = OutlinedDestructiveButtonVariables.ColorTextAccentRed,
            disabledTextColor = OutlinedDestructiveButtonVariables.ColorTextAccentRedDisabled,
            loadingSpinnerColor = OutlinedDestructiveButtonVariables.ColorLoadingSpinner
        ),
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed,
        isBorderless = true
    )
}


@Composable
fun KSBorderlessDestructiveButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false,
    imageId: Int? = null,
    imageContentDescription: String? = null
) {
    CreateButton(
        modifier = modifier
            .background(
                color = if (isPressed) BorderlessDestructiveButtonVariables.ColorBackgroundAccentRedSubtle else Color.Transparent,
                shape = RoundedCornerShape(size = BorderlessDestructiveButtonVariables.ButtonCornerRadius)
            ),
        onClickAction = onClickAction,
        text = text,
        colors = ButtonColors(
            backgroundColor = Color.Transparent,
            pressedColor = BorderlessDestructiveButtonVariables.ColorBackgroundAccentRedSubtle,
            disabledColor = Color.Transparent,
            textColor = if (isPressed) BorderlessDestructiveButtonVariables.ColorTextAccentRedBolder else BorderlessDestructiveButtonVariables.ColorTextAccentRed,
            disabledTextColor = BorderlessDestructiveButtonVariables.ColorTextAccentRedDisabled,
            loadingSpinnerColor = BorderlessDestructiveButtonVariables.ColorLoadingSpinner
        ),
        imageId = imageId,
        imageContentDescription = imageContentDescription,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed,
        isBorderless = true
    )
}


@Composable
fun FBLoginButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isPressed: Boolean = false
) {
    val backgroundColor by remember { mutableStateOf(if (isPressed) FBLoginButtonVariables.ButtonPressedColor else FBLoginButtonVariables.ButtonBackgroundColor) }

    Button(
        onClick = { onClickAction.invoke() },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimensions.minButtonHeight),
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
        shape = RoundedCornerShape(size = FBLoginButtonVariables.ButtonCornerRadius)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.com_facebook_button_icon),
                contentDescription = "Facebook Logo",
                contentScale = ContentScale.None,
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
            )
            Text(
                text = text,
                color = FBLoginButtonVariables.ButtonTextColor,
                style = typographyV2.buttonLabel,
            )
        }
    }
}


@Preview
@Composable
fun KSFilledButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall).fillMaxWidth().background(colors.kds_white),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSFilledButton(onClickAction = {}, text = "Filled", imageId = R.drawable.icon_eye_gray)
            KSFilledButton(onClickAction = {}, text = "Pressed", isPressed = true, imageId = R.drawable.icon_eye_gray)
            KSFilledButton(onClickAction = {}, text = "Disabled", isEnabled = false, imageId = R.drawable.icon_eye_gray)
            KSFilledButton(onClickAction = {}, text = "Loading", isLoading = true, imageId = R.drawable.icon_eye_gray)
        }
    }
}

@Preview
@Composable
fun KSGreenButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSGreenButton(onClickAction = {}, text = "Green", imageId = R.drawable.icon_eye_gray)
            KSGreenButton(onClickAction = {}, text = "Pressed", isPressed = true, imageId = R.drawable.icon_eye_gray)
            KSGreenButton(onClickAction = {}, text = "Disabled", isEnabled = false, imageId = R.drawable.icon_eye_gray)
            KSGreenButton(onClickAction = {}, text = "Loading", isLoading = true, imageId = R.drawable.icon_eye_gray)
        }
    }
}

@Preview
@Composable
fun KSFilledInvertedButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSFilledInvertedButton(onClickAction = {}, text = "Inverted", imageId = R.drawable.icon_eye_gray)
            KSFilledInvertedButton(onClickAction = {}, text = "Pressed", isPressed = true, imageId = R.drawable.icon_eye_gray)
            KSFilledInvertedButton(onClickAction = {}, text = "Disabled", isEnabled = false, imageId = R.drawable.icon_eye_gray)
            KSFilledInvertedButton(onClickAction = {}, text = "Loading", isLoading = true, imageId = R.drawable.icon_eye_gray)
        }
    }
}

@Preview
@Composable
fun KSFilledDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSFilledDestructiveButton(onClickAction = {}, text = "Destructive", imageId = R.drawable.icon_eye_gray)
            KSFilledDestructiveButton(onClickAction = {}, text = "Pressed", isPressed = true, imageId = R.drawable.icon_eye_gray)
            KSFilledDestructiveButton(onClickAction = {}, text = "Disabled", isEnabled = false, imageId = R.drawable.icon_eye_gray)
            KSFilledDestructiveButton(onClickAction = {}, text = "Loading", isLoading = true, imageId = R.drawable.icon_eye_gray)
        }
    }
}

@Preview
@Composable
fun KSBorderlessButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSBorderlessButton(onClickAction = {}, text = "Borderless", imageId = R.drawable.icon_eye_gray)
            KSBorderlessButton(onClickAction = {}, text = "Pressed", isPressed = true, imageId = R.drawable.icon_eye_gray)
            KSBorderlessButton(onClickAction = {}, text = "Disabled", isEnabled = false, imageId = R.drawable.icon_eye_gray)
            KSBorderlessButton(onClickAction = {}, text = "Loading", isLoading = true, imageId = R.drawable.icon_eye_gray)
        }
    }
}

@Preview
@Composable
fun KSOutlinedButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSOutlinedButton(onClickAction = {}, text = "Outlined", imageId = R.drawable.icon_eye_gray)
            KSOutlinedButton(onClickAction = {}, text = "Pressed", isPressed = true, imageId = R.drawable.icon_eye_gray)
            KSOutlinedButton(onClickAction = {}, text = "Disabled", isEnabled = false, imageId = R.drawable.icon_eye_gray)
            KSOutlinedButton(onClickAction = {}, text = "Loading", isLoading = true, imageId = R.drawable.icon_eye_gray)
        }
    }
}

@Preview
@Composable
fun KSOutlinedDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSOutlinedDestructiveButton(onClickAction = {}, text = "Destructive", imageId = R.drawable.icon_eye_gray)
            KSOutlinedDestructiveButton(onClickAction = {}, text = "Pressed", isPressed = true, imageId = R.drawable.icon_eye_gray)
            KSOutlinedDestructiveButton(onClickAction = {}, text = "Disabled", isEnabled = false, imageId = R.drawable.icon_eye_gray)
            KSOutlinedDestructiveButton(onClickAction = {}, text = "Loading", isLoading = true, imageId = R.drawable.icon_eye_gray)
        }
    }
}

@Preview
@Composable
fun KSBorderlessDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSBorderlessDestructiveButton(onClickAction = {}, text = "Borderless", imageId = R.drawable.icon_eye_gray)
            KSBorderlessDestructiveButton(onClickAction = {}, text = "Pressed", isPressed = true, imageId = R.drawable.icon_eye_gray)
            KSBorderlessDestructiveButton(onClickAction = {}, text = "Disabled", isEnabled = false, imageId = R.drawable.icon_eye_gray)
            KSBorderlessDestructiveButton(onClickAction = {}, text = "Loading", isLoading = true, imageId = R.drawable.icon_eye_gray)
        }
    }
}

@Preview
@Composable
fun FBLoginButtonPreview() {
    KSTheme {
        Column(
            Modifier.fillMaxWidth().background(colors.kds_white).padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FBLoginButton(onClickAction = {}, text = "Continue with Facebook")
            FBLoginButton(onClickAction = {}, text = "Continue with Facebook", isPressed = true)
        }
    }
}
