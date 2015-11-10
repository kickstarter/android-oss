package com.kickstarter.libs.rx.transformers;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public class TakeWhenTransformerTest {

  @Test
  public void testTakeWhen_sourceEmitsFirst() {

    final PublishSubject<Integer> source = PublishSubject.create();
    final PublishSubject<Void> sample = PublishSubject.create();
    final Observable<Integer> result = source.compose(Transformers.takeWhen(sample));

    final TestSubscriber<Integer> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    source.onNext(1);
    resultTest.assertNoValues();

    source.onNext(2);
    resultTest.assertNoValues();

    sample.onNext(null);
    resultTest.assertValues(2);

    sample.onNext(null);
    resultTest.assertValues(2, 2);

    source.onNext(3);
    resultTest.assertValues(2, 2);

    sample.onNext(null);
    resultTest.assertValues(2, 2, 3);
  }

  @Test
  public void testTakeWhen_sourceEmitsSecond() {

    final PublishSubject<Integer> source = PublishSubject.create();
    final PublishSubject<Void> sample = PublishSubject.create();
    final Observable<Integer> result = source.compose(Transformers.takeWhen(sample));

    final TestSubscriber<Integer> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    sample.onNext(null);
    resultTest.assertNoValues();

    sample.onNext(null);
    resultTest.assertNoValues();

    source.onNext(1);
    resultTest.assertNoValues();

    sample.onNext(null);
    resultTest.assertValues(1);

    source.onNext(2);
    resultTest.assertValues(1);

    sample.onNext(null);
    resultTest.assertValues(1, 2);

    source.onNext(3);
    resultTest.assertValues(1, 2);

    sample.onNext(null);
    resultTest.assertValues(1, 2, 3);
  }
}
