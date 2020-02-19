package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.SwipeRefresher;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.UpdatesAdapter;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.viewmodels.ProjectUpdatesViewModel;

import org.jetbrains.annotations.NotNull;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(ProjectUpdatesViewModel.ViewModel.class)
public class ProjectUpdatesActivity extends BaseActivity<ProjectUpdatesViewModel.ViewModel> implements UpdatesAdapter.Delegate {
  private UpdatesAdapter adapter;
  private RecyclerViewPaginator recyclerViewPaginator;
  private SwipeRefresher swipeRefresher;

  protected @Bind(R.id.updates_toolbar) KSToolbar updatesToolbar;
  protected @Bind(R.id.updates_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
  protected @Bind(R.id.updates_recycler_view) RecyclerView recyclerView;

  protected @BindString(R.string.project_subpages_menu_buttons_updates) String updatesTitleString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_updates);
    ButterKnife.bind(this);

    this.adapter = new UpdatesAdapter(this);
    this.recyclerView.setAdapter(this.adapter);
    this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.recyclerViewPaginator = new RecyclerViewPaginator(this.recyclerView, this.viewModel.inputs::nextPage, this.viewModel.outputs.isFetchingUpdates());
    this.swipeRefresher = new SwipeRefresher(
            this, this.swipeRefreshLayout, this.viewModel.inputs::refresh, this.viewModel.outputs::isFetchingUpdates
    );
    this.updatesToolbar.setTitle(this.updatesTitleString);

    this.viewModel.outputs.startUpdateActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pu -> this.startUpdateActivity(pu.first, pu.second));

    this.viewModel.outputs.projectAndUpdates()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.adapter::takeData);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    this.recyclerViewPaginator.stop();
    this.recyclerView.setAdapter(null);
  }

  @Override
  public void updateClicked(final @NotNull Update update) {
    this.viewModel.inputs.goToUpdate(update);
  }

  private void startUpdateActivity(final @NonNull Project project, final @NonNull Update update) {
    final Intent intent = new Intent(this, UpdateActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.UPDATE, update);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }

}
