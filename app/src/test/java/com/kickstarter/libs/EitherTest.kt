package com.kickstarter.libs

import org.junit.Assert.*
import org.junit.Test

class EitherTest {

    @Test fun testEither_CaseAnalysis() {
        val square: (Int) -> Int = { it * it }
        val length: (String) -> Int = String::length

        assertEquals(9, Either.Left<Int, String>(3).either(ifLeft = square, ifRight = length))
        assertEquals(5, Either.Right<Int, String>("hello").either(ifLeft = square, ifRight = length))
    }

    @Test fun testEither_IsLeft() {
        val intOrString = Either.Left<Int, String>(1)
        assertTrue(intOrString.isLeft())
        assertFalse(intOrString.isRight())
    }

    @Test fun testEither_IsRight() {
        val intOrString = Either.Right<Int, String>("hello")
        assertTrue(intOrString.isRight())
        assertFalse(intOrString.isLeft())
    }

    @Test fun testEither_Left() {
        val intOrString = Either.Left<Int, String>(1)
        assertEquals(1, intOrString.left())
    }

    @Test fun testEither_Map() {
        val double: (String) -> String = { it + it }

        assertEquals(
            Either.Right<Int, String>("hellohello").right(),
            Either.Right<Int, String>("hello").map(double).right()
        )
    }

    @Test fun testEither_MapLeft() {
        val square: (Int) -> Int = { it * it }
        assertEquals(
            Either.Left<Int, String>(9).left(),
            Either.Left<Int, String>(3).mapLeft(square).left()
        )
    }

    @Test fun testEither_Right() {
        val intOrString = Either.Right<Int, String>("hello")
        assertEquals("hello", intOrString.right())
    }
}
