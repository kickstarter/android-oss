package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.services.ApiResponses.InternalBuildEnvelope;
import com.kickstarter.ui.adapters.ProjectListAdapter;
import com.kickstarter.ui.containers.ApplicationContainer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.subjects.PublishSubject;

@RequiresPresenter(DiscoveryPresenter.class)
public class DiscoveryActivity extends BaseActivity<DiscoveryPresenter> {
  final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
  final ArrayList<Project> projects = new ArrayList<>();
  final ProjectListAdapter adapter = new ProjectListAdapter(projects, presenter);
  final PublishSubject<Integer> visibleItem = PublishSubject.create();
  final PublishSubject<Integer> itemCount = PublishSubject.create();
  Subscription pageSubscription;

  @Inject ApplicationContainer applicationContainer;

  @InjectView(R.id.recyclerView) RecyclerView recyclerView;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ((KsrApplication) getApplication()).component().inject(this);
    final ViewGroup container = applicationContainer.bind(this);
    final LayoutInflater layoutInflater = getLayoutInflater();

    layoutInflater.inflate(R.layout.discovery_layout, container);
    ButterKnife.inject(this, container);

    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);

    pageSubscription = RxUtils.combineLatestPair(visibleItem, itemCount)
      .distinctUntilChanged()
      .filter(itemAndCount -> itemAndCount.first == itemAndCount.second - 2)
      .subscribe(itemAndCount -> presenter.takeNextPage());

    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
        final int visibleItemCount = layoutManager.getChildCount();
        final int totalItemCount = layoutManager.getItemCount();
        final int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

        visibleItem.onNext(visibleItemCount + pastVisibleItems);
        itemCount.onNext(totalItemCount);
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    recyclerView.clearOnScrollListeners();
    pageSubscription.unsubscribe();
  }

  public void onItemsNext(final List<Project> newProjects) {
    projects.addAll(newProjects);
    adapter.notifyDataSetChanged();
  }

  public void startProjectDetailActivity(final Project project) {
    final Intent intent = new Intent(this, ProjectDetailActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void showBuildAlert(final InternalBuildEnvelope envelope) {
    new AlertDialog.Builder(this)
      .setTitle("Upgrade app")
      .setMessage("A newer build is available. Download upgrade?")
      .setPositiveButton(android.R.string.yes, (dialog, which) -> {
        Intent intent = new Intent(this, DownloadBetaActivity.class)
          .putExtra(getString(R.string.intent_internal_build_envelope), envelope);
        startActivity(intent);
      })
      .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
      })
      .setIcon(android.R.drawable.ic_dialog_alert)
      .show();
  }
}
