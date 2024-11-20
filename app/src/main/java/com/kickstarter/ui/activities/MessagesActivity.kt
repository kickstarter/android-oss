package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Pair
import android.view.View
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.kickstarter.R
import com.kickstarter.databinding.MessagesLayoutBinding
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.models.Backing
import com.kickstarter.models.BackingWrapper
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.MessagesAdapter
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.MessagesViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.math.RoundingMode

class MessagesActivity : AppCompatActivity() {
    private lateinit var ksCurrency: KSCurrency
    private lateinit var ksString: KSString
    private lateinit var adapter: MessagesAdapter

    private lateinit var binding: MessagesLayoutBinding

    private lateinit var viewModelFactory: MessagesViewModel.Factory
    private val viewModel: MessagesViewModel.MessagesViewModel by viewModels {
        viewModelFactory
    }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MessagesLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        setUpConnectivityStatusCheck(lifecycle)

        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = MessagesViewModel.Factory(env, intent)
            env
        }

        ksCurrency = requireNotNull(environment?.ksCurrency())
        ksString = requireNotNull(environment?.ksString())
        adapter = MessagesAdapter()

        binding.messagesRecyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.messagesRecyclerView.layoutManager = layoutManager

        binding.messagesBackingInfoView.messagesViewPledgeButton.text = getString(R.string.project_view_button)

        setAppBarOffsetChangedListener(binding.messagesAppBarLayout)

        binding.messageReplyLayout.messageEditText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            viewModel.inputs.messageEditTextIsFocused(hasFocus)
        }

        viewModel.outputs.backButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ViewUtils.setGone(binding.messagesToolbar.messagesToolbarBackButton, it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.backingAndProject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setBackingInfoView(it) }
            .addToDisposable(disposables)

        viewModel.outputs.backingInfoViewIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.setGone(binding.messagesBackingInfoView.backingInfoView, it) }
            .addToDisposable(disposables)

        viewModel.outputs.closeButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.setGone(binding.messagesToolbar.messagesToolbarCloseButton, it) }
            .addToDisposable(disposables)

        viewModel.outputs.creatorNameTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messagesCreatorNameTextView.text = ksString.format(getString(R.string.project_creator_by_creator), "creator_name", it) }
            .addToDisposable(disposables)

        viewModel.outputs.loadingIndicatorViewIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.setGone(binding.messagesLoadingIndicator, it) }
            .addToDisposable(disposables)

        viewModel.outputs.messageEditTextHint()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setMessageEditTextHint(it) }
            .addToDisposable(disposables)

        viewModel.outputs.messageEditTextShouldRequestFocus()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { requestFocusAndOpenKeyboard() }
            .addToDisposable(disposables)

        viewModel.outputs.messageList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.messages(it) }
            .addToDisposable(disposables)

        viewModel.outputs.projectNameTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messagesProjectNameTextView.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.projectNameToolbarTextViewText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messagesToolbar.messagesProjectNameCollapsedTextView.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.recyclerViewDefaultBottomPadding()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setDefaultRecyclerViewBottomPadding() }
            .addToDisposable(disposables)

        viewModel.outputs.recyclerViewInitialBottomPadding()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setInitialRecyclerViewBottomPadding(it) }
            .addToDisposable(disposables)

        viewModel.outputs.scrollRecyclerViewToBottom()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messagesRecyclerView.scrollToPosition(adapter.itemCount - 1) }
            .addToDisposable(disposables)

        viewModel.outputs.setMessageEditText()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messageReplyLayout.messageEditText.setText(it) }
            .addToDisposable(disposables)

        viewModel.outputs.sendMessageButtonIsEnabled()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messageReplyLayout.sendMessageButton.isEnabled = it }
            .addToDisposable(disposables)

        viewModel.outputs.showMessageErrorToast()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showToast(this, it) }
            .addToDisposable(disposables)

        viewModel.outputs.startBackingActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startBackingActivity(it) }
            .addToDisposable(disposables)

        viewModel.outputs.startProjectPageActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startProjectPageActivity(it) }
            .addToDisposable(disposables)

        viewModel.outputs.toolbarIsExpanded()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messagesAppBarLayout.setExpanded(it) }
            .addToDisposable(disposables)

        viewModel.outputs.viewPledgeButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.setGone(binding.messagesBackingInfoView.messagesViewPledgeButton, it) }
            .addToDisposable(disposables)

        binding.messagesBackingInfoView.messagesViewPledgeButton.setOnClickListener {
            viewPledgeButtonClicked()
        }

        binding.messagesProjectContainerView.setOnClickListener {
            projectContainerViewClicked()
        }

        binding.messageReplyLayout.sendMessageButton.setOnClickListener {
            sendMessageButtonClicked()
        }

        binding.messagesToolbar.messagesToolbarBackButton.setOnClickListener {
            finishWithAnimation()
        }

        binding.messagesToolbar.messagesToolbarCloseButton.setOnClickListener {
            finishWithAnimation()
        }

        binding.messageReplyLayout.messageEditText.doOnTextChanged { message, _, _, _ ->
            message?.let { onMessageEditTextChanged(it) }
        }

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }
    }

    private fun sendMessageButtonClicked() =
        viewModel.inputs.sendMessageButtonClicked()

    private fun viewPledgeButtonClicked() =
        viewModel.inputs.viewPledgeButtonClicked()

    private fun projectContainerViewClicked() =
        viewModel.inputs.projectContainerViewClicked()

    private fun onMessageEditTextChanged(message: CharSequence) =
        viewModel.inputs.messageEditTextChanged(message.toString())

    override fun onDestroy() {
        super.onDestroy()
        binding.messagesRecyclerView.adapter = null
        disposables.clear()
    }

    private fun requestFocusAndOpenKeyboard() {
        binding.messageReplyLayout.messageEditText.requestFocus()
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    /**
     * Sets an OffsetChangedListener for the view's AppBarLayout to:
     * 1. determine the toolbar's alpha based on scroll range
     * 2. adjust the view's bottom padding via inputs
     */
    private fun setAppBarOffsetChangedListener(appBarLayout: AppBarLayout) {
        appBarLayout.addOnOffsetChangedListener(
            OnOffsetChangedListener { layout: AppBarLayout, offset: Int ->
                binding.messagesToolbar.messagesProjectNameCollapsedTextView.alpha = (Math.abs(offset) / layout.totalScrollRange).toFloat()
                viewModel.inputs.appBarTotalScrollRange(layout.totalScrollRange)
                viewModel.inputs.appBarOffset(offset)
            }
        )
    }

    private fun setBackingInfoView(backingAndProject: Pair<Backing, Project>) {
        val pledgeAmount = ksCurrency.format(backingAndProject.first.amount(), backingAndProject.second, RoundingMode.HALF_UP)
        backingAndProject.first.pledgedAt()?.let {
            val pledgeDate = DateTimeUtils.relative(this, ksString, it)
            binding.messagesBackingInfoView.backingAmountTextView.text = Html.fromHtml(
                ksString.format(
                    getString(R.string.pledge_amount_pledged_on_pledge_date), "pledge_amount", pledgeAmount, "pledge_date", pledgeDate
                )
            )
        }
    }

    private fun setDefaultRecyclerViewBottomPadding() =
        binding
            .messagesRecyclerView
            .setPadding(0, 0, 0, resources.getDimension(R.dimen.message_reply_layout_height).toInt())

    private fun setInitialRecyclerViewBottomPadding(bottomPadding: Int) =
        binding
            .messagesRecyclerView
            .setPadding(0, 0, 0, (bottomPadding + resources.getDimension(R.dimen.message_reply_layout_height)).toInt())

    private fun setMessageEditTextHint(name: String) {
        binding.messageReplyLayout.messageEditText.hint = ksString.format(getString(R.string.Message_user_name), "user_name", name)
    }

    private fun startBackingActivity(projectAndBacker: BackingWrapper) {
        val intent = Intent(this, BackingActivity::class.java)
            .putExtra(IntentKey.BACKING, projectAndBacker.backing)
            .putExtra(IntentKey.PROJECT, projectAndBacker.project)
            .putExtra(IntentKey.BACKER, projectAndBacker.user)
            .putExtra(IntentKey.IS_FROM_MESSAGES_ACTIVITY, true)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProjectPageActivity(project: Project) {
        val intent = Intent().getProjectIntent(this)
            .putExtra(IntentKey.PROJECT, project)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
