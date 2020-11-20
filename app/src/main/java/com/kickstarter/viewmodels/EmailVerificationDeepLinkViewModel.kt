package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.EmailVerificationDeepLinkActivity

class EmailVerificationDeepLinkViewModel {
    interface Inputs {
    }

    interface Outputs {
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<EmailVerificationDeepLinkActivity>(environment), Outputs, Inputs {
        val inputs = this
        val outputs = this

        init {
        }
    }

}