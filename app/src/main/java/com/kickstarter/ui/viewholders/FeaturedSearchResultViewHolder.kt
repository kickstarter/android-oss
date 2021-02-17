package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.FeaturedSearchResultViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Project
import com.kickstarter.viewmodels.ProjectSearchResultHolderViewModel
import com.squareup.picasso.Picasso

class FeaturedSearchResultViewHolder(
    private val binding: FeaturedSearchResultViewBinding,
    protected val delegate: ProjectSearchResultViewHolder.Delegate
) : KSViewHolder(binding.root) {

    private val ksString = environment().ksString()
    private val viewModel = ProjectSearchResultHolderViewModel.ViewModel(environment())

    init {
        viewModel.outputs.deadlineCountdownValueTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.searchResultDeadlineCountdownTextView.text = it }
        viewModel.outputs.notifyDelegateOfResultClick()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.delegate.projectSearchResultClick(this, it) }
        viewModel.outputs.percentFundedTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.searchResultPercentFundedTextView.text = it }
        viewModel.outputs.projectForDeadlineCountdownUnitTextView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.searchResultDeadlineUnitTextView.text = ProjectUtils.deadlineCountdownDetail(it, context(), ksString) }
        viewModel.outputs.projectNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.projectNameTextView.text = it }
        viewModel.outputs.projectPhotoUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setProjectImageUrl(it) }
        binding.searchResultFundedTextView.setText(R.string.discovery_baseball_card_stats_funded)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectAndIsFeatured = ObjectUtils.requireNonNull(data as Pair<Project, Boolean>?)
        viewModel.inputs.configureWith(projectAndIsFeatured)
    }

    private fun setProjectImageUrl(imageUrl: String) {
        Picasso.get().load(imageUrl).into(binding.projectImageView)
    }

    override fun onClick(view: View) {
        viewModel.inputs.projectClicked()
    }
}
