package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.kickstarter.R;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Date;

@ParcelablePlease
public class Project implements Parcelable {
  Integer backers_count = null;
  String blurb = null;
  Category category = null;
  Integer deadline = null;
  Float goal = null;
  Integer id = null;
  Location location = null;
  String name = null;
  Float pledged = null;
  Photo photo = null;
  User creator = null;

  public Integer backersCount() { return backers_count; }
  public String blurb() { return blurb; }
  public Category category() { return category; }
  public Integer deadline() { return deadline; }
  public Float goal() { return goal; }
  public Integer id() { return id; }
  public Location location() { return location; }
  public String name() { return name; }
  public Float pledged() { return pledged; }
  public Photo photo() { return photo; }
  public User creator() { return creator; }

  public Float percentageFunded() {
    if (goal > 0.0f)
      return (pledged / goal) * 100.0f;

    return 0.0f;
  }

  public Long timeIntervalUntilDeadline() {
    Duration duration = new Duration(new DateTime(), new DateTime(deadline * 1000L));
    return Math.max(0L, duration.getStandardSeconds());
  }

  public Integer deadlineCountdown() {
    Long seconds = timeIntervalUntilDeadline();
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
    Long seconds = timeIntervalUntilDeadline();
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

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
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
