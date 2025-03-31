package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
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
import com.kickstarter.libs.utils.extensions.toDiscoveryParamsList
import com.kickstarter.models.Category
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
import com.kickstarter.ui.views.compose.search.FilterRowPillType
import com.kickstarter.ui.views.compose.search.SearchEmptyView
import com.kickstarter.ui.views.compose.search.SearchTopBar
import kotlinx.coroutines.launch

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
    return if (project.isSuccessful)
        CardProjectState.ENDED_SUCCESSFUL
    else if (project.isFailed)
        CardProjectState.ENDED_UNSUCCESSFUL
    else if (project.postCampaignPledgingEnabled() == true && project.isInPostCampaignPledgingPhase() == true)
        CardProjectState.LATE_PLEDGES_ACTIVE
    else if (!project.isLive)
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
    onDismissBottomSheet: (Category?, DiscoveryParams.Sort?) -> Unit = { category, sort -> },
    shouldShowPillbar: Boolean = true
) {
    var currentSearchTerm by rememberSaveable { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    val countApiIsReady = false // Hide all result counts until backend API is ready

    val selectedFilterCounts: SnapshotStateMap<String, Int> = remember {
        mutableStateMapOf(
            FilterRowPillType.SORT.name to 0,
            FilterRowPillType.CATEGORY.name to 0
        )
    }
    val initialCategoryPillText = stringResource(R.string.Category)
    val initProjectStatusPillText = stringResource(R.string.Project_Status)
    val categoryPillText = remember { mutableStateOf(initialCategoryPillText) }
    val projectStatusPillText = remember { mutableStateOf(initProjectStatusPillText) }
    var currentSort by remember { mutableStateOf(DiscoveryParams.Sort.MAGIC) }
    var currentCategory by remember { mutableStateOf<Category?>(null) }

    val activeBottomSheet = remember {
        mutableStateOf<FilterRowPillType?>(null)
    }

    val categorySheetState = rememberModalBottomSheetState(
        initialValue = Hidden,
        skipHalfExpanded = true
    )

    val sortSheetState = rememberModalBottomSheetState(
        initialValue = Hidden,
        skipHalfExpanded = false
    )

    ModalBottomSheetLayout(
        sheetState = if (activeBottomSheet.value == FilterRowPillType.CATEGORY) categorySheetState else sortSheetState,
        sheetContent = {
            when (activeBottomSheet.value) {
                FilterRowPillType.CATEGORY -> {
                    CategorySelectionSheet( // Switch out for MultiCategorySelectionSheet when count API is ready
                        currentCategory = currentCategory,
                        onDismiss = {
                            coroutineScope.launch { categorySheetState.hide() }
                            onDismissBottomSheet.invoke(currentCategory, currentSort)
                        },
                        categories = categories,
                        onApply = { selectedCategory ->

                            categoryPillText.value = selectedCategory.name()
                            coroutineScope.launch { categorySheetState.hide() }

                            if (selectedCategory.name() == initialCategoryPillText) { // User reset filter
                                onDismissBottomSheet.invoke(null, currentSort)
                                selectedFilterCounts[FilterRowPillType.CATEGORY.name] = 0
                                currentCategory = null
                            } else { // User applied valid filter
                                onDismissBottomSheet.invoke(selectedCategory, currentSort)
                                currentCategory = selectedCategory
                                if (countApiIsReady) {
                                    // Set selectedFilterCounts to actual count when count API is ready
                                } else {
                                    selectedFilterCounts[FilterRowPillType.CATEGORY.name] = 1
                                }
                            }
                        },
                        isLoading = false
                    )
                }

                FilterRowPillType.SORT -> {
                    SortSelectionBottomSheet(
                        currentSelection = currentSort,
                        sorts = ProjectSort.knownValues().toDiscoveryParamsList(),
                        onDismiss = { sort ->
                            currentSort = sort
                            coroutineScope.launch { sortSheetState.hide() }
                            onDismissBottomSheet.invoke(currentCategory, sort)
//                            // When a sort other than Recommended is applied, the Sort pill is in the active state
                            if (sort == DiscoveryParams.Sort.MAGIC) {
                                selectedFilterCounts[FilterRowPillType.SORT.name] = 0
                            } else {
                                selectedFilterCounts[FilterRowPillType.SORT.name] = 1
                            }
                        },
                    )
                }
                else -> {}
            }
        },
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
                        onValueChanged = {
                            onSearchTermChanged.invoke(it)
                            currentSearchTerm = it
                        },
                        selectedFilterCounts = selectedFilterCounts,
                        onSortPressed = {
                            activeBottomSheet.value = FilterRowPillType.SORT
                            coroutineScope.launch {
                                sortSheetState.show()
                            }
                        },
                        onCategoryPressed = {
                            activeBottomSheet.value = FilterRowPillType.CATEGORY
                            coroutineScope.launch {
                                categorySheetState.show()
                            }
                        },
                        shouldShowPillbar = shouldShowPillbar
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

@Composable
fun getFundingInfoString(projectCardState: CardProjectState, environment: Environment?, project: Project): String {
    return when (projectCardState) {
        CardProjectState.LIVE -> environment?.ksString()?.let {
            NumberUtils.format(
                project.deadlineCountdownValue(),
            ) + " " + project.deadlineCountdownDetail(LocalContext.current, it) + " • " + project.percentageFunded().toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)
        }.toString()
        CardProjectState.LATE_PLEDGES_ACTIVE -> stringResource(R.string.Late_pledges_active) + " • " + project.percentageFunded().toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)
        CardProjectState.LAUNCHING_SOON -> stringResource(R.string.Launching_soon)
        CardProjectState.ENDED_SUCCESSFUL -> stringResource(R.string.Ended) + " • " + project.percentageFunded().toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)
        CardProjectState.ENDED_UNSUCCESSFUL -> stringResource(R.string.Ended) + " • " + project.percentageFunded().toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)
    }
}
