package com.kickstarter.ui.fragments

import android.os.Bundle
import android.text.SpannableString
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.extensions.snackbar
import com.kickstarter.ui.extensions.text
import com.kickstarter.viewmodels.CancelPledgeViewModel
import kotlinx.android.synthetic.main.fragment_cancel_pledge.*

@RequiresFragmentViewModel(CancelPledgeViewModel.ViewModel::class)
class CancelPledgeFragment : BaseFragment<CancelPledgeViewModel.ViewModel>() {

    interface CancelPledgeDelegate {
        fun pledgeSuccessfullyCancelled()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_cancel_pledge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel.outputs.pledgeAmountAndProjectName()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setPromptText(it) }

        this.viewModel.outputs.showServerError()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { cancel_pledge_root?.let { view -> snackbar(view, getString(R.string.Something_went_wrong_please_try_again)).show() } }

        this.viewModel.outputs.showCancelError()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { cancel_pledge_root?.let { view -> snackbar(view, it).show() } }

        this.viewModel.outputs.dismiss()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { dismiss() }

        this.viewModel.outputs.success()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { (context as CancelPledgeDelegate?)?.pledgeSuccessfullyCancelled() }

        this.viewModel.outputs.progressBarIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { progress_bar?.let { view -> ViewUtils.setGone(view, !it) } }

        this.viewModel.outputs.cancelButtonIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { yes_cancel_pledge_button?.let { view -> ViewUtils.setGone(view, !it) } }

        yes_cancel_pledge_button.setOnClickListener {
            this.viewModel.inputs.confirmCancellationClicked(cancellation_note.text())
        }

        no_cancel_pledge_button.setOnClickListener {
            this.viewModel.inputs.goBackButtonClicked()
        }
    }

    private fun dismiss() {
        fragmentManager?.popBackStack(CancelPledgeFragment::class.java.simpleName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun setPromptText(amountAndProjectName: Pair<String, String>) {
        val ksString = (activity?.applicationContext as KSApplication).component().environment().ksString()
        val amount = amountAndProjectName.first
        val name = amountAndProjectName.second
        val formattedString = ksString.format(
            getString(R.string.Are_you_sure_you_wish_to_cancel_your_amount_pledge_to_project_name),
            "amount", amount, "project_name", name
        )

        val spannableString = SpannableString(formattedString)

        ViewUtils.addBoldSpan(spannableString, amount)
        ViewUtils.addBoldSpan(spannableString, name)

        cancel_prompt?.text = spannableString
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
