package com.kickstarter.models;

import android.content.res.Resources;
import android.os.Parcelable;

import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.AutoGson;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class User implements Parcelable, Relay {
  public abstract @Nullable Boolean alumniNewsletter();
  public abstract @Nullable Boolean artsCultureNewsletter();
  public abstract Avatar avatar();
  public abstract @Nullable Integer backedProjectsCount();
  public abstract @Nullable Integer createdProjectsCount();
  public abstract @Nullable Integer erroredBackingsCount();
  public abstract @Nullable Boolean facebookConnected();
  public abstract @Nullable Boolean filmNewsletter();
  public abstract @Nullable Boolean gamesNewsletter();
  public abstract @Nullable Boolean happeningNewsletter();
  public abstract long id();
  public abstract @Nullable Boolean inventNewsletter();
  public abstract @Nullable Boolean isAdmin();
  public abstract @Nullable Boolean isEmailVerified();
  public abstract @Nullable Location location();
  public abstract @Nullable Integer memberProjectsCount();
  public abstract @Nullable Boolean musicNewsletter();
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
  public abstract @Nullable Boolean notifyOfCommentReplies();
  public abstract @Nullable Boolean notifyOfCreatorDigest();
  public abstract @Nullable Boolean notifyOfCreatorEdu();
  public abstract @Nullable Boolean notifyOfFollower();
  public abstract @Nullable Boolean notifyOfFriendActivity();
  public abstract @Nullable Boolean notifyOfMessages();
  public abstract @Nullable Boolean notifyOfUpdates();
  public abstract @Nullable Boolean optedOutOfRecommendations();
  public abstract @Nullable Boolean promoNewsletter();
  public abstract @Nullable Boolean publishingNewsletter();
  public abstract @Nullable Boolean showPublicProfile();
  public abstract @Nullable Boolean social();
  public abstract @Nullable Integer starredProjectsCount();
  public abstract @Nullable Integer unreadMessagesCount();
  public abstract @Nullable Integer unseenActivityCount();
  public abstract @Nullable Boolean weeklyNewsletter();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder alumniNewsletter(Boolean __);
    public abstract Builder artsCultureNewsletter(Boolean __);
    public abstract Builder avatar(Avatar __);
    public abstract Builder backedProjectsCount(Integer __);
    public abstract Builder createdProjectsCount(Integer __);
    public abstract Builder erroredBackingsCount(Integer __);
    public abstract Builder facebookConnected(Boolean __);
    public abstract Builder filmNewsletter(Boolean __);
    public abstract Builder gamesNewsletter(Boolean __);
    public abstract Builder happeningNewsletter(Boolean __);
    public abstract Builder id(long __);
    public abstract Builder isAdmin(Boolean __);
    public abstract Builder isEmailVerified(Boolean __);
    public abstract Builder inventNewsletter(Boolean __);
    public abstract Builder location(Location __);
    public abstract Builder memberProjectsCount(Integer __);
    public abstract Builder musicNewsletter(Boolean __);
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
    public abstract Builder notifyOfCommentReplies(Boolean __);
    public abstract Builder notifyOfCreatorDigest(Boolean __);
    public abstract Builder notifyOfCreatorEdu(Boolean __);
    public abstract Builder notifyOfFollower(Boolean __);
    public abstract Builder notifyOfFriendActivity(Boolean __);
    public abstract Builder notifyOfMessages(Boolean __);
    public abstract Builder notifyOfUpdates(Boolean __);
    public abstract Builder optedOutOfRecommendations(Boolean __);
    public abstract Builder promoNewsletter(Boolean __);
    public abstract Builder publishingNewsletter(Boolean __);
    public abstract Builder showPublicProfile(Boolean __);
    public abstract Builder social(Boolean __);
    public abstract Builder starredProjectsCount(Integer __);
    public abstract Builder unreadMessagesCount(Integer __);
    public abstract Builder unseenActivityCount(Integer __);
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
    TWICE_A_DAY_SUMMARY(R.string.Twice_a_day_summary),
    DAILY_SUMMARY(R.string.Daily_summary);

    private int stringResId;

    EmailFrequency(final int stringResId) {
      this.stringResId = stringResId;
    }

    @NotNull
    public static String[] getStrings(final @NonNull Resources resources) {
      final String[] strings = new String[values().length];
      final EmailFrequency[] values = values();

      for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
        final EmailFrequency emailFrequency = values[i];
        strings[i] = resources.getString(emailFrequency.stringResId);
      }

      return strings;
    }
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    boolean equals = super.equals(obj);

    if (obj instanceof User) {
      final User otherUser = (User) obj;
      equals = Objects.equals(this.id(), otherUser.id()) &&
              Objects.equals(this.alumniNewsletter(), otherUser.alumniNewsletter()) &&
              Objects.equals(this.artsCultureNewsletter(), otherUser.artsCultureNewsletter()) &&
              Objects.equals(this.backedProjectsCount(), otherUser.backedProjectsCount()) &&
              Objects.equals(this.createdProjectsCount(), otherUser.createdProjectsCount()) &&
              Objects.equals(this.name(), otherUser.name()) &&
              Objects.equals(this.avatar(), otherUser.avatar()) &&
              Objects.equals(this.createdProjectsCount(), otherUser.createdProjectsCount()) &&
              Objects.equals(this.facebookConnected(), otherUser.facebookConnected());
              Objects.equals(this.filmNewsletter(), otherUser.filmNewsletter());
              Objects.equals(this.facebookConnected(), otherUser.facebookConnected());
              Objects.equals(this.gamesNewsletter(), otherUser.gamesNewsletter());
              Objects.equals(this.happeningNewsletter(), otherUser.happeningNewsletter());
              Objects.equals(this.inventNewsletter(), otherUser.inventNewsletter());
              Objects.equals(this.isAdmin(), otherUser.isAdmin());
              Objects.equals(this.location(), otherUser.location());
              Objects.equals(this.memberProjectsCount(), otherUser.memberProjectsCount());
              Objects.equals(this.musicNewsletter(), otherUser.musicNewsletter());
              Objects.equals(this.notifyMobileOfBackings(), otherUser.notifyMobileOfBackings());
              Objects.equals(this.notifyMobileOfComments(), otherUser.notifyMobileOfComments());
              Objects.equals(this.notifyMobileOfCreatorEdu(), otherUser.notifyMobileOfCreatorEdu());
              Objects.equals(this.notifyMobileOfFollower(), otherUser.notifyMobileOfFollower());
              Objects.equals(this.notifyMobileOfFriendActivity(), otherUser.notifyMobileOfFriendActivity());
              Objects.equals(this.notifyMobileOfMessages(), otherUser.notifyMobileOfMessages());
              Objects.equals(this.notifyMobileOfPostLikes(), otherUser.notifyMobileOfPostLikes());
              Objects.equals(this.notifyMobileOfUpdates(), otherUser.notifyMobileOfUpdates());
              Objects.equals(this.notifyOfBackings(), otherUser.notifyOfBackings());
              Objects.equals(this.notifyOfComments(), otherUser.notifyOfComments());
              Objects.equals(this.notifyOfCommentReplies(), otherUser.notifyOfCommentReplies());
              Objects.equals(this.notifyOfCreatorDigest(), otherUser.notifyOfCreatorDigest());
              Objects.equals(this.notifyOfCreatorEdu(), otherUser.notifyOfCreatorEdu());
              Objects.equals(this.notifyOfFollower(), otherUser.notifyOfFollower());
              Objects.equals(this.notifyOfFriendActivity(), otherUser.notifyOfFriendActivity());
              Objects.equals(this.notifyOfMessages(), otherUser.notifyOfMessages());
              Objects.equals(this.optedOutOfRecommendations(), otherUser.optedOutOfRecommendations());
              Objects.equals(this.promoNewsletter(), otherUser.promoNewsletter());
              Objects.equals(this.publishingNewsletter(), otherUser.publishingNewsletter());
              Objects.equals(this.showPublicProfile(), otherUser.showPublicProfile());
              Objects.equals(this.social(), otherUser.social());
              Objects.equals(this.starredProjectsCount(), otherUser.starredProjectsCount());
              Objects.equals(this.unreadMessagesCount(), otherUser.unreadMessagesCount());
              Objects.equals(this.unseenActivityCount(), otherUser.unseenActivityCount());
              Objects.equals(this.weeklyNewsletter(), otherUser.weeklyNewsletter());
    }

    return equals;
  }
}
