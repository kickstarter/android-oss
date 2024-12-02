package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.R
import com.kickstarter.databinding.ActivityAccountBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getPaymentMethodsIntent
import com.kickstarter.type.CurrencyCode
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.AccountViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class AccountActivity : AppCompatActivity() {

    private var currentCurrencySelection: CurrencyCode? = null
    private var newCurrencySelection: CurrencyCode? = null
    private var showCurrencyChangeDialog: AlertDialog? = null

    private lateinit var ksString: KSString

    private lateinit var binding: ActivityAccountBinding

    private val supportedCurrencies: List<CurrencyCode> by lazy {
        arrayListOf(
            CurrencyCode.AUD,
            CurrencyCode.CAD,
            CurrencyCode.CHF,
            CurrencyCode.DKK,
            CurrencyCode.EUR,
            CurrencyCode.GBP,
            CurrencyCode.HKD,
            CurrencyCode.JPY,
            CurrencyCode.MXN,
            CurrencyCode.NOK,
            CurrencyCode.PLN,
            CurrencyCode.NZD,
            CurrencyCode.SEK,
            CurrencyCode.SGD,
            CurrencyCode.USD
        )
    }

    private lateinit var viewModelFactory: AccountViewModel.Factory
    private val viewModel: AccountViewModel.AccountViewModel by viewModels { viewModelFactory }

    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()

        val env = this.getEnvironment()?.let { env ->
            viewModelFactory = AccountViewModel.Factory(env, intent = intent)
            env
        }

        binding = ActivityAccountBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root,
        )
        setContentView(binding.root)

        setUpConnectivityStatusCheck(lifecycle)
        this.ksString = requireNotNull(env?.ksString())

        setUpSpinner()

        this.viewModel.outputs.chosenCurrency()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setSpinnerSelection(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.email()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.createPasswordTextView.text = this.ksString.format(
                    getString(R.string.Youre_connected_via_Facebook_email_Create_a_password_for_this_account),
                    "email",
                    it
                )
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.error()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(binding.accountToolbar.accountToolbar, it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.progressBarIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.setGone(binding.progressBar, !it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.passwordRequiredContainerIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ViewUtils.setGone(binding.createPasswordContainer, it)
                ViewUtils.setGone(binding.passwordRequiredContainer, !it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.showEmailErrorIcon()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.setGone(binding.emailErrorIcon, !it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.success()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showSnackbar(
                    binding.accountContainer,
                    R.string.Got_it_your_changes_have_been_saved
                )
            }
            .addToDisposable(disposables)

        binding.createPasswordRow.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    CreatePasswordActivity::class.java
                )
            )
        }
        binding.changeEmailRow.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ChangeEmailActivity::class.java
                )
            )
        }
        binding.changePasswordRow.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ChangePasswordActivity::class.java
                )
            )
        }
        binding.paymentMethodsRow.setOnClickListener {
            startActivity(
                Intent().getPaymentMethodsIntent(
                    this
                )
            )
        }
        binding.privacyRow.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    PrivacyActivity::class.java
                )
            )
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun getListOfCurrencies(): List<String> {
        val strings = arrayListOf<String>()
        for (currency in supportedCurrencies) {
            strings.add(getStringForCurrencyCode(currency))
        }
        return strings
    }

    private fun getStringForCurrencyCode(currency: CurrencyCode): String {
        return when (currency) {
            CurrencyCode.AUD -> getString(R.string.Currency_AUD)
            CurrencyCode.CAD -> getString(R.string.Currency_CAD)
            CurrencyCode.CHF -> getString(R.string.Currency_CHF)
            CurrencyCode.DKK -> getString(R.string.Currency_DKK)
            CurrencyCode.EUR -> getString(R.string.Currency_EUR)
            CurrencyCode.GBP -> getString(R.string.Currency_GBP)
            CurrencyCode.HKD -> getString(R.string.Currency_HKD)
            CurrencyCode.JPY -> getString(R.string.Currency_JPY)
            CurrencyCode.MXN -> getString(R.string.Currency_MXN)
            CurrencyCode.NOK -> getString(R.string.Currency_NOK)
            CurrencyCode.NZD -> getString(R.string.Currency_NZD)
            CurrencyCode.PLN -> getString(R.string.Currency_PLN)
            CurrencyCode.SEK -> getString(R.string.Currency_SEK)
            CurrencyCode.SGD -> getString(R.string.Currency_SGD)
            CurrencyCode.USD -> getString(R.string.Currency_USD)
            else -> currency.rawValue
        }
    }

    private fun lazyFollowingOptOutConfirmationDialog(): AlertDialog {
        if (this.showCurrencyChangeDialog == null) {
            this.showCurrencyChangeDialog = AlertDialog.Builder(this, R.style.AlertDialog)
                .setCancelable(false)
                .setTitle(getString(R.string.Change_currency))
                .setMessage(getString(R.string.Project_goal_and_pledge))
                .setNegativeButton(R.string.Cancel) { _, _ ->
                    setSpinnerSelection(currentCurrencySelection!!.rawValue)
                }
                .setPositiveButton(R.string.Yes_change_currency) { _, _ ->
                    this.viewModel.inputs.onSelectedCurrency(newCurrencySelection!!)
                    setSpinnerSelection(newCurrencySelection!!.rawValue)
                }
                .create()
        }
        return this.showCurrencyChangeDialog!!
    }

    private fun setSpinnerSelection(currencyCode: String) {
        val selectedCurrencyCode = CurrencyCode.safeValueOf(currencyCode)
        currentCurrencySelection = selectedCurrencyCode
        binding.currencySpinner.setSelection(supportedCurrencies.indexOf(selectedCurrencyCode))
    }

    private fun setUpSpinner() {
        val arrayAdapter = ArrayAdapter<String>(this, R.layout.item_spinner, getListOfCurrencies())
        arrayAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.currencySpinner.adapter = arrayAdapter

        binding.currencySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    postion: Int,
                    id: Long
                ) {
                    currentCurrencySelection?.let {
                        if (supportedCurrencies.indexOf(it) != postion) {
                            newCurrencySelection = supportedCurrencies[postion]
                            lazyFollowingOptOutConfirmationDialog().show()
                        }
                    }
                }
            }
    }
}
