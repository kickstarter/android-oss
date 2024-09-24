package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.databinding.FragmentRewardsBinding
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.reduce
import com.kickstarter.ui.activities.compose.projectpage.RewardCarouselScreen
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.RewardsSelectionViewModel

class RewardsFragment : Fragment() {

    private lateinit var dialog: AlertDialog
    private var binding: FragmentRewardsBinding? = null

    private lateinit var rewardsSelectionViewModelFactory: RewardsSelectionViewModel.Factory
    private val viewModel: RewardsSelectionViewModel by viewModels { rewardsSelectionViewModelFactory }

    private lateinit var environment: Environment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            environment = env
            rewardsSelectionViewModelFactory = RewardsSelectionViewModel.Factory(env)
        }

        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentRewardsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createDialog()

        binding?.composeView?.apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            // Compose world
            setContent {
                KickstarterApp(
                    useDarkTheme = true
                ) {
                    KSTheme {

                        val rewardSelectionUIState by viewModel.rewardSelectionUIState.collectAsStateWithLifecycle()
                        val shippingUIState by viewModel.shippingUIState.collectAsStateWithLifecycle()
                        val projectData = rewardSelectionUIState.project
                        val indexOfBackedReward = rewardSelectionUIState.initialRewardIndex
                        val rewards = shippingUIState.filteredRw
                        val project = projectData.project()
                        val backing = projectData.backing()

                        val rewardLoading = shippingUIState.loading
                        val currentUserShippingRule = shippingUIState.selectedShippingRule
                        val shippingRules = shippingUIState.shippingRules

                        val listState = rememberLazyListState(
                            initialFirstVisibleItemIndex = indexOfBackedReward
                        )
                        RewardCarouselScreen(
                            lazyRowState = listState,
                            environment = environment,
                            rewards = rewards,
                            project = project,
                            backing = backing,
                            onRewardSelected = {
                                viewModel.onUserRewardSelection(it)
                            },
                            countryList = shippingRules,
                            onShippingRuleSelected = { shippingRule ->
                                viewModel.selectedShippingRule(shippingRule)
                            },
                            currentShippingRule = currentUserShippingRule,
                            isLoading = rewardLoading
                        )

                        LaunchedEffect(Unit) {
                            viewModel.flowUIRequest.collect {
                                viewModel.getPledgeData()?.let {
                                    if (viewModel.shouldShowAlert()) {
                                        showDialog()
                                    } else {
                                        showAddonsFragment(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createDialog() {
        context?.let { context ->
            dialog = AlertDialog.Builder(context, R.style.AlertDialog)
                .setCancelable(false)
                .setTitle(getString(R.string.Continue_with_this_reward))
                .setMessage(getString(R.string.It_may_not_offer_some_or_all_of_your_add_ons))
                .setNegativeButton(getString(R.string.No_go_back)) { _, _ -> {} }
                .setPositiveButton(getString(R.string.Yes_continue)) { _, _ ->
                    viewModel.getPledgeData()?.let { showAddonsFragment(it) }
                }.create()
        }
    }

    private fun showDialog() {
        if (this.isVisible)
            dialog.show()
    }

    private fun showAddonsFragment(pledgeDataAndReason: kotlin.Pair<PledgeData, PledgeReason>) {
        if (this.isVisible && this.parentFragmentManager.findFragmentByTag(BackingAddOnsFragment::class.java.simpleName) == null) {

            val reducedProject = pledgeDataAndReason.first.projectData().project().reduce()

            val reducedProjectData =
                pledgeDataAndReason.first.projectData().toBuilder().project(reducedProject).build()
            val reducedPledgeData =
                pledgeDataAndReason.first.toBuilder().projectData(reducedProjectData).build()

            val addOnsFragment = BackingAddOnsFragment.newInstance(
                Pair(
                    reducedPledgeData,
                    pledgeDataAndReason.second
                )
            )

            this.parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .add(
                    R.id.fragment_container,
                    addOnsFragment,
                    BackingAddOnsFragment::class.java.simpleName
                )
                .addToBackStack(BackingAddOnsFragment::class.java.simpleName)
                .commit()
        }
    }

    fun setState(state: Boolean?) {
        state?.let {
            viewModel.sendEvent(expanded = it)
        }
    }

    fun configureWith(projectData: ProjectData) {
        this.viewModel.provideProjectData(projectData)
    }
}
