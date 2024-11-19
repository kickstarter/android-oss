package com.kickstarter.ui.viewholders

import android.util.Pair
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ItemUpdateCardBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUIV2
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.viewmodels.UpdateCardViewHolderViewModel
import io.reactivex.disposables.CompositeDisposable

class UpdateCardViewHolder(private val binding: ItemUpdateCardBinding, val delegate: Delegate?) : KSViewHolder(binding.root) {

    interface Delegate {
        fun updateCardClicked(update: Update)
    }

    private val ksString = requireNotNull(environment().ksString())
    private var viewModel = UpdateCardViewHolderViewModel.ViewModel(environment())

    private val updateSequenceTemplate = context().getString(R.string.activity_project_update_update_count)
    private val disposables = CompositeDisposable()

    init {

        this.viewModel.outputs.backersOnlyContainerIsVisible()
            .compose(observeForUIV2())
            .subscribe { setBackersOnlyVisibility(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.blurb()
            .compose(observeForUIV2())
            .subscribe { this.binding.updateBlurb.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.commentsCount()
            .compose(observeForUIV2())
            .subscribe { setCommentsCount(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.commentsCountIsGone()
            .compose(observeForUIV2())
            .subscribe { this.binding.updateCommentsContainer.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.likesCount()
            .compose(observeForUIV2())
            .subscribe { setLikesCount(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.likesCountIsGone()
            .compose(observeForUIV2())
            .subscribe { this.binding.updateLikesContainer.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.publishDate()
            .compose(observeForUIV2())
            .subscribe { this.binding.updateDate.text = DateTimeUtils.longDate(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.sequence()
            .compose(observeForUIV2())
            .subscribe { this.binding.updateSequence.text = this.ksString.format(updateSequenceTemplate, "update_count", it.toString()) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showUpdateDetails()
            .compose(observeForUIV2())
            .subscribe { delegate?.updateCardClicked(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.title()
            .compose(observeForUIV2())
            .subscribe { this.binding.updateTitle.text = it }
            .addToDisposable(disposables)

        this.binding.updateContainer.setOnClickListener {
            this.viewModel.inputs.updateClicked()
        }
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
        val projectAndUpdate = requireNotNull(data as Pair<Project, Update>)
        val project = requireNotNull(projectAndUpdate.first) { Project::class.java.toString() + " required to be non-null." }
        val update = requireNotNull(projectAndUpdate.second) { Update::class.java.toString() + " required to be non-null." }

        this.viewModel.inputs.configureWith(project, update)
    }

    override fun destroy() {
        disposables.clear()
        viewModel.clear()
        super.destroy()
    }
}
