package com.kickstarter.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityChangeEmailBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.ChangeEmailViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ChangeEmailViewModel.ViewModel::class)
class ChangeEmailActivity : BaseActivity<ChangeEmailViewModel.ViewModel>() {

    private var saveEnabled = false

    private lateinit var binding: ActivityChangeEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeEmailBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.changeEmailActivityToolbar.changeEmailToolbar)

        binding.newEmail.onChange { this.viewModel.inputs.email(it) }
        binding.currentPassword.onChange { this.viewModel.inputs.password(it) }
        binding.sendVerificationEmail.setOnClickListener { this.viewModel.inputs.sendVerificationEmail() }

        binding.newEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            this@ChangeEmailActivity.viewModel.inputs.emailFocus(hasFocus)
        }

        this.viewModel.outputs.currentEmail()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.currentEmail.setText(it) }

        this.viewModel.outputs.emailErrorIsVisible()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it }
            .subscribe { binding.newEmailContainer.error = getString(R.string.Email_must_be_a_valid_email_address) }

        this.viewModel.outputs.emailErrorIsVisible()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .filter { !it }
            .subscribe { binding.newEmailContainer.error = null }

        this.viewModel.outputs.error()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(binding.changeEmailLayout, it) }

        this.viewModel.outputs.sendVerificationIsHidden()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.sendVerificationEmail.isGone = it
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
            .subscribe {
                binding.progressBar.isGone = !it
            }

        this.viewModel.outputs.success()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(binding.changeEmailLayout, R.string.Verification_email_sent) }

        this.viewModel.outputs.success()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { clearForm() }

        this.viewModel.outputs.warningText()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it != null) {
                    binding.emailWarningTextView.text = getString(it)
                    binding.emailWarningTextView.visibility = View.VISIBLE
                } else {
                    binding.emailWarningTextView.isGone = true
                }
            }

        this.viewModel.outputs.warningTextColor()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.emailWarningTextView.setTextColor(ContextCompat.getColor(this@ChangeEmailActivity, it)) }

        this.viewModel.outputs.verificationEmailButtonText()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.sendVerificationEmail.text = getString(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                this.viewModel.inputs.updateEmailClicked()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.newEmail.windowToken, 0)
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
        binding.newEmail.text = null
        binding.currentEmail.text = null
    }

    private fun updateMenu(saveEnabled: Boolean) {
        this.saveEnabled = saveEnabled
        invalidateOptionsMenu()
    }
}
