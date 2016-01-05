package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.libs.utils.KSColorUtils;
import com.kickstarter.libs.utils.StatusBarUtils;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.models.Category;
import com.kickstarter.viewmodels.DiscoveryFilterViewModel;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.DiscoveryFilterAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresViewModel(DiscoveryFilterViewModel.class)
public final class DiscoveryFilterActivity extends BaseActivity<DiscoveryFilterViewModel> {
  private DiscoveryFilterAdapter adapter;

  protected @Bind(R.id.close_button) TextView closeButton;
  protected @Bind(R.id.discovery_filter_layout) RelativeLayout layout;
  protected @Bind(R.id.background_gradient) LinearLayout backgroundGradientLayout;
  protected @Bind(R.id.background_image_view) ImageView backgroundImageView;
  protected @Bind(R.id.recycler_view) RecyclerView recyclerView;
  protected @BindColor(R.color.dark_blue_gradient_start) int darkBlueGradientStartColor;
  protected @BindDrawable(R.drawable.dark_blue_gradient) Drawable darkBlueGradientDrawable;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.discovery_filter_layout);
    ButterKnife.bind(this);

    final DiscoveryParams params = getIntent().getParcelableExtra(getString(R.string.intent_discovery_params));
    adapter = new DiscoveryFilterAdapter(viewModel, params);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(adapter);

    setBackground(params);
    setStatusBarColor(params);
    closeButton.setTextColor(DiscoveryUtils.overlayTextColor(this, params));
  }

  @OnClick(R.id.close_button)
  public void closeActivity() {
    onBackPressed();
  }

  public void loadCategories(final @NonNull List<Category> categories) {
    adapter.takeCategories(categories);
    recyclerView.scrollToPosition(adapter.scrollPositionForSelectedParams());
  }

  public void startDiscoveryActivity(final @NonNull DiscoveryParams newDiscoveryParams) {
    final Intent intent = new Intent().putExtra(getString(R.string.intent_discovery_params), newDiscoveryParams);
    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  private void setBackground(final @NonNull DiscoveryParams params) {
    resizeGradientView();
    layout.setBackgroundColor(DiscoveryUtils.primaryColor(this, params));
    backgroundImageView.setImageDrawable(backgroundDrawable(params));
    backgroundGradientLayout.setBackground(backgroundGradientDrawable(params));
  }

  private void setStatusBarColor(final @NonNull DiscoveryParams params) {
    if (ApiCapabilities.canSetStatusBarColor() && ApiCapabilities.canSetDarkStatusBarIcons()) {
      if (params.isCategorySet()) {
        final Category category = params.category();
        StatusBarUtils.apply(this, category.secondaryColor(this), category.overlayShouldBeLight());
      } else {
        StatusBarUtils.apply(this, KSColorUtils.darken(darkBlueGradientStartColor, 0.1f), true);
      }
    }
  }

  private @Nullable Drawable backgroundDrawable(final @NonNull DiscoveryParams params) {
    if (params.isCategorySet()) {
      return DiscoveryUtils.imageWithOrientation(params.category(), this);
    } else {
      return darkBlueGradientDrawable;
    }
  }

  private @Nullable GradientDrawable backgroundGradientDrawable(final @NonNull DiscoveryParams params) {
    if (!params.isCategorySet() || backgroundDrawable(params) == null) {
      return null;
    }

    final int color = params.category().color();
    final int[] gradientColors = new int[] {KSColorUtils.setAlpha(color, 242),
      KSColorUtils.setAlpha(color, 215),
      KSColorUtils.setAlpha(color, 0)};
    return new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
  }

  private void resizeGradientView() {
    final Display display = getWindowManager().getDefaultDisplay();
    final Point size = new Point();
    display.getSize(size);
    final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Math.round(size.x * 0.65f),
      RelativeLayout.LayoutParams.MATCH_PARENT);
    backgroundGradientLayout.setLayoutParams(layoutParams);
  }
}
