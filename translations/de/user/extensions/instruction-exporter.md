---
label: Instruction Exporter
order: 12
---

# Instruction Exporter

Kopiert die Anweisungen der Geschichte, mit Szenenrahmen, POV-Figur und anwesenden Figuren, in die Zwischenablage.

<a id="how-it-works"></a>

## So funktioniert es

Die Erweiterung fügt **Export instructions** zum Einstellungsmenü (⚙) des [Story-Editors](../books/story-editor.md) hinzu.
Sie durchläuft die Teile des aktiven Zweigs, vom ersten Teil bis zum aktuellen Ende der Geschichte, sammelt die Felder,
aus denen jeder Teil generiert wurde, und kopiert sie als einen Text. Jedes ausgefüllte Feld wird als sein Name, gefolgt von
einem Doppelpunkt, und seinem Inhalt in der nächsten Zeile geschrieben; die Teile werden durch eine Leerzeile getrennt, in der Reihenfolge der Geschichte:

![Export instructions im Einstellungsmenü](../../images/plugin-instructionexporter.png)

```
Scene Setting:
A harbour town at dawn
POV Character:
Mara
Present Characters:
Mara, the harbourmaster
Instructions:
Mara arrives and is questioned.

Scene Setting:
...
```

Leere Felder werden weggelassen, ebenso Teile, in denen nichts ausgefüllt ist. Füge den Text überall ein - um den Handlungsüberblick des Buches zu bewahren,
um die Anweisungen in einem anderen Buch wiederzuverwenden oder um sie an jemand anderen weiterzugeben.

<a id="usage"></a>

## Verwendung

Es gibt nichts zu konfigurieren. Öffne ein Buch und wähle im Einstellungsmenü *Export instructions*; eine Meldung bestätigt,
dass der Text in der Zwischenablage liegt. Wenn im Zweig keines dieser Felder ausgefüllt ist, wird nichts kopiert und du wirst entsprechend informiert.

Nur der aktive Zweig wird exportiert - um einen anderen zu exportieren, wechsle zuerst zu ihm.

!!!
Browser erlauben einer Seite nur dann, in die Zwischenablage zu schreiben, wenn sie über `https://` oder auf `localhost` geöffnet wurde. Über einfaches `http://` auf einer anderen Adresse hat der Eintrag keine Wirkung.
!!!
