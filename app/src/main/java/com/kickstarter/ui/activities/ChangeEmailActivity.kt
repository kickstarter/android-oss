package com.kickstarter.ui.activities

import android.os.Bundle
import android.widget.Toast
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.viewmodels.ChangeEmailViewModel
import kotlinx.android.synthetic.main.activity_change_email.*
import kotlinx.android.synthetic.main.change_email_toolbar.*
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber

@RequiresActivityViewModel(ChangeEmailViewModel.ViewModel::class)
class ChangeEmailActivity : BaseActivity<ChangeEmailViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)


        save_email_button.setOnClickListener {
            this.viewModel.inputs.makeNetworkCallClicked()
//            clearNameAndEmail()
        }

        this.viewModel.outputs.userEmail()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { user_current_email_text_view.text = it
                Timber.d(it, "USERNAME")}

//        this.viewModel.outputs.name()
//                .compose(bindToLifecycle())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe { name.text = it }

        this.viewModel.outputs.showProgressBar()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(progress_bar, !it)}

        this.viewModel.errors.error()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
    }

    private fun clearNameAndEmail() {
//        user_current_email_text_view.text = null
//        name.text = null
    }
}
