package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.kickstarter.libs.CurrencyOptions;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

@ParcelablePlease
public class Project implements Parcelable {
  Integer backers_count = null;
  String blurb = null;
  Category category = null;
  String country = null; // e.g.: US
  String currency = null; // e.g.: USD
  String currency_symbol = null; // e.g.: $
  Boolean currency_trailing_code = false;
  DateTime deadline = null;
  Float goal = null;
  Integer id = null;
  DateTime launched_at = null;
  Location location = null;
  String name = null;
  Float pledged = null;
  Photo photo = null;
  DateTime potd_at = null;
  User creator = null;

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
