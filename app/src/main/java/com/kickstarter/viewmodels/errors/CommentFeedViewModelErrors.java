package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface CommentFeedViewModelErrors {
  Observable<String> postCommentError();
}
