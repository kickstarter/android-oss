package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.preferences.BooleanPreferenceType;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.UserUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
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

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.zipPair;
import static com.kickstarter.libs.utils.BooleanUtils.isTrue;

public final class ThanksViewModel extends ActivityViewModel<ThanksActivity> implements ThanksViewModelInputs,
  ThanksViewModelOutputs {
  private final ApiClientType apiClient;
  private final BooleanPreferenceType hasSeenAppRatingPreference;
  private final BooleanPreferenceType hasSeenGamesNewsletterPreference;
  private final CurrentUserType currentUser;

  // INPUTS
  private final PublishSubject<Void> shareClick = PublishSubject.create();
  @Override
  public void shareClick() {
    this.shareClick.onNext(null);
  }

  private final PublishSubject<Void> shareOnFacebookClick = PublishSubject.create();
  @Override
  public void shareOnFacebookClick() {
    this.shareOnFacebookClick.onNext(null);
  }

  private final PublishSubject<Void> shareOnTwitterClick = PublishSubject.create();
  @Override
  public void shareOnTwitterClick() {
    this.shareOnTwitterClick.onNext(null);
  }

  private final PublishSubject<Void> signupToGamesNewsletterClick = PublishSubject.create();
  @Override
  public void signupToGamesNewsletterClick() {
    this.signupToGamesNewsletterClick.onNext(null);
  }

  // THANKS PROJECT VIEW HOLDER INPUT
  private final PublishSubject<Project> projectClick = PublishSubject.create();
  @Override
  public void projectClick(final @NonNull ThanksProjectViewHolder viewHolder, final @NonNull Project project) {
    this.projectClick.onNext(project);
  }

  // THANKS CATEGORY VIEW HOLDER INPUT
  private final PublishSubject<Category> categoryClick = PublishSubject.create();
  @Override
  public void categoryClick(final @NonNull ThanksCategoryViewHolder viewHolder, final @NonNull Category category) {
    this.categoryClick.onNext(category);
  }

  // OUTPUTS
  private final BehaviorSubject<String> projectName = BehaviorSubject.create();
  @Override
  public @NonNull Observable<String> projectName() {
    return this.projectName;
  }

  private final PublishSubject<Void> showConfirmGamesNewsletterDialog = PublishSubject.create();
  @Override
  public @NonNull Observable<Void> showConfirmGamesNewsletterDialog() {
    return this.showConfirmGamesNewsletterDialog;
  }

  private final PublishSubject<Void> showGamesNewsletterDialog = PublishSubject.create();
  @Override
  public @NonNull Observable<Void> showGamesNewsletterDialog() {
    return this.showGamesNewsletterDialog;
  }

  private final PublishSubject<Void> showRatingDialog = PublishSubject.create();
  @Override
  public @NonNull Observable<Void> showRatingDialog() {
    return this.showRatingDialog;
  }

  private final BehaviorSubject<Pair<List<Project>, Category>> showRecommendations = BehaviorSubject.create();
  @Override
  public @NonNull Observable<Pair<List<Project>, Category>> showRecommendations() {
    return this.showRecommendations;
  }

  private final PublishSubject<DiscoveryParams> startDiscovery = PublishSubject.create();
  @Override
  public @NonNull Observable<DiscoveryParams> startDiscovery() {
    return this.startDiscovery;
  }

  private final PublishSubject<Project> startProject = PublishSubject.create();
  @Override
  public @NonNull Observable<Project> startProject() {
    return this.startProject;
  }

  private final PublishSubject<Project> startShare = PublishSubject.create();
  @Override
  public @NonNull Observable<Project> startShare() {
    return this.startShare;
  }

  private final PublishSubject<Project> startShareOnFacebook = PublishSubject.create();
  @Override
  public @NonNull Observable<Project> startShareOnFacebook() {
    return this.startShareOnFacebook;
  }

  private final PublishSubject<Project> startShareOnTwitter = PublishSubject.create();
  @Override
  public @NonNull Observable<Project> startShareOnTwitter() {
    return this.startShareOnTwitter;
  }

  private final PublishSubject<User> signedUpToGamesNewsletter = PublishSubject.create();

  public final ThanksViewModelInputs inputs = this;
  public final ThanksViewModelOutputs outputs = this;

  public ThanksViewModel(final @NonNull Environment environment) {
    super(environment);

    this.apiClient = environment.apiClient();
    this.currentUser = environment.currentUser();
    this.hasSeenAppRatingPreference = environment.hasSeenAppRatingPreference();
    this.hasSeenGamesNewsletterPreference = environment.hasSeenGamesNewsletterPreference();

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

    final Observable<Boolean> isGamesCategory = rootCategory
      .map(c -> "games".equals(c.slug()));

    final Observable<Boolean> hasSeenGamesNewsletterDialog = Observable.just(this.hasSeenGamesNewsletterPreference.get());

    final Observable<Boolean> isSignedUpToGamesNewsletter = this.currentUser.observable()
      .map(u -> u != null && isTrue(u.gamesNewsletter()));

    final Observable<Boolean> showGamesNewsletter = Observable.combineLatest(
      isGamesCategory, hasSeenGamesNewsletterDialog, isSignedUpToGamesNewsletter,
      (isGames, hasSeen, isSignedUp) -> isGames && !hasSeen && !isSignedUp
    )
      .take(1);

    project
      .map(Project::name)
      .compose(bindToLifecycle())
      .subscribe(this.projectName::onNext);

    this.projectClick
      .compose(bindToLifecycle())
      .subscribe(this.startProject::onNext);

    project
      .compose(takeWhen(this.shareClick))
      .compose(bindToLifecycle())
      .subscribe(this.startShare::onNext);

    project
      .compose(takeWhen(this.shareOnFacebookClick))
      .compose(bindToLifecycle())
      .subscribe(this.startShareOnFacebook::onNext);

    project
      .compose(takeWhen(this.shareOnTwitterClick))
      .compose(bindToLifecycle())
      .subscribe(this.startShareOnTwitter::onNext);

    this.categoryClick
      .map(c -> DiscoveryParams.builder().category(c).build())
      .compose(bindToLifecycle())
      .subscribe(this.startDiscovery::onNext);

    project
      .flatMap(this::relatedProjects)
      .compose(zipPair(rootCategory))
      .compose(bindToLifecycle())
      .subscribe(this.showRecommendations::onNext);

    Observable.just(this.hasSeenAppRatingPreference.get())
      .take(1)
      .compose(combineLatestPair(showGamesNewsletter))
      .filter(ag -> !ag.first && !ag.second)
      .compose(ignoreValues())
      .compose(bindToLifecycle())
      .subscribe(__ -> this.showRatingDialog.onNext(null));

    showGamesNewsletter
      .filter(x -> x)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.showGamesNewsletterDialog.onNext(null));

    this.showGamesNewsletterDialog
      .compose(bindToLifecycle())
      .subscribe(__ -> this.hasSeenGamesNewsletterPreference.set(true));

    this.currentUser.observable()
      .filter(ObjectUtils::isNotNull)
      .compose(takeWhen(this.signupToGamesNewsletterClick))
      .flatMap(this::signupToGamesNewsletter)
      .compose(bindToLifecycle())
      .subscribe(this.signedUpToGamesNewsletter::onNext);

    this.currentUser.observable()
      .filter(ObjectUtils::isNotNull)
      .compose(takeWhen(this.signedUpToGamesNewsletter))
      .filter(UserUtils::isLocationGermany)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.showConfirmGamesNewsletterDialog.onNext(null));

    // Event tracking
    this.categoryClick
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackCheckoutFinishJumpToDiscovery());

    this.projectClick
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackCheckoutFinishJumpToProject());

    this.shareClick
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackCheckoutShowShareSheet());

    this.shareOnFacebookClick
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackCheckoutShowFacebookShareView());

    this.shareOnTwitterClick
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackCheckoutShowTwitterShareView());

    this.signedUpToGamesNewsletter
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackNewsletterToggle(true));
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

    return this.apiClient.fetchCategory(String.valueOf(category.rootId()))
      .compose(neverError());
  }

  /**
   * Returns a shuffled list of 3 recommended projectList, with fallbacks to similar and staff picked projectList
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

    final Observable<Project> recommendedProjects = this.apiClient.fetchProjects(recommendedParams)
      .retry(2)
      .map(DiscoverEnvelope::projects)
      .map(ListUtils::shuffle)
      .flatMap(Observable::from)
      .take(3);

    final Observable<Project> similarToProjects = this.apiClient.fetchProjects(similarToParams)
      .retry(2)
      .map(DiscoverEnvelope::projects)
      .flatMap(Observable::from);

    final Observable<Project> staffPickProjects = this.apiClient.fetchProjects(staffPickParams)
      .retry(2)
      .map(DiscoverEnvelope::projects)
      .flatMap(Observable::from);

    return Observable.concat(recommendedProjects, similarToProjects, staffPickProjects)
      .compose(neverError())
      .distinct()
      .take(3)
      .toList();
  }

  private Observable<User> signupToGamesNewsletter(final @NonNull User user) {
    return this.apiClient
      .updateUserSettings(user.toBuilder().gamesNewsletter(true).build())
      .compose(neverError());
  }
}
