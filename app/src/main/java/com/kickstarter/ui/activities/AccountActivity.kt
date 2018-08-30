package com.kickstarter.ui.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.kickstarter.R
import com.kickstarter.extensions.startActivityWithSlideUpTransition
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        chane_email_row.setOnClickListener { startActivityWithSlideUpTransition(Intent(this,
                ChangeEmailActivity::class.java)) }
    }
}
