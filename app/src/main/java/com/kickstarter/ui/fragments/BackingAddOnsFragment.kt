package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kickstarter.R
import com.kickstarter.databinding.FragmentBackingAddonsBinding
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.selectPledgeFragment
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.activities.compose.projectpage.AddOnsScreen
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.extensions.startLoginActivity
import com.kickstarter.viewmodels.projectpage.AddOnsViewModel

class BackingAddOnsFragment : Fragment() {
    private var binding: FragmentBackingAddonsBinding? = null

    private lateinit var viewModelFactoryC: AddOnsViewModel.Factory
    private val viewModelC: AddOnsViewModel by viewModels {
        viewModelFactoryC
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentBackingAddonsBinding.inflate(inflater, container, false)
        val view = binding?.root
        binding?.composeView?.apply {
            val env = this.context?.getEnvironment()?.let { env ->
                viewModelFactoryC = AddOnsViewModel.Factory(env, bundle = arguments)
                viewModelC.provideBundle(arguments)
                env
            }

            val ffClient = requireNotNull(env?.featureFlagClient())
            activity?.let { ffClient.activate(it) }

            viewModelC.provideErrorAction { message ->
                activity?.runOnUiThread {
                    showErrorToast(
                        context,
                        this,
                        message ?: getString(R.string.general_error_something_wrong)
                    )
                }
            }

            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KickstarterApp(
                    useDarkTheme = true
                ) {
                    val addOnsUIState by viewModelC.addOnsUIState.collectAsState()

                    val addOns = addOnsUIState.addOns
                    val totalCount = addOnsUIState.totalCount
                    val addOnsIsLoading = addOnsUIState.isLoading
                    val shippingRule = addOnsUIState.shippingRule
                    val totalPledgeAmount = addOnsUIState.totalPledgeAmount

                    val project = viewModelC.getProject()
                    val selectedRw = viewModelC.getSelectedReward()

                    KSTheme {
                        AddOnsScreen(
                            modifier = Modifier.padding(top = dimensions.paddingDoubleLarge),
                            environment = requireNotNull(env),
                            lazyColumnListState = rememberLazyListState(),
                            selectedReward = selectedRw,
                            addOns = addOns,
                            project = project,
                            onItemAddedOrRemoved = { quantity, rewardId ->
                                viewModelC.updateSelection(rewardId, quantity)
                            },
                            isLoading = addOnsIsLoading,
                            currentShippingRule = shippingRule,
                            onContinueClicked = {
                                if (viewModelC.isUserLoggedIn()) {
                                    viewModelC.getPledgeDataAndReason()?.let { pDataAndReason ->
                                        showPledgeFragment(
                                            pledgeData = pDataAndReason.first,
                                            pledgeReason = pDataAndReason.second,
                                            ffClient = ffClient
                                        )
                                    }
                                } else {
                                    activity?.startLoginActivity()
                                }
                            },
                            bonusAmountChanged = { bonusAmount ->
                                viewModelC.bonusAmountUpdated(bonusAmount)
                            },
                            addOnCount = totalCount,
                            totalPledgeAmount = totalPledgeAmount,
                            totalBonusSupport = addOnsUIState.totalBonusAmount
                        )
                    }
                }
            }
        }
        return view
    }

    private fun showPledgeFragment(
        pledgeData: PledgeData,
        pledgeReason: PledgeReason,
        ffClient: FeatureFlagClientType
    ) {
        val ffEnabled = ffClient.getBoolean(FlagKey.ANDROID_FIX_PLEDGE_REFACTOR)
        val fragment = this.selectPledgeFragment(pledgeData, pledgeReason, ffEnabled)
        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
            .add(R.id.fragment_container, fragment, fragment::class.java.simpleName)
            .addToBackStack(fragment::class.java.simpleName)
            .commit()
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
}
