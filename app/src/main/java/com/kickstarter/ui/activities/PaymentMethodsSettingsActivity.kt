package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySettingsPaymentMethodsBinding
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getPaymentSheetConfiguration
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.adapters.PaymentMethodsAdapter
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.showErrorSnackBar
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.PaymentMethodsViewModel
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.model.PaymentOption
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PaymentMethodsSettingsActivity : AppCompatActivity() {

    private lateinit var adapter: PaymentMethodsAdapter
    private var showDeleteCardDialog: AlertDialog? = null

    private lateinit var binding: ActivitySettingsPaymentMethodsBinding
    private lateinit var flowController: PaymentSheet.FlowController

    private lateinit var viewModelFactory: PaymentMethodsViewModel.Factory
    private val viewModel: PaymentMethodsViewModel by viewModels { viewModelFactory }

    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        compositeDisposable = CompositeDisposable()

        this.getEnvironment()?.let { env ->
            viewModelFactory = PaymentMethodsViewModel.Factory(env)
        }

        binding = ActivitySettingsPaymentMethodsBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        setUpConnectivityStatusCheck(lifecycle)
        setUpRecyclerView()

        flowController = PaymentSheet.FlowController.create(
            activity = this,
            paymentOptionCallback = ::onPaymentOption,
            paymentResultCallback = ::onPaymentSheetResult
        )

        compositeDisposable.add(
            this.viewModel.outputs.cards()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setCards(it) }
        )

        compositeDisposable.add(
            this.viewModel.outputs.dividerIsVisible()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    binding.paymentsDivider.isGone = !it
                }
        )

        compositeDisposable.add(
            this.viewModel.outputs.error()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showSnackbar(binding.settingPaymentMethodsActivityToolbar.paymentMethodsToolbar, it) }
        )

        compositeDisposable.add(
            this.viewModel.outputs.progressBarIsVisible()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    binding.progressBar.isGone = !it
                }
        )

        compositeDisposable.add(
            this.viewModel.outputs.showDeleteCardDialog()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { lazyDeleteCardConfirmationDialog().show() }
        )

        compositeDisposable.add(
            this.viewModel.successDeleting()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showSnackbar(binding.settingPaymentMethodsActivityToolbar.paymentMethodsToolbar, R.string.Got_it_your_changes_have_been_saved) }
        )

        binding.addNewCard.setOnClickListener {
            this.viewModel.inputs.newCardButtonClicked()
        }

        compositeDisposable.add(
            this.viewModel.outputs.presentPaymentSheet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    flowControllerPresentPaymentOption(it.first, it.second)
                }
        )

        compositeDisposable.add(
            this.viewModel.showError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showErrorSnackBar(binding.settingPaymentMethodsActivityToolbar.paymentMethodsToolbar, getString(R.string.general_error_something_wrong))
                }
        )

        compositeDisposable.add(
            this.viewModel.successSaving()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showSnackbar(binding.settingPaymentMethodsActivityToolbar.paymentMethodsToolbar, R.string.Got_it_your_changes_have_been_saved)
                }
        )
    }

    @Override
    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun flowControllerPresentPaymentOption(clientSecret: String, userEmail: String) {
        flowController.configureWithSetupIntent(
            setupIntentClientSecret = clientSecret,
            configuration = this.getPaymentSheetConfiguration(userEmail),
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
            this.viewModel.inputs.confirmedLoading(true)
        }
    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        this.viewModel.inputs.confirmedLoading(false)
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                showErrorSnackBar(binding.paymentMethodsContent, getString(R.string.general_error_oops))
            }
            is PaymentSheetResult.Failed -> {
                showErrorSnackBar(binding.paymentMethodsContent, getString(R.string.general_error_something_wrong))
            }
            is PaymentSheetResult.Completed -> {
                this.viewModel.inputs.savePaymentOption()
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
