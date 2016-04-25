package com.kickstarter.libs.rx.transformers;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public final class ErrorsTransformerTest {
  @Test
  public void testEmitsErrors() {
    final PublishSubject<Integer> source = PublishSubject.create();
    final Observable<Throwable> result = source
      .materialize()
      .compose(Transformers.errors());

    final TestSubscriber<Throwable> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    source.onNext(1);
    resultTest.assertNoValues();

    // Only emit when an error is thrown.
    source.onError(new Throwable());
    resultTest.assertValueCount(1);
  }

  @Test
  public void testCompletedDoesNotEmitAnErrorNotification() {
    final PublishSubject<Integer> source = PublishSubject.create();
    final Observable<Throwable> result = source
      .materialize()
      .compose(Transformers.errors());

    final TestSubscriber<Throwable> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    source.onCompleted();
    resultTest.assertValueCount(0);
  }
}
