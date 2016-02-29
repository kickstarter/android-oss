package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ThanksActivity;
import com.kickstarter.ui.adapters.ThanksAdapter;
import com.kickstarter.ui.viewholders.CategoryPromoViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardMiniViewHolder;
import com.kickstarter.viewmodels.outputs.ThanksViewModelOutputs;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ThanksViewModel extends ViewModel<ThanksActivity> implements ThanksViewModelOutputs, ThanksAdapter.Delegate {

  private final PublishSubject<Void> facebookClick = PublishSubject.create();
  private final PublishSubject<Void> shareClick = PublishSubject.create();
  private final PublishSubject<Void> twitterClick = PublishSubject.create();
  private final PublishSubject<Project> projectCardMiniClick = PublishSubject.create();
  private final PublishSubject<Category> categoryPromoClick = PublishSubject.create();

  private final ApiClientType apiClient;

  private final BehaviorSubject<Project> project = BehaviorSubject.create();
  @Override
  public Observable<Project> project() {
    return project;
  }

  public final ThanksViewModelOutputs outputs = this;

  public ThanksViewModel(final @NonNull Environment environment) {
    super(environment);

    apiClient = environment.apiClient();

    final Observable<Pair<ThanksActivity, Project>> viewAndProject = view()
      .compose(Transformers.combineLatestPair(project))
      .filter(vp -> vp.first != null);

    intent()
      .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
      .ofType(Project.class)
      .take(1)
      .compose(bindToLifecycle())
      .subscribe(project::onNext);

    shareClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowShareSheet());

    twitterClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowTwitterShareView());

    facebookClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowFacebookShareView());

    projectCardMiniClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutFinishJumpToProject());

    viewAndProject
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.show(vp.second));

    viewAndProject
      .compose(Transformers.takeWhen(facebookClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startFacebookShareIntent(vp.second));

    viewAndProject
      .compose(Transformers.takeWhen(shareClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startShareIntent(vp.second));

    viewAndProject
      .compose(Transformers.takeWhen(twitterClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startTwitterShareIntent(vp.second));

    viewChange()
      .compose(Transformers.takePairWhen(projectCardMiniClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startProjectIntent(vp.second));

    viewChange()
      .compose(Transformers.takePairWhen(categoryPromoClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startDiscoveryCategoryIntent(vp.second));

    final Observable<Category> rootCategory = project.flatMap(this::rootCategory);
    final Observable<Pair<List<Project>, Category>> projectsAndRootCategory = project
      .flatMap(this::relatedProjects)
      .compose(bindToLifecycle())
      .compose(Transformers.zipPair(rootCategory));

    view()
      .compose(Transformers.combineLatestPair(projectsAndRootCategory))
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vpc -> {
        final ThanksActivity view = vpc.first;
        final List<Project> ps = vpc.second.first;
        final Category category = vpc.second.second;
        view.showRecommended(ps, category);
      });

    categoryPromoClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutFinishJumpToDiscovery());

    projectCardMiniClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutFinishJumpToProject());

    shareClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowShareSheet());

    twitterClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowTwitterShareView());

    facebookClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowFacebookShareView());

    projectCardMiniClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutFinishJumpToProject());
  }

  /**
   * Given a project, returns an observable that emits the project's root category.
   */
  private @NonNull Observable<Category> rootCategory(final @NonNull Project project) {
    final Category category = project.category();

    if (category == null) {
      return Observable.empty();
    }

    if (category.parent() != null) {
      return Observable.just(category.parent());
    }

    return apiClient.fetchCategory(String.valueOf(category.rootId()))
      .compose(Transformers.neverError());
  }

  /**
   * Returns a shuffled list of 3 recommended projects, with fallbacks to similar and staff picked projects
   * for users with fewer than 3 recommendations.
   */
  private @NonNull Observable<List<Project>> relatedProjects(final @NonNull Project project) {
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

    final Category category = project.category();
    final DiscoveryParams staffPickParams = DiscoveryParams.builder()
      .category(category == null ? null : category.root())
      .backed(-1)
      .staffPicks(true)
      .perPage(3)
      .build();

    final Observable<Project> recommendedProjects = apiClient.fetchProjects(recommendedParams)
      .retry(2)
      .map(DiscoverEnvelope::projects)
      .map(ListUtils::shuffle)
      .flatMap(Observable::from)
      .take(3);

    final Observable<Project> similarToProjects = apiClient.fetchProjects(similarToParams)
      .retry(2)
      .map(DiscoverEnvelope::projects)
      .flatMap(Observable::from);

    final Observable<Project> staffPickProjects = apiClient.fetchProjects(staffPickParams)
      .retry(2)
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
  public void categoryPromoClick(final @NonNull CategoryPromoViewHolder viewHolder, final @NonNull Category category) {
    categoryPromoClick.onNext(category);
  }

  @Override
  public void projectCardMiniClick(final @NonNull ProjectCardMiniViewHolder viewHolder, final @NonNull Project project) {
    projectCardMiniClick.onNext(project);
  }
}
