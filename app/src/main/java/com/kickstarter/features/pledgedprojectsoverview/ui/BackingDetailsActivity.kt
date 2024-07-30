package com.kickstarter.features.pledgedprojectsoverview.ui

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kickstarter.databinding.ActivityBackingDetailsBinding
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.viewmodels.BackingDetailsViewModel
import kotlinx.coroutines.launch

const val REFRESH = "refresh"

class BackingDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackingDetailsBinding
    private lateinit var viewModelFactory: BackingDetailsViewModel.Factory
    private val viewModel: BackingDetailsViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackingDetailsBinding.inflate(layoutInflater)

        this.getEnvironment()?.let { env ->
            viewModelFactory = BackingDetailsViewModel.Factory(env, intent = intent)
            env
        }

        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.url.collect { url ->
                    binding.webView.loadUrl(url)
                }
            }
        }

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation(withResult = REFRESH, intentKey = IntentKey.REFRESH_PPO_LIST)
        }
    }
}
