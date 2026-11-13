---
label: Controllo di versione dei lorebook
order: 9
---

# Controllo di versione dei lorebook

Cronologia delle revisioni per [voci del lorebook](../lorebooks/entries.md): conserva le versioni precedenti del tuo lore e passa
da una all'altra - per esempio lo stato del mondo prima e dopo una guerra, oppure un personaggio in momenti diversi della
storia.

!!!
Il controllo di versione dei lorebook funziona nel [layout per telefono](../phone.md#extensions): il pannello si trova sotto la riga **Attivata** del lorebook, le revisioni di una voce sono in cima al suo modulo.
!!!

!!!
Nell'[editor a schermo intero](../lorebooks/lorebooks.md#editing-in-full-screen) la riga **Lorebook Revisions** si trova sopra l'elenco delle
voci, e la barra delle revisioni di una voce sotto la riga del nome della sua scheda. Passare a una revisione la mostra subito nella scheda.
!!!

Una revisione è un'istantanea dell'intera voce: nome, contenuto, nota, attivata, ordine, filtro e modalità di filtraggio, modalità
di inserimento, *Non cercare nel suo contenuto altre voci*, tag e tag negativi. (Le revisioni create prima che esistesse quel
flag risultano disattivate.)

![La barra delle revisioni di una voce](../../images/plugin-lorebookvcs.png)

<a id="entry-revisions"></a>

## Revisioni delle voci

Con l'estensione installata, i dettagli (⌄) di ogni voce del lorebook iniziano con una barra delle revisioni:

```
Revision: 2 / 3            [−]  [‹]  [Rev 2 (2026-10-08 14:30:12) ▾]  [›]  [+]
Modified: 2026-10-08 14:30:12
```

Le revisioni funzionano come slot. La voce mostra sempre una di esse - la **revisione corrente** - e ciò che modifichi nella
voce appartiene a quella revisione. La generazione usa sempre la voce così com'è, quindi la revisione corrente è quella che
il modello vede.

| Controllo | |
|---|---|
| **+** | Salva la voce così com'è ora come nuova revisione e la rende corrente. |
| **‹** / **›**, oppure l'elenco | Passa alla revisione precedente / successiva / scelta. Le modifiche restano nella revisione che lasci; l'altra viene caricata nella voce. |
| **−** | Elimina la revisione corrente e passa alla precedente. L'ultima revisione rimasta non può essere eliminata. |
| *n / total*, *Modified* | Quale revisione è corrente e quando è stata modificata l'ultima volta. |

La prima volta che si aprono i dettagli di una voce, il suo stato attuale diventa la revisione 1.

Un uso tipico:

1. Apri la voce di un personaggio e clicca su **+** - la revisione 2 è una copia della revisione 1.
2. Modifica la revisione 2 con il personaggio dopo gli eventi del capitolo 10.
3. Scrivi. Quando torni a modificare il capitolo 5, passa alla revisione 1 con **‹**; torna alla revisione 2 più tardi con **›**.

<a id="lorebook-revisions"></a>

## Revisioni dei lorebook

L'editor del lorebook ottiene una riga **Lorebook Revisions** con due pulsanti. Operano sul lorebook selezionato nell'editor:

| Pulsante | |
|---|---|
| export | Scarica la cronologia di tutte le voci del lorebook come `<lorebook>_vcs_history.json`. |
| import | **Import Marginalia VCS JSON** - una cronologia esportata da questa estensione; **Import SillyTavern VCS Extension JSON** - cronologia dall'estensione di cronologia delle versioni dei lorebook di SillyTavern. |

Un'importazione **sostituisce** la cronologia del lorebook; se il lorebook ha già una cronologia, oppure il file è stato esportato
da un lorebook diverso, l'importazione chiede prima una conferma.

Una cronologia di Marginalia viene abbinata alle voci innanzitutto tramite le voci stesse. Le voci che non trova - per esempio
quando la cronologia proviene da un'altra copia del lorebook - vengono abbinate in base al nome e all'ordine della loro
revisione corrente, oppure solo al nome quando una sola voce lo possiede. Le cronologie che non corrispondono a nessuna voce
vengono saltate; la conferma indica quante sono.

La cronologia di SillyTavern viene abbinata alle voci in base alla loro posizione nel lorebook, quindi importala in un lorebook
[importato dalle stesse world info](../lorebooks/import-export.md#importing-sillytavern-world-info), prima di aggiungere o eliminare
voci. Le chiavi di attivazione delle sue revisioni diventano il filtro della voce, nello stesso modo in cui le converte
l'[importazione delle world info](../lorebooks/import-export.md#importing-sillytavern-world-info).

<a id="storage"></a>

## Archiviazione

La cronologia è memorizzata nel database insieme al lorebook, quindi i [backup del database](../administration/database-backups.md)
la includono. I dati che un'estensione conserva in un lorebook vengono anche scritti nelle [esportazioni dei lorebook](../lorebooks/import-export.md#exporting)
e nei lorebook dei [backup dei libri](../books/backups.md), oltre che nelle copie dei lorebook di un libro su cui lavorano gli
[agenti](../agents.md). Ma l'importazione di un lorebook crea nuove voci, e la cronologia appartiene all'identità delle vecchie,
quindi non compare su di esse. Per spostare la cronologia su una copia importata, esportala dall'originale e importala nella
copia - le voci vengono poi abbinate per nome e ordine (vedi [Revisioni dei lorebook](#lorebook-revisions)).
