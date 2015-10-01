package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.activities.ThanksActivity;
import com.kickstarter.ui.adapters.ThanksAdapter;
import com.kickstarter.ui.viewholders.CategoryPromoViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardMiniViewHolder;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class ThanksPresenter extends Presenter<ThanksActivity> implements ThanksAdapter.Delegate {
  private final PublishSubject<Void> doneClick = PublishSubject.create();
  private final PublishSubject<Void> facebookClick = PublishSubject.create();
  private final PublishSubject<Void> shareClick = PublishSubject.create();
  private final PublishSubject<Void> twitterClick = PublishSubject.create();
  private final PublishSubject<Project> projectCardMiniClick = PublishSubject.create();
  private final PublishSubject<Category> categoryPromoClick = PublishSubject.create();

  @Inject ApiClient apiClient;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  public void takeProject(final Project project) {
    final Observable<Pair<ThanksActivity, Project>> viewAndProject = RxUtils.combineLatestPair(viewSubject, Observable.just(project))
      .filter(vp -> vp.first != null);

    addSubscription(viewAndProject
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.show(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, facebookClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startFacebookShareIntent(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, shareClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startShareIntent(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, twitterClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startTwitterShareIntent(vp.second)));

    addSubscription(RxUtils.takePairWhen(viewChange, projectCardMiniClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectIntent(vp.second)));

    addSubscription(RxUtils.takePairWhen(viewChange, categoryPromoClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startDiscoveryCategoryIntent(vp.second)));

    addSubscription(RxUtils.combineLatestPair(viewSubject.filter(v -> v != null), doneClick)
      .map(vp -> vp.first)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ThanksActivity::startDiscoveryActivity));

    final DiscoveryParams params = DiscoveryParams.builder()
      .category(project.category().root())
      .backed(-1)
      .perPage(3)
      .build();

    final Observable<List<Project>> recommendedProjects = apiClient.fetchProjects(params)
      .map(envelope -> envelope.projects);
    final Observable<Category> rootCategory = apiClient.fetchCategory(project.category().rootId());
    final Observable<Pair<List<Project>, Category>> projectsAndRootCategory =
      RxUtils.zipPair(recommendedProjects, rootCategory);

    addSubscription(
      RxUtils.combineLatestPair(viewSubject, projectsAndRootCategory)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vpc -> {
        final ThanksActivity view = vpc.first;
        final List<Project> projects = vpc.second.first;
        final Category category = vpc.second.second;
        view.showRecommended(projects, category);
      })
    );
  }

  public void takeDoneClick() {
    doneClick.onNext(null);
  }

  public void takeFacebookClick() {
    facebookClick.onNext(null);
  }

  public void takeShareClick() {
    shareClick.onNext(null);
  }

  public void takeTwitterClick() {
    twitterClick.onNext(null);
  }

  @Override
  public void categoryPromoClick(final CategoryPromoViewHolder viewHolder, final Category category) {
    categoryPromoClick.onNext(category);
  }

  @Override
  public void projectCardMiniClick(final ProjectCardMiniViewHolder viewHolder, final Project project) {
    projectCardMiniClick.onNext(project);
  }
}
