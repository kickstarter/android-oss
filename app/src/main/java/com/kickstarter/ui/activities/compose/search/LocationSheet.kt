package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Location
import com.kickstarter.ui.activities.compose.search.LocationTestTags.LOCATION_ANYWHERE
import com.kickstarter.ui.activities.compose.search.LocationTestTags.locationTag
import com.kickstarter.ui.activities.compose.search.PercentageRaisedTestTags.BUCKETS_LIST
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
    val env = Environment.builder().apolloClientV2(MockApolloClientV2()).build()
    val fakeViewModel= FilterMenuViewModel(env, isInPreview = true)

    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
            LocationSheet(
                selectedLocation = LocationFactory.vancouver()
            )
        }
    }
}

object LocationTestTags {
    const val LOCATION_LIST = "location_list"
    const val LOCATION_ANYWHERE = "location_anywhere"
    const val LOCATION_CITY = "location_city"
    fun locationTag(location: Location) = "location_${location.displayableName()}"
}

@Composable
fun LocationSheet(
    selectedLocation: Location? = null,
    onDismiss: () -> Unit = {},
    onApply: (Location?, Boolean?) -> Unit = { a, b -> },
    onNavigate: () -> Unit = {},
){
    val viewModel = LocalFilterMenuViewModel.current
    val locationsUIState by viewModel.locationsUIState.collectAsStateWithLifecycle()
    val defaultLocations =
        if (!LocalInspectionMode.current)
            locationsUIState.nearLocations
        else listOf(LocationFactory.vancouver())

    val searched = locationsUIState.searchedLocations
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

                // OutlinedTextField can be extracted as input-text design system piece
                OutlinedTextField(
                    modifier = Modifier
                        .testTag(SearchScreenTestTag.SEARCH_TEXT_INPUT.name)
                        .padding(
                            horizontal = dimensions.paddingLarge,
                            vertical = dimensions.paddingMedium
                        )
                        .fillMaxWidth(),
                    value = "",
                    onValueChange = {
//                        value = it
//                        onValueChanged(value)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
//                            keyboardController?.hide()
//                            focusManager.clearFocus()
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
                    singleLine = true
                )

                DefaultLocationComposable(modifier = Modifier.weight(1f), defaultLocations, currentLocation)

                KSSearchBottomSheetFooter(
                    leftButtonIsEnabled = false,
                    leftButtonClickAction = {
                        currentLocation.value = null
                        onApply(currentLocation.value, false)
                    },
                    rightButtonOnClickAction = {
                        onApply(currentLocation.value, true)
                    }
                )
            }
        }
    }
}

@Composable
private fun DefaultLocationComposable(
    modifier: Modifier = Modifier,
    defaultLocations: List<Location>,
    currentLocation: MutableState<Location?>
) {

    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    LazyColumn(
        modifier = modifier
            .testTag(LocationTestTags.LOCATION_LIST)
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
        itemsIndexed(defaultLocations){ index, location ->
            if (index == 0) {
                Row(
                    modifier = Modifier.testTag(LOCATION_ANYWHERE)
                        .padding(
                            top = dimensions.paddingMediumSmall,
                            bottom = dimensions.paddingMediumSmall
                        )
                        .clickable {
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
                ) {
                    RadioButton(
                        selected = currentLocation.value == null || currentLocation.value?.displayableName()?.isEmpty().isTrue(),
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
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            ) {
                RadioButton(
                    selected = currentLocation.value?.id() == location.id(),
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