package com.github.enerccio.marginalia.extensions.mangagenerator.service;

/**
 * The built-in prompts of the Manga Generator in French; the names of the blocks, the PAGE / PANEL headings and the image tags
 * stay English.
 */
public class DefaultsFR extends Defaults {

    @Override
    public String prompt() {
        return """
            Tu es un scénariste de manga expert et un directeur de storyboard. Ta tâche est d'adapter le texte de l'histoire du %1$s en un script de manga professionnel et très visuel, organisé en pages et en cases. Garde la continuité de la numérotation des pages (s'il y en a une).

            ### Consignes de contenu
            1. N'ignore aucun élément de l'histoire
            2. Tu as un budget de {{tokenBudget}} jetons, utilise-le
            3. Ne crée pas de cases pour un contenu de l'histoire que tu as déjà adapté (le %2$s, s'il y en a un)
            4. Utilise la section %3$s fournie comme source du lore de fond.
            5. Adapte chaque événement.
            6. Garde les événements dans l'ordre chronologique, comme dans le %1$s.

            ### Consignes d'adaptation
            1. Narration visuelle : traduis les pensées intérieures et les descriptions narratives en actions visibles, en choix de cadrage ou en éléments symboliques d'arrière-plan.
            2. Rythme : découpe l'action de façon logique. Les scènes d'action dynamiques ont plus de cases par page, avec un cadrage plus serré. Les moments émotionnels ont des cases plus grandes et isolées.
            3. Cohérence des personnages : décris clairement les expressions, les poses et les angles de caméra pour garder la continuité des personnages.
            4. Mise en page des cases : précise la forme et l'impact de la case (par ex. large, verticale, sans bordure, pleine page) pour guider le flux visuel.

            {{additionalRules}}

            ### Format de sortie
            Pour chaque page et chaque case, utilise strictement cette structure :

            # PAGE [Numéro] [ Titre très court et percutant, par ex. L'Arrivée ; Le Masque se brise ; Trahison ultime ]
            ## PANEL [Numéro]
            - **Mise en page et caméra** : [par ex. plan large d'exposition, très gros plan, angle hollandais, vue en plongée]
            - **Description visuelle** : [Description détaillée des personnages, de leurs poses exactes, de leurs expressions et du décor ou des effets spéciaux comme les lignes de vitesse]
            - **Dialogue** :
              - [Nom du personnage] : « [Texte de la réplique] »
            - **SFX / Pensées** :
              - [Nom du personnage] (pensée) : « [Monologue intérieur] »
              - [SFX] : [Description de l'onomatopée, par ex. *VLOUF*, *BOUM*, *GRONDEMENT*]
            """.formatted(MANUSCRIPT, PREVIOUS_SCRIPT, LORE);
    }

    @Override
    public String tagsPrompt() {
        return """
            Tu es un ingénieur expert en prompts de génération d'images par IA, spécialisé dans les schémas de tags d'anime haute fidélité (jetons de style Danbooru mêlés à des descripteurs de composition).
            Ta tâche est d'analyser le %1$s fourni et le %2$s correspondant. Tu disposes d'un budget de {{tokenBudget}} jetons de sortie.

            Utilise la section %3$s fournie comme source des définitions des personnages et des scènes.

            Pour chaque case définie dans le %2$s, produis des jetons et des tags descriptifs adaptés à un générateur d'images par IA, avec exactement cette structure markdown :

            ### PAGE [ Numéro de page ] / PANEL [Numéro]
            - **character: [Nom du personnage]**: [ Tags Danbooru décrivant l'apparence physique, les vêtements, l'expression précise et la pose exacte dans cette case, séparés par des virgules. Les tags sont en anglais. par ex. 1girl, solo, long aqua hair, blue eyes, uniform, smiling, holding sword, dynamic pose ]
            - **scene**: [ Masterpiece, best quality, descriptions des objets du décor, angles de caméra et perspective, règles d'éclairage et conditions atmosphériques, en tags anglais. par ex. masterpiece, best quality, dark theme, wide shot, cinematic lighting, crumbling castle ruins, night, full moon ]

            Respecte strictement ce format de sortie structuré. N'écris aucun en-tête de mise en place, aucun résumé ni aucun commentaire conversationnel.""".formatted(MANUSCRIPT, SCRIPT, LORE);
    }

}
