package com.kickstarter.libs.utils;

import android.content.Context;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.RewardsItem;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import static com.kickstarter.libs.utils.BooleanUtils.isTrue;

public final class RewardUtils {
  private RewardUtils() {}

  public static final double MAX_REWARD_LIMIT = 2000000000;

  /**
   * Returns the drawable resource ID of the check background based on project status.
   */
  public static @DrawableRes int checkBackgroundDrawable(final @NonNull Project project) {
    if (project.isLive()) {
      return R.drawable.circle_blue_alpha_6;
    } else {
      return R.drawable.circle_grey_300;
    }
  }

  /**
   * Returns `true` if the reward has backers, `false` otherwise.
   */
  public static boolean hasBackers(final @NonNull Reward reward) {
    return IntegerUtils.isNonZero(reward.backersCount());
  }

  public static boolean isAvailable(final @NonNull Project project, final @NonNull Reward reward) {
    return project.isLive() && !RewardUtils.isLimitReached(reward) && !RewardUtils.isExpired(reward);
  }

  /**
   * Returns `true` if the reward has expired.
   */
  public static boolean isExpired(final @NonNull Reward reward) {
    return isTimeLimited(reward) && reward.endsAt().isBeforeNow();
  }

  /**
   * Returns `true` if the reward has items, `false` otherwise.
   */
  public static boolean isItemized(final @NonNull Reward reward) {
    final List<RewardsItem> rewardsItems = reward.rewardsItems();
    return rewardsItems != null && !rewardsItems.isEmpty();
  }

  /**
   * Returns `true` if the reward has a limit set, and the limit has not been reached, `false` otherwise.
   */
  public static boolean isLimited(final @NonNull Reward reward) {
    return reward.limit() != null && !isLimitReached(reward);
  }

  /**
   * Returns `true` if the reward's limit has been reached, `false` otherwise.
   */
  public static boolean isLimitReached(final @NonNull Reward reward) {
    final Integer remaining = reward.remaining();

    return reward.limit() != null
      && remaining != null
      && remaining <= 0;
  }

  public static boolean isMaxRewardAmount(final double amount) {
    return amount >= MAX_REWARD_LIMIT;
  }

  /**
   * Returns `true` if the reward is considered the 'non-reward' option, i.e. the reward is the option
   * backers select when they want to pledge to a project without selecting a particular reward.
   */
  public static boolean isNoReward(final @NonNull Reward reward) {
    return reward.id() == 0;
  }

  /**
   * Returns `true` if the reward is a specific reward for a project, i.e. it is not the 'no-reward' option.
   */
  public static boolean isReward(final @NonNull Reward reward) {
    return !isNoReward(reward);
  }

  /**
   * Returns `true` if the reward has shipping enabled, `false` otherwise.
   */
  public static boolean isShippable(final @NonNull Reward reward) {
    return isTrue(reward.shippingEnabled());
  }

  /**
   * Returns `true` if the reward has a valid expiration date.
   */
  public static boolean isTimeLimited(final @NonNull Reward reward) {
    // TODO: 2019-06-14 remove epoch check after Garrow fixes `current` bug in backend
    return reward.endsAt() != null && !DateTimeUtils.isEpoch(reward.endsAt());
  }

  /**
   * Returns unit of time remaining in a readable string, e.g. `days to go`, `hours to go`.
   */
  public static @NonNull String deadlineCountdownDetail(final @NonNull Reward reward, final @NonNull Context context,
    final @NonNull KSString ksString) {
    return ksString.format(context.getString(R.string.discovery_baseball_card_time_left_to_go),
      "time_left", deadlineCountdownUnit(reward, context)
    );
  }

  /**
   * Returns the most appropriate unit for the time remaining until the reward
   * reaches its deadline.
   *
   * @param  context an Android context.
   * @return         the String unit.
   */
  public static @NonNull String deadlineCountdownUnit(final @NonNull Reward reward, final @NonNull Context context) {
    final Long seconds = timeInSecondsUntilDeadline(reward);
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
   * Returns time until reward reaches deadline in seconds, or 0 if the
   * reward has already finished.
   */
  public static @NonNull Long timeInSecondsUntilDeadline(final @NonNull Reward reward) {
    return Math.max(0L,
      new Duration(new DateTime(), reward.endsAt()).getStandardSeconds());
  }

  /**
   * Returns time remaining until reward reaches deadline in either seconds,
   * minutes, hours or days. A time unit is chosen such that the number is
   * readable, e.g. 5 minutes would be preferred to 300 seconds.
   *
   * @return the Integer time remaining.
   */
  public static int deadlineCountdownValue(final @NonNull Reward reward) {
    final Long seconds = timeInSecondsUntilDeadline(reward);
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
   * Returns the color resource ID of the rewards button based on project and if user has backed reward.
   */
  public static @ColorRes int pledgeButtonColor(final @NonNull Project project, final @NonNull Reward reward) {
    if (BackingUtils.isBacked(project, reward) && project.isLive()) {
      return R.color.button_pledge_manage;
    } else if (!project.isLive()) {
      return R.color.button_pledge_ended;
    } else {
      return R.color.button_pledge_live;
    }
  }

  /**
   * Returns the string resource ID of the rewards button based on project and reward status.
   */
  public static @StringRes int pledgeButtonAlternateText(final @NonNull Project project, final @NonNull Reward reward) {
    if (BackingUtils.isBacked(project, reward) && project.isLive()) {
      return R.string.Manage_your_pledge;
    } else if (BackingUtils.isBacked(project, reward) && !project.isLive()) {
      return R.string.View_your_pledge;
    } else if (isAvailable(project, reward) && project.isBacking()) {
      return R.string.Select_this_instead;
    } else if (!isAvailable(project, reward)) {
      return R.string.No_longer_available;
    } else {
      throw new IllegalStateException();
    }
  }
}
