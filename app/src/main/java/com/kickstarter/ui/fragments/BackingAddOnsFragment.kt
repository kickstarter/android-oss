package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.databinding.FragmentBackingAddonsBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.selectPledgeFragment
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.activities.compose.projectpage.AddOnsScreen
import com.kickstarter.ui.adapters.BackingAddOnsAdapter
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.BackingAddOnViewHolder
import com.kickstarter.viewmodels.projectpage.AddOnsViewModel
import io.reactivex.disposables.CompositeDisposable

class BackingAddOnsFragment : Fragment(), BackingAddOnViewHolder.ViewListener {
    private var binding: FragmentBackingAddonsBinding? = null

    private lateinit var viewModelFactoryC: AddOnsViewModel.Factory
    private val viewModelC: AddOnsViewModel by viewModels {
        viewModelFactoryC
    }

    private var disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentBackingAddonsBinding.inflate(inflater, container, false)
        val view = binding?.root
        binding?.composeView?.apply {
            val env = this?.context?.getEnvironment()?.let { env ->
                viewModelFactoryC = AddOnsViewModel.Factory(env, bundle = arguments)
                //viewModelFactory = BackingAddOnsFragmentViewModel.Factory(env, bundle = arguments)
                //viewModelC.provideBundle(arguments)
                env
            }
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KickstarterApp(
                    useDarkTheme = true
                ) {
                    arguments?.let {
                        val pledgeData =
                            it.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData?
                        val projectData = pledgeData?.projectData()

                        val addOnsUIState by viewModelC.addOnsUIState.collectAsStateWithLifecycle()

                        val selectedAddOnsMap = remember {
                            viewModelC.currentAddOnsSelections
                        }

                        val addOns = addOnsUIState.addOns
                        val addOnsIsLoading = addOnsUIState.isLoading
                        val project = projectData?.project() ?: Project.builder().build()
                        val pledgeFlowContext = viewModelC.pledgeflowcontext

                        KSTheme {
                            AddOnsScreen(
                                environment = requireNotNull(env),
                                lazyColumnListState = rememberLazyListState(),
                                rewardItems = addOns,
                                project = project,
                                selectedAddOnsMap = selectedAddOnsMap,
                                onItemAddedOrRemoved = { updateAddOnRewardCount ->
                                    viewModelC.onAddOnsAddedOrRemoved(selectedAddOnsMap)
                                },
                                isLoading = addOnsIsLoading,
                                onContinueClicked = {}
                            )
                        }

                        projectData?.let {
                            viewModelC.provideBundle(arguments)
                        }
                    }
                }
            }
        }
        return view
    }

    private val backingAddonsAdapter = BackingAddOnsAdapter(this)
    private lateinit var errorDialog: AlertDialog


    private fun selectProperString(totalSelected: Int, ksString: KSString): String {
        return when {
            totalSelected == 0 -> ksString.format(getString(R.string.Skip_add_ons), "", "")
            totalSelected == 1 -> ksString.format(getString(R.string.Continue_with_quantity_count_add_ons_one), "quantity_count", totalSelected.toString())
            totalSelected > 1 -> ksString.format(getString(R.string.Continue_with_quantity_count_add_ons_many), "quantity_count", totalSelected.toString())
            else -> ""
        }
    }

    private fun showErrorDialog() {
        if (!errorDialog.isShowing) {
            errorDialog.show()
        }
    }

    private fun dismissErrorDialog() {
        errorDialog.dismiss()
    }

    private fun populateAddOns(projectDataAndAddOnList: Triple<ProjectData, List<Reward>, ShippingRule>) {
        val projectData = projectDataAndAddOnList.first
        val selectedShippingRule = projectDataAndAddOnList.third
        val list = projectDataAndAddOnList
            .second
            .map {
                Triple(projectData, it, selectedShippingRule)
            }.toList()

        backingAddonsAdapter.populateDataForAddOns(list)
    }

    private fun showEmptyState(isEmptyState: Boolean) {
        backingAddonsAdapter.showEmptyState(isEmptyState)
    }


    private fun showPledgeFragment(pledgeData: PledgeData, pledgeReason: PledgeReason) {
        val fragment = this.selectPledgeFragment(pledgeData, pledgeReason)
        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
            .add(R.id.fragment_container, fragment, fragment::class.java.simpleName)
            .addToBackStack(fragment::class.java.simpleName)
            .commit()
    }

    private fun setupErrorDialog() {
//        context?.let { context ->
//            errorDialog = AlertDialog.Builder(context, R.style.AlertDialog)
//                .setCancelable(false)
//                .setTitle(getString(R.string.Something_went_wrong_please_try_again))
//                .setPositiveButton(getString(R.string.Retry)) { _, _ ->
//                    this.viewModel.inputs.retryButtonPressed()
//                }
//                .setNegativeButton(getString(R.string.general_navigation_buttons_close)) { _, _ -> dismissErrorDialog() }
//                .create()
//        }
    }

    companion object {
        fun newInstance(pledgeDataAndReason: Pair<PledgeData, PledgeReason>): BackingAddOnsFragment {
            val fragment = BackingAddOnsFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, pledgeDataAndReason.first)
            argument.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, pledgeDataAndReason.second)
            fragment.arguments = argument
            return fragment
        }
    }

    override fun quantityPerId(quantityPerId: Pair<Int, Long>) {
        //this.viewModel.inputs.quantityPerId(quantityPerId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }
}
