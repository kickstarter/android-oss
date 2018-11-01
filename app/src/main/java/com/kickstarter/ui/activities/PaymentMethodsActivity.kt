package com.kickstarter.ui.activities

import UserPaymentsQuery
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.extensions.showConfirmationSnackbar
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import com.kickstarter.viewmodels.PaymentMethodsViewModel
import kotlinx.android.synthetic.main.activity_settings_payment_methods.*
import kotlinx.android.synthetic.main.payment_methods_toolbar.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(PaymentMethodsViewModel.ViewModel::class)
class PaymentMethodsActivity : BaseActivity<PaymentMethodsViewModel.ViewModel>() {

    private lateinit var adapter: PaymentMethodsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_payment_methods)

        setUpRecyclerView()

        this.viewModel.outputs.getCards()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setCards(it) }

        add_new_card.setOnClickListener { startActivityForResult(Intent(this, NewCardActivity::class.java), ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD) }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD && resultCode == Activity.RESULT_OK) {
            showConfirmationSnackbar(payment_methods_toolbar, R.string.Got_it_your_changes_have_been_saved)
        }
    }

    private fun setCards(cards: MutableList<UserPaymentsQuery.Node>) = this.adapter.populateCards(cards)

    private fun setUpRecyclerView() {
        this.adapter = PaymentMethodsAdapter(this.viewModel)
        recycler_view.adapter = this.adapter
        recycler_view.layoutManager = LinearLayoutManager(this)
    }

}
