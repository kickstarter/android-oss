package com.kickstarter.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.Pair
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ProjectAdapter
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.fragments.*
import com.kickstarter.viewmodels.ProjectViewModel
import com.stripe.android.view.StripeEditText
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.project_layout.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ProjectViewModel.ViewModel::class)
class ProjectActivity : BaseActivity<ProjectViewModel.ViewModel>(), CancelPledgeFragment.CancelPledgeDelegate, NewCardFragment.OnCardSavedListener {
    private lateinit var adapter: ProjectAdapter
    private lateinit var ksString: KSString
    private lateinit var heartIcon: ImageButton
    private lateinit var projectRecyclerView: RecyclerView
    private lateinit var shareIcon: ImageButton

    private val projectBackButtonString = R.string.project_back_button
    private val managePledgeString = R.string.project_checkout_manage_navbar_title
    private val projectShareLabelString = R.string.project_accessibility_button_share_label
    private val projectShareCopyString = R.string.project_share_twitter_message
    private val projectStarConfirmationString = R.string.project_star_confirmation
    private val campaignString = R.string.project_subpages_menu_buttons_campaign

    private val animDuration = 200L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(when {
            environment().nativeCheckoutPreference().get() -> R.layout.activity_project
            else -> R.layout.project_layout
        })
        this.ksString = environment().ksString()

        this.projectRecyclerView = findViewById(R.id.project_recycler_view)
        this.heartIcon = findViewById(R.id.heart_icon)
        this.shareIcon = findViewById(R.id.share_icon)

        when {
            environment().nativeCheckoutPreference().get() -> {
                val viewTreeObserver = rewards_container.viewTreeObserver
                if (viewTreeObserver.isAlive) {
                    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            this@ProjectActivity.viewModel.inputs.onGlobalLayout()
                            rewards_container.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                    })
                }

                project_action_button.setOnClickListener {
                    this.viewModel.inputs.nativeProjectActionButtonClicked()
                }

                rewards_toolbar.setNavigationOnClickListener {
                    this.viewModel.inputs.hideRewardsSheetClicked()
                }

                rewards_toolbar.setOnMenuItemClickListener {
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

                this.supportFragmentManager.addOnBackStackChangedListener {
                    this.viewModel.inputs.fragmentStackCount(this.supportFragmentManager.backStackEntryCount)
                }

            }
            else -> {
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
            }
        }

        this.adapter = ProjectAdapter(this.viewModel)
        projectRecyclerView.adapter = this.adapter
        projectRecyclerView.layoutManager = LinearLayoutManager(this)

        this.viewModel.outputs.heartDrawableId()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.heartIcon.setImageDrawable(ContextCompat.getDrawable(this, it)) }

        this.viewModel.outputs.backingDetailsIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { styleProjectActionButton(it) }

        this.viewModel.outputs.projectAndUserCountry()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderProject(it) }

        this.viewModel.outputs.backingDetails()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { reward_infos.text = it }

        this.viewModel.outputs.rewardsButtonColor()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { project_action_button.backgroundTintList = ContextCompat.getColorStateList(this@ProjectActivity, it) }

        this.viewModel.outputs.rewardsButtonText()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { project_action_button.setText(it) }

        this.viewModel.outputs.rewardsToolbarTitle()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { rewards_toolbar.title = getString(it) }

        this.viewModel.outputs.setInitialRewardsContainerY()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setInitialRewardsContainerY() }

        this.viewModel.outputs.expandPledgeSheet()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { animateRewards(it) }

        this.viewModel.outputs.showShareSheet()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startShareIntent(it) }

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

        this.viewModel.outputs.startProjectUpdatesActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startProjectUpdatesActivity(it) }

        this.viewModel.outputs.startVideoActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startVideoActivity(it) }

        this.viewModel.outputs.startCheckoutActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startCheckoutActivity(it) }

        this.viewModel.outputs.startManagePledgeActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startManagePledge(it) }

        this.viewModel.outputs.startBackingActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startBackingActivity(it) }

        this.viewModel.outputs.showSavedPrompt()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.showStarToast() }

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

        this.viewModel.outputs.showRewardsFragment()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showRewardsFragment(it) }

        this.viewModel.outputs.showBackingFragment()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showBackingFragment(it) }

        this.viewModel.outputs.managePledgeMenuIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { toggleManagePledgeVisibility(it) }

        this.viewModel.outputs.showCancelPledgeFragment()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showCancelPledgeFragment(it) }

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

        this.heartIcon.setOnClickListener {
            this.viewModel.inputs.heartButtonClicked()
        }

        this.shareIcon.setOnClickListener {
            this.viewModel.inputs.shareButtonClicked()
        }
    }

    override fun back() {
        if (environment().nativeCheckoutPreference().get()) {
            handleNativeCheckoutBackPress()
        } else {
            super.back()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            var view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                if (view is StripeEditText) {
                    view = view.parent as View
                }
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    hideKeyboard()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun pledgeSuccessfullyCancelled() {
        this.viewModel.inputs.pledgeSuccessfullyCancelled()
    }

    override fun cardSaved(storedCard: StoredCard) {
        val pledgeFragment = supportFragmentManager.findFragmentByTag(PledgeFragment::class.java.simpleName) as PledgeFragment?
        pledgeFragment?.cardAdded(storedCard)
        supportFragmentManager.popBackStack()
    }

    override fun exitTransition(): Pair<Int, Int>? {
        return Pair.create(R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.projectRecyclerView.adapter = null
    }

    private fun animateRewards(expand: Boolean) {
        val targetToShow = if (!expand) action_buttons else pledge_container
        val showRewardsFragmentAnimator = ObjectAnimator.ofFloat(targetToShow, View.ALPHA, 0f, 1f)

        val targetToHide = if (!expand) pledge_container else action_buttons
        val hideRewardsFragmentAnimator = ObjectAnimator.ofFloat(targetToHide, View.ALPHA, 1f, 0f)

        val guideline = rewardsSheetGuideline()
        val initialValue = (if (expand) rewards_container.height - guideline else 0).toFloat()
        val finalValue = (if (expand) 0 else rewards_container.height - guideline).toFloat()
        val initialRadius = resources.getDimensionPixelSize(R.dimen.fab_radius).toFloat()

        val rewardsContainerYAnimator = ObjectAnimator.ofFloat(rewards_container, View.Y, initialValue, finalValue).apply {
            addUpdateListener { valueAnim ->
                val radius = initialRadius * if (expand) 1 - valueAnim.animatedFraction else valueAnim.animatedFraction
                rewards_container.radius = radius
            }
        }

        AnimatorSet().apply {
            playTogether(showRewardsFragmentAnimator, hideRewardsFragmentAnimator, rewardsContainerYAnimator)
            duration = animDuration

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    if (expand) {
                        action_buttons.visibility = View.GONE
                    }
                }

                override fun onAnimationStart(animation: Animator?) {
                    if (!expand) {
                        action_buttons.visibility = View.VISIBLE
                    }
                }
            })

            start()
        }
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

    private fun clearFragmentBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun handleNativeCheckoutBackPress() {
        val rewardsSheetIsExpanded = pledge_container.alpha == 1f
        when {
            supportFragmentManager.backStackEntryCount > 0 && rewardsSheetIsExpanded -> supportFragmentManager.popBackStack()
            rewardsSheetIsExpanded -> this.viewModel.inputs.hideRewardsSheetClicked()
            else -> {
                clearFragmentBackStack()
                super.back()
            }
        }
    }

    private fun renderProject(projectAndCountry: Pair<Project, String>) {
        val project = projectAndCountry.first
        val country = projectAndCountry.second
        this.adapter.takeProject(project, country, environment().nativeCheckoutPreference().get())
        if (!environment().nativeCheckoutPreference().get()) {
            ProjectViewUtils.setActionButton(project, this.back_project_button, this.manage_pledge_button, this.view_pledge_button, null)
        }
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

    private fun rewardsSheetGuideline(): Int = when {
        ViewUtils.isLandscape(this) -> 0
        else -> resources.getDimensionPixelSize(R.dimen.reward_fragment_guideline_constraint_end)
    }

    private fun setInitialRewardsContainerY() {
        val guideline = rewardsSheetGuideline()
        rewards_container.y = (rewards_container.height - guideline).toFloat()
        this.projectRecyclerView.setPadding(0, 0, 0, guideline)
    }

    private fun showRewardsFragment(project: Project) {
        val (rewardsFragment, backingFragment) = supportFragmentManager.findFragmentById(R.id.fragment_rewards) as RewardsFragment to
        supportFragmentManager.findFragmentById(R.id.fragment_backing) as BackingFragment
        if(!backingFragment.isHidden && supportFragmentManager.backStackEntryCount == 0) {
            supportFragmentManager.beginTransaction()
                    .show(rewardsFragment)
                    .hide(backingFragment)
                    .commit()
        }
        renderProject(backingFragment, rewardsFragment,project)
    }

    private fun showCancelPledgeFragment(project: Project) {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                .add(R.id.fragment_container, CancelPledgeFragment.newInstance(project), CancelPledgeFragment::class.java.simpleName)
                .addToBackStack(CancelPledgeFragment::class.java.simpleName)
                .commit()
    }

    private fun showPledgeFragment(pledgeDataAndPledgeReason: Pair<PledgeData, PledgeReason>) {
        val pledgeFragment = PledgeFragment.newInstance(pledgeDataAndPledgeReason.first)
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                .add(R.id.fragment_container, pledgeFragment, PledgeFragment::class.java.simpleName)
                .addToBackStack(PledgeFragment::class.java.simpleName)
                .commit()
    }

    private fun showBackingFragment(project: Project) {
        val (rewardsFragment, backingFragment) = supportFragmentManager.findFragmentById(R.id.fragment_rewards) as RewardsFragment to
        supportFragmentManager.findFragmentById(R.id.fragment_backing) as BackingFragment
        if(!rewardsFragment.isHidden && supportFragmentManager.backStackEntryCount == 0) {
            supportFragmentManager.beginTransaction()
                    .show(backingFragment)
                    .hide(rewardsFragment)
                    .commit()
        }
        renderProject(backingFragment, rewardsFragment, project)
    }

    private fun showCancelPledgeSuccess() {
        clearFragmentBackStack()
        Handler().postDelayed({
            showSnackbar(snackbar_anchor, getString(R.string.Youve_canceled_your_pledge))
        }, this.animDuration)
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

    private fun showStarToast() {
        ViewUtils.showToastFromTop(this, getString(this.projectStarConfirmationString), 0, resources.getDimensionPixelSize(R.dimen.grid_8))
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

    // todo: limit the apps you can share to
    private fun startShareIntent(project: Project) {
        val shareMessage = this.ksString.format(getString(this.projectShareCopyString), "project_title", project.name())

        val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, shareMessage + " " + project.webProjectUrl())
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

    private fun toggleManagePledgeVisibility(visible: Boolean) {
        if (visible) rewards_toolbar.inflateMenu(R.menu.manage_pledge) else rewards_toolbar.menu.clear()
    }
}
