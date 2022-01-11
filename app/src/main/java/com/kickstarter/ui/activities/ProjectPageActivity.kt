package com.kickstarter.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.FragmentManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.databinding.ActivityProjectPageBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Either
import com.kickstarter.libs.KSString
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ProjectPagerAdapter
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.ui.extensions.startRootCommentsActivity
import com.kickstarter.ui.extensions.startUpdatesActivity
import com.kickstarter.ui.fragments.BackingFragment
import com.kickstarter.ui.fragments.CancelPledgeFragment
import com.kickstarter.ui.fragments.NewCardFragment
import com.kickstarter.ui.fragments.PledgeFragment
import com.kickstarter.ui.fragments.RewardsFragment
import com.kickstarter.viewmodels.projectpage.ProjectPageViewModel
import com.stripe.android.view.CardInputWidget
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ProjectPageViewModel.ViewModel::class)
class ProjectPageActivity :
    BaseActivity<ProjectPageViewModel.ViewModel>(),
    CancelPledgeFragment.CancelPledgeDelegate,
    NewCardFragment.OnCardSavedListener,
    PledgeFragment.PledgeDelegate,
    BackingFragment.BackingDelegate {
    private lateinit var ksString: KSString

    private val projectShareLabelString = R.string.project_accessibility_button_share_label
    private val projectShareCopyString = R.string.project_share_twitter_message
    private val projectStarConfirmationString = R.string.project_star_confirmation

    private val animDuration = 200L
    private lateinit var binding: ActivityProjectPageBinding

    private var pagerAdapterMap = mutableMapOf(
        ProjectPagerTabs.OVERVIEW to true,
        // ProjectPagerTabs.CAMPAIGN to true, // TODO bring back ProjectPagerTabs.CAMPAIGN to true, on second place once the HTML parser is in place
        ProjectPagerTabs.FAQS to true,
        ProjectPagerTabs.RISKS to true,
        ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT to false
    )

    private var pagerAdapter = ProjectPagerAdapter(
        supportFragmentManager, pagerAdapterMap,
        lifecycle
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.ksString = environment().ksString()

        // Do not configure the pager at other lifecycle events apart from OnCreate
        if (savedInstanceState == null) {
            // - Configure pager on load, otherwise the first fragment on the pager gets no data
            configurePager()
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
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                // - Every time the ProjectData gets updated
                // - the fragments on the viewPager are updated as well
                pagerAdapter.updatedWithProjectData(it)
            }

        this.viewModel.outputs.updateEnvCommitmentsTabVisibility()
            .filter { ObjectUtils.isNotNull(it) }
            .map { requireNotNull(it) }
            .distinctUntilChanged()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isGone ->
                binding.projectDetailTabs.getTabAt(
                    ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT
                        .ordinal
                )?.view?.isGone = isGone
                if (!isGone) {
                    pagerAdapterMap[ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT] = !isGone
                }
                configurePager()
            }

        this.viewModel.outputs.backingDetailsSubtitle()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setBackingDetailsSubtitle(it) }

        this.viewModel.outputs.backingDetailsTitle()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.backingDetailsTitle.setText(it) }

        this.viewModel.outputs.backingDetailsIsVisible()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { styleProjectActionButton(it) }

        this.viewModel.outputs.expandPledgeSheet()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { expandPledgeSheet(it) }

        this.viewModel.outputs.goBack()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { back() }

        this.viewModel.outputs.heartDrawableId()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.heartIcon.setImageDrawable(ContextCompat.getDrawable(this, it)) }

        this.viewModel.outputs.managePledgeMenu()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateManagePledgeMenu(it) }

        this.viewModel.outputs.pledgeActionButtonColor()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.pledgeActionButton.backgroundTintList = ContextCompat.getColorStateList(this, it) }

        this.viewModel.outputs.pledgeActionButtonContainerIsGone()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.pledgeActionButtonsLayout.visibility = (!it).toVisibility() }

        this.viewModel.outputs.pledgeActionButtonText()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setPledgeActionButtonCTA(it) }

        this.viewModel.outputs.pledgeToolbarNavigationIcon()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.pledgeToolbar.navigationIcon = ContextCompat.getDrawable(this, it) }

        this.viewModel.outputs.pledgeToolbarTitle()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.pledgeToolbar.title = getString(it) }

        this.viewModel.outputs.prelaunchUrl()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { openProjectAndFinish(it) }

        this.viewModel.outputs.reloadProjectContainerIsGone()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.projectRetryLayout.pledgeSheetRetryContainer.visibility = (!it).toVisibility() }

        this.viewModel.outputs.reloadProgressBarIsGone()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgeContainerLayout.projectRetryLayout.pledgeSheetProgressBar.visibility = (!it).toVisibility() }

        this.viewModel.outputs.scrimIsVisible()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { animateScrimVisibility(it) }

        this.viewModel.outputs.setInitialRewardsContainerY()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setInitialRewardsContainerY() }

        this.viewModel.outputs.showCancelPledgeSuccess()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showCancelPledgeSuccess() }

        this.viewModel.outputs.showUpdatePledgeSuccess()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showUpdatePledgeSuccess() }

        this.viewModel.outputs.showCancelPledgeFragment()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showCancelPledgeFragment(it) }

        this.viewModel.outputs.showPledgeNotCancelableDialog()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showPledgeNotCancelableDialog() }

        this.viewModel.outputs.revealRewardsFragment()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { revealRewardsFragment() }

        this.viewModel.outputs.showSavedPrompt()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.showStarToast() }

        this.viewModel.outputs.showShareSheet()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startShareIntent(it) }

        this.viewModel.outputs.showUpdatePledge()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showPledgeFragment(it) }

        this.viewModel.outputs.startRootCommentsActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startRootCommentsActivity(it)
            }

        this.viewModel.outputs.startRootCommentsForCommentsThreadActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startRootCommentsActivity(it.second, it.first)
            }

        this.viewModel.outputs.startProjectUpdateActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startUpdatesActivity(it.second.first, it.first.first, it.first.second)
            }

        this.viewModel.outputs.startProjectUpdateToRepliesDeepLinkActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startUpdatesActivity(
                    it.second.first,
                    it.first.first,
                    it.first.second.isNotEmpty(),
                    it.first.second
                )
            }

        this.viewModel.outputs.startLoginToutActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.startLoginToutActivity() }

        this.viewModel.outputs.startMessagesActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startMessagesActivity(it) }

        this.viewModel.outputs.startThanksActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showCreatePledgeSuccess(it) }

        this.viewModel.outputs.startVideoActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.startVideoActivity(it) }

        this.viewModel.outputs.projectPhoto()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.mediaHeader.inputs.setProjectPhoto(it) }

        viewModel.outputs.playButtonIsVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.mediaHeader.inputs.setPlayButtonVisibility(it) }

        binding.mediaHeader.outputs.playButtonClicks()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { viewModel.inputs.playVideoButtonClicked() }

        viewModel.outputs.backingViewGroupIsVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.backingGroup.visibility = it.toVisibility()
            }

        viewModel.outputs.hideVideoPlayer()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                if (it) {
                    binding.projectAppBarLayout.setExpanded(false)
                }
            }

        binding.backIcon.setOnClickListener {
            (this as BaseActivity<*>).back()
        }
        setClickListeners()

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectAppBarLayout.setExpanded(false)
        }
    }

    private fun configurePager() {
        val viewPager = binding.projectPager
        val tabLayout = binding.projectDetailTabs

        pagerAdapter = ProjectPagerAdapter(supportFragmentManager, pagerAdapterMap, lifecycle)

        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getTabTitle(position)
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

        this.viewModel.outputs.updateFragments()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateFragments(it) }
    }

    override fun back() {
        if (binding.pledgeContainerLayout.pledgeContainerRoot.visibility == View.GONE) {
            super.back()
        } else {
            handleNativeCheckoutBackPress()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
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

    override fun cardSaved(storedCard: StoredCard) {
        pledgeFragment()?.cardAdded(storedCard)
        supportFragmentManager.popBackStack()
    }

    override fun refreshProject() {
        this.viewModel.inputs.refreshProject()
    }

    override fun showFixPaymentMethod() {
        this.viewModel.inputs.fixPaymentMethodButtonClicked()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {}

    override fun exitTransition(): Pair<Int, Int>? {
        return Pair.create(R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
    }

    private fun getTabTitle(position: Int) = when (position) {
        ProjectPagerTabs.OVERVIEW.ordinal -> getString(R.string.Overview)
        // ProjectPagerTabs.CAMPAIGN.ordinal -> getString(R.string.Campaign) // TODO bring back CAMPAIGN, on second place once the HTML parser is in place
        ProjectPagerTabs.FAQS.ordinal -> getString(R.string.Faq)
        ProjectPagerTabs.RISKS.ordinal -> getString(R.string.Risks)
        ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT.ordinal -> getString(R.string.Environmental_Commitments)
        else -> ""
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

                    override fun onAnimationEnd(animation: Animator?) {
                        if (!show) {
                            binding.pledgeContainerLayout.scrim.visibility = View.GONE
                        }
                    }

                    override fun onAnimationStart(animation: Animator?) {
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
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
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

                override fun onAnimationStart(animation: Animator?) {
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
            (fragment as BaseFragment<*>).setState(expand && fragment.isVisible)
        }
    }

    private fun handleNativeCheckoutBackPress() {
        val retryPadding = resources.getDimensionPixelSize(R.dimen.grid_4) // pledge_sheet_retry_container padding
        val pledgeSheetIsExpanded = binding.pledgeContainerLayout.pledgeContainerRoot.y <= retryPadding

        when {
            supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager.popBackStack()
            pledgeSheetIsExpanded -> this.viewModel.inputs.pledgeToolbarNavigationClicked()
            else -> super.back()
        }
    }

    private fun openProjectAndFinish(url: String) {
        ApplicationUtils.openUrlExternally(this, url)
        finish()
    }

    private fun pledgeFragment() = supportFragmentManager
        .findFragmentByTag(PledgeFragment::class.java.simpleName) as PledgeFragment?

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
            when {
                it.itemId == R.id.update_pledge -> {
                    this.viewModel.inputs.updatePledgeClicked()
                    true
                }
                it.itemId == R.id.rewards -> {
                    this.viewModel.inputs.viewRewardsClicked()
                    true
                }
                it.itemId == R.id.update_payment -> {
                    this.viewModel.inputs.updatePaymentClicked()
                    true
                }
                it.itemId == R.id.cancel_pledge -> {
                    this.viewModel.inputs.cancelPledgeClicked()
                    true
                }
                it.itemId == R.id.contact_creator -> {
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
            updateFragments(projectData)
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
        val pledgeFragment = PledgeFragment.newInstance(pledgeDataAndPledgeReason.first, pledgeDataAndPledgeReason.second)
        val tag = PledgeFragment::class.java.simpleName
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

    private fun startVideoActivity(project: Project) {
        val intent = Intent(this, VideoActivity::class.java)
            .putExtra(IntentKey.PROJECT, project)
        startActivity(intent)
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
            val rewardsFragment = rewardsFragment()
            val backingFragment = backingFragment()
            if (rewardsFragment != null && backingFragment != null) {
                when {
                    supportFragmentManager.backStackEntryCount == 0 -> when {
                        projectData.project().isBacking -> if (!rewardsFragment.isHidden) {
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

    private fun updateManagePledgeMenu(@MenuRes menu: Int?) {
        menu?.let {
            binding.pledgeContainerLayout.pledgeToolbar.inflateMenu(it)
        } ?: run {
            binding.pledgeContainerLayout.pledgeToolbar.menu.clear()
        }
    }
}
