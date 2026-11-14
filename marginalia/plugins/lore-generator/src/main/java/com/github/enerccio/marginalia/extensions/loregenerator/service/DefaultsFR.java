package com.github.enerccio.marginalia.extensions.loregenerator.service;

/**
 * The built-in prompts of the Lore Generator in French.
 */
public class DefaultsFR extends Defaults {

    @Override
    public String compactionPrompt() {
        return """
[Instruction système : tu es un moteur expert de compactage de données. Ta seule tâche est d'absorber le texte brut fourni ci-dessous et de le convertir en une entrée d'archive de lore ultra-dense, lisible par une machine, destinée à la mémoire de fond d'un LLM.]

RÈGLES ESSENTIELLES :

1. Supprime 100 % du remplissage atmosphérique, de la prose fleurie, de la mise en place narrative, de la mise en forme propre à l'univers (comme les en-têtes de rapport ou les formules de fin) et des effets rhétoriques.
2. N'extrais que les faits concrets : règles, mécanismes spatiaux, données démographiques, traits physiques, signes distinctifs, motivations et limites fonctionnelles.
   - EXCEPTION NARRATIVE : si le texte source contient une anecdote, un événement du passé ou un récit historique, NE le supprime PAS. Compresse-le plutôt dans un bloc « [Précédent historique] » ou « [Noyau psychologique] ». Retire la prose narrative, mais conserve exactement la dynamique de cause à effet, le traumatisme, la rancune ou le déclencheur de comportement créés par cet événement. Inclus les détails nécessaires dans les précédents historiques et garde TOUS les précédents historiques.
3. Structure entièrement la sortie avec des attributs markdown clairs en gras et des listes à puces nettes et minimalistes.
4. Garde une précision absolue dans les descriptions spatiales ou physiques (par exemple le côté précis d'un visage, des dispositions géométriques précises, des nombres exacts).
5. Renvoie SEULEMENT le bloc markdown brut. Pas d'introduction, pas de conclusion, pas de confirmation conversationnelle. Commence immédiatement par le premier bloc d'attributs.
6. IMPORTANT : renvoie-le dans un bloc de code markdown copiable entre triples guillemets inversés !
7. Ne résume PAS les informations qui font déjà partie du lore existant.

==== DÉBUT DU MATÉRIAU SOURCE BRUT ====
{{compactionData}}
==== FIN DU MATÉRIAU SOURCE BRUT ====
""";
    }

    @Override
    public String generationPrompt() {
        return """
[ Tu es un créateur de mondes expert et un maître de la prose très descriptive et atmosphérique. Ta tâche est de rédiger une entrée de lore détaillée en t'en tenant strictement aux paramètres fournis ci-dessous. Utilise des images vives et une langue riche tout en intégrant naturellement chaque détail essentiel. ]

### Paramètres
- Sujet : [Insère le nom du sujet]
- Détails essentiels à intégrer : [Insère les détails, faits ou mécanismes précis]
- Longueur / portée visée : [par ex. Exploration approfondie (800 mots et plus), Vignette moyenne (400 mots) ou Micro-lore (150 mots)]
- Structure requise : [Indique exactement quelles sections écrire, ou écris « Au choix : génère au moins X sections thématiques distinctes »]

### Consignes d'écriture
1. Ton et style : riche, évocateur et atmosphérique. Utilise des métaphores élégantes et une narration immersive adaptées au cadre (fantasy / science-fiction / moderne).
2. Rythme et profondeur : développe largement le lore. Ne te contente pas d'énoncer un fait ; consacre du temps à l'histoire, à l'influence culturelle, à l'expérience sensorielle et aux rumeurs qui l'entourent. Respecte la longueur visée.
3. Structure : tu dois suivre la structure requise indiquée ci-dessus. Utilise des titres markdown descriptifs pour chaque section.
4. Intégration des faits : drape les détails essentiels dans une prose luxuriante, en veillant à ce qu'ils restent assez clairs sous les images pour être exploités plus tard par un LLM.
5. Choix des noms : si tu dois inventer un nom, choisis des noms peu courants, qui ne risquent pas de se confondre avec les entrées de lore déjà générées.

### Sortie
Fournis seulement l'entrée de lore stylisée. N'ajoute aucun remplissage conversationnel en introduction ni en conclusion.
""";
    }

}
