package com.kickstarter.presenters.errors;

import rx.Observable;

public interface CommentFeedPresenterErrors {
  Observable<String> postCommentError();
}
