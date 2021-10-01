package com.kickstarter.mock.factories

import com.kickstarter.models.EnvironmentalCommitment
import type.EnvironmentalCommitmentCategory

class ProjectEnvironmentalCommitmentFactory private constructor() {
    companion object {
        fun getEnvironmentalCommitments(): List<EnvironmentalCommitment> {
            return listOf(
                getLongLastingDesignCategory(),
                getSustainableMaterialsCategory()
            )
        }

        private fun getLongLastingDesignCategory() = EnvironmentalCommitment.builder()
            .id(1L)
            .description(
                "No, there is no extra VAT or taxes for backers.\r\n\r\nWe will export the " +
                    "tables to local countries first and forward to respective shipping addresses through local couriers. We will clear the customs for all the desks. \r\n\r\nVAT and taxes are already included in the reward price and no extra payment will be required from backers."
            )
            .category(EnvironmentalCommitmentCategory.LONG_LASTING_DESIGN.name)
            .build()

        private fun getSustainableMaterialsCategory() = EnvironmentalCommitment.builder()
            .id(3L)
            .description(
                "No, there is no extra VAT or taxes for backers.\r\n\r\nWe will export the " +
                    "tables to local countries first and forward to respective shipping addresses through local couriers. We will clear the customs for all the desks. \r\n\r\nVAT and taxes are already included in the reward price and no extra payment will be required from backers."
            )
            .category(EnvironmentalCommitmentCategory.SUSTAINABLE_MATERIALS.name)
            .build()
    }
}
