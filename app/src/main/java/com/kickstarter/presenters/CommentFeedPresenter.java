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
import com.kickstarter.ui.activities.CommentFeedActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class CommentFeedPresenter extends Presenter<CommentFeedActivity> {
  private final PublishSubject<Void> postCommentClick = PublishSubject.create();
  private final PublishSubject<Void> nextPage = PublishSubject.create();

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    // Pagination


    nextPage.onNext(null);
  }

  // todo: add pagination to comments
  public void takeProject(final Project project) {
    final Observable<List<Comment>> comments = client.fetchProjectComments(project)
      .map(envelope -> envelope.comments);

    final Observable<Pair<CommentFeedActivity, List<Comment>>> viewAndComments =
      RxUtils.takePairWhen(viewSubject, comments);

    addSubscription(viewAndComments
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vc -> vc.first.loadProjectComments(project, vc.second)));
  }

}
