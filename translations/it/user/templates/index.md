---
label: Template e macro
order: 940
icon: code
---

# Template e macro

Tutto ciò che Marginalia invia al modello è costruito a partire da **template**: testi con segnaposto che vengono riempiti per
ogni parte generata. Puoi modificarli tutti, dalla cornice del prompt fino a una singola voce del lorebook.

{%{
```handlebars
Write the next section of the story.
{{#povCharacter}}Point of View Character: {{.}}{{/povCharacter}}
{{instructions}}
```
}%}

<a id="where-templates-are-used"></a>

## Dove vengono usati i template

| Template | Impostato in | Diventa |
|---|---|---|
| **Master Template** | libro [Prompt](../books/prompts.md), *Settings → Templates* | il messaggio di sistema: ruolo, lore, stile, riassunti |
| **User Prompt** | libro Prompt, *Settings → Templates* | la richiesta per la parte che viene scritta |
| **Summary Prompt** | libro Prompt, *Settings → Templates* | la richiesta per un [riassunto](../books/summaries.md) |
| **Meta Summary Prompt** | libro Prompt, *Settings → Templates* | la richiesta per un [meta riassunto](../books/summaries.md#meta-summaries) |
| **Lorebook entry content** | [voci del lorebook](../lorebooks/entries.md) | la lore inserita nel prompt |

*Point of View*, *Tense* e *Style* sono testo semplice, non template.

<a id="two-kinds-of-placeholders"></a>

## Due tipi di segnaposto

I template combinano due sintassi:

- **Handlebars** - variabili come {%{`{{instructions}}`}%} e sezioni come
  {%{`{{#sceneSetting}}...{{/sceneSetting}}`}%} che vengono stampate solo quando il valore non è vuoto. Vedi
  [Template di prompt](templates.md).
- **Macro di SillyTavern** - {%{`{{user}}`, `{{random::a::b}}`, `{{setvar::name::value}}`, `{{if ...}}`}%} e le altre macro
  che gli utenti di SillyTavern conoscono, così i lorebook e i prompt provenienti da SillyTavern continuano a funzionare. Vedi
  [Riferimento delle macro](macros.md).

Entrambe le sintassi funzionano in ogni template e possono essere combinate liberamente.

<a id="help-in-the-editor"></a>

## Aiuto nell'editor

Ogni campo di template ha un popover con suggerimenti:

- **Inserisci il template predefinito** copia il template predefinito nel campo;
- **Variabili di template disponibili** elenca le variabili di quel template, incluse quelle disponibili ovunque;
- **Macro disponibili** elenca tutte le macro con una breve descrizione.

![Il popover dei suggerimenti di un campo di template](../../images/template-hints.png)

Un template con un errore di sintassi (una sezione non chiusa, per esempio) non può essere salvato nei prompt del libro e in Settings; nelle voci del lorebook viene salvato, contrassegnato come non valido e usato come testo semplice finché non viene corretto.

Le variabili sconosciute (di solito refusi) non bloccano il salvataggio: il campo le elenca sotto di sé come avviso, vedi
[Refusi](templates.md#typos).

**Mostra il prompt** nel menu di una parte generata mostra il prompt così come è stato inviato: il modo migliore per verificare che cosa ha prodotto un template.

<a id="in-this-section"></a>

## In questa sezione

- [Template di prompt](templates.md) - variabili di ciascun template, sezioni, condizioni, ordine di valutazione
- [Riferimento delle macro](macros.md) - tutte le macro, variabili, condizionali e compatibilità con SillyTavern
