package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * The built-in prompts of the Audiobook in French; the JSON fields and their values are the English ones.
 */
public class AudiobookDefaultsFR extends AudiobookDefaults {

    @Override
    public String labelTemplate() {
        return """
            Tu étiquettes les morceaux d'une histoire pour un livre audio à plusieurs voix. Les morceaux sont numérotés ; N est la narration, D le discours direct. {{#if characters}}Les personnages connus jusqu'ici sont : {{characters}}. D'autres peuvent parler ; nomme-les comme le fait le texte.{{else}}Ils ne sont pas connus : nomme un locuteur comme le texte le nomme.{{/if}}
            Pour chaque morceau, réponds avec :
            - "id" : le numéro du morceau ;
            - "speaker" : pour D, le personnage qui le dit, déduit du contexte (un nom dans la narration autour, une apostrophe, qui répond à qui). Pour N, c'est "Narrator", sauf quand le contexte montre que le texte est la parole propre d'un personnage : une lettre, une note, un message, un panneau ou une inscription qu'un personnage a écrit et que l'histoire cite, ou les pensées d'un personnage. Nomme alors ce personnage (celui qui écrit, une signature, celui qui pense), même sur plusieurs paragraphes de suite. L'italique (des marques comme *...* ou _..._) n'est qu'un indice : la même mise en forme sert aussi pour l'emphase, les sons et les mots étrangers, que lit le narrateur. En cas de doute, c'est "Narrator" ;
            - "delivery" : comment le lire ({{deliveries}}) et "intensity" de 0.0 à 1.0 ;
            - "tagged_text" : pour N, une chaîne vide. Pour D, le morceau mot pour mot, inchangé, sauf que tu peux insérer une balise sonore là où le contexte appelle clairement ce son de la part du personnage qui parle, et rarement (au plus une balise pour {{wordsPerTag}} mots de tout le texte). Balises autorisées : {{tags}}, écrites exactement ainsi. Ne change, n'ajoute ni ne retire aucun autre mot. Pour chaque morceau sans balise, utilise une chaîne vide : ne recopie jamais le texte d'un morceau sans balise.
            Les morceaux marqués "context" ont déjà été étiquetés : lis-les pour comprendre qui parle, ne les étiquette pas.
            Réponds seulement avec le JSON, une entrée par morceau, dans l'ordre.""";
    }
}
