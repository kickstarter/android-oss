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
  public final static int LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD = 2;
  public final static int LOGIN_ACTIVITY_TWO_FACTOR_ACTIVITY_FORWARD = 3;
}
