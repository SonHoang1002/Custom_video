package com.hts.custom_video

import EditScreen
import ImportOptionItem
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hts.custom_video.ViewModel.MyViewModel
import com.hts.custom_video.ui.theme.Custom_videoTheme
import kotlinx.coroutines.delay


val TAG = "Main"
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Player : Screen("player/{videoUri}") {
        fun createRoute(videoUri: String) = "player/${Uri.encode(videoUri)}"
    }
}


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Trong NavHost của bạn

        setContent {
            Custom_videoTheme {
                val navController = rememberNavController()
                VideoAppNavHost(navController)
            }
        }
    }
}

@Composable
fun VideoAppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = Modifier.fillMaxSize()
    ) {
        val myViewModel = MyViewModel()
        // Màn hình chính
        composable("main") {
                MainScreen(
                    navController = navController,
                    name = "Android",
                    myViewModel = myViewModel
                )
        }

        composable(
            route = "player/{videoUri}",
//            arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            EditScreen(
                myViewModel = myViewModel,
            )
        }

    }
}

@Composable
fun MainScreen(navController: NavController, name: String, myViewModel: MyViewModel) {
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                try {
                    myViewModel.setVideoUri(uri.toString())
                    navController.navigate(Screen.Player.createRoute(it.toString()))
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Xử lý lỗi tại đây
                }
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                try {
                    myViewModel.setVideoUri(uri.toString())
                    navController.navigate(Screen.Player.createRoute(it.toString()))
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Xử lý lỗi tại đây
                }
            }
        }
    )

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)

        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Hello ",
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding().fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ImportOptionItem(
                        backgroundColor = Color.Blue,
                        title = "Import from File",
                        onTap = {
                            filePickerLauncher.launch(arrayOf("video/*"))
                        },
                        painterId = R.drawable.ic_launcher_background,
                        iconColor = Color.White,
                        textColor = Color.White,
                    )
                    Box(modifier = Modifier.width(16.dp))

                    ImportOptionItem(
                        backgroundColor = Color.Blue,
                        title = "Import from Gallery",
                        onTap = {
                            galleryLauncher.launch("video/*")
                        },
                        painterId = R.drawable.ic_launcher_background,
                        iconColor = Color.White,
                        textColor = Color.White
                    )

                }
//                EffectsDemo()
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    Custom_videoTheme {
//        val navController = rememberNavController()
//        MainScreen(navController, "Android",)
//    }
//}

@OptIn(UnstableApi::class)
@Composable
fun EffectsDemo() {
    val counter = remember { mutableStateOf(0) }
    val context = LocalContext.current


    // LaunchedEffect ~ useEffect với dependency
    LaunchedEffect(key1 = counter.value) {
         Log.d(TAG, "LaunchedEffect triggered by counter change")
        delay(500)
        Toast.makeText(context, "LaunchedEffect: Counter = ${counter.value}", Toast.LENGTH_SHORT).show()
    }

    // DisposableEffect ~ useEffect có cleanup
    DisposableEffect(key1 = counter) {
        Log.d(TAG, "DisposableEffect started with counter = ${counter.value}")
        onDispose {
            Log.d(TAG, "DisposableEffect cleaned up with counter = ${counter.value}")
        }
    }

    // DerivedStateOf giống useMemo
    val derivedMessage by remember {
        derivedStateOf {
            "Derived message from counter: ${counter.value * 10}"
        }
    }

    // SnapshotFlow ~ useEffect + state changes (dòng chảy giá trị)
    LaunchedEffect(Unit) {
        snapshotFlow { counter.value }
            .collect { value ->
                Log.d(TAG, "snapshotFlow observed: $value")
            }
    }

    Column(
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp)
    ) {
        Text("Counter: ${counter.value}")
        Text("Derived: $derivedMessage")

        Button(onClick = { counter.value++ }) {
            Text("Increase")
        }
    }
}
