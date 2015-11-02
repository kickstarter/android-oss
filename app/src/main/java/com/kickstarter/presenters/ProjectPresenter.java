package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.RxUtils;
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

public class ProjectPresenter extends Presenter<ProjectActivity> implements ProjectAdapter.Delegate {
  @Inject ApiClient client;
  @Inject CurrentUser currentUser;
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

    final Observable<User> loggedInUserOnStarClick = RxUtils.takeWhen(currentUser.observable(), starClick)
      .filter(u -> u != null);

    final Observable<User> loggedOutUserOnStarClick = RxUtils.takeWhen(currentUser.observable(), starClick)
      .filter(u -> u == null);

    final Observable<Project> projectOnUserChangeStar = RxUtils.takeWhen(initialProject, loggedInUserOnStarClick)
      .switchMap(this::toggleProjectStar)
      .share();

    final Observable<Project> starredProjectOnLoginSuccess = RxUtils.takeWhen(initialProject, loginSuccess)
      .take(1)
      .switchMap(this::starProject)
      .share();

    final Observable<Project> project = initialProject.map(Project::param).mergeWith(initialProjectParam)
      .filter(param -> param != null)
      .switchMap(param -> client.fetchProject(param).compose(Transformers.neverError()))
      .mergeWith(projectOnUserChangeStar)
      .mergeWith(starredProjectOnLoginSuccess)
      .share();

    final Observable<Pair<ProjectActivity, Project>> viewAndProject =
      RxUtils.combineLatestPair(viewSubject, project);

    addSubscription(
      viewAndProject
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.show(vp.second))
    );

    addSubscription(
      RxUtils.takePairWhen(
        viewSubject,
        projectOnUserChangeStar.mergeWith(starredProjectOnLoginSuccess)
      )
        .filter(vp -> vp.second.isStarred())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.showStarPrompt())
    );

    addSubscription(
      RxUtils.takeWhen(viewSubject, loggedOutUserOnStarClick)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(ProjectActivity::startLoginToutActivity)
    );

    addSubscription(
      RxUtils.takePairWhen(viewAndProject, rewardClick)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vpr -> {
          final ProjectActivity view = vpr.first.first;
          final Project p = vpr.first.second;
          final Reward r = vpr.second;
          view.startRewardSelectedCheckout(p, r);
        })
    );

    addSubscription(RxUtils.takeWhen(viewAndProject, backProjectClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startCheckoutActivity(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, shareClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startShareIntent(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, blurbClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showProjectDescription(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, commentsClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startCommentsActivity(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, creatorNameClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showCreatorBio(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, managePledgeClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.managePledge(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, updatesClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showUpdates(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, viewPledgeClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startViewPledgeActivity(vp.second)));
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

  public void projectShareClicked(@NonNull final ProjectViewHolder viewHolder) {
    shareClick.onNext(null);
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
