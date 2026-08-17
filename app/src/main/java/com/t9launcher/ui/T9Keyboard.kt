package com.t9launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class KeySpec(val digit: String, val letters: String)

/** T9 数字键盘（1-9）。点击数字直接输入并实时搜索。 */
@Composable
fun T9Keyboard(
    onDigit: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        KeyboardRow(
            listOf(KeySpec("1", ""), KeySpec("2", "ABC"), KeySpec("3", "DEF")),
            onDigit = onDigit
        )
        KeyboardRow(
            listOf(KeySpec("4", "GHI"), KeySpec("5", "JKL"), KeySpec("6", "MNO")),
            onDigit = onDigit
        )
        KeyboardRow(
            listOf(KeySpec("7", "PQRS"), KeySpec("8", "TUV"), KeySpec("9", "WXYZ")),
            onDigit = onDigit
        )
    }
}

@Composable
private fun KeyboardRow(
    keys: List<KeySpec>,
    onDigit: (Char) -> Unit
) {
    Row(Modifier.fillMaxWidth()) {
        keys.forEach { key ->
            KeyButton(
                digit = key.digit,
                letters = key.letters,
                modifier = Modifier.weight(1f),
                onClick = { onDigit(key.digit[0]) }
            )
        }
    }
}

@Composable
private fun KeyButton(
    digit: String,
    letters: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 3.dp, vertical = 7.dp)
            .height(56.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                style = MaterialTheme.typography.titleLarge
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
