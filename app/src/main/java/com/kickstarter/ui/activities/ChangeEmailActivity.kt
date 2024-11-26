package com.kickstarter.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityChangeEmailBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.ChangeEmailViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ChangeEmailActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: ChangeEmailViewModel.Factory
    private val viewModel: ChangeEmailViewModel.ChangeEmailViewModel by viewModels { viewModelFactory }

    private var saveEnabled = false

    private lateinit var binding: ActivityChangeEmailBinding

    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()

        this.getEnvironment()?.let { env ->
            viewModelFactory = ChangeEmailViewModel.Factory(env)
        }

        binding = ActivityChangeEmailBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root,
        )
        setContentView(binding.root)
        setSupportActionBar(binding.changeEmailActivityToolbar.changeEmailToolbar)

        setUpConnectivityStatusCheck(lifecycle)
        binding.newEmail.onChange { this.viewModel.inputs.email(it) }
        binding.currentPassword.onChange { this.viewModel.inputs.password(it) }
        binding.sendVerificationEmail.setOnClickListener { this.viewModel.inputs.sendVerificationEmail() }

        binding.newEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            this@ChangeEmailActivity.viewModel.inputs.emailFocus(hasFocus)
        }

        this.viewModel.outputs.currentEmail()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.currentEmail.setText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.emailErrorIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it }
            .subscribe {
                binding.newEmailContainer.error =
                    getString(R.string.Email_must_be_a_valid_email_address)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.emailErrorIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .filter { !it }
            .subscribe { binding.newEmailContainer.error = null }
            .addToDisposable(disposables)

        this.viewModel.outputs.error()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(binding.changeEmailLayout, it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.sendVerificationIsHidden()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.sendVerificationEmail.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.saveButtonIsEnabled()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateMenu(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.progressBarIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateMenu(!it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.progressBarIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.progressBar.isGone = !it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.success()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(binding.changeEmailLayout, R.string.Verification_email_sent) }
            .addToDisposable(disposables)

        this.viewModel.outputs.success()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { clearForm() }
            .addToDisposable(disposables)

        this.viewModel.outputs.warningText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it != 0) {
                    binding.emailWarningTextView.text = getString(it)
                    binding.emailWarningTextView.visibility = View.VISIBLE
                } else {
                    binding.emailWarningTextView.isGone = true
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.warningTextColor()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.emailWarningTextView.setTextColor(
                    ContextCompat.getColor(
                        this@ChangeEmailActivity,
                        it
                    )
                )
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.verificationEmailButtonText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.sendVerificationEmail.text = getString(it) }
            .addToDisposable(disposables)
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

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
