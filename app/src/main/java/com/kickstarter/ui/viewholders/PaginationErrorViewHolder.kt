package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemErrorPaginationBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.viewmodels.PaginationErrorViewHolderViewModel

@Suppress("UNCHECKED_CAST")
class PaginationErrorViewHolder(
    val binding: ItemErrorPaginationBinding,
    private val viewListener: ViewListener
) : KSViewHolder(binding.root) {

    interface ViewListener {
        fun retryCallback()
    }

    private val vm: PaginationErrorViewHolderViewModel.ViewModel = PaginationErrorViewHolderViewModel.ViewModel(environment())

    init {
        this.vm.outputs.isErrorPaginationVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.errorPaginationRetryButton.visibility = it.toVisibility()
            }

        binding.errorPaginationRetryButton.setOnClickListener {
            viewListener.retryCallback()
        }
    }

    override fun bindData(data: Any?) {
        if (data is Boolean) {
            this.vm.inputs.configureWith(data)
        }
    }
}
