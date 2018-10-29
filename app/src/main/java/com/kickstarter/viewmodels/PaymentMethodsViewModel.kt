package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.PaymentMethodsActivity

interface PaymentMethodsViewModel {
    interface Inputs {

    }

    interface Outputs {

    }

    class ViewModel(environment: Environment): ActivityViewModel<PaymentMethodsActivity>(environment) , Inputs, Outputs {

        private val client = environment.apolloClient()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

        }
    }
}