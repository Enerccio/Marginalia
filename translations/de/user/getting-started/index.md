---
label: Erste Schritte
order: 990
icon: rocket
---

# Erste Schritte

Marginalia läuft in Ihrem Browser. Sie können es auf zwei Arten installieren:

| | [Desktop-App](desktop-app.md) | [Server (Docker)](docker-server.md) |
|---|---|---|
| Geeignet für | Eine Person auf einem Computer | Mehrere Benutzer oder Zugriff von anderen Geräten |
| Installation | Herunterladen und starten, sonst nichts zu installieren | Docker und Docker Compose |
| Adresse | `http://127.0.0.1:8765` (nur Ihr Computer) | Port `8080`, in der Regel hinter einem Reverse Proxy mit HTTPS |
| Daten | `~/.marginalia` | `./data` neben `docker-compose.yml` |

Beide Versionen sind dieselbe Anwendung mit denselben Funktionen: Bücher, Lorebooks, Benutzer, Backups und Erweiterungen.
Um vom Telefon aus zu schreiben, betreiben Sie die Server-Version und öffnen Sie sie im Browser des Telefons: siehe
[Marginalia auf dem Telefon verwenden](../phone.md).

Marginalia hat kein eigenes Sprachmodell. Zum Schreiben von Text benötigt es eine **OpenAI-kompatible API**: einen Cloud-Dienst (OpenAI,
OpenRouter ...) oder ein Modell, das auf Ihrem eigenen Computer läuft (llama.cpp, LM Studio, Ollama, vLLM ...). Halten Sie die API-Adresse
und einen API-Schlüssel bereit (ein lokaler Server, der keinen Schlüssel verlangt, funktioniert ohne Schlüssel, siehe [Anbieter hinzufügen](../inference-providers.md#adding-a-provider)).

<a id="steps"></a>

## Schritte

1. Installieren Sie Marginalia: [Desktop-App](desktop-app.md) oder [Server](docker-server.md).
2. [Legen Sie das Administratorkonto an](first-start.md) beim ersten Start.
3. Folgen Sie der [Kurzanleitung](quick-start.md): Verbinden Sie ein Modell, erstellen Sie ein Protokoll und schreiben Sie den ersten Teil eines Buches.
4. Optional: Öffnen Sie das [Beispielbuch](example-book.md), eine fertige Geschichte mit Zusammenfassungen, Anmerkungen und Übersetzungen, um zu sehen, wie ein langes Buch aussieht.
