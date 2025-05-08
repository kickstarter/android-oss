package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
import com.kickstarter.type.RaisedBuckets
import com.kickstarter.ui.compose.designsystem.KSDimensions
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
            currentPercentage = RaisedBuckets.BUCKET_0,
            onDismiss = {},
            onApply = { bucket, applyAndDismiss ->
            }
        )
    }
}

@Composable
fun PercentageRaisedSheet(
    currentPercentage: RaisedBuckets? = null,
    onDismiss: () -> Unit,
    onApply: (RaisedBuckets?, Boolean?) -> Unit,
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
                    // TODO: extract as a re-usable composable for Category as well same UI, just changes the title
                    IconButton(
                        onClick = onNavigate,
                        modifier = Modifier
                            .padding(start = dimensions.paddingSmall)
                            .testTag(SearchScreenTestTag.BACK_BUTTON.name)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.Back),
                            tint = colors.kds_black
                        )
                    }
                    Text(
                        text = stringResource(R.string.Percentage_raised_fpo),
                        style = typographyV2.headingXL,
                        modifier = Modifier.weight(1f),
                        color = colors.textPrimary
                    )
                    IconButton(
                        modifier = Modifier.testTag(CategorySelectionSheetTestTag.DISMISS_BUTTON.name),
                        onClick = onDismiss
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = colors.icon)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .testTag("BUCKET")
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
                    items(RaisedBuckets.knownValues()) { bucket ->
                        Row(
                            modifier = Modifier.testTag("Bucket Row")
                                .padding(top = dimensions.paddingMediumSmall, bottom = dimensions.paddingMediumSmall)
                                .clickable {
                                    selectedPercentage.value = bucket
                                    onApply(selectedPercentage.value, false)
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
                                    .testTag("BUCKET TEXT")
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
private fun textForBucket(bucket: RaisedBuckets) = when (bucket) {
    RaisedBuckets.BUCKET_2 -> stringResource(R.string.Percentage_raised_bucket_2)
    RaisedBuckets.BUCKET_1 -> stringResource(R.string.Percentage_raised_bucket_1)
    RaisedBuckets.BUCKET_0 -> stringResource(R.string.Percentage_raised_bucket_0)
    RaisedBuckets.UNKNOWN__ -> ""
}
