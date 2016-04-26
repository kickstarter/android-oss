package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ActivityViewModel;
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
import com.kickstarter.viewmodels.errors.CommentFeedViewModelErrors;
import com.kickstarter.viewmodels.inputs.CommentFeedViewModelInputs;
import com.kickstarter.viewmodels.outputs.CommentFeedViewModelOutputs;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class CommentFeedViewModel extends ActivityViewModel<CommentFeedActivity> implements CommentFeedViewModelInputs,
  CommentFeedViewModelOutputs, CommentFeedViewModelErrors {
  // INPUTS
  private final PublishSubject<String> commentBodyInput = PublishSubject.create();
  @Override
  public void commentBodyInput(final @NonNull String string) {
    commentBodyInput.onNext(string);
  }
  private final PublishSubject<Void> commentDialogDismissed = PublishSubject.create();
  @Override
  public void commentDialogDismissed() {
    commentDialogDismissed.onNext(null);
  }
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  @Override
  public void loginSuccess() {
    loginSuccess.onNext(null);
  }
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
  }
  private final PublishSubject<Void> postCommentClicked = PublishSubject.create();
  @Override
  public void postCommentClicked() {
    postCommentClicked.onNext(null);
  }
  private final BehaviorSubject<Void> refresh = BehaviorSubject.create((Void) null);
  public void refresh() {
    refresh.onNext(null);
  }
  private final PublishSubject<Void> commentButtonClicked = PublishSubject.create();
  @Override
  public void commentButtonClicked() {
    commentButtonClicked.onNext(null);
  }

  // OUTPUTS
  private final BehaviorSubject<CommentFeedData> commentFeedData = BehaviorSubject.create();
  @Override
  public Observable<CommentFeedData> commentFeedData() {
    return commentFeedData;
  }
  private final BehaviorSubject<Void> dismissCommentDialog = BehaviorSubject.create();
  @Override
  public Observable<Void> dismissCommentDialog() {
    return dismissCommentDialog;
  }
  private final BehaviorSubject<Boolean> enablePostButton = BehaviorSubject.create();
  @Override
  public Observable<Boolean> enablePostButton() {
    return enablePostButton;
  }
  private final BehaviorSubject<String> currentCommentBody = BehaviorSubject.create();
  @Override
  public Observable<String> currentCommentBody() {
    return currentCommentBody;
  }
  private final BehaviorSubject<Boolean> isFetchingComments = BehaviorSubject.create();
  public Observable<Boolean> isFetchingComments() {
    return isFetchingComments;
  }
  private final BehaviorSubject<Pair<Project, Boolean>> showCommentDialog = BehaviorSubject.create();
  public Observable<Pair<Project, Boolean>> showCommentDialog() {
    return showCommentDialog;
  }
  private final BehaviorSubject<Boolean> showCommentButton = BehaviorSubject.create();
  public Observable<Boolean> showCommentButton() {
    return showCommentButton;
  }
  private final PublishSubject<Void> showCommentPostedToast = PublishSubject.create();
  @Override
  public Observable<Void> showCommentPostedToast() {
    return showCommentPostedToast;
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> postCommentError = PublishSubject.create();
  public Observable<String> postCommentError() {
    return postCommentError
      .map(ErrorEnvelope::errorMessage);
  }

  private final PublishSubject<Boolean> commentIsPosting = PublishSubject.create();

  private final ApiClientType client;
  private final CurrentUserType currentUser;

  public final CommentFeedViewModelInputs inputs = this;
  public final CommentFeedViewModelOutputs outputs = this;
  public final CommentFeedViewModelErrors errors = this;

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

    final ApiPaginator<Comment, CommentsEnvelope, Void> paginator =
      ApiPaginator.<Comment, CommentsEnvelope, Void>builder()
        .nextPage(nextPage)
        .startOverWith(refresh)
        .envelopeToListOfData(CommentsEnvelope::comments)
        .envelopeToMoreUrl(env -> env.urls().api().moreComments())
        .loadWithParams(__ -> initialProject.take(1).flatMap(client::fetchProjectComments))
        .loadWithPaginationPath(client::fetchProjectComments)
        .build();

    final Observable<List<Comment>> comments = paginator.paginatedData().share();

    final PublishSubject<Void> commentIsPosted = PublishSubject.create();

    final Observable<Boolean> commentHasBody = commentBodyInput
      .map(body -> body.length() > 0);

    final Observable<Comment> postedComment = project
      .compose(Transformers.combineLatestPair(commentBodyInput))
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
    commentBodyInput
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
      .subscribe(commentBodyInput::onNext);

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
      .compose(Transformers.pipeApiErrorsTo(postCommentError))
      .compose(Transformers.neverError())
      .doOnSubscribe(() -> commentIsPosting.onNext(true))
      .finallyDo(() -> commentIsPosting.onNext(false));
  }
}
