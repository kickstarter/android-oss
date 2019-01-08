package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Pair;

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
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

@RequiresActivityViewModel(SearchViewModel.ViewModel.class)
public final class SearchActivity extends BaseActivity<SearchViewModel.ViewModel> implements SearchAdapter.Delegate {
  private SearchAdapter adapter;
  private RecyclerViewPaginator paginator;

  protected @Bind(R.id.search_recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.search_toolbar) SearchToolbar toolbar;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.search_layout);
    ButterKnife.bind(this);

    this.adapter = new SearchAdapter(this);
    this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
    this.recyclerView.setAdapter(this.adapter);

    this.paginator = new RecyclerViewPaginator(this.recyclerView, this.viewModel.inputs::nextPage);

    RxRecyclerView.scrollEvents(this.recyclerView)
      .compose(bindToLifecycle())
      .filter(scrollEvent -> scrollEvent.dy() != 0) // Skip scroll events when y is 0, usually indicates new data
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> InputUtils.hideKeyboard(this, getCurrentFocus()));

    this.viewModel.outputs.popularProjects()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.adapter::loadPopularProjects);

    this.viewModel.outputs.searchProjects()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.adapter::loadSearchProjects);

    this.viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startProjectActivity);
  }

  private void startProjectActivity(final @NonNull Pair<Project, RefTag> projectAndRefTag) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, projectAndRefTag.first)
      .putExtra(IntentKey.REF_TAG, projectAndRefTag.second);

    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.paginator.stop();
    this.recyclerView.setAdapter(null);
  }

  public void projectSearchResultClick(final @NonNull ProjectSearchResultViewHolder viewHolder, final @NonNull Project project) {
    this.viewModel.inputs.projectClicked(project);
  }
}
