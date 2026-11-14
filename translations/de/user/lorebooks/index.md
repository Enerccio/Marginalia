---
label: Lorebooks
order: 950
icon: archive
---

# Lorebooks

!!!
Die [Telefonansicht](../phone.md#lorebooks) hat denselben Editor einspaltig: Die Schaltflächen der Lorebooks stehen in einem Menü, die Einstellungen des
Lorebooks in einem Abschnitt, der sich öffnet, und ein Eintrag öffnet sich in einem Formular. Nur wenige Dinge bleiben dem Desktop vorbehalten, siehe die Liste dort.
!!!

Ein Lorebook enthält das, was das Modell über deine Welt wissen muss: Figuren, Orte, Geschichte, Regeln, Fachbegriffe. Jedes
Wissensstück ist ein **Eintrag**. Vor jedem erzeugten Teil entscheidet Marginalia, welche Einträge relevant sind, und fügt sie in den Prompt ein. So weiß das Modell, wer Captain Varro ist, wenn er die Szene betritt, ohne dass jeder Eintrag in jedem Prompt Platz belegt.

![Der Lorebook-Editor](../../images/lorebook.png)

<a id="how-it-fits-together"></a>

## Wie alles zusammenhängt

```
Book ──uses──▶ Lorebook "Saga" ──sub lorebooks──▶ "Characters", "Places", "Magic"
                   │                                   │
                 entries                             entries
```

- Ein **Buch** verwendet ein Lorebook (auf seinem Reiter *Lorebook*).
- Ein Lorebook kann andere Lorebooks als **Sub-Lorebooks** einbinden. Deren Einträge werden ebenfalls berücksichtigt. So kannst du gemeinsames Material (eine Welt, die mehrere Bücher nutzen, ein Figurenensemble) in einem eigenen Lorebook halten und dort einbinden, wo du es brauchst.
- Jeder **Eintrag** enthält den Text, der in den Prompt kommt, und Bedingungen, die festlegen, wann er verwendet wird.

<a id="when-an-entry-is-used"></a>

## Wann ein Eintrag verwendet wird

Für jeden erzeugten Teil wird ein Eintrag **aktiviert**, wenn alle diese Bedingungen erfüllt sind:

1. der Eintrag und sein Lorebook sind eingeschaltet;
2. seine **Tags** passen zu den Tags des Buches (falls er welche hat);
3. sein **Filter** passt zu den Anweisungen für den Teil (falls er einen hat) oder, wenn der Eintrag in die Geschichte hineinschaut, zu den neuesten Teilen der Geschichte.

Aktivierte Einträge werden in ihrer **Reihenfolge** eingefügt, entweder in den Lore-Block des System-Prompts oder direkt vor der Anfrage für den Teil. Siehe [Einträge und Aktivierung](entries.md).

Einträge ohne Tags und ohne Filter sind immer aktiv. Verwende sie für alles, was das Modell immer wissen muss.

<a id="lorebooks-and-other-features"></a>

## Lorebooks und andere Funktionen

- Lorebook-Einträge können [Vorlagen und Makros](../templates/index.md) verwenden. SillyTavern-Weltinformationen funktionieren weiterhin.
- [SillyTavern-Weltinformationen](import-export.md#importing-sillytavern-world-info) können importiert werden.
- [Buch-Sicherungen](../books/backups.md) enthalten das Lorebook des Buches mit seinen Sub-Lorebooks.
- Die Erweiterung [Lore Generator](../extensions/lore-generator.md) schreibt mit dem Modell den Inhalt eines Eintrags, auf Grundlage der Lore, die du bereits hast.
- Die Erweiterung [Lorebook VCS](../extensions/lorebook-vcs.md) führt eine Versionsgeschichte der Einträge.

<a id="in-this-section"></a>

## In diesem Abschnitt

- [Lorebooks anlegen](lorebooks.md): der Lorebook-Editor, Sub-Lorebooks, das Verwenden eines Lorebooks in einem Buch
- [Einträge und Aktivierung](entries.md): Eintragsfelder, Tags, Filter, Reihenfolge und Einfügung
- [Import und Export](import-export.md): Marginalia-Lorebook-Dateien und SillyTavern-Weltinformationen
