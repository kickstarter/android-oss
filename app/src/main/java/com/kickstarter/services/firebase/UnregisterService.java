package com.kickstarter.services.firebase;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class UnregisterService extends JobService {
  public static final String UNREGISTER_SERVICE = "Unregister-service";

  @Override
  public boolean onStartJob(final JobParameters job) {
    Timber.d("onStartJob");
    Observable.fromCallable(() -> {
      FirebaseInstanceId.getInstance().deleteToken(FirebaseInstanceId.getInstance().getId(), FirebaseMessaging.INSTANCE_ID_SCOPE);
      return true;
    })
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> Timber.d("Successfully deleted token"), e -> Timber.e("Failed to delete token: %s", e));

    return false;
  }

  @Override
  public boolean onStopJob(final JobParameters job) {
    return false;
  }

}
