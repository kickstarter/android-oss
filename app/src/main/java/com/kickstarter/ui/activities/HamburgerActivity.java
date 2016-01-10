package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.models.User;
import com.kickstarter.ui.views.HamburgerNavigationHeaderType;
import com.kickstarter.viewmodels.HamburgerViewModel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(HamburgerViewModel.class)
public class HamburgerActivity extends BaseActivity<HamburgerViewModel> {
  @Bind(R.id.hamburger_layout) DrawerLayout drawerLayout;
  @Bind(R.id.hamburger_navigation_view) NavigationView navigationView;

  protected @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.hamburger_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    currentUser.observable()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::user);
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

  protected void user(final @Nullable User user) {
    header().user(user);
  }

  protected @NonNull HamburgerNavigationHeaderType header() {
    return (HamburgerNavigationHeaderType) navigationView.getHeaderView(0);
  }
}
