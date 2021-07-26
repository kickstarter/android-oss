package com.kickstarter.ui.viewholders

import androidx.constraintlayout.widget.Constraints
import com.kickstarter.R
import com.kickstarter.databinding.ItemErrorPaginationBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.viewmodels.PaginationErrorViewHolderViewModel

@Suppress("UNCHECKED_CAST")
class PaginationErrorViewHolder(
    val binding: ItemErrorPaginationBinding,
    private val viewListener: ViewListener,
    val isReply: Boolean = false
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
                binding.errorPaginationRetryButtonGroup.visibility = it.toVisibility()
            }

        binding.retryButton.setOnClickListener {
            viewListener.retryCallback()
        }
        binding.retryIcon.setOnClickListener {
            viewListener.retryCallback()
        }

        if (isReply) {
            val params = Constraints.LayoutParams(
                Constraints.LayoutParams.MATCH_PARENT,
                Constraints.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(context().resources.getDimension(R.dimen.grid_5).toInt(), 0, 0, 0)
            binding.paginationErrorCell.layoutParams = params
        }
    }

    override fun bindData(data: Any?) {
        if (data is Boolean) {
            this.vm.inputs.configureWith(data)
        }
    }
}
