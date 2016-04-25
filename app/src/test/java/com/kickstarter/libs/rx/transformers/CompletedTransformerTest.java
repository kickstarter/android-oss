package com.kickstarter.libs.rx.transformers;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public final class CompletedTransformerTest {
  @Test
  public void testEmitsCompleted() {
    final PublishSubject<Integer> source = PublishSubject.create();
    final Observable<Void> result = source
      .materialize()
      .compose(Transformers.completed());

    final TestSubscriber<Void> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    source.onNext(1);
    resultTest.assertNoValues();

    // Only emit when a completed notification occurs.
    source.onCompleted();
    resultTest.assertValueCount(1);
  }

  @Test
  public void testErrorDoesNotEmitACompletedNotification() {
    final PublishSubject<Integer> source = PublishSubject.create();
    final Observable<Void> result = source
      .materialize()
      .compose(Transformers.completed());

    final TestSubscriber<Void> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    source.onError(new Throwable());
    resultTest.assertValueCount(0);
  }
}
