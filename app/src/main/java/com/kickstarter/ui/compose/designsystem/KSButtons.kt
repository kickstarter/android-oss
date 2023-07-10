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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.extensions.safeLet

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSPrimaryButtonsPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_white)
                .padding(all = 8.dp)
        ) {
            KSPrimaryGreenButton(
                onClickAction = {  },
                isEnabled = true,
                text = "Back this project"
            )

            Spacer(modifier = Modifier.height(8.dp))

            KSPrimaryBlueButton(
                onClickAction = {  },
                isEnabled = true,
                text = "Manage your pledge"
            )

            Spacer(modifier = Modifier.height(8.dp))

            KSPrimaryBlackButton(
                onClickAction = {  },
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
                .padding(all = 8.dp)
        ) {
            KSSecondaryGreyButton(
                onClickAction = {  },
                isEnabled = true,
                text = "Fix your pledge"
            )

            Spacer(modifier = Modifier.height(8.dp))

            KSSecondaryWhiteButton(
                onClickAction = {  },
                isEnabled = true,
                text = "Back this project"
            )

            Spacer(modifier = Modifier.height(8.dp))

            KSSecondaryRedButton(
                onClickAction = {  },
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
                .padding(all = 8.dp)
        ) {
            KSPrimaryGreenButton(
                onClickAction = {  },
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
                .padding(all = 8.dp)
        ) {
            KSFacebookButton(
                onClickAction = { },
                text = "Continue with Facebook",
                isEnabled = true
            )

            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            KSSmallRedButton(onClickAction = {}, text = "RED", isEnabled = true)

            Spacer(modifier = Modifier.height(8.dp))

            KSSmallWhiteButton(onClickAction = {}, text = "WHITE", isEnabled = true)
        }
    }
}

@Composable
fun KSPrimaryGreenButton(
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSButton(
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.kds_create_700,
            text = text,
            textColor = colors.kds_white
        )
    }
}

@Composable
fun KSPrimaryBlueButton(
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSButton(
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.kds_trust_500,
            text = text,
            textColor = colors.kds_white
        )
    }
}

@Composable
fun KSPrimaryBlackButton(
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSButton(
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.kds_support_700,
            text = text,
            textColor = colors.kds_white
        )
    }
}

@Composable
fun KSSecondaryGreyButton(
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSButton(
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.kds_support_300,
            text = text,
            textColor = colors.kds_black)
    }
}

@Composable
fun KSSecondaryRedButton(
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSButton(
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.kds_alert,
            text = text,
            textColor = colors.kds_white)
    }
}

@Composable
fun KSSecondaryWhiteButton(
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeGrey) {
        KSButton(
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = colors.kds_white,
            text = text,
            textColor = colors.kds_create_700)
    }
}

@Composable
fun KSFacebookButton(
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSIconButton(
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = facebook_blue,
            imageId = R.drawable.com_facebook_button_icon,
            text = text,
            textColor = kds_white
        )
    }
}

@Composable
fun KSGooglePayButton(
    onClickAction: () -> Unit,
    isEnabled: Boolean
) {
    CompositionLocalProvider(LocalRippleTheme provides KSRippleThemeWhite) {
        KSIconButton(
            onClickAction = onClickAction,
            isEnabled = isEnabled,
            backgroundColor = kds_black,
            imageId = R.drawable.googlepay_button_content
        )
    }
}

@Composable
fun KSIconButton(
    onClickAction: () -> Unit,
    isEnabled: Boolean,
    backgroundColor: Color,
    imageId: Int,
    imageContentDescription: String? = null,
    text: String? = null,
    textColor: Color? = null,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            disabledBackgroundColor = kds_support_300
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.elevation(),
        enabled = isEnabled,
        shape = shapes.medium
    ) {
        Row {
            Image(
                modifier = Modifier.defaultMinSize(minHeight = 24.dp, minWidth = 24.dp),
                painter = painterResource(id = imageId),
                contentDescription = imageContentDescription
            )

            safeLet(text, textColor) { copy, color ->
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = copy,
                    color = if (isEnabled) color else colors.kds_support_400,
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
            backgroundColor = colors.kds_trust_500,
            text = text,
            textColor = colors.kds_white,
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
            backgroundColor = colors.kds_alert,
            text = text,
            textColor = colors.kds_white,
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
            backgroundColor = colors.kds_white,
            text = text,
            textColor = colors.kds_create_700,
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
            disabledBackgroundColor = kds_support_300
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.elevation(),
        enabled = isEnabled,
        shape = radius?.let { RoundedCornerShape(radius) } ?: shapes.medium
    ) {
        Text(
            text = text,
            color = if (isEnabled) textColor else colors.kds_support_400,
            style = typography.buttonText
        )
    }
}

@Composable
fun KSButton(
    onClickAction: () -> Unit,
    isEnabled: Boolean,
    backgroundColor: Color,
    text: String,
    textColor: Color,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            disabledBackgroundColor = kds_support_300
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.elevation(),
        enabled = isEnabled,
        shape = shapes.medium
    ) {
        Text(
            text = text,
            color = if (isEnabled) textColor else colors.kds_support_400,
            style = typography.body
        )
    }
}