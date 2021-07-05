package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemShowMoreRepliesBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.viewmodels.PaginationViewMoreViewHolderViewModel

@Suppress("UNCHECKED_CAST")
class PaginationViewMoreRepliesViewHolder(
    val binding: ItemShowMoreRepliesBinding,
    private val viewListener: ViewListener
) : KSViewHolder(binding.root) {

    interface ViewListener {
        fun loadMoreCallback()
    }

    private val vm: PaginationViewMoreViewHolderViewModel.ViewModel = PaginationViewMoreViewHolderViewModel.ViewModel(environment())

    init {
        this.vm.outputs.isViewMoreRepliesPaginationVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.viewMorePaginationButton.visibility = it.toVisibility()
            }

        binding.viewMorePaginationButton.setOnClickListener {
            viewListener.loadMoreCallback()
        }
    }

    override fun bindData(data: Any?) {
        if (data is Boolean) {
            this.vm.inputs.configureWith(data)
        }
    }
}
