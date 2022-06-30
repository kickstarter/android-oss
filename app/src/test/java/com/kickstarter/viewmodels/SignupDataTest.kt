package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.viewmodels.SignupViewModel.ViewModel.SignupData
import org.junit.Test

class SignupDataTest : KSRobolectricTestCase() {
    @Test
    fun testSignupData_isValid() {
        assertTrue(SignupData("brando", "b@kickstarter.com", "danisawesome", true).isValid)
        assertFalse(SignupData("", "b@kickstarter.com", "danisawesome", true).isValid)
        assertFalse(SignupData("brando", "b@kickstarter", "danisawesome", true).isValid)
        assertFalse(SignupData("brando", "b@kickstarter.com", "dan", true).isValid)
        assertTrue(SignupData("brando", "b@kickstarter.com", "danisawesome", false).isValid)
        assertFalse(SignupData("", "b@kickstarter.com", "danisawesome", false).isValid)
        assertFalse(SignupData("brando", "b@kickstarter", "danisawesome", false).isValid)
        assertFalse(SignupData("brando", "b@kickstarter.com", "dan", false).isValid)
    }
}
