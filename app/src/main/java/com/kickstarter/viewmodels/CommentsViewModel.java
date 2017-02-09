package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Either;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.ApiException;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.CommentsActivity;
import com.kickstarter.ui.adapters.data.CommentsData;
import com.kickstarter.viewmodels.inputs.CommentsViewModelInputs;
import com.kickstarter.viewmodels.outputs.CommentsViewModelOutputs;

import java.util.List;

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

public final class CommentsViewModel extends ActivityViewModel<CommentsActivity> implements CommentsViewModelInputs,
  CommentsViewModelOutputs {
  private final ApiClientType client;
  private final CurrentUserType currentUser;

  public CommentsViewModel(final @NonNull Environment environment) {
    super(environment);

    this.client = environment.apiClient();
    this.currentUser = environment.currentUser();

    final Observable<Either<Project, Update>> projectOrUpdate = intent()
      .take(1)
      .map(i -> {
        final Project project = i.getParcelableExtra(IntentKey.PROJECT);
        return project != null
          ? new Either.Left<Project, Update>(project)
          : new Either.Right<Project, Update>(i.getParcelableExtra(IntentKey.UPDATE));
      });

    final Observable<Project> initialProject = projectOrUpdate
      .flatMap(pu ->
        pu.isLeft()
          ? Observable.just(pu.left())
          : client.fetchProject(String.valueOf(pu.right().projectId())).compose(neverError())
      )
      .share();

    final Observable<Project> project = initialProject
      .compose(takeWhen(loginSuccess))
      .flatMap(p -> client.fetchProject(p).compose(neverError()))
      .mergeWith(initialProject)
      .share();

    final Observable<Boolean> commentHasBody = commentBodyChanged
      .map(body -> body.length() > 0);

    final Observable<Notification<Comment>> commentNotification = projectOrUpdate
      .compose(combineLatestPair(commentBodyChanged))
      .compose(takeWhen(postCommentClicked))
      .switchMap(projectOrUpdateAndBody ->
        this.postComment(projectOrUpdateAndBody.first, projectOrUpdateAndBody.second)
          .doOnSubscribe(() -> commentIsPosting.onNext(true))
          .doAfterTerminate(() -> commentIsPosting.onNext(false))
          .materialize()
      )
      .share();

    final Observable<Comment> postedComment = commentNotification
      .compose(values())
      .ofType(Comment.class);

    final Observable<Either<Project, Update>> startOverWith = merge(
      projectOrUpdate,
      projectOrUpdate.compose(takeWhen(refresh))
    );

    final ApiPaginator<Comment, CommentsEnvelope, Either<Project, Update>> paginator =
      ApiPaginator.<Comment, CommentsEnvelope, Either<Project, Update>>builder()
        .nextPage(nextPage)
        .startOverWith(startOverWith)
        .envelopeToListOfData(CommentsEnvelope::comments)
        .envelopeToMoreUrl(env -> env.urls().api().moreComments())
        .loadWithParams(pu ->
          pu.isLeft()
            ? client.fetchComments(pu.left())
            : client.fetchComments(pu.right())
        )
        .loadWithPaginationPath(client::fetchComments)
        .build();

    final Observable<List<Comment>> comments = paginator.paginatedData().share();

    commentNotification
      .compose(errors())
      .ofType(ApiException.class)
      .subscribe(e -> showPostCommentErrorToast.onNext(e.errorEnvelope()));

    project
      .compose(takeWhen(loginSuccess))
      .filter(Project::isBacking)
      .take(1)
      .compose(bindToLifecycle())
      .subscribe(p -> showCommentDialog.onNext(Pair.create(p, true)));

    project
      .compose(takeWhen(commentButtonClicked))
      .filter(Project::isBacking)
      .compose(bindToLifecycle())
      .subscribe(p -> showCommentDialog.onNext(Pair.create(p, true)));

    commentDialogDismissed
      .compose(bindToLifecycle())
      .subscribe(__ -> {
        showCommentDialog.onNext(null);
        dismissCommentDialog.onNext(null);
      });

    // Seed comment body with user input.
    commentBodyChanged
      .compose(bindToLifecycle())
      .subscribe(currentCommentBody::onNext);

    combineLatest(
      project,
      comments,
      currentUser.observable(),
      CommentsData::deriveData
    )
      .compose(bindToLifecycle())
      .subscribe(commentsData::onNext);

    project
      .map(Project::isBacking)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(showCommentButton::onNext);

    postedComment
      .compose(ignoreValues())
      .compose(bindToLifecycle())
      .subscribe(refresh::onNext);

    commentHasBody
      .compose(bindToLifecycle())
      .subscribe(enablePostButton::onNext);

    commentIsPosting
      .map(b -> !b)
      .compose(bindToLifecycle())
      .subscribe(enablePostButton::onNext);

    initialProject
      .compose(takeWhen(postedComment))
      .compose(bindToLifecycle())
      .subscribe(koala::trackProjectCommentCreate);

    postedComment
      .compose(bindToLifecycle())
      .subscribe(__ -> {
        commentDialogDismissed.onNext(null);
        showCommentPostedToast.onNext(null);
      });

    postedComment
      .map(__ -> "")
      .compose(bindToLifecycle())
      .subscribe(commentBodyChanged::onNext);

    initialProject.take(1)
      .compose(bindToLifecycle())
      .subscribe(koala::trackProjectCommentsView);

    initialProject
      .compose(takeWhen(nextPage.skip(1)))
      .compose(bindToLifecycle())
      .subscribe(koala::trackProjectCommentLoadMore);

    paginator.isFetching()
      .compose(bindToLifecycle())
      .subscribe(isFetchingComments);

    project
      .take(1)
      .compose(bindToLifecycle())
      .subscribe(__ -> refresh.onNext(null));
  }

  private @NonNull Observable<Comment> postComment(final @NonNull Either<Project, Update> projectOrUpdate, final @NonNull String body) {
    return projectOrUpdate.isLeft()
      ? client.postComment(projectOrUpdate.left(), body)
      : client.postComment(projectOrUpdate.right(), body);
  }

  private final PublishSubject<String> commentBodyChanged = PublishSubject.create();
  private final PublishSubject<Void> commentButtonClicked = PublishSubject.create();
  private final PublishSubject<Void> commentDialogDismissed = PublishSubject.create();
  private final PublishSubject<Boolean> commentIsPosting = PublishSubject.create();
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  private final PublishSubject<Void> postCommentClicked = PublishSubject.create();
  private final PublishSubject<Void> refresh = PublishSubject.create();

  private final BehaviorSubject<CommentsData> commentsData = BehaviorSubject.create();
  private final BehaviorSubject<String> currentCommentBody = BehaviorSubject.create();
  private final BehaviorSubject<Void> dismissCommentDialog = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> enablePostButton = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> isFetchingComments = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> showCommentButton = BehaviorSubject.create();
  private final BehaviorSubject<Pair<Project, Boolean>> showCommentDialog = BehaviorSubject.create();
  private final PublishSubject<Void> showCommentPostedToast = PublishSubject.create();
  private final PublishSubject<ErrorEnvelope> showPostCommentErrorToast = PublishSubject.create();

  public final CommentsViewModelInputs inputs = this;
  public final CommentsViewModelOutputs outputs = this;

  @Override public void commentBodyChanged(final @NonNull String string) {
    commentBodyChanged.onNext(string);
  }
  @Override public void commentButtonClicked() {
    commentButtonClicked.onNext(null);
  }
  @Override public void commentDialogDismissed() {
    commentDialogDismissed.onNext(null);
  }
  @Override public void loginSuccess() {
    loginSuccess.onNext(null);
  }
  @Override public void nextPage() {
    nextPage.onNext(null);
  }
  @Override public void postCommentClicked() {
    postCommentClicked.onNext(null);
  }
  @Override public void refresh() {
    refresh.onNext(null);
  }

  @Override public @NonNull Observable<CommentsData> commentsData() {
    return commentsData;
  }
  @Override public @NonNull Observable<String> currentCommentBody() {
    return currentCommentBody;
  }
  @Override public @NonNull Observable<Void> dismissCommentDialog() {
    return dismissCommentDialog;
  }
  @Override public @NonNull Observable<Boolean> enablePostButton() {
    return enablePostButton;
  }
  @Override public @NonNull Observable<Boolean> isFetchingComments() {
    return isFetchingComments;
  }
  @Override public @NonNull Observable<Boolean> showCommentButton() {
    return showCommentButton;
  }
  @Override public @NonNull Observable<Pair<Project, Boolean>> showCommentDialog() {
    return showCommentDialog;
  }
  @Override public @NonNull Observable<Void> showCommentPostedToast() {
    return showCommentPostedToast;
  }
  @Override public @NonNull Observable<String> showPostCommentErrorToast() {
    return showPostCommentErrorToast
      .map(ErrorEnvelope::errorMessage);
  }
}
