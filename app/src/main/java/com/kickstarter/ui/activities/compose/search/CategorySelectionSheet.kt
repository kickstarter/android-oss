package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.kickstarter.ui.compose.designsystem.KSPillButton
import com.kickstarter.ui.compose.designsystem.KSSearchBottomSheetFooter
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun CategoryRowSelectedWithSubcategoriesPills() {
    KSTheme {
        CategoryItemRow(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
            category = CategoryFactory.artCategory(),
            selectedCategory = CategoryFactory.ceramicsCategory(),
            onSelectionChange = {},
            subcategories = listOf(CategoryFactory.textilesCategory(), CategoryFactory.digitalArtCategory(), CategoryFactory.ceramicsCategory())
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun CategoryRowUnSelected() {
    KSTheme {
        CategoryItemRow(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
            category = CategoryFactory.artCategory(),
            onSelectionChange = {},
            subcategories = listOf(CategoryFactory.textilesCategory(), CategoryFactory.digitalArtCategory(), CategoryFactory.ceramicsCategory())
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun CategorySelectionSheetPreviewPhase2Off() {
    KSTheme {
        CategorySelectionSheet(
            currentCategory = CategoryFactory.textilesCategory(),
            categories = listOf(CategoryFactory.tabletopGamesCategory(), CategoryFactory.textilesCategory(), CategoryFactory.digitalArtCategory(), CategoryFactory.ceramicsCategory(), CategoryFactory.worldMusicCategory()),
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
            currentCategory = CategoryFactory.artCategory(),
            categories = listOf(CategoryFactory.tabletopGamesCategory(), CategoryFactory.textilesCategory(), CategoryFactory.digitalArtCategory(), CategoryFactory.ceramicsCategory(), CategoryFactory.worldMusicCategory()),
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
                        items(rootCat) { rootCategory ->
                            CategoryItemRow(
                                selectedCategory = selectedCategory.value,
                                category = rootCategory,
                                onSelectionChange = { category ->
                                    selectedCategory.value = category
                                    onApply(selectedCategory.value, null)
                                },
                                subcategories = categoryWithSubCats[rootCategory] ?: emptyList()
                            )
                        }
                    }
                }

                KSSearchBottomSheetFooter(
                    isLoading = isLoading,
                    leftButtonIsEnabled = selectedCategory.value != null,
                    leftButtonClickAction = {
                        selectedCategory.value = null
                        onApply(selectedCategory.value, false)
                    },
                    rightButtonOnClickAction = {
                        onApply(selectedCategory.value, true)
                    }
                )
            }
        }
    }
}

object CategoryItemRowTestTags {
    const val ROOTCATEGORY_TITLE = "rowcategory_title"
    const val RADIO_BUTTON = "radio_button"
    const val SUBCATEGORY_ROW = "subcategory_row"
    const val ROOTCATEGORY_ROW = "rootcategory_row"
    fun pillTag(category: Category) = "pill_${category.id()}"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryItemRow(
    modifier: Modifier = Modifier,
    category: Category,
    selectedCategory: Category? = null,
    onSelectionChange: (Category) -> Unit,
    subcategories: List<Category> = emptyList()
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    val isSelected = category.id() == selectedCategory?.id() || category.id() == selectedCategory?.parentId()

    Column(
        modifier = modifier
            .testTag(CategoryItemRowTestTags.RADIO_BUTTON)
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = backgroundDisabledColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = dimensions.dividerThickness.toPx()
                )
            }
            .clickable {
                onSelectionChange(category)
            }
            .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium),
    ) {
        Row(
            modifier = Modifier.testTag(CategoryItemRowTestTags.ROOTCATEGORY_ROW),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = modifier
                    .testTag(CategoryItemRowTestTags.ROOTCATEGORY_TITLE)
                    .weight(1f),
                color = colors.textPrimary,
                text = category.name(),
                style = typographyV2.headingLG
            )

            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(unselectedColor = colors.backgroundSelected, selectedColor = colors.backgroundSelected)
            )
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FlowRow(
                modifier = Modifier
                    .testTag(CategoryItemRowTestTags.SUBCATEGORY_ROW)
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KSPillButton(
                    text = stringResource(R.string.Project_status_all),
                    shouldShowIcon = false,
                    isSelected = selectedCategory?.isRoot == true,
                    modifier = Modifier.testTag(CategoryItemRowTestTags.pillTag(category)),
                    onClick = {
                        onSelectionChange(category)
                    }
                )
                subcategories.map { subcategory ->
                    KSPillButton(
                        text = subcategory.name(),
                        shouldShowIcon = false,
                        isSelected = selectedCategory?.id() == subcategory.id(),
                        modifier = Modifier.testTag(CategoryItemRowTestTags.pillTag(subcategory)),
                        onClick = {
                            onSelectionChange(subcategory)
                        }
                    )
                }
            }
        }
    }
}
