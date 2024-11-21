package com.kickstarter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.network.http.DefaultHttpEngine;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.AttributionEvents;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentConfigV2;
import com.kickstarter.libs.CurrentConfigTypeV2;
import com.kickstarter.libs.CurrentUserTypeV2;
import com.kickstarter.libs.DateTimeTypeConverter;
import com.kickstarter.libs.DeviceRegistrar;
import com.kickstarter.libs.DeviceRegistrarType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.FirebaseAnalyticsClient;
import com.kickstarter.libs.FirebaseAnalyticsClientType;
import com.kickstarter.libs.Font;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.AnalyticEvents;
import com.kickstarter.libs.CurrentUserV2;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.libs.SegmentTrackingClient;
import com.kickstarter.libs.TrackingClientType;
import com.kickstarter.libs.braze.BrazeClient;
import com.kickstarter.libs.braze.RemotePushClientType;
import com.kickstarter.libs.featureflag.FeatureFlagClient;
import com.kickstarter.libs.featureflag.FeatureFlagClientType;
import com.kickstarter.libs.graphql.DateAdapter;
import com.kickstarter.libs.graphql.DateTimeAdapter;
import com.kickstarter.libs.graphql.Iso8601DateTimeAdapter;
import com.kickstarter.libs.keystore.EncryptionEngine;
import com.kickstarter.libs.preferences.BooleanPreference;
import com.kickstarter.libs.preferences.BooleanPreferenceType;
import com.kickstarter.libs.preferences.IntPreference;
import com.kickstarter.libs.preferences.IntPreferenceType;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.preferences.StringPreferenceType;
import com.kickstarter.libs.qualifiers.AccessTokenPreference;
import com.kickstarter.libs.qualifiers.ActivitySamplePreference;
import com.kickstarter.libs.qualifiers.ApiRetrofitV2;
import com.kickstarter.libs.qualifiers.AppRatingPreference;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.qualifiers.ConfigPreference;
import com.kickstarter.libs.qualifiers.FirstSessionPreference;
import com.kickstarter.libs.qualifiers.GamesNewsletterPreference;
import com.kickstarter.libs.qualifiers.PackageNameString;
import com.kickstarter.libs.qualifiers.UserPreference;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.libs.qualifiers.WebRetrofit;
import com.kickstarter.libs.utils.PlayServicesCapability;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.services.ApiClientTypeV2;
import com.kickstarter.services.ApiServiceV2;
import com.kickstarter.services.ApolloClientTypeV2;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.services.interceptors.ApiRequestInterceptor;
import com.kickstarter.services.interceptors.GraphQLInterceptor;
import com.kickstarter.services.interceptors.KSRequestInterceptor;
import com.kickstarter.services.interceptors.WebRequestInterceptor;
import com.kickstarter.type.Date;
import com.kickstarter.ui.SharedPreferenceKey;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;

import org.joda.time.DateTime;

import java.net.CookieManager;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import okhttp3.CookieJar;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApplicationModule {
  private final Application application;

  public ApplicationModule(final @NonNull Application application) {
    this.application = application;
  }

  @Provides
  @Singleton
  static Environment provideEnvironment(final @NonNull @ActivitySamplePreference IntPreferenceType activitySamplePreference,
    final @NonNull ApiClientTypeV2 apiClientV2,
    final @NonNull ApolloClientTypeV2 apolloClientV2,
    final @NonNull Build build,
    final @NonNull CookieManager cookieManager,
    final @NonNull CurrentConfigTypeV2 currentConfig2,
    final @NonNull CurrentUserTypeV2 currentUser2,
    final @NonNull @FirstSessionPreference BooleanPreferenceType firstSessionPreference,
    final @NonNull Gson gson,
    final @NonNull @AppRatingPreference BooleanPreferenceType hasSeenAppRatingPreference,
    final @NonNull @GamesNewsletterPreference BooleanPreferenceType hasSeenGamesNewsletterPreference,
    final @NonNull InternalToolsType internalToolsType,
    final @NonNull KSCurrency ksCurrency,
    final @NonNull KSString ksString,
    final @NonNull AnalyticEvents analytics,
    final @NonNull AttributionEvents attributionEvents,
    final @NonNull Logout logout,
    final @NonNull PlayServicesCapability playServicesCapability,
    final @NonNull Scheduler schedulerV2,
    final @NonNull SharedPreferences sharedPreferences,
    final @NonNull Stripe stripe,
    final @NonNull @WebEndpoint String webEndpoint,
    final @NonNull FirebaseAnalyticsClientType firebaseAnalyticsClientType,
    final @NonNull FeatureFlagClientType featureFlagClient) {

    return Environment.builder()
      .activitySamplePreference(activitySamplePreference)
      .apiClientV2(apiClientV2)
      .apolloClientV2(apolloClientV2)
      .build(build)
      .cookieManager(cookieManager)
      .currentConfig2(currentConfig2)
      .currentUserV2(currentUser2)
      .firstSessionPreference(firstSessionPreference)
      .gson(gson)
      .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .internalTools(internalToolsType)
      .ksCurrency(ksCurrency)
      .ksString(ksString)
      .analytics(analytics)
      .attributionEvents(attributionEvents)
      .logout(logout)
      .playServicesCapability(playServicesCapability)
      .schedulerV2(schedulerV2)
      .sharedPreferences(sharedPreferences)
      .stripe(stripe)
      .webEndpoint(webEndpoint)
      .firebaseAnalyticsClient(firebaseAnalyticsClientType)
      .featureFlagClient(featureFlagClient)
      .build();
  }

  @Provides
  @Nonnull
  @Singleton
  static FirebaseAnalyticsClientType provideFirebaseAnalyticsClientType(final @NonNull FeatureFlagClientType ffClient, final @NonNull SharedPreferences sharedPreferences, final @ApplicationContext @NonNull Context context) {
    return new FirebaseAnalyticsClient(ffClient, sharedPreferences, FirebaseAnalytics.getInstance(context));
  }

  @Provides
  @Nonnull
  @Singleton
  static RemotePushClientType provideBrazeClient(final @NonNull Build build, final @ApplicationContext @NonNull Context context) {
    return new BrazeClient(context, build);
  }

  @Provides
  @Nonnull
  @Singleton
  static FeatureFlagClientType provideFeatureFlagClientType(final @NonNull Build build) {
    return new FeatureFlagClient(build);
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

    return new ApolloClient.Builder()
      .serverUrl(webEndpoint + "/graph")
      .addCustomScalarAdapter(Date.Companion.getType(), new DateAdapter())
      .addCustomScalarAdapter(com.kickstarter.type.DateTime.Companion.getType(), new DateTimeAdapter())
      .addCustomScalarAdapter(com.kickstarter.type.ISO8601DateTime.Companion.getType(), new Iso8601DateTimeAdapter())
      .httpEngine(new DefaultHttpEngine(okHttpClient))
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
  @ApiRetrofitV2
  @NonNull
  static Retrofit provideApiRetrofitV2(final @NonNull ApiEndpoint apiEndpoint,
                                     final @NonNull Gson gson,
                                     final @NonNull OkHttpClient okHttpClient) {
    return createRetrofitV2(apiEndpoint.url(), gson, okHttpClient);
  }

  @Provides
  @Singleton
  @NonNull
  static ApiRequestInterceptor provideApiRequestInterceptor(
          final @NonNull String clientId, final @NonNull CurrentUserTypeV2 currentUser,
          final @NonNull ApiEndpoint endpoint, final @NonNull Build build) {
    return new ApiRequestInterceptor(clientId, currentUser, endpoint.url(), build);
  }

  @Provides
  @Singleton
  @NonNull
  static GraphQLInterceptor provideGraphQLInterceptor(final @NonNull String clientId,
    final @NonNull CurrentUserTypeV2 currentUser, final @NonNull Build build) {
    return new GraphQLInterceptor(clientId, currentUser, build);
  }

  @Provides
  @Singleton
  @NonNull
  static ApiServiceV2 provideApiServiceV2(final @ApiRetrofitV2 @NonNull Retrofit retrofit) {
    return retrofit.create(ApiServiceV2.class);
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
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    return interceptor;
  }

  @Provides
  @Singleton
  @WebRetrofit
  @NonNull
  static Retrofit provideWebRetrofit(@NonNull @WebEndpoint final String webEndpoint,
    final @NonNull Gson gson,
    final @NonNull OkHttpClient okHttpClient) {
    return createRetrofitV2(webEndpoint, gson, okHttpClient);
  }

  @Provides
  @Singleton
  @NonNull
  static WebRequestInterceptor provideWebRequestInterceptor(final @NonNull CurrentUserTypeV2 currentUser,
    @NonNull @WebEndpoint final String endpoint, final @NonNull InternalToolsType internalTools, final @NonNull Build build) {
    return new WebRequestInterceptor(currentUser, endpoint, internalTools, build);
  }

  private static @NonNull Retrofit createRetrofitV2(final @NonNull String baseUrl, final @NonNull Gson gson, final @NonNull OkHttpClient okHttpClient) {
    return new Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
  }

  @Provides
  @Singleton
  @AccessTokenPreference
  @NonNull
  static StringPreferenceType provideAccessTokenPreference(final @NonNull SharedPreferences sharedPreferences, final @ApplicationContext @NonNull Context context, final @NonNull FeatureFlagClientType featureFlagClient) {
    return new EncryptionEngine(sharedPreferences, SharedPreferenceKey.ACCESS_TOKEN, context, featureFlagClient);
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
  @NonNull
  static  StringPreferenceType providesFeaturesFlagsPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, SharedPreferenceKey.FEATURE_FLAG);
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
  @Singleton
  SegmentTrackingClient provideSegmentTrackingClient(
          final @ApplicationContext @NonNull Context context,
          final @NonNull CurrentUserTypeV2 currentUser,
          final @NonNull Build build,
          final @NonNull CurrentConfigTypeV2 currentConfig,
          final @NonNull FeatureFlagClientType featureFlagClient) {
    return new SegmentTrackingClient(build, context, currentConfig, currentUser, featureFlagClient, PreferenceManager.getDefaultSharedPreferences(context));
  }

  @Provides
  @Singleton
  static AnalyticEvents provideAnalytics(
          final @ApplicationContext @NonNull Context context,
          final @NonNull CurrentUserTypeV2 currentUser,
          final @NonNull Build build,
          final @NonNull FeatureFlagClientType ffClient,
          final @NonNull SegmentTrackingClient segmentClient) {
    final List<TrackingClientType> clients = Arrays.asList(segmentClient);
    return new AnalyticEvents(clients);
  }

  @Provides
  @Singleton
  static AttributionEvents provideAttributionEvents(
    final @NonNull ApolloClientTypeV2 apolloClient) {
    return new AttributionEvents(apolloClient);
  }

  @Provides
  @Singleton
  static io.reactivex.Scheduler provideSchedulerV2() {
    return io.reactivex.schedulers.Schedulers.computation();
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
  static CurrentConfigTypeV2 provideCurrentConfig2(final @NonNull AssetManager assetManager,
                                                   final @NonNull Gson gson,
                                                   final @ConfigPreference @NonNull StringPreferenceType configPreference) {
    return new CurrentConfigV2(assetManager, gson, configPreference);
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
  static CurrentUserTypeV2 provideCurrentUser2(final @AccessTokenPreference @NonNull StringPreferenceType accessTokenPreference,
                                               final @NonNull DeviceRegistrarType deviceRegistrar, final @NonNull Gson gson,
                                               final @NonNull @UserPreference StringPreferenceType userPreference) {
    return new CurrentUserV2(accessTokenPreference, deviceRegistrar, gson, userPreference);
  }

  @Provides
  @Singleton
  @NonNull
  static DeviceRegistrarType provideDeviceRegistrar(final @NonNull PlayServicesCapability playServicesCapability,
                                                    final @ApplicationContext @NonNull Context context,
                                                    final @NonNull RemotePushClientType brazeClient) {
    return new DeviceRegistrar(playServicesCapability, context, brazeClient);
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
  static Font provideFont(final @NonNull AssetManager assetManager) {
    return new Font(assetManager);
  }

  @Provides
  @Singleton
  static Gson provideGson() {
    return new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .create();
  }

  @Provides
  @Singleton
  static KSCurrency provideKSCurrency(final @NonNull CurrentConfigTypeV2 currentConfig) {
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
  static Logout provideLogout(final @NonNull CookieManager cookieManager, final @NonNull CurrentUserTypeV2 currentUser, final @NonNull CurrentUserTypeV2 currentUserV2) {
    return new Logout(cookieManager, currentUserV2);
  }

  @Provides
  @Singleton
  @NonNull
  static PushNotifications providePushNotifications(final @ApplicationContext @NonNull Context context,
    final @NonNull ApiClientTypeV2 client) {
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
    PaymentConfiguration.init(
            context,
            stripePublishableKey);
    return new Stripe(context, stripePublishableKey);
  }
}
