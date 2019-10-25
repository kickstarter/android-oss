package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.CheckoutActivity;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public interface CheckoutViewModel {

  interface Inputs {
    /** Takes a url whenever a page has been intercepted by the web view.
     * @param url The url that has been intercepted */
    void pageIntercepted(final @NonNull String url);
  }

  interface Outputs {
    /** Emits when the activity should pop itself off the navigation stack. */
    Observable<Void> popActivityOffStack();

    /** The project associated with the current checkout. */
    Observable<Project> project();

    /** The title to display to the user. */
    Observable<String> title();

    /** The URL the web view should load, if its state has been destroyed. */
    Observable<String> url();
  }

  final class ViewModel extends ActivityViewModel<CheckoutActivity> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
        .ofType(Project.class)
        .compose(bindToLifecycle())
        .subscribe(this.project::onNext);

      intent()
        .map(i -> i.getStringExtra(IntentKey.TOOLBAR_TITLE))
        .ofType(String.class)
        .compose(bindToLifecycle())
        .subscribe(this.title::onNext);

      intent()
        .map(i -> i.getStringExtra(IntentKey.URL))
        .ofType(String.class)
        .take(1)
        .mergeWith(this.pageIntercepted)
        .compose(bindToLifecycle())
        .subscribe(this.url::onNext);
    }

    private final PublishSubject<String> pageIntercepted = PublishSubject.create();

    private final BehaviorSubject<Void> popActivityOffStack = BehaviorSubject.create();
    private final BehaviorSubject<Project> project = BehaviorSubject.create();
    private final BehaviorSubject<String> title = BehaviorSubject.create();
    private final BehaviorSubject<String> url = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void pageIntercepted(final @NonNull String str) {
      this.pageIntercepted.onNext(str);
    }

    @Override public @NonNull Observable<Void> popActivityOffStack() {
      return this.popActivityOffStack;
    }
    @Override public @NonNull Observable<Project> project() {
      return this.project;
    }
    @Override public @NonNull Observable<String> title() {
      return this.title;
    }
    @Override public @NonNull Observable<String> url() {
      return this.url;
    }
  }
}
