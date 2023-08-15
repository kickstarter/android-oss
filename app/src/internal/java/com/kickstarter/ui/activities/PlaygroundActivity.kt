package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import io.reactivex.android.schedulers.AndroidSchedulers

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

        val html2 = "<h1>This is heading 1</h1>\n" +
            "<h2>This is heading 2</h2>\n" +
            "<h3>This is heading 3</h3>\n" +
            "<h4>This is heading 4</h4>\n" +
            "<h5>This is heading 5</h5>\n" +
            "<h6>This is heading 6</h6>"

        val listOfElements = HTMLParser().parse(html2)


        // - The parser detects 6 elements and applies the style to each one
        binding.h1.text = (listOfElements[0] as TextViewElement).getStyledComponents(this)
        binding.h2.text = (listOfElements[1] as TextViewElement).getStyledComponents(this)
        binding.h3.text = (listOfElements[2] as TextViewElement).getStyledComponents(this)
        binding.h4.text = (listOfElements[3] as TextViewElement).getStyledComponents(this)
        binding.h5.text = (listOfElements[4] as TextViewElement).getStyledComponents(this)
        binding.h6.text = (listOfElements[5] as TextViewElement).getStyledComponents(this)

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
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showSnackbar(binding.stepper, "The updated value on the display is: $it")
            }.dispose()
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
