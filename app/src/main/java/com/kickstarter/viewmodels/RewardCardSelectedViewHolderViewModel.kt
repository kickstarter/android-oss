package com.kickstarter.viewmodels

interface RewardCardSelectedViewHolderViewModel : BaseRewardCardViewHolderViewModel {

    interface Inputs : BaseRewardCardViewHolderViewModel.Inputs
    interface Outputs : BaseRewardCardViewHolderViewModel.Outputs

    class ViewModel : BaseRewardCardViewHolderViewModel.ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this
    }
}
