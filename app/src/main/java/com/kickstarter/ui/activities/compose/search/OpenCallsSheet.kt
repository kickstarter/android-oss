package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.mock.factories.TagFactory
import com.kickstarter.models.Tag
import com.kickstarter.ui.compose.designsystem.KSPillButton
import com.kickstarter.ui.compose.designsystem.KSSearchBottomSheetFooter
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors

object OpenCallsTestTags {
    const val SHEET = "open_calls_sheet"
    const val CHIPS = "open_calls_chips"
    const val ALL_CHIP = "open_calls_all_chip"
    fun tagChip(tag: Tag) = "open_calls_chip_${tag.id()}"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OpenCallsSheet(
    tags: List<Tag> = emptyList(),
    currentTag: Tag? = null,
    onDismiss: () -> Unit = {},
    onApply: (Tag?, Boolean?) -> Unit = { _, _ -> },
    onNavigate: () -> Unit = {}
) {
    val dimensions = KSTheme.dimensions
    val selectedTag = remember { mutableStateOf(currentTag) }

    KSTheme {
        Surface(
            modifier = Modifier.testTag(OpenCallsTestTags.SHEET),
            color = colors.backgroundSurfacePrimary
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SearchSheetHeader(
                    title = stringResource(R.string.fpo_Open_calls),
                    onNavigate = onNavigate,
                    onDismiss = onDismiss
                )

                FlowRow(
                    modifier = Modifier
                        .testTag(OpenCallsTestTags.CHIPS)
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
                ) {
                    KSPillButton(
                        text = stringResource(R.string.fpo_Open_calls_all),
                        isSelected = selectedTag.value == null,
                        modifier = Modifier.testTag(OpenCallsTestTags.ALL_CHIP),
                        onClick = {
                            selectedTag.value = null
                            onApply(null, null)
                        }
                    )
                    tags.forEach { tag ->
                        KSPillButton(
                            text = tag.name(),
                            isSelected = selectedTag.value?.id() == tag.id(),
                            modifier = Modifier.testTag(OpenCallsTestTags.tagChip(tag)),
                            onClick = {
                                selectedTag.value = tag
                                onApply(tag, null)
                            }
                        )
                    }
                }

                KSSearchBottomSheetFooter(
                    leftButtonIsEnabled = selectedTag.value != null,
                    leftButtonClickAction = {
                        selectedTag.value = null
                        onApply(null, false)
                    },
                    rightButtonOnClickAction = {
                        onApply(selectedTag.value, true)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun OpenCallsSheetPreview() {
    KSTheme {
        OpenCallsSheet(
            tags = TagFactory.tags()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun OpenCallsSheetSelectedPreview() {
    KSTheme {
        OpenCallsSheet(
            tags = TagFactory.tags(),
            currentTag = TagFactory.witchstarter()
        )
    }
}
