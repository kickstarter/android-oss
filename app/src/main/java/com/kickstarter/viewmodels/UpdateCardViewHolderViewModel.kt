package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.models.Update
import com.kickstarter.ui.viewholders.UpdateCardViewHolder
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface UpdateCardViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [Update]. */
        fun configureWith(update:Update)

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

        /** Emits the title of the [Update]. */
        fun title(): Observable<String>

        /** Emits when we should start the [com.kickstarter.ui.activities.UpdateActivity]. */
        fun viewUpdate(): Observable<Update>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<UpdateCardViewHolder>(environment), Inputs, Outputs {

        private val update = PublishSubject.create<Update>()
        private val updateClicked = PublishSubject.create<Void>()

        private val backersOnlyContainerIsVisible = BehaviorSubject.create<Boolean>()
        private val blurb = BehaviorSubject.create<String>()
        private val commentsCount = BehaviorSubject.create<Int>()
        private val commentsCountIsGone = BehaviorSubject.create<Boolean>()
        private val likesCount = BehaviorSubject.create<Int>()
        private val likesCountIsGone = BehaviorSubject.create<Boolean>()
        private val publishDate = BehaviorSubject.create<DateTime>()
        private val sequence = BehaviorSubject.create<Int>()
        private val title = BehaviorSubject.create<String>()
        private val viewUpdate = PublishSubject.create<Update>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.update
                    .map { BooleanUtils.negate(it.isPublic ?: false) }
                    .compose(bindToLifecycle())
                    .subscribe(this.backersOnlyContainerIsVisible)

            this.update
                    .map { it.truncatedBody() }
                    .compose(bindToLifecycle())
                    .subscribe(this.blurb)

            this.update
                    .map { it.commentsCount() }
                    .filter { it != null }
                    .compose(bindToLifecycle())
                    .subscribe(this.commentsCount)

            this.update
                    .map { it.commentsCount() }
                    .map { IntegerUtils.isNullOrZero(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.commentsCountIsGone)

            this.update
                    .map { it.likesCount() }
                    .filter { it != null }
                    .compose(bindToLifecycle())
                    .subscribe(this.likesCount)

            this.update
                    .map { it.likesCount() }
                    .map { IntegerUtils.isNullOrZero(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.likesCountIsGone)

            this.update
                    .map { it.publishedAt() }
                    .compose(bindToLifecycle())
                    .subscribe(this.publishDate)

            this.update
                    .map { it.sequence() }
                    .compose(bindToLifecycle())
                    .subscribe(this.sequence)

            this.update
                    .map { it.title() }
                    .compose(bindToLifecycle())
                    .subscribe(this.title)

            this.update
                    .compose<Update>(takeWhen(this.updateClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.viewUpdate)

        }

        override fun configureWith(update: Update) {
            this.update.onNext(update)
        }

        override fun updateClicked() {
            this.updateClicked.onNext(null)
        }

        override fun backersOnlyContainerIsVisible(): Observable<Boolean> = this.backersOnlyContainerIsVisible

        override fun blurb(): Observable<String> = this.blurb

        override fun commentsCount(): Observable<Int> = this.commentsCount

        override fun commentsCountIsGone(): Observable<Boolean> = this.commentsCountIsGone

        override fun likesCount(): Observable<Int> = this.likesCount

        override fun likesCountIsGone(): Observable<Boolean> = this.likesCountIsGone

        override fun publishDate(): Observable<DateTime> = this.publishDate

        override fun sequence(): Observable<Int> = this.sequence

        override fun title(): Observable<String> = this.title

        override fun viewUpdate(): Observable<Update> = this.viewUpdate

    }
}
