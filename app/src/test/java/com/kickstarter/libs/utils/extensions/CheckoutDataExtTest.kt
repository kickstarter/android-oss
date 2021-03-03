package com.kickstarter.libs.utils.extensions

import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.data.CheckoutData
import junit.framework.TestCase
import type.CreditCardPaymentType

class CheckoutDataExtTest: TestCase() {

    fun testTotalAmount_WithoutShipping() {
        val coData = CheckoutData.builder()
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .shippingAmount(0.0)
                .amount(200.0)
                .build()

        assertEquals(coData.totalAmount(), 200.0)
    }

    fun testTotalAmount_WithShipping() {
        val coData = CheckoutData.builder()
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .shippingAmount(30.0)
                .amount(200.0)
                .build()

        assertEquals(coData.totalAmount(), 230.0)
    }

    fun testBonus_withUSDProject() {
        val project = ProjectFactory.project()
        val coData = CheckoutData.builder()
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .shippingAmount(30.0)
                .amount(200.0)
                .build()

        assertEquals(coData.bonus(), coData.bonus(project.staticUsdRate()))
    }

    fun testBonus_withCAProject() {
        val project = ProjectFactory.caProject()
        val coData = CheckoutData.builder()
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .shippingAmount(30.0)
                .amount(200.0)
                .build()

        val expectedBonusUSD = coData.bonus() * project.staticUsdRate()
        assertEquals(expectedBonusUSD, coData.bonus(project.staticUsdRate()))
    }

    fun testShipping_withUSDProject() {
        val project = ProjectFactory.project()
        val coData = CheckoutData.builder()
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .shippingAmount(30.0)
                .amount(200.0)
                .build()

        assertEquals(coData.shippingAmount(), coData.shippingAmount(project.staticUsdRate()))
    }

    fun testShipping_withCAProject() {
        val project = ProjectFactory.caProject()
        val coData = CheckoutData.builder()
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .shippingAmount(30.0)
                .amount(200.0)
                .build()

        val expectedShippingInUSD = coData.shippingAmount() * project.staticUsdRate()
        assertEquals(expectedShippingInUSD, coData.shippingAmount(project.staticUsdRate()))
    }
}