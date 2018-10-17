package com.kickstarter

import com.kickstarter.libs.UserCurrency
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import junit.framework.Assert
import junit.framework.TestCase
import type.CurrencyCode

class UserCurrencyTest: TestCase() {

    private fun createUserCurrency(countryCode: String): UserCurrency {
        val config = ConfigFactory.config().toBuilder()
                .countryCode(countryCode)
                .build()

        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        return UserCurrency(currentConfig)
    }

    fun testFormatCurrency_withUserInUS() {
        val currency = createUserCurrency("US")
        Assert.assertEquals("$100", currency.format(100.0f, ProjectFactory.project(), CurrencyCode.USD.rawValue()))
        Assert.assertEquals("CA$100", currency.format(100.0f, ProjectFactory.caProject(), CurrencyCode.CAD.rawValue()))
        Assert.assertEquals("£100", currency.format(100.0f, ProjectFactory.ukProject(), CurrencyCode.GBP.rawValue()))
    }

    fun testFormatCurrency_withUserInCA() {
        val currency = createUserCurrency("CA")
        Assert.assertEquals("US$100", currency.format(100.0f, ProjectFactory.project(), CurrencyCode.USD.rawValue()))
        Assert.assertEquals("CA$100", currency.format(100.0f, ProjectFactory.caProject(), CurrencyCode.CAD.rawValue()))
        Assert.assertEquals("£100", currency.format(100.0f, ProjectFactory.ukProject(), CurrencyCode.GBP.rawValue()))
    }

    fun testFormatCurrency_withUserInUK() {
        val currency = createUserCurrency("UK")
        Assert.assertEquals("US$100", currency.format(100.0f, ProjectFactory.project(), CurrencyCode.USD.rawValue()))
        Assert.assertEquals("CA$100", currency.format(100.0f, ProjectFactory.caProject(), CurrencyCode.CAD.rawValue()))
        Assert.assertEquals("£100", currency.format(100.0f, ProjectFactory.ukProject(), CurrencyCode.GBP.rawValue()))
    }

    fun testFormatCurrency_withUserInUnlaunchedCountry() {
        val currency = createUserCurrency("XX")
        Assert.assertEquals("US$100", currency.format(100.0f, ProjectFactory.project(), CurrencyCode.USD.rawValue()))
        Assert.assertEquals("US$100", currency.format(100.0f, ProjectFactory.caProject(), CurrencyCode.USD.rawValue()))
        Assert.assertEquals("US$100", currency.format(100.0f, ProjectFactory.ukProject(), CurrencyCode.USD.rawValue()))
    }

    fun testFormatCurrency_roundsDown() {
        val currency = createUserCurrency("US")
        val project = ProjectFactory.project()
        Assert.assertEquals("$100", currency.format(100.4f, project, CurrencyCode.USD.rawValue()))
        Assert.assertEquals("$100", currency.format(100.5f, project, CurrencyCode.USD.rawValue()))
        Assert.assertEquals("$101", currency.format(101.5f, project, CurrencyCode.USD.rawValue()))
        Assert.assertEquals("$100", currency.format(100.9f, project, CurrencyCode.USD.rawValue()))
    }
}
