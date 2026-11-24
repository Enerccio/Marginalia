---
label: Estensioni
order: 930
icon: plug
---

# Estensioni

Le estensioni aggiungono funzioni a Marginalia senza modificare l'applicazione stessa. Con Marginalia ne arrivano trenta:

| Estensione | Cosa aggiunge |
|---|---|
| [xAI Grok](grok.md) | I modelli Grok di xAI come fornitore di inferenza, tramite l'API di xAI, con il ragionamento mostrato separato dalla risposta. |
| [Arc Outline](arc-outline.md) | Gli archi del libro e i loro capitoli, con il loro stato, nella barra laterale; il modello può scrivere un arco al posto tuo. |
| [Anthropic Claude](anthropic.md) | I modelli Claude di Anthropic come fornitore di inferenza, tramite l'API di Anthropic, con i riassunti del ragionamento mostrati come ragionamento. |
| [Audiobook](audiobook.md) | Trasforma un libro in un audiolibro con un server vocale: una voce per il narratore e per ogni personaggio, uno studio per la versione finale, esportazione in mp3 con i segnalibri dei capitoli. [Configurazione del server vocale](audiobook-server.md). |
| [Author's Note](authors-note.md) | Una nota per il modello, inserita in ogni generazione del libro. |
| [Beta Readers](beta-readers.md) | Lettori che inventi (personaggi) e una chat con loro sul libro, da soli o in gruppo. |
| [Book Description Generator](book-description-generator.md) | Scrive con il modello il nome e la descrizione del libro a partire da una parte della storia, con il pulsante della scheda About. |
| [Chapter Marker](chapter-marker.md) | Titoli dei capitoli nello schema della storia, ricavati dalle intestazioni Markdown delle parti. |
| [Character Tracker](character-tracker.md) | I personaggi del libro con le loro relazioni, e una mappa di essi. |
| [Conlang Translator](conlang-translator.md) | Il parlato dei tuoi personaggi in una lingua costruita che descrivi, con l'inglese conservato dietro per i modelli e un clic per il lettore. |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | Fa funzionare le chiamate di funzione con i vecchi modelli Gemma 4 la cui API richiede un altro ruolo per i risultati degli strumenti; non serve con un template di chat aggiornato. |
| [Google Gemini](google-genai.md) | I modelli Gemini di Google come fornitore di inferenza, tramite l'API di Google, con i riassunti del ragionamento mostrati come ragionamento. |
| [Image Generator](image-generator.md) | Crea un'immagine per una parte della storia: il modello scrive il prompt, ComfyUI o un'API di immagini in stile OpenAI la disegna, tu puoi rigenerarla e accettarla. |
| [Prompt History](prompt-history.md) | Conserva le ultime 20 richieste inviate ai modelli nella tua sessione (e per ogni esecuzione di agente), con le risposte e il ragionamento, per il debug dei plugin e la verifica dei prompt; si apre da Impostazioni → Impostazioni estensioni o dalla console di un agente. |
| [Prompt Inspector](prompt-inspector.md) | Mostra il prompt di una generazione e chiede conferma prima che venga inviato al modello; si attiva e si disattiva nel menu delle impostazioni della storia. |
| [Instruction Exporter](instruction-exporter.md) | Copia negli appunti le istruzioni, le impostazioni di scena e i personaggi della storia. |
| [Knowledge Tracker](knowledge-tracker.md) | Ciò che ogni personaggio monitorato sa e non sa, inserito nel prompt perché il modello smetta di attribuire ai personaggi conoscenze che non possono avere. |
| [Lore Generator](lore-generator.md) | Scrive con il modello il contenuto di una voce del lorebook, a partire dalla lore che hai già. |
| [Lorebook VCS](lorebook-vcs.md) | Cronologia delle revisioni per le voci del lorebook. |
| [Manga Generator](manga-generator.md) | Trasforma parti della storia in una sceneggiatura di manga con pagine e vignette, e in tag per la generazione di immagini. |
| [Tools for the model](tools.md) | Il pulsante **Tools** dei generatori: quali suite di strumenti (nomi, luoghi) il modello può usare, e quali delle loro categorie. |
| [Name generator](name-generator.md) | Nomi che il modello usa al posto di inventarli: circa 1270 liste di nomi, come catene di Markov. |
| [Name provider](name-provider.md) | Nomi reali tra cui il modello può scegliere: nomi e cognomi di circa 100 paesi, con la grafia originale. |
| [Place provider](place-provider.md) | Luoghi reali che il modello può cercare: città, mari, baie, fiumi, montagne e altro, con coordinate e paesi, cosa c'è intorno a un luogo e la distanza tra due luoghi. |
| [Real Language Translator](real-language-translator.md) | Il parlato dei tuoi personaggi in una lingua reale (il giapponese per una storia ambientata in Giappone), con l'inglese conservato dietro per i modelli e un clic per il lettore. |
| [Reviewer](reviewer.md) | Recensioni delle parti della storia scritte dal modello: reazioni dei lettori, critica. |
| [Side Query](side-query.md) | Una chat con il modello sul tuo libro, accanto alla storia. |
| [Converter](sillytavern-converter.md) | Una chat di SillyTavern accanto alla storia e i dettagli del turno che il modello prepara a partire da essa, per riscrivere la chat come storia; oppure le parti di un libro più vecchio (da un backup o dal libro stesso) con le loro istruzioni, per riscriverlo. |
| [State Tracker](state-tracker.md) | Lo stato attuale dei personaggi monitorati (vestiti, ciò che portano, l'arma a portata di mano, ferite, umore, o categorie tue), inserito nel prompt perché il modello vi si attenga. |
| [Story Notes](story-notes.md) | Una piccola wiki di pagine tue (tag, collegamenti tra le pagine) nella barra laterale, per trame, idee e regole del mondo; il wizard della trama e gli agenti possono leggere e modificare le pagine che consenti. |
| [Read Aloud](tts-reader.md) | Legge ad alta voce un libro pubblicato nel visualizzatore, con la voce del tuo dispositivo. |
| [Chinese localization](chinese-localization.md) | Il cinese semplificato come lingua dell'interfaccia utente. |

<a id="installing"></a>

## Installazione

Le estensioni vengono installate da un **amministratore** per l'intera installazione di Marginalia, in *Admin → Extensions*:
sono file JAR, caricati con **Load Extension (.jar)**. Vedi [Gestione delle estensioni](../administration/extensions.md)
per i dettagli.

Una volta installata, un'estensione è disponibile a ogni utente. Un'estensione caricata mentre Marginalia è in esecuzione compare
nelle finestre aperte successivamente: chiudi e riapri un libro (o ricarica la pagina) per vederla.

!!!warning Usa versioni corrispondenti
Le estensioni agganciano l'interfaccia utente di una specifica versione di Marginalia. Un'estensione creata per una versione diversa può
smettere di funzionare o compromettere parti dell'applicazione. Usa le estensioni che arrivano con la tua versione di Marginalia.
!!!

Le estensioni sopra elencate fanno parte del codice sorgente di Marginalia (`marginalia/plugins`) e vengono compilate separatamente;
vedi il [README dei plugin](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md).

<a id="extensions-on-a-phone"></a>

## Estensioni su telefono

Il [layout per telefono](../phone.md) ha le proprie classi, quindi un'estensione deve supportarlo. Arc Outline, Author's Note, Beta Readers, Chapter Marker,
Character Tracker, Instruction Exporter e Reviewer lo supportano; Side Query, Lore Generator e Lorebook VCS non aggiungono nulla su telefono (vedi [Usare Marginalia su un telefono](../phone.md#extensions)). Name generator, Name provider, Place provider, Gemma 4 tools fix e Google Gemini non hanno schermate proprie (Google Gemini aggiunge solo un fornitore alla finestra del modello, che anche il telefono possiede), e Chinese localization si trova nel selettore della lingua del menu del telefono. Le estensioni si installano dalla pagina Admin, che sul telefono non è disponibile.

<a id="extension-settings"></a>

## Impostazioni delle estensioni

Le estensioni con impostazioni aggiungono una sezione in **Settings → Extension Settings** (la terza scheda della pagina *Settings*).
Queste impostazioni appartengono al tuo account, come il resto di *Settings*, e vengono salvate con il pulsante **Save** della pagina.

<a id="where-extension-data-is-kept"></a>

## Dove vengono conservati i dati delle estensioni

Le estensioni conservano i propri dati insieme ai dati a cui appartengono - le recensioni con la parte della storia, le conversazioni con il libro, le revisioni con il lorebook - quindi sono inclusi nei [backup del database](../administration/database-backups.md).
Se fanno parte anche dei [backup dei libri](../books/backups.md) e delle esportazioni è descritto nella pagina di ciascuna estensione.

La disinstallazione di un'estensione non elimina i suoi dati: installarla di nuovo li ripristina.
