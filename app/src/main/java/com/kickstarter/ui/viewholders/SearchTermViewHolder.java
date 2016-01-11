package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.services.DiscoveryParams;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public final class SearchTermViewHolder extends KSViewHolder {
  private DiscoveryParams params;

  protected @Bind(R.id.search_term_text_view) TextView termTextView;
  protected @Bind(R.id.search_term_view) LinearLayout layout;

  protected @BindString(R.string.search_most_popular) String mostPopularString;

  public SearchTermViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
  }

  public void onBind() {
    termTextView.setText(mostPopularString);
  }
}
