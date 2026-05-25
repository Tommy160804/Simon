package com.example.simon

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SimonEntity::class], version = 1, exportSchema = false)
abstract class SimonDatabase : RoomDatabase() {

    abstract fun simonDao(): SimonDAO

    // Applico il Singleton design pattern; così facendo il DB viene creato un'unica volta
    companion object {
        // Uso @Volatile perchè questa variabile deve essere immediatamente visibile a tutte le coroutines
        @Volatile
        // Salvo il DB in database
        private var database: SimonDatabase? = null

        fun getDatabase(context: Context): SimonDatabase {
            // Se il DB è già stato creato in precedenza allora il codice all'interno delle {} viene saltato
            // La sincronizzazione serve nel caso due parti dell'app richiedano contemporaneamente il DB
            return database ?: synchronized(this) {
                // Creo il DB e lo chiamo simon_database
                val istanzaDB = Room.databaseBuilder(
                    context.applicationContext,
                    SimonDatabase::class.java,
                    "simon_database"
                ).build()
                // Assegno il DB creato alla variabile database
                database = istanzaDB
                // istanzaDB è il risultato di questo blocco di codice
                istanzaDB
            }
        }
    }
}