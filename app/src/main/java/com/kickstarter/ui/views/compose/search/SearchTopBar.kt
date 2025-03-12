package com.kickstarter.ui.views.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.activities.compose.search.SearchScreenTestTag
import com.kickstarter.ui.compose.designsystem.IconPillButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.PillButton

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarPreview() {
    KSTheme {
        SearchTopBar(
            onBackPressed = {},
            onValueChanged = {},
            selectedFilterCounts = mapOf(
                FilterRowPillType.SORT.name to 0,
                FilterRowPillType.CATEGORY.name to 0
            ),
            onSortPressed = {},
            onCategoryPressed = {}
        )
    }
}

@Composable
fun SearchTopBar(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    onValueChanged: (String) -> Unit,
    selectedFilterCounts: Map<String, Int>,
    onSortPressed: () -> Unit = {},
    onCategoryPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val ffEnabled = if (!isPreview) {
        context.getEnvironment()?.featureFlagClient()?.getBoolean(FlagKey.ANDROID_SEARCH_FILTER) ?: false
    } else {
        true
    }

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
                    imageVector = Icons.Filled.ArrowBack,
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
        if (ffEnabled) {
            PillBar(
                selectedFilterCounts,
                onSortPressed,
                onCategoryPressed
            )
        }
    }
}

@Composable
fun PillBar(
    selectedFilterCounts: Map<String, Int>,
    onSortPressed: () -> Unit,
    onCategoryPressed: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensions.paddingMedium, dimensions.none, dimensions.none, dimensions.none),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconPillButton(
            selectedFilterCounts.getOrDefault(FilterRowPillType.SORT.name, 0) > 0,
            onSortPressed
        )
        PillButton(
            stringResource(R.string.fpo_category),
            selectedFilterCounts.getOrDefault(FilterRowPillType.CATEGORY.name, 0) > 0,
            selectedFilterCounts.getOrDefault(FilterRowPillType.CATEGORY.name, 0),
            onCategoryPressed
        )
    }
}

enum class FilterRowPillType {
    SORT,
    CATEGORY,
}
