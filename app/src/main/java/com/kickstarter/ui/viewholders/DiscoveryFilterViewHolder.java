package com.kickstarter.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Category;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

public class DiscoveryFilterViewHolder extends RecyclerView.ViewHolder {
  protected View view;
  @Bind(R.id.filter_text_view) TextView filterTextView;
  @Bind(R.id.vertical_line_group) View verticalLineGroup;
  @Bind(R.id.vertical_line_view_thick) View verticalLineView;
  @BindColor(R.color.white) int whiteColor;

  public DiscoveryFilterViewHolder(final View view) {
    super(view);
    this.view = view;
    ButterKnife.bind(this, view);
  }

  public void onBind(final Category category) {
    filterTextView.setText(category.name());
    verticalLineView.setBackgroundColor(whiteColor);
  }
}
