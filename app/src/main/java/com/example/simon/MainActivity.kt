package com.example.simon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.simon.ui.theme.SimonTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        // Schermo2
                        onNavigateToSecondScreen = {
                            println("Schermo 2")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, onNavigateToSecondScreen: () -> Unit
) {
    // Colori dei bottoni
    val colors = listOf(
        colorResource(id = R.color.red),
        colorResource(id = R.color.green),
        colorResource(id = R.color.blue),
        colorResource(id = R.color.magenta),
        colorResource(id = R.color.yellow),
        colorResource(id = R.color.cyan)
    )

    // Lettere dei bottoni (e lettere usate per il testo multi riga non editabile)
    val colorNames = listOf(
        stringResource(id = R.string.r),
        stringResource(id = R.string.g),
        stringResource(id = R.string.b),
        stringResource(id = R.string.m),
        stringResource(id = R.string.y),
        stringResource(id = R.string.c)
    )

    // Il colore iniziale della Box è un colore neutro (grigio)
    val initialColor = colorResource(id = R.color.gray)
    var containerColor by remember { mutableStateOf(initialColor) }
    var sequenceText by remember { mutableStateOf("") }

    // L'intero layout dell'UI principale si trova racchiuso in una Column
    // Gli elementi della Column sono;
    // 1) Area di testo multi riga non editabile che visualizza la sequenza di rettangoli premuti
    // 2) Matrice di bottoni
    // 3) Area pulsanti “Cancella” e “Fine partita”
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Area di Testo che contiene la sequenza che viene digitata
        Text(
            text = sequenceText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 40.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
            )

        // Il rettangolo che racchiude la matrice di bottoni
        Box(
            modifier = Modifier
                .padding(16.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                // Il colore della box è impostato a 0.3 di opacità per non dare fastidio al resto dell'UI
                .background(containerColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(6) { index ->
                    Button(
                        onClick = {
                            containerColor = colors[index]
                            val letter = colorNames[index]
                            // Forse non è necessario l'if qui
                            sequenceText = if (sequenceText.isEmpty()) letter else "$sequenceText, $letter"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors[index]),
                        shape = RectangleShape,
                        modifier = Modifier
                            .height(80.dp)
                            .fillMaxWidth()
                            .border(1.dp, Color.Black)
                    ) {
                        Text(
                            text = colorNames[index],
                            color = if (colors[index] == Color.Yellow || colors[index] == Color.Cyan) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        // Spinge sotto tutto ciò che si trova dopo queste righe di codice, cioè il bottone cancella e fine partita
        Spacer(modifier = Modifier.weight(1f))

        // Area Pulsanti Cancella e Fine Partita
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                // Cancello la sequenza della vecchia partita
                sequenceText = ""
                // Torno ad avere il colore neutro nella box
                containerColor = initialColor
            }) {
                Text(text = stringResource(id = R.string.cancella))
            }

            Button(onClick = { onNavigateToSecondScreen() }) {
                Text(text = stringResource(id = R.string.fine_partita))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SimonTheme {
    }
}