package com.kickstarter.ui.fragments.projectpage

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.FragmentProjectCampaignBinding
import com.kickstarter.libs.Configure
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.projectcampaign.HeaderElementAdapter
import com.kickstarter.ui.adapters.projectcampaign.ViewElementAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.startVideoActivity
import com.kickstarter.ui.views.RecyclerViewScrollListener
import com.kickstarter.viewmodels.projectpage.ProjectCampaignViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ProjectCampaignFragment :
    Fragment(),
    Configure,
    ViewElementAdapter.FullScreenDelegate {

    private lateinit var viewModelFactory: ProjectCampaignViewModel.Factory
    private val viewModel: ProjectCampaignViewModel.ProjectCampaignViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()

    private var binding: FragmentProjectCampaignBinding? = null
    private var viewElementAdapter: ViewElementAdapter? = null

    var startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data = result.data?.getLongExtra(IntentKey.VIDEO_SEEK_POSITION, 0)
            data?.let { viewModel.inputs.closeFullScreenVideo(it) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = ProjectCampaignViewModel.Factory(env)
        }

        binding = FragmentProjectCampaignBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewElementAdapter = ViewElementAdapter(requireActivity(), this)
        viewElementAdapter.let {
            lifecycle.addObserver(it as LifecycleObserver)
        }

        val headerElementAdapter = HeaderElementAdapter()

        binding?.projectCampaignViewListItems?.itemAnimator = null
        binding?.projectCampaignViewListItems?.layoutManager = LinearLayoutManager(context)
        binding?.projectCampaignViewListItems?.adapter = ConcatAdapter(
            headerElementAdapter,
            viewElementAdapter
        )

        headerElementAdapter.updateTitle(resources.getString(R.string.Story))

        this.viewModel.outputs.storyViewElements()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .delay(170, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewElementAdapter?.submitList(it)
            }.addToDisposable(disposables)

        this.viewModel.outputs.onScrollToVideoPosition()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .delay(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.projectCampaignViewListItems?.smoothScrollToPosition(it + 1)
            }.addToDisposable(disposables)

        this.viewModel.outputs.onOpenVideoInFullScreen()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                requireActivity().startVideoActivity(startForResult, it.first, it.second)
            }.addToDisposable(disposables)

        this.viewModel.outputs.updateVideoCloseSeekPosition()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewElementAdapter?.setPlayerSeekPosition(it.first, it.second)
            }.addToDisposable(disposables)

        val scrollListener = object : RecyclerViewScrollListener() {
            override fun onItemIsFirstVisibleItem(index: Int) {
                // play just visible item
                if (index != -1) {
                    viewElementAdapter?.playIndexThenPausePreviousPlayer(index)
                }
            }
        }

        binding?.projectCampaignViewListItems?.addOnScrollListener(scrollListener)
    }

    override fun onDetach() {
        super.onDetach()
        viewElementAdapter?.releaseAllPlayers()
    }

    override fun onPause() {
        super.onPause()
        viewElementAdapter?.releasePlayersOnPause()
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel?.inputs?.configureWith(projectData)
    }

    override fun onDestroyView() {
        binding?.projectCampaignViewListItems?.adapter = null
        disposables.clear()
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): ProjectCampaignFragment {
            val fragment = ProjectCampaignFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.PROJECT_PAGER_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onFullScreenOpened(index: Int, source: String, seekPosition: Long) {
        viewModel.inputs.openVideoInFullScreen(index, source, seekPosition)
    }
}
