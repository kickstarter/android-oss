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
import com.kickstarter.libs.CurrentConfig;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.DateTimeTypeConverter;
import com.kickstarter.libs.Font;
import com.kickstarter.libs.ForApplication;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.KoalaTrackingClient;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.libs.Release;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.qualifiers.AccessTokenPreference;
import com.kickstarter.libs.qualifiers.ConfigPreference;
import com.kickstarter.libs.qualifiers.UserPreference;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.ApiService;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.services.WebClient;
import com.kickstarter.services.WebService;
import com.kickstarter.services.interceptors.ApiRequestInterceptor;
import com.kickstarter.services.interceptors.KSRequestInterceptor;
import com.kickstarter.services.interceptors.WebRequestInterceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import org.joda.time.DateTime;

import java.net.CookieManager;
import java.util.Arrays;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

@Module
public class ApplicationModule {
  private final Application application;

  public ApplicationModule(@NonNull final Application application) {
    this.application = application;
  }

  // BEGIN: EXTRACT INTO SERVICES MODULE
  @Provides
  @Singleton
  @NonNull
  ApiClient provideApiClient(@NonNull final ApiService apiService, @NonNull final Gson gson) {
    return new ApiClient(apiService, gson);
  }

  @Provides
  @Singleton
  @NonNull
  OkHttpClient provideOkHttpClient(@NonNull final ApiRequestInterceptor apiRequestInterceptor, @NonNull final CookieManager cookieManager,
    @NonNull final HttpLoggingInterceptor httpLoggingInterceptor, @NonNull final KSRequestInterceptor ksRequestInterceptor,
    @NonNull final WebRequestInterceptor webRequestInterceptor) {
    final OkHttpClient okHttpClient = new OkHttpClient();

    okHttpClient.interceptors().addAll(
      Arrays.asList(httpLoggingInterceptor, apiRequestInterceptor, webRequestInterceptor, ksRequestInterceptor));
    okHttpClient.setCookieHandler(cookieManager);

    return okHttpClient;
  }

  @Provides
  @Singleton
  @Named("ApiRetrofit")
  @NonNull Retrofit provideApiRetrofit(@NonNull final ApiEndpoint apiEndpoint,
    @NonNull final Gson gson,
    @NonNull final OkHttpClient okHttpClient) {
    return createRetrofit(apiEndpoint.url, gson, okHttpClient);
  }

  @Provides
  @Singleton
  @NonNull ApiRequestInterceptor provideApiRequestInterceptor(@NonNull final String clientId,
    @NonNull final CurrentUser currentUser, @NonNull final ApiEndpoint endpoint) {
    return new ApiRequestInterceptor(clientId, currentUser, endpoint.url);
  }

  @Provides
  @Singleton
  @NonNull
  ApiService provideApiService(@Named("ApiRetrofit") @NonNull final Retrofit retrofit) {
    return retrofit.create(ApiService.class);
  }

  @Provides
  @Singleton
  String provideClientId(@NonNull final ApiEndpoint apiEndpoint) {
    return apiEndpoint == ApiEndpoint.PRODUCTION ?
      "***REMOVED***" :
      "***REMOVED***";
  }

  @Provides
  @Singleton
  @NonNull KSRequestInterceptor provideKSRequestInterceptor(@NonNull final Release release) {
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
  @NonNull WebClient provideWebClient(@NonNull final WebService webService) {
    return new WebClient(webService);
  }

  @Provides
  @Singleton
  @Named("WebRetrofit")
  @NonNull Retrofit provideWebRetrofit(@NonNull @WebEndpoint final String webEndpoint,
    @NonNull final Gson gson,
    @NonNull final OkHttpClient okHttpClient) {
    return createRetrofit(webEndpoint, gson, okHttpClient);
  }

  @Provides
  @Singleton
  @NonNull WebRequestInterceptor provideWebRequestInterceptor(@NonNull final CurrentUser currentUser,
    @NonNull @WebEndpoint final String endpoint, @NonNull final Release release) {
    return new WebRequestInterceptor(currentUser, endpoint, release);
  }

  @Provides
  @Singleton
  @NonNull
  WebService provideWebService(@Named("WebRetrofit") @NonNull final Retrofit retrofit) {
    return retrofit.create(WebService.class);
  }

  private @NonNull Retrofit createRetrofit(@NonNull String baseUrl, @NonNull final Gson gson, @NonNull final OkHttpClient okHttpClient) {
    return new Retrofit.Builder()
      .client(okHttpClient)
      .baseUrl(baseUrl)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .build();
  }
  // END: EXTRACT INTO SERVICES MODULE


  @Provides
  @Singleton
  @AccessTokenPreference
  @NonNull StringPreference provideAccessTokenPreference(@NonNull final SharedPreferences sharedPreferences) {
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
  Application provideApplication() {
    return application;
  }

  @Provides
  @Singleton
  Koala provideKoala(@ForApplication @NonNull final Context context, @NonNull final CurrentUser currentUser) {
    return new Koala(new KoalaTrackingClient(context, currentUser));
  }

  @Provides
  @Singleton
  @ForApplication
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
  Release provideRelease(@NonNull final PackageInfo packageInfo) {
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
  CookieManager provideCookieManager() {
    return new CookieManager();
  }

  @Provides
  @Singleton
  CurrentUser provideCurrentUser(@AccessTokenPreference @NonNull final StringPreference accessTokenPreference,
    @NonNull final Gson gson,
    @NonNull final PushNotifications pushNotifications,
    @NonNull @UserPreference final StringPreference userPreference) {
    return new CurrentUser(accessTokenPreference, gson, pushNotifications, userPreference);
  }

  @Provides
  @Singleton
  @WebEndpoint
  @NonNull String provideWebEndpoint(@NonNull final ApiEndpoint apiEndpoint) {
    final String url = (apiEndpoint == ApiEndpoint.PRODUCTION) ?
      "https://www.kickstarter.com" :
      apiEndpoint.url.replaceAll("(?<=\\Ahttps?:\\/\\/)api.", "");

    return url;
  }

  @Provides
  @Singleton
  Font provideFont(@NonNull final AssetManager assetManager) {
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
  @NonNull KSString provideKSString(final @Named("PackageName") @NonNull String packageName, final @NonNull Resources resources) {
    return new KSString(packageName, resources);
  }

  @Provides
  KSWebViewClient provideKSWebViewClient(@NonNull final OkHttpClient okHttpClient,
    @WebEndpoint final String webEndpoint) {
    return new KSWebViewClient(okHttpClient, webEndpoint);
  }

  @Provides
  @Singleton
  Logout provideLogout(@NonNull final CookieManager cookieManager, @NonNull final CurrentUser currentUser) {
    return new Logout(cookieManager, currentUser);
  }

  @Provides
  @Singleton
  PushNotifications providePushNotifications(@ForApplication @NonNull final Context context) {
    return new PushNotifications(context);
  }

  @Provides
  @Singleton
  PackageInfo providePackageInfo(@NonNull final Application application) {
    try {
      return application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  @Provides
  @Singleton
  @Named("PackageName")
  String providePackageName(@NonNull final Application application) {
    return application.getPackageName();
  }

  @Provides
  @Singleton
  Resources provideResources(@ForApplication @NonNull final Context context) {
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
  StringPreference provideUserPreference(@NonNull final SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, "user");
  }
}
