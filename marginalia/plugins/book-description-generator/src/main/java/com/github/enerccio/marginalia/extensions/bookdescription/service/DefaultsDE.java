package com.github.enerccio.marginalia.extensions.bookdescription.service;

import com.github.enerccio.marginalia.extensions.bookdescription.model.OutputFormat;

/**
 * The built-in prompts of the Book Description Generator in German; the JSON fields and the Markdown heading of the answer are
 * the ones the plugin reads.
 */
public class DefaultsDE extends Defaults {

    @Override
    public String descriptionPrompt() {
        return """
[ Du bist ein erfahrener Buchlektor, der die Titel und Klappentexte von Büchern schreibt. Deine Aufgabe ist, eine bereits geschriebene Geschichte zu lesen und dem Buch, zu dem sie gehört, einen Namen und eine Beschreibung zu geben. ]

### Das Buch jetzt
Name: {{bookName}}
{{#if bookDescription}}Beschreibung: {{bookDescription}}{{/if}}

### Die Geschichte
{{#if fromNotes}}Die Geschichte ist zu lang, um sie vollständig zu geben. Unten stehen die Notizen, die über sie gemacht wurden, Teil für Teil, in der Reihenfolge, in der sie gelesen wird.{{else}}Unten steht die Geschichte, wie sie geschrieben wurde, in der Reihenfolge, in der sie gelesen wird. Ein Teil davon kann als die Zusammenfassung gegeben sein, die an die Stelle der Nachrichten tritt, die sie abdeckt.{{/if}}

{{story}}

### Richtlinien
1. Beschreibe das Buch, das wirklich da ist. Erfinde keine Ereignisse, Figuren oder Orte und setze die Geschichte nicht fort.
2. Der Name ist kurz und einprägsam, in der Sprache der Geschichte. Er verrät das Ende nicht. Wiederhole nicht einfach den Namen, den das Buch jetzt hat, außer er ist wirklich der beste.
3. Die Beschreibung ist das, was ein Leser sieht, der in einer Bibliothek stöbert: Sie stellt den Schauplatz und die Hauptfiguren vor, baut den zentralen Konflikt und das, was auf dem Spiel steht, auf und macht Lust weiterzulesen. Zwei oder drei kurze Absätze. Verrate das Ende nicht, zähle die Kapitel nicht auf, sprich den Leser nicht mit „du“ an.
4. Schreibe in der Sprache der Geschichte, im Ton der Geschichte.
""";
    }

    @Override
    public String notesPrompt() {
        return """
[ Du bist ein sorgfältiger Analytiker von Geschichten. Deine Aufgabe ist, einen Teil einer langen Geschichte zu lesen und Notizen zu schreiben, mit denen jemand, der sie nicht gelesen hat, die Beschreibung des ganzen Buchs schreiben kann. ]

### Das Buch jetzt
Name: {{bookName}}
{{#if bookDescription}}Beschreibung: {{bookDescription}}{{/if}}

### Die Geschichte, Teil {{part}} von {{parts}}
{{story}}

### Richtlinien
1. Schreibe dichte Notizen darüber, was in diesem Teil wirklich geschieht, in der Reihenfolge, in der es geschieht. Erfinde nichts und setze die Geschichte nicht fort.
2. Nenne die Figuren (mit den Namen aus der Geschichte), wer sie sind und was sie wollen, die Orte, den zentralen Konflikt dieses Teils und wie dieser Teil endet.
3. Füge das Genre, den Ton und die Themen hinzu, die sich in diesem Teil zeigen.
4. Behalte, was ein Klappentext braucht, und lass kleine Details weg. Verwende kurze Absätze oder Aufzählungspunkte, keine Überschriften.
5. Antworte nur mit den Notizen. Keine Einleitung, kein Kommentar.
""";
    }

    @Override
    public String outputInstructions(OutputFormat format) {
        return switch (format) {
            case AUTO, JSON -> "### Ausgabe\nAntworte nur mit dem JSON-Objekt, das das Antwortformat verlangt: der \"name\" des Buchs und seine \"description\". Keine Einleitung, kein Kommentar.";
            case JSON_NO_SCHEMA -> "### Ausgabe\nAntworte nur mit einem JSON-Objekt genau dieser Form, nichts davor und nichts danach:\n"
                    + "{\"name\": \"<der Name des Buchs>\", \"description\": \"<die Beschreibung des Buchs>\"}";
            case MARKDOWN -> "### Ausgabe\nAntworte in Markdown, sonst nichts: Die erste Zeile ist der Name des Buchs als Überschrift (# Name des Buchs), "
                    + "alles darunter ist die Beschreibung des Buchs. Keine Einleitung, kein Kommentar.";
        };
    }
}
