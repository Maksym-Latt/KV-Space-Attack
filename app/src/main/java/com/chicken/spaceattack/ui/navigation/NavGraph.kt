package com.chicken.spaceattack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chicken.spaceattack.audio.AudioController
import com.chicken.spaceattack.ui.game.GameScreen
import com.chicken.spaceattack.ui.game.GameViewModel
import com.chicken.spaceattack.ui.menu.MainMenuScreen
import com.chicken.spaceattack.ui.menu.MenuViewModel
import com.chicken.spaceattack.ui.splash.LoadingScreen

sealed class Destinations(val route: String) {
    data object Loading : Destinations("loading")
    data object Menu : Destinations("menu")
    data object Game : Destinations("game")
}

@Composable
fun AppNavHost(audioController: AudioController, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Destinations.Loading.route) {
        composable(Destinations.Loading.route) {
            LoadingScreen(onFinished = {
                audioController.playMenuMusic()
                navController.navigate(Destinations.Menu.route) {
                    popUpTo(Destinations.Loading.route) { inclusive = true }
                }
            })
        }
        composable(Destinations.Menu.route) {
            val menuViewModel: MenuViewModel = hiltViewModel()
            MainMenuScreen(
                audioController = audioController,
                viewModel = menuViewModel,
                onStart = { navController.navigate(Destinations.Game.route) }
            )
        }
        composable(Destinations.Game.route) {
            val viewModel: GameViewModel = hiltViewModel()
            GameScreen(
                viewModel = viewModel,
                onBackToMenu = {
                    navController.popBackStack(Destinations.Menu.route, inclusive = false)
                }
            )
        }
    }
}
