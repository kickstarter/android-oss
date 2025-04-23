package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.models.Category
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSSearchBottomSheetFooter
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.PillButton

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun CategorySelectionSheetPreviewPhase2Off() {
    KSTheme {
        CategorySelectionSheet(
            categories = CategoryFactory.rootCategories(),
            onDismiss = {},
            onApply = { a, b -> },
            isLoading = false,
            shouldShowPhase2 = false
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun CategorySelectionSheetPreviewPhase2On() {
    KSTheme {
        CategorySelectionSheet(
            categories = CategoryFactory.rootCategories(),
            onDismiss = {},
            onApply = { a, b -> },
            isLoading = false,
            shouldShowPhase2 = true
        )
    }
}

@Composable
fun CategorySelectionSheet(
    currentCategory: Category? = null,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onApply: (Category?, Boolean?) -> Unit,
    isLoading: Boolean,
    onNavigate: () -> Unit = {},
    shouldShowPhase2: Boolean,
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions
    val categoryWithSubCats = mutableMapOf<Category, List<Category>>()

    categories.forEach { cat ->
        cat.parent()?.let { parentCat ->
            val subcatList = categoryWithSubCats[parentCat]?.toMutableStateList() ?: mutableStateListOf<Category>()
            subcatList.add(cat)
            categoryWithSubCats[parentCat] = subcatList.toList()
        }
    }

    val selectedCategory = remember { mutableStateOf(currentCategory) }

    KSTheme {
        Surface(
            color = colors.backgroundSurfacePrimary
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
                        .padding(top = dimensions.paddingLarge, bottom = dimensions.paddingLarge, end = dimensions.paddingMediumSmall),

                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (shouldShowPhase2) {
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
                    }
                    Text(
                        text = stringResource(R.string.Category),
                        style = typographyV2.headingXL,
                        modifier =
                        if (shouldShowPhase2) Modifier.weight(1f)
                        else Modifier.weight(1f).padding(start = dimensions.paddingMediumLarge),
                        color = colors.textPrimary
                    )
                    IconButton(
                        modifier = Modifier.testTag(CategorySelectionSheetTestTag.DISMISS_BUTTON.name),
                        onClick = onDismiss
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = colors.icon)
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
                            color = colors.textPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        val rootCat = categoryWithSubCats.keys.toList()
                        items(rootCat) { category ->
                            CategoryItemRow(
                                category = category,
                                isSelected = category.name() == selectedCategory.value?.name(),
                                onSelectionChange = { isChecked ->
                                    if (isChecked) {
                                        selectedCategory.value =
                                            categories.find { it.name() == category.name() }!!
                                    }

                                    onApply(selectedCategory.value, null)
                                },
                                subcategories = categoryWithSubCats.get(category) ?: emptyList()
                            )
                        }
                    }
                }

                val resetCategoryName = stringResource(R.string.Category)
                KSSearchBottomSheetFooter(
                    isLoading = isLoading,
                    resetOnclickAction = {
                        selectedCategory.value = null
                        onApply(selectedCategory.value, false)
                    },
                    onApply = {
                        selectedCategory.value?.let { onApply(it, true) }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryItemRow(
    category: Category,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    subcategories: List<Category> = emptyList()
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions
    val switchChecked = remember { mutableStateOf(isSelected) }

    Column(
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
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                color = colors.textPrimary,
                text = category.name(),
                style = typographyV2.headingLG
            )

            CustomSwitch(checked = switchChecked.value, onCheckedChange = { switchChecked.value = it })
        }

        AnimatedVisibility(
            visible = switchChecked.value,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PillButton(
                    text = "All",
                    shouldShowIcon = false,
                    isSelected = true,
                    modifier = Modifier.testTag("subcat"),
                    onClick = {
                    }
                )
                subcategories.map { subcat ->
                    PillButton(
                        text = subcat.name(),
                        shouldShowIcon = false,
                        isSelected = false,
                        modifier = Modifier.testTag("subcat"),
                        onClick = {
                        }
                    )
                }
            }
        }
    }
}
