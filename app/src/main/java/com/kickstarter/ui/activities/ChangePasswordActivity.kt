package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityChangePasswordBinding
import com.kickstarter.libs.Logout
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.ChangePasswordViewModel
import io.reactivex.disposables.CompositeDisposable

class ChangePasswordActivity : AppCompatActivity() {

    private var saveEnabled = false
    private var logout: Logout? = null
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var disposables: CompositeDisposable

    private lateinit var viewModelFactory: ChangePasswordViewModel.Factory
    private val viewModel: ChangePasswordViewModel.ChangePasswordViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()

        this.getEnvironment()?.let { env ->
            viewModelFactory = ChangePasswordViewModel.Factory(env)
        }
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.changePasswordActivityToolbar.changePasswordToolbar)

        this.logout = getEnvironment()?.logout()

        this.viewModel.outputs.progressBarIsVisible()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.progressBar.isGone = !it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.passwordWarning()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.warning.text = when {
                    it == 0 -> null
                    it != null -> getString(it)
                    else -> null
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.saveButtonIsEnabled()
            .compose(Transformers.observeForUIV2())
            .subscribe { updateMenu(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.success()
            .compose(Transformers.observeForUIV2())
            .subscribe { logout(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.error()
            .compose(Transformers.observeForUIV2())
            .subscribe { showSnackbar(binding.changePasswordActivityToolbar.changePasswordToolbar, it) }
            .addToDisposable(disposables)

        binding.currentPassword.onChange { this.viewModel.inputs.currentPassword(it) }
        binding.newPassword.onChange { this.viewModel.inputs.newPassword(it) }
        binding.confirmPassword.onChange { this.viewModel.inputs.confirmPassword(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                this.viewModel.inputs.changePasswordClicked()
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

    private fun logout(email: String) {
        this.logout?.execute()
        ApplicationUtils.startNewDiscoveryActivity(this)
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra(IntentKey.LOGIN_REASON, LoginReason.CHANGE_PASSWORD)
                .putExtra(IntentKey.EMAIL, email)
        )
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
