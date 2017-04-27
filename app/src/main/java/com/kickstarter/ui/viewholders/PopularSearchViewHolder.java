package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class PopularSearchViewHolder extends KSViewHolder {

  @Bind(R.id.search_term_text_view) TextView termTextView;
  @BindString(R.string.search_most_popular) String mostPopularString;


  @BindString(R.string.search_stats) String searchStatsString;

  protected @Inject KSString ksString;


  public PopularSearchViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    termTextView.setText(mostPopularString);
  }
}

