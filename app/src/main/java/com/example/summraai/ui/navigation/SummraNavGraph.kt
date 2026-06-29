package com.example.summraai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.summraai.ui.screens.AboutScreen
import com.example.summraai.ui.screens.CollectionsScreen
import com.example.summraai.ui.screens.HistoryScreen
import com.example.summraai.ui.screens.HomeScreen
import com.example.summraai.ui.screens.PdfSummaryScreen
import com.example.summraai.ui.screens.ProfileScreen
import com.example.summraai.ui.screens.SettingsScreen
import com.example.summraai.ui.screens.SplashScreen
import com.example.summraai.ui.screens.SummaryScreen
import com.example.summraai.ui.screens.TextSummaryScreen
import com.example.summraai.ui.screens.WebsiteSummaryScreen
import com.example.summraai.ui.screens.YoutubeSummaryScreen

@Composable
fun SummraNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = SummraScreen.Splash.route
    ) {
        composable(SummraScreen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(SummraScreen.Home.route) {
                        popUpTo(SummraScreen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(SummraScreen.Home.route) {
            HomeScreen(
                onNavigateToHistory = { navController.navigate(SummraScreen.History.route) },
                onNavigateToCollections = { navController.navigate(SummraScreen.Collections.route) },
                onNavigateToSettings = { navController.navigate(SummraScreen.Settings.route) },
                onNavigateToProfile = { navController.navigate(SummraScreen.Profile.route) },
                onNavigateToSummary = { summaryId -> navController.navigate(SummraScreen.SummaryResult.createRoute(summaryId)) },
                onNavigateToTextSummary = { navController.navigate(SummraScreen.TextSummary.route) },
                onNavigateToPdfSummary = { navController.navigate(SummraScreen.PdfSummary.route) },
                onNavigateToWebsiteSummary = { navController.navigate(SummraScreen.WebsiteSummary.route) },
                onNavigateToYoutubeSummary = { navController.navigate(SummraScreen.YoutubeSummary.route) }
            )
        }

        composable(SummraScreen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSummary = { summaryId -> navController.navigate(SummraScreen.SummaryResult.createRoute(summaryId)) }
            )
        }

        composable(SummraScreen.Collections.route) {
            CollectionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSummary = { summaryId -> navController.navigate(SummraScreen.SummaryResult.createRoute(summaryId)) }
            )
        }

        composable(SummraScreen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(SummraScreen.About.route) }
            )
        }

        composable(SummraScreen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(SummraScreen.Settings.route) }
            )
        }

        composable(
            route = SummraScreen.SummaryResult.route,
            arguments = listOf(navArgument("summaryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val summaryId = backStackEntry.arguments?.getString("summaryId") ?: ""
            SummaryScreen(
                summaryId = summaryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(SummraScreen.TextSummary.route) {
            TextSummaryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(SummraScreen.PdfSummary.route) {
            PdfSummaryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(SummraScreen.WebsiteSummary.route) {
            WebsiteSummaryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(SummraScreen.YoutubeSummary.route) {
            YoutubeSummaryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(SummraScreen.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
