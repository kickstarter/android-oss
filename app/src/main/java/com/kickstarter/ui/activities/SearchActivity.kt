package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.SearchLayoutBinding
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.recyclerviewpagination.RecyclerViewPaginatorV2
import com.kickstarter.libs.recyclerviewpagination.RecyclerViewScrollEvent
import com.kickstarter.libs.recyclerviewpagination.RxRecyclerView
import com.kickstarter.libs.utils.InputUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getPreLaunchProjectActivity
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.SearchAdapter
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.viewmodels.SearchViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class SearchActivity : AppCompatActivity(), SearchAdapter.Delegate {
    private lateinit var adapter: SearchAdapter
    private lateinit var paginator: RecyclerViewPaginatorV2
    lateinit var binding: SearchLayoutBinding

    private lateinit var viewModelFactory: SearchViewModel.Factory
    val viewModel: SearchViewModel.SearchViewModel by viewModels { viewModelFactory }

    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        disposables = CompositeDisposable()

        val env = this.getEnvironment()?.let { env ->
            viewModelFactory = SearchViewModel.Factory(env, intent = intent)
            env
        }

        binding = SearchLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SearchAdapter(this)
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchRecyclerView.adapter = adapter

        paginator = RecyclerViewPaginatorV2(binding.searchRecyclerView, { viewModel.inputs.nextPage() }, viewModel.outputs.isFetchingProjects())

        RxRecyclerView.scrollEvents(binding.searchRecyclerView)
            .filter { scrollEvent: RecyclerViewScrollEvent -> scrollEvent.dy() != 0 } // Skip scroll events when y is 0, usually indicates new data
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { InputUtils.hideKeyboard(this, currentFocus) }
            .addToDisposable(disposables)

        viewModel.outputs.popularProjects()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.loadPopularProjects(it) }
            .addToDisposable(disposables)

        viewModel.outputs.searchProjects()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.loadSearchProjects(it) }
            .addToDisposable(disposables)

        viewModel.outputs.startProjectActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startProjectActivity(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startPreLaunchProjectActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startPreLaunchProjectActivity(it.first, it.second) }
            .addToDisposable(disposables)
    }

    private fun startPreLaunchProjectActivity(project: Project, refTag: RefTag) {
        val intent = Intent().getPreLaunchProjectActivity(this, project.slug())
            .putExtra(IntentKey.REF_TAG, refTag)
            .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.SEARCH.value)
        startActivity(intent)
        TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
    }
    private fun startProjectActivity(projectAndRefTagAndIsFfEnabled: Pair<Project, RefTag>) {
        val intent = Intent().getProjectIntent(this)
            .putExtra(IntentKey.PROJECT, projectAndRefTagAndIsFfEnabled.first)
            .putExtra(IntentKey.REF_TAG, projectAndRefTagAndIsFfEnabled.second)
            .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.SEARCH.value)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    override fun onDestroy() {
        super.onDestroy()
        paginator.stop()
        binding.searchRecyclerView.adapter = null
        disposables.clear()
    }

    override fun projectSearchResultClick(viewHolder: KSViewHolder?, project: Project?) {
        project?.let { viewModel.inputs.projectClicked(it) }
    }
}
