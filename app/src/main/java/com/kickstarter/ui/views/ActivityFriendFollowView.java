package com.kickstarter.ui.views;

import android.content.Context;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ActivityFriendFollowView extends LinearLayout {
  @InjectView(R.id.follow_text) TextView follow_text;

  public ActivityFriendFollowView(Context context) {
    this(context, null);
  }

  public ActivityFriendFollowView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ActivityFriendFollowView(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public ActivityFriendFollowView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onFinishInflate() {
    ButterKnife.inject(this);

    follow_text.setText("Elise Wright" + " " + getResources().getString(R.string.is_following_you));
    final Spannable str = (Spannable) follow_text.getText();
    str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
  }
}
