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
  DateTime estimatedDeliveryOn = null;
  Boolean shippingEnabled = null;
  Integer id = null;
  String shippingPreference = null;
  String shippingSummary = null;
  Integer backersCount = null;
  Integer minimum = null;
  String description = null;

  public Integer backersCount() {
    return backersCount;
  }
  public DateTime estimatedDeliveryOn() {
    return estimatedDeliveryOn;
  }
  public Integer id() {
    return id;
  }
  public String shippingPreference() {
    return shippingPreference;
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
  public Boolean shippingEnabled() {
    return shippingEnabled;
  }
  public String shippingSummary() {
    return shippingSummary;
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
