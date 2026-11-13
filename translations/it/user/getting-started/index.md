---
label: Per iniziare
order: 990
icon: rocket
---

# Per iniziare

Marginalia funziona nel browser. Puoi installarlo in due modi:

| | [App desktop](desktop-app.md) | [Server (Docker)](docker-server.md) |
|---|---|---|
| Ideale per | Una persona su un computer | Più utenti, o accesso da altri dispositivi |
| Installazione | Scarica ed esegui, non serve altro | Docker e Docker Compose |
| Indirizzo | `http://127.0.0.1:8765` (solo sul tuo computer) | Porta `8080`, di norma dietro un reverse proxy con HTTPS |
| Dati | `~/.marginalia` | `./data` accanto a `docker-compose.yml` |

Entrambe le versioni sono la stessa applicazione con le stesse funzioni: libri, lorebook, utenti, backup ed estensioni.
Per scrivere da un telefono, avvia la versione server e aprila nel browser del telefono: vedi
[Usare Marginalia sul telefono](../phone.md).

Marginalia non ha un modello proprio. Per scrivere il testo ha bisogno di un'**API compatibile con OpenAI**: un servizio in cloud (OpenAI,
OpenRouter...) oppure un modello in esecuzione sul tuo computer (llama.cpp, LM Studio, Ollama, vLLM...). Tieni pronti l'indirizzo dell'API e una chiave API (un server locale che non richiede una chiave funziona senza, vedi [Aggiungere un provider](../inference-providers.md#adding-a-provider)).

<a id="steps"></a>

## Passaggi

1. Installa Marginalia: [app desktop](desktop-app.md) oppure [server](docker-server.md).
2. [Crea l'account amministratore](first-start.md) al primo avvio.
3. Segui la [guida rapida](quick-start.md): collega un modello, crea un protocollo e scrivi la prima parte di un libro.
4. Facoltativo: apri il [libro di esempio](example-book.md), un racconto completo con riassunti, annotazioni e traduzioni, per vedere com'è fatto un libro lungo.
