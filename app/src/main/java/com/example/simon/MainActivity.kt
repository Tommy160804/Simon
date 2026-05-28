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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.simon.ui.theme.SimonTheme
import androidx.compose.runtime.collectAsState
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp


class MainActivity : ComponentActivity() {

    // Istanzio il ViewModel che gestisce i dati di Room in background
    private val viewModel: SimonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimonTheme {
                // All'apertura dell'app viene mostrata la schermata ListaPartite
                var currentScreen by rememberSaveable { mutableIntStateOf(2) }

                // Se i dati nel DB cambiano allora la UI si aggiorna automaticamente
                val listaPartite by viewModel.partiteTot.collectAsState()

                // Lista di tutte le sequenze del giocatore; si aggiorna ogni volta che viene fatta un'altra partita
                // Estraggo dal DB solo la colonna sequenzaUtente
                val matchesHistory = remember(listaPartite) { listaPartite.map { it.sequenzaUtente } }
                // Lista di tutte le sequenze complete generate dal PC
                // Estraggo dal DB solo la colonna sequenzaCorretta
                val completeMatchHistory = remember(listaPartite) { listaPartite.map { it.sequenzaCorretta } }


                // Memorizza la sequenza cliccata dall'utente e la sequenza completa per mostrarla nella schermata DettaglioPartita
                var selectedSequence by rememberSaveable { mutableStateOf("") }
                var selectedCompleteSequence by rememberSaveable { mutableStateOf("") }

                // Variabile booleana per gestire il pop-up
                var popUp by rememberSaveable { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Se il flag è vero il pop-up si sovrappone a SchermataGioco
                    if (popUp) {
                        Dialog(
                            // Gestione tasto back
                            onDismissRequest = {
                                popUp = false
                                currentScreen = 2
                            },
                            properties = DialogProperties(
                                dismissOnBackPress = true,
                                // Se schiaccio qualcosa fuori dal pop up non succede nulla
                                dismissOnClickOutside = false
                            )
                        ) {
                            // Riutilizzo DettaglioPartita per il pop up, modificandolo leggermente
                            DettaglioPartita(
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .height(600.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                sequenzaUtente = selectedSequence,
                                sequenzaCorretta = selectedCompleteSequence,
                                // Gestione caso giocatore che preme il FAB del pop-up
                                onBackToList = {
                                    popUp = false
                                    currentScreen = 2
                                }
                            )
                        }
                    }

                    when (currentScreen) {
                        1 -> SchermataGioco(
                            modifier = Modifier.padding(innerPadding),
                            onNavigateToSecondScreen = { nuovaSequenza, nuovaSequenzaCompleta, causaUscita ->
                                // Questo if serve per gestire il caso limite in cui l'utente preme Avvia Partita e subito dopo preme Fine Partita
                                if (nuovaSequenzaCompleta.isNotEmpty()) {
                                    // Salvo la partita nel DB
                                    viewModel.salvaPartita(nuovaSequenza, nuovaSequenzaCompleta)
                                }

                                // Se il gioco finisce perchè il giocatore ha premuto un tasto errato,
                                // allora mostro il pop-up sopra SchermataGioco
                                if (causaUscita == "errore") {
                                    selectedSequence = nuovaSequenza
                                    selectedCompleteSequence = nuovaSequenzaCompleta
                                    popUp = true
                                }
                                // Se invece il giocatore ha schiacciato il tasto back oppure Fine Partita vado direttamente alla schermata Gioco
                                else {
                                    currentScreen = 2
                                }
                            },
                            onBack = { currentScreen = 2 }
                        )
                        2 -> ListaPartite(
                            modifier = Modifier.padding(innerPadding),
                            partite = matchesHistory, // Passa tutta la cronologia a ListaPartite
                            partiteComplete = completeMatchHistory, // Passa le partite complete a ListaPartite
                            newGame = { currentScreen = 1 },
                            // Gestisce il click su una partita e la navigazione alla schermata DettaglioPartita
                            onMatchClick = { sequenza, sequenzaCompleta ->
                                selectedSequence = sequenza
                                selectedCompleteSequence = sequenzaCompleta
                                currentScreen = 3 // Per la cronologia normale usiamo ancora la schermata intera
                            }
                        )
                        3 -> DettaglioPartita(
                            modifier = Modifier.padding(innerPadding),
                            sequenzaUtente = selectedSequence,
                            sequenzaCorretta = selectedCompleteSequence,
                            onBackToList = { currentScreen = 2 }
                        )
                    }
                }
            }
        }
    }
}