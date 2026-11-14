package com.github.enerccio.marginalia.extensions.imagegenerator.defaults;

/** The built-in prompt texts of the Image Generator in German; the image prompt itself is still asked for in English. */
public class ImageGeneratorDefaultsDE extends ImageGeneratorDefaults {

    @Override
    public String tagsInstruction() {
        return "Du schreibst Prompts für ein Bildgenerierungsmodell. Schreibe aus der Szene unten einen Prompt, der das Bild ihres anschaulichsten Moments beschreibt: wer da ist (Aussehen, Kleidung, Haltung), wo, das Licht und die Stimmung. Verwende kurze, durch Kommas getrennte Ausdrücke, keine Sätze, keine Namen von Personen (beschreibe sie), keinen Text im Bild. Schreibe den Prompt auf Englisch, egal in welcher Sprache die Szene ist. Antworte nur mit dem Prompt.";
    }

    @Override
    public String sentencesInstruction() {
        return "Du schreibst Prompts für ein Bildgenerierungsmodell, das natürliche Sprache versteht. Schreibe aus der Szene unten einen Prompt für das Bild ihres anschaulichsten Moments: einen einzigen Absatz aus drei bis sechs schlichten beschreibenden Sätzen. Sag, wer da ist (Alter, Aussehen, Kleidung, Haltung, Gesichtsausdruck) und was die Personen tun, wo es geschieht, wie das Bild gerahmt ist (Nahaufnahme, Totale, von welcher Seite), das Licht und die Stimmung. Beschreibe Personen nach ihrem Aussehen, nie mit ihrem Namen; beschreibe nur, was man sieht; kein Text und keine Buchstaben im Bild; keine Listen, Tags oder Überschriften. Schreibe den Prompt auf Englisch, egal in welcher Sprache die Szene ist. Antworte nur mit dem Prompt.";
    }

    @Override
    public String sceneSetting() {
        return "Schauplatz der Szene";
    }

    @Override
    public String pointOfView() {
        return "Perspektive";
    }

    @Override
    public String charactersPresent() {
        return "Anwesende Figuren";
    }

    @Override
    public String text() {
        return "Text";
    }

    @Override
    public String detailsHeader() {
        return "Details, denen das Bild folgen muss";
    }

    @Override
    public String styleHeader() {
        return "Der Stil des Bilds wird deinem Prompt automatisch hinzugefügt, beschreibe kein anderes Medium und keinen anderen Stil";
    }
}
