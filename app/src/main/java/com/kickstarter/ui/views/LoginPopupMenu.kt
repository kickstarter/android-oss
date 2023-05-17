package com.kickstarter.ui.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.R
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.ui.activities.HelpActivity
import com.kickstarter.ui.activities.HelpActivity.Terms

class LoginPopupMenu(context: Context, anchor: View) : PopupMenu(context, anchor) {

    init {
        menuInflater.inflate(R.menu.login_help_menu, menu)

        val activity = context as? AppCompatActivity

        setOnMenuItemClickListener { item: MenuItem ->
            val intent: Intent
            when (item.itemId) {
                R.id.terms -> {
                    intent = Intent(context, Terms::class.java)
                    activity?.startActivity(intent)
                }
                R.id.privacy_policy -> {
                    intent = Intent(context, HelpActivity.Privacy::class.java)
                    activity?.startActivity(intent)
                }
                R.id.cookie_policy -> {
                    intent = Intent(context, HelpActivity.CookiePolicy::class.java)
                    activity?.startActivity(intent)
                }
                R.id.help -> {
                    intent = Intent(Intent.ACTION_VIEW, Uri.parse(Secrets.HelpCenter.ENDPOINT))
                    activity?.startActivity(intent)
                }
            }
            true
        }
    }
}
