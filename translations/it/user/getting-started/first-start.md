---
label: Primo avvio
order: 80
---

# Primo avvio

<a id="create-the-administrator"></a>

## Crea l'amministratore

Quando Marginalia si avvia con un database vuoto, chiede il primo account invece di mostrare la schermata di accesso.
Questo account è l'**amministratore**: può gestire gli utenti, i backup del database, la pulizia e le estensioni.

![Creazione dell'account amministratore](../../images/first-start.png)

Compila:

| Campo | |
|---|---|
| **Nome utente** | Il nome di accesso. |
| **Nome completo** | Il nome mostrato nell'applicazione. |
| **Password**, **Ripeti la password** | Devono coincidere. Marginalia accetta anche una password vuota: non lasciarla mai vuota su un server, perché chiunque potrebbe accedere come amministratore usando solo il nome utente. |

*Amministratore* è selezionato e non può essere modificato per il primo account. Clicca su **OK**. La pagina si ricarica e mostra la
schermata di accesso.

!!!
Ricordati la password. Non esiste il ripristino della password via e-mail. Altri amministratori possono impostare una nuova password per un
utente in *Admin → Users*.
!!!

<a id="log-in"></a>

## Accedi

Accedi con il nuovo account. Con **Ricorda l'accesso** selezionato, il browser resta connesso per 30 giorni. Questo funziona solo
tramite HTTPS o su `127.0.0.1` con l'app desktop (vedi [Server](docker-server.md#https-and-reverse-proxy)).

![La schermata di accesso](../../images/login.png)

La versione di Marginalia e l'id breve del commit Git da cui è stata compilata (per esempio `Marginalia 1.5.0 (93ec118)`) sono
mostrati sotto il modulo di accesso.

<a id="the-workspace"></a>

## L'area di lavoro

Dopo l'accesso vedi l'area di lavoro. Le schede a sinistra sono:

| Scheda | |
|---|---|
| **Libri** | I tuoi libri. Aprendone uno si mostra il suo editor della storia. Vedi [Libri](../books/index.md). |
| **Lorebook** | Note su mondo e personaggi per il prompt. Vedi [Lorebook](../lorebooks/index.md). |
| **Impostazioni** | Le tue impostazioni personali: modello e protocollo predefiniti per i nuovi libri, modelli di prompt, backup. Vedi [Account e impostazioni](../account.md). |
| **Protocolli** | Impostazioni di generazione. Vedi [Protocolli](../protocols.md). |
| **Provider di inferenza** | I modelli a cui Marginalia si connette. Vedi [Provider di inferenza](../inference-providers.md). |
| **Risorse** | I file che hai caricato. Vedi [Risorse](../resources.md). |
| **Cestino** | Ciò che hai eliminato, finché un amministratore non esegue la pulizia. Vedi [Cestino](../trash.md). |

In basso ci sono **Admin** (solo per gli amministratori), il selettore **Lingua**, **Cambia password** ed **Esci**
(vedi [Amministrazione](../administration/index.md) per le pagine *Admin*).

![L'area di lavoro dopo il primo accesso](../../images/workspace.png)

Lo stesso testo della versione, in caratteri piccoli, si trova nell'angolo in basso a sinistra della pagina; passaci sopra con il mouse per vedere l'id completo del commit e
la data del commit e della compilazione. Indicalo quando segnali un problema. Una build eseguita da una copia di sviluppo
(`mvn jetty:run`) mostra `dev` al posto della versione.

Marginalia ricorda dove eri. La volta successiva che accedi (da qualsiasi dispositivo) apre la stessa scheda, con gli stessi filtri
dell'elenco dei libri, lo stesso lorebook e le stesse voci del lorebook aperte e, se avevi lasciato aperto un libro, quel libro sulla
stessa scheda - con la scena, il personaggio POV, i personaggi presenti e le istruzioni che avevi scritto per la parte successiva. Ciò
che non esiste più (un libro, un lorebook o un tag eliminato) viene saltato; un libro eliminato non viene aperto.

Continua con la [guida rapida](quick-start.md).
