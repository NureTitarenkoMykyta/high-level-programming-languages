package com.example.labtask4.features

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private const val MIN_NUMBER = 1
private const val MAX_NUMBER = 10

@Composable
fun GuessNumberGame(onBack: () -> Unit) {
    var secretHighlight by remember { mutableIntStateOf(Random.nextInt(MIN_NUMBER, MAX_NUMBER + 1)) }
    var userGuess by remember { mutableStateOf("") }
    var hintText by remember { mutableStateOf("I've thought of a number between $MIN_NUMBER and $MAX_NUMBER. Try to guess it!") }
    var attemptsCount by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Guess the Number",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = hintText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Attempts: $attemptsCount",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!isGameOver) {
            OutlinedTextField(
                value = userGuess,
                onValueChange = { userGuess = it },
                label = { Text("Your guess") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val guess = userGuess.toIntOrNull()
                    if (guess == null || guess !in MIN_NUMBER..MAX_NUMBER) {
                        hintText = "Please enter a valid number between $MIN_NUMBER and $MAX_NUMBER"
                        return@Button
                    }

                    attemptsCount++

                    when {
                        guess < secretHighlight -> {
                            hintText = "No, my number is bigger than $guess"
                        }
                        guess > secretHighlight -> {
                            hintText = "No, my number is smaller than $guess"
                        }
                        else -> {
                            hintText = "Congratulations! You guessed it in $attemptsCount attempts!"
                            isGameOver = true
                        }
                    }
                    userGuess = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check")
            }
        } else {
            Button(
                onClick = {
                    secretHighlight = Random.nextInt(MIN_NUMBER, MAX_NUMBER + 1)
                    userGuess = ""
                    hintText = "I've thought of a number between $MIN_NUMBER and $MAX_NUMBER. Try to guess it!"
                    attemptsCount = 0
                    isGameOver = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Play Again")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Menu")
        }
    }
}