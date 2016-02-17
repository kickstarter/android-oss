package com.kickstarter.ui.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.EnumAdapter;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.Release;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.qualifiers.ApiEndpointPreference;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.models.User;
import com.kickstarter.ui.viewmodels.InternalToolsViewModel;

import org.joda.time.format.DateTimeFormat;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresViewModel(InternalToolsViewModel.class)
public final class InternalToolsActivity extends BaseActivity<InternalToolsViewModel> {
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

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.internal_tools_layout);
    ButterKnife.bind(this);

    ((KSApplication) getApplicationContext()).component().inject(this);

    setupNetworkSection();
    setupBuildInformationSection();
  }

  @OnClick(R.id.playground_button)
  public void playgroundClick() {
    startActivity(new Intent(this, PlaygroundActivity.class));
  }

  @OnClick(R.id.push_notifications_button)
  public void pushNotificationsButtonClick() {
    final View view = LayoutInflater.from(this).inflate(R.layout.debug_push_notifications_layout, null);

    new AlertDialog.Builder(this)
      .setTitle("Push notifications")
      .setView(view)
      .show();
  }

  @OnClick(R.id.submit_bug_report_button)
  public void submitBugReportButtonClick() {
    currentUser.observable().take(1).subscribe(this::submitBugReport);
  }

  private void submitBugReport(final @Nullable User user) {
    final String email = "chrstphrwrght+21qbymyz894ttajaomwh@***REMOVED***";

    final List<String> debugInfo = Arrays.asList(
      user != null ? user.name() : "Logged Out",
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

    startActivity(Intent.createChooser(intent, getString(R.string.Select_email_application)));
  }

  private void setupNetworkSection() {
    final ApiEndpoint currentApiEndpoint = ApiEndpoint.from(apiEndpointPreference.get());
    final EnumAdapter<ApiEndpoint> endpointAdapter =
      new EnumAdapter<>(this, ApiEndpoint.class, false, R.layout.black_spinner_item);
    endpointSpinner.setAdapter(endpointAdapter);
    endpointSpinner.setSelection(currentApiEndpoint.ordinal());
    endpointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(final @NonNull AdapterView<?> adapterView, final @NonNull View view,
        final int position, final long id) {
        final ApiEndpoint selected = endpointAdapter.getItem(position);
        if (selected != currentApiEndpoint) {
          if (selected == ApiEndpoint.CUSTOM) {
            showCustomEndpointDialog(currentApiEndpoint.ordinal(), "https://");
          } else {
            setEndpointAndRelaunch(selected.url());
          }
        }
      }

      @Override
      public void onNothingSelected(final @NonNull AdapterView<?> adapterView) {}
    });
  }

  private void setupBuildInformationSection() {
    buildDate.setText(release.dateTime().toString(DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss aa zzz")));
    sha.setText(release.sha());
    variant.setText(release.variant());
    versionCode.setText(release.versionCode().toString());
    versionName.setText(release.versionName());
  }

  private void showCustomEndpointDialog(final int originalSelection, final @NonNull String defaultUrl) {
    final View view = LayoutInflater.from(this).inflate(R.layout.api_endpoint_layout, null);
    final EditText url = ButterKnife.findById(view, R.id.url);
    url.setText(defaultUrl);
    url.setSelection(url.length());

    new AlertDialog.Builder(this)
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

  private void setEndpointAndRelaunch(final @NonNull String endpoint) {
    apiEndpointPreference.set(endpoint);
    logout.execute();
    ProcessPhoenix.triggerRebirth(this);
  }

  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
