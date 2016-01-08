package com.kickstarter.libs;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.kickstarter.services.DiscoveryParams;

import auto.parcel.AutoParcel;

/**
 * A `RefTag` is a string identifier that Kickstarter uses to credit a pledge to a source of traffic, e.g. discovery,
 * activity, search, etc. This class represents all possible ref tags we support in the app.
 */
@AutoParcel
public abstract class RefTag implements Parcelable {
  static final @NonNull String COOKIE_VALUE_SEPARATOR = "%3F";

  public abstract @NonNull String tag();

  public static RefTag from(final @NonNull String tag) {
    return new AutoParcel_RefTag(tag);
  }

  public static @NonNull RefTag category() {
    return new AutoParcel_RefTag("category");
  }

  public static @NonNull RefTag category(final @NonNull DiscoveryParams.Sort sort) {
    return new AutoParcel_RefTag("category" + sort.refTagSuffix());
  }

  public static @NonNull RefTag city() {
    return new AutoParcel_RefTag("city");
  }

  public static @NonNull RefTag recommended() {
    return new AutoParcel_RefTag("recommended");
  }

  public static @NonNull RefTag recommended(final @NonNull DiscoveryParams.Sort sort) {
    return new AutoParcel_RefTag("recommended" + sort.refTagSuffix());
  }

  public static @NonNull RefTag social() {
    return new AutoParcel_RefTag("social");
  }

  public static @NonNull RefTag search() {
    return new AutoParcel_RefTag("search");
  }

  public static @NonNull RefTag discovery() {
    return new AutoParcel_RefTag("discovery");
  }

  public static @NonNull RefTag thanks() {
    return new AutoParcel_RefTag("thanks");
  }

  public static @NonNull RefTag activity() {
    return new AutoParcel_RefTag("activity");
  }

  public static @NonNull RefTag discoverPotd() {
    return new AutoParcel_RefTag("discover_potd");
  }

  public static @NonNull RefTag categoryFeatured() {
    return new AutoParcel_RefTag("category_featured");
  }

  @Override
  public String toString() {
    return tag();
  }
}
