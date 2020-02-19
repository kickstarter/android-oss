package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.ui.viewholders.UpdateViewHolder
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface UpdateViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [Project] and [Update]. */
        fun configureWith(project: Project, update:Update)

        /** Call when the user clicks on an update. */
        fun updateClicked()
    }

    interface Outputs {
        fun backersOnlyContainerIsVisible(): Observable<String>

        fun blurb(): Observable<String>

        fun commentsCount(): Observable<Int?>

        fun date(): Observable<DateTime>

        fun likesCount(): Observable<Int?>

        fun sequence(): Observable<Int>

        fun title(): Observable<String>

        fun viewUpdate(): Observable<Update>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<UpdateViewHolder>(environment), Inputs, Outputs {

        private val projectAndUpdate = PublishSubject.create<Pair<Project, Update>>()
        private val updateClicked = PublishSubject.create<Void>()

        private val backersOnlyContainerIsVisible = BehaviorSubject.create<Boolean>()
        private val blurb = BehaviorSubject.create<String>()
        private val commentsCount = BehaviorSubject.create<Int>()
        private val date = BehaviorSubject.create<DateTime>()
        private val likesCount = BehaviorSubject.create<Int>()
        private val sequence = BehaviorSubject.create<Int>()
        private val title = BehaviorSubject.create<String>()
        private val viewUpdate = PublishSubject.create<Update>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val update = this.projectAndUpdate
                    .map { it.second }

            val project = this.projectAndUpdate
                    .map { it.first }

            update
                    .map { it.body() }
                    .compose(bindToLifecycle())
                    .subscribe(this.blurb)

            update
                    .map { it.commentsCount() }
                    .compose(bindToLifecycle())
                    .subscribe(this.commentsCount)

            update
                    .map { it.publishedAt() }
                    .compose(bindToLifecycle())
                    .subscribe(this.date)

            update
                    .map { it.likesCount() }
                    .compose(bindToLifecycle())
                    .subscribe(this.likesCount)

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

        override fun blurb(): Observable<String> = this.blurb

        override fun commentsCount(): Observable<Int> = this.commentsCount

        override fun date(): Observable<DateTime> = this.date

        override fun likesCount(): Observable<Int> = this.likesCount

        override fun sequence(): Observable<Int> = this.sequence

        override fun title(): Observable<String> = this.title

        override fun viewUpdate(): Observable<Update> = this.viewUpdate

    }
}
