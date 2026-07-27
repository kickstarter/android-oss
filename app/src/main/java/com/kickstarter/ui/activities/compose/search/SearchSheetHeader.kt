package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSIconButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * Shared header row used across the search filter sub-sheets (Category, Location, Percentage
 * Raised, Amount Raised, Goal, Open Calls): a back button, the sheet [title], and a close button,
 * with a divider drawn along the bottom edge. Only the title changes between sheets.
 */
@Composable
fun SearchSheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    onNavigate: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val dimensions = KSTheme.dimensions
    val backgroundDisabledColor = colors.backgroundDisabled

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = backgroundDisabledColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = dimensions.dividerThickness.toPx()
                )
            }
            .padding(top = dimensions.paddingLarge, bottom = dimensions.paddingLarge, end = dimensions.paddingMediumSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KSIconButton(
            modifier = Modifier
                .padding(start = dimensions.paddingSmall)
                .testTag(SearchScreenTestTag.BACK_BUTTON.name),
            onClick = onNavigate,
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.Back)
        )

        Text(
            text = title,
            style = typographyV2.headingXL,
            modifier = Modifier.weight(1f),
            color = colors.textPrimary
        )

        KSIconButton(
            modifier = Modifier.testTag(CategorySelectionSheetTestTag.DISMISS_BUTTON.name),
            onClick = onDismiss,
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(id = R.string.accessibility_discovery_buttons_close)
        )
    }
}
