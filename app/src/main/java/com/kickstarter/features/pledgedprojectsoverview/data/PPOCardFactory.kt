package com.kickstarter.features.pledgedprojectsoverview.data

import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType
import com.kickstarter.type.CurrencyCode

class PPOCardFactory private constructor() {
    companion object {

        fun ppoCard(
            backingID: String?,
            deliveryAddress: DeliveryAddress?,
            amount: String?,
            currencyCode: CurrencyCode?,
            currencySymbol: String?,
            projectName: String?,
            projectId: String?,
            projectSlug: String?,
            imageUrl: String?,
            creatorName: String?,
            backingDetailsUrl: String?,
            timeNumberForAction: Int,
            viewType: PPOCardViewType?
        ): PPOCard {
            return PPOCard.builder()
                .backingId(backingID)
                .deliveryAddress(deliveryAddress)
                .amount(amount)
                .currencySymbol(currencySymbol)
                .currencyCode(currencyCode)
                .projectName(projectName)
                .projectId(projectId)
                .projectSlug(projectSlug)
                .imageUrl(imageUrl)
                .creatorName(creatorName)
                .backingDetailsUrl(backingDetailsUrl)
                .viewType(viewType)
                .timeNumberForAction(timeNumberForAction)
                .build()
        }

        fun deliveryAddress(
            addressID: String?,
            addressLine1: String?,
            addressLine2: String?,
            city: String?,
            region: String?,
            postalCode: String?,
            countryCode: String?,
            phoneNumber: String?,
            recipientName: String?
        ): DeliveryAddress {
            return DeliveryAddress.builder()
                .addressId(addressID)
                .addressLine1(addressLine1)
                .addressLine2(addressLine2)
                .city(city)
                .region(region)
                .postalCode(postalCode)
                .countryCode(countryCode)
                .phoneNumber(phoneNumber)
                .recipientName(recipientName)
                .build()
        }

        fun confirmAddressCard(): PPOCard {
            return ppoCard(
                backingID = "1234",
                amount = "12.0",
                deliveryAddress = deliveryAddress(
                    addressID = "12234",
                    addressLine1 = "123 First Street, Apt #5678",
                    addressLine2 = null,
                    city = "Los Angeles",
                    region = "CA",
                    postalCode = "90025-1234",
                    countryCode = "United States",
                    phoneNumber = "(555) 555-5555",
                    recipientName = "Firsty Lasty"
                ),
                currencySymbol = "$",
                currencyCode = CurrencyCode.USD,
                projectName = "Super Duper Project",
                projectId = "123456",
                projectSlug = "hello/hello",
                imageUrl = "image/url",
                creatorName = "creatorName",
                backingDetailsUrl = "backing/details/url",
                timeNumberForAction = 10,
                viewType = PPOCardViewType.CONFIRM_ADDRESS
            )
        }

        fun fixPaymentCard(): PPOCard {
            return ppoCard(
                backingID = "1234",
                amount = "12.00",
                deliveryAddress = deliveryAddress(
                    addressID = "12234",
                    addressLine1 = "123 First Street, Apt #5678",
                    addressLine2 = null,
                    city = "Los Angeles",
                    region = "CA",
                    postalCode = "90025-1234",
                    countryCode = "United States",
                    phoneNumber = "(555) 555-5555",
                    recipientName = "Firsty Lasty"
                ),
                currencySymbol = "$",
                currencyCode = CurrencyCode.USD,
                projectName = "Super Duper Project",
                projectId = "12345",
                projectSlug = "project/slug",
                imageUrl = "image/url",
                creatorName = "Creator Name",
                backingDetailsUrl = "backing/details/url",
                timeNumberForAction = 7,
                viewType = PPOCardViewType.FIX_PAYMENT
            )
        }

        fun authenticationRequiredCard(): PPOCard {
            // 3ds card
            return ppoCard(
                backingID = "1234",
                amount = "12.00",
                deliveryAddress = deliveryAddress(
                    addressID = "12234",
                    addressLine1 = "123 First Street, Apt #5678",
                    addressLine2 = null,
                    city = "Los Angeles",
                    region = "CA",
                    postalCode = "90025-1234",
                    countryCode = "United States",
                    phoneNumber = "(555) 555-5555",
                    recipientName = "Firsty Lasty"
                ),
                currencySymbol = "$",
                currencyCode = CurrencyCode.USD,
                projectName = "Super Duper Project",
                projectId = "12345",
                projectSlug = "project/slug",
                imageUrl = "image/url",
                creatorName = "Creator Name",
                backingDetailsUrl = "backing/details/url",
                timeNumberForAction = 7,
                viewType = PPOCardViewType.AUTHENTICATE_CARD
            )
        }
    }
}
