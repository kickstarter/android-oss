package com.kickstarter.ui.viewholders

import android.content.Intent
import android.content.res.Configuration
import android.text.Html
import android.text.SpannableString
import android.text.TextUtils
import android.util.Pair
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.kickstarter.R
import com.kickstarter.databinding.ProjectMainLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Photo
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ProjectSocialActivity
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.ProjectHolderViewModel
import com.squareup.picasso.Picasso
import org.joda.time.DateTime

class ProjectViewHolder(
    private val binding: ProjectMainLayoutBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {
    private val viewModel = ProjectHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()

    interface Delegate {
        fun projectViewHolderBlurbClicked(viewHolder: ProjectViewHolder)
        fun projectViewHolderBlurbVariantClicked(viewHolder: ProjectViewHolder)
        fun projectViewHolderCommentsClicked(viewHolder: ProjectViewHolder)
        fun projectViewHolderCreatorClicked(viewHolder: ProjectViewHolder)
        fun projectViewHolderCreatorInfoVariantClicked(viewHolder: ProjectViewHolder)
        fun projectViewHolderDashboardClicked(viewHolder: ProjectViewHolder)
        fun projectViewHolderUpdatesClicked(viewHolder: ProjectViewHolder)
        fun projectViewHolderVideoStarted(viewHolder: ProjectViewHolder)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectData = ObjectUtils.requireNonNull(data as ProjectData?)
        viewModel.inputs.configureWith(projectData)
    }

    private fun setAvatar(url: String) {
        val avatarImageView = if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.avatar
        } else {
            binding.avatar
        }
        Picasso.get()
            .load(url)
            .transform(CircleTransformation())
            .into(avatarImageView)

        val avatarVariantImageView = if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.creatorAvatarVerified?.avatarVariant
        } else {
            binding.creatorAvatarVerified?.avatarVariant
        }
        Picasso.get()
            .load(url)
            .transform(CircleTransformation())
            .into(avatarVariantImageView)
    }

    private fun setConvertedCurrencyView(pledgedAndGoal: Pair<String, String>) {
        binding.usdConversionTextView.text = ksString.format(
            context().getString(R.string.discovery_baseball_card_stats_convert_from_pledged_of_goal), "pledged", pledgedAndGoal.first, "goal", pledgedAndGoal.second
        )
    }

    private fun setCreatorDetailsTextView(backedAndLaunchedProjectsCount: Pair<Int, Int>) {
        val creatorDetailsTextView = if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.creatorDetails
        } else {
            binding.creatorDetails
        }
        creatorDetailsTextView?.text = ksString.format(
            context().getString(R.string.projects_launched_count_created_projects_backed_count_backed),
            "projects_backed_count", NumberUtils.format(backedAndLaunchedProjectsCount.first),
            "projects_launched_count", NumberUtils.format(backedAndLaunchedProjectsCount.second)
        )
    }

    private fun setCreatorDetailsVariantVisibility(visible: Boolean) {
        if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.creatorInfoVariant
        } else {
            binding.creatorInfoVariant
        }?.let { creatorInfoVariantContainer ->
            ViewUtils.setGone(creatorInfoVariantContainer, !visible)
        }

        if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.creatorInfo
        } else {
            binding.creatorInfo
        }?.let { creatorInfoContainer ->
            ViewUtils.setGone(creatorInfoContainer, visible)
        }
    }

    private fun setGoalTextView(goalString: String) {
        val goalText = if (ViewUtils.isFontScaleLarge(context()))
            ksString.format(context().getString(R.string.discovery_baseball_card_stats_pledged_of_goal_short), "goal", goalString)
        else
            ksString.format(context().getString(R.string.discovery_baseball_card_stats_pledged_of_goal), "goal", goalString)

        binding.statsView.goal.text = goalText
    }

    private fun setProjectPhoto(photo: Photo) {
        // Account for the grid2 start and end margins.
        val targetImageWidth = (ViewUtils.getScreenWidthDp(context()) * ViewUtils.getScreenDensity(context())).toInt() - context().resources.getDimension(R.dimen.grid_2).toInt() * 2
        val targetImageHeight = ProjectUtils.photoHeightFromWidthRatio(targetImageWidth)
        binding.projectMediaHeaderLayout.projectPhoto.maxHeight = targetImageHeight

        ResourcesCompat.getDrawable(context().resources, R.drawable.gray_gradient, null)?.let {
            Picasso.get()
                .load(photo.full())
                .resize(targetImageWidth, targetImageHeight)
                .centerCrop()
                .placeholder(it)
                .into(binding.projectMediaHeaderLayout.projectPhoto)
        }
    }

    private fun setCanceledProjectStateView() {
        binding.projectStateHeaderTextView.setText(R.string.project_status_funded)
        binding.projectStateSubheadTextView.setText(R.string.project_status_funding_project_canceled_by_creator)
    }

    private fun setBlurbTextViews(blurb: String) {
        val blurbHtml = Html.fromHtml(TextUtils.htmlEncode(blurb))
        if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.blurb
        } else {
            binding.blurb
        }?.let { blurbTextView ->
            blurbTextView.text = blurbHtml
        }

        if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.blurbVariant
        } else {
            binding.blurbVariant
        }?.let { blurbVariantTextView ->
            blurbVariantTextView.text = blurbHtml
        }
    }

    private fun setBlurbVariantVisibility(blurbVariantVisible: Boolean) {
        if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.blurbView
        } else {
            binding.blurbView
        }?.let { blurbViewGroup ->
            ViewUtils.setGone(blurbViewGroup, blurbVariantVisible)
        }

        if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.blurbViewVariant
        } else {
            binding.blurbViewVariant
        }?.let { blurbVariantViewGroup ->
            ViewUtils.setGone(blurbVariantViewGroup, !blurbVariantVisible)
        }
    }

    private fun setProjectDisclaimerGoalReachedString(deadline: DateTime) {
        binding.projectCreatorInfoLayout.projectDisclaimerTextView.text = ksString.format(
            context().getString(R.string.project_disclaimer_goal_reached),
            "deadline",
            DateTimeUtils.mediumDateShortTime(deadline)
        )
    }

    private fun setProjectDisclaimerGoalNotReachedString(goalAndDeadline: Pair<String, DateTime>) {
        binding.projectCreatorInfoLayout.projectDisclaimerTextView.text = ksString.format(
            context().getString(R.string.project_disclaimer_goal_not_reached),
            "goal_currency",
            goalAndDeadline.first,
            "deadline",
            DateTimeUtils.mediumDateShortTime(goalAndDeadline.second)
        )
    }

    private fun setProjectLaunchDateString(launchDate: String) {
        val launchedDateSpannableString = SpannableString(
            ksString.format(
                context().getString(R.string.You_launched_this_project_on_launch_date),
                "launch_date",
                launchDate
            )
        )
        ViewUtils.addBoldSpan(launchedDateSpannableString, launchDate)
        binding.projectCreatorDashboardHeader.projectLaunchDate.text = launchedDateSpannableString
    }

    private fun setProjectSocialClickListener() {
        binding.projectSocialView.background = ResourcesCompat.getDrawable(context().resources, R.drawable.click_indicator_light_masked, null)
        binding.projectSocialView.setOnClickListener { viewModel.inputs.projectSocialViewGroupClicked() }
    }

    private fun setSuccessfulProjectStateView(stateChangedAt: DateTime) {
        binding.projectStateHeaderTextView.setText(R.string.project_status_funded)
        binding.projectStateSubheadTextView.text =
            ksString.format(context().getString(R.string.project_status_project_was_successfully_funded_on_deadline), "deadline", DateTimeUtils.mediumDate(stateChangedAt))
    }

    private fun setSuspendedProjectStateView() {
        binding.projectStateHeaderTextView.setText(R.string.project_status_funding_suspended)
        binding.projectStateSubheadTextView.setText(R.string.project_status_funding_project_suspended)
    }

    private fun setUnsuccessfulProjectStateView(stateChangedAt: DateTime) {
        binding.projectStateHeaderTextView.text = context().getString(R.string.project_status_funding_unsuccessful)
        binding.projectStateSubheadTextView.text = ksString.format(context().getString(R.string.project_status_project_funding_goal_not_reached), "deadline", DateTimeUtils.mediumDate(stateChangedAt))
    }

    private fun setStatsMargins(shouldSetDefaultMargins: Boolean) {
        (binding.projectStatsView as? LinearLayout)?.let { projectStatsView ->
            if (shouldSetDefaultMargins) {
                ViewUtils.setLinearViewGroupMargins(
                    projectStatsView,
                    0, context().resources.getDimension(R.dimen.grid_3).toInt(),
                    0, context().resources.getDimension(R.dimen.grid_2).toInt()
                )
            } else {
                ViewUtils.setLinearViewGroupMargins(
                    projectStatsView,
                    0, context().resources.getDimension(R.dimen.grid_3).toInt(),
                    0, context().resources.getDimension(R.dimen.grid_4).toInt()
                )
            }
        }
    }

    private fun startProjectSocialActivity(project: Project) {
        val activity = context() as BaseActivity<*>
        val intent = Intent(context(), ProjectSocialActivity::class.java)
            .putExtra(IntentKey.PROJECT, project)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    /**
     * Set top margin of overlay text based on landscape screen height, scaled by screen density.
     */
    private fun setLandscapeOverlayText(project: Project) {
        if (binding.projectMediaHeaderLayout.landOverlayText != null && binding.projectMediaHeaderLayout.nameCreatorView != null) {
            val screenHeight = ViewUtils.getScreenHeightDp(context())
            val densityOffset = context().resources.displayMetrics.density
            val topMargin = screenHeight / 3f * 2 * densityOffset - context().resources.getDimension(R.dimen.grid_10).toInt()
            ViewUtils.setRelativeViewGroupMargins(
                binding.projectMediaHeaderLayout.landOverlayText, context().resources.getDimension(R.dimen.grid_4).toInt(),
                topMargin.toInt(), context().resources.getDimension(R.dimen.grid_4).toInt(), 0
            )
            if (!project.hasVideo()) {
                ViewUtils.setRelativeViewGroupMargins(binding.projectMediaHeaderLayout.nameCreatorView, 0, 0, 0, context().resources.getDimension(R.dimen.grid_2).toInt())
            } else {
                ViewUtils.setRelativeViewGroupMargins(binding.projectMediaHeaderLayout.nameCreatorView, 0, 0, 0, context().resources.getDimension(R.dimen.grid_1).toInt())
            }
        }
    }

    init {

        viewModel.outputs.avatarPhotoUrl()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setAvatar(it) }
        viewModel.outputs.backersCountTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(binding.statsView.backersCount::setText)
        viewModel.outputs.backingViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMediaHeaderLayout.projectMetadataLayout.backingGroup))
        viewModel.outputs.blurbTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setBlurbTextViews(it) }

        viewModel.outputs.blurbVariantIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setBlurbVariantVisibility(it) }

        this.viewModel.outputs.categoryTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                val categoryTextView = if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    binding.projectMediaHeaderLayout.category
                } else {
                    binding.category
                }
                categoryTextView?.text = it
            }

        viewModel.outputs.commentsCountTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.projectCreatorInfoLayout.commentsCount.text = it }

        viewModel.outputs.creatorBackedAndLaunchedProjectsCount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setCreatorDetailsTextView(it) }

        viewModel.outputs.creatorDetailsLoadingContainerIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                val creatorInfoLoadingContainer = if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    binding.projectMediaHeaderLayout.loadingPlaceholderCreatorInfoLayout?.creatorInfoLoadingContainer
                } else {
                    binding.loadingPlaceholderCreatorInfoLayout?.creatorInfoLoadingContainer
                }
                creatorInfoLoadingContainer?.let { loadingContainer -> ViewUtils.setGone(loadingContainer, !it) }
            }

        viewModel.outputs.creatorDetailsVariantIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setCreatorDetailsVariantVisibility(it) }

        viewModel.outputs.creatorNameTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                val creatorNameTextView = if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    binding.projectMediaHeaderLayout.creatorName
                } else {
                    binding.creatorName
                }
                creatorNameTextView?.text = it
            }

        viewModel.outputs.creatorNameTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                val creatorNameVariantTextView = if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    binding.projectMediaHeaderLayout.creatorNameVariant
                } else {
                    binding.creatorNameVariant
                }
                creatorNameVariantTextView?.text = it
            }

        viewModel.outputs.deadlineCountdownTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(binding.statsView.deadlineCountdownTextView::setText)

        viewModel.outputs.featuredTextViewRootCategory()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding.projectMediaHeaderLayout.projectMetadataLayout.featured.text =
                    (ksString.format(context().getString(R.string.discovery_baseball_card_metadata_featured_project), "category_name", it))
            }

        viewModel.outputs.featuredViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMediaHeaderLayout.projectMetadataLayout.featuredGroup))

        viewModel.outputs.goalStringForTextView()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setGoalTextView(it) }

        viewModel.outputs.locationTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                val locationTextView = if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    binding.projectMediaHeaderLayout.location
                } else {
                    binding.location
                }
                locationTextView?.text = it
            }

        viewModel.outputs.projectOutput()
            .subscribe {
                // todo: break down these helpers
                setLandscapeOverlayText(it)
                binding.statsView.deadlineCountdownUnitTextView.text = ProjectUtils.deadlineCountdownDetail(it, context(), ksString)
            }
        viewModel.outputs.percentageFundedProgress()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(binding.percentageFunded::setProgress)

        viewModel.outputs.percentageFundedProgressBarIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.percentageFunded))

        viewModel.outputs.playButtonIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMediaHeaderLayout.playButtonOverlay))

        binding.projectMediaHeaderLayout.playButtonOverlay.setOnClickListener {
            playButtonOnClick()
        }

        viewModel.outputs.pledgedTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(binding.statsView.pledged::setText)

        viewModel.outputs.projectDashboardButtonText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(binding.projectCreatorDashboardHeader.projectDashboardButton::setText)

        binding.projectCreatorDashboardHeader.projectDashboardButton.setOnClickListener {
            creatorDashboardOnClick()
        }

        viewModel.outputs.projectDashboardContainerIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectCreatorDashboardHeader.projectDashboardContainer))

        viewModel.outputs.projectDisclaimerGoalNotReachedString()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setProjectDisclaimerGoalNotReachedString(it) }

        viewModel.outputs.projectDisclaimerGoalReachedDateTime()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setProjectDisclaimerGoalReachedString(it) }

        viewModel.outputs.projectDisclaimerTextViewIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectCreatorInfoLayout.projectDisclaimerTextView))

        viewModel.outputs.projectLaunchDate()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setProjectLaunchDateString(it) }

        viewModel.outputs.projectLaunchDateIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectCreatorDashboardHeader.projectLaunchDate))

        viewModel.outputs.projectMetadataViewGroupBackgroundDrawableInt()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.projectMediaHeaderLayout.projectMetadataLayout.projectMetadataViewGroup.background = ContextCompat.getDrawable(context(), it) }

        viewModel.outputs.projectMetadataViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMediaHeaderLayout.projectMetadataLayout.projectMetadataViewGroup))

        viewModel.outputs.projectNameTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                val projectNameTextView = if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    binding.projectMediaHeaderLayout.projectName
                } else {
                    binding.projectName
                }
                projectNameTextView?.text = it
            }

        viewModel.outputs.projectPhoto()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setProjectPhoto(it) }

        viewModel.outputs.projectSocialTextViewFriends()
            .compose(bindToLifecycle())
            .compose<List<User?>>(observeForUI())
            .subscribe { binding.projectSocialText.text = SocialUtils.projectCardFriendNamepile(context(), it, ksString) }

        viewModel.outputs.projectSocialImageViewIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectSocialImage))

        viewModel.outputs.projectSocialImageViewUrl()
            .compose(bindToLifecycle())
            .compose(observeForUI())
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
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectSocialView))

        viewModel.outputs.projectStateViewGroupBackgroundColorInt()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.projectStateViewGroup.setBackgroundColor(ContextCompat.getColor(context(), it)) }

        viewModel.outputs.projectStateViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectStateViewGroup))

        viewModel.outputs.setCanceledProjectStateView()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setCanceledProjectStateView() }

        viewModel.outputs.setProjectSocialClickListener()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setProjectSocialClickListener() }

        viewModel.outputs.setSuccessfulProjectStateView()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setSuccessfulProjectStateView(it) }

        viewModel.outputs.setSuspendedProjectStateView()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setSuspendedProjectStateView() }

        viewModel.outputs.setUnsuccessfulProjectStateView()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setUnsuccessfulProjectStateView(it) }

        viewModel.outputs.shouldSetDefaultStatsMargins()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setStatsMargins(it) }

        viewModel.outputs.startProjectSocialActivity()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { startProjectSocialActivity(it) }

        viewModel.outputs.updatesCountTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(binding.projectCreatorInfoLayout.updatesCount::setText)

        viewModel.outputs.conversionPledgedAndGoalText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { pledgedAndGoal: Pair<String, String> -> setConvertedCurrencyView(pledgedAndGoal) }

        viewModel.outputs.conversionTextViewIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(binding.usdConversionTextView))

        if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.readMore
        } else {
            binding.readMore
        }?.let { readMoreButton ->
            readMoreButton.setOnClickListener {
                blurbVariantOnClick()
            }
        }

        if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.readMore
        } else {
            binding.readMore
        }?.let { readMoreButton ->
            readMoreButton.setOnClickListener {
                blurbVariantOnClick()
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
        if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.blurbView
        } else {
            binding.blurbView
        }?.let { blurbView ->
            blurbView.setOnClickListener {
                blurbOnClick()
            }
        }
        if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.creatorInfo
        } else {
            binding.creatorInfo
        }?.let { creatorInfo ->
            creatorInfo.setOnClickListener {
                creatorNameOnClick()
            }
        }

        if (context().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectMediaHeaderLayout.creatorInfoVariant
        } else {
            binding.creatorInfoVariant
        }?.let { creatorInfoVariant ->
            creatorInfoVariant.setOnClickListener {
                creatorInfoVariantOnClick()
            }
        }
    }

    private fun blurbOnClick() {
        delegate.projectViewHolderBlurbClicked(this)
    }

    private fun blurbVariantOnClick() {
        delegate.projectViewHolderBlurbVariantClicked(this)
    }

    private fun commentsOnClick() {
        delegate.projectViewHolderCommentsClicked(this)
    }

    private fun creatorNameOnClick() {
        delegate.projectViewHolderCreatorClicked(this)
    }

    private fun creatorInfoVariantOnClick() {
        delegate.projectViewHolderCreatorInfoVariantClicked(this)
    }

    private fun creatorDashboardOnClick() {
        delegate.projectViewHolderDashboardClicked(this)
    }

    private fun playButtonOnClick() {
        delegate.projectViewHolderVideoStarted(this)
    }

    private fun updatesOnClick() {
        delegate.projectViewHolderUpdatesClicked(this)
    }
}
