package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityCreatePasswordBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Logout
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.AccountViewModel
import com.kickstarter.viewmodels.CreatePasswordViewModel
import com.kickstarter.viewmodels.projectpage.PrelaunchProjectViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class CreatePasswordActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: CreatePasswordViewModel.Factory
    private val viewModel: CreatePasswordViewModel.CreatePasswordViewModel by viewModels { viewModelFactory }
    private var saveEnabled = false
    private var logout: Logout? = null
    private val disposables = CompositeDisposable()

    private lateinit var binding: ActivityCreatePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePasswordBinding.inflate(layoutInflater)

        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = CreatePasswordViewModel.Factory(env)
            env
        }

        setContentView(binding.root)
        setSupportActionBar(binding.createPasswordActivityToolbar.createPasswordToolbar)

        this.logout = environment?.logout()

        this.viewModel.outputs.progressBarIsVisible()
            .subscribe {
                binding.progressBar.isGone = !it
            }.addToDisposable(disposables)

        this.viewModel.outputs.passwordWarning()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.warning.text = when {
                    it != null -> getString(it)
                    else -> null
                }
            }.addToDisposable(disposables)

        this.viewModel.outputs.saveButtonIsEnabled()
                .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateMenu(it) }
                .addToDisposable(disposables)

        this.viewModel.outputs.success()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { logout(it) }
                .addToDisposable(disposables)

        this.viewModel.outputs.error()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showSnackbar(binding.createPasswordActivityToolbar.createPasswordToolbar, it) }
                .addToDisposable(disposables)

        binding.newPassword.onChange { this.viewModel.inputs.newPassword(it) }
        binding.confirmPassword.onChange { this.viewModel.inputs.confirmPassword(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                this.viewModel.inputs.createPasswordClicked()
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
                .putExtra(IntentKey.LOGIN_REASON, LoginReason.CREATE_PASSWORD)
                .putExtra(IntentKey.EMAIL, email)
        )
    }

    private fun updateMenu(saveEnabled: Boolean) {
        this.saveEnabled = saveEnabled
        invalidateOptionsMenu()
    }
}
