package com.kickstarter.presenters.outputs;

import rx.Observable;

public interface CommentFeedPresenterOutputs {
  // Emits when a comment has been successfully posted.
  Observable<Void> commentPosted();
}
