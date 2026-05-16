package com.example.labtask4.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.io.File
import kotlin.math.pow

@Composable
fun CalculatorForm(onBack: () -> Unit) {
    val context = LocalContext.current
    val historyFile = remember { File(context.filesDir, "calculator_history.txt") }

    var displayExpression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var historyText by remember { mutableStateOf("") }

    val loadHistory = {
        historyText = if (historyFile.exists()) {
            historyFile.readText()
        } else {
            ""
        }
    }

    LaunchedEffect(Unit) {
        loadHistory()
    }

    val deleteHistory = {
        if (historyFile.exists()) {
            historyFile.delete()
            loadHistory()
        }
    }

    val onDigitClick = { digit: String ->
        displayExpression += digit
    }

    val onOperatorClick = { op: String ->
        if (displayExpression.isNotEmpty() && !displayExpression.any { it in setOf('+', '-', '*', '/', '%', '^') }) {
            displayExpression += op
        }
    }

    val onClearClick = {
        displayExpression = ""
        resultText = ""
    }

    val onEqualClick = {
        val operators = charArrayOf('+', '-', '*', '/', '%', '^')
        val operatorIndex = displayExpression.indexOfAny(operators)

        if (operatorIndex > 0 && operatorIndex < displayExpression.length - 1) {
            val operation = displayExpression[operatorIndex].toString()
            val parts = displayExpression.split(operation)

            val num1 = parts[0].toDoubleOrNull()
            val num2 = parts[1].toDoubleOrNull()

            if (num1 != null && num2 != null) {
                var valid = true
                val result = when (operation) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "*" -> num1 * num2
                    "/" -> if (num2 != 0.0) num1 / num2 else { valid = false; Double.NaN }
                    "%" -> if (num2 != 0.0) num1 % num2 else { valid = false; Double.NaN }
                    "^" -> num1.pow(num2)
                    else -> 0.0
                }

                if (valid) {
                    resultText = "= $result"
                    try {
                        historyFile.appendText("$displayExpression = $result\n")
                        loadHistory()
                    } catch (e: Exception) {
                        resultText = "Error saving to file"
                    }
                } else {
                    resultText = "Error: Zero division"
                }
            } else {
                resultText = "Invalid format"
            }
        } else {
            resultText = "Enter expression (e.g. 5+3)"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (historyText.isNotEmpty()) {
                    TextButton(onClick = deleteHistory) {
                        Text("Clear History", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = historyText.ifEmpty { "No history yet" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = displayExpression.ifEmpty { "0" },
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = resultText,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val btnModifier = Modifier.weight(1f).height(54.dp)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onDigitClick("7") }, modifier = btnModifier) { Text("7") }
                Button(onClick = { onDigitClick("8") }, modifier = btnModifier) { Text("8") }
                Button(onClick = { onDigitClick("9") }, modifier = btnModifier) { Text("9") }
                Button(onClick = { onOperatorClick("+") }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("+") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onDigitClick("4") }, modifier = btnModifier) { Text("4") }
                Button(onClick = { onDigitClick("5") }, modifier = btnModifier) { Text("5") }
                Button(onClick = { onDigitClick("6") }, modifier = btnModifier) { Text("6") }
                Button(onClick = { onOperatorClick("-") }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("-") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onDigitClick("1") }, modifier = btnModifier) { Text("1") }
                Button(onClick = { onDigitClick("2") }, modifier = btnModifier) { Text("2") }
                Button(onClick = { onDigitClick("3") }, modifier = btnModifier) { Text("3") }
                Button(onClick = { onOperatorClick("*") }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("*") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onDigitClick("0") }, modifier = btnModifier) { Text("0") }
                Button(onClick = { onDigitClick(".") }, modifier = btnModifier) { Text(".") }
                Button(onClick = { onOperatorClick("^") }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("^") }
                Button(onClick = { onOperatorClick("/") }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("/") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onClearClick() }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("C") }
                Button(onClick = { onOperatorClick("%") }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("%") }
                Button(onClick = { onEqualClick() }, modifier = Modifier.weight(2f).height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Text("=") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Menu")
        }
    }
}