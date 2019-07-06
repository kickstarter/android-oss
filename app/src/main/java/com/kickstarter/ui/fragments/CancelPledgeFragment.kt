package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.extensions.text
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.activities.ProjectActivity
import com.kickstarter.viewmodels.CancelPledgeFragmentViewModel
import kotlinx.android.synthetic.main.fragment_cancel_pledge.*

@RequiresFragmentViewModel(CancelPledgeFragmentViewModel.ViewModel::class)
class CancelPledgeFragment : BaseFragment<CancelPledgeFragmentViewModel.ViewModel>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_cancel_pledge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel.outputs.pledgeAmountAndProjectName()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { setPromptText(it) }

        this.viewModel.outputs.showError()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { Snackbar.make(cancel_pledge_root, R.string.Something_went_wrong_please_try_again, Snackbar.LENGTH_SHORT).show() }

        this.viewModel.outputs.notifyProjectActivityOfSuccess()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { (activity as ProjectActivity?)?.showCancellationSuccess() }

        confirm_cancel_pledge_button.setOnClickListener {
            this.viewModel.inputs.confirmCancellationClicked(cancellation_note.text())
        }
    }

    private fun setPromptText(amountAndProjectName: Pair<String, String>) {
        val ksString = (activity?.applicationContext as KSApplication).component().environment().ksString()
        val amount = amountAndProjectName.first
        val name = amountAndProjectName.second
        cancel_prompt.text = ksString.format(getString(R.string.Are_you_sure_you_wish_to_cancel_your_amount_pledge_to_project_name),
                "amount", amount, "project_name", name)
    }

    companion object {

        fun newInstance(project: Project, backing: Backing): CancelPledgeFragment {
            val fragment = CancelPledgeFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.CANCEL_PLEDGE_PROJECT, project)
            argument.putParcelable(ArgumentsKey.CANCEL_PLEDGE_BACKING, backing)
            fragment.arguments = argument
            return fragment
        }
    }
}