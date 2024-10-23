package com.kickstarter.ui.viewholders

import androidx.core.view.isGone
import com.kickstarter.databinding.ItemAddCardBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

enum class State {
    DEFAULT, LOADING
}

class RewardAddCardViewHolder(val binding: ItemAddCardBinding, val delegate: Delegate) : KSViewHolder(binding.root) {

    private val viewModel: AddCardViewHolderViewModel.ViewModel = AddCardViewHolderViewModel.ViewModel()
    private val disposables = CompositeDisposable()

    init {
        this.binding.addCardButton.setOnClickListener {
            this.delegate.addNewCardButtonClicked()
        }

        this.viewModel.outputs
            .setDefaultState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding.newPaymentPlusIcon.isGone = false
                this.binding.newPaymentProgress.isGone = true
            }
            .addToDisposable(disposables)

        this.viewModel.outputs
            .setLoadingState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.binding.newPaymentProgress.isGone = false
                this.binding.newPaymentPlusIcon.isGone = true
            }
            .addToDisposable(disposables)
    }

    override fun bindData(data: Any?) {
        if (data is State) {
            this.viewModel.inputs.configureWith(data)
        }
    }

    interface Delegate {
        fun addNewCardButtonClicked()
    }

    override fun destroy() {
        disposables.clear()
        viewModel.clear()
        super.destroy()
    }
}
