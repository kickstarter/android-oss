package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Category;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class ThanksCategoryViewHolder extends KSViewHolder {
  private Category category;
  private final Context context;
  private final Delegate delegate;
  private final KSString ksString;

  protected @Bind(R.id.thanks_category_view) CardView cardView;
  protected @Bind(R.id.explore_text_view) TextView exploreTextView;
  protected @Bind(R.id.live_projects_text_view) TextView liveProjectsTextView;

  protected @BindString(R.string.category_promo_explore_category) String exploreCategoryString;
  protected @BindString(R.string.category_promo_project_count_live_projects) String countLiveProjectsString;

  public interface Delegate {
    void categoryClick(ThanksCategoryViewHolder viewHolder, Category category);
  }

  public ThanksCategoryViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.context = view.getContext();
    this.ksString = environment().ksString();
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    this.category = requireNonNull((Category) data, Category.class);
  }

  public void onBind() {
    this.cardView.setCardBackgroundColor(this.category.colorWithAlpha());
    final @ColorInt int categoryTextColor = this.category.overlayTextColor(this.context);
    this.exploreTextView.setTextColor(categoryTextColor);
    this.exploreTextView.setText(this.ksString.format(this.exploreCategoryString, "category_name", this.category.name()));

    final Integer projectsCount = this.category.projectsCount();
    if (projectsCount != null) {
      this.liveProjectsTextView.setVisibility(View.VISIBLE);
      this.liveProjectsTextView.setText(this.ksString.format(
        this.countLiveProjectsString,
        "project_count",
        NumberUtils.format(projectsCount)
      ));
    } else {
      this.liveProjectsTextView.setVisibility(View.GONE);
    }

    this.liveProjectsTextView.setTextColor(categoryTextColor);
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.delegate.categoryClick(this, this.category);
  }
}
