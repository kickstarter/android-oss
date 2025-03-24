package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun SortSelectionBottomSheetPreview() {
    KSTheme {
        SortSelectionBottomSheet(
            sorts = DiscoveryParams.Sort.values().toList(),
        )
    }
}

@Composable
fun SortSelectionBottomSheet(
    sorts: List<DiscoveryParams.Sort>,
    currentSelection: DiscoveryParams.Sort = DiscoveryParams.Sort.MAGIC,
    onDismiss: (DiscoveryParams.Sort) -> Unit = { },
    isLoading: Boolean = false,
) {
    var selectedOption by remember { mutableStateOf(currentSelection) }

    val onOptionSelected: (DiscoveryParams.Sort) -> Unit = {
        selectedOption = it
    }
    Column(
        modifier = Modifier
            .background(color = colors.backgroundSurfacePrimary)
            .padding(start = dimensions.paddingLarge, end = dimensions.paddingMediumSmall, bottom = dimensions.paddingLarge, top = dimensions.alertIconSize)
            .navigationBarsPadding()
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = dimensions.paddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.fpo_sort_by),
                color = colors.textPrimary,
                style = KSTheme.typographyV2.headingXL,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                modifier = Modifier.testTag(SortSelectionBottomSheetTestTag.DISMISS_BUTTON.name),
                onClick = { onDismiss.invoke(selectedOption) }
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = colors.textPrimary)
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
                items(sorts.filter { !getSortString(it).isNullOrZero() }) { sort ->
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
    sort: DiscoveryParams.Sort,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        modifier = Modifier.clickable { onSelected.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        getSortString(sort)?.let {
            Text(
                modifier = Modifier.weight(1f),
                color = colors.textPrimary,
                text = stringResource(it),
                style = KSTheme.typographyV2.headingLG
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onSelected,
            colors = RadioButtonDefaults.colors(unselectedColor = colors.backgroundSelected, selectedColor = colors.backgroundSelected)
        )
    }
}

fun getSortString(sort: DiscoveryParams.Sort): Int? {
    // omit distance until api is ready
    return when (sort) {
        DiscoveryParams.Sort.MAGIC -> R.string.Recommended
        DiscoveryParams.Sort.POPULAR -> R.string.discovery_sort_types_popularity
        DiscoveryParams.Sort.NEWEST -> R.string.discovery_sort_types_newest
        DiscoveryParams.Sort.ENDING_SOON -> R.string.discovery_sort_types_end_date
        DiscoveryParams.Sort.MOST_FUNDED -> R.string.discovery_sort_types_most_funded
        DiscoveryParams.Sort.MOST_BACKED -> R.string.discovery_sort_types_most_backed
        else -> null
    }
}

enum class SortSelectionBottomSheetTestTag {
    DISMISS_BUTTON,
}
