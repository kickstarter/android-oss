package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.Category;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class ThanksCategoryViewHolder extends KSViewHolder {
  private Category category;
  private final Delegate delegate;
  private final KSString ksString;

  protected @Bind(R.id.thanks_explore_category_button) Button exploreCategoryButton;
  protected @BindString(R.string.category_promo_explore_category) String exploreCategoryString;

  public interface Delegate {
    void categoryViewHolderClicked(Category category);
  }

  public ThanksCategoryViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.ksString = environment().ksString();
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    this.category = requireNonNull((Category) data, Category.class);
  }

  public void onBind() {
    this.exploreCategoryButton.setText(
      this.ksString.format(this.exploreCategoryString, "category_name", this.category.name())
    );
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.delegate.categoryViewHolderClicked(this.category);
  }
}
