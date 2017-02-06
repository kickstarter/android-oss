package com.kickstarter.viewmodels;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.RefTagUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;
import com.kickstarter.ui.activities.ProjectActivity;
import com.kickstarter.ui.adapters.ProjectAdapter;
import com.kickstarter.ui.intentmappers.IntentMapper;
import com.kickstarter.ui.intentmappers.ProjectIntentMapper;
import com.kickstarter.ui.viewholders.ProjectViewHolder;

import java.net.CookieManager;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface Project {

  interface Inputs {
    /** Call when the star button is clicked. */
    void starClicked();

    /** Call when the back project button is clicked. */
    void backProjectClicked();

    /** Call when the share button is clicked. */
    void shareClicked();

    /** Call when the blurb is clicked. */
    void blurbClicked();

    /** Call when the comments button is clicked. */
    void commentsClicked();

    /** Call when the creator name is clicked. */
    void creatorNameClicked();

    /** Call when the manage pledge button is clicked. */
    void managePledgeClicked();

    /** Call when the updates button is clicked. */
    void updatesClicked();

    /** Call when the play video button is clicked. */
    void playVideoClicked();

    /** Call when the view pledge button is clicked. */
    void viewPledgeClicked();
  }

  interface Outputs {
    /** Emits a project and country when a new value is available. If the view model is created with a full project
     * model, this observable will emit that project immediately, and then again when it has updated from the api. */
    Observable<Pair<com.kickstarter.models.Project, String>> projectAndUserCountry();

    /** Emits when the success prompt for starring should be displayed. */
    Observable<Void> showStarredPrompt();

    /** Emits when a login prompt should be displayed. */
    Observable<Void> showLoginTout();

    /** Emits when we should show the share sheet. */
    Observable<com.kickstarter.models.Project> showShareSheet();

    /** Emits when we should play the video. */
    Observable<com.kickstarter.models.Project> playVideo();

    /** Emits when we should start the campaign {@link com.kickstarter.ui.activities.WebViewActivity}. */
    Observable<com.kickstarter.models.Project> startCampaignWebViewActivity();

    /** Emits when we should start the creator bio {@link com.kickstarter.ui.activities.WebViewActivity}. */
    Observable<com.kickstarter.models.Project> startCreatorBioWebViewActivity();

    /** Emits when we should start the `ProjectUpdatesActivity.` */
    Observable<com.kickstarter.models.Project> startProjectUpdatesActivity();

    /** Emits when we should start {@link com.kickstarter.ui.activities.CommentsActivity}. */
    Observable<com.kickstarter.models.Project> startCommentsActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.CheckoutActivity}. */
    Observable<com.kickstarter.models.Project> startCheckout();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.CheckoutActivity} to manage the plege. */
    Observable<com.kickstarter.models.Project> startManagePledge();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ViewPledgeActivity}. */
    Observable<com.kickstarter.models.Project> startViewPledge();
  }

  final class ViewModel extends ActivityViewModel<ProjectActivity> implements ProjectAdapter.Delegate, Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;
    private final CookieManager cookieManager;
    private final CurrentConfigType currentConfig;
    private final SharedPreferences sharedPreferences;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.cookieManager = environment.cookieManager();
      this.currentConfig = environment.currentConfig();
      this.currentUser = environment.currentUser();
      this.sharedPreferences = environment.sharedPreferences();

      // An observable of the ref tag stored in the cookie for the project. Can emit `null`.
      final Observable<RefTag> cookieRefTag = this.project
        .take(1)
        .map(p -> RefTagUtils.storedCookieRefTagForProject(p, this.cookieManager, this.sharedPreferences));

      final Observable<com.kickstarter.models.Project> initialProject = intent()
        .flatMap(i -> ProjectIntentMapper.project(i, this.client))
        .share();

      final Observable<RefTag> refTag = intent()
        .flatMap(ProjectIntentMapper::refTag);

      final Observable<PushNotificationEnvelope> pushNotificationEnvelope = intent()
        .flatMap(ProjectIntentMapper::pushNotificationEnvelope);

      final Observable<User> loggedInUserOnStarClick = this.currentUser.observable()
        .compose(takeWhen(this.starClicked))
        .filter(u -> u != null);

      final Observable<User> loggedOutUserOnStarClick = this.currentUser.observable()
        .compose(takeWhen(this.starClicked))
        .filter(u -> u == null);

      final Observable<com.kickstarter.models.Project> projectOnUserChangeStar = initialProject
        .compose(takeWhen(loggedInUserOnStarClick))
        .switchMap(this::toggleProjectStar)
        .share();

      final Observable<com.kickstarter.models.Project> starredProjectOnLoginSuccess = this.showLoginTout
        .compose(combineLatestPair(this.currentUser.observable()))
        .filter(su -> su.second != null)
        .withLatestFrom(initialProject, (__, p) -> p)
        .take(1)
        .switchMap(this::starProject)
        .share();

      initialProject
        .mergeWith(projectOnUserChangeStar)
        .mergeWith(starredProjectOnLoginSuccess)
        .compose(bindToLifecycle())
        .subscribe(this.project::onNext);

      projectOnUserChangeStar.mergeWith(starredProjectOnLoginSuccess)
        .filter(com.kickstarter.models.Project::isStarred)
        .filter(com.kickstarter.models.Project::isLive)
        .filter(p -> !p.isApproachingDeadline())
        .compose(bindToLifecycle())
        .subscribe(__ -> this.showStarredPrompt.onNext(null));

      loggedOutUserOnStarClick
        .compose(bindToLifecycle())
        .subscribe(__ -> this.showLoginTout.onNext(null));

      this.shareClicked
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackShowProjectShareSheet());

      this.playVideoClicked
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackVideoStart(this.project.getValue()));

      projectOnUserChangeStar
        .mergeWith(starredProjectOnLoginSuccess)
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackProjectStar);

      Observable.combineLatest(refTag, cookieRefTag, this.project, Project.ViewModel.RefTagsAndProject::new)
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(data -> {
          // If a cookie hasn't been set for this ref+project then do so.
          if (data.refTagFromCookie == null && data.refTagFromIntent != null) {
            RefTagUtils.storeCookie(data.refTagFromIntent, data.project, this.cookieManager, this.sharedPreferences);
          }

          this.koala.trackProjectShow(
            data.project,
            data.refTagFromIntent,
            RefTagUtils.storedCookieRefTagForProject(data.project, this.cookieManager, this.sharedPreferences)
          );
        });

      pushNotificationEnvelope
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackPushNotification);

      intent()
        .filter(IntentMapper::appBannerIsSet)
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackOpenedAppBanner());
    }

    /**
     * A light-weight value to hold two ref tags and a project. Two ref tags are stored: one comes from parceled
     * data in the activity and the other comes from the ref stored in a cookie associated to the project.
     */
    private final class RefTagsAndProject {
      private final @Nullable RefTag refTagFromIntent;
      private final @Nullable RefTag refTagFromCookie;
      private final @NonNull com.kickstarter.models.Project project;

      private RefTagsAndProject(final @Nullable RefTag refTagFromIntent, final @Nullable RefTag refTagFromCookie,
        final @NonNull com.kickstarter.models.Project project) {
        this.refTagFromIntent = refTagFromIntent;
        this.refTagFromCookie = refTagFromCookie;
        this.project = project;
      }

      public @NonNull com.kickstarter.models.Project project() {
        return this.project;
      }
    }

    public @NonNull Observable<com.kickstarter.models.Project> starProject(final @NonNull com.kickstarter.models.Project project) {
      return this.client.starProject(project)
        .compose(neverError());
    }

    public @NonNull Observable<com.kickstarter.models.Project> toggleProjectStar(final @NonNull com.kickstarter.models.Project project) {
      return this.client.toggleProjectStar(project)
        .compose(neverError());
    }

    private final PublishSubject<Void> backProjectClicked = PublishSubject.create();
    private final PublishSubject<Void> shareClicked = PublishSubject.create();
    private final PublishSubject<Void> blurbClicked = PublishSubject.create();
    private final PublishSubject<Void> commentsClicked = PublishSubject.create();
    private final PublishSubject<Void> creatorNameClicked = PublishSubject.create();
    private final PublishSubject<Void> managePledgeClicked = PublishSubject.create();
    private final PublishSubject<Void> updatesClicked = PublishSubject.create();
    private final PublishSubject<Void> playVideoClicked = PublishSubject.create();
    private final PublishSubject<Void> viewPledgeClicked = PublishSubject.create();
    private final PublishSubject<Void> starClicked = PublishSubject.create();

    private final BehaviorSubject<com.kickstarter.models.Project> project = BehaviorSubject.create();
    private final PublishSubject<Void> showLoginTout = PublishSubject.create();
    private final PublishSubject<Void> showStarredPrompt = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void backProjectClicked() {
      this.backProjectClicked.onNext(null);
    }
    @Override public void blurbClicked() {
      this.blurbClicked.onNext(null);
    }
    @Override public void commentsClicked() {
      this.commentsClicked.onNext(null);
    }
    @Override public void creatorNameClicked() {
      this.creatorNameClicked.onNext(null);
    }
    @Override public void managePledgeClicked() {
      this.managePledgeClicked.onNext(null);
    }
    @Override public void playVideoClicked() {
      this.playVideoClicked.onNext(null);
    }
    @Override public void projectViewHolderBackProjectClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.backProjectClicked();
    }
    @Override public void projectViewHolderBlurbClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.blurbClicked();
    }
    @Override public void projectViewHolderCommentsClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.commentsClicked();
    }
    @Override public void projectViewHolderCreatorClicked(final @NonNull ProjectViewHolder viewHolder){
      this.creatorNameClicked();
    }
    @Override public void projectViewHolderManagePledgeClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.managePledgeClicked();
    }
    @Override public void projectViewHolderVideoStarted(final @NonNull ProjectViewHolder viewHolder) {
      this.playVideoClicked();
    }
    @Override public void projectViewHolderViewPledgeClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.viewPledgeClicked();
    }
    @Override public void projectViewHolderUpdatesClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.updatesClicked();
    }
    @Override public void shareClicked() {
      this.shareClicked.onNext(null);
    }
    @Override public void starClicked() {
      this.starClicked.onNext(null);
    }
    @Override public void updatesClicked() {
      this.updatesClicked.onNext(null);
    }
    @Override public void viewPledgeClicked() {
      this.viewPledgeClicked.onNext(null);
    }

    @Override public @NonNull Observable<com.kickstarter.models.Project> playVideo() {
      return this.project.compose(takeWhen(this.playVideoClicked));
    }
    @Override public @NonNull Observable<Pair<com.kickstarter.models.Project, String>> projectAndUserCountry() {
      return this.project.compose(combineLatestPair(this.currentConfig.observable().map(Config::countryCode)));
    }
    @Override public @NonNull Observable<com.kickstarter.models.Project> startCampaignWebViewActivity() {
      return this.project.compose(takeWhen(this.blurbClicked));
    }
    @Override public @NonNull Observable<com.kickstarter.models.Project> startCreatorBioWebViewActivity() {
      return this.project.compose(takeWhen(this.creatorNameClicked));
    }
    @Override public @NonNull Observable<com.kickstarter.models.Project> startCommentsActivity() {
      return this.project.compose(takeWhen(this.commentsClicked));
    }
    @Override public @NonNull Observable<Void> showLoginTout() {
      return this.showLoginTout;
    }
    @Override public @NonNull Observable<com.kickstarter.models.Project> showShareSheet() {
      return this.project.compose(takeWhen(this.shareClicked));
    }
    @Override public @NonNull Observable<Void> showStarredPrompt() {
      return this.showStarredPrompt;
    }
    @Override public @NonNull Observable<com.kickstarter.models.Project> startProjectUpdatesActivity() {
      return this.project.compose(takeWhen(this.updatesClicked));
    }
    @Override public @NonNull Observable<com.kickstarter.models.Project> startCheckout() {
      return this.project.compose(takeWhen(this.backProjectClicked));
    }
    @Override public @NonNull Observable<com.kickstarter.models.Project> startManagePledge() {
      return this.project.compose(takeWhen(this.managePledgeClicked));
    }
    @Override public @NonNull Observable<com.kickstarter.models.Project> startViewPledge() {
      return this.project.compose(takeWhen(this.viewPledgeClicked));
    }
  }
}
