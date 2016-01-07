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
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.InputUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.SearchAdapter;
import com.kickstarter.ui.toolbars.SearchToolbar;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;
import com.kickstarter.viewmodels.SearchViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(SearchViewModel.class)
public final class SearchActivity extends BaseActivity<SearchViewModel> implements SearchAdapter.Delegate {
  private SearchAdapter adapter;
  private RecyclerViewPaginator paginator;
  protected @Bind(R.id.search_recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.search_toolbar) SearchToolbar toolbar;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.search_layout);
    ButterKnife.bind(this);

    adapter = new SearchAdapter(this);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(adapter);

    paginator = new RecyclerViewPaginator(recyclerView, viewModel.inputs::nextPage);

    RxRecyclerView.scrollEvents(recyclerView)
      .compose(bindToLifecycle())
      .filter(scrollEvent -> scrollEvent.dy() != 0) // Skip scroll events when y is 0, usually indicates new data
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> InputUtils.hideKeyboard(this, getCurrentFocus()));

    viewModel.outputs.popularProjects()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(adapter::loadPopularProjects);

    viewModel.outputs.searchProjects()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(adapter::loadSearchProjects);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    paginator.stop();
  }

  public void projectSearchResultClick(final @NonNull ProjectSearchResultViewHolder viewHolder, final @NonNull Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
