package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaContext.Update;
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

import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.incrementalCount;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

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
    Observable<List<Activity>> activityList();

    /** Emits when view should be returned to Discovery projects. */
    Observable<Void> goToDiscovery();

    /** Emits when login tout should be shown. */
    Observable<Void> goToLogin();

    /** Emits a project when it should be shown. */
    Observable<Project> goToProject();

    /** Emits a SurveyResponse when it should be shown. */
    Observable<SurveyResponse> goToSurvey();

    /** Emits a boolean indicating whether activities are being fetched from the API. */
    Observable<Boolean> isFetchingActivities();

    /** Emits a boolean that determines if a logged-out, empty state should be displayed. */
    Observable<Boolean> loggedOutEmptyStateIsVisible();

    /** Emits a logged-in user with zero activities in order to display an empty state. */
    Observable<Boolean> loggedInEmptyStateIsVisible();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.UpdateActivity}. */
    Observable<Activity> startUpdateActivity();

    /** Emits a list of unanswered surveys to be shown in the user's activity feed */
    Observable<List<SurveyResponse>> surveys();
  }

  final class ViewModel extends ActivityViewModel<ActivityFeedActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      this.goToDiscovery = this.discoverProjectsClick;
      this.goToLogin = this.loginClick;
      this.goToSurvey = this.surveyClick;

      this.goToProject = Observable.merge(
        this.friendBackingClick,
        this.projectStateChangedClick,
        this.projectStateChangedPositiveClick,
        this.projectUpdateProjectClick
      )
        .map(Activity::project);

      this.startUpdateActivity = this.projectUpdateClick;

      final Observable<Void> refreshSurvey = Observable.merge(this.refresh, this.resume).share();

      this.currentUser.loggedInUser()
        .compose(takeWhen(refreshSurvey))
        .switchMap(__ -> this.client.fetchUnansweredSurveys().compose(neverError()).share())
        .compose(bindToLifecycle())
        .subscribe(this.surveys);

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
        .subscribe(this.activityList);

      paginator.isFetching()
        .compose(this.bindToLifecycle())
        .subscribe(this.isFetchingActivities);

      this.currentUser.loggedInUser()
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(__ -> this.refresh());

      this.currentUser.isLoggedIn()
        .map(loggedIn -> !loggedIn)
        .compose(this.bindToLifecycle())
        .subscribe(this.loggedOutEmptyStateIsVisible);

      this.currentUser.observable()
        .compose(takePairWhen(this.activityList))
        .map(ua -> ua.first != null && ua.second.size() == 0)
        .compose(this.bindToLifecycle())
        .subscribe(this.loggedInEmptyStateIsVisible);

      // Track viewing and paginating activity.
      this.nextPage
        .compose(incrementalCount())
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

      this.startUpdateActivity
        .map(Activity::project)
        .filter(ObjectUtils::isNotNull)
        .compose(this.bindToLifecycle())
        .subscribe(p -> this.koala.trackViewedUpdate(p, Update.ACTIVITY));
    }

    private final PublishSubject<Void> discoverProjectsClick = PublishSubject.create();
    private final PublishSubject<Activity> friendBackingClick = PublishSubject.create();
    private final PublishSubject<Void> loginClick = PublishSubject.create();
    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<Activity> projectStateChangedClick = PublishSubject.create();
    private final PublishSubject<Activity> projectStateChangedPositiveClick = PublishSubject.create();
    private final PublishSubject<Activity> projectUpdateClick = PublishSubject.create();
    private final PublishSubject<Activity> projectUpdateProjectClick = PublishSubject.create();
    private final PublishSubject<Void> refresh = PublishSubject.create();
    private final PublishSubject<Void> resume = PublishSubject.create();
    private final PublishSubject<SurveyResponse> surveyClick = PublishSubject.create();

    private final BehaviorSubject<List<Activity>> activityList = BehaviorSubject.create();
    private final Observable<Void> goToDiscovery;
    private final Observable<Void> goToLogin;
    private final Observable<Project> goToProject;
    private final Observable<SurveyResponse> goToSurvey;
    private final BehaviorSubject<Boolean> isFetchingActivities= BehaviorSubject.create();
    private final BehaviorSubject<Boolean> loggedInEmptyStateIsVisible = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> loggedOutEmptyStateIsVisible = BehaviorSubject.create();
    private final Observable<Activity> startUpdateActivity;
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
    @Override public void refresh() {
      this.refresh.onNext(null);
    }
    @Override public void resume() {
      this.resume.onNext(null);
    }

    @Override public @NonNull Observable<List<Activity>> activityList() {
      return this.activityList;
    }
    @Override public @NonNull Observable<Void> goToDiscovery() {
      return this.goToDiscovery;
    }
    @Override public @NonNull Observable<Void> goToLogin() {
      return this.goToLogin;
    }
    @Override public @NonNull Observable<Project> goToProject() {
      return this.goToProject;
    }
    @Override public @NonNull Observable<SurveyResponse> goToSurvey() {
      return this.goToSurvey;
    }
    @Override public @NonNull Observable<Boolean> isFetchingActivities() {
      return this.isFetchingActivities;
    }
    @Override public @NonNull Observable<Boolean> loggedInEmptyStateIsVisible() {
      return this.loggedInEmptyStateIsVisible;
    }
    @Override public @NonNull Observable<Boolean> loggedOutEmptyStateIsVisible() {
      return this.loggedOutEmptyStateIsVisible;
    }
    @Override public @NonNull Observable<Activity> startUpdateActivity() {
      return this.startUpdateActivity;
    }
    @Override public @NonNull Observable<List<SurveyResponse>> surveys() {
      return this.surveys;
    }
  }
}
