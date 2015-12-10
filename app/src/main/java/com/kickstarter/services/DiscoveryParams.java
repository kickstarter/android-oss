package com.kickstarter.services;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.models.Project;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class DiscoveryParams implements Parcelable {
  public abstract int backed();
  @Nullable public abstract Category category();
  @Nullable public abstract DateTime featuredAt();
  @Nullable public abstract Location location();
  public abstract boolean nearby();
  public abstract int page();
  public abstract int perPage();
  public abstract boolean staffPicks();
  public abstract int starred();
  public abstract int social();
  public abstract Sort sort();
  public abstract boolean recommended();
  @Nullable public abstract Project similarTo();
  @Nullable public abstract String term();

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
    public abstract Builder featuredAt(DateTime __);
    public abstract Builder location(Location __);
    public abstract Builder nearby(boolean __);
    public abstract Builder page(int __);
    public abstract Builder perPage(int __);
    public abstract Builder staffPicks(boolean __);
    public abstract Builder starred(int __);
    public abstract Builder social(int __);
    public abstract Builder sort(Sort __);
    public abstract Builder recommended(boolean __);
    public abstract Builder similarTo(Project __);
    public abstract Builder term(String __);
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
      .starred(0)
      .recommended(false);
  }

  public abstract Builder toBuilder();

  public DiscoveryParams nextPage () {
    return toBuilder().page(page() + 1).build();
  }

  public Map<String, String> queryParams() {
    return Collections.unmodifiableMap(new HashMap<String, String>() {{
      put("staff_picks", String.valueOf(staffPicks()));
      put("starred", String.valueOf(starred()));
      put("backed", String.valueOf(backed()));
      put("social", String.valueOf(social()));
      put("recommended", String.valueOf(recommended()));
      put("sort", sort().toString());
      put("page", String.valueOf(page()));
      put("per_page", String.valueOf(perPage()));

      if (category() != null) {
        put("category_id", String.valueOf(category().id()));
      }

      if (location() != null) {
        put("woe_id", String.valueOf(location().id()));
      }

      if (term() != null) {
        put("q", term());
      }

      if (similarTo() != null) {
        put("similar_to", String.valueOf(similarTo().id()));
      }

      if (staffPicks() && page() == 1) {
        put("include_potd", "true");
      }

      if (category() != null && page() == 1) {
        put("include_featured", "true");
      }
    }});
  }

  @Override
  public String toString() {
    return queryParams().toString();
  }

  public String filterString(@NonNull final Context context) {
    if (staffPicks()) {
      return context.getString(R.string.___Staff_Picks);
    } else if (nearby()) {
      return context.getString(R.string.___Nearby);
    } else if (starred() == 1) {
      return context.getString(R.string.___Starred);
    } else if (backed() == 1) {
      return context.getString(R.string.___Backing);
    } else if (social() == 1) {
      return context.getString(R.string.___Friends_Backed);
    } else if (category() != null) {
      return category().name();
    } else if (location() != null) {
      return location().name();
    } else {
      return context.getString(R.string.___Everything);
    }
  }

  public boolean isCategorySet() {
    return category() != null;
  }
}
