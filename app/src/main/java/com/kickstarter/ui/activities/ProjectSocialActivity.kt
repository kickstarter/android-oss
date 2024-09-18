package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.databinding.ProjectSocialLayoutBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.adapters.ProjectSocialAdapter
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.ProjectSocialViewModel.Factory
import com.kickstarter.viewmodels.ProjectSocialViewModel.ProjectSocialViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ProjectSocialActivity : AppCompatActivity() {

    private lateinit var binding: ProjectSocialLayoutBinding

    private lateinit var viewModelFactory: Factory
    private val viewModel: ProjectSocialViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env, intent = intent)
        }

        binding = ProjectSocialLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        val adapter = ProjectSocialAdapter()
        binding.projectSocialRecyclerView.adapter = adapter
        binding.projectSocialRecyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.outputs.project()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.takeProject(it) }
            .addToDisposable(disposables)

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }
    }

    override fun onDestroy() {
        disposables.clear()
        binding.projectSocialRecyclerView.adapter = null
        super.onDestroy()
    }
}
