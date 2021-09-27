package com.kickstarter.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kickstarter.R
import com.kickstarter.databinding.FragmentCheckoutRiskMessageBinding
import com.kickstarter.libs.BaseBottomSheetDialogFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.parseHtmlTag
import com.kickstarter.ui.extensions.makeLinks
import com.kickstarter.ui.extensions.parseHtmlTag
import com.kickstarter.viewmodels.CheckoutRiskMessageFragmentViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresFragmentViewModel(CheckoutRiskMessageFragmentViewModel.ViewModel::class)
class CheckoutRiskMessageFragment : BaseBottomSheetDialogFragment <CheckoutRiskMessageFragmentViewModel.ViewModel>() {

    interface Delegate {
        fun onDialogConfirmButtonClicked()
    }

    companion object {
        fun newInstance(delegate: Delegate):
            CheckoutRiskMessageFragment {
                val fragment = CheckoutRiskMessageFragment().apply {
                    this.delegate = delegate
                }
                return fragment
            }
    }

    private var binding: FragmentCheckoutRiskMessageBinding? = null
    private var delegate: Delegate? = null

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

        binding?.confirm?.setOnClickListener {
            delegate?.onDialogConfirmButtonClicked()
            dismissDialog()
        }

        viewModel.outputs.openLearnMoreAboutAccountabilityLink()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                context?.let { context ->
                    ApplicationUtils.openUrlExternally(context, it)
                }
            }

        (dialog as? BottomSheetDialog)?.let {
            it.findViewById<FrameLayout>(
                com.google.android.material.R.id
                    .design_bottom_sheet
            )?.let { bottomSheet ->
                val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
                bottomSheetBehavior.addBottomSheetCallback(object :
                        BottomSheetBehavior.BottomSheetCallback() {
                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        }

                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState ==
                                BottomSheetBehavior.STATE_HIDDEN
                            ) {
                                dismissDialog()
                            }
                        }
                    })
            }
        }

        binding?.learnMoreAboutAccountabilityTv?.parseHtmlTag()
        context?.resources?.getString(R.string.Learn_more_about_accountability)?.let {
            val args = Pair(
              it,
                View.OnClickListener {
                    this.viewModel.inputs.onLearnMoreAboutAccountabilityLinkClicked()
                },
            )
            binding?.learnMoreAboutAccountabilityTv?.makeLinks(args,
                linkColor = R.color.text_primary,
                isUnderlineText = true
            )
        }
    }

    fun dismissDialog() {
        dismiss()
    }
    override fun onStart() {
        super.onStart()
        // this forces the sheet to appear at max height even on landscape
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}
