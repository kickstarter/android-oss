package com.kickstarter.libs.rx.transformers

import com.kickstarter.libs.utils.extensions.addToDisposable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class TakeWhenTransformerTest {
    private val disposables = CompositeDisposable()

    @Test
    fun testTakeWhen_sourceEmitsFirst() {
        val source = PublishSubject.create<Int>()
        val sample = PublishSubject.create<Unit>()
        val result = source.compose(Transformers.takeWhenV2(sample))

        val resultTest = TestSubscriber<Int>()
        result.subscribe { resultTest.onNext(it) }.addToDisposable(disposables)

        source.onNext(1)
        resultTest.assertNoValues()

        source.onNext(2)
        resultTest.assertNoValues()

        sample.onNext(Unit)
        resultTest.assertValues(2)

        sample.onNext(Unit)
        resultTest.assertValues(2, 2)

        source.onNext(3)
        resultTest.assertValues(2, 2)

        sample.onNext(Unit)
        resultTest.assertValues(2, 2, 3)
    }

    @Test
    fun testTakeWhen_sourceEmitsSecond() {
        val source = PublishSubject.create<Int>()
        val sample = PublishSubject.create<Unit>()
        val result = source.compose(Transformers.takeWhenV2(sample))

        val resultTest = TestSubscriber<Int>()
        result.subscribe { resultTest.onNext(it) }.addToDisposable(disposables)

        sample.onNext(Unit)
        resultTest.assertNoValues()

        sample.onNext(Unit)
        resultTest.assertNoValues()

        source.onNext(1)
        resultTest.assertNoValues()

        sample.onNext(Unit)
        resultTest.assertValues(1)

        source.onNext(2)
        resultTest.assertValues(1)

        sample.onNext(Unit)
        resultTest.assertValues(1, 2)

        source.onNext(3)
        resultTest.assertValues(1, 2)

        sample.onNext(Unit)
        resultTest.assertValues(1, 2, 3)
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
