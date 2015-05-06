package com.kickstarter.presenters;

import android.content.Intent;
import android.os.Bundle;

import com.kickstarter.R;
import com.kickstarter.libs.Presenter;
import com.kickstarter.models.DiscoveryParams;
import com.kickstarter.models.Project;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.ProjectDetailActivity;
import com.kickstarter.ui.adapters.ProjectListAdapter;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class DiscoveryPresenter extends Presenter<DiscoveryActivity> {
  private static final KickstarterClient client = new KickstarterClient();
  private List<Project> projects;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    DiscoveryParams initial_params = new DiscoveryParams(true, DiscoveryParams.Sort.MAGIC);
    projects = client.fetchProjects(initial_params)
      .map(envelope -> envelope.projects)
      .toBlocking().last(); // TODO: Don't block

    Subscription subscription = viewSubject
      .filter(v -> v != null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> v.onItemsNext(projects));
    addSubscription(subscription);
  }

  public void onProjectClicked(final Project project, final ProjectListAdapter.ViewHolder viewHolder) {
    Timber.d("onProjectClicked %s", this.toString());
    Intent intent = new Intent(view(), ProjectDetailActivity.class);
    intent.putExtra("project", project);
    view().startActivity(intent);
    view().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
