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
        put("pwl", BooleanUtils.isTrue(params.staffPicks()));
        put("recommended", BooleanUtils.isTrue(params.recommended()));
        put("ref_tag", DiscoveryParamsUtils.refTag(params).tag());
        put("search_term", params.term());
        put("social", BooleanUtils.isIntTrue(params.social()));
        put("sort", params.sort() != null ? String.valueOf(params.sort()) : null);
        put("tag", params.tagId());
        put("watched", BooleanUtils.isIntTrue(params.starred()));

        final Category category = params.category();
        if (category != null) {
          if (category.isRoot()) {
            putAll(categoryProperties(category));
          } else {
            putAll(categoryProperties(category.root()));
            putAll(subcategoryProperties(category));
          }
        }
      }
    });

    return MapUtils.prefixKeys(properties, prefix);
  }

  public static @NonNull Map<String, Object> categoryProperties(final @NonNull Category category) {
    return categoryProperties(category, "category_");
  }

  public static @NonNull Map<String, Object> subcategoryProperties(final @NonNull Category category) {
    return categoryProperties(category, "subcategory_");
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
        final Category category = project.category();
        if (category != null) {
          if (category.isRoot()) {
            put("category", category.name());
          } else {
            final Category parent = category.parent();
            put("category", parent != null ? parent.name() : category.parentName());
            put("subcategory", category.name());
          }
        }
        put("comments_count", project.commentsCount());
        put("country", project.country());
        put("creator_uid", project.creator().id());
        put("currency", project.currency());
        put("current_pledge_amount", project.pledged());
        put("current_pledge_amount_usd", project.pledged() * project.staticUsdRate());
        put("deadline", project.deadline() != null ? project.deadline().getMillis() / 1000 : null);
        put("duration", Math.round(ProjectUtils.timeInSecondsOfDuration(project)));
        put("goal", project.goal());
        put("goal_usd", project.goal() * project.staticUsdRate());
        put("has_video", project.video() != null);
        put("hours_remaining", (int) Math.ceil(ProjectUtils.timeInSecondsUntilDeadline(project) / 60.0f / 60.0f));
        put("is_repeat_creator", IntegerUtils.intValueOrZero(project.creator().createdProjectsCount()) >= 2);
        put("launched_at", project.launchedAt() != null ? project.launchedAt().getMillis() / 1000 : null);
        put("location", project.location() != null ? project.location().name() : null);
        put("name", project.name());
        put("percent_raised", project.percentageFunded() / 100.0f);
        put("pid", project.id());
        put("prelaunch_activated", BooleanUtils.isTrue(project.prelaunchActivated()));
        put("rewards_count", project.hasRewards() ? project.rewards().size() : null);
        put("state", project.state());
        put("static_usd_rate", project.staticUsdRate());
        put("updates_count", project.updatesCount());
        put("user_is_project_creator", ProjectUtils.userIsCreator(project, loggedInUser));
        put("user_is_backer", project.isBacking());
        put("user_has_watched", project.isStarred());
      }
    };

    return MapUtils.prefixKeys(properties, prefix);
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
