package com.kickstarter.ui.activities

import UserPaymentsQuery
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.extensions.showConfirmationSnackbar
import com.kickstarter.extensions.showErrorSnackbar
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import com.kickstarter.viewmodels.PaymentMethodsViewModel
import kotlinx.android.synthetic.main.activity_settings_payment_methods.*
import kotlinx.android.synthetic.main.payment_methods_toolbar.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(PaymentMethodsViewModel.ViewModel::class)
class PaymentMethodsSettingsActivity : BaseActivity<PaymentMethodsViewModel.ViewModel>() {

    private lateinit var adapter: PaymentMethodsAdapter
    private var showDeleteCardDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_payment_methods)

        setUpRecyclerView()

        this.viewModel.outputs.cards()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setCards(it) }

        this.viewModel.outputs.dividerIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(payments_divider, !it) }

        this.viewModel.outputs.error()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showErrorSnackbar(payment_methods_toolbar, it) }

        this.viewModel.outputs.progressBarIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(progress_bar, !it) }

        this.viewModel.outputs.showDeleteCardDialog()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { lazyDeleteCardConfirmationDialog().show() }

        this.viewModel.success()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showConfirmationSnackbar(payment_methods_toolbar, R.string.Got_it_your_changes_have_been_saved) }

        add_new_card.setOnClickListener { startActivityForResult(Intent(this, NewCardActivity::class.java),
                ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD) }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD && resultCode == Activity.RESULT_OK) {
            showConfirmationSnackbar(payment_methods_toolbar, R.string.Got_it_your_changes_have_been_saved)
            this@PaymentMethodsSettingsActivity.viewModel.inputs.refreshCards()
        }
    }

    private fun setCards(cards: MutableList<UserPaymentsQuery.Node>) = this.adapter.populateCards(cards)

    private fun setUpRecyclerView() {
        this.adapter = PaymentMethodsAdapter(this.viewModel, object: DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return (oldItem as UserPaymentsQuery.Node).id() == (newItem as UserPaymentsQuery.Node).id()
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return (oldItem as UserPaymentsQuery.Node).id() == (newItem as UserPaymentsQuery.Node).id()
            }


        })
        recycler_view.adapter = this.adapter
        recycler_view.layoutManager = LinearLayoutManager(this)
    }

    private fun lazyDeleteCardConfirmationDialog(): AlertDialog {
        if (this.showDeleteCardDialog == null) {
            this.showDeleteCardDialog = AlertDialog.Builder(this, R.style.AlertDialog)
                    .setCancelable(false)
                    .setTitle(R.string.Remove_this_card)
                    .setMessage(R.string.Are_you_sure_you_wish_to_remove_this_card)
                    .setNegativeButton(R.string.No_nevermind) { _, _ -> lazyDeleteCardConfirmationDialog().dismiss() }
                    .setPositiveButton(R.string.Yes_remove) { _, _ -> this.viewModel.inputs.confirmDeleteCardClicked() }
                    .create()
        }
        return this.showDeleteCardDialog!!
    }

}
