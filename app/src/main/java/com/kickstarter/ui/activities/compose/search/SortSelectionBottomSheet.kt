package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.isNullOrZero
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.type.ProjectSort
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions


@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun BetaMessagingBottomSheetPreview() {
    KSTheme {
        SortSelectionBottomSheet(
            sorts = ProjectSort.knownValues(),
        )
    }
}

@Composable
fun SortSelectionBottomSheet(
    sorts: Array<ProjectSort>,
    initialSelection: ProjectSort = ProjectSort.MAGIC,
    onDismiss: (ProjectSort) -> Unit = { },
    isLoading: Boolean = false,
) {
    var selectedOption by remember { mutableStateOf(initialSelection) }

    val onOptionSelected: (ProjectSort) -> Unit = {
        selectedOption = it
    }
            Column(
                modifier = Modifier
                    .background(color = colors.backgroundSurfacePrimary)
                    .padding(start = dimensions.paddingLarge, end = dimensions.paddingSmall, bottom = dimensions.paddingLarge, top = dimensions.alertIconSize)
                    .navigationBarsPadding()
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .padding(bottom = dimensions.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort by",
                        style = KSTheme.typographyV2.headingXL,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        modifier = Modifier.testTag(CategorySelectionSheetTestTag.DISMISS_BUTTON.name),
                        onClick = { onDismiss.invoke(selectedOption) }
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = KSTheme.colors.textPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(sorts.filter { !sortToString(it).isNullOrZero() })
                        { sort ->
                            SortItemRow(
                                sort = sort,
                                isSelected = selectedOption == sort,
                                onSelected = {
                                    onOptionSelected.invoke(sort)
                                    onDismiss.invoke(sort)
                                }
                            )
                        }
                    }
                }
            }
        }

@Composable
fun SortItemRow(
    sort: ProjectSort,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
            sortToString(sort)?.let {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(it),
                    style = KSTheme.typographyV2.headingLG
                )
            }

        RadioButton(
            selected = isSelected,
            onClick = onSelected,
            colors = RadioButtonDefaults.colors(selectedColor = colors.backgroundSelected)
        )
    }
}

fun sortToString(sort : ProjectSort) : Int? {
    return when(sort) {
        ProjectSort.MAGIC -> R.string.Recommended
        ProjectSort.POPULARITY -> R.string.discovery_sort_types_popularity
        ProjectSort.NEWEST -> R.string.discovery_sort_types_newest
        ProjectSort.END_DATE -> R.string.discovery_sort_types_end_date
        ProjectSort.MOST_FUNDED -> R.string.discovery_sort_types_most_funded
        ProjectSort.MOST_BACKED -> R.string.discovery_sort_types_most_backed
        else -> null
    }
}