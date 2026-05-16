package com.example.labtask4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.labtask4.features.CalculatorForm
import com.example.labtask4.features.GuessNumberGame
import com.example.labtask4.features.RomanCalculatorForm
import com.example.labtask4.features.SubtractionForm

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("menu") }

    when (currentScreen) {
        "menu" -> MenuScreen(
            onNavigateToSubtraction = { currentScreen = "subtraction" },
            onNavigateToGuessGame = { currentScreen = "guess_game" },
            onNavigateToCalculator = { currentScreen = "calculator" },
            onNavigateToRomanCalculator = { currentScreen = "roman_calculator" }
        )
        "subtraction" -> SubtractionForm(onBack = { currentScreen = "menu" })
        "guess_game" -> GuessNumberGame(onBack = { currentScreen = "menu" })
        "calculator" -> CalculatorForm(onBack = { currentScreen = "menu" })
        "roman_calculator" -> RomanCalculatorForm(onBack = { currentScreen = "menu" })
    }
}

@Composable
fun MenuScreen(
    onNavigateToSubtraction: () -> Unit,
    onNavigateToGuessGame: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToRomanCalculator: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Task",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onNavigateToSubtraction, modifier = Modifier.fillMaxWidth()) {
            Text("Subtraction Form")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateToGuessGame, modifier = Modifier.fillMaxWidth()) {
            Text("Guess Number Game")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateToCalculator, modifier = Modifier.fillMaxWidth()) {
            Text("6-Operation Calculator")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateToRomanCalculator, modifier = Modifier.fillMaxWidth()) {
            Text("Roman Calculator")
        }
    }
}