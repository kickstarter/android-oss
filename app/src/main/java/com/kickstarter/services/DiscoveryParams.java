package com.kickstarter.services;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.kickstarter.R;
import com.kickstarter.libs.AutoGson;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class DiscoveryParams implements Parcelable {
  public abstract int backed();
  @Nullable public abstract Category category();
  @Nullable public abstract Location location();
  public abstract boolean nearby();
  public abstract int page();
  public abstract int perPage();
  public abstract boolean staffPicks();
  public abstract int starred();
  public abstract int social();
  public abstract Sort sort();

  public enum Sort {
    MAGIC, POPULAR, ENDING_SOON, NEWEST, MOST_FUNDED;
    @Override
    public String toString() {
      switch (this) {
        case MAGIC:
          return "magic";
        case POPULAR:
          return "popularity";
        case ENDING_SOON:
          return "end_date";
        case NEWEST:
          return "newest";
        case MOST_FUNDED:
          return "most_funded";
      }
      throw new AssertionError("Unhandled sort");
    }
  }

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder backed(int __);
    public abstract Builder category(Category __);
    public abstract Builder location(Location __);
    public abstract Builder nearby(boolean __);
    public abstract Builder page(int __);
    public abstract Builder perPage(int __);
    public abstract Builder staffPicks(boolean __);
    public abstract Builder starred(int __);
    public abstract Builder social(int __);
    public abstract Builder sort(Sort __);
    public abstract DiscoveryParams build();
  }

  public static Builder builder() {
    return new AutoParcel_DiscoveryParams.Builder()
      .backed(0)
      .nearby(false)
      .page(1)
      .perPage(15)
      .social(0)
      .sort(Sort.MAGIC)
      .staffPicks(false)
      .starred(0);
  }

  public abstract Builder toBuilder();

  public DiscoveryParams nextPage () {
    return toBuilder().page(page() + 1).build();
  }

  public ImmutableMap<String, String> queryParams() {
    final ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>()
      .put("staff_picks", String.valueOf(staffPicks()))
      .put("starred", String.valueOf(starred()))
      .put("backed", String.valueOf(backed()))
      .put("social", String.valueOf(social()))
      .put("sort", sort().toString())
      .put("page", String.valueOf(page()))
      .put("per_page", String.valueOf(perPage()));

    if (category() != null) {
      builder.put("category_id", String.valueOf(category().id()));
    }

    if (location() != null) {
      builder.put("woe_id", String.valueOf(location().id()));
    }

    if (staffPicks() && page() == 1) {
      builder.put("include_potd", "true");
    }

    return builder.build();
  }

  @Override
  public String toString() {
    return queryParams().toString();
  }

  public String filterString(final Context context) {
    if (staffPicks()) {
      return context.getString(R.string.Staff_Picks);
    } else if (nearby()) {
      return context.getString(R.string.Nearby);
    } else if (starred() == 1) {
      return context.getString(R.string.Starred);
    } else if (backed() == 1) {
      return context.getString(R.string.Backing);
    } else if (social() == 1) {
      return context.getString(R.string.Friends_Backed);
    } else if (category() != null) {
      return category().name();
    } else if (location() != null) {
      return location().name();
    } else {
      return context.getString(R.string.Everything);
    }
  }
}
