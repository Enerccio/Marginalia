---
label: Gemma-4-Tools-Fix
order: 15
---

# Gemma-4-Tools-Fix

Ermöglicht Funktionsaufrufe mit Gemma-4-Modellen, deren API für die Ergebnisse der Aufrufe eine andere Rolle erwartet.

<a id="how-it-works"></a>

## So funktioniert es

Wenn das Modell eine Funktion aufruft, sendet Marginalia das Ergebnis an das Modell zurück, und zwar in einer Nachricht mit der Rolle `tool`. Manche Server, die Gemma 4 ausführen, erwarten stattdessen die Rolle `tool_responses` und antworten sonst mit einem Fehler oder ignorieren das Ergebnis.

Die Erweiterung ändert die Rolle für solche Modelle: Enthält das **Modell** eines [Inferenzanbieters](../inference-providers.md) `gemma4` oder `gemma-4` (unabhängig von Groß- und Kleinschreibung), werden die Ergebnisse von Funktionsaufrufen an dieses Modell als `tool_responses` gesendet. Anfragen an andere Modelle bleiben unverändert. Es handelt sich um eine Portierung der SillyTavern-Erweiterung [SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix).

<a id="usage"></a>

## Verwendung

Es ist nichts zu konfigurieren. Lade die Erweiterung unter *Admin → Extensions*; sie gilt ab der nächsten Anfrage.
