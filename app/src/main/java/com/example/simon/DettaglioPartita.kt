package com.example.simon

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DettaglioPartita(modifier: Modifier = Modifier, sequenza: String, onBackToList: () -> Unit
) {
    // Intercetta il tasto back fisico del telefono per tornare alla lista
    BackHandler { onBackToList() }

    val initialColor = colorResource(id = R.color.gray)
    val textDarkGray = colorResource(id = R.color.dark_gray)
    val gray11 = colorResource(id = R.color.gray1)

    // Calcola il numero di elementi (stessa logica di MatchItem)
    val conteggio = if (sequenza.isEmpty()) 0 else sequenza.split(", ").size

    // Gestisce lo scorrimento verticale se la sequenza finale è troppo lunga
    val scrollState = rememberScrollState()


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(initialColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Box del Titolo (aggiunto per coerenza con lo stile del MainScreen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, textDarkGray, RoundedCornerShape(8.dp))
                .background(initialColor, RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Dettaglio Partita",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textDarkGray
            )
        }

        // Punteggio totale; non si trova nel posto corretto
        Text(
            // ERRORE; QUI DEVO SCRIVERE PUNTEGGIO NON SCORE --> da correggere
            text = stringResource(id = R.string.punti, conteggio),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textDarkGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Area di testo; mostra la sequenza senza troncamenti
        Box(
            modifier = Modifier
                // Permette all'area di testo di occupare tutto lo spazio centrale libero
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(2.dp, textDarkGray, RoundedCornerShape(12.dp))
                // NON VA BENE IL COLORE BIANCO
                // Il testo è troppo piccolo e probabilmente in grassetto è anche più bello
                .background(Color.White, RoundedCornerShape(12.dp)) // Usato il bianco per far risaltare il testo delle sequenze, ma non mi piace ancora, cambierò
                .padding(16.dp)
                .verticalScroll(scrollState) // Testo scorribile nel caso sia molto lunga la sequenza
        ) {
            Text(
                text = sequenza.ifEmpty { stringResource(id = R.string.nessun_rettangolo) },
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = textDarkGray,
                lineHeight = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Questo deve diventare un floating button
        // Box del pulsante inferiore per tornare alla lista delle partite
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, textDarkGray, RoundedCornerShape(12.dp))
                .background(initialColor, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Button(
                onClick = onBackToList,
                colors = ButtonDefaults.buttonColors(containerColor = gray11),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, textDarkGray, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "Torna alla lista",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}