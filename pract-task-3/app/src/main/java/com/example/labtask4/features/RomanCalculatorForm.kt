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

@Composable
fun RomanCalculatorForm(onBack: () -> Unit) {
    val context = LocalContext.current
    val historyFile = remember { File(context.filesDir, "roman_calculator_history.txt") }

    var displayExpression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var historyText by remember { mutableStateOf("") }

    val loadHistory = {
        historyText = if (historyFile.exists()) historyFile.readText() else ""
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

    val onCharClick = { char: String ->
        displayExpression += char
    }

    val onOperatorClick = { op: String ->
        if (displayExpression.isNotEmpty() && !displayExpression.any { it in setOf('+', '-', '*', '/') }) {
            displayExpression += op
        }
    }

    val onClearClick = {
        displayExpression = ""
        resultText = ""
    }

    val romanToInt = { roman: String ->
        val romanMap = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
        var res = 0
        var i = 0
        while (i < roman.length) {
            val s1 = romanMap[roman[i]] ?: 0
            if (i + 1 < roman.length) {
                val s2 = romanMap[roman[i + 1]] ?: 0
                if (s1 >= s2) {
                    res += s1
                    i++
                } else {
                    res += s2 - s1
                    i += 2
                }
            } else {
                res += s1
                i++
            }
        }
        res
    }

    val intToRoman = { number: Int ->
        val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val romanLetters = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        val roman = StringBuilder()
        var num = number
        var i = 0
        while (num > 0) {
            if (num >= values[i]) {
                roman.append(romanLetters[i])
                num -= values[i]
            } else {
                i++
            }
        }
        roman.toString()
    }

    val onEqualClick = {
        val operators = charArrayOf('+', '-', '*', '/')
        val operatorIndex = displayExpression.indexOfAny(operators)

        if (operatorIndex > 0 && operatorIndex < displayExpression.length - 1) {
            val operation = displayExpression[operatorIndex].toString()
            val parts = displayExpression.split(operation)

            val num1 = romanToInt(parts[0])
            val num2 = romanToInt(parts[1])

            if (num1 > 0 && num2 > 0) {
                val resultInt = when (operation) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "*" -> num1 * num2
                    "/" -> if (num2 != 0) num1 / num2 else -1
                    else -> -1
                }

                when {
                    resultInt == -1 -> resultText = "Error: Division by zero"
                    resultInt <= 0 -> resultText = "Error: Result less than I"
                    else -> {
                        val romanResult = intToRoman(resultInt)
                        resultText = "= $romanResult ($resultInt)"
                        try {
                            historyFile.appendText("$displayExpression = $romanResult\n")
                            loadHistory()
                        } catch (e: Exception) {
                            resultText = "Error saving to file"
                        }
                    }
                }
            } else {
                resultText = "Invalid Roman number"
            }
        } else {
            resultText = "Format: XI+V"
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
                    text = "Roman History:",
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
                Button(onClick = { onCharClick("I") }, modifier = btnModifier) { Text("I") }
                Button(onClick = { onCharClick("V") }, modifier = btnModifier) { Text("V") }
                Button(onClick = { onCharClick("X") }, modifier = btnModifier) { Text("X") }
                Button(onClick = { onOperatorClick("+") }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("+") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onCharClick("L") }, modifier = btnModifier) { Text("L") }
                Button(onClick = { onCharClick("C") }, modifier = btnModifier) { Text("C") }
                Button(onClick = { onCharClick("D") }, modifier = btnModifier) { Text("D") }
                Button(onClick = { onOperatorClick("-") }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("-") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onCharClick("M") }, modifier = btnModifier) { Text("M") }
                Button(onClick = { onClearClick() }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("C") }
                Button(onClick = { onOperatorClick("*") }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("*") }
                Button(onClick = { onOperatorClick("/") }, modifier = btnModifier, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("/") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onEqualClick() }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Text("=") }
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