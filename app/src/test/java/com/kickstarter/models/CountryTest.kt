package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.models.Country
import junit.framework.TestCase
import org.junit.Test

 class CountryTest : KSRobolectricTestCase() {

    @Test
    fun testAT() {
        TestCase.assertEquals("AT", Country.AT.countryCode)
        TestCase.assertEquals("EUR", Country.AT.currencyCode)
        TestCase.assertEquals("€", Country.AT.currencySymbol)
        TestCase.assertEquals(1, Country.AT.minPledge)
        TestCase.assertEquals(8_500, Country.AT.maxPledge)
        TestCase.assertEquals(false, Country.AT.trailingCode)
    }

    @Test
    fun testAU() {
        TestCase.assertEquals("AU", Country.AU.countryCode)
        TestCase.assertEquals("AUD", Country.AU.currencyCode)
        TestCase.assertEquals("$", Country.AU.currencySymbol)
        TestCase.assertEquals(1, Country.AU.minPledge)
        TestCase.assertEquals(13_000, Country.AU.maxPledge)
        TestCase.assertEquals(true, Country.AU.trailingCode)
    }

    @Test
    fun testBE() {
        TestCase.assertEquals("BE", Country.BE.countryCode)
        TestCase.assertEquals("EUR", Country.BE.currencyCode)
        TestCase.assertEquals("€", Country.BE.currencySymbol)
        TestCase.assertEquals(1, Country.BE.minPledge)
        TestCase.assertEquals(8_500, Country.BE.maxPledge)
        TestCase.assertEquals(false, Country.BE.trailingCode)
    }

     @Test
     fun testCA() {
         TestCase.assertEquals("CA", Country.CA.countryCode)
         TestCase.assertEquals("CAD", Country.CA.currencyCode)
         TestCase.assertEquals("$", Country.CA.currencySymbol)
         TestCase.assertEquals(1, Country.CA.minPledge)
         TestCase.assertEquals(13_000, Country.CA.maxPledge)
         TestCase.assertEquals(true, Country.CA.trailingCode)
     }

     @Test
     fun testCH() {
         TestCase.assertEquals("CH", Country.CH.countryCode)
         TestCase.assertEquals("CHF", Country.CH.currencyCode)
         TestCase.assertEquals("Fr", Country.CH.currencySymbol)
         TestCase.assertEquals(1, Country.CH.minPledge)
         TestCase.assertEquals(9_500, Country.CH.maxPledge)
         TestCase.assertEquals(true, Country.CH.trailingCode)
     }

     @Test
     fun testDE() {
         TestCase.assertEquals("DE", Country.DE.countryCode)
         TestCase.assertEquals("EUR", Country.DE.currencyCode)
         TestCase.assertEquals("€", Country.DE.currencySymbol)
         TestCase.assertEquals(1, Country.DE.minPledge)
         TestCase.assertEquals(8_500, Country.DE.maxPledge)
         TestCase.assertEquals(false, Country.DE.trailingCode)
     }

     @Test
     fun testDK() {
         TestCase.assertEquals("DK", Country.DK.countryCode)
         TestCase.assertEquals("DKK", Country.DK.currencyCode)
         TestCase.assertEquals("kr", Country.DK.currencySymbol)
         TestCase.assertEquals(5, Country.DK.minPledge)
         TestCase.assertEquals(65_000, Country.DK.maxPledge)
         TestCase.assertEquals(true, Country.DK.trailingCode)
     }

     @Test
     fun testES() {
         TestCase.assertEquals("ES", Country.ES.countryCode)
         TestCase.assertEquals("EUR", Country.ES.currencyCode)
         TestCase.assertEquals("€", Country.ES.currencySymbol)
         TestCase.assertEquals(1, Country.ES.minPledge)
         TestCase.assertEquals(8_500, Country.ES.maxPledge)
         TestCase.assertEquals(false, Country.ES.trailingCode)
     }

     @Test
     fun testFR() {
         TestCase.assertEquals("FR", Country.FR.countryCode)
         TestCase.assertEquals("EUR", Country.FR.currencyCode)
         TestCase.assertEquals("€", Country.FR.currencySymbol)
         TestCase.assertEquals(1, Country.FR.minPledge)
         TestCase.assertEquals(8_500, Country.FR.maxPledge)
         TestCase.assertEquals(false, Country.FR.trailingCode)
     }

     @Test
     fun testGB() {
         TestCase.assertEquals("GB", Country.GB.countryCode)
             TestCase.assertEquals("GBP", Country.GB.currencyCode)
         TestCase.assertEquals("£", Country.GB.currencySymbol)
         TestCase.assertEquals(1, Country.GB.minPledge)
         TestCase.assertEquals(8_000, Country.GB.maxPledge)
         TestCase.assertEquals(false, Country.GB.trailingCode)
     }

     @Test
     fun testHK() {
         TestCase.assertEquals("HK", Country.HK.countryCode)
         TestCase.assertEquals("HKD", Country.HK.currencyCode)
         TestCase.assertEquals("$", Country.HK.currencySymbol)
         TestCase.assertEquals(10, Country.HK.minPledge)
         TestCase.assertEquals(75_000, Country.HK.maxPledge)
         TestCase.assertEquals(true, Country.HK.trailingCode)
     }

     @Test
     fun testIE() {
         TestCase.assertEquals("IE", Country.IE.countryCode)
         TestCase.assertEquals("EUR", Country.IE.currencyCode)
         TestCase.assertEquals("€", Country.IE.currencySymbol)
         TestCase.assertEquals(1, Country.IE.minPledge)
         TestCase.assertEquals(8_500, Country.IE.maxPledge)
         TestCase.assertEquals(false, Country.IE.trailingCode)
     }

     @Test
     fun testIT() {
         TestCase.assertEquals("IT", Country.IT.countryCode)
         TestCase.assertEquals("EUR", Country.IT.currencyCode)
         TestCase.assertEquals("€", Country.IT.currencySymbol)
         TestCase.assertEquals(1, Country.IT.minPledge)
         TestCase.assertEquals(8_500, Country.IT.maxPledge)
         TestCase.assertEquals(false, Country.IT.trailingCode)
     }

     @Test
     fun testJP() {
         TestCase.assertEquals("JP", Country.JP.countryCode)
         TestCase.assertEquals("JPY", Country.JP.currencyCode)
         TestCase.assertEquals("¥", Country.JP.currencySymbol)
         TestCase.assertEquals(100, Country.JP.minPledge)
         TestCase.assertEquals(1_200_000, Country.JP.maxPledge)
         TestCase.assertEquals(false, Country.JP.trailingCode)
     }

     @Test
     fun testLU() {
         TestCase.assertEquals("LU", Country.LU.countryCode)
         TestCase.assertEquals("EUR", Country.LU.currencyCode)
         TestCase.assertEquals("€", Country.LU.currencySymbol)
         TestCase.assertEquals(1, Country.LU.minPledge)
         TestCase.assertEquals(8_500, Country.LU.maxPledge)
         TestCase.assertEquals(false, Country.LU.trailingCode)
     }

     @Test
     fun testMX() {
         TestCase.assertEquals("MX", Country.MX.countryCode)
         TestCase.assertEquals("MXN", Country.MX.currencyCode)
         TestCase.assertEquals("$", Country.MX.currencySymbol)
         TestCase.assertEquals(10, Country.MX.minPledge)
         TestCase.assertEquals(200_000, Country.MX.maxPledge)
         TestCase.assertEquals(true, Country.MX.trailingCode)
     }

     @Test
     fun testNL() {
         TestCase.assertEquals("NL", Country.NL.countryCode)
         TestCase.assertEquals("EUR", Country.NL.currencyCode)
         TestCase.assertEquals("€", Country.NL.currencySymbol)
         TestCase.assertEquals(1, Country.NL.minPledge)
         TestCase.assertEquals(8_500, Country.NL.maxPledge)
         TestCase.assertEquals(false, Country.NL.trailingCode)
     }

     @Test
     fun testNO() {
         TestCase.assertEquals("NO", Country.NO.countryCode)
         TestCase.assertEquals("NOK", Country.NO.currencyCode)
         TestCase.assertEquals("kr", Country.NO.currencySymbol)
         TestCase.assertEquals(5, Country.NO.minPledge)
         TestCase.assertEquals(80_000, Country.NO.maxPledge)
         TestCase.assertEquals(true, Country.NO.trailingCode)
     }

     @Test
     fun testNZ() {
         TestCase.assertEquals("NZ", Country.NZ.countryCode)
         TestCase.assertEquals("NZD", Country.NZ.currencyCode)
         TestCase.assertEquals("$", Country.NZ.currencySymbol)
         TestCase.assertEquals(1, Country.NZ.minPledge)
         TestCase.assertEquals(14_000, Country.NZ.maxPledge)
         TestCase.assertEquals(true, Country.NZ.trailingCode)
     }

     @Test
     fun testSE() {
         TestCase.assertEquals("SE", Country.SE.countryCode)
         TestCase.assertEquals("SEK", Country.SE.currencyCode)
         TestCase.assertEquals("kr", Country.SE.currencySymbol)
         TestCase.assertEquals(5, Country.SE.minPledge)
         TestCase.assertEquals(85_000, Country.SE.maxPledge)
         TestCase.assertEquals(true, Country.SE.trailingCode)
     }

     @Test
     fun testSG() {
         TestCase.assertEquals("SG", Country.SG.countryCode)
         TestCase.assertEquals("SGD", Country.SG.currencyCode)
         TestCase.assertEquals("$", Country.SG.currencySymbol)
         TestCase.assertEquals(2, Country.SG.minPledge)
         TestCase.assertEquals(13_000, Country.SG.maxPledge)
         TestCase.assertEquals(true, Country.SG.trailingCode)
     }

     @Test
     fun testUS() {
         TestCase.assertEquals("US", Country.US.countryCode)
         TestCase.assertEquals("USD", Country.US.currencyCode)
         TestCase.assertEquals("$", Country.US.currencySymbol)
         TestCase.assertEquals(1, Country.US.minPledge)
         TestCase.assertEquals(10_000, Country.US.maxPledge)
         TestCase.assertEquals(true, Country.US.trailingCode)
     }

}
