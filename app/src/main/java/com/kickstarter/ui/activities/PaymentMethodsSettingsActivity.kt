package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySettingsPaymentMethodsBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.extensions.getPaymentSheetConfiguration
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.PaymentMethodsViewModel
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.model.PaymentOption
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(PaymentMethodsViewModel.ViewModel::class)
class PaymentMethodsSettingsActivity : BaseActivity<PaymentMethodsViewModel.ViewModel>() {

    private lateinit var adapter: PaymentMethodsAdapter
    private var showDeleteCardDialog: AlertDialog? = null

    private lateinit var binding: ActivitySettingsPaymentMethodsBinding
    private var setupClientId: String = "seti_1KbABk4VvJ2PtfhKV8E7dvGe_secret_LHjfXxFl9UDucYtsL5a3WtySqjgqf5F" // TODO: delete once the real network call takes place
    private lateinit var flowController: PaymentSheet.FlowController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsPaymentMethodsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setUpRecyclerView()

        flowController = PaymentSheet.FlowController.create(
            activity = this,
            paymentOptionCallback = ::onPaymentOption,
            paymentResultCallback = ::onPaymentSheetResult
        )

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
            // - TODO, sent input to viewModel for the real networking call once CreateSetupIntent mutation project parameter becomes optional
            flowControllerPresentPaymentOption(setupClientId) // TODO: testing presenting the paymentSheet delete once
        }

        /*
        // - TODO, present paymentSheet with the setpUpclientID
        this.viewModel.outputs.presentPaymentSheet()
            .compose(Transformers.observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                setupClientId = it
                flowControllerPresentPaymentOption(it)
            }
         */
    }

    private fun flowControllerPresentPaymentOption(clientSecret: String) {
        flowController.configureWithSetupIntent(
            setupIntentClientSecret = clientSecret,
            configuration = this.getPaymentSheetConfiguration(),
            callback = ::onConfigured
        )
    }

    private fun onConfigured(success: Boolean, error: Throwable?) {
        if (success) {
            flowController.presentPaymentOptions()
        } else {
            showErrorToast(this, binding.paymentMethodsContent, getString(R.string.general_error_something_wrong))
        }
    }

    private fun onPaymentOption(paymentOption: PaymentOption?) {
        paymentOption?.let {
            flowController.confirm()
        }
    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                showErrorToast(this, binding.paymentMethodsContent, getString(R.string.general_error_oops))
            }
            is PaymentSheetResult.Failed -> {
                showErrorToast(this, binding.paymentMethodsContent, getString(R.string.general_error_something_wrong))
            }
            is PaymentSheetResult.Completed -> {
            }
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
