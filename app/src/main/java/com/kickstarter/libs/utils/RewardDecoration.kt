package com.kickstarter.libs.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RewardDecoration(private val margin: Int, private val colorActive: Int, private val colorInactive: Int,
                       private val radius: Float, private val padding: Float,
                       val interpolator: Interpolator = AccelerateDecelerateInterpolator()) : RecyclerView.ItemDecoration() {

    private val height = margin * 2 + radius * 2
    private val paint = Paint()

    init {
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        with(outRect) {
            val position = parent.getChildAdapterPosition(view)
            val itemCount = state.itemCount
            left = when (position) {
                0 -> margin
                else -> margin / 2
            }
            right = when (position) {
                itemCount - 1 -> margin
                else -> margin / 2
            }
            top = margin
            bottom = (height * 3/2).toInt()
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        paint.color = colorInactive

        val dividerY = parent.height - height
        c.drawLine(0f, dividerY, parent.width.toFloat(), dividerY, paint)

        val itemCount = state.itemCount

        if (itemCount == 0) {
            return
        }

        val widthOfCircles = radius * 2 * itemCount
        val paddingBetweenCircles = (itemCount - 1) * padding
        val indicatorWidth = widthOfCircles + paddingBetweenCircles
        val remainingWidth = parent.width - indicatorWidth

        if (remainingWidth < 0) {
            return
        }

        val indicatorX = remainingWidth / 2
        val indicatorY = parent.height - height / 2f

        drawPositions(c, indicatorX, indicatorY, itemCount)

        val layoutManager = parent.layoutManager as LinearLayoutManager
        val currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (currentPosition == RecyclerView.NO_POSITION) {
            return
        }

        drawCurrentPosition(c, indicatorX, indicatorY, currentPosition)
    }

    private fun drawPositions(c: Canvas, indicatorX: Float, indicatorY: Float, itemCount: Int) {
        var x = indicatorX
        (0 until itemCount).forEach { _ ->
            drawCircleFromLeftEdge(c, x, indicatorY)

            x += getCircleWidth() + padding
        }
    }

    private fun drawCurrentPosition(c: Canvas, indicatorX: Float, indicatorY: Float, position: Int) {
        paint.color = colorActive

        val x = indicatorX + (getCircleWidth() + padding) * position

        drawCircleFromLeftEdge(c, x, indicatorY)
    }

    private fun drawCircleFromLeftEdge(c: Canvas, x: Float, indicatorY: Float) {
        c.drawCircle(x + radius, indicatorY, radius, paint)
    }

    private fun getCircleWidth() = radius * 2
}
