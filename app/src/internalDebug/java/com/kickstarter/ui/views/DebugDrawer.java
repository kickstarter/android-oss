package com.kickstarter.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.EnumAdapter;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.qualifiers.ApiEndpointPreference;

import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

public class DebugDrawer extends FrameLayout {
  @Inject @ApiEndpointPreference StringPreference apiEndpointPreference;
  @Inject Build build;
  @Inject Logout logout;

  @Bind(R.id.build_date) TextView buildDate;
  @Bind(R.id.endpoint_spinner) Spinner endpointSpinner;
  @Bind(R.id.sha) TextView sha;
  @Bind(R.id.variant) TextView variant;
  @Bind(R.id.version_code) TextView versionCode;
  @Bind(R.id.version_name) TextView versionName;
  @BindDrawable(android.R.drawable.ic_dialog_alert) Drawable icDialogAlertDrawable;

  public DebugDrawer(@NonNull final Context context) {
    this(context, null);
  }

  public DebugDrawer(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DebugDrawer(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    ((KSApplication) getContext().getApplicationContext()).component().inject(this);

    LayoutInflater.from(context).inflate(R.layout.debug_drawer_view, this);
    ButterKnife.bind(this);

    setupNetworkSection();
    setupBuildInformationSection();
  }

  private void setupNetworkSection() {
    final ApiEndpoint currentApiEndpoint = ApiEndpoint.from(apiEndpointPreference.get());
    final EnumAdapter<ApiEndpoint> endpointAdapter =
      new EnumAdapter<>(getContext(), ApiEndpoint.class);
    endpointSpinner.setAdapter(endpointAdapter);
    endpointSpinner.setSelection(currentApiEndpoint.ordinal());
    endpointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(@NonNull final AdapterView<?> adapterView, @NonNull final View view,
        final int position, final long id) {
        final ApiEndpoint selected = endpointAdapter.getItem(position);
        if (selected != currentApiEndpoint) {
          if (selected == ApiEndpoint.CUSTOM) {
            showCustomEndpointDialog(currentApiEndpoint.ordinal(), "https://");
          } else {
            setEndpointAndRelaunch(selected.url);
          }
        }
      }

      @Override
      public void onNothingSelected(@NonNull final AdapterView<?> adapterView) {}
    });
  }

  private void setupBuildInformationSection() {
    buildDate.setText(build.dateTime().toString(DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss aa zzz")));
    sha.setText(build.sha());
    variant.setText(build.variant());
    versionCode.setText(build.versionCode().toString());
    versionName.setText(build.versionName());
  }

  private void showCustomEndpointDialog(final int originalSelection, @NonNull final String defaultUrl) {
    final View view = LayoutInflater.from(getContext().getApplicationContext()).inflate(R.layout.api_endpoint_layout, null);
    final EditText url = ButterKnife.findById(view, R.id.url);
    url.setText(defaultUrl);
    url.setSelection(url.length());

    new AlertDialog.Builder(getContext())
      .setTitle("Set API Endpoint")
      .setView(view)
      .setPositiveButton(android.R.string.yes, (dialog, which) -> {
        String inputUrl = url.getText().toString();
        if (!Strings.isNullOrEmpty(inputUrl)) {
          // Remove trailing '/'
          if (inputUrl.charAt(inputUrl.length() - 1) == '/') {
            inputUrl = inputUrl.substring(0, inputUrl.length() - 1);
          }
          setEndpointAndRelaunch(inputUrl);
        } else {
          endpointSpinner.setSelection(originalSelection);
        }
      })
      .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        endpointSpinner.setSelection(originalSelection);
        dialog.cancel();
      })
      .setOnCancelListener(dialogInterface -> {
        // TODO: Is this redundant?
        endpointSpinner.setSelection(originalSelection);
      })
      .setIcon(icDialogAlertDrawable)
      .show();
  }

  private void setEndpointAndRelaunch(@NonNull final String endpoint) {
    apiEndpointPreference.set(endpoint);
    logout.execute();
    ProcessPhoenix.triggerRebirth(getContext());
  }
}
