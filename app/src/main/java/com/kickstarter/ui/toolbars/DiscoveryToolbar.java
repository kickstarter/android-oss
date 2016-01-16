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
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.HamburgerActivity;
import com.kickstarter.ui.activities.LoginToutActivity;
import com.kickstarter.ui.activities.SearchActivity;
import com.kickstarter.ui.views.LoggedInMenu;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public final class DiscoveryToolbar extends KSToolbar {
  @Bind(R.id.activity_feed_button) TextView activityFeedButton;
  @Bind(R.id.filter_text_view) TextView filterTextView;
  //@Bind(R.id.login_button) TextView loginButton;
  @Bind(R.id.hamburger_button) TextView hamburgerButton;
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
    //activity.viewModel().inputs.menuButtonClicked();
  }

  @OnClick(R.id.hamburger_button)
  public void hamburgerButtonClick(@NonNull final View view) {
    final Context context = getContext();
    context.startActivity(new Intent(context, HamburgerActivity.class));
  }

  public void loadParams(@NonNull final DiscoveryParams params) {
    final Context context = getContext();

    filterTextView.setText(params.filterString(context));

    /*

    this.setBackgroundColor(DiscoveryUtils.primaryColor(context, params));

    final Observable<TextView> views = Observable.just(activityFeedButton,
      currentUserButton,
      filterExpandMoreButton,
      filterTextView,
      loginButton,
      searchButton,
      hamburgerButton);

    final @ColorInt int overlayTextColor = DiscoveryUtils.overlayTextColor(context, params);

    views.subscribe(view -> view.setTextColor(overlayTextColor));
    */
  }

  @OnClick(R.id.search_button)
  public void searchButtonClick(@NonNull final View view) {
    final Context context = getContext();
    context.startActivity(new Intent(context, SearchActivity.class));
  }

  protected void configureForLoggedIn(final @NonNull User user) {
    /*
    loginButton.setVisibility(GONE);
    currentUserButton.setVisibility(VISIBLE);
    currentUserButton.setOnClickListener(v -> {
      final LoggedInMenu menu = new LoggedInMenu(v.getContext(), user, currentUserButton);
      menu.show();
    });
    */
  }

  protected void configureForLoggedOut() {
    /*
    currentUserButton.setVisibility(GONE);
    loginButton.setVisibility(VISIBLE);
    loginButton.setOnClickListener(v -> {
      final Context context = getContext();
      final Intent intent = new Intent(context, LoginToutActivity.class)
        .putExtra(IntentKey.LOGIN_TYPE, LoginToutActivity.REASON_LOGIN_TAB);
      context.startActivity(intent);
    });
    */
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (isInEditMode()) {
      return;
    }

    addSubscription(currentUser.loggedOutUser()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> this.configureForLoggedOut())
    );

    addSubscription(currentUser.loggedInUser()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::configureForLoggedIn));
  }
}
