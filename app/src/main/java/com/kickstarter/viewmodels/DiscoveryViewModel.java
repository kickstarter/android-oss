package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.preferences.IntPreference;
import com.kickstarter.libs.qualifiers.ActivitySamplePreference;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.DiscoveryParamsUtils;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.WebClient;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.adapters.DiscoveryAdapter;
import com.kickstarter.ui.viewholders.DiscoveryActivityViewHolder;
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;
import com.kickstarter.viewmodels.inputs.DiscoveryViewModelInputs;
import com.kickstarter.viewmodels.outputs.DiscoveryViewModelOutputs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.utils.BooleanUtils.isTrue;

public final class DiscoveryViewModel extends ViewModel<DiscoveryActivity> implements DiscoveryAdapter.Delegate, DiscoveryViewModelInputs, DiscoveryViewModelOutputs {
  protected @Inject ApiClientType apiClient;
  protected @Inject WebClient webClient;
  protected @Inject BuildCheck buildCheck;
  protected @Inject CurrentUser currentUser;
  protected @Inject @ActivitySamplePreference IntPreference activitySamplePreference;

  // INPUTS
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
  }
  private final PublishSubject<Void> filterButtonClicked = PublishSubject.create();
  public void filterButtonClicked() {
    filterButtonClicked.onNext(null);
  }
  private final PublishSubject<DiscoveryParams> initializer = PublishSubject.create();
  public void initializer(final @NonNull DiscoveryParams params) {
    initializer.onNext(params);
  }

  // OUTPUTS
  private final BehaviorSubject<List<Project>> projects = BehaviorSubject.create();
  public Observable<List<Project>> projects() {
    return projects;
  }
  private final BehaviorSubject<DiscoveryParams> params = BehaviorSubject.create();
  public Observable<DiscoveryParams> params() {
    return params;
  }
  private final BehaviorSubject<Activity> activity = BehaviorSubject.create();
  public Observable<Activity> activity() {
    return activity;
  }
  private final BehaviorSubject<Boolean> shouldShowOnboarding = BehaviorSubject.create();
  public Observable<Boolean> shouldShowOnboarding() {
    return shouldShowOnboarding;
  }
  public Observable<DiscoveryParams> showFilters() {
    return params.compose(Transformers.takeWhen(filterButtonClicked));
  }
  private final PublishSubject<Project> showProject = PublishSubject.create();
  public Observable<Pair<Project, RefTag>> showProject() {
    return params.compose(Transformers.takePairWhen(showProject))
      .map(pp -> DiscoveryViewModel.projectAndRefTagFromParamsAndProject(pp.first, pp.second));
  }
  private final PublishSubject<Void> showSignupLogin = PublishSubject.create();
  public Observable<Void> showSignupLogin() {
    return showSignupLogin;
  }
  private final PublishSubject<Void> showActivityFeed = PublishSubject.create();
  public Observable<Void> showActivityFeed() {
    return showActivityFeed;
  }
  private final PublishSubject<Activity> showActivityUpdate = PublishSubject.create();
  public Observable<Activity> showActivityUpdate() {
    return showActivityUpdate;
  }
  private final BehaviorSubject<Boolean> shouldShowActivitySample = BehaviorSubject.create();
  public Observable<Boolean> shouldShowActivitySample() {
    return shouldShowActivitySample;
  }

  // ERRORS
  private PublishSubject<ErrorEnvelope> activityError = PublishSubject.create();

  private boolean hasSeenOnboarding = false;

  public final DiscoveryViewModelInputs inputs = this;
  public final DiscoveryViewModelOutputs outputs = this;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    buildCheck.bind(this, webClient);

    final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
      ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
        .nextPage(nextPage)
        .startOverWith(params)
        .envelopeToListOfData(DiscoverEnvelope::projects)
        .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
        .loadWithParams(apiClient::fetchProjects)
        .loadWithPaginationPath(apiClient::fetchProjects)
        .pageTransformation(this::bringPotdToFront)
        .clearWhenStartingOver(true)
        .concater(ListUtils::concatDistinct)
        .build();

    addSubscription(
      params.compose(Transformers.takePairWhen(paginator.loadingPage))
        .map(paramsAndPage -> paramsAndPage.first.toBuilder().page(paramsAndPage.second).build())
        .subscribe(p -> koala.trackDiscovery(p, !hasSeenOnboarding))
    );

    addSubscription(paginator.paginatedData.subscribe(projects));

    addSubscription(
      params
        .compose(Transformers.combineLatestPair(currentUser.isLoggedIn()))
        .map(pu -> isOnboardingVisible(pu.first, pu.second))
        .doOnNext(show -> hasSeenOnboarding = show || hasSeenOnboarding)
        .subscribe(shouldShowOnboarding::onNext)
    );

    addSubscription(
      currentUser.loggedInUser()
        .compose(Transformers.combineLatestPair(params))
        .flatMap(__ -> this.fetchActivity())
        .map(this::activityIfNotSeen)
        .doOnNext(this::saveLastSeenActivityId)
        .subscribe(activity::onNext)
    );

    initializer.subscribe(this.params::onNext);
    initializer.onNext(DiscoveryParams.builder().staffPicks(true).build());
  }

  /**
   * Given a list of projects, finds if it contains the POTD and if so
   * bumps it to the front of the list.
   */
  private List<Project> bringPotdToFront(final @NonNull List<Project> projects) {

    return Observable.from(projects)
      .reduce(new ArrayList<>(), this::prependPotdElseAppend)
      .toBlocking().single();
  }

  /**
   * Given a list of projects and a particular project, returns the list
   * when the project prepended if it's POTD and appends otherwise.
   */
  @NonNull private List<Project> prependPotdElseAppend(final @NonNull List<Project> projects, final @NonNull Project project) {
    return project.isPotdToday() ? ListUtils.prepend(projects, project) : ListUtils.append(projects, project);
  }

  private boolean isOnboardingVisible(final @NonNull DiscoveryParams currentParams, final boolean isLoggedIn) {
    return !isLoggedIn && !hasSeenOnboarding && isTrue(currentParams.staffPicks());
  }

  /**
   * Converts a pair (params, project) into a (project, refTag) pair that does some extra logic around POTD.
   */
  private static @NonNull Pair<Project, RefTag> projectAndRefTagFromParamsAndProject(final @NonNull DiscoveryParams params, final @NonNull Project project) {
    final RefTag refTag;
    if (project.isPotdToday()) {
      refTag = RefTag.discoverPotd();
    } else if (project.isFeaturedToday()) {
      refTag = RefTag.categoryFeatured();
    } else {
      refTag = DiscoveryParamsUtils.refTag(params);
    }

    return new Pair<>(project, refTag);
  }

  public Observable<Activity> fetchActivity() {
    return apiClient.fetchActivities(1)
      .map(ActivityEnvelope::activities)
      .map(activities -> activities.get(0))
      .compose(Transformers.pipeApiErrorsTo(activityError));
  }

  private @Nullable Activity activityIfNotSeen(final @Nullable Activity activity) {
    if (activity != null && activity.id() != activitySamplePreference.get()) {
      return activity;
    }
    return null;
  }

  private void saveLastSeenActivityId(final @Nullable Activity activity) {
    if (activity != null) {
      activitySamplePreference.set((int) activity.id());
    }
  }

  public void projectCardViewHolderClicked(final @NonNull ProjectCardViewHolder viewHolder, final @NonNull Project project) {
    this.showProject.onNext(project);
  }

  public void discoveryOnboardingViewHolderSignupLoginClicked(final @NonNull DiscoveryOnboardingViewHolder viewHolder) {
    this.showSignupLogin.onNext(null);
  }

  public void discoveryActivityViewHolderSeeActivityClicked(final @NonNull DiscoveryActivityViewHolder viewHolder) {
    this.showActivityFeed.onNext(null);
  }

  public void discoveryActivityViewHolderProjectClicked(final @NonNull DiscoveryActivityViewHolder viewHolder, final @NonNull Project project) {
    this.showProject.onNext(project);
  }

  public void discoveryActivityViewHolderUpdateClicked(final @NonNull DiscoveryActivityViewHolder viewHolder, final @NonNull Activity activity) {
    this.showActivityUpdate.onNext(activity);
  }
}

