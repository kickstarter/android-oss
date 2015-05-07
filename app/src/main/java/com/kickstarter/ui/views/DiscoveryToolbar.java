package com.kickstarter.ui.views;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.kickstarter.R;
import com.kickstarter.models.CurrentUser;
import com.kickstarter.models.User;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.LoginToutActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

public class DiscoveryToolbar extends Toolbar {
  @InjectView(R.id.category_spinner) Spinner category_spinner;
  @InjectView(R.id.login_group) ViewGroup login_group;
  @InjectView(R.id.current_user_group) ViewGroup current_user_group;
  @InjectView(R.id.toolbar) Toolbar toolbar;

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

    ButterKnife.inject(this);

    toggleLogin();
    initializeCategorySpinner();
  }

  protected void toggleLogin() {
    User user = CurrentUser.getUser(getContext().getApplicationContext());
    if (user != null) {
      login_group.setVisibility(INVISIBLE);
      current_user_group.setVisibility(VISIBLE);
      current_user_group.setOnClickListener(v -> {
        PopupMenu popup = new PopupMenu(v.getContext(), current_user_group);
        popup.getMenuInflater().inflate(R.menu.current_user_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
          switch (item.getItemId()) {
            case R.id.logout:
              logout(v);
              break;
          }

          return true;
        });

        popup.show();
      });
    } else {
      current_user_group.setVisibility(INVISIBLE);
      login_group.setVisibility(VISIBLE);
      login_group.setOnClickListener(v -> {
        Timber.d("login_group clicked");
        Intent intent = new Intent(getContext(), LoginToutActivity.class);
        getContext().startActivity(intent);
      });
    }
  }

  protected void initializeCategorySpinner() {
    ArrayAdapter<CharSequence> adapter;
    if (!isInEditMode()) {
      adapter = ArrayAdapter.createFromResource(getContext(),
        R.array.spinner_categories_array,
        android.R.layout.simple_spinner_item);
    } else {
      String sample_data[] = {"Staff Picks"};
      adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sample_data);
    }
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    category_spinner.setAdapter(adapter);

    // onItemSelected will fire immediately with the default selection
    category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(final AdapterView<?> spinner, final View view, final int position, final long itemId) {
        String item = spinner.getItemAtPosition(position).toString();
      }

      @Override
      public void onNothingSelected(final AdapterView<?> adapterView) {
      }
    });
  }

  protected void logout(final View v) {
    CurrentUser.unset(getContext().getApplicationContext());
    Intent intent = new Intent(getContext(), DiscoveryActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    v.getContext().startActivity(intent);
  }
}
