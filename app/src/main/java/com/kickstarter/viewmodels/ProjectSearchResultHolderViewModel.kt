package com.kickstarter.viewmodels

import android.util.Pair
import com.apollographql.apollo.api.CustomTypeValue
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.graphql.DateTimeAdapter
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.PairUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.models.Project
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface ProjectSearchResultHolderViewModel {
    interface Inputs {
        /** Call to configure the view model with a project and isFeatured data.  */
        fun configureWith(projectAndIsFeatured: Pair<Project, Boolean>)

        /** Call to say user clicked a project  */
        fun projectClicked()

        /** Clear subscriptions, called from ViewHolder when view is destroyed. */
        fun onCleared()
    }

    interface Outputs {
        /** Emits the formatted days to go text to be displayed.  */
        fun deadlineCountdownValueTextViewText(): Observable<String>

        /** Emits the project clicked by the user.  */
        fun notifyDelegateOfResultClick(): Observable<Project>

        /** Emits the percent funded text to be displayed.  */
        fun percentFundedTextViewText(): Observable<String>

        /** Emits the project be used to display the deadline countdown detail.  */
        fun projectForDeadlineCountdownUnitTextView(): Observable<Project>

        /** Emits the project title to be displayed.  */
        fun projectNameTextViewText(): Observable<String>

        /** Emits the project photo url to be displayed.  */
        fun projectPhotoUrl(): Observable<String>

        /** Emits a flag to show coming Soon label */
        fun displayPrelaunchProjectBadge(): Observable<Boolean>
    }

    class ProjectSearchResultHolderViewModel(environment: Environment) : Inputs, Outputs {

        private val projectAndIsFeatured = PublishSubject.create<Pair<Project, Boolean>>()
        private val projectClicked = PublishSubject.create<Unit>()
        private val deadlineCountdownValueTextViewText: Observable<String>
        private val notifyDelegateOfResultClick: Observable<Project>
        private val percentFundedTextViewText: Observable<String>
        private val projectForDeadlineCountdownDetail: Observable<Project>
        private val projectNameTextViewText: Observable<String>
        private val projectPhotoUrl: Observable<String>
        private val displayPrelaunchProjectBadge = BehaviorSubject.create<Boolean>()
        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this
        private val ffClient = requireNotNull(environment.featureFlagClient())

        init {
            deadlineCountdownValueTextViewText = projectAndIsFeatured
                .map {
                    NumberUtils.format(
                        it.first.deadlineCountdownValue(),
                    )
                }

            percentFundedTextViewText = projectAndIsFeatured
                .map {
                    NumberUtils.flooredPercentage(
                        it.first.percentageFunded(),
                    )
                }

            projectForDeadlineCountdownDetail = projectAndIsFeatured
                .map { it.first }

            projectPhotoUrl = projectAndIsFeatured
                .map {
                    Pair.create(
                        it.first.photo(),
                        it.second,
                    )
                }
                .filter { ObjectUtils.isNotNull(it.first) }
                .map {
                    if (it.second) {
                        it.first?.full()
                    } else {
                        it.first?.med()
                    }
                }

            projectNameTextViewText = projectAndIsFeatured
                .map { it.first.name() }

            notifyDelegateOfResultClick = projectAndIsFeatured
                .map { PairUtils.first(it) }
                .compose(Transformers.takeWhenV2(projectClicked))

            projectAndIsFeatured
                .filter { ffClient.getBoolean(FlagKey.ANDROID_PRE_LAUNCH_SCREEN) }
                .map {
                    it?.first?.displayPrelaunch() == true ||
                        it.first.launchedAt() ==
                        DateTimeAdapter().decode(CustomTypeValue.fromRawValue(0))
                }
                .subscribe {
                    displayPrelaunchProjectBadge.onNext(it)
                }.addToDisposable(disposables)
        }

        override fun configureWith(projectAndIsFeatured: Pair<Project, Boolean>) {
            this.projectAndIsFeatured.onNext(projectAndIsFeatured)
        }

        override fun projectClicked() {
            projectClicked.onNext(Unit)
        }

        override fun onCleared() {
            disposables.clear()
        }

        override fun deadlineCountdownValueTextViewText(): Observable<String> = deadlineCountdownValueTextViewText

        override fun notifyDelegateOfResultClick(): Observable<Project> = notifyDelegateOfResultClick

        override fun percentFundedTextViewText(): Observable<String> = percentFundedTextViewText

        override fun projectForDeadlineCountdownUnitTextView(): Observable<Project> = projectForDeadlineCountdownDetail

        override fun projectPhotoUrl(): Observable<String> = projectPhotoUrl

        override fun projectNameTextViewText(): Observable<String> = projectNameTextViewText

        override fun displayPrelaunchProjectBadge(): Observable<Boolean> = displayPrelaunchProjectBadge
    }
}
