package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.ui.views.KSWebView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DisplayWebViewActivity extends BaseActivity {
  @Bind(R.id.close_text_view) TextView closeTextView;
  @Bind(R.id.generic_webview) KSWebView webView;

  public static final int RIGHT_BAR_BUTTON_NONE = 0;
  public static final int RIGHT_BAR_BUTTON_CLOSE = 1;

  @IntDef({RIGHT_BAR_BUTTON_NONE, RIGHT_BAR_BUTTON_CLOSE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface RightBarButton {}

  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_view_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    final String url = intent.getExtras().getString(getString(R.string.intent_url));
    final int rightBarButton = intent.getExtras().getInt(getString(R.string.intent_right_bar_button), RIGHT_BAR_BUTTON_NONE);

    switch (rightBarButton) {
      case RIGHT_BAR_BUTTON_CLOSE:
        closeTextView.setVisibility(View.VISIBLE);
        break;
      case RIGHT_BAR_BUTTON_NONE:
        closeTextView.setVisibility(View.GONE);
        break;
    }

    webView.loadUrl(url);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overrideExitTransition();
  }

  @OnClick(R.id.close_text_view)
  public void closeTextViewOnClick() {
    finish();
    overrideExitTransition();
  }

  private void overrideExitTransition() {
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
