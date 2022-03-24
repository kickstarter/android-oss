package com.kickstarter

import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.ProjectFactory.caProject
import com.kickstarter.mock.factories.ProjectFactory.ukProject
import com.kickstarter.mock.factories.ConfigFactory.config
import com.kickstarter.libs.KSCurrency
import type.CurrencyCode
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.models.Country
import com.kickstarter.mock.MockCurrentConfig
import java.math.RoundingMode
import junit.framework.TestCase

class KSCurrencyTest : TestCase() {
    fun testFormatCurrency_withUserInCA() {
        val currency = createKSCurrency("CA")
        assertEquals("US$ 100", currency.format(100.9, project()))
        assertEquals("CA$ 100", currency.format(100.9, caProject()))
        assertEquals("£100", currency.format(100.9, ukProject()))
        assertEquals("US$ 100", currency.format(100.1, project()))
        assertEquals("CA$ 100", currency.format(100.1, caProject()))
        assertEquals("£100", currency.format(100.1, ukProject()))
        assertEquals("US$ 100.10", currency.format(100.1, project(), RoundingMode.HALF_UP))
        assertEquals("CA$ 100.10", currency.format(100.1, caProject(), RoundingMode.HALF_UP))
        assertEquals("£100.10", currency.format(100.1, ukProject(), RoundingMode.HALF_UP))
        assertEquals("US$ 100.90", currency.format(100.9, project(), RoundingMode.HALF_UP))
        assertEquals("CA$ 100.90", currency.format(100.9, caProject(), RoundingMode.HALF_UP))
        assertEquals("£100.90", currency.format(100.9, ukProject(), RoundingMode.HALF_UP))
        assertEquals("US$ 100", currency.format(100.0, project(), RoundingMode.HALF_UP))
        assertEquals("CA$ 100", currency.format(100.0, caProject(), RoundingMode.HALF_UP))
        assertEquals("£100", currency.format(100.0, ukProject(), RoundingMode.HALF_UP))
    }

    fun testFormatCurrency_withUserInCA_prefersUSD() {
        val currency = createKSCurrency("CA")
        val preferUSD_USProject = project()
            .toBuilder()
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals(
            "US$ 100",
            currency.format(100.0, preferUSD_USProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "US$ 100.10",
            currency.format(100.1, preferUSD_USProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "US$ 100.90",
            currency.format(100.9, preferUSD_USProject, true, RoundingMode.HALF_UP, true)
        )
        val preferUSD_CAProject = caProject()
            .toBuilder()
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals(
            "US$ 100",
            currency.format(100.0, preferUSD_CAProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "US$ 100.10",
            currency.format(100.1, preferUSD_CAProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "US$ 100.90",
            currency.format(100.9, preferUSD_CAProject, true, RoundingMode.HALF_UP, true)
        )
        val preferUSD_UKProject = ukProject()
            .toBuilder()
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals(
            "US$ 100",
            currency.format(100.0, preferUSD_UKProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "US$ 100.10",
            currency.format(100.1, preferUSD_UKProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "US$ 100.90",
            currency.format(100.9, preferUSD_UKProject, true, RoundingMode.HALF_UP, true)
        )
    }

    fun testFormatCurrency_withUserInUS_prefersUSD() {
        val currency = createKSCurrency("US")
        val preferUSD_USProject = project()
            .toBuilder()
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals(
            "$100",
            currency.format(100.0, preferUSD_USProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "$100.10",
            currency.format(100.1, preferUSD_USProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "$100.90",
            currency.format(100.9, preferUSD_USProject, true, RoundingMode.HALF_UP, true)
        )
        val preferUSD_CAProject = caProject()
            .toBuilder()
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals(
            "$100",
            currency.format(100.0, preferUSD_CAProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "$100.10",
            currency.format(100.1, preferUSD_CAProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "$100.90",
            currency.format(100.9, preferUSD_CAProject, true, RoundingMode.HALF_UP, true)
        )
        val preferUSD_UKProject = ukProject()
            .toBuilder()
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals(
            "$100",
            currency.format(100.0, preferUSD_UKProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "$100.10",
            currency.format(100.1, preferUSD_UKProject, true, RoundingMode.HALF_UP, true)
        )
        assertEquals(
            "$100.90",
            currency.format(100.9, preferUSD_UKProject, true, RoundingMode.HALF_UP, true)
        )
    }

    fun testFormatCurrency_withUserInUK() {
        val currency = createKSCurrency("UK")
        assertEquals("US$ 100", currency.format(100.1, project()))
        assertEquals("CA$ 100", currency.format(100.1, caProject()))
        assertEquals("£100", currency.format(100.1, ukProject()))
        assertEquals("US$ 100", currency.format(100.9, project()))
        assertEquals("CA$ 100", currency.format(100.9, caProject()))
        assertEquals("£100", currency.format(100.9, ukProject()))
        assertEquals("US$ 100.10", currency.format(100.1, project(), RoundingMode.HALF_UP))
        assertEquals("CA$ 100.10", currency.format(100.1, caProject(), RoundingMode.HALF_UP))
        assertEquals("£100.10", currency.format(100.1, ukProject(), RoundingMode.HALF_UP))
        assertEquals("US$ 100.90", currency.format(100.9, project(), RoundingMode.HALF_UP))
        assertEquals("CA$ 100.90", currency.format(100.9, caProject(), RoundingMode.HALF_UP))
        assertEquals("£100.90", currency.format(100.9, ukProject(), RoundingMode.HALF_UP))
        assertEquals("US$ 100", currency.format(100.0, project(), RoundingMode.HALF_UP))
        assertEquals("CA$ 100", currency.format(100.0, caProject(), RoundingMode.HALF_UP))
        assertEquals("£100", currency.format(100.0, ukProject(), RoundingMode.HALF_UP))
    }

    fun testFormatCurrency_withUserInUnlaunchedCountry() {
        val currency = createKSCurrency("XX")
        assertEquals("US$ 100", currency.format(100.1, project()))
        assertEquals("CA$ 100", currency.format(100.1, caProject()))
        assertEquals("£100", currency.format(100.1, ukProject()))
        assertEquals("US$ 100", currency.format(100.9, project()))
        assertEquals("CA$ 100", currency.format(100.9, caProject()))
        assertEquals("£100", currency.format(100.9, ukProject()))
        assertEquals("US$ 100.10", currency.format(100.1, project(), RoundingMode.HALF_UP))
        assertEquals("CA$ 100.10", currency.format(100.1, caProject(), RoundingMode.HALF_UP))
        assertEquals("£100.10", currency.format(100.1, ukProject(), RoundingMode.HALF_UP))
        assertEquals("US$ 100.90", currency.format(100.9, project(), RoundingMode.HALF_UP))
        assertEquals("CA$ 100.90", currency.format(100.9, caProject(), RoundingMode.HALF_UP))
        assertEquals("£100.90", currency.format(100.9, ukProject(), RoundingMode.HALF_UP))
        assertEquals("US$ 100", currency.format(100.0, project(), RoundingMode.HALF_UP))
        assertEquals("CA$ 100", currency.format(100.0, caProject(), RoundingMode.HALF_UP))
        assertEquals("£100", currency.format(100.0, ukProject(), RoundingMode.HALF_UP))
    }

    fun testPreferUSD_withUserInUS() {
        val currency = createKSCurrency("US")
        val preferUSD_USProject = project()
            .toBuilder()
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("$100", currency.formatWithUserPreference(100.1, preferUSD_USProject))
        assertEquals("$100", currency.formatWithUserPreference(100.9, preferUSD_USProject))
        assertEquals(
            "$100",
            currency.formatWithUserPreference(100.1, preferUSD_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "$100.10",
            currency.formatWithUserPreference(100.1, preferUSD_USProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "$101",
            currency.formatWithUserPreference(100.9, preferUSD_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "$100.90",
            currency.formatWithUserPreference(100.9, preferUSD_USProject, RoundingMode.HALF_UP, 2)
        )
        val preferUSD_CAProject = caProject()
            .toBuilder()
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("$75", currency.formatWithUserPreference(100.1, preferUSD_CAProject))
        assertEquals("$75", currency.formatWithUserPreference(100.9, preferUSD_CAProject))
        assertEquals(
            "$75",
            currency.formatWithUserPreference(100.1, preferUSD_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "$75.07",
            currency.formatWithUserPreference(100.1, preferUSD_CAProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "$76",
            currency.formatWithUserPreference(100.9, preferUSD_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "$75.68",
            currency.formatWithUserPreference(100.9, preferUSD_CAProject, RoundingMode.HALF_UP, 2)
        )
        val preferUSD_UKProject = ukProject()
            .toBuilder()
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("$150", currency.formatWithUserPreference(100.1, preferUSD_UKProject))
        assertEquals("$150", currency.formatWithUserPreference(100.9, preferUSD_UKProject))
        assertEquals(
            "$150",
            currency.formatWithUserPreference(100.1, preferUSD_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "$150.15",
            currency.formatWithUserPreference(100.1, preferUSD_UKProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "$151",
            currency.formatWithUserPreference(100.9, preferUSD_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "$151.35",
            currency.formatWithUserPreference(100.9, preferUSD_UKProject, RoundingMode.HALF_UP, 2)
        )
    }

    fun testPreferUSD_withUserInCA() {
        val currency = createKSCurrency("CA")
        val preferUSD_USProject = project()
            .toBuilder()
            .fxRate(1f)
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("US$ 100", currency.formatWithUserPreference(100.1, preferUSD_USProject))
        assertEquals("US$ 100", currency.formatWithUserPreference(100.9, preferUSD_USProject))
        assertEquals(
            "US$ 100",
            currency.formatWithUserPreference(100.1, preferUSD_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 100.10",
            currency.formatWithUserPreference(100.1, preferUSD_USProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "US$ 101",
            currency.formatWithUserPreference(100.9, preferUSD_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 100.90",
            currency.formatWithUserPreference(100.9, preferUSD_USProject, RoundingMode.HALF_UP, 2)
        )
        val preferUSD_CAProject = caProject()
            .toBuilder()
            .fxRate(.75f)
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("US$ 75", currency.formatWithUserPreference(100.1, preferUSD_CAProject))
        assertEquals("US$ 75", currency.formatWithUserPreference(100.9, preferUSD_CAProject))
        assertEquals(
            "US$ 75",
            currency.formatWithUserPreference(100.1, preferUSD_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 75.07",
            currency.formatWithUserPreference(100.1, preferUSD_CAProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "US$ 76",
            currency.formatWithUserPreference(100.9, preferUSD_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 75.68",
            currency.formatWithUserPreference(100.9, preferUSD_CAProject, RoundingMode.HALF_UP, 2)
        )
        val preferUSD_UKProject = ukProject()
            .toBuilder()
            .fxRate(1.5f)
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("US$ 150", currency.formatWithUserPreference(100.1, preferUSD_UKProject))
        assertEquals("US$ 150", currency.formatWithUserPreference(100.9, preferUSD_UKProject))
        assertEquals(
            "US$ 150",
            currency.formatWithUserPreference(100.1, preferUSD_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 150.15",
            currency.formatWithUserPreference(100.1, preferUSD_UKProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "US$ 151",
            currency.formatWithUserPreference(100.9, preferUSD_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 151.35",
            currency.formatWithUserPreference(100.9, preferUSD_UKProject, RoundingMode.HALF_UP, 2)
        )
    }

    fun testPreferUSD_withUserInUK() {
        val currency = createKSCurrency("UK")
        val preferUSD_USProject = project()
            .toBuilder()
            .fxRate(1f)
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("US$ 100", currency.formatWithUserPreference(100.1, preferUSD_USProject))
        assertEquals("US$ 100", currency.formatWithUserPreference(100.9, preferUSD_USProject))
        assertEquals(
            "US$ 100",
            currency.formatWithUserPreference(100.1, preferUSD_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 100.10",
            currency.formatWithUserPreference(100.1, preferUSD_USProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "US$ 101",
            currency.formatWithUserPreference(100.9, preferUSD_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 100.90",
            currency.formatWithUserPreference(100.9, preferUSD_USProject, RoundingMode.HALF_UP, 2)
        )
        val preferUSD_CAProject = caProject()
            .toBuilder()
            .fxRate(.75f)
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("US$ 75", currency.formatWithUserPreference(100.1, preferUSD_CAProject))
        assertEquals("US$ 75", currency.formatWithUserPreference(100.9, preferUSD_CAProject))
        assertEquals(
            "US$ 75",
            currency.formatWithUserPreference(100.1, preferUSD_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 75.07",
            currency.formatWithUserPreference(100.1, preferUSD_CAProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "US$ 76",
            currency.formatWithUserPreference(100.9, preferUSD_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 75.68",
            currency.formatWithUserPreference(100.9, preferUSD_CAProject, RoundingMode.HALF_UP, 2)
        )
        val preferUSD_UKProject = ukProject()
            .toBuilder()
            .fxRate(1.5f)
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("US$ 150", currency.formatWithUserPreference(100.1, preferUSD_UKProject))
        assertEquals("US$ 150", currency.formatWithUserPreference(100.9, preferUSD_UKProject))
        assertEquals(
            "US$ 150",
            currency.formatWithUserPreference(100.1, preferUSD_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 150.15",
            currency.formatWithUserPreference(100.1, preferUSD_UKProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "US$ 151",
            currency.formatWithUserPreference(100.9, preferUSD_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 151.35",
            currency.formatWithUserPreference(100.9, preferUSD_UKProject, RoundingMode.HALF_UP, 2)
        )
    }

    fun testPreferUSD_withUserInUnlaunchedCountry() {
        val currency = createKSCurrency("XX")
        val preferUSD_USProject = project()
            .toBuilder()
            .fxRate(1f)
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("US$ 100", currency.formatWithUserPreference(100.1, preferUSD_USProject))
        assertEquals("US$ 100", currency.formatWithUserPreference(100.9, preferUSD_USProject))
        assertEquals(
            "US$ 100",
            currency.formatWithUserPreference(100.1, preferUSD_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 100.10",
            currency.formatWithUserPreference(100.1, preferUSD_USProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "US$ 101",
            currency.formatWithUserPreference(100.9, preferUSD_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 100.90",
            currency.formatWithUserPreference(100.9, preferUSD_USProject, RoundingMode.HALF_UP, 2)
        )
        val preferUSD_CAProject = caProject()
            .toBuilder()
            .fxRate(.75f)
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("US$ 75", currency.formatWithUserPreference(100.1, preferUSD_CAProject))
        assertEquals("US$ 75", currency.formatWithUserPreference(100.9, preferUSD_CAProject))
        assertEquals(
            "US$ 75",
            currency.formatWithUserPreference(100.1, preferUSD_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 75.07",
            currency.formatWithUserPreference(100.1, preferUSD_CAProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "US$ 76",
            currency.formatWithUserPreference(100.9, preferUSD_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 75.68",
            currency.formatWithUserPreference(100.9, preferUSD_CAProject, RoundingMode.HALF_UP, 2)
        )
        val preferUSD_UKProject = ukProject()
            .toBuilder()
            .fxRate(1.5f)
            .currentCurrency(CurrencyCode.USD.rawValue())
            .build()
        assertEquals("US$ 150", currency.formatWithUserPreference(100.1, preferUSD_UKProject))
        assertEquals("US$ 150", currency.formatWithUserPreference(100.9, preferUSD_UKProject))
        assertEquals(
            "US$ 150",
            currency.formatWithUserPreference(100.1, preferUSD_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 150.15",
            currency.formatWithUserPreference(100.1, preferUSD_UKProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "US$ 151",
            currency.formatWithUserPreference(100.9, preferUSD_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "US$ 151.35",
            currency.formatWithUserPreference(100.9, preferUSD_UKProject, RoundingMode.HALF_UP, 2)
        )
    }

    fun testPreferCAD_withUserInCA() {
        val currency = createKSCurrency("CA")
        val preferCAD_USProject = project()
            .toBuilder()
            .fxRate(1.5f)
            .currentCurrency(CurrencyCode.CAD.rawValue())
            .build()
        assertEquals("CA$ 150", currency.formatWithUserPreference(100.1, preferCAD_USProject))
        assertEquals("CA$ 150", currency.formatWithUserPreference(100.9, preferCAD_USProject))
        assertEquals(
            "CA$ 150",
            currency.formatWithUserPreference(100.1, preferCAD_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "CA$ 150.15",
            currency.formatWithUserPreference(100.1, preferCAD_USProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "CA$ 151",
            currency.formatWithUserPreference(100.9, preferCAD_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "CA$ 151.35",
            currency.formatWithUserPreference(100.9, preferCAD_USProject, RoundingMode.HALF_UP, 2)
        )
        val preferCAD_CAProject = caProject()
            .toBuilder()
            .fxRate(1f)
            .currentCurrency(CurrencyCode.CAD.rawValue())
            .build()
        assertEquals("CA$ 100", currency.formatWithUserPreference(100.1, preferCAD_CAProject))
        assertEquals("CA$ 100", currency.formatWithUserPreference(100.9, preferCAD_CAProject))
        assertEquals(
            "CA$ 100",
            currency.formatWithUserPreference(100.1, preferCAD_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "CA$ 100.10",
            currency.formatWithUserPreference(100.1, preferCAD_CAProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "CA$ 101",
            currency.formatWithUserPreference(100.9, preferCAD_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "CA$ 100.90",
            currency.formatWithUserPreference(100.9, preferCAD_CAProject, RoundingMode.HALF_UP, 2)
        )
        val preferCAD_UKProject = ukProject()
            .toBuilder()
            .fxRate(.75f)
            .currentCurrency(CurrencyCode.CAD.rawValue())
            .build()
        assertEquals("CA$ 75", currency.formatWithUserPreference(100.1, preferCAD_UKProject))
        assertEquals("CA$ 75", currency.formatWithUserPreference(100.9, preferCAD_UKProject))
        assertEquals(
            "CA$ 75",
            currency.formatWithUserPreference(100.1, preferCAD_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "CA$ 75.07",
            currency.formatWithUserPreference(100.1, preferCAD_UKProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "CA$ 76",
            currency.formatWithUserPreference(100.9, preferCAD_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "CA$ 75.68",
            currency.formatWithUserPreference(100.9, preferCAD_UKProject, RoundingMode.HALF_UP, 2)
        )
    }

    fun testPreferGBP_withUserInUK() {
        val currency = createKSCurrency("UK")
        val preferGBP_USProject = project()
            .toBuilder()
            .fxRate(.75f)
            .currentCurrency(CurrencyCode.GBP.rawValue())
            .build()
        assertEquals("£75", currency.formatWithUserPreference(100.1, preferGBP_USProject))
        assertEquals("£75", currency.formatWithUserPreference(100.9, preferGBP_USProject))
        assertEquals(
            "£75",
            currency.formatWithUserPreference(100.1, preferGBP_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "£75.07",
            currency.formatWithUserPreference(100.1, preferGBP_USProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "£76",
            currency.formatWithUserPreference(100.9, preferGBP_USProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "£75.68",
            currency.formatWithUserPreference(100.9, preferGBP_USProject, RoundingMode.HALF_UP, 2)
        )
        val preferGBP_CAProject = caProject()
            .toBuilder()
            .fxRate(1.5f)
            .currentCurrency(CurrencyCode.GBP.rawValue())
            .build()
        assertEquals("£150", currency.formatWithUserPreference(100.1, preferGBP_CAProject))
        assertEquals("£150", currency.formatWithUserPreference(100.9, preferGBP_CAProject))
        assertEquals(
            "£150",
            currency.formatWithUserPreference(100.1, preferGBP_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "£150.15",
            currency.formatWithUserPreference(100.1, preferGBP_CAProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "£151",
            currency.formatWithUserPreference(100.9, preferGBP_CAProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "£151.35",
            currency.formatWithUserPreference(100.9, preferGBP_CAProject, RoundingMode.HALF_UP, 2)
        )
        val preferGBP_UKProject = ukProject()
            .toBuilder()
            .fxRate(1f)
            .currentCurrency(CurrencyCode.GBP.rawValue())
            .build()
        assertEquals("£100", currency.formatWithUserPreference(100.1, preferGBP_UKProject))
        assertEquals("£100", currency.formatWithUserPreference(100.9, preferGBP_UKProject))
        assertEquals(
            "£100",
            currency.formatWithUserPreference(100.1, preferGBP_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "£100.10",
            currency.formatWithUserPreference(100.1, preferGBP_UKProject, RoundingMode.HALF_UP, 2)
        )
        assertEquals(
            "£101",
            currency.formatWithUserPreference(100.9, preferGBP_UKProject, RoundingMode.HALF_UP, 0)
        )
        assertEquals(
            "£100.90",
            currency.formatWithUserPreference(100.9, preferGBP_UKProject, RoundingMode.HALF_UP, 2)
        )
    }

    fun testFormatCurrency_withCurrencyCodeExcluded() {
        val caCurrency = createKSCurrency("CA")
        assertEquals("US$ 100", caCurrency.format(100.0, project(), true))
        assertEquals("US$ 100", caCurrency.format(100.0, project(), false))
        assertEquals("CA$ 100", caCurrency.format(100.0, caProject(), true))
        assertEquals("CA$ 100", caCurrency.format(100.0, caProject(), false))
        val usCurrency = createKSCurrency("US")
        assertEquals("$100", usCurrency.format(100.0, project(), true))
        assertEquals("US$ 100", usCurrency.format(100.0, project(), false))
        assertEquals("CA$ 100", usCurrency.format(100.0, caProject(), true))
        assertEquals("CA$ 100", usCurrency.format(100.0, caProject(), false))
    }

    fun testCurrencyNeedsCode() {
        val usCurrency = createKSCurrency("US")
        assertFalse(usCurrency.currencyNeedsCode(Country.US, true))
        assertTrue(usCurrency.currencyNeedsCode(Country.US, false))
        val caCurrency = createKSCurrency("CA")
        assertTrue(caCurrency.currencyNeedsCode(Country.US, true))
        assertTrue(caCurrency.currencyNeedsCode(Country.US, false))
        val unlaunchedCurrency = createKSCurrency("XX")
        assertTrue(unlaunchedCurrency.currencyNeedsCode(Country.US, true))
        assertTrue(unlaunchedCurrency.currencyNeedsCode(Country.US, false))
    }

    fun testGetSymbolForCurrency() {
        val usCurrency = createKSCurrency("US")
        // US people looking at US currency just get the currency symbol.
        assertEquals("$", usCurrency.getCurrencySymbol(Country.US, true))
        assertEquals("\u00A0US$\u00A0", usCurrency.getCurrencySymbol(Country.US, false))
        // Singapore projects get a special currency prefix
        assertEquals("\u00A0S$\u00A0", usCurrency.getCurrencySymbol(Country.SG, false))
        // Kroner projects use the currency code prefix
        assertEquals("\u00A0CHF\u00A0", usCurrency.getCurrencySymbol(Country.CH, false))
        assertEquals("\u00A0DKK\u00A0", usCurrency.getCurrencySymbol(Country.DK, false))
        assertEquals("\u00A0NOK\u00A0", usCurrency.getCurrencySymbol(Country.NO, false))
        assertEquals("\u00A0SEK\u00A0", usCurrency.getCurrencySymbol(Country.SE, false))
        // Everything else
        assertEquals("\u00A0MX$\u00A0", usCurrency.getCurrencySymbol(Country.MX, false))
        val caCurrency = createKSCurrency("CA")
        assertEquals("\u00A0US$\u00A0", caCurrency.getCurrencySymbol(Country.US, true))
        assertEquals("\u00A0US$\u00A0", caCurrency.getCurrencySymbol(Country.US, false))
        val unlaunchedCurrency = createKSCurrency("XX")
        assertEquals("\u00A0US$\u00A0", unlaunchedCurrency.getCurrencySymbol(Country.US, true))
        assertEquals("\u00A0US$\u00A0", unlaunchedCurrency.getCurrencySymbol(Country.US, false))
    }

    companion object {
        private fun createKSCurrency(countryCode: String): KSCurrency {
            val config = config().toBuilder()
                .countryCode(countryCode)
                .build()
            val currentConfig: CurrentConfigType = MockCurrentConfig()
            currentConfig.config(config)
            return KSCurrency(currentConfig)
        }
    }
}