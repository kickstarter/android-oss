package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Presenter;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.ProjectDetailActivity;
import com.kickstarter.ui.view_holders.ProjectListViewHolder;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class DiscoveryPresenter extends Presenter<DiscoveryActivity> {
  @Inject ApiClient client;
  private List<Project> projects;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KsrApplication) context.getApplicationContext()).component().inject(this);

    DiscoveryParams initial_params = DiscoveryParams.params();
    projects = client.fetchProjects(initial_params)
      .map(envelope -> envelope.projects)
      .toBlocking().last(); // TODO: Don't block

    Subscription subscription = viewSubject
      .filter(v -> v != null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> v.onItemsNext(projects));
    addSubscription(subscription);
  }

  public void onProjectClicked(final Project project, final ProjectListViewHolder viewHolder) {
    Timber.d("onProjectClicked %s", this.toString());
    Intent intent = new Intent(view(), ProjectDetailActivity.class);
    intent.putExtra("project", project);
    view().startActivity(intent);
    view().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
