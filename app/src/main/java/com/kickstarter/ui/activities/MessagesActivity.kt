package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Pair
import android.view.View
import android.view.WindowManager
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.MessagesLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Backing
import com.kickstarter.models.BackingWrapper
import com.kickstarter.models.Message
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.MessagesAdapter
import com.kickstarter.viewmodels.MessagesViewModel
import java.math.RoundingMode

@RequiresActivityViewModel(MessagesViewModel.ViewModel::class)
class MessagesActivity : BaseActivity<MessagesViewModel.ViewModel>() {
    private lateinit var ksCurrency: KSCurrency
    private lateinit var ksString: KSString
    private lateinit var adapter: MessagesAdapter

    private lateinit var binding: MessagesLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MessagesLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ksCurrency = environment().ksCurrency()
        ksString = environment().ksString()
        adapter = MessagesAdapter()

        binding.messagesRecyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.messagesRecyclerView.layoutManager = layoutManager

        binding.messagesBackingInfoView.messagesViewPledgeButton.text = getString(R.string.project_view_button)

        setAppBarOffsetChangedListener(binding.messagesAppBarLayout)

        RxView.focusChanges(binding.messageReplyLayout.messageEditText)
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { viewModel.inputs.messageEditTextIsFocused(it) }

        viewModel.outputs.backButtonIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.messagesToolbar.messagesToolbarBackButton))

        viewModel.outputs.backingAndProject()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setBackingInfoView(it) }

        viewModel.outputs.backingInfoViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { ViewUtils.setGone(binding.messagesBackingInfoView.backingInfoView, it) }

        viewModel.outputs.closeButtonIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.messagesToolbar.messagesToolbarCloseButton))

        viewModel.outputs.creatorNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.messagesCreatorNameTextView.text = ksString.format(getString(R.string.project_creator_by_creator), "creator_name", it) }

        viewModel.outputs.goBack()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { back() }

        viewModel.outputs.loadingIndicatorViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.messagesLoadingIndicator))

        viewModel.outputs.messageEditTextHint()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setMessageEditTextHint(it) }

        viewModel.outputs.messageEditTextShouldRequestFocus()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { requestFocusAndOpenKeyboard() }

        viewModel.outputs.messageList()
            .compose(bindToLifecycle())
            .compose<List<Message?>>(Transformers.observeForUI())
            .subscribe { adapter.messages(it) }

        viewModel.outputs.projectNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.messagesProjectNameTextView.text = it }

        viewModel.outputs.projectNameToolbarTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.messagesToolbar.messagesProjectNameCollapsedTextView.text = it }

        viewModel.outputs.recyclerViewDefaultBottomPadding()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setDefaultRecyclerViewBottomPadding() }

        viewModel.outputs.recyclerViewInitialBottomPadding()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setInitialRecyclerViewBottomPadding(it) }

        viewModel.outputs.scrollRecyclerViewToBottom()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.messagesRecyclerView.scrollToPosition(adapter.itemCount - 1) }

        viewModel.outputs.setMessageEditText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.messageReplyLayout.messageEditText.setText(it) }

        viewModel.outputs.sendMessageButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.messageReplyLayout.sendMessageButton.isEnabled = it }

        viewModel.outputs.showMessageErrorToast()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { ViewUtils.showToast(this, it) }

        viewModel.outputs.startBackingActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startBackingActivity(it) }

        viewModel.outputs.toolbarIsExpanded()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.messagesAppBarLayout.setExpanded(it) }

        viewModel.outputs.viewPledgeButtonIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.messagesBackingInfoView.messagesViewPledgeButton))

        binding.messagesBackingInfoView.messagesViewPledgeButton.setOnClickListener {
            viewPledgeButtonClicked()
        }

        binding.messageReplyLayout.sendMessageButton.setOnClickListener {
            sendMessageButtonClicked()
        }

        binding.messageReplyLayout.sendMessageButton.setOnClickListener {
            sendMessageButtonClicked()
        }

        binding.messagesToolbar.messagesToolbarBackButton.setOnClickListener {
            backOrCloseButtonClicked()
        }

        binding.messagesToolbar.messagesToolbarCloseButton.setOnClickListener {
            backOrCloseButtonClicked()
        }

        binding.messageReplyLayout.messageEditText.doOnTextChanged { message, _, _, _ ->
            message?.let { onMessageEditTextChanged(it) }
        }
    }

    private fun backOrCloseButtonClicked() =
        viewModel.inputs.backOrCloseButtonClicked()

    private fun sendMessageButtonClicked() =
        viewModel.inputs.sendMessageButtonClicked()

    private fun viewPledgeButtonClicked() =
        viewModel.inputs.viewPledgeButtonClicked()

    private fun onMessageEditTextChanged(message: CharSequence) =
        viewModel.inputs.messageEditTextChanged(message.toString())

    override fun exitTransition() = if (binding.messagesToolbar.messagesToolbarBackButton.visibility == View.VISIBLE)
        TransitionUtils.slideInFromLeft() else null

    override fun onDestroy() {
        super.onDestroy()
        binding.messagesRecyclerView.adapter = null
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
}
