package com.kickstarter.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;

import com.kickstarter.R;
import com.kickstarter.ui.activities.SearchActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchToolbar extends Toolbar {
  public SearchToolbar(@NonNull final Context context) {
    super(context);
  }

  public SearchToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public SearchToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    if (isInEditMode()) {
      return;
    }

    ButterKnife.bind(this);
  }

  @OnClick(R.id.back_button)
  public void backButtonClick() {
    ((SearchActivity) getContext()).onBackPressed();
  }
}
