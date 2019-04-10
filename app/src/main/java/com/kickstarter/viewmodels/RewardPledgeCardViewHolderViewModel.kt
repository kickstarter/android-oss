package com.kickstarter.viewmodels

import com.kickstarter.libs.Environment

interface RewardPledgeCardViewHolderViewModel : BaseRewardCardViewHolderViewModel {

    interface Inputs : BaseRewardCardViewHolderViewModel.Inputs
    interface Outputs : BaseRewardCardViewHolderViewModel.Outputs

    class ViewModel(environment: Environment) : BaseRewardCardViewHolderViewModel.ViewModel(environment), Inputs, Outputs  {
        val inputs: Inputs = this
        val outputs: Outputs = this
    }
}
