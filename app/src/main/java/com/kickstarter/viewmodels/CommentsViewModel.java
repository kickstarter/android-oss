package com.kickstarter.viewmodels;

import android.content.SharedPreferences;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Either;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.EventContextValues;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.extensions.ProjectDataExtKt;
import com.kickstarter.libs.utils.extensions.StringExt;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.CommentsActivity;
import com.kickstarter.ui.adapters.data.CommentsData;
import com.kickstarter.ui.data.ProjectData;

import java.net.CookieManager;
import java.util.List;

import androidx.annotation.NonNull;
import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.errors;
import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.values;

public interface CommentsViewModel {

  interface Inputs {
    /** Call when the comment body changes. */
    void commentBodyChanged(String __);

    /** Call when the comment button is clicked. */
    void commentButtonClicked();

    /** Call when the comment dialog should be dismissed. */
    void commentDialogDismissed();

    /** Call when returning to activity with login success. */
    void loginSuccess();

    /** Invoke when pagination should happen. */
    void nextPage();

    /** Call when the post comment button is clicked. */
    void postCommentClicked();

    /** Invoke when the feed should be refreshed. */
    void refresh();
  }

  interface Outputs {
    /** Emits a boolean that determines if the comment button should be hidden. */
    Observable<Boolean> commentButtonHidden();

    /** Emits data to display comments. */
    Observable<CommentsData> commentsData();

    /** Emits the string that should be displayed in the comment dialog when it is shown. */
    Observable<String> currentCommentBody();

    /** Emits when the comment dialog should be dismissed. */
    Observable<Void> dismissCommentDialog();

    /** Emits a boolean indicating when the post button should be enabled. */
    Observable<Boolean> enablePostButton();

    /** Emits a boolean indicating whether comments are being fetched from the API. */
    Observable<Boolean> isFetchingComments();

    /** Emits a project and boolean to determine when the comment dialog should be shown. */
    Observable<Pair<Project, Boolean>> showCommentDialog();

    /** Emits when comment posted toast message should be displayed. */
    Observable<Void> showCommentPostedToast();

    /** Emits when we should display a post comment error toast. */
    Observable<String> showPostCommentErrorToast();
  }

  final class ViewModel extends ActivityViewModel<CommentsActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CookieManager cookieManager;
    private final CurrentUserType currentUser;
    private final SharedPreferences sharedPreferences;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.cookieManager = environment.cookieManager();
      this.currentUser = environment.currentUser();
      this.sharedPreferences = environment.sharedPreferences();

      final Observable<User> currentUser = Observable.merge(
        this.currentUser.observable(),
        this.loginSuccess.flatMap(__ -> this.client.fetchCurrentUser().compose(neverError())).share()
      );

      final Observable<Either<Project, Update>> projectOrUpdate = intent()
        .take(1)
        .map(i -> {
          final Project project = i.getParcelableExtra(IntentKey.PROJECT);
          return project != null
            ? new Either.Left<Project, Update>(project)
            : new Either.Right<Project, Update>(i.getParcelableExtra(IntentKey.UPDATE));
        })
        .filter(ObjectUtils::isNotNull);

      final Observable<ProjectData> projectData = intent()
              .map(i -> i.getParcelableExtra(IntentKey.PROJECT_DATA))
              .ofType(ProjectData.class)
              .take(1);

      projectData
        .map(it -> ProjectDataExtKt.storeCurrentCookieRefTag(it, this.cookieManager, this.sharedPreferences))
        .compose(bindToLifecycle())
        .subscribe(
          projectAndData -> this.lake.trackProjectScreenViewed(projectAndData, EventContextValues.ContextSectionName.COMMENTS.getContextName())
        );

      final Observable<Project> initialProject = projectOrUpdate
        .flatMap(pOrU ->
          pOrU.either(
            Observable::just,
            u -> this.client.fetchProject(String.valueOf(u.projectId())).compose(neverError())
          )
        )
        .share();

      final Observable<Project> project = Observable.merge(
        initialProject,
        initialProject
          .compose(takeWhen(this.loginSuccess))
          .flatMap(p -> this.client.fetchProject(p).compose(neverError()))
      )
        .share();

      final Observable<Boolean> commentHasBody = this.commentBodyChanged
        .map(it -> ObjectUtils.isNull(it) ? false : StringExt.isPresent(it));

      final Observable<Notification<Comment>> commentNotification = projectOrUpdate
        .compose(combineLatestPair(this.commentBodyChanged))
        .compose(takeWhen(this.postCommentClicked))
        .switchMap(projectOrUpdateAndBody ->
          this.postComment(projectOrUpdateAndBody.first, projectOrUpdateAndBody.second)
            .doOnSubscribe(() -> this.commentIsPosting.onNext(true))
            .doAfterTerminate(() -> this.commentIsPosting.onNext(false))
            .materialize()
        )
        .share();

      final Observable<Comment> postedComment = commentNotification
        .compose(values());

      final Observable<Either<Project, Update>> startOverWith = Observable.merge(
        projectOrUpdate,
        projectOrUpdate.compose(takeWhen(this.refresh))
      );

      final ApiPaginator<Comment, CommentsEnvelope, Either<Project, Update>> paginator =
        ApiPaginator.<Comment, CommentsEnvelope, Either<Project, Update>>builder()
          .nextPage(this.nextPage)
          .distinctUntilChanged(true)
          .startOverWith(startOverWith)
          .envelopeToListOfData(CommentsEnvelope::comments)
          .envelopeToMoreUrl(env -> env.urls().api().moreComments())
          .loadWithParams(pu -> pu.either(this.client::fetchComments, this.client::fetchComments))
          .loadWithPaginationPath(this.client::fetchComments)
          .build();

      final Observable<List<Comment>> comments = paginator.paginatedData().share();

      final Observable<Boolean> userCanComment = Observable.combineLatest(
        currentUser,
        project,
        Pair::create
      )
        .map(userAndProject -> {
          final User creator = userAndProject.second.creator();
          final boolean currentUserIsCreator = userAndProject.first != null && userAndProject.first.id() == creator.id();
          return currentUserIsCreator || userAndProject.second.isBacking();
        });

      final Observable<Project> commentableProject = Observable.combineLatest(
        project,
        userCanComment,
        Pair::create
      )
        .filter(pc -> pc.second)
        .map(pc -> pc.first);

      commentNotification
        .compose(errors())
        .map(ErrorEnvelope::fromThrowable)
        .subscribe(this.showPostCommentErrorToast::onNext);

      commentableProject
        .compose(takeWhen(this.loginSuccess))
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(p -> this.showCommentDialog.onNext(Pair.create(p, true)));

      commentableProject
        .compose(takeWhen(this.commentButtonClicked))
        .compose(bindToLifecycle())
        .subscribe(p -> this.showCommentDialog.onNext(Pair.create(p, true)));

      this.commentDialogDismissed
        .compose(bindToLifecycle())
        .subscribe(__ -> {
          this.showCommentDialog.onNext(null);
          this.dismissCommentDialog.onNext(null);
        });

      // Seed comment body with user input.
      this.commentBodyChanged
        .compose(bindToLifecycle())
        .subscribe(this.currentCommentBody::onNext);

      Observable.combineLatest(
        project,
        comments,
        currentUser,
        CommentsData::deriveData
      )
        .compose(bindToLifecycle())
        .subscribe(this.commentsData::onNext);

      userCanComment
        .map(BooleanUtils::negate)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.commentButtonHidden::onNext);

      postedComment
        .compose(ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(this.refresh::onNext);

      commentHasBody
        .compose(bindToLifecycle())
        .subscribe(this.enablePostButton::onNext);

      this.commentIsPosting
        .map(b -> !b)
        .compose(bindToLifecycle())
        .subscribe(this.enablePostButton::onNext);

      postedComment
        .compose(bindToLifecycle())
        .subscribe(__ -> {
          this.commentDialogDismissed.onNext(null);
          this.showCommentPostedToast.onNext(null);
        });

      postedComment
        .map(__ -> "")
        .compose(bindToLifecycle())
        .subscribe(this.commentBodyChanged::onNext);

      paginator.isFetching()
        .compose(bindToLifecycle())
        .subscribe(this.isFetchingComments);

      project
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(__ -> this.refresh.onNext(null));

      final Observable<Update> update = projectOrUpdate.map(Either::right);
    }

    private @NonNull Observable<Comment> postComment(final @NonNull Either<Project, Update> projectOrUpdate, final @NonNull String body) {
      return projectOrUpdate.either(
        p -> this.client.postComment(p, body),
        u -> this.client.postComment(u, body)
      );
    }

    private final PublishSubject<String> commentBodyChanged = PublishSubject.create();
    private final PublishSubject<Void> commentButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> commentDialogDismissed = PublishSubject.create();
    private final PublishSubject<Boolean> commentIsPosting = PublishSubject.create();
    private final PublishSubject<Void> loginSuccess = PublishSubject.create();
    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<Void> postCommentClicked = PublishSubject.create();
    private final PublishSubject<Void> refresh = PublishSubject.create();

    private final BehaviorSubject<Boolean> commentButtonHidden = BehaviorSubject.create();
    private final BehaviorSubject<CommentsData> commentsData = BehaviorSubject.create();
    private final BehaviorSubject<String> currentCommentBody = BehaviorSubject.create();
    private final BehaviorSubject<Void> dismissCommentDialog = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> enablePostButton = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> isFetchingComments = BehaviorSubject.create();
    private final BehaviorSubject<Pair<Project, Boolean>> showCommentDialog = BehaviorSubject.create();
    private final PublishSubject<Void> showCommentPostedToast = PublishSubject.create();
    private final PublishSubject<ErrorEnvelope> showPostCommentErrorToast = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void commentBodyChanged(final @NonNull String string) {
      this.commentBodyChanged.onNext(string);
    }
    @Override public void commentButtonClicked() {
      this.commentButtonClicked.onNext(null);
    }
    @Override public void commentDialogDismissed() {
      this.commentDialogDismissed.onNext(null);
    }
    @Override public void loginSuccess() {
      this.loginSuccess.onNext(null);
    }
    @Override public void nextPage() {
      this.nextPage.onNext(null);
    }
    @Override public void postCommentClicked() {
      this.postCommentClicked.onNext(null);
    }
    @Override public void refresh() {
      this.refresh.onNext(null);
    }

    @Override public @NonNull Observable<Boolean> commentButtonHidden() {
      return this.commentButtonHidden;
    }
    @Override public @NonNull Observable<CommentsData> commentsData() {
      return this.commentsData;
    }
    @Override public @NonNull Observable<String> currentCommentBody() {
      return this.currentCommentBody;
    }
    @Override public @NonNull Observable<Void> dismissCommentDialog() {
      return this.dismissCommentDialog;
    }
    @Override public @NonNull Observable<Boolean> enablePostButton() {
      return this.enablePostButton;
    }
    @Override public @NonNull Observable<Boolean> isFetchingComments() {
      return this.isFetchingComments;
    }
    @Override public @NonNull Observable<Pair<Project, Boolean>> showCommentDialog() {
      return this.showCommentDialog;
    }
    @Override public @NonNull Observable<Void> showCommentPostedToast() {
      return this.showCommentPostedToast;
    }
    @Override public @NonNull Observable<String> showPostCommentErrorToast() {
      return this.showPostCommentErrorToast
        .map(ErrorEnvelope::errorMessage);
    }
  }
}
