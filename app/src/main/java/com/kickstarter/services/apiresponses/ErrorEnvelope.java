package com.kickstarter.services.apiresponses;

import android.os.Parcelable;
import android.support.annotation.StringDef;

import com.kickstarter.libs.qualifiers.AutoGson;

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
}
