package com.kickstarter.services.ApiResponses;

import com.kickstarter.models.User;

public class AccessTokenEnvelope {
  public final String access_token;
  public final User user;

  private AccessTokenEnvelope(final String access_token, final User user) {
    this.access_token = access_token;
    this.user = user;
  }
}
