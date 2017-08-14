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
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.libs.utils.StatusBarUtils;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.activities.CreatorDashboardActivity;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.SearchActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;

public final class DiscoveryToolbar extends KSToolbar {
  @Bind(R.id.activity_feed_button) TextView activityFeedButton;
  @Bind(R.id.creator_dashboard_button) TextView creatorDashboardButton;
  @Bind(R.id.filter_text_view) TextView filterTextView;
  @Bind(R.id.discovery_status_bar) View discoveryStatusBar;
  @Bind(R.id.menu_button) TextView menuButton;
  @Bind(R.id.search_button) TextView searchButton;
  @Inject CurrentUserType currentUser;
  @Inject KSString ksString;
  @Inject Logout logout;

  public DiscoveryToolbar(final @NonNull Context context) {
    super(context);
  }

  public DiscoveryToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public DiscoveryToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
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
  }

  @OnClick(R.id.activity_feed_button)
  protected void activityFeedButtonClick() {
    final Context context = getContext();
    context.startActivity(new Intent(context, ActivityFeedActivity.class));
  }

  @OnClick(R.id.creator_dashboard_button)
  protected void creatorDashboardButtonClick() {
    final Context context = getContext();
    context.startActivity(new Intent(context, CreatorDashboardActivity.class));
  }

  @OnClick({R.id.menu_button, R.id.filter_text_view})
  protected void menuButtonClick() {
    final DiscoveryActivity activity = (DiscoveryActivity) getContext();
    activity.discoveryLayout().openDrawer(GravityCompat.START);
  }

  public void loadParams(final @NonNull DiscoveryParams params) {
    final DiscoveryActivity activity = (DiscoveryActivity) getContext();

    this.filterTextView.setText(params.filterString(activity, this.ksString, true, false));

    if (ApiCapabilities.canSetStatusBarColor() && ApiCapabilities.canSetDarkStatusBarIcons()) {
      this.discoveryStatusBar.setBackgroundColor(DiscoveryUtils.secondaryColor(activity, params.category()));
      if (DiscoveryUtils.overlayShouldBeLight(params.category())) {
        StatusBarUtils.setLightStatusBarIcons(activity);
      } else {
        StatusBarUtils.setDarkStatusBarIcons(activity);
      }
    }

    this.setBackgroundColor(DiscoveryUtils.primaryColor(activity, params.category()));

    final Observable<TextView> views = Observable.just(
      this.activityFeedButton,
      this.filterTextView,
      this.menuButton,
      this.searchButton
    );

    final @ColorInt int overlayTextColor = DiscoveryUtils.overlayTextColor(activity, params.category());

    views.subscribe(view -> view.setTextColor(overlayTextColor));
  }

  @OnClick(R.id.search_button)
  public void searchButtonClick() {
    final Context context = getContext();
    context.startActivity(new Intent(context, SearchActivity.class));
  }
}
