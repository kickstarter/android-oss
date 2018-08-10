package com.kickstarter.models;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class User implements Parcelable {
  public abstract @Nullable Boolean alumniNewsletter();
  public abstract @Nullable Boolean artsCultureNewsletter();
  public abstract Avatar avatar();
  public abstract @Nullable Integer backedProjectsCount();
  public abstract @Nullable Integer createdProjectsCount();
  public abstract @Nullable Boolean filmNewsletter();
  public abstract @Nullable Boolean gamesNewsletter();
  public abstract @Nullable Boolean happeningNewsletter();
  public abstract long id();
  public abstract @Nullable Boolean inventNewsletter();
  public abstract @Nullable Location location();
  public abstract @Nullable Integer memberProjectsCount();
  public abstract String name();
  public abstract @Nullable Boolean notifyMobileOfBackings();
  public abstract @Nullable Boolean notifyMobileOfComments();
  public abstract @Nullable Boolean notifyMobileOfCreatorEdu();
  public abstract @Nullable Boolean notifyMobileOfFollower();
  public abstract @Nullable Boolean notifyMobileOfFriendActivity();
  public abstract @Nullable Boolean notifyMobileOfMessages();
  public abstract @Nullable Boolean notifyMobileOfPostLikes();
  public abstract @Nullable Boolean notifyMobileOfUpdates();
  public abstract @Nullable Boolean notifyOfBackings();
  public abstract @Nullable Boolean notifyOfComments();
  public abstract @Nullable Boolean notifyOfCreatorDigest();
  public abstract @Nullable Boolean notifyOfCreatorEdu();
  public abstract @Nullable Boolean notifyOfFollower();
  public abstract @Nullable Boolean notifyOfFriendActivity();
  public abstract @Nullable Boolean notifyOfMessages();
  public abstract @Nullable Boolean notifyOfPostLikes();
  public abstract @Nullable Boolean notifyOfUpdates();
  public abstract @Nullable Boolean optedOutOfRecommendations();
  public abstract @Nullable Boolean promoNewsletter();
  public abstract @Nullable Boolean publishingNewsletter();
  public abstract @Nullable Boolean showPublicProfile();
  public abstract @Nullable Boolean social();
  public abstract @Nullable Integer starredProjectsCount();
  public abstract @Nullable Integer unreadMessagesCount();
  public abstract @Nullable Boolean weeklyNewsletter();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder alumniNewsletter(Boolean __);
    public abstract Builder artsCultureNewsletter(Boolean __);
    public abstract Builder avatar(Avatar __);
    public abstract Builder backedProjectsCount(Integer __);
    public abstract Builder createdProjectsCount(Integer __);
    public abstract Builder filmNewsletter(Boolean __);
    public abstract Builder gamesNewsletter(Boolean __);
    public abstract Builder happeningNewsletter(Boolean __);
    public abstract Builder id(long __);
    public abstract Builder inventNewsletter(Boolean __);
    public abstract Builder location(Location __);
    public abstract Builder memberProjectsCount(Integer __);
    public abstract Builder name(String __);
    public abstract Builder notifyMobileOfBackings(Boolean __);
    public abstract Builder notifyMobileOfComments(Boolean __);
    public abstract Builder notifyMobileOfCreatorEdu(Boolean __);
    public abstract Builder notifyMobileOfFollower(Boolean __);
    public abstract Builder notifyMobileOfFriendActivity(Boolean __);
    public abstract Builder notifyMobileOfMessages(Boolean __);
    public abstract Builder notifyMobileOfPostLikes(Boolean __);
    public abstract Builder notifyMobileOfUpdates(Boolean __);
    public abstract Builder notifyOfBackings(Boolean __);
    public abstract Builder notifyOfComments(Boolean __);
    public abstract Builder notifyOfCreatorDigest(Boolean __);
    public abstract Builder notifyOfCreatorEdu(Boolean __);
    public abstract Builder notifyOfFollower(Boolean __);
    public abstract Builder notifyOfFriendActivity(Boolean __);
    public abstract Builder notifyOfMessages(Boolean __);
    public abstract Builder notifyOfPostLikes(Boolean __);
    public abstract Builder notifyOfUpdates(Boolean __);
    public abstract Builder optedOutOfRecommendations(Boolean __);
    public abstract Builder promoNewsletter(Boolean __);
    public abstract Builder publishingNewsletter(Boolean __);
    public abstract Builder showPublicProfile(Boolean __);
    public abstract Builder social(Boolean __);
    public abstract Builder starredProjectsCount(Integer __);
    public abstract Builder unreadMessagesCount(Integer __);
    public abstract Builder weeklyNewsletter(Boolean __);
    public abstract User build();
  }

  public static Builder builder() {
    return new AutoParcel_User.Builder();
  }

  public @NonNull String param() {
    return String.valueOf(this.id());
  }

  public abstract Builder toBuilder();

  public enum EmailFrequency {
    DAILY(R.string.Daily_digest),
    INDIVIDUAL(R.string.Individual_Emails);

    private int stringResId;

    EmailFrequency(int stringResId) {
      this.stringResId = stringResId;
    }

    public int getStringResId() {
      return stringResId;
    }
  }

}
