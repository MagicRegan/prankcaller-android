package com.magicregan.prankcaller.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.magicregan.prankcaller.ui.screens.call.ActiveCallScreen
import com.magicregan.prankcaller.ui.screens.calls.CallsScreen
import com.magicregan.prankcaller.ui.screens.detail.PrankDetailScreen
import com.magicregan.prankcaller.ui.screens.home.HomeScreen
import com.magicregan.prankcaller.ui.screens.profile.ProfileScreen
import com.magicregan.prankcaller.ui.screens.purchase.PurchaseScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Calls : Screen("calls")
    data object Purchase : Screen("purchase")
    data object Profile : Screen("profile")
    data object PrankDetail : Screen("prank_detail/{prankId}") {
        fun createRoute(prankId: Int) = "prank_detail/$prankId"
    }
    data object ActiveCall : Screen("active_call/{prankId}") {
        fun createRoute(prankId: Int) = "active_call/$prankId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    onPlayPreview: (Int) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onPrankClick = { prankId ->
                    navController.navigate(Screen.PrankDetail.createRoute(prankId))
                },
                onPlayPreview = onPlayPreview
            )
        }

        composable(Screen.Calls.route) {
            CallsScreen()
        }

        composable(Screen.Purchase.route) {
            PurchaseScreen()
        }

        composable(Screen.Profile.route) {
            ProfileScreen()
        }

        composable(
            route = Screen.PrankDetail.route,
            arguments = listOf(navArgument("prankId") { type = NavType.IntType })
        ) {
            PrankDetailScreen(
                onBackClick = { navController.popBackStack() },
                onPlayPreview = onPlayPreview,
                onStartCall = { prankId ->
                    navController.navigate(Screen.ActiveCall.createRoute(prankId))
                }
            )
        }

        composable(
            route = Screen.ActiveCall.route,
            arguments = listOf(navArgument("prankId") { type = NavType.IntType })
        ) {
            ActiveCallScreen(
                onEndCall = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
    }
}
