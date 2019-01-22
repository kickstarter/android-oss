package com.kickstarter.libs.utils;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class KoalaUtils {
  private KoalaUtils() {}

  public static @NonNull Map<String, Object> discoveryParamsProperties(final @NonNull DiscoveryParams params) {
    return discoveryParamsProperties(params, "discover_");
  }

  public static @NonNull Map<String, Object> discoveryParamsProperties(final @NonNull DiscoveryParams params, final @NonNull String prefix) {

    final Map<String, Object> properties = Collections.unmodifiableMap(new HashMap<String, Object>() {
      {
        put("everything", BooleanUtils.isTrue(params.isAllProjects()));
        put("recommended", BooleanUtils.isTrue(params.recommended()));
        put("social", BooleanUtils.isIntTrue(params.social()));
        put("staff_picks", BooleanUtils.isTrue(params.staffPicks()));
        put("starred", BooleanUtils.isIntTrue(params.starred()));
        put("term", params.term());
        put("sort", params.sort() != null ? String.valueOf(params.sort()) : "");

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

    final Map<String, Object> prefixedProperties = MapUtils.prefixKeys(properties, prefix);

    prefixedProperties.put("page", params.page());
    prefixedProperties.put("per_page", params.perPage());

    return prefixedProperties;
  }

  public static @NonNull Map<String, Object> categoryProperties(final @NonNull Category category) {
    return categoryProperties(category, "category_");
  }

  public static @NonNull Map<String, Object> categoryProperties(final @NonNull Category category, final @NonNull String prefix) {
    final Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("id", category.id());
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

  public static @NonNull Map<String, Object> projectProperties(final @NonNull Project project, final @Nullable User loggedInUser) {
    return projectProperties(project, loggedInUser, "project_");
  }

  public static @NonNull Map<String, Object> projectProperties(final @NonNull Project project, final @Nullable User loggedInUser, final @NonNull String prefix) {
    final Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("backers_count", project.backersCount());
        put("comments_count", project.commentsCount());
        put("country", project.country());
        put("duration", Math.round(ProjectUtils.timeInSecondsOfDuration(project)));
        put("currency", project.currency());
        put("goal", project.goal());
        put("has_video", project.video() != null);
        put("hours_remaining", (int) Math.ceil(ProjectUtils.timeInSecondsUntilDeadline(project) / 60.0f / 60.0f));
        put("name", project.name());
        put("percent_raised", project.percentageFunded() / 100.0f);
        put("pid", project.id());
        put("pledged", project.pledged());
        put("state", project.state());
        put("update_count", project.updatesCount());

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

  public static @NonNull Map<String, Object> activityProperties(final @NonNull Activity activity, final @Nullable User loggedInUser) {
    return activityProperties(activity, loggedInUser, "activity_");
  }

  public static @NonNull Map<String, Object> activityProperties(final @NonNull Activity activity, final @Nullable User loggedInUser, final @NonNull String prefix) {
    Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("category", activity.category());
      }
    };

    properties = MapUtils.prefixKeys(properties, prefix);

    final Project project = activity.project();
    if (project != null) {
      properties.putAll(projectProperties(project, loggedInUser));

      final Update update = activity.update();
      if (update != null) {
        properties.putAll(updateProperties(project, update, loggedInUser));
      }
    }

    return properties;
  }

  public static @NonNull Map<String, Object> updateProperties(final @NonNull Project project, final @NonNull Update update, final @Nullable User loggedInUser) {
    return updateProperties(project, update, loggedInUser, "update_");
  }

  public static @NonNull Map<String, Object> updateProperties(final @NonNull Project project, final @NonNull Update update, final @Nullable User loggedInUser, final @NonNull String prefix) {
    Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("comments_count", update.commentsCount());
        put("has_liked", update.hasLiked());
        put("id", update.id());
        put("likes_count", update.likesCount());
        put("title", update.title());
        put("sequence", update.sequence());
        put("visible", update.visible());
        put("published_at", update.publishedAt());
      }
    };

    properties = MapUtils.prefixKeys(properties, prefix);

    properties.putAll(projectProperties(project, loggedInUser));

    return properties;
  }
}
