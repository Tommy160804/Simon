package com.example.simon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


enum class MotivoUscita {
    ERRORE,
    FINE
}

@Composable
fun SchermataGioco(modifier: Modifier = Modifier, onNavigateToSecondScreen: (String, String, MotivoUscita) -> Unit, onBack: () -> Unit) {

    // Lista dei 6 colori per i bottoni
    val colors = listOf(
        colorResource(id = R.color.red),
        colorResource(id = R.color.green),
        colorResource(id = R.color.blue),
        colorResource(id = R.color.magenta),
        colorResource(id = R.color.yellow),
        colorResource(id = R.color.cyan)
    )
    // Lista delle lettere associate ai colori per i testi dei bottoni e la sequenza
    val colorNames = listOf(
        stringResource(id = R.string.r),
        stringResource(id = R.string.g),
        stringResource(id = R.string.b),
        stringResource(id = R.string.m),
        stringResource(id = R.string.y),
        stringResource(id = R.string.c)
    )

    val initialColor = colorResource(id = R.color.gray)
    val textDarkGray = colorResource(id = R.color.dark_gray)
    val gray11 = colorResource(id = R.color.gray1)


    // Memorizza la stringa di testo che mostra a schermo la cronologia dei colori premuti dall'utente nel round attuale
    var sequenceText by rememberSaveable { mutableStateOf("") }

    var sequenzaTotale by rememberSaveable { mutableStateOf("") }

    val scrollState = rememberScrollState()

    var statoPartita by rememberSaveable { mutableStateOf(false) }

    // Salva la sequenza del PC in modo che sopravviva alla rotazione e al background
    val giocoSequenza = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { mutableStateListOf<Int>().apply { addAll(it) } }
        )
    ) { mutableStateListOf<Int>() }

    // Tiene traccia dell'indice del bottone attualmente illuminato
    var bottoneIlluminato by rememberSaveable { mutableIntStateOf(-1) }

    // Variabile che indica se è o meno il turno del PC
    var turnoPC by rememberSaveable { mutableStateOf(false) }

    // Variabile per capire se lo schermo è in rotazione durante una partita attiva
    var inRotazione by rememberSaveable { mutableStateOf(false) }

    // Variabili necessarie per salvare il punto in cui si trova la sequenza generata dal PC o dall'utente
    var indiceCorrentePC by rememberSaveable { mutableIntStateOf(0) }
    var indiceCorrenteUtente by rememberSaveable { mutableIntStateOf(0) }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // scope permette di fare operazioni asincrone dentro l'onClick di un bottone
    val scope = rememberCoroutineScope()

    // Inizializzazione del gestore dei suoni generati via codice
    val soundManager = remember { SoundManager() }

    var inPausa by rememberSaveable { mutableStateOf(false) }

    // Memorizza il job attualmente in corso
    var lampeggioUtente by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Flag per ripristinare il turno del giocatore dopo la terminazione forzata del processo
    var turnoBackground by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        // Se la partita è in corso il tasto back si comporta come il bottone FINE PARTITA
        if (statoPartita) {
            statoPartita = false
            val stringaDaPassare = sequenceText

            // Se giocoSequenza.size è 1 e il turnoPC è ancora true,
            // significa che il PC sta ancora facendo il primissimo lampeggio.
            val primaSequenza = (giocoSequenza.size == 1) && turnoPC

            // Salva solo se la lista non è vuota e se il PC ha finito di lampeggiare
            val stringaDaPassare1 = if (giocoSequenza.isNotEmpty() && !primaSequenza) {
                sequenzaTotale
            } else {
                ""
            }

            sequenceText = ""
            sequenzaTotale = ""
            giocoSequenza.clear()
            indiceCorrenteUtente = 0
            indiceCorrentePC = 0
            turnoBackground = false

            onNavigateToSecondScreen(stringaDaPassare, stringaDaPassare1, MotivoUscita.FINE)
        } else {
            // Torno a Lista Partite senza fare niente se la partita non è ancora iniziata
            onBack()
        }
    }

    /*
    * Definizione delle 3 funzioni lambda;
    * 1. Area di testo non editabile
    * 2. Box che racchiude la matrice di bottoni
    * 3. Box che racchiude i pulsanti Cancella e Fine Partita
    * */

    // 1. Area di Testo (Lista)
    val displayBox = @Composable { boxModifier: Modifier ->
        Box(
            modifier = boxModifier
                // Padding necessario affinché la box non sia attaccata al bordo del telefono
                .padding(16.dp)
                .border(2.dp, textDarkGray, RoundedCornerShape(8.dp))
                .background(initialColor, RoundedCornerShape(8.dp))
                // Padding necessario affinché le lettere non siano attaccate al bordo della box
                .padding(8.dp)
                /*
                * Questa riga di codice permette di gestire la lista nel momento in cui diventa molto lunga.
                * La scelta è una questione di estetica.
                * Al posto di rimpicciolire le lettere l'utente scorre col dito verso il basso la lista,
                * così facendo l'utente vede le lettere sempre con la stessa dimensione.
                * (Così facendo le prime lettere non sono più visibili ma sicuramente non sono necessarie all'utente per
                * proseguire il gioco, e in ogni caso è sufficiente scorrere col dito verso l'alto per vedere las prime lettere inserite).
                * */
                .verticalScroll(scrollState)
        ) {
            Text(
                text = sequenceText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textDarkGray,
                lineHeight = 30.sp
            )
        }
    }

    // 2. La box che racchiude la matrice di bottoni
    val gridBox = @Composable { boxModifier: Modifier ->
        /*
        * Al posto di usare Box è stata usata BoxWithConstraints;
        * così facendo l'app si può adattare su dispositivi diversi, essendo che viene calcolata l'altezza disponibile dinamicamente
        * Le altre due box invece sono statiche in quanto anche su dispositivi diversi le dimensioni è meglio che rimangano tali;
        * --> Su un telefono piccolo i bottoni Cancella e Fine Partita potrebbero diventare molto piccoli e scomodi da schiacciare.
        * --> Su un tablet invece il testo potrebbe diventare enorme e quindi non gradevole da vedere per l'occhio di una persona.
        * */
        BoxWithConstraints(
            modifier = boxModifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(2.dp, textDarkGray, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            // Questo calcolo serve per posizionare correttamente i bottoni in qualsiasi dispositivo
            val itemHeight = maxHeight / 3 - 8.dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                // Questa lista non si deve muovere (sono i 6 bottoni)
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize()
            ) {
                items(6) { index ->
                    // Restituisce true solo per il bottone che è illuminato
                    val illuminato: Boolean = (bottoneIlluminato == index)
                    val coloreBottone =
                        if (illuminato) colors[index] else colors[index].copy(alpha = 0.5f)

                    Button(
                        onClick = {
                            /*
                            * Vengono verificate 2 condizioni per fare si che l'utente possa cliccare solo quando è il suo turno;
                            * --> Il gioco deve essere avviato (l'utente non può premere se non ha cliccato "AVVIA PARTITA")
                            * --> Il gioco non deve essere nella fase di riproduzione della sua sequenza
                            * */
                            if (statoPartita && !turnoPC) {
                                // Se l'app era appena tornata dal background, al primo click dell'utente togliamo il flag di "salvato"
                                turnoBackground = false

                                if (index == giocoSequenza[indiceCorrenteUtente]) {
                                    val letter = colorNames[index]
                                    sequenceText = if (sequenceText.isEmpty()) letter else "$sequenceText, $letter"
                                    // Interrompo il lampeggio precedente (se è ancora attivo)
                                    lampeggioUtente?.cancel()

                                    // Riproduco il suono del bottone
                                    soundManager.suonoBottone(index)

                                    // Il bottone selezionato brilla per 400ms
                                    // Uso una coroutine per eseguire l'operazione in background e non bloccare l'app
                                    lampeggioUtente = scope.launch {
                                        bottoneIlluminato = index
                                        delay(400)
                                        bottoneIlluminato = -1
                                    }

                                    indiceCorrenteUtente++

                                    // Questo if viene eseguito si l'utente ha digitato l'intera sequenza corretta;
                                    // quindi è finita la sequenza del giocatore e tocca di nuovo al PC
                                    if (indiceCorrenteUtente == giocoSequenza.size) {
                                        indiceCorrenteUtente = 0
                                        indiceCorrentePC = 0
                                        turnoPC = true
                                    }
                                }
                                // Il giocatore ha cliccato il bottone sbagliato
                                else {
                                    soundManager.suonoErrore()

                                    statoPartita = false
                                    val stringaFinale = sequenceText
                                    val stringaFinale1 = sequenzaTotale
                                    sequenceText = ""
                                    giocoSequenza.clear()
                                    indiceCorrenteUtente = 0
                                    indiceCorrentePC = 0
                                    turnoBackground = false
                                    onNavigateToSecondScreen(stringaFinale, stringaFinale1, MotivoUscita.ERRORE)
                                }
                            }
                        },

                        // Pongo enabled = true; cosi i colori della matrice
                        // rimangono sempre visibili anche se la partita non è avviata
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(containerColor = coloreBottone),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth()
                            // Uso il colore del bottone per il bordo quando è acceso per fare risaltare di più il bottone
                            .border(
                                width = 1.dp,
                                color = if (illuminato) colors[index] else textDarkGray,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            text = colorNames[index],
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (colors[index] == Color.Yellow || colors[index] == Color.Cyan) textDarkGray else Color.White
                        )
                    }
                }
            }
        }
    }

    // 3. Box dei controlli inferiori
    val controlBox = @Composable { boxModifier: Modifier ->
        Box(
            modifier = boxModifier
                .padding(16.dp)
                .border(2.dp, textDarkGray, RoundedCornerShape(12.dp))
                .background(initialColor, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            // Se l'app subisce una terminazione forzata durante il turno del giocatore, attivo il flag al riavvio per mantenere i progressi
            if (statoPartita && !turnoPC && sequenceText.isNotEmpty() && indiceCorrenteUtente > 0) {
                LaunchedEffect(Unit) {
                    turnoBackground = true
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        statoPartita = true
                        sequenceText = ""
                        giocoSequenza.clear()
                        indiceCorrenteUtente = 0
                        indiceCorrentePC = 0
                        turnoPC = true
                        turnoBackground = false
                    },
                    enabled = !statoPartita,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gray11,
                        disabledContainerColor = initialColor,
                        contentColor = Color.White,
                        disabledContentColor = gray11
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        // Anche il colore del bordo si adatta allo stato enabled/disabled;
                        // Colore più chiaro se il bottone è disattivato
                        .border(
                            width = 1.dp,
                            color = if (!statoPartita) textDarkGray else textDarkGray.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        text = stringResource(id = R.string.avvia_partita),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        // Spazio verticale tra la prima e la seconda riga
                        lineHeight = 18.sp
                    )
                }
                Button(
                    onClick = {
                        inPausa = !inPausa
                    },
                    enabled = turnoPC,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (inPausa) Color.Red.copy(0.7f) else gray11,
                        disabledContainerColor = initialColor,
                        contentColor = Color.White,
                        disabledContentColor = gray11
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .border(
                            width = 1.dp,
                            color = if (inPausa) textDarkGray else textDarkGray.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        text = if (inPausa) stringResource(id = R.string.riprendi) else stringResource(
                            id = R.string.pausa
                        ),
                        // Soluzione per fare stare RIPRENDI in una sola riga
                        fontSize = if (inPausa) 13.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        // Spazio verticale tra la prima e la seconda riga (Nel caso altre lingue andassero a capo)
                        lineHeight = 18.sp
                    )
                }

                Button(
                    onClick = {
                        statoPartita = false
                        val stringaDaPassare = sequenceText

                        // Se giocoSequenza.size è 1 e il turnoPC è ancora true,
                        // significa che il PC sta ancora facendo il primissimo lampeggio.
                        val primaSequenza = (giocoSequenza.size == 1) && turnoPC

                        // Salva solo se la lista non è vuota E se il PC ha finito di lampeggiare (primaSequenza è false)
                        val stringaDaPassare1 = if (giocoSequenza.isNotEmpty() && !primaSequenza) {
                            sequenzaTotale
                        } else {
                            ""
                        }

                        sequenceText = ""
                        sequenzaTotale = ""
                        giocoSequenza.clear()
                        indiceCorrenteUtente = 0
                        indiceCorrentePC = 0
                        turnoBackground = false

                        onNavigateToSecondScreen(stringaDaPassare, stringaDaPassare1, MotivoUscita.FINE)
                    },
                    // Il pulsante è attivo solo se la partita è in corso (cioè dopo avere premuto AVVIA PARTITA)
                    enabled = statoPartita,
                    // I colori di FINE PARTITA sono invertiti rispetto quelli di AVVIA PARTITA
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gray11,
                        disabledContainerColor = initialColor,
                        contentColor = Color.White,
                        disabledContentColor = gray11
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        // Il bordo cambia colore in modo contrario rispetto ad AVVIA PARTITA
                        .border(
                            width = 1.dp,
                            color = if (statoPartita) textDarkGray else textDarkGray.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        text = stringResource(id = R.string.fine_partita),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        // Spazio verticale tra la prima e la seconda riga
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    /*
    * if/else che permettono di scegliere il layout corretto in base all'orientamento
    * */
    if (isLandscape) {
        // .weight(1f) sia alla matrice che alla colonna a dx; così facendo entrambi occupano metà schermo
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(initialColor)
                .then(modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            gridBox(Modifier.weight(1f).fillMaxHeight())

            Column(Modifier.weight(1f).fillMaxHeight()) {
                // .weight(1f) permette alla box con la lista di occupare tutto lo spazio disponibile
                displayBox(Modifier.weight(1f).fillMaxWidth())
                controlBox(Modifier.fillMaxWidth())
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(initialColor)
                .then(modifier),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            gridBox(Modifier.weight(1f).fillMaxWidth())
            displayBox(Modifier.fillMaxWidth().height(130.dp))
            controlBox(Modifier.fillMaxWidth())
        }
    }

    // Gestione rotazione dello schermo (intercetta la rotazione dello schermo)
    LaunchedEffect(isLandscape) {
        if (statoPartita) {
            inRotazione = true
        }
    }

    // Coroutine per gestire la transizione tra il turno giocatore e turno PC
    LaunchedEffect(turnoPC) {

        // if che viene eseguito solo se è il turno del PC e la partita è in corso
        if (turnoPC && statoPartita) {

            // Generazione mossa casuale, solo se il giocatore non sta ruotando lo schermo e se non è
            // appena stato ripristinato il turno del giocatore dopo una terminazione in background
            if (!inRotazione && !turnoBackground) {
                val nuovoIndiceCasuale = Random.nextInt(6)
                // la lettera viene aggiunta alla fine della sequenza
                giocoSequenza.add(nuovoIndiceCasuale)

                val letter = colorNames[nuovoIndiceCasuale]
                sequenzaTotale =
                    if (sequenzaTotale.isEmpty()) letter else "$sequenzaTotale, $letter"

                delay(1200)
            }

            inRotazione = false
            turnoBackground = false

            // Dopo i 1200ms l'area di testo viene ripulita
            sequenceText = ""

            // Il PC dopo che l'area di testo è stata cancellata aspetta altri 800ms in modo tale da dare sufficiente
            // tempo all'utente per capire che sta per iniziare la nuova sequenza generata dal PC
            // (Quindi dal momento in cui l'utente preme l'ultimo bottone al momento in cui inizia una nuova sequenza passano 1200ms + 800ms = 2 secondi)
            delay(800)

            val daSaltare = indiceCorrentePC
            for (i in daSaltare until giocoSequenza.size) {
                val indice = giocoSequenza[i]

                while (inPausa) {
                    // La coroutine controlla ogni 100ms se è stato schiacciato RIPRENDI o meno
                    delay(100)
                }

                // Salvo subito in modo che se il giocatore ruota lo schermo è salvato
                indiceCorrentePC = i + 1

                bottoneIlluminato = indice
                soundManager.suonoBottone(indice)
                delay(500)
                bottoneIlluminato = -1
                // Pausa tra il lampeggio di un bottone e il successivo
                delay(250)
            }

            // Azzero l'indice del PC per il round successivo
            indiceCorrentePC = 0
            turnoPC = false
            inPausa = false
        } else {
            inRotazione = false
        }
    }
}