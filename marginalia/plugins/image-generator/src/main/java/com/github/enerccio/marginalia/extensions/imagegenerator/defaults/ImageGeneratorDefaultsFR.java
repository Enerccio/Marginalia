package com.github.enerccio.marginalia.extensions.imagegenerator.defaults;

/** The built-in prompt texts of the Image Generator in French; the image prompt itself is still asked for in English. */
public class ImageGeneratorDefaultsFR extends ImageGeneratorDefaults {

    @Override
    public String tagsInstruction() {
        return "Tu écris des prompts pour un modèle de génération d'images. À partir de la scène ci-dessous, écris un prompt qui décrit l'image de son moment le plus visuel : qui est là (apparence, vêtements, pose), où, la lumière et l'ambiance. Utilise de courtes expressions séparées par des virgules, pas de phrases, pas de noms de personnes (décris-les), pas de texte dans l'image. Écris le prompt en anglais, quelle que soit la langue de la scène. Réponds uniquement avec le prompt.";
    }

    @Override
    public String sentencesInstruction() {
        return "Tu écris des prompts pour un modèle de génération d'images qui comprend le langage naturel. À partir de la scène ci-dessous, écris un prompt pour l'image de son moment le plus visuel : un seul paragraphe de trois à six phrases descriptives simples. Dis qui est là (âge, apparence, vêtements, pose, expression) et ce qu'ils font, où cela se passe, le cadrage de l'image (gros plan, plan large, de quel côté), la lumière et l'ambiance. Décris les personnes par leur apparence, jamais par leur nom ; décris seulement ce qui se voit ; pas de texte ni de lettres dans l'image ; pas de listes, de tags ni de titres. Écris le prompt en anglais, quelle que soit la langue de la scène. Réponds uniquement avec le prompt.";
    }

    @Override
    public String sceneSetting() {
        return "Cadre de la scène";
    }

    @Override
    public String pointOfView() {
        return "Point de vue";
    }

    @Override
    public String charactersPresent() {
        return "Personnages présents";
    }

    @Override
    public String text() {
        return "Texte";
    }

    @Override
    public String detailsHeader() {
        return "Détails que l'image doit respecter";
    }

    @Override
    public String styleHeader() {
        return "Le style de l'image est ajouté à ton prompt automatiquement, ne décris pas un autre médium ni un autre style";
    }
}
