package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
public class Comment implements Parcelable {
  public String body = null;
  public String created_at = null;
  public Author author = null;

  public String body() {
    return body;
  }
  public String createdAt() {
    return created_at;
  }
  public Author author() {
    return author;
  }

  @ParcelablePlease
  public static class Author implements Parcelable {
    public String name = null;
    public Avatar avatar = null;

    public String name() {
      return name;
    }
    public Avatar avatar () {
      return avatar;
    }

    @Override
    public int describeContents() { return 0; }
    @Override
    public void writeToParcel(Parcel dest, int flags) {com.kickstarter.models.AuthorParcelablePlease.writeToParcel(this, dest, flags);}
    public static final Creator<Author> CREATOR = new Creator<Author>() {
      public Author createFromParcel(Parcel source) {
        Author target = new Author();
        com.kickstarter.models.AuthorParcelablePlease.readFromParcel(target, source);
        return target;
      }
      public Author[] newArray(int size) {return new Author[size];}
    };
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
