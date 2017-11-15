package com.kickstarter.ui.viewholders

import android.view.View
import android.widget.Button
import butterknife.Bind
import butterknife.BindString
import butterknife.ButterKnife
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.models.Category

class ThanksCategoryViewHolder(view: View, private val delegate: Delegate) : KSViewHolder(view) {
  private val viewModel: ThanksCategoryHolderViewModel.ViewModel = ThanksCategoryHolderViewModel.ViewModel(environment())
  private val ksString: KSString = environment().ksString()

  @Bind(R.id.thanks_explore_category_button)
  lateinit var exploreCategoryButton: Button

  @BindString(R.string.category_promo_explore_category)
  lateinit var exploreCategoryString: String

  interface Delegate {
    fun categoryViewHolderClicked(category: Category)
  }

  init {
    ButterKnife.bind(this, view)

    this.viewModel.outputs.categoryName()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe { this.setCategoryButtonText(it) }

    this.viewModel.outputs.notifyDelegateOfCategoryClick()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe { this.delegate.categoryViewHolderClicked(it) }
  }

  override fun bindData(data: Any?) {
    val category = requireNonNull(data as Category?, Category::class.java)
    this.viewModel.inputs.configureWith(category)
  }

  private fun setCategoryButtonText(categoryName: String) {
    this.exploreCategoryButton.text = this.ksString.format(this.exploreCategoryString, "category_name", categoryName)
  }

  override fun onClick(view: View) {
    this.viewModel.inputs.categoryViewClicked()
  }
}
