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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kickstarter.R
import com.kickstarter.databinding.FragmentFrequentlyAskedQuestionBinding
import com.kickstarter.libs.Configure
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.SimpleDividerItemDecoration
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.reduceProjectPayload
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.MessageCreatorActivity
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.ui.adapters.FrequentlyAskedQuestionsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.FrequentlyAskedQuestionViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class FrequentlyAskedQuestionFragment : Fragment(), Configure {

    private var binding: FragmentFrequentlyAskedQuestionBinding? = null
    private var fqaAdapter = FrequentlyAskedQuestionsAdapter()
    private var disposables = CompositeDisposable()

    private lateinit var viewModelFactory: FrequentlyAskedQuestionViewModel.Factory
    private val viewModel: FrequentlyAskedQuestionViewModel.FrequentlyAskedQuestionViewModel by viewModels { viewModelFactory }
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

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = FrequentlyAskedQuestionViewModel.Factory(env)
        }

        this.viewModel.outputs.projectFaqList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.answerEmptyStateTv?.isVisible = it.isEmpty()
                binding?.fqaRecyclerView?.isGone = it.isEmpty()
                fqaAdapter.takeData(it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.bindEmptyState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.answerEmptyStateTv?.isVisible = true
                binding?.fqaRecyclerView?.isGone = true
            }
            .addToDisposable(disposables)

        binding?.askQuestionButton?.setOnClickListener {
            this.viewModel.inputs.askQuestionButtonClicked()
        }

        this.viewModel.outputs.askQuestionButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it) {
                    getString(R.string.Log_in_to_ask_the_project_creator_directly)
                } else {
                    getString(R.string.Ask_the_project_creator_directly)
                }.apply {
                    binding?.answerEmptyStateTv?.text =
                        getString(
                        R.string
                            .Looks_like_there_arent_any_frequently_asked_questions
                    ) +
                        " " + this
                }

                binding?.answerEmptyStateSepartor?.isGone = it
                binding?.askQuestionButton?.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.startComposeMessageActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startComposeMessageActivity(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startMessagesActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startMessagesActivity(it) }
            .addToDisposable(disposables)
    }

    private fun startComposeMessageActivity(it: Project?) {
        startActivity(
            Intent(requireContext(), MessageCreatorActivity::class.java)
                .putExtra(IntentKey.PROJECT, it?.reduceProjectPayload())
        )
    }

    private fun startMessagesActivity(project: Project) {
        startActivity(
            Intent(requireContext(), MessagesActivity::class.java)
                .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, MessagePreviousScreenType.CREATOR_BIO_MODAL)
                .putExtra(IntentKey.PROJECT, project.reduceProjectPayload())
                .putExtra(IntentKey.BACKING, project.backing())
        )
    }

    private fun setupRecyclerView() {
        binding?.fqaRecyclerView?.overScrollMode = View.OVER_SCROLL_NEVER
        binding?.fqaRecyclerView?.adapter = fqaAdapter
        ResourcesCompat.getDrawable(resources, R.drawable.divider_grey_300_horizontal, null)?.let {
            binding?.fqaRecyclerView?.addItemDecoration(SimpleDividerItemDecoration(it))
        }
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
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
