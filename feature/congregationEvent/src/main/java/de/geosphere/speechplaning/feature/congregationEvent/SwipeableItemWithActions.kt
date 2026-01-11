package de.geosphere.speechplaning.feature.congregationEvent

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val SWIPE_THRESHOLD = 3
private const val CLOSE_ANIMATION_THRESHOLD = 0.25 // 25% der Strecke, bevor animiert wird

@Composable
fun SwipeableItemWithActions(
    isRevealed: Boolean,
    actionsLeft: @Composable RowScope.() -> Unit,
    actionsRight: @Composable RowScope.() -> Unit = {},
    modifier: Modifier,
    onExpanded: () -> Unit,
    onCollapsed: () -> Unit,
    content: @Composable () -> Unit
) {
    var actionsLeftWidth by remember { mutableStateOf(0f) }
    var actionsRightWidth by remember { mutableStateOf(0f) }
    val offset = remember { Animatable(initialValue = 0f) }
    val scope = rememberCoroutineScope()
    var swipeDirection by remember { mutableStateOf<SwipeDirection?>(null) }

    LaunchedEffect(isRevealed, swipeDirection) {
        if (isRevealed) {
            when (swipeDirection) {
                SwipeDirection.LEFT -> offset.animateTo(-actionsRightWidth)
                SwipeDirection.RIGHT -> offset.animateTo(actionsLeftWidth)
                null -> offset.animateTo(0f)
            }
        } else {
            offset.animateTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Linke Aktionen (erscheinen beim nach-rechts-swipen)
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .onSizeChanged {
                    actionsLeftWidth = it.width.toFloat()
                }
                .align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actionsLeft()
        }

        // Rechte Aktionen (erscheinen beim nach-links-swipen)
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .onSizeChanged {
                    actionsRightWidth = it.width.toFloat()
                }
                .align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actionsRight()
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .pointerInput(actionsLeftWidth, actionsRightWidth) {
                    var totalDragAmount = 0f

                    detectHorizontalDragGestures(
                        onDragStart = {
                            // swipeDirection NICHT zurücksetzen - wir wollen wissen ob bereits eine Seite offen ist
                            totalDragAmount = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            scope.launch {
                                totalDragAmount += dragAmount

                                // Swipe nach rechts (positiver offset) - zeigt linke Aktionen
                                if (dragAmount > 0) {
                                    val maxOffset = when (swipeDirection) {
                                        SwipeDirection.LEFT -> 0f // Wenn rechts offen, nur zurück zu 0f erlauben
                                        else -> max(actionsLeftWidth, 0f)
                                    }
                                    val newOffset = (offset.value + dragAmount).coerceIn(0f, maxOffset)

                                    // Wenn RECHTE Seite offen (SwipeDirection.LEFT, offset negativ) und wir nach rechts wischen
                                    // Nach 25% des Weges → animiert zurück zur Mitte
                                    if (swipeDirection == SwipeDirection.LEFT &&
                                        actionsRightWidth > 0 &&
                                        newOffset >=
                                        -actionsRightWidth * (1f - CLOSE_ANIMATION_THRESHOLD)
                                    ) {
                                        offset.animateTo(0f)
                                        swipeDirection = null
                                    } else {
                                        offset.snapTo(newOffset)
                                        if (newOffset > actionsLeftWidth / SWIPE_THRESHOLD) {
                                            swipeDirection = SwipeDirection.RIGHT
                                        }
                                    }
                                }
                                // Swipe nach links (negativer offset) - zeigt rechte Aktionen
                                else {
                                    val minOffset = when (swipeDirection) {
                                        SwipeDirection.RIGHT -> 0f // Wenn links offen, nur zurück zu 0f erlauben
                                        else -> min(-actionsRightWidth, 0f)
                                    }
                                    val newOffset = (offset.value + dragAmount).coerceIn(minOffset, 0f)

                                    // Wenn LINKE Seite offen (SwipeDirection.RIGHT, offset positiv) und wir nach links wischen
                                    // Nach 25% des Weges → animiert zurück zur Mitte
                                    if (swipeDirection == SwipeDirection.RIGHT &&
                                        actionsLeftWidth > 0 &&
                                        newOffset <=
                                        actionsLeftWidth * (1f - CLOSE_ANIMATION_THRESHOLD)
                                    ) {
                                        offset.animateTo(0f)
                                        swipeDirection = null
                                    } else {
                                        offset.snapTo(newOffset)
                                        if (newOffset < -actionsRightWidth / SWIPE_THRESHOLD) {
                                            swipeDirection = SwipeDirection.LEFT
                                        }
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                // Check ob wir in die entgegengesetzte Richtung wischen (von offen zu geschlossen)
                                val isSwipingToCloseFromLeft = swipeDirection == SwipeDirection.RIGHT &&
                                    offset.value < actionsLeftWidth
                                val isSwipingToCloseFromRight = swipeDirection == SwipeDirection.LEFT &&
                                    offset.value > -actionsRightWidth

                                when {
                                    // Wenn wir von einer offenen Seite zurück zur Mitte wischen -> immer schließen
                                    isSwipingToCloseFromLeft || isSwipingToCloseFromRight -> {
                                        offset.animateTo(0f)
                                        swipeDirection = null
                                        onCollapsed()
                                    }
                                    // Nach rechts gescrollt genügend weit (nur wenn nicht bereits links offen)
                                    offset.value >= actionsLeftWidth / 2f &&
                                        actionsLeftWidth > 0 &&
                                        swipeDirection != SwipeDirection.LEFT -> {
                                        offset.animateTo(actionsLeftWidth)
                                        swipeDirection = SwipeDirection.RIGHT
                                        onExpanded()
                                    }
                                    // Nach links gescrollt genügend weit (nur wenn nicht bereits rechts offen)
                                    offset.value <= -actionsRightWidth / 2f &&
                                        actionsRightWidth > 0 &&
                                        swipeDirection != SwipeDirection.RIGHT -> {
                                        offset.animateTo(-actionsRightWidth)
                                        swipeDirection = SwipeDirection.LEFT
                                        onExpanded()
                                    }
                                    // Nicht genügend gescrollt -> zurück
                                    else -> {
                                        offset.animateTo(0f)
                                        swipeDirection = null
                                        onCollapsed()
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

enum class SwipeDirection {
    LEFT, // Nach links swiped, zeigt rechte Aktionen
    RIGHT // Nach rechts swiped, zeigt linke Aktionen
}

@Preview
@Composable
private fun SwipeBothDirectionsPreview() {
    SwipeableItemWithActions(
        isRevealed = false,
        actionsLeft = {
            Box(modifier = Modifier
                .size(40.dp)
                .padding(horizontal = 2.dp)
                .background(Color.Green))
            Box(modifier = Modifier
                .size(40.dp)
                .padding(horizontal = 2.dp)
                .background(Color.Red))
        },
        actionsRight = {
            Box(modifier = Modifier
                .size(40.dp)
                .padding(horizontal = 2.dp)
                .background(Color.Blue))
            Box(modifier = Modifier
                .size(40.dp)
                .padding(horizontal = 2.dp)
                .background(Color.Yellow))
        },
        modifier = Modifier,
        onExpanded = {},
        onCollapsed = {}
    ) {
        Box(modifier = Modifier
            .size(200.dp, 50.dp)
            .background(Color.Gray))
    }
}

@Preview
@Composable
private fun SwipeRightOnlyPreview() {
    SwipeableItemWithActions(
        isRevealed = true,
        actionsLeft = {
            Box(
                Modifier
                    .size(40.dp)
                    .padding(horizontal = 2.dp)
                    .background(Color.Green)
            )
            Box(
                Modifier
                    .size(40.dp)
                    .padding(horizontal = 2.dp)
                    .background(Color.Red)
            )
        },
        actionsRight = {},
        modifier = Modifier,
        onExpanded = {},
        onCollapsed = {}
    ) {
        Box(
            Modifier
                .size(200.dp, 50.dp)
                .background(Color.Gray)
        )
    }
}
