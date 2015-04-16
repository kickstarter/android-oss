package com.kickstarter.presenters;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;

import com.kickstarter.R;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.ProjectDetailActivity;
import com.kickstarter.ui.adapters.ProjectListAdapter;
import com.kickstarter.models.Project;
import com.kickstarter.services.KickstarterClient;

import java.util.List;

public class DiscoveryPresenter {
  private DiscoveryActivity view;
  private List<Project> projects;

  public DiscoveryPresenter() {
    KickstarterClient client = new KickstarterClient();
    projects = client.fetchProjects().toBlocking().last(); // TODO: Don't block
  }

  public void onTakeView(DiscoveryActivity view) {
    this.view = view;
    publish();
  }

  public void publish() {
    if (view != null) {
      if (projects != null) {
        view.onItemsNext(projects);
      }
    }
  }

  public void onProjectClicked(Project project, ProjectListAdapter.ViewHolder viewHolder) {
    Intent intent = new Intent(view, ProjectDetailActivity.class);
    intent.putExtra("project", project);
    view.startActivity(intent);
    view.overridePendingTransition(R.anim.slide_in_bottom, 0);
  }
}
