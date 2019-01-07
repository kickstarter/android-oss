package com.kickstarter.ui.viewholders;

import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class PopularSearchTitleViewHolder extends KSViewHolder {
  @Bind(R.id.heading) TextView termTextView;
  @BindString(R.string.Popular_Projects) String popularProjectsString;

  public PopularSearchTitleViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
    this.termTextView.setText(this.popularProjectsString);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    // no data to bind, this ViewHolder is just a static title
  }
}
