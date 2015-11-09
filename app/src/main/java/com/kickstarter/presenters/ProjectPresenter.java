package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.ProjectActivity;
import com.kickstarter.ui.adapters.ProjectAdapter;
import com.kickstarter.ui.viewholders.ProjectViewHolder;
import com.kickstarter.ui.viewholders.RewardViewHolder;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public final class ProjectPresenter extends Presenter<ProjectActivity> implements ProjectAdapter.Delegate {
  @Inject ApiClient client;
  @Inject CurrentUser currentUser;
  @Inject Koala koala;
  private final PublishSubject<Void> backProjectClick = PublishSubject.create();
  private final PublishSubject<Void> blurbClick = PublishSubject.create();
  private final PublishSubject<Void> commentsClick = PublishSubject.create();
  private final PublishSubject<Void> creatorNameClick = PublishSubject.create();
  private final PublishSubject<Void> shareClick = PublishSubject.create();
  private final PublishSubject<Void> updatesClick = PublishSubject.create();
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  private final PublishSubject<Void> managePledgeClick = PublishSubject.create();
  private final PublishSubject<Reward> rewardClick = PublishSubject.create();
  private final PublishSubject<Void> starClick = PublishSubject.create();
  private final PublishSubject<Void> viewPledgeClick = PublishSubject.create();
  private final PublishSubject<Project> initialProject = PublishSubject.create();
  private final PublishSubject<String> initialProjectParam = PublishSubject.create();

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<User> loggedInUserOnStarClick = currentUser.observable()
      .compose(Transformers.takeWhen(starClick))
      .filter(u -> u != null);

    final Observable<User> loggedOutUserOnStarClick = currentUser.observable()
      .compose(Transformers.takeWhen(starClick))
      .filter(u -> u == null);

    final Observable<Project> projectOnUserChangeStar = initialProject
      .compose(Transformers.takeWhen(loggedInUserOnStarClick))
      .switchMap(this::toggleProjectStar)
      .share();

    final Observable<Project> starredProjectOnLoginSuccess = initialProject
      .compose(Transformers.takeWhen(loginSuccess))
      .take(1)
      .switchMap(this::starProject)
      .share();

    final Observable<Project> project = initialProject.map(Project::param).mergeWith(initialProjectParam)
      .filter(param -> param != null)
      .switchMap(param -> client.fetchProject(param).compose(Transformers.neverError()))
      .mergeWith(projectOnUserChangeStar)
      .mergeWith(starredProjectOnLoginSuccess)
      .mergeWith(initialProject)
      .share();

    final Observable<Pair<ProjectActivity, Project>> viewAndProject = viewSubject
      .compose(Transformers.combineLatestPair(project));

    addSubscription(
      viewAndProject
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.show(vp.second))
    );

    addSubscription(
      viewSubject
        .compose(Transformers.takePairWhen(projectOnUserChangeStar.mergeWith(starredProjectOnLoginSuccess)))
        .filter(vp -> vp.second.isStarred())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.showStarPrompt())
    );

    addSubscription(
      viewSubject
        .compose(Transformers.takeWhen(loggedOutUserOnStarClick))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(ProjectActivity::startLoginToutActivity)
    );

    addSubscription(
      viewAndProject
        .compose(Transformers.takePairWhen(rewardClick))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vpr -> {
          final ProjectActivity view = vpr.first.first;
          final Project p = vpr.first.second;
          final Reward r = vpr.second;
          view.startRewardSelectedCheckout(p, r);
        })
    );

    addSubscription(viewAndProject
      .compose(Transformers.takeWhen(backProjectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startCheckoutActivity(vp.second)));

    addSubscription(viewAndProject
      .compose(Transformers.takeWhen(shareClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startShareIntent(vp.second)));

    addSubscription(viewAndProject
      .compose(Transformers.takeWhen(blurbClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showProjectDescription(vp.second)));

    addSubscription(viewAndProject
      .compose(Transformers.takeWhen(commentsClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startCommentsActivity(vp.second)));

    addSubscription(viewAndProject
      .compose(Transformers.takeWhen(creatorNameClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showCreatorBio(vp.second)));

    addSubscription(viewAndProject
      .compose(Transformers.takeWhen(managePledgeClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.managePledge(vp.second)));

    addSubscription(viewAndProject
      .compose(Transformers.takeWhen(updatesClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showUpdates(vp.second)));

    addSubscription(viewAndProject
      .compose(Transformers.takeWhen(viewPledgeClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startViewPledgeActivity(vp.second)));

    addSubscription(projectOnUserChangeStar.mergeWith(starredProjectOnLoginSuccess)
      .subscribe(koala::trackProjectStar));
  }

  public void initialize(@Nullable final Project initialProject, @Nullable final String param) {
    if (initialProject != null) {
      this.initialProject.onNext(initialProject);
    }
    if (initialProjectParam != null) {
      initialProjectParam.onNext(param);
    }
  }

  public void takeBackProjectClick() {
    backProjectClick.onNext(null);
  }

  public void projectBlurbClicked(@NonNull final ProjectViewHolder viewHolder) {
    blurbClick.onNext(null);
  }

  public void projectCommentsClicked(@NonNull final ProjectViewHolder viewHolder) {
    commentsClick.onNext(null);
  }

  public void projectCreatorNameClicked(@NonNull final ProjectViewHolder viewHolder){
    creatorNameClick.onNext(null);
  }

  public void rewardClicked(@NonNull final RewardViewHolder viewHolder, @NonNull final Reward reward) {
    rewardClick.onNext(reward);
  }

  public void takeShareClick() {
    shareClick.onNext(null);
  }

  public void takeStarClick() {
    starClick.onNext(null);
  }

  public void projectUpdatesClicked(@NonNull final ProjectViewHolder viewHolder) {
    updatesClick.onNext(null);
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
  }

  public void takeManagePledgeClick() {
    managePledgeClick.onNext(null);
  }

  public void takeViewPledgeClick() {
    viewPledgeClick.onNext(null);
  }

  public Observable<Project> starProject(@NonNull final Project project) {
    return client.starProject(project)
      .onErrorResumeNext(Observable.empty());
  }

  public Observable<Project> toggleProjectStar(@NonNull final Project project) {
    return client.toggleProjectStar(project)
      .onErrorResumeNext(Observable.empty());
  }
}
