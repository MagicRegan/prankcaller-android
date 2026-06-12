package com.magicregan.prankcaller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.magicregan.prankcaller.data.PrankDataSource
import com.magicregan.prankcaller.ui.components.AudioPlayerBar
import com.magicregan.prankcaller.ui.navigation.NavGraph
import com.magicregan.prankcaller.ui.navigation.Screen
import com.magicregan.prankcaller.ui.theme.BottomNavBackground
import com.magicregan.prankcaller.ui.theme.Crimson
import com.magicregan.prankcaller.ui.theme.DarkBackground
import com.magicregan.prankcaller.ui.theme.PrankCallerTheme
import com.magicregan.prankcaller.ui.theme.TextPrimary
import com.magicregan.prankcaller.ui.theme.TextSecondary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        exoPlayer = ExoPlayer.Builder(this).build()

        setContent {
            PrankCallerTheme {
                MainContent(exoPlayer!!)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}

@Composable
private fun MainContent(exoPlayer: ExoPlayer) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    var showAudioPlayer by remember { mutableStateOf(false) }
    var currentPreviewName by remember { mutableStateOf("") }
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var previewProgress by remember { mutableFloatStateOf(0f) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        isPreviewPlaying = false
                        previewProgress = 0f
                    }
                    Player.STATE_READY -> {
                        isPreviewPlaying = exoPlayer.isPlaying
                    }
                    else -> {}
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPreviewPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Progress updater
    if (isPreviewPlaying) {
        DisposableEffect(Unit) {
            var running = true
            scope.launch {
                while (running && exoPlayer.isPlaying) {
                    val duration = exoPlayer.duration.coerceAtLeast(1)
                    previewProgress = exoPlayer.currentPosition.toFloat() / duration.toFloat()
                    delay(100)
                }
            }
            onDispose { running = false }
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, "Prank List", Icons.AutoMirrored.Filled.List),
        BottomNavItem(Screen.Calls.route, "Calls", Icons.Filled.Call),
        BottomNavItem(Screen.Purchase.route, "Purchase", Icons.Filled.ShoppingCart),
        BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        bottomNavItems.any { it.route == dest.route }
    } == true

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            NavGraph(
                navController = navController,
                onPlayPreview = { prankId ->
                    val prank = PrankDataSource.getPrankById(prankId)
                    if (prank != null) {
                        currentPreviewName = prank.name
                        showAudioPlayer = true
                        exoPlayer.stop()
                        exoPlayer.setMediaItem(MediaItem.fromUri(prank.previewUrl))
                        exoPlayer.prepare()
                        exoPlayer.play()
                    }
                }
            )
        }

        // Audio player bar
        AudioPlayerBar(
            isVisible = showAudioPlayer,
            prankName = currentPreviewName,
            isPlaying = isPreviewPlaying,
            progress = previewProgress,
            onPlayPause = {
                if (isPreviewPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            },
            onClose = {
                exoPlayer.stop()
                showAudioPlayer = false
                isPreviewPlaying = false
                previewProgress = 0f
            }
        )

        // Bottom Navigation
        if (showBottomBar) {
            NavigationBar(
                containerColor = BottomNavBackground,
                modifier = Modifier.height(72.dp)
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.route
                    } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Crimson,
                            selectedTextColor = Crimson,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = BottomNavBackground
                        )
                    )
                }
            }
        }
    }
}
