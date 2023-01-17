package com.kickstarter.ui.fragments.projectpage

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.SpannableString
import android.text.TextUtils
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.kickstarter.R
import com.kickstarter.databinding.FragmentProjectOverviewBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Configure
import com.kickstarter.libs.KSString
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.SocialUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.deadlineCountdownDetail
import com.kickstarter.libs.utils.extensions.setGone
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ProjectSocialActivity
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.startCampaignWebViewActivity
import com.kickstarter.ui.extensions.startCreatorBioWebViewActivity
import com.kickstarter.ui.extensions.startCreatorDashboardActivity
import com.kickstarter.ui.extensions.startLoginActivity
import com.kickstarter.ui.extensions.startProjectUpdatesActivity
import com.kickstarter.ui.extensions.startRootCommentsActivity
import com.kickstarter.viewmodels.projectpage.ProjectOverviewViewModel
import com.squareup.picasso.Picasso
import org.joda.time.DateTime

@RequiresFragmentViewModel(ProjectOverviewViewModel.ViewModel::class)
class ProjectOverviewFragment : BaseFragment<ProjectOverviewViewModel.ViewModel>(), Configure {

    private lateinit var binding: FragmentProjectOverviewBinding
    private lateinit var ksString: KSString

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentProjectOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ksString = viewModel.kSString

        viewModel.outputs.avatarPhotoUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setAvatar(it) }

        viewModel.outputs.backersCountTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.statsView.backersCount::setText)

        viewModel.outputs.blurbTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setBlurbTextViews(it) }

        viewModel.outputs.blurbVariantIsVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setBlurbVariantVisibility(it) }

        this.viewModel.outputs.categoryTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.category.text = it
            }

        viewModel.outputs.commentsCountTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.projectCreatorInfoLayout.commentsCount.text = it }

        viewModel.outputs.creatorBackedAndLaunchedProjectsCount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setCreatorDetailsTextView(it) }

        viewModel.outputs.creatorDetailsLoadingContainerIsVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.loadingPlaceholderCreatorInfoLayout.creatorInfoLoadingContainer.setGone(!it)
            }

        viewModel.outputs.creatorDetailsVariantIsVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setCreatorDetailsVariantVisibility(it) }

        viewModel.outputs.creatorNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.creatorName.text = it
            }

        viewModel.outputs.creatorNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.creatorNameVariant.text = it
            }

        viewModel.outputs.deadlineCountdownTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.statsView.deadlineCountdownTextView::setText)

        viewModel.outputs.goalStringForTextView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setGoalTextView(it) }

        viewModel.outputs.locationTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.location.text = it
            }

        viewModel.outputs.projectOutput()
            .subscribe {
                context?.let { currentContext ->
                    // setLandscapeOverlayText(it)
                    binding.statsView.deadlineCountdownUnitTextView.text =
                        it.deadlineCountdownDetail(currentContext, ksString)
                }
            }

        viewModel.outputs.percentageFundedProgress()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.percentageFunded::setProgress)

        viewModel.outputs.percentageFundedProgressBarIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.percentageFunded::setGone)

        viewModel.outputs.pledgedTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.statsView.pledged::setText)

        viewModel.outputs.projectDashboardButtonText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.projectCreatorDashboardHeader.projectDashboardButton::setText)

        viewModel.outputs.projectDashboardContainerIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.projectCreatorDashboardHeader.projectDashboardContainer::setGone)

        viewModel.outputs.projectDisclaimerGoalNotReachedString()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setProjectDisclaimerGoalNotReachedString(it) }

        viewModel.outputs.projectDisclaimerGoalReachedDateTime()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setProjectDisclaimerGoalReachedString(it) }

        viewModel.outputs.projectDisclaimerTextViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.projectCreatorInfoLayout.projectDisclaimerTextView::setGone)

        viewModel.outputs.projectLaunchDate()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setProjectLaunchDateString(it) }

        viewModel.outputs.projectLaunchDateIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.projectCreatorDashboardHeader.projectLaunchDate.setGone())

        viewModel.outputs.projectNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.projectName.text = it
            }

        viewModel.outputs.projectSocialTextViewFriends()
            .compose(bindToLifecycle())
            .compose<List<User?>>(Transformers.observeForUI())
            .subscribe {
                context?.let { currentContext ->
                    binding.projectSocialText.text =
                        SocialUtils.projectCardFriendNamepile(currentContext, it, ksString)
                }
            }

        viewModel.outputs.projectSocialImageViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.projectSocialImage.setGone())

        viewModel.outputs.projectSocialImageViewUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { url: String? ->
                url?.let {
                    Picasso.get()
                        .load(it)
                        .transform(CircleTransformation())
                        .into(binding.projectSocialImage)
                }
            }

        viewModel.outputs.projectSocialViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.projectSocialView::setGone)

        viewModel.outputs.projectStateViewGroupBackgroundColorInt()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                context?.let { currentContext ->
                    binding.projectStateViewGroup.setBackgroundColor(
                        ContextCompat.getColor(
                            currentContext,
                            it
                        )
                    )
                }
            }

        viewModel.outputs.projectStateViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.projectStateViewGroup::setGone)

        viewModel.outputs.setCanceledProjectStateView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setCanceledProjectStateView() }

        viewModel.outputs.setProjectSocialClickListener()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setProjectSocialClickListener() }

        viewModel.outputs.setSuccessfulProjectStateView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setSuccessfulProjectStateView(it) }

        viewModel.outputs.setSuspendedProjectStateView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setSuspendedProjectStateView() }

        viewModel.outputs.setUnsuccessfulProjectStateView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setUnsuccessfulProjectStateView(it) }

        viewModel.outputs.shouldSetDefaultStatsMargins()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setStatsMargins(it) }

        viewModel.outputs.startProjectSocialActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startProjectSocialActivity(it) }

        viewModel.outputs.updatesCountTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.projectCreatorInfoLayout.updatesCount::setText)

        viewModel.outputs.conversionPledgedAndGoalText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { pledgedAndGoal: Pair<String, String> -> setConvertedCurrencyView(pledgedAndGoal) }

        viewModel.outputs.conversionTextViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.usdConversionTextView.setGone())

        viewModel.outputs.startCommentsView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                activity?.startRootCommentsActivity(it)
            }

        viewModel.outputs.startUpdatesView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                activity?.startProjectUpdatesActivity(it)
            }

        viewModel.outputs.startCreatorView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                activity?.startCreatorBioWebViewActivity(it.project())
            }

        viewModel.outputs.startCreatorDashboardView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                activity?.startCreatorDashboardActivity(it.project())
            }

        viewModel.outputs.startCampaignView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                activity?.startCampaignWebViewActivity(it)
            }

        viewModel.outputs.startReportProjectView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                Toast.makeText(this.activity, "Will open next screen soon", Toast.LENGTH_SHORT).show()
            }

        viewModel.outputs.startLoginView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                activity?.startLoginActivity()
            }

        viewModel.outputs.shouldShowReportProject()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.projectCreatorInfoLayout.reportProject.setGone(!it)
            }

        viewModel.outputs.shouldShowProjectFlagged()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.projectCreatorInfoLayout.projectFlagged.setGone(!it)
            }

        binding.projectCreatorDashboardHeader.projectDashboardButton.setOnClickListener {
            this.viewModel.inputs.creatorDashboardClicked()
        }

        binding.creatorInfo.setOnClickListener {
            this.viewModel.inputs.creatorInfoButtonClicked()
        }

        binding.creatorInfoVariant.setOnClickListener {
            this.viewModel.inputs.creatorInfoButtonClicked()
        }

        binding.blurbView.setOnClickListener {
            this.viewModel.inputs.campaignButtonClicked()
        }

        binding.projectCreatorInfoLayout.comments.setOnClickListener {
            this.viewModel.inputs.commentsButtonClicked()
        }

        binding.projectCreatorInfoLayout.updates.setOnClickListener {
            this.viewModel.inputs.updatesButtonClicked()
        }

        binding.projectCreatorInfoLayout.reportProject.setOnClickListener {
            this.viewModel.inputs.reportProjectButtonClicked()
        }
    }

    private fun setAvatar(url: String) {
        Picasso.get()
            .load(url)
            .transform(CircleTransformation())
            .into(binding.avatar)

        Picasso.get()
            .load(url)
            .transform(CircleTransformation())
            .into(binding.creatorAvatarVerified.avatarVariant)
    }

    private fun setConvertedCurrencyView(pledgedAndGoal: Pair<String, String>) {
        context?.let { currentContext ->
            binding.usdConversionTextView.text = ksString.format(
                currentContext.getString(R.string.discovery_baseball_card_stats_convert_from_pledged_of_goal),
                "pledged",
                pledgedAndGoal.first,
                "goal",
                pledgedAndGoal.second
            )
        }
    }

    private fun setCreatorDetailsTextView(backedAndLaunchedProjectsCount: Pair<Int, Int>) {
        context?.let { currentContext ->
            binding.creatorDetails.text = ksString.format(
                currentContext.getString(R.string.projects_launched_count_created_projects_backed_count_backed),
                "projects_backed_count", NumberUtils.format(backedAndLaunchedProjectsCount.first),
                "projects_launched_count", NumberUtils.format(backedAndLaunchedProjectsCount.second)
            )
        }
    }

    private fun setCreatorDetailsVariantVisibility(visible: Boolean) {
        binding.creatorInfoVariant.setGone(!visible)
        binding.creatorInfo.setGone(visible)
    }

    private fun setGoalTextView(goalString: String) {
        context?.let { currentContext ->
            val goalText = if (ViewUtils.isFontScaleLarge(currentContext))
                ksString.format(
                    currentContext.getString(R.string.discovery_baseball_card_stats_pledged_of_goal_short),
                    "goal",
                    goalString
                )
            else
                ksString.format(
                    currentContext.getString(R.string.discovery_baseball_card_stats_pledged_of_goal),
                    "goal",
                    goalString
                )

            binding.statsView.goal.text = goalText
        }
    }

    private fun setCanceledProjectStateView() {
        binding.projectStateHeaderTextView.setText(R.string.project_status_funded)
        binding.projectStateSubheadTextView.setText(R.string.project_status_funding_project_canceled_by_creator)
    }

    private fun setBlurbTextViews(blurb: String) {
        val blurbHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(TextUtils.htmlEncode(blurb), FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(TextUtils.htmlEncode(blurb))
        }
        binding.blurb.text = blurbHtml
    }

    private fun setBlurbVariantVisibility(blurbVariantVisible: Boolean) {
        binding.blurbView.setGone(blurbVariantVisible)
    }

    private fun setProjectDisclaimerGoalReachedString(deadline: DateTime) {
        context?.let { currentContext ->
            binding.projectCreatorInfoLayout.projectDisclaimerTextView.text = ksString.format(
                currentContext.getString(R.string.project_disclaimer_goal_reached),
                "deadline",
                DateTimeUtils.mediumDateShortTime(deadline)
            )
        }
    }

    private fun setProjectDisclaimerGoalNotReachedString(goalAndDeadline: Pair<String, DateTime>) {
        context?.let { currentContext ->
            binding.projectCreatorInfoLayout.projectDisclaimerTextView.text = ksString.format(
                currentContext.getString(R.string.project_disclaimer_goal_not_reached),
                "goal_currency",
                goalAndDeadline.first,
                "deadline",
                DateTimeUtils.mediumDateShortTime(goalAndDeadline.second)
            )
        }
    }

    private fun setProjectLaunchDateString(launchDate: String) {
        context?.let { currentContext ->
            val launchedDateSpannableString = SpannableString(
                ksString.format(
                    currentContext.getString(R.string.You_launched_this_project_on_launch_date),
                    "launch_date",
                    launchDate
                )
            )
            ViewUtils.addBoldSpan(launchedDateSpannableString, launchDate)
            binding.projectCreatorDashboardHeader.projectLaunchDate.text =
                launchedDateSpannableString
        }
    }

    private fun setProjectSocialClickListener() {
        context?.let { currentContext ->
            binding.projectSocialView.background = ResourcesCompat.getDrawable(
                currentContext.resources,
                R.drawable.click_indicator_light_masked,
                null
            )
            binding.projectSocialView.setOnClickListener { viewModel.inputs.projectSocialViewGroupClicked() }
        }
    }

    private fun setSuccessfulProjectStateView(stateChangedAt: DateTime) {
        context?.let { currentContext ->
            binding.projectStateHeaderTextView.setText(R.string.project_status_funded)
            binding.projectStateSubheadTextView.text =
                ksString.format(
                    currentContext.getString(R.string.project_status_project_was_successfully_funded_on_deadline),
                    "deadline",
                    DateTimeUtils.mediumDate(stateChangedAt)
                )
        }
    }

    private fun setSuspendedProjectStateView() {
        binding.projectStateHeaderTextView.setText(R.string.project_status_funding_suspended)
        binding.projectStateSubheadTextView.setText(R.string.project_status_funding_project_suspended)
    }

    private fun setUnsuccessfulProjectStateView(stateChangedAt: DateTime) {
        context?.let { currentContext ->
            binding.projectStateHeaderTextView.text =
                currentContext.getString(R.string.project_status_funding_unsuccessful)
            binding.projectStateSubheadTextView.text = ksString.format(
                currentContext.getString(R.string.project_status_project_funding_goal_not_reached),
                "deadline",
                DateTimeUtils.mediumDate(stateChangedAt)
            )
        }
    }

    private fun setStatsMargins(shouldSetDefaultMargins: Boolean) {
        context?.let { currentContext ->
            (binding.projectStatsView as? LinearLayout)?.let { projectStatsView ->
                if (shouldSetDefaultMargins) {
                    ViewUtils.setLinearViewGroupMargins(
                        projectStatsView,
                        0, currentContext.resources.getDimension(R.dimen.grid_3).toInt(),
                        0, currentContext.resources.getDimension(R.dimen.grid_2).toInt()
                    )
                } else {
                    ViewUtils.setLinearViewGroupMargins(
                        projectStatsView,
                        0, currentContext.resources.getDimension(R.dimen.grid_3).toInt(),
                        0, currentContext.resources.getDimension(R.dimen.grid_4).toInt()
                    )
                }
            }
        }
    }

    private fun startProjectSocialActivity(project: Project) {
        context?.let { currentContext ->
            val activity = currentContext as BaseActivity<*>
            val intent = Intent(currentContext, ProjectSocialActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
            activity.startActivity(intent)
            activity.overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.fade_out_slide_out_left
            )
        }
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel?.inputs?.configureWith(projectData)
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): ProjectOverviewFragment {
            val fragment = ProjectOverviewFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.PROJECT_PAGER_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
