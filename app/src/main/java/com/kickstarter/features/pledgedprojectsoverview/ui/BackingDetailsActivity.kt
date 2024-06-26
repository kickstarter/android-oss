package com.kickstarter.features.pledgedprojectsoverview.ui

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.databinding.ActivityBackingDetailsBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.viewmodels.BackingDetailsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class BackingDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackingDetailsBinding
    private lateinit var viewModelFactory: BackingDetailsViewModel.BackingDetailsViewModel.Factory
    private val viewModel: BackingDetailsViewModel.BackingDetailsViewModel by viewModels { viewModelFactory }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackingDetailsBinding.inflate(layoutInflater)

        this.getEnvironment()?.let { env ->
            viewModelFactory = BackingDetailsViewModel.BackingDetailsViewModel.Factory(env, intent = intent)
            env
        }

        setContentView(binding.root)

        this.viewModel.outputs.url()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.webView.loadUrl(it) }
            .addToDisposable(disposables)

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
