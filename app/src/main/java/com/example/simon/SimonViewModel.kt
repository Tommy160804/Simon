package com.example.simon

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SimonViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SimonDatabase.getDatabase(application).simonDao()

    // Converto il Flow del database in StateFlow per gestirlo nel ciclo di vita della UI
    val partiteTot: StateFlow<List<SimonEntity>> = dao.getPartite()
        .stateIn(
            scope = viewModelScope,
            // Tengo in vita il flusso se l'app va in background per meno di 3s
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = emptyList()
        )

    fun salvaPartita(sequenzaUtente: String, sequenzaPC: String) {
        if (sequenzaPC.isNotEmpty()) {
            val elementi = sequenzaPC.split(", ")
            val punti = elementi.size - 1


            // Salvo i dati della partita nel DB
            viewModelScope.launch {
                dao.inserisciPartita(
                    SimonEntity(
                        punteggio = punti,
                        dataPartita = System.currentTimeMillis(),
                        sequenzaUtente = sequenzaUtente,
                        sequenzaCorretta = sequenzaPC
                    )
                )
            }
        }
    }
}