package com.kickstarter.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kickstarter.R
import com.kickstarter.databinding.FragmentCheckoutRiskMessageBinding
import com.kickstarter.libs.BaseBottomSheetDialogFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.viewmodels.CheckoutRiskMessageFragmentViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresFragmentViewModel(CheckoutRiskMessageFragmentViewModel.ViewModel::class)
class CheckoutRiskMessageFragment : BaseBottomSheetDialogFragment <CheckoutRiskMessageFragmentViewModel.ViewModel>() {
    companion object {

        fun newInstance(): CheckoutRiskMessageFragment =
            CheckoutRiskMessageFragment()
    }

    private var binding: FragmentCheckoutRiskMessageBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogStyle)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentCheckoutRiskMessageBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val resources = resources

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val parent = view.parent as View
            parent.setPadding(
                resources.getDimensionPixelSize(R.dimen.grid_15_half), // LEFT 16dp
                0,
                resources.getDimensionPixelSize(R.dimen.grid_15_half), // RIGHT 16dp
                0
            )
            val layoutParams = parent.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            parent.layoutParams = layoutParams
        }

        binding?.learnMoreAboutAccountabilityTv?.setOnClickListener {
            this.viewModel.inputs.onLearnMoreAboutAccountabilityLinkClicked()
        }

        viewModel.outputs.openLearnMoreAboutAccountabilityLink()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                context?.let { context ->
                    ApplicationUtils.openUrlExternally(context, it)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        // this forces the sheet to appear at max height even on landscape
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}
