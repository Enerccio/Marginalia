package com.github.enerccio.marginalia.extensions.stconverter.service;

/**
 * The default prompt of the wand in Italian; the JSON fields are the English ones.
 */
public class DefaultsIT extends Defaults {

    @Override
    public String prompt() {
        return """
[ Sei un editor di storie esperto. L'autore sta riscrivendo una chat di gioco di ruolo come romanzo, un turno alla volta: per ogni turno uno scrittore riceve l'ambientazione della scena, il personaggio del punto di vista, i personaggi presenti e le istruzioni su ciò che accade, e scrive il testo del turno. Il tuo compito è leggere la chat qui sotto e dividerla in turni di questo tipo. ]

### Lore di sfondo
{{backgroundLore}}

### La chat
I messaggi qui sotto sono la chat nell'ordine in cui sono stati scritti, i messaggi da {{firstMessage}} a {{lastMessage}}. Ognuno sta sotto il suo numero e il nome di chi l'ha scritto. Un messaggio dell'utente è ciò che il personaggio dell'utente fa o dice, un messaggio di un personaggio è la risposta della storia.

{{chat}}

{{#if previousTurn}}
### A che punto è la storia
I turni prima del messaggio {{firstMessage}} sono già fatti. L'ultimo di essi ha questi dati; continua da lì. Mantieni lo stesso personaggio del punto di vista e la stessa scena (luogo, momento, personaggi presenti) finché la chat non li cambia, e non raccontare di nuovo ciò che raccontano già.

{{previousTurn}}

{{/if}}
### Linee guida
1. Dividi la chat in turni nei suoi punti di svolta naturali (un cambio di luogo, di momento, di punto di vista o di situazione, o quando una parte dell'azione è conclusa), nell'ordine in cui i messaggi sono stati scritti. Un turno è fatto di uno o più messaggi consecutivi; tutti insieme i turni coprono ogni messaggio da {{firstMessage}} a {{lastMessage}} esattamente una volta, senza lasciarne fuori nessuno e senza metterne nessuno in due turni. Preferisci turni abbastanza lunghi da essere una scena della storia, non una singola battuta di dialogo.
2. Indica di quali messaggi è fatto un turno con "firstMessage" e "lastMessage" (i numeri delle intestazioni).
3. "sceneSetting": dove e quando si svolge il turno, chi e che cosa c'è e che cosa conta della scena. Prendilo dalla chat e dal lore di sfondo; non inventare nulla che la chat non sostenga.
4. "povCharacter": l'unico personaggio dal cui punto di vista è scritto il turno, con il nome usato nella chat. Mantieni lo stesso personaggio da un turno all'altro, a meno che la storia non cambi davvero punto di vista.
5. "presentCharacters": i nomi dei personaggi che partecipano alla scena o vi si trovano, separati da virgole.
6. "instructions": ciò che lo scrittore deve scrivere per il turno, come istruzioni: gli eventi nell'ordine in cui accadono, ciò che i personaggi fanno, dicono e provano, i dettagli della chat che devono restare, come finisce il turno. Tutto ciò che raccontano i messaggi deve esserci, perché lo scrittore possa scrivere il turno senza vedere la chat. Non continuare la storia, non cambiare ciò che è accaduto, rispetta il lore di sfondo.

### Output
Rispondi solo con l'oggetto JSON richiesto dal formato di risposta: l'elenco dei "turns" con i campi sopra. Niente introduzione, niente commenti.
""";
    }
}
