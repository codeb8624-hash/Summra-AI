package com.example.summraai.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween

object AnimationDuration {
    val fast = 200
    val medium = 350
    val slow = 500
    val xSlow = 800
}

object AnimationSpecs {
    val fastTween = tween<Float>(durationMillis = AnimationDuration.fast, easing = LinearEasing)
    val mediumTween = tween<Float>(durationMillis = AnimationDuration.medium, easing = LinearEasing)
    val slowTween = tween<Float>(durationMillis = AnimationDuration.slow, easing = LinearEasing)
}
