package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySettingsPaymentMethodsBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.PaymentMethodsViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(PaymentMethodsViewModel.ViewModel::class)
class PaymentMethodsSettingsActivity : BaseActivity<PaymentMethodsViewModel.ViewModel>() {

    private lateinit var adapter: PaymentMethodsAdapter
    private var showDeleteCardDialog: AlertDialog? = null

    private lateinit var binding: ActivitySettingsPaymentMethodsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsPaymentMethodsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setUpRecyclerView()

        this.viewModel.outputs.cards()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setCards(it) }

        this.viewModel.outputs.dividerIsVisible()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.paymentsDivider.isGone = !it
            }

        this.viewModel.outputs.error()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(binding.settingPaymentMethodsActivityToolbar.paymentMethodsToolbar, it) }

        this.viewModel.outputs.progressBarIsVisible()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.progressBar.isGone = !it
               }

        this.viewModel.outputs.showDeleteCardDialog()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lazyDeleteCardConfirmationDialog().show() }

        this.viewModel.success()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(binding.settingPaymentMethodsActivityToolbar.paymentMethodsToolbar, R.string.Got_it_your_changes_have_been_saved) }

        binding.addNewCard.setOnClickListener {
            startActivityForResult(
                Intent(this, NewCardActivity::class.java),
                ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD && resultCode == Activity.RESULT_OK) {
            showSnackbar(binding.settingPaymentMethodsActivityToolbar.paymentMethodsToolbar, R.string.Got_it_your_changes_have_been_saved)
            this@PaymentMethodsSettingsActivity.viewModel.inputs.refreshCards()
        }
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

    private fun setCards(cards: List<StoredCard>) = this.adapter.populateCards(cards)

    private fun setUpRecyclerView() {
        this.adapter = PaymentMethodsAdapter(
            this.viewModel,
            object : DiffUtil.ItemCallback<Any>() {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return areCardsTheSame(oldItem as StoredCard, newItem as StoredCard)
                }

                override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return areCardsTheSame(oldItem as StoredCard, newItem as StoredCard)
                }

                private fun areCardsTheSame(oldCard: StoredCard, newCard: StoredCard): Boolean {
                    return oldCard.id() == newCard.id()
                }
            }
        )
        binding.recyclerView.adapter = this.adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }
}
