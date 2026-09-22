package com.incoteam.realsaathi

import android.graphics.Rect
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.incoteam.realsaathi.core.ui.StableMobileUi
import com.incoteam.realsaathi.ui.auth.AuthLayoutTuner
import com.incoteam.realsaathi.ui.home.CallControlsLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class ResponsiveLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun contentFillsDifferentWindowsWithoutShrinkingTouchTargets() {
        val window = mutableStateOf(320 to 480)
        var clicks = 0
        compose.setContent {
            Box(Modifier.requiredSize(window.value.first.dp, window.value.second.dp)) {
                StableMobileUi {
                    Box(Modifier.fillMaxSize().testTag("viewport")) {
                        Button(
                            onClick = { clicks++ },
                            modifier = Modifier.align(Alignment.BottomEnd).size(48.dp).testTag("action")
                        ) { Text("+") }
                    }
                }
            }
        }
        for ((width, height) in listOf(320 to 480, 360 to 640, 393 to 851, 411 to 891, 480 to 960, 720 to 960)) {
            compose.runOnIdle { window.value = width to height }
            compose.onNodeWithTag("viewport")
                .assertWidthIsEqualTo(minOf(width, 600).dp)
                .assertHeightIsEqualTo(height.dp)
            compose.onNodeWithTag("action").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        }
        compose.runOnIdle { window.value = 320 to 480 }
        compose.onNodeWithTag("action").performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test
    fun allSixCallControlsFitNarrowPhonesWithoutOverlap() {
        compose.setContent {
            Box(Modifier.requiredSize(240.dp, 116.dp).testTag("controls")) {
                CallControlsLayout {
                    repeat(6) { index ->
                        Box(Modifier.size(52.dp).testTag("control-$index"))
                    }
                }
            }
        }
        val container = compose.onNodeWithTag("controls").fetchSemanticsNode().boundsInRoot
        val controls = (0..5).map { index ->
            compose.onNodeWithTag("control-$index")
                .assertWidthIsEqualTo(52.dp).assertHeightIsEqualTo(52.dp)
                .fetchSemanticsNode().boundsInRoot
        }
        controls.forEach { bounds ->
            assertTrue("Control outside panel: $bounds", bounds.left >= container.left &&
                bounds.right <= container.right && bounds.top >= container.top && bounds.bottom <= container.bottom)
        }
        controls.forEachIndexed { index, bounds ->
            controls.drop(index + 1).forEach { other ->
                assertTrue("Controls overlap", !bounds.overlaps(other))
            }
        }
    }

    @Test
    fun loginAndOtpFitSmallLargeAndKeyboardWindows() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = ContextThemeWrapper(instrumentation.targetContext, R.style.Theme_RealSaathi)
            val density = context.resources.displayMetrics.density
            fun px(dp: Int) = (dp * density).roundToInt()
            for (layout in listOf(R.layout.activity_login, R.layout.activity_otp_verification)) {
                val root = LayoutInflater.from(context).inflate(layout, null) as ViewGroup
                val isLogin = layout == R.layout.activity_login
                val content = root.findViewById<View>(R.id.contentContainer)
                val bottom = root.findViewById<View>(R.id.bottomContent)
                if (!isLogin) root.findViewById<TextView>(R.id.textOtpPhone).text = "+91 9876543210"
                AuthLayoutTuner.apply(root, content, bottom, 118, 64)
                for ((width, height) in listOf(320 to 480, 360 to 640, 393 to 851, 411 to 891, 600 to 960)) {
                    fun measure() {
                        repeat(3) {
                            root.measure(
                                View.MeasureSpec.makeMeasureSpec(px(width), View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(px(height), View.MeasureSpec.EXACTLY)
                            )
                            root.layout(0, 0, px(width), px(height))
                        }
                    }
                    for (keyboardDp in listOf(0, 240, 0)) {
                        measure()
                        ViewCompat.dispatchApplyWindowInsets(root, WindowInsetsCompat.Builder()
                            .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, px(24), 0, px(24)))
                            .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, px(keyboardDp)))
                            .setVisible(WindowInsetsCompat.Type.ime(), keyboardDp > 0)
                            .build())
                        measure()
                        val action = root.findViewById<View>(if (isLogin) R.id.buttonSendOtp else R.id.buttonVerifyOtp)
                        val bounds = Rect(0, 0, action.width, action.height)
                        root.offsetDescendantRectToMyCoords(action, bounds)
                        val description = "layout=$layout window=${width}x$height keyboard=$keyboardDp bounds=$bounds"
                        assertTrue(description, action.width >= px(48) && action.height >= px(48))
                        assertTrue(description, bounds.left >= 0 && bounds.right <= px(width))
                        assertTrue(description, bounds.top >= px(24))
                        assertTrue(description, bounds.bottom <= px(height - maxOf(24, keyboardDp)))
                        assertTrue(description, root.findViewById<View>(R.id.scrollContent).height > 0)
                        if (!isLogin) {
                            val phone = root.findViewById<View>(R.id.textOtpPhone)
                            val edit = root.findViewById<View>(R.id.imageEditPhone)
                            val row = root.findViewById<View>(R.id.rowOtpDestination)
                            assertTrue(description, phone.left >= 0 && edit.right <= row.width)
                        }
                    }
                }
            }
        }
    }
}
