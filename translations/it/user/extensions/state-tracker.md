---
label: Monitoraggio dello stato
order: 29
---

# Monitoraggio dello stato

Tiene traccia dello **stato** attuale dei tuoi personaggi: cosa indossano, cosa portano con sé, l'arma in mano, le ferite,
l'umore o qualsiasi altra cosa tu definisca. Inserisce lo stato dei personaggi della scena nel prompt. Un modello dimentica
facilmente che la spada è stata perduta due capitoli prima o che il mantello è bagnato. Con il Monitoraggio dello stato il
prompt indica, per ogni personaggio tracciato nella scena, ciò che è vero di lui in questo momento.

!!!
Sei tu a scegliere chi viene tracciato e cosa si tiene traccia di lui. Al modello vengono descritti solo i personaggi che
aggiungi alla scheda, e solo mentre sono tra i *personaggi presenti* della parte che si sta scrivendo. In questa versione
lo stato lo mantieni tu (o lo fa un agente, con i comandi qui sotto); seguire la storia insieme al modello arriverà più
avanti.
!!!

<a id="categories"></a>

## Categorie

Una **categoria** è una cosa che può essere tracciata di un personaggio. Ogni categoria ha:

- un **nome**, che è anche come lo chiama il prompt (*Indossa*, *Arma in mano*, *Porta con sé*);
- un **tipo**:
  - **Valore singolo**: un valore breve, e un nuovo valore sostituisce il precedente (l'arma in mano, il luogo).
  - **Lista**: voci che si aggiungono e si tolgono una alla volta (ciò che si porta con sé, le ferite).
  - **Descrizione**: un unico testo che viene riscritto per intero (i vestiti, l'umore).
- un'**istruzione per il modello**: cosa appartiene alla categoria e cosa no. Il modello la legge quando segue la storia.
- **Solo ciò che dice il testo**: attivo, il valore cambia solo quando il testo lo dice; disattivo, cambia anche con le
  conseguenze ovvie (bagnato dopo un bagno). Anche questo viene usato dal modello quando segue la storia.
- **Scrivi "nessuno" se vuoto**: il prompt dice *nessuno* quando il personaggio non ha un valore (un'arma perduta) invece di
  omettere la categoria.

<a id="my-categories"></a>

### Le mie categorie

Le tue categorie personali si trovano in **Impostazioni → Impostazioni estensioni → Monitoraggio dello stato**. I libri
copiano le proprie categorie da qui. Finché non le modifichi, hai cinque categorie predefinite: *Indossa*, *Arma in mano*,
*Porta con sé*, *Ferite* e *Umore*. Puoi aggiungere, modificare ed eliminare categorie in quella schermata e premere **Salva**
nella schermata delle Impostazioni. **Ripristina le categorie predefinite** riporta le cinque predefinite.

<a id="the-categories-of-a-book"></a>

### Le categorie di un libro

Un libro conserva una **copia propria** di ogni categoria che usa, così un backup o un'esportazione del libro contiene tutto,
e eliminare una delle tue categorie non rompe mai un libro. Il pulsante **Categorie di questo libro** nella scheda Stato ne
elenca le categorie:

- **Aggiungi dalle mie categorie** copia una delle tue categorie nel libro.
- **Nuova categoria per questo libro** crea una categoria che esiste solo in questo libro (il carico di una nave, un legame
  magico). Una categoria del genere è contrassegnata come *Solo in questo libro*. **Salva nelle mie categorie** la aggiunge
  alle tue categorie, così anche altri libri possono usarla.
- Una copia che non è più uguale alla tua categoria è contrassegnata come *Diversa dalla mia categoria*. **Aggiorna dalle mie
  categorie** prende la tua versione. Non si aggiorna nulla senza di te, perché un'istruzione modificata cambia ciò che fa il modello.
- Modificare una categoria la cambia solo in questo libro. Il suo tipo non può cambiare finché i personaggi hanno valori di quella categoria.
- Eliminare una categoria dal libro elimina i valori di ogni personaggio in essa.

<a id="the-state-tab"></a>

## La scheda Stato

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) ha una scheda **Stato**. Tutto viene salvato nel libro man mano
che lo modifichi.

**Traccia un personaggio** chiede un **nome**, facoltativamente **alias** (gli altri nomi con cui il personaggio compare nel
campo *Personaggi presenti* di una parte, separati da virgole), e le **categorie tracciate**. L'elenco propone le categorie del
libro e le tue personali; scegliendone una tua, viene copiata nel libro. Un cavallo può tracciare solo *Ferite*, mentre
l'eroina traccia tutto.

Ogni personaggio tracciato mostra le sue categorie, ciascuna con il valore o i valori attuali:

- **+** imposta il valore di una categoria a valore singolo o descrizione (il valore attuale va nella cronologia) oppure aggiunge
  una voce a una lista.
- La matita modifica un valore, la croce lo **rimuove**: non è più vero, quindi passa nella cronologia e non viene inviato al modello.
- Il lucchetto contrassegna un valore come **bloccato** (una cicatrice, la spada di famiglia). Il riordino, che arriverà con il
  modello, lascia in pace i valori bloccati.
- **Cronologia** mostra i valori passati della categoria e il motivo per cui sono finiti (*sostituito* o *rimosso*).
  **Ripristina** rende di nuovo attuale un valore passato (per una categoria a valore singolo o descrizione, quello attuale va
  nella cronologia al suo posto). Elimina rimuove un valore in modo definitivo.

La matita del personaggio modifica il suo nome, gli alias e le categorie. Deselezionare una categoria la nasconde ma ne
conserva i valori; selezionarla di nuovo li riporta. Il cestino smette di tracciare il personaggio ed elimina tutto il suo stato.

<a id="in-the-prompt"></a>

## Nel prompt

Il pulsante con l'ingranaggio contiene le impostazioni del libro:

- **Nel prompt** inserisce lo stato dei personaggi tracciati della scena nel prompt di ogni generazione (l'ingranaggio diventa
  verde quando è attivo).
- **Profondità** e **Ruolo** indicano dove va, come per la Nota dell'autore: *Profondità* è il numero di messaggi dopo di essa
  (0 dopo le istruzioni del turno, 1 subito prima).
- **Separatori dei nomi** indicano come viene diviso il campo *Personaggi presenti* (per impostazione predefinita `,` `;` `/` `|` e
  a capo). Le note tra parentesi quadre vengono ignorate, quindi `Mara (asleep), Fiora` trova entrambi.

Il prompt riceve i personaggi della scena che hanno qualcosa da dire:

```
The current state of the characters of this scene. Keep to it; it changes only when the story changes it:

Fiora:
- Wearing: torn green cloak, leather boots
- Weapon at hand: none
- Carrying: rope; lantern
```

Puoi inserire lo stesso testo in qualsiasi punto di un modello con la macro
{%{`{{command::state-tracker-block::{{presentCharacters}}}}`}%} (come argomento si può usare qualsiasi elenco di nomi). La macro stampa
lo stato che **Nel prompt** sia attivo o no, quindi disattivalo quando usi la macro, altrimenti il modello riceve lo stato due volte.

<a id="commands"></a>

## Comandi

Per la barra dei comandi e per gli agenti:

| Comando | Cosa fa |
|---|---|
| `state-status` | Le impostazioni, le categorie del libro e quelle tue di cui non ha una copia. |
| `list-state-characters` | I personaggi tracciati con le loro categorie. |
| `track-state-character` | Inizia a tracciare un personaggio: nome, alias, categorie per nome. |
| `stop-tracking-state-character` | Smette di tracciare un personaggio ed elimina il suo stato (chiede prima conferma). |
| `list-character-state` | I valori di un personaggio, attuali e passati, numerati. |
| `set-character-state` | Imposta un valore: personaggio, categoria, testo. |
| `remove-character-state` | Rimuove un valore tramite il suo numero: passa nella cronologia. |
| `state-tracker-block` | Il testo del prompt per un elenco di personaggi (la macro). |
