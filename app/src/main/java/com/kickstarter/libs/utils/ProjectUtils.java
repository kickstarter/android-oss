package com.kickstarter.libs.utils;

import android.content.Context;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import type.CreditCardTypes;

public final class ProjectUtils {
  private ProjectUtils() {}

  public static boolean acceptedCardType(final @NonNull CreditCardTypes type, final @NonNull Project project) {
    final List<String> availableCardTypes = project.availableCardTypes();
    return availableCardTypes != null && availableCardTypes.contains(type.rawValue());
  }

  public static List<Pair<Project, DiscoveryParams>> combineProjectsAndParams(final @NonNull List<Project> projects, final @NonNull DiscoveryParams params) {
    final ArrayList<Pair<Project, DiscoveryParams>> projectAndParams = new ArrayList<>(projects.size());
    for (int i = 0; i < projects.size(); i++) {
      projectAndParams.add(Pair.create(projects.get(i), params));
    }
    return projectAndParams;
  }

  /**
   * Returns time until project reaches deadline along with the unit,
   * e.g. `25 minutes`, `8 days`.
   */
  public static @NonNull String deadlineCountdown(final @NonNull Project project, final @NonNull Context context) {
    return new StringBuilder().append(deadlineCountdownValue(project))
      .append(" ")
      .append(deadlineCountdownUnit(project, context))
      .toString();
  }

  /*
   * Returns unit of time remaining in a readable string, e.g. `days to go`, `hours to go`.
   */
  public static @NonNull String deadlineCountdownDetail(final @NonNull Project project, final @NonNull Context context,
    final @NonNull KSString ksString) {
    return ksString.format(context.getString(R.string.discovery_baseball_card_time_left_to_go),
      "time_left", deadlineCountdownUnit(project, context)
    );
  }

  /**
   * Returns the most appropriate unit for the time remaining until the project
   * reaches its deadline.
   *
   * @param  context an Android context.
   * @return         the String unit.
   */
  public static @NonNull String deadlineCountdownUnit(final @NonNull Project project, final @NonNull Context context) {
    final Long seconds = timeInSecondsUntilDeadline(project);
    if (seconds <= 1.0 && seconds > 0.0) {
      return context.getString(R.string.discovery_baseball_card_deadline_units_secs);
    } else if (seconds <= 120.0) {
      return context.getString(R.string.discovery_baseball_card_deadline_units_secs);
    } else if (seconds <= 120.0 * 60.0) {
      return context.getString(R.string.discovery_baseball_card_deadline_units_mins);
    } else if (seconds <= 72.0 * 60.0 * 60.0) {
      return context.getString(R.string.discovery_baseball_card_deadline_units_hours);
    }
    return context.getString(R.string.discovery_baseball_card_deadline_units_days);
  }

  /**
   * Returns time remaining until project reaches deadline in either seconds,
   * minutes, hours or days. A time unit is chosen such that the number is
   * readable, e.g. 5 minutes would be preferred to 300 seconds.
   *
   * @return the Integer time remaining.
   */
  public static int deadlineCountdownValue(final @NonNull Project project) {
    final Long seconds = timeInSecondsUntilDeadline(project);
    if (seconds <= 120.0) {
      return seconds.intValue(); // seconds
    } else if (seconds <= 120.0 * 60.0) {
      return (int) Math.floor(seconds / 60.0); // minutes
    } else if (seconds < 72.0 * 60.0 * 60.0) {
      return (int) Math.floor(seconds / 60.0 / 60.0); // hours
    }
    return (int) Math.floor(seconds / 60.0 / 60.0 / 24.0); // days
  }

  /**
   * Returns `true` if the project is no longer live, `false` otherwise.
   */
  public static boolean isCompleted(final @NonNull Project project) {
    final String state = project.state();
    return Project.STATE_CANCELED.equals(state) ||
      Project.STATE_FAILED.equals(state) ||
      Project.STATE_SUCCESSFUL.equals(state) ||
      Project.STATE_PURGED.equals(state) ||
      Project.STATE_SUSPENDED.equals(state);
  }

  /**
   * Returns `true` if the project name ends with a punctuation character.
   */
  public static boolean isProjectNamePunctuated(final @NonNull String name) {
    final String lastChar = name.substring(name.length() - 1);
    return lastChar.matches(".*\\p{Punct}");
  }

  public static @Nullable Metadata metadataForProject(final @NonNull Project project) {
    if (project.isBacking()) {
      return Metadata.BACKING;
    } else if (project.isStarred()) {
      return Metadata.SAVING;
    } else if (project.isFeaturedToday()) {
      return Metadata.CATEGORY_FEATURED;
    }
    return null;
  }

  /**
   * Returns 16:9 height relative to input width.
   */
  public static int photoHeightFromWidthRatio(final int width) {
    return width * 9 / 16;
  }

  /**
   * Returns time between project launch and deadline.
   */
  public static @NonNull Long timeInSecondsOfDuration(final @NonNull Project project) {
    return new Duration(project.launchedAt(), project.deadline()).getStandardSeconds();
  }

  /**
   * Returns time until project reaches deadline in seconds, or 0 if the
   * project has already finished.
   */
  public static @NonNull Long timeInSecondsUntilDeadline(final @NonNull Project project) {
    return Math.max(0L,
      new Duration(new DateTime(), project.deadline()).getStandardSeconds());
  }

  public static boolean userIsCreator(final @NonNull Project project, final @Nullable User user) {
    return user == null ? false : project.creator().id() == user.id();
  }

  public static boolean isUSUserViewingNonUSProject(final @NonNull String userCountry, final @NonNull String projectCountry) {
    return I18nUtils.isCountryUS(userCountry) && !I18nUtils.isCountryUS(projectCountry);
  }

  public enum Metadata {
    BACKING, SAVING, CATEGORY_FEATURED
  }
}
