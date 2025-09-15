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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.kickstarter.ui.activities.compose.search.FilterRowPillType
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

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
        KSSearchToolbarButtonsColumn()
    }
}

@Composable
fun KSSearchToolbarButtonsColumn() {
    Column(
        modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
    ) {
        Row {
            KSIconButton(onClick = {}, imageVector = Icons.Filled.Close)
        }
        Row {
            KSIconPillButton(type = FilterRowPillType.SORT, isSelected = false)
            KSIconPillButton(type = FilterRowPillType.SORT, isSelected = true)
            KSIconPillButton(type = FilterRowPillType.FILTER, isSelected = true)
            KSIconPillButton(type = FilterRowPillType.FILTER, isSelected = false)
        }
        Row {
            KSIconPillButton(type = FilterRowPillType.SORT, isSelected = true, count = 3)
            KSIconPillButton(type = FilterRowPillType.SORT, isSelected = false, count = 3)
            KSIconPillButton(type = FilterRowPillType.FILTER, isSelected = true, count = 3)
            KSIconPillButton(type = FilterRowPillType.FILTER, isSelected = false, count = 3)
        }
        Row {
            KSPillButton(countApiIsReady = false, text = "Category", isSelected = false, count = 0, onClick = {})
            KSPillButton(countApiIsReady = false, text = "Art", isSelected = true, count = 0, onClick = {})
        }
        Row {
            KSPillButton(countApiIsReady = true, text = "Late Pledges", isSelected = false, count = 30, onClick = {}, shouldShowTrailingIcon = false)
            KSPillButton(countApiIsReady = true, text = "Late Pledges", isSelected = true, count = 30, onClick = {}, shouldShowTrailingIcon = false)
        }
        Row {
            KSPillButton(countApiIsReady = false, text = "Late Pledges", isSelected = true, count = 0, onClick = {}, shouldShowTrailingIcon = false)
            KSPillButton(countApiIsReady = false, text = "Late Pledges", isSelected = false, count = 0, onClick = {}, shouldShowTrailingIcon = false)
        }
        Row {
            KSPillButton(countApiIsReady = false, text = "Late Pledges", isSelected = true, count = 0, onClick = {}, shouldShowTrailingIcon = true)
            KSPillButton(countApiIsReady = false, text = "Late Pledges", isSelected = false, count = 0, onClick = {}, shouldShowTrailingIcon = true)
        }
        Row {
            KSPillButton(countApiIsReady = false, text = "Projects We Love", isSelected = true, count = 0, onClick = {}, shouldShowLeadingIcon = true)
            KSPillButton(countApiIsReady = false, text = "Projects We Love", isSelected = false, count = 0, onClick = {}, shouldShowLeadingIcon = true)
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSOutlinedButtons() {
    KSTheme {
        Column(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
        ) {
            KSOutlinedButton(
                backgroundColor = colors.backgroundSurfacePrimary,
                textColor = colors.textPrimary,
                onClickAction = {},
                text = "OutlinedButton",
                isEnabled = true
            )

            KSOutlinedButton(
                backgroundColor = colors.backgroundSurfacePrimary,
                textColor = colors.textPrimary,
                onClickAction = {},
                text = "OutlinedButton Disabled",
                isEnabled = false
            )
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

@Composable
fun KSPrimaryBlueButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typographyV2.body,
    isEnabled: Boolean
) {
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

@Composable
fun KSPrimaryBlackButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typographyV2.body,
    isEnabled: Boolean
) {
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

@Composable
fun KSSecondaryGreyButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typographyV2.body,
    isEnabled: Boolean
) {
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

@Composable
fun KSSecondaryRedButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typographyV2.body,
    isEnabled: Boolean
) {
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

@Composable
fun KSSecondaryWhiteButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typographyV2.body,
    isEnabled: Boolean
) {
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

@Composable
fun KSFacebookButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean
) {
    KSIconTextButton(
        modifier = modifier,
        onClickAction = onClickAction,
        isEnabled = isEnabled,
        backgroundColor = colors.facebook_blue,
        imageId = R.drawable.com_facebook_button_icon,
        text = text,
        textColor = kds_white
    )
}

@Composable
fun KSGooglePayButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    isEnabled: Boolean
) {
    KSIconTextButton(
        modifier = modifier,
        onClickAction = onClickAction,
        isEnabled = isEnabled,
        backgroundColor = kds_black,
        imageId = R.drawable.googlepay_button_content
    )
}

@Composable
fun KSIconTextButton(
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
            containerColor = backgroundColor,
            disabledContainerColor = colors.backgroundActionDisabled
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.buttonElevation(),
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

@Composable
fun KSSmallRedButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean,
    radius: Dp? = null
) {
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

@Composable
fun KSSmallWhiteButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean,
    radius: Dp? = null
) {
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
            containerColor = backgroundColor,
            disabledContainerColor = colors.backgroundActionDisabled
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.buttonElevation(),
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
fun KSIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    imageVector: ImageVector = Icons.Filled.Close,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(imageVector = imageVector, contentDescription = contentDescription, tint = colors.icon)
    }
}

@Composable
fun KSIconPillButton(
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
fun KSPillButton(
    modifier: Modifier = Modifier,
    countApiIsReady: Boolean = false,
    text: String,
    isSelected: Boolean = false,
    count: Int = 0,
    onClick: () -> Unit,
    iconTrailing: ImageVector = Icons.Filled.KeyboardArrowDown,
    shouldShowTrailingIcon: Boolean = false,
    shouldShowLeadingIcon: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = colors.textAccentGrey
        ),
        border = if (isSelected) BorderStroke(
            dimensions.strokeWidth,
            colors.borderActive
        ) else BorderStroke(dimensions.borderThickness, colors.borderBold),
        shape = RoundedCornerShape(dimensions.pillButtonShapeSize),
        elevation = ButtonDefaults.buttonElevation(dimensions.none, dimensions.none, dimensions.none)
    ) {
        if (shouldShowLeadingIcon) {
            Icon(
                modifier = Modifier.padding(end = dimensions.paddingSmall),
                contentDescription = null,
                painter = painterResource(id = R.drawable.projectswelove),
                tint = Color.Unspecified
            )
        }
        Text(
            modifier = Modifier.padding(end = dimensions.paddingSmall),
            text = text,
            style = typographyV2.buttonLabel,
            color = colors.textAccentGrey
        )
        if (countApiIsReady && count > 0) {
            KSCountBadge(count)
        }
        if (shouldShowTrailingIcon) {
            Icon(
                imageVector = iconTrailing,
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
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        enabled = isEnabled,
        border = BorderStroke(dimensions.borderThickness, colors.borderBold),
        onClick = { onClickAction.invoke() }
    ) {
        Text(
            style = typographyV2.buttonLabel,
            color = if (isEnabled) textColor else textColor.copy(alpha = 0.38f),
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
    shouldWrapContentWidth: Boolean = false
) {
    val mod = if (shouldWrapContentWidth)
        modifier.wrapContentWidth()
    else modifier.fillMaxWidth()
    Button(
        modifier = mod
            .defaultMinSize(minHeight = dimensions.minButtonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = colors.backgroundActionDisabled
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.buttonElevation(),
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
