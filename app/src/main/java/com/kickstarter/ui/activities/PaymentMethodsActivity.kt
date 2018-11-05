package com.kickstarter.ui.activities

import UserPaymentsQuery
import android.os.Bundle
import android.support.v7.app.AlertDialog
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
    private var showDeleteCardDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method)

        setupRecyclerview()

        this.viewModel.outputs.getCards()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setCards(it) }

        this.viewModel.outputs.showDeleteCardDialog()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    lazyDeleteCardConfirmationDialog().show()
                }

    }

    private fun setCards(cards: MutableList<UserPaymentsQuery.Node>) = this.adapter.populateCards(cards)

    private fun setupRecyclerview() {
        this.adapter = PaymentMethodsAdapter(this.viewModel)
        recycler_view.adapter = this.adapter
        recycler_view.layoutManager = LinearLayoutManager(this)
    }

    private fun lazyDeleteCardConfirmationDialog(): AlertDialog {
        if (this.showDeleteCardDialog == null) {
            this.showDeleteCardDialog = AlertDialog.Builder(this, R.style.AlertDialog)
                    .setCancelable(false)
                    .setTitle("Remove this card")
                    .setMessage("Are you sure you wish to remove this card from your payment method options?")
                    .setNegativeButton("No Nevermind") { _, _ ->
                        lazyDeleteCardConfirmationDialog().dismiss()
                    }
                    .setPositiveButton("Yes, Remove") { _, _ ->
                        this.viewModel.inputs.confirmDeleteCardClicked()
                    }
                    .create()
        }
        return this.showDeleteCardDialog!!
    }

}
