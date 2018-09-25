package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.kickstarter.R
import com.kickstarter.ui.views.ChangeCurrencyDialog
import kotlinx.android.synthetic.main.activity_account.*

const val FRAGMENT_TAG = "changeCurrencyFragment"

class AccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        setUpSpinner()

        change_email_row.setOnClickListener { startActivity(Intent(this, ChangeEmailActivity::class.java)) }
        change_password_row.setOnClickListener { startActivity(Intent(this, ChangePasswordActivity::class.java)) }
        privacy_row.setOnClickListener { startActivity(Intent(this, PrivacyActivity::class.java)) }
    }

    private fun setUpSpinner() {
        val currencies = listOf(getString(R.string.Currency_AUD), getString(R.string.Currency_CAD),
                getString(R.string.Currency_CHF), getString(R.string.Currency_DKK), getString(R.string.Currency_EUR),
                getString(R.string.Currency_GBP), getString(R.string.Currency_HKD), getString(R.string.Currency_JPY),
                getString(R.string.Currency_MXN), getString(R.string.Currency_NOK), getString(R.string.Currency_NZD),
                getString(R.string.Currency_SEK), getString(R.string.Currency_SGD), getString(R.string.Currency_USD))

        val arrayAdapter = ArrayAdapter<String>(this, R.layout.item_spinner, currencies)
        arrayAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        currency_spinner.adapter = arrayAdapter
        val itemPosition = currency_spinner.selectedItemPosition
        currency_spinner.setSelection(itemPosition, false)

        currency_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, postion: Int, id: Long) {
                if (itemPosition != postion) {
                    showDialog()
                }
            }
        }

    }

    private fun showDialog() {
        val fm = supportFragmentManager
        ChangeCurrencyDialog().show(fm, FRAGMENT_TAG)
    }
}
