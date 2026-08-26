package com.incoteam.frndzz.ui.auth

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding

internal object AuthLayoutTuner {
    private const val MaxContentWidthDp = 390
    private const val BaseHorizontalPaddingDp = 20

    fun apply(
        contentContainer: View,
        bottomContent: View,
        regularTopPaddingDp: Int,
        compactTopPaddingDp: Int
    ) {
        val metrics = Resources.getSystem().displayMetrics
        val density = metrics.density
        val screenWidthDp = metrics.widthPixels / density
        val screenHeightDp = metrics.heightPixels / density
        val sidePaddingDp = if (screenWidthDp > MaxContentWidthDp) {
            ((screenWidthDp - MaxContentWidthDp) / 2f) + BaseHorizontalPaddingDp
        } else {
            BaseHorizontalPaddingDp.toFloat()
        }
        val sidePaddingPx = (sidePaddingDp * density).toInt()
        val topPaddingDp = when {
            screenHeightDp < 700f -> compactTopPaddingDp
            screenHeightDp < 780f -> ((regularTopPaddingDp + compactTopPaddingDp) / 2f).toInt()
            else -> regularTopPaddingDp
        }

        contentContainer.updatePadding(
            left = sidePaddingPx,
            top = (topPaddingDp * density).toInt(),
            right = sidePaddingPx
        )
        bottomContent.updatePadding(
            left = sidePaddingPx,
            right = sidePaddingPx
        )

        contentContainer.minimumWidth = 0
        bottomContent.minimumWidth = 0
        (contentContainer.layoutParams as? ViewGroup.LayoutParams)?.width = ViewGroup.LayoutParams.MATCH_PARENT
        (bottomContent.layoutParams as? ViewGroup.LayoutParams)?.width = ViewGroup.LayoutParams.MATCH_PARENT
    }
}
