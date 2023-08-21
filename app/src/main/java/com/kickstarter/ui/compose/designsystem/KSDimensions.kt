package com.kickstarter.ui.compose.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class KSDimensions(
    val dividerThickness: Dp = Dp.Unspecified,
    val none: Dp = 0.dp,
    val paddingSmall: Dp = Dp.Unspecified,
    val paddingMediumSmall: Dp = Dp.Unspecified,
    val paddingMedium: Dp = Dp.Unspecified,
    val paddingMediumLarge: Dp = Dp.Unspecified,
    val paddingLarge: Dp = Dp.Unspecified,
    val paddingXLarge: Dp = Dp.Unspecified,
    val paddingXXLarge: Dp = Dp.Unspecified,
    val paddingDoubleLarge: Dp = Dp.Unspecified,
    val minButtonHeight: Dp = Dp.Unspecified,
    val stepperHeight: Dp = Dp.Unspecified,
    val stepperWidth: Dp = Dp.Unspecified,
    val stepperButtonWidth: Dp = Dp.Unspecified,
    val footerHeight: Dp = Dp.Unspecified,
    val borderThickness: Dp = Dp.Unspecified,
    val listItemSpacingSmall: Dp = Dp.Unspecified,
    val listItemSpacingMediumSmall: Dp = Dp.Unspecified,
    val listItemSpacingMedium: Dp = Dp.Unspecified,
    val listItemSpacingLarge: Dp = Dp.Unspecified,
    val radiusSmall: Dp = Dp.Unspecified,
    val radiusMediumSmall: Dp = Dp.Unspecified,
    val radiusMedium: Dp = Dp.Unspecified,
    val radiusMediumLarge: Dp = Dp.Unspecified,
    val radiusLarge: Dp = Dp.Unspecified,
    val textInputTopPadding: Dp = Dp.Unspecified,
    val dropDownStandardWidth: Dp = Dp.Unspecified,
    val dropDownMenuImageSize: Dp = Dp.Unspecified,
    val imageSizeMedium: Dp = Dp.Unspecified,
    val imageSizeLarge: Dp = Dp.Unspecified,
    val dialogWidth: Dp = Dp.Unspecified,
    val dialogButtonSpacing: Dp = Dp.Unspecified,
    val elevationMedium: Dp = Dp.Unspecified,
    val assistiveTextTopSpacing: Dp = Dp.Unspecified,
    val verticalDividerWidth: Dp = Dp.Unspecified,
    val iconSizeMedium: Dp = Dp.Unspecified
)

val LocalKSCustomDimensions = staticCompositionLocalOf {
    KSDimensions()
}

val KSStandardDimensions = KSDimensions(
    dividerThickness = 1.dp,
    paddingSmall = 8.dp,
    paddingMediumSmall = 12.dp,
    paddingMedium = 16.dp,
    paddingMediumLarge = 18.dp,
    paddingLarge = 24.dp,
    paddingXLarge = 32.dp,
    paddingXXLarge = 40.dp,
    paddingDoubleLarge = 48.dp,
    minButtonHeight = 48.dp,
    stepperHeight = 36.dp,
    stepperWidth = 108.dp,
    stepperButtonWidth = 54.dp,
    footerHeight = 132.dp,
    borderThickness = 1.dp,
    listItemSpacingSmall = 8.dp,
    listItemSpacingMediumSmall = 12.dp,
    listItemSpacingMedium = 16.dp,
    listItemSpacingLarge = 24.dp,
    radiusSmall = 6.dp,
    radiusMediumSmall = 9.dp,
    radiusMedium = 12.dp,
    radiusMediumLarge = 16.dp,
    radiusLarge = 18.dp,
    textInputTopPadding = 6.dp,
    dropDownStandardWidth = 150.dp,
    dropDownMenuImageSize = 12.dp,
    imageSizeMedium = 24.dp,
    imageSizeLarge = 32.dp,
    dialogWidth = 280.dp,
    dialogButtonSpacing = 2.dp,
    elevationMedium = 8.dp,
    assistiveTextTopSpacing = 6.dp,
    verticalDividerWidth = 4.dp,
    iconSizeMedium = 18.dp
)
