package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.activities.compose.search.PillBarTestTags.pillTag
import com.kickstarter.ui.compose.designsystem.KSIconPillButton
import com.kickstarter.ui.compose.designsystem.KSPillButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarLocationActiveFilterPreview() {
    KSTheme {
        SearchTopBar(
            onBackPressed = {},
            onValueChanged = {},
            selectedFilterCounts = mapOf(
                FilterRowPillType.SORT.name to 0,
                FilterRowPillType.CATEGORY.name to 0,
                FilterRowPillType.PROJECT_STATUS.name to 0,
                FilterRowPillType.FILTER.name to 1,
                FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                FilterRowPillType.LOCATION.name to 1,
                FilterRowPillType.AMOUNT_RAISED.name to 0,
                FilterRowPillType.RECOMMENDED.name to 0,
                FilterRowPillType.PROJECTS_LOVED.name to 0,
                FilterRowPillType.SAVED.name to 0,
                FilterRowPillType.FOLLOWING.name to 0,
            ),
            onPillPressed = {},
            shouldShowPhase = true
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarAmountRaisedActiveFilterPreview() {
    KSTheme {
        SearchTopBar(
            onBackPressed = {},
            onValueChanged = {},
            selectedFilterCounts = mapOf(
                FilterRowPillType.SORT.name to 0,
                FilterRowPillType.CATEGORY.name to 0,
                FilterRowPillType.PROJECT_STATUS.name to 0,
                FilterRowPillType.FILTER.name to 1,
                FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                FilterRowPillType.AMOUNT_RAISED.name to 1,
                FilterRowPillType.RECOMMENDED.name to 0,
                FilterRowPillType.PROJECTS_LOVED.name to 0,
                FilterRowPillType.SAVED.name to 0,
                FilterRowPillType.FOLLOWING.name to 0,
            ),
            onPillPressed = {},
            shouldShowPhase = true
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarPercentageRaisedActiveFilterPreview() {
    KSTheme {
        SearchTopBar(
            onBackPressed = {},
            onValueChanged = {},
            selectedFilterCounts = mapOf(
                FilterRowPillType.SORT.name to 0,
                FilterRowPillType.CATEGORY.name to 0,
                FilterRowPillType.PROJECT_STATUS.name to 0,
                FilterRowPillType.FILTER.name to 1,
                FilterRowPillType.PERCENTAGE_RAISED.name to 1,
                FilterRowPillType.RECOMMENDED.name to 0,
                FilterRowPillType.PROJECTS_LOVED.name to 0,
                FilterRowPillType.SAVED.name to 0,
                FilterRowPillType.FOLLOWING.name to 0,
            ),
            onPillPressed = {},
            shouldShowPhase = true
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarProjectStatusActiveFilterPreview() {
    KSTheme {
        SearchTopBar(
            onBackPressed = {},
            onValueChanged = {},
            categoryPillText = "Art",
            projectStatusText = "Live",
            selectedFilterCounts = mapOf(
                FilterRowPillType.SORT.name to 0,
                FilterRowPillType.CATEGORY.name to 0,
                FilterRowPillType.PROJECT_STATUS.name to 1,
                FilterRowPillType.FILTER.name to 1,
                FilterRowPillType.RECOMMENDED.name to 0,
                FilterRowPillType.PROJECTS_LOVED.name to 0,
                FilterRowPillType.SAVED.name to 0,
                FilterRowPillType.FOLLOWING.name to 0,
            ),
            onPillPressed = {},
            shouldShowPhase = true
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarCategoryActiveFilterPreview() {
    KSTheme {
        SearchTopBar(
            onBackPressed = {},
            onValueChanged = {},
            categoryPillText = "Art",
            projectStatusText = "Live",
            selectedFilterCounts = mapOf(
                FilterRowPillType.SORT.name to 0,
                FilterRowPillType.CATEGORY.name to 1,
                FilterRowPillType.PROJECT_STATUS.name to 0,
                FilterRowPillType.FILTER.name to 1,
            ),
            onPillPressed = {},
            shouldShowPhase = true
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarAllActiveFiltersPreview() {
    KSTheme {
        SearchTopBar(
            onBackPressed = {},
            onValueChanged = {},
            categoryPillText = "Art",
            projectStatusText = "Live",
            selectedFilterCounts = mapOf(
                FilterRowPillType.SORT.name to 0,
                FilterRowPillType.CATEGORY.name to 1,
                FilterRowPillType.PROJECT_STATUS.name to 1,
                FilterRowPillType.FILTER.name to 1,
                FilterRowPillType.PERCENTAGE_RAISED.name to 1,
                FilterRowPillType.LOCATION.name to 1,
                FilterRowPillType.AMOUNT_RAISED.name to 1,
                FilterRowPillType.RECOMMENDED.name to 1,
                FilterRowPillType.PROJECTS_LOVED.name to 1,
                FilterRowPillType.SAVED.name to 1,
                FilterRowPillType.FOLLOWING.name to 1,
            ),
            onPillPressed = {},
            shouldShowPhase = true
        )
    }
}

@Composable
fun SearchTopBar(
    modifier: Modifier = Modifier,
    countApiIsReady: Boolean = false,
    categoryPillText: String = stringResource(R.string.Category),
    projectStatusText: String = stringResource(R.string.Project_status),
    percentageRaisedText: String = stringResource(R.string.Percentage_raised),
    locationText: String = stringResource(R.string.Location_fpo),
    amountRaisedText: String = stringResource(R.string.Amount_raised_fpo),
    recommendedText: String = stringResource(R.string.Recommended_fpo),
    projectsLovedText: String = stringResource(R.string.Projects_We_Love_fpo),
    savedProjectsText: String = stringResource(R.string.Saved_projects_fpo),
    followingText: String = stringResource(R.string.Following),
    onBackPressed: () -> Unit,
    onValueChanged: (String) -> Unit,
    selectedFilterCounts: Map<String, Int>,
    onPillPressed: (FilterRowPillType) -> Unit = {},
    shouldShowPhase: Boolean = true
) {

    var value by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.backgroundSurfacePrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.searchAppBarHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .testTag(SearchScreenTestTag.BACK_BUTTON.name)
                    .size(dimensions.clickableButtonHeight)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.Back),
                    tint = colors.kds_black
                )
            }

            OutlinedTextField(
                modifier = Modifier
                    .testTag(SearchScreenTestTag.SEARCH_TEXT_INPUT.name)
                    .padding(
                        start = dimensions.appBarSearchPadding,
                        end = dimensions.appBarEndPadding,
                        bottom = dimensions.appBarSearchPadding
                    )
                    .fillMaxSize(),
                value = value,
                onValueChange = {
                    value = it
                    onValueChanged(value)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(
                            modifier = Modifier.fillMaxHeight(),
                            onClick = {
                                value = ""
                                onValueChanged("")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(id = R.string.social_buttons_cancel),
                                tint = colors.kds_support_700
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = colors.backgroundSurfacePrimary,
                    errorLabelColor = colors.kds_alert,
                    unfocusedLabelColor = colors.textSecondary,
                    focusedLabelColor = colors.borderActive,
                    cursorColor = colors.kds_create_700,
                    errorCursorColor = colors.kds_alert,
                    textColor = colors.textAccentGrey,
                    disabledTextColor = colors.textDisabled,
                    focusedBorderColor = colors.borderActive,
                    unfocusedBorderColor = colors.borderBold
                ),
                label = {
                    Text(text = stringResource(id = R.string.tabbar_search))
                },
                singleLine = true
            )
        }
        PillBar(
            countApiIsReady = countApiIsReady,
            categoryPillText = categoryPillText,
            projectStatusText = projectStatusText,
            amountRaisedText = amountRaisedText,
            percentageRaisedText = percentageRaisedText,
            locationText = locationText,
            recommendedText = recommendedText,
            projectsLovedText = projectsLovedText,
            savedProjectsText = savedProjectsText,
            followingText = followingText,
            selectedFilterCounts = selectedFilterCounts,
            onPillPressed = onPillPressed,
            shouldShowPhase = shouldShowPhase,
        )
    }
}

object PillBarTestTags {
    fun pillTag(state: FilterRowPillType) = "pill_${state.name}"
}

@Composable
fun PillBar(
    countApiIsReady: Boolean = false,
    categoryPillText: String = stringResource(R.string.Category),
    projectStatusText: String = stringResource(R.string.Project_status),
    percentageRaisedText: String = stringResource(R.string.Percentage_raised),
    locationText: String = stringResource(R.string.Location_fpo),
    amountRaisedText: String = stringResource(R.string.Amount_raised_fpo),
    recommendedText: String = stringResource(R.string.Recommended_fpo),
    projectsLovedText: String = stringResource(R.string.Projects_We_Love_fpo),
    savedProjectsText: String = stringResource(R.string.Saved_projects_fpo),
    followingText: String = stringResource(R.string.Following),
    selectedFilterCounts: Map<String, Int>,
    onPillPressed: (FilterRowPillType) -> Unit,
    shouldShowPhase: Boolean = true
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(
                start = dimensions.paddingMediumLarge,
                end = dimensions.paddingMediumLarge,
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensions.listItemSpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KSIconPillButton(
            modifier = Modifier.testTag(pillTag(FilterRowPillType.SORT)),
            type = FilterRowPillType.SORT,
            isSelected = selectedFilterCounts.getOrDefault(FilterRowPillType.SORT.name, 0) > 0,
            onClick = { onPillPressed(FilterRowPillType.SORT) }
        )
        val activeFilters: Int =
            selectedFilterCounts.getOrDefault(FilterRowPillType.PROJECT_STATUS.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.CATEGORY.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.PERCENTAGE_RAISED.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.LOCATION.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.AMOUNT_RAISED.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.RECOMMENDED.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.PROJECTS_LOVED.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.SAVED.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.FOLLOWING.name, 0)

        KSIconPillButton(
            modifier = Modifier.testTag(pillTag(FilterRowPillType.FILTER)),
            type = FilterRowPillType.FILTER,
            isSelected = selectedFilterCounts.getOrDefault(
                FilterRowPillType.FILTER.name,
                0
            ) > 0,
            onClick = { onPillPressed(FilterRowPillType.FILTER) },
            count = activeFilters
        )

        KSPillButton(
            modifier = Modifier.testTag(pillTag(FilterRowPillType.CATEGORY)),
            countApiIsReady = countApiIsReady,
            text = categoryPillText,
            isSelected = selectedFilterCounts.getOrDefault(FilterRowPillType.CATEGORY.name, 0) > 0,
            count = selectedFilterCounts.getOrDefault(FilterRowPillType.CATEGORY.name, 0),
            onClick = { onPillPressed(FilterRowPillType.CATEGORY) }
        )
        KSPillButton(
            modifier = Modifier.testTag(pillTag(FilterRowPillType.PROJECT_STATUS)),
            countApiIsReady = countApiIsReady,
            text = projectStatusText,
            isSelected = selectedFilterCounts.getOrDefault(
                FilterRowPillType.PROJECT_STATUS.name,
                0
            ) > 0,
            count = selectedFilterCounts.getOrDefault(FilterRowPillType.PROJECT_STATUS.name, 0),
            onClick = { onPillPressed(FilterRowPillType.PROJECT_STATUS) }
        )
        KSPillButton(
            modifier = Modifier.testTag(pillTag(FilterRowPillType.LOCATION)),
            countApiIsReady = countApiIsReady,
            text = locationText,
            isSelected = selectedFilterCounts.getOrDefault(
                FilterRowPillType.LOCATION.name,
                0
            ) > 0,
            count = selectedFilterCounts.getOrDefault(
                FilterRowPillType.LOCATION.name,
                0
            ),
            onClick = { onPillPressed(FilterRowPillType.LOCATION) }
        )
        KSPillButton(
            modifier = Modifier.testTag(pillTag(FilterRowPillType.PERCENTAGE_RAISED)),
            countApiIsReady = countApiIsReady,
            text = percentageRaisedText,
            isSelected = selectedFilterCounts.getOrDefault(
                FilterRowPillType.PERCENTAGE_RAISED.name,
                0
            ) > 0,
            count = selectedFilterCounts.getOrDefault(
                FilterRowPillType.PERCENTAGE_RAISED.name,
                0
            ),
            onClick = { onPillPressed(FilterRowPillType.PERCENTAGE_RAISED) }
        )

        KSPillButton(
            modifier = Modifier.testTag(pillTag(FilterRowPillType.AMOUNT_RAISED)),
            countApiIsReady = countApiIsReady,
            text = amountRaisedText,
            isSelected = selectedFilterCounts.getOrDefault(
                FilterRowPillType.AMOUNT_RAISED.name,
                0
            ) > 0,
            count = selectedFilterCounts.getOrDefault(
                FilterRowPillType.AMOUNT_RAISED.name,
                0
            ),
            onClick = { onPillPressed(FilterRowPillType.AMOUNT_RAISED) }
        )

        if (shouldShowPhase) {
            KSPillButton(
                modifier = Modifier.testTag(pillTag(FilterRowPillType.RECOMMENDED)),
                text = recommendedText,
                isSelected = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.RECOMMENDED.name,
                    0
                ) > 0,
                count = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.RECOMMENDED.name,
                    0
                ),
                onClick = { onPillPressed(FilterRowPillType.RECOMMENDED) }
            )
            KSPillButton(
                modifier = Modifier.testTag(pillTag(FilterRowPillType.PROJECTS_LOVED)),
                text = projectsLovedText,
                isSelected = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.PROJECTS_LOVED.name,
                    0
                ) > 0,
                count = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.PROJECTS_LOVED.name,
                    0
                ),
                onClick = { onPillPressed(FilterRowPillType.PROJECTS_LOVED) }
            )
            KSPillButton(
                modifier = Modifier.testTag(pillTag(FilterRowPillType.SAVED)),
                text = savedProjectsText,
                isSelected = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.SAVED.name,
                    0
                ) > 0,
                count = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.SAVED.name,
                    0
                ),
                onClick = { onPillPressed(FilterRowPillType.SAVED) }
            )
            KSPillButton(
                modifier = Modifier.testTag(pillTag(FilterRowPillType.FOLLOWING)),
                text = followingText,
                isSelected = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.FOLLOWING.name,
                    0
                ) > 0,
                count = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.FOLLOWING.name,
                    0
                ),
                onClick = { onPillPressed(FilterRowPillType.FOLLOWING) }
            )
        }
    }
}

enum class FilterRowPillType {
    SORT,
    CATEGORY,
    FILTER,
    PROJECT_STATUS,
    PERCENTAGE_RAISED,
    LOCATION,
    AMOUNT_RAISED,
    RECOMMENDED,
    PROJECTS_LOVED,
    SAVED,
    FOLLOWING
}
