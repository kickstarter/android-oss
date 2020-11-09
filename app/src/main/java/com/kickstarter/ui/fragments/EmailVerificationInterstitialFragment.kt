package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.viewmodels.EmailVerificationInterstitialFragmentViewModel

@RequiresFragmentViewModel(EmailVerificationInterstitialFragmentViewModel.ViewModel::class)
class EmailVerificationInterstitialFragment : BaseFragment<EmailVerificationInterstitialFragmentViewModel.ViewModel>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_email_verification_interstitial, container, false)
    }

    companion object {
        fun newInstance(): EmailVerificationInterstitialFragment {
            return EmailVerificationInterstitialFragment()
        }
    }
}