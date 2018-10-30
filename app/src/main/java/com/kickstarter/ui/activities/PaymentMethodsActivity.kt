package com.kickstarter.ui.activities

import UserPaymentsQuery
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import com.kickstarter.viewmodels.PaymentMethodsViewModel
import kotlinx.android.synthetic.main.activity_payment_method.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(PaymentMethodsViewModel.ViewModel::class)
class PaymentMethodsActivity : BaseActivity<PaymentMethodsViewModel.ViewModel>() {

    private lateinit var adapter: PaymentMethodsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method)

        this.viewModel.outputs.getCards()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setCards(it) }

        this.adapter = PaymentMethodsAdapter(this.viewModel)
        recycler_view.adapter = this.adapter
        recycler_view.layoutManager = LinearLayoutManager(this)
    }

    private fun setCards(cards: MutableList<UserPaymentsQuery.Node>) = this.adapter.populateCards(cards)

}
