package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.kickstarter.R
import com.kickstarter.databinding.FacebookConfirmationLayoutBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.FacebookConfirmationViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class FacebookConfirmationActivity : ComponentActivity() {
    private lateinit var binding: FacebookConfirmationLayoutBinding
    private lateinit var viewModelFactory: FacebookConfirmationViewModel.Factory
    private val viewModel: FacebookConfirmationViewModel.FacebookConfirmationViewModel by viewModels {
        viewModelFactory
    }
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.getEnvironment()?.let { env ->
            viewModelFactory = FacebookConfirmationViewModel.Factory(env)
        }

        binding = FacebookConfirmationLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        binding.signUpWithFacebookToolbar.loginToolbar.title =
            getString(R.string.facebook_confirmation_navbar_title)

        viewModel.outputs.prefillEmail()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { prefillEmail(it) }
            .addToDisposable(disposables)

        viewModel.outputs.signupSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onSuccess() }
            .addToDisposable(disposables)

        viewModel.outputs.sendNewslettersIsChecked()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                SwitchCompatUtils.setCheckedWithoutAnimation(
                    binding.newsletterSwitch,
                    it
                )
            }
            .addToDisposable(disposables)

        viewModel.outputs.signupError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, getString(R.string.signup_error_title), it) }
            .addToDisposable(disposables)

        binding.newsletterSwitch.setOnClickListener {
            viewModel.inputs.sendNewslettersClick(binding.newsletterSwitch.isChecked)
        }

        binding.createNewAccountButton.setOnClickListener {
            createNewAccountClick()
        }

        binding.loginButton.setOnClickListener {
            loginWithEmailClick()
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun createNewAccountClick() {
        viewModel.inputs.createNewAccountClick()
    }

    private fun loginWithEmailClick() {
        val intent = Intent(this, LoginToutActivity::class.java)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
    }

    private fun onSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    private fun prefillEmail(email: String) {
        binding.email.text = email
    }
}
