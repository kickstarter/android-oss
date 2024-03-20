package com.kickstarter.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.util.Pair
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aghajari.zoomhelper.ZoomHelper
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.databinding.ActivityProjectPageBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Either
import com.kickstarter.libs.KSString
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.compose.projectpage.ProjectPledgeButtonAndFragmentContainer
import com.kickstarter.ui.adapters.ProjectPagerAdapter
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.ActivityResult.Companion.create
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.selectPledgeFragment
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.ui.extensions.startRootCommentsActivity
import com.kickstarter.ui.extensions.startUpdatesActivity
import com.kickstarter.ui.extensions.startVideoActivity
import com.kickstarter.ui.fragments.BackingFragment
import com.kickstarter.ui.fragments.CancelPledgeFragment
import com.kickstarter.ui.fragments.PledgeFragment
import com.kickstarter.ui.fragments.RewardsFragment
import com.kickstarter.viewmodels.projectpage.CheckoutFlowViewModel
import com.kickstarter.viewmodels.projectpage.ConfirmDetailsViewModel
import com.kickstarter.viewmodels.projectpage.PagerTabConfig
import com.kickstarter.viewmodels.projectpage.ProjectPageViewModel
import com.kickstarter.viewmodels.projectpage.RewardsSelectionViewModel
import com.stripe.android.view.CardInputWidget
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class ProjectPageActivity :
    AppCompatActivity(),
    CancelPledgeFragment.CancelPledgeDelegate,
    PledgeFragment.PledgeDelegate,
    BackingFragment.BackingDelegate {
    private lateinit var ksString: KSString

    private lateinit var viewModelFactory: ProjectPageViewModel.Factory
    private val viewModel: ProjectPageViewModel.ProjectPageViewModel by viewModels { viewModelFactory }

    private lateinit var checkoutViewModelFactory: CheckoutFlowViewModel.Factory
    private val checkoutFlowViewModel: CheckoutFlowViewModel by viewModels { checkoutViewModelFactory }

    private val rewardsSelectionViewModelFactory = RewardsSelectionViewModel.Factory()
    private val rewardsSelectionViewModel: RewardsSelectionViewModel by viewModels { rewardsSelectionViewModelFactory }

    private lateinit var confirmDetailsViewModelFactory: ConfirmDetailsViewModel.Factory
    private val confirmDetailsViewModel: ConfirmDetailsViewModel by viewModels { confirmDetailsViewModelFactory }

    private val projectShareLabelString = R.string.project_accessibility_button_share_label
    private val projectShareCopyString = R.string.project_share_twitter_message
    private val projectStarConfirmationString = R.string.project_star_confirmation

    private val animDuration = 200L
    private lateinit var binding: ActivityProjectPageBinding

    private var disposables = CompositeDisposable()

    private val pagerAdapterList = mutableListOf(
        ProjectPagerTabs.OVERVIEW,
        ProjectPagerTabs.CAMPAIGN,
        ProjectPagerTabs.FAQS,
        ProjectPagerTabs.RISKS,
    )

    var startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data = result.data?.getLongExtra(IntentKey.VIDEO_SEEK_POSITION, 0)
            data?.let {
                viewModel.inputs.closeFullScreenVideo(it)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpConnectivityStatusCheck(lifecycle)
        // TODO: Initialize and use this flow only when post campaign is on and enabled for a project
        val composeView = binding.pledgeContainerCompose
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KickstarterApp {
                    val flowUIState by checkoutFlowViewModel.flowUIState.collectAsStateWithLifecycle()

                    val expanded = flowUIState.expanded
                    val currentPage = flowUIState.currentPage

                    val rewardSelectionUIState by rewardsSelectionViewModel.rewardSelectionUIState.collectAsStateWithLifecycle()

                    val projectData = rewardSelectionUIState.project
                    val indexOfBackedReward = rewardSelectionUIState.initialRewardIndex
                    val rewardsList = rewardSelectionUIState.rewardList
                    val showRewardCarouselAlertDialog = rewardSelectionUIState.showAlertDialog

                    LaunchedEffect(Unit) {
                        rewardsSelectionViewModel.flowUIRequest.collect {
                            checkoutFlowViewModel.changePage(it)
                        }
                    }

                    val confirmUiState by confirmDetailsViewModel.confirmDetailsUIState.collectAsStateWithLifecycle()

                    val totalAmount: Double = confirmUiState.totalAmount
                    val rewardsAndAddOns = confirmUiState.rewardsAndAddOns
                    val shippingAmount = confirmUiState.shippingAmount
                    val initialBonusAmount = confirmUiState.initialBonusSupportAmount
                    val totalBonusSupportAmount = confirmUiState.totalBonusSupportAmount
                    val currentShippingRule = confirmUiState.currentShippingRule
                    val maxPledgeAmount = confirmUiState.maxPledgeAmount
                    val minStepAmount = confirmUiState.minStepAmount

                    val checkoutPayment by confirmDetailsViewModel.checkoutPayment.collectAsStateWithLifecycle()

                    LaunchedEffect(checkoutPayment.id) {
                        if (checkoutPayment.id != 0L) checkoutFlowViewModel.onConfirmDetailsContinueClicked()
                    }

                    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })

                    this@ProjectPageActivity.onBackPressedDispatcher.addCallback {
                        if (expanded) checkoutFlowViewModel.onBackPressed(pagerState.currentPage)
                        else finishWithAnimation()
                    }

                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(currentPage) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = currentPage,
                                animationSpec = tween(
                                    durationMillis = 200,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    }

                    val shippingRules =
                        checkoutFlowViewModel.shippingRules.subscribeAsState(initial = listOf()).value

                    val currentUserShippingRule =
                        checkoutFlowViewModel.defaultShippingRule.subscribeAsState(
                            initial = ShippingRule.builder().build()
                        ).value

                    var selectedReward: Reward? = null

                    val addOnsMap: MutableMap<Reward, Int> = mutableMapOf()

                    val addOns =
                        checkoutFlowViewModel.addOns.subscribeAsState(initial = listOf()).value

                    ProjectPledgeButtonAndFragmentContainer(
                        expanded = expanded,
                        onContinueClicked = { checkoutFlowViewModel.onBackThisProjectClicked() },
                        onBackClicked = {
                            checkoutFlowViewModel.onBackPressed(pagerState.currentPage)
                        },
                        pagerState = pagerState,
                        onAddOnsContinueClicked = {
                            checkoutFlowViewModel.onAddOnsContinueClicked()
                        },
                        currentShippingRule = currentShippingRule ?: currentUserShippingRule,
                        shippingRules = shippingRules,
                        environment = getEnvironment(),
                        initialRewardCarouselPosition = indexOfBackedReward,
                        rewardsList = rewardsList,
                        showRewardCarouselDialog = showRewardCarouselAlertDialog,
                        onRewardAlertDialogNegativeClicked = {
                            rewardsSelectionViewModel.onRewardCarouselAlertClicked(wasPositive = false)
                        },
                        onRewardAlertDialogPositiveClicked = {
                            rewardsSelectionViewModel.onRewardCarouselAlertClicked(wasPositive = true)
                        },
                        addOns = addOns,
                        project = projectData.project(),
                        onRewardSelected = { reward ->
                            selectedReward = reward
                            checkoutFlowViewModel.userRewardSelection(reward)
                            rewardsSelectionViewModel.onUserRewardSelection(reward)
                            confirmDetailsViewModel.onUserSelectedReward(reward)
                        },
                        onAddOnAddedOrRemoved = { updateAddOnRewardCount ->
                            addOnsMap[updateAddOnRewardCount.keys.first()] =
                                updateAddOnRewardCount[updateAddOnRewardCount.keys.first()] ?: 0

                            confirmDetailsViewModel.onUserUpdatedAddOns(addOnsMap)
                        },
                        selectedReward = selectedReward,
                        totalAmount = totalAmount,
                        selectedRewardAndAddOnList = rewardsAndAddOns,
                        initialBonusSupportAmount = initialBonusAmount,
                        totalBonusSupportAmount = totalBonusSupportAmount,
                        maxPledgeAmount = maxPledgeAmount,
                        minStepAmount = minStepAmount,
                        onShippingRuleSelected = {
                            confirmDetailsViewModel.onShippingRuleSelected(it)
                        },
                        shippingAmount = shippingAmount,
                        onConfirmDetailsContinueClicked = {
                            confirmDetailsViewModel.onContinueClicked {
                                checkoutFlowViewModel.onConfirmDetailsContinueClicked()
                            }
                        },
                        onBonusSupportMinusClicked = { confirmDetailsViewModel.decrementBonusSupport() },
                        onBonusSupportPlusClicked = { confirmDetailsViewModel.incrementBonusSupport() }
                    )
                }
            }
        }

        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = ProjectPageViewModel.Factory(env)
            checkoutViewModelFactory = CheckoutFlowViewModel.Factory(env)
            confirmDetailsViewModelFactory = ConfirmDetailsViewModel.Factory(env)
            env
        }
        this.ksString = requireNotNull(environment?.ksString())

        viewModel.configureWith(intent)

        // Do not configure the pager at other lifecycle events apart from OnCreate
        if (savedInstanceState == null) {
            // - Configure pager on load, otherwise the first fragment on the pager gets no data
            configurePager(pagerAdapterList)
        }

        val viewTreeObserver = binding.pledgeContainerLayout.pledgeContainerRoot.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    this@ProjectPageActivity.viewModel.inputs.onGlobalLayout()
                    binding.pledgeContainerLayout.pledgeContainerRoot.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        this.supportFragmentManager.addOnBackStackChangedListener {
            this.viewModel.inputs.fragmentStackCount(this.supportFragmentManager.backStackEntryCount)
            val fragments = this.supportFragmentManager.fragments
            val lastFragmentWithView = fragments.last { it.view != null }
            for (fragment in fragments) {
                if (fragment == lastFragmentWithView) {
                    fragment.view?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                } else {
                    fragment.view?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                }
            }
        }

        this.viewModel.outputs.projectData()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                // - Every time the ProjectData gets updated
                // - the fragments on the viewPager are updated as well
                (binding.projectPager.adapter as? ProjectPagerAdapter)?.updatedWithProjectData(it)
                checkoutFlowViewModel.provideProjectData(it)
                rewardsSelectionViewModel.provideProjectData(it)
                confirmDetailsViewModel.provideProjectData(it)
            }.addToDisposable(disposables)

        this.viewModel.outputs.updateTabs()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateTabs ->
                configureTabs(updateTabs)
                configurePager(pagerAdapterList)
            }.addToDisposable(disposables)

        this.viewModel.outputs.backingDetailsSubtitle()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setBackingDetailsSubtitle(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.backingDetailsTitle()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.backingDetailsTitle.setText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.backingDetailsIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { styleProjectActionButton(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.expandPledgeSheet()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { expandPledgeSheet(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.goBack()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onBackPressedDispatcher.onBackPressed() }
            .addToDisposable(disposables)

        this.viewModel.outputs.heartDrawableId()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.heartIcon.setImageDrawable(ContextCompat.getDrawable(this, it)) }
            .addToDisposable(disposables)

        this.viewModel.outputs.managePledgeMenu()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateManagePledgeMenu(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeActionButtonColor()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.pledgeActionButton.backgroundTintList = ContextCompat.getColorStateList(this, it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeActionButtonContainerIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.pledgeActionButtonsLayout.visibility = (!it).toVisibility() }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeActionButtonText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setPledgeActionButtonCTA(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeToolbarNavigationIcon()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.pledgeToolbar.navigationIcon = ContextCompat.getDrawable(this, it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeToolbarTitle()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.pledgeToolbar.title = getString(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.prelaunchUrl()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { openProjectAndFinish(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.reloadProjectContainerIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.projectRetryLayout.pledgeSheetRetryContainer.visibility = (!it).toVisibility() }
            .addToDisposable(disposables)

        this.viewModel.outputs.reloadProgressBarIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.projectRetryLayout.pledgeSheetProgressBar.visibility = (!it).toVisibility() }
            .addToDisposable(disposables)

        this.viewModel.outputs.scrimIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { animateScrimVisibility(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.setInitialRewardsContainerY()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setInitialRewardsContainerY() }
            .addToDisposable(disposables)

        this.viewModel.outputs.showCancelPledgeSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showCancelPledgeSuccess() }
            .addToDisposable(disposables)

        this.viewModel.outputs.showUpdatePledgeSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showUpdatePledgeSuccess() }
            .addToDisposable(disposables)

        this.viewModel.outputs.showCancelPledgeFragment()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showCancelPledgeFragment(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showPledgeNotCancelableDialog()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showPledgeNotCancelableDialog() }
            .addToDisposable(disposables)

        this.viewModel.outputs.revealRewardsFragment()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { revealRewardsFragment() }
            .addToDisposable(disposables)

        this.viewModel.outputs.showSavedPrompt()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.showStarToast() }
            .addToDisposable(disposables)

        this.viewModel.outputs.showShareSheet()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startShareIntent(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showUpdatePledge()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showPledgeFragment(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startRootCommentsActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startRootCommentsActivity(it)
            }.addToDisposable(disposables)

        this.viewModel.outputs.startRootCommentsForCommentsThreadActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startRootCommentsActivity(it.second, it.first)
            }.addToDisposable(disposables)

        this.viewModel.outputs.startProjectUpdateActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startUpdatesActivity(it.second.first, it.first.first, it.first.second)
            }.addToDisposable(disposables)

        this.viewModel.outputs.startProjectUpdateToRepliesDeepLinkActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startUpdatesActivity(
                    it.second.first,
                    it.first.first,
                    it.first.second.isNotEmpty(),
                    it.first.second
                )
            }.addToDisposable(disposables)

        this.viewModel.outputs.startLoginToutActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.startLoginToutActivity() }
            .addToDisposable(disposables)

        this.viewModel.outputs.startMessagesActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startMessagesActivity(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startThanksActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showCreatePledgeSuccess(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.projectMedia()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.mediaHeader.inputs.setProjectMedia(it) }
            .addToDisposable(disposables)

        viewModel.outputs.playButtonIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.mediaHeader.inputs.setPlayButtonVisibility(it) }
            .addToDisposable(disposables)

        viewModel.outputs.updateVideoCloseSeekPosition()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.mediaHeader.inputs.setPlayerSeekPosition(it) }
            .addToDisposable(disposables)

        binding.mediaHeader.outputs.onFullScreenClicked()
            .compose(Transformers.observeForUI())
            .subscribe { viewModel.inputs.fullScreenVideoButtonClicked(it) }

        binding.mediaHeader.outputs.playButtonClicks()
            .compose(Transformers.observeForUI())
            .subscribe { viewModel.inputs.onVideoPlayButtonClicked() }

        this.viewModel.outputs.onOpenVideoInFullScreen()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.startVideoActivity(startForResult, it.first, it.second)
            }.addToDisposable(disposables)

        viewModel.outputs.backingViewGroupIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.backingGroup.visibility = it.toVisibility()
            }.addToDisposable(disposables)

        viewModel.outputs.hideVideoPlayer()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it) {
                    binding.projectAppBarLayout.setExpanded(false)
                }
            }.addToDisposable(disposables)

        binding.backIcon.setOnClickListener {
            if (binding.pledgeContainerLayout.pledgeContainerRoot.visibility == View.GONE) {
                onBackPressedDispatcher.onBackPressed()
            } else {
                handleNativeCheckoutBackPress()
            }
        }

        setClickListeners()

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectAppBarLayout.setExpanded(false)
        }

        binding.projectAppBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            if (verticalOffset != 0) {
                binding.mediaHeader.inputs.pausePlayer()
            }
        }

        // TODO: change logic to use this if project is non-post campaign
//        this.onBackPressedDispatcher.addCallback {
//            finishWithAnimation()
//        }

        // TODO: handle the showing/hiding based on project post campaign flags
        binding.pledgeContainerLayout.pledgeContainerRoot.isGone = true
    }

    /**
     * Give a List of configurations will iterate over it and apply
     * the configuration required.
     * Will check first if the tab already exists in the pager before adding it.
     *
     * @param updateTabs
     */
    private fun configureTabs(updateTabs: List<PagerTabConfig>) {
        updateTabs.forEach { tabConfig ->
            if (tabConfig.isActive) {
                // - avoid adding the tab if it already exists
                if (!pagerAdapterList.contains(tabConfig.tab)) {
                    pagerAdapterList.add(tabConfig.tab)
                }
            }
        }
    }

    private fun configurePager(pagerList: List<ProjectPagerTabs>) {
        val viewPager = binding.projectPager
        val tabLayout = binding.projectDetailTabs

        viewPager.adapter = ProjectPagerAdapter(supportFragmentManager, pagerList, lifecycle)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getTabTitle(position, pagerList)
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewModel.inputs.tabSelected(tab.position)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.mediaHeader.inputs.initializePlayer()
        this.viewModel.outputs.updateFragments()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateFragments(it) }
            .addToDisposable(disposables)
    }

    public override fun onPause() {
        super.onPause()
        binding.mediaHeader.inputs.releasePlayer()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            ZoomHelper.getInstance().dispatchTouchEvent(it, this)
        }

        if (event?.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText || view?.parent is CardInputWidget) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    hideKeyboard()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun pledgePaymentSuccessfullyUpdated() {
        this.viewModel.inputs.pledgePaymentSuccessfullyUpdated()
    }

    override fun pledgeSuccessfullyCancelled() {
        this.viewModel.inputs.pledgeSuccessfullyCancelled()
    }

    override fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>) {
        this.viewModel.inputs.pledgeSuccessfullyCreated(checkoutDataAndPledgeData)
    }

    override fun pledgeSuccessfullyUpdated() {
        this.viewModel.inputs.pledgeSuccessfullyUpdated()
    }

    override fun refreshProject() {
        this.viewModel.inputs.refreshProject()
    }

    override fun showFixPaymentMethod() {
        this.viewModel.inputs.fixPaymentMethodButtonClicked()
    }

    fun exitTransition(): Pair<Int, Int>? {
        return Pair.create(R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
    }

    private fun getTabTitle(position: Int, pagerList: List<ProjectPagerTabs>) = when (pagerList[position]) {
        ProjectPagerTabs.OVERVIEW -> getString(R.string.Overview)
        ProjectPagerTabs.CAMPAIGN -> getString(R.string.Campaign)
        ProjectPagerTabs.FAQS -> getString(R.string.Faq)
        ProjectPagerTabs.RISKS -> getString(R.string.Risks)
        ProjectPagerTabs.USE_OF_AI -> getString(R.string.Use_of_ai)
        ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT -> getString(R.string.Environmental_commitments)
    }

    private fun animateScrimVisibility(show: Boolean) {
        val shouldAnimateIn = show && binding.pledgeContainerLayout.scrim.alpha <= 1f
        val shouldAnimateOut = !show && binding.pledgeContainerLayout.scrim.alpha >= 0f
        if (shouldAnimateIn || shouldAnimateOut) {
            val finalAlpha = if (show) 1f else 0f
            binding.pledgeContainerLayout.scrim.animate()
                .alpha(finalAlpha)
                .setDuration(200L)
                .setListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        if (!show) {
                            binding.pledgeContainerLayout.scrim.visibility = View.GONE
                        }
                    }

                    override fun onAnimationStart(animation: Animator) {
                        if (show) {
                            binding.pledgeContainerLayout.scrim.visibility = View.VISIBLE
                        }
                    }
                })
        }
    }

    private fun backingFragment() = supportFragmentManager.findFragmentById(R.id.fragment_backing) as BackingFragment?

    private fun clearFragmentBackStack(): Boolean {
        return supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun expandPledgeSheet(expandAndAnimate: Pair<Boolean, Boolean>) {
        var statusBarHeight = 0
        // TODO: Replace with window insets compat
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }

        val expand = expandAndAnimate.first
        val animate = expandAndAnimate.second
        val targetToShow = if (!expand) binding.pledgeContainerLayout.pledgeActionButtonsLayout else binding.pledgeContainerLayout.pledgeContainer
        val showRewardsFragmentAnimator = ObjectAnimator.ofFloat(targetToShow, View.ALPHA, 0f, 1f)

        val targetToHide = if (!expand) binding.pledgeContainerLayout.pledgeContainer else binding.pledgeContainerLayout.pledgeActionButtonsLayout
        val hideRewardsFragmentAnimator = ObjectAnimator.ofFloat(targetToHide, View.ALPHA, 1f, 0f)

        val guideline = rewardsSheetGuideline()
        val initialValue = (if (expand) binding.pledgeContainerLayout.pledgeContainerRoot.height - guideline else 0).toFloat()
        val finalValue = ((if (expand) 0 else binding.pledgeContainerLayout.pledgeContainerRoot.height - guideline) + statusBarHeight).toFloat()
        val initialRadius = resources.getDimensionPixelSize(R.dimen.fab_radius).toFloat()

        val pledgeContainerYAnimator = ObjectAnimator.ofFloat(binding.pledgeContainerLayout.pledgeContainerRoot, View.Y, initialValue, finalValue).apply {
            addUpdateListener { valueAnim ->
                val radius = initialRadius * if (expand) 1 - valueAnim.animatedFraction else valueAnim.animatedFraction
                binding.pledgeContainerLayout.pledgeContainerRoot.radius = radius
            }
        }

        AnimatorSet().apply {
            playTogether(showRewardsFragmentAnimator, hideRewardsFragmentAnimator, pledgeContainerYAnimator)
            duration = animDuration

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    setFragmentsState(expand)
                    if (expand) {
                        binding.pledgeContainerLayout.pledgeActionButtonsLayout.visibility = View.GONE
                        binding.projectActivityToolbar.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        binding.pledgeContainerLayout.pledgeToolbar.requestFocus()
                    } else {
                        binding.pledgeContainerLayout.pledgeContainer.visibility = View.GONE
                        binding.projectActivityToolbar.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                        if (animate) {
                            binding.projectActivityToolbar.requestFocus()
                        }
                    }
                }

                override fun onAnimationStart(animation: Animator) {
                    if (expand) {
                        binding.pledgeContainerLayout.pledgeContainer.visibility = View.VISIBLE
                    } else if (animate) {
                        binding.pledgeContainerLayout.pledgeActionButtonsLayout.visibility = View.VISIBLE
                    }
                }
            })

            start()
        }
    }

    private fun setFragmentsState(expand: Boolean) {
        supportFragmentManager.fragments.map { fragment ->
            when (fragment) {
                is BaseFragment<*> -> {
                    fragment.setState(expand && fragment.isVisible)
                }
                is RewardsFragment -> {
                    fragment.setState(expand && fragment.isVisible)
                }
                is BackingFragment -> {
                    fragment.setState(expand && fragment.isVisible)
                }
            }
        }
    }

    private fun handleNativeCheckoutBackPress() {
        val retryPadding = resources.getDimensionPixelSize(R.dimen.grid_4) // pledge_sheet_retry_container padding
        val pledgeSheetIsExpanded = binding.pledgeContainerLayout.pledgeContainerRoot.y <= retryPadding

        when {
            supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager.popBackStack()
            pledgeSheetIsExpanded -> this.viewModel.inputs.pledgeToolbarNavigationClicked()
            else -> onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun openProjectAndFinish(url: String) {
        ApplicationUtils.openUrlExternally(this, url)
        finish()
    }

    private fun renderProject(backingFragment: BackingFragment, rewardsFragment: RewardsFragment, projectData: ProjectData) {
        rewardsFragment.configureWith(projectData)
        backingFragment.configureWith(projectData)
    }

    private fun revealRewardsFragment() {
        rewardsFragment()?.let {
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .show(it)
                .addToBackStack(RewardsFragment::class.java.simpleName)
                .commit()
        }
    }

    private fun rewardsFragment() = supportFragmentManager.findFragmentById(R.id.fragment_rewards) as RewardsFragment?

    private fun rewardsSheetGuideline(): Int = resources.getDimensionPixelSize(R.dimen.reward_fragment_guideline_constraint_end)

    private fun setBackingDetailsSubtitle(stringResOrTitle: Either<String, Int>?) {
        stringResOrTitle?.let { either ->
            @StringRes val stringRes = either.right()
            val title = either.left()
            binding.pledgeContainerLayout.backingDetailsSubtitle.text = stringRes?.let { getString(it) } ?: title
        }
    }

    private fun setClickListeners() {
        binding.pledgeContainerLayout.pledgeActionButton.setOnClickListener {
            this.viewModel.inputs.nativeProjectActionButtonClicked()
        }

        binding.pledgeContainerLayout.pledgeToolbar.setNavigationOnClickListener {
            this.viewModel.inputs.pledgeToolbarNavigationClicked()
        }

        binding.pledgeContainerLayout.pledgeToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.update_pledge -> {
                    this.viewModel.inputs.updatePledgeClicked()
                    true
                }
                R.id.rewards -> {
                    this.viewModel.inputs.viewRewardsClicked()
                    true
                }
                R.id.update_payment -> {
                    this.viewModel.inputs.updatePaymentClicked()
                    true
                }
                R.id.cancel_pledge -> {
                    this.viewModel.inputs.cancelPledgeClicked()
                    true
                }
                R.id.contact_creator -> {
                    this.viewModel.inputs.contactCreatorClicked()
                    true
                }
                else -> false
            }
        }

        binding.pledgeContainerLayout.projectRetryLayout.pledgeSheetRetryContainer.setOnClickListener {
            this.viewModel.inputs.reloadProjectContainerClicked()
        }

        binding.heartIcon.setOnClickListener {
            this.viewModel.inputs.heartButtonClicked()
        }

        binding.shareIcon.setOnClickListener {
            this.viewModel.inputs.shareButtonClicked()
        }
    }

    private fun setInitialRewardsContainerY() {
        val guideline = rewardsSheetGuideline()
        binding.pledgeContainerLayout.pledgeContainerRoot.y = (binding.root.height - guideline).toFloat()
    }

    private fun showCancelPledgeFragment(project: Project) {
        val cancelPledgeFragment = CancelPledgeFragment.newInstance(project)
        val tag = CancelPledgeFragment::class.java.simpleName
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
            .add(R.id.fragment_container, cancelPledgeFragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    private fun showCancelPledgeSuccess() {
        clearFragmentBackStack()
        showSnackbar(binding.snackbarAnchor, getString(R.string.Youve_canceled_your_pledge))
    }

    private fun showCreatePledgeSuccess(checkoutDatandProjectData: Pair<CheckoutData, PledgeData>) {
        val checkoutData = checkoutDatandProjectData.first
        val pledgeData = checkoutDatandProjectData.second
        val projectData = pledgeData.projectData()
        if (clearFragmentBackStack()) {
            startActivity(
                Intent(this, ThanksActivity::class.java)
                    .putExtra(IntentKey.PROJECT, projectData.project())
                    .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
                    .putExtra(IntentKey.PLEDGE_DATA, pledgeData)
            )
        }
    }

    private fun showPledgeNotCancelableDialog() {
        AlertDialog.Builder(this, R.style.Dialog)
            .setMessage(R.string.We_dont_allow_cancelations_that_will_cause_a_project_to_fall_short_of_its_goal_within_the_last_24_hours)
            .setPositiveButton(getString(R.string.general_alert_buttons_ok)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showPledgeFragment(pledgeDataAndPledgeReason: Pair<PledgeData, PledgeReason>) {
        val pledgeFragment = this.selectPledgeFragment(pledgeDataAndPledgeReason.first, pledgeDataAndPledgeReason.second)
        val tag = pledgeFragment::class.java.simpleName
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
            .add(R.id.fragment_container, pledgeFragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    private fun setPledgeActionButtonCTA(stringRes: Int) {
        binding.pledgeContainerLayout.pledgeActionButton.setText(stringRes)
        binding.pledgeContainerLayout.pledgeActionButton.contentDescription = when (stringRes) {
            R.string.Manage -> getString(R.string.Manage_your_pledge)
            else -> getString(stringRes)
        }
    }

    private fun showStarToast() {
        ViewUtils.showToastFromTop(this, getString(this.projectStarConfirmationString), 0, resources.getDimensionPixelSize(R.dimen.grid_8))
    }

    private fun showUpdatePledgeSuccess() {
        clearFragmentBackStack()
        backingFragment()?.pledgeSuccessfullyUpdated()
    }

    private fun startShareIntent(projectNameAndShareUrl: Pair<String, String>) {
        val name = projectNameAndShareUrl.first
        val shareMessage = this.ksString.format(getString(this.projectShareCopyString), "project_title", name)

        val url = projectNameAndShareUrl.second
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "$shareMessage $url")
        startActivity(Intent.createChooser(intent, getString(this.projectShareLabelString)))
    }

    private fun startLoginToutActivity() {
        val intent = Intent(this, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.STAR_PROJECT)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
    }

    private fun startMessagesActivity(project: Project) {
        startActivity(
            Intent(this, MessagesActivity::class.java)
                .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, MessagePreviousScreenType.PROJECT_PAGE)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.BACKING, project.backing())
        )
    }

    private fun styleProjectActionButton(detailsAreVisible: Boolean) {
        val buttonParams = binding.pledgeContainerLayout.pledgeActionButton.layoutParams as LinearLayout.LayoutParams
        when {
            detailsAreVisible -> {
                binding.pledgeContainerLayout.backingDetails.visibility = View.VISIBLE
                buttonParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                binding.pledgeContainerLayout.pledgeActionButton.cornerRadius = resources.getDimensionPixelSize(R.dimen.grid_2)
            }
            else -> {
                binding.pledgeContainerLayout.backingDetails.visibility = View.GONE
                buttonParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                binding.pledgeContainerLayout.pledgeActionButton.cornerRadius = resources.getDimensionPixelSize(R.dimen.fab_radius)
            }
        }
        binding.pledgeContainerLayout.pledgeActionButton.layoutParams = buttonParams
    }

    private fun updateFragments(projectData: ProjectData) {
        try {
            // - Every time the ProjectData gets updated
            // - the fragments on the viewPager are updated as well
            (binding.projectPager.adapter as? ProjectPagerAdapter)?.updatedWithProjectData(projectData)

            val rewardsFragment = rewardsFragment()
            val backingFragment = backingFragment()
            if (rewardsFragment != null && backingFragment != null) {
                when (supportFragmentManager.backStackEntryCount) {
                    0 -> when {
                        projectData.project().isBacking() -> if (!rewardsFragment.isHidden) {
                            supportFragmentManager.beginTransaction()
                                .show(backingFragment)
                                .hide(rewardsFragment)
                                .commitNow()
                        }
                        else -> if (!backingFragment.isHidden) {
                            supportFragmentManager.beginTransaction()
                                .show(rewardsFragment)
                                .hide(backingFragment)
                                .commitNow()
                        }
                    }
                }
                renderProject(backingFragment, rewardsFragment, projectData)
            }
        } catch (e: IllegalStateException) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        viewModel.activityResult(create(requestCode, resultCode, intent))
    }

    override fun onDestroy() {
        binding.projectPager.adapter = null
        binding.mediaHeader.inputs.releasePlayer()
        disposables.clear()
        super.onDestroy()
    }

    private fun updateManagePledgeMenu(@MenuRes menu: Int) {
        if (menu != 0) {
            binding.pledgeContainerLayout.pledgeToolbar.menu.clear()
            binding.pledgeContainerLayout.pledgeToolbar.inflateMenu(menu)
        } else run {
            binding.pledgeContainerLayout.pledgeToolbar.menu.clear()
        }
    }
}
