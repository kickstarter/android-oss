package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.presenters.errors.CommentFeedPresenterErrors;
import com.kickstarter.presenters.inputs.CommentFeedPresenterInputs;
import com.kickstarter.presenters.outputs.CommentFeedPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.CommentFeedParams;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.CommentFeedActivity;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

public final class CommentFeedPresenter extends Presenter<CommentFeedActivity> implements CommentFeedPresenterInputs, CommentFeedPresenterOutputs, CommentFeedPresenterErrors {
  // INPUTS
  private final ReplaySubject<Project> initialProject = ReplaySubject.createWithSize(1);
  public void initialProject(@NonNull final Project project) { initialProject.onNext(project); }
  private final PublishSubject<String> commentBody = PublishSubject.create();
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() { nextPage.onNext(null); }
  private final BehaviorSubject<Empty> refresh = BehaviorSubject.create(Empty.create());
  public void refresh() {
    refresh.onNext(Empty.create());
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
  private final PublishSubject<CommentFeedParams> params = PublishSubject.create();

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  public final CommentFeedPresenterInputs inputs = this;
  public final CommentFeedPresenterOutputs outputs = this;
  public final CommentFeedPresenterErrors errors = this;

  @Override
  public void commentBody(@NonNull final String string) {
    commentBody.onNext(string);
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<Project> project = initialProject
      .compose(Transformers.takeWhen(loginSuccess))
      .flatMap(client::fetchProject)
      .mergeWith(initialProject)
      .share();

    final Observable<List<Comment>> comments = refresh
      .switchMap(__ -> commentsWithPagination())
      .share();

    final Observable<Boolean> commentHasBody = commentBody
      .map(body -> body.length() > 0);

    final Observable<Comment> postedComment = project
      .compose(Transformers.takePairWhen(bodyOnPostClick))
      .switchMap(pb -> postComment(pb.first, pb.second))
      .share();

    addSubscription(project
        .compose(Transformers.takeWhen(loginSuccess))
        .filter(Project::isBacking)
        .take(1)
        .compose(Transformers.ignoreValues())
        .subscribe(showCommentDialog::onNext)
      );

    addSubscription(Observable.combineLatest(
        currentUser.observable(),
        viewSubject,
        comments,
        project,
        Arrays::asList)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(uvcp -> {
          final User u = (User) uvcp.get(0);
          final CommentFeedActivity view = (CommentFeedActivity) uvcp.get(1);
          final List<Comment> cs = (List<Comment>) uvcp.get(2);
          final Project p = (Project) uvcp.get(3);
          view.show(p, cs, u);
        })
    );

    addSubscription(project
        .map(Project::isBacking)
        .distinctUntilChanged()
        .subscribe(showCommentButton::onNext)
    );

    addSubscription(postedComment
        .compose(Transformers.ignoreValues())
        .subscribe(__ -> refresh.onNext(Empty.create()))
    );

    addSubscription(viewSubject
        .compose(Transformers.combineLatestPair(commentHasBody))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(ve -> ve.first.enablePostButton(ve.second))
    );

    addSubscription(viewSubject
        .compose(Transformers.takePairWhen(commentIsPosting))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.disablePostButton(vp.second))
    );

    addSubscription(project
        .compose(Transformers.takeWhen(refresh))
        .map(p -> CommentFeedParams.builder().project(p).build())
        .subscribe(p -> {
          params.onNext(p);
          nextPage();
        })
    );

    // Koala tracking
    addSubscription(initialProject
      .compose(Transformers.takePairWhen(postedComment))
      .subscribe(cp -> koala.trackProjectCommentCreate(cp.first, cp.second))
    );
    addSubscription(initialProject.take(1).subscribe(koala::trackProjectCommentsView));
    addSubscription(initialProject
      .compose(Transformers.takeWhen(nextPage))
      .subscribe(koala::trackProjectCommentLoadMore)
    );

    addSubscription(project.take(1).subscribe(__ -> refresh.onNext(Empty.create())));
  }

  private Observable<List<Comment>> commentsWithPagination() {
    return params
      .compose(Transformers.takeWhen(nextPage))
      .concatMap(this::commentsFromParams)
      .takeUntil(List::isEmpty)
      .scan(ListUtils::concat);
  }

  private Observable<List<Comment>> commentsFromParams(@NonNull final CommentFeedParams params) {
    return client.fetchProjectComments(params)
      .compose(Transformers.neverError())
      .doOnNext(env -> keepPaginationParams(params, env))
      .map(CommentsEnvelope::comments)
      .doOnSubscribe(() -> isFetchingComments.onNext(true))
      .finallyDo(() -> isFetchingComments.onNext(false));
  }

  private void keepPaginationParams(@NonNull final CommentFeedParams currentParams, @NonNull final CommentsEnvelope envelope) {
    final CommentsEnvelope.UrlsEnvelope urls = envelope.urls();
    if (urls != null) {
      final CommentsEnvelope.UrlsEnvelope.ApiEnvelope api = urls.api();
      if (api != null) {
        final String moreUrl = api.moreComments();
        if (moreUrl != null) {
          this.params.onNext(currentParams.nextPageFromUrl(moreUrl));
        }
      }
    }
  }

  private Observable<Comment> postComment(@NonNull final Project project, @NonNull final String body) {
    return client.postProjectComment(project, body)
      .compose(Transformers.pipeApiErrorsTo(postCommentError))
      .doOnSubscribe(() -> commentIsPosting.onNext(true))
      .finallyDo(() -> {
        commentIsPosting.onNext(false);
        commentPosted.onNext(null);
      });
  }

  public void postClick(@NonNull final String body) {
    bodyOnPostClick.onNext(body);
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
  }
}
