
package com.kickstarter.ui.viewholders;

import android.view.View;
import android.widget.Button;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.Category;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class ThanksCategoryViewHolder extends KSViewHolder {
  private final ThanksCategoryHolderViewModel.ViewModel viewModel;
  private final Delegate delegate;
  private final KSString ksString;

  protected @Bind(R.id.thanks_explore_category_button) Button exploreCategoryButton;
  protected @BindString(R.string.category_promo_explore_category) String exploreCategoryString;

  public interface Delegate {
    void categoryViewHolderClicked(Category category);
  }

  public ThanksCategoryViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.viewModel = new ThanksCategoryHolderViewModel.ViewModel(environment());
    this.delegate = delegate;
    this.ksString = environment().ksString();
    ButterKnife.bind(this, view);

    this.viewModel.getOutputs().categoryName()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setCategoryButtonText);

    this.viewModel.getOutputs().notifyDelegateOfCategoryClick()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.delegate::categoryViewHolderClicked);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Category category = requireNonNull((Category) data, Category.class);
    this.viewModel.getInputs().configureWith(category);
  }

  private void setCategoryButtonText(final @NonNull String categoryName) {
    this.exploreCategoryButton.setText(
      this.ksString.format(this.exploreCategoryString, "category_name", categoryName)
    );
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.viewModel.getInputs().categoryViewClicked();
  }
}
