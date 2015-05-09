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
  private Context context;

  public CurrentUser(final Context context) {
    this.context = context;
  }

  public User getUser() {
    return gson.fromJson(sharedPreferences().getString(USER_KEY, null), User.class);
  }

  public String getToken() {
    return sharedPreferences().getString(ACCESS_TOKEN_KEY, null);
  }

  public void set(final User user, final String access_token) {
    Timber.d("Set current user to %s", user.name());

    SharedPreferences.Editor editor = sharedPreferencesEditor();
    editor.putString(USER_KEY, gson.toJson(user, User.class));
    editor.putString(ACCESS_TOKEN_KEY, access_token);
    editor.apply();
  }

  public void unset() {
    Timber.d("Unset current user");

    SharedPreferences.Editor editor = sharedPreferencesEditor();
    editor.remove(USER_KEY);
    editor.remove(ACCESS_TOKEN_KEY);
    editor.apply();
  }

  public boolean exists() {
    return sharedPreferences().contains(USER_KEY);
  }

  // TODO: Inject shared preferences
  private SharedPreferences sharedPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  private SharedPreferences.Editor sharedPreferencesEditor() {
    return sharedPreferences().edit();
  }
}
