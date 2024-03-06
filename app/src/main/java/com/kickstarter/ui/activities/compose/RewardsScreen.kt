package com.kickstarter.ui.activities.compose

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData

@Composable
fun RewardsScreenPreview() {

}
@Composable
fun RewardsScreen(
        onBackClicked: () -> Unit,
        scaffoldState: ScaffoldState,
        rewards : List<Reward> = listOf(),
        projectData : ProjectData,

        ) {
//we need a rewards card
    // a list of rewards
    //back button
    //transition to the next slide when tapping a reward
    //passing data forward
}