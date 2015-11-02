package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.ApiErrorTransformer;
import com.kickstarter.libs.rx.transformers.NeverErrorTransformer;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.RxUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.presenters.errors.CommentFeedPresenterErrors;
import com.kickstarter.presenters.inputs.CommentFeedPresenterInputs;
import com.kickstarter.presenters.outputs.CommentFeedPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.CommentFeedActivity;
import com.kickstarter.ui.viewholders.EmptyCommentFeedViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class CommentFeedPresenter extends Presenter<CommentFeedActivity> implements CommentFeedPresenterInputs, CommentFeedPresenterOutputs, CommentFeedPresenterErrors {
  // INPUTS
  private final PublishSubject<String> commentBody = PublishSubject.create();
  private final PublishSubject<Void> contextClick = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> commentPosted = PublishSubject.create();
  public Observable<Void> commentPosted() { return commentPosted.asObservable(); }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> postCommentError = PublishSubject.create();
  public Observable<String> postCommentError() {
    return postCommentError
      .map(ErrorEnvelope::errorMessages)
      .map(ListUtils::first);
  }

  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  private final PublishSubject<String> bodyOnPostClick = PublishSubject.create();
  private final PublishSubject<Void> commentDialogShown = PublishSubject.create();
  private final PublishSubject<Boolean> commentIsPosting = PublishSubject.create();
  private final PublishSubject<Project> initialProject = PublishSubject.create();
  private final PublishSubject<Void> refreshFeed = PublishSubject.create();

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  public CommentFeedPresenterInputs inputs() {
    return this;
  }
  public CommentFeedPresenterOutputs outputs() { return this; }
  public CommentFeedPresenterErrors errors() { return this; }

  @Override
  public void commentBody(@NonNull final String string) {
    commentBody.onNext(string);
  }

  @Override
  public void emptyCommentFeedLoginClicked(@NonNull final EmptyCommentFeedViewHolder viewHolder) {
    loginClick.onNext(null);
  }

  @Override
  public void projectContextClicked(@NonNull final ProjectContextViewHolder viewHolder) {
    contextClick.onNext(null);
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<Project> project = RxUtils.takeWhen(initialProject, loginSuccess)
      .mergeWith(initialProject)
      .flatMap(client::fetchProject)
      .share();

    final Observable<List<Comment>> comments = RxUtils.takeWhen(project, refreshFeed)
      .switchMap(this::comments)
      .map(CommentsEnvelope::comments)
      .share();

    final Observable<List<?>> viewCommentsProject = Observable.combineLatest(
      Arrays.asList(viewSubject, comments, project),
      Arrays::asList);

    final Observable<Pair<CommentFeedActivity, Project>> viewAndProject =
      RxUtils.combineLatestPair(viewSubject, project);

    final Observable<Boolean> commentHasBody = commentBody
      .map(body -> body.length() > 0);

    final Observable<Comment> postedComment = RxUtils.takePairWhen(project, bodyOnPostClick)
      .switchMap(pb -> postComment(pb.first, pb.second));

    addSubscription(postedComment
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::postCommentSuccess)
    );

    addSubscription(
      RxUtils.combineLatestPair(viewAndProject, loginSuccess)
        .map(vpl -> vpl.first)
        .filter(vp -> vp.second.isBacking())
        .take(1)
        .map(vp -> vp.first)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(CommentFeedActivity::showCommentDialog)
    );

    addSubscription(RxUtils.combineLatestPair(currentUser.observable(), viewCommentsProject)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(uvcp -> {
        final User u = uvcp.first;
        final CommentFeedActivity view = (CommentFeedActivity) uvcp.second.get(0);
        final List<Comment> cs = (List<Comment>) uvcp.second.get(1);
        final Project p = (Project) uvcp.second.get(2);
        view.show(p, cs, u);
      })
    );

    addSubscription(RxUtils.takeWhen(viewSubject, contextClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(CommentFeedActivity::onBackPressed)
    );

    addSubscription(RxUtils.takeWhen(viewSubject, loginClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(CommentFeedActivity::commentFeedLogin)
    );

    addSubscription(RxUtils.combineLatestPair(viewSubject, commentHasBody)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ve -> ve.first.enablePostButton(ve.second))
    );

    addSubscription(RxUtils.takeWhen(viewSubject, refreshFeed)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(CommentFeedActivity::dismissCommentDialog)
    );

    addSubscription(RxUtils.takePairWhen(viewSubject, commentIsPosting)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.disablePostButton(vp.second))
    );
  }

  private Observable<Comment> postComment(@NonNull final Project project, @NonNull final String body) {
    return client.postProjectComment(project, body)
      .compose(new ApiErrorTransformer<>(postCommentError))
      .doOnSubscribe(() -> commentIsPosting.onNext(true))
      .doOnCompleted(() -> commentPosted.onNext(null))
      .finallyDo(() -> commentIsPosting.onNext(false));
  }

  private Observable<CommentsEnvelope> comments(@NonNull final Project project) {
    return client.fetchProjectComments(project)
      .compose(new NeverErrorTransformer<>());
  }

  // todo: add pagination to comments
  public void initialize(@NonNull final Project initialProject) {
    this.initialProject.onNext(initialProject);
    refreshFeed.onNext(null);
  }

  public void postClick(@NonNull final String body) {
    bodyOnPostClick.onNext(body);
  }

  public void takeCommentDialogShown() {
    commentDialogShown.onNext(null);
  }

  private void postCommentSuccess(@Nullable final Comment comment) {
    refreshFeed.onNext(null);
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
    refreshFeed.onNext(null);
  }
}
