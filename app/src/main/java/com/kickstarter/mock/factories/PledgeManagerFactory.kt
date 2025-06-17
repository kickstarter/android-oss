package com.kickstarter.mock.factories

import com.kickstarter.models.PledgeManager

class PledgeManagerFactory {
    companion object {
        fun pledgeManagerAcceptsNetNewBackers(): PledgeManager {
            return PledgeManager.builder()
                .id(-1)
                .acceptsNewBackers(true)
                .state(PledgeManager.PledgeManagerStateEnum.APPROVED)
                .build()
        }
        fun pledgeManagerDoesNotAcceptNetNewBackers(): PledgeManager {
            return PledgeManager.builder()
                .id(-1)
                .acceptsNewBackers(false)
                .state(PledgeManager.PledgeManagerStateEnum.APPROVED)
                .build()
        }
        fun pledgeManagerInNonApprovedState(): PledgeManager {
            return PledgeManager.builder()
                .id(-1)
                .acceptsNewBackers(true)
                .state(PledgeManager.PledgeManagerStateEnum.SUBMITTED)
                .build()
        }
    }
}
