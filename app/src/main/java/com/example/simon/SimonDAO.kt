package com.example.simon

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SimonDAO {
    // Salvo una nuova partita nel database
    @Insert
    suspend fun inserisciPartita(partita: SimonEntity)

    // Uso StateFlow al posto di Flow in quanto StateFlow permette al giocatore di vedere immediatamente
    // in Lista Partite la partita appena conclusa.
    // Prende tutte le partite, ordinate dalla più recente alla più vecchia
    @Query("SELECT * FROM tabella_partite ORDER BY dataPartita DESC")
    fun getPartite(): Flow<List<SimonEntity>>

}