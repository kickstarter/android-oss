package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.DiscoveryUtils;
import com.kickstarter.libs.KSColorUtils;
import com.kickstarter.libs.StatusBarUtils;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.models.Category;
import com.kickstarter.presenters.DiscoveryFilterPresenter;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.DiscoveryFilterAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresPresenter(DiscoveryFilterPresenter.class)
public class DiscoveryFilterActivity extends BaseActivity<DiscoveryFilterPresenter> {
  DiscoveryFilterAdapter adapter;
  LinearLayoutManager layoutManager;

  @Bind(R.id.close_button) TextView closeButton;
  @Bind(R.id.discovery_filter_layout) RelativeLayout layout;
  @Bind(R.id.recycler_view) RecyclerView recyclerView;
  @BindColor(R.color.dark_blue_gradient_start) int darkBlueGradientStartColor;
  @BindColor(R.color.text_dark) int darkColor;
  @BindColor(R.color.white) int lightColor;
  @BindDrawable(R.drawable.dark_blue_gradient) Drawable darkBlueGradientDrawable;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.discovery_filter_layout);
    ButterKnife.bind(this);

    layoutManager = new LinearLayoutManager(this);
    final DiscoveryParams discoveryParams = getIntent().getParcelableExtra(getString(R.string.intent_discovery_params));
    adapter = new DiscoveryFilterAdapter(presenter, discoveryParams);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);

    loadParams(discoveryParams);

    presenter.initialize(DiscoveryParams.builder().build()); // TODO: Replace with params from discovery intent
  }

  @OnClick(R.id.close_button)
  public void closeActivity() {
    onBackPressed();
  }

  public void loadCategories(@NonNull final List<Category> categories) {
    adapter.takeCategories(categories);
  }

  public void startDiscoveryActivity(@NonNull final DiscoveryParams newDiscoveryParams) {
    final Intent intent = new Intent().putExtra(getString(R.string.intent_discovery_params), newDiscoveryParams);
    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  private void loadParams(@NonNull final DiscoveryParams params) {
    if (ApiCapabilities.canSetStatusBarColor() && ApiCapabilities.canSetDarkStatusBarIcons()) {
      if (params.isCategorySet()) {
        final Category category = params.category();
        StatusBarUtils.apply(this, category.secondaryColor(this), category.overlayShouldBeLight());
      } else {
        StatusBarUtils.apply(this, KSColorUtils.darken(darkBlueGradientStartColor, 0.1f), true);
      }
    }

    if (params.isCategorySet()) {
      recyclerView.setBackgroundColor(params.category().colorWithAlpha());
    } else {
      recyclerView.setBackground(darkBlueGradientDrawable);
    }

    closeButton.setTextColor(DiscoveryUtils.overlayTextColor(this, params));
  }
}
