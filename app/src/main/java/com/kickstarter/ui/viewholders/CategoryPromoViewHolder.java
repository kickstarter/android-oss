package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSColorUtils;
import com.kickstarter.models.Category;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

public class CategoryPromoViewHolder extends KsrViewHolder {
  private Category category;
  private final Delegate delegate;
  @Bind(R.id.card_view) CardView cardView;
  @Bind(R.id.explore_text_view) TextView exploreTextView;
  @Bind(R.id.live_projects_text_view) TextView liveProjectsTextView;

  public interface Delegate {
    void categoryPromoClick(@NonNull final CategoryPromoViewHolder viewHolder, @NonNull final Category category);
  }

  public CategoryPromoViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    category = (Category) datum;

    final Context context = view.getContext();

    cardView.setCardBackgroundColor(category.colorWithAlpha());
    final @ColorInt int categoryTextColor = category.overlayColor(context);
    exploreTextView.setTextColor(categoryTextColor);
    exploreTextView.setText(context.getString(R.string.Explore_Category, category.name()));
    liveProjectsTextView.setText(context.getString(R.string.Number_live_projects, category.projectsCount()));
    liveProjectsTextView.setTextColor(categoryTextColor);
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.categoryPromoClick(this, category);
  }
}
