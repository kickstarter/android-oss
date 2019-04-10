package com.kickstarter.services.apirequests;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class SettingsBody {
  public abstract int optedOutOfRecommendations();
  public abstract boolean notifyMobileOfBackings();
  public abstract boolean notifyMobileOfComments();
  public abstract boolean notifyMobileOfCreatorEdu();
  public abstract boolean notifyMobileOfFollower();
  public abstract boolean notifyMobileOfFriendActivity();
  public abstract boolean notifyMobileOfMessages();
  public abstract boolean notifyMobileOfPostLikes();
  public abstract boolean notifyMobileOfUpdates();
  public abstract boolean notifyOfBackings();
  public abstract boolean notifyOfComments();
  public abstract boolean notifyOfCommentReplies();
  public abstract boolean notifyOfCreatorDigest();
  public abstract boolean notifyOfCreatorEdu();
  public abstract boolean notifyOfFollower();
  public abstract boolean notifyOfFriendActivity();
  public abstract boolean notifyOfMessages();
  public abstract boolean notifyOfUpdates();
  public abstract int showPublicProfile();
  public abstract int social();
  public abstract int alumniNewsletter();
  public abstract int artsCultureNewsletter();
  public abstract int filmNewsletter();
  public abstract int gamesNewsletter();
  public abstract int happeningNewsletter();
  public abstract int inventNewsletter();
  public abstract int musicNewsletter();
  public abstract int promoNewsletter();
  public abstract int publishingNewsletter();
  public abstract int weeklyNewsletter();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder optedOutOfRecommendations(int __);
    public abstract Builder notifyMobileOfBackings(boolean __);
    public abstract Builder notifyMobileOfComments(boolean __);
    public abstract Builder notifyMobileOfCreatorEdu(boolean __);
    public abstract Builder notifyMobileOfFollower(boolean __);
    public abstract Builder notifyMobileOfFriendActivity(boolean __);
    public abstract Builder notifyMobileOfMessages(boolean __);
    public abstract Builder notifyMobileOfPostLikes(boolean __);
    public abstract Builder notifyMobileOfUpdates(boolean __);
    public abstract Builder notifyOfBackings(boolean __);
    public abstract Builder notifyOfComments(boolean __);
    public abstract Builder notifyOfCommentReplies(boolean __);
    public abstract Builder notifyOfCreatorDigest(boolean __);
    public abstract Builder notifyOfCreatorEdu(boolean __);
    public abstract Builder notifyOfFollower(boolean __);
    public abstract Builder notifyOfFriendActivity(boolean __);
    public abstract Builder notifyOfMessages(boolean __);
    public abstract Builder notifyOfUpdates(boolean __);
    public abstract Builder showPublicProfile(int __);
    public abstract Builder social(int __);
    public abstract Builder alumniNewsletter(int __);
    public abstract Builder artsCultureNewsletter(int __);
    public abstract Builder filmNewsletter(int __);
    public abstract Builder gamesNewsletter(int __);
    public abstract Builder happeningNewsletter(int __);
    public abstract Builder inventNewsletter(int __);
    public abstract Builder musicNewsletter(int __);
    public abstract Builder promoNewsletter(int __);
    public abstract Builder publishingNewsletter(int __);
    public abstract Builder weeklyNewsletter(int __);
    public abstract SettingsBody build();
  }

  public static Builder builder() {
    return new AutoParcel_SettingsBody.Builder();
  }

  public abstract Builder toBuilder();
}
