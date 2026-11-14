package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.db.AppEntity
import com.example.data.system.AppDiscoveryManager
import kotlin.math.roundToInt

data class AppDragSession(
    val app: AppEntity,
    val pointerRootPos: Offset,
    val isRevealingDesktop: Boolean = false,
)

interface AppDragCoordinator {
    val session: AppDragSession?
    val canvasBoundsInRoot: Rect?
    fun updateCanvasBounds(bounds: Rect)
    fun computeDropFractions(rootPos: Offset): Pair<Float, Float>
    fun onDragMoved(app: AppEntity, rootPos: Offset, revealed: Boolean)
    fun onDrop(app: AppEntity, rootPos: Offset)
    fun onCancel()
}

val LocalAppDragCoordinator = compositionLocalOf<AppDragCoordinator?> { null }

@Composable
fun rememberAppDragToDesktopController(
    gridSnapEnabled: Boolean,
    gridSnapStepDp: Int,
    onDropApp: (app: AppEntity, xFraction: Float, yFraction: Float) -> Unit,
): AppDragCoordinator {
    val density = LocalDensity.current
    val latestGridSnap by rememberUpdatedState(gridSnapEnabled)
    val latestGridStep by rememberUpdatedState(gridSnapStepDp)
    val latestOnDrop by rememberUpdatedState(onDropApp)

    var currentSession by remember { mutableStateOf<AppDragSession?>(null) }
    var currentCanvasBounds by remember { mutableStateOf<Rect?>(null) }

    return remember {
        object : AppDragCoordinator {
            override val session: AppDragSession?
                get() = currentSession

            override val canvasBoundsInRoot: Rect?
                get() = currentCanvasBounds

            override fun updateCanvasBounds(bounds: Rect) {
                currentCanvasBounds = bounds
            }

            override fun computeDropFractions(rootPos: Offset): Pair<Float, Float> {
                val bounds = currentCanvasBounds
                if (bounds == null || bounds.width <= 12f || bounds.height <= 12f) {
                    return 0.38f to 0.36f
                }
                val wPx = with(density) { 84.dp.toPx() }
                val hPx = with(density) { 92.dp.toPx() }
                val stepPx = with(density) { latestGridStep.dp.toPx().coerceAtLeast(8f) }
                var leftPx = (rootPos.x - bounds.left - wPx / 2f).coerceIn(0f, (bounds.width - wPx).coerceAtLeast(1f))
                var topPx = (rootPos.y - bounds.top - hPx / 2f).coerceIn(0f, (bounds.height - hPx).coerceAtLeast(1f))
                if (latestGridSnap) {
                    leftPx = ((leftPx / stepPx).roundToInt() * stepPx).coerceIn(0f, (bounds.width - wPx).coerceAtLeast(1f))
                    topPx = ((topPx / stepPx).roundToInt() * stepPx).coerceIn(0f, (bounds.height - hPx).coerceAtLeast(1f))
                }
                val fx = (leftPx / bounds.width).coerceIn(0f, 0.90f)
                val fy = (topPx / bounds.height).coerceIn(0f, 0.90f)
                return fx to fy
            }

            override fun onDragMoved(app: AppEntity, rootPos: Offset, revealed: Boolean) {
                currentSession = AppDragSession(
                    app = app,
                    pointerRootPos = rootPos,
                    isRevealingDesktop = revealed,
                )
            }

            override fun onDrop(app: AppEntity, rootPos: Offset) {
                val (fx, fy) = computeDropFractions(rootPos)
                currentSession = null
                latestOnDrop(app, fx, fy)
            }

            override fun onCancel() {
                currentSession = null
            }
        }
    }
}

/**
 * Enables clicking to launch, stationary long-pressing to open customization/options,
 * and long-pressing + dragging past 18dp to float and drop the app shortcut onto the home desktop.
 */
@Composable
fun Modifier.drawerAppItemInteraction(
    app: AppEntity,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
): Modifier {
    val dragCoordinator = LocalAppDragCoordinator.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val latestOnClick by rememberUpdatedState(onClick)
    val latestOnLongPress by rememberUpdatedState(onLongPress)
    val latestCoordinator by rememberUpdatedState(dragCoordinator)

    var itemCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var suppressNextClick by remember(app.packageName) { mutableStateOf(false) }

    return this
        .onGloballyPositioned { itemCoords = it }
        .semantics {
            onClick {
                latestOnClick()
                true
            }
            onLongClick {
                latestOnLongPress()
                true
            }
        }
        .clickable {
            if (suppressNextClick) {
                suppressNextClick = false
            } else {
                latestOnClick()
            }
        }
        .pointerInput(app.packageName) {
            val revealThresholdPx = with(density) { 18.dp.toPx() }
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                suppressNextClick = false
                val dragStart = awaitLongPressOrCancellation(down.id)
                if (dragStart != null) {
                    suppressNextClick = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val startLocal = dragStart.position
                    var lastLocal = startLocal
                    var movedToDesktop = false

                    val startRoot = itemCoords?.takeIf { it.isAttached }?.localToRoot(startLocal) ?: startLocal
                    latestCoordinator?.onDragMoved(app, startRoot, revealed = false)

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull { it.id == dragStart.id }
                            ?: event.changes.firstOrNull()
                        if (change == null || !change.pressed) {
                            change?.consume()
                            break
                        }
                        lastLocal = change.position
                        val dist = (lastLocal - startLocal).getDistance()
                        if (!movedToDesktop && dist >= revealThresholdPx) {
                            movedToDesktop = true
                        }
                        if (movedToDesktop || dist > 4f) {
                            change.consume()
                        }
                        val currentRoot = itemCoords?.takeIf { it.isAttached }?.localToRoot(lastLocal) ?: lastLocal
                        latestCoordinator?.onDragMoved(app, currentRoot, revealed = movedToDesktop)
                    }

                    val coord = latestCoordinator
                    if (movedToDesktop && coord != null) {
                        val finalRoot = itemCoords?.takeIf { it.isAttached }?.localToRoot(lastLocal) ?: lastLocal
                        coord.onDrop(app, finalRoot)
                    } else {
                        coord?.onCancel()
                        latestOnLongPress()
                    }
                }
            }
        }
}

@Composable
fun AppDragOverlayGhost(
    session: AppDragSession?,
    targetPageTitle: String,
    appDiscovery: AppDiscoveryManager,
    iconShapeKey: String,
) {
    if (session == null) return
    val density = LocalDensity.current
    val halfBadgeW = with(density) { 46.dp.toPx() }
    val halfBadgeH = with(density) { 54.dp.toPx() }
    val xPx = (session.pointerRootPos.x - halfBadgeW).roundToInt()
    val yPx = (session.pointerRootPos.y - halfBadgeH).roundToInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_drag_to_desktop_ghost"),
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            shadowElevation = 16.dp,
            modifier = Modifier
                .offset { IntOffset(xPx, yPx) }
                .padding(4.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppIconView(
                    app = session.app,
                    appDiscovery = appDiscovery,
                    size = 52.dp,
                    iconShapeKey = iconShapeKey,
                )
                Text(
                    text = session.app.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (session.isRevealingDesktop) "Drop on $targetPageTitle" else "Drag to desktop",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (session.isRevealingDesktop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = if (session.isRevealingDesktop) 0.16f else 0.08f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}
