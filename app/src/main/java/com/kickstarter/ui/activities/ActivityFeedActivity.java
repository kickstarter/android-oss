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
import butterknife.Optional;

public class ActivityFeedActivity extends BaseActivity {
  @Optional @InjectView(R.id.discover_projects_button) Button discover_projects_button;

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
    Intent intent = new Intent(this, DiscoveryActivity.class);
    startActivity(intent);
  }
}
