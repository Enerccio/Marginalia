package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

/**
 * The built-in system messages of the Knowledge Tracker in Italian; the JSON of the answer, its values ("knows",
 * "does-not-know"), the tool name and the marker "learned in part N" stay English.
 */
public class KnowledgeTrackerDefaultsIT extends KnowledgeTrackerDefaults {

    @Override
    public String extractionSystem() {
        return """
            Tieni traccia di ciò che sanno i personaggi di una storia. Ricevi il mondo (il lorebook, se è dato), i personaggi SEGUITI con gli elementi numerati di ciò che ciascuno sa o non sa ora, e i nuovi MESSAGGI della storia (numerati). Riporta, per ogni personaggio seguito, solo ciò che è cambiato in questi messaggi.

            Regole:
            1. add: conoscenza che il personaggio ACQUISISCE in questi messaggi: ciò che ha visto, sentito, letto, gli è stato detto o ha capito da sé. Non ciò che sa il lettore, non ciò che dice solo il narratore, non ciò che sanno altri personaggi.
            2. NON aggiungere conoscenze generali che un abitante di quel mondo ha comunque (chi è il re per un suddito del regno, le usanze comuni, la lingua, la geografia della propria casa), né nulla che derivi dal personaggio stesso come lo descrive il testo del mondo (la sua specie, il corpo, le capacità, la storia, la famiglia, il mestiere).
            3. NON aggiungere nulla che gli elementi del personaggio dicano già.
            4. retract: il numero di un elemento che i messaggi contraddicono chiaramente (per esempio «non ha mai incontrato X» dopo che si incontrano). Solo quando il testo mostra la contraddizione. Un elemento che dice che un personaggio non sa Y si ritira solo se il personaggio scopre Y in questi messaggi. Un elemento che dice più di una cosa si ritira solo quando i messaggi lo contraddicono del tutto; quando ne è contraddetta solo una parte, lascia l'elemento e aggiungi ciò che è stato appreso. Giudica un elemento da come FINISCONO i messaggi: quando più avanti in questi messaggi al personaggio viene detto, ricordato o lui stesso ricava di nuovo ciò che dice l'elemento (chiede chi è qualcuno e poi gli viene detto), l'elemento è vero alla fine, quindi non ritirarlo e non aggiungerlo di nuovo.
            5. Non dedurre mai. Nel dubbio, non riportare nulla. Un personaggio che non è presente, o che non vede né sente un evento (pensieri privati, sussurri, una lettera chiusa), non ne apprende nulla.
            6. Ogni cambiamento richiede una citazione di prova copiata dai messaggi e il numero del messaggio.
            7. Registra solo conoscenze che possono contare più avanti nella storia: segreti, identità, relazioni e sentimenti rivelati, piani, promesse, scoperte e fatti che un altro personaggio non sa. NON registrare eventi che tutti i personaggi presenti hanno visto o sentito insieme (il testo della storia li contiene già), chiacchiere, battute, stati d'animo, il tempo atmosferico, oggetti e dettagli dell'ambiente, ciò che un personaggio ha fatto lui stesso, né lo stesso fatto per più personaggi. Al massimo 3 aggiunte per personaggio, di solito 0 o 1. Quando nulla conta, non riportare nulla.

            Rispondi solo con JSON, senza altro testo: {"characters":[{"name":"...","retract":[{"item":N,"evidence":"...","message":N}],"add":[{"kind":"knows|does-not-know","text":"...","evidence":"...","message":N}]}]}. Un personaggio senza cambiamenti ha liste vuote.""";
    }

    @Override
    public String readingNote() {
        return """
            Puoi anche chiamare read_part con il numero di una parte precedente della storia. Gli elementi appresi in una parte precedente lo dicono ("learned in part N") e i messaggi dicono quale parte sono. Quando un elemento viene da un sentito dire, da una voce, da un piano o da una presentazione e questi messaggi sembrano confermarlo, ripeterlo o contraddirlo, oppure l'elemento da solo non ti dice che cosa sapeva il personaggio in quel momento, leggi prima quella parte e poi decidi, invece di tirare a indovinare. Non leggere parti per nessun altro motivo (al massimo %d). La tua risposta finale resta solo il JSON.""".formatted(PartReader.MAX_READS);
    }

    @Override
    public String tidySystem() {
        return """
            Metti ordine in ciò che sanno i personaggi di una storia. Ricevi i personaggi SEGUITI con gli elementi numerati che sanno o non sanno, gli elementi che restano come sono (solo per confronto) e le ultime parti della storia come contesto. Gli elementi vengono inviati a un modello di scrittura perché non dia a un personaggio conoscenze che non può avere; ogni elemento occupa spazio e deve valerne la pena.

            Regole:
            1. drop: togli un elemento che non vale la pena tenere: una banalità (un dettaglio della scena, chiacchiere, una battuta, uno stato d'animo, un oggetto), un evento che tutti i personaggi della scena hanno visto insieme (la storia lo contiene già), qualcosa superato da eventi successivi, o qualcosa che dice lo stesso di un altro elemento o di un elemento che resta.
            2. merge: unisci due o più elementi sulla stessa cosa in un solo elemento che conservi ogni loro fatto. Non aggiungere nulla che non sia negli elementi.
            3. Non togliere né unire MAI un segreto, un'identità, una relazione, un sentimento rivelato, un piano, una promessa, una scoperta o qualcosa che un altro personaggio non sa, anche se le ultime parti non lo menzionano: che un elemento non sia comparso di recente non è un motivo per toglierlo.
            4. Nel dubbio, tienilo. Riporta solo ciò di cui sei sicuro; le liste vuote sono normali.
            5. Ogni drop e ogni merge richiede un breve motivo.

            Rispondi solo con JSON, senza altro testo: {"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"merge":[{"items":[N,N],"kind":"knows|does-not-know","text":"...","reason":"..."}]}]}. Un personaggio senza nulla da fare ha liste vuote.""";
    }
}
