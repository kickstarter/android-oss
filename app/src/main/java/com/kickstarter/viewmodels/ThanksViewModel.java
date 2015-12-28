package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
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

public final class ThanksViewModel extends ViewModel<ThanksActivity> implements ThanksAdapter.Delegate {
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

    addSubscription(shareClick.subscribe(__ -> koala.trackCheckoutShowShareSheet()));

    addSubscription(twitterClick.subscribe(__ -> koala.trackCheckoutShowTwitterShareView()));

    addSubscription(facebookClick.subscribe(__ -> koala.trackCheckoutShowFacebookShareView()));

    addSubscription(projectCardMiniClick.subscribe(__ -> koala.trackCheckoutFinishJumpToProject()));
  }

  public void takeProject(@NonNull final Project project) {
    final Observable<Pair<ThanksActivity, Project>> viewAndProject = view
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

    final Observable<Category> rootCategory = apiClient.fetchCategory(project.category().rootId())
      .compose(Transformers.neverError());
    final Observable<Pair<List<Project>, Category>> projectsAndRootCategory = moreProjects(project)
      .compose(Transformers.zipPair(rootCategory));

    addSubscription(view
        .compose(Transformers.combineLatestPair(projectsAndRootCategory))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vpc -> {
          final ThanksActivity view = vpc.first;
          final List<Project> ps = vpc.second.first;
          final Category category = vpc.second.second;
          view.showRecommended(ps, category);
        })
    );
  }

  /**
   * Returns a shuffled list of 3 recommended projects, with fallbacks to similar and staff picked projects
   * for users with fewer than 3 recommendations.
   */
  public Observable<List<Project>> moreProjects(final @NonNull Project project) {
    final DiscoveryParams recommendedParams = DiscoveryParams.builder()
      .backed(-1)
      .recommended(true)
      .perPage(6)
      .build();

    final DiscoveryParams similarToParams = DiscoveryParams.builder()
      .backed(-1)
      .similarTo(project)
      .perPage(3)
      .build();

    final DiscoveryParams staffPickParams = DiscoveryParams.builder()
      .category(project.category().root())
      .backed(-1)
      .staffPicks(true)
      .perPage(3)
      .build();

    // shuffle projects to show fresh recommendations
    final Observable<Project> recommendedProjects = apiClient.fetchProjects(recommendedParams)
      .map(DiscoverEnvelope::projects)
      .map(ListUtils::shuffle)
      .flatMap(Observable::from)
      .take(3);

    final Observable<Project> similarToProjects = apiClient.fetchProjects(similarToParams)
      .map(DiscoverEnvelope::projects)
      .flatMap(Observable::from);

    final Observable<Project> staffPickProjects = apiClient.fetchProjects(staffPickParams)
      .map(DiscoverEnvelope::projects)
      .flatMap(Observable::from);

    return Observable.concat(recommendedProjects, similarToProjects, staffPickProjects)
      .compose(Transformers.neverError())
      .distinct()
      .take(3)
      .toList();
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
