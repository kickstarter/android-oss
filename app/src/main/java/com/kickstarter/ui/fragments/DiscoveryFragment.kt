package com.kickstarter.ui.fragments

import android.animation.AnimatorSet
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.FragmentDiscoveryBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.recyclerviewpagination.RecyclerViewPaginatorV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.AnimationUtils.crossFadeAndReverse
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getPreLaunchProjectActivity
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.getSetPasswordActivity
import com.kickstarter.models.Activity
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ActivityFeedActivity
import com.kickstarter.ui.activities.EditorialActivity
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.activities.UpdateActivity
import com.kickstarter.ui.adapters.DiscoveryActivitySampleAdapter
import com.kickstarter.ui.adapters.DiscoveryEditorialAdapter
import com.kickstarter.ui.adapters.DiscoveryOnboardingAdapter
import com.kickstarter.ui.adapters.DiscoveryProjectCardAdapter
import com.kickstarter.ui.data.Editorial
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.viewholders.EditorialViewHolder
import com.kickstarter.viewmodels.DiscoveryFragmentViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class DiscoveryFragment : Fragment() {
    private var heartsAnimation: AnimatorSet? = null
    private var recyclerViewPaginator: RecyclerViewPaginatorV2? = null

    private var binding: FragmentDiscoveryBinding? = null
    private var discoveryEditorialAdapter: DiscoveryEditorialAdapter? = null
    private val projectStarConfirmationString = R.string.project_star_confirmation
    private val disposables = CompositeDisposable()

    private lateinit var viewModelFactory: DiscoveryFragmentViewModel.Factory
    private val viewModel: DiscoveryFragmentViewModel.DiscoveryFragmentViewModel by viewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = DiscoveryFragmentViewModel.Factory(env)
        }
        binding = FragmentDiscoveryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                activity?.runOnUiThread { viewModel.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED) }
            }
        }

        val discoveryActivitySampleAdapter = DiscoveryActivitySampleAdapter(this.viewModel.inputs)
        val discoveryEditorialAdapter = DiscoveryEditorialAdapter(this.viewModel.inputs)
        val discoveryOnboardingAdapter = DiscoveryOnboardingAdapter(this.viewModel.inputs)
        val discoveryProjectCardAdapter = DiscoveryProjectCardAdapter(this.viewModel.inputs)

        val discoveryAdapter = ConcatAdapter(discoveryOnboardingAdapter, discoveryEditorialAdapter, discoveryActivitySampleAdapter, discoveryProjectCardAdapter)
        this.discoveryEditorialAdapter = discoveryEditorialAdapter

        binding?.discoveryRecyclerView?.apply {
            adapter = discoveryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerViewPaginator = RecyclerViewPaginatorV2(
                this,
                { this@DiscoveryFragment.viewModel.inputs.nextPage() },
                this@DiscoveryFragment.viewModel.outputs.isFetchingProjects()
            )
        }

        binding?.discoverySwipeRefreshLayout?.let { swipeRefreshLayout ->
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.inputs.refresh()
            }

            this.viewModel.outputs.isFetchingProjects()
                .compose(Transformers.observeForUIV2())
                .subscribe {
                    swipeRefreshLayout.isRefreshing = it
                }
                .addToDisposable(disposables)
        }

        this.viewModel.outputs.activity()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { discoveryActivitySampleAdapter.takeActivity(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.clearActivities()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { discoveryActivitySampleAdapter.takeActivity(null) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startHeartAnimation()
            .compose(Transformers.observeForUIV2())
            .filter { !(lazyHeartCrossFadeAnimation()?.isRunning?:false) }
            .subscribe { lazyHeartCrossFadeAnimation()?.start() }
            .addToDisposable(disposables)

        this.viewModel.outputs.projectList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { discoveryProjectCardAdapter.takeProjects(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.shouldShowEditorial()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { discoveryEditorialAdapter.setShouldShowEditorial(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.shouldShowEmptySavedView()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding?.discoveryEmptyView?.isGone = !it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.shouldShowOnboardingView()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { discoveryOnboardingAdapter.setShouldShowOnboardingView(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showActivityFeed()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startActivityFeedActivity() }
            .addToDisposable(disposables)

        this.viewModel.outputs.startSetPasswordActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startSetPasswordActivity(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startEditorialActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startEditorialActivity(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startUpdateActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startUpdateActivity(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startProjectActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startProjectActivity(it.first, it.second) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startPreLaunchProjectActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startPreLaunchProjectActivity(it.first, it.second) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showLoginTout()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startLoginToutActivity() }
            .addToDisposable(disposables)

        binding?.discoveryHeartsContainer?.let {
            it.setOnClickListener {
                this.viewModel.inputs.heartContainerClicked()
            }
        }

        this.viewModel.outputs.startLoginToutActivityToSaveProject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.startLoginToutActivity() }
            .addToDisposable(disposables)

        this.viewModel.outputs.scrollToSavedProjectPosition()
            .filter { it != -1 }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.discoveryRecyclerView?.smoothScrollToPosition(it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.showSavedPrompt()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.showStarToast() }
            .addToDisposable(disposables)
    }

    override fun onPause() {
        super.onPause()
        heartsAnimation = null
    }

    override fun onDetach() {
        super.onDetach()
        binding?.discoveryRecyclerView?.adapter = null
        recyclerViewPaginator?.stop()
    }

    val isAttached: Boolean
        get() = viewModel != null

    val isInstantiated: Boolean
        get() = binding?.discoveryRecyclerView != null

    private val editorialImageView: ImageView?
        get() {
            val layoutManager = binding?.discoveryRecyclerView?.layoutManager as? LinearLayoutManager
            if (layoutManager != null && discoveryEditorialAdapter != null) {
                for (i in layoutManager.findFirstVisibleItemPosition()..layoutManager.findLastVisibleItemPosition()) {
                    val childView = layoutManager.getChildAt(i)
                    if (childView != null) {
                        val viewHolder = binding?.discoveryRecyclerView?.getChildViewHolder(childView)
                        if (viewHolder is EditorialViewHolder) {
                            return childView.findViewById(R.id.editorial_graphic)
                        }
                    }
                }
            }
            return null
        }

    private fun lazyHeartCrossFadeAnimation(): AnimatorSet? {
        if (heartsAnimation == null) {
            binding?.discoveryEmptyHeartOutline?.let { discoveryEmptyHeartOutline ->
                binding?.discoveryEmptyHeartFilled?.let {
                    heartsAnimation = crossFadeAndReverse(discoveryEmptyHeartOutline, it, 400L)
                }
            }
        }
        return heartsAnimation
    }

    private fun startActivityFeedActivity() {
        context?.let {
            startActivity(Intent(context, ActivityFeedActivity::class.java))
        }
    }

    private fun startSetPasswordActivity(email: String) {
        context?.let {
            val intent = Intent().getSetPasswordActivity(requireContext(), email)
            startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
            TransitionUtils.transition(requireContext(), TransitionUtils.fadeIn())
        }
    }

    private fun showStarToast() {
        context?.let {
            ViewUtils.showToastFromTop(
                requireContext(),
                getString(this.projectStarConfirmationString),
                0,
                resources.getDimensionPixelSize(R.dimen.grid_8)
            )
        }
    }

    private fun startEditorialActivity(editorial: Editorial) {
        val activity = activity
        // The transition view must be an ImageView
        val editorialImageView = editorialImageView
        if (activity != null && editorialImageView != null) {
            val intent = Intent(activity, EditorialActivity::class.java)
                .putExtra(IntentKey.EDITORIAL, editorial)
            val options = ActivityOptions.makeSceneTransitionAnimation(activity, editorialImageView, "editorial")
            startActivity(intent, options.toBundle())
        }
    }

    private fun startLoginToutActivity() {
        context?.let {
            val intent = Intent(context, LoginToutActivity::class.java)
                .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT)
            startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
            TransitionUtils.transition(it, TransitionUtils.fadeIn())
        }
    }

    private fun startPreLaunchProjectActivity(project: Project, refTag: RefTag) {
        context?.let {
            val intent = Intent().getPreLaunchProjectActivity(requireContext(), project.slug())
                .putExtra(IntentKey.REF_TAG, refTag)
                .putExtra(
                    IntentKey.PREVIOUS_SCREEN,
                    ThirdPartyEventValues.ScreenName.DISCOVERY.value
                )
            startActivity(intent)
            TransitionUtils.transition(requireContext(), TransitionUtils.slideInFromRight())
        }
    }

    private fun startProjectActivity(project: Project, refTag: RefTag) {
        context?.let {
            val intent = Intent().getProjectIntent(it)
                .putExtra(IntentKey.PROJECT_PARAM, project.slug())
                .putExtra(IntentKey.REF_TAG, refTag)
                .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.DISCOVERY.value)
            startActivity(intent)
            TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
        }
    }

    private fun startUpdateActivity(activity: Activity) {
        context?.let {
            val intent = Intent(context, UpdateActivity::class.java)
                .putExtra(IntentKey.PROJECT, activity.project())
                .putExtra(IntentKey.UPDATE, activity.update())
            startActivity(intent)
            TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
        }
    }

    fun refresh() {
        this.viewModel.inputs.refresh()
    }

    fun takeCategories(categories: List<Category>) {
        this.viewModel.inputs.rootCategories(categories)
    }

    fun updateParams(params: DiscoveryParams) {
        this.viewModel.inputs.paramsFromActivity(params)
    }

    fun clearPage() {
        this.viewModel.inputs.clearPage()
    }

    fun scrollToTop() {
        binding?.discoveryRecyclerView?.smoothScrollToPosition(0)
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): DiscoveryFragment {
            val fragment = DiscoveryFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.DISCOVERY_SORT_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
