package com.kickstarter.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ApplicationUtils;
import com.kickstarter.libs.utils.UrlUtils;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.viewmodels.DeepLinkViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(DeepLinkViewModel.ViewModel.class)
public final class DeepLinkActivity extends BaseActivity<DeepLinkViewModel.ViewModel> {
  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.viewModel.outputs.startBrowser()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startBrowser);

    this.viewModel.outputs.startDiscoveryActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> startDiscoveryActivity());

    this.viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startProjectActivity);
  }

  private void startDiscoveryActivity() {
    ApplicationUtils.startNewDiscoveryActivity(this);
    finish();
  }

  private void startProjectActivity(final @NonNull Uri uri) {
    final Intent projectIntent = new Intent(this, ProjectActivity.class)
      .setData(uri);
    final String ref = UrlUtils.INSTANCE.refTag(uri.toString());
    if (ref != null) {
      projectIntent.putExtra(IntentKey.REF_TAG, RefTag.from(ref));
    }
    startActivity(projectIntent);
    finish();
  }

  private void startBrowser(final @NonNull String url) {
    ApplicationUtils.openUrlExternally(this, url);
    finish();
  }
}
