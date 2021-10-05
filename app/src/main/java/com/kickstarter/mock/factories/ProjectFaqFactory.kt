package com.kickstarter.mock.factories

import com.kickstarter.models.ProjectFaq
import org.joda.time.DateTime

class ProjectFaqFactory private constructor() {
    companion object {
        fun getFaqs(): List<ProjectFaq> {
            return listOf(getFaq(), getFaq(), getFaq(), getFaq(), getFaq(), getFaq())
        }

        private fun getFaq() = ProjectFaq.builder()
            .id(7L)
            .answer("No, there is no extra VAT or taxes for backers.\r\n\r\nWe will export the tables to local countries first and forward to respective shipping addresses through local couriers. We will clear the customs for all the desks. \r\n\r\nVAT and taxes are already included in the reward price and no extra payment will be required from backers.")
            .question("Is there any extra VAT and taxes on top of shipping cost?")
            .createdAt(DateTime.now())
            .build()
    }
}
