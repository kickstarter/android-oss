package com.kickstarter.factories;

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
    final ErrorEnvelope envelope = ErrorEnvelope.builder()
      .errorMessages(errorEnvelope.errorMessages())
      .httpCode(errorEnvelope.httpCode())
      .build();
    final ResponseBody body = ResponseBody.create(null, new Gson().toJson(errorEnvelope));
    final retrofit2.Response<Observable<User>> response = retrofit2.Response.error(errorEnvelope.httpCode(), body);

    return new ApiException(envelope, response);
  }
}
