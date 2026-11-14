---
label: Home
icon: home
order: 1000
---

# Marginalia

Marginalia è uno strumento self-hosted per la scrittura di narrativa lunga, co-scritta con modelli linguistici di grandi dimensioni. Tu scrivi le istruzioni per ogni parte della storia, il modello scrive la prosa e Marginalia tiene in ordine tutto il resto: la storia a ramificazioni, il lore del mondo, i riassunti che permettono ai libri lunghi di stare nel contesto del modello, e le impostazioni del prompt che definiscono lo stile.

Funziona nel browser — come applicazione desktop sul tuo computer, oppure come piccolo server per te e i tuoi amici — e lavora con qualsiasi API compatibile con OpenAI: OpenAI, OpenRouter, LiteLLM, llama.cpp, LM Studio, Ollama, vLLM e altre.

![The story editor](images/hero-story-editor.png)

[!button variant="primary" text="Get started"](user/getting-started/index.md)
[!button text="Download"](https://github.com/Enerccio/Marginalia/releases)
[!button text="Source on GitHub"](https://github.com/Enerccio/Marginalia)

<a id="what-it-does"></a>

## Cosa fa

- **Libri con storie ramificate** – rigenera o scorri una parte, dirama da qualsiasi punto, muoviti nell'albero della storia, modifica il testo generato. Vedi [Libri](user/books/index.md).
- **Lorebook** – note su mondo e personaggi inserite nel prompt solo quando servono: tramite tag del libro, filtri di testo o regex, lorebook annidati. È possibile importare il world info di SillyTavern. Vedi [Lorebook](user/lorebooks/index.md).
- **Template e macro** – i prompt sono template Handlebars con macro compatibili con SillyTavern, così il lore importato continua a funzionare. Vedi [Template e macro](user/templates/index.md).
- **Riassunti** – le parti più vecchie vengono condensate, così i libri lunghi entrano nella finestra di contesto. Vedi [Riassunti](user/books/summaries.md).
- **Qualsiasi modello compatibile con OpenAI** – inclusi i modelli di ragionamento, con limiti per modello e impostazioni di generazione commutabili. Vedi [Provider di inferenza](user/inference-providers.md) e [Protocolli](user/protocols.md).
- **Backup** – backup per singolo libro con ripristino e copia, e backup completi del database su pianificazione. Vedi [Backup del libro](user/books/backups.md) e [Backup del database](user/administration/database-backups.md).
- **Più utenti** – ciascuno con i propri libri, lore e modelli; gli amministratori gestiscono utenti, backup e pulizia. Vedi [Amministrazione](user/administration/index.md).
- **Libri pubblicati** – una vista di sola lettura che puoi condividere. Vedi [Pubblicazione e visualizzatore](user/books/publishing.md).
- **Ricerca e sostituzione, comandi** – cerca e sostituisci nella storia e nei lorebook, e usa una riga di comando accanto al menu della storia con comandi che leggono e modificano il libro. Vedi [Comandi](user/commands/index.md) e [Espressioni regolari](user/regular-expressions.md).
- **Agenti** – un modello che lavora da solo su un libro tramite i comandi, su una copia: lo osservi, gli parli in una console e revisioni le sue modifiche prima che raggiungano il libro. Vedi [Agenti](user/agents.md).
- **Estensioni** – i plugin aggiungono funzioni a runtime: segnalibri di capitolo, cronologia dei lorebook, revisioni IA, domande secondarie al modello. Vedi [Estensioni](user/extensions/index.md).

<a id="which-guide-do-you-need"></a>

## Quale guida ti serve?

|                                           |                                                                                                                                   |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **[Guida utente](user/index.md)**         | Vuoi installare Marginalia, eseguirlo per altri o scrivere con esso. Inizia da [Per iniziare](user/getting-started/index.md). |
| **[Guida per sviluppatori](developer/index.md)** | Vuoi compilare Marginalia dal sorgente, modificarlo, scrivere un'estensione o capire come funziona internamente.                       |

<a id="quick-links"></a>

## Collegamenti rapidi

| Voglio...                      |                                                          |
|--------------------------------|----------------------------------------------------------|
| Installarlo sul mio computer   | [App desktop](user/getting-started/desktop-app.md)       |
| Eseguirlo su un server         | [Server (Docker)](user/getting-started/docker-server.md) |
| Collegare un modello           | [Provider di inferenza](user/inference-providers.md)     |
| Scrivere la prima parte di un libro | [Avvio rapido](user/getting-started/quick-start.md) |
| Usare i miei lorebook di SillyTavern | [Importazione ed esportazione](user/lorebooks/import-export.md) |
| Risolvere un problema          | [Risoluzione dei problemi e FAQ](user/troubleshooting.md) |
| Scrivere un'estensione         | [Sviluppo di plugin](developer/plugins/index.md)         |
| Segnalare un bug o contribuire | [Contribuire](developer/contributing.md)                 |

<a id="about"></a>

## Informazioni

Marginalia è open source con licenza [MIT](https://github.com/Enerccio/Marginalia/blob/master/LICENSE).
Le immagini usate negli screenshot di questo manuale sono citate nella pagina [Crediti](credits.md).
Le release sono su [GitHub](https://github.com/Enerccio/Marginalia/releases); bug e idee vanno nel [tracker dei problemi](https://github.com/Enerccio/Marginalia/issues).
