package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Activity;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class KoalaUtils {
  private KoalaUtils() {}

  public static @NonNull Map<String, Object> discoveryParamsProperties(final @NonNull DiscoveryParams params) {
    return discoveryParamsProperties(params, "discover_");
  }

  public static @NonNull Map<String, Object> discoveryParamsProperties(final @NonNull DiscoveryParams params, final @NonNull String prefix) {

    final Map<String, Object> properties = Collections.unmodifiableMap(new HashMap<String, Object>() {
      {
        put("staff_picks", params.staffPicks());
        put("starred", params.starred());
        put("social", params.social());
        put("term", params.term());
        put("sort", params.sort() != null ? String.valueOf(params.sort()) : "");
        put("page", params.page());
        put("per_page", params.perPage());

        final Category category = params.category();
        if (category != null) {
          putAll(categoryProperties(category));
        }

        final Location location = params.location();
        if (location != null) {
          putAll(locationProperties(location));
        }
      }
    });

    return MapUtils.prefixKeys(properties, prefix);
  }

  public static @NonNull Map<String, Object> categoryProperties(final @NonNull Category category) {
    return categoryProperties(category, "category_");
  }

  public static @NonNull Map<String, Object> categoryProperties(final @NonNull Category category, final @NonNull String prefix) {
    final Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("id", String.valueOf(category.id()));
        put("name", String.valueOf(category.name()));
      }
    };

    return MapUtils.prefixKeys(properties, prefix);
  }

  public static @NonNull Map<String, Object> locationProperties(final @NonNull Location location) {
    return locationProperties(location, "location_");
  }

  public static @NonNull Map<String, Object> locationProperties(final @NonNull Location location, final @NonNull String prefix) {
    final Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("id", location.id());
        put("name", location.name());
        put("displayable_name", location.displayableName());
        put("city", location.city());
        put("state", location.state());
        put("country", location.country());
        put("projects_count", location.projectsCount());
      }
    };

    return MapUtils.prefixKeys(properties, prefix);
  }

  public static @NonNull Map<String, Object> userProperties(final @NonNull User user) {
    return userProperties(user, "user_");
  }

  public static @NonNull Map<String, Object> userProperties(final @NonNull User user, final @NonNull String prefix) {
    final Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("uid", user.id());
        put("backed_projects_count", user.backedProjectsCount());
        put("created_projects_count", user.createdProjectsCount());
        put("starred_projects_count", user.starredProjectsCount());
      }
    };

    return MapUtils.prefixKeys(properties, prefix);
  }

  public static @NonNull Map<String, Object> projectProperties(final @NonNull Project project) {
    return projectProperties(project, "project_");
  }

  public static @NonNull Map<String, Object> projectProperties(final @NonNull Project project, final @NonNull String prefix) {
    return projectProperties(project, null, prefix);
  }

  public static @NonNull Map<String, Object> projectProperties(final @NonNull Project project, final @Nullable User loggedInUser, final @NonNull String prefix) {
    final Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("backers_count", project.backersCount());
        put("country", project.country());
        put("currency", project.currency());
        put("goal", project.goal());
        put("pid", project.id());
        put("name", project.name());
        put("state", project.state());
        put("update_count", project.updatesCount());
        put("comments_count", project.commentsCount());
        put("pledged", project.pledged());
        put("percent_raised", project.percentageFunded() / 100.0f);
        put("has_video", project.video() != null);
        put("hours_remaining", (int) Math.ceil(ProjectUtils.timeInSecondsUntilDeadline(project) / 60.0f / 60.0f));
        put("duration", Math.round(ProjectUtils.timeInSecondsOfDuration(project)));

        final Category category = project.category();
        if (category != null) {
          put("category", category.name());
          final Category parent = category.parent();
          if (parent != null) {
            put("parent_category", parent.name());
          }
        }

        final Location location = project.location();
        if (location != null) {
          put("location", location.name());
        }
      }
    };

    final Map<String, Object> prefixedMap = MapUtils.prefixKeys(properties, prefix);

    prefixedMap.putAll(userProperties(project.creator(), "creator_"));

    if (loggedInUser != null) {
      prefixedMap.put("user_is_project_creator", ProjectUtils.userIsCreator(project, loggedInUser));
      prefixedMap.put("user_is_backer", project.isBacking());
      prefixedMap.put("user_has_starred", project.isStarred());
    }

    return prefixedMap;
  }

  public static @NonNull Map<String, Object> activityProperties(final @NonNull Activity activity) {
    return activityProperties(activity, "activity_");
  }

  public static @NonNull Map<String, Object> activityProperties(final @NonNull Activity activity, final @NonNull String prefix) {
    Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("category", activity.category());
      }
    };

    properties = MapUtils.prefixKeys(properties, prefix);

    final Project project = activity.project();
    if (project != null) {
      properties.putAll(projectProperties(project));

      final Update update = activity.update();
      if (update != null) {
        properties.putAll(updateProperties(project, update));
      }
    }

    return properties;
  }

  public static @NonNull Map<String, Object> updateProperties(final @NonNull Project project, final @NonNull Update update) {
    return updateProperties(project, update, "update_");
  }

  public static @NonNull Map<String, Object> updateProperties(final @NonNull Project project, final @NonNull Update update, final @NonNull String prefix) {
    Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("id", update.id());
        put("title", update.title());
        put("visible", update.visible());
        put("comments_count", update.commentsCount());

        // TODO: add `public` to `Update` model
        // put("public")
        // TODO: how to convert update.publishedAt() to seconds since 1970
        // put("published_at", update.publishedAt())
      }
    };

    properties = MapUtils.prefixKeys(properties, prefix);

    properties.putAll(projectProperties(project));

    return properties;
  }
}
