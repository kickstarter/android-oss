package com.kickstarter.ui.activities;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.viewmodels.DownloadBetaViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@RequiresActivityViewModel(DownloadBetaViewModel.class)
public final class DownloadBetaActivity extends BaseActivity<DownloadBetaViewModel> {
  protected @Bind(R.id.build) TextView buildTextView;
  protected @Bind(R.id.changelog) TextView changelogTextView;

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.download_beta_layout);
    ButterKnife.bind(this);

    final Observable<String> build = this.viewModel.outputs.internalBuildEnvelope()
      .map(InternalBuildEnvelope::build)
      .filter(ObjectUtils::isNotNull)
      .map(Object::toString);

    build
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.buildTextView::setText);

    build
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::requestDownload);

    this.viewModel.outputs.internalBuildEnvelope()
      .map(InternalBuildEnvelope::changelog)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.changelogTextView::setText);
  }

  @OnClick(R.id.open_downloads_button)
  public void openDownloadsOnClick(final @NonNull View v) {
    final Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
    startActivity(intent);
  }

  private void requestDownload(final @NonNull String build) {
    final Intent intent = new Intent(Intent.ACTION_VIEW)
      .setData(Uri.parse("https://www.kickstarter.com/mobile/beta/builds/" + build));
    startActivity(intent);
  }
}
