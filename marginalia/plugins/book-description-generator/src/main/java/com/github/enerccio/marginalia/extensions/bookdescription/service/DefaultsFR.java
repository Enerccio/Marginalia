package com.github.enerccio.marginalia.extensions.bookdescription.service;

import com.github.enerccio.marginalia.extensions.bookdescription.model.OutputFormat;

/**
 * The built-in prompts of the Book Description Generator in French; the JSON fields and the Markdown heading of the answer are
 * the ones the plugin reads.
 */
public class DefaultsFR extends Defaults {

    @Override
    public String descriptionPrompt() {
        return """
[ Tu es un éditeur expérimenté qui rédige les titres et les quatrièmes de couverture des livres. Ta tâche est de lire une histoire déjà écrite et de donner au livre auquel elle appartient un nom et une description. ]

### Le livre actuellement
Nom : {{bookName}}
{{#if bookDescription}}Description : {{bookDescription}}{{/if}}

### L'histoire
{{#if fromNotes}}L'histoire est trop longue pour être donnée en entier. Voici les notes qui en ont été tirées, partie par partie, dans l'ordre de lecture de l'histoire.{{else}}Voici l'histoire telle qu'elle a été écrite, dans l'ordre de lecture. Une partie peut en être donnée sous la forme du résumé qui remplace les messages qu'il couvre.{{/if}}

{{story}}

### Consignes
1. Décris le livre tel qu'il est réellement. N'invente ni événements, ni personnages, ni lieux, et ne continue pas l'histoire.
2. Le nom est court et mémorable, dans la langue de l'histoire. Il ne dévoile pas la fin. Ne répète pas simplement le nom actuel du livre, sauf s'il est vraiment le meilleur.
3. La description est ce que voit un lecteur qui flâne dans une bibliothèque : elle présente le cadre et les personnages principaux, pose le conflit central et les enjeux, et donne envie de lire la suite. Deux ou trois courts paragraphes. Ne dévoile pas la fin, ne liste pas les chapitres, ne t'adresse pas au lecteur par « vous ».
4. Écris dans la langue de l'histoire, dans le ton de l'histoire.
""";
    }

    @Override
    public String notesPrompt() {
        return """
[ Tu es un analyste d'histoires attentif. Ta tâche est de lire une partie d'une longue histoire et d'écrire des notes dont quelqu'un qui ne l'a pas lue pourra se servir pour rédiger la description du livre entier. ]

### Le livre actuellement
Nom : {{bookName}}
{{#if bookDescription}}Description : {{bookDescription}}{{/if}}

### L'histoire, partie {{part}} sur {{parts}}
{{story}}

### Consignes
1. Écris des notes denses sur ce qui se passe réellement dans cette partie, dans l'ordre où cela se passe. N'invente rien et ne continue pas l'histoire.
2. Nomme les personnages (par les noms utilisés dans l'histoire) avec qui ils sont et ce qu'ils veulent, les lieux, le conflit central de cette partie et la façon dont elle se termine.
3. Ajoute le genre, le ton et les thèmes qui apparaissent dans cette partie.
4. Garde ce dont une quatrième de couverture a besoin et omets les détails secondaires. Utilise de courts paragraphes ou des puces, pas de titres.
5. Réponds seulement avec les notes. Pas d'introduction, pas de commentaire.
""";
    }

    @Override
    public String outputInstructions(OutputFormat format) {
        return switch (format) {
            case AUTO, JSON -> "### Sortie\nRéponds seulement avec l'objet JSON demandé par le format de réponse : le \"name\" du livre et sa \"description\". Pas d'introduction, pas de commentaire.";
            case JSON_NO_SCHEMA -> "### Sortie\nRéponds seulement avec un objet JSON exactement de cette forme, rien avant et rien après :\n"
                    + "{\"name\": \"<le nom du livre>\", \"description\": \"<la description du livre>\"}";
            case MARKDOWN -> "### Sortie\nRéponds en Markdown, rien d'autre : la première ligne est le nom du livre sous forme de titre (# Nom du livre), "
                    + "tout ce qui suit est la description du livre. Pas d'introduction, pas de commentaire.";
        };
    }
}
