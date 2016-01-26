package com.kickstarter.libs;

public class ActivityRequestCodes {
  /*
   * Format for this - if activity A starts activity B:
   *
   * ACTIVITY_A_ACTIVITY_B_REASON
   *
   * Where REASON describes why activity A is requesting a result from activity B.
   */
  public final static int CHECKOUT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED = 1;
  public final static int LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_CONTEXTUAL_FLOW = 2;
  public final static int LOGIN_TOUT_ACTIVITY_SIGNUP_ACTIVITY_CONTEXTUAL_FLOW = 3;
  public final static int LOGIN_ACTIVITY_TWO_FACTOR_ACTIVITY_FORWARD = 4;
  public final static int PROJECT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED = 5;
  public final static int DISCOVERY_ACTIVITY_DISCOVERY_FILTER_ACTIVITY_SELECT_FILTER = 6;
  public final static int COMMENT_FEED_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED = 7;
  public final static int ACTIVITY_FEED_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED = 8;
  public final static int FACEBOOK_CONFIRMATION_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED = 9;
  public final static int LOGIN_TOUT_ACTIVITY_FACEBOOK_CONFIRMATION_ACTIVITY_FORWARD = 10;
}
