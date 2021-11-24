package com.kickstarter.ui.fragments.projectpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.databinding.FragmentProjectCampaignBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Configure
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.adapters.projectcampaign.ViewElementAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.ProjectCampaignViewModel
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@RequiresFragmentViewModel(ProjectCampaignViewModel.ViewModel::class)
class ProjectCampaignFragment : BaseFragment<ProjectCampaignViewModel.ViewModel>(), Configure {

    private var binding: FragmentProjectCampaignBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentProjectCampaignBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var adapter = ViewElementAdapter(requireActivity())
        binding?.projectCampaignViewListItems?.layoutManager = LinearLayoutManager(context)
        binding?.projectCampaignViewListItems?.adapter = adapter

        this.viewModel.outputs.storyViewElements()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .delay(200, TimeUnit.MILLISECONDS)
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                adapter.submitList(it)
            }
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
