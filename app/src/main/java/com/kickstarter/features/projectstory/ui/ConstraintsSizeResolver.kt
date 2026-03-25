package com.kickstarter.features.projectstory.ui

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import coil.size.Dimension
import coil.size.Size
import coil.size.SizeResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

/*
 * These `SizeResolver`s and utility functions are lifted directly from the same version of the
 * Coil library that is used in this project (2.7.0). In this version they are marked `internal`,
 * but they are needed to match the behavior of `AsyncImage` when using `rememberAsyncImagePainter()`.
 *
 * This file should be removed upon upgrading to Coil 3.+.
 *
 * See:
 * - https://github.com/coil-kt/coil/blob/2.7.0/coil-compose-base/src/main/java/coil/compose/ConstraintsSizeResolver.kt
 * - https://github.com/coil-kt/coil/blob/2.7.0/coil-compose-base/src/main/java/coil/compose/utils.kt
 */

private val ZeroConstraints = Constraints.fixed(0, 0)

@Stable
private fun Constraints.toSizeOrNull(): Size? {
    if (isZero) {
        return null
    } else {
        val width = if (hasBoundedWidth) Dimension(maxWidth) else Dimension.Undefined
        val height = if (hasBoundedHeight) Dimension(maxHeight) else Dimension.Undefined
        return Size(width, height)
    }
}

/**
 * A [SizeResolver] that computes the size from the constraints passed during the layout phase.
 */
class ConstraintsSizeResolver : SizeResolver, LayoutModifier {

    private val currentConstraints = MutableStateFlow(ZeroConstraints)

    override suspend fun size(): Size {
        return currentConstraints
            .mapNotNull(Constraints::toSizeOrNull)
            .first()
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        // Cache the current constraints.
        currentConstraints.value = constraints

        // Measure and layout the content.
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }

    fun setConstraints(constraints: Constraints) {
        currentConstraints.value = constraints
    }
}

/* In Coil 2.7.0, this is the default SizeResolver when using `rememberAsyncImagePainter()`,
 * a surprising difference from `AsyncImage`, which uses a `ConstraintsSizeResolver` by default when
 * when `ContentScale` is anything but `None`. Moreover, there is a bug in 2.7.0 that prevents the
 * default resolver from being activated in certain conditions, so it's more reliable to pass in an
 * instance regardless. */
class DisplaySizeResolver(private val context: Context) : SizeResolver {

    override suspend fun size(): Size {
        val metrics = context.resources.displayMetrics
        val maxDimension = Dimension(maxOf(metrics.widthPixels, metrics.heightPixels))
        return Size(maxDimension, maxDimension)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is DisplaySizeResolver && context == other.context
    }

    override fun hashCode() = context.hashCode()
}
