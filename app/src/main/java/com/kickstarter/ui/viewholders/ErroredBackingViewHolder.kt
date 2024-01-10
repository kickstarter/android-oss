package com.kickstarter.ui.viewholders

import com.kickstarter.R
import com.kickstarter.databinding.ItemErroredBackingBinding
import com.kickstarter.libs.RelativeDateTimeOptions
import com.kickstarter.libs.rx.transformers.Transformers.observeForUIV2
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.ErroredBacking
import com.kickstarter.viewmodels.ErroredBackingViewHolderViewModel
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime

class ErroredBackingViewHolder(private val binding: ItemErroredBackingBinding, val delegate: Delegate?) : KSViewHolder(binding.root) {

    interface Delegate {
        fun managePledgeClicked(projectSlug: String)
    }

    private val ksString = requireNotNull(environment().ksString())
    private var viewModel = ErroredBackingViewHolderViewModel.ViewModel()
    private val disposables = CompositeDisposable()

    init {
        this.viewModel.outputs.notifyDelegateToStartFixPaymentMethod()
            .compose(observeForUIV2())
            .subscribe { delegate?.managePledgeClicked(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.projectFinalCollectionDate()
            .compose(observeForUIV2())
            .subscribe { setProjectFinaCollectionDateText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.projectName()
            .compose(observeForUIV2())
            .subscribe { binding.erroredBackingProjectTitle.text = it }
            .addToDisposable(disposables)

        binding.erroredBackingManageButton.setOnClickListener {
            this.viewModel.inputs.manageButtonClicked()
        }
    }

    private fun setProjectFinaCollectionDateText(finalCollectionDate: DateTime) {
        val options = RelativeDateTimeOptions.builder()
            .absolute(true)
            .relativeToDateTime(DateTime.now())
            .build()

        val timeRemaining = DateTimeUtils.relative(context(), this.ksString, finalCollectionDate, options)
        val fixWithinTemplate = context().getString(R.string.Fix_within_time_remaining)
        binding.erroredBackingProjectCollectionDate.text = this.ksString.format(
            fixWithinTemplate,
            "time_remaining", timeRemaining
        )
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val erroredBacking = requireNotNull(data as ErroredBacking)

        this.viewModel.inputs.configureWith(erroredBacking)
    }

    override fun destroy() {
        disposables.clear()
        viewModel.clear()
        super.destroy()
    }
}
