package com.kickstarter.ui.viewholders

import android.view.View
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.libs.RelativeDateTimeOptions
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.models.ErroredBacking
import com.kickstarter.viewmodels.ErroredBackingViewHolderViewModel
import kotlinx.android.synthetic.main.item_errored_backing.view.*
import org.joda.time.DateTime

class ErroredBackingViewHolder(private val view: View, val delegate: Delegate?) : KSViewHolder(view) {

    interface Delegate {
        fun managePledgeClicked(projectSlug: String)
    }

    private val ksString = environment().ksString()
    private var viewModel = ErroredBackingViewHolderViewModel.ViewModel(environment())

    init {

        this.viewModel.outputs.notifyDelegateToStartFixPaymentMethod()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { delegate?.managePledgeClicked(it) }

        this.viewModel.outputs.projectFinalCollectionDate()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setProjectFinaCollectionDateText(it) }

        this.viewModel.outputs.projectName()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.view.errored_backing_project_title.text = it }

        RxView.clicks(this.view.errored_backing_manage_button)
                .compose(bindToLifecycle())
                .subscribe { this.viewModel.inputs.manageButtonClicked() }

    }

    private fun setProjectFinaCollectionDateText(finalCollectionDate: DateTime) {
        val options = RelativeDateTimeOptions.builder()
                .absolute(true)
                .relativeToDateTime(DateTime.now())
                .build()

        val timeRemaining = DateTimeUtils.relative(context(), this.ksString, finalCollectionDate, options)
        val fixWithinTemplate = context().getString(R.string.Fix_within_time_remaining)
        this.view.errored_backing_project_collection_date.text = this.ksString.format(fixWithinTemplate,
                "time_remaining", timeRemaining)
    }

    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val erroredBacking = requireNonNull(data as ErroredBacking)

        this.viewModel.inputs.configureWith(erroredBacking)
    }

}
