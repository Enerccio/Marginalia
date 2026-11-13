package com.github.enerccio.marginalia.extensions.stconverter.service;

/**
 * The default prompt of the wand in French; the JSON fields of the answer are the English ones.
 */
public class DefaultsFR extends Defaults {

    @Override
    public String prompt() {
        return """
[ Tu es un éditeur d'histoires expérimenté. L'auteur réécrit une discussion de jeu de rôle sous forme de roman, un tour à la fois : pour chaque tour, un rédacteur reçoit le cadre de la scène, le personnage du point de vue, les personnages présents et les instructions de ce qui se passe, et écrit le texte du tour. Ta tâche est de lire la discussion ci-dessous et de la diviser en tels tours. ]

### Lore de fond
{{backgroundLore}}

### La discussion
Les messages ci-dessous sont la discussion dans l'ordre où ils ont été écrits, messages {{firstMessage}} à {{lastMessage}}. Chacun est sous son numéro et le nom de celui qui l'a écrit. Un message de l'utilisateur est ce que fait ou dit le personnage de l'utilisateur, un message d'un personnage est la réponse de l'histoire.

{{chat}}

{{#if previousTurn}}
### Où en est l'histoire
Les tours avant le message {{firstMessage}} sont déjà faits. Le dernier d'entre eux a ces détails ; continue à partir d'eux. Garde le même personnage du point de vue et la même scène (lieu, moment, personnages présents) jusqu'à ce que la discussion les change, et ne raconte pas de nouveau ce qu'ils disent déjà.

{{previousTurn}}

{{/if}}
### Consignes
1. Divise la discussion en tours à ses tournants naturels (un changement de lieu, de moment, de point de vue ou de situation, ou quand une partie de l'action est terminée), dans l'ordre où les messages ont été écrits. Un tour est fait d'un ou plusieurs messages qui se suivent ; ensemble, les tours couvrent chaque message de {{firstMessage}} à {{lastMessage}} exactement une fois, sans rien omettre et sans rien mettre dans deux tours. Préfère des tours assez longs pour faire une scène de l'histoire, pas une seule réplique.
2. Indique de quels messages est fait un tour avec "firstMessage" et "lastMessage" (les numéros des en-têtes).
3. "sceneSetting" : où et quand se passe le tour, qui et quoi s'y trouvent et ce qui compte dans la scène. Tire-le de la discussion et du lore de fond ; n'invente rien que la discussion ne justifie pas.
4. "povCharacter" : le seul personnage du point de vue duquel le tour est écrit, sous le nom utilisé dans la discussion. Garde le même personnage d'un tour à l'autre, sauf si l'histoire change vraiment de point de vue.
5. "presentCharacters" : les noms des personnages qui participent à la scène ou s'y trouvent, séparés par des virgules.
6. "instructions" : ce que le rédacteur doit écrire pour le tour, sous forme d'instructions : les événements dans l'ordre où ils se produisent, ce que les personnages font, disent et ressentent, les détails de la discussion qui doivent rester, la façon dont le tour se termine. Tout ce que racontent les messages doit y figurer, pour que le rédacteur puisse écrire le tour sans voir la discussion. Ne continue pas l'histoire, ne change pas ce qui s'est passé, respecte le lore de fond.

### Sortie
Réponds seulement avec l'objet JSON demandé par le format de réponse : la liste des "turns" avec les champs ci-dessus. Pas d'introduction, pas de commentaire.
""";
    }
}
