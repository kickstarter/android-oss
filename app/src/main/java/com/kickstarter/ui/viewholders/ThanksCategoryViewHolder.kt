package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ThanksCategoryViewBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Category

class ThanksCategoryViewHolder(
    private val binding: ThanksCategoryViewBinding,
    delegate: Delegate
) : KSViewHolder(binding.root) {
    private val viewModel = ThanksCategoryHolderViewModel.ViewModel(environment())
    private val delegate: Delegate = delegate
    private val ksString: KSString = environment().ksString()

    interface Delegate {
        fun categoryViewHolderClicked(category: Category?)
    }

    init {
        viewModel.outputs.categoryName()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { categoryName: String -> setCategoryButtonText(categoryName) }
        viewModel.outputs.notifyDelegateOfCategoryClick()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { category: Category? -> this.delegate.categoryViewHolderClicked(category) }
    }
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val category = ObjectUtils.requireNonNull(data as Category?, Category::class.java)
        viewModel.inputs.configureWith(category)
    }

    private fun setCategoryButtonText(categoryName: String) {
        binding.thanksExploreCategoryButton.text = ksString.format(context().getString(R.string.category_promo_explore_category), "category_name", categoryName)
    }

    override fun onClick(view: View) {
        viewModel.inputs.categoryViewClicked()
    }
}
