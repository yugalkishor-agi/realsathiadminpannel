package com.incoteam.frndzz

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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.incoteam.frndzz.core.network.isInternetAvailable
import com.incoteam.frndzz.core.network.registerInternetAvailabilityCallback
import com.incoteam.frndzz.core.network.unregisterInternetAvailabilityCallback
import com.incoteam.frndzz.core.ui.StableMobileUi
import com.incoteam.frndzz.core.ui.withStableFontScale
import com.incoteam.frndzz.ui.auth.login.LoginActivity
import com.incoteam.frndzz.ui.home.HomeActivity
import com.incoteam.frndzz.ui.home.NoInternetBlockingScreen
import com.incoteam.frndzz.ui.theme.FrndzzTheme
import kotlinx.coroutines.delay

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
        val sessionManager = (application as FrndzzApp).sessionManager
        val routeToHome = sessionManager.hasActiveSession()

        setContent {
            StableMobileUi {
                FrndzzTheme(darkTheme = true, dynamicColor = false) {
                    if (hasInternetConnection) {
                        val destination = remember(routeToHome) {
                            if (routeToHome) HomeActivity::class.java
                            else LoginActivity::class.java
                        }

                        LaunchedEffect(destination, routeToHome, hasInternetConnection) {
                            if (!hasInternetConnection) return@LaunchedEffect
                            delay(1850L)
                            navigateTo(destination)
                        }

                        FrndzzAnimatedSplash()
                    } else {
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
private fun FrndzzAnimatedSplash() {
    val brandPurple = Color(0xFF5A12F6)
    val brandPink = Color(0xFFFF0D8F)
    val brandViolet = Color(0xFF9A10E3)
    val bgColor = Color(0xFF040206)
    val reveal = remember { Animatable(0.14f) }
    val ambientMotion = rememberInfiniteTransition(label = "ambient")
    val glowPulse by ambientMotion.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    val orbitalRotation by ambientMotion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitalRotation"
    )
    val counterOrbitalRotation by ambientMotion.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counterOrbitalRotation"
    )
    val logoFloat by ambientMotion.animateFloat(
        initialValue = -10f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoFloat"
    )
    val logoTilt by ambientMotion.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoTilt"
    )
    val beamShift by ambientMotion.animateFloat(
        initialValue = -0.08f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beamShift"
    )

    LaunchedEffect(Unit) {
        reveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 980, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF09020B),
                            bgColor,
                            Color(0xFF050208)
                        )
                    )
                )
                drawCircle(
                    color = brandPurple.copy(alpha = 0.26f * glowPulse),
                    radius = size.minDimension * 0.5f * glowPulse,
                    center = Offset(size.width * 0.16f, size.height * 0.86f)
                )
                drawCircle(
                    color = brandPink.copy(alpha = 0.24f * glowPulse),
                    radius = size.minDimension * 0.38f * (2.02f - glowPulse),
                    center = Offset(size.width * 0.82f, size.height * 0.14f)
                )
                drawLine(
                    brush = Brush.linearGradient(
                        listOf(
                            Color.Transparent,
                            brandPink.copy(alpha = 0.2f * reveal.value),
                            Color.Transparent
                        )
                    ),
                    start = Offset(size.width * (0.12f + beamShift), size.height * 0.04f),
                    end = Offset(size.width * (0.7f + beamShift), size.height * 0.96f),
                    strokeWidth = size.width * 0.17f
                )
                drawLine(
                    brush = Brush.linearGradient(
                        listOf(
                            Color.Transparent,
                            brandPurple.copy(alpha = 0.16f * reveal.value),
                            Color.Transparent
                        )
                    ),
                    start = Offset(size.width * (0.88f - beamShift), size.height * 0.1f),
                    end = Offset(size.width * (0.3f - beamShift), size.height * 0.98f),
                    strokeWidth = size.width * 0.12f
                )
                repeat(6) { index ->
                    val y = size.height * (0.22f + (index * 0.08f))
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f * reveal.value),
                        start = Offset(size.width * 0.1f, y),
                        end = Offset(size.width * 0.9f, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.34f)
                        )
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(306.dp)
                    .graphicsLayer {
                        alpha = 0.48f + (0.52f * reveal.value)
                        scaleX = 0.86f + (0.14f * reveal.value)
                        scaleY = 0.86f + (0.14f * reveal.value)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(292.dp)
                        .graphicsLayer {
                            alpha = 0.58f * reveal.value
                            rotationZ = orbitalRotation
                        }
                        .border(
                            width = 1.4.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    brandPurple.copy(alpha = 0.62f),
                                    brandPink.copy(alpha = 0.94f),
                                    brandViolet.copy(alpha = 0.48f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(244.dp)
                        .graphicsLayer {
                            alpha = 0.38f * reveal.value
                            rotationZ = counterOrbitalRotation
                        }
                        .border(
                            width = 1.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    brandPink.copy(alpha = 0.8f),
                                    Color.Transparent,
                                    brandPurple.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(252.dp)
                        .graphicsLayer {
                            alpha = 0.7f * reveal.value
                            scaleX = glowPulse
                            scaleY = glowPulse
                        }
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        brandPink.copy(alpha = 0.28f),
                                        brandViolet.copy(alpha = 0.16f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension * 0.5f
                            )
                        }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer {
                            translationY = logoFloat
                            rotationZ = logoTilt
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(194.dp)
                            .clip(RoundedCornerShape(46.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF170713).copy(alpha = 0.96f),
                                        Color(0xFF08050B).copy(alpha = 0.98f)
                                    )
                                )
                            )
                            .border(
                                width = 1.7.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        brandPurple.copy(alpha = 0.62f),
                                        brandPink.copy(alpha = 0.96f)
                                    )
                                ),
                                shape = RoundedCornerShape(46.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.06f),
                                            brandPink.copy(alpha = 0.13f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_brand_mark),
                                contentDescription = stringResource(id = R.string.content_desc_brand_logo),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(124.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(id = R.string.login_brand),
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 5.6.sp,
                modifier = Modifier.graphicsLayer { alpha = 0.72f + (0.28f * reveal.value) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                brandPurple.copy(alpha = 0.24f),
                                brandPink.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                brandPurple.copy(alpha = 0.36f),
                                brandPink.copy(alpha = 0.56f)
                            )
                        ),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .graphicsLayer { alpha = 0.68f + (0.32f * reveal.value) }
            ) {
                Text(
                    text = stringResource(id = R.string.splash_chip),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(id = R.string.splash_tagline),
                color = Color(0xFFF0DAE6).copy(alpha = 0.72f + (0.28f * reveal.value)),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = 0.7f + (0.3f * reveal.value) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            SplashAudioMeter(
                reveal = reveal.value,
                brandPurple = brandPurple,
                brandPink = brandPink,
                brandViolet = brandViolet
            )
        }

        Text(
            text = stringResource(id = R.string.splash_powered_by),
            color = Color.White.copy(alpha = 0.68f + (0.18f * reveal.value)),
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
                .graphicsLayer { alpha = 0.62f + (0.38f * reveal.value) }
        )
    }
}

@Composable
private fun SplashAudioMeter(
    reveal: Float,
    brandPurple: Color,
    brandPink: Color,
    brandViolet: Color
) {
    val meterTransition = rememberInfiniteTransition(label = "meter")
    val barOne by meterTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 540, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barOne"
    )
    val barTwo by meterTransition.animateFloat(
        initialValue = 18f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 680, delayMillis = 80, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barTwo"
    )
    val barThree by meterTransition.animateFloat(
        initialValue = 12f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 620, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barThree"
    )
    val barFour by meterTransition.animateFloat(
        initialValue = 16f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 710, delayMillis = 210, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barFour"
    )
    val barFive by meterTransition.animateFloat(
        initialValue = 11f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 560, delayMillis = 120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barFive"
    )

    val bars = listOf(barOne, barTwo, barThree, barFour, barFive)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.graphicsLayer { alpha = 0.58f + (0.42f * reveal) }
    ) {
        bars.forEachIndexed { index, height ->
            val colors = when (index % 3) {
                0 -> listOf(brandPurple.copy(alpha = 0.9f), brandPink.copy(alpha = 0.98f))
                1 -> listOf(brandViolet.copy(alpha = 0.9f), Color.White.copy(alpha = 0.94f))
                else -> listOf(brandPink.copy(alpha = 0.96f), brandPurple.copy(alpha = 0.92f))
            }

            Box(
                modifier = Modifier
                    .width(7.dp)
                    .height((12f + height).dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.verticalGradient(colors))
            )
        }
    }
}
