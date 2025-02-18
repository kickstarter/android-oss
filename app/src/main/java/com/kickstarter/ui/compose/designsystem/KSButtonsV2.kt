package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kickstarter.R
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

@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClickAction: () -> Unit,
    text: String,
    backgroundColor: Color,
    pressedColor: Color,
    disabledColor: Color,
    textColor: Color,
    disabledTextColor: Color,
    loadingSpinnerColor: Color,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false
) {
    val currentBackgroundColor by remember(isPressed, isEnabled, isLoading) {
        mutableStateOf(
            when {
                !isEnabled -> disabledColor
                isPressed -> pressedColor
                else -> backgroundColor
            }
        )
    }

    val currentTextColor by remember(isEnabled) {
        mutableStateOf(if (isEnabled) textColor else disabledTextColor)
    }

    Button(
        modifier = modifier
            .width(100.dp)
            .height(40.dp)
            .background(color = currentBackgroundColor, shape = RoundedCornerShape(size = ButtonVariables.ButtonCornerRadius))
            .padding(0.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = currentBackgroundColor,
            disabledBackgroundColor = disabledColor
        ),
        onClick = { onClickAction.invoke() },
        enabled = isEnabled && !isLoading,
        shape = RoundedCornerShape(size = ButtonVariables.ButtonCornerRadius)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = loadingSpinnerColor,
                    strokeWidth = 2.dp
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.invoke()
                Text(text = text, color = currentTextColor, style = typographyV2.buttonLabel)
            }
        }
    }
}

@Composable
fun KSFilledButton(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false
) {
    BaseButton(
        modifier, leadingIcon, onClickAction, text,
        FilledButtonVariables.ColorBackgroundAction,
        FilledButtonVariables.ColorBackgroundActionPressed,
        FilledButtonVariables.ColorBackgroundActionDisabled,
        Color.White,
        FilledButtonVariables.ColorTextInverseDisabled,
        FilledButtonVariables.ColorLoadingSpinner,
        isEnabled, isLoading, isPressed
    )
}

@Composable
fun KSGreenButton(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false
) {
    BaseButton(
        modifier, leadingIcon, onClickAction, text,
        GreenButtonVariables.ColorBackgroundAccentGreenBold,
        GreenButtonVariables.ColorBackgroundAccentGreenBoldPressed,
        GreenButtonVariables.ColorBackgroundAccentGreenDisabled,
        Color.White,
        GreenButtonVariables.ColorTextDisabled,
        GreenButtonVariables.ColorLoadingSpinner,
        isEnabled, isLoading, isPressed
    )
}

@Composable
fun KSFilledInvertedButton(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false
) {
    BaseButton(
        modifier, leadingIcon, onClickAction, text,
        FilledInvertedButtonVariables.ColorBackgroundSurfacePrimary,
        FilledInvertedButtonVariables.ColorBackgroundInversePressed,
        FilledInvertedButtonVariables.ColorBackgroundInverseDisabled,
        FilledInvertedButtonVariables.ColorTextPrimary,
        FilledInvertedButtonVariables.ColorTextDisabled,
        FilledInvertedButtonVariables.ColorLoadingSpinner,
        isEnabled, isLoading, isPressed
    )
}

@Composable
fun KSFilledDestructiveButton(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false
) {
    BaseButton(
        modifier, leadingIcon, onClickAction, text,
        FilledDestructiveButtonVariables.ColorBackgroundDangerBold,
        FilledDestructiveButtonVariables.ColorBackgroundDangerBold,
        FilledDestructiveButtonVariables.ColorBackgroundDangerDisabled,
        FilledDestructiveButtonVariables.ColorTextInversePrimary,
        FilledDestructiveButtonVariables.ColorTextAccentRedInverseDisabled,
        FilledDestructiveButtonVariables.ColorLoadingSpinner,
        isEnabled, isLoading, isPressed
    )
}

@Composable
fun KSBorderlessButton(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false
) {
    BaseButton(
        modifier = modifier
            .width(100.dp)
            .height(40.dp)
            .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
        leadingIcon = leadingIcon,
        onClickAction = onClickAction,
        text = text,
        backgroundColor = Color.Transparent,
        pressedColor = BorderlessButtonVariables.ColorBackgroundInversePressed,
        disabledColor = Color.Transparent,
        textColor = BorderlessButtonVariables.ColorTextPrimary,
        disabledTextColor = BorderlessButtonVariables.ColorTextDisabled,
        loadingSpinnerColor = BorderlessButtonVariables.ColorLoadingSpinner,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed
    )
}

@Composable
fun KSOutlinedButton(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false
) {
    BaseButton(
        modifier = modifier
            .width(100.dp)
            .height(40.dp)
            .border(
                width = 1.dp,
                color = when {
                    isPressed -> OutlinedButtonVariables.ColorBackgroundInversePressed
                    !isEnabled -> OutlinedButtonVariables.ColorBorderSubtle
                    else -> OutlinedButtonVariables.ColorBorderBold
                },
                shape = RoundedCornerShape(size = ButtonVariables.ButtonCornerRadius)
            )
            .padding(0.dp),
        leadingIcon = leadingIcon,
        onClickAction = onClickAction,
        text = text,
        backgroundColor = Color.Transparent,
        pressedColor = OutlinedButtonVariables.ColorBackgroundInversePressed,
        disabledColor = Color.Transparent,
        textColor = OutlinedButtonVariables.ColorTextPrimary,
        disabledTextColor = OutlinedButtonVariables.ColorTextDisabled,
        loadingSpinnerColor = OutlinedButtonVariables.ColorLoadingSpinner,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed
    )
}

@Composable
fun KSOutlinedDestructiveButton(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false
) {
    BaseButton(
        modifier = modifier
            .width(100.dp)
            .height(40.dp)
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
            )
            .padding(0.dp),
        leadingIcon = leadingIcon,
        onClickAction = onClickAction,
        text = text,
        backgroundColor = Color.Transparent,
        pressedColor = OutlinedDestructiveButtonVariables.ColorBackgroundAccentRedSubtle,
        disabledColor = Color.Transparent,
        textColor = OutlinedDestructiveButtonVariables.ColorTextAccentRed,
        disabledTextColor = OutlinedDestructiveButtonVariables.ColorTextAccentRedDisabled,
        loadingSpinnerColor = OutlinedDestructiveButtonVariables.ColorLoadingSpinner,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed
    )
}

@Composable
fun KSBorderlessDestructiveButton(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    isPressed: Boolean = false
) {
    BaseButton(
        modifier = modifier
            .width(100.dp)
            .height(40.dp)
            .background(
                color = if (isPressed) BorderlessDestructiveButtonVariables.ColorBackgroundAccentRedSubtle else Color.Transparent,
                shape = RoundedCornerShape(size = BorderlessDestructiveButtonVariables.ButtonCornerRadius)
            )
            .padding(0.dp),
        leadingIcon = leadingIcon,
        onClickAction = onClickAction,
        text = text,
        backgroundColor = Color.Transparent,
        pressedColor = BorderlessDestructiveButtonVariables.ColorBackgroundAccentRedSubtle,
        disabledColor = Color.Transparent,
        textColor = if (isPressed) BorderlessDestructiveButtonVariables.ColorTextAccentRedBolder else BorderlessDestructiveButtonVariables.ColorTextAccentRed,
        disabledTextColor = BorderlessDestructiveButtonVariables.ColorTextAccentRedDisabled,
        loadingSpinnerColor = BorderlessDestructiveButtonVariables.ColorLoadingSpinner,
        isEnabled = isEnabled,
        isLoading = isLoading,
        isPressed = isPressed
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
            .width(208.dp)
            .height(40.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(size = FBLoginButtonVariables.ButtonCornerRadius)
            )
            .padding(0.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
        shape = RoundedCornerShape(size = FBLoginButtonVariables.ButtonCornerRadius)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    style = typographyV2.buttonLabel
                )
            }
        }
    }
}

@Preview
@Composable
fun KSFilledButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSFilledButton(onClickAction = {}, text = "Filled")
            KSFilledButton(onClickAction = {}, text = "Pressed", isPressed = true)
            KSFilledButton(onClickAction = {}, text = "Disabled", isEnabled = false)
            KSFilledButton(onClickAction = {}, text = "Loading", isLoading = true)
        }
    }
}

@Preview
@Composable
fun KSGreenButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSGreenButton(onClickAction = {}, text = "Green")
            KSGreenButton(onClickAction = {}, text = "Pressed", isPressed = true)
            KSGreenButton(onClickAction = {}, text = "Disabled", isEnabled = false)
            KSGreenButton(onClickAction = {}, text = "Loading", isLoading = true)
        }
    }
}

@Preview
@Composable
fun KSFilledInvertedButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSFilledInvertedButton(onClickAction = {}, text = "Inverted")
            KSFilledInvertedButton(onClickAction = {}, text = "Pressed", isPressed = true)
            KSFilledInvertedButton(onClickAction = {}, text = "Disabled", isEnabled = false)
            KSFilledInvertedButton(onClickAction = {}, text = "Loading", isLoading = true)
        }
    }
}

@Preview
@Composable
fun KSFilledDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSFilledDestructiveButton(onClickAction = {}, text = "Destructive")
            KSFilledDestructiveButton(onClickAction = {}, text = "Pressed", isPressed = true)
            KSFilledDestructiveButton(onClickAction = {}, text = "Disabled", isEnabled = false)
            KSFilledDestructiveButton(onClickAction = {}, text = "Loading", isLoading = true)
        }
    }
}

@Preview
@Composable
fun KSBorderlessButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSBorderlessButton(onClickAction = {}, text = "Borderless")
            KSBorderlessButton(onClickAction = {}, text = "Pressed", isPressed = true)
            KSBorderlessButton(onClickAction = {}, text = "Disabled", isEnabled = false)
            KSBorderlessButton(onClickAction = {}, text = "Loading", isLoading = true)
        }
    }
}

@Preview
@Composable
fun KSOutlinedButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSOutlinedButton(onClickAction = {}, text = "Outlined")
            KSOutlinedButton(onClickAction = {}, text = "Pressed", isPressed = true)
            KSOutlinedButton(onClickAction = {}, text = "Disabled", isEnabled = false)
            KSOutlinedButton(onClickAction = {}, text = "Loading", isLoading = true)
        }
    }
}

@Preview
@Composable
fun KSOutlinedDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSOutlinedDestructiveButton(onClickAction = {}, text = "Destructive")
            KSOutlinedDestructiveButton(onClickAction = {}, text = "Pressed", isPressed = true)
            KSOutlinedDestructiveButton(onClickAction = {}, text = "Disabled", isEnabled = false)
            KSOutlinedDestructiveButton(onClickAction = {}, text = "Loading", isLoading = true)
        }
    }
}

@Preview
@Composable
fun KSBorderlessDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier.padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
        ) {
            KSBorderlessDestructiveButton(onClickAction = {}, text = "Borderless")
            KSBorderlessDestructiveButton(onClickAction = {}, text = "Pressed", isPressed = true)
            KSBorderlessDestructiveButton(onClickAction = {}, text = "Disabled", isEnabled = false)
            KSBorderlessDestructiveButton(onClickAction = {}, text = "Loading", isLoading = true)
        }
    }
}
@Preview
@Composable
fun FBLoginButtonPreview() {
    KSTheme {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FBLoginButton(onClickAction = {}, text = "Continue with Facebook")
            FBLoginButton(onClickAction = {}, text = "Continue with Facebook", isPressed = true)
        }
    }
}
