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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.FragmentDiscoveryBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.RecyclerViewPaginator
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.SwipeRefresher
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.AnimationUtils.crossFadeAndReverse
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.getPreLaunchProjectActivity
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.getSetPasswordActivity
import com.kickstarter.libs.utils.extensions.isNotNull
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
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

@RequiresFragmentViewModel(DiscoveryFragmentViewModel.ViewModel::class)
class DiscoveryFragment : BaseFragment<DiscoveryFragmentViewModel.ViewModel>() {
    private var heartsAnimation: AnimatorSet? = null
    private var recyclerViewPaginator: RecyclerViewPaginator? = null

    private var binding: FragmentDiscoveryBinding? = null
    private var discoveryEditorialAdapter: DiscoveryEditorialAdapter? = null
    private val projectStarConfirmationString = R.string.project_star_confirmation

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentDiscoveryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.lifecycle()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .filter { it.isNotNull() }
            .subscribe {
                this.viewModel.inputs.fragmentLifeCycle(it)
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
            recyclerViewPaginator = RecyclerViewPaginator(
                this,
                { this@DiscoveryFragment.viewModel.inputs.nextPage() },
                this@DiscoveryFragment.viewModel.outputs.isFetchingProjects()
            )
        }

        binding?.discoverySwipeRefreshLayout?.let {
            SwipeRefresher(
                this,
                it,
                { this.viewModel.inputs.refresh() }
            ) { this.viewModel.outputs.isFetchingProjects() }
        }

        this.viewModel.outputs.activity()
            .compose(bindToLifecycle())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { discoveryActivitySampleAdapter.takeActivity(it) }

        this.viewModel.outputs.startHeartAnimation()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .filter { !(lazyHeartCrossFadeAnimation()?.isRunning?:false) }
            .subscribe { lazyHeartCrossFadeAnimation()?.start() }

        this.viewModel.outputs.projectList()
            .compose(bindToLifecycle())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { discoveryProjectCardAdapter.takeProjects(it) }

        this.viewModel.outputs.shouldShowEditorial()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { discoveryEditorialAdapter.setShouldShowEditorial(it) }

        this.viewModel.outputs.shouldShowEmptySavedView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.discoveryEmptyView?.isGone = !it
            }

        this.viewModel.outputs.shouldShowOnboardingView()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { discoveryOnboardingAdapter.setShouldShowOnboardingView(it) }

        this.viewModel.outputs.showActivityFeed()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startActivityFeedActivity() }

        this.viewModel.outputs.startSetPasswordActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startSetPasswordActivity(it) }

        this.viewModel.outputs.startEditorialActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startEditorialActivity(it) }

        this.viewModel.outputs.startUpdateActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startUpdateActivity(it) }

        this.viewModel.outputs.startProjectActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startProjectActivity(it.first, it.second) }

        this.viewModel.outputs.startPreLaunchProjectActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startPreLaunchProjectActivity(it.first, it.second) }

        this.viewModel.outputs.showLoginTout()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startLoginToutActivity() }

        binding?.discoveryHeartsContainer?.let {
            RxView.clicks(it)
                .compose(bindToLifecycle())
                .subscribe { this.viewModel.inputs.heartContainerClicked() }
        }

        this.viewModel.outputs.startLoginToutActivityToSaveProject()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.startLoginToutActivity() }

        this.viewModel.outputs.scrollToSavedProjectPosition()
            .filter { it != -1 }
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.discoveryRecyclerView?.smoothScrollToPosition(it)
            }

        this.viewModel.outputs.showSavedPrompt()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.showStarToast() }
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
        startActivity(Intent(activity, ActivityFeedActivity::class.java))
    }

    private fun startSetPasswordActivity(email: String) {
        val intent = Intent().getSetPasswordActivity(requireContext(), email)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(requireContext(), TransitionUtils.fadeIn())
    }

    private fun showStarToast() {
        ViewUtils.showToastFromTop(requireContext(), getString(this.projectStarConfirmationString), 0, resources.getDimensionPixelSize(R.dimen.grid_8))
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
        val intent = Intent(activity, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        context?.let {
            TransitionUtils.transition(it, TransitionUtils.fadeIn())
        }
    }

    private fun startPreLaunchProjectActivity(project: Project, refTag: RefTag) {
        val intent = Intent().getPreLaunchProjectActivity(requireContext(), project.slug())
            .putExtra(IntentKey.REF_TAG, refTag)
            .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.DISCOVERY.value)
        startActivity(intent)
        TransitionUtils.transition(requireContext(), TransitionUtils.slideInFromRight())
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
        val intent = Intent(getActivity(), UpdateActivity::class.java)
            .putExtra(IntentKey.PROJECT, activity.project())
            .putExtra(IntentKey.UPDATE, activity.update())
        startActivity(intent)
        context?.let {
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
