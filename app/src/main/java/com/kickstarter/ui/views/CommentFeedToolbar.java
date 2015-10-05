package com.kickstarter.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommentFeedToolbar extends Toolbar {
  @Bind(R.id.comment_button) TextView commentButton;

  // set project observable here
  Context context;

  public CommentFeedToolbar(@NonNull final Context context) {
    super(context);
  }

  public CommentFeedToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public CommentFeedToolbar(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
    context = getContext();
  }

  @Nullable
  @OnClick(R.id.comment_button)
  public void showCommentDialog() {
    // reuse from activity
  }
}
