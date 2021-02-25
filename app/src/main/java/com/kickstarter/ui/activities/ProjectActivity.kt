package com.kickstarter.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.libs.*
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ProjectAdapter
import com.kickstarter.ui.data.*
import com.kickstarter.ui.fragments.*
import com.kickstarter.viewmodels.ProjectViewModel
import com.stripe.android.view.CardInputWidget
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.pledge_container.*
import kotlinx.android.synthetic.main.project_retry.*
import kotlinx.android.synthetic.main.project_toolbar.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ProjectViewModel.ViewModel::class)
class ProjectActivity : BaseActivity<ProjectViewModel.ViewModel>(), CancelPledgeFragment.CancelPledgeDelegate,
        NewCardFragment.OnCardSavedListener, PledgeFragment.PledgeDelegate, BackingFragment.BackingDelegate {
    private lateinit var adapter: ProjectAdapter
    private lateinit var ksString: KSString

    private val projectShareLabelString = R.string.project_accessibility_button_share_label
    private val projectShareCopyString = R.string.project_share_twitter_message
    private val projectStarConfirmationString = R.string.project_star_confirmation

    private val animDuration = 200L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)
        this.ksString = environment().ksString()

        val viewTreeObserver = pledge_container_root.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    this@ProjectActivity.viewModel.inputs.onGlobalLayout()
                    pledge_container_root.viewTreeObserver.removeOnGlobalLayoutListener(this)
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

        this.adapter = ProjectAdapter(this.viewModel)
        project_recycler_view.adapter = this.adapter
        project_recycler_view.layoutManager = LinearLayoutManager(this)

        this.viewModel.outputs.backingDetailsSubtitle()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setBackingDetailsSubtitle(it) }

        this.viewModel.outputs.backingDetailsTitle()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { backing_details_title.setText(it) }

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
                .subscribe { heart_icon.setImageDrawable(ContextCompat.getDrawable(this, it)) }

        this.viewModel.outputs.managePledgeMenu()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updateManagePledgeMenu(it) }

        this.viewModel.outputs.pledgeActionButtonColor()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { pledge_action_button.backgroundTintList = ContextCompat.getColorStateList(this, it) }

        this.viewModel.outputs.pledgeActionButtonContainerIsGone()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(pledge_action_buttons, it) }

        this.viewModel.outputs.pledgeActionButtonText()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setPledgeActionButtonCTA(it) }

        this.viewModel.outputs.pledgeToolbarNavigationIcon()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { pledge_toolbar.navigationIcon = ContextCompat.getDrawable(this, it) }

        this.viewModel.outputs.pledgeToolbarTitle()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { pledge_toolbar.title = getString(it) }

        this.viewModel.outputs.prelaunchUrl()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { openProjectAndFinish(it) }

        this.viewModel.outputs.projectData()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderProject(it) }

        this.viewModel.outputs.reloadProjectContainerIsGone()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(pledge_sheet_retry_container, it) }

        this.viewModel.outputs.reloadProgressBarIsGone()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(pledge_sheet_progress_bar, it) }

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

        this.viewModel.outputs.startCampaignWebViewActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startCampaignWebViewActivity(it) }

        this.viewModel.outputs.startCommentsActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startCommentsActivity(it) }

        this.viewModel.outputs.startCreatorBioWebViewActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startCreatorBioWebViewActivity(it) }

        this.viewModel.outputs.startCreatorDashboardActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startCreatorDashboardActivity(it) }

        this.viewModel.outputs.startProjectUpdatesActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startProjectUpdatesActivity(it) }

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

        setClickListeners()
    }

    override fun onResume() {
        super.onResume()

        this.viewModel.outputs.updateFragments()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updateFragments(it) }
    }

    override fun back() {
        if (pledge_container_root.visibility == View.GONE) {
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

    override fun onDestroy() {
        super.onDestroy()
        project_recycler_view.adapter = null
    }

    private fun animateScrimVisibility(show: Boolean) {
        val shouldAnimateIn = show && scrim.alpha <= 1f
        val shouldAnimateOut = !show && scrim.alpha >= 0f
        if (shouldAnimateIn || shouldAnimateOut) {
            val finalAlpha = if (show) 1f else 0f
            scrim.animate()
                    .alpha(finalAlpha)
                    .setDuration(200L)
                    .setListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator?) {
                            if (!show) {
                                ViewUtils.setGone(scrim, true)
                            }
                        }

                        override fun onAnimationStart(animation: Animator?) {
                            if (show) {
                                ViewUtils.setGone(scrim, false)
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
        val expand = expandAndAnimate.first
        val animate = expandAndAnimate.second
        val targetToShow = if (!expand) pledge_action_buttons else pledge_container
        val showRewardsFragmentAnimator = ObjectAnimator.ofFloat(targetToShow, View.ALPHA, 0f, 1f)

        val targetToHide = if (!expand) pledge_container else pledge_action_buttons
        val hideRewardsFragmentAnimator = ObjectAnimator.ofFloat(targetToHide, View.ALPHA, 1f, 0f)

        val guideline = rewardsSheetGuideline()
        val initialValue = (if (expand) pledge_container_root.height - guideline else 0).toFloat()
        val finalValue = (if (expand) 0 else pledge_container_root.height - guideline).toFloat()
        val initialRadius = resources.getDimensionPixelSize(R.dimen.fab_radius).toFloat()

        val pledgeContainerYAnimator = ObjectAnimator.ofFloat(pledge_container_root, View.Y, initialValue, finalValue).apply {
            addUpdateListener { valueAnim ->
                val radius = initialRadius * if (expand) 1 - valueAnim.animatedFraction else valueAnim.animatedFraction
                pledge_container_root.radius = radius
            }
        }

        AnimatorSet().apply {
            playTogether(showRewardsFragmentAnimator, hideRewardsFragmentAnimator, pledgeContainerYAnimator)
            duration = animDuration

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    if (expand) {
                        pledge_action_buttons.visibility = View.GONE
                        project_recycler_view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        toolbar.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        pledge_toolbar.requestFocus()
                    } else {
                        pledge_container.visibility = View.GONE
                        project_recycler_view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                        toolbar.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                        if (animate) {
                            toolbar.requestFocus()
                        }
                    }
                }

                override fun onAnimationStart(animation: Animator?) {
                    if (expand) {
                        pledge_container.visibility = View.VISIBLE
                    } else if (animate){
                        pledge_action_buttons.visibility = View.VISIBLE
                    }
                }
            })

            start()
        }
    }

    private fun handleNativeCheckoutBackPress() {
        val pledgeSheetIsExpanded = pledge_container_root.y == 0f

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

    private fun renderProject(projectData: ProjectData) {
        this.adapter.takeProject(projectData)
        project_recycler_view.setPadding(0, 0, 0, rewardsSheetGuideline())
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
            backing_details_subtitle.text = stringRes?.let { getString(it) }?: title
        }
    }

    private fun setClickListeners() {
        pledge_action_button.setOnClickListener {
            this.viewModel.inputs.nativeProjectActionButtonClicked()
        }

        pledge_toolbar.setNavigationOnClickListener {
            this.viewModel.inputs.pledgeToolbarNavigationClicked()
        }

        pledge_toolbar.setOnMenuItemClickListener {
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

        pledge_sheet_retry_container.setOnClickListener {
            this.viewModel.inputs.reloadProjectContainerClicked()
        }

        heart_icon.setOnClickListener {
            this.viewModel.inputs.heartButtonClicked()
        }

        share_icon.setOnClickListener {
            this.viewModel.inputs.shareButtonClicked()
        }
    }

    private fun setInitialRewardsContainerY() {
        val guideline = rewardsSheetGuideline()
        pledge_container_root.y = (root.height - guideline).toFloat()
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
        showSnackbar(snackbar_anchor, getString(R.string.Youve_canceled_your_pledge))
    }

    private fun showCreatePledgeSuccess(checkoutDatandProjectData: Pair<CheckoutData, PledgeData>) {
        val checkoutData = checkoutDatandProjectData.first
        val pledgeData = checkoutDatandProjectData.second
        val projectData = pledgeData.projectData()
        if (clearFragmentBackStack()) {
            updateFragments(projectData)
            startActivity(Intent(this, ThanksActivity::class.java)
                    .putExtra(IntentKey.PROJECT, projectData.project())
                    .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
                    .putExtra(IntentKey.PLEDGE_DATA, pledgeData))
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
        pledge_action_button.setText(stringRes)
        pledge_action_button.contentDescription = when (stringRes) {
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

    private fun startCampaignWebViewActivity(projectData: ProjectData) {
        val intent = Intent(this, CampaignDetailsActivity::class.java)
                .putExtra(IntentKey.PROJECT_DATA, projectData)
        startActivityForResult(intent, ActivityRequestCodes.SHOW_REWARDS)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startCreatorBioWebViewActivity(project: Project) {
        val intent = Intent(this, CreatorBioActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.URL, project.creatorBioUrl())
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startCreatorDashboardActivity(project: Project) {
        val intent = Intent(this, CreatorDashboardActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProjectUpdatesActivity(projectAndData:  Pair<Project, ProjectData> ) {
        val intent = Intent(this, ProjectUpdatesActivity::class.java)
                .putExtra(IntentKey.PROJECT, projectAndData.first)
                .putExtra(IntentKey.PROJECT_DATA, projectAndData.second)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startCommentsActivity(projectAndData: Pair<Project, ProjectData>) {
        val intent = Intent(this, CommentsActivity::class.java)
                .putExtra(IntentKey.PROJECT, projectAndData.first)
                .putExtra(IntentKey.PROJECT_DATA, projectAndData.second)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
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
        startActivity(Intent(this, MessagesActivity::class.java)
                .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Message.PROJECT_PAGE)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.BACKING, project.backing()))
    }

    private fun startVideoActivity(project: Project) {
        val intent = Intent(this, VideoActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
        startActivity(intent)
    }

    private fun styleProjectActionButton(detailsAreVisible: Boolean) {
        val buttonParams = pledge_action_button.layoutParams as LinearLayout.LayoutParams
        when {
            detailsAreVisible -> {
                backing_details.visibility = View.VISIBLE
                buttonParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                pledge_action_button.cornerRadius = resources.getDimensionPixelSize(R.dimen.grid_2)
            }
            else -> {
                backing_details.visibility = View.GONE
                buttonParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                pledge_action_button.cornerRadius = resources.getDimensionPixelSize(R.dimen.fab_radius)
            }
        }
        pledge_action_button.layoutParams = buttonParams
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
            pledge_toolbar.inflateMenu(it)
        } ?: run {
            pledge_toolbar.menu.clear()
        }
    }
}
