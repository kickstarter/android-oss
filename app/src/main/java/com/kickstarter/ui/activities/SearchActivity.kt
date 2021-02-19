package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView
import com.kickstarter.R
import com.kickstarter.databinding.SearchLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.RecyclerViewPaginator
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.InputUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.SearchAdapter
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.viewmodels.SearchViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(SearchViewModel.ViewModel::class)
class SearchActivity : BaseActivity<SearchViewModel.ViewModel>(), SearchAdapter.Delegate {
    private lateinit var adapter: SearchAdapter
    private lateinit var paginator: RecyclerViewPaginator
    lateinit var binding: SearchLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SearchLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SearchAdapter(this)
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchRecyclerView.adapter = adapter

        paginator = RecyclerViewPaginator(binding.searchRecyclerView, { viewModel.inputs.nextPage() }, viewModel.outputs.isFetchingProjects)

        RxRecyclerView.scrollEvents(binding.searchRecyclerView)
            .compose(bindToLifecycle())
            .filter { scrollEvent: RecyclerViewScrollEvent -> scrollEvent.dy() != 0 } // Skip scroll events when y is 0, usually indicates new data
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { InputUtils.hideKeyboard(this, currentFocus) }

        viewModel.outputs.popularProjects()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.loadPopularProjects(it) }

        viewModel.outputs.searchProjects()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.loadSearchProjects(it) }

        viewModel.outputs.startProjectActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startProjectActivity(it) }
    }

    private fun startProjectActivity(projectAndRefTag: Pair<Project, RefTag>) {
        val intent = Intent(this, ProjectActivity::class.java)
            .putExtra(IntentKey.PROJECT, projectAndRefTag.first)
            .putExtra(IntentKey.REF_TAG, projectAndRefTag.second)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    override fun onDestroy() {
        super.onDestroy()
        paginator.stop()
        binding.searchRecyclerView.adapter = null
    }

    override fun projectSearchResultClick(viewHolder: KSViewHolder?, project: Project?) {
        project?.let { viewModel.inputs.projectClicked(it) }
    }
}
