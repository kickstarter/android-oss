package com.kickstarter.features.pledgedprojectsoverview.data

import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType
import type.CurrencyCode

class PPOCardFactory private constructor() {
    companion object {

        fun ppoCard(
            backingID: String?,
            address: String?,
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
            showBadge: Boolean,
            viewType: PPOCardViewType?
        ): PPOCard {
            return PPOCard.builder()
                .backingId(backingID)
                .address(address)
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
                .showBadge(showBadge)
                .build()
        }

        fun confirmAddressCard(): PPOCard {
            return ppoCard(
                backingID = "1234",
                amount = "12.0",
                address = "Firsty Lasty\n123 First Street, Apt #5678\nLos Angeles, CA 90025-1234\nUnited States",
                currencySymbol = "$",
                currencyCode = CurrencyCode.USD,
                projectName = "Super Duper Project",
                projectId = "123456",
                projectSlug = "hello/hello",
                imageUrl = "image/url",
                creatorName = "creatorName",
                backingDetailsUrl = "backing/details/url",
                timeNumberForAction = 10,
                showBadge = false,
                viewType = PPOCardViewType.CONFIRM_ADDRESS
            )
        }

        fun fixPaymentCard(): PPOCard {
            return ppoCard(
                backingID = "1234",
                amount = "12.0",
                address = "Firsty Lasty\n123 First Street, Apt #5678\nLos Angeles, CA 90025-1234\nUnited States",
                currencySymbol = "$",
                currencyCode = CurrencyCode.USD,
                projectName = "Super Duper Project",
                projectId = "123456",
                projectSlug = "richardamorgan/blam-and-glam-comic-issue-1-8-and-toy-figure",
                imageUrl = "image/url",
                creatorName = "creatorName",
                backingDetailsUrl = "backing/details/url",
                timeNumberForAction = 10,
                showBadge = false,
                viewType = PPOCardViewType.FIX_PAYMENT
            )
        }
    }
}
