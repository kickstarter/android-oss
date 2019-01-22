package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.ThanksShareViewHolder;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface ThanksShareHolderViewModel {

  interface Inputs {
    /** Call to configure the view model with a project. */
    void configureWith(Project project);

    /** Call when the share button is clicked. */
    void shareClick();

    /** Call when the share on Facebook button is clicked. */
    void shareOnFacebookClick();

    /** Call when the share on Twitter button is clicked. */
    void shareOnTwitterClick();
  }

  interface Outputs {
    /** Emits the backing's project name. */
    Observable<String> projectName();

    /** Share the project using Android's app chooser. */
    Observable<Project> startShare();

    /** Share the project on Facebook. */
    Observable<Project> startShareOnFacebook();

    /** Share the project on Twitter. */
    Observable<Project> startShareOnTwitter();
  }

  final class ViewModel extends ActivityViewModel<ThanksShareViewHolder> implements Inputs, Outputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.project
        .map(Project::name)
        .compose(bindToLifecycle())
        .subscribe(this.projectName::onNext);

      this.project
        .compose(takeWhen(this.shareClick))
        .compose(bindToLifecycle())
        .subscribe(this.startShare::onNext);

      this.project
        .compose(takeWhen(this.shareOnFacebookClick))
        .compose(bindToLifecycle())
        .subscribe(this.startShareOnFacebook::onNext);

      this.project
        .compose(takeWhen(this.shareOnTwitterClick))
        .compose(bindToLifecycle())
        .subscribe(this.startShareOnTwitter::onNext);

      this.shareClick
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackCheckoutShowShareSheet());

      this.shareOnFacebookClick
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackCheckoutShowFacebookShareView());

      this.shareOnTwitterClick
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackCheckoutShowTwitterShareView());
    }

    private final PublishSubject<Project> project = PublishSubject.create();
    private final PublishSubject<Void> shareClick = PublishSubject.create();
    private final PublishSubject<Void> shareOnFacebookClick = PublishSubject.create();
    private final PublishSubject<Void> shareOnTwitterClick = PublishSubject.create();

    private final BehaviorSubject<String> projectName = BehaviorSubject.create();
    private final PublishSubject<Project> startShare = PublishSubject.create();
    private final PublishSubject<Project> startShareOnFacebook = PublishSubject.create();
    private final PublishSubject<Project> startShareOnTwitter = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void configureWith(final @NonNull Project project) {
      this.project.onNext(project);
    }
    @Override public void shareClick() {
      this.shareClick.onNext(null);
    }
    @Override public void shareOnFacebookClick() {
      this.shareOnFacebookClick.onNext(null);
    }
    @Override public void shareOnTwitterClick() {
      this.shareOnTwitterClick.onNext(null);
    }

    @Override public @NonNull Observable<Project> startShare() {
      return this.startShare;
    }
    @Override public @NonNull Observable<Project> startShareOnFacebook() {
      return this.startShareOnFacebook;
    }
    @Override public @NonNull Observable<Project> startShareOnTwitter() {
      return this.startShareOnTwitter;
    }
    @Override public @NonNull Observable<String> projectName() {
      return this.projectName;
    }
  }
}
