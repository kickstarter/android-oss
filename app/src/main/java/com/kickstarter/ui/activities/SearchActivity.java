package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.SearchPresenter;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.SearchAdapter;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;
import com.kickstarter.ui.views.SearchToolbar;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresPresenter(SearchPresenter.class)
public class SearchActivity extends BaseActivity<SearchPresenter> implements SearchAdapter.Delegate {
  private SearchAdapter adapter;
  LinearLayoutManager layoutManager;
  @Bind(R.id.search_recycler_view) RecyclerView recyclerView;
  @Bind(R.id.search_toolbar) SearchToolbar toolbar;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.search_layout);
    ButterKnife.bind(this);

    layoutManager = new LinearLayoutManager(this);
    adapter = new SearchAdapter(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);

    addSubscription(presenter.outputs().clear()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> adapter.clear()));

    addSubscription(presenter.outputs().newData()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::loadParamsAndProjects));

    addSubscription(presenter.outputs().startProjectActivity()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startProjectIntent));
  }

  public void projectSearchResultClick(@NonNull final ProjectSearchResultViewHolder viewHolder, @NonNull final Project project) {
    presenter.projectClick(project);
  }

  public void loadParamsAndProjects(@NonNull final Pair<DiscoveryParams, List<Project>> paramsAndProjects) {
    adapter.loadProjectsAndParams(paramsAndProjects.first, paramsAndProjects.second);
  }

  public void startProjectIntent(@NonNull final Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
