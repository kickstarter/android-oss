package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.CurrentUser;
import com.kickstarter.presenters.ActivityFeedPresenter;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

@RequiresPresenter(ActivityFeedPresenter.class)
public class ActivityFeedActivity extends BaseActivity<ActivityFeedPresenter> {
  @Optional @InjectView(R.id.discover_projects_button) Button discover_projects_button;
  @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ((KsrApplication) getApplication()).component().inject(this);
    if (currentUser.exists()) {
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
