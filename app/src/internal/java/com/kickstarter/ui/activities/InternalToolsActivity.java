package com.kickstarter.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.preferences.StringPreferenceType;
import com.kickstarter.libs.qualifiers.ApiEndpointPreference;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.Secrets;
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

@RequiresActivityViewModel(InternalToolsViewModel.class)
public final class InternalToolsActivity extends BaseActivity<InternalToolsViewModel> {
  private CurrentUserType currentUser;

  @Inject @ApiEndpointPreference StringPreferenceType apiEndpointPreference;
  @Inject Build build;
  @Inject Logout logout;

  @Bind(R.id.build_date) TextView buildDate;
  @Bind(R.id.sha) TextView sha;
  @Bind(R.id.variant) TextView variant;
  @Bind(R.id.version_code) TextView versionCode;
  @Bind(R.id.version_name) TextView versionName;
  @Bind(R.id.test_apollo) Button testApolloButton;
  @BindDrawable(android.R.drawable.ic_dialog_alert) Drawable icDialogAlertDrawable;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.internal_tools_layout);
    ButterKnife.bind(this);

    ((KSApplication) getApplicationContext()).component().inject(this);

    this.currentUser = environment().currentUser();

    setupBuildInformationSection();

    this.testApolloButton.setText("Apollo POC");
  }

  @OnClick(R.id.playground_button)
  public void playgroundButtonClicked() {
    final Intent intent = new Intent(this, PlaygroundActivity.class);
    startActivity(intent);
  }

  @OnClick(R.id.push_notifications_button)
  public void pushNotificationsButtonClick() {
    final View view = View.inflate(this, R.layout.debug_push_notifications_layout, null);

    new AlertDialog.Builder(this)
      .setTitle("Push notifications")
      .setView(view)
      .show();
  }

  @OnClick(R.id.change_endpoint_custom_button)
  public void changeEndpointCustomButton() {
    showCustomEndpointDialog();
  }

  @OnClick(R.id.change_endpoint_hivequeen_button)
  public void changeEndpointHivequeenButton() {
    showHivequeenEndpointDialog();
  }

  @OnClick(R.id.change_endpoint_staging_button)
  public void changeEndpointStagingButton() {
    setEndpointAndRelaunch(ApiEndpoint.STAGING);
  }

  @OnClick(R.id.change_endpoint_production_button)
  public void changeEndpointProductionButton() {
    setEndpointAndRelaunch(ApiEndpoint.PRODUCTION);
  }

  @OnClick(R.id.submit_bug_report_button)
  public void submitBugReportButtonClick() {
    this.currentUser.observable().take(1).subscribe(this::submitBugReport);
  }

  @OnClick(R.id.test_apollo)
  public void testApolloButtonClick() {
    startActivity(new Intent(this, TestApolloActivity.class));
  }

  private void submitBugReport(final @Nullable User user) {
    final String email = Secrets.FIELD_REPORT_EMAIL;

    final List<String> debugInfo = Arrays.asList(
      user != null ? user.name() : "Logged Out",
      this.build.variant(),
      this.build.versionName(),
      this.build.versionCode().toString(),
      this.build.sha(),
      Integer.toString(android.os.Build.VERSION.SDK_INT),
      android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL,
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

  private void showCustomEndpointDialog() {
    final View view = View.inflate(this, R.layout.custom_endpoint_layout, null);
    final EditText customEndpointEditText = ButterKnife.findById(view, R.id.custom_endpoint_edit_text);

    new AlertDialog.Builder(this)
      .setTitle("Change endpoint")
      .setView(view)
      .setPositiveButton(android.R.string.yes, (dialog, which) -> {
        final String url = customEndpointEditText.getText().toString();
        if (URLUtil.isValidUrl(url)) {
          setEndpointAndRelaunch(ApiEndpoint.from(url));
        }
      })
      .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        dialog.cancel();
      })
      .setIcon(this.icDialogAlertDrawable)
      .show();
  }

  private void showHivequeenEndpointDialog() {
    final View view = View.inflate(this, R.layout.hivequeen_endpoint_layout, null);
    final EditText hivequeenNameEditText = ButterKnife.findById(view, R.id.hivequeen_name_edit_text);

    new AlertDialog.Builder(this)
      .setTitle("Change endpoint")
      .setView(view)
      .setPositiveButton(android.R.string.yes, (dialog, which) -> {
        final String hivequeenName = hivequeenNameEditText.getText().toString();
        if (hivequeenName.length() > 0) {
          setEndpointAndRelaunch(ApiEndpoint.from(Secrets.Api.Endpoint.hqHost(hivequeenName)));
        }
      })
      .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        dialog.cancel();
      })
      .setIcon(this.icDialogAlertDrawable)
      .show();
  }

  @SuppressLint("SetTextI18n")
  private void setupBuildInformationSection() {
    this.buildDate.setText(this.build.dateTime().toString(DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss aa zzz")));
    this.sha.setText(this.build.sha());
    this.variant.setText(this.build.variant());
    this.versionCode.setText(this.build.versionCode().toString());
    this.versionName.setText(this.build.versionName());
  }

  private void setEndpointAndRelaunch(final @NonNull ApiEndpoint apiEndpoint) {
    this.apiEndpointPreference.set(apiEndpoint.url());
    this.logout.execute();
    try {
      Thread.sleep(500L);
    } catch (InterruptedException ignored) {

    }
    ProcessPhoenix.triggerRebirth(this);
  }

  protected @NonNull Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
