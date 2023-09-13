package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.FeaturedSearchResultViewBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deadlineCountdownDetail
import com.kickstarter.models.Project
import com.kickstarter.viewmodels.ProjectSearchResultHolderViewModel
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class FeaturedSearchResultViewHolder(
    private val binding: FeaturedSearchResultViewBinding,
    protected val delegate: ProjectSearchResultViewHolder.Delegate
) : KSViewHolder(binding.root) {

    private val ksString = requireNotNull(environment().ksString())
    private val viewModel = ProjectSearchResultHolderViewModel.ProjectSearchResultHolderViewModel(environment())
    private val disposables = CompositeDisposable()

    init {
        viewModel.outputs.deadlineCountdownValueTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.searchResultDeadlineCountdownTextView.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.notifyDelegateOfResultClick()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.delegate.projectSearchResultClick(this, it) }
            .addToDisposable(disposables)

        viewModel.outputs.percentFundedTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.searchResultPercentFundedTextView.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectForDeadlineCountdownUnitTextView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.searchResultDeadlineUnitTextView.text = it.deadlineCountdownDetail(context(), ksString) }
            .addToDisposable(disposables)

        viewModel.outputs.projectNameTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.projectNameTextView.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectPhotoUrl()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setProjectImageUrl(it) }
            .addToDisposable(disposables)

        binding.searchResultFundedTextView.setText(R.string.discovery_baseball_card_stats_funded)

        viewModel.outputs.displayPrelaunchProjectBadge()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.searchResultGroup.isGone = it
                binding.searchResultComingSoon.isVisible = it
            }.addToDisposable(disposables)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectAndIsFeatured = requireNotNull(data as Pair<Project, Boolean>?)
        viewModel.inputs.configureWith(projectAndIsFeatured)
    }

    private fun setProjectImageUrl(imageUrl: String) {
        Picasso.get().load(imageUrl).into(binding.projectImageView)
    }

    override fun onClick(view: View) {
        viewModel.inputs.projectClicked()
    }
}
