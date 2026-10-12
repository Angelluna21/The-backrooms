package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.backrooms.ui.GameScreen
import com.example.backrooms.ui.MainMenuScreen
import com.example.backrooms.ui.TerminalScreen
import com.example.backrooms.util.HapticHelper
import com.example.backrooms.viewmodel.GameViewModel
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    MAIN_MENU,
    GAME,
    TERMINAL
}

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val hapticHelper = HapticHelper(this)
        gameViewModel.setHapticHelper(hapticHelper)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }

                    when (currentScreen) {
                        AppScreen.MAIN_MENU -> {
                            MainMenuScreen(
                                onStartGame = { levelId, difficulty ->
                                    gameViewModel.startLevel(levelId, difficulty)
                                    currentScreen = AppScreen.GAME
                                },
                                onOpenTerminal = {
                                    currentScreen = AppScreen.TERMINAL
                                }
                            )
                        }
                        AppScreen.GAME -> {
                            BackHandler {
                                gameViewModel.pauseGame()
                            }
                            GameScreen(
                                viewModel = gameViewModel,
                                onNavigateMainMenu = {
                                    currentScreen = AppScreen.MAIN_MENU
                                }
                            )
                        }
                        AppScreen.TERMINAL -> {
                            BackHandler {
                                currentScreen = AppScreen.MAIN_MENU
                            }
                            TerminalScreen(
                                onNavigateBack = {
                                    currentScreen = AppScreen.MAIN_MENU
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
