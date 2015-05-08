package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.models.CurrentUser;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ActivityFeedActivity extends BaseActivity {
  @InjectView(R.id.discover_projects_button) Button discover_projects_button;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (CurrentUser.exists(this)) {
      // TODO: Show different layout if no activities exist
      setContentView(R.layout.activity_feed_layout);
    } else {
      setContentView(R.layout.empty_activity_feed_layout);
    }

    ButterKnife.inject(this);
  }

  public void discoverProjectsButtonOnClick(final View view) {
    // Ideally we could pop DiscoveryActivity back to the front with
    // FLAG_ACTIVITY_REORDER_TO_FRONT, but there's a long-standing bug:
    // https://code.google.com/p/android/issues/detail?id=63570#c2
    Intent intent = new Intent(this, DiscoveryActivity.class);
    startActivity(intent);
  }
}
