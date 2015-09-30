package com.kickstarter.models;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.kickstarter.R;
import com.kickstarter.libs.AutoGson;
import com.kickstarter.libs.CurrencyOptions;
import com.kickstarter.libs.NumberUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Project implements Parcelable {
  public abstract int backersCount();
  public abstract String blurb();
  @Nullable public abstract Category category();
  @Nullable public abstract Integer commentsCount();
  public abstract String country(); // e.g.: US
  public abstract DateTime createdAt();
  public abstract User creator();
  public abstract String currency(); // e.g.: USD
  public abstract String currencySymbol(); // e.g.: $
  public abstract boolean currencyTrailingCode();
  @Nullable public abstract DateTime deadline();
  public abstract float goal();
  public abstract long id(); // in the Kickstarter app, this is project.pid not project.id
  public abstract boolean isBacking();
  public abstract boolean isStarred();
  @Nullable public abstract DateTime launchedAt();
  @Nullable public abstract Location location();
  public abstract String name();
  public abstract float pledged();
  @Nullable public abstract Photo photo();
  @Nullable public abstract DateTime potdAt();
  @Nullable public abstract String slug();
  @State public abstract String state();
  @Nullable public abstract Integer updatesCount();
  @Nullable public abstract List<Reward> rewards();
  public abstract DateTime updatedAt();
  public abstract Urls urls();
  @Nullable public abstract Video video();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder backersCount(int __);
    public abstract Builder blurb(String __);
    public abstract Builder category(Category __);
    public abstract Builder commentsCount(Integer __);
    public abstract Builder country(String __);
    public abstract Builder createdAt(DateTime __);
    public abstract Builder creator(User __);
    public abstract Builder currency(String __);
    public abstract Builder currencySymbol(String __);
    public abstract Builder currencyTrailingCode(boolean __);
    public abstract Builder deadline(DateTime __);
    public abstract Builder goal(float __);
    public abstract Builder id(long __);
    public abstract Builder isBacking(boolean __);
    public abstract Builder isStarred(boolean __);
    public abstract Builder launchedAt(DateTime __);
    public abstract Builder location(Location __);
    public abstract Builder name(String __);
    public abstract Builder pledged(float __);
    public abstract Builder photo(Photo __);
    public abstract Builder potdAt(DateTime __);
    public abstract Builder rewards(List<Reward> __);
    public abstract Builder slug(String __);
    public abstract Builder state(@State String __);
    public abstract Builder updatedAt(DateTime __);
    public abstract Builder updatesCount(Integer __);
    public abstract Builder urls(Urls __);
    public abstract Builder video(Video __);
    public abstract Project build();
  }

  public static Builder builder() {
    return new AutoParcel_Project.Builder()
      .isBacking(false)
      .isStarred(false)
      .rewards(new ArrayList<>());
  }

  public abstract Builder toBuilder();

  public static final String STATE_STARTED      = "started";
  public static final String STATE_SUBMITTED    = "submitted";
  public static final String STATE_LIVE         = "live";
  public static final String STATE_SUCCESSFUL   = "successful";
  public static final String STATE_FAILED       = "failed";
  public static final String STATE_CANCELED     = "canceled";
  public static final String STATE_SUSPENDED    = "suspended";
  public static final String STATE_PURGED       = "purged";

  @Retention(RetentionPolicy.SOURCE)
  @StringDef({STATE_STARTED, STATE_SUBMITTED, STATE_LIVE, STATE_SUCCESSFUL, STATE_FAILED, STATE_CANCELED, STATE_SUSPENDED, STATE_PURGED})
  public @interface State {}

  public String creatorBioUrl() {
    return urls().web().creatorBio();
  }

  public String descriptionUrl() {
    return urls().web().description();
  }

  public String formattedBackersCount() {
    return NumberUtils.numberWithDelimiter(backersCount());
  }

  public String formattedCommentsCount() {
    return NumberUtils.numberWithDelimiter(commentsCount());
  }

  public String formattedUpdatesCount() {
    return NumberUtils.numberWithDelimiter(updatesCount());
  }

  public String updatesUrl() {
    return urls().web().updates();
  }

  public String webProjectUrl() {
    return urls().web().project();
  }

  @AutoParcel
  @AutoGson
  public abstract static class Urls implements Parcelable {
    public abstract Web web();
    @Nullable public abstract Api api();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder web(Web __);
      public abstract Builder api(Api __);
      public abstract Urls build();
    }

    public static Builder builder() {
      return new AutoParcel_Project_Urls.Builder();
    }

    @AutoParcel
    @AutoGson
    public abstract static class Web implements Parcelable {
      public abstract String project();
      @Nullable public abstract String projectShort();
      public abstract String rewards();
      @Nullable public abstract String updates();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract Builder project(String __);
        public abstract Builder projectShort(String __);
        public abstract Builder rewards(String __);
        public abstract Builder updates(String __);
        public abstract Web build();
      }

      public static Builder builder() {
        return new AutoParcel_Project_Urls_Web.Builder();
      }

      public String creatorBio() {
        return Uri.parse(project())
          .buildUpon()
          .appendEncodedPath("/creator_bio")
          .toString();
      }

      public String description() {
        return Uri.parse(project())
          .buildUpon()
          .appendEncodedPath("/description")
          .toString();
      }
    }

    @AutoParcel
    @AutoGson
    public abstract static class Api implements Parcelable {
      @Nullable public abstract String project();
      @Nullable public abstract String comments();
      @Nullable public abstract String updates();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract Builder project(String __);
        public abstract Builder comments(String __);
        public abstract Builder updates(String __);
        public abstract Api build();
      }

      public static Builder builder() {
        return new AutoParcel_Project_Urls_Api.Builder();
      }
    }
  }

  public CurrencyOptions currencyOptions() {
    return new CurrencyOptions(country(), currencySymbol(), currency());
  }

  public boolean hasComments() {
    return this.commentsCount() != null && Integer.valueOf(this.commentsCount()) != 0;
  }

  public boolean hasRewards() {
    return rewards() != null;
  }

  /** Returns whether the project is in a canceled state. */
  public boolean isCanceled() {
    return STATE_CANCELED.equals(state());
  }

  /** Returns whether the project is in a failed state. */
  public boolean isFailed() {
    return STATE_FAILED.equals(state());
  }

  /** Returns whether the project is in a live state. */
  public boolean isLive() {
    return STATE_LIVE.equals(state());
  }

  public boolean isPotdToday() {
    if (potdAt() == null) {
      return false;
    }

    final DateTime startOfDayUTC = new DateTime(DateTimeZone.UTC).withTime(0, 0, 0, 0);
    return startOfDayUTC.isEqual(potdAt().withZone(DateTimeZone.UTC));
  }

  /** Returns whether the project is in a purged state. */
  public boolean isPurged() {
    return STATE_PURGED.equals(state());
  }

  /** Returns whether the project is in a live state. */
  public boolean isStarted() {
    return STATE_STARTED.equals(state());
  }

  /** Returns whether the project is in a submitted state. */
  public boolean isSubmitted() {
    return STATE_SUBMITTED.equals(state());
  }

  /** Returns whether the project is in a suspended state. */
  public boolean isSuspended() {
    return STATE_SUSPENDED.equals(state());
  }

  /** Returns whether the project is in a successful state. */
  public boolean isSuccessful() {
    return STATE_SUCCESSFUL.equals(state());
  }

  public Float percentageFunded() {
    if (goal() > 0.0f) {
      return (pledged() / goal()) * 100.0f;
    }

    return 0.0f;
  }

  /**
   * Returns a String describing the time remaining for a project, e.g.
   * 25 minutes to go, 8 days to go.
   *
   * @param  context an Android context.
   * @return         the String time remaining.
   */
  public String timeToGo(final Context context) {
    return new StringBuilder(deadlineCountdown(context))
      .append(context.getString(R.string._to_go))
      .toString();
  }

  /**
   * Returns time until project reaches deadline along with the unit,
   * e.g. 25 minutes, 8 days.
   *
   * @param  context an Android context.
   * @return         the String time remaining.
   */
  public String deadlineCountdown(final Context context) {
    return new StringBuilder().append(deadlineCountdownValue())
      .append(" ")
      .append(deadlineCountdownUnit(context))
      .toString();
  }

  /**
   * Returns time until project reaches deadline in seconds, or 0 if the
   * project has already finished.
   *
   * @return the Long number of seconds remaining.
   */
  public Long timeInSecondsUntilDeadline() {
    return Math.max(0L,
      new Duration(new DateTime(), deadline()).getStandardSeconds());
  }

  /**
   * Returns time remaining until project reaches deadline in either seconds,
   * minutes, hours or days. A time unit is chosen such that the number is
   * readable, e.g. 5 minutes would be preferred to 300 seconds.
   *
   * @return the Integer time remaining.
   */
  public Integer deadlineCountdownValue() {
    final Long seconds = timeInSecondsUntilDeadline();
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
   * Returns the most appropriate unit for the time remaining until the project
   * reaches its deadline.
   *
   * @param  context an Android context.
   * @return         the String unit.
   */
  public String deadlineCountdownUnit(final Context context) {
    final Long seconds = timeInSecondsUntilDeadline();
    if (seconds <= 1.0 && seconds > 0.0) {
      return context.getString(R.string.secs);
    } else if (seconds <= 120.0) {
      return context.getString(R.string.secs);
    } else if (seconds <= 120.0 * 60.0) {
      return context.getString(R.string.mins);
    } else if (seconds <= 72.0 * 60.0 * 60.0) {
      return context.getString(R.string.hours);
    }
    return context.getString(R.string.days);
  }

  public String param() {
    return slug() != null ? slug() : String.valueOf(id());
  }

  public String secureWebProjectUrl() {
    // TODO: Just use http with local env
    return Uri.parse(webProjectUrl()).buildUpon().scheme("https").build().toString();
  }

  public String newPledgeUrl() {
    return Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/new").toString();
  }

  public String editPledgeUrl() {
    return Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/edit").toString();
  }

  public String rewardSelectedUrl(final Reward reward) {
    return Uri.parse(newPledgeUrl())
      .buildUpon().scheme("https")
      .appendQueryParameter("backing[backer_reward_id]", String.valueOf(reward.id()))
      .appendQueryParameter("clicked_reward", "true")
      .build()
      .toString();
  }
}
