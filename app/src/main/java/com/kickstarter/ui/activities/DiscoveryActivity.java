package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.adapters.DiscoveryAdapter;
import com.kickstarter.ui.containers.ApplicationContainer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.subjects.PublishSubject;

@RequiresPresenter(DiscoveryPresenter.class)
public class DiscoveryActivity extends BaseActivity<DiscoveryPresenter> {
  DiscoveryAdapter adapter;
  LinearLayoutManager layoutManager;
  final List<Project> projects = new ArrayList<>();
  final PublishSubject<Integer> visibleItem = PublishSubject.create();
  final PublishSubject<Integer> itemCount = PublishSubject.create();
  Subscription pageSubscription;

  @Inject ApplicationContainer applicationContainer;

  @BindColor(R.color.discovery_toolbar) int discoveryToolbarColor;
  @Bind(R.id.recycler_view) RecyclerView recyclerView;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ((KSApplication) getApplication()).component().inject(this);
    final ViewGroup container = applicationContainer.bind(this);
    final LayoutInflater layoutInflater = getLayoutInflater();

    layoutInflater.inflate(R.layout.discovery_layout, container);
    ButterKnife.bind(this, container);

    setStatusBarColor();

    layoutManager = new LinearLayoutManager(this);
    adapter = new DiscoveryAdapter(projects, presenter);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);

    pageSubscription = RxUtils.combineLatestPair(visibleItem, itemCount)
      .distinctUntilChanged()
      .filter(this::closeToBottom)
      .subscribe(__ -> presenter.takeNextPage());

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

  public void loadProjects(final List<Project> newProjects) {
    final int oldProjectsSize = projects.size();
    projects.clear();
    projects.addAll(newProjects);
    adapter.notifyItemRangeInserted(oldProjectsSize, projects.size());
  }

  public void clearItems() {
    loadProjects(new ArrayList<>());
  }

  public void startProjectActivity(final Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void showBuildAlert(final InternalBuildEnvelope envelope) {
    new AlertDialog.Builder(this)
      .setTitle(getString(R.string.Upgrade_app))
      .setMessage(getString(R.string.A_newer_build_is_available))
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

  private boolean closeToBottom(final Pair<Integer, Integer> itemAndCount) {
    return itemAndCount.first == itemAndCount.second - 2;
  }

  private void setStatusBarColor() {
    if (ApiCapabilities.canSetStatusBarColor()) {
      final Window window = getWindow();
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.setStatusBarColor(discoveryToolbarColor);
    }
  }
}
