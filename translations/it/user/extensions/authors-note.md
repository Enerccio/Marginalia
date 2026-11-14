---
label: Nota dell'autore
order: 23
---

# Nota dell'autore

Una nota per il modello, inviata con ogni generazione del libro: il tono che vuoi in questo momento, di cosa tratta il capitolo corrente, un promemoria che il modello continua a dimenticare. A differenza delle istruzioni di un turno, la nota resta finché non la cambi.

<a id="the-authors-note-tab"></a>

## La scheda Nota dell'autore

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) mostra una scheda **Nota dell'autore** accanto a *Struttura*. Le modifiche vengono salvate nel libro mentre scrivi e usate dalla generazione successiva.

![La scheda Nota dell'autore](../../images/plugin-authorsnote.png)

| Campo | |
|---|---|
| **Inserisci nel prompt** | Attiva o disattiva la nota senza eliminarla. |
| **Profondità di inserimento** | Dove va la nota, contata in messaggi dalla fine del prompt: `0` - dopo le istruzioni del turno (l'ultimo messaggio in assoluto), `1` - subito prima di esse, `2` - prima dell'ultima parte della storia, e così via. Una profondità elevata colloca la nota subito dopo il prompt di sistema. |
| **Ruolo** | Se la nota viene inviata come messaggio *Sistema*, *Utente* o *Assistente*. |
| **Nota dell'autore** | Il testo inviato al modello. Una nota vuota non viene inviata. Sotto di essa, una stima della sua dimensione in token. |
| **Nota dell'autore (privata)** | Note per te stesso: piani, promemoria. Non vengono mai inviate al modello. |

!!!
Più la nota è vicina alla fine del prompt, più forte è il suo effetto. Profondità `1` con ruolo *Sistema* è un buon punto di partenza.
La nota non viene elaborata come [template](../templates/macros.md): le macro vengono inviate così come sono scritte.
!!!

<a id="how-the-prompt-looks"></a>

## Com'è fatto il prompt

Una generazione invia per primo il prompt di sistema, poi la storia come chat - ogni parte come una risposta del modello a *Genera storia* - e per ultime le istruzioni del turno. Con profondità `1` la nota viene inserita subito prima delle istruzioni:

1. prompt di sistema (template, lore, riassunti)
2. parti della storia…
3. **la Nota dell'autore**
4. le istruzioni del turno

I token della nota vengono sottratti lo spazio disponibile per la storia: una nota più lunga significa meno parti di storia nel prompt. Una nota che non lascia spazio nemmeno al resto del prompt interrompe la generazione con *Limite contestuale insufficiente*.

<a id="the-note-as-a-macro"></a>

## La nota come macro

Al posto dell'inserimento automatico (o oltre a esso), puoi collocare la nota da solo con la macro [{%{`{{command::authors-note}}`}%}](../templates/macros.md#commands): inseriscila nel template principale, nel prompt utente o in una voce del lorebook, e stamperà il testo della nota nel punto del prompt che preferisci. Stampa la nota così come è memorizzata in questo momento, che **Inserisci nel prompt** sia attivo o no, quindi disattivalo quando colloca la nota con la macro, altrimenti il modello la riceve due volte. La profondità e il ruolo non si applicano alla macro (la nota fa parte del messaggio che contiene la macro), e una nota vuota non stampa nulla.

<a id="storage"></a>

## Archiviazione

La nota viene memorizzata insieme al libro, quindi fa parte dei [backup del libro](../books/backups.md) e delle copie.
