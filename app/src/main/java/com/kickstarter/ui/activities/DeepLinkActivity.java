package com.kickstarter.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ApplicationUtils;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.viewmodels.DeepLinkViewModel;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(DeepLinkViewModel.ViewModel.class)
public class DeepLinkActivity extends BaseActivity<DeepLinkViewModel.ViewModel> {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.empty_view);

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

  private void startProjectActivity(Uri uri) {
    final Intent projectIntent = new Intent(this, ProjectActivity.class)
      .setData(uri)
      .putExtra(IntentKey.REF_TAG, RefTag.from(uri.getQueryParameter("ref")));
    startActivity(projectIntent);
    finish();
  }
}
