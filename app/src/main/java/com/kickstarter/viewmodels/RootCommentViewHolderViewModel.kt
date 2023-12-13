package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.extensions.assignAuthorBadge
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardBadge
import com.kickstarter.ui.views.CommentCardStatus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

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

    class ViewModel(environment: Environment) : Inputs, Outputs {
        private val initCellConfig = BehaviorSubject.create<CommentCardData>()
        private val onShowCanceledPledgeRootCommentClicked = PublishSubject.create<Unit>()
        private val authorBadge = BehaviorSubject.create<CommentCardBadge>()

        private val currentUser = requireNotNull(environment.currentUserV2())

        private val bindRootComment = BehaviorSubject.create<CommentCardData>()
        private val showCanceledPledgeRootComment = PublishSubject.create<CommentCardStatus>()

        val inputs = this
        val outputs = this

        private val disposables = CompositeDisposable()

        init {
            val commentCardData = this.initCellConfig
            commentCardData
                .subscribe {
                    this.bindRootComment.onNext(it)
                }
                .addToDisposable(disposables)

            commentCardData
                .map { requireNotNull(it.comment) }
                .compose(Transformers.takeWhenV2(this.onShowCanceledPledgeRootCommentClicked))
                .subscribe { this.showCanceledPledgeRootComment.onNext(CommentCardStatus.CANCELED_PLEDGE_COMMENT) }
                .addToDisposable(disposables)

            commentCardData
                .withLatestFrom(currentUser.observable()) { comment, user -> Pair(comment, user) }
                .filter { it.first.comment.isNotNull() }
                .map { requireNotNull(it.first.comment?.assignAuthorBadge(it.second.getValue())) }
                .subscribe { this.authorBadge.onNext(it) }
                .addToDisposable(disposables)
        }

        // - Inputs
        override fun configureWith(comment: CommentCardData) = this.initCellConfig.onNext(comment)
        override fun onShowCanceledPledgeRootCommentClicked() = this.onShowCanceledPledgeRootCommentClicked.onNext(Unit)
        // - Outputs
        override fun bindRootComment(): Observable<CommentCardData> = this.bindRootComment
        override fun showCanceledPledgeRootComment(): Observable<CommentCardStatus> = this.showCanceledPledgeRootComment
        override fun authorBadge(): Observable<CommentCardBadge> = this.authorBadge

        fun clear() {
            disposables.clear()
        }
    }
}
