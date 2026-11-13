package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

/**
 * The built-in system messages of the Knowledge Tracker in French; the JSON of the answer, its values ("knows",
 * "does-not-know") and the tool name stay English.
 */
public class KnowledgeTrackerDefaultsFR extends KnowledgeTrackerDefaults {

    @Override
    public String extractionSystem() {
        return """
            Tu suis ce que savent les personnages d'une histoire. Tu reçois le monde (le lorebook, s'il est donné), les personnages SUIVIS avec les éléments numérotés de ce que chacun sait ou ne sait pas actuellement, et les nouveaux MESSAGES de l'histoire (numérotés). Indique, pour chaque personnage suivi, seulement ce qui a changé dans ces messages.

            Règles :
            1. add : une connaissance que le personnage ACQUIERT dans ces messages : ce qu'il a vu, entendu, lu, ce qu'on lui a dit ou ce qu'il a compris lui-même. Pas ce que sait le lecteur, pas ce que dit seulement le narrateur, pas ce que savent les autres personnages.
            2. N'ajoute PAS de connaissance générale qu'un habitant de ce monde a de toute façon (qui est le roi pour un sujet du royaume, les coutumes communes, la langue, la géographie de son propre pays), ni rien qui découle du personnage lui-même tel que le décrit le texte du monde (son espèce, son corps, ses capacités, son histoire, sa famille, son métier).
            3. N'ajoute RIEN que les éléments du personnage disent déjà.
            4. retract : le numéro d'un élément que les messages contredisent clairement (par exemple « n'a jamais rencontré X » après qu'ils se sont rencontrés). Seulement quand le texte montre la contradiction. Un élément disant qu'un personnage ne sait pas Y n'est retiré que si le personnage apprend Y dans ces messages. Un élément qui dit plusieurs choses n'est retiré que si les messages contredisent le tout ; quand une partie seulement est contredite, laisse l'élément et ajoute ce qui a été appris. Juge un élément d'après la FIN des messages : quand on dit de nouveau au personnage, qu'on lui rappelle ou qu'il comprend de nouveau ce que dit l'élément plus loin dans ces messages (il demande qui est quelqu'un, puis on le lui dit), l'élément est vrai à la fin, ne le retire donc pas et ne l'ajoute pas de nouveau.
            5. Ne déduis jamais. En cas de doute, n'indique rien. Un personnage absent, ou qui ne voit ni n'entend un événement (pensées intimes, chuchotements, lettre fermée), n'en apprend rien.
            6. Chaque changement a besoin d'une citation de preuve copiée des messages et du numéro du message.
            7. N'enregistre que les connaissances qui pourraient compter plus tard dans l'histoire : secrets, identités, relations et sentiments révélés, projets, promesses, découvertes, et faits qu'un autre personnage ignore. N'enregistre PAS les événements que tous les personnages présents ont vus ou entendus ensemble (le texte de l'histoire les contient), les bavardages, les plaisanteries, les humeurs, la météo, les objets et les détails du décor, ce qu'un personnage a fait lui-même, ni le même fait pour plusieurs personnages. Au plus 3 ajouts par personnage, d'habitude 0 ou 1. Quand rien ne compte, n'indique rien.

            Réponds seulement en JSON, sans autre texte : {"characters":[{"name":"...","retract":[{"item":N,"evidence":"...","message":N}],"add":[{"kind":"knows|does-not-know","text":"...","evidence":"...","message":N}]}]}. Un personnage sans changement a des listes vides.""";
    }

    @Override
    public String readingNote() {
        return """
            Tu peux aussi appeler read_part avec le numéro d'une partie précédente de l'histoire. Les éléments appris dans une partie précédente le disent ("learned in part N") et les messages indiquent à quelle partie ils appartiennent. Quand un élément vient d'un ouï-dire, d'une rumeur, d'un projet ou d'une présentation et que ces messages semblent le confirmer, le répéter ou le contredire, ou que l'élément seul ne te dit pas ce que le personnage savait à ce moment, lis d'abord cette partie puis décide, au lieu de deviner. Ne lis pas de parties pour une autre raison (au plus %d). Ta réponse finale reste le JSON seul.""".formatted(PartReader.MAX_READS);
    }

    @Override
    public String tidySystem() {
        return """
            Tu ranges ce que savent les personnages d'une histoire. Tu reçois les personnages SUIVIS avec les éléments numérotés qu'ils savent ou ne savent pas, les éléments qui restent tels quels (pour comparaison seulement) et les dernières parties de l'histoire comme contexte. Les éléments sont envoyés à un modèle d'écriture pour qu'il ne donne pas à un personnage une connaissance qu'il ne peut pas avoir ; chaque élément coûte de la place et doit la valoir.

            Règles :
            1. drop : un élément qui ne vaut pas d'être gardé : une anecdote (un détail de la scène, un bavardage, une plaisanterie, une humeur, un objet), un événement que tous les personnages de la scène ont vu ensemble (l'histoire le contient déjà), quelque chose que des événements ultérieurs ont dépassé, ou quelque chose qui dit la même chose qu'un autre élément ou qu'un élément qui reste.
            2. merge : deux éléments ou plus sur le même sujet en un seul élément qui garde chacun de leurs faits. N'ajoute rien qui ne soit pas dans les éléments.
            3. Ne retire ni ne fusionne JAMAIS un secret, une identité, une relation, un sentiment révélé, un projet, une promesse, une découverte, ni rien qu'un autre personnage ignore, même quand les dernières parties n'en parlent pas : un élément qui n'est pas revenu récemment n'est pas une raison de le retirer.
            4. En cas de doute, garde-le. N'indique que ce dont tu es sûr ; des listes vides sont normales.
            5. Chaque retrait et chaque fusion a besoin d'une courte raison.

            Réponds seulement en JSON, sans autre texte : {"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"merge":[{"items":[N,N],"kind":"knows|does-not-know","text":"...","reason":"..."}]}]}. Un personnage sans rien à faire a des listes vides.""";
    }
}
