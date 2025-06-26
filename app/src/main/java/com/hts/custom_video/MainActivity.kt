package com.hts.custom_video

import EditScreen
import ImportOptionItem
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hts.custom_video.ui.theme.Custom_videoTheme

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
        // Màn hình chính
        composable("main") {
                MainScreen(
                    navController = navController,
                    name = "Android",
                )
        }

        composable(
            route = "player/{videoUri}",
            arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val videoUri = backStackEntry.arguments?.getString("videoUri")
            EditScreen(videoUri = videoUri!!)
        }

    }
}

@Composable
fun MainScreen(navController : NavController,  name: String) {
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                try {
//                    context.contentResolver.takePersistableUriPermission(
//                        it,
//                        Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    )
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
                .fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Hello ",
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding()
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ImportOptionItem(
                        backgroundColor = Color.Blue,
                        title = "Import from File",
                        onTap = {
                            filePickerLauncher.launch(arrayOf("video/*"))
                        },
                        iconColor = Color.White,
                        textColor = Color.White
                    )
                    Box(modifier = Modifier.width(16.dp))

                    ImportOptionItem(
                        backgroundColor = Color.Blue,
                        title = "Import from Gallery",
                        onTap = {
                            galleryLauncher.launch("video/*")
                        },
                        iconColor = Color.White,
                        textColor = Color.White
                    )

                }
                Text(
                    text = "Hello $name!",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Custom_videoTheme {
        val navController = rememberNavController()
        MainScreen(navController, "Android")
    }
}