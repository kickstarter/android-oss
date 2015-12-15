package com.kickstarter.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.viewmodels.HelpViewModel;
import com.kickstarter.ui.views.KSWebView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

@RequiresViewModel(HelpViewModel.class)
public final class HelpActivity extends BaseActivity<HelpViewModel> {
  public static final int HELP_TYPE_TERMS = 0;
  public static final int HELP_TYPE_PRIVACY = 1;
  public static final int HELP_TYPE_HOW_IT_WORKS = 2;
  public static final int HELP_TYPE_COOKIE_POLICY = 3;
  public static final int HELP_TYPE_FAQ = 4;
  public static final int HELP_TYPE_GENERAL = 5;

  @IntDef({HELP_TYPE_TERMS, HELP_TYPE_PRIVACY, HELP_TYPE_HOW_IT_WORKS, HELP_TYPE_COOKIE_POLICY, HELP_TYPE_FAQ, HELP_TYPE_GENERAL})
  @Retention(RetentionPolicy.SOURCE)
  public @interface HelpType {}

  @Bind(R.id.kickstarter_web_view) KSWebView kickstarterWebView;

  @Inject @WebEndpoint String webEndpoint;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ((KSApplication) getApplicationContext()).component().inject(this);
    setContentView(R.layout.help_layout);
    ButterKnife.bind(this);

    @HelpType int helpType = getIntent().getExtras().getInt(getString(R.string.intent_help_type));
    final String url = getUrlForHelpType(helpType);
    kickstarterWebView.loadUrl(url);
  }

  protected String getUrlForHelpType(@HelpType final int helpType) {
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
        builder.appendEncodedPath("help/faq/kickstarter+basics?ref=faq_nav#TheKickApp");
        break;
      case HELP_TYPE_GENERAL:
        builder.appendEncodedPath("help");
    }
    return builder.toString();
  }
}
