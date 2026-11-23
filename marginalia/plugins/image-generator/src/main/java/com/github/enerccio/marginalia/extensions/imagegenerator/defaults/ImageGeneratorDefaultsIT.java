package com.github.enerccio.marginalia.extensions.imagegenerator.defaults;

/** The built-in prompt texts of the Image Generator in Italian; the image prompt itself is still asked for in English. */
public class ImageGeneratorDefaultsIT extends ImageGeneratorDefaults {

    @Override
    public String tagsInstruction() {
        return "Scrivi prompt per un modello di generazione di immagini. Dalla scena qui sotto, scrivi un prompt che descriva l'immagine del suo momento più visivo: chi c'è (aspetto, abiti, posa), dove, la luce e l'atmosfera. Usa brevi espressioni separate da virgole, niente frasi, niente nomi di persone (descrivile), niente testo nell'immagine. Scrivi il prompt in inglese, qualunque sia la lingua della scena. Rispondi solo con il prompt.";
    }

    @Override
    public String sentencesInstruction() {
        return "Scrivi prompt per un modello di generazione di immagini che comprende il linguaggio naturale. Dalla scena qui sotto, scrivi un prompt per l'immagine del suo momento più visivo: un solo paragrafo di tre-sei semplici frasi descrittive. Di' chi c'è (età, aspetto, abiti, posa, espressione) e cosa fa, dove succede, come è inquadrata l'immagine (primo piano, campo lungo, da quale lato), la luce e l'atmosfera. Descrivi le persone dal loro aspetto, mai con il loro nome; descrivi solo ciò che si vede; niente testo né lettere nell'immagine; niente elenchi, tag o titoli. Scrivi il prompt in inglese, qualunque sia la lingua della scena. Rispondi solo con il prompt.";
    }

    @Override
    public String sceneSetting() {
        return "Ambientazione della scena";
    }

    @Override
    public String pointOfView() {
        return "Punto di vista";
    }

    @Override
    public String charactersPresent() {
        return "Personaggi presenti";
    }

    @Override
    public String text() {
        return "Testo";
    }

    @Override
    public String detailsHeader() {
        return "Dettagli che l'immagine deve seguire";
    }

    @Override
    public String styleHeader() {
        return "L'immagine è fatta in questo stile. Inizia il prompt con esso e mantieni ogni espressione o frase fedele a esso: niente termini fotografici o di ripresa (foto, cinematografico, obiettivo, profondità di campo) a meno che lo stile sia una fotografia, e nessun altro mezzo";
    }
}
