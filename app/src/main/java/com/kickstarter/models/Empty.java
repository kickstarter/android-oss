package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

/**
 * A class with no values.
 */
@AutoGson
@AutoParcel
public abstract class Empty implements Parcelable {
  public static Empty create() {
    return new AutoParcel_Empty();
  }
}
