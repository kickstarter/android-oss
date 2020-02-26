package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.ui.viewholders.UpdateCardViewHolder
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface UpdateCardViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [Project] and [Update]. */
        fun configureWith(project: Project, update:Update)

        /** Call when the user clicks on an update. */
        fun updateClicked()
    }

    interface Outputs {
        fun backersOnlyContainerIsVisible(): Observable<Boolean>

        fun blurb(): Observable<String>

        fun commentsCount(): Observable<Int>

        fun commentsCountIsGone(): Observable<Boolean>

        fun date(): Observable<DateTime>

        fun likesCount(): Observable<Int>

        fun likesCountIsGone(): Observable<Boolean>

        fun sequence(): Observable<Int>

        fun title(): Observable<String>

        fun viewUpdate(): Observable<Update>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<UpdateCardViewHolder>(environment), Inputs, Outputs {

        private val projectAndUpdate = PublishSubject.create<Pair<Project, Update>>()
        private val updateClicked = PublishSubject.create<Void>()

        private val backersOnlyContainerIsVisible = BehaviorSubject.create<Boolean>()
        private val blurb = BehaviorSubject.create<String>()
        private val commentsCount = BehaviorSubject.create<Int>()
        private val commentsCountIsGone = BehaviorSubject.create<Boolean>()
        private val date = BehaviorSubject.create<DateTime>()
        private val likesCount = BehaviorSubject.create<Int>()
        private val likesCountIsGone = BehaviorSubject.create<Boolean>()
        private val sequence = BehaviorSubject.create<Int>()
        private val title = BehaviorSubject.create<String>()
        private val viewUpdate = PublishSubject.create<Update>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val update = this.projectAndUpdate
                    .map { it.second }

            update
                    .map { BooleanUtils.negate(it.isPublic ?: false) }
                    .compose(bindToLifecycle())
                    .subscribe(this.backersOnlyContainerIsVisible)

            update
                    .map { it.truncatedBody() }
                    .compose(bindToLifecycle())
                    .subscribe(this.blurb)

            update
                    .map { it.commentsCount() }
                    .filter { it != null }
                    .compose(bindToLifecycle())
                    .subscribe(this.commentsCount)

            update
                    .map { it.commentsCount() }
                    .map { IntegerUtils.isNullOrZero(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.commentsCountIsGone)

            update
                    .map { it.publishedAt() }
                    .compose(bindToLifecycle())
                    .subscribe(this.date)

            update
                    .map { it.likesCount() }
                    .filter { it != null }
                    .compose(bindToLifecycle())
                    .subscribe(this.likesCount)

            update
                    .map { it.likesCount() }
                    .map { IntegerUtils.isNullOrZero(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.likesCountIsGone)

            update
                    .map { it.sequence() }
                    .compose(bindToLifecycle())
                    .subscribe(this.sequence)

            update
                    .map { it.title() }
                    .compose(bindToLifecycle())
                    .subscribe(this.title)

            update
                    .compose<Update>(takeWhen(this.updateClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.viewUpdate)

        }

        override fun configureWith(project: Project, update: Update) {
            this.projectAndUpdate.onNext(Pair.create(project, update))
        }

        override fun updateClicked() {
            this.updateClicked.onNext(null)
        }

        override fun backersOnlyContainerIsVisible(): Observable<Boolean> = this.backersOnlyContainerIsVisible

        override fun blurb(): Observable<String> = this.blurb

        override fun commentsCount(): Observable<Int> = this.commentsCount

        override fun commentsCountIsGone(): Observable<Boolean> = this.commentsCountIsGone

        override fun date(): Observable<DateTime> = this.date

        override fun likesCount(): Observable<Int> = this.likesCount

        override fun likesCountIsGone(): Observable<Boolean> = this.likesCountIsGone

        override fun sequence(): Observable<Int> = this.sequence

        override fun title(): Observable<String> = this.title

        override fun viewUpdate(): Observable<Update> = this.viewUpdate

    }
}
