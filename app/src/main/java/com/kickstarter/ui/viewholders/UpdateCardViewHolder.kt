package com.kickstarter.ui.viewholders

import android.view.View
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.viewmodels.UpdateCardViewHolderViewModel
import kotlinx.android.synthetic.main.item_update_card.view.*

class UpdateCardViewHolder(private val view: View, val delegate: Delegate?) : KSViewHolder(view) {

    interface Delegate {
        fun updateClicked(update: Update)
    }

    private val ksString = environment().ksString()
    private var viewModel = UpdateCardViewHolderViewModel.ViewModel(environment())

    private val updateSequenceTemplate = context().getString(R.string.activity_project_update_update_count)

    init {

        this.viewModel.outputs.backersOnlyContainerIsVisible()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setUpdatePublicUI(it) }

        this.viewModel.outputs.blurb()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.update_blurb.text = it }

        this.viewModel.outputs.commentsCount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setCommentsCount(it) }

        this.viewModel.outputs.commentsCountIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.update_comments_container, it) }

        this.viewModel.outputs.likesCount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setLikesCount(it) }

        this.viewModel.outputs.likesCountIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(this.view.update_likes_container, it) }

        this.viewModel.outputs.publishDate()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.update_date.text = DateTimeUtils.longDate(it) }

        this.viewModel.outputs.sequence()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.update_sequence.text = this.ksString.format(updateSequenceTemplate, "update_count", it.toString()) }

        this.viewModel.outputs.title()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.update_title.text = it }

        this.viewModel.outputs.viewUpdate()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { delegate?.updateClicked(it) }

        RxView.clicks(this.view.update_container)
                .compose(bindToLifecycle())
                .subscribe { this.viewModel.inputs.updateClicked() }
    }

    private fun setCommentsCount(commentsCount: Int) {
        this.view.update_comments_count.text = commentsCount.toString()
        this.view.update_comments_count.contentDescription = this.ksString.format("comments_count_comments",
                commentsCount,
                "comments_count",
                NumberUtils.format(commentsCount))
    }

    private fun setLikesCount(likesCount: Int) {
        this.view.update_likes_count.text = likesCount.toString()
        this.view.update_comments_count.contentDescription = this.ksString.format("likes_count_likes",
                likesCount,
                "likes_count",
                NumberUtils.format(likesCount))
    }

    private fun setUpdatePublicUI(backersOnly: Boolean) {
        ViewUtils.setGone(this.view.update_backers_only, !backersOnly)
        ViewUtils.setGone(this.view.update_details, backersOnly)
        this.view.update_container.isClickable = !backersOnly
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val projectAndUpdate = requireNonNull(data as Pair<Project, Update>)
        val project = requireNonNull(projectAndUpdate.first, Project::class.java)
        val update = requireNonNull(projectAndUpdate.second, Update::class.java)

        this.viewModel.inputs.configureWith(project, update)
    }

}
