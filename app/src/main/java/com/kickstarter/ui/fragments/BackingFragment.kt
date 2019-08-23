package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.R
import com.kickstarter.extensions.showSnackbar
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.Project
import com.kickstarter.viewmodels.BackingFragmentViewModel
import kotlinx.android.synthetic.main.fragment_backing.*

@RequiresFragmentViewModel(BackingFragmentViewModel.ViewModel::class)
class BackingFragment: BaseFragment<BackingFragmentViewModel.ViewModel>()  {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_backing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel.outputs.showUpdatePledgeSuccess()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { showSnackbar(backing_root, getString(R.string.Got_it_your_changes_have_been_saved)) }
    }

    fun takeProject(project: Project) {
        this.viewModel.inputs.project(project)
    }

    fun pledgeSuccessfullyCancelled() {
        this.viewModel.inputs.pledgeSuccessfullyUpdated()
    }

}
