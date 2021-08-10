package com.kickstarter.ui.viewholders

import android.util.Pair
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.ItemUpdateCardBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.viewmodels.UpdateCardViewHolderViewModel

class UpdateCardViewHolder(private val binding: ItemUpdateCardBinding, val delegate: Delegate?) : KSViewHolder(binding.root) {

    interface Delegate {
        fun updateCardClicked(update: Update)
    }

    private val ksString = environment().ksString()
    private var viewModel = UpdateCardViewHolderViewModel.ViewModel(environment())

    private val updateSequenceTemplate = context().getString(R.string.activity_project_update_update_count)

    init {

        this.viewModel.outputs.backersOnlyContainerIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setBackersOnlyVisibility(it) }

        this.viewModel.outputs.blurb()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.updateBlurb.text = it }

        this.viewModel.outputs.commentsCount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setCommentsCount(it) }

        this.viewModel.outputs.commentsCountIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(this.binding.updateCommentsContainer, it) }

        this.viewModel.outputs.likesCount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setLikesCount(it) }

        this.viewModel.outputs.likesCountIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(this.binding.updateLikesContainer, it) }

        this.viewModel.outputs.publishDate()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.updateDate.text = DateTimeUtils.longDate(it) }

        this.viewModel.outputs.sequence()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.updateSequence.text = this.ksString.format(updateSequenceTemplate, "update_count", it.toString()) }

        this.viewModel.outputs.showUpdateDetails()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { delegate?.updateCardClicked(it) }

        this.viewModel.outputs.title()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.binding.updateTitle.text = it }

        RxView.clicks(this.binding.updateContainer)
            .compose(bindToLifecycle())
            .subscribe { this.viewModel.inputs.updateClicked() }
    }

    private fun setBackersOnlyVisibility(show: Boolean) {
        ViewUtils.setGone(this.binding.updateBackersOnly, !show)
        ViewUtils.setGone(this.binding.updateDetails, show)
        this.binding.updateContainer.isClickable = !show
    }

    private fun setCommentsCount(commentsCount: Int) {
        this.binding.updateCommentsCount.text = commentsCount.toString()
        this.binding.updateCommentsCount.contentDescription = this.ksString.format(
            "comments_count_comments",
            commentsCount,
            "comments_count",
            NumberUtils.format(commentsCount)
        )
    }

    private fun setLikesCount(likesCount: Int) {
        this.binding.updateLikesCount.text = likesCount.toString()
        this.binding.updateLikesCount.contentDescription = this.ksString.format(
            "likes_count_likes",
            likesCount,
            "likes_count",
            NumberUtils.format(likesCount)
        )
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val projectAndUpdate = requireNonNull(data as Pair<Project, Update>)
        val project = requireNonNull(projectAndUpdate.first, Project::class.java)
        val update = requireNonNull(projectAndUpdate.second, Update::class.java)

        this.viewModel.inputs.configureWith(project, update)
    }
}
