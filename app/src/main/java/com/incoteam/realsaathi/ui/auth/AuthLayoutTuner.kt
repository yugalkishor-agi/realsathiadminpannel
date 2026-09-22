package com.incoteam.realsaathi.ui.auth

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import kotlin.math.max
import kotlin.math.roundToInt

internal object AuthLayoutTuner {
    private const val MaxContentWidthDp = 430f
    private const val BaseHorizontalPaddingDp = 20f

    fun apply(
        root: View,
        contentContainer: View,
        bottomContent: View,
        regularTopPaddingDp: Int,
        compactTopPaddingDp: Int
    ) {
        var lastInsets = WindowInsetsCompat.Builder().build()
        val density = root.resources.displayMetrics.density

        fun updateLayout() {
            if (root.width == 0 || root.height == 0) return
            val bars = lastInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val keyboard = lastInsets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomInset = max(bars.bottom, keyboard.bottom)
            val availableWidthDp = (root.width - bars.left - bars.right) / density
            val availableHeightDp = (root.height - bars.top - bottomInset) / density
            val sidePadding = (max(0f, (availableWidthDp - MaxContentWidthDp) / 2f) +
                BaseHorizontalPaddingDp) * density
            val topPaddingDp = when {
                keyboard.bottom > bars.bottom || availableHeightDp < 480f -> 20
                availableHeightDp < 700f -> compactTopPaddingDp
                availableHeightDp < 780f -> (regularTopPaddingDp + compactTopPaddingDp) / 2
                else -> regularTopPaddingDp
            }

            root.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bottomInset)
            contentContainer.updatePadding(
                left = sidePadding.roundToInt(),
                top = (topPaddingDp * density).roundToInt(),
                right = sidePadding.roundToInt()
            )
            bottomContent.updatePadding(left = sidePadding.roundToInt(), right = sidePadding.roundToInt())
        }

        root.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) {
                updateLayout()
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            lastInsets = insets
            updateLayout()
            // The root owns system/keyboard padding, including its Compose offline overlay.
            WindowInsetsCompat.CONSUMED
        }
        updateLayout()
        root.post { ViewCompat.requestApplyInsets(root) }
    }
}