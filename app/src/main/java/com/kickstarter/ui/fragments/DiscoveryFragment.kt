package com.kickstarter.ui.fragments

import android.animation.AnimatorSet
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.libs.*
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.AnimationUtils.crossFadeAndReverse
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Activity
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.*
import com.kickstarter.ui.adapters.DiscoveryAdapter
import com.kickstarter.ui.data.Editorial
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.viewholders.EditorialViewHolder
import com.kickstarter.viewmodels.DiscoveryFragmentViewModel
import kotlinx.android.synthetic.main.fragment_discovery.*

@RequiresFragmentViewModel(DiscoveryFragmentViewModel.ViewModel::class)
class DiscoveryFragment : BaseFragment<DiscoveryFragmentViewModel.ViewModel>() {
    private var heartsAnimation: AnimatorSet? = null
    private var recyclerViewPaginator: RecyclerViewPaginator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_discovery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val discoveryAdapter = DiscoveryAdapter(this.viewModel.inputs)

        discovery_recycler_view.apply {
            adapter = discoveryAdapter
            layoutManager = LinearLayoutManager(context)
        }

        SwipeRefresher(
            this, discovery_swipe_refresh_layout, { this.viewModel.inputs.refresh() }
        ) { this.viewModel.outputs.isFetchingProjects }

        recyclerViewPaginator = RecyclerViewPaginator(
            discovery_recycler_view,
            { this.viewModel.inputs.nextPage() },
            this.viewModel.outputs.isFetchingProjects
        )

        this.viewModel.outputs.activity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { discoveryAdapter.takeActivity(it) }

        this.viewModel.outputs.startHeartAnimation()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .filter { !(lazyHeartCrossFadeAnimation()?.isRunning?:false) }
            .subscribe { lazyHeartCrossFadeAnimation()?.start() }

        this.viewModel.outputs.projectList()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { discoveryAdapter.takeProjects(it) }

        this.viewModel.outputs.shouldShowEditorial()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { discoveryAdapter.setShouldShowEditorial(it) }

        this.viewModel.outputs.shouldShowEmptySavedView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { ViewUtils.setGone(discovery_empty_view, !it) }

        this.viewModel.outputs.shouldShowOnboardingView()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { discoveryAdapter.setShouldShowOnboardingView(it) }

        this.viewModel.outputs.showActivityFeed()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startActivityFeedActivity() }

        this.viewModel.outputs.startEditorialActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startEditorialActivity(it) }

        this.viewModel.outputs.startUpdateActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startUpdateActivity(it) }

        this.viewModel.outputs.startProjectActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startProjectActivity(it.first, it.second) }

        this.viewModel.outputs.showLoginTout()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startLoginToutActivity() }

        RxView.clicks(discovery_hearts_container)
            .compose(bindToLifecycle())
            .subscribe { this.viewModel.inputs.heartContainerClicked() }
    }

    override fun onPause() {
        super.onPause()
        heartsAnimation = null
    }

    override fun onDetach() {
        super.onDetach()
        discovery_recycler_view?.adapter = null
        recyclerViewPaginator?.stop()
    }

    val isAttached: Boolean
        get() = viewModel != null

    val isInstantiated: Boolean
        get() = discovery_recycler_view != null

    private val editorialImageView: ImageView?
        get() {
            val layoutManager = discovery_recycler_view.layoutManager as? LinearLayoutManager
            val discoveryAdapter = discovery_recycler_view.adapter as? DiscoveryAdapter
            if (layoutManager != null && discoveryAdapter != null) {
                for (i in layoutManager.findFirstVisibleItemPosition()..
                          layoutManager.findLastVisibleItemPosition()) {
                    val childView = layoutManager.getChildAt(i)
                    if (childView != null) {
                        val viewHolder = discovery_recycler_view.getChildViewHolder(childView)
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
            heartsAnimation = crossFadeAndReverse(discovery_empty_heart_outline, discovery_empty_heart_filled, 400L)
        }
        return heartsAnimation
    }

    private fun startActivityFeedActivity() {
        startActivity(Intent(activity, ActivityFeedActivity::class.java))
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

    private fun startProjectActivity(project: Project, refTag: RefTag) {
        val intent = Intent(activity, ProjectActivity::class.java)
            .putExtra(IntentKey.PROJECT, project)
            .putExtra(IntentKey.REF_TAG, refTag)
        startActivity(intent)
        context?.let {
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

    fun takeCategories(categories: List<Category?>) {
        this.viewModel.inputs.rootCategories(categories)
    }

    fun updateParams(params: DiscoveryParams) {
        this.viewModel.inputs.paramsFromActivity(params)
    }

    fun clearPage() {
        this.viewModel.inputs.clearPage()
    }

    fun scrollToTop() {
        discovery_recycler_view.smoothScrollToPosition(0)
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
