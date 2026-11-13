package com.github.enerccio.marginalia.extensions.loregenerator.service;

/**
 * The built-in prompts of the Lore Generator in German.
 */
public class DefaultsDE extends Defaults {

    @Override
    public String compactionPrompt() {
        return """
[Systemanweisung: Du bist eine erfahrene Maschine zur Datenverdichtung. Deine einzige Aufgabe ist, den unten gegebenen Rohtext aufzunehmen und in einen maschinenlesbaren, extrem dichten Eintrag eines Lore-Archivs für das Hintergrundgedächtnis eines LLM umzuwandeln.]

KRITISCHE REGELN:

1. Entferne 100 % des atmosphärischen Füllstoffs, der blumigen Prosa, der erzählerischen Hinführung, der Formatierung innerhalb der Welt (etwa Berichtsköpfe oder Grußformeln) und der rhetorischen Ausschmückung.
2. Ziehe nur die harten Fakten heraus: Regeln, räumliche Mechanik, Demografie, körperliche Merkmale, Kennungen, Beweggründe und funktionale Grenzen.
   - ERZÄHLERISCHE AUSNAHME: Enthält der Quelltext eine Anekdote, ein Ereignis aus der Vorgeschichte oder eine historische Erzählung, LÖSCHE sie NICHT. Verdichte sie stattdessen zu einem Block „[Historischer Präzedenzfall]“ oder „[Psychologischer Kern]“. Entferne die erzählende Prosa, aber bewahre die genaue kausale Beziehungsdynamik, das Trauma, den Groll oder den Verhaltensauslöser, den dieses Ereignis geschaffen hat. Nimm die nötigen Details in die historischen Präzedenzfälle auf und behalte ALLE historischen Präzedenzfälle.
3. Gliedere die gesamte Ausgabe mit klaren, fett gesetzten Markdown-Attributen und sauberen, minimalistischen Aufzählungspunkten.
4. Bleibe bei räumlichen oder körperlichen Beschreibungen absolut genau (z. B. bestimmte Gesichtshälften, genaue geometrische Anordnungen, exakte Zahlen).
5. Gib NUR den rohen Markdown-Block aus. Keine Einleitung, kein Schluss, keine Bestätigung im Gesprächston. Beginne sofort mit dem ersten Attributblock.
6. WICHTIG: Gib ihn als kopierbares Markdown in dreifachen Backticks (Codeblock) aus!
7. Fasse KEINE Informationen zusammen, die bereits vorhandene Lore sind.

==== ANFANG DES ROHEN QUELLMATERIALS ====
{{compactionData}}
==== ENDE DES ROHEN QUELLMATERIALS ====
""";
    }

    @Override
    public String generationPrompt() {
        return """
[ Du bist ein erfahrener Weltenbauer und ein Meister sehr bildhafter, atmosphärischer Prosa. Deine Aufgabe ist, einen ausführlichen Lore-Eintrag streng nach den unten angegebenen Parametern zu verfassen. Verwende lebendige Bilder und reiche Sprache und sorge dafür, dass jedes Kerndetail nahtlos eingebettet ist. ]

### Eingabeparameter
- Thema: [Name des Themas einfügen]
- Kerndetails, die eingewoben werden sollen: [konkrete Details, Fakten oder Mechaniken einfügen]
- Ziellänge / Umfang: [z. B. Ausführliche Abhandlung (800+ Wörter), Mittlere Vignette (400 Wörter) oder Mikro-Lore (150 Wörter)]
- Geforderte Gliederung: [Gib genau an, welche Abschnitte geschrieben werden sollen, oder schreibe „Freie Wahl: Erzeuge mindestens X verschiedene thematische Abschnitte“]

### Schreibrichtlinien
1. Ton & Stil: reich, eindringlich und atmosphärisch. Verwende elegante Metaphern und ein immersives Erzählen, das zum Setting passt (Fantasy / Science-Fiction / Gegenwart).
2. Tempo & Tiefe: Baue die Lore ausführlich aus. Nenne nicht nur eine Tatsache; widme dich der Geschichte, der kulturellen Wirkung, dem sinnlichen Erleben und den Gerüchten darum. Halte die gewünschte Ziellänge ein.
3. Gliederung: Du musst der oben angegebenen geforderten Gliederung folgen. Verwende für jeden Abschnitt beschreibende Markdown-Überschriften.
4. Einbindung der Fakten: Hülle die Kerndetails nahtlos in üppige Prosa. Sorge dafür, dass sie unter den Bildern klar genug bleiben, damit ein LLM sie später herauslesen kann.
5. Namenswahl: Wenn du einen Namen erfinden musst, wähle weniger gebräuchliche Namen, die nicht zu Verwechslungen mit bereits erzeugten Lore-Einträgen führen.

### Ausgabe
Gib nur den gestalteten Lore-Eintrag aus. Kein einleitender oder abschließender Gesprächsfüllstoff.
""";
    }
}
