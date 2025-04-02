package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSSearchBottomSheetFooter
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.PillButton

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

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun FilterMenuSheetPreview() {
    KSTheme {
        FilterMenuBottomSheet(
            selectedProjectStatus = DiscoveryParams.PublicState.LIVE,
            onApply = {},
            onDismiss = {}
        )
    }
}

enum class FilterType {
    CATEGORIES,
    PROJECT_STATUS,
//    LOCATION,
//    PERCENTAGE_RAISED,
//    AMOUNT_PLEDGED,
//    GOAL_RAISED
}

@Composable
fun FilterMenuBottomSheet(
    selectedProjectStatus: DiscoveryParams.PublicState? = null,
    availableFilters: List<FilterType> = FilterType.values().asList(),
    onDismiss: () -> Unit = {},
    onApply: (DiscoveryParams.PublicState?) -> Unit = {}
) {
    val projStatus = remember { mutableStateOf(selectedProjectStatus) }
    Column(
        modifier = Modifier
            .background(color = colors.backgroundSurfacePrimary)
    ) {
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
                        ProjectStatusRow(
                            text = titleForFilter(filter),
                            callback = { status -> projStatus.value = status },
                            selectedStatus = projStatus
                        )
//                    FilterType.LOCATION ->
//                        FilterRow(
//                            text = titleForFilter(filter),
//                            callback = onDismiss,
//                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
//                        )
//                    FilterType.PERCENTAGE_RAISED ->
//                        FilterRow(
//                            text = titleForFilter(filter),
//                            callback = onDismiss,
//                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
//                        )
//                    FilterType.AMOUNT_PLEDGED ->
//                        FilterRow(
//                            text = titleForFilter(filter),
//                            callback = onDismiss,
//                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
//                        )
//                    FilterType.GOAL_RAISED ->
//                        FilterRow(
//                            text = titleForFilter(filter),
//                            callback = onDismiss,
//                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
//                        )
                }
            }
        }

        KSSearchBottomSheetFooter(
            resetOnclickAction = {
                projStatus.value = null // Resets to default value
                onApply(projStatus.value)
            },
            onApply = {
                onApply(projStatus.value)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectStatusRow(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.Filter_fpo),
    selectedStatus: MutableState<DiscoveryParams.PublicState?> = mutableStateOf<DiscoveryParams.PublicState?>(null), // -> Reset button will trigger this piece
    callback: (DiscoveryParams.PublicState?) -> Unit = {},
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions
    var switchChecked by remember { mutableStateOf(false) }

    // - Pill options list
    val pillOptions = listOf(
        null to stringResource(R.string.Project_Status_All_fpo),
        DiscoveryParams.PublicState.LIVE to stringResource(R.string.Project_Status_Live_fpo),
        DiscoveryParams.PublicState.LATE_PLEDGE to stringResource(R.string.Project_Status_Late_Pledges_fpo),
        DiscoveryParams.PublicState.SUCCESSFUL to stringResource(R.string.Project_Status_Successful_fpo), // TODO: might require to be removed waiting on Alison's confirmation
        DiscoveryParams.PublicState.UPCOMING to stringResource(R.string.Project_Status_Upcoming_fpo),
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
        Column(
            verticalArrangement = Arrangement.spacedBy(KSTheme.dimensions.listItemSpacingSmall)
        ) {
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
                        onClick = {
                            val newSelection = if (selectedStatus.value == state) null else state
                            selectedStatus.value = newSelection
                            callback(newSelection)
                        }
                    )
                }
            }

            // TODO: This row might potentially not be necessary waiting for Alison's confirmation
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.Include_ended_projects_fpo),
                    style = typographyV2.body,
                    color = colors.textPrimary
                )

                CustomSwitch(
                    checked = switchChecked,
                    onCheckedChange = {
                        switchChecked = it
                        if (switchChecked) {
                            callback(null)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun titleForFilter(filter: FilterType): String {
    return when (filter) {
        FilterType.CATEGORIES -> stringResource(R.string.Category)
        FilterType.PROJECT_STATUS -> stringResource(R.string.Project_Status_fpo)
//        FilterType.LOCATION -> stringResource(R.string.Location)
//        FilterType.PERCENTAGE_RAISED -> stringResource(R.string.Percentage_raised_fpo)
//        FilterType.AMOUNT_PLEDGED -> stringResource(R.string.Amount_pledged_fpo)
//        FilterType.GOAL_RAISED -> stringResource(R.string.Goal_fpo)
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
        val style =
            if (text == stringResource(R.string.Filter_fpo)) typographyV2.headingXL
            else typographyV2.headingLG

        Text(
            text = text,
            style = style,
            modifier = Modifier.weight(1f),
            color = colors.textPrimary
        )
        IconButton(
            onClick = callback
        ) {
            Icon(imageVector = icon, contentDescription = "Close", tint = colors.icon) // TODO: Content description change
        }
    }
}
