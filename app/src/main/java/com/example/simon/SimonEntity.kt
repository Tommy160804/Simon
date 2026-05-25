package com.example.simon

import androidx.room.Entity
import androidx.room.PrimaryKey

// Creo la tabella tabella_partite
@Entity(tableName = "tabella_partite")
data class SimonEntity(
    // Definisco le colonne della tabella

    @PrimaryKey(autoGenerate = true) // L'id viene generato automaticamente, non serve doverlo gestire
    val id: Int = 0,
    val dataPartita: Long, // Mi serve sapere quando ho fatto una partita
    val punteggio: Int,
    val sequenzaUtente: String,
    val sequenzaCorretta: String
)
