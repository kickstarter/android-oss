package com.kickstarter.presenters;

import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;

import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.ProjectDetailActivity;
import com.kickstarter.ui.adapters.ProjectListAdapter;
import com.kickstarter.models.Project;
import com.kickstarter.services.KickstarterClient;

import java.util.List;

public class DiscoveryPresenter {
  private DiscoveryActivity view; // TODO: Move this to interface, e.g. discoveryView

  public DiscoveryPresenter(DiscoveryActivity discoveryActivity) {
    this.view = discoveryActivity;
  }

  public void onCreate() {
    KickstarterClient client = new KickstarterClient();
    List<Project> projects = client.fetchProjects().toBlocking().last();
    view.setProjects(projects);
  }

  public void onProjectClicked(Project project, ProjectListAdapter.ViewHolder viewHolder) {
    Intent intent = new Intent(view, ProjectDetailActivity.class);
    intent.putExtra("project", project);
    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
      view,
      Pair.create(viewHolder.category(), "category"),
      Pair.create(viewHolder.photo(), "photo"));
    view.startActivity(intent, options.toBundle());
  }
}
