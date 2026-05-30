package com.example.simon

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SimonEntity::class], version = 1, exportSchema = false)
abstract class SimonDatabase : RoomDatabase() {

    abstract fun simonDao(): SimonDAO

    companion object {
        // @Volatile perchè questa variabile deve essere subito visibile a tutte le coroutines
        @Volatile
        private var database: SimonDatabase? = null

        fun getDatabase(context: Context): SimonDatabase {
            // La sincronizzazione serve nel caso due parti dell'app richiedano contemporaneamente il DB
            return database ?: synchronized(this) {
                // Creo il DB
                val istanzaDB = Room.databaseBuilder(
                    context.applicationContext,
                    SimonDatabase::class.java,
                    "simon_database"
                ).build()
                database = istanzaDB
                istanzaDB
            }
        }
    }
}