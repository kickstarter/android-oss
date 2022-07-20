package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.UrlUtils;
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

    /** Emits the project name and url to share using Android's default share behavior. */
    Observable<Pair<String, String>> startShare();

    /** Emits the project and url to share using Facebook. */
    Observable<Pair<Project, String>> startShareOnFacebook();

    /** Emits the project name and url to share using Twitter. */
    Observable<Pair<String, String>> startShareOnTwitter();
  }

  final class ViewModel extends ActivityViewModel<ThanksShareViewHolder> implements Inputs, Outputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.project
        .map(Project::name)
        .compose(bindToLifecycle())
        .subscribe(this.projectName::onNext);

      this.project
        .map(p -> Pair.create(p.name(), UrlUtils.INSTANCE.appendRefTag(p.webProjectUrl(), RefTag.thanksShare().tag())))
        .compose(takeWhen(this.shareClick))
        .compose(bindToLifecycle())
        .subscribe(this.startShare::onNext);

      this.project
        .map(p -> Pair.create(p, UrlUtils.INSTANCE.appendRefTag(p.webProjectUrl(), RefTag.thanksFacebookShare().tag())))
        .compose(takeWhen(this.shareOnFacebookClick))
        .compose(bindToLifecycle())
        .subscribe(this.startShareOnFacebook::onNext);

      this.project
        .map(p -> Pair.create(p.name(), UrlUtils.INSTANCE.appendRefTag(p.webProjectUrl(), RefTag.thanksTwitterShare().tag())))
        .compose(takeWhen(this.shareOnTwitterClick))
        .compose(bindToLifecycle())
        .subscribe(this.startShareOnTwitter::onNext);
    }

    private final PublishSubject<Project> project = PublishSubject.create();
    private final PublishSubject<Void> shareClick = PublishSubject.create();
    private final PublishSubject<Void> shareOnFacebookClick = PublishSubject.create();
    private final PublishSubject<Void> shareOnTwitterClick = PublishSubject.create();

    private final BehaviorSubject<String> projectName = BehaviorSubject.create();
    private final PublishSubject<Pair<String, String>> startShare = PublishSubject.create();
    private final PublishSubject<Pair<Project, String>> startShareOnFacebook = PublishSubject.create();
    private final PublishSubject<Pair<String, String>> startShareOnTwitter = PublishSubject.create();

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

    @Override public @NonNull Observable<Pair<String, String>> startShare() {
      return this.startShare;
    }
    @Override public @NonNull Observable<Pair<Project, String>> startShareOnFacebook() {
      return this.startShareOnFacebook;
    }
    @Override public @NonNull Observable<Pair<String, String>> startShareOnTwitter() {
      return this.startShareOnTwitter;
    }
    @Override public @NonNull Observable<String> projectName() {
      return this.projectName;
    }
  }
}
