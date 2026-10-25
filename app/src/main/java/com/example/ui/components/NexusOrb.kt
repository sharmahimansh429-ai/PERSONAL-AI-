package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRed
import com.example.ui.theme.ParticleCyan
import com.example.ui.theme.ParticleSilver
import com.example.ui.theme.ParticleTitanium
import com.example.ui.theme.ParticleViolet
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

/**
 * Pre-calculated point on unit sphere with coordinates and metadata
 */
private class FinePoint(
    val x0: Float,
    val y0: Float,
    val z0: Float,
    val phi: Float,
    val lambda: Float,
    val layer: Int, // 0 = fine wave shell, 1 = dense core stardust, 2 = orbital stream
    val highlightType: Int // 0 = silver, 1 = cyan, 2 = titanium, 3 = violet
)

@Composable
fun NexusOrb(
    modifier: Modifier = Modifier,
    orbState: OrbState = OrbState.IDLE,
    soundLevel: Float = 0f,
    orbSize: Dp = 230.dp,
    onClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val touchPulse = remember { Animatable(0f) }

    // Parallax interactive 3D rotation angles
    var dragYaw by remember { mutableFloatStateOf(0f) }
    var dragPitch by remember { mutableFloatStateOf(0f) }

    // Precomputed fine particle cloud (~2,960 dots using Fibonacci distribution)
    val particles = remember {
        val list = ArrayList<FinePoint>(2960)
        val goldenRatio = (1f + sqrt(5f)) / 2f
        val goldenAngle = (2f * PI * (1f - 1f / goldenRatio)).toFloat()

        // Layer 0: High-density fine wave shell (2,400 points)
        val countShell = 2400
        for (i in 0 until countShell) {
            val y0 = 1f - (i.toFloat() / (countShell - 1f)) * 2f
            val rCirc = sqrt((1f - y0 * y0).coerceAtLeast(0f))
            val theta = goldenAngle * i
            val x0 = cos(theta) * rCirc
            val z0 = sin(theta) * rCirc
            val phi = atan2(z0, x0)
            val lambda = kotlin.math.asin(y0.coerceIn(-1f, 1f))
            val highlight = when {
                i % 18 == 0 -> 1 // Restrained cyan highlight
                i % 29 == 0 -> 3 // Subtle violet glint
                i % 6 == 0 -> 2  // Brushed titanium
                else -> 0        // Pure metallic silver
            }
            list.add(FinePoint(x0, y0, z0, phi, lambda, layer = 0, highlightType = highlight))
        }

        // Layer 1: Inner nucleus stardust (320 points)
        val countInner = 320
        for (i in 0 until countInner) {
            val y0 = 1f - (i.toFloat() / (countInner - 1f)) * 2f
            val rCirc = sqrt((1f - y0 * y0).coerceAtLeast(0f))
            val theta = goldenAngle * (i + 1200)
            val radInner = 0.38f + 0.12f * sin(i.toFloat() * 0.7f)
            val x0 = cos(theta) * rCirc * radInner
            val z0 = sin(theta) * rCirc * radInner
            val highlight = if (i % 12 == 0) 1 else 0
            list.add(FinePoint(x0, y0 * radInner, z0, theta, y0, layer = 1, highlightType = highlight))
        }

        // Layer 2: Orbital fine harmonic ring (240 points)
        val countRing = 240
        for (i in 0 until countRing) {
            val angle = (2f * PI * i / countRing).toFloat()
            val rSpread = 1.16f + 0.05f * sin(angle * 4f)
            val tilt = (22f * PI / 180f).toFloat()
            val x0 = cos(angle) * rSpread
            val y0 = sin(angle) * sin(tilt) * rSpread
            val z0 = sin(angle) * cos(tilt) * rSpread
            val highlight = if (i % 10 == 0) 1 else 2
            list.add(FinePoint(x0, y0, z0, angle, tilt, layer = 2, highlightType = highlight))
        }

        list
    }

    val infiniteTransition = rememberInfiniteTransition(label = "NexusOrbTransition")

    // State-based animation speeds
    val rotationPeriod = when (orbState) {
        OrbState.THINKING -> 3200
        OrbState.LISTENING -> 6800
        OrbState.SPEAKING -> 4800
        OrbState.ERROR -> 11000
        OrbState.IDLE -> 9000
    }

    val continuousYaw by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationPeriod, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "continuousYaw"
    )

    val surfaceWavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (orbState == OrbState.THINKING) 1700 else 4200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "surfaceWavePhase"
    )

    val breathingCycle by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (orbState == OrbState.SPEAKING) 750 else 2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingCycle"
    )

    Box(
        modifier = modifier
            .size(orbSize)
            .pointerInput(Unit) {
                detectTapGestures {
                    coroutineScope.launch {
                        touchPulse.snapTo(0f)
                        touchPulse.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
                        touchPulse.snapTo(0f)
                    }
                    onClick()
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    dragYaw = (dragYaw + dragAmount.x * 0.007f)
                    dragPitch = (dragPitch - dragAmount.y * 0.007f).coerceIn(-1.1f, 1.1f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.44f

            // Total 3D viewing angles with drag parallax
            val totalYaw = continuousYaw + dragYaw
            val totalPitch = (0.24f * sin(continuousYaw * 0.5f) + dragPitch).toFloat()

            // 1. Volumetric Ambient Glow Backdrop (Restrained dark graphite & titanium glow)
            drawVolumetricAura(center, baseRadius, orbState)

            // 2. Faint Metallic Reticles (Subtle hairline orbits)
            drawMetallicReticles(center, baseRadius, totalYaw, orbState)

            // 3. Audio & Tap Harmonic Wave Ripples
            if (orbState == OrbState.SPEAKING || orbState == OrbState.LISTENING || touchPulse.value > 0f) {
                drawWaveHarmonics(center, baseRadius, surfaceWavePhase, soundLevel, touchPulse.value, orbState)
            }

            // 4. Render 3D Stardust Particle Waveform
            draw3DFineParticleCloud(
                particles = particles,
                center = center,
                baseRadius = baseRadius,
                yaw = totalYaw,
                pitch = totalPitch,
                wavePhase = surfaceWavePhase,
                breathing = breathingCycle,
                soundLevel = soundLevel,
                touchPulse = touchPulse.value,
                orbState = orbState
            )
        }
    }
}

// 1. Soft Volumetric Backdrop (Subtle deep graphite / charcoal aura)
private fun DrawScope.drawVolumetricAura(
    center: Offset,
    radius: Float,
    state: OrbState
) {
    val auraColor = when (state) {
        OrbState.ERROR -> NeonRed
        OrbState.LISTENING -> NeonEmerald
        OrbState.THINKING -> ParticleViolet
        OrbState.SPEAKING -> ParticleCyan
        OrbState.IDLE -> Color(0xFF64748B)
    }

    val glowRadius = radius * 1.9f
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to auraColor.copy(alpha = 0.12f),
                0.35f to auraColor.copy(alpha = 0.04f),
                0.7f to Color(0xFF0F172A).copy(alpha = 0.015f),
                1.0f to Color.Transparent
            ),
            center = center,
            radius = glowRadius
        ),
        radius = glowRadius,
        center = center
    )
}

// 2. Subtle Metallic Titanium Hairline Orbits
private fun DrawScope.drawMetallicReticles(
    center: Offset,
    radius: Float,
    yaw: Float,
    state: OrbState
) {
    val ringColor = when (state) {
        OrbState.ERROR -> NeonRed.copy(alpha = 0.20f)
        OrbState.LISTENING -> NeonEmerald.copy(alpha = 0.20f)
        else -> Color(0xFF334155).copy(alpha = 0.28f)
    }

    // Outer faint metallic hairline orbit
    drawCircle(
        color = ringColor,
        radius = radius * 1.32f,
        center = center,
        style = Stroke(
            width = 0.8.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 14f), yaw * 12f)
        )
    )

    // Inner fine concentric guide
    drawCircle(
        color = Color(0xFF1E293B).copy(alpha = 0.40f),
        radius = radius * 1.14f,
        center = center,
        style = Stroke(width = 0.6.dp.toPx())
    )
}

// 3. Audio & Tap Harmonic Wave Ripples
private fun DrawScope.drawWaveHarmonics(
    center: Offset,
    radius: Float,
    phase: Float,
    soundLevel: Float,
    tapProgress: Float,
    state: OrbState
) {
    val waveColor = when (state) {
        OrbState.LISTENING -> NeonEmerald
        OrbState.SPEAKING -> ParticleCyan
        OrbState.ERROR -> NeonRed
        else -> ParticleSilver
    }

    if (state == OrbState.SPEAKING || state == OrbState.LISTENING) {
        val count = 3
        for (i in 0 until count) {
            val fraction = ((phase / (2 * PI).toFloat()) + (i.toFloat() / count)) % 1f
            val rippleRadius = radius * (1.02f + fraction * 0.90f)
            val alpha = (1f - fraction) * (0.20f + soundLevel.coerceIn(0f, 1f) * 0.35f)
            drawCircle(
                color = waveColor.copy(alpha = alpha.coerceIn(0f, 0.60f)),
                radius = rippleRadius,
                center = center,
                style = Stroke(width = 0.9.dp.toPx() * (1f - fraction * 0.5f))
            )
        }
    }

    if (tapProgress > 0f) {
        val tapRadius = radius * (1.0f + tapProgress * 1.35f)
        val tapAlpha = (1f - tapProgress).coerceIn(0f, 1f) * 0.55f
        drawCircle(
            color = ParticleSilver.copy(alpha = tapAlpha),
            radius = tapRadius,
            center = center,
            style = Stroke(width = 1.2.dp.toPx() * (1f - tapProgress * 0.6f))
        )
    }
}

// 4. Primary 3D Fine Particle Waveform (70-85% smaller particles, high density stardust)
private fun DrawScope.draw3DFineParticleCloud(
    particles: List<FinePoint>,
    center: Offset,
    baseRadius: Float,
    yaw: Float,
    pitch: Float,
    wavePhase: Float,
    breathing: Float,
    soundLevel: Float,
    touchPulse: Float,
    orbState: OrbState
) {
    val cosYaw = cos(yaw)
    val sinYaw = sin(yaw)
    val cosPitch = cos(pitch)
    val sinPitch = sin(pitch)

    val camDist = baseRadius * 3.4f

    // Wave amplitude based on state
    val waveAmp = when (orbState) {
        OrbState.THINKING -> 0.16f
        OrbState.LISTENING -> 0.10f + soundLevel.coerceIn(0f, 1f) * 0.18f
        OrbState.SPEAKING -> 0.12f + soundLevel.coerceIn(0f, 1f) * 0.22f
        OrbState.ERROR -> 0.15f
        OrbState.IDLE -> 0.07f
    }

    val stateRadialScale = when (orbState) {
        OrbState.LISTENING -> 0.93f // Inward magnetic flow
        OrbState.SPEAKING -> 1.05f  // Expanding circular waves
        OrbState.THINKING -> 1.02f
        else -> 1.0f
    }

    val effectiveBaseRadius = baseRadius * breathing * stateRadialScale

    // Lighting vector for metallic grey specular reflection
    val lx = 0.35f
    val ly = 0.45f
    val lz = 0.82f

    val shellBaseRadiusPx = 0.22.dp.toPx()
    val shellVarRadiusPx = 0.44.dp.toPx()
    val nucleusBaseRadiusPx = 0.28.dp.toPx()
    val nucleusVarRadiusPx = 0.38.dp.toPx()
    val ringBaseRadiusPx = 0.18.dp.toPx()
    val ringVarRadiusPx = 0.30.dp.toPx()

    for (p in particles) {
        // Fluid 3D spherical harmonics wave equations
        val w1 = sin(3.2f * p.phi + wavePhase * 1.3f) * cos(2.6f * p.lambda)
        val w2 = sin(5.4f * p.lambda - wavePhase * 1.9f) * cos(3.2f * p.phi)
        val w3 = cos(4.0f * (p.x0 + p.y0 + p.z0) + wavePhase * 0.9f)
        val surfaceDisplacement = (0.50f * w1 + 0.30f * w2 + 0.20f * w3) * waveAmp

        val touchDisplacement = if (touchPulse > 0f && p.layer == 0) {
            0.22f * sin(touchPulse * PI.toFloat())
        } else 0f

        val r = when (p.layer) {
            0 -> effectiveBaseRadius * (1f + surfaceDisplacement + touchDisplacement)
            1 -> effectiveBaseRadius * 0.50f * (1f + surfaceDisplacement * 0.35f)
            else -> effectiveBaseRadius * (1.16f + 0.04f * sin(p.phi * 4f + wavePhase))
        }

        // Cartesian 3D coordinates
        val x3d = p.x0 * r
        val y3d = p.y0 * r
        val z3d = p.z0 * r

        // 3D Rotation (Yaw + Pitch)
        val xRot = x3d * cosYaw + z3d * sinYaw
        val zTemp = -x3d * sinYaw + z3d * cosYaw
        val yRot = y3d * cosPitch - zTemp * sinPitch
        val zRot = y3d * sinPitch + zTemp * cosPitch

        // Perspective scale & screen projection
        val perspective = camDist / (camDist - zRot)
        val screenX = center.x + xRot * perspective
        val screenY = center.y + yRot * perspective

        // Depth factor [0 = deep back, 1 = front closest]
        val depthNorm = ((zRot + baseRadius) / (2f * baseRadius)).coerceIn(0f, 1f)

        // Ultra-fine particle radius (70-85% size reduction)
        val dotRadius = when (p.layer) {
            1 -> (nucleusBaseRadiusPx + depthNorm * nucleusVarRadiusPx) * perspective
            2 -> (ringBaseRadiusPx + depthNorm * ringVarRadiusPx) * perspective
            else -> (shellBaseRadiusPx + depthNorm * shellVarRadiusPx) * perspective
        }

        // Metallic reflection calculation
        val nx = p.x0 * cosYaw + p.z0 * sinYaw
        val nz = -p.x0 * sinYaw + p.z0 * cosYaw
        val nyRot = p.y0 * cosPitch - nz * sinPitch
        val nzRot = p.y0 * sinPitch + nz * cosPitch
        val dotProduct = (nx * lx + nyRot * ly + nzRot * lz).coerceIn(0f, 1f)
        val specular = dotProduct.pow(3) * 0.35f

        val baseAlpha = when (p.layer) {
            1 -> 0.30f + depthNorm * 0.55f + specular
            2 -> 0.18f + depthNorm * 0.60f
            else -> 0.14f + depthNorm * 0.72f + specular
        }.coerceIn(0.08f, 0.95f)

        // Determine particle color with subtle metallic highlights
        val dotColor = when {
            orbState == OrbState.ERROR -> {
                if (p.highlightType == 1 || p.highlightType == 3) NeonRed else ParticleSilver
            }
            orbState == OrbState.LISTENING -> {
                if (p.highlightType == 1) NeonEmerald else if (depthNorm > 0.75f) ParticleSilver else ParticleTitanium
            }
            orbState == OrbState.THINKING -> {
                if (p.highlightType == 3) ParticleViolet else if (p.highlightType == 1) ParticleCyan else ParticleSilver
            }
            else -> {
                when (p.highlightType) {
                    1 -> ParticleCyan      // Restrained cyan stardust highlight
                    2 -> ParticleTitanium  // Brushed titanium grey
                    3 -> ParticleViolet    // Subtle violet glint
                    else -> if (specular > 0.2f) Color(0xFFF1F5F9) else ParticleSilver // Metallic silver
                }
            }
        }

        // Draw fine stardust particle
        drawCircle(
            color = dotColor.copy(alpha = baseAlpha),
            radius = dotRadius,
            center = Offset(screenX, screenY)
        )

        // Micro-bloom halo for prominent foreground points (restrained, no large beads)
        if (depthNorm > 0.88f && p.layer == 0) {
            drawCircle(
                color = dotColor.copy(alpha = 0.14f * (depthNorm - 0.88f) * 8.3f),
                radius = dotRadius * 1.8f,
                center = Offset(screenX, screenY)
            )
        }
    }
}
