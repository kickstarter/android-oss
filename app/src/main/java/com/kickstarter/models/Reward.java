package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.kickstarter.libs.DateTimeUtils;

import org.joda.time.DateTime;

@ParcelablePlease
public class Reward implements Parcelable {
  String reward = null;
  Integer limit = null;
  DateTime estimated_delivery_on = null;
  Boolean shipping_enabled = null;
  Integer id = null;
  String shipping_preference = null;
  String shipping_summary = null;
  Integer backers_count = null;
  Integer minimum = null;
  String description = null;

  public Integer backers_count() {
    return backers_count;
  }
  public DateTime estimated_delivery_on() {
    return estimated_delivery_on;
  }
  public Integer id() {
    return id;
  }
  public String shipping_preference() {
    return shipping_preference;
  }
  public Integer limit() {
    return limit;
  }
  public Integer minimum() {
    return minimum;
  }
  public String description() {
    return description;
  }
  public Boolean shipping_enabled() {
    return shipping_enabled;
  }
  public String shipping_summary() {
    return shipping_summary;
  }

  @Override
  public int describeContents() { return 0; }
  @Override
  public void writeToParcel(Parcel dest, int flags) {RewardParcelablePlease.writeToParcel(this, dest, flags);}
  public static final Creator<Reward> CREATOR = new Creator<Reward>() {
    public Reward createFromParcel(Parcel source) {
      Reward target = new Reward();
      RewardParcelablePlease.readFromParcel(target, source);
      return target;
    }
    public Reward[] newArray(int size) {return new Reward[size];}
  };
}
