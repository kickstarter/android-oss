package com.kickstarter.viewmodels

import android.text.SpannableString
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.neverError
import com.kickstarter.libs.utils.BackingUtils
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.UserUtils
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.User
import com.kickstarter.ui.fragments.BackingFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface BackingFragmentViewModel {
    interface Inputs {
        /** Configure with current project.  */
        fun project(project: Project)
    }

    interface Outputs {
        /** Emits the backer's sequence. */
        fun backerNumber(): Observable<String>

        /** Emits the expiration of the backing's card. */
        fun cardExpiration(): Observable<String>

        /** Emits a boolean determining if the card section should be visible. */
        fun cardIsVisible(): Observable<Boolean>

        /** Emits the last four digits of the backing's card. */
        fun cardLastFour(): Observable<String>

        /** Emits the card brand drawable to display. */
        fun cardLogo(): Observable<Int>

        /** Emits the amount pledged minus the shipping. */
        fun pledgeAmount(): Observable<String>

        /** Emits the date of the pledge. */
        fun pledgeDate(): Observable<String>

        /** Emits the project and currently backed reward. */
        fun projectAndReward(): Observable<Pair<Project, Reward>>

        /** Emits the currency symbol string of the project. */
        fun projectCurrencySymbol(): Observable<Pair<SpannableString, Boolean>>

        /** Emits the shipping location of the backing. */
        fun shippingLocation(): Observable<String>

        /** Emits the total amount pledged. */
        fun totalAmount(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<BackingFragment>(environment), Inputs, Outputs {

        private val projectInput = PublishSubject.create<Project>()

        private val backerNumber = BehaviorSubject.create<String>()
        private val cardExpiration = BehaviorSubject.create<String>()
        private val cardIsVisible = BehaviorSubject.create<Boolean>()
        private val cardLastFour = BehaviorSubject.create<String>()
        private val cardLogo = BehaviorSubject.create<Int>()
        private val pledgeAmount = BehaviorSubject.create<String>()
        private val pledgeDate = BehaviorSubject.create<String>()
        private val projectAndReward = BehaviorSubject.create<Pair<Project, Reward>>()
        private val projectCurrencySymbol = BehaviorSubject.create<Pair<SpannableString, Boolean>>()
        private val shippingLocation = BehaviorSubject.create<String>()
        private val totalAmount = BehaviorSubject.create<String>()

        private val apiClient = this.environment.apiClient()
        private val currentUser = this.environment.currentUser()
        val ksString: KSString = this.environment.ksString()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val backing = this.projectInput
                    .filter { it.isBacking }
                    .compose<Pair<Project, User>>(combineLatestPair(this.currentUser.loggedInUser().distinctUntilChanged { old, new -> !UserUtils.userHasChanged(old, new) }))
                    .switchMap { this.apiClient.fetchProjectBacking(it.first, it.second).compose(neverError()) }
                    .share()

            this.projectInput
                    .filter { it.isBacking }
                    .map { project -> project.rewards()?.firstOrNull { BackingUtils.isBacked(project, it) }?.let { Pair(project, it) } }
                    .compose(bindToLifecycle())
                    .subscribe(this.projectAndReward)

            backing
                    .map { NumberUtils.format(it.sequence().toFloat()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.backerNumber)

            val paymentSource = backing
                    .map { it.paymentSource() }
                    .filter { it != null }
                    .ofType(Backing.PaymentSource::class.java)

            paymentSource
                    .map { source -> source.expirationDate()?.let { DateTimeUtils.fullDate(it) } }
                    .compose(bindToLifecycle())
                    .subscribe(this.cardExpiration)
        }

        override fun project(project: Project) {
            this.projectInput.onNext(project)
        }

        override fun backerNumber(): Observable<String> = this.backerNumber

        override fun cardExpiration(): Observable<String> = this.cardExpiration

        override fun cardIsVisible(): Observable<Boolean> = this.cardIsVisible

        override fun cardLastFour(): Observable<String> = this.cardLastFour

        override fun cardLogo(): Observable<Int> = this.cardLogo

        override fun pledgeAmount(): Observable<String> = this.pledgeAmount

        override fun pledgeDate(): Observable<String> = this.pledgeDate

        override fun projectCurrencySymbol(): Observable<Pair<SpannableString, Boolean>> = this.projectCurrencySymbol

        override fun projectAndReward(): Observable<Pair<Project, Reward>> = this.projectAndReward

        override fun shippingLocation(): Observable<String> = this.shippingLocation

        override fun totalAmount(): Observable<String> = this.totalAmount
    }
}
