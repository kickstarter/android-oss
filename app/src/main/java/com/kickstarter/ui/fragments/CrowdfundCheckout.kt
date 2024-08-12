package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kickstarter.databinding.FragmentCrowdfundCheckoutBinding
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel.Factory
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel

class CrowdfundCheckout: Fragment() {

    private var binding: FragmentCrowdfundCheckoutBinding? = null

    private lateinit var viewModelFactory: Factory
    private val viewModel: CrowdfundCheckoutViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentCrowdfundCheckoutBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.composeView?.apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            // Compose world
            setContent {
                KickstarterApp(
                    useDarkTheme = true
                ) {
                    KSTheme {

                    }
                }
            }
        }
    }
}