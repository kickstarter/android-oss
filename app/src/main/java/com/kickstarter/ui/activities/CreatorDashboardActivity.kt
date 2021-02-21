package com.kickstarter.ui.activities

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.kickstarter.databinding.CreatorDashboardLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ToolbarUtils.fadeAndTranslateToolbarTitleOnExpand
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.adapters.CreatorDashboardAdapter
import com.kickstarter.ui.adapters.CreatorDashboardBottomSheetAdapter
import com.kickstarter.viewmodels.CreatorDashboardViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(CreatorDashboardViewModel.ViewModel::class)
class CreatorDashboardActivity : BaseActivity<CreatorDashboardViewModel.ViewModel>() {
    private lateinit var bottomSheetAdapter: CreatorDashboardBottomSheetAdapter
    private lateinit var adapter: CreatorDashboardAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var binding: CreatorDashboardLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreatorDashboardLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CreatorDashboardAdapter(viewModel.inputs)

        binding.creatorDashboardRecyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        binding.creatorDashboardRecyclerView.layoutManager = layoutManager

        // Set up the bottom sheet recycler view.
        bottomSheetAdapter = CreatorDashboardBottomSheetAdapter(viewModel.inputs)
        binding.creatorDashboardBottomSheetRecyclerView.adapter = bottomSheetAdapter
        binding.creatorDashboardBottomSheetRecyclerView.layoutManager = LinearLayoutManager(this)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.creatorDashboardBottomSheetRecyclerView)

        fadeAndTranslateToolbarTitleOnExpand(
            binding.creatorDashboardToolbarLayout.creatorDashboardAppBar,
            binding.creatorDashboardToolbarLayout.creatorDashboardProjectNameSmall
        )

        viewModel.outputs.bottomSheetShouldExpand()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { bottomSheetShouldExpand(it) }

        viewModel.outputs.progressBarIsVisible()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ViewUtils.setGone(
                    binding.creatorDashboardProgressBar, !it
                )
            }

        viewModel.outputs.projectDashboardData()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { adapter.takeProjectDashboardData(it) }

        viewModel.outputs.projectName()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setProjectNameTextViews(it) }

        viewModel.outputs.projectsForBottomSheet()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setProjectsForDropdown(it) }

        createAndSetBottomSheetCallback()

        binding.creatorDashboardBottomSheetScrim.setOnClickListener { bottomSheetScrimClicked() }
    }

    override fun back() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            viewModel.inputs.backClicked()
        } else {
            super.back()
        }
    }

    private fun bottomSheetScrimClicked() {
        viewModel.inputs.scrimClicked()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.creatorDashboardRecyclerView.adapter = null
        binding.creatorDashboardBottomSheetRecyclerView.adapter = null
        bottomSheetBehavior.setBottomSheetCallback(null)
    }

    private fun bottomSheetShouldExpand(expand: Boolean) = if (expand) {
        showBottomSheet()
    } else {
        hideBottomSheet()
    }

    private fun createAndSetBottomSheetCallback() {
        val bottomSheetCallback: BottomSheetCallback = object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.creatorDashboardBottomSheetScrim.visibility = View.GONE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback)
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        binding.creatorDashboardBottomSheetScrim.visibility = View.GONE
    }

    private fun setProjectNameTextViews(projectName: String) {
        binding.creatorDashboardToolbarLayout.creatorDashboardProjectName.text = projectName
        binding.creatorDashboardToolbarLayout.creatorDashboardProjectNameSmall.text = projectName
    }

    private fun setProjectsForDropdown(projects: List<Project>) {
        bottomSheetAdapter.takeProjects(projects)
    }

    private fun showBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.creatorDashboardBottomSheetScrim.visibility = View.VISIBLE
    }
}
