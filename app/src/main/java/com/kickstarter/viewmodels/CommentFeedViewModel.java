package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class CommentFeedViewModel extends ViewModel<CommentFeedActivity> implements CommentFeedViewModelInputs,
  CommentFeedViewModelOutputs, CommentFeedViewModelErrors {
  // INPUTS
  private final PublishSubject<String> commentBody = PublishSubject.create();
  @Override
  public void commentBody(final @NonNull String string) {
    commentBody.onNext(string);
  }
  private final PublishSubject<Void> dismissCommentDialog = PublishSubject.create();
  @Override
  public void dismissCommentDialog() {
    dismissCommentDialog.onNext(null);
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
  private final BehaviorSubject<Void> commentPosted = BehaviorSubject.create();
  @Override
  public Observable<Void> commentPosted() {
    return commentPosted.asObservable();
  }
  private final BehaviorSubject<CommentFeedData> commentFeedData = BehaviorSubject.create();
  @Override
  public Observable<CommentFeedData> commentFeedData() {
    return commentFeedData;
  }
  private final BehaviorSubject<Boolean> enablePostButton = BehaviorSubject.create();
  @Override
  public Observable<Boolean> postButtonIsEnabled() {
    return enablePostButton;
  }
  private final BehaviorSubject<String> initialCommentBody = BehaviorSubject.create();
  @Override
  public Observable<String> initialCommentBody() {
    return initialCommentBody;
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
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

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

    final Observable<Boolean> commentHasBody = commentBody
      .map(body -> body.length() > 0);

    final Observable<Comment> postedComment = project
      .compose(Transformers.combineLatestPair(commentBody))
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

    project
      .compose(Transformers.takeWhen(dismissCommentDialog))
      .compose(bindToLifecycle())
      .subscribe(p -> showCommentDialog.onNext(Pair.create(p, false)));

    // Seed initial comment body with user input, if any.
    commentBody
      .compose(bindToLifecycle())
      .subscribe(initialCommentBody::onNext);

    Observable.combineLatest(
      project,
      comments,
      currentUser.observable(),
      CommentFeedData::deriveData
    )
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(commentFeedData::onNext);

    project
      .map(Project::isBacking)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(showCommentButton);

    postedComment
      .compose(Transformers.ignoreValues())
      .compose(bindToLifecycle())
      .subscribe(__ -> refresh.onNext(null));

    commentHasBody
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(enablePostButton::onNext);

    commentIsPosting
      .map(b -> !b)
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(enablePostButton::onNext);

    initialProject
      .compose(Transformers.takeWhen(commentPosted))
      .compose(bindToLifecycle())
      .subscribe(p -> {
        koala.trackProjectCommentCreate(p);
        dismissCommentDialog.onNext(null);
      });

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
      .finallyDo(() -> {
        commentIsPosting.onNext(false);
        commentPosted.onNext(null);
      });
  }
}
