package com.kickstarter.mock.factories;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiException;
import com.kickstarter.services.apiresponses.ErrorEnvelope;

import java.util.Collections;

import okhttp3.ResponseBody;
import rx.Observable;

public final class ApiExceptionFactory {
  private ApiExceptionFactory() {}

  public static @NonNull ApiException badRequestException() {
    final ErrorEnvelope envelope = ErrorEnvelope.builder()
      .errorMessages(Collections.singletonList("bad request"))
      .httpCode(400)
      .build();
    final ResponseBody body = ResponseBody.create(null, "");
    final retrofit2.Response<Observable<User>> response = retrofit2.Response.error(400, body);

    return new ApiException(envelope, response);
  }

  public static @NonNull ApiException apiError(final @NonNull ErrorEnvelope errorEnvelope) {
    final ResponseBody body = ResponseBody.create(null, new Gson().toJson(errorEnvelope));
    final retrofit2.Response<Observable<User>> response = retrofit2.Response.error(errorEnvelope.httpCode(), body);

    return new ApiException(errorEnvelope, response);
  }

  public static @NonNull ApiException invalidLoginException() {
    final ErrorEnvelope envelope = ErrorEnvelope.builder()
      .errorMessages(Collections.singletonList("Invalid login."))
      .httpCode(401)
      .ksrCode(ErrorEnvelope.INVALID_XAUTH_LOGIN)
      .build();

    final ResponseBody body = ResponseBody.create(null, new Gson().toJson(envelope));
    final retrofit2.Response<Observable<User>> response = retrofit2.Response.error(envelope.httpCode(), body);

    return new ApiException(envelope, response);
  }

  public static @NonNull ApiException tfaRequired() {
    final ErrorEnvelope envelope = ErrorEnvelope.builder()
      .ksrCode(ErrorEnvelope.TFA_REQUIRED)
      .httpCode(403)
      .errorMessages(Collections.singletonList("Two-factor authentication required."))
      .build();
    final ResponseBody body = ResponseBody.create(null, new Gson().toJson(envelope));
    final retrofit2.Response<Observable<User>> response = retrofit2.Response.error(envelope.httpCode(), body);

    return new ApiException(envelope, response);
  }

  public static @NonNull ApiException tfaFailed() {
    final ErrorEnvelope envelope = ErrorEnvelope.builder()
      .ksrCode(ErrorEnvelope.TFA_FAILED)
      .httpCode(400)
      .build();
    final ResponseBody body = ResponseBody.create(null, new Gson().toJson(envelope));
    final retrofit2.Response<Observable<User>> response = retrofit2.Response.error(envelope.httpCode(), body);

    return new ApiException(envelope, response);
  }
}
