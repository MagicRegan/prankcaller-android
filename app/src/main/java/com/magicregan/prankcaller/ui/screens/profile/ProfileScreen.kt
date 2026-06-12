package com.magicregan.prankcaller.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.magicregan.prankcaller.ui.theme.CardBackground
import com.magicregan.prankcaller.ui.theme.Crimson
import com.magicregan.prankcaller.ui.theme.CrimsonDark
import com.magicregan.prankcaller.ui.theme.DarkBackground
import com.magicregan.prankcaller.ui.theme.GoldCredits
import com.magicregan.prankcaller.ui.theme.TextPrimary
import com.magicregan.prankcaller.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val credits by viewModel.credits.collectAsState()
    val isLoggedIn by viewModel.isLoggedInState.collectAsState()
    val loggedOut by viewModel.loggedOut.collectAsState()
    val userEmail = viewModel.userEmail

    // Navigate to login after sign out
    if (loggedOut) {
        onNavigateToLogin()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrimsonDark)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Profile avatar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = CrimsonDark,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = TextPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isLoggedIn) (userEmail ?: "Prankster") else "Not logged in",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = GoldCredits,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$credits credits remaining",
                        color = GoldCredits,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Settings items
            ProfileMenuItem(
                icon = Icons.Filled.Settings,
                title = "Settings",
                subtitle = "App preferences"
            )
            ProfileMenuItem(
                icon = Icons.Filled.Notifications,
                title = "Notifications",
                subtitle = "Manage alerts"
            )
            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.Help,
                title = "Help & FAQ",
                subtitle = "Get support"
            )
            ProfileMenuItem(
                icon = Icons.Filled.Info,
                title = "About",
                subtitle = "Version 1.0"
            )
            if (isLoggedIn) {
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Sign Out",
                    subtitle = "",
                    onClick = { viewModel.logout() }
                )
            } else {
                ProfileMenuItem(
                    icon = Icons.Filled.Person,
                    title = "Log In / Sign Up",
                    subtitle = "Create account or sign in",
                    onClick = onNavigateToLogin
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Crimson,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
