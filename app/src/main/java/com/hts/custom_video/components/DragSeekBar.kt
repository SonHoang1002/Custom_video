package com.hts.custom_video.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.exoplayer.ExoPlayer
import com.hts.custom_video.Utils.TimerUtils
import com.hts.custom_video.ViewModel.MyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


@SuppressLint("UnrememberedMutableState", "ConfigurationScreenWidthHeight")
@Composable
fun BuildVideoThumbnails(
    myViewModel: MyViewModel,
    exoPlayer: ExoPlayer,
    thumbnails: List<Bitmap>,
    durations: Long,
    setThumbnails: (List<Bitmap>) -> Unit,
    setDurations: (Long) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val thumbWidthDp = 20.dp
    val seekBarHeightDp = 50.dp
    val currentThumbWidthDp = 3.dp

    val screenWidthPx = with(density) { screenWidthDp.toPx() }
    val thumbWidthPx = with(density) { thumbWidthDp.toPx() }
    val currentThumbWidthPx = with(density) { currentThumbWidthDp.toPx() }

    val thumbnailWidthPx = screenWidthPx - 2 * thumbWidthPx

    val limitOffsetLeft = 0f
    val limitOffsetRight = screenWidthPx - thumbWidthPx

    // States
    var offsetLeft by remember { mutableFloatStateOf(limitOffsetLeft) }
    var offsetRight by remember { mutableFloatStateOf(limitOffsetRight) }
    var currentPositionOffset by remember { mutableFloatStateOf(thumbWidthPx) }

    // Load thumbnails and duration
    LaunchedEffect(myViewModel.getVideoUriValue) {
        withContext(Dispatchers.IO) {
            val metadata = MediaMetadataRetriever()
            try {
                metadata.setDataSource(context, myViewModel.getVideoUriValue.toUri())
                val durationMs = metadata.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )?.toLong() ?: 0L

                val newThumbnails = (0..8).mapNotNull { i ->
                    val timeUs = (durationMs * i / 8) * 1000
                    metadata.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                }

                withContext(Dispatchers.Main) {
                    setDurations(durationMs)
                    setThumbnails(newThumbnails)
                    myViewModel.setTrimRightDurationMs(durationMs)
                }
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                metadata.release()
            }
        }
    }

    // Cập nhật thumb theo vị trí video
    LaunchedEffect(exoPlayer) {
        while (true) {
            val duration = exoPlayer.duration.takeIf { it > 0 } ?: 1L
            val current = exoPlayer.currentPosition
            val ratio = current.toFloat() / duration.toFloat()
            currentPositionOffset =
                thumbWidthPx + (thumbnailWidthPx - currentThumbWidthPx) * minOf(1f, ratio)
            delay(50)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(text = TimerUtils().formatTime(myViewModel.getTrimLeftDurationMsValue))
            Text(text = TimerUtils().formatTime(myViewModel.getBetweenDuration()))
            Text(text = TimerUtils().formatTime(myViewModel.getTrimRightDurationMsValue))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(seekBarHeightDp)
        ) {

            // Thumbnails
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = thumbWidthDp)
            ) {
                thumbnails.forEach {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .width(screenWidthDp / thumbnails.size)
                            .fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Thumb trái
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetLeft.toInt(), 0) }
                    .width(thumbWidthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(Color.Red)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            offsetLeft = (offsetLeft + dragAmount)
                                .coerceIn(
                                    limitOffsetLeft,
                                    offsetRight - thumbWidthPx - currentThumbWidthPx
                                )
                            val newDuration =
                                (offsetLeft) / thumbnailWidthPx * exoPlayer.duration
                            if(exoPlayer.isLoading){
                                exoPlayer.pause()
                            }
                            exoPlayer.seekTo(newDuration.toLong())
                            myViewModel.setTrimLeftDurationMs(newDuration.toLong())
                            Log.d("newDuration", "BuildVideoThumbnails: $newDuration, current: ${exoPlayer.duration}")
                        }
                    }
            )

            // Thumb phải
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetRight.toInt(), 0) }
                    .width(thumbWidthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                    .background(Color.Blue)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            offsetRight = (offsetRight + dragAmount)
                                .coerceIn(
                                    offsetLeft + thumbWidthPx + currentThumbWidthPx,
                                    limitOffsetRight
                                )
                            val ratioRight = (offsetRight - thumbWidthPx) / thumbnailWidthPx
                            val newDuration =
                                ratioRight  * exoPlayer.duration
                            if(exoPlayer.isLoading){
                                exoPlayer.pause()
                            }
                            exoPlayer.seekTo(newDuration.toLong())
                            Log.d("newDuration", "BuildVideoThumbnails: $newDuration, current: ${exoPlayer.duration}, ratioRight = $ratioRight")
                            myViewModel.setTrimRightDurationMs(newDuration.toLong())
                        }
                    }
            )

            // Thumb hiện tại (Green, không tương tác)
            Box(
                modifier = Modifier
                    .offset { IntOffset(currentPositionOffset.toInt(), 0) }
                    .width(currentThumbWidthDp)
                    .fillMaxHeight()
                    .background(Color.Green)
            )
        }
    }
}

