package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ExperimentsClientType;
import com.kickstarter.libs.OptimizelyEvent;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.preferences.BooleanPreferenceType;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.ExperimentData;
import com.kickstarter.libs.utils.ExperimentRevenueData;
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
import com.kickstarter.ui.adapters.ThanksAdapter;
import com.kickstarter.ui.adapters.data.ThanksData;
import com.kickstarter.ui.data.CheckoutData;
import com.kickstarter.ui.data.PledgeData;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;
import com.kickstarter.ui.viewholders.ThanksCategoryViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.utils.BooleanUtils.isTrue;

public interface ThanksViewModel {

  interface Inputs extends ProjectCardViewHolder.Delegate, ThanksCategoryViewHolder.Delegate,
    ThanksAdapter.Delegate {
    /** Call when the user clicks the close button. */
    void closeButtonClicked();

    /** Call when the user accepts the prompt to signup to the Games newsletter. */
    void signupToGamesNewsletterClick();
  }

  interface Outputs {
    /** Emits the data to configure the adapter with. */
    Observable<ThanksData> adapterData();

    /** Emits when we should finish the {@link com.kickstarter.ui.activities.ThanksActivity}. */
    Observable<Void> finish();

    /** Show a dialog confirming the user will be signed up to the games newsletter. Required for German users. */
    Observable<Void> showConfirmGamesNewsletterDialog();

    /** Show a dialog prompting the user to sign-up to the games newsletter. */
    Observable<Void> showGamesNewsletterDialog();

    /** Show a dialog prompting the user to rate the app. */
    Observable<Void> showRatingDialog();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.DiscoveryActivity}. */
    Observable<DiscoveryParams> startDiscoveryActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}. */
    Observable<Pair<Project, RefTag>> startProjectActivity();
  }

  final class ViewModel extends ActivityViewModel<ThanksActivity> implements Inputs, Outputs {
    private final ApiClientType apiClient;
    private final BooleanPreferenceType hasSeenAppRatingPreference;
    private final BooleanPreferenceType hasSeenGamesNewsletterPreference;
    private final CurrentUserType currentUser;
    private final ExperimentsClientType optimizely;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.apiClient = environment.apiClient();
      this.currentUser = environment.currentUser();
      this.hasSeenAppRatingPreference = environment.hasSeenAppRatingPreference();
      this.hasSeenGamesNewsletterPreference = environment.hasSeenGamesNewsletterPreference();
      this.optimizely = environment.optimizely();

      final Observable<Project> project = intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
        .ofType(Project.class)
        .take(1)
        .compose(bindToLifecycle());

      final Observable<Category> rootCategory = project.flatMap(p -> rootCategory(p, this.apiClient));

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

      this.categoryCardViewHolderClicked
        .map(c -> DiscoveryParams.builder().category(c).build())
        .compose(bindToLifecycle())
        .subscribe(this.startDiscoveryActivity::onNext);

      this.closeButtonClicked
        .compose(bindToLifecycle())
        .subscribe(this.finish);

      this.projectCardViewHolderClicked
        .compose(bindToLifecycle())
        .subscribe(p -> this.startProjectActivity.onNext(Pair.create(p, RefTag.thanks())));

      Observable.combineLatest(
        project,
        rootCategory,
        project.flatMap(p -> this.relatedProjects(p, this.apiClient)),
        ThanksData::new
      )
        .compose(bindToLifecycle())
        .subscribe(this.adapterData::onNext);

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
        .flatMap(u -> this.signupToGamesNewsletter(u, this.apiClient))
        .compose(bindToLifecycle())
        .subscribe(this.signedUpToGamesNewsletter::onNext);

      this.currentUser.observable()
        .filter(ObjectUtils::isNotNull)
        .compose(takeWhen(this.signedUpToGamesNewsletter))
        .filter(UserUtils::isLocationGermany)
        .compose(bindToLifecycle())
        .subscribe(__ -> this.showConfirmGamesNewsletterDialog.onNext(null));

      // Event tracking
      this.categoryCardViewHolderClicked
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackCheckoutFinishJumpToDiscovery());

      this.projectCardViewHolderClicked
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackCheckoutFinishJumpToProject);

      this.signedUpToGamesNewsletter
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackNewsletterToggle(true));

      final Observable<CheckoutData> checkoutData = intent()
        .map(i -> i.getParcelableExtra(IntentKey.CHECKOUT_DATA))
        .ofType(CheckoutData.class)
        .take(1);

      final Observable<PledgeData> pledgeData = intent()
        .map(i -> i.getParcelableExtra(IntentKey.PLEDGE_DATA))
        .ofType(PledgeData.class)
        .take(1);

      final Observable<Pair<CheckoutData, PledgeData>> checkoutAndPledgeData =
        Observable.combineLatest(checkoutData, pledgeData, Pair::create);

      checkoutAndPledgeData
        .compose(bindToLifecycle())
        .subscribe(checkoutDataPledgeData -> this.lake.trackThanksPageViewed(checkoutDataPledgeData.first, checkoutDataPledgeData.second));

      checkoutAndPledgeData
        .compose(combineLatestPair(this.currentUser.observable()))
        .map(this::experimentRevenueData)
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(data -> this.optimizely.trackRevenue(OptimizelyEvent.APP_COMPLETED_CHECKOUT, data));
    }

    private ExperimentRevenueData experimentRevenueData(final @NonNull Pair<Pair<CheckoutData, PledgeData>, User> dataAndUser) {
      final User currentUser = dataAndUser.second;
      final PledgeData pledgeData = dataAndUser.first.second;
      final RefTag intentRefTag = pledgeData.projectData().refTagFromIntent();
      final RefTag cookieRefTag = pledgeData.projectData().refTagFromCookie();
      final ExperimentData experimentData = new ExperimentData(currentUser, intentRefTag, cookieRefTag);
      final CheckoutData checkoutData = dataAndUser.first.first;
      return new ExperimentRevenueData(experimentData, checkoutData, pledgeData);
    }

    /**
     * Given a project, returns an observable that emits the project's root category.
     */
    private static @NonNull Observable<Category> rootCategory(final @NonNull Project project, final @NonNull ApiClientType client) {
      final Category category = project.category();

      if (category == null) {
        return Observable.empty();
      }

      if (category.parent() != null) {
        return Observable.just(category.parent());
      }

      return client.fetchCategory(String.valueOf(category.rootId()))
        .compose(neverError());
    }

    /**
     * Returns a shuffled list of 3 recommended projects, with fallbacks to similar and staff picked projects
     * for users with fewer than 3 recommendations.
     */
    private @NonNull Observable<List<Project>> relatedProjects(final @NonNull Project project, final @NonNull ApiClientType client) {
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

      final Observable<Project> recommendedProjects = client.fetchProjects(recommendedParams)
        .retry(2)
        .map(DiscoverEnvelope::projects)
        .map(ListUtils::shuffle)
        .flatMap(Observable::from)
        .take(3);

      final Observable<Project> similarToProjects = client.fetchProjects(similarToParams)
        .retry(2)
        .map(DiscoverEnvelope::projects)
        .flatMap(Observable::from);

      final Observable<Project> staffPickProjects = client.fetchProjects(staffPickParams)
        .retry(2)
        .map(DiscoverEnvelope::projects)
        .flatMap(Observable::from);

      return Observable.concat(recommendedProjects, similarToProjects, staffPickProjects)
        .compose(neverError())
        .distinct()
        .take(3)
        .toList();
    }

    private Observable<User> signupToGamesNewsletter(final @NonNull User user, final @NonNull ApiClientType client) {
      return client
        .updateUserSettings(user.toBuilder().gamesNewsletter(true).build())
        .compose(neverError());
    }

    private final PublishSubject<Category> categoryCardViewHolderClicked = PublishSubject.create();
    private final PublishSubject<Void> closeButtonClicked = PublishSubject.create();
    private final PublishSubject<Project> projectCardViewHolderClicked  = PublishSubject.create();
    private final PublishSubject<Void> signupToGamesNewsletterClick = PublishSubject.create();

    private final BehaviorSubject<ThanksData> adapterData = BehaviorSubject.create();
    private final PublishSubject<Void> finish = PublishSubject.create();
    private final PublishSubject<Void> showConfirmGamesNewsletterDialog = PublishSubject.create();
    private final PublishSubject<Void> showGamesNewsletterDialog = PublishSubject.create();
    private final PublishSubject<Void> showRatingDialog = PublishSubject.create();
    private final PublishSubject<User> signedUpToGamesNewsletter = PublishSubject.create();
    private final PublishSubject<DiscoveryParams> startDiscoveryActivity = PublishSubject.create();
    private final PublishSubject<Pair<Project, RefTag>> startProjectActivity = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void categoryViewHolderClicked(final @NonNull Category category) {
      this.categoryCardViewHolderClicked.onNext(category);
    }
    @Override public void closeButtonClicked() {
      this.closeButtonClicked.onNext(null);
    }
    @Override public void signupToGamesNewsletterClick() {
      this.signupToGamesNewsletterClick.onNext(null);
    }
    @Override public void projectCardViewHolderClicked(final @NonNull Project project) {
      this.projectCardViewHolderClicked.onNext(project);
    }

    @Override public @NonNull Observable<ThanksData> adapterData() {
      return this.adapterData;
    }
    @Override public @NonNull Observable<Void> finish() {
      return this.finish;
    }
    @Override public @NonNull Observable<Void> showConfirmGamesNewsletterDialog() {
      return this.showConfirmGamesNewsletterDialog;
    }
    @Override public @NonNull Observable<Void> showGamesNewsletterDialog() {
      return this.showGamesNewsletterDialog;
    }
    @Override public @NonNull Observable<Void> showRatingDialog() {
      return this.showRatingDialog;
    }
    @Override public @NonNull Observable<DiscoveryParams> startDiscoveryActivity() {
      return this.startDiscoveryActivity;
    }
    @Override public @NonNull Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
  }
}
