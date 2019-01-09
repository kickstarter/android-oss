package com.kickstarter.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.libs.utils.AnimationUtils;
import com.kickstarter.libs.utils.chrome.CustomTabActivityHelper;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.HelpViewModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import butterknife.Bind;
import butterknife.ButterKnife;

@RequiresActivityViewModel(HelpViewModel.class)
public class HelpActivity extends BaseActivity<HelpViewModel> implements KSWebViewClient.Delegate {
  public static final int HELP_TYPE_TERMS = 0;
  public static final int HELP_TYPE_PRIVACY = 1;
  public static final int HELP_TYPE_HOW_IT_WORKS = 2;
  public static final int HELP_TYPE_COOKIE_POLICY = 3;

  @IntDef({HELP_TYPE_TERMS, HELP_TYPE_PRIVACY, HELP_TYPE_HOW_IT_WORKS, HELP_TYPE_COOKIE_POLICY})
  @Retention(RetentionPolicy.SOURCE)
  public @interface HelpType {}

  private @HelpType int helpType;

  protected @Bind(R.id.kickstarter_web_view) KSWebView kickstarterWebView;
  protected @Bind(R.id.loading_indicator_view) View loadingIndicatorView;

  private @WebEndpoint String webEndpoint;

  protected void helpType(final @HelpType int helpType) {
    this.helpType = helpType;
  }

  public static class Terms extends HelpActivity {
    public Terms() {
      helpType(HELP_TYPE_TERMS);
    }
  }

  public static class Privacy extends HelpActivity {
    public Privacy() {
      helpType(HELP_TYPE_PRIVACY);
    }
  }

  public static class CookiePolicy extends HelpActivity {
    public CookiePolicy() {
      helpType(HELP_TYPE_COOKIE_POLICY);
    }
  }

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.help_layout);
    ButterKnife.bind(this);
    this.webEndpoint = environment().webEndpoint();

    final String url = getUrlForHelpType(this.helpType);
//    this.kickstarterWebView.loadUrl(url);
//    this.kickstarterWebView.client().setDelegate(this);

    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_right);
    builder.setCloseButtonIcon(icon);
    builder.setShowTitle(true);
    builder.setToolbarColor(ContextCompat.getColor(this, R.color.white));
    builder.setExitAnimations(this, R.anim.settings_slide_in_from_bottom, R.anim.settings_slide_out_from_top);
    CustomTabsIntent customTabsIntent = builder.build();


    CustomTabActivityHelper.Companion.openCustomTab(this, customTabsIntent, Uri.parse(url), (activity, uri) -> {
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      activity.startActivity(intent);
    });

  }

  protected String getUrlForHelpType(final @HelpType int helpType) {
    final Uri.Builder builder = Uri.parse(this.webEndpoint).buildUpon();
    switch (helpType) {
      case HELP_TYPE_TERMS:
        builder.appendEncodedPath("terms-of-use");
        break;
      case HELP_TYPE_PRIVACY:
        builder.appendEncodedPath("privacy");
        break;
      case HELP_TYPE_HOW_IT_WORKS:
        builder.appendEncodedPath("hello");
        break;
      case HELP_TYPE_COOKIE_POLICY:
        builder.appendEncodedPath("cookies");
        break;
    }
    return builder.toString();
  }

  @Override
  public void webViewExternalLinkActivated(final @NonNull KSWebViewClient webViewClient, final @NonNull String url) {}

  @Override
  public void webViewOnPageStarted(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    this.loadingIndicatorView.startAnimation(AnimationUtils.INSTANCE.appearAnimation());
  }

  @Override
  public void webViewOnPageFinished(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    this.loadingIndicatorView.startAnimation(AnimationUtils.INSTANCE.disappearAnimation());
  }

  @Override
  public void webViewPageIntercepted(final @NonNull KSWebViewClient webViewClient, final @NonNull String url) {}
}
