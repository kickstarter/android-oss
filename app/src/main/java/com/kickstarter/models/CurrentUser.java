package com.kickstarter.models;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import timber.log.Timber;

public class CurrentUser {
  private static final String USER_KEY = "user";
  private static final String ACCESS_TOKEN_KEY = "access_token";

  private static final Gson gson = new Gson();
  private final SharedPreferences sharedPreferences;

  public CurrentUser(final SharedPreferences sharedPreferences) {
    this.sharedPreferences = sharedPreferences;
  }

  public User getUser() {
    return gson.fromJson(sharedPreferences.getString(USER_KEY, null), User.class);
  }

  public String getToken() {
    return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
  }

  public void set(final User user, final String access_token) {
    Timber.d("Set current user to %s", user.name());

    final SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(USER_KEY, gson.toJson(user, User.class));
    editor.putString(ACCESS_TOKEN_KEY, access_token);
    editor.apply();
  }

  public void unset() {
    Timber.d("Unset current user");

    final SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.remove(USER_KEY);
    editor.remove(ACCESS_TOKEN_KEY);
    editor.apply();
  }

  public boolean exists() {
    return sharedPreferences.contains(USER_KEY);
  }
}
