package com.kickstarter.ui.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.EnumAdapter;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.Release;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.qualifiers.ApiEndpointPreference;
import com.kickstarter.models.User;

import org.joda.time.format.DateTimeFormat;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DebugDrawer extends FrameLayout {
  @Inject @ApiEndpointPreference StringPreference apiEndpointPreference;
  @Inject Release release;
  @Inject CurrentUser currentUser;
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

  @OnClick(R.id.push_notifications_button)
  public void pushNotificationsButtonClick() {
    final View view = LayoutInflater.from(getContext().getApplicationContext()).inflate(R.layout.debug_push_notifications_layout, null);

    new AlertDialog.Builder(getContext())
      .setTitle("Push notifications")
      .setView(view)
      .show();
  }

  @OnClick(R.id.submit_bug_report_button)
  public void submitBugReportButtonClick() {
    currentUser.observable().take(1).subscribe(this::submitBugReport);
  }

  private void submitBugReport(@Nullable final User user) {
    final Context context = getContext();

    final String email = "chrstphrwrght+21qbymyz894ttajaomwh@***REMOVED***";

    final List<String> debugInfo = Arrays.asList(
      (user != null ? user.name() : "Logged Out"),
      release.variant(),
      release.versionName(),
      release.versionCode().toString(),
      release.sha(),
      Integer.toString(Build.VERSION.SDK_INT),
      Build.MANUFACTURER + " " + Build.MODEL,
      Locale.getDefault().getLanguage()
    );

    final String body = new StringBuilder()
      .append(TextUtils.join(" | ", debugInfo))
      .append("\r\n\r\nDescribe the bug and add a subject. Attach images if it helps!\r\n")
      .append("—————————————\r\n")
      .toString();

    final Intent intent = new Intent(android.content.Intent.ACTION_SEND)
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
      .setType("message/rfc822")
      .putExtra(Intent.EXTRA_TEXT, body)
      .putExtra(Intent.EXTRA_EMAIL, new String[]{email});

    context.startActivity(Intent.createChooser(intent, context.getString(R.string.___Select_email_application)));
  }

  private void setupNetworkSection() {
    final ApiEndpoint currentApiEndpoint = ApiEndpoint.from(apiEndpointPreference.get());
    final EnumAdapter<ApiEndpoint> endpointAdapter =
      new EnumAdapter<>(getContext(), ApiEndpoint.class, false, R.layout.white_spinner_item);
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
    buildDate.setText(release.dateTime().toString(DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss aa zzz")));
    sha.setText(release.sha());
    variant.setText(release.variant());
    versionCode.setText(release.versionCode().toString());
    versionName.setText(release.versionName());
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
        if (inputUrl.length() > 0) {
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
