package com.github.enerccio.marginalia.extensions.loregenerator.service;

/**
 * The built-in prompts of the Lore Generator in Italian.
 */
public class DefaultsIT extends Defaults {

    @Override
    public String compactionPrompt() {
        return """
[Istruzione di sistema: sei un motore esperto di compattazione dei dati. Il tuo unico compito è assorbire il testo grezzo fornito qui sotto e convertirlo in una voce d'archivio di lore ultradensa e leggibile da una macchina, per la memoria di fondo di un LLM.]

REGOLE CRITICHE:

1. Elimina il 100 % del riempitivo atmosferico, della prosa fiorita, della preparazione narrativa, della formattazione interna al mondo (come intestazioni di rapporti o formule di congedo) e dell'ornamento retorico.
2. Estrai solo i fatti concreti: regole, meccaniche spaziali, demografia, tratti fisici, identificativi, motivazioni e limiti funzionali.
   - ECCEZIONE NARRATIVA: Se il testo sorgente contiene un aneddoto, un evento del passato o una narrazione storica, NON eliminarlo. Comprimilo invece in un blocco «[Precedente storico]» o «[Nucleo psicologico]». Togli la prosa narrativa, ma conserva l'esatta dinamica causale, il trauma, il rancore o l'innesco comportamentale creato da quell'evento. Includi i dettagli necessari nei precedenti storici e conserva TUTTI i precedenti storici.
3. Struttura tutto l'output con attributi markdown chiari in grassetto e punti elenco puliti e minimalisti.
4. Mantieni una precisione assoluta nelle descrizioni spaziali o fisiche (ad es. il lato preciso di un volto, disposizioni geometriche precise, numeri esatti).
5. Restituisci SOLO il blocco markdown grezzo. Niente introduzione, chiusura o conferma colloquiale. Inizia subito con il primo blocco di attributi.
6. IMPORTANTE: restituiscilo in un blocco di codice markdown copiabile tra tripli apici inversi!
7. NON riassumere informazioni che sono già lore esistente.

==== INIZIO DEL MATERIALE SORGENTE GREZZO ====
{{compactionData}}
==== FINE DEL MATERIALE SORGENTE GREZZO ====
""";
    }

    @Override
    public String generationPrompt() {
        return """
[ Sei un esperto costruttore di mondi e un maestro della prosa molto descrittiva e atmosferica. Il tuo compito è creare una voce di lore dettagliata basata rigorosamente sui parametri forniti qui sotto. Usa immagini vivide e un linguaggio ricco, assicurandoti che ogni dettaglio essenziale sia incorporato con naturalezza. ]

### Parametri di input
- Soggetto: [Inserisci il nome del soggetto]
- Dettagli essenziali da intrecciare: [Inserisci dettagli, fatti o meccaniche specifici]
- Lunghezza / portata: [ad es. Approfondimento (oltre 800 parole), Vignetta media (400 parole) o Micro-lore (150 parole)]
- Struttura richiesta: [Indica esattamente quali sezioni scrivere, oppure scrivi «A tua scelta: genera almeno X sezioni tematiche distinte»]

### Linee guida di scrittura
1. Tono e stile: ricco, evocativo e atmosferico. Usa metafore eleganti e una narrazione immersiva adatta all'ambientazione (fantasy / fantascienza / contemporaneo).
2. Ritmo e profondità: sviluppa ampiamente il lore. Non limitarti a enunciare un fatto; dedica spazio alla storia, all'impatto culturale, all'esperienza sensoriale e alle voci che lo circondano. Rispetta la lunghezza richiesta.
3. Struttura: devi seguire la struttura richiesta indicata sopra. Usa intestazioni markdown descrittive per ogni sezione.
4. Integrazione dei fatti: avvolgi con naturalezza i dettagli essenziali in una prosa sontuosa. Assicurati che restino abbastanza chiari sotto le immagini perché un LLM possa ricavarli in seguito.
5. Scelta dei nomi: se devi inventare un nome, scegli nomi poco comuni, che non creino confusione di contesto con le voci di lore già generate.

### Output
Fornisci solo la voce di lore stilizzata. Niente riempitivi colloquiali di introduzione o di chiusura.
""";
    }
}
