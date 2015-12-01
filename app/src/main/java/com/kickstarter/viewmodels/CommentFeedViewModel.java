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
import com.kickstarter.services.ApiClient;
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
  public void initialProject(@NonNull final Project project) { initialProject.onNext(project); }
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

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  public final CommentFeedViewModelInputs inputs = this;
  public final CommentFeedViewModelOutputs outputs = this;
  public final CommentFeedViewModelErrors errors = this;

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

    final ApiPaginator<Comment, CommentsEnvelope, Void> paginator =
      ApiPaginator.<Comment, CommentsEnvelope, Void>builder()
        .nextPage(nextPage)
        .startOverWith(refresh.compose(Transformers.ignoreValues()))
        .envelopeToListOfData(CommentsEnvelope::comments)
        .loadWithNextUrl(env -> env.urls().api().moreComments(), client::fetchProjectComments)
        .loadWithParams(__ -> initialProject.take(1).flatMap(client::fetchProjectComments))
        .build();

    final Observable<List<Comment>> comments = paginator.paginatedData.share();

    paginator.isFetching.subscribe(isFetchingComments);

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
        .subscribe(showCommentDialog)
      );

    addSubscription(Observable.combineLatest(
        currentUser.observable(),
        view,
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
        .subscribe(showCommentButton)
    );

    addSubscription(postedComment
        .compose(Transformers.ignoreValues())
        .subscribe(__ -> refresh.onNext(Empty.get()))
    );

    addSubscription(view
        .compose(Transformers.combineLatestPair(commentHasBody))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(ve -> ve.first.enablePostButton(ve.second))
    );

    addSubscription(view
        .compose(Transformers.takePairWhen(commentIsPosting))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.disablePostButton(vp.second))
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

    addSubscription(project.take(1).subscribe(__ -> refresh.onNext(Empty.get())));
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
