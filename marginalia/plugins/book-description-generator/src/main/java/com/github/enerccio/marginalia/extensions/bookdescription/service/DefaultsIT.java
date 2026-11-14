package com.github.enerccio.marginalia.extensions.bookdescription.service;

import com.github.enerccio.marginalia.extensions.bookdescription.model.OutputFormat;

/**
 * The built-in prompts of the Book Description Generator in Italian; the JSON fields and the Markdown heading of the answer are
 * the ones the plugin reads.
 */
public class DefaultsIT extends Defaults {

    @Override
    public String descriptionPrompt() {
        return """
[ Sei un editor di libri esperto che scrive i titoli e le quarte di copertina. Il tuo compito è leggere una storia già scritta e dare al libro a cui appartiene un nome e una descrizione. ]

### Il libro ora
Nome: {{bookName}}
{{#if bookDescription}}Descrizione: {{bookDescription}}{{/if}}

### La storia
{{#if fromNotes}}La storia è troppo lunga per essere data per intero. Qui sotto ci sono le note prese su di essa, parte per parte, nell'ordine in cui si legge.{{else}}Qui sotto c'è la storia così come è stata scritta, nell'ordine in cui si legge. Una parte può essere data come il riassunto che sostituisce i messaggi che copre.{{/if}}

{{story}}

### Linee guida
1. Descrivi il libro che c'è davvero. Non inventare eventi, personaggi o luoghi e non continuare la storia.
2. Il nome è breve e facile da ricordare, nella lingua della storia. Non svela il finale. Non ripetere semplicemente il nome che il libro ha ora, a meno che non sia davvero il migliore.
3. La descrizione è ciò che vede un lettore che curiosa in una biblioteca: presenta l'ambientazione e i personaggi principali, imposta il conflitto centrale e la posta in gioco e fa venire voglia di continuare a leggere. Due o tre brevi paragrafi. Non svelare il finale, non elencare i capitoli, non rivolgerti al lettore con il «tu».
4. Scrivi nella lingua della storia, con il tono della storia.
""";
    }

    @Override
    public String notesPrompt() {
        return """
[ Sei un attento analista di storie. Il tuo compito è leggere una parte di una storia lunga e scrivere note che chi non l'ha letta possa usare per scrivere la descrizione dell'intero libro. ]

### Il libro ora
Nome: {{bookName}}
{{#if bookDescription}}Descrizione: {{bookDescription}}{{/if}}

### La storia, parte {{part}} di {{parts}}
{{story}}

### Linee guida
1. Scrivi note dense su ciò che accade davvero in questa parte, nell'ordine in cui accade. Non inventare nulla e non continuare la storia.
2. Nomina i personaggi (con i nomi usati nella storia), chi sono e che cosa vogliono, i luoghi, il conflitto centrale di questa parte e come finisce questa parte.
3. Aggiungi il genere, il tono e i temi che emergono in questa parte.
4. Conserva ciò che serve a una quarta di copertina e tralascia i dettagli minori. Usa brevi paragrafi o punti elenco, niente intestazioni.
5. Rispondi solo con le note. Niente introduzione, niente commenti.
""";
    }

    @Override
    public String outputInstructions(OutputFormat format) {
        return switch (format) {
            case AUTO, JSON -> "### Output\nRispondi solo con l'oggetto JSON richiesto dal formato di risposta: il \"name\" del libro e la sua \"description\". Niente introduzione, niente commenti.";
            case JSON_NO_SCHEMA -> "### Output\nRispondi solo con un oggetto JSON esattamente di questa forma, niente prima e niente dopo:\n"
                    + "{\"name\": \"<il nome del libro>\", \"description\": \"<la descrizione del libro>\"}";
            case MARKDOWN -> "### Output\nRispondi in Markdown, nient'altro: la prima riga è il nome del libro come intestazione (# Nome del libro), "
                    + "tutto ciò che sta sotto è la descrizione del libro. Niente introduzione, niente commenti.";
        };
    }
}
