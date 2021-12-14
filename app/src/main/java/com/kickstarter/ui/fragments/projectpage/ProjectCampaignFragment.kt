package com.kickstarter.ui.fragments.projectpage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.FragmentProjectCampaignBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Configure
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.VideoActivity
import com.kickstarter.ui.adapters.projectcampaign.HeaderElementAdapter
import com.kickstarter.ui.adapters.projectcampaign.ViewElementAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.views.RecyclerViewScrollListener
import com.kickstarter.viewmodels.projectpage.ProjectCampaignViewModel
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@RequiresFragmentViewModel(ProjectCampaignViewModel.ViewModel::class)
class ProjectCampaignFragment :
    BaseFragment<ProjectCampaignViewModel.ViewModel>(),
    Configure,
    ViewElementAdapter.FullScreenDelegate {

    private var binding: FragmentProjectCampaignBinding? = null
    private var viewElementAdapter: ViewElementAdapter? = null
    private var delegate: ProjectCampaignFragmentDelegate? = null

    var startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data = result.data?.getLongExtra(IntentKey.VIDEO_SEEK_POSITION, 0)
            data?.let { viewModel.inputs.closeFullScreenVideo(it) }
            // viewElementAdapter?.setPlayerSeekPosition(index,data)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentProjectCampaignBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewElementAdapter = ViewElementAdapter(requireActivity(), this)
        val headerElementAdapter = HeaderElementAdapter()
        binding?.projectCampaignViewListItems?.itemAnimator?.changeDuration = 0
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
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                viewElementAdapter?.submitList(it)
            }

        this.viewModel.outputs.onScrollToVideoPosition()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .delay(300, TimeUnit.MILLISECONDS)
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.projectCampaignViewListItems?.smoothScrollToPosition(it + 1)
            }

        this.viewModel.outputs.onOpenVideoInFullScreen()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                val intent = Intent(context, VideoActivity::class.java)
                    .putExtra(IntentKey.VIDEO_URL_SOURCE, it.first)
                    .putExtra(IntentKey.VIDEO_SEEK_POSITION, it.second)
                startForResult.launch(intent)
            }

        this.viewModel.outputs.updateVideoCloseSeekPosition()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                viewElementAdapter?.setPlayerSeekPosition(it.first, it.second)
            }

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

    override fun onFullScreenClosed(index: Int) {
    }

    override fun onFullScreenOpened(index: Int, source: String, seekPosition: Long) {
        viewModel.inputs.openVideoInFullScreen(index, source, seekPosition)
    }

    fun setProjectCampaignFragmentDelegate(delegate: ProjectCampaignFragmentDelegate) {
        this.delegate = delegate
    }

    interface ProjectCampaignFragmentDelegate {
        fun onFullScreenVideoOpened(video: Pair<String, Long>)
    }
}
