package com.incoteam.realsaathi.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.constrainHeight

/** A single-line call controls panel using stable layout APIs across the supported Compose runtimes. */
@Composable
internal fun CallControlsLayout(content: @Composable () -> Unit) {
    Layout(content = content, modifier = Modifier.fillMaxWidth()) { measurables, constraints ->
        val columns = measurables.size.coerceAtLeast(1)
        val children = measurables.map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0, maxWidth = constraints.maxWidth / columns))
        }
        val height = children.maxOfOrNull { it.height } ?: 0
        val layoutHeight = constraints.constrainHeight(height)
        layout(constraints.maxWidth, layoutHeight) {
            val spacing = (constraints.maxWidth - children.sumOf { it.width }) / (children.size + 1)
            var x = spacing
            children.forEach { child ->
                child.placeRelative(x, (layoutHeight - child.height) / 2)
                x += child.width + spacing
            }
        }
    }
}
