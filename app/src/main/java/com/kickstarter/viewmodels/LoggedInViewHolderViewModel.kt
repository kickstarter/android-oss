package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.isZero
import com.kickstarter.models.User
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface LoggedInViewHolderViewModel {

    interface Inputs {
        /** Call with the current user when the data is bound to the view. */
        fun configureWith(user: User)
    }

    interface Outputs {
        /** Emits the user's unseen activity and errored backings count. */
        fun activityCount(): Observable<Int>

        /** Emits the color resource ID of the activity count text color. */
        fun activityCountTextColor(): Observable<Int>

        /** Emits the user's medium avatar URL. */
        fun avatarUrl(): Observable<String>

        /** Emits a boolean determining the dashboard row's visibility depending on if the user is a creator/collaborator. */
        fun dashboardRowIsGone(): Observable<Boolean>

        /** Emits the user's name. */
        fun name(): Observable<String>

        /** Emits the user's unread messages count. */
        fun unreadMessagesCount(): Observable<Int>

        /** Emits the user to pass to delegate. */
        fun user(): Observable<User>
    }

    class ViewModel(val environment: Environment) : ActivityViewModel<LoggedInViewHolder>(environment), Inputs, Outputs {

        private val user = PublishSubject.create<User>()

        private val activityCount = BehaviorSubject.create<Int>()
        private val activityCountTextColor = BehaviorSubject.create<Int>()
        private val avatarUrl = BehaviorSubject.create<String>()
        private val dashboardRowIsGone = BehaviorSubject.create<Boolean>()
        private val name = BehaviorSubject.create<String>()
        private val unreadMessagesCount = BehaviorSubject.create<Int>()
        private val userOutput = BehaviorSubject.create<User>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.user
                .map { it.name() }
                .compose(bindToLifecycle())
                .subscribe(this.name)

            this.user
                .compose(bindToLifecycle())
                .subscribe(this.userOutput)

            this.user
                .map { it.avatar().medium() }
                .compose(bindToLifecycle())
                .subscribe(this.avatarUrl)

            this.user
                .map { it.unreadMessagesCount() }
                .compose(bindToLifecycle())
                .subscribe(this.unreadMessagesCount)

            this.user
                .map {
                    it.unseenActivityCount().intValueOrZero() + it.erroredBackingsCount()
                        .intValueOrZero()
                }
                .compose(bindToLifecycle())
                .subscribe(this.activityCount)

            this.user
                .map { it.erroredBackingsCount().intValueOrZero().isZero() }
                .map { if (it.isTrue()) R.color.text_primary else R.color.kds_alert }
                .compose(bindToLifecycle())
                .subscribe(this.activityCountTextColor)

            this.user
                .map { it.memberProjectsCount().intValueOrZero().isZero() }
                .compose(bindToLifecycle())
                .subscribe(this.dashboardRowIsGone)
        }

        override fun configureWith(@NonNull user: User) {
            this.user.onNext(user)
        }

        @NonNull
        override fun activityCount(): Observable<Int> = this.activityCount

        @NonNull
        override fun activityCountTextColor(): Observable<Int> = this.activityCountTextColor

        @NonNull
        override fun avatarUrl(): Observable<String> = this.avatarUrl

        @NonNull
        override fun dashboardRowIsGone(): Observable<Boolean> = this.dashboardRowIsGone

        @NonNull
        override fun name(): Observable<String> = this.name

        @NonNull
        override fun unreadMessagesCount(): Observable<Int> = this.unreadMessagesCount

        @NonNull
        override fun user(): Observable<User> = this.userOutput
    }
}
