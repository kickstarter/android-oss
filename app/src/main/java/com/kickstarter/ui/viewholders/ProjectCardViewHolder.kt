package com.kickstarter.ui.viewholders

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.kickstarter.R
import com.kickstarter.databinding.ProjectCardViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.viewmodels.ProjectCardHolderViewModel
import com.squareup.picasso.Picasso
import org.joda.time.DateTime

class ProjectCardViewHolder(
    private val binding: ProjectCardViewBinding,
    delegate: Delegate
) : KSViewHolder(binding.root) {
    private val viewModel = ProjectCardHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()

    interface Delegate {
        fun projectCardViewHolderClicked(project: Project?)
    }
    init {
        viewModel.outputs.backersCountTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.projectCardStats.backersCount.text = it }

        viewModel.outputs.backingViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMetadataView.backingGroup))
        viewModel.outputs.deadlineCountdownText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.projectCardStats.deadlineCountdown.text = it }
        viewModel.outputs.featuredViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMetadataView.featuredGroup))
        viewModel.outputs.friendAvatar2IsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { ViewUtils.setGone(binding.friendRowBackingGroup.friendBackingAvatar2, it) }
        viewModel.outputs.friendAvatar3IsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { ViewUtils.setGone(binding.friendRowBackingGroup.friendBackingAvatar3, it) }
        viewModel.outputs.friendAvatarUrl1()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setFriendAvatarUrl(it, binding.friendRowBackingGroup.friendBackingAvatar2) }
        viewModel.outputs.friendAvatarUrl2()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setFriendAvatarUrl(it, binding.friendRowBackingGroup.friendBackingAvatar2) }
        viewModel.outputs.friendAvatarUrl3()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setFriendAvatarUrl(it, binding.friendRowBackingGroup.friendBackingAvatar3) }
        viewModel.outputs.friendBackingViewIsHidden()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.friendRowBackingGroup.friendBackingGroup))
        viewModel.outputs.friendsForNamepile()
            .compose(bindToLifecycle())
            .compose<List<User?>>(Transformers.observeForUI())
            .subscribe { binding.friendRowBackingGroup.friendBackingMessage.text = SocialUtils.projectCardFriendNamepile(context(), it, ksString) }
        viewModel.outputs.fundingUnsuccessfulViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectStateViewGroup.fundingUnsuccessfulViewGroup))
        viewModel.outputs.imageIsInvisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setInvisible(binding.projectCardPhoto.photo))
        viewModel.outputs.locationName()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.projectCardTags.locationTextView.text = it }
        viewModel.outputs.locationContainerIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectCardTags.locationContainer))
        viewModel.outputs.nameAndBlurbText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setStyledNameAndBlurb(it) }
        viewModel.outputs.notifyDelegateOfProjectClick()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { delegate.projectCardViewHolderClicked(it) }
        viewModel.outputs.percentageFundedTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.projectCardStats.percent.text = it }
        viewModel.outputs.percentageFundedForProgressBar()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.percentageFunded.progress = it }
        viewModel.outputs.photoUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { resizeProjectImage(it) }
        viewModel.outputs.projectCanceledAt()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setCanceledTextView(it) }
        viewModel.outputs.projectCardStatsViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectCardStats.projectCardStatsViewGroup))
        viewModel.outputs.projectFailedAt()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setFailedAtTextView(it) }
        viewModel.outputs.projectForDeadlineCountdownDetail()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setDeadlineCountdownText(it) }
        viewModel.outputs.projectStateViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectStateViewGroup.projectStateViewGroup))
        viewModel.outputs.projectSubcategoryName()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setSubcategoryTextView(it) }
        viewModel.outputs.projectSubcategoryIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectCardTags.subcategoryContainer))
        viewModel.outputs.projectSuccessfulAt()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setSuccessfullyFundedDateTextView(it) }
        viewModel.outputs.projectSuspendedAt()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setSuspendedAtTextView(it) }
        viewModel.outputs.projectTagContainerIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectCardTags.projectTags))
        viewModel.outputs.projectWeLoveIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectCardTags.projectWeLoveContainer))
        viewModel.outputs.rootCategoryNameForFeatured()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.projectMetadataView.featured.text = ksString.format(context().getString(R.string.discovery_baseball_card_metadata_featured_project), "category_name", it) }
        viewModel.outputs.metadataViewGroupBackgroundDrawable()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.projectMetadataView.projectMetadataViewGroup.background = ContextCompat.getDrawable(context(), it) }
        viewModel.outputs.metadataViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMetadataView.projectMetadataViewGroup))
        viewModel.outputs.savedViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectMetadataView.savedViewGroup))
        viewModel.outputs.fundingSuccessfulViewGroupIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.projectStateViewGroup.fundingSuccessfulViewGroup))
        viewModel.outputs.setDefaultTopPadding()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setDefaultTopPadding(it) }
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectAndParams = ObjectUtils.requireNonNull(data as Pair<Project, DiscoveryParams>?)
        viewModel.inputs.configureWith(projectAndParams)
    }

    private fun setSubcategoryTextView(subcategory: String) {
        binding.projectCardTags.subcategoryTextView.text = subcategory
    }

    private fun setStyledNameAndBlurb(nameAndBlurb: Pair<String, String>) {
        val nameString = if (ProjectUtils.isProjectNamePunctuated(nameAndBlurb.first)) nameAndBlurb.first.toString() + " " else nameAndBlurb.first.toString() + ": "
        val blurbString = nameAndBlurb.second
        val styledString = SpannableString(nameString + blurbString)
        styledString.setSpan(ForegroundColorSpan(context().getColor(R.color.kds_support_700)), 0, nameString.length, 0)
        styledString.setSpan(
            ForegroundColorSpan(context().getColor(R.color.kds_support_400)),
            nameString.length,
            nameString.length + blurbString.length,
            0
        )
        binding.nameAndBlurbTextView.text = styledString
    }

    private fun resizeProjectImage(avatarUrl: String?) {
        val targetImageWidth = (ViewUtils.getScreenWidthDp(context()) * ViewUtils.getScreenDensity(context()) - context().resources.getDimension(R.dimen.grid_4)).toInt()
        val targetImageHeight = ProjectUtils.photoHeightFromWidthRatio(targetImageWidth)

        binding.projectCardPhoto.photo.maxHeight = targetImageHeight
        avatarUrl?.let {
            ResourcesCompat.getDrawable(context().resources, R.drawable.gray_gradient, null)?.let { placeholder ->
                Picasso.get()
                    .load(it)
                    .resize(targetImageWidth, targetImageHeight) // required to fit properly into apis < 18
                    .centerCrop()
                    .placeholder(placeholder)
                    .into(binding.projectCardPhoto.photo)
            }
        }
    }

    private fun setDeadlineCountdownText(project: Project) {
        binding.projectCardStats.deadlineCountdownUnit.text = ProjectUtils.deadlineCountdownDetail(project, context(), ksString)
    }

    private fun setFriendAvatarUrl(avatarUrl: String, imageView: ImageView) {
        Picasso.get().load(avatarUrl)
            .transform(CircleTransformation())
            .into(imageView)
    }

    private fun setDefaultTopPadding(setDefaultPadding: Boolean) {
        binding.landCardViewGroup?.let {
            if (setDefaultPadding) {
                adjustLandscapeTopPadding(it, context().resources.getDimension(R.dimen.grid_2).toInt(), context().resources.getDimension(R.dimen.grid_2).toInt(), context().resources.getDimension(R.dimen.grid_2).toInt(), context().resources.getDimension(R.dimen.grid_2).toInt())
                adjustViewGroupTopMargin(binding.projectCardViewGroup, 0)
            } else {
                adjustLandscapeTopPadding(it, context().resources.getDimension(R.dimen.grid_2).toInt(), context().resources.getDimension(R.dimen.grid_3).toInt(), context().resources.getDimension(R.dimen.grid_2).toInt(), context().resources.getDimension(R.dimen.grid_2).toInt())
                adjustViewGroupTopMargin(binding.projectCardViewGroup, this.context().resources.getDimension(R.dimen.grid_1).toInt())
            }
        }
    }

    override fun onClick(view: View) {
        viewModel.inputs.projectCardClicked()
    }

    /**
     * Adjust spacing between cards when metadata label is present.
     */
    private fun adjustViewGroupTopMargin(viewGroup: ViewGroup, topMargin: Int) {
        val marginParams = MarginLayoutParams(
            viewGroup.layoutParams
        )
        marginParams.setMargins(0, topMargin, 0, 0)
        viewGroup.layoutParams = marginParams
    }

    /**
     * Adjust card content spacing when metadata label is present.
     */
    private fun adjustLandscapeTopPadding(
        landscapeViewGroup: ViewGroup?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        landscapeViewGroup?.setPadding(left, top, right, bottom)
    }

    private fun setCanceledTextView(projectCanceledAt: DateTime) {
        binding.projectStateViewGroup.fundingUnsuccessfulDateTextView.text = DateTimeUtils.relative(context(), ksString, projectCanceledAt)
        binding.projectStateViewGroup.fundingUnsuccessfulTextView.setText(R.string.discovery_baseball_card_status_banner_canceled)
    }

    private fun setSuccessfullyFundedDateTextView(projectSuccessfulAt: DateTime) {
        binding.projectStateViewGroup.fundingSuccessfulDateTextView.text = DateTimeUtils.relative(context(), ksString, projectSuccessfulAt)
    }

    private fun setFailedAtTextView(projectFailedAt: DateTime) {
        binding.projectStateViewGroup.fundingUnsuccessfulDateTextView.text = DateTimeUtils.relative(context(), ksString, projectFailedAt)
        binding.projectStateViewGroup.fundingUnsuccessfulTextView.setText(R.string.dashboard_creator_project_funding_unsuccessful)
    }

    private fun setSuspendedAtTextView(projectSuspendedAt: DateTime) {
        binding.projectStateViewGroup.fundingUnsuccessfulDateTextView.text = DateTimeUtils.relative(context(), ksString, projectSuspendedAt)
    }
}
