package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.models.AiDisclosure
import io.reactivex.disposables.CompositeDisposable
import org.junit.Test

class AiDisclosureExtTest : KSRobolectricTestCase() {

    val disposables = CompositeDisposable()

    @Test
    fun testEmptyValues() {
        val nullVal: AiDisclosure? = null
        assertTrue(nullVal.isNull())

        val emptyVal = AiDisclosure.builder().build()
        assertTrue(emptyVal.isUIEmptyValues())
    }
}
