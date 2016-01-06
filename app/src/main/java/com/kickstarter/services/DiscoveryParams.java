package com.kickstarter.services;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.models.Project;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class DiscoveryParams implements Parcelable {
  public abstract int backed();
  @Nullable public abstract Category category();
  @Nullable public abstract String categorySlug();
  @Nullable public abstract Location location();
  @Nullable public abstract String locationSlug();
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
    public String toString() throws AssertionError {
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

    public static Sort fromString(final @NonNull String string) throws AssertionError {
      switch (string) {
        case "magic":
          return MAGIC;
        case "popularity":
          return POPULAR;
        case "end_date":
          return ENDING_SOON;
        case "newest":
          return NEWEST;
        case "most_funded":
          return MOST_FUNDED;
      }
      throw new AssertionError("Unhandled sort");
    }
  }

  public static @NonNull DiscoveryParams fromUri(final @NonNull Uri uri) {
    DiscoveryParams.Builder builder = DiscoveryParams.builder();

    final Integer backed = ObjectUtils.toInteger(uri.getQueryParameter("backed"));
    if (backed != null) {
      builder = builder.backed(backed);
    }

    if (KSUri.isDiscoverCategoriesPath(uri.getPath())) {
      builder = builder.categorySlug(uri.getLastPathSegment());
    }

    final String categorySlug = uri.getQueryParameter("category_id");
    if (categorySlug != null) {
      builder = builder.categorySlug(categorySlug);
    }

    if (KSUri.isDiscoverPlacesPath(uri.getPath())) {
      builder = builder.locationSlug(uri.getLastPathSegment());
    }

    final String locationSlug = uri.getQueryParameter("location_id");
    if (locationSlug != null) {
      builder = builder.locationSlug(locationSlug);
    }

    final Integer page = ObjectUtils.toInteger(uri.getQueryParameter("page"));
    if (page != null) {
      builder = builder.page(page);
    }

    final Integer perPage = ObjectUtils.toInteger(uri.getQueryParameter("per_page"));
    if (perPage != null) {
      builder = builder.perPage(perPage);
    }

    final Boolean recommended = ObjectUtils.toBoolean(uri.getQueryParameter("recommended"));
    if (recommended != null) {
      builder = builder.recommended(recommended);
    }

    final Integer social = ObjectUtils.toInteger(uri.getQueryParameter("social"));
    if (social != null) {
      builder = builder.social(social);
    }

    final Boolean staffPicks = ObjectUtils.toBoolean(uri.getQueryParameter("staff_picks"));
    if (staffPicks != null) {
      builder = builder.staffPicks(staffPicks);
    }

    final String sortString = uri.getQueryParameter("sort");
    if (sortString != null) {
      try {
        final Sort sort = Sort.fromString(uri.getQueryParameter("sort"));
        if (sort != null) {
          builder = builder.sort(sort);
        }
      } catch (AssertionError e) {}
    }

    final Integer starred = ObjectUtils.toInteger(uri.getQueryParameter("starred"));
    if (starred != null) {
      builder = builder.starred(starred);
    }

    final String term = uri.getQueryParameter("term");
    if (term != null) {
      builder = builder.term(term);
    }

    return builder.build();
  }

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder backed(int __);
    public abstract Builder category(Category __);
    public abstract Builder categorySlug(String __);
    public abstract Builder location(Location __);
    public abstract Builder locationSlug(String __);
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

      if (categorySlug() != null) {
        put("category_id", categorySlug());
      }

      if (location() != null) {
        put("woe_id", String.valueOf(location().id()));
      }

      if (locationSlug() != null) {
        put("woe_id", locationSlug());
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
  public @NonNull String toString() {
    return queryParams().toString();
  }

  public @NonNull String filterString(@NonNull final Context context) {
    if (staffPicks()) {
      return context.getString(R.string.discovery_recommended);
    } else if (starred() == 1) {
      return context.getString(R.string.discovery_saved);
    } else if (backed() == 1) {
      return context.getString(R.string.discovery_backing);
    } else if (social() == 1) {
      return context.getString(R.string.discovery_friends_backed);
    } else if (category() != null) {
      return category().name();
    } else if (categorySlug() != null) {
      return categorySlug();
    } else if (location() != null) {
      return location().name();
    } else if (locationSlug() != null) {
      return locationSlug();
    } else {
      return context.getString(R.string.discovery_everything);
    }
  }

  public boolean isCategorySet() {
    return category() != null;
  }
}
