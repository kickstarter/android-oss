package com.kickstarter.libs.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public final class ProjectUtils {
  private ProjectUtils() {}

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
  public static @NonNull Integer deadlineCountdownValue(final @NonNull Project project) {
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
   * Returns time until project reaches deadline in seconds, or 0 if the
   * project has already finished.
   */
  public static @NonNull Long timeInSecondsUntilDeadline(final @NonNull Project project) {
    return Math.max(0L,
      new Duration(new DateTime(), project.deadline()).getStandardSeconds());
  }

  public static boolean userIsCreator(@NonNull final Project project, @NonNull final User user) {
    return project.creator().id() == user.id();
  }
}

