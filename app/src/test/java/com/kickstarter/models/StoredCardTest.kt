package com.kickstarter.models

import com.kickstarter.R
import com.kickstarter.mock.factories.IdFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.models.extensions.getBackingData
import com.kickstarter.models.extensions.getCardTypeDrawable
import com.stripe.android.model.CardBrand
import junit.framework.TestCase
import org.junit.Test
import type.CreditCardTypes
import java.util.Date

class StoredCardTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val expiration = Date()
        val id = IdFactory.id().toString()

        val storedCard = StoredCard.builder()
            .id(id)
            .expiration(expiration)
            .lastFourDigits("1234")
            .type(CreditCardTypes.DISCOVER)
            .build()

        val resourceID = storedCard.getCardTypeDrawable()
        assertEquals(storedCard.id(), id)
        assertEquals(storedCard.expiration(), expiration)
        assertEquals(storedCard.lastFourDigits(), "1234")
        assertEquals(storedCard.type(), CreditCardTypes.DISCOVER)
        assertEquals(resourceID, R.drawable.discover_md)
    }

    @Test
    fun testCardFomPaymentSheet() {

        val storedCard = StoredCard.builder()
            .lastFourDigits("1234")
            .clientSetupId("ClientSetupID")
            .resourceId(1234)
            .build()

        val resourceID = storedCard.getCardTypeDrawable()
        assertEquals(storedCard.lastFourDigits(), "1234")
        assertEquals(storedCard.type(), CreditCardTypes.`$UNKNOWN`)
        assertEquals(resourceID, 1234)
    }

    @Test
    fun testStoredCard_equalFalse() {
        val expiration = Date()

        val storedCard = StoredCardFactory.discoverCard()
        val storedCard2 = StoredCard.builder().expiration(expiration).build()
        val storedCard3 = StoredCard.builder().type(CreditCardTypes.DISCOVER).build()
        val storedCard4 = StoredCard.builder().lastFourDigits("123").build()

        assertFalse(storedCard == storedCard2)
        assertFalse(storedCard == storedCard3)
        assertFalse(storedCard == storedCard4)

        assertFalse(storedCard3 == storedCard2)
        assertFalse(storedCard3 == storedCard4)
    }

    @Test
    fun testStoredCard_equalTrue() {
        val storedCard1 = StoredCard.builder().build()
        val storedCard2 = StoredCard.builder().build()

        assertEquals(storedCard1, storedCard2)
    }

    @Test
    fun testStoredCardToBuilder() {
        val lastFourDigits = "3556"
        val storedCard = StoredCardFactory.visa().toBuilder()
            .lastFourDigits(lastFourDigits).build()

        assertEquals(storedCard.lastFourDigits(), lastFourDigits)
    }

    @Test
    fun testStoredCardIssuer() {
        assertEquals(StoredCard.issuer(CreditCardTypes.AMEX), CardBrand.AmericanExpress.code)
        assertEquals(StoredCard.issuer(CreditCardTypes.DINERS), CardBrand.DinersClub.code)
        assertEquals(StoredCard.issuer(CreditCardTypes.DISCOVER), CardBrand.Discover.code)
        assertEquals(StoredCard.issuer(CreditCardTypes.JCB), CardBrand.JCB.code)
        assertEquals(StoredCard.issuer(CreditCardTypes.MASTERCARD), CardBrand.MasterCard.code)
        assertEquals(StoredCard.issuer(CreditCardTypes.UNION_PAY), CardBrand.UnionPay.code)
        assertEquals(StoredCard.issuer(CreditCardTypes.VISA), CardBrand.Visa.code)
        assertEquals(StoredCard.issuer(CreditCardTypes.`$UNKNOWN`), CardBrand.Unknown.code)
    }

    @Test
    fun testStoredCardGetCardTypeDrawable() {
        assertEquals(getCardTypeDrawable(CreditCardTypes.AMEX), R.drawable.amex_md)
        assertEquals(getCardTypeDrawable(CreditCardTypes.DINERS), R.drawable.diners_md)
        assertEquals(getCardTypeDrawable(CreditCardTypes.DISCOVER), R.drawable.discover_md)
        assertEquals(getCardTypeDrawable(CreditCardTypes.JCB), R.drawable.jcb_md)
        assertEquals(getCardTypeDrawable(CreditCardTypes.MASTERCARD), R.drawable.mastercard_md)
        assertEquals(getCardTypeDrawable(CreditCardTypes.UNION_PAY), R.drawable.union_pay_md)
        assertEquals(getCardTypeDrawable(CreditCardTypes.VISA), R.drawable.visa_md)
        assertEquals(getCardTypeDrawable(CreditCardTypes.`$UNKNOWN`), R.drawable.generic_bank_md)
    }

    @Test
    fun getBackingDataFromPaymentInfo() {
        val storedCard = StoredCardFactory.visa()
        val backingData = storedCard.getBackingData(ProjectFactory.project(), "", locationId = null, rewards = listOf(RewardFactory.reward()), cookieRefTag = null)

        assertEquals(backingData.setupIntentClientSecret, null)
        assertEquals(backingData.paymentSourceId, storedCard.id())

        val storedCardFromPaymentSheet = StoredCardFactory.fromPaymentSheetCard()
        val backingDataFromPaymentSheet = storedCard.getBackingData(ProjectFactory.project(), "", locationId = null, rewards = listOf(RewardFactory.reward()), cookieRefTag = null)
        assertEquals(backingDataFromPaymentSheet.setupIntentClientSecret, storedCardFromPaymentSheet.clientSetupId())
        assertEquals(backingDataFromPaymentSheet.paymentSourceId, null)
    }
}
