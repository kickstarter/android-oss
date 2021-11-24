package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Pair
import android.view.View
import androidx.annotation.RequiresApi
import com.kickstarter.R
import com.kickstarter.databinding.PlaygroundLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.boldStyle
import com.kickstarter.libs.utils.extensions.bulletStyle
import com.kickstarter.libs.utils.extensions.color
import com.kickstarter.libs.utils.extensions.italicStyle
import com.kickstarter.libs.utils.extensions.linkStyle
import com.kickstarter.libs.utils.extensions.size
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.PlaygroundViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(PlaygroundViewModel.ViewModel::class)
class PlaygroundActivity : BaseActivity<PlaygroundViewModel.ViewModel?>() {
    private lateinit var binding: PlaygroundLayoutBinding
    private lateinit var view: View

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlaygroundLayoutBinding.inflate(layoutInflater)
        view = binding.root
        setContentView(view)

        val context = this

        // - Allow clickable spans
        binding.text.linksClickable = true
        binding.text.isClickable = true
        binding.text.movementMethod = LinkMovementMethod.getInstance()

        val headerSize = resources.getDimensionPixelSize(R.dimen.title_3)
        val body = resources.getDimensionPixelSize(R.dimen.callout)
        val styledString = SpannableString("Meneame")

        styledString.size(body)
        // styledString.size(headerSize)
        styledString.color()
        styledString.boldStyle()
        styledString.italicStyle()
        styledString.linkStyle { ApplicationUtils.openUrlExternally(context, "https://www.meneame.net/") }
        styledString.bulletStyle()

        binding.text.text = styledString

        setStepper()
        setProjectActivityButtonClicks()
    }

    /**
     * Set up the stepper example
     */
    private fun setStepper() {
        binding.stepper.inputs.setMinimum(1)
        binding.stepper.inputs.setMaximum(9)
        binding.stepper.inputs.setInitialValue(5)
        binding.stepper.inputs.setVariance(1)

        binding.stepper.outputs.display()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showSnackbar(binding.stepper, "The updated value on the display is: $it")
            }
    }

    private fun setProjectActivityButtonClicks() {
        binding.newProjectActivity.setOnClickListener { startProjectActivity(Pair(ProjectFactory.project(), RefTag.searchFeatured())) }
    }

    private fun startProjectActivity(projectAndRefTag: Pair<Project, RefTag>) {
        val intent = Intent(this, ProjectPageActivity::class.java)
            .putExtra(IntentKey.PROJECT, projectAndRefTag.first)
            .putExtra(IntentKey.REF_TAG, projectAndRefTag.second)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
