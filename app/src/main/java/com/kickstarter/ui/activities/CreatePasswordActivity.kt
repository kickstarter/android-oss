package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.Menu
import android.view.MenuItem
import com.kickstarter.R
import com.kickstarter.extensions.onChange
import com.kickstarter.extensions.showErrorSnackbar
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Logout
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.viewmodels.CreatePasswordViewModel
import kotlinx.android.synthetic.main.activity_create_password.*
import kotlinx.android.synthetic.main.create_password_toolbar.*

@RequiresActivityViewModel(CreatePasswordViewModel.ViewModel::class)
class CreatePasswordActivity : BaseActivity<CreatePasswordViewModel.ViewModel>() {

    private var saveEnabled = false
    private lateinit var logout: Logout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_password)
        setSupportActionBar(create_password_toolbar)

        this.logout = environment().logout()

        this.viewModel.outputs.progressBarIsVisible()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(progress_bar, !it) }

        this.viewModel.outputs.passwordWarning()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    warning.text = when {
                        it != null -> getString(it)
                        else -> null
                    }
                }

        this.viewModel.outputs.saveButtonIsEnabled()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { updateMenu(it) }

        this.viewModel.outputs.success()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { logout(it) }

        this.viewModel.outputs.error()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { showErrorSnackbar(create_password_toolbar, it) }

        new_password.onChange { this.viewModel.inputs.newPassword(it) }
        confirm_password.onChange { this.viewModel.inputs.confirmPassword(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                this.viewModel.inputs.submitPasswordClicked()
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

    override fun exitTransition(): Pair<Int, Int> = TransitionUtils.slideUpFromBottom()

    private fun logout(email: String) {
        this.logout.execute()
        ApplicationUtils.startNewDiscoveryActivity(this)
        startActivity(Intent(this, LoginActivity::class.java)
                .putExtra(IntentKey.LOGIN_REASON, LoginReason.CHANGE_PASSWORD)
                .putExtra(IntentKey.EMAIL, email))
    }

    private fun updateMenu(saveEnabled: Boolean) {
        this.saveEnabled = saveEnabled
        invalidateOptionsMenu()
    }
}
