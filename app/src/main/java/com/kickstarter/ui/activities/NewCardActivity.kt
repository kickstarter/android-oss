package com.kickstarter.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.kickstarter.R
import com.kickstarter.extensions.onChange
import com.kickstarter.extensions.showConfirmationSnackbar
import com.kickstarter.extensions.showErrorSnackbar
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.viewmodels.NewCardViewModel
import com.stripe.android.view.CardInputListener
import kotlinx.android.synthetic.main.activity_new_card.*

@RequiresActivityViewModel(NewCardViewModel.ViewModel::class)
class NewCardActivity  : BaseActivity<NewCardViewModel.ViewModel>(){
    private var saveEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_card)
        setSupportActionBar(new_card_toolbar)


        this.viewModel.outputs.progressBarIsVisible()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(progress_bar, !it) }

        this.viewModel.outputs.saveButtonIsEnabled()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { updateMenu(it) }

        this.viewModel.outputs.success()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { showConfirmationSnackbar(new_card_toolbar, it) }

        this.viewModel.outputs.error()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { showErrorSnackbar(new_card_toolbar, it) }

        cardholder_name.onChange { this.viewModel.inputs.name(it) }
        postal_code.onChange { this.viewModel.inputs.postalCode(it) }
        addCardListener()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                this.viewModel.inputs.saveCardClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.save, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val save = menu.findItem(R.id.save)
        save.isEnabled = saveEnabled
        return super.onPrepareOptionsMenu(menu)
    }

    private fun addCardListener() {
        card_input_widget.setCardInputListener(object : CardInputListener {
            override fun onFocusChange(focusField: String?) {

            }

            override fun onPostalCodeComplete() {
            }

            override fun onCardComplete() {
                cardChanged()
            }

            override fun onExpirationComplete() {
                cardChanged()
            }

            override fun onCvcComplete() {
                cardChanged()
            }
        })
    }

    private fun cardChanged() {
        this@NewCardActivity.viewModel.inputs.card(card_input_widget.card)
    }

    private fun updateMenu(saveEnabled: Boolean) {
        this.saveEnabled = saveEnabled
        invalidateOptionsMenu()
    }
}
