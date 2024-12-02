package com.kickstarter.viewmodels

import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.featureflag.FlipperFlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.isZero
import com.kickstarter.models.User
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface LoggedInViewHolderViewModel {

    interface Inputs {
        /** Call with the current user when the data is bound to the view. */
        fun configureWith(user: User)

        /** Call when the viewholder is finished. */
        fun onCleared()
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
        fun pledgedProjectsIsVisible(): Observable<Boolean>
        fun pledgedProjectsIndicatorIsVisible(): Observable<Boolean>
    }

    class ViewModel(val environment: Environment) : Inputs, Outputs {

        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val featureFlagClient = requireNotNull(environment.featureFlagClient())

        private val user = PublishSubject.create<User>()
        private val activityCount = BehaviorSubject.create<Int>()
        private val activityCountTextColor = BehaviorSubject.create<Int>()
        private val avatarUrl = BehaviorSubject.create<String>()
        private val dashboardRowIsGone = BehaviorSubject.create<Boolean>()
        private val name = BehaviorSubject.create<String>()
        private val unreadMessagesCount = BehaviorSubject.create<Int>()
        private val userOutput = BehaviorSubject.create<User>()
        private val pledgedProjectsIsVisible = BehaviorSubject.create<Boolean>()
        private val pledgedProjectsIndicatorIsVisible = BehaviorSubject.create<Boolean>()

        private val disposables = CompositeDisposable()
        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.user
                .map { it.name() }
                .subscribe { this.name.onNext(it) }
                .addToDisposable(disposables)

            this.user
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)

            this.user
                .map { it.avatar().medium() }
                .subscribe { this.avatarUrl.onNext(it) }
                .addToDisposable(disposables)

            this.user
                .map { it.unreadMessagesCount() }
                .subscribe { this.unreadMessagesCount.onNext(it) }
                .addToDisposable(disposables)

            this.user
                .map {
                    it.unseenActivityCount().intValueOrZero() + it.erroredBackingsCount()
                        .intValueOrZero()
                }
                .subscribe { this.activityCount.onNext(it) }
                .addToDisposable(disposables)

            this.user
                .map { it.erroredBackingsCount().intValueOrZero().isZero() }
                .map { if (it.isTrue()) R.color.text_primary else R.color.kds_alert }
                .subscribe { this.activityCountTextColor.onNext(it) }
                .addToDisposable(disposables)

            this.user
                .map { it.memberProjectsCount().intValueOrZero().isZero() }
                .subscribe { this.dashboardRowIsGone.onNext(it) }
                .addToDisposable(disposables)

            this.user
                .map { it.ppoHasAction().isTrue() }
                .subscribe { this.pledgedProjectsIndicatorIsVisible.onNext(it) }
                .addToDisposable(disposables)

            featureFlagClient.isBackendEnabledFlag(this.apolloClient.userPrivacy(), FlipperFlagKey.FLIPPER_PLEDGED_PROJECTS_OVERVIEW)
                .compose(Transformers.neverErrorV2())
                .map { ffEnabledBackend ->
                    val ffEnabledMobile = featureFlagClient.getBoolean(FlagKey.ANDROID_PLEDGED_PROJECTS_OVERVIEW)

                    return@map ffEnabledMobile && ffEnabledBackend
                }
                .subscribe { this.pledgedProjectsIsVisible.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun configureWith(user: User) {
            this.user.onNext(user)
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
        }

        override fun activityCount(): Observable<Int> = this.activityCount

        override fun activityCountTextColor(): Observable<Int> = this.activityCountTextColor

        override fun avatarUrl(): Observable<String> = this.avatarUrl

        override fun dashboardRowIsGone(): Observable<Boolean> = this.dashboardRowIsGone

        override fun name(): Observable<String> = this.name

        override fun unreadMessagesCount(): Observable<Int> = this.unreadMessagesCount

        override fun user(): Observable<User> = this.userOutput

        override fun pledgedProjectsIsVisible(): Observable<Boolean> = this.pledgedProjectsIsVisible
        override fun pledgedProjectsIndicatorIsVisible(): Observable<Boolean> = this.pledgedProjectsIndicatorIsVisible
    }
}
