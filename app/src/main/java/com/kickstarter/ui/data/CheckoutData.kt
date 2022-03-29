package com.kickstarter.ui.data

import android.os.Parcelable
import auto.parcel.AutoParcel
import com.kickstarter.models.Backing
import com.kickstarter.models.Message
import com.kickstarter.models.Project
import com.kickstarter.models.User
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime
import type.CreditCardPaymentType

@Parcelize
 class CheckoutData private constructor(
    private val amount: Double,
    private val id: Long?,
    private val paymentType: CreditCardPaymentType,
    private val shippingAmount: Double,
    private val bonusAmount: Double?
) : Parcelable {

     fun amount()=this.amount
     fun id()=this.id
     fun paymentType()= this.paymentType
     fun shippingAmount()=this.shippingAmount
     fun bonusAmount() =this.bonusAmount

    @Parcelize
    @Parcelize
    data class Builder(
        private var body: String = "",
        private var createdAt: DateTime = DateTime.now(),
        private var id: Long = 0L,
        private var recipient: User = User.builder().build(),
        private var sender: User = User.builder().build()
    ) : Parcelable {

    }
     class Builder {

     fun toBuilder(): Builder

    companion object {

        fun builder(): Builder {
            return AutoParcel_CheckoutData.Builder()
        }
    }
}
