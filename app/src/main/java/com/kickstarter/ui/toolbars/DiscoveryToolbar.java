package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.libs.utils.StatusBarUtils;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.SearchActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;

public final class DiscoveryToolbar extends KSToolbar {
  @Bind(R.id.activity_feed_button) TextView activityFeedButton;
  @Bind(R.id.filter_text_view) TextView filterTextView;
  @Bind(R.id.discovery_status_bar) View discoveryStatusBar;
  @Bind(R.id.menu_button) TextView menuButton;
  @Bind(R.id.search_button) TextView searchButton;
  @Inject CurrentUser currentUser;
  @Inject Logout logout;

  public DiscoveryToolbar(@NonNull final Context context) {
    super(context);
  }

  public DiscoveryToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public DiscoveryToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    if (isInEditMode()) {
      return;
    }

    ButterKnife.bind(this);
    ((KSApplication) getContext().getApplicationContext()).component().inject(this);

    activityFeedButton.setOnClickListener(v -> {
      final Context context = getContext();
      context.startActivity(new Intent(context, ActivityFeedActivity.class));
    });
  }

  @OnClick({R.id.menu_button, R.id.filter_text_view})
  protected void menuButtonClick() {
    final DiscoveryActivity activity = (DiscoveryActivity) getContext();
    activity.discoveryLayout().openDrawer(GravityCompat.START);
  }

  public void loadParams(@NonNull final DiscoveryParams params) {
    final DiscoveryActivity activity = (DiscoveryActivity) getContext();

    filterTextView.setText(params.filterString(activity));

    if (ApiCapabilities.canSetStatusBarColor() && ApiCapabilities.canSetDarkStatusBarIcons()) {
      discoveryStatusBar.setBackgroundColor(DiscoveryUtils.secondaryColor(activity, params));
      if (DiscoveryUtils.overlayShouldBeLight(params)) {
        StatusBarUtils.setLightStatusBarIcons(activity);
      } else {
        StatusBarUtils.setDarkStatusBarIcons(activity);
      }
    }

    this.setBackgroundColor(DiscoveryUtils.primaryColor(activity, params));

    final Observable<TextView> views = Observable.just(activityFeedButton,
      filterTextView,
      menuButton,
      searchButton);

    final @ColorInt int overlayTextColor = DiscoveryUtils.overlayTextColor(activity, params);

    views.subscribe(view -> view.setTextColor(overlayTextColor));
  }

  @OnClick(R.id.search_button)
  public void searchButtonClick(@NonNull final View view) {
    final Context context = getContext();
    context.startActivity(new Intent(context, SearchActivity.class));
  }
}
