package com.kickstarter.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.kickstarter.libs.CurrencyOptions;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import java.util.List;

@ParcelablePlease
public class Project implements Parcelable {
  public Integer backers_count = null;
  public String blurb = null;
  public Category category = null;
  public String country = null; // e.g.: US
  public String currency = null; // e.g.: USD
  public String currency_symbol = null; // e.g.: $
  public Boolean currency_trailing_code = false;
  public DateTime deadline = null;
  public Float goal = null;
  public Integer id = null; // in the Kickstarter app, this is project.pid not project.id
  public DateTime launched_at = null;
  public Location location = null;
  public String name = null;
  public Float pledged = null;
  public Photo photo = null;
  public Video video = null;
  public DateTime potd_at = null;
  public String slug = null;
  public User creator = null;
  public Urls urls = null;
  public List<Reward> rewards = null;

  public Integer backersCount() { return backers_count; }
  public String blurb() { return blurb; }
  public Category category() { return category; }
  public User creator() { return creator; }
  public String country() { return country; }
  public String currency() { return currency; }
  public String currencySymbol() { return currency_symbol; }
  public Boolean currencyTrailingCode() { return currency_trailing_code; }
  public DateTime deadline() { return deadline; }
  public Float goal() { return goal; }
  public Integer id() { return id; }
  public DateTime launchedAt() { return launched_at; }
  public Location location() { return location; }
  public String name() { return name; }
  public Float pledged() { return pledged; }
  public Photo photo() { return photo; }
  public Video video() { return video; }
  public String slug() { return slug; }
  public Urls urls() { return urls; }
  public String webProjectUrl() { return urls().web().project(); }
  public List<Reward> rewards() {
    return rewards;
  }

  @ParcelablePlease
  public static class Urls implements Parcelable {
    public Web web = null;

    public Web web() {
      return web;
    }

    @ParcelablePlease
    public static class Web implements Parcelable {
      public String project = null;
      public String rewards = null;

      public String project() { return project; }
      public String rewards() { return rewards; }

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
    return new CurrencyOptions(country, currency_symbol, currency);
  }

  public boolean isPotdToday() {
    if (potd_at == null) {
      return false;
    }

    final DateTime startOfDayUTC = new DateTime(DateTimeZone.UTC).withTime(0, 0, 0, 0);
    return startOfDayUTC.isEqual(potd_at.withZone(DateTimeZone.UTC));
  }

  public Float percentageFunded() {
    if (goal > 0.0f) {
      return (pledged / goal) * 100.0f;
    }

    return 0.0f;
  }

  public Long timeIntervalUntilDeadline() {
    final Duration duration = new Duration(new DateTime(), deadline);
    return Math.max(0L, duration.getStandardSeconds());
  }

  public Integer deadlineCountdown() {
    final Long seconds = timeIntervalUntilDeadline();
    if (seconds <= 120.0) {
      return seconds.intValue(); // seconds
    } else if (seconds <= 120.0 * 60.0) {
      return (int) Math.floor(seconds / 60.0); // minutes
    } else if (seconds < 72.0 * 60.0 * 60.0) {
      return (int) Math.floor(seconds / 60.0 / 60.0); // hours
    }
    return (int) Math.floor(seconds / 60.0 / 60.0 / 24.0); // days
  }

  public String deadlineCountdownUnit() {
    // TODO: Extract into string resource - needs context for lookup though
    final Long seconds = timeIntervalUntilDeadline();
    if (seconds <= 1.0 && seconds > 0.0) {
      return "secs";
    } else if (seconds <= 120.0) {
      return "secs";
    } else if (seconds <= 120.0 * 60.0) {
      return "mins";
    } else if (seconds <= 72.0 * 60.0 * 60.0) {
      return "hours";
    }
    return "days";
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

  // Parcelable
  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    ProjectParcelablePlease.writeToParcel(this, dest, flags);
  }

  public static final Creator<Project> CREATOR = new Creator<Project>() {
    public Project createFromParcel(Parcel source) {
      Project target = new Project();
      ProjectParcelablePlease.readFromParcel(target, source);
      return target;
    }

    public Project[] newArray(int size) {
      return new Project[size];
    }
  };
}
