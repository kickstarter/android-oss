package com.kickstarter.ui.activities;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.services.ApiResponses.InternalBuildEnvelope;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

public class DownloadBetaActivity extends AppCompatActivity {
  @InjectView(R.id.build) TextView build;
  @InjectView(R.id.changelog) TextView changelog;
  @InjectView(R.id.open_downloads_button) Button openDownloadsButton;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.download_beta_layout);
    ButterKnife.inject(this);

    final Intent intent = getIntent();
    final InternalBuildEnvelope envelope = intent.getExtras().getParcelable("internalBuildEnvelope");

    build.setText(envelope.build().toString());
    changelog.setText(envelope.changelog());

    requestDownload();
  }

  public void openDownloadsOnClick(final View v) {
    Timber.d("openDownloadsOnClick triggered");
    final Intent i = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
    startActivity(i);
  }

  private void requestDownload() {
    final Intent intent = new Intent(Intent.ACTION_VIEW)
      .setData(Uri.parse("http://ksr.10.0.3.2.xip.io/mobile/beta/newest"));
    startActivity(intent);
  }
}
