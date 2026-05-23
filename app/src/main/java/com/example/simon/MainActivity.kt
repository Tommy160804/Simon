package com.example.simon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.simon.ui.theme.SimonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimonTheme {
                // All'apertura dell'app viene mostrata la schermata ListaPartite
                var currentScreen by rememberSaveable { mutableIntStateOf(2) }

                // Memorizza la lista di tutte le sequenze giocate
                val matchesHistory = rememberSaveable { mutableStateListOf<String>() }

                // Memorizza la lista di tutte le sequenze complete
                val completeMatchHistory = rememberSaveable { mutableStateListOf<String>() }

                // Memorizza la sequenza selezionata per mostrarla nella schermata DettaglioPartita
                var selectedSequence by rememberSaveable { mutableStateOf("") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        1 -> SchermataGioco(
                            modifier = Modifier.padding(innerPadding),
                            onNavigateToSecondScreen = { nuovaSequenza, nuovaSequenzaCompleta->
                                // Questo if serve per gestire il caso limite in cui l'utente preme Avvia Partita e subito dopo preme Fine Partita
                                if (nuovaSequenzaCompleta.isNotEmpty()) {
                                    // Aggiunge la partita appena finita alla lista (anche se vuota)
                                    matchesHistory.add(nuovaSequenza)
                                    // Aggiunge la partita completa alla lista
                                    completeMatchHistory.add(nuovaSequenzaCompleta)
                                }
                                currentScreen = 2
                            },
                            onBack = { currentScreen = 2 }
                        )
                        2 -> ListaPartite(
                            modifier = Modifier.padding(innerPadding),
                            partite = matchesHistory, // Passa tutta la cronologia a ListaPartite
                            partiteComplete = completeMatchHistory, // Passa le partite complete a ListaPartite
                            newGame = { currentScreen = 1 },
                            // Gestisce il click su una partita e la navigazione alla schermata DettaglioPartita
                            onMatchClick = { sequenza ->
                                selectedSequence = sequenza
                                currentScreen = 3
                            }
                        )
                        3 -> DettaglioPartita(
                            modifier = Modifier.padding(innerPadding),
                            sequenza = selectedSequence,
                            onBackToList = { currentScreen = 2 }
                        )
                    }
                }
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SimonTheme {
    }
}
*/