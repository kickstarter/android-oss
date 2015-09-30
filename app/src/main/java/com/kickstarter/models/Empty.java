package com.kickstarter.models;

import auto.parcel.AutoParcel;

/**
 * A class with no values.
 */
@AutoParcel
public abstract class Empty {
  public static Empty create() {
    return new AutoParcel_Empty();
  }
}
