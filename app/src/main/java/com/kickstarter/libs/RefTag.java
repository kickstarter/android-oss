package com.kickstarter.libs;

import android.os.Parcelable;

import com.kickstarter.services.DiscoveryParams;

import androidx.annotation.NonNull;
import auto.parcel.AutoParcel;

/**
 * A {@link RefTag} is a string identifier that Kickstarter uses to credit a pledge to a source of traffic, e.g. discovery,
 * activity, search, etc. This class represents all possible ref tags we support in the app.
 */
@AutoParcel
public abstract class RefTag implements Parcelable {
  public abstract @NonNull String tag();

  public static RefTag from(final @NonNull String tag) {
    return new AutoParcel_RefTag(tag);
  }

  public static @NonNull RefTag activity() {
    return new AutoParcel_RefTag("activity");
  }

  public static @NonNull RefTag activitySample() {
    return new AutoParcel_RefTag("discovery_activity_sample");
  }

  public static @NonNull RefTag category() {
    return new AutoParcel_RefTag("category");
  }

  public static @NonNull RefTag category(final @NonNull DiscoveryParams.Sort sort) {
    return new AutoParcel_RefTag("category" + sort.refTagSuffix());
  }

  public static @NonNull RefTag categoryFeatured() {
    return new AutoParcel_RefTag("category_featured");
  }

  public static @NonNull RefTag city() {
    return new AutoParcel_RefTag("city");
  }

  public static @NonNull RefTag dashboard() {
    return new AutoParcel_RefTag("dashboard");
  }

  public static @NonNull RefTag deepLink() {
    return new AutoParcel_RefTag("android_deep_link");
  }

  public static @NonNull RefTag discovery() {
    return new AutoParcel_RefTag("discovery");
  }

  public static @NonNull RefTag pledgeInfo() {
    return new AutoParcel_RefTag("pledge_info");
  }

  public static @NonNull RefTag projectShare() {
    return new AutoParcel_RefTag("android_project_share");
  }

  public static @NonNull RefTag push() {
    return new AutoParcel_RefTag("push");
  }

  public static @NonNull RefTag recommended() {
    return new AutoParcel_RefTag("recommended");
  }

  public static @NonNull RefTag recommended(final @NonNull DiscoveryParams.Sort sort) {
    return new AutoParcel_RefTag("recommended" + sort.refTagSuffix());
  }

  public static @NonNull RefTag search() {
    return new AutoParcel_RefTag("search");
  }

  public static @NonNull RefTag searchFeatured() {
    return new AutoParcel_RefTag("search_featured");
  }

  public static @NonNull RefTag searchPopular() {
    return new AutoParcel_RefTag("search_popular_title_view");
  }

  public static @NonNull RefTag searchPopularFeatured() {
    return new AutoParcel_RefTag("search_popular_featured");
  }

  public static @NonNull RefTag social() {
    return new AutoParcel_RefTag("social");
  }

  public static @NonNull RefTag survey() {
    return new AutoParcel_RefTag("survey");
  }

  public static @NonNull RefTag thanks() {
    return new AutoParcel_RefTag("thanks");
  }

  public static @NonNull RefTag thanksFacebookShare() {
    return new AutoParcel_RefTag("android_thanks_facebook_share");
  }

  public static @NonNull RefTag thanksTwitterShare() {
    return new AutoParcel_RefTag("android_thanks_twitter_share");
  }

  public static @NonNull RefTag thanksShare() {
    return new AutoParcel_RefTag("android_thanks_share");
  }

  public static @NonNull RefTag update() {
    return new AutoParcel_RefTag("update");
  }

  public static @NonNull RefTag updateShare() {
    return new AutoParcel_RefTag("android_update_share");
  }
}
