package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.CommentFeedActivity;
import com.kickstarter.viewmodels.errors.CommentFeedViewModelErrors;
import com.kickstarter.viewmodels.inputs.CommentFeedViewModelInputs;
import com.kickstarter.viewmodels.outputs.CommentFeedViewModelOutputs;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

public final class CommentFeedViewModel extends ViewModel<CommentFeedActivity> implements CommentFeedViewModelInputs, CommentFeedViewModelOutputs, CommentFeedViewModelErrors {
  // INPUTS
  private final ReplaySubject<Project> initialProject = ReplaySubject.createWithSize(1);
  public void initialProject(final @NonNull Project project) { initialProject.onNext(project); }
  private final PublishSubject<String> commentBody = PublishSubject.create();
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() { nextPage.onNext(null); }
  private final BehaviorSubject<Empty> refresh = BehaviorSubject.create(Empty.get());
  public void refresh() {
    refresh.onNext(Empty.get());
  }

  // OUTPUTS
  private final PublishSubject<Void> commentPosted = PublishSubject.create();
  public Observable<Void> commentPosted() { return commentPosted.asObservable(); }
  private final PublishSubject<Boolean> isFetchingComments = PublishSubject.create();
  public final Observable<Boolean> isFetchingComments() { return isFetchingComments; }
  private final PublishSubject<Void> showCommentDialog = PublishSubject.create();
  public Observable<Void> showCommentDialog() { return showCommentDialog; }
  private final BehaviorSubject<Boolean> showCommentButton = BehaviorSubject.create();
  public Observable<Boolean> showCommentButton() { return showCommentButton; }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> postCommentError = PublishSubject.create();
  public Observable<String> postCommentError() {
    return postCommentError
      .map(ErrorEnvelope::errorMessage);
  }

  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  private final PublishSubject<String> bodyOnPostClick = PublishSubject.create();
  private final PublishSubject<Boolean> commentIsPosting = PublishSubject.create();

  protected @Inject ApiClientType client;
  protected @Inject CurrentUser currentUser;

  public final CommentFeedViewModelInputs inputs = this;
  public final CommentFeedViewModelOutputs outputs = this;
  public final CommentFeedViewModelErrors errors = this;

  @Override
  public void commentBody(final @NonNull String string) {
    commentBody.onNext(string);
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<Project> project = initialProject
      .compose(Transformers.takeWhen(loginSuccess))
      .flatMap(p -> client.fetchProject(p).compose(Transformers.neverError()))
      .mergeWith(initialProject)
      .share();

    final ApiPaginator<Comment, CommentsEnvelope, Void> paginator =
      ApiPaginator.<Comment, CommentsEnvelope, Void>builder()
        .nextPage(nextPage)
        .startOverWith(refresh.compose(Transformers.ignoreValues()))
        .envelopeToListOfData(CommentsEnvelope::comments)
        .envelopeToMoreUrl(env -> env.urls().api().moreComments())
        .loadWithParams(__ -> initialProject.take(1).flatMap(client::fetchProjectComments))
        .loadWithPaginationPath(client::fetchProjectComments)
        .build();

    final Observable<List<Comment>> comments = paginator.paginatedData.share();

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
      .compose(Transformers.ignoreValues())
      .compose(bindToLifecycle())
      .subscribe(showCommentDialog);

    Observable.combineLatest(
      currentUser.observable(),
      view,
      comments,
      project,
      Arrays::asList)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
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
      .subscribe(__ -> refresh.onNext(Empty.get()));

    view
      .compose(Transformers.combineLatestPair(commentHasBody))
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ve -> ve.first.enablePostButton(ve.second));

    view
        .compose(Transformers.takePairWhen(commentIsPosting))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.disablePostButton(vp.second));

    // Koala tracking
    initialProject
      .compose(Transformers.takePairWhen(postedComment))
      .compose(bindToLifecycle())
      .subscribe(cp -> koala.trackProjectCommentCreate(cp.first, cp.second));
    initialProject
      .take(1)
      .compose(bindToLifecycle())
      .subscribe(koala::trackProjectCommentsView);
    initialProject
      .compose(Transformers.takeWhen(nextPage.skip(1)))
      .compose(bindToLifecycle())
      .subscribe(koala::trackProjectCommentLoadMore);

    paginator.isFetching
      .compose(bindToLifecycle())
      .subscribe(isFetchingComments);

    project
      .take(1)
      .compose(bindToLifecycle())
      .subscribe(__ -> refresh.onNext(Empty.get()));
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
