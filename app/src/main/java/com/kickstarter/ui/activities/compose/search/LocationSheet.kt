package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Location
import com.kickstarter.ui.activities.compose.search.LocationTestTags.INPUT_BUTTON
import com.kickstarter.ui.activities.compose.search.LocationTestTags.INPUT_SEARCH
import com.kickstarter.ui.activities.compose.search.LocationTestTags.LOCATION_ANYWHERE
import com.kickstarter.ui.activities.compose.search.LocationTestTags.SUGGESTED_LOCATIONS_LIST
import com.kickstarter.ui.activities.compose.search.LocationTestTags.locationTag
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSIconButton
import com.kickstarter.ui.compose.designsystem.KSSearchBottomSheetFooter
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LocationSheetPreview() {
    val env = Environment.builder().apolloClientV2(
        object : MockApolloClientV2() {
            override suspend fun getLocations(
                useDefault: Boolean,
                term: String?,
                lat: Float?,
                long: Float?,
                radius: Float?,
                filterByCoordinates: Boolean?
            ): Result<List<Location>> {
                if (useDefault) return Result.success(listOf(LocationFactory.vancouver()))
                val searched = listOf(
                    LocationFactory.sydney(),
                    LocationFactory.mexico(),
                    LocationFactory.canada(),
                    LocationFactory.germany(),
                    LocationFactory.unitedStates(),
                    LocationFactory.nigeria(),
                    LocationFactory.sydney(),
                    LocationFactory.mexico(),
                    LocationFactory.canada(),
                    LocationFactory.germany(),
                    LocationFactory.unitedStates(),
                    LocationFactory.nigeria(),
                )
                if (term.isNotNull()) return Result.success(searched)

                return Result.success(emptyList())
            }
        }
    ).build()
    val fakeViewModel = FilterMenuViewModel(env)

    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
            LocationSheet()
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DefaultLocationComposablePreview() {

    KSTheme {
        Surface(
            color = colors.backgroundSurfacePrimary
        ) {
            DefaultLocationComposable(
                defaultLocations = listOf(LocationFactory.vancouver(), LocationFactory.sydney()),
                currentLocation = remember { mutableStateOf(LocationFactory.sydney()) },
                onclickCallback = { location ->
                }
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SuggestedLocationsComposablePreview() {

    KSTheme {
        Surface(
            color = colors.backgroundSurfacePrimary
        ) {
            SuggestedLocationsComposable(
                suggestedLocations = listOf(LocationFactory.mexico(), LocationFactory.unitedStates(), LocationFactory.germany(), LocationFactory.nigeria()),
                currentLocation = remember { mutableStateOf(LocationFactory.sydney()) },
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InputDefaultPreview() {
    KSTheme {
        Surface(
            color = colors.backgroundSurfacePrimary
        ) {
            val isFocused = remember { mutableStateOf(false) }
            var inputValue = remember { mutableStateOf("") }
            InputSearchComposable(isFocused = isFocused, input = inputValue)
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InputSelectedPreview() {
    KSTheme {
        Surface(
            color = colors.backgroundSurfacePrimary
        ) {
            val isFocused = remember { mutableStateOf(true) }
            var inputValue = remember { mutableStateOf(LocationFactory.vancouver().displayableName()) }
            InputSearchComposable(isFocused = isFocused, input = inputValue)
        }
    }
}

object LocationTestTags {
    const val DEFAULT_LOCATION_LIST = "default_location_list"
    const val SUGGESTED_LOCATIONS_LIST = "suggested_location_list"
    const val LOCATION_ANYWHERE = "location_anywhere"
    const val INPUT_SEARCH = "input_search"
    const val INPUT_BUTTON = "input_button"
    fun locationTag(location: Location) = "location_${location.displayableName()}"
}

@Composable
fun LocationSheet(
    selectedLocation: Location? = null,
    onDismiss: () -> Unit = {},
    onApply: (Location?, Boolean?) -> Unit = { a, b -> },
    onNavigate: () -> Unit = {},
) {
    val viewModel = LocalFilterMenuViewModel.current
    val locationsUIState by viewModel.locationsUIState.collectAsStateWithLifecycle()
    val defaultLocations = if (!LocalInspectionMode.current) // - Load real data from VM
        locationsUIState.nearLocations
    else listOf(LocationFactory.vancouver()) // - Load hardcoded option while in preview

    val searched = if (!LocalInspectionMode.current) // - Load real data from VM
        locationsUIState.searchedLocations
    else listOf(LocationFactory.sydney(), LocationFactory.mexico(), LocationFactory.canada(), LocationFactory.germany()) // - Load hardcoded option while in preview

    val isLoading = locationsUIState.isLoading

    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    val currentLocation = remember { mutableStateOf(selectedLocation) }

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
                        .padding(
                            top = dimensions.paddingLarge,
                            bottom = dimensions.paddingLarge,
                            end = dimensions.paddingMediumSmall
                        ),

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
                        text = stringResource(R.string.Location),
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

                val isFocused = remember { mutableStateOf(false) }
                var inputValue = remember { mutableStateOf("") }

                InputSearchComposable(
                    isFocused = isFocused,
                    input = inputValue,
                    searchCallback = { term -> viewModel.updateQuery(term) },
                    cancelCallback = {
                        currentLocation.value = null
                        inputValue.value = ""
                        viewModel.clearQuery()
                    }
                )

                if (!isFocused.value && inputValue.value.isEmpty()) {
                    DefaultLocationComposable(
                        modifier = Modifier.weight(1f),
                        defaultLocations = defaultLocations,
                        currentLocation = currentLocation,
                        onclickCallback = { locationClicked ->
                            if (locationClicked.isNotNull()) {
                                currentLocation.value = locationClicked
                            } else {
                                currentLocation.value = null
                                inputValue.value = ""
                            }
                        }
                    )
                }

                if (isFocused.value && inputValue.value.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f)
                    )
                }

                if (inputValue.value.isNotEmpty()) {
                    SuggestedLocationsComposable(
                        modifier = Modifier.weight(1f),
                        suggestedLocations = searched,
                        currentLocation = currentLocation,
                        onclickCallback = { clickedLocation ->
                            currentLocation.value = clickedLocation
                            inputValue.value = clickedLocation?.displayableName() ?: ""
                        }
                    )
                }

                KSSearchBottomSheetFooter(
                    leftButtonIsEnabled = currentLocation.value.isNotNull(),
                    leftButtonClickAction = {
                        currentLocation.value = null
                        inputValue.value = ""
                        onApply(currentLocation.value, false)
                    },
                    rightButtonIsEnabled = currentLocation.value.isNotNull() || (currentLocation.value.isNull() && inputValue.value.isNullOrEmpty()),
                    rightButtonOnClickAction = {
                        onApply(currentLocation.value, true)
                    }
                )
            }
        }
    }
}

@Composable
private fun SuggestedLocationsComposable(
    modifier: Modifier = Modifier,
    suggestedLocations: List<Location> = emptyList(),
    currentLocation: MutableState<Location?>,
    onclickCallback: (Location?) -> Unit = {}
) {
    val dimensions: KSDimensions = KSTheme.dimensions

    LazyColumn(
        modifier = modifier
            .testTag(SUGGESTED_LOCATIONS_LIST)
            .fillMaxWidth()
            .padding(top = dimensions.paddingSmall, start = dimensions.paddingMedium),
        horizontalAlignment = Alignment.Start,
    ) {
        items(suggestedLocations) { location ->
            Row(
                modifier = Modifier.testTag(locationTag(location))
                    .animateItem()
                    .padding(
                        top = dimensions.paddingMediumSmall,
                        start = dimensions.paddingMediumSmall,
                        bottom = dimensions.paddingMediumSmall
                    )
                    .clickable {
                        onclickCallback(location)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    color = colors.textPrimary,
                    text = location.displayableName(),
                    style = typographyV2.headingLG
                )
            }
        }
    }
}

@Composable
private fun InputSearchComposable(
    modifier: Modifier = Modifier,
    isFocused: MutableState<Boolean>,
    input: MutableState<String>, // TODO: evaluate if needed here
    searchCallback: (String) -> Unit = { a -> },
    cancelCallback: () -> Unit = {},
) {
    val dimensions: KSDimensions = KSTheme.dimensions
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium)
            .fillMaxWidth()
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = modifier
                .testTag(INPUT_SEARCH)
                .weight(1f)
                .onFocusChanged { it ->
                    if (it.isFocused) isFocused.value = true
                }
                .testTag(SearchScreenTestTag.SEARCH_TEXT_INPUT.name),
            value = input.value,
            onValueChange = {
                input.value = it
                searchCallback(it)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                }
            ),
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
                Text(text = stringResource(id = R.string.Location_searchbox_placeholder))
            },
            singleLine = true,
            trailingIcon = {
                if (input.value.isNotEmpty()) {
                    KSIconButton(
                        onClick = {
                            input.value = ""
                            cancelCallback()
                        },
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(id = R.string.social_buttons_cancel)
                    )
                }
            },
        )

        AnimatedVisibility(
            visible = isFocused.value || input.value.isNotEmpty(),
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            TextButton(
                onClick = {
                    input.value = ""
                    keyboardController?.hide()
                    isFocused.value = false
                    focusManager.clearFocus()
                    cancelCallback()
                },
                modifier = modifier
                    .testTag(INPUT_BUTTON)
                    .padding(start = 8.dp, top = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.Cancel),
                    color = colors.textAccentGreen,
                    style = typographyV2.bodyLG
                )
            }
        }
    }
}

@Composable
private fun DefaultLocationComposable(
    modifier: Modifier = Modifier,
    defaultLocations: List<Location>,
    currentLocation: MutableState<Location?>,
    onclickCallback: (Location?) -> Unit = {}
) {

    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    val isSelectedId = currentLocation.value?.id()

    LazyColumn(
        modifier = modifier
            .testTag(LocationTestTags.DEFAULT_LOCATION_LIST)
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = backgroundDisabledColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = dimensions.dividerThickness.toPx()
                )
            }
            .padding(
                horizontal = dimensions.paddingLarge,
                vertical = dimensions.paddingMedium
            ),
    ) {
        itemsIndexed(defaultLocations) { index, location ->
            if (index == 0) {
                Row(
                    modifier = Modifier.testTag(LOCATION_ANYWHERE)
                        .padding(
                            top = dimensions.paddingMediumSmall,
                            bottom = dimensions.paddingMediumSmall
                        )
                        .clickable {
                            onclickCallback(null)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
                ) {
                    RadioButton(
                        selected = isSelectedId == null,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            unselectedColor = colors.backgroundSelected,
                            selectedColor = colors.backgroundSelected
                        )
                    )
                    Text(
                        modifier = Modifier
                            .weight(1f),
                        color = colors.textPrimary,
                        text = stringResource(R.string.Location_Anywhere),
                        style = typographyV2.headingLG
                    )
                }
            }
            Row(
                modifier = Modifier.testTag(locationTag(location))
                    .padding(
                        top = dimensions.paddingMediumSmall,
                        bottom = dimensions.paddingMediumSmall
                    )
                    .clickable {
                        onclickCallback(location)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            ) {
                RadioButton(
                    selected = isSelectedId == location.id(),
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        unselectedColor = colors.backgroundSelected,
                        selectedColor = colors.backgroundSelected
                    )
                )
                Text(
                    modifier = Modifier
                        .weight(1f),
                    color = colors.textPrimary,
                    text = location.displayableName(),
                    style = typographyV2.headingLG
                )
            }
        }
    }
}
