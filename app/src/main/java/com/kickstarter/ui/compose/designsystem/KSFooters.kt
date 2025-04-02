package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun FullButtonFooterPreview() {
    KSTheme {
        Column(modifier = Modifier.background(color = colors.kds_support_500)) {
            KSFullButtonFooter(buttonText = "Back this project", onClickAction = {})
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SmallButtonFooterPreview() {
    KSTheme {
        Column(modifier = Modifier.background(color = colors.kds_support_500)) {
            KSSmallButtonFooter(
                buttonText = "Manage",
                onClickAction = {},
                titleText = "You're a backer",
                subtitleText = "$24 Committed"
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchBottomSheetFooterPreview() {
    KSTheme {
        Column {
            KSSearchBottomSheetFooter()
        }
    }
}

@Composable
fun KSFullButtonFooter(
    buttonText: String,
    onClickAction: () -> Unit,
    enabled: Boolean = true
) {
    KSStandardFooter {
        KSPrimaryGreenButton(onClickAction = onClickAction, text = buttonText, isEnabled = enabled)
    }
}

@Composable
fun KSSmallButtonFooter(
    buttonText: String,
    onClickAction: () -> Unit,
    enabled: Boolean = true,
    titleText: String,
    subtitleText: String
) {
    KSStandardFooter {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = titleText,
                    style = typographyV2.headingXL,
                    color = colors.kds_support_700
                )
                Text(
                    text = subtitleText,
                    style = typographyV2.headingSM,
                    color = colors.kds_support_700
                )
            }

            KSSmallBlueButton(
                modifier = Modifier.height(dimensions.minButtonHeight),
                onClickAction = onClickAction,
                text = buttonText,
                isEnabled = enabled,
                radius = dimensions.radiusMedium
            )
        }
    }
}

@Composable
fun KSStandardFooter(
    content: @Composable () -> Unit
) {
    Surface(
        color = Color.Transparent,
        elevation = dimensions.elevationMedium,
        shape = RoundedCornerShape(
            topStart = dimensions.radiusLarge,
            topEnd = dimensions.radiusLarge
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.footerHeight)
                .background(
                    color = colors.kds_white,
                    shape = RoundedCornerShape(
                        topStart = dimensions.radiusLarge,
                        topEnd = dimensions.radiusLarge
                    )
                )
                .padding(dimensions.paddingMedium)
        ) {
            content()
        }
    }
}

enum class BottomSheetFooterTestTags {
    RESET,
    SEE_RESULTS
}
@Composable
fun KSSearchBottomSheetFooter(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    resetOnclickAction: () -> Unit = {},
    onApply: () -> Unit = {}
) {

    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensions.searchBottomSheetFooter)
            .drawBehind {
                drawLine(
                    color = backgroundDisabledColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = dimensions.dividerThickness.toPx()
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(dimensions.paddingLarge),
            horizontalArrangement = Arrangement.spacedBy(dimensions.paddingMedium),
        ) {
            KSOutlinedButton(
                modifier = Modifier
                    .defaultMinSize(minHeight = dimensions.minButtonHeight)
                    .testTag(BottomSheetFooterTestTags.RESET.name),
                backgroundColor = colors.backgroundSurfacePrimary,
                textColor = colors.textPrimary,
                onClickAction = {
                    resetOnclickAction.invoke()
                },
                text = stringResource(R.string.Reset_filters),
                isEnabled = !isLoading
            )
            KSButton(
                modifier = Modifier
                    .weight(1f)
                    .testTag(BottomSheetFooterTestTags.SEE_RESULTS.name),
                backgroundColor = colors.kds_black,
                textColor = colors.kds_white,
                onClickAction = {
                    onApply.invoke()
                },
                shape = RoundedCornerShape(size = KSTheme.dimensions.radiusExtraSmall),
                text = stringResource(R.string.See_results),
                textStyle = typographyV2.buttonLabel,
                isEnabled = !isLoading,
            )
        }
    }
}
