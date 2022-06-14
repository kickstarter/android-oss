package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.extensions.assignAuthorBadge
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.RootCommentViewHolder
import com.kickstarter.ui.views.CommentCardBadge
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
        fun authorBadge(): Observable<CommentCardBadge>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<RootCommentViewHolder>(environment), Inputs, Outputs {
        private val initCellConfig = BehaviorSubject.create<CommentCardData>()
        private val onShowCanceledPledgeRootCommentClicked = PublishSubject.create<Void>()
        private val authorBadge = BehaviorSubject.create<CommentCardBadge>()

        private val currentUser = requireNotNull(environment.currentUser())

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

            commentCardData
                .withLatestFrom(currentUser.observable()) { comment, user -> Pair(comment, user) }
                .map { it.first.comment?.assignAuthorBadge(it.second) }
                .compose(bindToLifecycle())
                .subscribe { this.authorBadge.onNext(it) }
        }

        // - Inputs
        override fun configureWith(comment: CommentCardData) = this.initCellConfig.onNext(comment)
        override fun onShowCanceledPledgeRootCommentClicked() = this.onShowCanceledPledgeRootCommentClicked.onNext(null)
        // - Outputs
        override fun bindRootComment(): Observable<CommentCardData> = this.bindRootComment
        override fun showCanceledPledgeRootComment(): Observable<CommentCardStatus> = this.showCanceledPledgeRootComment
        override fun authorBadge(): Observable<CommentCardBadge> = this.authorBadge
    }
}
