package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.kickstarter.R
import com.kickstarter.libs.utils.safeLet
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSPrimaryButtonsPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_white)
                .padding(all = dimensions.paddingSmall)
        ) {
            KSPrimaryGreenButton(
                onClickAction = { },
                isEnabled = true,
                text = "Back this project"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSPrimaryBlueButton(
                onClickAction = { },
                isEnabled = true,
                text = "Manage your pledge"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSPrimaryBlackButton(
                onClickAction = { },
                isEnabled = true,
                text = "Share this project"
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSSecondaryButtonsPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_white)
                .padding(all = dimensions.paddingSmall)
        ) {
            KSSecondaryGreyButton(
                onClickAction = { },
                isEnabled = true,
                text = "Fix your pledge"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSSecondaryWhiteButton(
                onClickAction = { },
                isEnabled = true,
                text = "Back this project"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSSecondaryRedButton(
                onClickAction = { },
                isEnabled = true,
                text = "Cancel my pledge"
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSDisabledButtonsPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_white)
                .padding(all = dimensions.paddingSmall)
        ) {
            KSPrimaryGreenButton(
                onClickAction = { },
                isEnabled = false,
                text = "Back this project"
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSOtherButtonsPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_white)
                .padding(all = dimensions.paddingSmall)
        ) {
            KSFacebookButton(
                onClickAction = { },
                text = "Continue with Facebook",
                isEnabled = true
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSGooglePayButton(onClickAction = { }, isEnabled = true)
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSSmallButtonsPreview() {
    KSTheme {
        Column {
            KSSmallBlueButton(onClickAction = {}, text = "BLUE", isEnabled = true)

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSSmallRedButton(onClickAction = {}, text = "RED", isEnabled = true)

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSSmallWhiteButton(onClickAction = {}, text = "WHITE", isEnabled = true)
        }
    }
}

@Composable
fun KSPrimaryGreenButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typography.body,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSButton(
            modifier = modifier,
            leadingIcon = leadingIcon,
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.backgroundAccentGreenBold,
            text = text,
            textStyle = textStyle,
            textColor = colors.textInversePrimary
        )
    }
}

@Composable
fun KSPrimaryBlueButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typography.body,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSButton(
            modifier = modifier,
            leadingIcon = leadingIcon,
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.backgroundAccentBlueBold,
            text = text,
            textStyle = textStyle,
            textColor = colors.textInversePrimary
        )
    }
}

@Composable
fun KSPrimaryBlackButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typography.body,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSButton(
            modifier = modifier,
            leadingIcon = leadingIcon,
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.backgroundAction,
            text = text,
            textStyle = textStyle,
            textColor = colors.textInversePrimary
        )
    }
}

@Composable
fun KSSecondaryGreyButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typography.body,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSButton(
            modifier = modifier,
            leadingIcon = leadingIcon,
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.backgroundAccentGrayBold,
            text = text,
            textStyle = textStyle,
            textColor = colors.textPrimary
        )
    }
}

@Composable
fun KSSecondaryRedButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typography.body,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSButton(
            modifier = modifier,
            leadingIcon = leadingIcon,
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.backgroundDangerBold,
            text = text,
            textStyle = textStyle,
            textColor = colors.textInversePrimary
        )
    }
}

@Composable
fun KSSecondaryWhiteButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typography.body,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeGrey) {
        KSButton(
            modifier = modifier,
            leadingIcon = leadingIcon,
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.backgroundSurfacePrimary,
            text = text,
            textStyle = textStyle,
            textColor = colors.textAccentGreenBold
        )
    }
}

@Composable
fun KSFacebookButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSIconButton(
            modifier = modifier,
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.facebook_blue,
            imageId = R.drawable.com_facebook_button_icon,
            text = text,
            textColor = kds_white
        )
    }
}

@Composable
fun KSGooglePayButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSIconButton(
            modifier = modifier,
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = kds_black,
            imageId = R.drawable.googlepay_button_content
        )
    }
}

@Composable
fun KSIconButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    isEnabled: Boolean,
    backgroundColor: Color,
    imageId: Int,
    imageContentDescription: String? = null,
    text: String? = null,
    textColor: Color? = null,
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimensions.minButtonHeight),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            disabledBackgroundColor = colors.backgroundActionDisabled
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.elevation(),
        enabled = isEnabled,
        shape = shapes.medium
    ) {
        Row {
            Image(
                modifier = Modifier.defaultMinSize(
                    minHeight = dimensions.imageSizeMedium,
                    minWidth = dimensions.imageSizeMedium
                ),
                painter = painterResource(id = imageId),
                contentDescription = imageContentDescription
            )

            safeLet(text, textColor) { copy, color ->
                Spacer(modifier = Modifier.width(dimensions.listItemSpacingMediumSmall))

                Text(
                    text = copy,
                    color = if (isEnabled) color else colors.textAccentGrey,
                    style = typography.body
                )
            }
        }
    }
}

@Composable
fun KSSmallBlueButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean,
    radius: Dp? = null
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSSmallButton(
            modifier = modifier,
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.backgroundAccentBlueBold,
            text = text,
            textColor = colors.textInversePrimary,
            radius = radius
        )
    }
}

@Composable
fun KSSmallRedButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean,
    radius: Dp? = null
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSSmallButton(
            modifier = modifier,
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.backgroundDangerBold,
            text = text,
            textColor = colors.textInversePrimary,
            radius = radius
        )
    }
}

@Composable
fun KSSmallWhiteButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean,
    radius: Dp? = null
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeGrey) {
        KSSmallButton(
            modifier = modifier,
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.backgroundInverse,
            text = text,
            textColor = colors.textAccentGreenBold,
            radius = radius
        )
    }
}

@Composable
fun KSSmallButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    isEnabled: Boolean,
    backgroundColor: Color,
    text: String,
    textColor: Color,
    radius: Dp? = null
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            disabledBackgroundColor = colors.backgroundActionDisabled
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.elevation(),
        enabled = isEnabled,
        shape = radius?.let { RoundedCornerShape(radius) } ?: shapes.medium
    ) {
        Text(
            text = text,
            color = if (isEnabled) textColor else colors.textAccentGrey,
            style = typography.buttonText
        )
    }
}

@Composable
fun KSButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    isEnabled: Boolean,
    backgroundColor: Color,
    text: String,
    textStyle: TextStyle = typography.body,
    textColor: Color,
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimensions.minButtonHeight),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            disabledBackgroundColor = colors.backgroundActionDisabled
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.elevation(),
        enabled = isEnabled,
        shape = shapes.medium
    ) {
        leadingIcon()

        Text(
            text = text,
            color = if (isEnabled) textColor else colors.textSecondary,
            style = textStyle
        )
    }
}
