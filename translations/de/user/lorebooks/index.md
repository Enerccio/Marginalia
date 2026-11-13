---
label: Lorebooks
order: 950
icon: archive
---

# Lorebooks

!!!
Das [Telefon-Layout](../phone.md#lorebooks) hat denselben Editor in einer Spalte: Die Schaltflächen der Lorebooks befinden sich in einem Menü, die Einstellungen des
Lorebooks in einem Abschnitt, der sich öffnet, und ein Eintrag öffnet sich als Formular. Nur wenige Dinge bleiben dem Desktop vorbehalten, siehe die Liste dort.
!!!

Ein Lorebook enthält, was das Modell über deine Welt wissen muss: Figuren, Orte, Geschichte, Regeln, Fachbegriffe. Jedes
Wissensstück ist ein **Eintrag**. Vor jedem generierten Teil entscheidet Marginalia, welche Einträge relevant sind, und fügt
sie in den Prompt ein – so weiß das Modell, wer Hauptmann Varro ist, wenn er die Szene betritt, ohne dass jeder Eintrag in jedem Prompt Platz beansprucht.

![Der Lorebook-Editor](../../images/lorebook.png)

<a id="how-it-fits-together"></a>

## So greift alles ineinander

```
Buch ──verwendet──▶ Lorebook "Saga" ──Unter-Lorebooks──▶ "Figuren", "Orte", "Magie"
                   │                                   │
                 Einträge                            Einträge
```

- Ein **Buch** verwendet ein Lorebook (auf seinem Reiter *Lorebook*).
- Ein Lorebook kann andere Lorebooks als **Unter-Lorebooks** einbinden. Deren Einträge wirken ebenfalls mit, sodass du gemeinsam genutztes Material (eine Welt, die mehrere Bücher verwenden, ein Figurenensemble) in einem eigenen Lorebook aufbewahren und dort einbinden kannst, wo es benötigt wird.
- Jeder **Eintrag** enthält den Text, der in den Prompt gelangt, sowie Bedingungen, die festlegen, wann er verwendet wird.

<a id="when-an-entry-is-used"></a>

## Wann ein Eintrag verwendet wird

Für jeden generierten Teil wird ein Eintrag **aktiviert**, wenn alle der folgenden Punkte zutreffen:

1. der Eintrag und sein Lorebook sind aktiviert;
2. seine **Tags** passen zu den Tags des Buches (sofern er welche hat);
3. sein **Filter** passt zu den Anweisungen für den Teil (sofern er einen hat) oder, wenn der Eintrag in die Geschichte hineinschaut, auch zu den neuesten Teilen der Geschichte.

Aktivierte Einträge werden in ihrer **Reihenfolge** eingefügt – in den Lore-Block des System-Prompts oder direkt vor die Anfrage für den Teil. Siehe [Einträge & Aktivierung](entries.md).

Einträge ohne Tags und ohne Filter sind immer aktiv: Verwende sie für alles, was das Modell stets wissen muss.

<a id="lorebooks-and-other-features"></a>

## Lorebooks und andere Funktionen

- Lorebook-Einträge können [Vorlagen und Makros](../templates/index.md) verwenden – SillyTavern-Welt-Infos funktionieren weiterhin.
- [SillyTavern-Welt-Infos](import-export.md#importing-sillytavern-world-info) können importiert werden.
- [Buch-Sicherungen](../books/backups.md) enthalten das Lorebook des Buches mit seinen Unter-Lorebooks.
- Die Erweiterung [Lore-Generator](../extensions/lore-generator.md) schreibt den Inhalt eines Eintrags mithilfe des Modells, auf Grundlage der Lore, die du bereits hast.
- Die Erweiterung [Lorebook-VCS](../extensions/lorebook-vcs.md) führt einen Revisionsverlauf der Einträge.

<a id="in-this-section"></a>

## In diesem Abschnitt

- [Lorebooks anlegen](lorebooks.md) – der Lorebook-Editor, Unter-Lorebooks, Verwenden eines Lorebooks in einem Buch
- [Einträge & Aktivierung](entries.md) – Eintragsfelder, Tags, Filter, Reihenfolge und Einfügen
- [Import & Export](import-export.md) – Marginalia-Lorebook-Dateien und SillyTavern-Welt-Infos
