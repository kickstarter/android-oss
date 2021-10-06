package com.kickstarter.ui.fragments.projectpage

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.TextUtils
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.libs.utils.SocialUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Photo
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ProjectSocialActivity
import com.kickstarter.ui.data.ProjectData
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

        viewModel.outputs.backingViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMediaHeaderLayout.projectMetadataLayout.backingGroup))

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
                context?.let { currentContext ->
                    val categoryTextView = if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        binding.projectMediaHeaderLayout.category
                    } else {
                        binding.category
                    }
                    categoryTextView?.text = it
                }
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
                context?.let { currentContext ->
                    val creatorInfoLoadingContainer = if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        binding.projectMediaHeaderLayout.loadingPlaceholderCreatorInfoLayout?.creatorInfoLoadingContainer
                    } else {
                        binding.loadingPlaceholderCreatorInfoLayout?.creatorInfoLoadingContainer
                    }
                    creatorInfoLoadingContainer?.let { loadingContainer -> ViewUtils.setGone(loadingContainer, !it) }
                }
            }

        viewModel.outputs.creatorDetailsVariantIsVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setCreatorDetailsVariantVisibility(it) }

        viewModel.outputs.creatorNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                context?.let { currentContext ->
                    val creatorNameTextView = if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        binding.projectMediaHeaderLayout.creatorName
                    } else {
                        binding.creatorName
                    }
                    creatorNameTextView?.text = it
                }
            }

        viewModel.outputs.creatorNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                context?.let { currentContext ->
                    val creatorNameVariantTextView =
                        if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            binding.projectMediaHeaderLayout.creatorNameVariant
                        } else {
                            binding.creatorNameVariant
                        }
                    creatorNameVariantTextView?.text = it
                }
            }

        viewModel.outputs.deadlineCountdownTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.statsView.deadlineCountdownTextView::setText)

        viewModel.outputs.featuredTextViewRootCategory()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                context?.let { currentContext ->
                    binding.projectMediaHeaderLayout.projectMetadataLayout.featured.text =
                        (
                            ksString.format(
                                currentContext.getString(R.string.discovery_baseball_card_metadata_featured_project),
                                "category_name",
                                it
                            )
                            )
                }
            }

        viewModel.outputs.featuredViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMediaHeaderLayout.projectMetadataLayout.featuredGroup))

        viewModel.outputs.goalStringForTextView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setGoalTextView(it) }

        viewModel.outputs.locationTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                context?.let { currentContext ->
                    val locationTextView =
                        if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            binding.projectMediaHeaderLayout.location
                        } else {
                            binding.location
                        }
                    locationTextView?.text = it
                }
            }

        viewModel.outputs.projectOutput()
            .subscribe {
                context?.let { currentContext ->
                    setLandscapeOverlayText(it)
                    binding.statsView.deadlineCountdownUnitTextView.text =
                        ProjectUtils.deadlineCountdownDetail(it, currentContext, ksString)
                }
            }
        viewModel.outputs.percentageFundedProgress()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.percentageFunded::setProgress)

        viewModel.outputs.percentageFundedProgressBarIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.percentageFunded))

        viewModel.outputs.playButtonIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMediaHeaderLayout.playButtonOverlay))

        viewModel.outputs.pledgedTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.statsView.pledged::setText)

        viewModel.outputs.projectDashboardButtonText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(binding.projectCreatorDashboardHeader.projectDashboardButton::setText)

        binding.projectCreatorDashboardHeader.projectDashboardButton.setOnClickListener {
            creatorDashboardOnClick()
        }

        viewModel.outputs.projectDashboardContainerIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectCreatorDashboardHeader.projectDashboardContainer))

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
            .subscribe(ViewUtils.setGone(binding.projectCreatorInfoLayout.projectDisclaimerTextView))

        viewModel.outputs.projectLaunchDate()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setProjectLaunchDateString(it) }

        viewModel.outputs.projectLaunchDateIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectCreatorDashboardHeader.projectLaunchDate))

        viewModel.outputs.projectMetadataViewGroupBackgroundDrawableInt()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                context?.let { currentContext ->
                    binding.projectMediaHeaderLayout.projectMetadataLayout.projectMetadataViewGroup.background =
                        ContextCompat.getDrawable(currentContext, it)
                }
            }

        viewModel.outputs.projectMetadataViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMediaHeaderLayout.projectMetadataLayout.projectMetadataViewGroup))

        viewModel.outputs.projectNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                context?.let { currentContext ->
                    val projectNameTextView =
                        if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            binding.projectMediaHeaderLayout.projectName
                        } else {
                            binding.projectName
                        }
                    projectNameTextView?.text = it
                }
            }

        viewModel.outputs.projectPhoto()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setProjectPhoto(it) }

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
            .subscribe(ViewUtils.setGone(binding.projectSocialImage))

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
            .subscribe(ViewUtils.setGone(binding.projectSocialView))

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
            .subscribe(ViewUtils.setGone(binding.projectStateViewGroup))

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
            .subscribe(ViewUtils.setGone(binding.usdConversionTextView))

        context?.let { currentContext ->
            if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.projectMediaHeaderLayout.readMore
            } else {
                binding.readMore
            }?.let { readMoreButton ->
                readMoreButton.setOnClickListener {
                    blurbVariantOnClick()
                }
            }

            if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.projectMediaHeaderLayout.readMore
            } else {
                binding.readMore
            }?.let { readMoreButton ->
                readMoreButton.setOnClickListener {
                    blurbVariantOnClick()
                }
            }

            if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.projectMediaHeaderLayout.blurbView
            } else {
                binding.blurbView
            }?.let { blurbView ->
                blurbView.setOnClickListener {
                    blurbOnClick()
                }
            }
            if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.projectMediaHeaderLayout.creatorInfo
            } else {
                binding.creatorInfo
            }?.let { creatorInfo ->
                creatorInfo.setOnClickListener {
                    creatorNameOnClick()
                }
            }

            if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.projectMediaHeaderLayout.creatorInfoVariant
            } else {
                binding.creatorInfoVariant
            }?.let { creatorInfoVariant ->
                creatorInfoVariant.setOnClickListener {
                    creatorInfoVariantOnClick()
                }
            }
        }

        binding.projectCreatorInfoLayout.campaign.setOnClickListener {
            blurbOnClick()
        }
        binding.projectCreatorInfoLayout.comments.setOnClickListener {
            commentsOnClick()
        }
        binding.projectCreatorInfoLayout.updates.setOnClickListener {
            updatesOnClick()
        }
    }

    private fun setAvatar(url: String) {
        context?.let { currentContext ->
            val avatarImageView =
                if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    binding.projectMediaHeaderLayout.avatar
                } else {
                    binding.avatar
                }
            Picasso.get()
                .load(url)
                .transform(CircleTransformation())
                .into(avatarImageView)

            val avatarVariantImageView =
                if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    binding.projectMediaHeaderLayout.creatorAvatarVerified?.avatarVariant
                } else {
                    binding.creatorAvatarVerified?.avatarVariant
                }
            Picasso.get()
                .load(url)
                .transform(CircleTransformation())
                .into(avatarVariantImageView)
        }
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
            val creatorDetailsTextView =
                if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    binding.projectMediaHeaderLayout.creatorDetails
                } else {
                    binding.creatorDetails
                }
            creatorDetailsTextView?.text = ksString.format(
                currentContext.getString(R.string.projects_launched_count_created_projects_backed_count_backed),
                "projects_backed_count", NumberUtils.format(backedAndLaunchedProjectsCount.first),
                "projects_launched_count", NumberUtils.format(backedAndLaunchedProjectsCount.second)
            )
        }
    }

    private fun setCreatorDetailsVariantVisibility(visible: Boolean) {
        context?.let { currentContext ->
            if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.projectMediaHeaderLayout.creatorInfoVariant
            } else {
                binding.creatorInfoVariant
            }?.let { creatorInfoVariantContainer ->
                ViewUtils.setGone(creatorInfoVariantContainer, !visible)
            }

            if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.projectMediaHeaderLayout.creatorInfo
            } else {
                binding.creatorInfo
            }?.let { creatorInfoContainer ->
                ViewUtils.setGone(creatorInfoContainer, visible)
            }
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

    private fun setProjectPhoto(photo: Photo) {
        context?.let { currentContext ->
            // Account for the grid2 start and end margins.
            val targetImageWidth =
                (ViewUtils.getScreenWidthDp(currentContext) * ViewUtils.getScreenDensity(currentContext)).toInt() - currentContext.resources.getDimension(
                    R.dimen.grid_2
                ).toInt() * 2
            val targetImageHeight = ProjectUtils.photoHeightFromWidthRatio(targetImageWidth)
            binding.projectMediaHeaderLayout.projectPhoto.maxHeight = targetImageHeight

            ResourcesCompat.getDrawable(currentContext.resources, R.drawable.gray_gradient, null)?.let {
                Picasso.get()
                    .load(photo.full())
                    .resize(targetImageWidth, targetImageHeight)
                    .centerCrop()
                    .placeholder(it)
                    .into(binding.projectMediaHeaderLayout.projectPhoto)
            }
        }
    }

    private fun setCanceledProjectStateView() {
        binding.projectStateHeaderTextView.setText(R.string.project_status_funded)
        binding.projectStateSubheadTextView.setText(R.string.project_status_funding_project_canceled_by_creator)
    }

    private fun setBlurbTextViews(blurb: String) {
        context?.let { currentContext ->
            val blurbHtml = Html.fromHtml(TextUtils.htmlEncode(blurb))
            if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.projectMediaHeaderLayout.blurb
            } else {
                binding.blurb
            }?.let { blurbTextView ->
                blurbTextView.text = blurbHtml
            }

            if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.projectMediaHeaderLayout.blurbVariant
            } else {
                binding.blurbVariant
            }?.let { blurbVariantTextView ->
                blurbVariantTextView.text = blurbHtml
            }
        }
    }

    private fun setBlurbVariantVisibility(blurbVariantVisible: Boolean) {
        context?.let { currentContext ->
            if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.projectMediaHeaderLayout.blurbView
            } else {
                binding.blurbView
            }?.let { blurbViewGroup ->
                ViewUtils.setGone(blurbViewGroup, blurbVariantVisible)
            }

            if (currentContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.projectMediaHeaderLayout.blurbViewVariant
            } else {
                binding.blurbViewVariant
            }?.let { blurbVariantViewGroup ->
                ViewUtils.setGone(blurbVariantViewGroup, !blurbVariantVisible)
            }
        }
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

    /**
     * Set top margin of overlay text based on landscape screen height, scaled by screen density.
     */
    private fun setLandscapeOverlayText(project: Project) {
        context?.let { currentContext ->
            if (binding.projectMediaHeaderLayout.landOverlayText != null && binding.projectMediaHeaderLayout.nameCreatorView != null) {
                val screenHeight = ViewUtils.getScreenHeightDp(currentContext)
                val densityOffset = currentContext.resources.displayMetrics.density
                val topMargin =
                    screenHeight / 3f * 2 * densityOffset - currentContext.resources.getDimension(R.dimen.grid_10)
                        .toInt()
                ViewUtils.setRelativeViewGroupMargins(
                    binding.projectMediaHeaderLayout.landOverlayText!!,
                    currentContext.resources.getDimension(R.dimen.grid_4).toInt(),
                    topMargin.toInt(),
                    currentContext.resources.getDimension(R.dimen.grid_4).toInt(),
                    0
                )
                if (!project.hasVideo()) {
                    ViewUtils.setRelativeViewGroupMargins(
                        binding.projectMediaHeaderLayout.nameCreatorView!!,
                        0,
                        0,
                        0,
                        currentContext.resources.getDimension(R.dimen.grid_2).toInt()
                    )
                } else {
                    ViewUtils.setRelativeViewGroupMargins(
                        binding.projectMediaHeaderLayout.nameCreatorView!!,
                        0,
                        0,
                        0,
                        currentContext.resources.getDimension(R.dimen.grid_1).toInt()
                    )
                }
            }
        }
    }

    private fun blurbOnClick() {
    }

    private fun blurbVariantOnClick() {
    }

    private fun commentsOnClick() {
    }

    private fun creatorNameOnClick() {
    }

    private fun creatorInfoVariantOnClick() {
    }

    private fun creatorDashboardOnClick() {
    }

    private fun playButtonOnClick() {
    }

    private fun updatesOnClick() {
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
