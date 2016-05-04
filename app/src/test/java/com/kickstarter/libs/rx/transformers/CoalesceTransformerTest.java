package com.kickstarter.libs.rx.transformers;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.coalesceTransformer;

public final class CoalesceTransformerTest {
  @Test
  public void testCoalesceTransformer() {
    final Integer theDefault = 100;
    final PublishSubject<Integer> source = PublishSubject.create();
    final Observable<Integer> result = source
      .compose(coalesceTransformer(theDefault));

    final TestSubscriber<Integer> resultTest = TestSubscriber.create();
    result.subscribe(resultTest);

    source.onNext(1);
    resultTest.assertValue(1);

    source.onNext(2);
    resultTest.assertValues(1, 2);

    source.onNext(null);
    resultTest.assertValues(1, 2, theDefault);
  }
}
