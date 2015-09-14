package com.kickstarter.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Category;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiscoveryFilterViewHolder extends RecyclerView.ViewHolder {
  protected View view;
  @Bind(R.id.category_name_text_view) TextView categoryNameTextView;

  public DiscoveryFilterViewHolder(final View view) {
    super(view);
    this.view = view;
    ButterKnife.bind(this, view);
  }

  public void onBind(final Category category) {
    categoryNameTextView.setText(category.name());
  }
}
