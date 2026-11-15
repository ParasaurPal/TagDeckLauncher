package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

val NiagaraAlphabetLetters: List<String> = ('A'..'Z').map { it.toString() } + "#"

fun appIndexLetter(displayName: String): String {
    val first = displayName.trim().firstOrNull()?.uppercaseChar() ?: '#'
    return if (first in 'A'..'Z') first.toString() else "#"
}

/** Returns the closest populated index letter for the current filtered app set. */
fun nearestActiveAlphabetLetter(targetIndex: Int, activeLetters: Set<String>): String? {
    val clampedIndex = targetIndex.coerceIn(0, NiagaraAlphabetLetters.lastIndex)
    return NiagaraAlphabetLetters
        .withIndex()
        .asSequence()
        .filter { it.value in activeLetters }
        .minByOrNull { kotlin.math.abs(it.index - clampedIndex) }
        ?.value
}

@Composable
fun NiagaraWaveAlphabetBar(
    activeLetters: Set<String>,
    waveEnabled: Boolean,
    waveIntensity: Float,
    baseFontSp: Float,
    selectedFontSp: Float,
    accentColorHex: Long,
    indexWidthDp: Float = 28f,
    indexColorHex: Long = accentColorHex,
    isLeftEdge: Boolean,
    hapticsEnabled: Boolean,
    onSelectLetter: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    var containerHeightPx by remember { mutableIntStateOf(1) }
    var touchY by remember { mutableFloatStateOf(-1f) }
    var isTouching by remember { mutableStateOf(false) }
    var activeLetter by remember { mutableStateOf<String?>(null) }

    val accent = Color(accentColorHex)
    val indexColor = Color(indexColorHex)

    fun handlePointerY(y: Float) {
        touchY = y.coerceIn(0f, containerHeightPx.toFloat())
        val count = NiagaraAlphabetLetters.size
        val targetIndex = ((touchY / containerHeightPx.toFloat()) * count)
            .toInt()
            .coerceIn(0, count - 1)
        // Empty initials are not dead stops: snap to the nearest letter that
        // actually exists in the currently filtered app collection.
        val letter = nearestActiveAlphabetLetter(targetIndex, activeLetters) ?: return
        if (letter != activeLetter) {
            activeLetter = letter
            if (hapticsEnabled) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            onSelectLetter(letter)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxHeight()
            .testTag("niagara_alphabet_bar"),
    ) {
        if (!isLeftEdge && isTouching && activeLetter != null) {
            FloatingLetterIndicator(
                letter = activeLetter!!,
                accent = accent,
                yPx = touchY,
                containerHeightPx = containerHeightPx,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(indexWidthDp.coerceIn(18f, 48f).dp)
                .onSizeChanged { containerHeightPx = it.height.coerceAtLeast(1) }
                .pointerInput(activeLetters) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isTouching = true
                        try {
                            handlePointerY(down.position.y)
                            var pressed = true
                            while (pressed) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                handlePointerY(change.position.y)
                                change.consume()
                                pressed = change.pressed
                            }
                        } finally {
                            isTouching = false
                            activeLetter = null
                        }
                    }
                },
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val count = NiagaraAlphabetLetters.size
            val rawWaveIndex = if (isTouching) {
                ((touchY / containerHeightPx.toFloat()) * count - 0.5f)
                    .coerceIn(0f, (count - 1).toFloat())
            } else -10f
            val selectedIdx by animateFloatAsState(
                targetValue = rawWaveIndex,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 520f),
                label = "alphabet_wave_position",
            )

            NiagaraAlphabetLetters.forEachIndexed { index, letter ->
                val isPresent = letter in activeLetters
                val dist = abs(index - selectedIdx)
                val waveFactor = if (isTouching && waveEnabled && dist < 4.5f) {
                    exp(-(dist * dist) / 3.2f) * waveIntensity.coerceIn(0.2f, 1.8f)
                } else 0f

                val fontSize = (baseFontSp + (selectedFontSp - baseFontSp) * waveFactor.coerceAtMost(1.2f)).sp
                val horizPushPx = if (waveEnabled) {
                    val push = (waveFactor * 36f).roundToInt()
                    if (isLeftEdge) push else -push
                } else 0
                val isCurrent = isTouching && activeLetter == letter

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = letter,
                        fontSize = fontSize,
                        lineHeight = fontSize,
                        fontWeight = if (isCurrent || waveFactor > 0.35f) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = when {
                            isCurrent -> accent
                            isPresent -> indexColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f)
                        },
                        modifier = Modifier
                            .offset { IntOffset(horizPushPx, 0) }
                            .alpha(if (isPresent || isCurrent) 1f else 0.45f),
                    )
                }
            }
        }

        if (isLeftEdge && isTouching && activeLetter != null) {
            FloatingLetterIndicator(
                letter = activeLetter!!,
                accent = accent,
                yPx = touchY,
                containerHeightPx = containerHeightPx,
            )
        }
    }
}

@Composable
private fun FloatingLetterIndicator(
    letter: String,
    accent: Color,
    yPx: Float,
    containerHeightPx: Int,
) {
    val clampedOffset = (yPx - 48f).coerceIn(8f, (containerHeightPx - 110f).coerceAtLeast(8f)).roundToInt()
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 6.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = accent,
            shadowElevation = 10.dp,
            modifier = Modifier
                .offset { IntOffset(0, clampedOffset) }
                .size(54.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                )
            }
        }
    }
}
