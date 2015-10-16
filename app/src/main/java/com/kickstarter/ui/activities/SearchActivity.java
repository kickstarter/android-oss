package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.SearchPresenter;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.SearchAdapter;
import com.kickstarter.ui.views.SearchToolbar;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

@RequiresPresenter(SearchPresenter.class)
public class SearchActivity extends BaseActivity<SearchPresenter> {
  private SearchAdapter adapter;
  LinearLayoutManager layoutManager;
  final List<Project> projects = new ArrayList<>();
  public @Bind(R.id.search_recycler_view) RecyclerView recyclerView;
  public @Bind(R.id.search_toolbar) SearchToolbar toolbar;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.search_layout);
    ButterKnife.bind(this);

    layoutManager = new LinearLayoutManager(this);
    adapter = new SearchAdapter(presenter);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
  }

  public void loadParamsAndProjects(@NonNull final DiscoveryParams params, @NonNull final List<Project> newProjects) {
    projects.clear();
    projects.addAll(newProjects);
    adapter.notifyDataSetChanged();
  }

  public void startProjectIntent(@NonNull final Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public SearchAdapter adapter() {
    return adapter;
  }
}
