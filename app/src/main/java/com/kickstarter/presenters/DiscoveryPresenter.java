package com.kickstarter.presenters;

import android.content.Intent;

import com.kickstarter.R;
import com.kickstarter.models.DiscoveryParams;
import com.kickstarter.models.Project;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.ProjectDetailActivity;
import com.kickstarter.ui.adapters.ProjectListAdapter;

import java.util.List;

public class DiscoveryPresenter {
  private DiscoveryActivity view;
  private List<Project> projects;

  public DiscoveryPresenter() {
    KickstarterClient client = new KickstarterClient();

    DiscoveryParams initial_params = new DiscoveryParams(true, DiscoveryParams.Sort.MAGIC);
    projects = client.fetchProjects(initial_params)
      .map(envelope -> envelope.projects)
      .toBlocking().last(); // TODO: Don't block
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
    view.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
