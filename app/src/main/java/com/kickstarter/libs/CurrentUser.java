package com.kickstarter.libs;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.models.User;

import timber.log.Timber;

public class CurrentUser {
  private static final Gson gson = new Gson();
  private final StringPreference userPreference;
  private final StringPreference accessTokenPreference;

  public CurrentUser(final StringPreference userPreference, final StringPreference accessTokenPreference) {
    this.userPreference = userPreference;
    this.accessTokenPreference = accessTokenPreference;
  }

  public User getUser() {
    return gson.fromJson(userPreference.get(), User.class);
  }

  public String getAccessToken() {
    return accessTokenPreference.get();
  }

  public void set(final User user, final String access_token) {
    Timber.d("Set current user to %s", user.name());

    userPreference.set(gson.toJson(user, User.class));
    accessTokenPreference.set(access_token);
  }

  public void unset() {
    Timber.d("Unset current user");

    userPreference.delete();
    accessTokenPreference.delete();
  }

  public boolean exists() {
    return userPreference.isSet();
  }
}
