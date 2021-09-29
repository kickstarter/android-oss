package com.kickstarter.viewmodels
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.ui.fragments.CheckoutRiskMessageFragment
import rx.Observable
import rx.subjects.PublishSubject

interface CheckoutRiskMessageFragmentViewModel {
    interface Inputs {
        fun onLearnMoreAboutAccountabilityLinkClicked()
    }

    interface Outputs {
        fun openLearnMoreAboutAccountabilityLink(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) :
        FragmentViewModel<CheckoutRiskMessageFragment>(environment), Inputs, Outputs {
        private val onLearnMoreAboutAccountabilityLinkClicked = PublishSubject.create<Void>()

        private val openLearnMoreAboutAccountabilityLink = PublishSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this
        init {
            this.onLearnMoreAboutAccountabilityLinkClicked
                .compose(bindToLifecycle())
                .subscribe {
                    this.openLearnMoreAboutAccountabilityLink.onNext(
                        UrlUtils
                            .appendPath(environment.webEndpoint(), TRUST)
                    )
                }
        }

        override fun onLearnMoreAboutAccountabilityLinkClicked() = this
            .onLearnMoreAboutAccountabilityLinkClicked.onNext(null)

        override fun openLearnMoreAboutAccountabilityLink(): Observable<String> =
            this.openLearnMoreAboutAccountabilityLink
    }
    companion object {
        const val TRUST = "trust"
    }
}
