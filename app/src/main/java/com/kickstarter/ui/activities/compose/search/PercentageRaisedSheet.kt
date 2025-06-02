package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.activities.compose.search.PercentageRaisedTestTags.BUCKETS_LIST
import com.kickstarter.ui.activities.compose.search.PercentageRaisedTestTags.bucketTag
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSIconButton
import com.kickstarter.ui.compose.designsystem.KSSearchBottomSheetFooter
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun CategoryRowSelectedWithSubcategoriesPills() {
    KSTheme {
        PercentageRaisedSheet(
            onNavigate = {},
            onDismiss = {},
            onApply = { bucket, applyAndDismiss ->
            }
        )
    }
}

object PercentageRaisedTestTags {
    const val BUCKETS_LIST = "buckets_list"
    fun bucketTag(bucket: DiscoveryParams.RaisedBuckets) = "bucket_${bucket.name}"
}

@Composable
fun PercentageRaisedSheet(
    currentPercentage: DiscoveryParams.RaisedBuckets? = null,
    onDismiss: () -> Unit = {},
    onApply: (DiscoveryParams.RaisedBuckets?, Boolean?) -> Unit = { a, b -> },
    onNavigate: () -> Unit = {},
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    val selectedPercentage = remember { mutableStateOf(currentPercentage) }

    KSTheme {
        Surface(
            color = colors.backgroundSurfacePrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // TODO: extract this row as a title re-usable composable can be used with Category as well same UI, just changes the title
                Row(
                    modifier = Modifier
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
                        text = stringResource(R.string.Percentage_raised),
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

                LazyColumn(
                    modifier = Modifier
                        .testTag(BUCKETS_LIST)
                        .fillMaxWidth()
                        .weight(1f)
                        .drawBehind {
                            drawLine(
                                color = backgroundDisabledColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = dimensions.dividerThickness.toPx()
                            )
                        }
                        .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium),
                ) {
                    val validBuckets = DiscoveryParams.RaisedBuckets.values()
                    items(validBuckets) { bucket ->
                        Row(
                            modifier = Modifier.testTag(bucketTag(bucket))
                                .padding(top = dimensions.paddingMediumSmall, bottom = dimensions.paddingMediumSmall)
                                .clickable {
                                    selectedPercentage.value = bucket
                                    onApply(selectedPercentage.value, null)
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
                        ) {
                            RadioButton(
                                selected = selectedPercentage.value == bucket,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(unselectedColor = colors.backgroundSelected, selectedColor = colors.backgroundSelected)
                            )
                            Text(
                                modifier = Modifier
                                    .weight(1f),
                                color = colors.textPrimary,
                                text = textForBucket(bucket),
                                style = typographyV2.headingLG
                            )
                        }
                    }
                }

                KSSearchBottomSheetFooter(
                    leftButtonIsEnabled = selectedPercentage.value != null,
                    leftButtonClickAction = {
                        selectedPercentage.value = null
                        onApply(selectedPercentage.value, false)
                    },
                    rightButtonOnClickAction = {
                        onApply(selectedPercentage.value, true)
                    }
                )
            }
        }
    }
}

@Composable
fun textForBucket(bucket: DiscoveryParams.RaisedBuckets) = when (bucket) {
    DiscoveryParams.RaisedBuckets.BUCKET_2 -> stringResource(R.string.Percentage_raised_bucket_2)
    DiscoveryParams.RaisedBuckets.BUCKET_1 -> stringResource(R.string.Percentage_raised_bucket_1)
    DiscoveryParams.RaisedBuckets.BUCKET_0 -> stringResource(R.string.Percentage_raised_bucket_0)
    else -> ""
}
