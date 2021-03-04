package com.kickstarter.ui.viewholders

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.util.Pair
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.ShareOpenGraphAction
import com.facebook.share.model.ShareOpenGraphContent
import com.facebook.share.model.ShareOpenGraphObject
import com.facebook.share.widget.ShareDialog
import com.kickstarter.R
import com.kickstarter.databinding.ThanksShareViewBinding
import com.kickstarter.libs.TweetComposer
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.viewmodels.ThanksShareHolderViewModel

class ThanksShareViewHolder(private val binding: ThanksShareViewBinding) : KSViewHolder(binding.root) {
    private val viewModel = ThanksShareHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()
    private val shareDialog: ShareDialog

    init {
        shareDialog = ShareDialog(context() as Activity)
        viewModel.outputs.projectName()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { showBackedProject(it) }
        viewModel.outputs.startShare()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startShare(it) }
        viewModel.outputs.startShareOnFacebook()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startShareOnFacebook(it) }
        viewModel.outputs.startShareOnTwitter()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startShareOnTwitter(it) }

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

    fun shareButtonClicked() {
        viewModel.inputs.shareClick()
    }

    fun shareOnFacebookButtonClicked() {
        viewModel.inputs.shareOnFacebookClick()
    }

    fun shareOnTwitterButtonClicked() {
        viewModel.inputs.shareOnTwitterClick()
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val project = ObjectUtils.requireNonNull(data as Project?)
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
        val project = projectAndShareUrl.first
        val shareUrl = projectAndShareUrl.second
        val photo = project.photo()
        val shareOpenGraphObject = ShareOpenGraphObject.Builder()
            .putString("og:type", "kickstarter:project")
            .putString("og:title", project.name())
            .putString("og:description", project.blurb())
            .putString("og:image", photo?.small())
            .putString("og:url", shareUrl)
            .build()
        val action = ShareOpenGraphAction.Builder()
            .setActionType("kickstarter:back")
            .putObject("project", shareOpenGraphObject)
            .build()
        val content = ShareOpenGraphContent.Builder()
            .setPreviewPropertyName("project")
            .setAction(action)
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
}
