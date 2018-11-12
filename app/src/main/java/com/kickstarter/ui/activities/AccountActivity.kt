package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.extensions.showConfirmationSnackbar
import com.kickstarter.extensions.showErrorSnackbar
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.viewmodels.AccountViewModel
import kotlinx.android.synthetic.main.account_toolbar.*
import kotlinx.android.synthetic.main.activity_account.*
import rx.android.schedulers.AndroidSchedulers
import type.CurrencyCode



@RequiresActivityViewModel(AccountViewModel.ViewModel::class)
class AccountActivity : BaseActivity<AccountViewModel.ViewModel>() {

    private var currentCurrencySelection: CurrencyCode? = null
    private var newCurrencySelection: CurrencyCode? = null
    private var showCurrencyChangeDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        if (BuildConfig.DEBUG) {
            payment_methods_row.visibility = View.VISIBLE
        }

        setUpSpinner()

        this.viewModel.outputs.chosenCurrency()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    setSpinnerSelection(it)
                }

        this.viewModel.outputs.error()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { showErrorSnackbar(account_toolbar, it) }

        this.viewModel.outputs.progressBarIsVisible()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(progress_bar, !it) }

        this.viewModel.outputs.success()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { showConfirmationSnackbar(account_container, R.string.Got_it_your_changes_have_been_saved) }

        change_email_row.setOnClickListener { startActivity(Intent(this, ChangeEmailActivity::class.java)) }
        change_password_row.setOnClickListener { startActivity(Intent(this, ChangePasswordActivity::class.java)) }
        privacy_row.setOnClickListener { startActivity(Intent(this, PrivacyActivity::class.java)) }
    }

    private fun getListOfCurrencies(): List<String> {
        val strings = arrayListOf<String>()
        for (currency in CurrencyCode.values()) {
            strings.add(getStringForCurrencyCode(currency))
        }
        return strings.filter { it != CurrencyCode.`$UNKNOWN`.rawValue() }
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
            CurrencyCode.SEK -> getString(R.string.Currency_SEK)
            CurrencyCode.SGD -> getString(R.string.Currency_SGD)
            CurrencyCode.USD -> getString(R.string.Currency_USD)
            else -> CurrencyCode.`$UNKNOWN`.rawValue()
        }
    }

    private fun lazyFollowingOptOutConfirmationDialog(): AlertDialog {
        if (this.showCurrencyChangeDialog == null) {
            this.showCurrencyChangeDialog = AlertDialog.Builder(this, R.style.AlertDialog)
                    .setCancelable(false)
                    .setTitle(getString(R.string.Change_currency))
                    .setMessage(getString(R.string.This_allows_you_to_see_project_goal_and_pledge_amounts_in_your_preferred_currency)
                            + "\n\n" + getString(R.string.A_successfully_funded_project_will_collect_your_pledge_in_its_native_currency))
                    .setNegativeButton(R.string.Cancel) { _, _ ->
                        setSpinnerSelection(currentCurrencySelection!!.rawValue())
                    }
                    .setPositiveButton(R.string.Yes_change_currency) { _, _ ->
                        this.viewModel.inputs.onSelectedCurrency(newCurrencySelection!!)
                        setSpinnerSelection(newCurrencySelection!!.rawValue())
                    }
                    .create()
        }
        return this.showCurrencyChangeDialog!!
    }

    private fun setSpinnerSelection(currencyCode: String) {
        currentCurrencySelection = CurrencyCode.safeValueOf(currencyCode)
        currency_spinner.setSelection(currentCurrencySelection!!.ordinal)
    }

    private fun setUpSpinner() {
        val arrayAdapter = ArrayAdapter<String>(this, R.layout.item_spinner, getListOfCurrencies())
        arrayAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        currency_spinner.adapter = arrayAdapter

        currency_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, postion: Int, id: Long) {
                if (currentCurrencySelection != null && currentCurrencySelection!!.ordinal != postion) {
                    newCurrencySelection = CurrencyCode.values()[postion]
                    lazyFollowingOptOutConfirmationDialog().show()
                }
            }
        }
    }
}
