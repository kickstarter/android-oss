package com.kickstarter.services.apiresponses;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.services.ApiError;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class ErrorEnvelope implements Parcelable {
  public abstract List<String> errorMessages();
  public abstract int httpCode();
  public abstract String ksrCode();

  public static final String INVALID_XAUTH_LOGIN = "invalid_xauth_login";
  public static final String TFA_FAILED = "tfa_failed";
  public static final String TFA_REQUIRED = "tfa_required";

  @StringDef({INVALID_XAUTH_LOGIN, TFA_FAILED, TFA_REQUIRED})
  @Retention(RetentionPolicy.SOURCE)
  public @interface ErrorCode {}

  /*
    Tries to extract an api error envelope from an exception, and if it
     can't returns `null`.
   */
  @Nullable public static ErrorEnvelope fromThrowable(@NonNull final Throwable e) {
    if (e instanceof ApiError) {
      final ApiError error = (ApiError) e;
      final ErrorEnvelope envelope = error.errorEnvelope();
      return envelope;
    }

    return null;
  }

  public boolean isInvalidLoginError() {
    return ksrCode().equals(INVALID_XAUTH_LOGIN);
  }

  public boolean isTfaRequiredError() {
    return ksrCode().equals(TFA_REQUIRED);
  }

  public boolean isTfaFailedError() {
    return ksrCode().equals(TFA_FAILED);
  }

  /*
    When logging in the only two possible errors are INVALID_XAUTH_LOGIN
    and TFA_REQUIRED, so we consider anything else an unknown error.
   */
  public boolean isGenericLoginError() {
    return
      !ksrCode().equals(INVALID_XAUTH_LOGIN) &&
        !ksrCode().equals(TFA_REQUIRED);
  }
}
