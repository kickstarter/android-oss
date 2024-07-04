package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.databinding.FragmentRewardsBinding
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.reduce
import com.kickstarter.libs.utils.extensions.selectPledgeFragment
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.ui.activities.compose.projectpage.RewardCarouselScreen
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.RewardsFragmentViewModel.Factory
import com.kickstarter.viewmodels.RewardsFragmentViewModel.RewardsFragmentViewModel
import com.kickstarter.viewmodels.usecases.ShippingRulesState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class RewardsFragment : Fragment() {

    private lateinit var dialog: AlertDialog
    private var binding: FragmentRewardsBinding? = null

    private lateinit var viewModelFactory: Factory
    private val viewModel: RewardsFragmentViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var environment: Environment
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env)
            environment = env
        }

        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentRewardsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @Composable
    private fun ScrollToPosition(
        scrollToPosition: State<Int>,
        listState: LazyListState
    ) {
        LaunchedEffect(scrollToPosition) {
            // Animate scroll to the scrollToPosition item
            listState.animateScrollToItem(index = scrollToPosition.value)
        }
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

                        val projectData: State<ProjectData> = viewModel.projectData().subscribeAsState(initial = ProjectData.builder().build())
                        val backing = projectData.value.backing() ?: projectData.value.project().backing()
                        val project = projectData.value.project()
                        val rewards = project.rewards() ?: emptyList()

                        val rules = viewModel.countrySelectorRules().subscribeAsState(initial = ShippingRulesState())
                        val listState = rememberLazyListState()

                        RewardCarouselScreen(
                            lazyRowState = listState,
                            environment = requireNotNull(environment),
                            rewards = rewards,
                            project = project,
                            backing = backing,
                            onRewardSelected = {
                                viewModel.inputs.rewardClicked(it)
                            },
                            countryList = rules.value.shippingRules,
                            onShippingRuleSelected = {},
                            currentShippingRule = ShippingRuleFactory.usShippingRule(),
                            isLoading = rules.value.loading
                        )

                        ScrollToPosition(viewModel.outputs.backedRewardPosition().subscribeAsState(initial = 0), listState)
                    }
                }
            }
        }

        this.viewModel.outputs.showPledgeFragment()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                dialog.dismiss()
                showPledgeFragment(it.first, it.second)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.showAddOnsFragment()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                dialog.dismiss()
                showAddonsFragment(it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.showAlert()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showAlert()
            }
            .addToDisposable(disposables)
    }

    fun setState(state: Boolean?) {
        state?.let {
            viewModel.isExpanded(state)
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
                    this.viewModel.inputs.alertButtonPressed()
                }.create()
        }
    }

    private fun showAlert() {
        if (this.isVisible)
            dialog.show()
    }

    override fun onDetach() {
        disposables.clear()
        super.onDetach()
    }

    fun configureWith(projectData: ProjectData) {
        this.viewModel.inputs.configureWith(projectData)
    }

    private fun showPledgeFragment(
        pledgeData: PledgeData,
        pledgeReason: PledgeReason
    ) {
        val fragment = this.selectPledgeFragment(pledgeData, pledgeReason)

        if (this.isVisible && this.parentFragmentManager.findFragmentByTag(fragment::class.java.simpleName) == null) {
            this.parentFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .add(
                    R.id.fragment_container,
                    fragment,
                    fragment::class.java.simpleName
                )
                .addToBackStack(fragment::class.java.simpleName)
                .commit()
        }
    }

    private fun showAddonsFragment(pledgeDataAndReason: Pair<PledgeData, PledgeReason>) {
        if (this.isVisible && this.parentFragmentManager.findFragmentByTag(BackingAddOnsFragment::class.java.simpleName) == null) {

            val reducedProject = pledgeDataAndReason.first.projectData().project().reduce()

            val reducedProjectData = pledgeDataAndReason.first.projectData().toBuilder().project(reducedProject).build()
            val reducedPledgeData = pledgeDataAndReason.first.toBuilder().projectData(reducedProjectData).build()

            val addOnsFragment = BackingAddOnsFragment.newInstance(Pair(reducedPledgeData, pledgeDataAndReason.second))

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
}
