package com.kickstarter.models;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringDef;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.kickstarter.R;
import com.kickstarter.libs.CurrencyOptions;
import com.kickstarter.libs.NumberUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

@ParcelablePlease
public class Project implements Parcelable {
  public Integer backersCount = null;
  public String blurb = null;
  public Category category = null;
  public Integer commentsCount = null;
  public String country = null; // e.g.: US
  public DateTime createdAt = null;
  public String currency = null; // e.g.: USD
  public String currencySymbol = null; // e.g.: $
  public boolean currencyTrailingCode = false;
  public DateTime deadline = null;
  public Float goal = null;
  public Integer id = null; // in the Kickstarter app, this is project.pid not project.id
  public DateTime launchedAt = null;
  public Location location = null;
  public String name = null;
  public Float pledged = null;
  public Photo photo = null;
  public Video video = null;
  public DateTime potdAt = null;
  public @State String state = null;
  public String slug = null;
  public User creator = null;
  public Integer updatesCount = null;
  public Urls urls = null;
  public List<Reward> rewards = null;
  public boolean isBacking = false;
  public boolean isStarred = false;
  public DateTime updatedAt = null;

  public Integer backersCount() { return backersCount; }
  public String formattedBackersCount() {
    return NumberUtils.numberWithDelimiter(backersCount);
  }
  public String blurb() { return blurb; }
  public Category category() { return category; }
  public User creator() { return creator; }
  public String formattedCommentsCount() {
    return NumberUtils.numberWithDelimiter(commentsCount);
  }
  public String country() { return country; }
  public String currency() { return currency; }
  public String currencySymbol() { return currencySymbol; }
  public boolean currencyTrailingCode() { return currencyTrailingCode; }
  public DateTime deadline() { return deadline; }
  public Float goal() { return goal; }
  public Integer id() { return id; }
  public boolean isBacking() {
    return isBacking;
  }
  public boolean isStarred() {
    return isStarred;
  }
  public DateTime launchedAt() { return launchedAt; }
  public Location location() { return location; }
  public String name() { return name; }
  public Float pledged() { return pledged; }
  public Photo photo() { return photo; }
  public @State String state() { return state; }
  public Video video() { return video; }
  public String slug() { return slug; }
  public String formattedUpdatesCount() {
    return NumberUtils.numberWithDelimiter(updatesCount);
  }
  public Urls urls() { return urls; }
  public String creatorBioUrl() {
    return urls().web().creatorBio();
  }
  public String descriptionUrl() {
    return urls().web().description();
  }
  public String webProjectUrl() { return urls().web().project(); }
  public String updatesUrl() {
    return urls().web().updates();
  }

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

  public List<Reward> rewards() {
    return rewards;
  }

  static public Project createFromParam(final String param) {
    final Project project = new Project();
    try {
      project.id = Integer.parseInt(param);
    } catch (NumberFormatException e) {
      project.slug = param;
    }
    return project;
  }

  @ParcelablePlease
  public static class Urls implements Parcelable {
    public Web web = null;
    public Api api = null;

    public Web web() {
      return web;
    }
    public Api api() {
      return api;
    }

    @ParcelablePlease
    public static class Web implements Parcelable {
      public String project = null;
      public String rewards = null;
      public String updates = null;

      public String creatorBio() { return Uri.parse(project()).buildUpon().appendEncodedPath("/creator_bio").toString(); }
      public String description() { return Uri.parse(project()).buildUpon().appendEncodedPath("/description").toString(); }
      public String project() { return project; }
      public String rewards() { return rewards; }
      public String updates() { return updates; }

      @Override
      public int describeContents() { return 0; }
      @Override
      public void writeToParcel(Parcel dest, int flags) {com.kickstarter.models.WebParcelablePlease.writeToParcel(this, dest, flags);}
      public static final Creator<Web> CREATOR = new Creator<Web>() {
        public Web createFromParcel(Parcel source) {
          Web target = new Web();
          com.kickstarter.models.WebParcelablePlease.readFromParcel(target, source);
          return target;
        }
        public Web[] newArray(int size) {return new Web[size];}
      };
    }

    @ParcelablePlease
    public static class Api implements Parcelable {
      public String comments = null;

      public String comments() { return comments; }

      @Override
      public int describeContents() { return 0; }
      @Override
      public void writeToParcel(Parcel dest, int flags) {com.kickstarter.models.ApiParcelablePlease.writeToParcel(this, dest, flags);}
      public static final Creator<Api> CREATOR = new Creator<Api>() {
        public Api createFromParcel(Parcel source) {
          Api target = new Api();
          com.kickstarter.models.ApiParcelablePlease.readFromParcel(target, source);
          return target;
        }
        public Api[] newArray(int size) {return new Api[size];}
      };
    }

    @Override
    public int describeContents() { return 0; }
    @Override
    public void writeToParcel(Parcel dest, int flags) {com.kickstarter.models.UrlsParcelablePlease.writeToParcel(this, dest, flags);}
    public static final Creator<Urls> CREATOR = new Creator<Urls>() {
      public Urls createFromParcel(Parcel source) {
        Urls target = new Urls();
        com.kickstarter.models.UrlsParcelablePlease.readFromParcel(target, source);
        return target;
      }
      public Urls[] newArray(int size) {return new Urls[size];}
    };
  }

  public CurrencyOptions currencyOptions() {
    return new CurrencyOptions(country, currencySymbol, currency);
  }

  /** Returns whether the project is in a canceled state. */
  public boolean isCanceled() {
    return STATE_CANCELED.equals(state);
  }

  /** Returns whether the project is in a failed state. */
  public boolean isFailed() {
    return STATE_FAILED.equals(state);
  }

  /** Returns whether the project is in a live state. */
  public boolean isLive() {
    return STATE_LIVE.equals(state);
  }

  public boolean isPotdToday() {
    if (potdAt == null) {
      return false;
    }

    final DateTime startOfDayUTC = new DateTime(DateTimeZone.UTC).withTime(0, 0, 0, 0);
    return startOfDayUTC.isEqual(potdAt.withZone(DateTimeZone.UTC));
  }

  /** Returns whether the project is in a purged state. */
  public boolean isPurged() {
    return STATE_PURGED.equals(state);
  }

  /** Returns whether the project is in a live state. */
  public boolean isStarted() {
    return STATE_STARTED.equals(state);
  }

  /** Returns whether the project is in a submitted state. */
  public boolean isSubmitted() {
    return STATE_SUBMITTED.equals(state);
  }

  /** Returns whether the project is in a suspended state. */
  public boolean isSuspended() {
    return STATE_SUSPENDED.equals(state);
  }

  /** Returns whether the project is in a successful state. */
  public boolean isSuccessful() {
    return STATE_SUCCESSFUL.equals(state);
  }

  public Float percentageFunded() {
    if (goal > 0.0f) {
      return (pledged / goal) * 100.0f;
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
      new Duration(new DateTime(), deadline).getStandardSeconds());
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

  public boolean isDisplayable() {
    return createdAt != null;
  }

  public String param() {
    return id != null ? id.toString() : slug;
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

  @Override
  public int describeContents() { return 0; }
  @Override
  public void writeToParcel(Parcel dest, int flags) {ProjectParcelablePlease.writeToParcel(this, dest, flags);}
  public static final Creator<Project> CREATOR = new Creator<Project>() {
    public Project createFromParcel(Parcel source) {
      Project target = new Project();
      ProjectParcelablePlease.readFromParcel(target, source);
      return target;
    }
    public Project[] newArray(int size) {return new Project[size];}
  };
}
