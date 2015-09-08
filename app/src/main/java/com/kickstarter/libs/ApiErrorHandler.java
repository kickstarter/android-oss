package com.kickstarter.libs;

import android.content.Context;
import android.widget.Toast;

import com.kickstarter.R;
import com.kickstarter.services.ApiError;

import retrofit.RetrofitError;

public abstract class ApiErrorHandler {
  final Throwable e;
  final Context context;

  public ApiErrorHandler(final Throwable e, final Context context) {
    this.e = e;
    this.context = context;
  }

  public void handleError() {
    if (e instanceof ApiError) {
      handleApiError((ApiError) e);
    } else if (e instanceof RetrofitError) {
      RetrofitError retrofitError = (RetrofitError) e;
      if (retrofitError.getKind() == RetrofitError.Kind.NETWORK) {
        displayError(R.string.Unable_to_connect);
      } else {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException(e);
    }
  }

  public abstract void handleApiError(final ApiError apiError);

  public void displayError(final int id) {
    // Toast by default, but this could be overridden
    final Toast toast = Toast.makeText(context, context.getResources().getString(id), Toast.LENGTH_LONG);
    toast.show();
  }
}
