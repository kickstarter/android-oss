package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.viewmodels.HamburgerViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresViewModel(HamburgerViewModel.class)
public class HamburgerActivity extends BaseActivity<HamburgerViewModel> {
  @Bind(R.id.hamburger_layout) DrawerLayout drawerLayout;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.hamburger_layout);
    ButterKnife.bind(this);
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
}
