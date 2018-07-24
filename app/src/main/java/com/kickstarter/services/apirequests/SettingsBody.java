package com.kickstarter.services.apirequests;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class SettingsBody {
  public abstract int optedOutOfRecommendations();
  public abstract boolean notifyMobileOfFollower();
  public abstract boolean notifyMobileOfFriendActivity();
  public abstract boolean notifyMobileOfMessages();
  public abstract boolean notifyMobileOfUpdates();
  public abstract boolean notifyOfFollower();
  public abstract boolean notifyOfFriendActivity();
  public abstract boolean notifyOfMessages();
  public abstract boolean notifyOfUpdates();
  public abstract int showPublicProfile();
  public abstract int social();
  public abstract int alumniNewsletter();
  public abstract int artsNewsNewsletter();
  public abstract int filmsNewsletter();
  public abstract int gamesNewsletter();
  public abstract int happeningNewsletter();
  public abstract int inventNewsletter();
  public abstract int promoNewsletter();
  public abstract int readsNewsletter();
  public abstract int subscribeAllNewsletter();
  public abstract int weeklyNewsletter();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder optedOutOfRecommendations(int __);
    public abstract Builder notifyMobileOfFollower(boolean __);
    public abstract Builder notifyMobileOfFriendActivity(boolean __);
    public abstract Builder notifyMobileOfMessages(boolean __);
    public abstract Builder notifyMobileOfUpdates(boolean __);
    public abstract Builder notifyOfFollower(boolean __);
    public abstract Builder notifyOfFriendActivity(boolean __);
    public abstract Builder notifyOfMessages(boolean __);
    public abstract Builder notifyOfUpdates(boolean __);
    public abstract Builder showPublicProfile(int __);
    public abstract Builder social(int __);
    public abstract Builder alumniNewsletter(int __);
    public abstract Builder artsNewsNewsletter(int __);
    public abstract Builder filmsNewsletter(int __);
    public abstract Builder gamesNewsletter(int __);
    public abstract Builder happeningNewsletter(int __);
    public abstract Builder inventNewsletter(int __);
    public abstract Builder promoNewsletter(int __);
    public abstract Builder readsNewsletter(int __);
    public abstract Builder subscribeAllNewsletter(int __);
    public abstract Builder weeklyNewsletter(int __);
    public abstract SettingsBody build();
  }

  public static Builder builder() {
    return new AutoParcel_SettingsBody.Builder();
  }

  public abstract Builder toBuilder();
}
