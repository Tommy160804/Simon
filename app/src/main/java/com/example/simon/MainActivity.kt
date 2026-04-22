package com.example.simon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
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
                var currentScreen by rememberSaveable { mutableStateOf(1) }

                // Memorizza la lista di tutte le sequenze giocate
                val matchesHistory = rememberSaveable { mutableStateListOf<String>() }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        1 -> MainScreen(
                            modifier = Modifier.padding(innerPadding),
                            onNavigateToSecondScreen = { nuovaSequenza ->
                                // Aggiunge la partita appena finita alla lista (anche se vuota)
                                matchesHistory.add(nuovaSequenza)
                                currentScreen = 2
                            }
                        )
                        2 -> SecondScreen(
                            modifier = Modifier.padding(innerPadding),
                            partite = matchesHistory, // Passa tutta la cronologia al secondScreen
                            onBack = { currentScreen = 1 }
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
