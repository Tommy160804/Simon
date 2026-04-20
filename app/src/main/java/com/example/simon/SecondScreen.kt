package com.example.simon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.ui.res.stringResource

@Composable
fun SecondScreen(modifier: Modifier = Modifier, sequenza: String, onBack: () -> Unit) {
    // Versione prova schermo2 (non inerente alla consegna)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(id = R.string.sequenza))
        Text(
            text = stringResource(id = R.string.dati_salvati, sequenza),
            style = MaterialTheme.typography.displaySmall
        )
        Button(onClick = onBack) {
            Text(text = stringResource(id = R.string.riprova))
        }
    }
}