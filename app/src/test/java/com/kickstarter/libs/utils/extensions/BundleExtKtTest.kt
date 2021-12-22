package com.kickstarter.libs.utils.extensions

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class BundleExtKtTest : KSRobolectricTestCase() {

    @Test
    fun testMaybeGetBundle() {
        val viewModelBundle = Bundle()
        val bundle = Bundle()
        bundle.putBundle("viewModel", viewModelBundle)
        assertEquals(bundle.maybeGetBundle("viewModel"), viewModelBundle)
        assertEquals(bundle.maybeGetBundle(""), null)
        assertEquals(bundle.maybeGetBundle("view_model_state"), null)
    }
}
