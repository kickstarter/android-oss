package com.kickstarter.ui.fragments

import android.os.Bundle
import android.text.SpannableString
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.databinding.FragmentCancelPledgeBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUIV2
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.extensions.snackbar
import com.kickstarter.ui.extensions.text
import com.kickstarter.viewmodels.CancelPledgeViewModel.CancelPledgeViewModel
import com.kickstarter.viewmodels.CancelPledgeViewModel.Factory
import io.reactivex.disposables.CompositeDisposable

class CancelPledgeFragment : Fragment() {

    interface CancelPledgeDelegate {
        fun pledgeSuccessfullyCancelled()
    }

    private var binding: FragmentCancelPledgeBinding? = null

    private lateinit var viewModelFactory: Factory
    private val viewModel: CancelPledgeViewModel by viewModels {
        viewModelFactory
    }
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentCancelPledgeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env, bundle = arguments)
        }

        this.viewModel.outputs.pledgeAmountAndProjectName()
            .compose(observeForUIV2())
            .subscribe { setPromptText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showServerError()
            .compose(observeForUIV2())
            .subscribe { binding?.cancelPledgeRoot?.let { view -> snackbar(view, getString(R.string.Something_went_wrong_please_try_again)).show() } }
            .addToDisposable(disposables)

        this.viewModel.outputs.showCancelError()
            .compose(observeForUIV2())
            .subscribe { binding?.cancelPledgeRoot?.let { view -> snackbar(view, it).show() } }
            .addToDisposable(disposables)

        this.viewModel.outputs.dismiss()
            .compose(observeForUIV2())
            .subscribe { dismiss() }
            .addToDisposable(disposables)

        this.viewModel.outputs.success()
            .compose(observeForUIV2())
            .subscribe { (context as CancelPledgeDelegate?)?.pledgeSuccessfullyCancelled() }
            .addToDisposable(disposables)

        this.viewModel.outputs.progressBarIsVisible()
            .compose(observeForUIV2())
            .subscribe { binding?.progressBar?.let { view -> ViewUtils.setGone(view, !it) } }
            .addToDisposable(disposables)

        this.viewModel.outputs.cancelButtonIsVisible()
            .compose(observeForUIV2())
            .subscribe { binding?.yesCancelPledgeButton?.let { view -> ViewUtils.setGone(view, !it) } }
            .addToDisposable(disposables)

        binding?.yesCancelPledgeButton?.setOnClickListener {
            binding?.cancellationNote?.text()?.let { text ->
                this.viewModel.inputs.confirmCancellationClicked(
                    text
                )
            }
        }

        binding?.noCancelPledgeButton ?.setOnClickListener {
            this.viewModel.inputs.goBackButtonClicked()
        }
    }

    private fun dismiss() {
        parentFragmentManager?.popBackStack(CancelPledgeFragment::class.java.simpleName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun setPromptText(amountAndProjectName: Pair<String, String>) {
        val ksString = requireNotNull((activity?.applicationContext as? KSApplication)?.component()?.environment()?.ksString())
        val amount = amountAndProjectName.first
        val name = amountAndProjectName.second
        val formattedString = ksString.format(
            getString(R.string.Are_you_sure_you_wish_to_cancel_your_amount_pledge_to_project_name),
            "amount", amount, "project_name", name
        )

        val spannableString = SpannableString(formattedString)

        ViewUtils.addBoldSpan(spannableString, amount)
        ViewUtils.addBoldSpan(spannableString, name)

        binding?.cancelPrompt?.text = spannableString
    }

    companion object {

        fun newInstance(project: Project): CancelPledgeFragment {
            val fragment = CancelPledgeFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.CANCEL_PLEDGE_PROJECT, project)
            fragment.arguments = argument
            return fragment
        }
    }
}
