package com.kickstarter.ui.views;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.User;
import com.kickstarter.ui.activities.LoginToutActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

public class DiscoveryToolbar extends Toolbar {
  @InjectView(R.id.category_spinner) Spinner spinner;
  @InjectView(R.id.login_group) ViewGroup login_group;
  @InjectView(R.id.current_user_group) ViewGroup current_user_group;
  @InjectView(R.id.current_user_name) TextView current_user_name;
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
    initializeSpinner();

    login_group.setOnClickListener(v -> {
      Timber.d("login_group onClick");
      Intent intent = new Intent(getContext(), LoginToutActivity.class);
      getContext().startActivity(intent);
    });
  }

  protected void toggleLogin() {
    if (User.haveCurrent()) {
      login_group.setVisibility(INVISIBLE);
      current_user_group.setVisibility(VISIBLE);
      current_user_name.setText(User.current().name());
    } else {
      current_user_group.setVisibility(INVISIBLE);
      login_group.setVisibility(VISIBLE);
    }
  }

  protected void initializeSpinner() {
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
      R.array.categories_array,
      android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);

    // onItemSelected will fire immediately with the default selection
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
        String item = spinner.getItemAtPosition(position).toString();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });
  }
}
