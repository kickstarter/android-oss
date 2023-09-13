package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertThrows
import org.junit.Test
import io.reactivex.subjects.PublishSubject

class AnyExtKtTest : KSRobolectricTestCase() {

    val disposables = CompositeDisposable()

    @Test
    fun testIsNull() {
        val nullVal : Any? = null
        assertTrue(nullVal.isNull())

        val nonNullVal : Any = "hello"

        assertFalse(nonNullVal.isNull())
    }

    @Test
    fun testIsNotNull() {
        val nullVal : Any? = null
        assertFalse(nullVal.isNotNull())

        val nonNullVal : Any = "hello"

        assertTrue(nonNullVal.isNotNull())
    }

    @Test
    fun testCoalesce() {
        val hello : Any = "hello"
        val thing: Any = "thing"
        assertEquals(hello, hello.coalesce(thing))

        assertEquals(thing, null.coalesce(thing))
    }

    @Test
    fun testCoalesceWith() {
        val theDefault = 100
        val source = rx.subjects.PublishSubject.create<Int?>()
        val result = source
                .map(coalesceWith(theDefault))

        val resultTest = TestSubscriber.create<Int>()
        result.subscribe { resultTest.onNext(it) }

        source.onNext(1)
        resultTest.assertValue(1)

        source.onNext(2)
        resultTest.assertValues(1, 2)

        source.onNext(null)
        resultTest.assertValues(1, 2, theDefault)
    }

    @Test
    fun testCoalesceWithV2() {
        val theDefault = "100"
        val source : PublishSubject<Any> = PublishSubject.create()
        val result : Observable<Any> = source
                .map(coalesceWithV2(theDefault))

        val resultTest = TestSubscriber.create<Any>()
        result.subscribe { resultTest.onNext(it) }.addToDisposable(disposables)

        source.onNext("1")
        resultTest.assertValue("1")

        source.onNext("2")
        resultTest.assertValues("1", "2")

        source.onNext("")
        resultTest.assertValues("1", "2", theDefault)
    }

    @Test
    fun testNumToString() {
        val long = 5L
        val float = 5.5F
        val int = 12324
        val double = 6.43
        val notNum = "gdfgdgf"

        assertEquals("5", long.numToString())
        assertEquals("5.5", float.numToString())
        assertEquals("12324", int.numToString())
        assertEquals("6.43", double.numToString())
        assertNull(null, notNum.numToString())
    }

    @Test
    fun testRequireNonNull() {
        val string = "string"
        val nullString = null
        assertEquals(string, string.requireNonNull(String.javaClass))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            nullString.requireNonNull(String.Companion::class.java)
        }

        assertEquals(exception.message, "class kotlin.jvm.internal.StringCompanionObject required to be non-null.")
    }
}