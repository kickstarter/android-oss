package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.WebViewViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(WebViewViewModel.ViewModel.class)
public final class WebViewActivity extends BaseActivity<WebViewViewModel.ViewModel> {
  protected @Bind(R.id.web_view_toolbar) KSToolbar toolbar;
  protected @Bind(R.id.web_view) KSWebView webView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_view_layout);
    ButterKnife.bind(this);

    this.viewModel.outputs.toolbarTitle()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.toolbar::setTitle);

    this.viewModel.outputs.url()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.webView::loadUrl);
  }

  @Override
  public void back() {
    // This logic is sound only for web view activities without RequestHandlers.
    // TODO: Refactor the client to update web history properly for activities with RequestHandlers.
    if (this.webView.canGoBack()) {
      this.webView.goBack();
    } else {
      super.back();
    }
  }

  @Override
  protected @NonNull Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
