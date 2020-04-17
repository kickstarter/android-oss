package com.kickstarter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.crashlytics.android.Crashlytics;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.AutoParcelAdapterFactory;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.CurrentConfig;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.DateTimeTypeConverter;
import com.kickstarter.libs.DeviceRegistrar;
import com.kickstarter.libs.DeviceRegistrarType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ExperimentsClientType;
import com.kickstarter.libs.Font;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.KoalaTrackingClient;
import com.kickstarter.libs.LakeTrackingClient;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.OptimizelyExperimentsClient;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.libs.graphql.DateAdapter;
import com.kickstarter.libs.graphql.EmailAdapter;
import com.kickstarter.libs.models.OptimizelyEnvironment;
import com.kickstarter.libs.preferences.BooleanPreference;
import com.kickstarter.libs.preferences.BooleanPreferenceType;
import com.kickstarter.libs.preferences.IntPreference;
import com.kickstarter.libs.preferences.IntPreferenceType;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.preferences.StringPreferenceType;
import com.kickstarter.libs.qualifiers.AccessTokenPreference;
import com.kickstarter.libs.qualifiers.ActivitySamplePreference;
import com.kickstarter.libs.qualifiers.ApiRetrofit;
import com.kickstarter.libs.qualifiers.AppRatingPreference;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.qualifiers.ConfigPreference;
import com.kickstarter.libs.qualifiers.FirstSessionPreference;
import com.kickstarter.libs.qualifiers.GamesNewsletterPreference;
import com.kickstarter.libs.qualifiers.KoalaEndpoint;
import com.kickstarter.libs.qualifiers.KoalaRetrofit;
import com.kickstarter.libs.qualifiers.KoalaTracker;
import com.kickstarter.libs.qualifiers.LakeEndpoint;
import com.kickstarter.libs.qualifiers.LakeRetrofit;
import com.kickstarter.libs.qualifiers.LakeTracker;
import com.kickstarter.libs.qualifiers.PackageNameString;
import com.kickstarter.libs.qualifiers.UserPreference;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.libs.qualifiers.WebRetrofit;
import com.kickstarter.libs.utils.PlayServicesCapability;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.ApiService;
import com.kickstarter.services.ApolloClientType;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.services.KoalaService;
import com.kickstarter.services.LakeService;
import com.kickstarter.services.WebClient;
import com.kickstarter.services.WebClientType;
import com.kickstarter.services.WebService;
import com.kickstarter.services.interceptors.ApiRequestInterceptor;
import com.kickstarter.services.interceptors.GraphQLInterceptor;
import com.kickstarter.services.interceptors.KSRequestInterceptor;
import com.kickstarter.services.interceptors.WebRequestInterceptor;
import com.kickstarter.ui.SharedPreferenceKey;
import com.optimizely.ab.android.sdk.OptimizelyManager;
import com.stripe.android.Stripe;

import org.joda.time.DateTime;

import java.net.CookieManager;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import dagger.Module;
import dagger.Provides;
import okhttp3.CookieJar;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import type.CustomType;

@Module
public final class ApplicationModule {
  private final Application application;

  public ApplicationModule(final @NonNull Application application) {
    this.application = application;
  }

  @Provides
  @Singleton
  static Environment provideEnvironment(final @NonNull @ActivitySamplePreference IntPreferenceType activitySamplePreference,
    final @NonNull ApiClientType apiClient,
    final @NonNull ApolloClientType apolloClient,
    final @NonNull Build build,
    final @NonNull BuildCheck buildCheck,
    final @NonNull CookieManager cookieManager,
    final @NonNull CurrentConfigType currentConfig,
    final @NonNull CurrentUserType currentUser,
    final @NonNull @FirstSessionPreference BooleanPreferenceType firstSessionPreference,
    final @NonNull Gson gson,
    final @NonNull @AppRatingPreference BooleanPreferenceType hasSeenAppRatingPreference,
    final @NonNull @GamesNewsletterPreference BooleanPreferenceType hasSeenGamesNewsletterPreference,
    final @NonNull InternalToolsType internalToolsType,
    final @NonNull @KoalaTracker Koala koala,
    final @NonNull KSCurrency ksCurrency,
    final @NonNull KSString ksString,
    final @NonNull @LakeTracker Koala lake,
    final @NonNull Logout logout,
    final @NonNull ExperimentsClientType optimizely,
    final @NonNull PlayServicesCapability playServicesCapability,
    final @NonNull Scheduler scheduler,
    final @NonNull SharedPreferences sharedPreferences,
    final @NonNull Stripe stripe,
    final @NonNull WebClientType webClient,
    final @NonNull @WebEndpoint String webEndpoint) {

    return Environment.builder()
      .activitySamplePreference(activitySamplePreference)
      .apiClient(apiClient)
      .apolloClient(apolloClient)
      .build(build)
      .buildCheck(buildCheck)
      .cookieManager(cookieManager)
      .currentConfig(currentConfig)
      .currentUser(currentUser)
      .firstSessionPreference(firstSessionPreference)
      .gson(gson)
      .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .internalTools(internalToolsType)
      .koala(koala)
      .ksCurrency(ksCurrency)
      .ksString(ksString)
      .lake(lake)
      .logout(logout)
      .optimizely(optimizely)
      .playServicesCapability(playServicesCapability)
      .scheduler(scheduler)
      .sharedPreferences(sharedPreferences)
      .stripe(stripe)
      .webClient(webClient)
      .webEndpoint(webEndpoint)
      .build();
  }

  @Provides
  @Singleton
  @NonNull
  static ApolloClient provideApolloClient(final @NonNull Build build, final @NonNull HttpLoggingInterceptor httpLoggingInterceptor,
    final @NonNull GraphQLInterceptor graphQLInterceptor, @NonNull @WebEndpoint final String webEndpoint, final @NonNull KSRequestInterceptor ksRequestInterceptor) {

    final OkHttpClient.Builder builder = new OkHttpClient.Builder()
      .addInterceptor(graphQLInterceptor)
      .addInterceptor(ksRequestInterceptor);

    // Only log in debug mode to avoid leaking sensitive information.
    if (build.isDebug()) {
      builder.addInterceptor(httpLoggingInterceptor);
    }

    final OkHttpClient okHttpClient = builder.build();

    return ApolloClient.builder()
      .addCustomTypeAdapter(CustomType.DATE, new DateAdapter())
      .addCustomTypeAdapter(CustomType.EMAIL, new EmailAdapter())
      .serverUrl(webEndpoint + "/graph")
      .okHttpClient(okHttpClient)
      .build();
  }

  @Provides
  @Singleton
  @NonNull
  static OkHttpClient provideOkHttpClient(final @NonNull ApiRequestInterceptor apiRequestInterceptor, final @NonNull CookieJar cookieJar,
    final @NonNull HttpLoggingInterceptor httpLoggingInterceptor, final @NonNull KSRequestInterceptor ksRequestInterceptor,
    final @NonNull Build build, final @NonNull WebRequestInterceptor webRequestInterceptor) {

    final OkHttpClient.Builder builder = new OkHttpClient.Builder();

    // Only log in debug mode to avoid leaking sensitive information.
    if (build.isDebug()) {
      builder.addInterceptor(httpLoggingInterceptor);
    }

    return builder
      .addInterceptor(apiRequestInterceptor)
      .addInterceptor(webRequestInterceptor)
      .addInterceptor(ksRequestInterceptor)
      .cookieJar(cookieJar)
      .build();
  }

  @Provides
  @Singleton
  @ApiRetrofit
  @NonNull
  static Retrofit provideApiRetrofit(final @NonNull ApiEndpoint apiEndpoint,
    final @NonNull Gson gson,
    final @NonNull OkHttpClient okHttpClient) {
    return createRetrofit(apiEndpoint.url(), gson, okHttpClient);
  }

  @Provides
  @Singleton
  @KoalaRetrofit
  @NonNull
  static Retrofit provideKoalaRetrofit(@NonNull @KoalaEndpoint final String koalaEndpoint,
    final @NonNull Gson gson,
    final @NonNull OkHttpClient okHttpClient) {
    return createRetrofit(koalaEndpoint, gson, okHttpClient);
  }

  @Provides
  @Singleton
  @LakeRetrofit
  @NonNull
  static Retrofit provideLakeRetrofit(@NonNull @LakeEndpoint final String lakeEndpoint,
    final @NonNull Gson gson,
    final @NonNull OkHttpClient okHttpClient) {
    return createRetrofit(lakeEndpoint, gson, okHttpClient);
  }

  @Provides
  @Singleton
  @NonNull
  static ApiRequestInterceptor provideApiRequestInterceptor(final @NonNull String clientId,
    final @NonNull CurrentUserType currentUser, final @NonNull ApiEndpoint endpoint) {
    return new ApiRequestInterceptor(clientId, currentUser, endpoint.url());
  }

  @Provides
  @Singleton
  @NonNull
  static GraphQLInterceptor provideGraphQLInterceptor(final @NonNull String clientId,
    final @NonNull CurrentUserType currentUser, final @NonNull Build build) {
    return new GraphQLInterceptor(clientId, currentUser, build);
  }

  @Provides
  @Singleton
  @NonNull
  static ApiService provideApiService(final @ApiRetrofit @NonNull Retrofit retrofit) {
    return retrofit.create(ApiService.class);
  }

  @Provides
  @Singleton
  @NonNull
  static KoalaService provideKoalaService(final @KoalaRetrofit @NonNull Retrofit retrofit) {
    return retrofit.create(KoalaService.class);
  }

  @Provides
  @Singleton
  @NonNull
  static LakeService provideLakeService(final @LakeRetrofit @NonNull Retrofit retrofit) {
    return retrofit.create(LakeService.class);
  }

  @Provides
  @Singleton
  static String provideClientId(final @NonNull ApiEndpoint apiEndpoint) {
    return apiEndpoint == ApiEndpoint.PRODUCTION
      ? Secrets.Api.Client.PRODUCTION
      : Secrets.Api.Client.STAGING;
  }

  @Provides
  @Singleton
  @NonNull
  static KSRequestInterceptor provideKSRequestInterceptor(final @NonNull Build build) {
    return new KSRequestInterceptor(build);
  }

  @Provides
  @Singleton
  @NonNull
  static HttpLoggingInterceptor provideHttpLoggingInterceptor() {
    final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
    return interceptor;
  }

  @Provides
  @Singleton
  @NonNull
  static WebClientType provideWebClientType(final @NonNull WebService webService) {
    return new WebClient(webService);
  }

  @Provides
  @Singleton
  @WebRetrofit
  @NonNull
  static Retrofit provideWebRetrofit(@NonNull @WebEndpoint final String webEndpoint,
    final @NonNull Gson gson,
    final @NonNull OkHttpClient okHttpClient) {
    return createRetrofit(webEndpoint, gson, okHttpClient);
  }

  @Provides
  @Singleton
  @NonNull
  static WebRequestInterceptor provideWebRequestInterceptor(final @NonNull CurrentUserType currentUser,
    @NonNull @WebEndpoint final String endpoint, final @NonNull InternalToolsType internalTools, final @NonNull Build build) {
    return new WebRequestInterceptor(currentUser, endpoint, internalTools, build);
  }

  @Provides
  @Singleton
  @NonNull
  static WebService provideWebService(final @WebRetrofit @NonNull Retrofit retrofit) {
    return retrofit.create(WebService.class);
  }

  private static @NonNull Retrofit createRetrofit(final @NonNull String baseUrl, final @NonNull Gson gson, final @NonNull OkHttpClient okHttpClient) {
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
  @NonNull
  static StringPreferenceType provideAccessTokenPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, SharedPreferenceKey.ACCESS_TOKEN);
  }

  @Provides
  @Singleton
  @NonNull
  static PlayServicesCapability providePlayServicesCapability(final @ApplicationContext @NonNull Context context) {
    return new PlayServicesCapability(context);
  }

  @Provides
  @Singleton
  @ConfigPreference
  @NonNull
  static StringPreferenceType providesConfigPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, SharedPreferenceKey.CONFIG);
  }

  @Provides
  @Singleton
  @ActivitySamplePreference
  @NonNull
  static IntPreferenceType provideActivitySamplePreference(final @NonNull SharedPreferences sharedPreferences) {
    return new IntPreference(sharedPreferences, SharedPreferenceKey.LAST_SEEN_ACTIVITY_ID);
  }

  @Provides
  @Singleton
  @AppRatingPreference
  @NonNull
  static BooleanPreferenceType provideAppRatingPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new BooleanPreference(sharedPreferences, SharedPreferenceKey.HAS_SEEN_APP_RATING);
  }

  @Provides
  @Singleton
  @FirstSessionPreference
  @NonNull
  static BooleanPreferenceType provideFirstSessionPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new BooleanPreference(sharedPreferences, SharedPreferenceKey.FIRST_SESSION);
  }

  @Provides
  @Singleton
  Application provideApplication() {
    return this.application;
  }

  @Provides
  @KoalaTracker
  @Singleton
  static Koala provideKoala(final @ApplicationContext @NonNull Context context, final @NonNull CurrentUserType currentUser,
    final @NonNull Build build, final @NonNull CurrentConfigType currentConfig) {
    return new Koala(new KoalaTrackingClient(context, currentUser, build, currentConfig));
  }

  @Provides
  @LakeTracker
  @Singleton
  static Koala provideLake(final @ApplicationContext @NonNull Context context, final @NonNull CurrentUserType currentUser,
    final @NonNull Build build, final @NonNull CurrentConfigType currentConfig) {
    return new Koala(new LakeTrackingClient(context, currentUser, build, currentConfig));
  }

  @Provides
  @Singleton
  static Scheduler provideScheduler() {
    return Schedulers.computation();
  }

  @Provides
  @Singleton
  @ApplicationContext
  Context provideApplicationContext() {
    return this.application;
  }

  @Provides
  @Singleton
  AssetManager provideAssetManager() {
    return this.application.getAssets();
  }

  @Provides
  @Singleton
  @NonNull
  static Build provideBuild(final @NonNull PackageInfo packageInfo) {
    return new Build(packageInfo);
  }

  @Provides
  @Singleton
  static CurrentConfigType provideCurrentConfig(final @NonNull AssetManager assetManager,
                                                final @NonNull Gson gson,
                                                final @ConfigPreference @NonNull StringPreferenceType configPreference) {
    return new CurrentConfig(assetManager, gson, configPreference);
  }

  @Provides
  @Singleton
  static CookieJar provideCookieJar(final @NonNull CookieManager cookieManager) {
    return new JavaNetCookieJar(cookieManager);
  }

  @Provides
  @Singleton
  static CookieManager provideCookieManager() {
    return new CookieManager();
  }

  @Provides
  @Singleton
  static CurrentUserType provideCurrentUser(final @AccessTokenPreference @NonNull StringPreferenceType accessTokenPreference,
    final @NonNull DeviceRegistrarType deviceRegistrar, final @NonNull Gson gson,
    final @NonNull @UserPreference StringPreferenceType userPreference) {
    return new CurrentUser(accessTokenPreference, deviceRegistrar, gson, userPreference);
  }

  @Provides
  @Singleton
  @NonNull
  static DeviceRegistrarType provideDeviceRegistrar(final @NonNull PlayServicesCapability playServicesCapability,
                                                    final @ApplicationContext @NonNull Context context) {
    return new DeviceRegistrar(playServicesCapability, context);
  }

  @Provides
  @Singleton
  @GamesNewsletterPreference
  @NonNull
  static BooleanPreferenceType provideGamesNewsletterPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new BooleanPreference(sharedPreferences, SharedPreferenceKey.HAS_SEEN_GAMES_NEWSLETTER);
  }

  @Provides
  @Singleton
  @WebEndpoint
  @NonNull
  static String provideWebEndpoint(final @NonNull ApiEndpoint apiEndpoint) {
    return (apiEndpoint == ApiEndpoint.PRODUCTION) ?
      "https://www.kickstarter.com" :
      apiEndpoint.url().replaceAll("(?<=\\Ahttps?:\\/\\/)api.", "");
  }

  @Provides
  @Singleton
  @KoalaEndpoint
  @NonNull
  static String provideKoalaEndpoint(final @NonNull ApiEndpoint apiEndpoint) {
    return (apiEndpoint == ApiEndpoint.PRODUCTION) ?
      Secrets.KoalaEndpoint.PRODUCTION :
      Secrets.KoalaEndpoint.STAGING;
  }

  @Provides
  @Singleton
  @LakeEndpoint
  @NonNull
  static String provideLakeEndpoint(final @NonNull ApiEndpoint apiEndpoint) {
    return (apiEndpoint == ApiEndpoint.PRODUCTION) ?
      Secrets.LakeEndpoint.PRODUCTION :
      Secrets.LakeEndpoint.STAGING;
  }

  @Provides
  @Singleton
  static Font provideFont(final @NonNull AssetManager assetManager) {
    return new Font(assetManager);
  }

  @Provides
  @Singleton
  static Gson provideGson() {
    return new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .registerTypeAdapterFactory(new AutoParcelAdapterFactory())
      .create();
  }

  @Provides
  @Singleton
  static KSCurrency provideKSCurrency(final @NonNull CurrentConfigType currentConfig) {
    return new KSCurrency(currentConfig);
  }

  @Provides
  @Singleton
  @NonNull
  static KSString provideKSString(final @PackageNameString @NonNull String packageName, final @NonNull Resources resources) {
    return new KSString(packageName, resources);
  }

  @Provides
  static KSWebViewClient provideKSWebViewClient(final @NonNull OkHttpClient okHttpClient,
    final @WebEndpoint String webEndpoint) {
    return new KSWebViewClient(okHttpClient, webEndpoint);
  }

  @Provides
  @Singleton
  static Logout provideLogout(final @NonNull CookieManager cookieManager, final @NonNull CurrentUserType currentUser) {
    return new Logout(cookieManager, currentUser);
  }

  @Provides
  @Singleton
  @NonNull
  static PushNotifications providePushNotifications(final @ApplicationContext @NonNull Context context,
    final @NonNull ApiClientType client) {
    return new PushNotifications(context, client);
  }

  @Provides
  @Singleton
  static PackageInfo providePackageInfo(final @NonNull Application application) {
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
  static String providePackageName(final @NonNull Application application) {
    return application.getPackageName();
  }

  @Provides
  @Singleton
  static Resources provideResources(final @ApplicationContext @NonNull Context context) {
    return context.getResources();
  }

  @Provides
  @Singleton
  SharedPreferences provideSharedPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(this.application);
  }

  @Provides
  @Singleton
  @UserPreference
  @NonNull
  static StringPreferenceType provideUserPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, SharedPreferenceKey.USER);
  }

  @Provides
  @Singleton
  Stripe provideStripe(final @ApplicationContext @NonNull Context context, final @NonNull ApiEndpoint apiEndpoint) {
    final String stripePublishableKey = apiEndpoint == ApiEndpoint.PRODUCTION
      ? Secrets.StripePublishableKey.PRODUCTION
      : Secrets.StripePublishableKey.STAGING;
    return new Stripe(context, stripePublishableKey);
  }

  @Provides
  @Singleton
  @NonNull
  ExperimentsClientType provideOptimizely(final @ApplicationContext @NonNull Context context, final @NonNull ApiEndpoint apiEndpoint, final @NonNull Build build) {
    final OptimizelyEnvironment optimizelyEnvironment;
    if (apiEndpoint == ApiEndpoint.PRODUCTION) {
      optimizelyEnvironment = OptimizelyEnvironment.PRODUCTION;
    } else if (apiEndpoint == ApiEndpoint.STAGING) {
      optimizelyEnvironment = OptimizelyEnvironment.STAGING;
    } else {
      optimizelyEnvironment = OptimizelyEnvironment.DEVELOPMENT;
    }

    final OptimizelyManager optimizelyManager = OptimizelyManager.builder()
      .withSDKKey(optimizelyEnvironment.getSdkKey())
      .withDatafileDownloadInterval(60L * 10L)
      .withEventDispatchInterval(2L)
      .build(context);

    optimizelyManager.initialize(context, null, optimizely -> {
      if (!optimizely.isValid()) {
        Crashlytics.logException(new Throwable("Optimizely failed to initialize."));
      } else {
        if (build.isDebug()) {
          Log.d(ApplicationModule.class.getSimpleName(), "🔮 Optimizely successfully initialized.");
        }
      }
    });
    return new OptimizelyExperimentsClient(optimizelyManager, optimizelyEnvironment);
  }
}
