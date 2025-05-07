package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.kickstarter.R
import com.kickstarter.libs.utils.safeLet
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.views.compose.search.FilterRowPillType

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
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSSearchToolbarButtons() {
    KSTheme {
        Column(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
        ) {
            IconPillButton(type = FilterRowPillType.SORT, isSelected = false)
            IconPillButton(type = FilterRowPillType.SORT, isSelected = true)
            IconPillButton(type = FilterRowPillType.FILTER, isSelected = true)
            IconPillButton(type = FilterRowPillType.FILTER, isSelected = false)
            IconPillButton(type = FilterRowPillType.SORT, isSelected = true, count = 3)
            IconPillButton(type = FilterRowPillType.SORT, isSelected = false, count = 3)
            IconPillButton(type = FilterRowPillType.FILTER, isSelected = true, count = 3)
            IconPillButton(type = FilterRowPillType.FILTER, isSelected = false, count = 3)
            PillButton(countApiIsReady = false, text = "Category", isSelected = false, count = 0, onClick = {})
            PillButton(countApiIsReady = false, text = "Art", isSelected = true, count = 0, onClick = {})
            PillButton(countApiIsReady = true, text = "Late Pledges", isSelected = false, count = 30, onClick = {}, shouldShowIcon = false)
            PillButton(countApiIsReady = true, text = "Late Pledges", isSelected = true, count = 30, onClick = {}, shouldShowIcon = false)
            PillButton(countApiIsReady = false, text = "Late Pledges", isSelected = true, count = 0, onClick = {}, shouldShowIcon = false)
            PillButton(countApiIsReady = false, text = "Late Pledges", isSelected = false, count = 0, onClick = {}, shouldShowIcon = false)
            PillButton(countApiIsReady = false, text = "% Raised", isSelected = false, count = 0, onClick = {})
        }
    }
}

@Composable
fun KSPrimaryGreenButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typographyV2.body,
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
    textStyle: TextStyle = typographyV2.body,
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
    textStyle: TextStyle = typographyV2.body,
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
    textStyle: TextStyle = typographyV2.body,
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
    textStyle: TextStyle = typographyV2.body,
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
    textStyle: TextStyle = typographyV2.body,
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
                    style = typographyV2.body
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
            style = typographyV2.buttonLabel
        )
    }
}

@Composable
fun IconPillButton(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit = {},
    type: FilterRowPillType,
    count: Int = 0
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .width(if (count > 0) dimensions.iconPillButtonSizeLarge else dimensions.iconPillButtonSize)
            .border(
                if (isSelected) dimensions.strokeWidth else dimensions.borderThickness,
                if (isSelected) colors.borderActive else colors.borderBold,
                RoundedCornerShape(dimensions.pillButtonShapeSize)
            )
            .size(dimensions.iconPillButtonSize)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterForFilterType(type),
                contentDescription = descriptionForFilterType(type),
                tint = colors.icon
            )
            if (count > 0) {
                Spacer(Modifier.padding(start = dimensions.paddingXSmall))
                KSCountBadge(count)
            }
        }
    }
}

@Composable
private fun descriptionForFilterType(type: FilterRowPillType): String {
    return when (type) {
        FilterRowPillType.SORT -> stringResource(R.string.Sort_by)
        FilterRowPillType.FILTER -> stringResource(R.string.Filter)
        else -> stringResource(R.string.Sort_by)
    }
}

@Composable
private fun painterForFilterType(type: FilterRowPillType): Painter {
    return when (type) {
        FilterRowPillType.SORT -> painterResource(id = R.drawable.ic_sort)
        FilterRowPillType.FILTER -> painterResource(id = R.drawable.ic_filter)
        else -> painterResource(id = R.drawable.ic_sort)
    }
}

@Composable
fun PillButton(
    modifier: Modifier = Modifier,
    countApiIsReady: Boolean = false,
    text: String,
    isSelected: Boolean = false,
    count: Int = 0,
    onClick: () -> Unit,
    icon: ImageVector = Icons.Filled.KeyboardArrowDown,
    shouldShowIcon: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = Color.Transparent,
            contentColor = colors.textAccentGrey
        ),
        border = if (isSelected) BorderStroke(
            dimensions.strokeWidth,
            colors.borderActive
        ) else BorderStroke(dimensions.borderThickness, colors.borderBold),
        shape = RoundedCornerShape(dimensions.pillButtonShapeSize),
        elevation = ButtonDefaults.elevation(dimensions.none, dimensions.none, dimensions.none)
    ) {
        Text(
            modifier = Modifier.padding(end = dimensions.paddingSmall),
            text = text,
            style = typographyV2.buttonLabel,
            color = colors.textAccentGrey
        )
        if (countApiIsReady && count > 0) {
            KSCountBadge(count)
        }
        if (shouldShowIcon) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = colors.icon
            )
        }
    }
}

@Composable
fun KSOutlinedButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    backgroundColor: Color,
    text: String,
    textColor: Color = colors.textPrimary,
    isEnabled: Boolean = true,
) {
    OutlinedButton(
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = backgroundColor,
            contentColor = textColor
        ),
        enabled = isEnabled,
        border = BorderStroke(dimensions.borderThickness, colors.borderBold),
        onClick = { onClickAction.invoke() }
    ) {
        Text(
            style = typographyV2.buttonLabel,
            color = if (isEnabled) textColor else textColor.copy(alpha = ContentAlpha.disabled),
            text = text
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
    textStyle: TextStyle = typographyV2.body,
    textColor: Color,
    shape: RoundedCornerShape? = null,
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
        shape = shape ?: shapes.medium
    ) {
        leadingIcon()

        Text(
            text = text,
            color = if (isEnabled) textColor else colors.textSecondary,
            style = textStyle
        )
    }
}
