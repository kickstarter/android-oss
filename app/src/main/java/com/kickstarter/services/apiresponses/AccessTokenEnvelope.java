package com.kickstarter.services.ApiResponses;

import com.kickstarter.models.User;

public class AccessTokenEnvelope {
  public final String accessToken;
  public final User user;

  private AccessTokenEnvelope(final String accessToken, final User user) {
    this.accessToken = accessToken;
    this.user = user;
  }
}
