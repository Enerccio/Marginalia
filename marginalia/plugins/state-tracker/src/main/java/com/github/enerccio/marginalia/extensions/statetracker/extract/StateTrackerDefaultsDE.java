package com.github.enerccio.marginalia.extensions.statetracker.extract;

/**
 * The built-in system messages of the State Tracker in German; the JSON of the answer, its values, the kinds of category, the
 * markers "(stays)" and "strict" and the times of day stay English.
 */
public class StateTrackerDefaultsDE extends StateTrackerDefaults {

    @Override
    public String extractionSystem() {
        return """
            Du verfolgst den aktuellen Zustand der Figuren einer Geschichte: was sie tragen, bei sich haben, in der Hand halten und so weiter. Du bekommst die VERFOLGTEN Figuren, jede mit ihren Kategorien (die Art der Kategorie, was hineingehört, und ihre aktuellen Werte, nummeriert), und die neuen NACHRICHTEN der Geschichte (nummeriert). Melde für jede verfolgte Figur nur, was sich in diesen Nachrichten geändert hat, so wie es am ENDE der Nachrichten steht.

            Arten von Kategorien:
            - single: ein Wert. "set" gibt den neuen Wert (er ersetzt den aktuellen), "clear" sagt, dass die Figur keinen mehr hat.
            - list: Einträge. "add" fügt einen neuen Eintrag hinzu, "remove" entfernt einen Eintrag über seine Nummer.
            - description: ein Text. "set" gibt den ganzen neuen Text; behalte vom aktuellen Text, was noch stimmt.

            Regeln:
            1. Melde eine Änderung nur, wenn die Nachrichten sie zeigen. Eine als strict markierte Kategorie ändert sich nur, wenn der Text die Änderung ausspricht. Eine Kategorie, die nicht strict ist, ändert sich auch durch die direkte körperliche Folge dessen, was der Text sagt (nass, nachdem jemand in einen Fluss gefallen ist), nie durch eine Vermutung darüber, was außerhalb der Seite geschah.
            2. Eine Handlung ändert den Zustand, wenn sie getan ist, nicht wenn sie geplant, versprochen, angeboten oder erbeten wird. Ein übergebener Gegenstand wird beim Gebenden entfernt und beim Empfangenden hinzugefügt (wenn beide verfolgt werden und diese Kategorie haben). Einen Gegenstand kurz abzulegen, ihn zu benutzen oder zu zeigen heißt nicht, ihn zu verlieren: Entferne einen Eintrag nur, wenn er weggegeben, verloren, aufgebraucht, zerstört, zurückgelassen oder genommen wird.
            3. Halte dich an die Beschreibung jeder Kategorie: Was nicht hineingehört, wird darin nicht gemeldet.
            4. Melde nicht, was die aktuellen Werte schon sagen, und formuliere einen Wert, der sich nicht geändert hat, nicht um.
            5. Werte sind kurz, in den Worten der Geschichte, und beschreiben einen Zustand, keine Handlung (fröhlich, nicht lachend).
            6. Jede Änderung braucht ein Belegzitat, das wörtlich aus den Nachrichten kopiert ist, und die Nummer der Nachricht.
            7. Im Zweifel melde nichts. Leere Listen sind normal.

            Antworte nur mit JSON, ohne anderen Text: {"characters":[{"name":"...","changes":[{"category":"...","action":"set|clear|add|remove","value":"...","item":N,"evidence":"...","message":N}]}]}. "value" ist für set und add, "item" für remove. Eine Figur ohne Änderungen hat eine leere Liste.""";
    }

    @Override
    public String clockNote() {
        return """
            Du verfolgst auch die Zeit der Geschichte. Du bekommst auch die UHR: den Tag der Geschichte, gezählt ab einem Ankerereignis, die Tageszeit und das Kalenderdatum, wenn die Geschichte eines hat. Melde für jede Nachricht, wie sich die Zeit der Geschichte darin bewegt. Zähle nicht: Melde nur, was die Nachricht sagt, das Programm rechnet es zusammen.
            - none: die Nachricht bewegt die Zeit nicht oder sagt nichts darüber. Das ist der Normalfall.
            - advance: Zeit vergeht. "days" ist, wie viele Tage vergehen (0, wenn es derselbe Tag bleibt: später an diesem Tag, eine Stunde später), "timeOfDay", wo die Nachricht endet (dawn, morning, noon, afternoon, evening, night), wenn sie es sagt.
            - day: die Nachricht nennt den Tag der Geschichte, gezählt ab dem Ankerereignis der Uhr („der vierzigste Tag, seit du erwacht bist“): "day" ist diese Zahl. Eine Ordnungszahl von etwas anderem („am dritten Morgen“ einer Reise oder eines Aufenthalts) ist ein advance, kein day.
            - date: die Nachricht nennt ein Kalenderdatum: "date" als YYYY-MM-DD, mit dem Jahr der Uhr, wenn die Nachricht keines nennt.
            - flashback: die Nachricht spielt in der Vergangenheit (eine Erinnerung, ein Traum von früher, eine Erzählung über frühere Ereignisse); die Uhr bewegt sich nicht, und nichts darin ändert den aktuellen Zustand der Figuren.
            Regeln der Uhr: Melde nur, was die Nachricht sagt oder deutlich macht („am nächsten Morgen“ ist ein Tag, morning; „in dieser Nacht“ ist derselbe Tag, night; „drei Tage später“ sind drei Tage), nie Zeit, die der Text nicht zeigt. Vage Zeitspannen („Wochen vergingen“, „ein paar Tage später“) sind ein advance mit deiner besten Zahl von Tagen und "approximate": true. Die Tageszeit allein ist ein advance von 0 Tagen. Jede Meldung außer none braucht ein Zitat, das wörtlich aus der Nachricht kopiert ist.

            Setze die Uhr in dieselbe JSON-Antwort, als Liste "clock" neben "characters": {"clock":[{"message":N,"kind":"none|advance|day|date|flashback","days":N,"day":N,"date":"YYYY-MM-DD","timeOfDay":"...","approximate":false,"evidence":"..."}],"characters":[...]}. "clock" hat einen Eintrag pro Nachricht, in der Reihenfolge, ohne die Felder, die eine Art nicht verwendet. Ohne verfolgte Figuren ist "characters" leer.""";
    }

    @Override
    public String tidySystem() {
        return """
            Du hältst den aktuellen Zustand der Figuren einer Geschichte ordentlich: was sie tragen, bei sich haben, halten, fühlen und so weiter. Du bekommst die VERFOLGTEN Figuren, jede mit ihren Kategorien und deren aktuellen Werten (nummeriert; ein mit "(stays)" markierter Wert kann nicht geändert werden), und die letzten NACHRICHTEN der Geschichte (nummeriert). Manche Zustände verblassen, ohne dass die Geschichte es sagt. Entscheide für jeden nummerierten Wert, ob er am ENDE der Nachrichten noch stimmt.

            Für jeden Wert eines davon:
            - behalten: sag nichts darüber. Das ist der Normalfall.
            - drop: er stimmt nicht mehr oder ist nicht mehr verfolgenswert (ein Zorn von vor mehreren Szenen, den die Nachrichten nicht mehr zeigen, außer Atem lange nach dem Lauf, „hält eine Tasse“ lange nach dem Essen, nasse Kleidung, die Zeit zum Trocknen hatte).
            - update: die Nachrichten zeigen, dass sich der Zustand weiterentwickelt hat (die Stimmung hat sich gebessert, der blaue Fleck verblasst): gib den neuen kurzen Wert.
            - merge: zwei oder mehr Einträge derselben list-Kategorie sind dasselbe („Seil“ und „Seilrolle“): gib den einen Eintrag, der sie ersetzt.

            Regeln:
            1. Dauerhaftes bleibt: Kleidung, mitgeführte Gegenstände, Waffen, Narben und Verletzungen, die Zeit zum Heilen brauchen, werden behalten, außer die Nachrichten zeigen etwas anderes. Verwirf oder aktualisiere vor allem kurzlebige Zustände (Stimmungen, Gefühle, nass, kalt, müde, außer Atem sein, etwas kurz in der Hand halten).
            2. Urteile nur nach den Nachrichten und der Zeit, die in ihnen vergeht, nie nach einer Vermutung darüber, was außerhalb der Seite geschah.
            3. Werte sind kurz, in den Worten der Geschichte, und beschreiben einen Zustand, keine Handlung.
            4. Jedes drop, update und merge braucht eine kurze Begründung.
            5. Im Zweifel behalten. Leere Listen sind normal.
            6. Ein Wert kann sagen, an welchem Tag der Geschichte er gesetzt wurde und wie lange das her ist: Nutze das, ein kurzlebiger Zustand, der vor vielen Tagen gesetzt wurde, ist verblasst.

            Antworte nur mit JSON, ohne anderen Text: {"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"update":[{"item":N,"value":"...","reason":"..."}],"merge":[{"items":[N,M],"value":"...","reason":"..."}]}]}. Eine Figur, bei der nichts zu ändern ist, hat leere Listen.""";
    }
}
