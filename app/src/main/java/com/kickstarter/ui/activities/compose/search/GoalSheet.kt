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
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSIconButton
import com.kickstarter.ui.compose.designsystem.KSSearchBottomSheetFooter
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GoalSheetPreview() {
    KSTheme {
        GoalSheet(
            onNavigate = {},
            onDismiss = {},
            onApply = { bucket, applyAndDismiss ->
            }
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GoalSelectedPreview() {
    KSTheme {
        GoalSheet(
            currentGoalBucket = DiscoveryParams.GoalBuckets.BUCKET_2,
            onNavigate = {},
            onDismiss = {},
            onApply = { bucket, applyAndDismiss ->
            }
        )
    }
}

object GoalTestTags {
    const val BUCKETS_LIST = "goal_buckets_list"
    fun bucketTag(bucket: DiscoveryParams.GoalBuckets) = "bucket_${bucket.name}"
}

@Composable
fun GoalSheet(
    currentGoalBucket: DiscoveryParams.GoalBuckets? = null,
    onDismiss: () -> Unit = {},
    onApply: (DiscoveryParams.GoalBuckets?, Boolean?) -> Unit = { _, _ -> },
    onNavigate: () -> Unit = {}
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    val selectedGoalBucket = remember { mutableStateOf(currentGoalBucket) }

    KSTheme {
        Surface(color = colors.backgroundSurfacePrimary) {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                            .padding(start = dimensions.paddingSmall),
                        onClick = onNavigate,
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.Back)
                    )

                    Text(
                        text = stringResource(R.string.Goal_fpo),
                        style = typographyV2.headingXL,
                        modifier = Modifier.weight(1f),
                        color = colors.textPrimary
                    )

                    KSIconButton(
                        onClick = onDismiss,
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(id = R.string.accessibility_discovery_buttons_close)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .testTag(GoalTestTags.BUCKETS_LIST)
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
                        .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium)
                ) {
                    val validBuckets = DiscoveryParams.GoalBuckets.values()
                    items(validBuckets) { bucket ->
                        Row(
                            modifier = Modifier
                                .testTag(GoalTestTags.bucketTag(bucket))
                                .padding(top = dimensions.paddingMediumSmall, bottom = dimensions.paddingMediumSmall)
                                .clickable {
                                    selectedGoalBucket.value = bucket
                                    onApply(selectedGoalBucket.value, null)
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
                        ) {
                            RadioButton(
                                selected = selectedGoalBucket.value == bucket,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    unselectedColor = colors.backgroundSelected,
                                    selectedColor = colors.backgroundSelected
                                )
                            )
                            Text(
                                modifier = Modifier.weight(1f),
                                color = colors.textPrimary,
                                text = textForBucket(bucket),
                                style = typographyV2.headingLG
                            )
                        }
                    }
                }

                KSSearchBottomSheetFooter(
                    leftButtonIsEnabled = selectedGoalBucket.value != null,
                    leftButtonClickAction = {
                        selectedGoalBucket.value = null
                        onApply(null, false)
                    },
                    rightButtonOnClickAction = {
                        onApply(selectedGoalBucket.value, true)
                    }
                )
            }
        }
    }
}

@Composable
fun textForBucket(bucket: DiscoveryParams.GoalBuckets): String = when (bucket) {
    DiscoveryParams.GoalBuckets.BUCKET_0 -> stringResource(R.string.Bucket_0_fpo)
    DiscoveryParams.GoalBuckets.BUCKET_1 -> stringResource(R.string.Bucket_1_fpo)
    DiscoveryParams.GoalBuckets.BUCKET_2 -> stringResource(R.string.Bucket_2_fpo)
    DiscoveryParams.GoalBuckets.BUCKET_3 -> stringResource(R.string.Bucket_3_fpo)
    DiscoveryParams.GoalBuckets.BUCKET_4 -> stringResource(R.string.Bucket_4_fpo)
}
