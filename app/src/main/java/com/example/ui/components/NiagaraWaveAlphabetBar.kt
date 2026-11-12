package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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

@Composable
fun NiagaraWaveAlphabetBar(
    activeLetters: Set<String>,
    waveEnabled: Boolean,
    waveIntensity: Float,
    baseFontSp: Float,
    selectedFontSp: Float,
    accentColorHex: Long,
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

    fun handlePointerY(y: Float) {
        touchY = y.coerceIn(0f, containerHeightPx.toFloat())
        val count = NiagaraAlphabetLetters.size
        val idx = ((touchY / containerHeightPx.toFloat()) * count).toInt().coerceIn(0, count - 1)
        val letter = NiagaraAlphabetLetters[idx]
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
                .width(30.dp)
                .onSizeChanged { containerHeightPx = it.height.coerceAtLeast(1) }
                .pointerInput(activeLetters) {
                    detectTapGestures(
                        onPress = { offset ->
                            isTouching = true
                            handlePointerY(offset.y)
                            tryAwaitRelease()
                            isTouching = false
                            activeLetter = null
                        }
                    )
                }
                .pointerInput(activeLetters) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isTouching = true
                            handlePointerY(offset.y)
                        },
                        onDragEnd = {
                            isTouching = false
                            activeLetter = null
                        },
                        onDragCancel = {
                            isTouching = false
                            activeLetter = null
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            handlePointerY(change.position.y)
                        }
                    )
                },
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val count = NiagaraAlphabetLetters.size
            val selectedIdx = if (isTouching && touchY >= 0f) {
                (touchY / containerHeightPx.toFloat()) * (count - 1)
            } else -10f

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
                            isPresent -> MaterialTheme.colorScheme.onSurface
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
