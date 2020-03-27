package com.kickstarter.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.preferences.StringPreferenceType;
import com.kickstarter.libs.qualifiers.ApiEndpointPreference;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.viewmodels.InternalToolsViewModel;

import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(InternalToolsViewModel.class)
public final class InternalToolsActivity extends BaseActivity<InternalToolsViewModel> {

  @Inject @ApiEndpointPreference StringPreferenceType apiEndpointPreference;
  @Inject Build build;
  @Inject Logout logout;

  @Bind(R.id.api_endpoint) TextView apiEndpoint;
  @Bind(R.id.build_date) TextView buildDate;
  @Bind(R.id.commit_sha) TextView commitSha;
  @Bind(R.id.device_id) TextView deviceID;
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

    setupBuildInformationSection();
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

  @OnClick(R.id.crash_button)
  public void crashButtonClicked() {
    throw new RuntimeException("Forced a crash!");
  }

  @OnClick(R.id.feature_flags_button)
  public void featureFlagsClick() {
    final Intent featureFlagIntent = new Intent(this, FeatureFlagsActivity.class);
    startActivity(featureFlagIntent);
  }

  @OnClick(R.id.reset_device_id)
  public void resetDeviceIdClick() {
    //FirebaseInstanceId.getInstance().deleteInstanceId();
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
    this.apiEndpoint.setText(this.apiEndpointPreference.get());
    this.buildDate.setText(this.build.buildDate().toString(DateTimeFormat.forPattern("MMM dd, yyyy h:mm:ss aa zzz")));
    this.commitSha.setText(this.build.sha());
    this.deviceID.setText(FirebaseInstanceId.getInstance().getId());
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
