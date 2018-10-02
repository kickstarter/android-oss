package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.models.CurrencyEnum
import com.kickstarter.viewmodels.AccountActivityViewModel
import kotlinx.android.synthetic.main.activity_account.*
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import type.CurrencyCode

@RequiresActivityViewModel(AccountActivityViewModel.ViewModel::class)
class AccountActivity : BaseActivity<AccountActivityViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        this.viewModel.outputs.chosenCurrency()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val text = getUserChosenCurrency(it)
                    Timber.d(" This is the currency: $text")
                }

        setUpSpinner()

        change_email_row.setOnClickListener { startActivity(Intent(this, ChangeEmailActivity::class.java)) }
        change_password_row.setOnClickListener { startActivity(Intent(this, ChangePasswordActivity::class.java)) }
        privacy_row.setOnClickListener { startActivity(Intent(this, PrivacyActivity::class.java)) }
    }

    private fun setUpSpinner() {
        val currencies = CurrencyEnum.getCurrencies(this.resources)

        val arrayAdapter = ArrayAdapter<String>(this, R.layout.item_spinner, currencies)
        arrayAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        currency_spinner.adapter = arrayAdapter
    }

    private fun getUserChosenCurrency(currencyCode: String): String {
         return when (currencyCode) {
            CurrencyCode.AUD.toString() -> getString(R.string.Currency_AUD)
            CurrencyCode.CAD.toString() -> getString(R.string.Currency_CAD)
            CurrencyCode.CHF.toString() -> getString(R.string.Currency_CHF)
            CurrencyCode.DKK.toString() -> getString(R.string.Currency_DKK)
            CurrencyCode.EUR.toString() -> getString(R.string.Currency_EUR)
            CurrencyCode.GBP.toString() -> getString(R.string.Currency_GBP)
            CurrencyCode.HKD.toString() -> getString(R.string.Currency_HKD)
            CurrencyCode.JPY.toString() -> getString(R.string.Currency_JPY)
            CurrencyCode.MXN.toString() -> getString(R.string.Currency_MXN)
            CurrencyCode.NOK.toString() -> getString(R.string.Currency_NOK)
            CurrencyCode.NZD.toString() -> getString(R.string.Currency_NZD)
            CurrencyCode.SEK.toString() -> getString(R.string.Currency_SEK)
            CurrencyCode.SGD.toString() -> getString(R.string.Currency_SGD)
            CurrencyCode.USD .toString()-> getString(R.string.Currency_USD)
             else -> "couldn't get code"
        }
    }
}
