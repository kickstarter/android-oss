package com.kickstarter.libs.rx.transformers;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public final class ValuesTransformerTest {

  @Test
  public void testEmitsOnNext() {
    final PublishSubject<Integer> source = PublishSubject.create();
    final Observable<Integer> result = source
      .materialize()
      .compose(Transformers.values());

    final TestSubscriber<Integer> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    // onNext should emit values.
    source.onNext(1);
    resultTest.assertValues(1);

    source.onNext(2);
    resultTest.assertValues(1, 2);

    // Completing the source stream should not emit values.
    source.onCompleted();
    resultTest.assertValues(1, 2);
  }

  @Test
  public void testErrorsDoNotEmit() {
    final PublishSubject<Integer> source = PublishSubject.create();
    final Observable<Integer> result = source
      .materialize()
      .compose(Transformers.values());

    final TestSubscriber<Integer> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    //
    source.onNext(1);
    resultTest.assertValues(1);

    // An error in the source stream should not emit values.
    source.onError(new Throwable());

    resultTest.assertValues(1);
  }
}
