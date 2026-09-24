package com.incoteam.realsaathi

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.incoteam.realsaathi.core.network.isInternetAvailable
import com.incoteam.realsaathi.core.network.registerInternetAvailabilityCallback
import com.incoteam.realsaathi.core.network.unregisterInternetAvailabilityCallback
import com.incoteam.realsaathi.core.ui.StableMobileUi
import com.incoteam.realsaathi.core.ui.withStableFontScale
import com.incoteam.realsaathi.ui.auth.login.LoginActivity
import com.incoteam.realsaathi.ui.home.HomeActivity
import com.incoteam.realsaathi.ui.home.NoInternetBlockingScreen
import com.incoteam.realsaathi.ui.theme.RealSaathiTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var hasInternetConnection by mutableStateOf(false)
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withStableFontScale())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hasInternetConnection = isInternetAvailable()
        networkCallback = registerInternetAvailabilityCallback { available ->
            runOnUiThread {
                hasInternetConnection = available
            }
        }
        val sessionManager = (application as RealSaathiApp).sessionManager
        val routeToHome = sessionManager.hasActiveSession()

        setContent {
            RealSaathiTheme(darkTheme = true, dynamicColor = false) {
                DisposableEffect(hasInternetConnection) {
                    val controller = WindowCompat.getInsetsController(window, window.decorView)
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    if (hasInternetConnection) {
                        controller.hide(WindowInsetsCompat.Type.systemBars())
                    } else {
                        controller.show(WindowInsetsCompat.Type.systemBars())
                    }
                    onDispose { controller.show(WindowInsetsCompat.Type.systemBars()) }
                }

                if (hasInternetConnection) {
                    val destination = remember(routeToHome) {
                        if (routeToHome) HomeActivity::class.java
                        else LoginActivity::class.java
                    }

                    LaunchedEffect(destination, routeToHome, hasInternetConnection) {
                        if (!hasInternetConnection) return@LaunchedEffect
                        delay(2800L)
                        navigateTo(destination)
                    }

                    RealSaathiAnimatedSplash()
                } else {
                    StableMobileUi {
                        NoInternetBlockingScreen(
                            title = "No internet connection",
                            subtitle = "Turn on internet to continue.",
                            onTap = ::showNoInternetMessage
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterInternetAvailabilityCallback(networkCallback)
        super.onDestroy()
    }

    private fun navigateTo(destination: Class<*>) {
        startActivity(
            Intent(this, destination).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    private fun showNoInternetMessage() {
        Toast.makeText(this, "No internet connection. Turn on internet.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun RealSaathiAnimatedSplash() {
    val brandPurple = Color(0xFF5A12F6)
    val brandPink = Color(0xFFFF0D8F)
    val brandViolet = Color(0xFF9A10E3)
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    val manOffset = remember { Animatable(-1.18f) }
    val womanOffset = remember { Animatable(1.18f) }
    val embraceScale = remember { Animatable(0.94f) }
    val completeLogoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleRise = remember { Animatable(22f) }
    val underlineProgress = remember { Animatable(0f) }

    val ambient = rememberInfiniteTransition(label = "splashAmbient")
    val glowPulse by ambient.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splashGlow"
    )

    LaunchedEffect(Unit) {
        delay(120)
        coroutineScope {
            launch {
                manOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
                )
            }
            launch {
                womanOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
                )
            }
        }

        coroutineScope {
            launch {
                completeLogoAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                )
            }
            launch {
                embraceScale.animateTo(
                    targetValue = 1.045f,
                    animationSpec = tween(durationMillis = 190, easing = FastOutSlowInEasing)
                )
                embraceScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                )
            }
        }

        delay(80)
        coroutineScope {
            launch {
                titleAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
                )
            }
            launch {
                titleRise.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
                )
            }
            launch {
                underlineProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 620, delayMillis = 130, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            background,
                            surface,
                            background
                        )
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            brandPink.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.82f, size.height * 0.22f),
                        radius = size.minDimension * 0.54f * glowPulse
                    ),
                    radius = size.minDimension * 0.54f * glowPulse,
                    center = Offset(size.width * 0.82f, size.height * 0.22f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            brandPurple.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.14f, size.height * 0.82f),
                        radius = size.minDimension * 0.58f * glowPulse
                    ),
                    radius = size.minDimension * 0.58f * glowPulse,
                    center = Offset(size.width * 0.14f, size.height * 0.82f)
                )
            }
    ) {
        val stageHeight = minOf(maxHeight * 0.76f, maxWidth * 1.72f)
        val logoSize = minOf(maxWidth * 0.88f, stageHeight * 0.82f)
        val manWidth = stageHeight * 0.70f
        val womanWidth = stageHeight * 0.75f
        val characterSeparation = stageHeight * 0.12f
        val entryDistance = maxWidth * 0.75f

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(stageHeight),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(logoSize * 1.1f)
                        .graphicsLayer {
                            scaleX = glowPulse
                            scaleY = glowPulse
                            alpha = 0.22f + (completeLogoAlpha.value * 0.34f)
                        }
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(
                                        brandPink.copy(alpha = 0.32f),
                                        brandViolet.copy(alpha = 0.16f),
                                        Color.Transparent
                                    )
                                )
                            )
                        }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = embraceScale.value
                            scaleY = embraceScale.value
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.splash_man),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(manWidth)
                            .height(stageHeight)
                            .graphicsLayer {
                                translationX = -characterSeparation.toPx() +
                                    (manOffset.value * entryDistance.toPx())
                                alpha = 1f - completeLogoAlpha.value
                            }
                    )

                    Image(
                        painter = painterResource(id = R.drawable.splash_woman),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(womanWidth)
                            .height(stageHeight)
                            .graphicsLayer {
                                translationX = characterSeparation.toPx() +
                                    (womanOffset.value * entryDistance.toPx())
                                alpha = 1f - completeLogoAlpha.value
                            }
                    )

                    Image(
                        painter = painterResource(id = R.drawable.realsaathi_logo_transparent),
                        contentDescription = stringResource(id = R.string.content_desc_brand_logo),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(logoSize)
                            .graphicsLayer { alpha = completeLogoAlpha.value }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.login_brand),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(horizontal = 24.dp).graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleRise.value
                }
            )

            Spacer(modifier = Modifier.height(11.dp))

            Box(
                modifier = Modifier
                    .width((92f * underlineProgress.value).dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(brandPurple, brandPink, Color(0xFFFF87C7))
                        )
                    )
                    .graphicsLayer { alpha = titleAlpha.value }
            )
        }
    }
}
