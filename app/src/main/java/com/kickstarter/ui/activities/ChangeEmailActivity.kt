package com.kickstarter.ui.activities

import android.os.Bundle
import android.widget.Toast
import com.kickstarter.R
import com.kickstarter.extensions.showSuccessSnackbar
import com.kickstarter.extensions.text
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.viewmodels.ChangeEmailViewModel
import kotlinx.android.synthetic.main.activity_change_email.*
import kotlinx.android.synthetic.main.change_email_toolbar.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ChangeEmailViewModel.ViewModel::class)
class ChangeEmailActivity : BaseActivity<ChangeEmailViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)

        save_email_button.setOnClickListener {
            this.viewModel.inputs.updateEmailClicked(new_email_edit_text.text(), current_password_edit_text.text())
            clearPasswordAndEmail()
        }

        this.viewModel.outputs.email()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { user_current_email_text_view.text = it }

        this.viewModel.errors.error()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }

        this.viewModel.outputs.showProgressBar()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(progress_bar, !it) }

        this.viewModel.outputs.success()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showSnackbar() }
    }

    private fun clearPasswordAndEmail() {
        new_email_edit_text.text = null
        current_password_edit_text.text = null
    }

    private fun showSnackbar() {
        showSuccessSnackbar(change_email_layout, R.string.Verification_email_sent )
    }
}
