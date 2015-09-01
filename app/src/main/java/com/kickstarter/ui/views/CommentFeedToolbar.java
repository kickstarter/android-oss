package com.kickstarter.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUser;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CommentFeedToolbar extends Toolbar {
  @InjectView(R.id.comment_button) TextView commentButton;
  @Inject CurrentUser currentUser;

  public CommentFeedToolbar(final Context context) {
    super(context);
  }

  public CommentFeedToolbar(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public CommentFeedToolbar(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.inject(this);
    ((KsrApplication) getContext().getApplicationContext()).component().inject(this);
  }

  @Nullable
  @OnClick(R.id.comment_button)
  public void postComment() {
    // if logged in, post
  }
}
