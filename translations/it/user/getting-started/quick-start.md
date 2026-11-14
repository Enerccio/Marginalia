---
label: Avvio rapido
order: 70
---

# Avvio rapido

Dall'installazione iniziale alla prima parte generata di una storia. Presuppone che Marginalia sia
[installato](index.md) e che tu sia connesso come [amministratore](first-start.md).

<a id="1-connect-a-model"></a>

## 1. Collega un modello

1. Apri la scheda **Provider di inferenza** e clicca su **Aggiungi provider di inferenza**.
2. Inserisci un **Nome**, ad esempio `OpenRouter - Claude`.
3. In **Impostazioni generali**, imposta **Dimensione massima del contesto** alla dimensione di contesto del modello, ad esempio `32000`, e
   **Token massimi di risposta** alla lunghezza massima che vuoi per ogni parte, ad esempio `2000`.
4. In **Impostazioni per tipo**, inserisci l'**OpenAI URL** (termina con `/v1`) e la **Chiave API** (lasciala vuota per un server locale che non richiede chiave), clicca su **Aggiorna** e
   scegli il **Modello**.
5. Clicca su **OK**.

I campi sono descritti in [Provider di inferenza](../inference-providers.md), con gli URL dei servizi più comuni.

<a id="2-create-a-protocol"></a>

## 2. Crea un protocollo

1. Apri la scheda **Protocolli** e clicca su **Aggiungi protocollo**.
2. Inserisci un **Nome**, ad esempio `Default`.
3. Lascia vuoti **Token massimi di contesto** e **Token massimi di risposta**: vengono usati i limiti del provider di inferenza.
4. Clicca su **OK**.

Vedi [Protocolli](../protocols.md).

<a id="3-set-the-defaults"></a>

## 3. Imposta i valori predefiniti

Apri la scheda **Impostazioni** e scegli il nuovo provider come **Modello predefinito** e il nuovo protocollo come
**Protocollo predefinito**. Clicca su **Salva**. I nuovi libri partiranno così con entrambi, senza doverli selezionare ogni volta.

![Modello e protocollo predefiniti in Impostazioni](../../images/settings-defaults.png)

<a id="4-create-a-book"></a>

## 4. Crea un libro

1. Apri la scheda **Libri** e clicca su **Aggiungi libro**.
2. Inserisci il nome del libro. Il libro si apre.
3. Nella scheda **Informazioni**, controlla che **Modello** e **Protocolli** siano impostati (provengono dai valori predefiniti) e aggiungi una
   **Descrizione** della storia, se vuoi.

<a id="5-write-the-first-part"></a>

## 5. Scrivi la prima parte

1. Apri la scheda **Storia**.
2. Clicca su **+** nell'angolo in basso a destra. Si apre il pannello *Istruzioni del nuovo turno*.
3. Descrivi cosa deve accadere in **Istruzioni**, ad esempio:
   > Un guardiano del faro su un'isola remota trova un messaggio in una bottiglia indirizzato a lei, datato cinquant'anni nel futuro. Presenta lei e l'isola. Circa 800 parole.

   *Ambientazione della scena*, *Personaggio POV* e *Personaggi presenti* sono facoltativi; compilali per dare al modello indicazioni più precise.
4. Clicca su **Genera**. Il testo compare mentre il modello lo scrive. Il pulsante rosso **Interrompi** (al posto di **+**)
   interrompe la generazione.

![La prima parte generata](../../images/quick-start-first-part.png)

Clicca di nuovo su **+** per la parte successiva. Ogni parte può essere modificata, rigenerata o sostituita con una versione alternativa
dal suo menu (☰); vedi [Editor della storia](../books/story-editor.md) e
[Diramazioni e albero della storia](../books/branches-and-story-tree.md).

<a id="next-steps"></a>

## Prossimi passi

- Descrivi il tuo mondo e i tuoi personaggi in un [lorebook](../lorebooks/index.md).
- Regola lo stile, il punto di vista e il tempo verbale nei [prompt](../books/prompts.md) del libro.
- [Riassumi](../books/summaries.md) le parti più vecchie man mano che il libro cresce.
- Imposta i [backup del libro](../books/backups.md).
