package com.kickstarter.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.HelpViewModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

@RequiresActivityViewModel(HelpViewModel.class)
public class HelpActivity extends BaseActivity<HelpViewModel> implements KSWebViewClient.Delegate {
  public static final int HELP_TYPE_TERMS = 0;
  public static final int HELP_TYPE_PRIVACY = 1;
  public static final int HELP_TYPE_HOW_IT_WORKS = 2;
  public static final int HELP_TYPE_COOKIE_POLICY = 3;
  public static final int HELP_TYPE_FAQ = 4;

  @IntDef({HELP_TYPE_TERMS, HELP_TYPE_PRIVACY, HELP_TYPE_HOW_IT_WORKS, HELP_TYPE_COOKIE_POLICY, HELP_TYPE_FAQ})
  @Retention(RetentionPolicy.SOURCE)
  public @interface HelpType {}

  private @HelpType int helpType;

  protected @Bind(R.id.kickstarter_web_view) KSWebView kickstarterWebView;
  protected @Bind(R.id.loading_indicator_view) View loadingIndicatorView;

  @Inject @WebEndpoint String webEndpoint;

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

  public static class HowItWorks extends HelpActivity {
    public HowItWorks() {
      helpType(HELP_TYPE_HOW_IT_WORKS);
    }
  }

  public static class CookiePolicy extends HelpActivity {
    public CookiePolicy() {
      helpType(HELP_TYPE_COOKIE_POLICY);
    }
  }

  public static class Faq extends HelpActivity {
    public Faq() {
      helpType(HELP_TYPE_FAQ);
    }
  }

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ((KSApplication) getApplicationContext()).component().inject(this);
    setContentView(R.layout.help_layout);
    ButterKnife.bind(this);

    final String url = getUrlForHelpType(this.helpType);
    kickstarterWebView.loadUrl(url);
    kickstarterWebView.client().setDelegate(this);
  }

  protected String getUrlForHelpType(final @HelpType int helpType) {
    final Uri.Builder builder = Uri.parse(webEndpoint).buildUpon();
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
      case HELP_TYPE_FAQ:
        builder.appendEncodedPath("help/faq/kickstarter+basics");
        break;
    }
    return builder.toString();
  }

  @Override
  public void webViewOnPageStarted(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    final AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
    animation.setDuration(300L);
    animation.setFillAfter(true);
    loadingIndicatorView.startAnimation(animation);
  }

  @Override
  public void webViewOnPageFinished(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    final AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
    animation.setDuration(300L);
    animation.setFillAfter(true);
    loadingIndicatorView.startAnimation(animation);
  }

  @Override
  public void webViewPageIntercepted(final @NonNull KSWebViewClient webViewClient, final @NonNull String url) {}
}
