package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.activities.compose.search.FilterMenuTestTags.OTHERS_ROW
import com.kickstarter.ui.activities.compose.search.FilterMenuTestTags.switchTag
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSIconButton
import com.kickstarter.ui.compose.designsystem.KSPillButton
import com.kickstarter.ui.compose.designsystem.KSSearchBottomSheetFooter
import com.kickstarter.ui.compose.designsystem.KSSwitch
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import io.reactivex.Observable

object FilterMenuTestTags {
    const val SHEET = "filter_menu_sheet"
    const val LIST = "filters_list"
    const val DISMISS_ROW = "dismiss_row"
    const val CATEGORY_ROW = "category_filter_row"
    const val PROJECT_STATUS_ROW = "project_status_row"
    const val PERCENTAGE_RAISED_ROW = "percentage_raised_row"
    const val AMOUNT_RAISED_ROW = "amount_raised_row"
    const val LOCATION_ROW = "location_row"
    const val OTHERS_ROW = "others_row"
    const val GOAL_ROW = "goal_row"
    const val FOOTER = "footer"

    fun pillTag(state: DiscoveryParams.State?) = "pill_${state?.name ?: "ALL"}"
    fun switchTag(param: String) = "switch_$param"
}

enum class FilterType {
    CATEGORIES,
    PROJECT_STATUS,
    LOCATION,
    PERCENTAGE_RAISED,
    AMOUNT_RAISED,
    OTHERS,
    GOAL,
}

@Composable
fun FilterMenuSheet(
    modifier: Modifier = Modifier,
    selectedProjectStatus: DiscoveryParams.State? = null,
    availableFilters: List<FilterType> = FilterType.values().toList(),
    onDismiss: () -> Unit = {},
    onApply: (DiscoveryParams.State?, Boolean, Boolean, Boolean, Boolean, Boolean?) -> Unit = { a, b, c, d, e, f -> },
    onNavigate: (FilterType) -> Unit = {},
    selectedLocation: Location? = null,
    selectedPercentage: DiscoveryParams.RaisedBuckets? = null,
    selectedAmount: DiscoveryParams.AmountBuckets? = null,
    selectedCategory: Category? = null,
    selectedRecommended: MutableState<Boolean> = mutableStateOf(false),
    selectedProjectsLoved: MutableState<Boolean> = mutableStateOf(false),
    selectedSaved: MutableState<Boolean> = mutableStateOf(false),
    selectedSocial: MutableState<Boolean> = mutableStateOf(false),
    selectedGoal: DiscoveryParams.GoalBuckets? = null
) {
    val viewModel = LocalFilterMenuViewModel.current
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val filteredFilters = if (loggedInUser) availableFilters else availableFilters.filter { it != FilterType.OTHERS }

    val projStatus = remember { mutableStateOf(selectedProjectStatus) }

    Surface(
        modifier = modifier
            .testTag(FilterMenuTestTags.SHEET),
        color = colors.backgroundSurfacePrimary
    ) {
        Column(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary)
        ) {
            FilterRow(
                onClickAction = onDismiss,
                icon = Icons.Filled.Close,
                modifier = Modifier.testTag(FilterMenuTestTags.DISMISS_ROW)
            )

            LazyColumn(modifier = Modifier.weight(1f).testTag(FilterMenuTestTags.LIST)) {
                items(filteredFilters) { filter ->
                    when (filter) {
                        FilterType.CATEGORIES -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.CATEGORIES) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.CATEGORY_ROW),
                            subText = selectedCategory?.let { it.name() }
                        )
                        FilterType.PROJECT_STATUS -> ProjectStatusRow(
                            text = titleForFilter(filter),
                            callback = { status ->
                                projStatus.value = status
                                onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, null)
                            },
                            selectedStatus = projStatus,
                            modifier = Modifier.testTag(FilterMenuTestTags.PROJECT_STATUS_ROW)
                        )
                        FilterType.LOCATION -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.LOCATION) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.LOCATION_ROW),
                            subText = selectedLocation?.displayableName()
                        )
                        FilterType.PERCENTAGE_RAISED -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.PERCENTAGE_RAISED) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW),
                            subText = selectedPercentage?.let { textForBucket(it) }
                        )
                        FilterType.AMOUNT_RAISED -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.AMOUNT_RAISED) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.AMOUNT_RAISED_ROW),
                            subText = selectedAmount?.let { textForBucket(it) }
                        )
                        FilterType.OTHERS -> OtherFiltersRow(
                            selectedStaffPicked = selectedProjectsLoved,
                            selectedRecommended = selectedRecommended,
                            selectedStarred = selectedSaved,
                            selectedSocial = selectedSocial,
                            callbackRecommended = { recommended ->
                                selectedRecommended.value = recommended
                                onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, null)
                            },
                            callbackStarred = { starred ->
                                selectedSaved.value = starred
                                onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, null)
                            },
                            callbackStaffPicked = { staffPicked ->
                                selectedProjectsLoved.value = staffPicked
                                onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, null)
                            },
                            callbackSocial = { social ->
                                selectedSocial.value = social
                                onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, null)
                            }
                        )
                        FilterType.GOAL -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.GOAL) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.GOAL_ROW),
                            subText = selectedGoal?.let { textForBucket(it) }
                        )
                    }
                }
            }

            KSSearchBottomSheetFooter(
                modifier = Modifier.testTag(FilterMenuTestTags.FOOTER),
                leftButtonIsEnabled = true,
                leftButtonClickAction = {
                    projStatus.value = null
                    selectedRecommended.value = false
                    selectedProjectsLoved.value = false
                    selectedSaved.value = false
                    selectedSocial.value = false
                    onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, false)
                },
                rightButtonOnClickAction = {
                    onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, true)
                },
                leftButtonText = stringResource(R.string.Reset_all_filters)
            )
        }
    }
}

@Composable
private fun OtherFiltersRow(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.Show_only_fpo),
    selectedStaffPicked: MutableState<Boolean> = mutableStateOf(false),
    callbackStaffPicked: (Boolean) -> Unit = {},
    selectedStarred: MutableState<Boolean> = mutableStateOf(false),
    callbackStarred: (Boolean) -> Unit = {},
    selectedSocial: MutableState<Boolean> = mutableStateOf(false),
    callbackSocial: (Boolean) -> Unit = {},
    selectedRecommended: MutableState<Boolean> = mutableStateOf(false),
    callbackRecommended: (Boolean) -> Unit = {},
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    Row(
        modifier = modifier
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
                start = dimensions.paddingLarge,
                top = dimensions.paddingLarge,
                bottom = dimensions.paddingLarge,
                end = dimensions.paddingMediumSmall
            )
    ) {
        Column(
            modifier = Modifier.testTag(OTHERS_ROW)
        ) {
            Text(
                text = text,
                style = typographyV2.headingLG,
                color = colors.textPrimary
            )

            Row(
                modifier = Modifier.testTag(DiscoveryParams::recommended.name),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = stringResource(R.string.Recommended_fpo),
                    style = typographyV2.bodyMD,
                    color = colors.textSecondary
                )

                KSSwitch(
                    modifier = Modifier.testTag(switchTag(DiscoveryParams::recommended.name)),
                    checked = selectedRecommended.value,
                    onCheckChanged = {
                        selectedRecommended.value = it
                        if (it)
                            callbackRecommended(it)
                    }
                )
            }
            Row(
                modifier = Modifier.testTag(DiscoveryParams::staffPicks.name),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = stringResource(R.string.Projects_We_Love_fpo),
                    style = typographyV2.bodyMD,
                    color = colors.textSecondary
                )

                KSSwitch(
                    modifier = Modifier.testTag(switchTag(DiscoveryParams::staffPicks.name)),
                    checked = selectedStaffPicked.value.isTrue(),
                    onCheckChanged = {
                        selectedStaffPicked.value = it
                        callbackStaffPicked(it)
                    }
                )
            }
            Row(
                modifier = Modifier.testTag(DiscoveryParams::starred.name),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = stringResource(R.string.Saved_projects_fpo),
                    style = typographyV2.bodyMD,
                    color = colors.textSecondary
                )

                KSSwitch(
                    modifier = Modifier.testTag(switchTag(DiscoveryParams::starred.name)),
                    checked = selectedStarred.value,
                    onCheckChanged = {
                        selectedStarred.value = it
                        callbackStarred(it)
                    }
                )
            }
            Row(
                modifier = Modifier.testTag(DiscoveryParams::social.name),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = stringResource(R.string.Following_fpo),
                    style = typographyV2.bodyMD,
                    color = colors.textSecondary
                )

                KSSwitch(
                    modifier = Modifier.testTag(switchTag(DiscoveryParams::social.name)),
                    checked = selectedSocial.value.isTrue(),
                    onCheckChanged = {
                        selectedSocial.value = it
                        callbackSocial(it)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectStatusRow(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.Filter),
    selectedStatus: MutableState<DiscoveryParams.State?> = mutableStateOf(null),
    callback: (DiscoveryParams.State?) -> Unit = {},
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions
    var switchChecked by remember { mutableStateOf(false) }

    val pillOptions = listOf(
        null to stringResource(R.string.Project_status_all),
        DiscoveryParams.State.LIVE to stringResource(R.string.Project_status_live),
        DiscoveryParams.State.LATE_PLEDGES to stringResource(R.string.Project_status_late_pledge),
        DiscoveryParams.State.UPCOMING to stringResource(R.string.Project_status_upcoming),
        DiscoveryParams.State.SUCCESSFUL to stringResource(R.string.Project_status_successful)
    )

    Row(
        modifier = modifier
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
                start = dimensions.paddingLarge,
                top = dimensions.paddingLarge,
                bottom = dimensions.paddingLarge,
                end = dimensions.paddingMediumSmall
            )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(KSTheme.dimensions.listItemSpacingSmall)) {
            Text(
                text = text,
                style = typographyV2.headingLG,
                color = colors.textPrimary
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pillOptions.forEach { (state, label) ->
                    KSPillButton(
                        text = label,
                        isSelected = selectedStatus.value == state,
                        modifier = Modifier.testTag(FilterMenuTestTags.pillTag(state)),
                        onClick = {
                            val newSelection = if (selectedStatus.value == state) null else state
                            selectedStatus.value = newSelection
                            callback(newSelection)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.height(0.dp), // placeholder for future API support
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.Include_ended_projects),
                    style = typographyV2.body,
                    color = colors.textPrimary
                )

                CustomSwitch(
                    checked = switchChecked,
                    onCheckedChange = {
                        switchChecked = it
                        if (switchChecked) {
                            callback(DiscoveryParams.State.SUCCESSFUL)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterRow(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.Filter),
    onClickAction: () -> Unit,
    icon: ImageVector,
    subText: String? = null
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions
    Row(
        modifier = modifier
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
                start = dimensions.paddingLarge,
                top = dimensions.paddingLarge,
                bottom = dimensions.paddingLarge,
                end = dimensions.paddingMediumSmall
            )
            .clickable { onClickAction.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val style = if (text == stringResource(R.string.Filter)) {
            typographyV2.headingXL
        } else {
            typographyV2.headingLG
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text,
                style = style,
                color = colors.textPrimary
            )

            if (subText != null) {
                Text(
                    modifier = Modifier.padding(
                        top = dimensions.paddingSmall
                    ),
                    text = subText,
                    style = typographyV2.bodyMD,
                    color = colors.textSecondary
                )
            }
        }

        KSIconButton(
            modifier = Modifier.testTag(text),
            onClick = {
                onClickAction.invoke()
            },
            imageVector = icon
        )
    }
}

@Composable
private fun titleForFilter(filter: FilterType): String {
    return when (filter) {
        FilterType.CATEGORIES -> stringResource(R.string.Category)
        FilterType.PROJECT_STATUS -> stringResource(R.string.Project_status)
        FilterType.LOCATION -> stringResource(R.string.Location_fpo)
        FilterType.PERCENTAGE_RAISED -> stringResource(R.string.Percentage_raised)
        FilterType.AMOUNT_RAISED -> stringResource(R.string.Amount_raised_fpo)
        FilterType.GOAL -> stringResource(R.string.Goal_fpo)
        FilterType.OTHERS -> stringResource(R.string.Show_only_fpo)
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ProjectStatusRowPreview() {
    KSTheme {
        ProjectStatusRow(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
            text = titleForFilter(FilterType.PROJECT_STATUS)
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun OthersRowPreview() {
    KSTheme {
        OtherFiltersRow(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
            text = titleForFilter(FilterType.OTHERS)
        )
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FilterMenuSheetPreview_UserLoggedOut() {
    // Mocked user holder
    val mockUser = object : CurrentUserTypeV2() {
        override fun setToken(accessToken: String) {
        }

        override fun login(newUser: User) {
        }

        override fun logout() {
        }

        override val accessToken: String?
            get() = null

        override fun refresh(freshUser: User) {
        }

        override fun observable(): Observable<KsOptional<User>> {
            return Observable.just(KsOptional.empty())
        }

        override fun getUser(): User? {
            return null
        }
    }
    val env = Environment.builder()
        .apolloClientV2(MockApolloClientV2())
        .currentUserV2(mockUser)
        .build()
    val fakeViewModel = FilterMenuViewModel(environment = env)
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
            FilterMenuSheet(
                selectedProjectStatus = DiscoveryParams.State.LIVE,
                onDismiss = {},
                onApply = { a, b, c, d, e, f -> },
                selectedLocation = LocationFactory.vancouver(),
                selectedAmount = DiscoveryParams.AmountBuckets.BUCKET_4
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FilterMenuSheetPreview_UserLoggedIn() {

    // Mocked user holder
    val mockUser = object : CurrentUserTypeV2() {
        private var user = UserFactory.user()
        override fun setToken(accessToken: String) {
        }

        override fun login(newUser: User) {
        }

        override fun logout() {
        }

        override val accessToken: String?
            get() = "Token"

        override fun refresh(freshUser: User) {
            user = freshUser
        }

        override fun observable(): Observable<KsOptional<User>> {
            return Observable.just(KsOptional.of(user))
        }

        override fun getUser(): User? {
            return null
        }
    }

    val env = Environment.builder()
        .apolloClientV2(MockApolloClientV2())
        .currentUserV2(mockUser)
        .build()

    val fakeViewModel = FilterMenuViewModel(environment = env)
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
            FilterMenuSheet(
                selectedProjectStatus = DiscoveryParams.State.LIVE,
                onDismiss = {},
                onApply = { a, b, c, d, e, f -> },
                selectedLocation = LocationFactory.vancouver(),
                selectedAmount = DiscoveryParams.AmountBuckets.BUCKET_4
            )
        }
    }
}
