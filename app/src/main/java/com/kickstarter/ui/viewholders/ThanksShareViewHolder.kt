package com.kickstarter.ui.viewholders

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.util.Pair
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.kickstarter.R
import com.kickstarter.databinding.ThanksShareViewBinding
import com.kickstarter.libs.TweetComposer
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Project
import com.kickstarter.viewmodels.ThanksShareHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ThanksShareViewHolder(private val binding: ThanksShareViewBinding) : KSViewHolder(binding.root) {
    private val viewModel = ThanksShareHolderViewModel.ThanksShareViewHolderViewModel()
    private val ksString = requireNotNull(environment().ksString())
    private val shareDialog: ShareDialog = ShareDialog(context() as Activity)
    private var disposables = CompositeDisposable()

    init {
        viewModel.outputs.projectName()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showBackedProject(it) }
            .addToDisposable(disposables)

        viewModel.outputs.startShare()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startShare(it) }
            .addToDisposable(disposables)

        viewModel.outputs.startShareOnFacebook()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startShareOnFacebook(it) }
            .addToDisposable(disposables)

        viewModel.outputs.startShareOnTwitter()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startShareOnTwitter(it) }
            .addToDisposable(disposables)

        binding.shareButton.setOnClickListener {
            shareButtonClicked()
        }
        binding.thanksFacebookShareButton.setOnClickListener {
            shareOnFacebookButtonClicked()
        }
        binding.thanksTwitterShareButton.setOnClickListener {
            shareOnTwitterButtonClicked()
        }
    }

    private fun shareButtonClicked() {
        viewModel.inputs.shareClick()
    }

    private fun shareOnFacebookButtonClicked() {
        viewModel.inputs.shareOnFacebookClick()
    }

    private fun shareOnTwitterButtonClicked() {
        viewModel.inputs.shareOnTwitterClick()
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val project = requireNotNull(data as Project?)
        viewModel.inputs.configureWith(project)
    }

    private fun shareString(projectName: String): String {
        return ksString.format(context().getString(R.string.project_checkout_share_twitter_I_just_backed_project_on_kickstarter), "project_name", projectName)
    }

    private fun showBackedProject(projectName: String) {
        binding.backedProject.text = Html.fromHtml(ksString.format(context().getString(R.string.You_have_successfully_backed_project_html), "project_name", projectName))
    }

    private fun startShare(projectNameAndShareUrl: Pair<String, String>) {
        val projectName = projectNameAndShareUrl.first
        val shareUrl = projectNameAndShareUrl.second
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            .putExtra(Intent.EXTRA_TEXT, String.format("%s %s", shareString(projectName), shareUrl))
        context().startActivity(Intent.createChooser(intent, context().getString(R.string.project_accessibility_button_share_label)))
    }

    private fun startShareOnFacebook(projectAndShareUrl: Pair<Project, String>) {
        if (!ShareDialog.canShow(ShareLinkContent::class.java)) {
            return
        }
        val shareUrl = projectAndShareUrl.second

        val content = ShareLinkContent.Builder()
            .setContentUrl(Uri.parse(shareUrl))
            .build()

        shareDialog.show(content)
    }

    private fun startShareOnTwitter(projectNameAndShareUrl: Pair<String, String>) {
        val projectName = projectNameAndShareUrl.first
        val shareUrl = projectNameAndShareUrl.second
        TweetComposer.Builder(context())
            .text(shareString(projectName))
            .uri(Uri.parse(shareUrl))
            .show()
    }

    override fun destroy() {
        disposables.clear()
        super.destroy()
    }
}
