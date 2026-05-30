package com.example.simon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.simon.ui.theme.SimonTheme
import androidx.compose.runtime.collectAsState
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp


private enum class Screen {
    SCHERMATA_GIOCO,
    LISTA_PARTITE,
    DETTAGLIO_PARTITA
}

class MainActivity : ComponentActivity() {

    // Istanzio il ViewModel che gestisce i dati di Room in background
    private val viewModel: SimonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimonTheme {
                // All'apertura dell'app viene mostrata la schermata ListaPartite
                var currentScreen by rememberSaveable { mutableStateOf(Screen.LISTA_PARTITE) }

                val listaPartite by viewModel.partiteTot.collectAsState()

                // Memorizza la sequenza cliccata dall'utente e la sequenza completa per mostrarla nella schermata DettaglioPartita
                var selectedSequence by rememberSaveable { mutableStateOf("") }
                var selectedCompleteSequence by rememberSaveable { mutableStateOf("") }

                var popUp by rememberSaveable { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Se il flag è vero il pop-up si sovrappone a SchermataGioco
                    if (popUp) {
                        Dialog(
                            // Gestione tasto back
                            onDismissRequest = {
                                popUp = false
                                currentScreen = Screen.LISTA_PARTITE
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
                                    currentScreen = Screen.LISTA_PARTITE
                                }
                            )
                        }
                    }

                    when (currentScreen) {
                        Screen.SCHERMATA_GIOCO -> SchermataGioco(
                            modifier = Modifier.padding(innerPadding),
                            onNavigateToSecondScreen = { nuovaSequenza, nuovaSequenzaCompleta, causaUscita ->
                                // Questo if gestisce il caso limite in cui l'utente preme Avvia Partita e subito dopo preme Fine Partita
                                if (nuovaSequenzaCompleta.isNotEmpty()) {
                                    // Salvo la partita nel DB
                                    viewModel.salvaPartita(nuovaSequenza, nuovaSequenzaCompleta)
                                }

                                // Se il gioco finisce perchè il giocatore ha premuto un tasto errato,
                                // allora mostro il pop-up sopra SchermataGioco
                                if (causaUscita == MotivoUscita.ERRORE) {
                                    selectedSequence = nuovaSequenza
                                    selectedCompleteSequence = nuovaSequenzaCompleta
                                    popUp = true
                                }
                                // Se invece il giocatore ha schiacciato il tasto back oppure Fine Partita vado direttamente alla schermata Gioco
                                else {
                                    currentScreen = Screen.LISTA_PARTITE
                                }
                            },
                            onBack = { currentScreen = Screen.LISTA_PARTITE }
                        )
                        Screen.LISTA_PARTITE -> ListaPartite(
                            modifier = Modifier.padding(innerPadding),
                            partite = listaPartite,
                            newGame = { currentScreen = Screen.SCHERMATA_GIOCO },
                            // Gestisco il click su una partita e la navigazione alla schermata DettaglioPartita
                            onMatchClick = { sequenza, sequenzaCompleta ->
                                selectedSequence = sequenza
                                selectedCompleteSequence = sequenzaCompleta
                                currentScreen = Screen.DETTAGLIO_PARTITA
                            }
                        )
                        Screen.DETTAGLIO_PARTITA -> DettaglioPartita(
                            modifier = Modifier.padding(innerPadding),
                            sequenzaUtente = selectedSequence,
                            sequenzaCorretta = selectedCompleteSequence,
                            onBackToList = { currentScreen = Screen.LISTA_PARTITE }
                        )
                    }
                }
            }
        }
    }
}