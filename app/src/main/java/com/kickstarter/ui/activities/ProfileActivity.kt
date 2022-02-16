package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isGone
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ProfileLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.RecyclerViewPaginator
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ProfileAdapter
import com.kickstarter.viewmodels.ProfileViewModel
import com.squareup.picasso.Picasso

@RequiresActivityViewModel(ProfileViewModel.ViewModel::class)
class ProfileActivity : BaseActivity<ProfileViewModel.ViewModel>() {
    private lateinit var adapter: ProfileAdapter
    private lateinit var paginator: RecyclerViewPaginator
    private lateinit var binding: ProfileLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfileLayoutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.adapter = ProfileAdapter(this.viewModel)
        val spanCount = if (ViewUtils.isLandscape(this)) 3 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        binding.recyclerView.adapter = this.adapter

        this.paginator = RecyclerViewPaginator(
            binding.recyclerView, { this.viewModel.inputs.nextPage() },
            this.viewModel.outputs.isFetchingProjects()
        )

        this.viewModel.outputs.avatarImageViewUrl()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { url -> Picasso.get().load(url).transform(CircleTransformation()).into(binding.avatarImageView) }

        this.viewModel.outputs.backedCountTextViewHidden()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding.backedCountTextView.isGone = it
            }

        this.viewModel.outputs.backedCountTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding.backedCountTextView.text = it
            }

        this.viewModel.outputs.backedTextViewHidden()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding.backedTextView.isGone = it
            }

        this.viewModel.outputs.createdCountTextViewHidden()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding.createdCountTextView.isGone = it
            }

        this.viewModel.outputs.createdCountTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding.createdCountTextView.text = it
            }

        this.viewModel.outputs.createdTextViewHidden()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding.createdTextView.isGone = it
            }

        this.viewModel.outputs.dividerViewHidden()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding.dividerView.isGone = it
            }

        this.viewModel.outputs.projectList()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                this.loadProjects(it)
            }

        this.viewModel.outputs.resumeDiscoveryActivity()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { resumeDiscoveryActivity() }

        this.viewModel.outputs.startMessageThreadsActivity()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.startMessageThreadsActivity() }

        this.viewModel.outputs.startProjectActivity()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.startProjectActivity(it) }

        this.viewModel.outputs.userNameTextViewText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.userNameTextView.text = it }

        binding.profileActivityToolbar.messagesButton.setOnClickListener { this.viewModel.inputs.messagesButtonClicked() }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.paginator.stop()
        binding.recyclerView.adapter = null
    }

    private fun loadProjects(projects: List<Project>) {
        if (projects.isEmpty()) {
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.setPadding(
                0, binding.recyclerView.paddingTop, binding.recyclerView.paddingRight, binding.recyclerView.paddingBottom
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
