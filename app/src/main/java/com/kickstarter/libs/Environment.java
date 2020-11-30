package com.kickstarter.libs;

import android.content.SharedPreferences;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.kickstarter.libs.preferences.BooleanPreferenceType;
import com.kickstarter.libs.preferences.IntPreferenceType;
import com.kickstarter.libs.utils.PlayServicesCapability;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.ApolloClientType;
import com.kickstarter.services.WebClientType;
import com.stripe.android.Stripe;

import java.net.CookieManager;

import auto.parcel.AutoParcel;
import okhttp3.OkHttpClient;
import rx.Scheduler;

@AutoParcel
public abstract class Environment implements Parcelable {
  public abstract IntPreferenceType activitySamplePreference();
  public abstract ApiClientType apiClient();
  public abstract ApolloClientType apolloClient();
  public abstract OkHttpClient okHttpClient();
  public abstract Build build();
  public abstract BuildCheck buildCheck();
  public abstract CookieManager cookieManager();
  public abstract CurrentConfigType currentConfig();
  public abstract CurrentUserType currentUser();
  public abstract BooleanPreferenceType firstSessionPreference();
  public abstract Gson gson();
  public abstract BooleanPreferenceType hasSeenAppRatingPreference();
  public abstract BooleanPreferenceType hasSeenGamesNewsletterPreference();
  public abstract InternalToolsType internalTools();
  public abstract Koala koala();
  public abstract KSCurrency ksCurrency();
  public abstract KSString ksString();
  public abstract Koala lake();
  public abstract Logout logout();
  public abstract ExperimentsClientType optimizely();
  public abstract PlayServicesCapability playServicesCapability();
  public abstract Scheduler scheduler();
  public abstract SharedPreferences sharedPreferences();
  public abstract Stripe stripe();
  public abstract WebClientType webClient();
  public abstract String webEndpoint();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder activitySamplePreference(IntPreferenceType __);
    public abstract Builder apiClient(ApiClientType __);
    public abstract Builder apolloClient(ApolloClientType __);
    public abstract Builder okHttpClient(OkHttpClient __);
    public abstract Builder build(Build __);
    public abstract Builder buildCheck(BuildCheck __);
    public abstract Builder cookieManager(CookieManager __);
    public abstract Builder currentConfig(CurrentConfigType __);
    public abstract Builder currentUser(CurrentUserType __);
    public abstract Builder firstSessionPreference(BooleanPreferenceType __);
    public abstract Builder gson(Gson __);
    public abstract Builder hasSeenAppRatingPreference(BooleanPreferenceType __);
    public abstract Builder hasSeenGamesNewsletterPreference(BooleanPreferenceType __);
    public abstract Builder internalTools(InternalToolsType __);
    public abstract Builder koala(Koala __);
    public abstract Builder ksCurrency(KSCurrency __);
    public abstract Builder ksString(KSString __);
    public abstract Builder lake(Koala __);
    public abstract Builder logout(Logout __);
    public abstract Builder optimizely(ExperimentsClientType __);
    public abstract Builder playServicesCapability(PlayServicesCapability __);
    public abstract Builder scheduler(Scheduler __);
    public abstract Builder sharedPreferences(SharedPreferences __);
    public abstract Builder stripe(Stripe __);
    public abstract Builder webClient(WebClientType __);
    public abstract Builder webEndpoint(String __);
    public abstract Environment build();
  }

  public static Builder builder() {
    return new AutoParcel_Environment.Builder();
  }

  public abstract Builder toBuilder();
}
