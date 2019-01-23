package com.kickstarter.libs.models;

import android.os.Parcelable;

import com.google.gson.Gson;
import com.kickstarter.libs.qualifiers.AutoGson;

import androidx.annotation.NonNull;
import auto.parcel.AutoParcel;

/**
 * A value that represents a fully authorized Android Pay transaction. It is sent back to our server to be processed
 * and saved as a stored card.
 */
@AutoGson
@AutoParcel
public abstract class AndroidPayAuthorizedPayload implements Parcelable {
  public abstract @NonNull AndroidPayWallet androidPayWallet();
  public abstract @NonNull StripeToken stripeToken();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder androidPayWallet(AndroidPayWallet __);
    public abstract Builder stripeToken(StripeToken __);
    public abstract AndroidPayAuthorizedPayload build();
  }
  public abstract Builder toBuilder();
  public static Builder builder() {
    return new AutoParcel_AndroidPayAuthorizedPayload.Builder();
  }

  /**
   * A value that represents the parts of the authorized transaction that correspond specifically to an
   * Android Pay wallet.
   */
  @AutoParcel
  @AutoGson
  public abstract static class AndroidPayWallet implements Parcelable {
    public abstract @NonNull String googleTransactionId();
    /**
     * Last 4 of credit card.
     */
    public abstract @NonNull String instrumentDetails();
    /**
     * Network name of credit card.
     */
    public abstract @NonNull String instrumentType();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder googleTransactionId(String __);
      public abstract Builder instrumentDetails(String __);
      public abstract Builder instrumentType(String __);
      public abstract AndroidPayWallet build();
    }
    public abstract Builder toBuilder();
    public static Builder builder() {
      return new AutoParcel_AndroidPayAuthorizedPayload_AndroidPayWallet.Builder();
    }
  }

  /**
   * A value that represents the parts of the authorized transaction that correspond specifically to an
   * Stripe token.
   */
  @AutoParcel
  @AutoGson
  public abstract static class StripeToken implements Parcelable {
    public abstract String id();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder id(String __);
      public abstract StripeToken build();
    }
    public abstract Builder toBuilder();
    public static Builder builder() {
      return new AutoParcel_AndroidPayAuthorizedPayload_StripeToken.Builder();
    }
  }

  public static @NonNull StripeToken create(final @NonNull String str, final @NonNull Gson gson) {
    return gson.fromJson(str, StripeToken.class);
  }
}
