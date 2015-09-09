package com.kickstarter.ui.views;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.R;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import timber.log.Timber;

public class CommentFeedToolbar extends Toolbar {
  @Bind(R.id.comment_button) TextView commentButton;

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
    ButterKnife.bind(this);
  }

  @Nullable
  @OnClick(R.id.comment_button)
  public void postComment() {
    Timber.d("post comment");

    Dialog dialog = new Dialog(getContext());
    dialog.setTitle("Leave a comment");
    dialog.show();
  }
}
