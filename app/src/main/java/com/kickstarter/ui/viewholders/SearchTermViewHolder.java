package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.services.DiscoveryParams;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchTermViewHolder extends KsrViewHolder {
  private DiscoveryParams params;

  @Bind(R.id.search_term_text_view) TextView searchTermTextView;

  public SearchTermViewHolder(@NonNull final View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    params = (DiscoveryParams) datum;

    final Context context = view.getContext();

    if (params.sort() == DiscoveryParams.Sort.POPULAR) {
      searchTermTextView.setText(context.getString(R.string.Most_Popular));
    } else if (params.term() != null) {
      searchTermTextView.setText(params.term());
    } else {
      searchTermTextView.setText("");
    }
  }
}

