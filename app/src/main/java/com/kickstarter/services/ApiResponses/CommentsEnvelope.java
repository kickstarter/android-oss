package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.Comment;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class CommentsEnvelope implements Parcelable {
  public abstract List<Comment> comments();
  public abstract UrlsEnvelope urls();

  @AutoGson
  @AutoParcel
  public abstract static class UrlsEnvelope implements Parcelable {
    public abstract ApiEnvelope api();

    @AutoGson
    @AutoParcel
    public abstract static class ApiEnvelope implements Parcelable {
      public abstract String moreComments();
      public abstract String newerComments();
    }
  }
}
