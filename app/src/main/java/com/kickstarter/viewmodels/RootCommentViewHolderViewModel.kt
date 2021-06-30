package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.models.Comment
import com.kickstarter.ui.viewholders.RootCommentViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject

interface RootCommentViewHolderViewModel {
    interface Inputs {
        fun configureWith(configureCellWith: Comment)
    }

    interface Outputs {
        fun bindRootComment(): Observable<Comment>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<RootCommentViewHolder>(environment), Inputs, Outputs {
        private val bindRootComment = BehaviorSubject.create<Comment>()
        private val initCellConfig = BehaviorSubject.create<Comment>()

        val inputs = this
        val outputs = this

        init {
            this.initCellConfig
                .compose(bindToLifecycle())
                .subscribe {
                    this.bindRootComment.onNext(it)
                }
        }

        // - Inputs
        override fun configureWith(comment: Comment) = this.initCellConfig.onNext(comment)

        // - Outputs
        override fun bindRootComment(): Observable<Comment> = this.bindRootComment
    }
}
