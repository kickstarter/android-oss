package com.kickstarter.ui.fragments.projectpage

import android.app.Activity
import android.content.Intent
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kickstarter.R
import com.kickstarter.databinding.FragmentProjectOverviewBinding
import com.kickstarter.libs.Configure
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.SocialUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deadlineCountdownDetail
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ProjectSocialActivity
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.setClickableHtml
import com.kickstarter.ui.extensions.startCreatorBioWebViewActivity
import com.kickstarter.ui.extensions.startLoginActivity
import com.kickstarter.ui.extensions.startProjectUpdatesActivity
import com.kickstarter.ui.extensions.startReportProjectActivity
import com.kickstarter.ui.extensions.startRootCommentsActivity
import com.kickstarter.viewmodels.projectpage.ProjectOverviewViewModel.ProjectOverviewViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime

class ProjectOverviewFragment : Fragment(), Configure {

    private lateinit var binding: FragmentProjectOverviewBinding
    private lateinit var ksString: KSString

    private lateinit var viewModelFactory: ProjectOverviewViewModel.Factory
    private val viewModel: ProjectOverviewViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentProjectOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    var startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringExtra(IntentKey.FLAGGINGKIND)
            data?.let {
                if (it.isNotEmpty()) {
                    this.viewModel.refreshFlaggedState(it)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = ProjectOverviewViewModel.Factory(env)
            env
        }

        ksString = viewModel.ksString

        viewModel.outputs.avatarPhotoUrl()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setAvatar(it) }
            .addToDisposable(disposables)

        viewModel.outputs.backersCountTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(binding.statsView.backersCount::setText)
            .addToDisposable(disposables)

        viewModel.outputs.blurbTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setBlurbTextViews(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.categoryTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.category.text = it
            }
            .addToDisposable(disposables)

        viewModel.outputs.commentsCountTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.projectCreatorInfoLayout.commentsCount.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.creatorDetailsLoadingContainerIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.loadingPlaceholderCreatorInfoLayout.creatorInfoLoadingContainer.isGone = !it
            }
            .addToDisposable(disposables)

        viewModel.outputs.creatorDetailsIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.creatorInfo.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.creatorNameTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.creatorName.text = it
            }
            .addToDisposable(disposables)

        viewModel.outputs.deadlineCountdownTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(binding.statsView.deadlineCountdownTextView::setText)
            .addToDisposable(disposables)

        viewModel.outputs.goalStringForTextView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setGoalTextView(it) }
            .addToDisposable(disposables)

        viewModel.outputs.locationTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.location.text = it
            }
            .addToDisposable(disposables)

        viewModel.outputs.projectOutput()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                context?.let { currentContext ->
                    binding.statsView.deadlineCountdownUnitTextView.text =
                        it.deadlineCountdownDetail(currentContext, ksString)
                }
            }
            .addToDisposable(disposables)

        viewModel.outputs.percentageFundedProgress()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(binding.percentageFunded::setProgress)
            .addToDisposable(disposables)

        viewModel.outputs.percentageFundedProgressBarIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.percentageFunded.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.pledgedTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(binding.statsView.pledged::setText)
            .addToDisposable(disposables)

        viewModel.outputs.projectDisclaimerGoalNotReachedString()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setProjectDisclaimerGoalNotReachedString(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectDisclaimerGoalReachedDateTime()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setProjectDisclaimerGoalReachedString(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectDisclaimerTextViewIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.projectCreatorInfoLayout.projectDisclaimerTextView.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectLaunchDate()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setProjectLaunchDateString(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectLaunchDateIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.projectCreatorDashboardHeader.projectLaunchDate.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectNameTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.projectName.text = it
            }
            .addToDisposable(disposables)

        viewModel.outputs.projectSocialTextViewFriends()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                context?.let { currentContext ->
                    binding.projectSocialText.text =
                        SocialUtils.projectCardFriendNamepile(currentContext, it, ksString)
                }
            }
            .addToDisposable(disposables)

        viewModel.outputs.projectSocialImageViewIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.projectSocialImage.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectSocialImageViewUrl()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { url: String? ->
                url?.let {
                    binding.projectSocialImage.loadCircleImage(it)
                }
            }
            .addToDisposable(disposables)

        viewModel.outputs.projectSocialViewGroupIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.projectSocialView.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectStateViewGroupBackgroundColorInt()
            .observeOn(AndroidSchedulers.mainThread())
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
            .addToDisposable(disposables)

        viewModel.outputs.projectStateViewGroupIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.projectStateViewGroup.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.setCanceledProjectStateView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setCanceledProjectStateView() }
            .addToDisposable(disposables)

        viewModel.outputs.setProjectSocialClickListener()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setProjectSocialClickListener() }
            .addToDisposable(disposables)

        viewModel.outputs.setSuccessfulProjectStateView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setSuccessfulProjectStateView(it) }
            .addToDisposable(disposables)

        viewModel.setSuccessfulProjectStillCollectingView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setSuccessfulProjectStillCollectingView(it) }
            .addToDisposable(disposables)

        viewModel.outputs.setSuspendedProjectStateView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setSuspendedProjectStateView() }
            .addToDisposable(disposables)

        viewModel.outputs.setUnsuccessfulProjectStateView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setUnsuccessfulProjectStateView(it) }
            .addToDisposable(disposables)

        viewModel.outputs.shouldSetDefaultStatsMargins()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setStatsMargins(it) }
            .addToDisposable(disposables)

        viewModel.outputs.startProjectSocialActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startProjectSocialActivity(it) }
            .addToDisposable(disposables)

        viewModel.outputs.updatesCountTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(binding.projectCreatorInfoLayout.updatesCount::setText)
            .addToDisposable(disposables)

        viewModel.outputs.conversionPledgedAndGoalText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pledgedAndGoal: Pair<String, String> -> setConvertedCurrencyView(pledgedAndGoal) }
            .addToDisposable(disposables)

        viewModel.outputs.conversionTextViewIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.usdConversionTextView.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.startCommentsView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                activity?.startRootCommentsActivity(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.startUpdatesView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                activity?.startProjectUpdatesActivity(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.startCreatorView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                activity?.startCreatorBioWebViewActivity(it.project())
            }
            .addToDisposable(disposables)

        viewModel.outputs.startReportProjectView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                activity?.startReportProjectActivity(it.project(), startForResult)
            }
            .addToDisposable(disposables)

        viewModel.outputs.startLoginView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                activity?.startLoginActivity()
            }
            .addToDisposable(disposables)

        viewModel.outputs.shouldShowReportProject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.projectCreatorInfoLayout.reportProject.isGone = !it
            }
            .addToDisposable(disposables)

        viewModel.outputs.shouldShowProjectFlagged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.projectCreatorInfoLayout.projectFlagged.isGone = !it
            }
            .addToDisposable(disposables)

        viewModel.outputs.openExternallyWithUrl()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                context?.let { it1 -> ApplicationUtils.openUrlExternally(it1, it) }
            }
            .addToDisposable(disposables)

        binding.creatorInfo.setOnClickListener {
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

        binding.projectCreatorInfoLayout.textReported.setClickableHtml { url ->
            this.viewModel.inputs.linkClicked(url)
        }
    }

    private fun setAvatar(url: String) {
        binding.avatar.loadCircleImage(url)
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
        val blurbHtml =
            Html.fromHtml(TextUtils.htmlEncode(blurb), FROM_HTML_MODE_LEGACY)
        binding.blurb.text = blurbHtml
    }

    private fun setProjectDisclaimerGoalReachedString(deadline: DateTime) {
        context?.let { currentContext ->
            binding.projectCreatorInfoLayout.projectDisclaimerTextView.text = ksString.format(
                currentContext.getString(R.string.project_disclaimer_goal_reached),
                "deadline",
                DateTimeUtils.mediumDateShortTimeWithTimeZone(deadline)
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
                DateTimeUtils.mediumDateShortTimeWithTimeZone(goalAndDeadline.second)
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

    private fun setSuccessfulProjectStillCollectingView(stateChangedAt: DateTime) {
        context?.let { currentContext ->
            binding.projectStateHeaderTextView.setText(R.string.project_status_funded)
            binding.projectStateSubheadTextView.text =
                ksString.format(
                    currentContext.getString(R.string.project_status_project_was_successfully_funded_on_deadline_but_you_can_still_pledge_for_available_rewards),
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
            val intent = Intent(currentContext, ProjectSocialActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
            activity?.startActivity(intent)
            activity?.overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.fade_out_slide_out_left
            )
        }
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel.inputs.configureWith(projectData)
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
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
