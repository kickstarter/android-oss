package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityChangePasswordBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Logout
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.ChangePasswordViewModel
// import kotlinx.android.synthetic.main.activity_change_password.*
// import kotlinx.android.synthetic.main.change_password_toolbar.*

@RequiresActivityViewModel(ChangePasswordViewModel.ViewModel::class)
class ChangePasswordActivity : BaseActivity<ChangePasswordViewModel.ViewModel>() {

    private var saveEnabled = false
    private lateinit var logout: Logout
    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.changePasswordActivityToolbar.changePasswordToolbar)

        this.logout = environment().logout()

        this.viewModel.outputs.progressBarIsVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.progressBar.isGone = !it
              }

        this.viewModel.outputs.passwordWarning()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.warning.text = when {
                    it != null -> getString(it)
                    else -> null
                }
            }

        this.viewModel.outputs.saveButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { updateMenu(it) }

        this.viewModel.outputs.success()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { logout(it) }

        this.viewModel.outputs.error()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { showSnackbar(binding.changePasswordActivityToolbar.changePasswordToolbar, it) }

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
        this.logout.execute()
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
}
