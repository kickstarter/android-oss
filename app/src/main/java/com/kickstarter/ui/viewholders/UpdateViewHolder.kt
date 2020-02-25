package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.viewmodels.UpdateViewHolderViewModel
import kotlinx.android.synthetic.main.update_card_view.view.*

class UpdateViewHolder(private val view: View, val delegate: Delegate?) : KSViewHolder(view) {

    interface Delegate {
        fun updateClicked(update: Update)
    }

    private val ksString = environment().ksString()
    private var viewModel = UpdateViewHolderViewModel.ViewModel(environment())

    private val updateSequenceTemplate = context().getString(R.string.activity_project_update_update_count)

    init {

        this.viewModel.outputs.backersOnlyContainerIsVisible()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {  }

        this.viewModel.outputs.blurb()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.update_blurb.text = it }

        this.viewModel.outputs.commentsCount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setCommentsCount(it) }

        this.viewModel.outputs.date()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.update_date.text = DateTimeUtils.longDate(it) }

        this.viewModel.outputs.likesCount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setLikesCount(it) }

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

    private fun setCommentsCount(commentsCount: Int?) {
        commentsCount?.let {
            this.view.update_comments_count.text = it.toString()
            this.view.update_comments_count.contentDescription = this.ksString.format("comments_count_comments", it,
                    "comments_count", NumberUtils.format(it))
        }?: run {
            this.view.update_comments_count.text = null
            this.view.update_comments_count.contentDescription = null
        }
    }

    private fun setLikesCount(commentsCount: Int?) {
        commentsCount?.let {
            this.view.update_likes_count.text = it.toString()
            //todo add content description
            this.view.update_likes_count.contentDescription = it.toString()
        }?: run {
            this.view.update_likes_count.text = null
            this.view.update_likes_count.contentDescription = null
        }
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val projectAndUpdate = requireNonNull(data as Pair<Project, Update>)
        val project = requireNonNull(projectAndUpdate.first, Project::class.java)
        val update = requireNonNull(projectAndUpdate.second, Update::class.java)

        this.viewModel.inputs.configureWith(project, update)
    }

}
