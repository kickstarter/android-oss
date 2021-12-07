package com.kickstarter.ui.fragments.projectpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.FragmentProjectCampaignBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Configure
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.adapters.projectcampaign.HeaderElementAdapter
import com.kickstarter.ui.adapters.projectcampaign.ViewElementAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.views.RecyclerViewScrollListener
import com.kickstarter.viewmodels.projectpage.ProjectCampaignViewModel
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@RequiresFragmentViewModel(ProjectCampaignViewModel.ViewModel::class)
class ProjectCampaignFragment : BaseFragment<ProjectCampaignViewModel.ViewModel>(), Configure {

    private var binding: FragmentProjectCampaignBinding? = null
    private var viewElementAdapter: ViewElementAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentProjectCampaignBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewElementAdapter = ViewElementAdapter(requireActivity())
        val headerElementAdapter = HeaderElementAdapter()
        binding?.projectCampaignViewListItems?.layoutManager = LinearLayoutManager(context)
        binding?.projectCampaignViewListItems?.adapter = ConcatAdapter(headerElementAdapter,
            viewElementAdapter)

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

    override fun onDestroy() {
        super.onDestroy()
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
}
