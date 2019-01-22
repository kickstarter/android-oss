package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.services.ApiException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class ErrorEnvelope implements Parcelable {
  public abstract @Nullable List<String> errorMessages();
  public abstract int httpCode();
  public abstract @Nullable String ksrCode();
  public abstract @Nullable FacebookUser facebookUser();

  @AutoGson
  @AutoParcel
  public static abstract class FacebookUser implements Parcelable {
    public abstract long id();
    public abstract String name();
    public abstract String email();

    @AutoParcel.Builder
    public static abstract class Builder {
      public abstract Builder id(long __);
      public abstract Builder name(String __);
      public abstract Builder email(String __);
      public abstract FacebookUser build();
    }

    public static Builder builder() {
      return new AutoParcel_ErrorEnvelope_FacebookUser.Builder();
    }

    public abstract Builder toBuilder();
  }

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder errorMessages(List<String> __);
    public abstract Builder httpCode(int __);
    public abstract Builder ksrCode(String __);
    public abstract Builder facebookUser(FacebookUser __);
    public abstract ErrorEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_ErrorEnvelope.Builder();
  }

  public abstract Builder toBuilder();

  public static final String CONFIRM_FACEBOOK_SIGNUP = "confirm_facebook_signup";
  public static final String INVALID_XAUTH_LOGIN = "invalid_xauth_login";
  public static final String TFA_FAILED = "tfa_failed";
  public static final String TFA_REQUIRED = "tfa_required";
  public static final String MISSING_FACEBOOK_EMAIL = "missing_facebook_email";
  public static final String FACEBOOK_INVALID_ACCESS_TOKEN = "facebook_invalid_access_token";
  public static final String UNAUTHORIZED = "unauthorized";

  @StringDef({INVALID_XAUTH_LOGIN, TFA_FAILED, TFA_REQUIRED})
  @Retention(RetentionPolicy.SOURCE)
  public @interface ErrorCode {}

  /**
   * Tries to extract an {@link ErrorEnvelope} from an exception, and if it
   * can't returns null.
   */
  public static @Nullable ErrorEnvelope fromThrowable(final @NonNull Throwable t) {
    if (t instanceof ApiException) {
      final ApiException exception = (ApiException) t;
      return exception.errorEnvelope();
    }

    return null;
  }

  public boolean isConfirmFacebookSignupError() {
    return CONFIRM_FACEBOOK_SIGNUP.equals(ksrCode());
  }

  public boolean isInvalidLoginError() {
    return INVALID_XAUTH_LOGIN.equals(ksrCode());
  }

  public boolean isTfaRequiredError() {
    return TFA_REQUIRED.equals(ksrCode());
  }

  public boolean isTfaFailedError() {
    return TFA_FAILED.equals(ksrCode());
  }

  public boolean isMissingFacebookEmailError() {
    return MISSING_FACEBOOK_EMAIL.equals(ksrCode());
  }

  public boolean isFacebookInvalidAccessTokenError() {
    return FACEBOOK_INVALID_ACCESS_TOKEN.equals(ksrCode());
  }

  public boolean isUnauthorizedError() {
    return UNAUTHORIZED.equals(ksrCode());
  }

  /*
    When logging in the only two possible errors are INVALID_XAUTH_LOGIN
    and TFA_REQUIRED, so we consider anything else an unknown error.
   */
  public boolean isGenericLoginError() {
    return
      !INVALID_XAUTH_LOGIN.equals(ksrCode()) &&
        !TFA_REQUIRED.equals(ksrCode());
  }

  /**
   * Returns the first error message available, or `null` if there are none.
   */
  public @Nullable String errorMessage() {
    if (errorMessages() == null) {
      return null;
    } else {
      return ListUtils.first(errorMessages());
    }
  }
}
