package com.kickstarter.ui.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import com.kickstarter.R
import com.kickstarter.databinding.ActivityNewsletterBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.viewmodels.NewsletterViewModel

class NewsletterActivity : BaseActivity<NewsletterViewModel.ViewModel>() {

    private lateinit var binding: ActivityNewsletterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_newsletter)
    }
}
