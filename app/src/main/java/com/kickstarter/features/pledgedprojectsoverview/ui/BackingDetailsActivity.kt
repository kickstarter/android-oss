package com.kickstarter.features.pledgedprojectsoverview.ui

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityBackingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbarTitle = intent.getStringExtra(IntentKey.TOOLBAR_TITLE)
        toolbarTitle?.let { binding.titleTextView.text = it }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
                topMargin = insets.top
            }

            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = imeInsets.bottom)

            WindowInsetsCompat.CONSUMED
        }

        this.getEnvironment()?.let { env ->
            viewModelFactory = BackingDetailsViewModel.Factory(env, intent = intent)
            env
        }

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
