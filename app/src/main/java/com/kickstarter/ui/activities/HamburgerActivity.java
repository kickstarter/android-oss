package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.models.HamburgerNavigationItem;
import com.kickstarter.ui.adapters.HamburgerNavigationAdapter;
import com.kickstarter.ui.viewholders.HamburgerNavigationFilterViewHolder;
import com.kickstarter.viewmodels.HamburgerViewModel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

@RequiresViewModel(HamburgerViewModel.class)
public final class HamburgerActivity extends BaseActivity<HamburgerViewModel> implements HamburgerNavigationAdapter.Delegate {
  private final RecyclerView.LayoutManager navigationLayoutManager = new LinearLayoutManager(this);
  private HamburgerNavigationAdapter navigationAdapter;

  protected @Inject CurrentUser currentUser;

  protected @Bind(R.id.hamburger_layout) DrawerLayout drawerLayout;
  protected @Bind(R.id.hamburger_navigation_view) NavigationView navigationView;
  protected @Bind(R.id.hamburger_navigation_recycler_view) RecyclerView navigationRecyclerView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.hamburger_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    navigationAdapter = new HamburgerNavigationAdapter(this);
    navigationRecyclerView.setLayoutManager(navigationLayoutManager);
    navigationRecyclerView.setAdapter(navigationAdapter);

    viewModel.hamburgerNavigationData()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(navigationAdapter::data);
  }

  @OnClick({R.id.menu_button, R.id.filter_text_view})
  protected void menuButtonClick() {
    drawerLayout.openDrawer(GravityCompat.START);
  }

  @OnClick(R.id.activity_feed_button)
  protected void activityFeedButtonClick() {
    Timber.d("Activity feed clicked");
  }

  @OnClick(R.id.search_button)
  protected void searchButtonClick() {
    Timber.d("Search clicked");
  }

  @Override
  public void filterClicked(final @NonNull HamburgerNavigationFilterViewHolder viewHolder,
    final @NonNull HamburgerNavigationItem hamburgerNavigationItem) {
    viewModel.inputs.filterClicked(hamburgerNavigationItem);
  }
}
