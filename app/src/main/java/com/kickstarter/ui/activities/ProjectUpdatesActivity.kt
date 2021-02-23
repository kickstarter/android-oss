package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ActivityUpdatesBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.RecyclerViewPaginator
import com.kickstarter.libs.SwipeRefresher
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.UpdatesAdapter
import com.kickstarter.viewmodels.ProjectUpdatesViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ProjectUpdatesViewModel.ViewModel::class)
class ProjectUpdatesActivity : BaseActivity<ProjectUpdatesViewModel.ViewModel>(), UpdatesAdapter.Delegate {
    private val adapter: UpdatesAdapter = UpdatesAdapter(this)
    private lateinit var recyclerViewPaginator: RecyclerViewPaginator
    private lateinit var swipeRefresher: SwipeRefresher

    private lateinit var binding: ActivityUpdatesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.updatesRecyclerView.adapter = adapter
        binding.updatesRecyclerView.layoutManager = LinearLayoutManager(this)
        
        recyclerViewPaginator = RecyclerViewPaginator(
            binding.updatesRecyclerView,
            { viewModel.inputs.nextPage() }, 
            viewModel.outputs.isFetchingUpdates
        )
        
        swipeRefresher = SwipeRefresher(
            this, binding.updatesSwipeRefreshLayout, { viewModel.inputs.refresh() }
        ) { viewModel.outputs.isFetchingUpdates }
        
        binding.updatesToolbar.commentsToolbar.setTitle(getString(R.string.project_subpages_menu_buttons_updates))
        
        viewModel.outputs.horizontalProgressBarIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.updatesProgressBar))
        
        viewModel.outputs.startUpdateActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startUpdateActivity(it.first, it.second) }
        
        viewModel.outputs.projectAndUpdates()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.takeData(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerViewPaginator.stop()
        binding.updatesRecyclerView.adapter = null
    }

    override fun updateCardClicked(update: Update) {
        viewModel.inputs.updateClicked(update)
    }

    private fun startUpdateActivity(project: Project, update: Update) {
        val intent = Intent(this, UpdateActivity::class.java)
            .putExtra(IntentKey.PROJECT, project)
            .putExtra(IntentKey.UPDATE, update)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    override fun exitTransition() = TransitionUtils.slideInFromLeft()
}
