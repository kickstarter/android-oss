package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.ui.fragments.EmailVerificationInterstitialFragment

class EmailVerificationInterstitialFragmentViewModel {
    interface Inputs {
        /** Invoked when the retry button on the add-on Error alert dialog is pressed */
        fun retryButtonPressed()
    }
    interface Outputs { }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<EmailVerificationInterstitialFragment>(environment), Outputs, Inputs {
        val inputs = this
        val outputs = this

        override fun retryButtonPressed() {
            TODO("Not yet implemented")
        }
    }
}
