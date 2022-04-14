package com.kickstarter.libs.rx.operators;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kickstarter.services.ApiException;
import com.kickstarter.services.ResponseException;
import com.kickstarter.services.apiresponses.ErrorEnvelope;


import androidx.annotation.NonNull;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;

/**
 * Takes a {@link retrofit2.Response}, if it's successful send it to {@link Subscriber#onNext}, otherwise
 * attempt to parse the error.
 *
 * Errors that conform to the API's error format are converted into an {@link ApiException} exception and sent to
 * {@link Subscriber#onError}, otherwise a more generic {@link ResponseException} is sent to {@link Subscriber#onError}.
 *
 * @param <T> The response type.
 */
public final class ApiErrorOperator<T> implements Observable.Operator<T, retrofit2.Response<T>> {
  private final Gson gson;

  public ApiErrorOperator(final @NonNull Gson gson) {
    this.gson = gson;
  }

  @Override
  public Subscriber<? super Response<T>> call(final @NonNull Subscriber<? super T> subscriber) {
    final Gson gson = this.gson;

    return new Subscriber<retrofit2.Response<T>>() {
      @Override
      public void onCompleted() {
        if (!subscriber.isUnsubscribed()) {
          subscriber.onCompleted();
        }
      }

      @Override
      public void onError(final @NonNull Throwable e) {
        if (!subscriber.isUnsubscribed()) {
          subscriber.onError(e);
        }
      }

      @Override
      public void onNext(final @NonNull retrofit2.Response<T> response) {
        if (subscriber.isUnsubscribed()) {
          return;
        }

        if (response == null) {
          subscriber.onError(new ResponseException(response));
          return;
        }

        if (!response.isSuccessful()) {
            ErrorEnvelope envelope;
            try {
              envelope = gson.fromJson(response.errorBody().string(), ErrorEnvelope.class);
            } catch (final @NonNull Exception e) {
              envelope = null;
            }

            if (envelope != null) {
              subscriber.onError(new ApiException(envelope, response));
            } else {
              subscriber.onError(new ResponseException(response));
            }

        } else { 
          subscriber.onNext(response.body());
          subscriber.onCompleted();
        }
      }
    };
  }
}
