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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon



/*
*  OSS; Il testo di dettaglio partita sia che sia visualizzato a tutto schermo (cioè schiacciando una partita da lista partite)
*       oppure in formato pop-up (cioè quando il giocatore preme un pulsante sbagliato durante una partita),
*       ha sempre la stessa dimensione; la scelta è dovuta al fatto che un testo più piccolo di cosi nel pop-up potrebbe
*       risultare difficile da leggere.
* */

@Composable
fun DettaglioPartita(modifier: Modifier = Modifier, sequenzaUtente: String, sequenzaCorretta: String, onBackToList: () -> Unit
) {
    // Intercetta il tasto back fisico del telefono per tornare alla lista
    BackHandler { onBackToList() }

    val initialColor = colorResource(id = R.color.gray)
    val textDarkGray = colorResource(id = R.color.dark_gray)
    // Calcola il numero di elementi (stessa logica presente in ListaPartite.kt)
    val conteggio = if (sequenzaCorretta.isEmpty()) 0 else { sequenzaCorretta.split(", ").size }
    val conteggio0 = if (sequenzaUtente.isEmpty()) 0 else { sequenzaUtente.split(", ").size }
    val elementiSenzaErrore = if(conteggio0 != 0) sequenzaUtente.split(", ") else emptyList()

    val parteVerde = if (elementiSenzaErrore.isNotEmpty()) {
        elementiSenzaErrore.joinToString(", ") + ", "
    } else {
        ""
    }
    val parteRossa = sequenzaCorretta.removePrefix(parteVerde)

    // Gestisce lo scorrimento verticale se la sequenza finale è troppo lunga
    val scrollState = rememberScrollState()


    // Aggiunta un'altra box per permettere al FAB di poterci stare sopra
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(initialColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Box del Titolo (aggiunto per coerenza con lo stile del MainScreen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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

            // Crea la distanza tra la box del titolo e quella della sequenza
            // Il padding dentro la box per creare lo spazio causa problemi con il FAB; (il FAB si posiziona nel posto sbagliato)
            Box(modifier = Modifier.height(16.dp))

            // Area di testo; mostra la sequenza senza troncamenti
            Box(
                modifier = Modifier
                    // Permette all'area di testo di occupare tutto lo spazio centrale libero
                    .weight(1f)
                    .fillMaxWidth()
                    .border(2.dp, textDarkGray, RoundedCornerShape(12.dp))
                    .background(initialColor, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                // Row gestisce; i punti a sinistra, la linea in mezzo e le sequenze a destra
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Punteggio totale
                    Text(
                        text = stringResource(id = R.string.punti, conteggio - 1),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDarkGray,
                        modifier = Modifier.weight(0.35f), // Prende il 35% dello spazio a sinistra
                        textAlign = TextAlign.Center // Allineato a sinistra come nella lista delle partite
                    )

                    // Linea grigia inserita tra i punti e l'elenco dei bottoni
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .width(1.5.dp)
                            .background(textDarkGray)
                    )

                    // Box che gestisce la lista di bottoni premuti
                    Box(
                        modifier = Modifier
                            .weight(0.65f) //  Prende il 65% dello spazio a destra
                            .fillMaxHeight()
                            .verticalScroll(scrollState), // Testo scorribile nel caso sia molto lunga la sequenza
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(
                                    color = colorResource(R.color.dark_green),
                                    fontWeight = FontWeight.Bold
                                )) {
                                    append(parteVerde)
                                }
                                withStyle(style = SpanStyle(
                                    color = colorResource(R.color.red),
                                    fontWeight = FontWeight.Bold
                                )
                                ) {
                                    append(parteRossa)
                                }

                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDarkGray,
                            lineHeight = 34.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                FloatingActionButton(
                    onClick = onBackToList,
                    shape = CircleShape,
                    // Rosso sfumato 0.6; rosso normale è troppo "forte"
                    containerColor = Color.Red.copy(alpha = 0.6f),
                    contentColor = Color.White,
                    // Sfumando il colore si vede un'ombra; la tolgo
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(56.dp)
                        // Aggiungo un bordo scuro coerente con il resto della UI
                        .border(1.dp, textDarkGray, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}