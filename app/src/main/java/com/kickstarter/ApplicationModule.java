package com.kickstarter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.AutoParcelAdapterFactory;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.CurrentConfig;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.DateTimeTypeConverter;
import com.kickstarter.libs.DeviceRegistrar;
import com.kickstarter.libs.DeviceRegistrarType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.Font;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.KoalaTrackingClient;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.libs.Release;
import com.kickstarter.libs.preferences.BooleanPreference;
import com.kickstarter.libs.preferences.BooleanPreferenceType;
import com.kickstarter.libs.preferences.IntPreference;
import com.kickstarter.libs.preferences.IntPreferenceType;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.qualifiers.AccessTokenPreference;
import com.kickstarter.libs.qualifiers.ActivitySamplePreference;
import com.kickstarter.libs.qualifiers.ApiRetrofit;
import com.kickstarter.libs.qualifiers.AppRatingPreference;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.qualifiers.ConfigPreference;
import com.kickstarter.libs.qualifiers.GamesNewsletterPreference;
import com.kickstarter.libs.qualifiers.PackageNameString;
import com.kickstarter.libs.qualifiers.UserPreference;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.libs.qualifiers.WebRetrofit;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.ApiService;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.services.WebClient;
import com.kickstarter.services.WebClientType;
import com.kickstarter.services.WebService;
import com.kickstarter.services.interceptors.ApiRequestInterceptor;
import com.kickstarter.services.interceptors.KSRequestInterceptor;
import com.kickstarter.services.interceptors.WebRequestInterceptor;

import org.joda.time.DateTime;

import java.net.CookieManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.CookieJar;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public final class ApplicationModule {
  private final Application application;

  public ApplicationModule(final @NonNull Application application) {
    this.application = application;
  }

  @Provides
  @Singleton
  Environment provideEnvironment(final @NonNull @ActivitySamplePreference IntPreferenceType activitySamplePreference,
    final @NonNull ApiClientType apiClient,
    final @NonNull BuildCheck buildCheck,
    final @NonNull CookieManager cookieManager,
    final @NonNull CurrentConfig currentConfig,
    final @NonNull CurrentUser currentUser,
    final @NonNull @AppRatingPreference BooleanPreferenceType hasSeenAppRatingPreference,
    final @NonNull @GamesNewsletterPreference BooleanPreferenceType hasSeenGamesNewsletterPreference,
    final @NonNull Koala koala,
    final @NonNull SharedPreferences sharedPreferences,
    final @NonNull WebClientType webClient) {

    return Environment.builder()
      .activitySamplePreference(activitySamplePreference)
      .apiClient(apiClient)
      .buildCheck(buildCheck)
      .cookieManager(cookieManager)
      .currentConfig(currentConfig)
      .currentUser(currentUser)
      .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .koala(koala)
      .sharedPreferences(sharedPreferences)
      .webClient(webClient)
      .build();
  }

  @Provides
  @Singleton
  @NonNull ApiClientType provideApiClientType(final @NonNull ApiService apiService, final @NonNull Gson gson) {
    return new ApiClient(apiService, gson);
  }

  @Provides
  @Singleton
  @NonNull
  OkHttpClient provideOkHttpClient(final @NonNull ApiRequestInterceptor apiRequestInterceptor, final @NonNull CookieJar cookieJar,
    final @NonNull HttpLoggingInterceptor httpLoggingInterceptor, final @NonNull KSRequestInterceptor ksRequestInterceptor,
    final @NonNull WebRequestInterceptor webRequestInterceptor) {

    return new OkHttpClient.Builder()
      .addInterceptor(httpLoggingInterceptor)
      .addInterceptor(apiRequestInterceptor)
      .addInterceptor(webRequestInterceptor)
      .addInterceptor(ksRequestInterceptor)
      .cookieJar(cookieJar)
      .build();
  }

  @Provides
  @Singleton
  @ApiRetrofit
  @NonNull Retrofit provideApiRetrofit(final @NonNull ApiEndpoint apiEndpoint,
    final @NonNull Gson gson,
    final @NonNull OkHttpClient okHttpClient) {
    return createRetrofit(apiEndpoint.url(), gson, okHttpClient);
  }

  @Provides
  @Singleton
  @NonNull ApiRequestInterceptor provideApiRequestInterceptor(final @NonNull String clientId,
    final @NonNull CurrentUser currentUser, final @NonNull ApiEndpoint endpoint) {
    return new ApiRequestInterceptor(clientId, currentUser, endpoint.url());
  }

  @Provides
  @Singleton
  @NonNull
  ApiService provideApiService(final @ApiRetrofit @NonNull Retrofit retrofit) {
    return retrofit.create(ApiService.class);
  }

  @Provides
  @Singleton
  String provideClientId(final @NonNull ApiEndpoint apiEndpoint) {
    return apiEndpoint == ApiEndpoint.PRODUCTION ?
      "***REMOVED***" :
      "***REMOVED***";
  }

  @Provides
  @Singleton
  @NonNull KSRequestInterceptor provideKSRequestInterceptor(final @NonNull Release release) {
    return new KSRequestInterceptor(release);
  }

  @Provides
  @Singleton
  @NonNull HttpLoggingInterceptor provideHttpLoggingInterceptor() {
    final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
    return interceptor;
  }

  @Provides
  @Singleton
  @NonNull WebClientType provideWebClientType(final @NonNull WebService webService) {
    return new WebClient(webService);
  }

  @Provides
  @Singleton
  @WebRetrofit
  @NonNull Retrofit provideWebRetrofit(@NonNull @WebEndpoint final String webEndpoint,
    final @NonNull Gson gson,
    final @NonNull OkHttpClient okHttpClient) {
    return createRetrofit(webEndpoint, gson, okHttpClient);
  }

  @Provides
  @Singleton
  @NonNull WebRequestInterceptor provideWebRequestInterceptor(final @NonNull CurrentUser currentUser,
    @NonNull @WebEndpoint final String endpoint, final @NonNull InternalToolsType internalTools, final @NonNull Release release) {
    return new WebRequestInterceptor(currentUser, endpoint, internalTools, release);
  }

  @Provides
  @Singleton
  @NonNull
  WebService provideWebService(final @WebRetrofit @NonNull Retrofit retrofit) {
    return retrofit.create(WebService.class);
  }

  private @NonNull Retrofit createRetrofit(final @NonNull String baseUrl, final @NonNull Gson gson, final @NonNull OkHttpClient okHttpClient) {
    return new Retrofit.Builder()
      .client(okHttpClient)
      .baseUrl(baseUrl)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .build();
  }

  @Provides
  @Singleton
  @AccessTokenPreference
  @NonNull StringPreference provideAccessTokenPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, "access_token");
  }

  @Provides
  @Singleton
  @ConfigPreference
  @NonNull StringPreference providesConfigPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, "config");
  }

  @Provides
  @Singleton
  @ActivitySamplePreference
  @NonNull IntPreferenceType provideActivitySamplePreference(final @NonNull SharedPreferences sharedPreferences) {
    return new IntPreference(sharedPreferences, "last_seen_activity_id");
  }

  @Provides
  @Singleton
  @AppRatingPreference
  @NonNull BooleanPreferenceType provideAppRatingPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new BooleanPreference(sharedPreferences, "has_seen_app_rating");
  }

  @Provides
  @Singleton
  Application provideApplication() {
    return application;
  }

  @Provides
  @Singleton
  Koala provideKoala(final @ApplicationContext @NonNull Context context, final @NonNull CurrentUser currentUser) {
    return new Koala(new KoalaTrackingClient(context, currentUser));
  }

  @Provides
  @Singleton
  @ApplicationContext
  Context provideApplicationContext() {
    return application;
  }

  @Provides
  @Singleton
  AssetManager provideAssetManager() {
    return application.getAssets();
  }

  @Provides
  @Singleton
  Release provideRelease(final @NonNull PackageInfo packageInfo) {
    return new Release(packageInfo);
  }

  @Provides
  @Singleton
  CurrentConfig provideCurrentConfig(final @NonNull AssetManager assetManager,
    final @NonNull Gson gson,
    final @ConfigPreference @NonNull StringPreference configPreference) {
    return new CurrentConfig(assetManager, gson, configPreference);
  }

  @Provides
  @Singleton
  CookieJar provideCookieJar(final @NonNull CookieManager cookieManager) {
    return new JavaNetCookieJar(cookieManager);
  }

  @Provides
  @Singleton
  CookieManager provideCookieManager() {
    return new CookieManager();
  }

  @Provides
  @Singleton
  CurrentUser provideCurrentUser(@AccessTokenPreference final @NonNull StringPreference accessTokenPreference,
    final @NonNull DeviceRegistrarType deviceRegistrar,
    final @NonNull Gson gson,
    @NonNull @UserPreference final StringPreference userPreference) {
    return new CurrentUser(accessTokenPreference, deviceRegistrar, gson, userPreference);
  }

  @Provides
  @Singleton
  @NonNull DeviceRegistrarType provideDeviceRegistrar(final @ApplicationContext @NonNull Context context) {
    return new DeviceRegistrar(context);
  }

  @Provides
  @Singleton
  @GamesNewsletterPreference
  @NonNull BooleanPreferenceType provideGamesNewsletterPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new BooleanPreference(sharedPreferences, "has_seen_games_newsletter");
  }

  @Provides
  @Singleton
  @WebEndpoint
  @NonNull String provideWebEndpoint(final @NonNull ApiEndpoint apiEndpoint) {
    return (apiEndpoint == ApiEndpoint.PRODUCTION) ?
      "https://www.kickstarter.com" :
      apiEndpoint.url().replaceAll("(?<=\\Ahttps?:\\/\\/)api.", "");
  }

  @Provides
  @Singleton
  Font provideFont(final @NonNull AssetManager assetManager) {
    return new Font(assetManager);
  }

  @Provides
  @Singleton
  Gson provideGson() {
    return new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .registerTypeAdapterFactory(new AutoParcelAdapterFactory())
      .create();
  }

  @Provides
  @Singleton
  KSCurrency provideKSCurrency(final @NonNull CurrentConfig currentConfig) {
    return new KSCurrency(currentConfig);
  }

  @Provides
  @Singleton
  @NonNull KSString provideKSString(final @PackageNameString @NonNull String packageName, final @NonNull Resources resources) {
    return new KSString(packageName, resources);
  }

  @Provides
  KSWebViewClient provideKSWebViewClient(final @NonNull OkHttpClient okHttpClient,
    @WebEndpoint final String webEndpoint) {
    return new KSWebViewClient(okHttpClient, webEndpoint);
  }

  @Provides
  @Singleton
  Logout provideLogout(final @NonNull CookieManager cookieManager, final @NonNull CurrentUser currentUser) {
    return new Logout(cookieManager, currentUser);
  }

  @Provides
  @Singleton
  @NonNull PushNotifications providePushNotifications(final @ApplicationContext @NonNull Context context, final @NonNull ApiClientType client,
    final @NonNull DeviceRegistrarType deviceRegistrar) {
    return new PushNotifications(context, client, deviceRegistrar);
  }

  @Provides
  @Singleton
  PackageInfo providePackageInfo(final @NonNull Application application) {
    try {
      return application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  @Provides
  @Singleton
  @PackageNameString
  String providePackageName(final @NonNull Application application) {
    return application.getPackageName();
  }

  @Provides
  @Singleton
  Resources provideResources(final @ApplicationContext @NonNull Context context) {
    return context.getResources();
  }

  @Provides
  @Singleton
  SharedPreferences provideSharedPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(application);
  }

  @Provides
  @Singleton
  @UserPreference
  StringPreference provideUserPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, "user");
  }
}
