package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * The built-in prompts of the Audiobook in German; the JSON fields and their values are the English ones.
 */
public class AudiobookDefaultsDE extends AudiobookDefaults {

    @Override
    public String labelTemplate() {
        return """
            Du beschriftest die Stücke einer Geschichte für ein Hörbuch mit mehreren Stimmen. Die Stücke sind nummeriert; N ist Erzählung, D ist direkte Rede. {{#if characters}}Die bisher bekannten Figuren sind: {{characters}}. Andere können sprechen; nenne sie so, wie der Text sie nennt.{{else}}Sie sind nicht bekannt: Nenne einen Sprecher so, wie der Text ihn nennt.{{/if}}
            Antworte für jedes Stück mit:
            - "id": die Nummer des Stücks;
            - "speaker": bei D die Figur, die es sagt, aus dem Kontext erschlossen (ein Name in der umgebenden Erzählung, eine Anrede, wer wem antwortet). Bei N ist es "Narrator", außer wenn der Kontext zeigt, dass der Text die eigenen Worte einer Figur ist: ein Brief, eine Notiz, eine Nachricht, ein Schild oder eine Inschrift, die eine Figur geschrieben hat und die die Geschichte zitiert, oder die Gedanken einer Figur. Dann nenne diese Figur (wer schreibt, eine Unterschrift, wer denkt), auch über mehrere Absätze hintereinander. Kursivschrift (Markierungen wie *...* oder _..._) ist nur ein Hinweis: Dieselbe Auszeichnung wird auch für Betonung, Geräusche und Fremdwörter verwendet, die der Erzähler liest. Im Zweifel ist es "Narrator";
            - "delivery": wie es gelesen werden soll ({{deliveries}}), und "intensity" von 0.0 bis 1.0;
            - "tagged_text": bei N eine leere Zeichenkette. Bei D das Stück Wort für Wort, unverändert, außer dass du ein Geräusch-Tag einfügen darfst, wo der Kontext dieses Geräusch der sprechenden Figur eindeutig verlangt, und selten (höchstens ein Tag pro {{wordsPerTag}} Wörter des ganzen Texts). Erlaubte Tags: {{tags}}, genau so geschrieben. Ändere, ergänze oder entferne kein anderes Wort. Für jedes Stück ohne Tag verwende eine leere Zeichenkette: Kopiere nie den Text eines Stücks, das kein Tag bekommt.
            Mit "context" markierte Stücke wurden schon beschriftet: Lies sie, um zu verstehen, wer spricht, beschrifte sie nicht.
            Antworte nur mit dem JSON, ein Eintrag pro Stück, in der Reihenfolge.""";
    }
}
