---
label: Prompt-Verlauf
order: 5
---

# Prompt-Verlauf

Zeigt, was tatsächlich an das Modell gesendet wurde und was es geantwortet hat. Das hilft beim Debuggen eines Plugins oder beim Prüfen, was im Prompt steckt, bei anderen Anfragen als einer regulären Generierung.

<a id="how-it-works"></a>

## So funktioniert es

Die Erweiterung speichert die letzten **20 Anfragen**, die in deiner Sitzung an die Modelle gesendet wurden: die gesendeten Nachrichten, die Antwort und die Überlegungen des Modells (sofern der Anbieter sie liefert). Alle Arten von Anfragen werden gespeichert, auch Generierungen. Sie fügt in *Einstellungen → Erweiterungseinstellungen* das Panel **Prompt-Verlauf** hinzu. Mit der Schaltfläche **Prompt-Verlauf anzeigen** öffnest du die Liste, mit der neuesten Anfrage zuerst, mit:

- dem **Datum** der Anfrage,
- dem **Modell**: der Name der KI und die Modell-ID,
- dem **Anfragetyp**: `generation` (ein neuer Teil, ein Swipe, eine Neugenerierung), `annotate`, `summary`, `impersonate`, `redraft`, `agent`, `help` oder der Name der eigenen Arbeitsart einer Erweiterung,
- dem Status (läuft, abgeschlossen, abgebrochen, fehlgeschlagen) und der Dauer.

Wähle eine Anfrage aus, um sie in den Reitern unter der Liste zu lesen: **Anfrage** (jede Nachricht unter ihrer Rolle), **Überlegungen** und **Antwort** (die Antwort, die Funktionen, die das Modell aufgerufen hat, und den Fehler einer fehlgeschlagenen Anfrage). **Aktualisieren** lädt die Liste neu, **Leeren** leert sie. Eine Anfrage erscheint in der Liste, sobald sie gesendet wird: Während sie läuft, wachsen Antwort und Überlegungen im Dialog mit, während das Modell sie schreibt.

![Der Verlauf der Anfragen](../../images/plugin-prompt-history.png)

Für einen [Agenten](../agents.md) enthält die Fußzeile seiner Konsole die Schaltfläche **Prompt-Verlauf**: Sie öffnet dieselbe Liste mit den Anfragen dieses Laufs.

<a id="notes"></a>

## Hinweise

- Der Verlauf wird nur im Arbeitsspeicher gehalten, platzsparend gepackt. Er ist weg, wenn du dich abmeldest oder der Server neu startet, und er ist nicht Teil einer Sicherung.
- Jede Browsersitzung hat eine eigene Liste. Die Anfragen eines Agenten sind darin nicht enthalten: Sie werden je Lauf gespeichert (20 pro Lauf, für die 10 letzten Läufe) und in der Konsole des Laufs angezeigt.
- Die Nachrichten werden vollständig angezeigt, einschließlich des Erzähltexts der Geschichte.
