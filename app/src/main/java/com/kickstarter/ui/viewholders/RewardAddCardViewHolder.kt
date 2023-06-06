package com.kickstarter.ui.viewholders

import androidx.core.view.isGone
import com.kickstarter.databinding.ItemAddCardBinding
import rx.android.schedulers.AndroidSchedulers

enum class State {
    DEFAULT, LOADING
}

class RewardAddCardViewHolder(val binding: ItemAddCardBinding, val delegate: Delegate) : KSViewHolder(binding.root) {

    private val viewModel: AddCardViewHolderViewModel.ViewModel = AddCardViewHolderViewModel.ViewModel(environment())

    init {
        this.binding.addCardButton.setOnClickListener {
            this.delegate.addNewCardButtonClicked()
        }

        this.viewModel.outputs
            .setDefaultState()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding.newPaymentPlusIcon.isGone = false
                this.binding.newPaymentProgress.isGone = true
            }

        this.viewModel.outputs
            .setLoadingState()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding.newPaymentProgress.isGone = false
                this.binding.newPaymentPlusIcon.isGone = true
            }
    }

    override fun bindData(data: Any?) {
        if (data is State) {
            this.viewModel.inputs.configureWith(data)
        }
    }

    interface Delegate {
        fun addNewCardButtonClicked()
    }
}
