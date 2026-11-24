---
label: Gemma 4 Tools-Korrektur
order: 15
---

# Gemma 4 Tools-Korrektur

Sorgt dafür, dass Funktionsaufrufe mit Gemma-4-Modellen funktionieren, deren API für die Ergebnisse der Aufrufe eine andere Rolle erwartet.

!!!warning Bei einer aktuellen Chat-Vorlage nicht nötig
Laden Sie sie nur für ein altes Gemma-4-Modell. Googles Chat-Vorlage für Gemma 4 liest die Ergebnisse mit der Rolle `tool`, also mit der, die Marginalia seit April 2026 sendet (jede spätere GGUF-Datei hat sie). Mit einer solchen Vorlage verschlechtert die Erweiterung den Prompt: Das Ergebnis kommt als zusätzlicher Zug an, den die Vorlage nicht kennt. llama.cpp ignoriert außerdem die alte Form, dort funktioniert nur die Rolle `tool`.

Eine alte GGUF-Datei behält die Vorlage, mit der sie erstellt wurde. Um Ihre zu prüfen, rendern Sie einen Funktionsaufruf mit seinem Ergebnis über llama.cpp mit `/apply-template` und suchen Sie im Modellzug nach `<|tool_response>`: Wenn es dort vorkommt, laden Sie die Erweiterung nicht. Ein Modell, das seine Aufrufe als Text schreibt, statt sie auszuführen, wird stattdessen durch die *Function call rule* in den [Modelleinstellungen](../inference-providers.md) unterstützt.
!!!

<a id="how-it-works"></a>

## So funktioniert es

Ruft das Modell eine Funktion auf, sendet Marginalia das Ergebnis mit der Rolle `tool` zurück. Manche Server, die Gemma 4 ausführen, erwarten stattdessen die Rolle `tool_responses` und antworten sonst mit einem Fehler oder ignorieren das Ergebnis.

Die Erweiterung ändert die Rolle für solche Modelle: Enthält das *Model* eines [Inferenz-Anbieters](../inference-providers.md) `gemma4` oder `gemma-4` (unabhängig von Groß- und Kleinschreibung), werden die Ergebnisse von Funktionsaufrufen an dieses Modell als `tool_responses` gesendet. Anfragen an andere Modelle bleiben unverändert. Die Erweiterung ist eine Portierung der SillyTavern-Erweiterung [SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix).

<a id="usage"></a>

## Verwendung

Es gibt nichts zu konfigurieren. Laden Sie die Erweiterung unter *Admin → Extensions*; sie gilt ab der nächsten Anfrage.
