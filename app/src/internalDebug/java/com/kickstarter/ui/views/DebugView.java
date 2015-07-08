package com.kickstarter.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.EnumAdapter;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.qualifiers.ApiEndpointPreference;

import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DebugView extends FrameLayout {
  @Inject @ApiEndpointPreference StringPreference apiEndpointPreference;
  @Inject Build build;

  @InjectView(R.id.build_date) TextView buildDate;
  @InjectView(R.id.endpoint_spinner) Spinner endpointSpinner;
  @InjectView(R.id.sha) TextView sha;
  @InjectView(R.id.variant) TextView variant;
  @InjectView(R.id.version_code) TextView versionCode;
  @InjectView(R.id.version_name) TextView versionName;

  public DebugView(final Context context) {
    this(context, null);
  }

  public DebugView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DebugView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    ((KsrApplication) getContext().getApplicationContext()).component().inject(this);

    LayoutInflater.from(context).inflate(R.layout.debug_view, this);
    ButterKnife.inject(this);

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
      public void onItemSelected(final AdapterView<?> adapterView, final View view, final int position, final long id) {
        final ApiEndpoint selected = endpointAdapter.getItem(position);
        if (selected != currentApiEndpoint) {
          if (selected == ApiEndpoint.CUSTOM) {
            // TODO: Show custom endpoint dialog
          } else {
            setEndpointAndRelaunch(selected.url);
          }
        }
      }

      @Override
      public void onNothingSelected(final AdapterView<?> adapterView) {}
    });
  }

  private void setupBuildInformationSection() {
    buildDate.setText(build.dateTime().toString(DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss aa zzz")));
    sha.setText(build.sha());
    variant.setText(build.variant());
    versionCode.setText(build.versionCode().toString());
    versionName.setText(build.versionName());
  }

  private void setEndpointAndRelaunch(final String endpoint) {
    apiEndpointPreference.set(endpoint);
    ProcessPhoenix.triggerRebirth(getContext());
  }
}
