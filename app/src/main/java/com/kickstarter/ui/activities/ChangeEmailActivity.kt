package com.kickstarter.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.kickstarter.R
import com.kickstarter.extensions.onChange
import com.kickstarter.extensions.showErrorSnackbar
import com.kickstarter.extensions.showSuccessSnackbar
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

        new_email.onChange { this.viewModel.inputs.email(it)}
        current_password.onChange { this.viewModel.inputs.password(it)}

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
                .subscribe {
                    when {
                        it -> new_email_container.error = getString(R.string.Got_it)
                        else -> new_email_container.error = null
                    }
                }

        this.viewModel.outputs.error()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showErrorSnackbar(change_email_layout, it) }

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
                .subscribe { showSuccessSnackbar(change_email_layout, R.string.Verification_email_sent) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                this.viewModel.inputs.updateEmailClicked()
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

    private fun updateMenu(saveEnabled: Boolean) {
        this.saveEnabled = saveEnabled
        invalidateOptionsMenu()
    }
}
