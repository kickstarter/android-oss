package com.kickstarter.ui.activities;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.services.ApiResponses.InternalBuildEnvelope;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DownloadBetaActivity extends AppCompatActivity {
  @Bind(R.id.build) TextView buildTextView;
  @Bind(R.id.changelog) TextView changelogTextView;
  InternalBuildEnvelope internalBuildEnvelope;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.download_beta_layout);
    ButterKnife.bind(this);

    internalBuildEnvelope = getIntent().getExtras()
      .getParcelable(getString(R.string.intent_internal_build_envelope));

    buildTextView.setText(internalBuildEnvelope.build().toString());
    changelogTextView.setText(internalBuildEnvelope.changelog());

    requestDownload();
  }

  public void openDownloadsOnClick(final View v) {
    final Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
    startActivity(intent);
  }

  private void requestDownload() {
    final Intent intent = new Intent(Intent.ACTION_VIEW)
      .setData(Uri.parse("https://www.kickstarter.com/mobile/beta/builds/" + internalBuildEnvelope.build()));
    startActivity(intent);
  }
}
