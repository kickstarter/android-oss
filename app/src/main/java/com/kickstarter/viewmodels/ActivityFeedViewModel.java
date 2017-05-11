package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.FeatureKey;
import com.kickstarter.libs.KoalaContext.Update;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.adapters.ActivityFeedAdapter;
import com.kickstarter.ui.viewholders.EmptyActivityFeedViewHolder;
import com.kickstarter.ui.viewholders.FriendBackingViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedPositiveViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedViewHolder;
import com.kickstarter.ui.viewholders.ProjectUpdateViewHolder;
import com.kickstarter.ui.viewholders.UnansweredSurveyViewHolder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.utils.ObjectUtils.coalesce;

public interface ActivityFeedViewModel {

  interface Inputs extends ActivityFeedAdapter.Delegate {
    /** Invoke when pagination should happen. */
    void nextPage();

    /** Invoke when activity's onResume runs */
    void resume();

    /** Invoke when the feed should be refreshed. */
    void refresh();
  }

  interface Outputs {
    /** Emits a list of activities representing the user's activity feed. */
    Observable<List<Activity>> activities();

    /** Emits when view should be returned to Discovery projects. */
    Observable<Void> goToDiscovery();

    /** Emits when login tout should be shown. */
    Observable<Void> goToLogin();

    /** Emits a project when it should be shown. */
    Observable<Project> goToProject();

    /** Emits an activity when project update should be shown. */
    Observable<Activity> goToProjectUpdate();

    /** Emits a SurveyResponse when it should be shown. */
    Observable<SurveyResponse> goToSurvey();

    /** Emits a boolean indicating whether activities are being fetched from the API. */
    Observable<Boolean> isFetchingActivities();

    /** Emits a boolean that determines if a logged-out, empty state should be displayed. */
    Observable<Boolean> loggedOutEmptyStateIsVisible();

    /** Emits a logged-in user with zero activities in order to display an empty state. */
    Observable<Boolean> loggedInEmptyStateIsVisible();

    /** Emits a list of unanswered surveys to be shown in the user's activity feed */
    Observable<List<SurveyResponse>> surveys();
  }

  final class ViewModel extends ActivityViewModel<ActivityFeedActivity> implements
    Inputs, Outputs {

    private final ApiClientType client;
    private final CurrentConfigType currentConfig;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentConfig = environment.currentConfig();
      this.currentUser = environment.currentUser();

      this.goToDiscovery = this.discoverProjectsClick;
      this.goToLogin = this.loginClick;
      this.goToProjectUpdate = this.projectUpdateClick;
      this.goToSurvey = this.surveyClick;
      this.goToProject = Observable.merge(
        this.friendBackingClick,
        this.projectStateChangedClick,
        this.projectStateChangedPositiveClick,
        this.projectUpdateProjectClick
      )
        .map(Activity::project);

      final Observable<Boolean> surveyFeatureEnabled = this.currentConfig.observable()
        .map(config -> coalesce(config.features().get(FeatureKey.ANDROID_SURVEYS), false));

      Observable.combineLatest(
          resume,
          this.currentUser.isLoggedIn(),
          Pair::create
        )
        .map(resumedAndLoggedIn -> resumedAndLoggedIn.second)
        .filter(loggedIn -> loggedIn)
        .compose(Transformers.combineLatestPair(surveyFeatureEnabled))
        .switchMap(loggedInAndEnabled ->
          loggedInAndEnabled.second
            ? this.client.fetchUnansweredSurveys()
            : Observable.just(new ArrayList<SurveyResponse>())
        )
        .compose(this.bindToLifecycle())
        .subscribe(this.surveys::onNext);

      final ApiPaginator<Activity, ActivityEnvelope, Void> paginator = ApiPaginator.<Activity, ActivityEnvelope, Void>builder()
        .nextPage(this.nextPage)
        .startOverWith(this.refresh)
        .envelopeToListOfData(ActivityEnvelope::activities)
        .envelopeToMoreUrl(env -> env.urls().api().moreActivities())
        .loadWithParams(__ -> this.client.fetchActivities())
        .loadWithPaginationPath(this.client::fetchActivitiesWithPaginationPath)
        .build();

      paginator.paginatedData()
        .compose(this.bindToLifecycle())
        .subscribe(this.activities);

      paginator.isFetching()
        .compose(this.bindToLifecycle())
        .subscribe(this.isFetchingActivities);

      this.currentUser.loggedInUser()
        .take(1)
        .compose(this.bindToLifecycle())
        .subscribe(__ -> this.refresh());

      this.currentUser.isLoggedIn()
        .map(loggedIn -> !loggedIn)
        .compose(this.bindToLifecycle())
        .subscribe(this.loggedOutEmptyStateIsVisible);

      this.currentUser.observable()
        .compose(Transformers.takePairWhen(this.activities))
        .map(ua -> ua.first != null && ua.second.size() == 0)
        .compose(this.bindToLifecycle())
        .subscribe(this.loggedInEmptyStateIsVisible);

      // Track viewing and paginating activity.
      this.nextPage
        .compose(Transformers.incrementalCount())
        .startWith(0)
        .compose(this.bindToLifecycle())
        .subscribe(this.koala::trackActivityView);

      // Track tapping on any of the activity items.
      Observable.merge(
        this.friendBackingClick,
        this.projectStateChangedPositiveClick,
        this.projectStateChangedClick,
        this.projectUpdateProjectClick
      )
        .compose(this.bindToLifecycle())
        .subscribe(this.koala::trackActivityTapped);

      this.goToProjectUpdate
        .map(Activity::project)
        .filter(ObjectUtils::isNotNull)
        .compose(this.bindToLifecycle())
        .subscribe(p -> this.koala.trackViewedUpdate(p, Update.ACTIVITY));
    }

    private final PublishSubject<Void> discoverProjectsClick = PublishSubject.create();
    private final PublishSubject<Activity> friendBackingClick = PublishSubject.create();
    private final PublishSubject<Void> loginClick = PublishSubject.create();
    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<Void> resume = PublishSubject.create();
    private final PublishSubject<Activity> projectStateChangedClick = PublishSubject.create();
    private final PublishSubject<Activity> projectStateChangedPositiveClick = PublishSubject.create();
    private final PublishSubject<Activity> projectUpdateClick = PublishSubject.create();
    private final PublishSubject<Activity> projectUpdateProjectClick = PublishSubject.create();
    private final PublishSubject<Void> refresh = PublishSubject.create();
    private final PublishSubject<SurveyResponse> surveyClick = PublishSubject.create();

    private final BehaviorSubject<List<Activity>> activities = BehaviorSubject.create();
    private final Observable<Void> goToDiscovery;
    private final Observable<Void> goToLogin;
    private final Observable<Project> goToProject;
    private final Observable<Activity> goToProjectUpdate;
    private final Observable<SurveyResponse> goToSurvey;
    private final BehaviorSubject<Boolean> isFetchingActivities= BehaviorSubject.create();
    private final BehaviorSubject<Boolean> loggedInEmptyStateIsVisible = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> loggedOutEmptyStateIsVisible = BehaviorSubject.create();
    private final BehaviorSubject<List<SurveyResponse>> surveys = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void emptyActivityFeedDiscoverProjectsClicked(final @NonNull EmptyActivityFeedViewHolder viewHolder) {
      this.discoverProjectsClick.onNext(null);
    }

    @Override public void emptyActivityFeedLoginClicked(final @NonNull EmptyActivityFeedViewHolder viewHolder) {
      this.loginClick.onNext(null);
    }

    @Override public void friendBackingClicked(final @NonNull FriendBackingViewHolder viewHolder, final @NonNull Activity activity) {
      this.friendBackingClick.onNext(activity);
    }

    @Override public void nextPage() {
      this.nextPage.onNext(null);
    }

    @Override public void projectStateChangedClicked(final @NonNull ProjectStateChangedViewHolder viewHolder,
      final @NonNull Activity activity) {
      this.projectStateChangedClick.onNext(activity);
    }

    @Override public void projectStateChangedPositiveClicked(final @NonNull ProjectStateChangedPositiveViewHolder viewHolder,
      final @NonNull Activity activity) {
      this.projectStateChangedPositiveClick.onNext(activity);
    }

    @Override public void projectUpdateClicked(final @NonNull ProjectUpdateViewHolder viewHolder,
      final @NonNull Activity activity) {
      this.projectUpdateClick.onNext(activity);
    }

    @Override public void projectUpdateProjectClicked(final @NonNull ProjectUpdateViewHolder viewHolder,
      final @NonNull Activity activity) {
      this.projectUpdateProjectClick.onNext(activity);
    }

    @Override public void surveyClicked(final @NonNull UnansweredSurveyViewHolder viewHolder, final @NonNull SurveyResponse surveyResponse) {
      this.surveyClick.onNext(surveyResponse);
    }

    @Override public void refresh() {
      this.refresh.onNext(null);
    }

    @Override public void resume() {
      this.resume.onNext(null);
    }

    @Override @NonNull public Observable<List<Activity>> activities() {
      return this.activities;
    }

    @Override @NonNull public Observable<Void> goToDiscovery() {
      return this.goToDiscovery;
    }

    @Override @NonNull public Observable<Void> goToLogin() {
      return this.goToLogin;
    }

    @Override @NonNull public Observable<Project> goToProject() {
      return this.goToProject;
    }

    @Override @NonNull public Observable<Activity> goToProjectUpdate() {
      return this.goToProjectUpdate;
    }

    @Override @NonNull public Observable<SurveyResponse> goToSurvey() {
      return this.goToSurvey;
    }

    @Override @NonNull  public Observable<Boolean> isFetchingActivities() {
      return this.isFetchingActivities;
    }

    @Override @NonNull  public Observable<Boolean> loggedInEmptyStateIsVisible() {
      return this.loggedInEmptyStateIsVisible;
    }

    @NonNull
    @Override public Observable<Boolean> loggedOutEmptyStateIsVisible() {
      return this.loggedOutEmptyStateIsVisible;
    }

    @Override public Observable<List<SurveyResponse>> surveys() {
      return this.surveys;
    }
  }
}
