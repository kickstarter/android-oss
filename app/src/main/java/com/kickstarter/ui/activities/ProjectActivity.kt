package com.kickstarter.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Pair
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.MenuRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.extensions.hideKeyboard
import com.kickstarter.extensions.showSnackbar
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.KoalaContext
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ProjectViewUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ProjectAdapter
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
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
        NewCardFragment.OnCardSavedListener, PledgeFragment.PledgeDelegate {
    private lateinit var adapter: ProjectAdapter
    private lateinit var ksString: KSString

    private val projectBackButtonString = R.string.project_back_button
    private val managePledgeString = R.string.project_checkout_manage_navbar_title
    private val projectShareLabelString = R.string.project_accessibility_button_share_label
    private val projectShareCopyString = R.string.project_share_twitter_message
    private val projectStarConfirmationString = R.string.project_star_confirmation
    private val campaignString = R.string.project_subpages_menu_buttons_campaign

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

        this.viewModel.outputs.backingDetailsIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { styleProjectActionButton(it) }

        this.viewModel.outputs.backingDetails()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { reward_infos.text = it }

        this.viewModel.outputs.expandPledgeSheet()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { expandPledgeSheet(it) }

        this.viewModel.outputs.heartDrawableId()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { heart_icon.setImageDrawable(ContextCompat.getDrawable(this, it)) }

        this.viewModel.outputs.horizontalProgressBarIsGone()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(project_progress_bar, it) }

        this.viewModel.outputs.managePledgeMenu()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updateManagePledgeMenu(it) }

        this.viewModel.outputs.pledgeActionButtonContainerIsGone()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(pledge_action_buttons, it) }

        this.viewModel.outputs.pledgeContainerIsGone()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(pledge_container_root, it) }

        this.viewModel.outputs.prelaunchUrl()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { openProjectAndFinish(it) }

        this.viewModel.outputs.projectActionButtonContainerIsGone()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(project_action_buttons, if (ViewUtils.isLandscape(this)) true else it) }

        this.viewModel.outputs.projectAndNativeCheckoutEnabled()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderProject(it) }

        this.viewModel.outputs.reloadProjectContainerIsGone()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(pledge_sheet_retry_container, it) }

        this.viewModel.outputs.retryProgressBarIsGone()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(pledge_sheet_progress_bar, it) }

        this.viewModel.outputs.rewardsButtonColor()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { project_action_button.backgroundTintList = ContextCompat.getColorStateList(this@ProjectActivity, it) }

        this.viewModel.outputs.rewardsButtonText()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setProjectActionCTA(it) }

        this.viewModel.outputs.rewardsToolbarTitle()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { pledge_toolbar.title = getString(it) }

        this.viewModel.outputs.setInitialRewardsContainerY()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setInitialRewardsContainerY() }

        this.viewModel.outputs.showSavedPrompt()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.showStarToast() }

        this.viewModel.outputs.showShareSheet()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { startShareIntent(it) }

        this.viewModel.outputs.startBackingActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startBackingActivity(it) }

        this.viewModel.outputs.startCampaignWebViewActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startCampaignWebViewActivity(it) }

        this.viewModel.outputs.startCheckoutActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startCheckoutActivity(it) }

        this.viewModel.outputs.startCommentsActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startCommentsActivity(it) }

        this.viewModel.outputs.startCreatorBioWebViewActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startCreatorBioWebViewActivity(it) }

        this.viewModel.outputs.startProjectUpdatesActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startProjectUpdatesActivity(it) }

        this.viewModel.outputs.startVideoActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startVideoActivity(it) }

        this.viewModel.outputs.startManagePledgeActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startManagePledge(it) }

        this.viewModel.outputs.startLoginToutActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startLoginToutActivity() }

        this.viewModel.outputs.scrimIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { animateScrimVisibility(it) }

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

        this.viewModel.outputs.showUpdatePledge()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showPledgeFragment(it) }

        this.viewModel.outputs.startMessagesActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { startMessagesActivity(it) }

        this.viewModel.outputs.startThanksActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showCreatePledgeSuccess(it) }

        setClickListeners()
    }

    override fun onResume() {
        super.onResume()

        this.viewModel.outputs.showRewardsFragment()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showRewardsFragment(it) }

        this.viewModel.outputs.showBackingFragment()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showBackingFragment(it) }
    }

    private fun setClickListeners() {
        project_action_button.setOnClickListener {
            this.viewModel.inputs.nativeProjectActionButtonClicked()
        }

        pledge_toolbar.setNavigationOnClickListener {
            this.viewModel.inputs.collapsePledgeSheet()
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

        project_action_buttons.visibility = when {
            ViewUtils.isLandscape(this) -> View.GONE
            else -> View.VISIBLE
        }

        back_project_button.setOnClickListener {
            this.viewModel.inputs.backProjectButtonClicked()
        }

        manage_pledge_button.setOnClickListener {
            this.viewModel.inputs.managePledgeButtonClicked()
        }

        view_pledge_button.setOnClickListener {
            this.viewModel.inputs.viewPledgeButtonClicked()
        }

        heart_icon.setOnClickListener {
            this.viewModel.inputs.heartButtonClicked()
        }

        share_icon.setOnClickListener {
            this.viewModel.inputs.shareButtonClicked()
        }
    }

    private fun setProjectActionCTA(stringRes: Int) {
        project_action_button.setText(stringRes)
        project_action_button.contentDescription = when (stringRes) {
            R.string.Manage -> getString(R.string.Manage_your_pledge)
            else -> getString(stringRes)
        }
    }

    override fun back() {
        if (pledge_container != null) {
            handleNativeCheckoutBackPress()
        } else {
            super.back()
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

    override fun pledgeSuccessfullyCreated() {
        this.viewModel.inputs.pledgeSuccessfullyCreated()
    }

    override fun pledgeSuccessfullyUpdated() {
        this.viewModel.inputs.pledgeSuccessfullyUpdated()
    }

    override fun cardSaved(storedCard: StoredCard) {
        val pledgeFragment = supportFragmentManager.findFragmentByTag(PledgeFragment::class.java.simpleName) as PledgeFragment?
        pledgeFragment?.cardAdded(storedCard)
        supportFragmentManager.popBackStack()
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

        val rewardsContainerYAnimator = ObjectAnimator.ofFloat(pledge_container_root, View.Y, initialValue, finalValue).apply {
            addUpdateListener { valueAnim ->
                val radius = initialRadius * if (expand) 1 - valueAnim.animatedFraction else valueAnim.animatedFraction
                pledge_container_root.radius = radius
            }
        }

        AnimatorSet().apply {
            playTogether(showRewardsFragmentAnimator, hideRewardsFragmentAnimator, rewardsContainerYAnimator)
            duration = when {
                animate -> animDuration
                else -> 0L
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    if (expand) {
                        pledge_action_buttons.visibility = View.GONE
                        this@ProjectActivity.project_recycler_view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        toolbar.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        pledge_toolbar.requestFocus()
                    } else {
                        pledge_container.visibility = View.GONE
                        this@ProjectActivity.project_recycler_view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                        toolbar.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                        if (animate) {
                            toolbar.requestFocus()
                        }
                    }
                }

                override fun onAnimationStart(animation: Animator?) {
                    if (expand) {
                        pledge_container.visibility = View.VISIBLE
                    } else {
                        pledge_action_buttons.visibility = View.VISIBLE
                    }
                }
            })

            start()
        }
    }

    private fun handleNativeCheckoutBackPress() {
        val pledgeSheetIsExpanded = pledge_container.alpha == 1f

        val pledgeFragment = supportFragmentManager.findFragmentByTag(PledgeFragment::class.java.simpleName) as PledgeFragment?
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        val backStackIsNotEmpty = backStackEntryCount > 0
        val topOfStackIndex = backStackEntryCount.minus(1)

        val pledgeReason = when {
            backStackIsNotEmpty && supportFragmentManager.getBackStackEntryAt(topOfStackIndex).name == PledgeFragment::class.java.simpleName -> {
                pledgeFragment?.arguments?.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON)
            }
            else -> null
        }

        when {
            pledgeReason == PledgeReason.PLEDGE || pledgeReason == PledgeReason.UPDATE_REWARD -> pledgeFragment?.backPressed()
            backStackIsNotEmpty && pledgeSheetIsExpanded -> supportFragmentManager.popBackStack()
            pledgeSheetIsExpanded -> this.viewModel.inputs.collapsePledgeSheet()
            else -> {
                clearFragmentBackStack()
                super.back()
            }
        }
    }

    private fun openProjectAndFinish(url: String) {
        val uri = Uri.parse(url)

        val fakeUri = Uri.parse("http://www.kickstarter.com")
        val browserIntent = Intent(Intent.ACTION_VIEW, fakeUri)
        val queryIntentActivities = packageManager.queryIntentActivities(browserIntent, 0)
        val targetIntents = queryIntentActivities
                .filter { !it.activityInfo.packageName.contains("com.kickstarter") }
                .map { resolveInfo ->
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage(resolveInfo.activityInfo.packageName)
                    intent
                }
                .toMutableList()

        if (targetIntents.isNotEmpty()) {
            /* We need to remove the first intent so it's not duplicated
            when we add the EXTRA_INITIAL_INTENTS intents. */
            val chooserIntent = Intent.createChooser(targetIntents.removeAt(0), "")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toTypedArray())
            startActivity(chooserIntent)
        }

        finish()
    }

    private fun renderProject(projectAndNativeCheckoutEnabled: Pair<Project, Boolean>) {
        val project = projectAndNativeCheckoutEnabled.first
        val nativeCheckoutEnabled = projectAndNativeCheckoutEnabled.second
        this.adapter.takeProject(project, nativeCheckoutEnabled)
        ProjectViewUtils.setActionButton(project, this.back_project_button, this.manage_pledge_button, this.view_pledge_button)
    }

    private fun renderProject(backingFragment: BackingFragment, rewardsFragment: RewardsFragment, project: Project) {
        rewardsFragment.takeProject(project)
        backingFragment.takeProject(project)
    }

    private fun revealRewardsFragment() {
        val rewardsFragment = supportFragmentManager.findFragmentById(R.id.fragment_rewards) as RewardsFragment
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .show(rewardsFragment)
                .addToBackStack(RewardsFragment::class.java.simpleName)
                .commit()
    }

    private fun rewardsSheetGuideline(): Int = resources.getDimensionPixelSize(R.dimen.reward_fragment_guideline_constraint_end)

    private fun setInitialRewardsContainerY() {
        val guideline = rewardsSheetGuideline()
        pledge_container_root.y = (root.height - guideline).toFloat()
    }

    private fun showBackingFragment(project: Project) {
        val (rewardsFragment, backingFragment) = supportFragmentManager.findFragmentById(R.id.fragment_rewards) as RewardsFragment to
                supportFragmentManager.findFragmentById(R.id.fragment_backing) as BackingFragment
        if (!rewardsFragment.isHidden && supportFragmentManager.backStackEntryCount == 0 && !isFinishing) {
            supportFragmentManager.beginTransaction()
                    .show(backingFragment)
                    .hide(rewardsFragment)
                    .commitNow()
        }
        renderProject(backingFragment, rewardsFragment, project)
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

    private fun showCreatePledgeSuccess(project: Project) {
        if (clearFragmentBackStack()) {
            showBackingFragment(project)
            startActivity(Intent(this, ThanksActivity::class.java)
                    .putExtra(IntentKey.PROJECT, project))
        }
    }

    private fun showPledgeNotCancelableDialog() {
        AlertDialog.Builder(this, R.style.Dialog)
                .setMessage(R.string.We_dont_allow_cancelations_that_will_cause_a_project_to_fall_short_of_its_goal_within_the_last_24_hours)
                .setPositiveButton(getString(R.string.general_alert_buttons_ok)) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun showUpdatePledgeSuccess() {
        clearFragmentBackStack()
        val backingFragment = supportFragmentManager.findFragmentById(R.id.fragment_backing) as BackingFragment
        backingFragment.pledgeSuccessfullyUpdated()
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

    private fun showRewardsFragment(project: Project) {
        val (rewardsFragment, backingFragment) = supportFragmentManager.findFragmentById(R.id.fragment_rewards) as RewardsFragment to
                supportFragmentManager.findFragmentById(R.id.fragment_backing) as BackingFragment
        if (!backingFragment.isHidden && supportFragmentManager.backStackEntryCount == 0 && !isFinishing) {
            supportFragmentManager.beginTransaction()
                    .show(rewardsFragment)
                    .hide(backingFragment)
                    .commitNow()
        }
        renderProject(backingFragment, rewardsFragment, project)
    }

    private fun showStarToast() {
        ViewUtils.showToastFromTop(this, getString(this.projectStarConfirmationString), 0, resources.getDimensionPixelSize(R.dimen.grid_8))
    }

    private fun startCampaignWebViewActivity(project: Project) {
        startWebViewActivity(getString(this.campaignString), project.descriptionUrl())
    }

    private fun startCreatorBioWebViewActivity(project: Project) {
        val intent = Intent(this, CreatorBioActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.URL, project.creatorBioUrl())
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProjectUpdatesActivity(project: Project) {
        val intent = Intent(this, ProjectUpdatesActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startCheckoutActivity(project: Project) {
        val intent = Intent(this, CheckoutActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.URL, project.newPledgeUrl())
                .putExtra(IntentKey.TOOLBAR_TITLE, this.projectBackButtonString)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startManagePledge(project: Project) {
        val intent = Intent(this, CheckoutActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.URL, project.editPledgeUrl())
                .putExtra(IntentKey.TOOLBAR_TITLE, this.managePledgeString)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startCommentsActivity(project: Project) {
        val intent = Intent(this, CommentsActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
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

    private fun startWebViewActivity(toolbarTitle: String, url: String) {
        val intent = Intent(this, WebViewActivity::class.java)
                .putExtra(IntentKey.TOOLBAR_TITLE, toolbarTitle)
                .putExtra(IntentKey.URL, url)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startLoginToutActivity() {
        val intent = Intent(this, LoginToutActivity::class.java)
                .putExtra(IntentKey.LOGIN_REASON, LoginReason.STAR_PROJECT)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
    }

    private fun startBackingActivity(projectAndBacker: Pair<Project, User>) {
        val intent = Intent(this, BackingActivity::class.java)
                .putExtra(IntentKey.PROJECT, projectAndBacker.first)
                .putExtra(IntentKey.BACKER, projectAndBacker.second)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
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
        val buttonParams = project_action_button.layoutParams as LinearLayout.LayoutParams
        when {
            detailsAreVisible -> {
                backing_details.visibility = View.VISIBLE
                buttonParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                project_action_button.cornerRadius = resources.getDimensionPixelSize(R.dimen.grid_2)
            }
            else -> {
                backing_details.visibility = View.GONE
                buttonParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                project_action_button.cornerRadius = resources.getDimensionPixelSize(R.dimen.fab_radius)
            }
        }
        project_action_button.layoutParams = buttonParams
    }

    private fun updateManagePledgeMenu(@MenuRes menu: Int?) {
        menu?.let {
            pledge_toolbar.inflateMenu(it)
        } ?: run {
            pledge_toolbar.menu.clear()
        }
    }
}
