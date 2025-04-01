package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun CategorySelectionSheetPreview() {
    KSTheme {
        FilterMenuBottomSheet(
            selectedProjectStatus = DiscoveryParams.PublicState.LIVE,
            availableFilters = FilterType.values().asList(),
            onApply = {},
            onDismiss = {}
        )
    }
}

enum class FilterType {
    CATEGORIES,
    PROJECT_STATUS,
    LOCATION,
    PERCENTAGE_RAISED,
    AMOUNT_PLEDGED,
    GOAL_RAISED
}

@Composable
fun FilterMenuBottomSheet(
    selectedProjectStatus: DiscoveryParams.PublicState? = null,
    availableFilters: List<FilterType> = emptyList(),
    onDismiss: () -> Unit = {},
    onApply: (DiscoveryParams.State) -> Unit = {}
) {
    val projStatus = remember { mutableStateOf(selectedProjectStatus) }

    KSTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .heightIn(min = 200.dp, max = 770.dp),
            color = colors.backgroundSurfacePrimary
        ) {
            Column {
                FilterRow(callback = onDismiss, icon = Icons.Filled.Close)
                LazyColumn {
                    items(availableFilters) { filter ->
                        when (filter) {
                            FilterType.CATEGORIES ->
                                FilterRow(
                                    text = titleForFilter(filter),
                                    callback = onDismiss,
                                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
                                )
                            FilterType.PROJECT_STATUS ->
                                FilterRow(
                                    text = titleForFilter(filter),
                                    callback = onDismiss,
                                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
                                )
                            FilterType.LOCATION ->
                                FilterRow(
                                    modifier = Modifier.alpha(0f), // Hide those rows for following phases
                                    text = titleForFilter(filter),
                                    callback = onDismiss,
                                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
                                )
                            FilterType.PERCENTAGE_RAISED ->
                                FilterRow(
                                    modifier = Modifier.alpha(0f), // Hide those rows for following phases
                                    text = titleForFilter(filter),
                                    callback = onDismiss,
                                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
                                )
                            FilterType.AMOUNT_PLEDGED ->
                                FilterRow(
                                    modifier = Modifier.alpha(0f), // Hide those rows for following phases
                                    text = titleForFilter(filter),
                                    callback = onDismiss,
                                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
                                )
                            FilterType.GOAL_RAISED ->
                                FilterRow(
                                    modifier = Modifier.alpha(0f), // Hide those rows for following phases
                                    text = titleForFilter(filter),
                                    callback = onDismiss,
                                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
                                )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun titleForFilter(filter: FilterType): String {
    return when (filter) {
        FilterType.CATEGORIES -> stringResource(R.string.Category)
        FilterType.PROJECT_STATUS -> stringResource(R.string.Project_Status_fpo)
        FilterType.LOCATION -> stringResource(R.string.Location)
        FilterType.PERCENTAGE_RAISED -> stringResource(R.string.Percentage_raised_fpo)
        FilterType.AMOUNT_PLEDGED -> stringResource(R.string.Amount_pledged_fpo)
        FilterType.GOAL_RAISED -> stringResource(R.string.Goal_fpo)
    }
}

@Composable
private fun FilterRow(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.Filter_fpo),
    callback: () -> Unit,
    icon: ImageVector
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions
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
            .padding(
                start = dimensions.paddingLarge,
                top = dimensions.paddingLarge,
                bottom = dimensions.paddingLarge,
                end = dimensions.paddingMediumSmall
            ),

        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = typographyV2.headingXL,
            modifier = Modifier.weight(1f),
            color = colors.textPrimary
        )
        IconButton(
            modifier = Modifier.testTag(CategorySelectionSheetTestTag.DISMISS_BUTTON.name),
            onClick = callback
        ) {
            Icon(imageVector = icon, contentDescription = "Close", tint = colors.icon) // TODO: Content description change
        }
    }
}
