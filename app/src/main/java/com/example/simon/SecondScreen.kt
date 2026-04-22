package com.example.simon

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SecondScreen(modifier: Modifier = Modifier, partite: List<String>, onBack: () -> Unit) {

    // SecondScreen è dichiarato BackHandler, quindi il tasto back al posto di chiudere l'app esegue il codice
    // di onBack che si trova in mainActivity.
    BackHandler { onBack() }

    val initialColor = colorResource(id = R.color.gray)
    val textDarkGray = colorResource(id = R.color.dark_gray)
    val gray11 = colorResource(id = R.color.gray1)


    // I due box e la lista devono essere in colonna sia in modalità portrait che in modalità landscape
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(initialColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Box del Titolo (aggiunto per coerenza con lo stile del MainScreen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Distanza dai bordi dello schermo e dalla lista
                .border(2.dp, textDarkGray, RoundedCornerShape(8.dp)) // Bordo nero
                .background(initialColor, RoundedCornerShape(8.dp)) // Sfondo grigio
                .padding(8.dp), // Spazio interno per il testo
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.partite_concluse),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textDarkGray
            )
        }

        // 2. Lista dinamica
        LazyColumn(
            modifier = Modifier
                // Permette alla lista di prendere tutto lo spazio disponibile tra i due box
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            // Distanza tra ogni elemento della lista
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // items crea un componente MatchItem per ogni stringa che si trova nella lista partite
            items(partite) { sequenza ->
                MatchItem(sequenza)
            }
        }

        // 3. Box del pulsante inferiore
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Distanza dai bordi dello schermo e dalla lista
                .border(2.dp, textDarkGray, RoundedCornerShape(12.dp))
                .background(initialColor, RoundedCornerShape(12.dp))
                .padding(12.dp) // Spazio tra il bottone e la box
        ) {
            Button(
                // Il bottone NUOVA SEQUENZA chiama onBack in MainActivity il quale permette di tornare allo schermo 1
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = gray11),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, textDarkGray, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = stringResource(id = R.string.nuova_sequenza),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MatchItem(sequenza: String) {
    val textDarkGray = colorResource(id = R.color.dark_gray)

    // Calcola il numero di elementi della stringa
    val conteggio = if (sequenza.isEmpty()) 0 else sequenza.split(", ").size

    // Card serve per migliorare l'aspetto della lista;
    // di base crea uno sfondo arrotondato (di colore bianco) per ogni elemento della lista
    Card(
        modifier = Modifier.fillMaxWidth(),
        // Sfumatura che conferisce un aspetto 3D
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sinistra --> Numero rettangoli
            Text(
                text = stringResource(id = R.string.punti, conteggio),
                fontWeight = FontWeight.Bold,
                color = textDarkGray,
                // 30% dello schermo per la parte sinistra
                modifier = Modifier.weight(0.3f)
            )

            // Destra --> Sequenza troncata
            // maxLines = 1 e overflow = Ellipsis gestiscono automaticamente l'indicatore grafico di troncamento (...)
            Text(
                // Gestione del caso in cui l'utente non schiaccia nessun pulsante
                text = sequenza.ifEmpty { stringResource(id = R.string.nessun_rettangolo) },
                color = textDarkGray,
                // 70% dello schermo per la parte destra
                modifier = Modifier.weight(0.7f),
                // Allinea il testo sulla destra del contenitore
                textAlign = Arrangement.End.let { TextAlign.End },
                // Il testo può occupare un'unica riga
                maxLines = 1,
                // Se il testo è troppo lungo aggiunge i 3 puntini di sospensione
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}