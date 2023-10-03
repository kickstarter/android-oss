package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.databinding.ProjectSocialLayoutBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.adapters.ProjectSocialAdapter
import com.kickstarter.ui.viewholders.ProjectContextViewHolder
import com.kickstarter.viewmodels.ProjectSocialViewModel.ProjectSocialViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ProjectSocialActivity : AppCompatActivity(), ProjectSocialAdapter.Delegate {

    private lateinit var binding: ProjectSocialLayoutBinding

    private lateinit var viewModelFactory: ProjectSocialViewModel.Factory
    private val viewModel: ProjectSocialViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            viewModelFactory = ProjectSocialViewModel.Factory(env, intent = intent)
            env
        }

        binding = ProjectSocialLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ProjectSocialAdapter(this)
        binding.projectSocialRecyclerView.adapter = adapter
        binding.projectSocialRecyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.outputs.project()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.takeProject(it) }
            .addToDisposable(disposables)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
        binding.projectSocialRecyclerView.adapter = null
    }

    override fun projectContextClicked(viewHolder: ProjectContextViewHolder?) {

    }
    override fun onBackPressed() {
//            super.onBackPressed()
//            val exitTransitions = exitTransition()
//            if (exitTransitions != null) {
//                overridePendingTransition(exitTransitions.first, exitTransitions.second)
//            }
    }


//    override fun exitTransition() = TransitionUtils.slideInFromLeft()
}
