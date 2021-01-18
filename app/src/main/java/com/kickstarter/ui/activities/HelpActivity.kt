package com.kickstarter.ui.activities

import android.net.Uri
import android.os.Bundle
import androidx.annotation.IntDef
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.qualifiers.WebEndpoint
import com.kickstarter.viewmodels.HelpViewModel
import kotlinx.android.synthetic.main.help_layout.*
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@RequiresActivityViewModel(HelpViewModel::class)
open class HelpActivity : BaseActivity<HelpViewModel?>() {
    @IntDef(HELP_TYPE_TERMS, HELP_TYPE_PRIVACY, HELP_TYPE_HOW_IT_WORKS, HELP_TYPE_COOKIE_POLICY, HELP_TYPE_ACCESSIBILITY)
    @Retention(RetentionPolicy.SOURCE)
    annotation class HelpType

    @HelpType
    private var helpType = 0

    @WebEndpoint
    private var webEndpoint: String? = null
    protected fun helpType(@HelpType helpType: Int) {
        this.helpType = helpType
    }

    class Terms : HelpActivity() {
        init {
            helpType(HELP_TYPE_TERMS)
        }
    }

    class Privacy : HelpActivity() {
        init {
            helpType(HELP_TYPE_PRIVACY)
        }
    }

    class CookiePolicy : HelpActivity() {
        init {
            helpType(HELP_TYPE_COOKIE_POLICY)
        }
    }

    class AccessibilityStatement : HelpActivity() {
        init {
            helpType(HELP_TYPE_ACCESSIBILITY)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_layout)
        webEndpoint = environment().webEndpoint()
        val url = getUrlForHelpType(helpType)
        kickstarter_web_view.loadUrl(url)
    }

    protected fun getUrlForHelpType(@HelpType helpType: Int): String {
        val builder = Uri.parse(webEndpoint).buildUpon()
        when (helpType) {
            HELP_TYPE_TERMS -> builder.appendEncodedPath(TERMS_OF_USE)
            HELP_TYPE_PRIVACY -> builder.appendEncodedPath(PRIVACY)
            HELP_TYPE_HOW_IT_WORKS -> builder.appendEncodedPath(HELLO)
            HELP_TYPE_COOKIE_POLICY -> builder.appendEncodedPath(COOKIES)
            HELP_TYPE_ACCESSIBILITY -> builder.appendEncodedPath(ACCESSIBILITY)
        }
        return builder.toString()
    }

    companion object {
        const val HELP_TYPE_TERMS = 0
        const val HELP_TYPE_PRIVACY = 1
        const val HELP_TYPE_HOW_IT_WORKS = 2
        const val HELP_TYPE_COOKIE_POLICY = 3
        const val HELP_TYPE_ACCESSIBILITY = 4
        const val TERMS_OF_USE = "terms-of-use"
        const val PRIVACY = "privacy"
        const val HELLO = "hello"
        const val COOKIES = "cookies"
        const val ACCESSIBILITY = "accessibility"
    }
}