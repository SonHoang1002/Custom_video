import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.compose.rememberNavController
import com.hts.custom_video.ViewModel.MyViewModel
import com.hts.custom_video.components.BuildVideoThumbnails
import compose.icons.FeatherIcons
import compose.icons.feathericons.PauseCircle
import compose.icons.feathericons.PlayCircle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(UnstableApi::class)
@Composable
fun EditScreen(myViewModel: MyViewModel) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var thumbnails by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var durations by remember { mutableLongStateOf(0L) }
    val configuration = LocalConfiguration.current
    LaunchedEffect(myViewModel.getVideoUriValue) {
        exoPlayer.setMediaItem(MediaItem.fromUri(myViewModel.getVideoUriValue))
        exoPlayer.setPlaybackSpeed(5f)
        exoPlayer.prepare()
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            thumbnails?.forEach { it.recycle() }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            BuildHeading()
            BuildCustomPlayer(exoPlayer = exoPlayer)
            BuildVideoThumbnails(
                myViewModel = myViewModel,
                exoPlayer,
                thumbnails = thumbnails,
                durations = durations,
                setThumbnails = { thumbnails = it },
                setDurations = { durations = it },
            )
        }
    }
}

@Composable
fun BuildHeading() {
    val navController = rememberNavController()
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
//            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .size(50.dp)
            .background(Color.Black),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    modifier = Modifier
                        .size(24.dp),
                    contentDescription = "Home Icon", tint = Color.Red
                )
            }
            Text(
                "Video Speed", fontSize = TextUnit(20f, TextUnitType.Sp),
                color = Color.Red,
                modifier = Modifier
            )
        }
        IconButton(
            onClick = {
                navController.clearBackStack<Unit>()
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                modifier = Modifier
                    .size(24.dp), tint = Color.Red,
                contentDescription = "Done"
            )
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(UnstableApi::class)
@Composable
fun BuildCustomPlayer(exoPlayer: ExoPlayer) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val videoSize = Size(screenWidth.value, screenWidth.value)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .width(videoSize.width.dp)
            .height(videoSize.height.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
            Surface(modifier = Modifier.aspectRatio(16f / 9f)) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(16f / 9f)
                )
                BuildVideoOverlay(exoPlayer, videoSize)
            }

    }
}

@Composable
fun BuildVideoOverlay(exoPlayer: ExoPlayer, videoSize: Size) {
    val isPlaying = remember { mutableStateOf(exoPlayer.isPlaying) }
    val showButton = remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Lắng nghe thay đổi trạng thái từ ExoPlayer
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying.value = isPlayingNow
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    isPlaying.value = false
                    showButton.value = true
                }
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    Box(
        modifier = Modifier
            .width(videoSize.width.dp)
            .height(videoSize.height.dp)
            .background(Color.Transparent)
            .clickable {
                showButton.value = !showButton.value
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showButton.value,
            enter = fadeIn(tween(300)) + scaleIn(tween(300)),
            exit = fadeOut(tween(300)) + scaleOut(tween(300))
        ) {
            Icon(
                imageVector = if (isPlaying.value) FeatherIcons.PauseCircle else FeatherIcons.PlayCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                    .padding(12.dp)
                    .clickable {
                        if (isPlaying.value) {
                            exoPlayer.pause()
                        } else {
                            Log.d("BuildVideoOverlay", "BuildVideoOverlay: currentPosition = ${exoPlayer.currentPosition}, duration = ${exoPlayer.duration}")
                            if (exoPlayer.currentPosition >= exoPlayer.duration) {
                                exoPlayer.seekTo(0L)
                            }
                            exoPlayer.play()
                        }

                        isPlaying.value = exoPlayer.isPlaying

                        // Tự động ẩn nút sau 1.5s nếu đang phát
                        if (exoPlayer.isPlaying) {
                            coroutineScope.launch {
                                delay(1500)
                                showButton.value = false
                            }
                        }
                    }
            )
        }
    }
}

@Composable
fun VideoSeekBar(
    thumbnails: List<Bitmap>,
    duration: Long,
    onSeekChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragEnd by remember { mutableStateOf<Offset?>(null) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragStart = offset
                        },
                        onDrag = { change, _ ->
                            dragEnd = change.position
                        },
                        onDragEnd = {
                            dragStart?.let { start ->
                                dragEnd?.let { end ->
                                    val startPos = minOf(start.x, end.x)
                                    val startTime = (startPos / size.width) * duration
                                    onSeekChanged(startTime.toLong())
                                }
                            }
                            dragStart = null
                            dragEnd = null
                        }
                    )
                }
        ) {
            // Tính toán kích thước mỗi thumbnail
            val thumbnailWidth = size.width / thumbnails.size
            val thumbnailHeight = size.height

            // Vẽ từng thumbnail
            thumbnails.forEachIndexed { index, bitmap ->
                val left = index * thumbnailWidth

                // Đảm bảo bitmap không null và chưa bị recycle
                if (!bitmap.isRecycled) {
                    val imageBitmap = bitmap.asImageBitmap()

                    // Tính toán tỉ lệ khung hình
                    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    val drawHeight = thumbnailHeight
                    val drawWidth = drawHeight * aspectRatio

                    // Căn giữa theo chiều ngang nếu cần
                    val xOffset = left + (thumbnailWidth - drawWidth) / 2

                    drawImage(
                        image = imageBitmap,
                        srcSize = IntSize(bitmap.width, bitmap.height),
                        dstSize = IntSize(drawWidth.toInt(), drawHeight.toInt()),
                        dstOffset = IntOffset(xOffset.toInt(), 0)
                    )
                }

                // Vẽ đường phân cách giữa các thumbnail
                if (index > 0) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.5f),
                        start = Offset(left.toFloat(), 0f),
                        end = Offset(left.toFloat(), size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // Vẽ vùng chọn
            dragStart?.let { start ->
                dragEnd?.let { end ->
                    val left = minOf(start.x, end.x).coerceIn(0f, size.width)
                    val right = maxOf(start.x, end.x).coerceIn(0f, size.width)

                    // Vẽ nền vùng chọn
                    drawRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = Offset(left, 0f),
                        size = Size(right - left, size.height)
                    )

                    // Vẽ handle
                    val handleWidth = 4.dp.toPx()
                    listOf(left, right).forEach { x ->
                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(x - handleWidth / 2, 0f),
                            size = Size(handleWidth, size.height)
                        )
                    }
                }
            }
        }
    }
}