package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ProfileLayoutBinding
import com.kickstarter.libs.recyclerviewpagination.RecyclerViewPaginatorV2
import com.kickstarter.libs.rx.transformers.Transformers.observeForUIV2
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ProfileAdapter
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.ProfileViewModel
import io.reactivex.disposables.CompositeDisposable

class ProfileActivity : ComponentActivity() {
    private lateinit var adapter: ProfileAdapter
    private lateinit var paginator: RecyclerViewPaginatorV2
    private lateinit var binding: ProfileLayoutBinding

    private lateinit var profileViewModelFactory: ProfileViewModel.Factory
    private val viewModel: ProfileViewModel.ProfileViewModel by viewModels { profileViewModelFactory }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfileLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root,
        )
        setContentView(binding.root)

        getEnvironment()?.let { env ->
            profileViewModelFactory = ProfileViewModel.Factory(env)
        }

        this.adapter = ProfileAdapter(this.viewModel)
        val spanCount = if (ViewUtils.isLandscape(this)) 3 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        binding.recyclerView.adapter = this.adapter

        this.paginator = RecyclerViewPaginatorV2(
            binding.recyclerView, { this.viewModel.inputs.nextPage() },
            this.viewModel.outputs.isFetchingProjects()
        )

        this.viewModel.outputs.avatarImageViewUrl()
            .compose(observeForUIV2())
            .subscribe { url -> binding.avatarImageView.loadCircleImage(url) }
            .addToDisposable(disposables)

        this.viewModel.outputs.backedCountTextViewHidden()
            .compose(observeForUIV2())
            .subscribe {
                binding.backedCountTextView.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.backedCountTextViewText()
            .compose(observeForUIV2())
            .subscribe {
                binding.backedCountTextView.text = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.backedTextViewHidden()
            .compose(observeForUIV2())
            .subscribe {
                binding.backedTextView.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.createdCountTextViewHidden()
            .compose(observeForUIV2())
            .subscribe {
                binding.createdCountTextView.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.createdCountTextViewText()
            .compose(observeForUIV2())
            .subscribe {
                binding.createdCountTextView.text = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.createdTextViewHidden()
            .compose(observeForUIV2())
            .subscribe {
                binding.createdTextView.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.dividerViewHidden()
            .compose(observeForUIV2())
            .subscribe {
                binding.dividerView.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.projectList()
            .compose(observeForUIV2())
            .subscribe {
                runOnUiThread {
                    this.loadProjects(it)
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.resumeDiscoveryActivity()
            .compose(observeForUIV2())
            .subscribe { resumeDiscoveryActivity() }
            .addToDisposable(disposables)

        this.viewModel.outputs.startMessageThreadsActivity()
            .compose(observeForUIV2())
            .subscribe { this.startMessageThreadsActivity() }
            .addToDisposable(disposables)

        this.viewModel.outputs.startProjectActivity()
            .compose(observeForUIV2())
            .subscribe { this.startProjectActivity(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.userNameTextViewText()
            .compose(observeForUIV2())
            .subscribe { binding.userNameTextView.text = it }
            .addToDisposable(disposables)

        binding.profileActivityToolbar.messagesButton.setOnClickListener { this.viewModel.inputs.messagesButtonClicked() }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.paginator.stop()
        binding.recyclerView.adapter = null
        disposables.clear()
    }

    private fun loadProjects(projects: List<Project>) {
        if (projects.isEmpty()) {
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.setPadding(
                0,
                binding.recyclerView.paddingTop,
                binding.recyclerView.paddingRight,
                binding.recyclerView.paddingBottom
            )

            if (ViewUtils.isPortrait(this)) {
                disableNestedScrolling()
            }
        }

        this.adapter.takeProjects(projects)
    }

    private fun disableNestedScrolling() {
        binding.recyclerView.isNestedScrollingEnabled = false
    }

    private fun resumeDiscoveryActivity() {
        ApplicationUtils.resumeDiscoveryActivity(this)
    }

    private fun startMessageThreadsActivity() {
        val intent = Intent(this, MessageThreadsActivity::class.java)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProjectActivity(project: Project) {
        val intent = Intent().getProjectIntent(this)
            .putExtra(IntentKey.PROJECT, project)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
