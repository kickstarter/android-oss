package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.viewmodels.PaymentMethodsViewModel

@RequiresActivityViewModel(PaymentMethodsViewModel.ViewModel::class)
class PaymentMethodsActivity : BaseActivity<PaymentMethodsViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method)
    }
}
