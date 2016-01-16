package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jakewharton.rxbinding.support.v4.widget.RxDrawerLayout;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter;
import com.kickstarter.viewmodels.HamburgerViewModel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(HamburgerViewModel.class)
public final class HamburgerActivity extends BaseActivity<HamburgerViewModel> {
  private final RecyclerView.LayoutManager navigationLayoutManager = new LinearLayoutManager(this);
  private DiscoveryDrawerAdapter navigationAdapter;

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

    navigationAdapter = new DiscoveryDrawerAdapter(viewModel);
    navigationRecyclerView.setLayoutManager(navigationLayoutManager);
    navigationRecyclerView.setAdapter(navigationAdapter);

    viewModel.navigationDrawerData()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(data -> {
        navigationAdapter.takeData(data);
        //        navigationRecyclerView.scrollToPosition(6);
      });

    viewModel
      .openDrawer()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(RxDrawerLayout.open(drawerLayout, GravityCompat.START));
  }

  @OnClick({R.id.menu_button, R.id.filter_text_view})
  protected void menuButtonClick() {
    drawerLayout.openDrawer(GravityCompat.START);
  }

  @OnClick(R.id.activity_feed_button)
  protected void activityFeedButtonClick() {
  }

  @OnClick(R.id.search_button)
  protected void searchButtonClick() {
  }

  private void toggleDrawer(final boolean open) {
    if (open) {
      drawerLayout.openDrawer(GravityCompat.START);
    } else {
      drawerLayout.closeDrawers();
    }
  }
}
