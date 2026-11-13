---
label: Template di prompt
order: 100
---

# Template di prompt

I template usano la sintassi [Handlebars](https://handlebarsjs.com/guide/), estesa con le [macro](macros.md) di SillyTavern.
Questa pagina descrive le variabili che riceve ciascun template e la parte di sintassi di Handlebars.

{%{

<a id="variables"></a>

## Variabili

Una variabile tra doppie parentesi graffe viene sostituita con il suo valore: `{{instructions}}` diventa le istruzioni che hai scritto per
la parte. Un valore vuoto non stampa nulla.

<a id="variables-of-each-template"></a>

### Variabili di ciascun template

| Template | Variabili |
|---|---|
| **Template principale** | `{{backgroundLore}}` voci del lorebook attivate (*Nel blocco del lore*), `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}`, `{{summaries}}` tutti i riassunti del ramo, `{{annotations}}` le [note](../books/annotations.md#giving-a-note-to-the-model) che hai contrassegnato per darle al modello, per le parti nel prompt |
| **Prompt utente** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}` - le istruzioni del turno |
| **Prompt di riassunto** | `{{text}}` le parti da riassumere, `{{backgroundLore}}` lore attivo quando è stata scritta la parte riassunta |
| **Prompt di meta-riassunto** | `{{summaryBlocks}}` i riassunti da unire, dal più vecchio, `{{backgroundLore}}` lore attivo quando è stata scritta la parte del meta-riassunto |
| **Voce del lorebook** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}`, `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` |

<a id="variables-available-everywhere"></a>

### Variabili disponibili ovunque

Oltre alle proprie variabili, ogni template può usare:

| Variabile | |
|---|---|
| `{{povCharacter}}`, `{{sceneSetting}}`, `{{presentCharacters}}`, `{{instructions}}` | Le istruzioni del turno della parte in scrittura (per un riassunto: della parte riassunta). |
| `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` | Le impostazioni narrative del libro. |
| `{{manuscriptName}}`, `{{manuscriptDescription}}` | Il nome e la descrizione del libro. |

Anche il popover dei suggerimenti (ⓘ) di ogni campo del template le elenca.

<a id="typos"></a>

### Errori di battitura

Una variabile che non esiste viene stampata come la parola **`Error`**: `{{instruction}}` (manca la *s*) inserisce *Error* nel
prompt. I campi dei template (*Prompt* del libro, *Impostazioni*, voci del lorebook) elencano questi nomi sotto il campo come
*Nomi sconosciuti (inviati al modello come "Error")*. È solo un avviso - il template viene comunque salvato - quindi correggi i nomi
che elenca. Le variabili e le sezioni vengono controllate ovunque, anche dentro i rami `{{#if}}` che non vengono stampati. I nomi
usati solo come condizione non vengono segnalati, perché non stampano *Error*: in `{{#if name}}` un nome sconosciuto è
falso, nella forma `{{if name}}` di SillyTavern viene trattato come testo semplice, che è vero.

<a id="sections"></a>

## Sezioni

Una sezione stampa il proprio contenuto solo quando la variabile ha un valore. Al suo interno, `{{.}}` è il valore:

```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```

Con *Ambientazione della scena* vuota, l'intera riga scompare - i template predefiniti usano questo meccanismo ovunque, così il prompt
non ha intestazioni vuote.

Una **sezione invertita** stampa il proprio contenuto solo quando la variabile è vuota:

```handlebars
{{^povCharacter}}Write from an omniscient narrator's point of view.{{/povCharacter}}
```

<a id="conditions"></a>

## Condizioni

`{{#if}}` con un `{{else}}` opzionale:

```handlebars
{{#if presentCharacters}}
Characters present: {{presentCharacters}}
{{else}}
The POV character is alone.
{{/if}}
```

`{{#unless x}}...{{/unless}}` è l'opposto di `{{#if x}}`. Anche la forma di SillyTavern `{{if x}}...{{/if}}` funziona e
offre più opzioni (negazione, variabili, confronti), vedi [Condizionali](macros.md#conditionals).

Le macro senza argomenti si possono usare come variabili nelle sezioni e nelle condizioni:

```handlebars
{{#lastMessage}}The story so far ends with: {{.}}{{/lastMessage}}
{{#if summary}}...{{/if}}
```

<a id="comments-and-escaping"></a>

## Commenti e escape

| Sintassi | Risultato |
|---|---|
| `{{!-- a comment --}}`, `{{! short comment }}` | Rimossi dall'output. Usali per le note nei tuoi template. |
| `\{{instructions}}` | Stampa `{{instructions}}` alla lettera invece del valore. |
| `{{{instructions}}}` | Uguale a `{{instructions}}`. In Marginalia nulla viene sottoposto a escape HTML, quindi `<`, `&` e le virgolette vengono stampati così come sono. |

Anche il commento di SillyTavern `{{// ...}}` e l'escape `\{\{...\}\}` funzionano, vedi [Utility](macros.md#utility).

<a id="order-of-evaluation"></a>

## Ordine di valutazione

Per una parte generata, i template vengono elaborati in quest'ordine:

1. il **prompt utente**;
2. le **voci del lorebook** attivate, nel loro ordine;
3. il **template principale**.

Tutti condividono un unico insieme di [variabili](macros.md#variables): un valore impostato con `{{setvar}}` nel prompt utente può
essere letto nelle voci del lorebook e nel template principale; un valore impostato in una voce del lorebook è visibile alle voci successive
e al template principale, ma non al prompt utente, che è già stato elaborato.

I filtri del lorebook vedono il prompt utente già elaborato, quindi una macro nel prompt utente può produrre testo che attiva
le voci.

<a id="what-is-kept-between-parts"></a>

## Cosa viene conservato tra le parti

| | |
|---|---|
| **Variabili locali** (`{{setvar}}`, `{{.name}}`) | Salvate con la parte generata. La parte successiva parte dai valori dell'ultima parte del ramo attivo, quindi ogni ramo ha i propri valori. *Rigenera* riparte dai valori della parte precedente. |
| **Variabili globali** (`{{setglobalvar}}`, `{{$name}}`) | Salvate con il libro, condivise da tutti i rami. Non vengono annullate da *Rigenera* né dall'eliminazione di una parte. |
| **Riassunti** | Possono leggere le variabili, ma le loro modifiche vengono scartate. |

`{{pick}}` dà sempre lo stesso risultato per lo stesso libro, `{{random}}` ne dà uno nuovo ogni volta.

<a id="writing-your-own-templates"></a>

## Scrivere i propri template

Parti dal predefinito: apri il popover dei suggerimenti e clicca su **Inserisci il template predefinito**, poi modificalo. Cose da mantenere:

- il blocco `{{#backgroundLore}}` nel template principale - senza di esso il lorebook non viene usato (solo le voci con *Prima del prompt utente* raggiungono ancora il modello);
- il blocco `{{#summaries}}` nel template principale - senza di esso i riassunti vanno persi;
- `{{instructions}}` nel prompt utente - senza di esso il modello non vede cosa deve accadere;
- `{{text}}` nel prompt di riassunto - senza di esso non c'è nulla da riassumere.

}%}
