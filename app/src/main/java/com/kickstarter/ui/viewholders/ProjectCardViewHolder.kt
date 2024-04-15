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
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import com.kickstarter.R
import com.kickstarter.databinding.ProjectCardViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.SocialUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deadlineCountdownDetail
import com.kickstarter.libs.utils.extensions.isProjectNamePunctuated
import com.kickstarter.libs.utils.extensions.photoHeightFromWidthRatio
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.loadImageWithResize
import com.kickstarter.viewmodels.ProjectCardHolderViewModel
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime

class ProjectCardViewHolder(
    private val binding: ProjectCardViewBinding,
    delegate: Delegate
) : KSViewHolder(binding.root) {
    private val viewModel = ProjectCardHolderViewModel.ViewModel()
    private val ksString = requireNotNull(environment().ksString())
    private val disposables = CompositeDisposable()

    interface Delegate {
        fun projectCardViewHolderClicked(project: Project)
        fun onHeartButtonClicked(project: Project)
    }

    init {
        viewModel.outputs.backersCountTextViewText()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectCardStats.backersCount.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.backingViewGroupIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectMetadataView.backingGroup.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.deadlineCountdownText()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectCardStats.deadlineCountdown.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.featuredViewGroupIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectMetadataView.featuredGroup.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.friendAvatar2IsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.friendRowBackingGroup.friendBackingAvatar2.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.friendAvatar3IsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.friendRowBackingGroup.friendBackingAvatar3.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.friendAvatarUrl1()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                setFriendAvatarUrl(
                    it,
                    binding.friendRowBackingGroup.friendBackingAvatar1
                )
            }
            .addToDisposable(disposables)

        viewModel.outputs.friendAvatarUrl2()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                setFriendAvatarUrl(
                    it,
                    binding.friendRowBackingGroup.friendBackingAvatar2
                )
            }
            .addToDisposable(disposables)

        viewModel.outputs.friendAvatarUrl3()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                setFriendAvatarUrl(
                    it,
                    binding.friendRowBackingGroup.friendBackingAvatar3
                )
            }
            .addToDisposable(disposables)

        viewModel.outputs.friendBackingViewIsHidden()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.friendRowBackingGroup.friendBackingGroup.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.friendsForNamepile()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.friendRowBackingGroup.friendBackingMessage.text =
                    SocialUtils.projectCardFriendNamepile(context(), it, ksString)
            }
            .addToDisposable(disposables)

        viewModel.outputs.fundingUnsuccessfulViewGroupIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectStateViewGroup.fundingUnsuccessfulViewGroup.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.imageIsInvisible()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectCardPhoto.photo.isInvisible = it }
            .addToDisposable(disposables)

        viewModel.outputs.locationName()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectCardTags.locationTextView.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.locationContainerIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectCardTags.locationContainer.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.nameAndBlurbText()
            .compose(Transformers.observeForUIV2())
            .subscribe { setStyledNameAndBlurb(it) }
            .addToDisposable(disposables)

        viewModel.outputs.notifyDelegateOfProjectClick()
            .compose(Transformers.observeForUIV2())
            .subscribe { delegate.projectCardViewHolderClicked(it) }
            .addToDisposable(disposables)

        viewModel.outputs.percentageFundedTextViewText()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectCardStats.percent.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.percentageFundedForProgressBar()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.percentageFunded.progress = it }
            .addToDisposable(disposables)

        viewModel.outputs.photoUrl()
            .compose(Transformers.observeForUIV2())
            .subscribe { resizeProjectImage(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectCanceledAt()
            .compose(Transformers.observeForUIV2())
            .subscribe { setCanceledTextView(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectCardStatsViewGroupIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectCardStats.projectCardStatsViewGroup.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectFailedAt()
            .compose(Transformers.observeForUIV2())
            .subscribe { setFailedAtTextView(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectForDeadlineCountdownDetail()
            .compose(Transformers.observeForUIV2())
            .subscribe { setDeadlineCountdownText(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectStateViewGroupIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectStateViewGroup.projectStateViewGroup.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectSubcategoryName()
            .compose(Transformers.observeForUIV2())
            .subscribe { setSubcategoryTextView(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectSubcategoryIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectCardTags.subcategoryContainer.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectSuccessfulAt()
            .compose(Transformers.observeForUIV2())
            .subscribe { setSuccessfullyFundedDateTextView(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectSuspendedAt()
            .compose(Transformers.observeForUIV2())
            .subscribe { setSuspendedAtTextView(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectTagContainerIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectCardTags.projectTags.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectWeLoveIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectCardTags.projectWeLoveContainer.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.rootCategoryNameForFeatured()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.projectMetadataView.featured.text = ksString.format(
                    context().getString(R.string.discovery_baseball_card_metadata_featured_project),
                    "category_name",
                    it
                )
            }
            .addToDisposable(disposables)

        viewModel.outputs.metadataViewGroupBackgroundDrawable()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.projectMetadataView.projectMetadataViewGroup.background =
                    ContextCompat.getDrawable(context(), it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.metadataViewGroupIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectMetadataView.projectMetadataViewGroup.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.savedViewGroupIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectMetadataView.savedViewGroup.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.comingSoonViewGroupIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectMetadataView.comingSoonGroup.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.fundingSuccessfulViewGroupIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.projectStateViewGroup.fundingSuccessfulViewGroup.isGone = it }
            .addToDisposable(disposables)

        viewModel.outputs.setDefaultTopPadding()
            .compose(Transformers.observeForUIV2())
            .subscribe { setDefaultTopPadding(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.heartDrawableId()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.heartButton?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context(),
                        it
                    )
                )
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.notifyDelegateOfHeartButtonClicked()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                delegate.onHeartButtonClicked(it)
            }
            .addToDisposable(disposables)

        binding.heartButton?.setOnClickListener {
            this.viewModel.inputs.heartButtonClicked()
        }
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectAndParams = requireNotNull(data as? Pair<Project, DiscoveryParams>)
        viewModel.inputs.configureWith(projectAndParams)
    }

    private fun setSubcategoryTextView(subcategory: String) {
        binding.projectCardTags.subcategoryTextView.text = subcategory
    }

    private fun setStyledNameAndBlurb(nameAndBlurb: Pair<String, String>) {
        val nameString =
            if (isProjectNamePunctuated(nameAndBlurb.first)) nameAndBlurb.first.toString() + " " else nameAndBlurb.first.toString() + ": "
        val blurbString = nameAndBlurb.second
        val styledString = SpannableString(nameString + blurbString)
        styledString.setSpan(
            ForegroundColorSpan(context().getColor(R.color.kds_support_700)),
            0,
            nameString.length,
            0
        )
        styledString.setSpan(
            ForegroundColorSpan(context().getColor(R.color.kds_support_400)),
            nameString.length,
            nameString.length + blurbString.length,
            0
        )
        binding.nameAndBlurbTextView.text = styledString
    }

    private fun resizeProjectImage(avatarUrl: String?) {
        val targetImageWidth = getProjectImageWidth()
        val targetImageHeight = photoHeightFromWidthRatio(targetImageWidth)

        binding.projectCardPhoto.photo.maxHeight = targetImageHeight
        avatarUrl?.let {
            ResourcesCompat.getDrawable(context().resources, R.drawable.gray_gradient, null)
                ?.let { placeholder ->
                    binding.projectCardPhoto.photo.loadImageWithResize(it, targetImageWidth, targetImageHeight, placeholder)
                }
        }
    }

    private fun getProjectImageWidth(): Int {
        val screenWidthDp = ViewUtils.getScreenWidthDp(context())
        val screenDensityDp = ViewUtils.getScreenDensity(context())
        val reducedSizeDp = context().resources.getDimension(R.dimen.grid_4)

        return (screenWidthDp * screenDensityDp - reducedSizeDp).toInt()
    }

    private fun setDeadlineCountdownText(project: Project) {
        binding.projectCardStats.deadlineCountdownUnit.text =
            project.deadlineCountdownDetail(context(), ksString)
    }

    private fun setFriendAvatarUrl(avatarUrl: String, imageView: ImageView) {
        imageView.loadCircleImage(avatarUrl)
    }

    private fun setDefaultTopPadding(setDefaultPadding: Boolean) {
        binding.landCardViewGroup?.let {
            if (setDefaultPadding) {
                adjustLandscapeTopPadding(
                    it,
                    context().resources.getDimension(R.dimen.grid_2).toInt(),
                    context().resources.getDimension(R.dimen.grid_2).toInt(),
                    context().resources.getDimension(R.dimen.grid_2).toInt(),
                    context().resources.getDimension(R.dimen.grid_2).toInt()
                )
                adjustViewGroupTopMargin(binding.projectCardViewGroup, 0)
            } else {
                adjustLandscapeTopPadding(
                    it,
                    context().resources.getDimension(R.dimen.grid_2).toInt(),
                    context().resources.getDimension(R.dimen.grid_3).toInt(),
                    context().resources.getDimension(R.dimen.grid_2).toInt(),
                    context().resources.getDimension(R.dimen.grid_2).toInt()
                )
                adjustViewGroupTopMargin(
                    binding.projectCardViewGroup,
                    this.context().resources.getDimension(R.dimen.grid_1).toInt()
                )
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
        binding.projectStateViewGroup.fundingUnsuccessfulDateTextView.text =
            DateTimeUtils.relative(context(), ksString, projectCanceledAt)
        binding.projectStateViewGroup.fundingUnsuccessfulTextView.setText(R.string.discovery_baseball_card_status_banner_canceled)
    }

    private fun setSuccessfullyFundedDateTextView(projectSuccessfulAt: DateTime) {
        binding.projectStateViewGroup.fundingSuccessfulDateTextView.text =
            DateTimeUtils.relative(context(), ksString, projectSuccessfulAt)
    }

    private fun setFailedAtTextView(projectFailedAt: DateTime) {
        binding.projectStateViewGroup.fundingUnsuccessfulDateTextView.text =
            DateTimeUtils.relative(context(), ksString, projectFailedAt)
        binding.projectStateViewGroup.fundingUnsuccessfulTextView.setText(R.string.dashboard_creator_project_funding_unsuccessful)
    }

    private fun setSuspendedAtTextView(projectSuspendedAt: DateTime) {
        binding.projectStateViewGroup.fundingUnsuccessfulDateTextView.text =
            DateTimeUtils.relative(context(), ksString, projectSuspendedAt)
    }

    override fun destroy() {
        disposables.clear()
        viewModel.onCleared()
        super.destroy()
    }
}
