package com.kickstarter.ui.activities

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.IntDef
import com.kickstarter.databinding.HelpLayoutBinding
import com.kickstarter.libs.Environment
import com.kickstarter.libs.qualifiers.WebEndpoint
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.utils.WindowInsetsUtil
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

open class HelpActivity : ComponentActivity() {
    @IntDef(
        HELP_TYPE_TERMS,
        HELP_TYPE_PRIVACY,
        HELP_TYPE_HOW_IT_WORKS,
        HELP_TYPE_COOKIE_POLICY,
        HELP_TYPE_ACCESSIBILITY
    )
    @Retention(RetentionPolicy.SOURCE)
    annotation class HelpType

    @HelpType
    private var helpType = 0

    @WebEndpoint
    private var webEndpoint: String? = null

    private var environment: Environment? = null

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

    private lateinit var binding: HelpLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        environment = this.getEnvironment()
        webEndpoint = environment?.webEndpoint()
        binding = HelpLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root,
        )
        setContentView(binding.root)

        val url = getUrlForHelpType(helpType)

        url?.let { binding.kickstarterWebView.loadUrl(it) }
    }

    private fun getUrlForHelpType(@HelpType helpType: Int): String? {
        webEndpoint?.let {
            val builder = Uri.parse(it).buildUpon()
            when (helpType) {
                HELP_TYPE_TERMS -> builder.appendEncodedPath(TERMS_OF_USE)
                HELP_TYPE_PRIVACY -> builder.appendEncodedPath(PRIVACY)
                HELP_TYPE_HOW_IT_WORKS -> builder.appendEncodedPath(HELLO)
                HELP_TYPE_COOKIE_POLICY -> builder.appendEncodedPath(COOKIES)
                HELP_TYPE_ACCESSIBILITY -> builder.appendEncodedPath(ACCESSIBILITY)
            }
            return builder.toString()
        } ?: run {
            return null
        }
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
