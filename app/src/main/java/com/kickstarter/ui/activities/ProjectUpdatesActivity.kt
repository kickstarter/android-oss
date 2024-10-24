package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ActivityUpdatesBinding
import com.kickstarter.libs.recyclerviewpagination.RecyclerViewPaginatorV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.UpdatesAdapter
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.ProjectUpdatesViewModel.Factory
import com.kickstarter.viewmodels.ProjectUpdatesViewModel.ProjectUpdatesViewModel
import io.reactivex.disposables.CompositeDisposable

class ProjectUpdatesActivity : AppCompatActivity(), UpdatesAdapter.Delegate {
    private val adapter: UpdatesAdapter = UpdatesAdapter(this)
    private lateinit var recyclerViewPaginator: RecyclerViewPaginatorV2

    private lateinit var binding: ActivityUpdatesBinding

    private lateinit var viewModelFactory: Factory
    private val viewModel: ProjectUpdatesViewModel by viewModels {
        viewModelFactory
    }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatesBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        setUpConnectivityStatusCheck(lifecycle)

        this.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env, intent)
        }

        binding.updatesRecyclerView.adapter = adapter
        binding.updatesRecyclerView.layoutManager = LinearLayoutManager(this)

        recyclerViewPaginator = RecyclerViewPaginatorV2(
            binding.updatesRecyclerView,
            { viewModel.inputs.nextPage() },
            viewModel.outputs.isFetchingUpdates()
        )

        binding.updatesSwipeRefreshLayout.setOnRefreshListener {
            viewModel.inputs.refresh()
        }

        viewModel.outputs.isFetchingUpdates()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                // - Hides loading spinner from SwipeRefreshLayout according to isFetchingUpdates
                binding.updatesSwipeRefreshLayout.isRefreshing = it
            }
            .addToDisposable(disposables)

        binding.updatesToolbar.commentsToolbar.setTitle(getString(R.string.project_subpages_menu_buttons_updates))

        viewModel.outputs.horizontalProgressBarIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.updatesProgressBar.isGone = it
            }
            .addToDisposable(disposables)

        viewModel.outputs.startUpdateActivity()
            .compose(Transformers.observeForUIV2())
            .subscribe { startUpdateActivity(it.first, it.second) }
            .addToDisposable(disposables)

        viewModel.outputs.projectAndUpdates()
            .compose(Transformers.observeForUIV2())
            .subscribe { adapter.takeData(it) }
            .addToDisposable(disposables)

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerViewPaginator.stop()
        binding.updatesRecyclerView.adapter = null
        disposables.clear()
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
}
