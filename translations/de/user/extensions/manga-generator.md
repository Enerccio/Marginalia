---
label: Manga-Generator
order: 8
---

# Manga-Generator

Verwandeln Sie Ihre Geschichte in einen Manga: Das Modell passt Teile des Buchs an ein Skript aus Seiten und Panels an und schreibt danach für jedes Panel Tags zur Bildgenerierung – bereit für Stable Diffusion, Forge, Midjourney und ähnliche Werkzeuge. Es handelt sich um eine Portierung der Erweiterung [MangaGenerator](https://github.com/Enerccio/SillyTavern-MangaGenerator) von SillyTavern.

<a id="opening-it"></a>

## Öffnen

Wählen Sie **Manga-Generator** im Einstellungsmenü (⚙) des [Story-Editors](../books/story-editor.md). Es öffnet sich ein großes Fenster; auf dem Story-Bildschirm selbst wird nichts hinzugefügt. Sie können das Fenster verschieben und seine Größe ändern.

## Mangas

Ein Buch kann mehrere Mangas haben, die als Reiter oben im Fenster angezeigt werden. Die Schaltflächen neben den Reitern fügen einen Manga hinzu (+), benennen ihn um, verschieben ihn nach links oder rechts und löschen ihn. Ein Manga wird zusammen mit dem Buch gespeichert, ist also in den Sicherungen und Kopien des Buchs enthalten.

Unterhalb der Reiter stehen die Einstellungen des geöffneten Mangas:

| Einstellung | Wofür sie da ist |
|---|---|
| Modell und Protokoll | Wohin die Anfragen gehen. Standardmäßig das eigene des Buchs; die letzte Wahl wird gemerkt. |
| Token-Budget | Wie lang ein Skript ist, das vom Modell angefordert wird (`{{tokenBudget}}` im Prompt). |
| Teile pro Abschnitt | Wie viele Teile der Geschichte ein neuer Abschnitt anpasst. |
| Kontext davor / danach | Wie viele weitere Teile vor und nach denen, die angepasst werden, dem Modell als Hintergrund gegeben werden. |
| Skript-Prompt, Tags-Prompt | Ersetzen die eingebauten Prompts dieses Mangas. **Auf Standard zurücksetzen** stellt den eingebauten Text wieder her. |
| Zusätzliche Regeln | Werden anstelle von `{{additionalRules}}` in den Skript-Prompt eingesetzt, für eigene Regeln (keine Soundeffekte, ein Stil ...). |
| Alles kopieren | Kopiert jedes Skript des Mangas und danach jeden Tags-Text in die Zwischenablage. |
| Dauerbetrieb | Erzeugt den Manga selbstständig, siehe [Dauerbetrieb](#continuous-mode). |

<a id="settings"></a>

## Einstellungen

*Einstellungen → Erweiterungseinstellungen → Manga-Generator-Einstellungen* enthält, womit ein **neuer** Manga beginnt: das Token-Budget, Teile pro Abschnitt, den Kontext davor und danach, den Skript-Prompt, den Tags-Prompt und die zusätzlichen Regeln – sowie einen Text, der vor jede Anfrage gesetzt wird, für Anweisungen, die für alle Ihre Mangas gelten. Ein leer gelassener oder unveränderter Prompt ist der eingebaute. Ein bereits angelegter Manga behält seine eigenen Einstellungen. Das Feld wird mit der Schaltfläche **Speichern** auf der Seite *Einstellungen* gespeichert.

<a id="sections"></a>

## Abschnitte

**Abschnitt hinzufügen** fügt einen Abschnitt für die Teile der Geschichte hinzu, die auf den letzten Abschnitt folgen (der erste beginnt mit Teil 1). Die Teile sind wie in der Gliederung ab 1 nummeriert. Ein Abschnitt zeigt:

- **Anpassen von Teil / bis Teil** – die Teile, die das Modell in Panels umsetzt,
- **Kontext von Teil / bis Teil** – die Teile, die das Modell liest; die außerhalb des angepassten Bereichs dienen nur als Hintergrund (frühere Ereignisse oder das, was danach kommt), und dem Modell wird gesagt, dass es sie nicht anpassen soll. Ändern Sie eine dieser vier Zahlen, wann immer Sie einen anderen Ausschnitt wünschen.

Jeder Abschnitt hat zwei Ansichten, **Skript** und **Tags**:

1. **Skript erzeugen** streamt das Skript in den Abschnitt. Die Überlegungen eines Reasoning-Modells werden darüber angezeigt (*Gedacht 12,3 s*). Das Skript setzt die Seitenzahlen des vorherigen Abschnitts fort und wiederholt nicht, was dieses Skript bereits abdeckt.
2. Wechseln Sie zu **Tags** und wählen Sie **Tags erzeugen**: für jedes Panel des Skripts Tags für die Figuren und die Szene.

![Das Manga-Generator-Fenster mit dem Skript eines Abschnitts](../../images/plugin-manga.png)

Beide Texte können von Hand **bearbeitet** (der Stift), **kopiert** werden, und eine Erzeugung lässt sich mit **Abbrechen** stoppen – was das Modell bis dahin geschrieben hat, bleibt erhalten. Abschnitte werden unabhängig voneinander erzeugt, Sie können also mehrere starten; solange ein Modell schreibt, lassen sich Abschnitte nicht löschen und Mangas nicht verändern.

![Derselbe Abschnitt in der Tags-Ansicht](../../images/plugin-manga-tags.png)

**Überschreiben** gibt einem Abschnitt einen eigenen Skript-Prompt, Tags-Prompt oder eigene zusätzliche Regeln oder einen **Regelzusatz**, der den Regeln des Mangas hinzugefügt wird, statt sie zu ersetzen. Das Auge zeigt die genaue Anfrage, die das Modell erhalten würde, mit ihrer Größe.

<a id="continuous-mode"></a>

## Dauerbetrieb

**Dauerbetrieb** (neben *Abschnitt hinzufügen*) lässt den Manga sich selbst schreiben, jeweils eine Textart nach der anderen, sodass das Modell immer wieder denselben Prompt erhält (und ein Server, der einen Prompt-Cache führt, ihn wiederverwenden kann). Wählen Sie im Feld neben der Schaltfläche, was erzeugt werden soll:

- **Nur Skripte** – Er geht die Abschnitte der Reihe nach durch und erzeugt das Skript jedes Abschnitts, der noch keines hat (ein Abschnitt mit Skript bleibt unverändert). Wenn alle eines haben, fügt er einen Abschnitt für die nächsten Teile der Geschichte hinzu – so wie *Abschnitt hinzufügen* es tut, mit den *Teilen pro Abschnitt* und dem Kontext des Mangas –, erzeugt dessen Skript und macht weiter, bis der Geschichte kein Teil mehr für einen weiteren Abschnitt bleibt.
- **Nur Tags** – Er erzeugt die Tags jedes Abschnitts, der ein Skript, aber keine Tags hat. Er fügt keinen Abschnitt hinzu; er endet, wenn jedes Skript seine Tags hat.

In beiden Fällen endet er, wenn Sie **Dauerbetrieb beenden** drücken (die Schaltfläche wird dazu; was das Modell bis dahin geschrieben hat, bleibt erhalten). Ein Fehler des Modells, ein Abschnitt ohne Text in seinen Teilen oder eine leere Antwort beenden ihn ebenfalls, mit einer Meldung. Während er läuft, können die Abschnitte nicht von Hand erzeugt oder verändert werden, und die Mangas können nicht verändert werden. Wenn Sie das Fenster schließen, während er läuft, wird er in das [Fenster der Läufe](../books/story-editor.md#the-panel-of-the-runs) eingeklappt und läuft weiter; schließen Sie es dort, wird er beendet. Jeder Abschnitt, an dem er arbeitet, wird in die Ansicht gescrollt. Um beides zu erhalten, führen Sie zuerst *Nur Skripte* und danach *Nur Tags* aus.

<a id="the-lore-and-the-size-of-the-context"></a>

## Lorebook und die Größe des Kontexts

Das Modell erhält den Text der Teile des Abschnitts und damit das Lore, das dieser Text verlangt: Einträge des Lorebooks des Buchs (und seiner Unter-Lorebooks), deren Filter in diesem Text vorkommen, gefunden wie bei einer Generierung, einschließlich der Scan-Tiefe. Auch Einträge, die immer aktiv sind, kommen hinzu. So erreichen die Figuren und Orte dieser Teile das Modell mit ihren Beschreibungen, die die Bild-Tags benötigen.

Alles muss in die **Kontextgröße** des Modells passen – den Kontext der KI und des Protokolls abzüglich des Platzes für die Antwort. Passen die Einträge nicht, werden die späteren weggelassen (eine Meldung sagt, wie viele; das Auge zeigt es ebenfalls) – machen Sie den Abschnitt kleiner oder das Lore kürzer. Eine Anfrage, die auch ohne das Lore nicht passt, wird trotzdem mit einer Warnung gesendet.

<a id="what-is-different-from-sillytavern"></a>

## Was anders ist als in SillyTavern

- Eine Chat-Nachricht ist ein **Teil** der Geschichte; vor Nachrichten stehen keine Namen, es gibt keine Persona und keine Charakterkarte – Figuren stammen aus dem Lorebook.
- Der „Trigger“ der Welt-Info aus der Erweiterung wird nicht benötigt: Das Lore wird durch den Text selbst aktiviert.
- Die Kontextgröße wird aus der KI und dem Protokoll übernommen; dafür gibt es keine Einstellung.
- Ein neuer Abschnitt umfasst 3 Teile mit 1 Teil Kontext auf jeder Seite, da ein Teil einer Geschichte weit länger ist als eine Chat-Nachricht. Ändern Sie dies in den Einstellungen (siehe oben).
- Reiter werden mit den Pfeiltasten verschoben, nicht per Ziehen.
