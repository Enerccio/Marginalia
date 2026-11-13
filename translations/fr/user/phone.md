---
label: Utiliser Marginalia sur téléphone
order: 985
icon: device-mobile
---

# Utiliser Marginalia sur téléphone

Marginalia dispose d'une mise en page conçue pour les téléphones. Vous écrivez, modifiez et régénérez des parties, éditez les lorebooks, gérez vos modèles et
(en tant qu'administrateur) l'installation depuis le navigateur du téléphone. La mise en page téléphone **n'est pas une copie de la version bureau** : les
mêmes éléments sont disposés en une seule colonne, certaines fenêtres sont plus simples et toutes les extensions ne fonctionnent pas dans cette mise en page. Cette page liste
les différences.

Pour lire un livre sur téléphone, il existe aussi la [visionneuse](books/publishing.md), conçue pour cet usage.

<a id="when-the-phone-layout-is-used"></a>

## Quand la mise en page téléphone est utilisée

Marginalia regarde la largeur de la fenêtre du navigateur **au chargement de la page**. Une largeur inférieure à 768 pixels donne la mise en page téléphone, au-delà on obtient
l'[espace de travail](index.md#the-workspace) habituel.

- Les tablettes et les téléphones en mode paysage ont généralement une largeur supérieure à 768 pixels et reçoivent la mise en page bureau.
- La mise en page **ne change pas pendant que vous travaillez**. Si vous tournez le téléphone ou la tablette, ou si vous redimensionnez la fenêtre, la mise en page reste
  la même jusqu'à ce que vous **rechargiez la page**. Pour obtenir la mise en page bureau, plus large, sur une tablette ou un téléphone tourné, tournez-le puis rechargez ; pour revenir à la mise en page téléphone, retournez-le et rechargez à nouveau.
- Une fenêtre de navigateur bureau étroite reçoit elle aussi la mise en page téléphone, ce qui permet de l'essayer facilement.

Comme pour la version bureau, la mise en page téléphone peut être ajoutée à l'écran d'accueil comme une application (*Install app* / *Add to Home Screen*
dans le menu du navigateur).

<a id="the-workspace"></a>

## L'espace de travail

Les onglets de gauche sont remplacés par un **tiroir**. Touchez le bouton de menu dans la barre supérieure pour l'ouvrir, touchez un onglet pour y accéder.
L'onglet actif occupe tout l'écran et son nom s'affiche dans la barre supérieure.

![L'espace de travail téléphone avec le tiroir ouvert](../images/phone-workspace-drawer.png)

Le tiroir contient les mêmes onglets que la version bureau - *Livres*, *Lorebooks*, *Paramètres*, *Protocoles*, *Fournisseurs d'inférence*,
*Agents* (uniquement pour les utilisateurs autorisés à utiliser les [agents](agents.md#on-a-phone)), *Ressources* et *Corbeille* - et, pour un administrateur,
*Administration* (le bouton de la version bureau devient un onglet ici). En bas se trouvent le sélecteur **Langue**, **Changer le mot de passe** et **Déconnexion**. Les extensions peuvent ajouter des boutons, mais uniquement
aux emplacements qu'elles connaissent sur téléphone, voir [Extensions](#extensions).

Tant que l'onglet *Paramètres* contient des modifications non enregistrées, le tiroir est verrouillé comme sur la version bureau : enregistrez ou abandonnez d'abord.

<a id="books"></a>

### Livres

La liste des livres est en une colonne. Au-dessus se trouvent le filtre par nom, le filtre par étiquette et les boutons de tri (*Dernière modification*,
*Créé*, *Nom* ; touchez le bouton actif une seconde fois pour inverser l'ordre), puis **Ajouter un livre**, la loupe (la [recherche avancée](books/managing-books.md#advanced-search) : étiquettes à inclure et à exclure, dates, nombre de parties et texte) et l'actualisation. Chaque livre a
des boutons : l'étoile en fait un favori, **Déplacer vers le dossier** le place dans un [dossier](books/folders.md), le crayon l'ouvre et la corbeille le supprime (dans la [Corbeille](trash.md)).

![La liste des livres sur téléphone](../images/phone-book-list.png)

La liste affiche un [dossier](books/folders.md) à la fois, comme sur la version bureau : le chemin du dossier dans lequel vous vous trouvez, sous forme de boutons pour revenir en arrière,
la case **Inclure les sous-dossiers**, puis les dossiers au-dessus des livres (touchez-en un pour y entrer ; il indique le nombre de livres qu'il contient) et, au
niveau racine, le dossier virtuel **Favoris**. Les filtres par nom et par étiquette cherchent dans le dossier courant, ou dans tout ce qu'il contient avec *Inclure les
sous-dossiers*. **Ajouter un livre** et les imports placent le nouveau livre dans le dossier dans lequel vous vous trouvez. Le dossier courant est mémorisé.

![Dossiers sur téléphone](../images/phone-folders.png)

![Le menu de la liste des livres](../images/phone-books-menu.png)

Un dossier se crée avec le bouton **Nouveau dossier** de l'en-tête, et se renomme, se configure (description, étiquettes, langue, modèle et protocole
pour ses livres) ou se supprime depuis le bouton **⋮** de sa ligne. Le bouton **⋮** de l'en-tête donne accès aux fenêtres qui agissent sur plusieurs livres :
**Organiser** (les deux volets de dossiers sont l'un au-dessus de l'autre sur téléphone, il n'y a pas d'indication au clavier), **Étiqueter des livres**, **Étiqueter des dossiers** et
les modifications en masse des [dossiers](books/folders.md) (modèle et protocole, langue, renommage). La
langue d'un livre peut aussi être héritée d'un dossier ici, et l'onglet *Prompts* indique d'où vient chaque valeur.

### Lorebooks

Le téléphone dispose de l'**éditeur de lorebook complet** de la version bureau, disposé en une colonne. Le lorebook se choisit dans une liste ; le bouton **⋮**
à côté ouvre le menu du lorebook : **Ajouter un lorebook**, **Cloner**, **Supprimer**, **Importer** (un fichier de lorebook, un fichier SillyTavern, une archive zip de fichiers Markdown, un lorebook partagé par un autre utilisateur), **Exporter** (le lorebook ou le format SillyTavern),
**Étiqueter des lorebooks** et **Modifier la description**. Sous la case *Activé*, le compteur de jetons et les boutons **Rechercher et remplacer** et **aperçu d'activation**, la section **Paramètres** s'ouvre sur le nom, les sous-lorebooks, les étiquettes, la profondeur d'analyse et *Partager avec d'autres utilisateurs* du lorebook. Voir [Lorebooks](lorebooks/index.md).

En dessous se trouvent **Ajouter une entrée** (et le **⋮** avec la modification en masse et les étiquettes de plusieurs entrées), la zone de recherche et les entrées. Une entrée s'ouvre
avec sa case d'activation et son nom sur une même ligne ; ouverte, elle présente un formulaire : nom, ordre, étiquettes, mode de filtrage et texte, mode d'insertion, étiquettes négatives,
le contexte d'activation et sa portée, *Chercher aussi dans les résumés*, *Empêcher la récursion*, *Seulement via d'autres entrées*, le contenu, la note et
**Supprimer l'entrée**. Tout ce qui est modifié est enregistré immédiatement, comme sur la version bureau. Sous la zone de recherche se trouvent les cases d'étiquettes et de statut qui
limitent la liste ; **Sélectionner des entrées** dans le menu **⋮** place une case devant chaque entrée et affiche **Supprimer la sélection** pour celles qui sont cochées. Ce qui n'est pas ici : la case *Seulement les lorebooks racines*.

L'onglet **Lorebook** du tiroir de l'histoire affiche le lore du livre comme l'onglet de la version bureau : une zone de recherche, les entrées qui s'ouvrent sur leur
contenu, et le crayon qui modifie le texte d'une entrée.

Le même éditeur est utilisé dans l'onglet *Lorebook* d'un livre. Lorsque le lorebook du livre a des sous-lorebooks, la liste
**Lorebook affiché** sous celui-ci permet de passer l'onglet à l'un d'eux, puis de revenir, sans changer le lorebook du livre. Pendant qu'un [agent](agents.md) travaille sur un livre qui utilise le lorebook, celui-ci s'affiche en texte et rien de son contenu ne peut être modifié.

![Un lorebook sur téléphone](../images/phone-lorebook.png)

![Le menu du lorebook](../images/phone-lorebook-menu.png)

![Les paramètres du lorebook](../images/phone-lorebook-settings.png)

![Une entrée ouverte dans son formulaire, avec les révisions du Lorebook VCS au-dessus](../images/phone-lorebook-entry.png)

Le bouton à côté de la case *Activé* ouvre **Rechercher et remplacer** pour les entrées du lorebook, voir
[Entrées](lorebooks/entries.md#search-and-replace).

![Rechercher et remplacer dans un lorebook sur téléphone](../images/phone-lorebook-search-replace.png)

<a id="settings-protocols-providers-resources-trash"></a>

### Paramètres, protocoles, fournisseurs, ressources, corbeille

- **Paramètres** sont les mêmes que sur la version bureau, disposés dans une colonne étroite ; **Importer la sauvegarde comme nouveau livre** est le bouton situé au-dessus de *Abandonner les modifications*.
- **Protocoles** et **Fournisseurs d'inférence** listent chaque entrée sous forme de texte avec les boutons à côté (en dessous, pour les fournisseurs, qui en ont davantage). Les boîtes de dialogue
  d'édition sont celles de la version bureau, de sorte qu'un protocole peut être [partagé](protocols.md#sharing-a-protocol) depuis le téléphone, et le bouton
  **Importer d'un autre utilisateur** au-dessus de la liste importe un protocole partagé par un autre utilisateur.
- **Ressources** regroupe les fichiers par l'objet qui les utilise (ouvrez un groupe avec sa flèche). Chaque fichier est une carte (une image, le nom, le type, la taille, la date et les boutons pour télécharger, remplacer et supprimer), avec
  une case à cocher pour *Supprimer la sélection* ; cocher un groupe supprime tous ses fichiers.
- **Corbeille** affiche chaque objet supprimé sous forme de carte (nom, type, date de suppression). L'explication de la corbeille
  se trouve derrière le bouton *i* ; les boutons des filtres passent sur une seconde ligne. La restauration fonctionne comme décrit dans [Corbeille](trash.md).

![Les ressources sur téléphone](../images/phone-resources.png)

![Les fournisseurs d'inférence sur téléphone, avec le bouton d'information d'un fournisseur partagé](../images/phone-providers.png)

![La corbeille sur téléphone](../images/phone-trash.png)
<a id="the-book-window"></a>

## La fenêtre du livre

Ouvrir un livre l'affiche en **plein écran**. Le bouton de fermeture est le **✕** dans le coin supérieur, et les onglets - *À propos*,
*Prompts*, *Lorebook*, *Histoire*, *Vue des branches*, *Sauvegardes* - forment une seule rangée que l'on fait défiler latéralement. Ils fonctionnent comme sur ordinateur (voir [Livres](books/index.md)), avec leurs champs disposés en une seule colonne.

<a id="the-story"></a>

### L'histoire

L'onglet histoire est une colonne unique de parties. Les différences avec l'[éditeur d'histoire](books/story-editor.md) :

![L'histoire d'un livre sur un téléphone](../images/phone-book-story.png)

- Le **plan** (et les onglets que les extensions ajoutent à côté, comme la [Note de l'auteur](extensions/authors-note.md))
  se trouve dans un **tiroir**. Ouvrez-le avec le bouton en haut à gauche.
- Le bouton **Résumés** et le menu des réglages (⚙, avec *Changer les styles du texte*, *Rechercher et remplacer*, *Importer*, *Suppression en masse* et *Exporter l'histoire*) se trouvent dans la barre supérieure, et non
  en bas.
- Le bouton **+** pour une nouvelle partie se trouve en bas.
- La fenêtre **Résumés** a sa barre de menus (*Rechercher et remplacer*, *Créer des résumés...*, *Refaire tous les résumés*, *Exporter les résumés*, *Importer des résumés*) sur une ligne à part ;
  sur un écran étroit, les éléments sont derrière le bouton **⋯**. [Importer des résumés](books/summaries.md#export-and-import-of-the-summaries)
  est la même fenêtre que sur ordinateur, à la largeur de l'écran.

![Importer des résumés sur un téléphone](../images/phone-summaries-import.png)
- [Annotations](books/annotations.md) : l'onglet **Annotations** se trouve dans le tiroir à côté du plan ; un appui sur un passage souligné
  ouvre sa note ; **Annoter la partie** et **Annoter avec l'IA** se trouvent dans le menu de la partie, **Annoter la sélection** dans le menu d'un appui long sur le texte sélectionné.
- **Rechercher et remplacer** ouvre la même fenêtre que sur ordinateur, avec les champs et les boutons les uns sous les autres :

  ![Rechercher et remplacer dans l'histoire sur un téléphone](../images/phone-search-replace.png)

- Une partie ne montre que son texte, sous une petite ligne avec deux icônes. La première est le **menu** de la partie (modifier, régénérer,
  balayer, brancher, historique, afficher le prompt, images, résumé, supprimer) ; la seconde ouvre une **fenêtre contextuelle** avec ce que l'ordinateur affiche dans la
  colonne à droite d'une partie : les identifiants, le modèle, le protocole, les jetons, les mots et les détails du tour.
- **Historique** dans le menu d'une partie ouvre l'[historique des versions](books/version-history.md) en page plein écran : les versions sont
  des cartes au-dessus de la version sélectionnée, avec les mêmes actions que sur ordinateur.

  ![L'historique des versions d'une partie sur un téléphone](../images/phone-history.png)

- **Statistiques** se trouve dans le menu des réglages de l'histoire (les statistiques du livre) et sous forme de bouton dans l'en-tête de la liste des livres
  (tous les livres). La fenêtre est une page plein écran ; son calendrier affiche les 16 dernières semaines (une année sur ordinateur), voir
  [Statistiques d'écriture](books/statistics.md).

  ![Les statistiques de tous les livres sur un téléphone](../images/phone-statistics.png)

- La vue d'ensemble des [résumés](books/summaries.md) garde une largeur fixe et défile latéralement.

## Administration

Un administrateur dispose de l'onglet **Administration** dans le tiroir, avec les mêmes onglets que la [page d'administration](administration/index.md) sur ordinateur, dans une rangée que l'on fait défiler latéralement :
*Utilisateurs*, *Sauvegardes de la base de données*, *Nettoyage*, *Fichiers de ressources*, *Assistant d'aide*, *Agents*, *Mettre à jour* et *Extensions*. Les listes (utilisateurs, sauvegardes, extensions) sont
des cartes avec leurs boutons sous le texte, et les barres d'outils passent sur une seconde ligne. Installer une extension (*Load Extension*) et téléverser une sauvegarde de la base ouvrent le sélecteur de fichiers du téléphone. Les autres utilisateurs n'ont pas cet onglet.

![Les utilisateurs de l'onglet Administration sur un téléphone](../images/phone-admin-users.png)

![Les sauvegardes de la base de données sur un téléphone](../images/phone-admin-backups.png)

<a id="what-is-missing"></a>

## Ce qui manque

Le téléphone fait ce que fait l'ordinateur, avec ces différences :

| Absent ou différent sur le téléphone | Où le faire |
|---|---|
| La case *Seulement les lorebooks racines* et la sélection de nombreuses entrées de lorebook à supprimer d'un coup | Disposition ordinateur. |
| La barre de commandes, les raccourcis clavier, la vue des différences d'une partie régénérée et les notes en marge | Disposition ordinateur, ils nécessitent un clavier ou un grand écran. |
| Les fenêtres avec beaucoup de colonnes ou deux volets (la vue d'ensemble des [résumés](books/summaries.md), la conversation des Beta Readers, le Générateur de manga) sont petites | Elles fonctionnent : le mode paysage est plus pratique. |

Tout ce qui ne figure pas dans cette liste fonctionne comme sur ordinateur : générer, modifier, régénérer, balayer et brancher
des parties, les résumés et méta-résumés, les prompts du livre, l'arbre des branches, les sauvegardes du livre, l'export, les ressources, la corbeille, les lorebooks et l'administration.

## Extensions

La disposition téléphone a ses propres classes, donc une extension doit la prendre en charge explicitement. Les extensions livrées avec
Marginalia :

| Extension | Sur le téléphone |
|---|---|
| [Arc Outline](extensions/arc-outline.md) | Fonctionne. L'onglet Arcs se trouve dans le tiroir du plan. |
| [Note de l'auteur](extensions/authors-note.md) | Fonctionne. La note se trouve dans le tiroir du plan de l'histoire. |
| [Beta Readers](extensions/beta-readers.md) | Fonctionne. L'onglet Beta Readers se trouve dans le tiroir du plan ; l'espace de la conversation est réduit, le mode paysage est plus pratique. |
| [Générateur de description de livre](extensions/book-description-generator.md) | Fonctionne. Le bouton se trouve à la fin de l'onglet À propos ; la boîte de dialogue affiche les options au-dessus du résultat. |
| [Chapter Marker](extensions/chapter-marker.md) | Fonctionne. Les titres de chapitres se trouvent dans le tiroir du plan. |
| [Character Tracker](extensions/character-tracker.md) | Fonctionne. L'onglet Personnages se trouve dans le tiroir du plan. |
| [Story Notes](extensions/story-notes.md) | Fonctionne. L'onglet Notes se trouve dans le tiroir du plan ; la boîte de dialogue de page tient à l'écran, une page longue est plus facile à écrire en paysage. |
| [Conlang Translator](extensions/conlang-translator.md) | En partie. Le panneau dans *Paramètres → Paramètres de l'extension*, l'éditeur, la relecture et l'assistant fonctionnent (la grille du lexique défile latéralement), de même que l'appui sur une réplique traduite dans la visionneuse ; le côté histoire (**Traduire les dialogues**, **Traduire les parties (langue construite)**, le clic dans l'histoire) n'a pas été essayé sur un téléphone, voir [Limites](extensions/conlang-translator.md#limits). |
| [Convertisseur](extensions/sillytavern-converter.md) | Fonctionne. L'onglet Convertisseur se trouve dans le tiroir du plan ; la plage de la conversation se choisit en deux appuis, voir [Sur un téléphone](extensions/sillytavern-converter.md#on-a-phone). |
| [Exportateur d'instructions](extensions/instruction-exporter.md) | Fonctionne. **Exporter les instructions** se trouve dans le menu des réglages de l'histoire. |
| [Générateur de manga](extensions/manga-generator.md) | Fonctionne. **Manga Generator** se trouve dans le menu des réglages de l'histoire ; sa fenêtre est grande, le mode paysage est plus pratique. |
| [Reviewer](extensions/reviewer.md) | Fonctionne. Ses éléments se trouvent dans le menu d'une partie, ses réglages dans *Paramètres → Paramètres de l'extension*. |
| [Side Query](extensions/side-query.md) | Fonctionne. L'onglet Side Query se trouve dans le tiroir du plan. |
| [Générateur de lore](extensions/lore-generator.md) | Fonctionne. Le bouton baguette se trouve dans la rangée de chaque entrée du lorebook ; sa fenêtre affiche le prompt au-dessus du résultat. |
| [Lorebook VCS](extensions/lorebook-vcs.md) | Fonctionne. Le panneau du lorebook se trouve sous sa ligne *Activé*, les révisions d'une entrée sont en haut de son formulaire. |
| [Générateur d'images](extensions/image-generator.md) | Fonctionne. **Generate image** se trouve dans le menu d'une partie. |

Les extensions provenant d'autres sources peuvent se comporter différemment, ou ne rien ajouter du tout sur le téléphone ; elles apparaissent là où leur
auteur a fait en sorte qu'elles fonctionnent. Si une extension semble manquer, vérifiez-la dans la disposition ordinateur. La manière dont une extension
prend en charge le téléphone est décrite dans le [manuel développeur](../developer/plugins/extendable.md#the-phone-twins).

<a id="tips"></a>

## Astuces

- Écrire un long texte sur un téléphone est plus facile en mode paysage. La disposition reste celle du téléphone après le tour, jusqu'à
  ce que vous rechargiez la page.
- Si vous avez besoin de quelque chose qui manque sur le téléphone, tournez le téléphone sur le côté et rechargez la page, ou ouvrez Marginalia dans une
  fenêtre plus large ; vos livres et vos réglages sont les mêmes partout.
- Pour accéder à Marginalia depuis un téléphone, il faut qu'il tourne en tant que [serveur](getting-started/docker-server.md) : l'
  [application de bureau](getting-started/desktop-app.md) n'écoute que sur votre ordinateur.
