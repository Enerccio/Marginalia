---
label: Lorebook
order: 950
icon: archive
---

# Lorebook

!!!
Il [layout per telefono](../phone.md#lorebooks) ha lo stesso editor su una sola colonna: i pulsanti dei lorebook sono in un menu, le impostazioni del
lorebook si trovano in una sezione che si apre, e una voce si apre in un modulo. Solo alcune cose restano sul desktop, vedi l'elenco lì.
!!!

Un lorebook raccoglie ciò che il modello deve sapere sul tuo mondo: personaggi, luoghi, storia, regole, terminologia. Ogni
pezzo di conoscenza è una **voce**. Prima di ogni parte generata, Marginalia decide quali voci sono pertinenti e le inserisce
nel prompt, così il modello sa chi è il Capitano Varro quando entra nella scena, senza che ogni voce occupi spazio in ogni prompt.

![L'editor dei lorebook](../../images/lorebook.png)

<a id="how-it-fits-together"></a>

## Come si integra tutto

```
Book ──uses──▶ Lorebook "Saga" ──sub lorebooks──▶ "Characters", "Places", "Magic"
                   │                                   │
                 entries                             entries
```

- Un **libro** usa un lorebook (nella sua scheda **Lorebook**).
- Un lorebook può includere altri lorebook come **sotto-lorebook**. Anche le loro voci partecipano, quindi puoi tenere il materiale condiviso (un mondo usato da più libri, un cast di personaggi) in un lorebook separato e includerlo dove serve.
- Ogni **voce** contiene il testo che va nel prompt e le condizioni che decidono quando viene usata.

<a id="when-an-entry-is-used"></a>

## Quando una voce viene usata

Per ogni parte generata, una voce viene **attivata** quando sono vere tutte queste condizioni:

1. la voce e il suo lorebook sono abilitati;
2. i suoi **tag** corrispondono ai tag del libro (se ne ha);
3. il suo **filtro** corrisponde alle istruzioni per la parte (se ne ha) oppure, quando la voce guarda nella storia, anche le parti più recenti della storia.

Le voci attivate vengono inserite secondo il loro **ordine** - nel blocco di lore del prompt di sistema, oppure subito prima della richiesta della parte. Vedi [Voci e attivazione](entries.md).

Le voci senza tag e senza filtro sono sempre attive: usale per ciò che il modello deve sempre sapere.

<a id="lorebooks-and-other-features"></a>

## Lorebook e altre funzionalità

- Le voci dei lorebook possono usare [template e macro](../templates/index.md) - le world info di SillyTavern continuano a funzionare.
- Le [world info di SillyTavern](import-export.md#importing-sillytavern-world-info) possono essere importate.
- I [backup del libro](../books/backups.md) contengono il lorebook del libro con i suoi sotto-lorebook.
- L'estensione [Lore Generator](../extensions/lore-generator.md) scrive il contenuto di una voce con il modello, a partire dalla lore che hai già.
- L'estensione [Lorebook VCS](../extensions/lorebook-vcs.md) conserva una cronologia delle revisioni delle voci.

<a id="in-this-section"></a>

## In questa sezione

- [Creare lorebook](lorebooks.md) - l'editor dei lorebook, i sotto-lorebook, usare un lorebook in un libro
- [Voci e attivazione](entries.md) - campi della voce, tag, filtri, ordine e inserimento
- [Importazione ed esportazione](import-export.md) - file lorebook di Marginalia e world info di SillyTavern
