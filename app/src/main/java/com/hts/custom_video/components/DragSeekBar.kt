package com.hts.custom_video.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun DraggableBox(seekBarHeight: Int) {
    var offsetX = remember { mutableStateOf(0f) }
    var offsetStart = remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .width(30.dp)
                .height(seekBarHeight.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .background(Color.Red)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures (
                        onDragStart = {  startOffset ->
                            offsetStart.value = startOffset.x
                        },
                        onHorizontalDrag = { pointerInput, dragAmount ->
                            offsetX.value  = offsetX.value + dragAmount
                        },
                        onDragEnd = {
                            offsetStart.value = 0f
                        }
                    )
                }
        )
    }
}
