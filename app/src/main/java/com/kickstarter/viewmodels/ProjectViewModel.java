package com.kickstarter.viewmodels;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.RefTagUtils;
import com.kickstarter.models.Project;
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
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface ProjectViewModel {

  interface Inputs {
    /** Call when the back project button is clicked. */
    void backProjectButtonClicked();

    /** Call when the blurb view is clicked. */
    void blurbTextViewClicked();

    /** Call when the comments text view is clicked. */
    void commentsTextViewClicked();

    /** Call when the creator name is clicked. */
    void creatorNameTextViewClicked();

    /** Call when the share button is clicked. */
    void shareButtonClicked();

    /** Call when the manage pledge button is clicked. */
    void managePledgeButtonClicked();

    /** Call when the play video button is clicked. */
    void playVideoButtonClicked();

    /** Call when the star button is clicked. */
    void starButtonClicked();

    /** Call when the updates button is clicked. */
    void updatesTextViewClicked();

    /** Call when the view pledge button is clicked. */
    void viewPledgeButtonClicked();
  }

  interface Outputs {
    /** Emits a project and country when a new value is available. If the view model is created with a full project
     * model, this observable will emit that project immediately, and then again when it has updated from the api. */
    Observable<Pair<Project, String>> projectAndUserCountry();

    /** Emits when the success prompt for starring should be displayed. */
    Observable<Void> showStarredPrompt();

    /** Emits when we should start {@link com.kickstarter.ui.activities.LoginToutActivity}. */
    Observable<Void> startLoginToutActivity();

    /** Emits when we should show the share sheet. */
    Observable<Project> showShareSheet();

    /** Emits when we should play the video. */
    Observable<Project> playVideo();

    /** Emits when we should start the campaign {@link com.kickstarter.ui.activities.WebViewActivity}. */
    Observable<Project> startCampaignWebViewActivity();

    /** Emits when we should start the creator bio {@link com.kickstarter.ui.activities.WebViewActivity}. */
    Observable<Project> startCreatorBioWebViewActivity();

    /** Emits when we should start {@link com.kickstarter.ui.activities.ProjectUpdatesActivity}. */
    Observable<Project> startProjectUpdatesActivity();

    /** Emits when we should start {@link com.kickstarter.ui.activities.CommentsActivity}. */
    Observable<Project> startCommentsActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.CheckoutActivity}. */
    Observable<Project> startCheckoutActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.CheckoutActivity} to manage the pledge. */
    Observable<Project> startManagePledgeActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ViewPledgeActivity}. */
    Observable<Project> startViewPledgeActivity();
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

      final Observable<Project> initialProject = intent()
        .flatMap(i -> ProjectIntentMapper.project(i, this.client))
        .share();

      // An observable of the ref tag stored in the cookie for the project. Can emit `null`.
      final Observable<RefTag> cookieRefTag = initialProject
        .take(1)
        .map(p -> RefTagUtils.storedCookieRefTagForProject(p, this.cookieManager, this.sharedPreferences));

      final Observable<RefTag> refTag = intent()
        .flatMap(ProjectIntentMapper::refTag);

      final Observable<PushNotificationEnvelope> pushNotificationEnvelope = intent()
        .flatMap(ProjectIntentMapper::pushNotificationEnvelope);

      final Observable<User> loggedInUserOnStarClick = this.currentUser.observable()
        .compose(takeWhen(this.starButtonClicked))
        .filter(u -> u != null);

      final Observable<User> loggedOutUserOnStarClick = this.currentUser.observable()
        .compose(takeWhen(this.starButtonClicked))
        .filter(u -> u == null);

      final Observable<Project> projectOnUserChangeStar = initialProject
        .compose(takeWhen(loggedInUserOnStarClick))
        .switchMap(this::toggleProjectStar)
        .share();

      final Observable<Project> starredProjectOnLoginSuccess = this.startLoginToutActivity
        .compose(combineLatestPair(this.currentUser.observable()))
        .filter(su -> su.second != null)
        .withLatestFrom(initialProject, (__, p) -> p)
        .take(1)
        .switchMap(this::starProject)
        .share();

      final Observable<Project> currentProject = Observable.merge(
        initialProject,
        projectOnUserChangeStar,
        starredProjectOnLoginSuccess
      );

      projectOnUserChangeStar.mergeWith(starredProjectOnLoginSuccess)
        .filter(Project::isStarred)
        .filter(Project::isLive)
        .filter(p -> !p.isApproachingDeadline())
        .compose(bindToLifecycle())
        .subscribe(__ -> this.showStarredPrompt.onNext(null));

      loggedOutUserOnStarClick
        .compose(bindToLifecycle())
        .subscribe(__ -> this.startLoginToutActivity.onNext(null));

      currentProject
        .compose(combineLatestPair(this.currentConfig.observable()))
        .map(pc -> Pair.create(pc.first, pc.second.countryCode()))
        .compose(bindToLifecycle())
        .subscribe(this.projectAndUserCountry::onNext);

      this.playVideo = currentProject.compose(takeWhen(this.playVideoButtonClicked));
      this.showShareSheet = currentProject.compose(takeWhen(this.shareButtonClicked));
      this.startCampaignWebViewActivity = currentProject.compose(takeWhen(this.blurbTextViewClicked));
      this.startCheckoutActivity = currentProject.compose(takeWhen(this.backProjectButtonClicked));
      this.startCreatorBioWebViewActivity = currentProject.compose(takeWhen(this.creatorNameTextViewClicked));
      this.startCommentsActivity = currentProject.compose(takeWhen(this.commentsTextViewClicked));
      this.startManagePledgeActivity = currentProject.compose(takeWhen(this.managePledgeButtonClicked));
      this.startProjectUpdatesActivity = currentProject.compose(takeWhen(this.updatesTextViewClicked));
      this.startViewPledgeActivity = currentProject.compose(takeWhen(this.viewPledgeButtonClicked));

      this.shareButtonClicked
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackShowProjectShareSheet());

      this.playVideo
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackVideoStart);

      projectOnUserChangeStar
        .mergeWith(starredProjectOnLoginSuccess)
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackProjectStar);

      Observable.combineLatest(refTag, cookieRefTag, currentProject, ProjectViewModel.ViewModel.RefTagsAndProject::new)
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
        }
      );

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
      private final @NonNull Project project;

      private RefTagsAndProject(final @Nullable RefTag refTagFromIntent, final @Nullable RefTag refTagFromCookie,
        final @NonNull Project project) {
        this.refTagFromIntent = refTagFromIntent;
        this.refTagFromCookie = refTagFromCookie;
        this.project = project;
      }

      public @NonNull Project project() {
        return this.project;
      }
    }

    public @NonNull Observable<Project> starProject(final @NonNull Project project) {
      return this.client.starProject(project)
        .compose(neverError());
    }

    public @NonNull Observable<Project> toggleProjectStar(final @NonNull Project project) {
      return this.client.toggleProjectStar(project)
        .compose(neverError());
    }

    private final PublishSubject<Void> backProjectButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> blurbTextViewClicked = PublishSubject.create();
    private final PublishSubject<Void> commentsTextViewClicked = PublishSubject.create();
    private final PublishSubject<Void> creatorNameTextViewClicked = PublishSubject.create();
    private final PublishSubject<Void> managePledgeButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> playVideoButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> shareButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> starButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> updatesTextViewClicked = PublishSubject.create();
    private final PublishSubject<Void> viewPledgeButtonClicked = PublishSubject.create();

    private final Observable<Project> playVideo;
    private final PublishSubject<Pair<Project, String>> projectAndUserCountry = PublishSubject.create();
    private final PublishSubject<Void> startLoginToutActivity = PublishSubject.create();
    private final Observable<Project> showShareSheet;
    private final PublishSubject<Void> showStarredPrompt = PublishSubject.create();
    private final Observable<Project> startCampaignWebViewActivity;
    private final Observable<Project> startCheckoutActivity;
    private final Observable<Project> startCommentsActivity;
    private final Observable<Project> startCreatorBioWebViewActivity;
    private final Observable<Project> startManagePledgeActivity;
    private final Observable<Project> startProjectUpdatesActivity;
    private final Observable<Project> startViewPledgeActivity;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void backProjectButtonClicked() {
      this.backProjectButtonClicked.onNext(null);
    }
    @Override public void blurbTextViewClicked() {
      this.blurbTextViewClicked.onNext(null);
    }
    @Override public void commentsTextViewClicked() {
      this.commentsTextViewClicked.onNext(null);
    }
    @Override public void creatorNameTextViewClicked() {
      this.creatorNameTextViewClicked.onNext(null);
    }
    @Override public void managePledgeButtonClicked() {
      this.managePledgeButtonClicked.onNext(null);
    }
    @Override public void playVideoButtonClicked() {
      this.playVideoButtonClicked.onNext(null);
    }
    @Override public void projectViewHolderBackProjectClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.backProjectButtonClicked();
    }
    @Override public void projectViewHolderBlurbClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.blurbTextViewClicked();
    }
    @Override public void projectViewHolderCommentsClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.commentsTextViewClicked();
    }
    @Override public void projectViewHolderCreatorClicked(final @NonNull ProjectViewHolder viewHolder){
      this.creatorNameTextViewClicked();
    }
    @Override public void projectViewHolderManagePledgeClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.managePledgeButtonClicked();
    }
    @Override public void projectViewHolderVideoStarted(final @NonNull ProjectViewHolder viewHolder) {
      this.playVideoButtonClicked();
    }
    @Override public void projectViewHolderViewPledgeClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.viewPledgeButtonClicked();
    }
    @Override public void projectViewHolderUpdatesClicked(final @NonNull ProjectViewHolder viewHolder) {
      this.updatesTextViewClicked();
    }
    @Override public void shareButtonClicked() {
      this.shareButtonClicked.onNext(null);
    }
    @Override public void starButtonClicked() {
      this.starButtonClicked.onNext(null);
    }
    @Override public void updatesTextViewClicked() {
      this.updatesTextViewClicked.onNext(null);
    }
    @Override public void viewPledgeButtonClicked() {
      this.viewPledgeButtonClicked.onNext(null);
    }

    @Override public @NonNull Observable<Project> playVideo() {
      return this.playVideo;
    }
    @Override public @NonNull Observable<Pair<Project, String>> projectAndUserCountry() {
      return this.projectAndUserCountry;
    }
    @Override public @NonNull Observable<Project> startCampaignWebViewActivity() {
      return this.startCampaignWebViewActivity;
    }
    @Override public @NonNull Observable<Project> startCreatorBioWebViewActivity() {
      return this.startCreatorBioWebViewActivity;
    }
    @Override public @NonNull Observable<Project> startCommentsActivity() {
      return this.startCommentsActivity;
    }
    @Override public @NonNull Observable<Void> startLoginToutActivity() {
      return this.startLoginToutActivity;
    }
    @Override public @NonNull Observable<Project> showShareSheet() {
      return this.showShareSheet;
    }
    @Override public @NonNull Observable<Void> showStarredPrompt() {
      return this.showStarredPrompt;
    }
    @Override public @NonNull Observable<Project> startProjectUpdatesActivity() {
      return this.startProjectUpdatesActivity;
    }
    @Override public @NonNull Observable<Project> startCheckoutActivity() {
      return this.startCheckoutActivity;
    }
    @Override public @NonNull Observable<Project> startManagePledgeActivity() {
      return this.startManagePledgeActivity;
    }
    @Override public @NonNull Observable<Project> startViewPledgeActivity() {
      return this.startViewPledgeActivity;
    }
  }
}
