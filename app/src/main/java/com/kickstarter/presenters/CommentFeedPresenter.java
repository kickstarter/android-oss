package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.ui.activities.CommentFeedActivity;
import com.kickstarter.ui.adapters.CommentFeedAdapter;
import com.kickstarter.ui.viewholders.CommentViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class CommentFeedPresenter extends Presenter<CommentFeedActivity> implements CommentFeedAdapter.Delegate {
  private final PublishSubject<Comment> testCommentClick = PublishSubject.create();
  private final PublishSubject<Project> contextClick = PublishSubject.create();

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  // todo: add pagination to comments
  public void takeProject(final Project project) {
    final Observable<List<Comment>> comments = client.fetchProjectComments(project)
      .map(CommentsEnvelope::comments)
      .takeUntil(List::isEmpty);

    final Observable<Pair<CommentFeedActivity, List<Comment>>> viewAndComments =
      RxUtils.takePairWhen(viewSubject, comments);

    addSubscription(viewAndComments
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vc -> vc.first.loadProjectComments(project, vc.second)));

    // works
    addSubscription(RxUtils.takePairWhen(viewSubject, testCommentClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vc -> vc.first.testActivity())
    );

    addSubscription(RxUtils.takePairWhen(viewSubject, contextClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vc -> vc.first.onBackPressed())
    );
  }

  public void commentClick(final CommentViewHolder viewHolder, final Comment comment) {
    testCommentClick.onNext(comment);
  }

  public void contextClick(final ProjectContextViewHolder viewHolder, final Project project) {
    contextClick.onNext(project);
  }
}
