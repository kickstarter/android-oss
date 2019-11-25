package com.kickstarter.ui;

public final class IntentKey {
  private IntentKey() {}

  /*
   * Names require package prefix:
   * http://developer.android.com/reference/android/content/Intent.html#putExtra%28java.lang.String,%20boolean%29
   */
  public static final String BACKER = "com.kickstarter.kickstarter.intent_backer";
  public static final String BACKING = "com.kickstarter.kickstarter.intent_backing";
  public static final String DISCOVERY_PARAMS = "com.kickstarter.kickstarter.intent_discovery_params";
  public static final String EDITORIAL = "com.kickstarter.kickstarter.intent_editorial";
  public static final String EMAIL = "com.kickstarter.kickstarter.intent_email";
  public static final String FACEBOOK_LOGIN = "com.kickstarter.kickstarter.intent_facebook_login";
  public static final String FACEBOOK_TOKEN = "com.kickstarter.kickstarter.intent_facebook_token";
  public static final String FACEBOOK_USER = "com.kickstarter.kickstarter.intent_facebook_user";
  public static final String INTERNAL_BUILD_ENVELOPE = "com.kickstarter.kickstarter.intent_internal_build_envelope";
  public static final String IS_FROM_MESSAGES_ACTIVITY = "com.kickstarter.kickstarter.intent_is_from_messages_activity";
  public static final String KOALA_CONTEXT = "com.kickstarter.kickstarter.intent_koala_context";
  public static final String KOALA_EVENT = "com.kickstarter.kickstarter.intent_koala_event";
  public static final String KOALA_EVENT_NAME = "com.kickstarter.kickstarter.intent_koala_event_name";
  public static final String LOGIN_REASON = "com.kickstarter.kickstarter.intent_login_reason";
  public static final String MASKED_WALLET = "com.kickstarter.kickstarter.masked_wallet";
  public static final String MESSAGE_THREAD = "com.kickstarter.kickstarter.intent_message_thread";
  public static final String NATIVE_CHECKOUT_ENABLED = "com.kickstarter.kickstarter.intent_native_checkout_enabled";
  public static final String PASSWORD = "com.kickstarter.kickstarter.intent_password";
  public static final String PROJECT = "com.kickstarter.kickstarter.intent_project";
  public static final String PROJECT_PARAM = "com.kickstarter.kickstarter.intent_project_param";
  public static final String REF_TAG = "com.kickstarter.kickstarter.ref_tag";
  public static final String SURVEY_RESPONSE = "com.kickstarter.kickstarter.survey_response";
  public static final String TOOLBAR_TITLE = "com.kickstarter.kickstarter.intent_toolbar_title";
  public static final String UPDATE = "com.kickstarter.kickstarter.intent_update";
  public static final String URL = "com.kickstarter.kickstarter.intent_url";
  public static final String PUSH_NOTIFICATION_ENVELOPE = "com.kickstarter.kickstarter.push_notification_envelope";
}
