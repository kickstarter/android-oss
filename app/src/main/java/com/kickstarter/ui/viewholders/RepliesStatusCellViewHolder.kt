package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemShowMoreRepliesBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.viewmodels.RepliesStatusCellViewHolderViewModel

@Suppress("UNCHECKED_CAST")
class RepliesStatusCellViewHolder(
    val binding: ItemShowMoreRepliesBinding,
    private val viewListener: ViewListener
) : KSViewHolder(binding.root) {

    interface ViewListener {
        fun loadMoreCallback()
        fun retryCallback()
    }

    private val vm: RepliesStatusCellViewHolderViewModel.ViewModel = RepliesStatusCellViewHolderViewModel.ViewModel(environment())

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
        if (data is RepliesStatusCellType) {
            this.vm.inputs.configureWith(data)
        }
    }
}

enum class RepliesStatusCellType {
    PAGINATION_ERROR,
    VIEW_MORE,
    INITIAL_ERROR
}
