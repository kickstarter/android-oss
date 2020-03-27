package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.models.ErroredBacking
import com.kickstarter.viewmodels.ErroredBackingViewHolderViewModel

class ErroredBackingViewHolder(private val view: View, val delegate: Delegate?) : KSViewHolder(view) {

    interface Delegate {
        fun managePledgeClicked(projectSlug: String)
    }

    private val ksString = environment().ksString()
    private var viewModel = ErroredBackingViewHolderViewModel.ViewModel(environment())


    init {


    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val erroredBacking = requireNonNull(data as ErroredBacking)

        this.viewModel.inputs.configureWith(erroredBacking)
    }

}
