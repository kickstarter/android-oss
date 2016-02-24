package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.CommentFeedActivity;
import com.kickstarter.viewmodels.errors.CommentFeedViewModelErrors;
import com.kickstarter.viewmodels.inputs.CommentFeedViewModelInputs;
import com.kickstarter.viewmodels.outputs.CommentFeedViewModelOutputs;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class CommentFeedViewModel extends ViewModel<CommentFeedActivity> implements CommentFeedViewModelInputs, CommentFeedViewModelOutputs, CommentFeedViewModelErrors {
  // INPUTS
  private final PublishSubject<String> commentBody = PublishSubject.create();
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
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
  private final PublishSubject<Void> commentPosted = PublishSubject.create();
  public Observable<Void> commentPosted() {
    return commentPosted.asObservable();
  }
  private final PublishSubject<Boolean> isFetchingComments = PublishSubject.create();
  public Observable<Boolean> isFetchingComments() {
    return isFetchingComments;
  }
  private final PublishSubject<Project> showCommentDialog = PublishSubject.create();
  public Observable<Project> showCommentDialog() {
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

  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  private final PublishSubject<String> bodyOnPostClick = PublishSubject.create();
  private final PublishSubject<Boolean> commentIsPosting = PublishSubject.create();

  private final ApiClientType client;
  private final CurrentUserType currentUser;

  public final CommentFeedViewModelInputs inputs = this;
  public final CommentFeedViewModelOutputs outputs = this;
  public final CommentFeedViewModelErrors errors = this;

  @Override
  public void commentBody(final @NonNull String string) {
    commentBody.onNext(string);
  }

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
      .compose(Transformers.takePairWhen(bodyOnPostClick))
      .switchMap(pb -> postComment(pb.first, pb.second))
      .share();

    project
      .compose(Transformers.takeWhen(loginSuccess))
      .filter(Project::isBacking)
      .take(1)
      .compose(bindToLifecycle())
      .subscribe(showCommentDialog);

    project
      .compose(Transformers.takeWhen(commentButtonClicked))
      .filter(Project::isBacking)
      .compose(bindToLifecycle())
      .subscribe(showCommentDialog);

    Observable.combineLatest(
        currentUser.observable(),
        view(),
        comments,
        project,
        Arrays::asList)
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(uvcp -> {
        final User u = (User) uvcp.get(0);
        final CommentFeedActivity view = (CommentFeedActivity) uvcp.get(1);
        final List<Comment> cs = (List<Comment>) uvcp.get(2);
        final Project p = (Project) uvcp.get(3);
        view.show(p, cs, u);
      });

    project
      .map(Project::isBacking)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(showCommentButton);

    postedComment
      .compose(Transformers.ignoreValues())
      .compose(bindToLifecycle())
      .subscribe(__ -> refresh.onNext(null));

    view()
      .compose(Transformers.combineLatestPair(commentHasBody))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(ve -> ve.first.enablePostButton(ve.second));

    view()
        .compose(Transformers.takePairWhen(commentIsPosting))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.disablePostButton(vp.second));

    // Koala tracking
    initialProject
      .compose(Transformers.takePairWhen(postedComment))
      .compose(bindToLifecycle())
      .subscribe(cp -> koala.trackProjectCommentCreate(cp.first, cp.second));

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

  public void postClick(final @NonNull String body) {
    bodyOnPostClick.onNext(body);
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
  }
}
