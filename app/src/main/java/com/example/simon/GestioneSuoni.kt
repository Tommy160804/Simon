package com.example.simon

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.sin
import kotlin.math.PI

/*
*  In GestioneSuoni.kt vengono usati i thread al posto delle coroutines;
*  probabilmente l'uso delle coroutines è meglio ma ho preferito usare i thread per imparare a usarli
*  Il risultato è equivalente.
* */

class SoundManager {

    // Frequenze in Hz associate ai 6 bottoni di gioco
    private val frequenze = doubleArrayOf(415.30, 311.13, 246.94, 209.30, 164.81, 130.81)

    private val frequenzaErrore = 80.0

    private val sampleRate = 44100

    // Uso AudioTrack il quale accetta un flusso continuo di byte.
    // audioTrack può essere null; ad esempio se all'inizio della partita la scheda audio non è ancora pronta il gioco deve potere
    // funzionare lo stesso anche senza audio.
    private var audioTrack: AudioTrack? = null

    private val audioLock = Any()

    init {
        try {
            // Calcola la dimensione minima del buffer hardware richiesta dal sistema operativo Android per non fare saltare l'audio
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            // Inizializzo audioTrack
            // Con la versione attuale di Android funziona, quindi ora come ora va bene
            // OSS; uso MODE_STREAM e non MODE_STATIC in quanto MODE_STATIC causava problemi
            @Suppress("DEPRECATION")
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize,
                // Al posto di dare un file audio, do un flusso di byte
                AudioTrack.MODE_STREAM
            )

            // Faccio partire il canale audio
            audioTrack?.play()

            /*
            *  Questo blocco di codice serve a evitare un grosso problema;
            *  ogni volta che il canale dell'audio non viene usato per qualche secondo questo viene messo in standby;
            *  al riavvio i primi bottoni cliccati si sentono male (audio distorto);
            *  --> Vi sono due situazioni in cui ciò capita;
            *      1) All'avvio di ogni partita
            *      2) Se l'utente durante il suo turno aspetta qualche secondo a premere un bottone
            *  Per evitare che il canale audio vada in standby ho creato un thread che invia un piccolo blocco audio silenzioso ogni 50ms,
            *  cosi facendo si risolve sia il problema 1) che 2).
            * */
            thread {
                // array di 512 byte inizializzati a zero che corrispondono al silenzio assoluto
                val heartbeatBuffer = ByteArray(512)
                // Blocco try/catch che prende eventuali errori evitando crash
                // Ad esempio se viene staccata per un qualche motivo la scheda audio, il gioco continua a funzionare senza audio ma almeno non crasha
                try {
                    while (true) {
                        // è necessario sincronizzare il suono del silenzio assoluto con quello dei bottoni; cioè o viene riprodotto uno o l'altro,
                        // ma mai contemporaneamente (cosi facendo si crea una colonna di suoni e non una sovrapposizione).
                        synchronized(audioLock) {
                            audioTrack?.write(heartbeatBuffer, 0, heartbeatBuffer.size)
                        }
                        // Invio i 512 byte ogni 50ms per non tenere sveglio il canale dell'audio
                        Thread.sleep(50)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun suonoBottone(buttonIndex: Int) {
            generatoreOnda(frequenze[buttonIndex],300,false)
    }

    fun suonoErrore() {
        generatoreOnda(frequenzaErrore,600,true)
    }

    // OSS; Per scrivere questa funzione mi sono aiutato con l'AI; mi ha consigliato di sfumare il suono dei bottoni e mi ha consigliato le formule per le onde
    private fun generatoreOnda(frequency: Double, durataMs: Int, isError: Boolean) {
        // Generazione matematica dell'audio in un thread; il thread viene eseguito in background per non bloccare l'app
        thread {
            // Calcolo il numero totale di campioni necessari per coprire la durata in millisecondi
            // OSS; Se la durata è un 1s allora numero di campioni è esattamente sampleRate (ovvero 44100 campioni in un secondo)
            val numCampioni = (durataMs * sampleRate / 1000)

            // Memorizzo qui il valore dell'onda
            val sample = DoubleArray(numCampioni)

            // Vi è il x2 perchè ogni campione è formato da 2 byte
            val datiSuono = ByteArray(2 * numCampioni)

            // Suggerimento dell'AI per sfumare il suono all'inizio e alla fine ed evitare fastidiosi rumori che a volte si verificavano prima
            val campioniSfumati = 441

            // Genero matematicamente ogni singolo campione dell'onda
            for (i in 0 until numCampioni) {
                if (isError) {
                    // Onda quadra che alterna valori sopra e sotto lo 0 per l'errore
                    sample[i] = if (sin(2 * PI * i / (sampleRate / frequency)) > 0) 0.4 else -0.4
                } else {
                    // Onda sinusoidale per tutti i bottoni
                    sample[i] = sin(2 * PI * i / (sampleRate / frequency))
                }

                // Rampa di volume crescente nei primi 441 campioni del suono
                if (i < campioniSfumati) { sample[i] *= (i.toDouble() / campioniSfumati) }
                // Rampa di volume decrescente negli ultimi 441 campioni del suono
                else if (i > numCampioni - campioniSfumati) { sample[i] *= ((numCampioni - i).toDouble() / campioniSfumati) }
            }

            var indice = 0
            // Converte i valori nell'equivalente formato intero a 16-bit
            for (i in sample) {
                val valShort = (i * 32767).toInt() // normalizzazione a 16 bit
                // Spezzo il valore short a 16 bit in 2 byte (formato Little Endian per AudioTrack)
                datiSuono[indice++] = (valShort and 0x00ff).toByte()
                datiSuono[indice++] = ((valShort and 0xff00) ushr 8).toByte()
            }

            try {
                // audioTrack è una risorsa condivisa, perciò è necessario gestire la concorrenza
                synchronized(audioLock) {
                    // Tutti i byte generati vengono spediti all'hardware che li trasforma in suono
                    // audiotrack può essere null; previene il crash dell'app se per un qualche motivo è proprio null
                    audioTrack?.write(datiSuono, 0, datiSuono.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}