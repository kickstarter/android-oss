package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.joda.time.DateTime;

@ParcelablePlease
public class Comment implements Parcelable {
  public String body = null;
  public DateTime createdAt = null;
  public User author = null;
  public Integer id = null;

  public String body() {
    return body;
  }
  public DateTime createdAt() {
    return createdAt;
  }
  public User author() {
    return author;
  }
  public Integer id() {
    return id;
  }
  
  @Override
  public int describeContents() { return 0; }
  @Override
  public void writeToParcel(Parcel dest, int flags) {CommentParcelablePlease.writeToParcel(this, dest, flags);}
  public static final Creator<Comment> CREATOR = new Creator<Comment>() {
    public Comment createFromParcel(Parcel source) {
      Comment target = new Comment();
      CommentParcelablePlease.readFromParcel(target, source);
      return target;
    }
    public Comment[] newArray(int size) {return new Comment[size];}
  };
}
