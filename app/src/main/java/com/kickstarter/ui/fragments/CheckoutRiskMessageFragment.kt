package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.databinding.FragmentCheckoutRiskMessageBinding
import com.kickstarter.libs.BaseBottomSheetDialogFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.viewmodels.CheckoutRiskMessageFragmentViewModel

@RequiresFragmentViewModel(CheckoutRiskMessageFragmentViewModel.ViewModel::class)
class CheckoutRiskMessageFragment : BaseBottomSheetDialogFragment <CheckoutRiskMessageFragmentViewModel.ViewModel>() {
    companion object {
        fun newInstance(): CheckoutRiskMessageFragment =
            CheckoutRiskMessageFragment()
    }

    private var binding: FragmentCheckoutRiskMessageBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentCheckoutRiskMessageBinding.inflate(inflater, container, false)
        return binding?.root
    }
}
