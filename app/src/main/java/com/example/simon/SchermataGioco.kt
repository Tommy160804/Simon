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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SchermataGioco(modifier: Modifier = Modifier, onNavigateToSecondScreen: (String) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }

    // Lista dei 6 colori per i bottoni
    val colors = listOf(colorResource(id = R.color.red), colorResource(id = R.color.green), colorResource(id = R.color.blue), colorResource(id = R.color.magenta), colorResource(id = R.color.yellow), colorResource(id = R.color.cyan))
    // Lista delle lettere associate ai colori per i testi dei bottoni e la sequenza
    val colorNames = listOf(stringResource(id = R.string.r), stringResource(id = R.string.g), stringResource(id = R.string.b), stringResource(id = R.string.m), stringResource(id = R.string.y), stringResource(id = R.string.c))

    val initialColor = colorResource(id = R.color.gray)
    val textDarkGray = colorResource(id = R.color.dark_gray)
    val gray11 = colorResource(id = R.color.gray1)

    // Memorizza l'intero (ARGB) del colore di sfondo del container principale
    var containerColorArgb by rememberSaveable { mutableIntStateOf(initialColor.toArgb()) }

    // Converte il valore intero salvato in un oggetto 'Color' di Compose
    val containerColor = Color(containerColorArgb)

    // Memorizza la stringa di testo che mostra a schermo la cronologia dei colori premuti dall'utente nel round attuale
    var sequenceText by rememberSaveable { mutableStateOf("") }

    // Gestisce lo stato dello scorrimento verticale per l'area di testo
    val scrollState = rememberScrollState()

    // Variabile booleana per lo stato del gioco: 'true' se la partita è in corso
    var statoPartita by rememberSaveable { mutableStateOf(false) }

    // Memorizza la sequenza di indici (0..5) generata dal PC
    val giocoSequenza = remember { mutableStateListOf<Int>() }

    // Tiene traccia dell'indice del bottone attualmente "illuminato"
    // -1 significa che non vi è nessun bottone illuminato (ad esempio a inizio partita)
    var bottoneIlluminato by rememberSaveable { mutableIntStateOf(-1) }

    // Variabile di stato che indica se è o meno il turno del PC
    var turnoPC by rememberSaveable { mutableStateOf(false) }

    // Tiene traccia della posizione corrente che l'utente deve indovinare
    var utenteIndiceCorrente by rememberSaveable { mutableIntStateOf(0) }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

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
                * proseguire il gioco, e in ogni caso è sufficiente scorrere col dito verso l'alto per vedere le prime lettere inserite).
                * */
                .verticalScroll(scrollState)
        ) {
            Text(
                text = sequenceText,
                fontSize = 24.sp,
                // Testo in grassetto
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
                // Il colore cambia leggermente al tocco (opacità 0.3)
                .background(containerColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
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
                    // L'unico colore che non viene sfumato è quello illuminato
                    val coloreBottone = if (illuminato) colors[index] else colors[index].copy(alpha = 0.4f)

                    Button(
                        onClick = {
                            /*
                            *  Vengono verificate 3 condizioni per fare si che l'utente possa cliccare solo quando è il suo turno;
                            *  --> Il gioco deve essere avviato (l'utente non può premere se non ha cliccato "AVVIA PARTITA")
                            *  --> nessun bottone deve essere nel mezzo di un lampeggio
                            *  --> Il gioco non deve essere nella fase di riproduzione della sua sequenza
                            * */
                            if (statoPartita && bottoneIlluminato == -1 && !turnoPC) {
                                // Imposto il colore dello sfondo in base al colore appena premuto
                                containerColorArgb = colors[index].toArgb()
                                val letter = colorNames[index]

                                // Mostra il carattere appena scelto nella stringa di testo non editabile
                                sequenceText = if (sequenceText.isEmpty()) letter else "$sequenceText, $letter"

                                // Il gioco controlla che l'utente abbia premuto il tasto corretto
                                if (index == giocoSequenza[utenteIndiceCorrente]) {
                                    // Incremento utenteIndiceCorrente; ora il gioco si aspetta che l'utente inserisca il prossimo colore corretto
                                    utenteIndiceCorrente++

                                    // Questo if viene eseguito se l'utente ha digitato l'intera sequenza corretta
                                    if (utenteIndiceCorrente == giocoSequenza.size) {
                                        utenteIndiceCorrente = 0
                                        // Inizia il turno del PC
                                        turnoPC = true
                                    }
                                } else {
                                    // Errore da parte dell'utente; termina la partita e passa la stringa finale
                                    statoPartita = false
                                    val stringaFinale = sequenceText
                                    sequenceText = ""
                                    giocoSequenza.clear()
                                    utenteIndiceCorrente = 0
                                    onNavigateToSecondScreen(stringaFinale)
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
                            .border(1.dp, textDarkGray, RoundedCornerShape(20.dp))
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        statoPartita = true
                        sequenceText = ""
                        // Ripristino il colore grigio
                        // android studio dice; "Assigned value never used", ma non è vero; senza questa riga dopo avere premuto
                        containerColorArgb = initialColor.toArgb()
                        giocoSequenza.clear()
                        utenteIndiceCorrente = 0
                        // L'utente avvia la partita; come prima cosa tocca al PC riprodurre una sequenza (di un elemento il primo turno)
                        turnoPC = true
                    },
                    enabled = !statoPartita,
                    colors = ButtonDefaults.buttonColors(containerColor = gray11),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .border(1.dp, textDarkGray, RoundedCornerShape(8.dp))
                ) {
                    Text(text = "AVVIA PARTITA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        // Centra il testo orizzontalmente
                        textAlign = TextAlign.Center,
                        // Spazio verticale tra la prima e la seconda riga
                        lineHeight = 18.sp
                    )
                }
                Button(
                    onClick = {
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gray11),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .border(1.dp, textDarkGray, RoundedCornerShape(8.dp))
                ) {
                    Text(text = "PAUSA",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        // Centra il testo orizzontalmente
                        textAlign = TextAlign.Center,
                        // Spazio verticale tra la prima e la seconda riga (Nel caso altre lingue andassero a capo)
                        lineHeight = 18.sp
                    )
                }

                Button(
                    onClick = {
                        statoPartita = false
                        val stringaDaPassare = sequenceText
                        sequenceText = ""
                        giocoSequenza.clear()
                        utenteIndiceCorrente = 0
                        onNavigateToSecondScreen(stringaDaPassare)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gray11),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .border(1.dp, textDarkGray, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = stringResource(id = R.string.fine_partita),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        // Centra il testo orizzontalmente
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
        // Modalità landscape;
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
        // Modalità portrait
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

    // Coroutine per gestire la transizione tra turno giocatore e turno PC
    LaunchedEffect(turnoPC) {
        // if che viene eseguito solo se è il turno del PC e la partita è in corso
        if (turnoPC && statoPartita) {
            // Appena l'utente finisce di digitare l'ultima lettera della sequenza, la sequenza resta
            // visibile all'utente per altri 1.2 secondi.
            delay(1200)

            // Dopo i 1200ms l'area di testo viene ripulita (perchè ricomincia il turno del PC)
            sequenceText = ""

            // Il PC genera la nuova lettera
            val nuovoIndiceCasuale = Random.nextInt(6)
            // la lettera viene aggiunta alla fine della sequenza
            giocoSequenza.add(nuovoIndiceCasuale)

            // Tempo che intercorre tra la rimozione della stringa di testo non editabile e la prima lettera della sequenza proposta dal PC
            // (è anche il tempo che l'utente deve aspettare per vedere la prima lettera generata casualmente dal PC dopo avere cliccato AVVIA PARTITA)
            delay(800)

            // Il PC fa lampeggiare tutta la sequenza aggiornata
            for (indice in giocoSequenza) {
                bottoneIlluminato = indice
                containerColorArgb = colors[indice].toArgb()
                delay(500)

                bottoneIlluminato = -1
                containerColorArgb = initialColor.toArgb()
                delay(250)
            }

            // turnoPC torna ad avere valore false in quanto inizia il turno del giocatore
            turnoPC = false
        }
    }
}