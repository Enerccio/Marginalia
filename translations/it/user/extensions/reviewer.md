---
label: Revisore
order: 2
---

# Revisore

Permette al modello di rivedere ciò che hai scritto: reazioni dei lettori, una critica da editor, un controllo di continuità - qualunque cosa tu gli chieda. Le revisioni sono conservate con la parte, accanto alla storia, e non entrano mai nel libro.

![Una revisione di una parte della storia](../../images/plugin-reviewer.png)

<a id="reviewing-a-part"></a>

## Revisionare una parte

Il menu (☰) di ogni parte nell'[editor della storia](../books/story-editor.md) contiene un sottomenu **Revisione**:

| Voce | |
|---|---|
| **View / Generate Review** | Apre la finestra di revisione. Se la parte non ha ancora una revisione, ne viene generata una subito. |
| **Advanced Options** | Apre le [opzioni avanzate](#advanced-options), poi genera una revisione con esse. |
| **Delete Review** | Rimuove tutte le revisioni della parte. |

Nella finestra di revisione:

- **‹** / **›** scorrono le revisioni della parte; **›** sull'ultima revisione ne genera una nuova;
- **Stop generating** interrompe la revisione in corso di scrittura;
- fai clic sul testo di una revisione per modificarlo; viene salvato quando fai clic fuori dal campo;
- *Thinking Process* mostra il ragionamento dei [modelli di ragionamento](../inference-providers.md#reasoning-models);
- **Chiudi** chiude la finestra.

<a id="what-the-model-gets"></a>

## Che cosa riceve il modello

Per impostazione predefinita la revisione usa lo **stesso prompt da cui è stata scritta la parte** (*Use standard prompt info*): il prompt di sistema con lore e riassunti, le parti precedenti, poi la parte stessa e infine il prompt di revisione. Il modello rivede la parte con il contesto completo della storia.

Il **pre-prompt** del profilo, se impostato, viene inviato per primo come messaggio di sistema.

<a id="advanced-options"></a>

## Opzioni avanzate

| Opzione | |
|---|---|
| **Prompt Override** | Un prompt di revisione diverso per questa revisione. Parte dal prompt del profilo. |
| **Use standard prompt info** | Attivo: il prompt da cui è stata scritta la parte, come sopra. Disattivo: solo le parti della storia, con le opzioni qui sotto. |
| **Include Lorebook** | (solo con disattivo) Aggiunge la lore attiva quando la parte è stata scritta. |
| **With summaries** | (solo con disattivo) Invia prima i [riassunti](../books/summaries.md) della storia, poi le parti successive all'ultimo riassunto, come fa una generazione: la parte con il riassunto più recente e tutte quelle precedenti sono rappresentate dai riassunti. I riassunti occupano prima il loro spazio nel limite di token, le parti ricevono il resto, dalla più recente. Un riassunto la cui storia è stata modificata dopo la sua creazione non viene inviato (la revisione non lo rimuove). Disattivo: nessun riassunto, tante delle parti più recenti quante ne entrano. |
| **Send original text** | Visibile solo quando è caricato un traduttore ([Real Language Translator](real-language-translator.md), [Conlang Translator](conlang-translator.md)). Disattivo: la revisione legge l'inglese dietro il parlato tradotto, come fa ogni modello. Attivo: la parte revisionata (e, con *Use standard prompt info* disattivo, le parti precedenti) viene inviata così come è memorizzata, con il parlato nella traduzione. Il prompt da cui è stata scritta la parte (*Use standard prompt info* attivo) è stato costruito dall'inglese e resta tale. |
| **Token Limit** | (solo con disattivo) Quanti token di storia inviare: le parti fino a quella revisionata compresa, dalla più recente, tante quante ne entrano. Parte dallo spazio riservato al prompt del modello. |

Con *Use standard prompt info* disattivo, la finestra mostra la dimensione della richiesta (*Total tokens*). **Generate** apre la finestra di revisione con queste opzioni; le altre revisioni generate in quella finestra le usano a loro volta.

![Opzioni avanzate di revisione](../../images/plugin-reviewer-advanced.png)

<a id="profiles"></a>

## Profili

Le impostazioni del Revisore si trovano in **Impostazioni → Extension Settings → Reviewer Settings**. Sono organizzate in **profili**:

![Le impostazioni del Revisore](../../images/plugin-reviewer-settings.png)

| Campo | |
|---|---|
| **Profile** | Il profilo da modificare - e quello usato per tutte le revisioni. |
| **New**, **Rinomina**, **Elimina** | Gestiscono i profili. Il profilo *_Default* non può essere rinominato né eliminato. |
| **Reviewer Model** | Il [fornitore di inferenza](../inference-providers.md) che scrive le revisioni. Vuoto: il modello del libro. |
| **Reviewer Protocol** | Il [protocollo](../protocols.md). Vuoto: il protocollo del libro. |
| **Review Pre-Prompt (System)** | Un messaggio di sistema inviato per primo, ad esempio *Sei un editor di narrativa esigente.* |
| **Review Post-Prompt (User)** | La richiesta di revisione, inviata per ultima. |

Fai clic su **Salva** in cima alla pagina *Impostazioni* per conservare le modifiche.

Il post-prompt predefinito simula una discussione su un forum di narrativa: un thread inventato con trenta risposte di lettori diversi, positive e negative. Altre idee:

- *Elenca gli errori di continuità nell'ultima parte rispetto alla storia precedente. Cita i passaggi.*
- *Come editor di linea, segnala cliché, ripetizioni e verbi deboli nell'ultima parte, con suggerimenti.*
- *Descrivi come si sentirebbe un lettore alla prima lettura alla fine di questa parte, e che cosa si aspetta dopo.*

Crea un profilo per ogni tipo di revisione e selezionalo prima di revisionare. Un modello meno costoso per le revisioni fa risparmiare: impostalo come *Reviewer Model* del profilo.

<a id="storage"></a>

## Archiviazione

Le revisioni sono memorizzate insieme alla parte della storia, quindi fanno parte dei [backup del libro](../books/backups.md) e delle copie. [Branch Story](../books/branches-and-story-tree.md#branching-from-an-earlier-part) non le copia. I profili sono memorizzati nelle impostazioni utente.
