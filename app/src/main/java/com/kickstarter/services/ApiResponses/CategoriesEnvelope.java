package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.AutoGson;
import com.kickstarter.models.Category;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class CategoriesEnvelope implements Parcelable {
  public abstract List<Category> categories();
}
