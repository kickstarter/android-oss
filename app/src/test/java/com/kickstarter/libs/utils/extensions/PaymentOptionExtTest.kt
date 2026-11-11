package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import com.stripe.android.model.PaymentMethod
import com.stripe.android.paymentsheet.model.PaymentOption
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class PaymentOptionExtTest : KSRobolectricTestCase() {

    @Test
    fun `test Google Pay PaymentOption`() {
        val paymentOption = mockk<PaymentOption> {
            every { paymentMethodType } returns PAYMENT_METHOD_TYPE_GOOGLE_PAY
        }

        assertTrue(paymentOption.isGooglePay())
        assertFalse(paymentOption.isLink())
        assertEquals(PaymentOptionWalletType.GOOGLE_PAY, paymentOption.walletType())
        assertEquals("google_pay", PaymentOptionWalletType.GOOGLE_PAY.identifier)
    }

    @Test
    fun `test Link PaymentOption`() {
        val paymentOption = mockk<PaymentOption> {
            every { paymentMethodType } returns PaymentMethod.Type.Link.code
        }

        assertTrue(paymentOption.isLink())
        assertFalse(paymentOption.isGooglePay())
        assertEquals(PaymentOptionWalletType.LINK, paymentOption.walletType())
        assertEquals("link", PaymentOptionWalletType.LINK.identifier)
    }

    @Test
    fun `test card PaymentOption`() {
        val paymentOption = mockk<PaymentOption> {
            every { paymentMethodType } returns PaymentMethod.Type.Card.code
        }

        assertNull(paymentOption.walletType())
    }
}
