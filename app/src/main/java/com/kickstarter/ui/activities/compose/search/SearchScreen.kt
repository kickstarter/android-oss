package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue.Hidden
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.deadlineCountdownDetail
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isLatePledgesActive
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.toDiscoveryParamsList
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Photo
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.type.ProjectSort
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSHeadsupSnackbar
import com.kickstarter.ui.compose.designsystem.KSProjectCardLarge
import com.kickstarter.ui.compose.designsystem.KSProjectCardSmall
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PagerPreview() {
    KSTheme {
        val testPagerState = rememberPagerState(initialPage = 0, pageCount = { FilterPages.values().size })
        val testSheetState = rememberModalBottomSheetState(
            initialValue = Hidden,
            skipHalfExpanded = true
        )

        val categories = CategoryFactory.rootCategories()
        val selectedStatus = DiscoveryParams.State.LIVE

        val appliedFilters = mutableListOf<Pair<DiscoveryParams.State?, Category?>>()
        val dismissed = mutableListOf<Boolean>()
        val selectedCounts = mutableListOf<Pair<Int?, Int?>>()

        Box(modifier = Modifier.size(400.dp)) {
            FilterPagerSheet(
                selectedProjectStatus = selectedStatus,
                currentCategory = categories[0],
                categories = categories,
                onDismiss = { dismissed.add(true) },
                onApply = { state, category, bucket, location, amountBucket, _, _, _, _, goalBucket -> appliedFilters.add(Pair(state, category)) },
                updateSelectedCounts = { statusCount, categoryCount, raisedBucket, location, amountBucketCount, _, _, _, _, goalBucketCount ->
                    selectedCounts.add(
                        statusCount to categoryCount
                    )
                },
                pagerState = testPagerState,
                sheetState = testSheetState,
                shouldShowPhase = true
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchScreenPreviewNonEmpty() {
    KSTheme {
        SearchScreen(
            onBackClicked = { },
            scaffoldState = rememberScaffoldState(),
            errorSnackBarHostState = SnackbarHostState(),
            isLoading = false,
            isDefaultList = true,
            itemsList = List(100) {
                Project.builder()
                    .name("This is a test $it")
                    .pledged((it * 2).toDouble())
                    .photo(Photo.builder().altText("").full("").build())
                    .goal(100.0)
                    .state(if (it in 10..20) Project.STATE_SUBMITTED else Project.STATE_LIVE)
                    .build()
            },
            lazyColumnListState = rememberLazyListState(),
            showEmptyView = false,
            categories = listOf(),
            onSearchTermChanged = {},
            onItemClicked = { project -> }
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchScreenPreviewEmpty() {
    KSTheme {
        SearchScreen(
            onBackClicked = { },
            scaffoldState = rememberScaffoldState(),
            errorSnackBarHostState = SnackbarHostState(),
            isLoading = true,
            itemsList = listOf(),
            lazyColumnListState = rememberLazyListState(),
            showEmptyView = true,
            categories = listOf(),
            onSearchTermChanged = {},
            onItemClicked = { project -> }
        )
    }
}

enum class SearchScreenTestTag {
    BACK_BUTTON,
    SEARCH_TEXT_INPUT,
    EMPTY_VIEW,
    LOADING_VIEW,
    IN_LIST_LOADING_VIEW,
    LIST_VIEW,
    DISCOVER_PROJECTS_TITLE,
    FEATURED_PROJECT_VIEW,
    NORMAL_PROJECT_VIEW,
}

enum class CardProjectState {
    LIVE,
    LATE_PLEDGES_ACTIVE,
    LAUNCHING_SOON,
    ENDED_SUCCESSFUL,
    ENDED_UNSUCCESSFUL
}

fun getCardProjectState(project: Project): CardProjectState {
    return if (project.isSuccessful && !project.isLatePledgesActive())
        CardProjectState.ENDED_SUCCESSFUL
    else if (project.isFailed)
        CardProjectState.ENDED_UNSUCCESSFUL
    else if (project.isLatePledgesActive())
        CardProjectState.LATE_PLEDGES_ACTIVE
    else if (project.prelaunchActivated().isTrue() && !project.isLive)
        CardProjectState.LAUNCHING_SOON
    else if (project.isLive)
        CardProjectState.LIVE
    else {
        CardProjectState.LIVE
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchScreen(
    environment: Environment? = null,
    onBackClicked: () -> Unit,
    scaffoldState: ScaffoldState,
    errorSnackBarHostState: SnackbarHostState = SnackbarHostState(),
    isDefaultList: Boolean = true,
    isLoading: Boolean,
    itemsList: List<Project> = listOf(),
    lazyColumnListState: LazyListState,
    showEmptyView: Boolean,
    categories: List<Category>,
    onSearchTermChanged: (String) -> Unit,
    onItemClicked: (Project) -> Unit,
    onApplySearchWithParams: (
        Category?,
        DiscoveryParams.Sort?,
        DiscoveryParams.State?,
        DiscoveryParams.RaisedBuckets?,
        Location?,
        DiscoveryParams.AmountBuckets?,
        Boolean,
        Boolean,
        Boolean,
        Boolean,
        DiscoveryParams.GoalBuckets?
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ ->
    },
    shouldShowPhase: Boolean = true
) {
    var currentSearchTerm by rememberSaveable { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    val countApiIsReady = false // Hide all result counts until backend API is ready

    val selectedFilterCounts: SnapshotStateMap<String, Int> = remember {
        mutableStateMapOf(
            FilterRowPillType.SORT.name to 0,
            FilterRowPillType.CATEGORY.name to 0,
            FilterRowPillType.FILTER.name to 0,
            FilterRowPillType.PROJECT_STATUS.name to 0,
            FilterRowPillType.PERCENTAGE_RAISED.name to 0,
            FilterRowPillType.LOCATION.name to 0,
            FilterRowPillType.AMOUNT_RAISED.name to 0,
            FilterRowPillType.RECOMMENDED.name to 0,
            FilterRowPillType.PROJECTS_LOVED.name to 0,
            FilterRowPillType.SAVED.name to 0,
            FilterRowPillType.FOLLOWING.name to 0,
            FilterRowPillType.GOAL.name to 0
        )
    }

    val initialCategoryPillText = stringResource(R.string.Category)
    val categoryPillText = remember { mutableStateOf(initialCategoryPillText) }

    val initialProjectStatsPillText = stringResource(R.string.Project_status)
    val projectStatusPill = remember { mutableStateOf(initialProjectStatsPillText) }

    val currentSort = remember { mutableStateOf(DiscoveryParams.Sort.MAGIC) }
    val currentCategory = remember { mutableStateOf<Category?>(null) }

    val currentProjectState = remember { mutableStateOf<DiscoveryParams.State?>(null) }
    val currentPercentage = remember { mutableStateOf<DiscoveryParams.RaisedBuckets?>(null) }
    val currentAmountRaised = remember { mutableStateOf<DiscoveryParams.AmountBuckets?>(null) }
    val currentLocation = remember { mutableStateOf<Location?>(null) }

    val currentStaffPicked = remember { mutableStateOf<Boolean>(false) }
    val currentStarred = remember { mutableStateOf<Boolean>(false) }
    val currentSocial = remember { mutableStateOf<Boolean>(false) }
    val currentRecommended = remember { mutableStateOf<Boolean>(false) }

    val currentGoal = remember { mutableStateOf<DiscoveryParams.GoalBuckets?>(null) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { FilterPages.values().size })

    val activeBottomSheet = remember {
        mutableStateOf<FilterRowPillType?>(null)
    }

    val sortSheetState = rememberModalBottomSheetState(
        initialValue = Hidden,
        skipHalfExpanded = false
    )

    val mainFilterMenuState = rememberModalBottomSheetState(
        initialValue = Hidden,
        skipHalfExpanded = true
    )

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState(
            activeBottomSheet,
            sortSheetState,
            mainFilterMenuState
        ),
        sheetContent = sheetContent(
            activeBottomSheet,
            coroutineScope,
            currentCategory,
            onDismissBottomSheet = onApplySearchWithParams,
            currentSort,
            currentProjectState,
            categories,
            categoryPillText,
            projectStatusPill,
            initialCategoryPillText,
            selectedFilterCounts,
            countApiIsReady,
            sortSheetState,
            mainFilterMenuState,
            pagerState,
            currentPercentage = currentPercentage,
            currentLocation = currentLocation,
            currentAmountRaised = currentAmountRaised,
            currentRecommended = currentRecommended,
            currentProjectsLoved = currentStaffPicked,
            currentSavedProjects = currentStarred,
            currentFollowing = currentSocial,
            currentGoal = currentGoal,
            shouldShowPhase = shouldShowPhase
        ),
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetBackgroundColor = colors.kds_white
    ) {
        Scaffold(
            modifier = Modifier.systemBarsPadding(),
            scaffoldState = scaffoldState,
            snackbarHost = {
                SnackbarHost(
                    modifier = Modifier.padding(dimensions.paddingSmall),
                    hostState = errorSnackBarHostState,
                    snackbar = { data ->
                        if (data.actionLabel == KSSnackbarTypes.KS_ERROR.name) {
                            KSErrorSnackbar(text = data.message)
                        } else {
                            KSHeadsupSnackbar(text = data.message)
                        }
                    }
                )
            },
            topBar = {
                Surface(elevation = 3.dp) {
                    SearchTopBar(
                        countApiIsReady = countApiIsReady,
                        categoryPillText = categoryPillText.value,
                        onBackPressed = onBackClicked,
                        projectStatusText = projectStatusPill.value,
                        percentageRaisedText = currentPercentage.value?.let { textForBucket(it) } ?: stringResource(R.string.Percentage_raised),
                        locationText = currentLocation.value?.displayableName() ?: stringResource(R.string.Location_fpo),
                        amountRaisedText = currentAmountRaised.value?.let { textForBucket(it) } ?: stringResource(R.string.Amount_raised_fpo),
                        goalText = currentGoal.value?.let { textForBucket(it) } ?: stringResource(R.string.Goal_fpo),
                        recommendedStatus = currentRecommended,
                        projectsLovedStatus = currentStaffPicked,
                        savedProjects = currentStarred,
                        following = currentSocial,
                        onValueChanged = {
                            onSearchTermChanged.invoke(it)
                            currentSearchTerm = it
                        },
                        selectedFilterCounts = selectedFilterCounts,
                        onPillPressedOpensBottomSheet = onPillPressedOpensBottomSheet(
                            activeBottomSheet,
                            coroutineScope,
                            sortSheetState,
                            mainFilterMenuState,
                            pagerState
                        ),
                        onPillPressedShowOnlyToggles = { rowPillType, value ->
                            selectedFilterCounts[rowPillType.name] = if (value) 1 else 0
                            val total = selectedFilterCounts[FilterRowPillType.FILTER.name] ?: 0
                            selectedFilterCounts[FilterRowPillType.FILTER.name] = if (value) total + 1 else total - 1

                            when (rowPillType) {
                                FilterRowPillType.RECOMMENDED -> currentRecommended.value = value
                                FilterRowPillType.PROJECTS_LOVED -> currentStaffPicked.value = value
                                FilterRowPillType.SAVED -> currentStarred.value = value
                                FilterRowPillType.FOLLOWING -> currentSocial.value = value
                                else -> {
                                    // Other pills open bottomSheet, handled in onPillPressed
                                }
                            }
                            onApplySearchWithParams(
                                currentCategory.value,
                                currentSort.value,
                                currentProjectState.value,
                                currentPercentage.value,
                                currentLocation.value,
                                currentAmountRaised.value,
                                currentRecommended.value,
                                currentStaffPicked.value,
                                currentStarred.value,
                                currentSocial.value,
                                currentGoal.value
                            )
                        },
                        shouldShowPhase = shouldShowPhase
                    )
                }
            },
            backgroundColor = colors.kds_white
        ) { padding ->
            if (showEmptyView) {
                SearchEmptyView(
                    modifier = Modifier
                        .testTag(SearchScreenTestTag.EMPTY_VIEW.name)
                        .background(colors.backgroundSurfaceSecondary),
                    environment = environment,
                    currentSearchTerm = currentSearchTerm
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .testTag(SearchScreenTestTag.LIST_VIEW.name)
                        .padding(padding)
                        .background(colors.backgroundSurfaceSecondary)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = dimensions.paddingMediumLarge,
                        end = dimensions.paddingMediumLarge
                    ),
                    state = lazyColumnListState,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    itemsIndexed(itemsList) { index, project ->
                        if (index == 0 && isDefaultList) {
                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                            Text(
                                modifier = Modifier
                                    .testTag(SearchScreenTestTag.DISCOVER_PROJECTS_TITLE.name)
                                    .fillMaxWidth(),
                                text = stringResource(id = R.string.activity_empty_state_logged_in_button),
                                style = typographyV2.title2,
                                color = colors.kds_support_700,
                                textAlign = TextAlign.Start
                            )
                        }

                        val state = getCardProjectState(project)
                        val fundingInfoString = getFundingInfoString(state, environment, project)

                        if (index == 0) {
                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                            KSProjectCardLarge(
                                modifier = Modifier
                                    .testTag(SearchScreenTestTag.FEATURED_PROJECT_VIEW.name),
                                photo = project.photo(),
                                title = project.name(),
                                state = state,
                                fundingInfoString = fundingInfoString,
                                fundedPercentage = project.percentageFunded().toInt(),
                            ) {
                                onItemClicked(project)
                            }

                            if (itemsList.size > 1) {
                                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                            }
                        } else {
                            KSProjectCardSmall(
                                modifier = Modifier
                                    .testTag(SearchScreenTestTag.NORMAL_PROJECT_VIEW.name + index),
                                photo = project.photo(),
                                title = project.name(),
                                state = state,
                                fundingInfoString = fundingInfoString,
                                fundedPercentage = project.percentageFunded().toInt(),
                            ) {
                                onItemClicked(project)
                            }

                            if (index < itemsList.size - 1) {
                                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                            } else {
                                Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                            }
                        }
                    }

                    item(isLoading) {
                        if (isLoading && itemsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                            KSCircularProgressIndicator(
                                modifier = Modifier
                                    .testTag(SearchScreenTestTag.IN_LIST_LOADING_VIEW.name)
                                    .size(size = dimensions.imageSizeLarge)
                            )

                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                        }
                    }
                }
            }

            if (isLoading && itemsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .testTag(SearchScreenTestTag.LOADING_VIEW.name)
                        .fillMaxSize()
                        .background(color = colors.kds_black.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    KSCircularProgressIndicator()
                }
            }
        }
    }
}

enum class FilterPages {
    MAIN_FILTER,
    CATEGORIES,
    PERCENTAGE_RAISED,
    LOCATION,
    AMOUNT_RAISED,
    GOAL
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterPagerSheet(
    selectedProjectStatus: DiscoveryParams.State?,
    currentCategory: Category?,
    categories: List<Category>,
    currentPercentage: DiscoveryParams.RaisedBuckets? = null,
    currentLocation: Location? = null,
    currentAmountRaised: DiscoveryParams.AmountBuckets? = null,
    currentGoal: DiscoveryParams.GoalBuckets? = null,
    currentRecommended: MutableState<Boolean> = remember { mutableStateOf(false) },
    currentProjectsLoved: MutableState<Boolean> = remember { mutableStateOf(false) },
    currentSavedProjects: MutableState<Boolean> = remember { mutableStateOf(false) },
    currentFollowing: MutableState<Boolean> = remember { mutableStateOf(false) },
    onDismiss: () -> Unit,
    onApply: (
        DiscoveryParams.State?,
        Category?,
        DiscoveryParams.RaisedBuckets?,
        Location?,
        DiscoveryParams.AmountBuckets?,
        Boolean,
        Boolean,
        Boolean,
        Boolean,
        DiscoveryParams.GoalBuckets?
    ) -> Unit,
    updateSelectedCounts: (
        projectStatusCount: Int?,
        categoryCount: Int?,
        raisedBucketCount: Int?,
        location: Int?,
        amountBucket: Int?,
        recommended: Int?,
        projectsLoved: Int?,
        savedProjects: Int?,
        social: Int?,
        goalBucket: Int?
    ) -> Unit,
    pagerState: PagerState,
    sheetState: ModalBottomSheetState,
    shouldShowPhase: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val category = remember { mutableStateOf(currentCategory) }
    val projectState = remember { mutableStateOf(selectedProjectStatus) }
    val percentageBucket = remember { mutableStateOf(currentPercentage) }
    val location = remember { mutableStateOf(currentLocation) }
    val amountRaised = remember { mutableStateOf(currentAmountRaised) }
    val goal = remember { mutableStateOf(currentGoal) }

    // - In case de bottomSheet is dismissed without the user pressing
    // - see results button, should return to default states
    LaunchedEffect(!sheetState.isVisible) {
        coroutineScope.launch {
            if (currentCategory != category.value) {
                category.value = currentCategory
            }

            if (selectedProjectStatus != projectState.value) {
                projectState.value = selectedProjectStatus
            }

            if (currentPercentage != percentageBucket.value) {
                percentageBucket.value = currentPercentage
            }

            if (currentLocation != location.value) {
                location.value = currentLocation
            }

            if (currentAmountRaised != amountRaised.value) {
                amountRaised.value = currentAmountRaised
            }

            if (currentGoal != goal.value) {
                goal.value = currentGoal
            }
        }
    }

    HorizontalPager(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .heightIn(
                min = dimensions.bottomSheetMinHeight,
                max = dimensions.bottomSheetMaxHeight
            ),
        state = pagerState,

    ) { page ->
        when (page) {
            FilterPages.MAIN_FILTER.ordinal -> FilterMenuSheet(
                selectedLocation = currentLocation,
                selectedPercentage = currentPercentage,
                selectedProjectStatus = projectState.value,
                selectedAmount = currentAmountRaised,
                selectedCategory = currentCategory,
                selectedRecommended = currentRecommended,
                selectedProjectsLoved = currentProjectsLoved,
                selectedSaved = currentSavedProjects,
                selectedSocial = currentFollowing,
                selectedGoal = currentGoal,
                onDismiss = {
                    onDismiss.invoke()
                },
                onApply = { selectedProjectState, selectedRecommended, selectedProjectsLoved, selectedSaved, selectedSocial, applyAndDismiss ->
                    projectState.value = selectedProjectState
                    currentRecommended.value = selectedRecommended
                    currentProjectsLoved.value = selectedProjectsLoved
                    currentSavedProjects.value = selectedSaved
                    currentFollowing.value = selectedSocial

                    if (applyAndDismiss != null) {
                        // - Reset to default values
                        if (applyAndDismiss.isFalse()) {
                            category.value = null
                            percentageBucket.value = null
                            location.value = null
                            amountRaised.value = null
                            goal.value = null
                        }
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            goalBucket = goal.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss,
                        )
                    }
                },
                onNavigate = { filterType ->
                    if (filterType == FilterType.CATEGORIES) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(FilterPages.CATEGORIES.ordinal)
                        }
                    }

                    if (filterType == FilterType.PERCENTAGE_RAISED) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(FilterPages.PERCENTAGE_RAISED.ordinal)
                        }
                    }

                    if (filterType == FilterType.LOCATION) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(FilterPages.LOCATION.ordinal)
                        }
                    }

                    if (filterType == FilterType.AMOUNT_RAISED) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(FilterPages.AMOUNT_RAISED.ordinal)
                        }
                    }
                    if (filterType == FilterType.GOAL) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(FilterPages.GOAL.ordinal)
                        }
                    }
                },
                availableFilters = if (shouldShowPhase) FilterType.values().asList()
                else FilterType.values().asList().filter { it != FilterType.OTHERS }
            )

            FilterPages.CATEGORIES.ordinal -> CategorySelectionSheet(
                onNavigate = {
                    coroutineScope.launch { pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal) }
                },
                currentCategory = category.value,
                onDismiss = onDismiss,
                categories = categories,
                onApply = { selectedCategory, applyAndDismiss ->
                    category.value = selectedCategory
                    if (applyAndDismiss != null) {
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            goalBucket = goal.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss
                        )
                    }
                },
                isLoading = false
            )

            FilterPages.LOCATION.ordinal -> LocationSheet(
                selectedLocation = currentLocation,
                onNavigate = {
                    coroutineScope.launch { pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal) }
                },
                onDismiss = onDismiss,
                onApply = { selLocation, applyAndDismiss ->
                    location.value = selLocation
                    if (applyAndDismiss != null) {
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            goalBucket = goal.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss
                        )
                    }
                }
            )

            FilterPages.PERCENTAGE_RAISED.ordinal -> PercentageRaisedSheet(
                currentPercentage = DiscoveryParams.RaisedBuckets.fromString(currentPercentage?.name),
                onNavigate = {
                    coroutineScope.launch { pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal) }
                },
                onDismiss = onDismiss,
                onApply = { selectedBucket, applyAndDismiss ->
                    percentageBucket.value = selectedBucket
                    if (applyAndDismiss != null) {
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            goalBucket = goal.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss
                        )
                    }
                }
            )

            FilterPages.AMOUNT_RAISED.ordinal -> AmountRaisedSheet(
                currentBucket = DiscoveryParams.AmountBuckets.fromString(currentAmountRaised?.name),
                onNavigate = {
                    coroutineScope.launch { pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal) }
                },
                onDismiss = onDismiss,
                onApply = { selectedBucket, applyAndDismiss ->
                    amountRaised.value = selectedBucket
                    if (applyAndDismiss != null) {
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            goalBucket = goal.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss
                        )
                    }
                }
            )

            FilterPages.GOAL.ordinal -> GoalSheet(
                currentGoalBucket = DiscoveryParams.GoalBuckets.fromString(currentGoal?.name),
                onNavigate = {
                    coroutineScope.launch { pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal) }
                },
                onDismiss = onDismiss,
                onApply = { selectedBucket, applyAndDismiss ->
                    goal.value = selectedBucket
                    if (applyAndDismiss != null) {
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            goalBucket = goal.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss
                        )
                    }
                }
            )
        }
    }
}

/**
 * Applies user selection.
 * @param shouldDismiss the context for this value refers to which button in the footer the user has pressed,
 * it applies to both FilterMenu screen and CategorySelection screen
 *  shouldDismiss = false -> user has pressed "Reset" button on the footer. (Not dismiss bottomSheet).
 *  shouldDismiss = true -> user has pressed "See results" button on the footer. (Should dismiss bottomSheet).
 */
private fun applyUserSelection(
    onApply: (
        DiscoveryParams.State?,
        Category?,
        DiscoveryParams.RaisedBuckets?,
        Location?,
        DiscoveryParams.AmountBuckets?,
        recommended: Boolean,
        projectsLoved: Boolean,
        savedProjects: Boolean,
        social: Boolean,
        goalBucket: DiscoveryParams.GoalBuckets?
    ) -> Unit,
    projectState: DiscoveryParams.State?,
    category: Category?,
    location: Location?,
    percentageRaisedBucket: DiscoveryParams.RaisedBuckets?,
    amountRaisedBucket: DiscoveryParams.AmountBuckets?,
    recommended: Boolean,
    projectsLoved: Boolean,
    savedProjects: Boolean,
    social: Boolean,
    goalBucket: DiscoveryParams.GoalBuckets?,
    updateSelectedCounts: (
        projectStatusCount: Int?,
        categoryCount: Int?,
        raisedBucketCount: Int?,
        location: Int?,
        amountBucketCount: Int?,
        recommended: Int?,
        projectsLoved: Int?,
        savedProjects: Int?,
        social: Int?,
        goalBucketCount: Int?
    ) -> Unit,
    onDismiss: () -> Unit,
    shouldDismiss: Boolean
) {
    onApply(
        projectState,
        category,
        percentageRaisedBucket,
        location,
        amountRaisedBucket,
        recommended,
        projectsLoved,
        savedProjects,
        social,
        goalBucket
    )

    updateSelectedCounts(
        if (projectState != null) 1 else 0,
        if (category != null) 1 else 0,
        if (percentageRaisedBucket != null) 1 else 0,
        if (location != null) 1 else 0,
        if (amountRaisedBucket != null) 1 else 0,
        if (recommended) 1 else 0,
        if (projectsLoved) 1 else 0,
        if (savedProjects) 1 else 0,
        if (social) 1 else 0,
        if (goalBucket != null) 1 else 0
    )
    if (shouldDismiss) {
        onDismiss.invoke()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun onPillPressedOpensBottomSheet(
    activeBottomSheet: MutableState<FilterRowPillType?>,
    coroutineScope: CoroutineScope,
    sortSheetState: ModalBottomSheetState,
    mainFilterMenuState: ModalBottomSheetState,
    pagerState: PagerState
): (FilterRowPillType) -> Unit =
    { filterRowPillType ->
        activeBottomSheet.value = filterRowPillType
        when (filterRowPillType) {
            FilterRowPillType.SORT -> coroutineScope.launch {
                sortSheetState.show()
            }
            FilterRowPillType.FILTER,
            FilterRowPillType.PROJECT_STATUS -> {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal)
                    mainFilterMenuState.show()
                }
            }

            FilterRowPillType.CATEGORY -> {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(FilterPages.CATEGORIES.ordinal)
                    mainFilterMenuState.show()
                }
            }

            FilterRowPillType.LOCATION -> {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(FilterPages.LOCATION.ordinal)
                    mainFilterMenuState.show()
                }
            }

            FilterRowPillType.PERCENTAGE_RAISED -> {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(FilterPages.PERCENTAGE_RAISED.ordinal)
                    mainFilterMenuState.show()
                }
            }

            FilterRowPillType.AMOUNT_RAISED -> {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(FilterPages.AMOUNT_RAISED.ordinal)
                    mainFilterMenuState.show()
                }
            }

            FilterRowPillType.GOAL -> {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(FilterPages.GOAL.ordinal)
                    mainFilterMenuState.show()
                }
            }
            FilterRowPillType.SAVED,
            FilterRowPillType.PROJECTS_LOVED,
            FilterRowPillType.FOLLOWING,
            FilterRowPillType.RECOMMENDED -> {
                // Dealt in another callback, this ones do not open BottomSheet
            }
        }
    }

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun sheetContent(
    activeBottomSheet: MutableState<FilterRowPillType?>,
    coroutineScope: CoroutineScope,
    currentCategory: MutableState<Category?>,
    onDismissBottomSheet: (
        Category?,
        DiscoveryParams.Sort?,
        DiscoveryParams.State?,
        DiscoveryParams.RaisedBuckets?,
        Location?,
        DiscoveryParams.AmountBuckets?,
        Boolean,
        Boolean,
        Boolean,
        Boolean,
        DiscoveryParams.GoalBuckets?
    ) -> Unit,
    currentSort: MutableState<DiscoveryParams.Sort>,
    currentProjectState: MutableState<DiscoveryParams.State?>,
    categories: List<Category>,
    categoryPillText: MutableState<String>,
    projectStatusPillText: MutableState<String>,
    initialCategoryPillText: String,
    selectedFilterCounts: SnapshotStateMap<String, Int>,
    countApiIsReady: Boolean,
    sortSheetState: ModalBottomSheetState,
    menuSheetState: ModalBottomSheetState,
    pagerState: PagerState,
    currentPercentage: MutableState<DiscoveryParams.RaisedBuckets?>,
    currentAmountRaised: MutableState<DiscoveryParams.AmountBuckets?>,
    currentGoal: MutableState<DiscoveryParams.GoalBuckets?>,
    currentLocation: MutableState<Location?> = mutableStateOf(null),
    currentRecommended: MutableState<Boolean> = mutableStateOf(false),
    currentProjectsLoved: MutableState<Boolean> = mutableStateOf(false),
    currentSavedProjects: MutableState<Boolean> = mutableStateOf(false),
    currentFollowing: MutableState<Boolean> = mutableStateOf(false),
    shouldShowPhase: Boolean = true
): @Composable() (ColumnScope.() -> Unit) {
    val liveString = stringResource(R.string.Project_status_live)
    val successfulString = stringResource(R.string.Project_status_successful)
    val upcomingString = stringResource(R.string.Project_status_upcoming)
    val latePledgeString = stringResource(R.string.Project_status_late_pledge)
    val defaultString = stringResource(R.string.Project_status)

    return {
        when (activeBottomSheet.value) {
            FilterRowPillType.CATEGORY,
            FilterRowPillType.PROJECT_STATUS,
            FilterRowPillType.PERCENTAGE_RAISED,
            FilterRowPillType.LOCATION,
            FilterRowPillType.AMOUNT_RAISED,
            FilterRowPillType.RECOMMENDED,
            FilterRowPillType.PROJECTS_LOVED,
            FilterRowPillType.SAVED,
            FilterRowPillType.FOLLOWING,
            FilterRowPillType.GOAL,
            FilterRowPillType.FILTER -> {
                FilterPagerSheet(
                    sheetState = menuSheetState,
                    pagerState = pagerState,
                    selectedProjectStatus = currentProjectState.value,
                    currentCategory = currentCategory.value,
                    currentPercentage = currentPercentage.value,
                    currentLocation = currentLocation.value,
                    currentAmountRaised = currentAmountRaised.value,
                    currentRecommended = currentRecommended,
                    currentProjectsLoved = currentProjectsLoved,
                    currentSavedProjects = currentSavedProjects,
                    currentFollowing = currentFollowing,
                    currentGoal = currentGoal.value,
                    categories = categories,
                    onDismiss = {
                        coroutineScope.launch { menuSheetState.hide() }
                    },
                    onApply = { project, category, percentageBucket, location, amountRaisedBucket, recommended, projectsLoved, savedProjects, following, goalBucket ->
                        currentProjectState.value = project
                        currentCategory.value = category
                        currentPercentage.value = percentageBucket
                        currentLocation.value = location
                        currentAmountRaised.value = amountRaisedBucket
                        currentRecommended.value = recommended
                        currentProjectsLoved.value = projectsLoved
                        currentSavedProjects.value = savedProjects
                        currentFollowing.value = following
                        currentGoal.value = goalBucket
                        projectStatusPillText.value = when (project) {
                            DiscoveryParams.State.LIVE -> liveString
                            DiscoveryParams.State.SUCCESSFUL -> successfulString
                            DiscoveryParams.State.UPCOMING -> upcomingString
                            DiscoveryParams.State.LATE_PLEDGES -> latePledgeString
                            else -> defaultString
                        }
                        categoryPillText.value = category?.name() ?: initialCategoryPillText
                        onDismissBottomSheet(
                            currentCategory.value,
                            currentSort.value,
                            currentProjectState.value,
                            currentPercentage.value,
                            currentLocation.value,
                            currentAmountRaised.value,
                            currentRecommended.value,
                            currentProjectsLoved.value,
                            currentSavedProjects.value,
                            currentFollowing.value,
                            currentGoal.value
                        )
                    },
                    updateSelectedCounts = { statusCount, categoryCount, raisedBucket, location, amountBucket, recommended, projectsLoved, savedProjects, following, goalBucket ->

                        selectedFilterCounts[FilterRowPillType.FILTER.name] =
                            (statusCount ?: 0) + (categoryCount ?: 0) + (raisedBucket ?: 0) +
                            (location ?: 0) + (amountBucket ?: 0) +
                            (if (currentRecommended.value.isTrue()) 1 else 0) +
                            (if (currentProjectsLoved.value.isTrue()) 1 else 0) +
                            (if (currentSavedProjects.value.isTrue()) 1 else 0) +
                            (if (currentFollowing.value.isTrue()) 1 else 0) +
                            (goalBucket ?: 0)

                        statusCount?.let {
                            selectedFilterCounts[FilterRowPillType.PROJECT_STATUS.name] = it
                        }
                        categoryCount?.let {
                            selectedFilterCounts[FilterRowPillType.CATEGORY.name] = it
                        }
                        raisedBucket?.let {
                            selectedFilterCounts[FilterRowPillType.PERCENTAGE_RAISED.name] = it
                        }
                        location?.let {
                            selectedFilterCounts[FilterRowPillType.LOCATION.name] = it
                        }
                        amountBucket?.let {
                            selectedFilterCounts[FilterRowPillType.AMOUNT_RAISED.name] = it
                        }
                        recommended?.let {
                            selectedFilterCounts[FilterRowPillType.RECOMMENDED.name] =
                                if (currentRecommended.value.isTrue()) 1 else 0
                        }
                        projectsLoved?.let {
                            selectedFilterCounts[FilterRowPillType.PROJECTS_LOVED.name] =
                                if (currentProjectsLoved.value.isTrue()) 1 else 0
                        }
                        savedProjects?.let {
                            selectedFilterCounts[FilterRowPillType.SAVED.name] =
                                if (currentSavedProjects.value.isTrue()) 1 else 0
                        }
                        following?.let {
                            selectedFilterCounts[FilterRowPillType.FOLLOWING.name] =
                                if (currentFollowing.value.isTrue()) 1 else 0
                        }
                        goalBucket?.let {
                            selectedFilterCounts[FilterRowPillType.GOAL.name] = it
                        }
                    },
                    shouldShowPhase = shouldShowPhase
                )
            }

            FilterRowPillType.SORT -> {
                SortSelectionBottomSheet(
                    currentSelection = currentSort.value,
                    sorts = ProjectSort.knownValues().toDiscoveryParamsList(),
                    onDismiss = { sort ->
                        currentSort.value = sort
                        coroutineScope.launch { sortSheetState.hide() }
                        onDismissBottomSheet(
                            currentCategory.value,
                            sort,
                            currentProjectState.value,
                            currentPercentage.value,
                            currentLocation.value,
                            currentAmountRaised.value,
                            currentRecommended.value,
                            currentProjectsLoved.value,
                            currentFollowing.value,
                            currentFollowing.value,
                            currentGoal.value
                        )

                        selectedFilterCounts[FilterRowPillType.SORT.name] =
                            if (sort == DiscoveryParams.Sort.MAGIC) 0 else 1
                    }
                )
            }

            null -> {}
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun modalBottomSheetState(
    activeBottomSheet: MutableState<FilterRowPillType?>,
    sortSheetState: ModalBottomSheetState,
    mainFilterMenuState: ModalBottomSheetState
) = when (activeBottomSheet.value) {
    FilterRowPillType.SORT -> sortSheetState
    FilterRowPillType.PROJECT_STATUS,
    FilterRowPillType.CATEGORY,
    FilterRowPillType.PERCENTAGE_RAISED,
    FilterRowPillType.LOCATION,
    FilterRowPillType.AMOUNT_RAISED,
    FilterRowPillType.RECOMMENDED,
    FilterRowPillType.PROJECTS_LOVED,
    FilterRowPillType.SAVED,
    FilterRowPillType.FOLLOWING,
    FilterRowPillType.GOAL,
    FilterRowPillType.FILTER -> mainFilterMenuState

    null -> sortSheetState
}

@Composable
fun getFundingInfoString(
    projectCardState: CardProjectState,
    environment: Environment?,
    project: Project
): String {
    return when (projectCardState) {
        CardProjectState.LIVE -> environment?.ksString()?.let {
            NumberUtils.format(
                project.deadlineCountdownValue(),
            ) + " " + project.deadlineCountdownDetail(
                LocalContext.current,
                it
            ) + " • " + project.percentageFunded()
                .toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)
        }.toString()

        CardProjectState.LATE_PLEDGES_ACTIVE -> stringResource(R.string.Late_pledges_active) + " • " + project.percentageFunded()
            .toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)

        CardProjectState.LAUNCHING_SOON -> stringResource(R.string.Launching_soon)
        CardProjectState.ENDED_SUCCESSFUL -> stringResource(R.string.Ended) + " • " + project.percentageFunded()
            .toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)

        CardProjectState.ENDED_UNSUCCESSFUL -> stringResource(R.string.Ended) + " • " + project.percentageFunded()
            .toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)
    }
}
