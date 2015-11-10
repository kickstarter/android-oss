package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
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
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.LoginToutActivity;
import com.kickstarter.ui.activities.SearchActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public final class DiscoveryToolbar extends KSToolbar {
  @Bind(R.id.activity_feed_button) TextView activityFeedButton;
  @Bind(R.id.current_user_button) TextView currentUserButton;
  @Bind(R.id.filter_expand_more_button) TextView filterExpandMoreButton;
  @Bind(R.id.filter_text_view) TextView filterTextView;
  @Bind(R.id.login_button) TextView loginButton;
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

  @OnClick(R.id.filter_button)
  public void filterButtonClick(@NonNull final View view) {
    final DiscoveryActivity activity = (DiscoveryActivity) getContext();
    activity.presenter().filterButtonClick();
  }

  public void loadParams(@NonNull final DiscoveryParams params) {
    final Context context = getContext();

    this.setBackgroundColor(DiscoveryUtils.primaryColor(context, params));

    filterTextView.setText(params.filterString(context));

    final Observable<TextView> views = Observable.just(activityFeedButton,
      currentUserButton,
      filterExpandMoreButton,
      filterTextView,
      loginButton,
      searchButton);

    final @ColorInt int overlayTextColor = DiscoveryUtils.overlayTextColor(context, params);

    views.subscribe(view -> view.setTextColor(overlayTextColor));
  }

  @OnClick(R.id.search_button)
  public void searchButtonClick(@NonNull final View view) {
    final Context context = getContext();
    context.startActivity(new Intent(context, SearchActivity.class));
  }

  protected void showLoggedInMenu(@NonNull final User user) {
    loginButton.setVisibility(GONE);
    currentUserButton.setVisibility(VISIBLE);
    currentUserButton.setOnClickListener(v -> {
      final PopupMenu popup = new PopupMenu(v.getContext(), currentUserButton);
      popup.getMenuInflater().inflate(R.menu.current_user_menu, popup.getMenu());

      popup.setOnMenuItemClickListener(item -> {
        switch (item.getItemId()) {
          case R.id.logout:
            final Context context = v.getContext();
            logout.execute();
            final Intent intent = new Intent(context, DiscoveryActivity.class)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            break;
        }

        return true;
      });

      popup.show();
    });
  }

  protected void showLoggedOutMenu() {
    currentUserButton.setVisibility(GONE);
    loginButton.setVisibility(VISIBLE);
    loginButton.setOnClickListener(v -> {
      Intent intent = new Intent(getContext(), LoginToutActivity.class)
        .putExtra(getContext().getResources().getString(R.string.intent_login_type), LoginToutActivity.REASON_LOGIN_TAB);
      getContext().startActivity(intent);
    });
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (isInEditMode()) {
      return;
    }

    if (currentUser.getUser() == null) {
      showLoggedOutMenu();
    }

    addSubscription(currentUser.loggedInUser()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showLoggedInMenu));
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }
}
