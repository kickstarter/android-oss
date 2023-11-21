package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNullOrZero
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.ui.viewholders.UpdateCardViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

interface UpdateCardViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [Project] and [Update]. */
        fun configureWith(project: Project, update: Update)

        /** Call when the user clicks on an [Update]. */
        fun updateClicked()
    }

    interface Outputs {
        /** Emits a boolean determining if the backers only container should be visible. */
        fun backersOnlyContainerIsVisible(): Observable<Boolean>

        /** Emits a truncated version of the [Update]'s body. */
        fun blurb(): Observable<String>

        /** Emits the number of comments of the [Update]. */
        fun commentsCount(): Observable<Int>

        /** Emits a boolean determining if the comments count container should be visible. */
        fun commentsCountIsGone(): Observable<Boolean>

        /** Emits the number of likes of the [Update]. */
        fun likesCount(): Observable<Int>

        /** Emits a boolean determining if the likes count container should be visible. */
        fun likesCountIsGone(): Observable<Boolean>

        /** Emits the publish timestamp of the [Update]. */
        fun publishDate(): Observable<DateTime>

        /** Emits the sequence of the [Update]. */
        fun sequence(): Observable<Int>

        /** Emits when the [UpdateCardViewHolder.Delegate] should start the [com.kickstarter.ui.activities.UpdateActivity]. */
        fun showUpdateDetails(): Observable<Update>

        /** Emits the title of the [Update]. */
        fun title(): Observable<String>
    }

    class ViewModel(environment: Environment) : Inputs, Outputs {

        private val projectAndUpdate = PublishSubject.create<Pair<Project, Update>>()
        private val updateClicked = PublishSubject.create<Unit>()

        private val backersOnlyContainerIsVisible = BehaviorSubject.create<Boolean>()
        private val blurb = BehaviorSubject.create<String>()
        private val commentsCount = BehaviorSubject.create<Int>()
        private val commentsCountIsGone = BehaviorSubject.create<Boolean>()
        private val likesCount = BehaviorSubject.create<Int>()
        private val likesCountIsGone = BehaviorSubject.create<Boolean>()
        private val publishDate = BehaviorSubject.create<DateTime>()
        private val sequence = BehaviorSubject.create<Int>()
        private val showUpdateDetails = PublishSubject.create<Update>()
        private val title = BehaviorSubject.create<String>()
        private val currentUser = requireNotNull(environment.currentUserV2())

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val disposables = CompositeDisposable()

        init {

            val update = this.projectAndUpdate
                .map { it.second }

            val project = this.projectAndUpdate
                .map { it.first }

            val isCreator = Observable.combineLatest(this.currentUser.observable(), project) { user, project ->
                Pair(user, project)
            }
                .map { it.first.getValue()?.let { user -> it.second.userIsCreator(user) } ?: false }

            this.projectAndUpdate
                .compose<Pair<Pair<Project, Update>, Boolean>>(Transformers.combineLatestPair(isCreator))
                .map {
                    when {
                        it.first.first.isBacking() || it.second -> false
                        else -> (it.first.second.isPublic() ?: false).negate()
                    }
                }
                .subscribe { this.backersOnlyContainerIsVisible.onNext(it) }
                .addToDisposable(disposables)

            update
                .map { it.truncatedBody() }
                .subscribe { this.blurb.onNext(it) }
                .addToDisposable(disposables)

            update
                .filter { it.commentsCount().isNotNull() }
                .map { requireNotNull(it.commentsCount()) }
                .subscribe { this.commentsCount.onNext(it) }
                .addToDisposable(disposables)

            update
                .map { it.commentsCount().isNullOrZero() }
                .subscribe { this.commentsCountIsGone.onNext(it) }
                .addToDisposable(disposables)

            update
                .filter { it.likesCount().isNotNull() }
                .map { requireNotNull(it.likesCount()) }
                .subscribe { this.likesCount.onNext(it) }
                .addToDisposable(disposables)

            update
                .map { it.likesCount().isNullOrZero() }
                .subscribe { this.likesCountIsGone.onNext(it) }
                .addToDisposable(disposables)

            update
                .filter { it.publishedAt().isNotNull() }
                .map { requireNotNull(it.publishedAt()) }
                .subscribe { this.publishDate.onNext(it) }
                .addToDisposable(disposables)

            update
                .map { it.sequence() }
                .subscribe { this.sequence.onNext(it) }
                .addToDisposable(disposables)

            update
                .map { it.title() }
                .subscribe { this.title.onNext(it) }
                .addToDisposable(disposables)

            update
                .compose(takeWhenV2(this.updateClicked))
                .subscribe { this.showUpdateDetails.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun configureWith(project: Project, update: Update) {
            this.projectAndUpdate.onNext(Pair.create(project, update))
        }

        override fun updateClicked() {
            this.updateClicked.onNext(Unit)
        }

        override fun backersOnlyContainerIsVisible(): Observable<Boolean> = this.backersOnlyContainerIsVisible

        override fun blurb(): Observable<String> = this.blurb

        override fun commentsCount(): Observable<Int> = this.commentsCount

        override fun commentsCountIsGone(): Observable<Boolean> = this.commentsCountIsGone

        override fun likesCount(): Observable<Int> = this.likesCount

        override fun likesCountIsGone(): Observable<Boolean> = this.likesCountIsGone

        override fun publishDate(): Observable<DateTime> = this.publishDate

        override fun sequence(): Observable<Int> = this.sequence

        override fun showUpdateDetails(): Observable<Update> = this.showUpdateDetails

        override fun title(): Observable<String> = this.title

        fun clear() {
            disposables.clear()
        }
    }
}
