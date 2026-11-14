---
label: Vorlesen
order: 26
---

# Vorlesen

Liest ein veröffentlichtes Buch im [Viewer](../books/publishing.md) mit der Stimme deines Geräts vor.

<a id="using-it"></a>

## Verwendung

Der Vorleser hat ein kleines Bedienelement neben dem Stil-Button, oben rechts:

- **Abspielen** startet bei dem Teil, der auf dem Bildschirm steht, oder bei dem Teil, an dem deine Textauswahl beginnt. Der gerade gelesene Teil wird hervorgehoben und ins Blickfeld gescrollt, und die folgenden Teile werden während des Vorlesens nachgeladen.
- **Pausieren** und **Stopp**.
- Das **Zahnrad** öffnet die Einstellungen: die **Stimme** (zuerst die Stimmen deines Browsers, und zwar die der Sprache des Buches; der Standard ist die beste Stimme für diese Sprache), die **Geschwindigkeit** und die **Tonhöhe**.

<a id="good-to-know"></a>

## Gut zu wissen

- Die Sprachausgabe wird von deinem Browser erzeugt (`speechSynthesis`). Nichts wird an den Server gesendet, und die Einstellungen werden im Browser gespeichert, gelten also nur für dieses Gerät und diesen Browser. Die Stimmen und ihre Qualität hängen davon ab.
- Vorgelesen wird der Text auf der Seite: Die übersetzte Sprache der Übersetzer für [Conlangs](conlang-translator.md) und [echte Sprachen](real-language-translator.md) wird so vorgelesen, wie das Buch sie anzeigt.
- Pausieren funktioniert nicht auf jedem Telefon; dort stoppt das Vorlesen an der Stelle, an der es war.
- Mit der Erweiterung [Hörbuch](audiobook.md#a-part-without-audio) kann die Stimme des Browsers die Teile vorlesen, die noch kein Audio haben, wenn du dort den Button drückst, den der Player anbietet.
- Das Verlassen des Buches beendet das Vorlesen. Das Bedienelement wird ausgeblendet, wenn der Browser nicht sprechen kann.
