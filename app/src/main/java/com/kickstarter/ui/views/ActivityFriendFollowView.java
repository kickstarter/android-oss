package com.kickstarter.ui.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ActivityFriendFollowView extends CardView {
  @InjectView(R.id.follow_text) TextView follow_text;

  public ActivityFriendFollowView(Context context) {
    super(context);
  }

  public ActivityFriendFollowView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ActivityFriendFollowView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    ButterKnife.inject(this);

    follow_text.setText("Elise Wright" + " " + getResources().getString(R.string.is_following_you));
    final Spannable str = (Spannable) follow_text.getText();
    str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
  }
}
