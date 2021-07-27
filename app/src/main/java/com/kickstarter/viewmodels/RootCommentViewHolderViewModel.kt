package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.RootCommentViewHolder
import com.kickstarter.ui.views.CommentCardStatus
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface RootCommentViewHolderViewModel {
    interface Inputs {
        fun configureWith(configureCellWith: CommentCardData)
        fun onShowCanceledPledgeRootCommentClicked()
    }

    interface Outputs {
        fun bindRootComment(): Observable<CommentCardData>
        fun showCanceledPledgeRootComment(): Observable<CommentCardStatus>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<RootCommentViewHolder>(environment), Inputs, Outputs {
        private val initCellConfig = BehaviorSubject.create<CommentCardData>()
        private val onShowCanceledPledgeRootCommentClicked = PublishSubject.create<Void>()

        private val bindRootComment = BehaviorSubject.create<CommentCardData>()
        private val showCanceledPledgeRootComment = PublishSubject.create<CommentCardStatus>()
        val inputs = this
        val outputs = this

        init {
            val commentCardData = this.initCellConfig
            commentCardData.compose(bindToLifecycle())
                .subscribe {
                    this.bindRootComment.onNext(it)
                }

            commentCardData
                .map { requireNotNull(it.comment) }
                .compose(Transformers.takeWhen(this.onShowCanceledPledgeRootCommentClicked))
                .compose(bindToLifecycle())
                .subscribe { this.showCanceledPledgeRootComment.onNext(CommentCardStatus.CANCELED_PLEDGE_COMMENT) }
        }

        // - Inputs
        override fun configureWith(comment: CommentCardData) = this.initCellConfig.onNext(comment)
        override fun onShowCanceledPledgeRootCommentClicked() = this.onShowCanceledPledgeRootCommentClicked.onNext(null)
        // - Outputs
        override fun bindRootComment(): Observable<CommentCardData> = this.bindRootComment
        override fun showCanceledPledgeRootComment(): Observable<CommentCardStatus> = this.showCanceledPledgeRootComment
    }
}
