package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSSearchBottomSheetFooter
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.PillButton

object FilterMenuTestTags {
    const val SHEET = "filter_menu_sheet"
    const val DISMISS_ROW = "dismiss_row"
    const val CATEGORY_ROW = "category_filter_row"
    const val PROJECT_STATUS_ROW = "project_status_row"
    const val FOOTER = "footer"

    fun pillTag(state: DiscoveryParams.State?) = "pill_${state?.name ?: "ALL"}"
}

enum class FilterType {
    CATEGORIES,
    PROJECT_STATUS,
    PERCENTAGE_RAISED
}

@Composable
fun FilterMenuBottomSheet(
    selectedProjectStatus: DiscoveryParams.State? = null,
    availableFilters: List<FilterType> = FilterType.values().asList(),
    onDismiss: () -> Unit = {},
    onApply: (DiscoveryParams.State?, Boolean?) -> Unit = { a, b -> },
    onNavigate: (FilterType) -> Unit = {}
) {
    val projStatus = remember { mutableStateOf(selectedProjectStatus) }

    Surface(
        modifier = Modifier
            .testTag(FilterMenuTestTags.SHEET),
        color = colors.backgroundSurfacePrimary
    ) {
        Column(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary)
        ) {
            FilterRow(
                onClickAction = onDismiss,
                icon = Icons.Filled.Close,
                modifier = Modifier.testTag(FilterMenuTestTags.DISMISS_ROW)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(availableFilters) { filter ->
                    when (filter) {
                        FilterType.CATEGORIES -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.CATEGORIES) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.CATEGORY_ROW)
                        )
                        FilterType.PROJECT_STATUS -> ProjectStatusRow(
                            text = titleForFilter(filter),
                            callback = { status ->
                                projStatus.value = status
                                onApply(projStatus.value, null)
                            },
                            selectedStatus = projStatus,
                            modifier = Modifier.testTag(FilterMenuTestTags.PROJECT_STATUS_ROW)
                        )
                        FilterType.PERCENTAGE_RAISED -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.PERCENTAGE_RAISED) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.CATEGORY_ROW)
                        )
                    }
                }
            }

            KSSearchBottomSheetFooter(
                modifier = Modifier.testTag(FilterMenuTestTags.FOOTER),
                leftButtonIsEnabled = projStatus.value.isNotNull(),
                leftButtonClickAction = {
                    projStatus.value = null
                    onApply(projStatus.value, false)
                },
                rightButtonOnClickAction = {
                    onApply(projStatus.value, true)
                },
                leftButtonText = stringResource(R.string.Reset_all_filters)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectStatusRow(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.Filter),
    selectedStatus: MutableState<DiscoveryParams.State?> = mutableStateOf(null),
    callback: (DiscoveryParams.State?) -> Unit = {},
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions
    var switchChecked by remember { mutableStateOf(false) }

    val pillOptions = listOf(
        null to stringResource(R.string.Project_status_all),
        DiscoveryParams.State.LIVE to stringResource(R.string.Project_status_live),
        DiscoveryParams.State.LATE_PLEDGES to stringResource(R.string.Project_status_late_pledge),
        DiscoveryParams.State.UPCOMING to stringResource(R.string.Project_status_upcoming),
        DiscoveryParams.State.SUCCESSFUL to stringResource(R.string.Project_status_successful)
    )

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
            )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(KSTheme.dimensions.listItemSpacingSmall)) {
            Text(
                text = text,
                style = typographyV2.headingLG,
                color = colors.textPrimary
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pillOptions.forEach { (state, label) ->
                    PillButton(
                        text = label,
                        shouldShowIcon = false,
                        isSelected = selectedStatus.value == state,
                        modifier = Modifier.testTag(FilterMenuTestTags.pillTag(state)),
                        onClick = {
                            val newSelection = if (selectedStatus.value == state) null else state
                            selectedStatus.value = newSelection
                            callback(newSelection)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.height(0.dp), // placeholder for future API support
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.Include_ended_projects),
                    style = typographyV2.body,
                    color = colors.textPrimary
                )

                CustomSwitch(
                    checked = switchChecked,
                    onCheckedChange = {
                        switchChecked = it
                        if (switchChecked) {
                            callback(DiscoveryParams.State.SUCCESSFUL)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterRow(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.Filter),
    onClickAction: () -> Unit,
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
        val style = if (text == stringResource(R.string.Filter)) {
            typographyV2.headingXL
        } else {
            typographyV2.headingLG
        }

        Text(
            text = text,
            style = style,
            modifier = Modifier.weight(1f),
            color = colors.textPrimary
        )
        IconButton(
            modifier = Modifier.testTag(text),
            onClick = {
                onClickAction.invoke()
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Dismiss Filter Menu",
                tint = colors.icon
            )
        }
    }
}

@Composable
private fun titleForFilter(filter: FilterType): String {
    return when (filter) {
        FilterType.CATEGORIES -> stringResource(R.string.Category)
        FilterType.PROJECT_STATUS -> stringResource(R.string.Project_status)
        FilterType.PERCENTAGE_RAISED -> stringResource(R.string.Percentage_raised_fpo)
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ProjectStatusRowPreview() {
    KSTheme {
        ProjectStatusRow(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
            text = titleForFilter(FilterType.PROJECT_STATUS)
        )
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FilterMenuSheetPreview() {
    KSTheme {
        FilterMenuBottomSheet(
            selectedProjectStatus = DiscoveryParams.State.LIVE,
            onApply = { a, b -> },
            onDismiss = {}
        )
    }
}
