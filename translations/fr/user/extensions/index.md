---
label: Extensions
order: 930
icon: plug
---

# Extensions

Les extensions ajoutent des fonctionnalités à Marginalia sans modifier l'application elle-même. Trente extensions sont fournies avec Marginalia :

| Extension | Ce qu'elle ajoute |
|---|---|
| [xAI Grok](grok.md) | Les modèles Grok de xAI comme fournisseur d'inférence, via l'API propre à xAI, avec le raisonnement affiché séparément de la réponse. |
| [Arc Outline](arc-outline.md) | Les arcs du livre et leurs chapitres, avec leur état, dans la barre latérale ; le modèle peut rédiger un arc pour vous. |
| [Anthropic Claude](anthropic.md) | Les modèles Claude d'Anthropic comme fournisseur d'inférence, via l'API propre à Anthropic, avec les résumés de réflexion affichés comme raisonnement. |
| [Audiobook](audiobook.md) | Transforme un livre en livre audio avec un serveur de synthèse vocale : une voix pour le narrateur et pour chaque personnage, un studio pour la version finale, un export en mp3 avec des repères de chapitres. [Configurer le serveur de synthèse vocale](audiobook-server.md). |
| [Author's Note](authors-note.md) | Une note destinée au modèle, insérée dans chaque génération du livre. |
| [Beta Readers](beta-readers.md) | Des lecteurs que vous inventez (personas) et une discussion avec eux à propos du livre, seul à seul ou en groupe. |
| [Book Description Generator](book-description-generator.md) | Rédige avec le modèle le nom et la description du livre à partir d'une partie de son histoire, avec le bouton de l'onglet About. |
| [Chapter Marker](chapter-marker.md) | Titres de chapitres dans le plan de l'histoire, à partir des titres Markdown dans les parties. |
| [Character Tracker](character-tracker.md) | Les personnages du livre avec leurs relations, et une carte de ceux-ci. |
| [Conlang Translator](conlang-translator.md) | La parole de vos personnages dans une langue construite que vous décrivez, avec l'anglais conservé en arrière-plan pour les modèles et un clic pour le lecteur. |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | Permet l'appel de fonctions avec les anciens modèles Gemma 4 dont l'API attend un autre rôle pour les résultats des outils ; inutile avec un modèle de chat récent. |
| [Google Gemini](google-genai.md) | Les modèles Gemini de Google comme fournisseur d'inférence, via l'API propre à Google, avec les résumés de réflexion affichés comme raisonnement. |
| [Image Generator](image-generator.md) | Crée une image pour une partie de l'histoire : le modèle rédige le prompt, ComfyUI ou une API d'images de type OpenAI la dessine, vous la relancez et l'acceptez. |
| [Prompt History](prompt-history.md) | Conserve les 20 dernières requêtes envoyées aux modèles dans votre session (et par exécution d'agent), avec les réponses et le raisonnement, pour déboguer les extensions et vérifier les prompts ; ouvert depuis Settings → Extension Settings ou depuis la console d'un agent. |
| [Prompt Inspector](prompt-inspector.md) | Affiche le prompt d'une génération et demande confirmation avant son envoi au modèle ; activé et désactivé dans le menu des paramètres de l'histoire. |
| [Instruction Exporter](instruction-exporter.md) | Copie les instructions, les réglages de scène et les personnages de l'histoire dans le presse-papiers. |
| [Knowledge Tracker](knowledge-tracker.md) | Ce que chaque personnage suivi sait et ne sait pas, ajouté au prompt pour que le modèle cesse de donner aux personnages des connaissances qu'ils ne peuvent pas avoir. |
| [Lore Generator](lore-generator.md) | Rédige avec le modèle le contenu d'une entrée du lorebook, à partir des éléments de lore que vous avez déjà. |
| [Lorebook VCS](lorebook-vcs.md) | Historique des révisions des entrées du lorebook. |
| [Manga Generator](manga-generator.md) | Transforme des parties de l'histoire en script de manga, avec des pages et des cases, et en balises de génération d'images. |
| [Tools for the model](tools.md) | Le bouton **Tools** des générateurs : quelles suites d'outils (noms, lieux) le modèle peut utiliser, et lesquelles de leurs catégories. |
| [Name generator](name-generator.md) | Des noms que le modèle peut utiliser au lieu de les inventer : environ 1270 listes de noms, sous forme de chaînes de Markov. |
| [Name provider](name-provider.md) | De vrais noms parmi lesquels le modèle peut choisir : prénoms et noms de famille d'environ 100 pays, avec l'orthographe d'origine. |
| [Place provider](place-provider.md) | De vrais lieux que le modèle peut consulter : villes, mers, baies, fleuves, montagnes et autres, avec coordonnées et pays, ce qui se trouve autour d'un lieu et la distance entre deux lieux. |
| [Real Language Translator](real-language-translator.md) | La parole de vos personnages dans une langue réelle (le japonais pour une histoire se déroulant au Japon), avec l'anglais conservé en arrière-plan pour les modèles et un clic pour le lecteur. |
| [Reviewer](reviewer.md) | Critiques des parties de l'histoire rédigées par le modèle : réactions de lecteurs, critique. |
| [Side Query](side-query.md) | Une discussion avec le modèle à propos de votre livre, à côté de l'histoire. |
| [Converter](sillytavern-converter.md) | Une conversation SillyTavern à côté de l'histoire, et les détails de tour que le modèle en prépare, pour réécrire la conversation sous forme d'histoire ; ou les parties d'un livre plus ancien (depuis une sauvegarde ou le livre lui-même) avec leurs instructions, pour le reformuler. |
| [State Tracker](state-tracker.md) | L'état actuel des personnages suivis (vêtements, objets portés, arme à portée de main, blessures, humeur, ou vos propres catégories), ajouté au prompt pour que le modèle s'y tienne. |
| [Story Notes](story-notes.md) | Un petit wiki de vos propres pages (étiquettes, liens entre elles) dans la barre latérale, pour les intrigues, les idées et les règles du monde ; l'assistant d'intrigue et les agents peuvent lire et modifier les pages que vous autorisez. |
| [Read Aloud](tts-reader.md) | Lit un livre publié à voix haute dans la visionneuse, avec la voix de votre appareil. |
| [Chinese localization](chinese-localization.md) | Chinois simplifié comme langue de l'interface utilisateur. |

## Installing

Les extensions sont installées par un **administrateur** pour toute l'installation de Marginalia, dans *Admin → Extensions* :
ce sont des fichiers JAR, envoyés avec **Load Extension (.jar)**. Voir [Managing extensions](../administration/extensions.md)
pour les détails.

Une fois installée, une extension est disponible pour tous les utilisateurs. Une extension chargée pendant que Marginalia tourne apparaît
dans les fenêtres ouvertes ensuite : fermez et rouvrez un livre (ou rechargez la page) pour la voir.

!!!warning Utilisez des versions correspondantes
Les extensions se raccordent à l'interface utilisateur d'une version précise de Marginalia. Une extension conçue pour une autre version peut
cesser de fonctionner ou casser des parties de l'application. Utilisez les extensions fournies avec votre version de Marginalia.
!!!

Les extensions ci-dessus font partie du code source de Marginalia (`marginalia/plugins`) et sont compilées séparément ;
voir le [README des plugins](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md).

<a id="extensions-on-a-phone"></a>

## Les extensions sur téléphone

La [mise en page téléphone](../phone.md) a ses propres classes, donc une extension doit la prendre en charge. Arc Outline, Author's Note, Beta Readers, Chapter Marker,
Character Tracker, Instruction Exporter et Reviewer la prennent en charge ; Side Query, Lore Generator et Lorebook VCS n'ajoutent rien sur un téléphone (voir [Utiliser Marginalia sur un téléphone](../phone.md#extensions)). Name generator, Name provider, Place provider, Gemma 4 tools fix et Google Gemini n'ont pas d'écrans propres (Google Gemini ajoute seulement un fournisseur à la boîte de dialogue des modèles, que le téléphone affiche aussi), et Chinese localization se trouve dans le sélecteur de langue du menu du téléphone. Les extensions s'installent depuis la page Admin, que le téléphone n'a pas.

## Extension settings

Les extensions ayant des réglages ajoutent une section à **Settings → Extension Settings** (le troisième onglet de la page *Settings*).
Ces réglages appartiennent à votre compte, comme le reste de *Settings*, et sont enregistrés avec son bouton **Save**.

<a id="where-extension-data-is-kept"></a>

## Où sont conservées les données des extensions

Les extensions conservent leurs données avec les données auxquelles elles appartiennent : les critiques avec la partie de l'histoire, les conversations avec le livre, les révisions avec le lorebook. Elles sont donc incluses dans les [sauvegardes de la base de données](../administration/database-backups.md).
Leur présence dans les [sauvegardes de livres](../books/backups.md) et les exports est décrite sur la page de chaque extension.

Désinstaller une extension ne supprime pas ses données : la réinstaller les rend à nouveau disponibles.
