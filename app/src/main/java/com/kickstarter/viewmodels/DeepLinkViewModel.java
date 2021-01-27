package com.kickstarter.viewmodels;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.libs.utils.UrlUtils;
import com.kickstarter.services.KSUri;
import com.kickstarter.ui.activities.DeepLinkActivity;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;

public interface DeepLinkViewModel {

  interface Outputs {
    /** Emits when we should start an external browser because we don't want to deep link. */
    Observable<String> startBrowser();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.DiscoveryActivity}. */
    Observable<Void> startDiscoveryActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}. */
    Observable<Uri> startProjectActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity} with pledge sheet expanded. */
    Observable<Uri> startProjectActivityForCheckout();
  }

  final class ViewModel extends ActivityViewModel<DeepLinkActivity> implements Outputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<Uri> uriFromIntent = intent()
        .map(Intent::getData)
        .ofType(Uri.class);

      uriFromIntent
        .filter(this::lastPathSegmentIsProjects)
        .compose(ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(this.startDiscoveryActivity::onNext);

      uriFromIntent
        .filter(uri -> KSUri.isProjectUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .filter(uri -> !KSUri.isCheckoutUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .filter(uri -> !KSUri.isProjectPreviewUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .map(this::appendRefTagIfNone)
        .compose(bindToLifecycle())
        .subscribe(this.startProjectActivity::onNext);

      uriFromIntent
        .filter(uri -> KSUri.isCheckoutUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .map(this::appendRefTagIfNone)
        .compose(bindToLifecycle())
        .subscribe(this.startProjectActivityWithCheckout::onNext);

      final Observable<Uri> projectPreview = uriFromIntent
        .filter(uri -> KSUri.isProjectPreviewUri(uri, Secrets.WebEndpoint.PRODUCTION));

      final Observable<Uri> unsupportedDeepLink = uriFromIntent
        .filter(uri -> !lastPathSegmentIsProjects(uri))
        .filter(uri -> !KSUri.isCheckoutUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .filter(uri -> !KSUri.isProjectUri(uri, Secrets.WebEndpoint.PRODUCTION));

      Observable.merge(projectPreview, unsupportedDeepLink)
        .map(Uri::toString)
        .filter(url -> !TextUtils.isEmpty(url))
        .compose(bindToLifecycle())
        .subscribe(this.startBrowser::onNext);
    }

    private Uri appendRefTagIfNone(final @NonNull Uri uri) {
      final String url = uri.toString();
      final String ref = UrlUtils.INSTANCE.refTag(url);
      if (ObjectUtils.isNull(ref)) {
        return Uri.parse(UrlUtils.INSTANCE.appendRefTag(url, RefTag.deepLink().tag()));
      }

      return uri;
    }

    private boolean lastPathSegmentIsProjects(final @NonNull Uri uri) {
      return uri.getLastPathSegment().equals("projects");
    }

    private final BehaviorSubject<String> startBrowser = BehaviorSubject.create();
    private final BehaviorSubject<Void> startDiscoveryActivity = BehaviorSubject.create();
    private final BehaviorSubject<Uri> startProjectActivity = BehaviorSubject.create();
    private final BehaviorSubject<Uri> startProjectActivityWithCheckout = BehaviorSubject.create();

    public final Outputs outputs = this;

    @Override public @NonNull Observable<String> startBrowser() {
      return this.startBrowser;
    }
    @Override public @NonNull Observable<Void> startDiscoveryActivity() {
      return this.startDiscoveryActivity;
    }
    @Override public @NonNull Observable<Uri> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<Uri> startProjectActivityForCheckout() {
      return this.startProjectActivityWithCheckout;
    }
  }
}
