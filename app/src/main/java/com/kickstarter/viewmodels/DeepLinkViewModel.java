package com.kickstarter.viewmodels;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.KSUri;
import com.kickstarter.ui.activities.DeepLinkActivity;

import java.net.HttpURLConnection;
import java.net.URL;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public interface DeepLinkViewModel {

  interface Outputs {
    /** Emits when we should start the {@link com.kickstarter.ui.activities.DiscoveryActivity}. */
    Observable<Void> startDiscoveryActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}. */
    Observable<Uri> startProjectActivity();
  }

  final class ViewModel extends ActivityViewModel<DeepLinkActivity> implements Outputs {
    public ViewModel(@NonNull Environment environment) {
      super(environment);

      final Observable<Uri> kickstarterComUri = intent()
        .map(Intent::getData)
        .filter(ObjectUtils::isNotNull)
        .ofType(Uri.class);

      final Observable<Uri> emailUri = intent()
        .observeOn(Schedulers.io())
        .map(Intent::getData)
        .filter(ObjectUtils::isNotNull)
        .filter(uri -> uri.getHost().equals("emails.kickstarter.com"))
        .map(uri -> {
          try {
            URL originalUrl = new URL(uri.toString());
            HttpURLConnection ucon = (HttpURLConnection) originalUrl.openConnection();
            ucon.connect();
            ucon.getInputStream();
            URL redirectUrl = ucon.getURL();
            return Uri.parse(redirectUrl.toString());
          } catch (Exception e) {
            Log.e(DeepLinkViewModel.class.toString(), e.getLocalizedMessage());
            throw new RuntimeException(e);
          }
        })
        .compose(Transformers.neverError());

      Observable<Uri> uriFromIntent = Observable.merge(kickstarterComUri, emailUri).filter(ObjectUtils::isNotNull);

      uriFromIntent
        .filter(uri -> uri.getLastPathSegment().equals("projects"))
        .compose(Transformers.ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(this.startDiscoveryActivity::onNext);

      uriFromIntent
        .filter(uri -> KSUri.isProjectUri(uri, uri.toString()))
        .compose(bindToLifecycle())
        .subscribe(this.startProjectActivity::onNext);

    }
    private final BehaviorSubject<Void> startDiscoveryActivity = BehaviorSubject.create();
    private final BehaviorSubject<Uri> startProjectActivity = BehaviorSubject.create();

    public final Outputs outputs = this;

    @Override
    public Observable<Void> startDiscoveryActivity() {
      return startDiscoveryActivity;
    }
    @Override
    public Observable<Uri> startProjectActivity() {
      return startProjectActivity;
    }
  }
}
