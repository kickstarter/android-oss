package com.kickstarter.ui.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ProjectAdapter
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.fragments.RewardsFragment
import com.kickstarter.viewmodels.ProjectViewModel
import kotlinx.android.synthetic.main.project_layout.*
import kotlinx.android.synthetic.main.project_toolbar.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ProjectViewModel.ViewModel::class)
class ProjectActivity : BaseActivity<ProjectViewModel.ViewModel>() {
    private lateinit var adapter: ProjectAdapter
    private lateinit var ksString: KSString

    private var grid8Dimen = R.dimen.grid_8

    private val projectBackButtonString = R.string.project_back_button
    private val managePledgeString = R.string.project_checkout_manage_navbar_title
    private val projectShareLabelString = R.string.project_accessibility_button_share_label
    private val projectShareCopyString = R.string.project_share_twitter_message
    private val projectStarConfirmationString = R.string.project_star_confirmation
    private val campaignString = R.string.project_subpages_menu_buttons_campaign
    private val creatorString = R.string.project_subpages_menu_buttons_creator

    private val animDuration = 200L
    private var rewardsExpanded = false
    private val show: ObjectAnimator = ObjectAnimator.ofFloat(null, View.ALPHA, 0f, 1f)
    private val hide: ObjectAnimator = ObjectAnimator.ofFloat(null, View.ALPHA, 1f, 0f)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.project_layout)
        this.ksString = environment().ksString()

        this.viewModel.outputs.heartDrawableId()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { heart_icon.setImageDrawable(ContextCompat.getDrawable(this, it)) }

        this.viewModel.outputs.projectAndUserCountry()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    this.renderProject(it.first, it.second)
                    this.setupRewardsFragment(it.first)
                }

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

        this.viewModel.outputs.showShareSheet()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.startShareIntent(it) }

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

        this.viewModel.outputs.viewToHide()
                .compose(observeForUI())
                .subscribe {
                    val view = findViewById<View>(it.first)
                    ViewUtils.setGone(view, true)

                    this.adapter = ProjectAdapter(this.viewModel, it.second)
                    project_recycler_view.adapter = this.adapter
                    project_recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
                }

        back_project_button.setOnClickListener {
            this.viewModel.inputs.backProjectButtonClicked()
        }

        heart_icon.setOnClickListener {
            this.viewModel.inputs.heartButtonClicked()
        }

        horizontal_fragment_pledge_button.setOnClickListener {
            animateRewards()
            rewardsExpanded = true
        }

        manage_pledge_button.setOnClickListener {
            this.viewModel.inputs.managePledgeButtonClicked()
        }

        project_action_buttons.visibility = when {
            ViewUtils.isLandscape(this) -> View.GONE
            else -> View.VISIBLE
        }

        rewards_toolbar.setNavigationOnClickListener {
            onBackPressed()
            project_toolbar.visibility = View.VISIBLE
        }

        share_icon.setOnClickListener {
            this.viewModel.inputs.shareButtonClicked()
        }

        show.addUpdateListener { valueAnim ->
            val initialRadius = resources.getDimensionPixelSize(R.dimen.fab_radius).toFloat()
            val radius = initialRadius * if (rewardsExpanded) 1 - valueAnim.animatedFraction else valueAnim.animatedFraction
            rewards_container.radius = radius
        }

        view_pledge_button.setOnClickListener {
            this.viewModel.inputs.viewPledgeButtonClicked()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.project_recycler_view.adapter = null
    }

    private fun animateRewards() {
        val set = AnimatorSet()
        show.target = if (rewardsExpanded) horizontal_fragment_pledge_button else pledge_container
        hide.target = if (rewardsExpanded) pledge_container else horizontal_fragment_pledge_button
        set.playTogether(show, hide)
        set.duration = animDuration

        val durationTransition = AutoTransition()
        durationTransition.duration = animDuration
        durationTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition) {
                if (rewardsExpanded) {
                    horizontal_fragment_pledge_button.visibility = View.GONE
                    project_toolbar.visibility = View.GONE
                }
            }

            override fun onTransitionResume(transition: Transition) {
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionCancel(transition: Transition) {
            }

            override fun onTransitionStart(transition: Transition) {
                set.start()
                if (!rewardsExpanded) {
                    horizontal_fragment_pledge_button.visibility = View.VISIBLE
                }
            }
        })

        TransitionManager.beginDelayedTransition(root, durationTransition)
        setRewardConstraints()
    }

    private fun renderProject(project: Project, configCountry: String) {
        this.adapter.takeProject(project, configCountry)
        ProjectUtils.setActionButton(project, this.back_project_button, this.manage_pledge_button, this.view_pledge_button)
    }

    private fun setupRewardsFragment(project: Project) {
        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, RewardsFragment.newInstance(project))
                .commit()
    }

    private fun setRewardConstraints() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(root)
        if (rewardsExpanded) {
            constraintSet.clear(R.id.rewards_container, ConstraintSet.BOTTOM)
            constraintSet.connect(R.id.rewards_container, ConstraintSet.TOP, R.id.guideline, ConstraintSet.TOP, 0)
        } else {
            constraintSet.connect(R.id.rewards_container, ConstraintSet.TOP, R.id.root, ConstraintSet.TOP, 0)
            constraintSet.connect(R.id.rewards_container, ConstraintSet.BOTTOM, R.id.root, ConstraintSet.BOTTOM, 0)
        }
        constraintSet.applyTo(root)
    }

    private fun startCampaignWebViewActivity(project: Project) {
        startWebViewActivity(getString(this.campaignString), project.descriptionUrl())
    }

    private fun startCreatorBioWebViewActivity(project: Project) {
        startWebViewActivity(getString(this.creatorString), project.creatorBioUrl())
    }

    private fun startProjectUpdatesActivity(project: Project) {
        val intent = Intent(this, ProjectUpdatesActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun showStarToast() {
        ViewUtils.showToastFromTop(this, getString(this.projectStarConfirmationString), 0, resources.getDimensionPixelSize(this.grid8Dimen))
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

    private fun startVideoActivity(project: Project) {
        val intent = Intent(this, VideoActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
        startActivity(intent)
    }

    override fun exitTransition(): Pair<Int, Int>? {
        return Pair.create(R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
    }

    override fun onBackPressed() {
        if (rewardsExpanded) {
            animateRewards()
            rewardsExpanded = false
        } else {
            super.onBackPressed()
        }
    }
}
