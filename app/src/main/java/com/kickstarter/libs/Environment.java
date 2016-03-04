package com.kickstarter.libs;

import android.content.SharedPreferences;
import android.os.Parcelable;

import com.kickstarter.libs.preferences.BooleanPreferenceType;
import com.kickstarter.libs.preferences.IntPreferenceType;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.WebClientType;

import java.net.CookieManager;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class Environment implements Parcelable {
  public abstract IntPreferenceType activitySamplePreference();
  public abstract ApiClientType apiClient();
  public abstract BuildCheck buildCheck();
  public abstract CookieManager cookieManager();
  public abstract CurrentConfig currentConfig();
  public abstract CurrentUserType currentUser();
  public abstract BooleanPreferenceType hasSeenAppRatingPreference();
  public abstract BooleanPreferenceType hasSeenGamesNewsletterPreference();
  public abstract Koala koala();
  public abstract SharedPreferences sharedPreferences();
  public abstract WebClientType webClient();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder activitySamplePreference(IntPreferenceType __);
    public abstract Builder apiClient(ApiClientType __);
    public abstract Builder buildCheck(BuildCheck __);
    public abstract Builder cookieManager(CookieManager __);
    public abstract Builder currentConfig(CurrentConfig __);
    public abstract Builder currentUser(CurrentUserType __);
    public abstract Builder hasSeenAppRatingPreference(BooleanPreferenceType __);
    public abstract Builder hasSeenGamesNewsletterPreference(BooleanPreferenceType __);
    public abstract Builder koala(Koala __);
    public abstract Builder sharedPreferences(SharedPreferences __);
    public abstract Builder webClient(WebClientType __);
    public abstract Environment build();
  }

  public static Builder builder() {
    return new AutoParcel_Environment.Builder();
  }

  public abstract Builder toBuilder();
}
