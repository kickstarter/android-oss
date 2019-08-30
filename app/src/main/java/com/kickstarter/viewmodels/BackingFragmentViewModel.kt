package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.fragments.BackingFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import type.CreditCardTypes
import type.PaymentTypes
import java.text.SimpleDateFormat
import java.util.*

interface BackingFragmentViewModel {
    interface Inputs {
        /** Call when the pledge has been successfully updated. */
        fun pledgeSuccessfullyUpdated()

        /** Configure with current project.  */
        fun project(project: Project)

        /** Call when the mark as received checkbox is checked. */
        fun receivedCheckboxToggled(checked: Boolean)
    }

    interface Outputs {
        /** Emits the backer's sequence. */
        fun backerNumber(): Observable<String>

        /** Emits the expiration of the backing's card. */
        fun cardExpiration(): Observable<String>

        /** Emits a boolean determining if the card section should be visible. */
        fun cardIsGone(): Observable<Boolean>

        /** Emits the last four digits of the backing's card. */
        fun cardLastFour(): Observable<String>

        /** Emits the card brand drawable to display. */
        fun cardLogo(): Observable<Int>

        /** Emits the amount pledged minus the shipping. */
        fun pledgeAmount(): Observable<CharSequence>

        /** Emits the date the backing was pledged on. */
        fun pledgeDate(): Observable<String>

        /** Emits the project and currently backed reward. */
        fun projectAndReward(): Observable<Pair<Project, Reward>>

        /** Emits a boolean that determines if received checkbox should be checked. */
        fun receivedCheckboxChecked(): Observable<Boolean>

        /** Emits a boolean determining if the delivered section should be visible. */
        fun receivedSectionIsGone(): Observable<Boolean>

        /** Emits the shipping amount of the backing. */
        fun shippingAmount(): Observable<CharSequence>

        /** Emits the shipping location of the backing. */
        fun shippingLocation(): Observable<String>

        /** Emits a boolean determining if the shipping summary should be visible. */
        fun shippingSummaryIsGone(): Observable<Boolean>

        /** Emits when the backing has successfully been updated. */
        fun showUpdatePledgeSuccess(): Observable<Void>

        /** Emits the total amount pledged. */
        fun totalAmount(): Observable<CharSequence>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<BackingFragment>(environment), Inputs, Outputs {

        private val pledgeSuccessfullyCancelled = PublishSubject.create<Void>()
        private val projectInput = PublishSubject.create<Project>()
        private val receivedCheckboxToggled = PublishSubject.create<Boolean>()

        private val backerNumber = BehaviorSubject.create<String>()
        private val cardExpiration = BehaviorSubject.create<String>()
        private val cardIsGone = BehaviorSubject.create<Boolean>()
        private val cardLastFour = BehaviorSubject.create<String>()
        private val cardLogo = BehaviorSubject.create<Int>()
        private val pledgeAmount = BehaviorSubject.create<CharSequence>()
        private val pledgeDate = BehaviorSubject.create<String>()
        private val projectAndReward = BehaviorSubject.create<Pair<Project, Reward>>()
        private val receivedCheckboxChecked = BehaviorSubject.create<Boolean>()
        private val receivedSectionIsGone = BehaviorSubject.create<Boolean>()
        private val shippingAmount = BehaviorSubject.create<CharSequence>()
        private val shippingLocation = BehaviorSubject.create<String>()
        private val shippingSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val showUpdatePledgeSuccess = PublishSubject.create<Void>()
        private val totalAmount = BehaviorSubject.create<CharSequence>()

        private val apiClient = this.environment.apiClient()
        private val ksCurrency = this.environment.ksCurrency()
        val ksString: KSString = this.environment.ksString()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.pledgeSuccessfullyCancelled
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePledgeSuccess)

            val backedProject = this.projectInput
                    .filter { it.isBacking }

            val backing = backedProject
                    .map { it.backing() }
                    .ofType(Backing::class.java)

            backedProject
                    .map { project -> project.rewards()?.firstOrNull { BackingUtils.isBacked(project, it) }?.let { Pair(project, it) } }
                    .compose(bindToLifecycle())
                    .subscribe(this.projectAndReward)

            backing
                    .map { NumberUtils.format(it.sequence().toFloat()) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.backerNumber)

            backing
                    .map { DateTimeUtils.longDate(it.pledgedAt()) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeDate)

            backing
                    .map { it.amount() - it.shippingAmount() }
                    .compose<Pair<Double, Project>>(combineLatestPair(backedProject))
                    .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeAmount)

            backing
                    .map { ObjectUtils.isNull(it.locationId()) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingSummaryIsGone)

            backing
                    .map { it.shippingAmount() }
                    .compose<Pair<Float, Project>>(combineLatestPair(backedProject))
                    .map { ProjectViewUtils.styleCurrency(it.first.toDouble(), it.second, this.ksCurrency) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingAmount)

            backing
                    .map { it.location() }
                    .map { it?.displayableName() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingLocation)

            backing
                    .map { it.amount() }
                    .compose<Pair<Double, Project>>(combineLatestPair(backedProject))
                    .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.totalAmount)

            val paymentSource = backing
                    .map { it.paymentSource() }
                    .filter { it != null }
                    .ofType(Backing.PaymentSource::class.java)

            paymentSource
                    .map { PaymentTypes.safeValueOf(it.paymentType()) != PaymentTypes.CREDIT_CARD }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.cardIsGone)

            val simpleDateFormat = SimpleDateFormat(StoredCard.DATE_FORMAT, Locale.getDefault())

            paymentSource
                    .map { source -> source.expirationDate()?.let { simpleDateFormat.format(it) } }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.cardExpiration)

            paymentSource
                    .map { it.lastFour()?: "" }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.cardLastFour)

            paymentSource
                    .map { it.type() }
                    .map {  StoredCard.getCardTypeDrawable(CreditCardTypes.safeValueOf(it)) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.cardLogo)

            backing
                    .map { it.backerCompletedAt() }
                    .map { ObjectUtils.isNotNull(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle<Boolean>())
                    .subscribe(this.receivedCheckboxChecked)

            backing
                    .compose<Pair<Backing, Project>>(combineLatestPair(backedProject))
                    // combine the project, backing, and checked boolean (<<Project,Backing>, Checked>) to make client call
                    .compose(takePairWhen<Pair<Backing, Project>, Boolean>(this.receivedCheckboxToggled))
                    .switchMap { this.apiClient.postBacking(it.first.second, it.first.first, it.second).compose(neverError()) }
                    .compose(bindToLifecycle())
                    .share()
                    .subscribe()

            val rewardIsReceivable = backing
                    .map { ObjectUtils.isNotNull(it.rewardId()) }

            val backingIsCollected = backing
                    .map { it.status() }
                    .map { it == Backing.STATUS_COLLECTED }

            rewardIsReceivable
                    .compose(combineLatestPair<Boolean, Boolean>(backingIsCollected))
                    .map { it.first && it.second }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.receivedSectionIsGone)
        }

        override fun pledgeSuccessfullyUpdated() {
            this.showUpdatePledgeSuccess.onNext(null)
        }

        override fun project(project: Project) {
            this.projectInput.onNext(project)
        }

        override fun receivedCheckboxToggled(checked: Boolean) {
            this.receivedCheckboxToggled.onNext(checked)
        }

        override fun backerNumber(): Observable<String> = this.backerNumber

        override fun cardExpiration(): Observable<String> = this.cardExpiration

        override fun cardIsGone(): Observable<Boolean> = this.cardIsGone

        override fun cardLastFour(): Observable<String> = this.cardLastFour

        override fun cardLogo(): Observable<Int> = this.cardLogo

        override fun pledgeAmount(): Observable<CharSequence> = this.pledgeAmount

        override fun pledgeDate(): Observable<String> = this.pledgeDate

        override fun projectAndReward(): Observable<Pair<Project, Reward>> = this.projectAndReward

        override fun receivedCheckboxChecked(): Observable<Boolean> = this.receivedCheckboxChecked

        override fun receivedSectionIsGone(): Observable<Boolean> = this.receivedSectionIsGone

        override fun shippingAmount(): Observable<CharSequence> = this.shippingAmount

        override fun shippingLocation(): Observable<String> = this.shippingLocation

        override fun shippingSummaryIsGone(): Observable<Boolean> = this.shippingSummaryIsGone

        override fun showUpdatePledgeSuccess(): Observable<Void> = this.showUpdatePledgeSuccess

        override fun totalAmount(): Observable<CharSequence> = this.totalAmount
    }
}
