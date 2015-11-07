package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
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
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  public void takeProject(@NonNull final Project project) {
    final Observable<Pair<ThanksActivity, Project>> viewAndProject = viewSubject
      .compose(Transformers.combineLatestPair(Observable.just(project)))
      .filter(vp -> vp.first != null);

    addSubscription(viewAndProject
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.show(vp.second)));

    addSubscription(viewAndProject
      .compose(Transformers.takeWhen(facebookClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startFacebookShareIntent(vp.second)));

    addSubscription(viewAndProject
      .compose(Transformers.takeWhen(shareClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startShareIntent(vp.second)));

    addSubscription(viewAndProject
      .compose(Transformers.takeWhen(twitterClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startTwitterShareIntent(vp.second)));

    addSubscription(viewChange
      .compose(Transformers.takePairWhen(projectCardMiniClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectIntent(vp.second)));

    addSubscription(viewChange
      .compose(Transformers.takePairWhen(categoryPromoClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startDiscoveryCategoryIntent(vp.second)));

    addSubscription(viewSubject.filter(v -> v != null)
      .compose(Transformers.combineLatestPair(doneClick))
      .map(vp -> vp.first)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ThanksActivity::startDiscoveryActivity));

    final DiscoveryParams params = DiscoveryParams.builder()
      .category(project.category().root())
      .backed(-1)
      .perPage(3)
      .build();

    final Observable<List<Project>> recommendedProjects = apiClient.fetchProjects(params)
      .compose(Transformers.neverError())
      .map(DiscoverEnvelope::projects);
    final Observable<Category> rootCategory = apiClient.fetchCategory(project.category().rootId())
      .compose(Transformers.neverError());
    final Observable<Pair<List<Project>, Category>> projectsAndRootCategory = recommendedProjects
      .compose(Transformers.zipPair(rootCategory));

    addSubscription(viewSubject
        .compose(Transformers.combineLatestPair(projectsAndRootCategory))
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
  public void categoryPromoClick(@NonNull final CategoryPromoViewHolder viewHolder, @NonNull final Category category) {
    categoryPromoClick.onNext(category);
  }

  @Override
  public void projectCardMiniClick(@NonNull final ProjectCardMiniViewHolder viewHolder, @NonNull final Project project) {
    projectCardMiniClick.onNext(project);
  }
}
