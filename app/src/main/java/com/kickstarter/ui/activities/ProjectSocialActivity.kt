package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.ui.adapters.ProjectSocialAdapter;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;
import com.kickstarter.viewmodels.ProjectSocialViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(ProjectSocialViewModel.ViewModel.class)
public final class ProjectSocialActivity extends BaseActivity<ProjectSocialViewModel.ViewModel> implements ProjectSocialAdapter.Delegate {
  protected @Bind(R.id.project_social_recycler_view) RecyclerView recyclerView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.project_social_layout);
    ButterKnife.bind(this);

    final ProjectSocialAdapter adapter = new ProjectSocialAdapter(this);

    this.recyclerView.setAdapter(adapter);
    this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.viewModel.outputs.project()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(adapter::takeProject);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.recyclerView.setAdapter(null);
  }

  @Override
  public void projectContextClicked(final @NonNull ProjectContextViewHolder viewHolder) {
    back();
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
