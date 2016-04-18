package com.kickstarter.ui;

public final class IntentKey {
  private IntentKey() {}

  /*
   * Names require package prefix:
   * http://developer.android.com/reference/android/content/Intent.html#putExtra%28java.lang.String,%20boolean%29
   */
  public static final String DISCOVERY_PARAMS = "com.kickstarter.kickstarter.intent_discovery_params";
  public static final String EMAIL = "com.kickstarter.kickstarter.intent_email";
  public static final String FACEBOOK_LOGIN = "com.kickstarter.kickstarter.intent_facebook_login";
  public static final String FACEBOOK_TOKEN = "com.kickstarter.kickstarter.intent_facebook_token";
  public static final String FACEBOOK_USER = "com.kickstarter.kickstarter.intent_facebook_user";
  public static final String INTERNAL_BUILD_ENVELOPE = "com.kickstarter.kickstarter.intent_internal_build_envelope";
  public static final String LOGIN_REASON = "com.kickstarter.kickstarter.intent_login_reason";
  public static final String PASSWORD = "com.kickstarter.kickstarter.intent_password";
  public static final String PROJECT = "com.kickstarter.kickstarter.intent_project";
  public static final String PROJECT_PARAM = "com.kickstarter.kickstarter.intent_project_param";
  public static final String TOOLBAR_TITLE = "com.kickstarter.kickstarter.intent_toolbar_title";
  public static final String URL = "com.kickstarter.kickstarter.intent_url";
  public static final String REF_TAG = "com.kickstarter.kickstarter.ref_tag";
  public static final String MASKED_WALLET = "com.kickstarter.kickstarter.masked_wallet";
  public static final String PUSH_NOTIFICATION_ENVELOPE = "com.kickstarter.kickstarter.push_notification_envelope";
}
