package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

/**
 * The built-in system messages of the Knowledge Tracker in German; the JSON of the answer, its values ("knows",
 * "does-not-know"), the tool name and the marker "learned in part N" stay English.
 */
public class KnowledgeTrackerDefaultsDE extends KnowledgeTrackerDefaults {

    @Override
    public String extractionSystem() {
        return """
            Du verfolgst, was die Figuren einer Geschichte wissen. Du bekommst die Welt (das Lorebook, falls gegeben), die VERFOLGTEN Figuren mit den nummerierten Einträgen dessen, was jede jetzt weiß oder nicht weiß, und die neuen NACHRICHTEN der Geschichte (nummeriert). Melde für jede verfolgte Figur nur, was sich in diesen Nachrichten geändert hat.

            Regeln:
            1. add: Wissen, das die Figur in diesen Nachrichten GEWINNT: was sie gesehen, gehört, gelesen hat, was ihr gesagt wurde oder was sie selbst erschlossen hat. Nicht, was der Leser weiß, nicht, was nur der Erzähler sagt, nicht, was andere Figuren wissen.
            2. Füge KEIN allgemeines Wissen hinzu, das ein Bewohner dieser Welt ohnehin hat (wer der König ist für einen Untertan des Reichs, übliche Bräuche, die Sprache, die Geografie der eigenen Heimat), und nichts, was aus der Figur selbst folgt, wie der Welttext sie beschreibt (ihre Art, ihr Körper, ihre Fähigkeiten, ihre Geschichte, ihre Familie, ihr Beruf).
            3. Füge NICHTS hinzu, was die Einträge der Figur schon sagen.
            4. retract: die Nummer eines Eintrags, dem die Nachrichten klar widersprechen (zum Beispiel „ist X nie begegnet“, nachdem sie sich begegnet sind). Nur wenn der Text den Widerspruch zeigt. Ein Eintrag, der sagt, dass eine Figur Y nicht weiß, wird nur zurückgezogen, wenn die Figur Y in diesen Nachrichten erfährt. Ein Eintrag, der mehr als eine Sache sagt, wird nur zurückgezogen, wenn die Nachrichten ihm ganz widersprechen; wird nur ein Teil widerlegt, lass den Eintrag stehen und füge hinzu, was erfahren wurde. Beurteile einen Eintrag danach, wie die Nachrichten ENDEN: Wenn der Figur später in diesen Nachrichten gesagt, in Erinnerung gerufen oder von ihr wieder erschlossen wird, was der Eintrag sagt (sie fragt, wer jemand ist, und bekommt es dann gesagt), ist der Eintrag am Ende wahr; zieh ihn also nicht zurück und füge ihn nicht noch einmal hinzu.
            5. Schließe nie etwas nur. Im Zweifel melde nichts. Eine Figur, die nicht anwesend ist oder ein Ereignis nicht sieht oder hört (private Gedanken, Geflüster, ein verschlossener Brief), erfährt nichts daraus.
            6. Jede Änderung braucht ein Belegzitat, das aus den Nachrichten kopiert ist, und die Nummer der Nachricht.
            7. Halte nur Wissen fest, das später in der Geschichte zählen kann: Geheimnisse, Identitäten, offenbarte Beziehungen und Gefühle, Pläne, Versprechen, Entdeckungen und Tatsachen, die eine andere Figur nicht weiß. Halte KEINE Ereignisse fest, die alle anwesenden Figuren gemeinsam gesehen oder gehört haben (der Text der Geschichte enthält sie selbst), keinen Small Talk, keine Witze, Stimmungen, kein Wetter, keine Gegenstände und Details der Umgebung, nicht, was eine Figur selbst getan hat, und nicht dieselbe Tatsache für mehrere Figuren. Höchstens 3 Ergänzungen pro Figur, meist 0 oder 1. Wenn nichts zählt, melde nichts.

            Antworte nur mit JSON, ohne anderen Text: {"characters":[{"name":"...","retract":[{"item":N,"evidence":"...","message":N}],"add":[{"kind":"knows|does-not-know","text":"...","evidence":"...","message":N}]}]}. Eine Figur ohne Änderungen hat leere Listen.""";
    }

    @Override
    public String readingNote() {
        return """
            Du kannst auch read_part mit der Nummer eines früheren Teils der Geschichte aufrufen. Einträge, die in einem früheren Teil erfahren wurden, sagen das ("learned in part N"), und die Nachrichten sagen, welcher Teil sie sind. Wenn ein Eintrag aus Hörensagen, einem Gerücht, einem Plan oder einer Vorstellung stammt und diese Nachrichten ihn zu bestätigen, zu wiederholen oder ihm zu widersprechen scheinen, oder wenn der Eintrag allein nicht sagt, was die Figur zu diesem Zeitpunkt wusste, lies zuerst diesen Teil und entscheide dann, statt zu raten. Lies Teile aus keinem anderen Grund (höchstens %d). Deine endgültige Antwort ist weiterhin nur das JSON.""".formatted(PartReader.MAX_READS);
    }

    @Override
    public String tidySystem() {
        return """
            Du räumst auf, was die Figuren einer Geschichte wissen. Du bekommst die VERFOLGTEN Figuren mit den nummerierten Einträgen dessen, was sie wissen oder nicht wissen, die Einträge, die bleiben, wie sie sind (nur zum Vergleich), und die letzten Teile der Geschichte als Kontext. Die Einträge werden an ein Schreibmodell geschickt, damit es einer Figur kein Wissen gibt, das sie nicht haben kann; jeder Eintrag kostet Platz und muss ihn wert sein.

            Regeln:
            1. drop: Entferne einen Eintrag, der es nicht wert ist, behalten zu werden: eine Belanglosigkeit (ein Detail der Szene, Small Talk, ein Witz, eine Stimmung, ein Gegenstand), ein Ereignis, das alle Figuren der Szene gemeinsam gesehen haben (die Geschichte enthält es schon), etwas, das spätere Ereignisse überholt haben, oder etwas, das dasselbe sagt wie ein anderer Eintrag oder wie ein Eintrag, der bleibt.
            2. merge: Fasse zwei oder mehr Einträge zur selben Sache zu einem Eintrag zusammen, der jede ihrer Tatsachen behält. Füge nichts hinzu, was nicht in den Einträgen steht.
            3. Entferne oder verbinde NIE ein Geheimnis, eine Identität, eine Beziehung, ein offenbartes Gefühl, einen Plan, ein Versprechen, eine Entdeckung oder etwas, das eine andere Figur nicht weiß, auch wenn die letzten Teile es nicht erwähnen: Dass ein Eintrag zuletzt nicht vorkam, ist kein Grund, ihn zu entfernen.
            4. Im Zweifel behalte ihn. Melde nur, wessen du dir sicher bist; leere Listen sind normal.
            5. Jedes drop und jedes merge braucht eine kurze Begründung.

            Antworte nur mit JSON, ohne anderen Text: {"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"merge":[{"items":[N,N],"kind":"knows|does-not-know","text":"...","reason":"..."}]}]}. Eine Figur, bei der nichts zu tun ist, hat leere Listen.""";
    }
}
