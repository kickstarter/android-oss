package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.BackingAddOnViewHolderViewModel

class BackingAddOnViewHolder (private val view: View) : KSViewHolder(view) {

    private var viewModel = BackingAddOnViewHolderViewModel.ViewModel(environment())

    init {
    }

    override fun bindData(data: Any?) {
        if (data is (Pair<*, *>)) {
            if (data.second is Reward) {
                bindData(data as Pair<ProjectData, Reward>)
            }
        }
    }
}