package com.kickstarter.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import timber.log.Timber;

public class CurrentUser {
  private static final String USER_KEY = "user";
  private static final String ACCESS_TOKEN_KEY = "access_token";

  private static final Gson gson = new Gson();

  private CurrentUser() {}

  public static User getUser(final Context context) {
    return gson.fromJson(sharedPreferences(context).getString(USER_KEY, null), User.class);
  }

  public static String getToken(final Context context) {
    return sharedPreferences(context).getString(ACCESS_TOKEN_KEY, null);
  }

  public static void set(final Context context, final User user, final String access_token) {
    Timber.d("Set current user to %s", user.name());

    SharedPreferences.Editor editor = sharedPreferencesEditor(context);
    editor.putString(USER_KEY, gson.toJson(user, User.class));
    editor.putString(ACCESS_TOKEN_KEY, access_token);
    editor.apply();
  }

  public static void unset(final Context context) {
    Timber.d("Unset current user");

    SharedPreferences.Editor editor = sharedPreferencesEditor(context);
    editor.remove(USER_KEY);
    editor.remove(ACCESS_TOKEN_KEY);
    editor.apply();
  }

  private static SharedPreferences sharedPreferences(final Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  private static SharedPreferences.Editor sharedPreferencesEditor(final Context context) {
    return sharedPreferences(context).edit();
  }
}

/*
  public static void setCurrent(final User user) {
    currentUser = user;
  }

  public static void unsetCurrent() {
    currentUser = null;
  }

  public static User current() {
    return currentUser;
  }

  public static boolean haveCurrent() {
    return currentUser != null;
  }

  public boolean isCurrent() {
    return this.id == User.current().id();
  }
*/
