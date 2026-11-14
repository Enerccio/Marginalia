---
label: Beispielbuch
order: 80
---

# Beispielbuch

*Unregistered* ist ein fertiges Buch zum Öffnen, Lesen und Auseinandernehmen: etwa 100 Teile (rund 99.000 Wörter) einer einzigen Geschichte, mit allem, was die Funktionen von Marginalia dabei hinterlassen haben. Es ist eine gute Möglichkeit zu sehen, wie ein langes Buch im [Story-Editor](../books/story-editor.md), in den [Zusammenfassungen](../books/summaries.md), den [Anmerkungen](../books/annotations.md) und der [Statistik](../books/statistics.md) aussieht, oder die Erweiterungen an etwas Vorhandenem auszuprobieren.

> Ein tschechischer Kellerführer, der Japanisch aus Romanen gelernt hat, und eine japanische Krankenschwester im Urlaub gehen durch einen zugemauerten Torbogen in einem Prager Keller und erwachen in einem Flusstal, das von einem Reich mit der Sprache Vrakht beherrscht wird.
> Keiner von beiden versteht die Sprache, und ein Beamter für die Lebenssteuer hat zwei Personen gefunden, die nicht in seinem Register stehen.

<a id="what-is-in-it"></a>

## Was darin enthalten ist

| | |
|---|---|
| **Die Geschichte** | 100 Teile auf der Hauptlinie und ein Seitenzweig von 3 Teilen, in 32 Kapiteln (die Überschriften stehen im Text). |
| **Zusammenfassungen** | Eine für jedes Kapitel, egal wie lang (das letzte Kapitel jeder 25 Teile bleibt ganz erhalten, da das Modell es unverändert liest), und die Meta-Zusammenfassungen, die die ältesten zusammenführen, im kompakten Format der Standard-[Zusammenfassungsvorlage](../books/summaries.md). |
| **Anmerkungen** | Etwa 450 Prüfnotizen (Kontinuität, Stil, Lore, Erzählperspektive, Tempo), die die eigene Funktion *Mit KI kommentieren* des Buchs vorgeschlagen hat und die Neufassung umgesetzt hat; jeder Teil hat außerdem seine Versionen (insgesamt 360, siehe [Versionsverlauf](../books/version-history.md)). |
| **Lorebook** | 31 Einträge: die Welt, die Orte, die Personen und die Sprachen. |
| **Prompts** | Jeder Teil behält den Prompt, aus dem er geschrieben wurde, und die aktivierte Lore: **Prompt anzeigen**, **Prompts vergleichen** und **Lore-Nutzung** im Menü des Teils ([Buch-Prompts](../books/prompts.md)). |
| **Knowledge Tracker** | Sieben verfolgte Figuren und was jede von ihnen weiß und nicht weiß, Teil für Teil ([Knowledge Tracker](../extensions/knowledge-tracker.md)). |
| **Übersetzte Rede** | Etwa 1.250 übersetzte Passagen: die Rede des Dominions in **Vrakht** und des Tals in **Aethel-Vael** ([Conlang Translator](../extensions/conlang-translator.md)), sowie **Japanisch** und **Tschechisch** ([Real Language Translator](../extensions/real-language-translator.md)). |
| **Kapitel** | Der Umriss der Geschichte, von [Arc Outline](../extensions/arc-outline.md) aus dem Text erstellt. |
| **Ein Bild** | Eine Illustration in Teil 31. |

Die Daten der vier Erweiterungen sind im Buch enthalten, du siehst sie aber nur, wenn die Erweiterungen geladen sind; ohne sie werden sie aufbewahrt und ignoriert. Das Buch enthält kein Modell und kein Protokoll: Nach dem Import wählst du deins in den
Einstellungen des Buchs.

<a id="download"></a>

## Herunterladen

Die Dateien sind an die Veröffentlichung [example-book-1](https://github.com/Enerccio/Marginalia/releases/tag/example-book-1) angehängt:

| Datei | |
|---|---|
| [`unregistered-book.zip`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-book.zip) (5,7 MB) | Das Buch: die Sicherung mit seinem Bild. |
| [`unregistered-lorebook.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-lorebook.json) | Das Lorebook des Buchs, für sich allein. Die Buchdatei enthält es ebenfalls, du brauchst diese Datei nur, um das Lorebook ohne das Buch anzusehen. |
| [`vrakht.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/vrakht.json) | Die konstruierte Sprache des Dominions, für den Conlang Translator. |
| [`aethel-vael.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/aethel-vael.json) | Die konstruierte Sprache des Tals, für den Conlang Translator. |

<a id="importing-it"></a>

## Den Import durchführen

1. **Die Sprachen** (falls du den Conlang Translator hast): unter **Einstellungen → Erweiterungseinstellungen → Conlang Translator** auf **Importieren** klicken und `vrakht.json` auswählen, danach dasselbe für `aethel-vael.json`
   ([Conlang Translator](../extensions/conlang-translator.md#languages)). Tu das vor dem Buch, damit die Sprachangaben des Buchs etwas haben, worauf sie verweisen können.
2. **Das Buch:** im Tab *Einstellungen* des Buchfensters auf **Sicherung als neues Buch importieren** klicken, einen Namen eingeben und `unregistered-book.zip` hochladen
   ([wie eine Sicherung importiert wird](../books/backups.md#importing-a-backup-as-a-new-book)). Wenn Marginalia nach dem Lorebook fragt, wähle **Importieren**: es ist in der Datei enthalten.
3. Öffne das neue Buch und wähle in seinen Einstellungen dein Modell und Protokoll.

Die übersetzten Passagen werden im Buch gespeichert, sodass die Rede in Vrakht, Aethel-Vael, Japanisch und Tschechisch so angezeigt wird, wie sie übersetzt wurde. Um mit dem Real Language Translator weitere Texte zu übersetzen, legst du dort die Sprachen *Japanisch* und *Tschechisch* an (das Plugin bietet sie als Vorlagen an), da das Plugin seine Sprachen nicht zusammen mit einem Buch exportiert.

<a id="where-it-comes-from-and-the-licence"></a>

## Herkunft und Lizenz

Die Geschichte, das Lorebook und die beiden Sprachen sind Originalwerke. Der Umriss, das Lorebook, die Sprachen und der Plan der Teile wurden vom Autor von Marginalia geschrieben; der Text der Teile wurde **von einem KI-Modell geschrieben** (Claude Haiku) über die eigene Generierungsschleife von Marginalia: generieren, kommentieren, neu fassen, das Wissen verfolgen, übersetzen, zusammenfassen. Es ist eine Demonstration des Programms, kein veröffentlichter Roman, und es trägt die Eigenheiten der Prosa eines Modells.

- Der Text, das Lorebook und die beiden Sprachen sind unter [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/) gemeinfrei gestellt: Verwende sie für alles, was du möchtest.
- Das Bild ist *Interior of a Cottage in Scheveningen* (1887) von Willem Adriaan Alexander Liernur, gemeinfrei, von [Rawpixel](https://www.rawpixel.com/image/13744617/image-face-person-art).
