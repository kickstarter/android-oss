package com.kickstarter.ui.activities;

import android.net.Uri;
import android.os.Bundle;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.HelpViewModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.ButterKnife;

@RequiresActivityViewModel(HelpViewModel.class)
public class HelpActivity extends BaseActivity<HelpViewModel> {
  public static final int HELP_TYPE_TERMS = 0;
  public static final int HELP_TYPE_PRIVACY = 1;
  public static final int HELP_TYPE_HOW_IT_WORKS = 2;
  public static final int HELP_TYPE_COOKIE_POLICY = 3;
  public static final int HELP_TYPE_ACCESSIBILITY = 4;
  public static final String TERMS_OF_USE = "terms-of-use";
  public static final String PRIVACY = "privacy";
  public static final String HELLO = "hello";
  public static final String COOKIES = "cookies";
  public static final String ACCESSIBILITY = "accessibility";

  @IntDef({HELP_TYPE_TERMS, HELP_TYPE_PRIVACY, HELP_TYPE_HOW_IT_WORKS, HELP_TYPE_COOKIE_POLICY, HELP_TYPE_ACCESSIBILITY})
  @Retention(RetentionPolicy.SOURCE)
  public @interface HelpType {}

  private @HelpType int helpType;

  protected @Bind(R.id.kickstarter_web_view) KSWebView kickstarterWebView;

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

  public static class AccessibilityStatement extends HelpActivity {
    public AccessibilityStatement() {
      helpType(HELP_TYPE_ACCESSIBILITY);
    }
  }

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.help_layout);
    ButterKnife.bind(this);
    this.webEndpoint = environment().webEndpoint();

    final String url = getUrlForHelpType(this.helpType);
    this.kickstarterWebView.loadUrl(url);
  }

  protected String getUrlForHelpType(final @HelpType int helpType) {
    final Uri.Builder builder = Uri.parse(this.webEndpoint).buildUpon();
    switch (helpType) {
      case HELP_TYPE_TERMS:
        builder.appendEncodedPath(TERMS_OF_USE);
        break;
      case HELP_TYPE_PRIVACY:
        builder.appendEncodedPath(PRIVACY);
        break;
      case HELP_TYPE_HOW_IT_WORKS:
        builder.appendEncodedPath(HELLO);
        break;
      case HELP_TYPE_COOKIE_POLICY:
        builder.appendEncodedPath(COOKIES);
        break;
    }
    return builder.toString();
  }
}
