package com.kickstarter.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

import static com.kickstarter.libs.utils.BooleanUtils.isFalse;
import static com.kickstarter.libs.utils.BooleanUtils.isTrue;

@AutoGson
@AutoParcel
public abstract class DiscoveryParams implements Parcelable {
  public abstract @Nullable Integer backed();
  public abstract @Nullable Category category();
  public abstract @Nullable String categoryParam();
  public abstract @Nullable Location location();
  public abstract @Nullable String locationParam();
  public abstract @Nullable Integer page();
  public abstract @Nullable Integer perPage();
  public abstract @Nullable Integer pledged();
  public abstract @Nullable Boolean staffPicks();
  public abstract @Nullable Integer starred();
  public abstract @Nullable Integer social();
  public abstract @Nullable Sort sort();
  public abstract @Nullable Boolean recommended();
  public abstract @Nullable Project similarTo();
  public abstract @Nullable State state();
  public abstract @Nullable String term();

  public enum Sort {
    HOME, POPULAR, NEWEST, ENDING_SOON;
    @Override
    public @NonNull String toString() {
      switch (this) {
        case HOME:
          return "home";
        case POPULAR:
          return "popularity";
        case NEWEST:
          return "newest";
        case ENDING_SOON:
          return "end_date";
      }
      return "";
    }

    public static @Nullable Sort fromString(final @NonNull String string) {
      switch (string) {
        case "home":
          return HOME;
        case "popularity":
          return POPULAR;
        case "newest":
          return NEWEST;
        case "end_date":
          return ENDING_SOON;
      }
      return HOME;
    }

    public @NonNull String refTagSuffix() {
      switch (this) {
        case HOME:
          return "";
        case POPULAR:
          return "_popular";
        case NEWEST:
          return "_newest";
        case ENDING_SOON:
          return "_ending_soon";
        default:
          return "";
      }
    }
  }

  @SuppressLint("DefaultLocale")
  public enum State {
    STARTED, SUBMITTED, LIVE, SUCCESSFUL, CANCELED, FAILED;
    @Override
    public @NonNull String toString() {
      return name().toLowerCase();
    }

    public static @Nullable State fromString(final @NonNull String string) {
      switch (string) {
        case "started":
          return STARTED;
        case "submitted":
          return SUBMITTED;
        case "live":
          return LIVE;
        case "successful":
          return SUCCESSFUL;
        case "canceled":
          return CANCELED;
        case "failed":
          return FAILED;
      }

      return null;
    }
  }

  /**
   * Returns a {@link DiscoveryParams} constructed by parsing data out of the given {@link Uri}.
   */
  public static @NonNull DiscoveryParams fromUri(final @NonNull Uri uri) {
    Builder builder = DiscoveryParams.builder();

    if (KSUri.isDiscoverCategoriesPath(uri.getPath())) {
      builder = builder.categoryParam(uri.getLastPathSegment());
    }

    if (KSUri.isDiscoverPlacesPath(uri.getPath())) {
      builder = builder.locationParam(uri.getLastPathSegment());
    }

    if (KSUri.isDiscoverScopePath(uri.getPath(), "ending-soon")) {
      builder = builder.sort(Sort.ENDING_SOON);
    }

    if (KSUri.isDiscoverScopePath(uri.getPath(), "newest")) {
      builder = builder.sort(Sort.NEWEST).staffPicks(true);
    }

    if (KSUri.isDiscoverScopePath(uri.getPath(), "popular")) {
      builder = builder.sort(Sort.POPULAR);
    }

    if (KSUri.isDiscoverScopePath(uri.getPath(), "recently-launched")) {
      builder = builder.sort(Sort.NEWEST);
    }

    if (KSUri.isDiscoverScopePath(uri.getPath(), "small-projects")) {
      builder = builder.pledged(0);
    }

    if (KSUri.isDiscoverScopePath(uri.getPath(), "social")) {
      builder = builder.social(0);
    }

    if (KSUri.isDiscoverScopePath(uri.getPath(), "successful")) {
      builder = builder.sort(Sort.ENDING_SOON).state(State.SUCCESSFUL);
    }

    final Integer backed = ObjectUtils.toInteger(uri.getQueryParameter("backed"));
    if (backed != null) {
      builder = builder.backed(backed);
    }

    final String categoryParam = uri.getQueryParameter("category_id");
    if (categoryParam != null) {
      builder = builder.categoryParam(categoryParam);
    }

    final String locationParam = uri.getQueryParameter("woe_id");
    if (locationParam != null) {
      builder = builder.locationParam(locationParam);
    }

    final Integer page = ObjectUtils.toInteger(uri.getQueryParameter("page"));
    if (page != null) {
      builder = builder.page(page);
    }

    final Integer perPage = ObjectUtils.toInteger(uri.getQueryParameter("per_page"));
    if (perPage != null) {
      builder = builder.perPage(perPage);
    }

    final Integer pledged = ObjectUtils.toInteger(uri.getQueryParameter("pledged"));
    if (pledged != null) {
      builder = builder.pledged(pledged);
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

    final String sortParam = uri.getQueryParameter("sort");
    if (sortParam != null) {
      builder = builder.sort(Sort.fromString(sortParam));
    }

    final Integer starred = ObjectUtils.toInteger(uri.getQueryParameter("starred"));
    if (starred != null) {
      builder = builder.starred(starred);
    }

    final String stateParam = uri.getQueryParameter("state");
    if (stateParam != null) {
      builder = builder.state(State.fromString(stateParam));
    }

    final String term = uri.getQueryParameter("term");
    if (term != null) {
      builder = builder.term(term);
    }

    return builder.build();
  }

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder backed(Integer __);
    public abstract Builder category(Category __);
    public abstract Builder categoryParam(String __);
    public abstract Builder location(Location __);
    public abstract Builder locationParam(String __);
    public abstract Builder page(Integer __);
    public abstract Builder perPage(Integer __);
    public abstract Builder pledged(Integer __);
    public abstract Builder sort(Sort __);
    public abstract Builder staffPicks(Boolean __);
    public abstract Builder starred(Integer __);
    public abstract Builder social(Integer __);
    public abstract Builder recommended(Boolean __);
    public abstract Builder similarTo(Project __);
    public abstract Builder state(State __);
    public abstract Builder term(String __);
    public abstract DiscoveryParams build();

    /**
     * Returns a builder containing the contents of this builder and `otherBuilder`. If a value for the same property
     * exists in both builders, the returned builder will contain the value from `otherBuilder`.
     */
    public @NonNull DiscoveryParams.Builder mergeWith(final @NonNull Builder otherBuilder) {
      final DiscoveryParams other = otherBuilder.build();
      DiscoveryParams.Builder retVal = this;

      if (other.backed() != null) {
        retVal = retVal.backed(other.backed());
      }
      if (other.category() != null) {
        retVal = retVal.category(other.category());
      }
      if (other.categoryParam() != null) {
        retVal = retVal.categoryParam(other.categoryParam());
      }
      if (other.location() != null) {
        retVal = retVal.location(other.location());
      }
      if (other.page() != null) {
        retVal = retVal.page(other.page());
      }
      if (other.perPage() != null) {
        retVal = retVal.perPage(other.perPage());
      }
      if (other.pledged() != null) {
        retVal = retVal.pledged(other.pledged());
      }
      if (other.social() != null) {
        retVal = retVal.social(other.social());
      }
      if (other.staffPicks() != null) {
        retVal = retVal.staffPicks(other.staffPicks());
      }
      if (other.starred() != null) {
        retVal = retVal.starred(other.starred());
      }
      if (other.state() != null) {
        retVal = retVal.state(other.state());
      }
      if (other.sort() != null) {
        retVal = retVal.sort(other.sort());
      }
      if (other.recommended() != null) {
        retVal = retVal.recommended(other.recommended());
      }
      if (other.similarTo() != null) {
        retVal = retVal.similarTo(other.similarTo());
      }
      if (other.term() != null) {
        retVal = retVal.term(other.term());
      }

      return retVal;
    }
  }

  public static @NonNull Builder builder() {
    return new AutoParcel_DiscoveryParams.Builder()
      .page(1)
      .perPage(15);
  }

  public abstract Builder toBuilder();

  public @NonNull DiscoveryParams nextPage() {
    final Integer page = page();
    return page != null ? toBuilder().page(page + 1).build() : this;
  }

  public @NonNull Map<String, String> queryParams() {
    return Collections.unmodifiableMap(new HashMap<String, String>() {
      {
        if (backed() != null) {
          put("backed", String.valueOf(backed()));
        }

        if (category() != null) {
          put("category_id", String.valueOf(category().id()));
        }

        if (categoryParam() != null) {
          put("category_id", categoryParam());
        }

        if (location() != null) {
          put("woe_id", String.valueOf(location().id()));
        }

        if (locationParam() != null) {
          put("woe_id", locationParam());
        }

        if (page() != null) {
          put("page", String.valueOf(page()));
        }

        if (perPage() != null) {
          put("per_page", String.valueOf(perPage()));
        }

        if (pledged() != null) {
          put("pledged", String.valueOf(pledged()));
        }

        if (recommended() != null) {
          put("recommended", String.valueOf(recommended()));
        }

        if (similarTo() != null) {
          put("similar_to", String.valueOf(similarTo().id()));
        }

        if (starred() != null) {
          put("starred", String.valueOf(starred()));
        }

        if (social() != null) {
          put("social", String.valueOf(social()));
        }

        final Sort sort = sort();
        if (sort != null) {
          put("sort", sort.toString());
        }

        if (staffPicks() != null) {
          put("staff_picks", String.valueOf(staffPicks()));
        }

        final State state = state();
        if (state != null) {
          put("state", state.toString());
        }

        if (term() != null) {
          put("q", term());
        }

        if (shouldIncludeFeatured()) {
          put("include_featured", "true");
        }
      }
    });
  }

  /**
   * Determines if the `include_featured` flag should be included in a discovery request so that we guarantee that the
   * featured project for the category comes back.
   */
  public boolean shouldIncludeFeatured() {
    return category() != null && category().parent() == null && page() != null && page() == 1 && (sort() == null || sort() == Sort.HOME);
  }

  @Override
  public @NonNull String toString() {
    return queryParams().toString();
  }

  public @NonNull String filterString(final @NonNull Context context, final @NonNull KSString ksString) {
    return filterString(context, ksString, false, false);
  }

  /**
   * Determines the correct string to display for a filter depending on where it is shown.
   *
   * @param context           context
   * @param ksString          ksString for string formatting
   * @param isToolbar         true if string is being displayed in the {@link com.kickstarter.ui.toolbars.DiscoveryToolbar}
   * @param isParentFilter    true if string is being displayed as a {@link com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder}
   *
   * @return the appropriate filter string
   */
  public @NonNull String filterString(final @NonNull Context context, final @NonNull KSString ksString,
    final boolean isToolbar, final boolean isParentFilter) {
    if (isTrue(staffPicks())) {
      return context.getString(R.string.Projects_We_Love);
    } else if (starred() != null && starred() == 1) {
      return context.getString(R.string.Saved);
    } else if (backed() != null && backed() == 1) {
      return context.getString(R.string.discovery_backing);
    } else if (social() != null && social() == 1) {
      return isToolbar ? context.getString(R.string.Following) : context.getString(R.string.Backed_by_people_you_follow);
    } else if (category() != null) {
      return category().isRoot() && !isParentFilter && !isToolbar
        ? ksString.format(context.getString(R.string.All_category_name_Projects), "category_name", category().name())
        : category().name();
    } else if (location() != null) {
      return location().displayableName();
    } else if (isTrue(recommended())) {
      return isToolbar ? context.getString(R.string.Recommended) : context.getString(R.string.discovery_recommended_for_you);
    } else {
      return context.getString(R.string.All_Projects);
    }
  }

  public static DiscoveryParams getDefaultParams(final @Nullable User user) {
    final Builder builder = DiscoveryParams.builder();
    if (user != null && isFalse(user.optedOutOfRecommendations())) {
      builder.recommended(true).backed(-1);
    }
    return builder
      .sort(Sort.HOME)
      .build();
  }

  /**
   * Determines if params are for All Projects, i.e. discovery without params.
   * @return true if is All Projects.
   */
  public boolean isAllProjects() {
    return isFalse(staffPicks()) && (starred() == null || starred() != 1) && (backed() == null || backed() != 1)
      && (social() == null || social() != 1) && category() == null && location() == null && isFalse(recommended());
  }

  /**
   * Determines if params are for Saved Projects, i.e. discovery with starred params.
   * @return true if is Saved Projects.
   */
  public boolean isSavedProjects() {
    return isTrue(starred() != null && starred() == 1) && isFalse(staffPicks()) && (backed() == null || backed() != 1)
      && (social() == null || social() != 1) && category() == null && location() == null && isFalse(recommended());
  }

  public boolean isCategorySet() {
    return category() != null;
  }
}
