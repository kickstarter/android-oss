package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.RecyclerViewPaginator
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ProfileAdapter
import com.kickstarter.viewmodels.ProfileViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_layout.*
import kotlinx.android.synthetic.main.profile_toolbar.*

@RequiresActivityViewModel(ProfileViewModel.ViewModel::class)
class ProfileActivity : BaseActivity<ProfileViewModel.ViewModel>() {
    private lateinit var adapter: ProfileAdapter
    private lateinit var paginator: RecyclerViewPaginator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_layout)

        this.adapter = ProfileAdapter(this.viewModel)
        val spanCount = if (ViewUtils.isLandscape(this)) 3 else 2
        recycler_view.layoutManager = GridLayoutManager(this, spanCount)
        recycler_view.adapter = this.adapter

        this.paginator = RecyclerViewPaginator(recycler_view, { this.viewModel.inputs.nextPage() },
                this.viewModel.outputs.isFetchingProjects())

        this.viewModel.outputs.avatarImageViewUrl()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { url -> Picasso.with(this).load(url).transform(CircleTransformation()).into(avatar_image_view) }

        this.viewModel.outputs.backedCountTextViewHidden()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(backed_count_text_view))

        this.viewModel.outputs.backedCountTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe( { backed_count_text_view.text = it })

        this.viewModel.outputs.backedTextViewHidden()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(backed_text_view))

        this.viewModel.outputs.createdCountTextViewHidden()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(created_count_text_view))

        this.viewModel.outputs.createdCountTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe({ created_count_text_view.text = it })

        this.viewModel.outputs.createdTextViewHidden()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(created_text_view))

        this.viewModel.outputs.dividerViewHidden()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe(ViewUtils.setGone(divider_view))

        this.viewModel.outputs.projectList()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe({ this.loadProjects(it) })

        this.viewModel.outputs.resumeDiscoveryActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {resumeDiscoveryActivity() }

        this.viewModel.outputs.startMessageThreadsActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.startMessageThreadsActivity() }

        this.viewModel.outputs.startProjectActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe({ this.startProjectActivity(it) })

        this.viewModel.outputs.userNameTextViewText()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe({ user_name_text_view.text = it })

        messages_button.setOnClickListener { this.viewModel.inputs.messagesButtonClicked() }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.paginator.stop()
        recycler_view.adapter = null
    }

    private fun loadProjects(projects: List<Project>) {
        if (projects.isEmpty()) {
            recycler_view.layoutManager = LinearLayoutManager(this)
            recycler_view.setPadding(
                    0, recycler_view.paddingTop, recycler_view.paddingRight, recycler_view.paddingBottom
            )

            if (ViewUtils.isPortrait(this)) {
                disableNestedScrolling()
            }
        }

        this.adapter.takeProjects(projects)
    }

    private fun disableNestedScrolling() {
        recycler_view.isNestedScrollingEnabled = false
    }

    private fun resumeDiscoveryActivity() {
        ApplicationUtils.resumeDiscoveryActivity(this)
    }

    private fun startMessageThreadsActivity() {
        val intent = Intent(this, MessageThreadsActivity::class.java)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProjectActivity(project: Project) {
        val intent = Intent(this, ProjectActivity::class.java)
                .putExtra(IntentKey.PROJECT, project)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
