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
                amount = "$12.00",
                address = "Firsty Lasty\n123 First Street, Apt #5678\nLos Angeles, CA 90025-1234\nUnited States",
                currencySymbol = "$",
                currencyCode = CurrencyCode.USD,
                projectName = "Quiet, Brain! Heartfelt Activities for Mental Health",
                projectId = "UHJvamVjdC0xOTY1MjA4NTUx",
                projectSlug = "theawkwardyeti/quiet-brain-heartfelt-activities-for-mental-health",
                imageUrl = "https://i-dev.kickstarter.com/assets/043/304/083/e027563139c4f494a87cb2b38b04096e_original.jpeg?anim=false&fit=cover&gravity=auto&height=576&origin=ugc-qa&q=92&width=1024&sig=om2pKNBJ3o802YChBvd%2B%2FQoCq177%2BhnWlv5H%2BTdYm74%3D",
                creatorName = "Nick Seluk",
                backingDetailsUrl = "backing/details/url",
                timeNumberForAction = 7,
                showBadge = false,
                viewType = PPOCardViewType.FIX_PAYMENT
            )
        }
    }
}
