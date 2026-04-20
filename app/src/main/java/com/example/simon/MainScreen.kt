package com.example.simon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onNavigateToSecondScreen: (String) -> Unit
) {
    val colors = listOf(colorResource(id = R.color.red), colorResource(id = R.color.green), colorResource(id = R.color.blue), colorResource(id = R.color.magenta), colorResource(id = R.color.yellow), colorResource(id = R.color.cyan))
    val colorNames = listOf(stringResource(id = R.string.r), stringResource(id = R.string.g), stringResource(id = R.string.b), stringResource(id = R.string.m), stringResource(id = R.string.y), stringResource(id = R.string.c))

    // Colore sfondo iniziale
    val initialColor = colorResource(id = R.color.gray)

    var containerColor by remember { mutableStateOf(initialColor) }
    var sequenceText by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Column principale con lo sfondo grigio neutro
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(initialColor)
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Area di Testo (Lista)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                // Padding necessario affinché la box non sia attaccata al bordo del telefono
                .padding(16.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                .background(initialColor, RoundedCornerShape(8.dp))
                // Padding necessario affinché le lettere non siano attaccate al bordo della box
                .padding(8.dp)
                /*
                * Questa riga di codice permette di gestire la lista nel momento in cui diventa molto lunga.
                * La scelta è una questione di estetica.
                * Al posto di rimpicciolire le lettere l'utente scorre col dito verso il basso la lista,
                * così facendo l'utente vede le lettere sempre con la stessa dimensione.
                * (Così facendo le prime lettere non sono più visibili ma sicuramente non sono necessarie all'utente per
                * proseguire il gioco, e in ogni caso è sufficiente scorrere col dito verso l'alto per vedere le prime lettere inserite).
                * */
                .verticalScroll(scrollState)
        ) {
            Text(
                text = sequenceText,
                fontSize = 24.sp,
                // Testo in grassetto
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 30.sp
            )
        }

        // 2. La box che racchiude la matrice di bottoni
        /*
        * Al posto di usare Box è stata usata BoxWithConstraints;
        * così facendo l'app si può adattare su dispositivi diversi, essendo che viene calcolata l'altezza disponibile dinamicamente
        * Le altre due box invece sono statiche in quanto anche su dispositivi diversi le dimensioni è meglio che rimangano tali;
        * --> Su un telefono piccolo i bottoni Cancella e Fine Partita potrebbero diventare molto piccoli e scomodi da schiacciare.
        * --> Su un tablet invece il testo potrebbe diventare enorme e quindi non gradevole da vedere per l'occhio di una persona.
        * */
        BoxWithConstraints(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .weight(1f)
                .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                // Il colore cambia leggermente al tocco (opacità 0.3)
                .background(containerColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            // Questo calcolo serve per posizionare correttamente i bottoni in qualsiasi dispositivo
            val itemHeight = maxHeight / 3 - 8.dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                // Questa lista non si deve muovere (sono i 6 bottoni)
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize()
            ) {
                items(6) { index ->
                    Button(
                        onClick = {
                            containerColor = colors[index]
                            val letter = colorNames[index]
                            sequenceText = if (sequenceText.isEmpty()) letter else "$sequenceText, $letter"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors[index]),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth()
                            .border(1.dp, Color.Black, RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            text = colorNames[index],
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            // La lettera all'interno dei bottoni da schiacciare cambia in base al colore del bottone.
                            // (Ad esempio la lettera "Y" sul giallo se è bianca non si legge praticamente).
                            color = if (colors[index] == Color.Yellow || colors[index] == Color.Cyan) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        // 3. Box che racchiude i pulsanti Cancella e Fine Partita
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                .background(initialColor, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        sequenceText = ""
                        containerColor = initialColor
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Text(text = stringResource(id = R.string.cancella), color = Color.White)
                }

                Button(
                    onClick = {
                        val stringaDaPassare = sequenceText
                        sequenceText = ""
                        onNavigateToSecondScreen(stringaDaPassare)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Text(text = stringResource(id = R.string.fine_partita), color = Color.White)
                }
            }
        }
    }
}