package com.github.enerccio.marginalia.extensions.statetracker.extract;

/**
 * The built-in system messages of the State Tracker in Italian; the JSON of the answer, its values, the kinds of category, the
 * markers "(stays)" and "strict" and the times of day stay English.
 */
public class StateTrackerDefaultsIT extends StateTrackerDefaults {

    @Override
    public String extractionSystem() {
        return """
            Tieni traccia dello stato attuale dei personaggi di una storia: cosa indossano, portano, tengono in mano e così via. Ricevi i personaggi SEGUITI, ciascuno con le sue categorie (il tipo della categoria, cosa ci rientra e i suoi valori attuali, numerati), e i nuovi MESSAGGI della storia (numerati). Riporta, per ogni personaggio seguito, solo ciò che è cambiato in questi messaggi, così com'è alla FINE dei messaggi.

            Tipi di categoria:
            - single: un solo valore. "set" dà il nuovo valore (sostituisce quello attuale), "clear" dice che il personaggio non ne ha più.
            - list: voci. "add" aggiunge una nuova voce, "remove" toglie una voce tramite il suo numero.
            - description: un solo testo. "set" dà tutto il nuovo testo; conserva del testo attuale ciò che è ancora vero.

            Regole:
            1. Riporta un cambiamento solo quando i messaggi lo mostrano. Una categoria marcata strict cambia solo quando il testo dichiara il cambiamento. Una categoria che non è strict cambia anche per la conseguenza fisica diretta di ciò che dice il testo (bagnato dopo essere caduto in un fiume), mai per un'ipotesi su ciò che è successo fuori dalla pagina.
            2. Un'azione cambia lo stato quando è compiuta, non quando è progettata, promessa, offerta o chiesta. Un oggetto consegnato viene tolto a chi lo dà e aggiunto a chi lo riceve (se sono seguiti e hanno quella categoria). Posare un oggetto per un momento, usarlo o mostrarlo non è perderlo: togli una voce solo quando viene regalata, persa, consumata, distrutta, lasciata indietro o presa.
            3. Segui la descrizione di ogni categoria: ciò che non ci rientra non viene riportato lì.
            4. Non riportare ciò che i valori attuali dicono già, e non riformulare un valore che non è cambiato.
            5. I valori sono brevi, con le parole della storia, e descrivono uno stato, non un'azione (allegro, non sta ridendo).
            6. Ogni cambiamento ha bisogno di una citazione copiata parola per parola dai messaggi e del numero del messaggio.
            7. Nel dubbio, non riportare nulla. Le liste vuote sono normali.

            Rispondi solo con JSON, senza altro testo: {"characters":[{"name":"...","changes":[{"category":"...","action":"set|clear|add|remove","value":"...","item":N,"evidence":"...","message":N}]}]}. "value" è per set e add, "item" per remove. Un personaggio senza cambiamenti ha una lista vuota.""";
    }

    @Override
    public String clockNote() {
        return """
            Tieni traccia anche del tempo della storia. Ricevi anche l'OROLOGIO: il giorno della storia contato da un evento di riferimento, il momento della giornata e la data del calendario quando la storia ne ha una. Per ogni messaggio, riporta come si muove in esso il tempo della storia. Non contare: riporta solo ciò che dice il messaggio, il programma fa la somma.
            - none: il messaggio non fa avanzare il tempo, o non ne dice nulla. È il caso normale.
            - advance: passa del tempo. "days" è quanti giorni passano (0 quando resta lo stesso giorno: più tardi quel giorno, un'ora dopo), "timeOfDay" dove finisce il messaggio (dawn, morning, noon, afternoon, evening, night) quando lo dice.
            - day: il messaggio indica il giorno della storia contato dall'evento di riferimento dell'orologio («il quarantesimo giorno da quando ti sei svegliato»): "day" è quel numero. Un ordinale di qualcos'altro («al terzo mattino» di un viaggio o di un soggiorno) è un advance, non un day.
            - date: il messaggio indica una data del calendario: "date" come YYYY-MM-DD, con l'anno dell'orologio quando il messaggio non ne dà uno.
            - flashback: il messaggio è ambientato nel passato (un ricordo, un sogno del passato, il racconto di fatti precedenti); l'orologio non si muove, e niente di ciò che contiene cambia lo stato attuale dei personaggi.
            Regole dell'orologio: riporta solo ciò che il messaggio dice o rende chiaro («la mattina dopo» è un giorno, morning; «quella notte» è lo stesso giorno, night; «tre giorni dopo» sono tre giorni), mai un tempo che il testo non mostra. Gli intervalli vaghi («passarono settimane», «qualche giorno dopo») sono un advance con la tua migliore stima dei giorni e "approximate": true. Il solo momento della giornata è un advance di 0 giorni. Ogni segnalazione diversa da none ha bisogno di una citazione copiata parola per parola dal messaggio.

            Metti l'orologio nella stessa risposta JSON, come lista "clock" accanto a "characters": {"clock":[{"message":N,"kind":"none|advance|day|date|flashback","days":N,"day":N,"date":"YYYY-MM-DD","timeOfDay":"...","approximate":false,"evidence":"..."}],"characters":[...]}. "clock" ha una voce per messaggio, in ordine, senza i campi che un tipo non usa. Senza personaggi seguiti, "characters" è vuoto.""";
    }

    @Override
    public String tidySystem() {
        return """
            Tieni in ordine lo stato attuale dei personaggi di una storia: cosa indossano, portano, tengono, provano e così via. Ricevi i personaggi SEGUITI, ciascuno con le sue categorie e i loro valori attuali (numerati; un valore marcato "(stays)" non può essere cambiato), e gli ultimi MESSAGGI della storia (numerati). Alcuni stati svaniscono senza che la storia lo dica. Decidi, per ogni valore numerato, se è ancora vero alla FINE dei messaggi.

            Per ogni valore, una di queste:
            - tenerlo: non dirne nulla. È il caso normale.
            - drop: non è più vero o non vale più la pena seguirlo (una rabbia di parecchie scene fa che i messaggi non mostrano più, senza fiato molto dopo la corsa, «tiene una tazza» molto dopo il pasto, vestiti bagnati che hanno avuto il tempo di asciugarsi).
            - update: i messaggi mostrano che lo stato è andato avanti (l'umore è migliorato, il livido sta sbiadendo): dai il nuovo valore breve.
            - merge: due o più voci della stessa categoria list sono la stessa cosa («corda» e «rotolo di corda»): dai l'unica voce che le sostituisce.

            Regole:
            1. Ciò che dura resta: vestiti, oggetti portati, armi, cicatrici e ferite che hanno bisogno di tempo per guarire si tengono, a meno che i messaggi mostrino altro. Scarta o aggiorna soprattutto gli stati passeggeri (umori, sentimenti, essere bagnati, avere freddo, essere stanchi, senza fiato, tenere qualcosa per un momento).
            2. Giudica solo dai messaggi e dal tempo che passa in essi, mai da un'ipotesi su ciò che è successo fuori dalla pagina.
            3. I valori sono brevi, con le parole della storia, e descrivono uno stato, non un'azione.
            4. Ogni drop, update e merge ha bisogno di un breve motivo.
            5. Nel dubbio, tieni. Le liste vuote sono normali.
            6. Un valore può dire in quale giorno della storia è stato impostato e quanto tempo fa: usalo, uno stato passeggero impostato molti giorni fa è svanito.

            Rispondi solo con JSON, senza altro testo: {"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"update":[{"item":N,"value":"...","reason":"..."}],"merge":[{"items":[N,M],"value":"...","reason":"..."}]}]}. Un personaggio senza niente da cambiare ha liste vuote.""";
    }
}
