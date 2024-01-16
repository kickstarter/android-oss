package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemShowMoreRepliesBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.viewmodels.RepliesStatusCellViewHolderViewModel
import io.reactivex.disposables.CompositeDisposable

class RepliesStatusCellViewHolder(
    val binding: ItemShowMoreRepliesBinding,
    private val viewListener: ViewListener
) : KSViewHolder(binding.root) {

    interface ViewListener {
        fun loadMoreCallback()
        fun retryCallback()
    }

    private val vm: RepliesStatusCellViewHolderViewModel.ViewModel = RepliesStatusCellViewHolderViewModel.ViewModel()
    private val disposables = CompositeDisposable()

    init {
        this.vm.outputs.isViewMoreRepliesPaginationVisible()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.viewMorePaginationButton.visibility = it.toVisibility()
            }.addToDisposable(disposables)

        binding.viewMorePaginationButton.setOnClickListener {
            viewListener.loadMoreCallback()
        }

        this.vm.outputs.isErrorPaginationVisible()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.errorPaginationRetryButtonGroup.visibility = it.toVisibility()
            }.addToDisposable(disposables)

        binding.retryButton.setOnClickListener {
            viewListener.retryCallback()
        }
        binding.retryIcon.setOnClickListener {
            viewListener.retryCallback()
        }
    }

    override fun bindData(data: Any?) {
        if (data is RepliesStatusCellType) {
            this.vm.inputs.configureWith(data)
        }
    }

    override fun destroy() {
        disposables.clear()
        vm.clear()
        super.destroy()
    }
}

enum class RepliesStatusCellType {
    PAGINATION_ERROR,
    VIEW_MORE,
    INITIAL_ERROR,
    EMTPY
}
