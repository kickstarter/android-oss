package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.ui.activities.CommentFeedActivity;
import com.kickstarter.ui.adapters.CommentFeedAdapter;
import com.kickstarter.ui.viewholders.EmptyCommentFeedViewHolder;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class CommentFeedPresenter extends Presenter<CommentFeedActivity> implements CommentFeedAdapter.Delegate {
  private final PublishSubject<Void> contextClick = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  private final PublishSubject<String> bodyOnPostClick = PublishSubject.create();
  private final PublishSubject<Void> commentDialogShown = PublishSubject.create();
  private final PublishSubject<Boolean> commentIsPosting = PublishSubject.create();
  private final PublishSubject<Void> refreshFeed = PublishSubject.create();

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  // todo: add pagination to comments
  public void initialize(@NonNull final Project initialProject) {

    final Observable<Project> project = loginSuccess
      .flatMap(__ -> client.fetchProject(initialProject))
      .startWith(initialProject);
      // TODO: currently we are loading projects twice after login, need to figure out .share()

    final Observable<List<Comment>> comments = refreshFeed
      .switchMap(__ -> client.fetchProjectComments(initialProject))
      .map(CommentsEnvelope::comments)
      .share();

    final Observable<List<?>> viewCommentsProject = Observable.combineLatest(
      Arrays.asList(viewSubject, comments, project),
      Arrays::asList);

    final Observable<Pair<CommentFeedActivity, Project>> viewAndProject =
      RxUtils.combineLatestPair(viewSubject, project);

    final Observable<CharSequence> commentBody =  RxUtils.takeWhen(viewSubject, commentDialogShown)
      .flatMap(v -> RxTextView.textChanges(v.commentBodyEditText));

    final Observable<Boolean> commentHasBody = commentBody
      .map(body -> body.length() > 0);

    final Observable<Comment> postedComment = bodyOnPostClick
      .flatMap(body -> client.postProjectComment(initialProject, body))
      .share();

    addSubscription(postedComment
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::postCommentSuccess, this::postCommentError)
    );

    addSubscription(RxUtils.takeWhen(viewAndProject, loginSuccess)
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
      .subscribe(CommentFeedActivity::onBackPressed));

    addSubscription(RxUtils.takeWhen(viewSubject, loginClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(CommentFeedActivity::commentFeedLogin));

    addSubscription(
      RxUtils.combineLatestPair(viewSubject, commentHasBody)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(ve -> ve.first.enablePostButton(ve.second))
    );

    addSubscription(
      RxUtils.takeWhen(viewSubject, refreshFeed)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(CommentFeedActivity::dismissCommentDialog)
    );

    addSubscription(
      RxUtils.takePairWhen(viewSubject, commentIsPosting)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.disablePostButton(vp.second))
    );

    refreshFeed.onNext(null);
  }

  public void emptyCommentFeedLoginClicked(@NonNull final EmptyCommentFeedViewHolder viewHolder) {
    loginClick.onNext(null);
  }

  public void postClick(@NonNull final String body) {
    commentIsPosting.onNext(true);
    bodyOnPostClick.onNext(body);
  }

  public void takeCommentDialogShown() {
    commentDialogShown.onNext(null);
  }

  private void postCommentSuccess(@Nullable final Comment comment) {
    commentIsPosting.onNext(false);
    refreshFeed.onNext(null);
    final Context context = view().getApplicationContext();
    final Toast toast = Toast.makeText(context, context.getString(R.string.Comment_posted), Toast.LENGTH_LONG);
    toast.show();
  }

  private void postCommentError(@NonNull final Throwable e) {
    commentIsPosting.onNext(false);
    // todo: handle 422s and network errors
  }

  public void projectContextClicked() {
    contextClick.onNext(null);
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
  }
}
