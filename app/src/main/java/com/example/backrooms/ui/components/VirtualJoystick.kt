package com.example.backrooms.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
    onMove: (x: Float, y: Float) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(size)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                val radius = (size.toPx() / 2f) * 0.8f
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                        val delta = offset - center
                        val dist = hypot(delta.x, delta.y)
                        val clamped = if (dist > radius) {
                            val angle = kotlin.math.atan2(delta.y, delta.x)
                            Offset(cos(angle) * radius, sin(angle) * radius)
                        } else {
                            delta
                        }
                        thumbOffset = clamped
                        onMove(clamped.x / radius, -clamped.y / radius)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = thumbOffset + dragAmount
                        val dist = hypot(newOffset.x, newOffset.y)
                        val clamped = if (dist > radius) {
                            val angle = kotlin.math.atan2(newOffset.y, newOffset.x)
                            Offset(cos(angle) * radius, sin(angle) * radius)
                        } else {
                            newOffset
                        }
                        thumbOffset = clamped
                        onMove(clamped.x / radius, -clamped.y / radius)
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.width / 2f) * 0.8f

            // Outer ring
            drawCircle(
                color = Color(0x66FFFFFF),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color(0x33000000),
                radius = baseRadius,
                center = center
            )

            // Inner thumb stick
            val thumbPos = center + thumbOffset
            drawCircle(
                color = Color(0xAAFFF59D),
                radius = 26.dp.toPx(),
                center = thumbPos
            )
            drawCircle(
                color = Color(0xFFE5C34D),
                radius = 18.dp.toPx(),
                center = thumbPos
            )
        }
    }
}
