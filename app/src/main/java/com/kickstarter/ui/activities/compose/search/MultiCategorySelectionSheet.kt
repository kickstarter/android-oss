package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSButton
import com.kickstarter.ui.compose.designsystem.KSButtonType
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors

@Composable
fun MultiCategorySelectionSheet(
    categories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onApply: (Int) -> Unit,
    isLoading: Boolean,
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions
    var selectedCategories by remember { mutableStateOf(categories.associate { it.name to false }) }
    val totalSelectedResults = selectedCategories
        .filter { it.value }
        .keys
        .mapNotNull { categoryName ->
            categories.find { it.name == categoryName }?.totalResults
        }
        .sum()

    KSTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .heightIn(min = 200.dp, max = 770.dp),
            color = Color.White
        ) {
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
                        .padding(dimensions.paddingLarge),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category",
                        style = KSTheme.typographyV2.headingXL,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        modifier = Modifier.testTag(CategorySelectionSheetTestTag.DISMISS_BUTTON.name),
                        onClick = onDismiss
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
                        items(categories) { category ->
                            MultiCategoryItemRow(
                                category = category,
                                isSelected = selectedCategories[category.name] ?: false,
                                onSelectionChange = { isChecked ->
                                    selectedCategories = selectedCategories.toMutableMap().apply {
                                        this[category.name] = isChecked
                                    }
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .drawBehind {
                            drawLine(
                                color = backgroundDisabledColor,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = dimensions.dividerThickness.toPx()
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensions.paddingLarge),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.paddingMedium),
                    ) {
                        KSButton(
                            onClickAction = {
                                selectedCategories = selectedCategories.mapValues { false }
                            },
                            type = KSButtonType.Outlined,
                            text = stringResource(R.string.Reset),
                            isEnabled = !isLoading
                        )
                        KSButton(
                            modifier = Modifier.weight(1f),
                            onClickAction = { onApply(totalSelectedResults) },
                            type = KSButtonType.Filled,
                            text = "See $totalSelectedResults results",
                            isEnabled = totalSelectedResults > 0 && !isLoading,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MultiCategoryItemRow(
    category: CategoryItem,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

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
            .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                style = KSTheme.typographyV2.headingLG
            )

            Box(
                modifier = Modifier
                    .padding(start = dimensions.paddingSmall)
                    .background(
                        color = if (isSelected) colors.borderAccentGreenSubtle.copy(alpha = 0.3f) else backgroundDisabledColor,
                        shape = RoundedCornerShape(dimensions.radiusExtraSmall)
                    )
                    .padding(
                        horizontal = dimensions.paddingSmall,
                        vertical = dimensions.paddingXSmall
                    )
            ) {
                Text(
                    text = category.totalResults.toString(),
                    style = KSTheme.typographyV2.headingSM,
                    color = colors.kds_black
                )
            }
        }

        CustomSwitch(
            checked = isSelected,
            onCheckedChange = onSelectionChange,
        )
    }
}

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    thumbSize: Dp = 24.dp,
    trackWidth: Dp = 48.dp,
    trackHeight: Dp = 32.dp,
    checkedThumbColor: Color = colors.kds_white,
    uncheckedThumbColor: Color = colors.backgroundAccentGrayBold,
    checkedTrackColor: Color = colors.kds_black,
    uncheckedTrackColor: Color = colors.kds_white,
    uncheckedBorderColor: Color = Color.Gray,
    paddingHorizontal: Dp = 6.dp,
) {
    val switchModifier = Modifier
        .size(width = trackWidth, height = trackHeight)
        .clip(CircleShape)

    Box(
        modifier = switchModifier
            .background(if (checked) checkedTrackColor else uncheckedTrackColor)
            .border(
                width = if (!checked) 1.dp else 0.dp,
                color = if (!checked) uncheckedBorderColor else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier.run {
                size(thumbSize)
                    .offset(x = if (checked) (trackWidth - thumbSize - paddingHorizontal) else 4.dp)
                    .clip(CircleShape)
                    .background(if (checked) checkedThumbColor else uncheckedThumbColor)
            },
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Checked",
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

data class CategoryItem(
    val name: String,
    val totalResults: Int,
)

val sampleCategories = listOf(
    CategoryItem("Art", 1034),
    CategoryItem("Games", 765),
    CategoryItem("Design", 546),
    CategoryItem("Publishing", 489),
    CategoryItem("Fashion", 445),
    CategoryItem("Comics", 322),
    CategoryItem("Film & Video", 322),
    CategoryItem("Food", 217),
    CategoryItem("Technology", 654),
    CategoryItem("Music", 879),
    CategoryItem("Photography", 410),
    CategoryItem("Theater", 295),
    CategoryItem("Dance", 125),
    CategoryItem("Crafts", 300),
    CategoryItem("Journalism", 180),
)

enum class CategorySelectionSheetTestTag {
    APPLY_BUTTON,
    DISMISS_BUTTON
}
