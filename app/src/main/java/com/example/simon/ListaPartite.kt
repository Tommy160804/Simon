package com.example.simon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun ListaPartite(modifier: Modifier = Modifier, partite: List<String>, partiteComplete: List<String>, newGame: () -> Unit, onMatchClick: (String, String) -> Unit) {

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
        // 1. Box del Titolo
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

            // MatchItem crea un elemento della lista per ogni partita completata
            itemsIndexed(partite) { index, sequenza ->
                val sequenzaCompleta = partiteComplete[index]
                MatchItem(
                    sequenza = sequenza,
                    sequenza1 = sequenzaCompleta,
                    onClick = { onMatchClick(sequenza, sequenzaCompleta) }
                )
            }
        }

        // 3. Box del pulsante inferiore
        // Il pulsante NUOVA PARTITA cambia schermata (passa a SchermataGioco)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Distanza dai bordi dello schermo e dalla lista
                .border(2.dp, textDarkGray, RoundedCornerShape(12.dp))
                .background(initialColor, RoundedCornerShape(12.dp))
                .padding(12.dp) // Spazio tra il bottone e la box
        ) {
            Button(
                onClick = newGame,
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
// MatchItem definisce l'aspetto grafico e il comportamento di una singola riga della lista
// Ho aggiunto il parametro onClick per gestire la pressione del componente da parte dell'utente
fun MatchItem(sequenza: String, sequenza1: String, onClick: () -> Unit) {
    val textDarkGray = colorResource(id = R.color.dark_gray)

    // Calcola il numero di elementi della stringa
    // Lascio l'if/else per sicurezza ma in teoria non dovrebbe mai essere 0 la sequenza
    // Vi è il -1 perchè la sequenza indovinata sarà sempre pari alla sequenza totale proposta dal PC -1 in quanto l'ultimo elemento sarà errato
    // (altrimenti la partita continuerebbe all'infinito senza errori da parte del giocatore)
    val conteggio = if (sequenza1.isEmpty()) 0 else {(sequenza1.split(", ").size - 1)}

    // Trasformo la stringa di bottoni corretti premuti dal'utente (nell'ultimo giro, cioè quello in cui sbaglia) nel formato con la virgola
    // L'ultimo elemento che l'utente schiaccia è sicuramente errato perciò lo rimuovo
    val elementiSenzaErrore = sequenza.split(", ").dropLast(1)

    // Aggiungo ", " alla fine solo se la lista contiene almeno un elemento indovinato
    val sequenzaCorretta = if (elementiSenzaErrore.isNotEmpty()) {
        elementiSenzaErrore.joinToString(", ") + ", "
    } else {
        ""
    }

    // Sequenza che nell'ultimo giro non è stata indovinata dall'utente
    val sequenzaRimanente = sequenza1.removePrefix(sequenzaCorretta)

    // Card serve per migliorare l'aspetto della lista;
    // di base crea uno sfondo arrotondato (di colore bianco) per ogni elemento della lista
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Ora la card è cliccabile
            .clickable { onClick() },
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
                text = buildAnnotatedString {
                    // Verde la sequenza corretta
                    withStyle(style = SpanStyle(
                        color = colorResource(R.color.dark_green),
                        fontWeight = FontWeight.Bold
                    )
                    ) {
                        append(sequenzaCorretta)
                    }
                    // Rossa la sequenza rimanente/errore
                    withStyle(style = SpanStyle(color = colorResource(R.color.red), fontWeight = FontWeight.Bold)) {
                        append(sequenzaRimanente)
                    }
                },
                // Occupa il 70% dello schermo rimanente
                modifier = Modifier.weight(0.7f),
                // Allinea tutto a destra
                textAlign = TextAlign.End,
                // Gestione del troncamento su un'unica riga
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}