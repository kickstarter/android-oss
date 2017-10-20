package com.kickstarter.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ApplicationUtils;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.viewmodels.DeepLinkViewModel;

import java.util.List;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(DeepLinkViewModel.ViewModel.class)
public class DeepLinkActivity extends BaseActivity<DeepLinkViewModel.ViewModel> {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.viewModel.outputs.startDiscoveryActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> startDiscoveryActivity());

    this.viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startProjectActivity);

    this.viewModel.outputs.startBrowser()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startBrowser);

    this.viewModel.outputs.requestPackageManager()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::inputPackageManager);
  }

  private void startDiscoveryActivity() {
    ApplicationUtils.startNewDiscoveryActivity(this);
    finish();
  }

  private void startProjectActivity(String url) {
    Uri uri = Uri.parse(url);
    final Intent projectIntent = new Intent(this, ProjectActivity.class)
      .setData(uri);
    String ref = uri.getQueryParameter("ref");
    if (ref != null) {
      projectIntent.putExtra(IntentKey.REF_TAG, RefTag.from(ref));
    }
    startActivity(projectIntent);
    finish();
  }

  private void startBrowser(List<Intent> targetIntents) {
    // Now present the user with the list of apps we have found (this chooser
    // is smart enough to just open a single option directly, so we don't need
    // to handle that case).
    Intent chooserIntent = Intent.createChooser(targetIntents.remove(0), "");
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
      targetIntents.toArray(new Parcelable[targetIntents.size()]));
    startActivity(chooserIntent);

    finish();
  }

  private void inputPackageManager(String url) {
    if (!TextUtils.isEmpty(url)) {
      this.viewModel.inputs.packageManager(getPackageManager());
    }
  }
}
