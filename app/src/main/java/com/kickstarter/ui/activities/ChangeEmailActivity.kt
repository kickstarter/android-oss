package com.kickstarter.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.kickstarter.R
import com.kickstarter.extensions.onChange
import com.kickstarter.extensions.showConfirmationSnackbar
import com.kickstarter.extensions.showErrorSnackbar
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.viewmodels.ChangeEmailViewModel
import kotlinx.android.synthetic.main.activity_change_email.*
import kotlinx.android.synthetic.main.change_email_toolbar.*
import rx.android.schedulers.AndroidSchedulers


@RequiresActivityViewModel(ChangeEmailViewModel.ViewModel::class)
class ChangeEmailActivity : BaseActivity<ChangeEmailViewModel.ViewModel>() {

    private var saveEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)
        setSupportActionBar(change_email_toolbar)

        new_email.onChange { this.viewModel.inputs.email(it) }
        current_password.onChange { this.viewModel.inputs.password(it) }

        new_email.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            this@ChangeEmailActivity.viewModel.inputs.emailFocus(hasFocus)
        }

        this.viewModel.outputs.currentEmail()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { current_email.text = it }

        this.viewModel.outputs.emailErrorIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { it }
                .subscribe { new_email_container.error = getString(R.string.Email_must_be_a_valid_email_address) }

        this.viewModel.outputs.emailErrorIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { !it }
                .subscribe { new_email_container.error = null }

        this.viewModel.outputs.error()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showErrorSnackbar(change_email_layout, it) }

        this.viewModel.outputs.isEmailVerified()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    ViewUtils.setGone(email_verification_text_view, it)
                    ViewUtils.setGone(resend_email_row, it)
                }

        this.viewModel.outputs.saveButtonIsEnabled()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updateMenu(it) }

        this.viewModel.outputs.progressBarIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updateMenu(!it) }

        this.viewModel.outputs.progressBarIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(progress_bar, !it) }

        this.viewModel.outputs.success()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showConfirmationSnackbar(change_email_layout, R.string.Verification_email_sent) }

        this.viewModel.outputs.success()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { clearForm() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                this.viewModel.inputs.updateEmailClicked()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(new_email.windowToken, 0)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = this.menuInflater
        inflater.inflate(R.menu.save, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val save = menu.findItem(R.id.save)
        save.isEnabled = this.saveEnabled
        return super.onPrepareOptionsMenu(menu)
    }

    private fun clearForm() {
        new_email.text = null
        current_password.text = null
    }

    private fun updateMenu(saveEnabled: Boolean) {
        this.saveEnabled = saveEnabled
        invalidateOptionsMenu()
    }
}
