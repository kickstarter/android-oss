package com.kickstarter.models;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class User implements Parcelable {
  public abstract Avatar avatar();
  public abstract @Nullable Integer backedProjectsCount();
  public abstract boolean happeningNewsletter();
  public abstract long id();
  public abstract @Nullable Integer launchedProjectsCount();
  public abstract String name();
  public abstract boolean notifyMobileOfBackings();
  public abstract boolean notifyMobileOfComments();
  public abstract boolean notifyMobileOfFollower();
  public abstract boolean notifyMobileOfFriendActivity();
  public abstract boolean notifyMobileOfUpdates();
  public abstract boolean notifyOfBackings();
  public abstract boolean notifyOfComments();
  public abstract boolean notifyOfFollower();
  public abstract boolean notifyOfFriendActivity();
  public abstract boolean notifyOfUpdates();
  public abstract boolean promoNewsletter();
  public abstract @Nullable Integer starredProjectsCount();
  public abstract boolean weeklyNewsletter();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder avatar(Avatar __);
    public abstract Builder backedProjectsCount(Integer __);
    public abstract Builder happeningNewsletter(boolean __);
    public abstract Builder id(long __);
    public abstract Builder launchedProjectsCount(Integer __);
    public abstract Builder name(String __);
    public abstract Builder notifyMobileOfBackings(boolean __);
    public abstract Builder notifyMobileOfComments(boolean __);
    public abstract Builder notifyMobileOfFollower(boolean __);
    public abstract Builder notifyMobileOfFriendActivity(boolean __);
    public abstract Builder notifyMobileOfUpdates(boolean __);
    public abstract Builder notifyOfBackings(boolean __);
    public abstract Builder notifyOfComments(boolean __);
    public abstract Builder notifyOfFollower(boolean __);
    public abstract Builder notifyOfFriendActivity(boolean __);
    public abstract Builder notifyOfUpdates(boolean __);
    public abstract Builder promoNewsletter(boolean __);
    public abstract Builder starredProjectsCount(Integer __);
    public abstract Builder weeklyNewsletter(boolean __);
    public abstract User build();
  }

  public static Builder builder() {
    return new AutoParcel_User.Builder();
  }

  public String param() {
    return String.valueOf(this.id());
  }

  public abstract Builder toBuilder();
}
