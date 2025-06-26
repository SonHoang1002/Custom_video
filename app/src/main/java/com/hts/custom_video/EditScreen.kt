import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import com.hts.custom_video.components.DraggableBox
import compose.icons.FeatherIcons
import compose.icons.feathericons.Pause
import compose.icons.feathericons.PauseCircle
import compose.icons.feathericons.Play
import compose.icons.feathericons.PlayCircle
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun EditScreen(videoUri: String) {
    val context = LocalContext.current
//    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var thumbnails by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var durations by remember { mutableStateOf(0L) }
//    var isLoading by remember { mutableStateOf(true) }
//    val configuration = LocalConfiguration.current
//    val screenWidth = configuration.screenWidthDp.dp
//    val screenHeight = configuration.screenHeightDp
//    LaunchedEffect(videoUri) {
//        exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
//        exoPlayer.prepare()
//        isLoading = false
//    }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            exoPlayer.release()
//            thumbnails?.forEach { it.recycle() }
//        }
//    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            buildHeading()
            // Video Player
            buildCustomPlayer(videoUri)
            buildVideoThumbnails(
                videoUrl = videoUri,
                thumbnails = thumbnails,
                durations = durations,
                setThumbnails = { thumbnails = it },
                setDurations = { durations = it },
            )
        }
    }
}

@Composable
fun buildHeading() {
    val navController = rememberNavController()
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 20.dp)
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
                    navController.clearBackStack<Unit>()
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

@OptIn(UnstableApi::class)
@Composable
fun buildCustomPlayer(videoUri: String) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isLoading by remember { mutableStateOf(true) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val videoSize = Size(screenWidth.value, screenWidth.value)
    LaunchedEffect(videoUri) {
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
        exoPlayer.prepare()
        isLoading = false
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .width(videoSize.width.dp)
            .height(videoSize.height.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else {
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
                buildVideoOverlay(exoPlayer, videoSize)
            }
        }
    }
}

@Composable
fun buildVideoOverlay(exoPlayer: ExoPlayer, videoSize: Size) {
    val isPlaying = remember { mutableStateOf(exoPlayer.isPlaying) }
    val showButton = remember { mutableStateOf(true) }

    // Để trigger UI khi trạng thái thay đổi bên ngoài ExoPlayer
    LaunchedEffect(exoPlayer.isPlaying) {
        isPlaying.value = exoPlayer.isPlaying
    }
    // Lần đầu vào thì sẽ hiển thị nút, not play
    // khi bấm vào nút thì play, âẩn đi nút, playing
    // khi đang chạy video ( không hiển thị gì ) -> bấm vào video -> hiển thị nút pause
    //  nếu sau đó bấm pause -> hiển thị nút play
    //  nếu sau 1.5s mà ko bấm -> ẩn nút đi

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
                            isPlaying.value = false
                            exoPlayer.pause()
                        } else {
                            isPlaying.value = true
                            exoPlayer.play()
                        }
                    }
            )
        }
    }
}

@Composable
fun buildVideoThumbnails(
    videoUrl: String,
    thumbnails: List<Bitmap>,
    durations: Long,
    setThumbnails: (List<Bitmap>) -> Unit,
    setDurations: (Long) -> Unit
) {
    val localContext = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    LaunchedEffect(videoUrl) {
        withContext(Dispatchers.IO) {
            val metadata = MediaMetadataRetriever()
            metadata.setDataSource(localContext, videoUrl.toUri())

            // Lấy duration
            val durationMs =
                metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0L
            withContext(Dispatchers.Main) {
                setDurations(durationMs)
            }

            // Lấy thumbnails
            val newThumbnails = (0..8).mapNotNull { i ->
                val timeUs = (durationMs * i / 8) * 1000
                metadata.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            }

            withContext(Dispatchers.Main) {
                setThumbnails(newThumbnails)
            }

            metadata.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 40.dp)
        ) {
            thumbnails.forEach {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .width(screenWidth / 9)
                        .height(100.dp)
                )
            }
        }
        DraggableBox()
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
    val density = LocalDensity.current

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