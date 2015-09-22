package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Category;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CategoryPromoViewHolder extends KsrViewHolder {
  private Category category;
  final Delegate delegate;
  @Bind(R.id.card_view) CardView cardView;
  @Bind(R.id.explore_text_view) TextView exploreTextView;
  @Bind(R.id.live_projects_text_view) TextView liveProjectsTextView;

  public interface Delegate{
    void categoryPromoClick(final CategoryPromoViewHolder viewHolder, final Category category);
  }

  public CategoryPromoViewHolder(final View view, final CategoryPromoViewHolder.Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ButterKnife.bind(this, view);
  }

  public void onBind(final Object datum) {
    category = (Category) datum;

    final Context context = view.getContext();

    cardView.setCardBackgroundColor(category.color());
    final int categoryTextColor = category.overlayTextColor(context);
    exploreTextView.setTextColor(categoryTextColor);
    exploreTextView.setText(context.getString(R.string.Explore_Category, category.name()));
    liveProjectsTextView.setText(context.getString(R.string.Number_live_projects, category.projectsCount()));
    liveProjectsTextView.setTextColor(categoryTextColor);
  }

  @Override
  public void onClick(final View view) {
    delegate.categoryPromoClick(this, category);
  }
}
