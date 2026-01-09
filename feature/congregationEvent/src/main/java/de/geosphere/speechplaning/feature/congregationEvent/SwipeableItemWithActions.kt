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
import kotlin.math.roundToInt

@Composable
fun SwipeableItemWithActions(
    isRevealed: Boolean,
    actionsLeft: @Composable RowScope.() -> Unit,
    modifier: Modifier,
    onExpanded: () -> Unit,
    onCollapsed: () -> Unit,
    content: @Composable () -> Unit
) {
    var contextMenuWith by remember { mutableStateOf(0f) }
    val offset = remember { Animatable(initialValue = 0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRevealed, contextMenuWith) {
        if (isRevealed) {
            offset.animateTo(contextMenuWith)
        } else {
            offset.animateTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .background(de.geosphere.speechplaning.theme.secondaryLightHighContrast)
                .onSizeChanged {
                    contextMenuWith = it.width.toFloat()
                }
                .padding(horizontal = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actionsLeft()
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .pointerInput(contextMenuWith) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            scope.launch {
                                val newOffset = (offset.value + dragAmount).coerceIn(0f, contextMenuWith)
                                // offset.snapTo(newOffset.coerceIn(-contextMenuWith, 0f))
                                offset.snapTo(newOffset)
                                // offset.animateTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            when {
                                offset.value >= contextMenuWith / 2 -> {
                                    scope.launch {
                                        offset.animateTo(contextMenuWith)
                                        onExpanded()
                                    }
                                }

                                else -> {
                                    scope.launch {
                                        offset.animateTo(0f)
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

@Preview
@Composable
private fun WernerPreview() {
    SwipeableItemWithActions(
        isRevealed = false,
        actionsLeft = {
            Box(
                Modifier
                    .size(40.dp)
                    .background(Color.Green)
            )
            Box(
                Modifier
                    .size(40.dp)
                    .background(Color.Red)
            )
            Box(
                Modifier
                    .size(40.dp)
                    .background(Color.Yellow)
            )
        },
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

@Preview
@Composable
private fun Werner2Preview() {
    SwipeableItemWithActions(
        isRevealed = true,
        actionsLeft = {
            Box(
                Modifier
                    .size(40.dp)
                    .background(Color.Green)
            )
            Box(
                Modifier
                    .size(40.dp)
                    .background(Color.Red)
            )
            Box(
                Modifier
                    .size(40.dp)
                    .background(Color.Yellow)
            )
        },
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
