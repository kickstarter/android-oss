package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Pair
import android.view.View
import androidx.annotation.RequiresApi
import com.kickstarter.R
import com.kickstarter.databinding.PlaygroundLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.htmlparser.HTMLParser
import com.kickstarter.libs.htmlparser.TextViewElement
import com.kickstarter.libs.htmlparser.getStyledComponents
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
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

        // - Allow clickable spans
        binding.text.linksClickable = true
        binding.text.isClickable = true
        binding.text.movementMethod = LinkMovementMethod.getInstance()

        val headerSize = resources.getDimensionPixelSize(R.dimen.title_3)
        val body = resources.getDimensionPixelSize(R.dimen.callout)

        val url = "https://www.meneame.net/"
        val html = "<ul>\n" +
            "   <li>This</li>\n" +
            "   <li>This and more text <a href=\\\"http://record.pt\\\" target=\\\"_blank\\\" rel=\\\"noopener\\\"><em><strong>lalalalalala</strong></em></a></li>\n" +
            "   <li>More text here <em><strong>lalalalalala</strong></em> and here </li>\n" +
            "   <li><strong>is</strong></li>\n" +
            "   <li>is <strong>is</strong> is</li>\n" +
            "   <li><em><strong>is</strong></em> with some <strong>text</strong> in the middle <em><strong>is</strong></em></li>\n" +
            "   <li><em>a</em></li>\n" +
            "   <li><a href=\\\"http://record.pt\\\" target=\\\"_blank\\\" rel=\\\"noopener\\\">link</a></li>\n" +
            "   <li>text with <a href=\\\"http://record.pt\\\" target=\\\"_blank\\\" rel=\\\"noopener\\\">link</a></li>\n" +
            "   <li>Hola <strong><em>que tal </em></strong> majete</li>\n" +
            "   <li><a href=$url target=\\\"_blank\\\" rel=\\\"noopener\\\"><em><strong>Meneane </strong></em></a><a href=$url target=\\\"_blank\\\" rel=\\\"noopener\\\">Another URL in this list</a> and some text</li>"
        "</ul>"
        val listOfElements = HTMLParser().parse(html)
        val element: TextViewElement = listOfElements.first() as TextViewElement

        binding.text.text = element.getStyledComponents(body, headerSize, this)

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
