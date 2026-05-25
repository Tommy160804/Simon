package com.example.simon

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/*
*  Aggiunto il ViewModel per separare la logica di schermataGioco (che si occupa di disegnare la UI)
*  da quella del salvataggio dati nel DB (compito del ViewModel).
* */
class SimonViewModel(application: Application) : AndroidViewModel(application) {

    // Creo la variabile privata dao per potere fare operazioni sul DB
    private val dao = SimonDatabase.getDatabase(application).simonDao()

    val partiteTot: StateFlow<List<SimonEntity>> = dao.getPartite()
        .stateIn(
            // Rimane attivo in background finché il ViewModel è attivo; ovvero sempre mentre l'app è accesa
            scope = viewModelScope,
            // Interrompe il collegamento col DB se il giocatore mette l'app in background per più di 3 secondi (o se esce dall'app)
            started = SharingStarted.WhileSubscribed(3000),
            // Gestisce la lista vuota all'avvio del DB
            initialValue = emptyList()
        )

    fun salvaPartita(sequenzaUtente: String, sequenzaPC: String) {
        if (sequenzaPC.isNotEmpty()) {
            // Calcolo il punteggio ottenuto dal giocatore nella partita
            val elementi = sequenzaPC.split(", ")
            // Vi è il -1 perchè l'ultimo elemento della sequenza del PC non è stato indovinato dal giocatore
            // (altrimenti non sarebbe finita la partita)
            val punti = if (elementi.isEmpty()) 0 else elementi.size - 1


            // Coroutine che salva i dati della partita nel DB
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