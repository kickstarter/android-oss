package com.kickstarter.ui.views;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Logout;
import com.kickstarter.models.User;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.LoginToutActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class DiscoveryToolbar extends Toolbar {
  @Bind(R.id.activity_feed_button) TextView activityFeedButton;
  @Bind(R.id.category_spinner) Spinner categorySpinner;
  @Bind(R.id.current_user_button) TextView currentUserButton;
  @Bind(R.id.login_button) TextView loginButton;
  @Bind(R.id.toolbar) Toolbar toolbar;
  @Inject CurrentUser currentUser;
  @Inject Logout logout;

  Subscription loginSubscription;

  public DiscoveryToolbar(final Context context) {
    super(context);
  }

  public DiscoveryToolbar(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public DiscoveryToolbar(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    if (isInEditMode()) {
      return;
    }

    ButterKnife.bind(this);
    ((KsrApplication) getContext().getApplicationContext()).component().inject(this);

    initializeCategorySpinner();

    activityFeedButton.setOnClickListener(v -> {
      Intent intent = new Intent(getContext(), ActivityFeedActivity.class);
      getContext().startActivity(intent);
    });
  }

  protected void showLoggedInMenu(final User user) {
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
      Intent intent = new Intent(getContext(), LoginToutActivity.class);
      getContext().startActivity(intent);
    });

  }

  protected void initializeCategorySpinner() {
    final ArrayAdapter<CharSequence> adapter;
    if (!isInEditMode()) {
      adapter = ArrayAdapter.createFromResource(getContext(),
        R.array.spinner_categories_array,
        android.R.layout.simple_spinner_item);
    } else {
      final String sampleData[] = {"Staff Picks"};
      adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sampleData);
    }
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    categorySpinner.setAdapter(adapter);

    // onItemSelected will fire immediately with the default selection
    categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(final AdapterView<?> spinner, final View view, final int position, final long itemId) {
        final String item = spinner.getItemAtPosition(position).toString();
      }

      @Override
      public void onNothingSelected(final AdapterView<?> adapterView) {
      }
    });
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (currentUser.getUser() == null) {
      showLoggedOutMenu();
    }

    loginSubscription = currentUser.loggedInUser()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showLoggedInMenu);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    loginSubscription.unsubscribe();
  }
}
