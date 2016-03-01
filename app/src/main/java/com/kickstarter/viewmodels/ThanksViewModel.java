package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ThanksActivity;
import com.kickstarter.ui.viewholders.ThanksCategoryViewHolder;
import com.kickstarter.ui.viewholders.ThanksProjectViewHolder;
import com.kickstarter.viewmodels.inputs.ThanksViewModelInputs;
import com.kickstarter.viewmodels.outputs.ThanksViewModelOutputs;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.zipPair;

public final class ThanksViewModel extends ViewModel<ThanksActivity> implements ThanksViewModelInputs, ThanksViewModelOutputs {
  private final PublishSubject<Category> categoryClick = PublishSubject.create();
  private final PublishSubject<Project> projectClick = PublishSubject.create();
  private final BehaviorSubject<String> projectName = BehaviorSubject.create();
  private final PublishSubject<Void> shareClick = PublishSubject.create();
  private final PublishSubject<Void> shareOnFacebookClick = PublishSubject.create();
  private final PublishSubject<Void> shareOnTwitterClick = PublishSubject.create();
  private final BehaviorSubject<Pair<List<Project>, Category>> showRecommendations = BehaviorSubject.create();
  private final BehaviorSubject<DiscoveryParams> startDiscovery = BehaviorSubject.create();
  private final BehaviorSubject<Project> startProject = BehaviorSubject.create();
  private final BehaviorSubject<Project> startShare = BehaviorSubject.create();
  private final BehaviorSubject<Project> startShareOnFacebook = BehaviorSubject.create();
  private final BehaviorSubject<Project> startShareOnTwitter = BehaviorSubject.create();

  private final ApiClientType apiClient;

  public ThanksViewModel(final @NonNull Environment environment) {
    super(environment);

    apiClient = environment.apiClient();

    final Observable<Project> project = intent()
      .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
      .ofType(Project.class)
      .take(1)
      .compose(bindToLifecycle());

    final Observable<Category> rootCategory = project.flatMap(this::rootCategory);
    final Observable<Pair<List<Project>, Category>> projectsAndRootCategory = project
      .flatMap(this::relatedProjects)
      .compose(bindToLifecycle())
      .compose(zipPair(rootCategory));

    project
      .map(Project::name)
      .compose(bindToLifecycle())
      .subscribe(projectName::onNext);

    projectClick
      .compose(bindToLifecycle())
      .subscribe(startProject::onNext);

    project
      .compose(takeWhen(shareClick))
      .compose(bindToLifecycle())
      .subscribe(startShare::onNext);

    project
      .compose(takeWhen(shareOnFacebookClick))
      .compose(bindToLifecycle())
      .subscribe(startShareOnFacebook::onNext);

    project
      .compose(takeWhen(shareOnTwitterClick))
      .compose(bindToLifecycle())
      .subscribe(startShareOnTwitter::onNext);

    categoryClick
      .compose(bindToLifecycle())
      .subscribe(c -> startDiscovery.onNext(DiscoveryParams.builder().category(c).build()));

    projectsAndRootCategory
      .compose(bindToLifecycle())
      .subscribe(showRecommendations::onNext);

    // Event tracking
    categoryClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutFinishJumpToDiscovery());

    projectClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutFinishJumpToProject());

    shareClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowShareSheet());

    shareOnFacebookClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowFacebookShareView());

    shareOnTwitterClick
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackCheckoutShowTwitterShareView());

    projectClick
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
      .compose(neverError());
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
      .compose(neverError())
      .distinct()
      .take(3)
      .toList();
  }

  // INPUTS
  public final ThanksViewModelInputs inputs = this;

  @Override
  public void categoryClick(final @NonNull ThanksCategoryViewHolder viewHolder, final @NonNull Category category) {
    categoryClick.onNext(category);
  }

  @Override
  public void projectClick(final @NonNull ThanksProjectViewHolder viewHolder, final @NonNull Project project) {
    projectClick.onNext(project);
  }

  @Override
  public void shareClick() {
    shareClick.onNext(null);
  }

  @Override
  public void shareOnFacebookClick() {
    shareOnFacebookClick.onNext(null);
  }

  @Override
  public void shareOnTwitterClick() {
    shareOnTwitterClick.onNext(null);
  }

  // OUTPUTS
  public final ThanksViewModelOutputs outputs = this;

  @Override
  public @NonNull Observable<String> projectName() {
    return projectName;
  }

  @Override
  public @NonNull Observable<Pair<List<Project>, Category>> showRecommendations() {
    return showRecommendations;
  }

  @Override
  public @NonNull Observable<DiscoveryParams> startDiscovery() {
    return startDiscovery;
  }

  @Override
  public @NonNull Observable<Project> startProject() {
    return startProject;
  }

  @Override
  public @NonNull Observable<Project> startShare() {
    return startShare;
  }

  @Override
  public @NonNull Observable<Project> startShareOnFacebook() {
    return startShareOnFacebook;
  }

  @Override
  public @NonNull Observable<Project> startShareOnTwitter() {
    return startShareOnTwitter;
  }
}
