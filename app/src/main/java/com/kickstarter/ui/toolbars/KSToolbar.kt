package com.kickstarter.ui.toolbars

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Environment
import com.kickstarter.libs.qualifiers.WebEndpoint
import com.kickstarter.libs.utils.Secrets
import rx.Subscription
import rx.subscriptions.CompositeSubscription

open class KSToolbar @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Toolbar(context, attrs, defStyleAttr) {

    private var backgroundPaint: Paint? = null

    @WebEndpoint
    private var webEndpoint: String? = null

    private val subscriptions = CompositeSubscription()

    init {
        init(context)
    }
    private fun init(context: Context) {
        if (!isInEditMode) {
            backgroundPaint = Paint()
            backgroundPaint?.style = Paint.Style.FILL
            backgroundPaint?.color = ContextCompat.getColor(context, R.color.kds_trust_500)
            webEndpoint = environment().webEndpoint()
        }
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInEditMode && webEndpoint != Secrets.WebEndpoint.PRODUCTION) {
            canvas.drawRect(0f, 0f, context.resources.getDimension(R.dimen.grid_2), height.toFloat(), backgroundPaint!!)
        }
    }

    protected fun environment(): Environment {
        return (context.applicationContext as KSApplication).component().environment()
    }

    /**
     * If the toolbar has a textview with id title_text_view, set its title.
     */
    fun setTitle(title: String) {
        findViewById<TextView>(R.id.title_text_view)?.let {
            it.text = title
        }
    }

    @CallSuper
    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<View>(R.id.back_button)?.setOnClickListener {
            backButtonClick()
        }
    }

    @CallSuper
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        subscriptions.clear()
    }

    protected fun addSubscription(subscription: Subscription) {
        subscriptions.add(subscription)
    }

    private fun backButtonClick() {
        if (context is BaseActivity<*>) {
            (context as BaseActivity<*>).back()
        } else {
            (context as AppCompatActivity).onBackPressed()
        }
    }
}
