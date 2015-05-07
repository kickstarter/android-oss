package com.kickstarter.ui.activities;

import android.os.Bundle;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.models.CurrentUser;

public class ActivityFeedActivity extends BaseActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (CurrentUser.exists(this)) {
      // TODO: Show different layout if no activities exist
      setContentView(R.layout.activity_feed_layout);
    } else {
      setContentView(R.layout.empty_activity_feed_layout);
    }
  }
}
