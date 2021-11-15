package com.kickstarter.ui.fragments.projectpage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.FragmentFrequentlyAskedQuestionBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Configure
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.SimpleDividerItemDecoration
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.MessageCreatorActivity
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.ui.adapters.FrequentlyAskedQuestionsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.FrequentlyAskedQuestionViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresFragmentViewModel(FrequentlyAskedQuestionViewModel.ViewModel::class)
class FrequentlyAskedQuestionFragment :
    BaseFragment<FrequentlyAskedQuestionViewModel.ViewModel>(),
    Configure {
    private var binding: FragmentFrequentlyAskedQuestionBinding? = null
    private var fqaAdapter = FrequentlyAskedQuestionsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentFrequentlyAskedQuestionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        this.viewModel.outputs.projectFaqList()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.answerEmptyStateTv?.isVisible = it.isEmpty()
                binding?.fqaRecyclerView?.isGone = it.isEmpty()
                fqaAdapter.takeData(it)
            }

        this.viewModel.outputs.bindEmptyState()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.answerEmptyStateTv?.isVisible = true
                binding?.fqaRecyclerView?.isGone = true
            }

        binding?.askQuestionButton?.setOnClickListener {
            this.viewModel.inputs.askQuestionButtonClicked()
        }

        this.viewModel.outputs.askQuestionButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe {
                if (it) {
                  getString(R.string.Log_in_to_ask_the_project_creator_directly)
              } else {
                  getString(R.string.Ask_the_project_creator_directly)
              }.apply {
                                binding?.answerEmptyStateTv?.text =
                                    getString(R.string
                                        .Looks_like_there_arent_any_frequently_asked_questions) +
                                            " " + this
                    }

                binding?.answerEmptyStateSepartor?.isGone = it
                binding?.askQuestionButton?.isGone = it
            }

        this.viewModel.outputs.startComposeMessageActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { startComposeMessageActivity(it) }

        this.viewModel.outputs.startMessagesActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { startMessagesActivity(it) }
    }

    private fun startComposeMessageActivity(it: Project?) {
        startActivity(
            Intent(requireContext(), MessageCreatorActivity::class.java)
                .putExtra(IntentKey.PROJECT, it)
        )
    }

    private fun startMessagesActivity(project: Project) {
        startActivity(
            Intent(requireContext(), MessagesActivity::class.java)
                .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, MessagePreviousScreenType.CREATOR_BIO_MODAL)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.BACKING, project.backing())
        )
    }

    private fun setupRecyclerView() {
        binding?.fqaRecyclerView?.adapter = fqaAdapter
        ResourcesCompat.getDrawable(resources, R.drawable.divider_grey_300_horizontal, null)?.let {
            binding?.fqaRecyclerView?.addItemDecoration(SimpleDividerItemDecoration(it))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): FrequentlyAskedQuestionFragment {
            val fragment = FrequentlyAskedQuestionFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.PROJECT_PAGER_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel?.inputs?.configureWith(projectData)
    }
}
