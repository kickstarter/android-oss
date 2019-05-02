package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.viewmodels.KSR10FragmentViewModel
import kotlinx.android.synthetic.main.fragment_ksr_10.*
import java.util.*

@RequiresFragmentViewModel(KSR10FragmentViewModel.ViewModel::class)
class KSR10Fragment : BaseFragment<KSR10FragmentViewModel.ViewModel>() {

    private val random = Random()
    private val durationBound = 500

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_ksr_10, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewTreeObserver = ksr10_root.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    this@KSR10Fragment.viewModel.inputs.onGlobalLayout()
                    ksr10_root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        this.viewModel.outputs.startAnimations()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { animateShapes() }

        this.viewModel.outputs.dismiss()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { dismiss() }

        show_me_projects.setOnClickListener {
            this.viewModel.inputs.closeClicked()
        }
    }

    private fun dismiss() {
        this.fragmentManager?.popBackStack()
    }

    private fun animateShapes() {
        startAnimations(R.anim.bounce_down, orange_disc, left_blue_block, right_blue_block)
        startAnimations(R.anim.bounce_up, yellow_disc, green_dice)
    }

    private fun startAnimations(animRes: Int, vararg imageViews: ImageView) {
        for (imageView in imageViews) {
            this.context?.let {
                val animation = AnimationUtils.loadAnimation(it, animRes)
                val randomDuration = this.random.nextInt(this.durationBound) + animation.duration
                animation.duration = randomDuration
                imageView.startAnimation(animation)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): KSR10Fragment {
            return KSR10Fragment()
        }
    }
}
