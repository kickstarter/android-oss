package com.kickstarter.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.showErrorSnackBar
import com.kickstarter.ui.extensions.showSuccessSnackBar
import com.kickstarter.viewmodels.EmailVerificationInterstitialFragmentViewModel
import kotlinx.android.synthetic.main.fragment_email_verification_interstitial.*

@RequiresFragmentViewModel(EmailVerificationInterstitialFragmentViewModel.ViewModel::class)
class EmailVerificationInterstitialFragment : BaseFragment<EmailVerificationInterstitialFragmentViewModel.ViewModel>() {

    private lateinit var callback: Callbacks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_email_verification_interstitial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.activity?.hideKeyboard()

        this.viewModel.outputs.startEmailActivity()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                startActivity(Intent.createChooser(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL), "Choose Email"))
            }

        this.viewModel.outputs.isSkipLinkShown()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                ViewUtils.setGone(email_verification_interstitial_skip, !it)
            }

        this.viewModel.outputs.dismissInterstitial()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { close() }

        this.viewModel.outputs.showSnackbarError()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                this.activity?.showErrorSnackBar(view, this.getString(it))
            }

        this.viewModel.outputs.showSnackbarSuccess()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                this.activity?.showSuccessSnackBar(view, this.getString(it))
            }

        this.viewModel.outputs.loadingIndicatorGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(email_verification_loading_indicator, it) }

        email_verification_interstitial_cta_button.setOnClickListener {
            this.viewModel.inputs.openInboxButtonPressed()
        }

        email_verification_interstitial_resend_button.setOnClickListener {
            this.viewModel.inputs.resendEmailButtonPressed()
        }

        email_verification_interstitial_skip.setOnClickListener {
            this.viewModel.inputs.skipButtonPressed()
        }
    }

    /** Configure with current [Callbacks]. */
    fun configureWithCallback(callback: Callbacks) {
        this.callback = callback
    }

    /**
     * Callback to the parent activity to finish the flow
     * with setResult(Activity.RESULT_OK)
     */
    private fun close() = callback.onDismiss()

    companion object {
        fun newInstance(): EmailVerificationInterstitialFragment {
            val fragment = EmailVerificationInterstitialFragment()
            val argument = Bundle()
            fragment.arguments = argument
            return fragment
        }
    }
}
