package com.kickstarter.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiEndpoints;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.EnumAdapter;

import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DebugView extends FrameLayout {
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
    final EnumAdapter<ApiEndpoints> endpointAdapter =
      new EnumAdapter<>(getContext(), ApiEndpoints.class);
    // TODO: Set current selection
    endpointSpinner.setAdapter(endpointAdapter);
  }

  private void setupBuildInformationSection() {
    buildDate.setText(build.dateTime().toString(DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss aa zzz")));
    sha.setText(build.sha());
    variant.setText(build.variant());
    versionCode.setText(build.versionCode().toString());
    versionName.setText(build.versionName());
  }
}
