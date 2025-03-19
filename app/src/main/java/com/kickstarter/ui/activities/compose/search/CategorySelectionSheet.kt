package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.models.Category
import com.kickstarter.ui.compose.designsystem.KSButton
import com.kickstarter.ui.compose.designsystem.KSButtonType
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors

@Composable
fun CategorySelectionSheet(
    currentCategory: Category? = null,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onApply: (Category) -> Unit,
    isLoading: Boolean,
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    val selectedCategory = remember { mutableStateOf(currentCategory) }

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
                            CategoryItemRow(
                                category = category,
                                isSelected = category.name() == selectedCategory.value?.name(),
                                onSelectionChange = { isChecked ->
                                    if (isChecked) {
                                        selectedCategory.value =
                                            categories.find { it.name() == category.name() }!!
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
                        val resetCategoryName = stringResource(R.string.fpo_category)
                        KSButton(
                            onClickAction = {
                                selectedCategory.value = Category.builder().name(resetCategoryName).build()
                            },
                            type = KSButtonType.Outlined,
                            text = stringResource(R.string.Reset),
                            isEnabled = !isLoading
                        )
                        KSButton(
                            modifier = Modifier.weight(1f),
                            onClickAction = {
                                selectedCategory.value?.let { onApply(it) }
                            },
                            type = KSButtonType.Filled,
                            text = "See results",
                            isEnabled = !isLoading,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItemRow(
    category: Category,
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
            .clickable { onSelectionChange(!isSelected) }
            .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name(),
                style = KSTheme.typographyV2.headingLG
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = null, // null recommended for accessibility with screenreaders
            colors = RadioButtonDefaults.colors(selectedColor = colors.backgroundSelected)
        )
    }
}
