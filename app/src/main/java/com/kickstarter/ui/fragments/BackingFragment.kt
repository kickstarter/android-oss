package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.models.Project
import com.kickstarter.viewmodels.BackingFragmentViewModel

@RequiresFragmentViewModel(BackingFragmentViewModel.ViewModel::class)
class BackingFragment: BaseFragment<BackingFragmentViewModel.ViewModel>()  {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_backing, container, false)
    }

    fun takeProject(project: Project) {
        this.viewModel.inputs.project(project)
    }

}
