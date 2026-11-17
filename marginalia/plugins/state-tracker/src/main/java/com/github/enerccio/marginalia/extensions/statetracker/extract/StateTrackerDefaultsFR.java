package com.github.enerccio.marginalia.extensions.statetracker.extract;

/**
 * The built-in system messages of the State Tracker in French; the JSON of the answer, its values, the kinds of category, the
 * markers "(stays)" and "strict" and the times of day stay English.
 */
public class StateTrackerDefaultsFR extends StateTrackerDefaults {

    @Override
    public String extractionSystem() {
        return """
            Tu suis l'état actuel des personnages d'une histoire : ce qu'ils portent, transportent, tiennent en main, etc. Tu reçois les personnages SUIVIS, chacun avec ses catégories (le type de la catégorie, ce qui y a sa place et ses valeurs actuelles, numérotées), et les nouveaux MESSAGES de l'histoire (numérotés). Signale, pour chaque personnage suivi, seulement ce qui a changé dans ces messages, tel que c'est à la FIN des messages.

            Types de catégorie :
            - single : une seule valeur. "set" donne la nouvelle valeur (elle remplace l'actuelle), "clear" indique que le personnage n'en a plus.
            - list : des entrées. "add" ajoute une nouvelle entrée, "remove" retire une entrée par son numéro.
            - description : un seul texte. "set" donne tout le nouveau texte ; garde du texte actuel ce qui est encore vrai.

            Règles :
            1. Ne signale un changement que lorsque les messages le montrent. Une catégorie marquée strict ne change que lorsque le texte énonce le changement. Une catégorie qui n'est pas strict change aussi par la conséquence physique directe de ce que dit le texte (mouillé après une chute dans une rivière), jamais par une supposition sur ce qui s'est passé hors de la page.
            2. Une action change l'état quand elle est faite, pas quand elle est prévue, promise, proposée ou demandée. Un objet remis est retiré à celui qui le donne et ajouté à celui qui le reçoit (s'ils sont suivis et ont cette catégorie). Poser un objet un instant, s'en servir ou le montrer, ce n'est pas le perdre : ne retire une entrée que si l'objet est donné, perdu, épuisé, détruit, laissé derrière ou pris.
            3. Suis la description de chaque catégorie : ce qui n'y a pas sa place n'y est pas signalé.
            4. Ne signale pas ce que disent déjà les valeurs actuelles, et ne reformule pas une valeur qui n'a pas changé.
            5. Les valeurs sont courtes, dans les mots de l'histoire, et décrivent un état, pas une action (joyeux, pas en train de rire).
            6. Chaque changement a besoin d'une citation copiée mot pour mot des messages et du numéro du message.
            7. Dans le doute, ne signale rien. Les listes vides sont normales.

            Réponds uniquement en JSON, sans autre texte : {"characters":[{"name":"...","changes":[{"category":"...","action":"set|clear|add|remove","value":"...","item":N,"evidence":"...","message":N}]}]}. "value" sert pour set et add, "item" pour remove. Un personnage sans changement a une liste vide.""";
    }

    @Override
    public String clockNote() {
        return """
            Tu suis aussi le temps de l'histoire. Tu reçois aussi l'HORLOGE : le jour de l'histoire compté depuis un événement d'ancrage, le moment de la journée, et la date du calendrier quand l'histoire en a une. Pour chaque message, signale comment le temps de l'histoire y avance. Ne compte pas : signale seulement ce que dit le message, le programme fait l'addition.
            - none : le message ne fait pas avancer le temps, ou n'en dit rien. C'est le cas par défaut.
            - advance : du temps passe. "days" est le nombre de jours qui passent (0 quand on reste le même jour : plus tard ce jour-là, une heure plus tard), "timeOfDay" le moment où se termine le message (dawn, morning, noon, afternoon, evening, night) quand il le dit.
            - day : le message donne le jour de l'histoire compté depuis l'événement d'ancrage de l'horloge (« le quarantième jour depuis ton réveil ») : "day" est ce nombre. Un ordinal d'autre chose (« au troisième matin » d'un voyage ou d'un séjour) est un advance, pas un day.
            - date : le message donne une date du calendrier : "date" au format YYYY-MM-DD, avec l'année de l'horloge quand le message n'en donne pas.
            - flashback : le message se passe dans le passé (un souvenir, un rêve du passé, le récit d'événements antérieurs) ; l'horloge n'avance pas, et rien de ce qu'il contient ne change l'état actuel des personnages.
            Règles de l'horloge : signale seulement ce que le message dit ou rend évident (« le lendemain matin » est un jour, morning ; « cette nuit-là » est le même jour, night ; « trois jours plus tard » fait trois jours), jamais un temps que le texte ne montre pas. Les durées vagues (« des semaines passèrent », « quelques jours plus tard ») sont un advance avec ta meilleure estimation du nombre de jours et "approximate": true. Le moment de la journée seul est un advance de 0 jour. Chaque signalement autre que none a besoin d'une citation copiée mot pour mot du message.

            Mets l'horloge dans la même réponse JSON, comme liste "clock" à côté de "characters" : {"clock":[{"message":N,"kind":"none|advance|day|date|flashback","days":N,"day":N,"date":"YYYY-MM-DD","timeOfDay":"...","approximate":false,"evidence":"..."}],"characters":[...]}. "clock" a une entrée par message, dans l'ordre, sans les champs qu'un type n'utilise pas. Sans personnage suivi, "characters" est vide.""";
    }

    @Override
    public String tidySystem() {
        return """
            Tu tiens en ordre l'état actuel des personnages d'une histoire : ce qu'ils portent, transportent, tiennent, ressentent, etc. Tu reçois les personnages SUIVIS, chacun avec ses catégories et leurs valeurs actuelles (numérotées ; une valeur marquée "(stays)" ne peut pas être changée), et les derniers MESSAGES de l'histoire (numérotés). Certains états s'estompent sans que l'histoire le dise. Décide, pour chaque valeur numérotée, si elle est encore vraie à la FIN des messages.

            Pour chaque valeur, une seule de ces options :
            - la garder : n'en dis rien. C'est le cas par défaut.
            - drop : elle n'est plus vraie ou ne vaut plus d'être suivie (une colère d'il y a plusieurs scènes que les messages ne montrent plus, essoufflé longtemps après la course, « tient une tasse » longtemps après le repas, des vêtements mouillés qui ont eu le temps de sécher).
            - update : les messages montrent que l'état a évolué (l'humeur s'est améliorée, le bleu s'estompe) : donne la nouvelle valeur courte.
            - merge : deux entrées ou plus d'une même catégorie list sont la même chose (« corde » et « rouleau de corde ») : donne l'entrée unique qui les remplace.

            Règles :
            1. Ce qui dure reste : les vêtements, les objets transportés, les armes, les cicatrices et les blessures qui ont besoin de temps pour guérir sont gardés sauf si les messages montrent le contraire. Abandonne ou mets à jour surtout les états passagers (humeurs, sentiments, être mouillé, avoir froid, être fatigué, essoufflé, tenir quelque chose un instant).
            2. Juge seulement d'après les messages et le temps qui passe en eux, jamais d'après une supposition sur ce qui s'est passé hors de la page.
            3. Les valeurs sont courtes, dans les mots de l'histoire, et décrivent un état, pas une action.
            4. Chaque drop, update et merge a besoin d'une courte raison.
            5. Dans le doute, garde. Les listes vides sont normales.
            6. Une valeur peut indiquer le jour de l'histoire où elle a été posée et depuis combien de temps : sers-t'en, un état passager posé il y a de nombreux jours s'est estompé.

            Réponds uniquement en JSON, sans autre texte : {"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"update":[{"item":N,"value":"...","reason":"..."}],"merge":[{"items":[N,M],"value":"...","reason":"..."}]}]}. Un personnage sans rien à changer a des listes vides.""";
    }
}
