package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ThanksCategoryViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Category
import io.reactivex.disposables.CompositeDisposable

class ThanksCategoryViewHolder(
    private val binding: ThanksCategoryViewBinding,
    delegate: Delegate
) : KSViewHolder(binding.root) {
    private val viewModel = ThanksCategoryHolderViewModel.ViewModel(environment())
    private val delegate: Delegate = delegate
    private val ksString = requireNotNull(environment().ksString())
    private val disposables = CompositeDisposable()

    interface Delegate {
        fun categoryViewHolderClicked(category: Category)
    }

    init {
        viewModel.outputs.categoryName()
            .compose(Transformers.observeForUIV2())
            .subscribe { categoryName: String -> setCategoryButtonText(categoryName) }
            .addToDisposable(disposables)
        viewModel.outputs.notifyDelegateOfCategoryClick()
            .filter { it.isNotNull() }
            .compose(Transformers.observeForUIV2())
            .subscribe { category: Category -> this.delegate.categoryViewHolderClicked(category) }
            .addToDisposable(disposables)
    }
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val category = requireNotNull(data as Category?) { Category::class.java.toString() + " required to be non-null." }
        viewModel.inputs.configureWith(category)
    }

    private fun setCategoryButtonText(categoryName: String) {
        binding.thanksExploreCategoryButton.text = ksString.format(context().getString(R.string.category_promo_explore_category), "category_name", categoryName)
    }

    override fun onClick(view: View) {
        viewModel.inputs.categoryViewClicked()
    }

    override fun destroy() {
        disposables.clear()
        super.destroy()
    }
}
