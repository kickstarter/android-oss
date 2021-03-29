package com.kickstarter.ui.activities;

import android.os.Bundle;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.models.BackingWrapper;
import com.kickstarter.ui.data.ProjectData;
import com.kickstarter.ui.fragments.BackingFragment;
import com.kickstarter.viewmodels.BackingViewModel;

import androidx.annotation.Nullable;

@RequiresActivityViewModel(BackingViewModel.ViewModel.class)
public final class BackingActivity extends BaseActivity<BackingViewModel.ViewModel> implements BackingFragment.BackingDelegate {

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.backing_layout);

    this.viewModel.outputs.showBackingFragment()
            .compose(bindToLifecycle())
            .subscribe( it -> {
              this.startBackingFragment(it);
              this.backingFragment().pageViewed();
            });

    this.viewModel.outputs.isRefreshing()
            .compose(bindToLifecycle())
            .subscribe(it -> backingFragment().isRefreshing(it));

  }

  private void startBackingFragment(final BackingWrapper backingWrapper) {
    final ProjectData data = ProjectData.Companion.builder()
            .project(backingWrapper.getProject())
            .backing(backingWrapper.getBacking())
            .user(backingWrapper.getUser())
            .build();
    backingFragment().configureWith(data);

    getSupportFragmentManager().beginTransaction()
            .show(backingFragment())
            .commit();
  }

  private BackingFragment backingFragment() {
    return  (BackingFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_backing);
  }

  @Override
  public void refreshProject() {
    this.viewModel.inputs.refresh();
  }

  @Override
  public void showFixPaymentMethod() { }
}

