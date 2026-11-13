---
label: Générateur de manga
order: 8
---

# Générateur de manga

Transformez votre histoire en manga : le modèle adapte des parties du livre en un scénario de pages et de cases, puis rédige pour chaque case des mots-clés de génération d'image - prêts pour Stable Diffusion, Forge, Midjourney et autres. C'est un portage de l'extension [MangaGenerator](https://github.com/Enerccio/SillyTavern-MangaGenerator) de SillyTavern.

<a id="opening-it"></a>

## L'ouvrir

Choisissez **Générateur de manga** dans le menu des paramètres (⚙) de l'[éditeur d'histoire](../books/story-editor.md). Une grande fenêtre s'ouvre ; rien n'est ajouté à l'écran de l'histoire lui-même. Vous pouvez déplacer la fenêtre et en modifier la taille.

## Mangas

Un livre peut contenir plusieurs mangas, affichés sous forme d'onglets en haut de la fenêtre. Les boutons à côté des onglets permettent d'ajouter un manga (+), de le renommer, de le déplacer vers la gauche ou la droite, et de le supprimer. Un manga est conservé avec le livre : il fait donc partie des sauvegardes et des copies du livre.

Sous les onglets se trouvent les paramètres du manga ouvert :

| Paramètre | Rôle |
|---|---|
| Modèle et protocole | Vers quoi partent les requêtes. Par défaut, ceux du livre ; le dernier choix est mémorisé. |
| Budget de jetons | Longueur du scénario demandée au modèle (`{{tokenBudget}}` dans le prompt). |
| Parties par section | Nombre de parties de l'histoire adaptées par une nouvelle section. |
| Contexte avant / après | Nombre de parties supplémentaires situées avant et après celles-ci, que le modèle reçoit comme toile de fond. |
| Prompt de scénario, Prompt des mots-clés | Remplacent les prompts intégrés de ce manga. **Rétablir la valeur par défaut** remet le texte intégré. |
| Règles supplémentaires | Remplacent `{{additionalRules}}` dans le prompt de scénario, pour vos propres règles (pas d'effets sonores, un style...). |
| Tout copier | Copie dans le presse-papiers chaque scénario du manga, puis chaque texte de mots-clés. |
| Mode continu | Génère le manga de lui-même, voir [Mode continu](#continuous-mode). |

<a id="settings"></a>

## Paramètres

*Paramètres → Paramètres d'extension → Paramètres du Générateur de manga* contient ce avec quoi un **nouveau** manga démarre : le budget de jetons, les parties par section, le contexte avant et après, le prompt de scénario, le prompt des mots-clés et les règles supplémentaires - ainsi qu'un texte placé avant chaque requête, pour des instructions valables pour tous vos mangas. Un prompt laissé vide, ou non modifié, correspond au prompt intégré. Un manga déjà créé conserve ses propres paramètres. Le panneau est enregistré avec le bouton **Enregistrer** de la page *Paramètres*.

## Sections

**Ajouter une section** ajoute une section pour les parties de l'histoire qui suivent la dernière section (la première commence à la partie 1). Les parties sont numérotées à partir de 1, comme dans le plan. Une section affiche :

- **Adapter de la partie / à la partie** - les parties que le modèle transforme en cases,
- **Contexte de la partie / à la partie** - les parties que le modèle lit ; celles en dehors de la plage adaptée ne servent que de toile de fond (événements antérieurs, ou ce qui suit), et le modèle reçoit l'instruction de ne pas les adapter. Modifiez l'un de ces quatre numéros à tout moment pour changer de tranche.

Chaque section comporte deux vues, **Scénario** et **Mots-clés** :

1. **Générer le scénario** diffuse le scénario dans la section. La réflexion d'un modèle de raisonnement s'affiche au-dessus (*Réflexion pendant 12,3 s*). Le scénario poursuit la numérotation des pages de la section précédente et ne répète pas ce que ce scénario couvre déjà.
2. Passez à **Mots-clés** puis **Générer les mots-clés** : pour chaque case du scénario, des mots-clés pour les personnages et la scène.

![La fenêtre du Générateur de manga avec le scénario d'une section](../../images/plugin-manga.png)

Chacun de ces deux textes peut être **modifié** à la main (le crayon), **copié**, et une génération peut être arrêtée avec **Interrompre** - ce que le modèle a déjà écrit est conservé. Les sections se génèrent indépendamment, vous pouvez donc en lancer plusieurs ; tant qu'un modèle écrit, les sections ne peuvent pas être supprimées et les mangas ne peuvent pas être modifiés.

![La même section, vue Mots-clés](../../images/plugin-manga-tags.png)

**Surcharger** donne à une section son propre prompt de scénario, prompt des mots-clés ou règles supplémentaires, ou un **Complément de règles** qui s'ajoute aux règles du manga au lieu de les remplacer. L'œil affiche la requête exacte que recevrait le modèle, avec sa taille.

<a id="continuous-mode"></a>

## Mode continu

Le **Mode continu** (à côté de *Ajouter une section*) laisse le manga s'écrire tout seul, un type de texte à la fois, si bien que le modèle reçoit le même prompt encore et encore (et un serveur qui conserve un cache de prompts peut le réutiliser). Choisissez ce qu'il génère dans la liste à côté du bouton :

- **Scénarios seulement** - il parcourt les sections dans l'ordre et génère le scénario de chaque section qui n'en a pas (une section déjà pourvue d'un scénario reste telle quelle). Quand toutes en ont un, il ajoute une section pour les parties suivantes de l'histoire - comme le fait *Ajouter une section*, avec les *Parties par section* et le contexte du manga -, génère son scénario, et continue jusqu'à ce que l'histoire n'ait plus de partie pour une autre section.
- **Mots-clés seulement** - il génère les mots-clés de chaque section qui a un scénario mais pas de mots-clés. Il n'ajoute aucune section ; il se termine quand chaque scénario a ses mots-clés.

Dans les deux cas, il s'arrête quand vous appuyez sur **Arrêter le mode continu** (le bouton se transforme en celui-ci ; ce que le modèle a écrit jusque-là est conservé). Une erreur du modèle, une section sans texte dans ses parties, ou une réponse vide l'arrêtent aussi, avec un message. Pendant son fonctionnement, les sections ne peuvent pas être générées ou modifiées à la main, et les mangas ne peuvent pas être modifiés. Fermer la fenêtre pendant qu'il tourne le replie dans le [panneau des exécutions](../books/story-editor.md#the-panel-of-the-runs), où il continue ; le fermer là l'arrête. Chaque section sur laquelle il travaille est amenée à l'écran. Pour obtenir les deux, lancez d'abord *Scénarios seulement*, puis *Mots-clés seulement*.

<a id="the-lore-and-the-size-of-the-context"></a>

## L'univers et la taille du contexte

Le modèle reçoit le texte des parties de la section, et avec lui l'univers qu'appelle ce texte : les entrées du carnet d'univers du livre (et de ses sous-carnets) dont les filtres apparaissent dans ce texte, retrouvées comme lors d'une génération, profondeur de analyse comprise. Les entrées toujours actives sont aussi incluses. Ainsi, les personnages et les lieux de ces parties parviennent au modèle avec leurs descriptions, dont les mots-clés d'image ont besoin.

Le tout doit tenir dans la **taille de contexte** du modèle - le contexte de l'IA et du protocole, moins la place réservée à la réponse. Quand les entrées ne tiennent pas, les dernières sont laissées de côté (un avis indique combien, l'œil l'affiche aussi) : rendez la section plus petite, ou l'univers plus court. Une requête qui ne tient pas même sans l'univers est tout de même envoyée, avec un avertissement.

<a id="what-is-different-from-sillytavern"></a>

## Ce qui diffère de SillyTavern

- Un message de conversation est une **partie** de l'histoire ; il n'y a ni noms devant les messages, ni persona, ni fiche de personnage - les personnages viennent du carnet d'univers.
- Le « déclencheur » d'informations de monde de l'extension n'est pas nécessaire : l'univers est activé par le texte lui-même.
- La taille du contexte est reprise de l'IA et du protocole ; il n'existe aucun réglage pour elle.
- Une nouvelle section prend 3 parties avec 1 partie de contexte de chaque côté, car une partie d'histoire est bien plus longue qu'un message de conversation. Modifiez cela dans les paramètres (voir ci-dessous).
- Les onglets se déplacent avec les boutons fléchés, et non par glisser-déposer.
