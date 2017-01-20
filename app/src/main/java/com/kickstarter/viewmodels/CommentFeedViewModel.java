package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.CommentFeedActivity;
import com.kickstarter.ui.adapters.data.CommentFeedData;
import com.kickstarter.viewmodels.inputs.CommentFeedViewModelInputs;
import com.kickstarter.viewmodels.outputs.CommentFeedViewModelOutputs;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class CommentFeedViewModel extends ActivityViewModel<CommentFeedActivity> implements CommentFeedViewModelInputs,
  CommentFeedViewModelOutputs {
  private final ApiClientType client;
  private final CurrentUserType currentUser;

  public CommentFeedViewModel(final @NonNull Environment environment) {
    super(environment);

    this.client = environment.apiClient();
    this.currentUser = environment.currentUser();

    final Observable<Project> initialProject = intent()
      .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
      .ofType(Project.class)
      .filter(ObjectUtils::isNotNull);

    final Observable<Project> project = initialProject
      .compose(Transformers.takeWhen(loginSuccess))
      .flatMap(p -> client.fetchProject(p).compose(Transformers.neverError()))
      .mergeWith(initialProject)
      .share();

    final Observable<Project> startOverWith = Observable.merge(
      initialProject.take(1),
      initialProject.compose(Transformers.takeWhen(refresh))
      );

    final ApiPaginator<Comment, CommentsEnvelope, Project> paginator =
      ApiPaginator.<Comment, CommentsEnvelope, Project>builder()
        .nextPage(nextPage)
        .startOverWith(startOverWith)
        .envelopeToListOfData(CommentsEnvelope::comments)
        .envelopeToMoreUrl(env -> env.urls().api().moreComments())
        .loadWithParams(client::fetchProjectComments)
        .loadWithPaginationPath(client::fetchProjectComments)
        .build();

    final Observable<List<Comment>> comments = paginator.paginatedData().share();

    final PublishSubject<Void> commentIsPosted = PublishSubject.create();

    final Observable<Boolean> commentHasBody = commentBodyChanged
      .map(body -> body.length() > 0);

    final Observable<Comment> postedComment = project
      .compose(Transformers.combineLatestPair(commentBodyChanged))
      .compose(Transformers.takeWhen(postCommentClicked))
      .switchMap(pb -> postComment(pb.first, pb.second))
      .share();

    project
      .compose(Transformers.takeWhen(loginSuccess))
      .filter(Project::isBacking)
      .take(1)
      .compose(bindToLifecycle())
      .subscribe(p -> showCommentDialog.onNext(Pair.create(p, true)));

    project
      .compose(Transformers.takeWhen(commentButtonClicked))
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

    Observable.combineLatest(
      project,
      comments,
      currentUser.observable(),
      CommentFeedData::deriveData
    )
      .compose(bindToLifecycle())
      .subscribe(commentFeedData::onNext);

    project
      .map(Project::isBacking)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(showCommentButton::onNext);

    postedComment
      .compose(Transformers.ignoreValues())
      .compose(bindToLifecycle())
      .subscribe(__ -> {
        refresh.onNext(null);
        commentIsPosted.onNext(null);
      });

    commentHasBody
      .compose(bindToLifecycle())
      .subscribe(enablePostButton::onNext);

    commentIsPosting
      .map(b -> !b)
      .compose(bindToLifecycle())
      .subscribe(enablePostButton::onNext);

    initialProject
      .compose(Transformers.takeWhen(commentIsPosted))
      .compose(bindToLifecycle())
      .subscribe(koala::trackProjectCommentCreate);

    commentIsPosted
      .compose(bindToLifecycle())
      .subscribe(__ -> {
        commentDialogDismissed.onNext(null);
        showCommentPostedToast.onNext(null);
      });

    commentIsPosted
      .map(__ -> "")
      .compose(bindToLifecycle())
      .subscribe(commentBodyChanged::onNext);

    initialProject.take(1)
      .compose(bindToLifecycle())
      .subscribe(koala::trackProjectCommentsView);

    initialProject
      .compose(Transformers.takeWhen(nextPage.skip(1)))
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

  private Observable<Comment> postComment(final @NonNull Project project, final @NonNull String body) {
    return client.postProjectComment(project, body)
      .compose(Transformers.pipeApiErrorsTo(showPostCommentErrorToast))
      .compose(Transformers.neverError())
      .doOnSubscribe(() -> commentIsPosting.onNext(true))
      .doAfterTerminate(() -> commentIsPosting.onNext(false));
  }

  private final PublishSubject<String> commentBodyChanged = PublishSubject.create();
  private final PublishSubject<Void> commentButtonClicked = PublishSubject.create();
  private final PublishSubject<Void> commentDialogDismissed = PublishSubject.create();
  private final PublishSubject<Boolean> commentIsPosting = PublishSubject.create();
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  private final PublishSubject<Void> postCommentClicked = PublishSubject.create();
  private final PublishSubject<Void> refresh = PublishSubject.create();

  private final BehaviorSubject<CommentFeedData> commentFeedData = BehaviorSubject.create();
  private final BehaviorSubject<String> currentCommentBody = BehaviorSubject.create();
  private final BehaviorSubject<Void> dismissCommentDialog = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> enablePostButton = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> isFetchingComments = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> showCommentButton = BehaviorSubject.create();
  private final BehaviorSubject<Pair<Project, Boolean>> showCommentDialog = BehaviorSubject.create();
  private final PublishSubject<Void> showCommentPostedToast = PublishSubject.create();
  private final PublishSubject<ErrorEnvelope> showPostCommentErrorToast = PublishSubject.create();

  public final CommentFeedViewModelInputs inputs = this;
  public final CommentFeedViewModelOutputs outputs = this;

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

  @Override public @NonNull Observable<CommentFeedData> commentFeedData() {
    return commentFeedData;
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
