---
label: Gestion des livres
order: 100
---

# Gestion des livres
<a id="the-book-list"></a>

## La liste des livres

L'onglet **Livres** liste vos livres. Les livres des autres utilisateurs ne sont pas affichés. La liste affiche un [dossier](folders.md) à la fois : les dossiers du dossier courant apparaissent au-dessus de ses livres ; au niveau racine, sans dossier créé, il s'agit simplement de tous vos livres.

![L'onglet Livres](../../images/books.png)

| Colonne | |
|---|---|
| **Nom** | Le nom du livre. |
| **Mots-clés** | Les mots-clés du livre. |
| **Créé** | Date de création du livre. |
| **Dernière modification** | Date de la dernière modification du livre. |

La liste est triée par *Dernière modification*, les plus récents en premier. Cliquez sur un en-tête de colonne pour trier par cette colonne ; avec plusieurs colonnes de tri, la liste est triée par toutes, dans l'ordre.

La ligne sous les en-têtes filtre la liste :

- **Nom** - livres dont le nom *commence* par le texte. Utilisez `*` comme joker : `*dragon` trouve chaque livre ayant « dragon » n'importe où dans le nom.
- **Mots-clés** - livres qui ont *tous* les mots-clés sélectionnés.

Le bouton d'actualisation à la fin de la ligne de filtres recharge la liste.

Chaque ligne a des boutons : l'étoile met le livre en [favori](folders.md#favorites), le crayon l'ouvre, **Déplacer vers le dossier** le place dans un [dossier](folders.md), le bouton de clonage en fait une copie et la corbeille le supprime. L'en-tête contient **Nouveau dossier**, le menu **Gérer** (**Organiser**, la fenêtre pour [déplacer et copier](folders.md#organise) livres et dossiers, **Étiqueter des livres**, **Statistiques** et le sous-menu **Modifications en masse**) et **Importer**, qui crée un nouveau livre à partir de fichiers que vous possédez (voir [Importer un livre](#importing-a-book)).

<a id="a-book-is-open-in-one-window"></a>

### Un livre est ouvert dans une seule fenêtre

Un livre ne peut être ouvert que dans une seule fenêtre à la fois, y compris entre vos onglets de navigateur, d'autres navigateurs et votre téléphone. Lorsque vous ouvrez un livre ouvert ailleurs, Marginalia indique depuis quand et demande **Ouvrir ici** : le livre s'ouvre alors ici et sa fenêtre à l'autre endroit se ferme, en indiquant pourquoi (si une partie est en cours de génération là-bas, la question le signale et **Ouvrir ici** arrête la génération ; ce qui a déjà été écrit reste). **Annuler** le laisse où il est. Lorsque vous vous connectez et que Marginalia ouvre le livre que vous aviez ouvert en dernier, il ne prend pas le livre à une autre fenêtre : il le laisse fermé et vous indique où il se trouve. Une fenêtre restée dans un onglet fermé ou sur un appareil éteint ne retient pas le livre : **Ouvrir ici** le prend immédiatement.

<a id="advanced-search"></a>

### Recherche avancée

**Rechercher** (dans l'en-tête, à côté de **Gérer** ; la loupe de l'en-tête sur téléphone) ouvre une fenêtre qui trouve des livres selon plus de critères que la ligne de filtres. Remplissez ce que vous voulez vérifier, laissez le reste vide, et appuyez sur **Rechercher** (ou sur Enter dans un champ de texte) :

| Champ | Trouve les livres... |
| ----- | -------------- |
| **Nom** | dont le nom contient le texte (`*` est un joker). |
| **Mots-clés à inclure** | qui ont les mots-clés : *Tous les mots-clés* ou *N'importe lequel des mots-clés*, selon votre choix à côté du champ. |
| **Mots-clés à exclure** | qui n'ont pas ces mots-clés : avec *N'importe lequel des mots-clés*, un livre qui en a au moins un est écarté ; avec *Tous les mots-clés*, seul un livre qui les a tous est écarté. |
| **Created from / to**, **Modified from / to** | créés ou dernièrement modifiés dans la plage ; le jour de la date *to* est inclus. |
| **Number of parts from / to** | ayant ce nombre de parties dans toutes ses branches (les parties dans la corbeille ne sont pas comptées). |
| **Texte** | avec une partie contenant chaque mot du texte. Les mots, `"phrases"` et `*` fonctionnent comme dans [la recherche dans l'histoire](story-editor.md#searching-the-story). |
| **Inclure les versions** | désactivé (par défaut) : seul le texte actuel des parties est cherché ; activé : un livre est aussi trouvé si une version antérieure d'une de ses parties contient les mots, voir [Historique des versions](version-history.md#searching-the-old-text). |
| **Rechercher dans toutes les branches** | désactivé (par défaut) : le texte est cherché seulement dans la branche où se trouve le livre (l'histoire telle que vous la voyez en ouvrant le livre) ; activé : dans les parties de toutes les branches. |

Un livre doit réussir chaque critère que vous avez rempli. Les livres trouvés sont listés en dessous avec leurs mots-clés, les plus récemment modifiés en premier ; un clic sur un en-tête de colonne les trie. Le bouton en forme de dossier ouvre un livre (dans sa fenêtre au-dessus de la recherche, si bien que fermer le livre vous ramène aux résultats). **Effacer** vide les champs.

![La recherche avancée des livres](../../images/book-search-advanced.png)

<a id="cloning-a-book"></a>

### Cloner un livre

Le bouton de clonage demande le **Nom** du nouveau livre (le nom de l'original suivi de *(Copy)* par défaut) et propose la case **Seulement la branche actuelle**, cochée par défaut. Le nouveau livre est un livre distinct qui reçoit :

![Clonage d'un livre](../../images/book-clone.png)

- les paramètres du livre (description, langue, prompts, point de vue, temps, style, paramètres de sauvegarde), les données que les extensions conservent dans le livre, et les mots-clés ;
- le même **Modèle**, **Protocole** et le même **lorebook**. Le lorebook est *lié*, et non copié : une modification de celui-ci apparaît dans les deux livres ;
- l'histoire avec sa propre copie des parties, de leurs résumés et de leurs images. Avec **Seulement la branche actuelle**, seules les parties du début à la fin de la [branche active](branches-and-story-tree.md) sont copiées ; sans cette option, toutes les branches le sont.

Le clone démarre un [historique des versions](version-history.md#backups-copies-and-the-trash) propre (une version par partie) ; l'historique de l'original n'est pas copié.

Le clone n'est pas publié, même si l'original l'est. L'original n'est pas modifié. Il est placé dans le même [dossier](folders.md) que l'original.

<a id="tagging-many-books-at-once"></a>

### Étiqueter plusieurs livres à la fois

**Étiqueter des livres** (dans le menu **Gérer** de l'en-tête) ouvre une fenêtre qui ajoute un mot-clé à plusieurs livres, ou le retire, sans ouvrir chacun. Choisissez le **Mot-clé** (ou tapez-en un nouveau puis appuyez sur Enter) : la liste affiche chaque livre avec ses mots-clés ; les livres qui ont le mot-clé choisi apparaissent en premier et le montrent en surbrillance. Chaque ligne a un bouton **+** qui applique le mot-clé à ce livre et un bouton **-** qui le retire. Pour faire de même avec plusieurs livres, cochez leurs cases (la case de l'en-tête coche tout) et utilisez **Appliquer le mot-clé aux éléments cochés** ou **Retirer le mot-clé des éléments cochés** en bas. Un livre sur lequel un agent travaille est grisé : il est verrouillé, donc il ne peut être coché ni modifié tant que l'exécution n'est pas mise en pause ou arrêtée. La même fenêtre étiquette [les lorebooks et leurs entrées](../lorebooks/lorebooks.md#tagging-many-at-once).

![Étiquetage de livres](../../images/multi-tag-books.png)

<a id="changing-the-model-and-protocol-of-many-books"></a>

### Changer le modèle et le protocole de plusieurs livres

Le sous-menu **Modifications en masse** du menu **Gérer** contient l'élément **Changer le modèle/protocole en masse**. Il ouvre une fenêtre qui applique un seul **Modèle** et/ou un seul **Protocole** à plusieurs livres, sans ouvrir chacun. Laissez une case vide (elle affiche *Ne pas remplacer*) et cette partie des livres reste inchangée : choisissez seulement un modèle pour changer les modèles et conserver les protocoles. Le tableau liste les livres du [dossier](folders.md) courant par nom, avec le modèle et le protocole qu'ils ont actuellement ; **Inclure les sous-dossiers** ajoute les livres qui s'y trouvent. Tapez dans le filtre sous **Nom** pour réduire la liste, cochez les livres à modifier (la case de l'en-tête coche tous les livres affichés) et appuyez sur **Remplacer et quitter**. Les livres que le filtre masque sont décochés, donc seul ce que vous voyez est modifié. Un livre sur lequel un agent travaille est grisé : il est verrouillé, donc il ne peut être coché ni modifié tant que l'exécution n'est pas mise en pause ou arrêtée.

![Changement du modèle et du protocole de plusieurs livres](../../images/mass-change-model-protocol.png)

L'élément **Changer la langue en masse** du même menu fonctionne de la même façon pour la **Langue** des livres : choisissez la langue, filtrez et cochez les livres, appuyez sur **Remplacer et quitter**. Le tableau indique la langue que chaque livre a actuellement, avec ce qu'il hérite de ses dossiers (voir [Langue](folders.md#language)).

![Changement de la langue de plusieurs livres](../../images/mass-change-language.png)

<a id="renaming-many-books"></a>

### Renommer plusieurs livres

L'élément **Renommage en masse** renomme les livres cochés en une seule fois. Choisissez la méthode :

| Méthode | Ce qu'elle fait |
|---|---|
| **Ajouter un préfixe** | Place le **Texte à ajouter** avant le nom. |
| **Ajouter un suffixe** | Place le **Texte à ajouter** après le nom. |
| **Rechercher et remplacer** | Remplace chaque occurrence de **Rechercher** dans le nom par **Remplacer par** (vide le supprime). Le texte est pris tel quel, et non comme un motif ; **Respecter la casse** est désactivé par défaut. |
| **Numéroter les livres** | Donne des numéros aux livres : le **Modèle de nom** est le nouveau nom, avec `{name}` pour le nom actuel et `{n}` pour le numéro, par exemple `{name} {n}`, `{n}. {name}` ou `Chapter {n}`. Le premier livre coché reçoit **Commencer à**, chacun des suivants reçoit **Pas** de plus, et **Chiffres** complète le numéro avec des zéros (`007`). |

![Numérotation des livres de tous les dossiers](../../images/mass-rename.png)

La colonne **Nouveau nom** affiche ce que chaque livre coché obtiendrait pendant que vous tapez, vérifiez-la donc avant d'appuyer sur **Renommer**. Une numérotation compte les livres cochés dans l'ordre de la liste (par nom). Les espaces au début et à la fin du nouveau nom sont supprimés. Rien n'est renommé, et la fenêtre reste ouverte, lorsqu'un texte manque, que le modèle n'a pas `{n}`, qu'aucun nom ne changerait, ou qu'un nom finirait vide. Le filtrage, **Inclure les sous-dossiers** et les livres verrouillés fonctionnent comme dans les autres modifications en masse.

**Étiqueter des livres** et l'élément **Étiqueter des dossiers** agissent de la même façon sur les livres, ou sur les dossiers, du dossier courant.
<a id="creating-a-book"></a>

## Créer un livre

1. Cliquez sur **Ajouter un livre**.
2. Saisissez le nom puis confirmez.

Le nouveau livre est placé dans le [dossier](folders.md) affiché dans la liste, comme les livres que vous importez. Il s'ouvre aussitôt. Il démarre avec votre **Modèle par défaut** et votre **Protocole par défaut** de l'onglet *Paramètres*
(voir [Compte et paramètres](../account.md)), si vous les avez définis ; sinon, sélectionnez-les dans l'onglet *À propos* avant
de générer du texte.

<a id="importing-a-book"></a>

### Importer un livre

**Importer** dans l'en-tête de la liste (le bouton fichier sur téléphone) ouvre une seule fenêtre pour tout ce dont un nouveau livre peut être composé.
Choisissez la **Source** ; le texte, les fichiers acceptés et les options sous le nom suivent ce choix.

| Source | Envoi | Options |
|---|---|---|
| **Importer depuis une archive** | une `.zip` de fichiers `.md`, une partie par fichier, voir [Importer dans le livre](story-editor.md#importing-into-the-book) | aucune |
| **Importer depuis SillyTavern** | une ou plusieurs discussions `.jsonl`, voir [ci-dessous](#importing-from-sillytavern) | fusionner les messages de l'utilisateur et de l'assistant |
| **Importer depuis LM Studio** | une ou plusieurs conversations, voir [ci-dessous](#importing-from-lm-studio) | fusionner les messages de l'utilisateur et de l'assistant |
| **Importer depuis un document** | un manuscrit dans un seul fichier (Word, LibreOffice, RTF, Markdown, texte brut, EPUB), voir [ci-dessous](#importing-a-finished-manuscript) | mots par partie, niveau de titre des chapitres |
| **Importer depuis un coffre Obsidian** | le `.zip` d'un coffre, voir [ci-dessous](#from-an-obsidian-vault-or-a-scrivener-project) | les mêmes |
| **Importer depuis un projet Scrivener** | le `.zip` d'un projet `.scriv`, voir [ci-dessous](#from-an-obsidian-vault-or-a-scrivener-project) | les mêmes |

Envoyez le fichier, donnez un nom au livre (il est rempli à partir du fichier, du titre d'un document ou du personnage d'une discussion ; le livre
s'appelle *Nouveau livre* si vous laissez le champ vide) et cliquez sur **Importer**. Une fenêtre de progression avec **Arrêter** affiche les parties
enregistrées ; arrêter conserve ce qui a déjà été enregistré. Un fichier que la source ne peut pas lire est refusé avec un avertissement, les autres envois sont conservés.

La même fenêtre, depuis le menu des paramètres (⚙) d'un livre, ajoute les parties à ce livre à l'emplacement de votre choix, voir
[Importer dans le livre](story-editor.md#importing-into-the-book).

![Les sources de la fenêtre d'import](../../images/import-source.png)

![Importer une archive zip de fichiers texte comme nouveau livre](../../images/archive-upload.png)

<a id="importing-a-finished-manuscript"></a>

### Importer un manuscrit terminé

Pour un brouillon déjà existant, choisissez **Importer depuis un document** et envoyez le fichier : un `.docx` (Word), un `.odt` (LibreOffice,
OpenOffice), un `.rtf` (Rich Text, de WordPad, TextEdit et de tout autre programme), un `.md` (un seul fichier Markdown), un `.txt` (texte brut) ou un `.epub` (un livre électronique). Le texte devient les parties d'un nouveau livre, entièrement marquées comme écrites par vous (l'import n'est pas compté dans les
[statistiques](statistics.md) comme écriture, et l'historique d'une partie indique *Import depuis un document*).

- **Chapitres.** Un paragraphe au style *Titre* (un titre `#` dans un fichier Markdown) commence un chapitre, et chaque chapitre commence par son titre (le premier
  titre d'une partie est son titre de chapitre, comme dans l'[export](export.md)). **Les chapitres commencent au niveau de titre** indique quels titres
  ce sont : le niveau que vous choisissez et ceux au-dessus de lui (le niveau le plus élevé du document est choisi au départ) ; les titres
  plus profonds restent dans le texte comme des titres plus petits. Un document sans titres est un seul chapitre. Le *Titre* du document n'est pas
  importé comme texte, il donne son nom au livre (dans un fichier Markdown, la `title:` d'un en-tête en haut, ou un seul titre `#` placé avant
  des chapitres `##`). Un fichier texte brut n'a pas de titres : une ligne seule qui indique `Chapter 12`, `Part IV: The Return`,
  `Book two` ou `Prologue` (aussi `Epilogue`, `Interlude`, `Preface`, `Foreword`, `Afterword`) commence alors un chapitre ; le choix
  du niveau de titre n'est alors pas nécessaire.
- **Mots par partie.** Le texte est découpé entre les paragraphes en parties d'environ ce nombre de mots (1000 au départ), à la limite de paragraphe la plus proche du nombre. Une partie ne contient jamais la fin d'un chapitre et le début du suivant, et un chapitre n'est pas coupé pour rien : ce qui resterait à la fin d'un chapitre, s'il représente moins de la moitié du nombre, reste avec la partie précédente,
  de sorte qu'un chapitre allant jusqu'à une fois et demie le nombre forme une seule partie. Un paragraphe plus long que le nombre n'est pas coupé.
- Une ligne sous le nom indique combien de chapitres, de mots et de parties l'import produira ; elle change quand vous modifiez les options.
- **Paragraphes d'un fichier texte.** Les lignes vides séparent les paragraphes et les lignes d'un paragraphe sont réunies (un texte coupé à une largeur donnée devient un seul paragraphe) ; un fichier sans lignes vides a un paragraphe par ligne. Un fichier Markdown est conservé tel qu'il est écrit, découpé aux lignes vides, aux titres et aux blocs de code ; un texte en Windows-1252 au lieu d'UTF-8 est lu aussi.
- **Mise en forme.** L'italique et le gras sont conservés (sous forme Markdown `*italic*`, `**bold**`) ; le reste de la mise en forme (polices, tailles,
  alignement), les images, les en-têtes et pieds de page, les notes de bas de page, les commentaires et le texte supprimé des modifications suivies ne sont pas importés. Les tableaux sont lus cellule par cellule.

![Importer un document Word, découpé en parties d'environ 500 mots](../../images/document-import.png)

Un ancien fichier `.doc` doit d'abord être enregistré au format `.docx`, `.odt` ou `.rtf`.

**Un EPUB** est lu page par page dans son ordre de lecture. Les titres des pages (`h1` à `h6`) sont les chapitres ; une page sans
titre à laquelle la table des matières du livre électronique attribue un nom reçoit ce nom comme titre. La couverture, la table des matières, les notes de bas de page et
les lectures du texte ruby sont laissées de côté, et le titre du livre électronique donne son nom au livre. Un livre protégé par DRM ne peut pas être lu.

<a id="from-an-obsidian-vault-or-a-scrivener-project"></a>

#### Depuis un coffre Obsidian ou un projet Scrivener

Les deux sont des dossiers : compressez d'abord le dossier en zip (sur Mac, **Compresser** dans le Finder ; le `.scriv` de Scrivener est aussi un dossier) et envoyez le `.zip`. Les options sont les
mêmes que pour un document.

- **Coffre Obsidian.** Chaque note (`.md`) est un chapitre nommé d'après la note (ou le `title:` de son en-tête), dans l'ordre des chemins, les numéros
  étant comptés comme des nombres (`Chapter 2` avant `Chapter 10`). Un dossier est une partie placée au-dessus de ses notes, et les titres à l'intérieur d'une note sont situés sous ceux de la note elle-même. Les liens `[[Note|text]]` deviennent leur texte ; les intégrations (`[[file]]` précédé d'un `!`), les commentaires `%%...%%`, les identifiants de bloc et le marqueur d'un encadré sont supprimés ; un surlignage `==text==` devient du texte brut. Les dossiers cachés (`.obsidian`, `.trash`), les pièces jointes et les notes sans texte sont ignorés. Si le
  coffre contient des notes qui ne font pas partie de l'histoire, supprimez-les avant de compresser.
- **Projet Scrivener.** Le manuscrit (le dossier *Draft* du classeur, Scrivener 2 et 3) est importé, pas les recherches, la corbeille ni les documents qui ne sont pas *inclus dans la compilation*. Un dossier, ou un document qui contient des documents, est un titre (sa profondeur dans le classeur est son niveau, son intitulé le texte) ; les documents qui sont tout ce qu'un dossier contient sont ses scènes, placées les unes après les autres avec un changement de scène entre elles (leurs intitulés ne sont pas utilisés). Un document directement dans le manuscrit, ou dans un dossier qui ne contient que d'autres dossiers, est un chapitre portant son intitulé. Le nom du projet donne son nom au livre.

Quelle que soit la source, le nouveau livre est ouvert à la fin de l'import (même si vous l'avez arrêté), et la fenêtre [**Import terminé**](story-editor.md#after-an-import) propose d'écrire les résumés des parties et de laisser un agent créer les notes de l'univers.

<a id="importing-from-sillytavern"></a>

### Importer depuis SillyTavern

Pour poursuivre un jeu de rôle ou une histoire commencés dans [SillyTavern](https://github.com/SillyTavern/SillyTavern), utilisez
**Importer → Importer depuis SillyTavern**. Dans SillyTavern, exportez la discussion (*Manage chat files*, ou prenez les fichiers `.jsonl` dans `data/<user>/chats/<character>/`), puis :

1. Envoyez **une ou plusieurs** discussions `.jsonl`. Un fichier qui n'est pas une discussion SillyTavern est refusé, les autres sont conservés.
2. Donnez un nom au livre (il est rempli à partir du personnage de la première discussion).
3. Cochez **Fusionner les messages de l'utilisateur et de l'assistant en une seule partie** si vous voulez une partie par tour : le message de l'utilisateur
   (plusieurs messages consécutifs sont réunis par une ligne vide) devient les *instructions* de la partie, et la réponse qui suit son texte. Sans cette option, chaque message devient une partie à part entière, avec son texte comme texte de la partie, de sorte que le livre se lit comme la discussion.
4. Cliquez sur **Importer**. Une fenêtre de progression avec **Arrêter** affiche les parties enregistrées ; arrêter conserve ce qui a été enregistré.

![Importer une discussion SillyTavern](../../images/sillytavern-import.png)

SillyTavern copie la discussion lorsque vous créez une *branche* ou un *point de contrôle*, de sorte que les discussions d'une même histoire partagent leur
début. Envoyez-les ensemble : Marginalia trouve les messages communs et les enregistre une seule fois ; les discussions deviennent les
branches d'un même arbre, que vous pouvez parcourir dans la [vue en arbre](story-editor.md). Les *swipes* (réponses alternatives) d'un message deviennent des parties soeurs, et le livre s'ouvre à la fin de la discussion écrite en dernier (d'après la date de son
dernier message). Les discussions qui ne commencent pas par le même message deviennent des premières parties distinctes. Les messages cachés (système) sont ignorés, ainsi que les swipes vides. Le raisonnement et le nom du modèle d'une réponse sont conservés avec la partie.

<a id="importing-from-lm-studio"></a>

### Importer depuis LM Studio

Pour poursuivre une conversation menée dans [LM Studio](https://lmstudio.ai), utilisez **Importer → Importer depuis LM Studio**.
LM Studio conserve chaque conversation dans un fichier `<number>.conversation.json`, dans le dossier `.lmstudio/conversations` de votre dossier personnel (sous Windows,
`%USERPROFILE%\.lmstudio\conversations`). Le fonctionnement est le même que pour [Importer depuis SillyTavern](#importing-from-sillytavern) :

1. Envoyez **une ou plusieurs** conversations. Un fichier qui n'est pas une conversation LM Studio est refusé, les autres sont conservés.
2. Donnez un nom au livre (il est rempli avec le nom de la conversation, ou le nom du fichier si elle n'en a pas).
3. Cochez **Fusionner les messages de l'utilisateur et de l'assistant en une seule partie** pour une partie par tour (votre message devient les *instructions* de la partie), ou laissez-la décochée pour que chaque message soit une partie à part entière.
4. Cliquez sur **Importer**.

![La fenêtre d'import depuis LM Studio avec une conversation envoyée](../../images/lmstudio-import.png)

Les versions d'un message que LM Studio conserve (les réponses régénérées, le texte modifié) deviennent des parties soeurs, et la version affichée est celle sur laquelle s'ouvre le livre ; les conversations qui partagent leur début sont réunies en un seul arbre. Le raisonnement d'un modèle de raisonnement est conservé comme raisonnement de la partie, avec le nom du modèle. Le prompt système de la conversation, les appels d'outils et les fichiers joints ne sont pas importés.

Pour démarrer un livre à partir d'un fichier de sauvegarde, utilisez **Importer la sauvegarde comme nouveau livre** dans l'onglet *Paramètres*, voir
[Sauvegardes du livre](backups.md#importing-a-backup-as-a-new-book).
<a id="the-about-tab"></a>

## L'onglet À propos

![L'onglet À propos d'un livre](../../images/book-about.png)

| Champ | |
|---|---|
| **Nom** | Le nom du livre. |
| **Mots-clés** | Mots-clés pour trier vos livres et pour l'activation du [lorebook](../lorebooks/entries.md). Tapez pour choisir un mot-clé existant ou en créer un nouveau. |
| **Description** | Ce dont parle le livre. Disponible pour les modèles de prompt sous la forme de la description du livre. |
| **Langue** | La langue dans laquelle le livre est écrit. Utilisée pour la césure automatique du texte dans le style de lecture du livre et dans la [visionneuse](publishing.md) ; elle ne change pas ce que le modèle écrit. Vide signifie *hériter* : la langue du [dossier](folders.md#language) le plus proche au-dessus du livre qui en définit une, sinon l'anglais ; la case indique alors laquelle et d'où elle vient. |
| **Modèle** | Le [fournisseur d'inférence](../inference-providers.md) qui écrit ce livre. |
| **Protocoles** | Le [protocole](../protocols.md) avec les réglages de génération. |
| **Versions conservées par partie** | Le nombre de versions antérieures du texte d'une partie que conserve l'[historique](version-history.md). Vide signifie *hériter* : le nombre gris dans la case est la limite qui s'applique, et la ligne en dessous indique quel dossier, vos réglages ou l'administrateur la fixe. **0** ne conserve aucun historique. |
| **Publié** | Permet à d'autres utilisateurs de lire le livre, voir [Publication et visionneuse](publishing.md). Une fois cochée, le lien de lecture du livre (`<address of Marginalia>/view/<book ID>`) s'affiche sous la case ; copiez-le et envoyez-le aux lecteurs. |
| **Montrer les images aux lecteurs** | Coché par défaut. Les lecteurs d'un livre publié voient les images de ses parties ; décochez pour ne leur montrer que le texte. Vous voyez toujours vos propres images. |

Les modifications sont enregistrées au fur et à mesure que vous tapez.

Plus bas figurent des compteurs en lecture seule :

| | |
|---|---|
| **Nombre total de mots**, **Nombre total de jetons** | Toutes les parties du livre, dans toutes les branches. |
| **Mots de la branche active**, **Jetons de la branche active** | Seulement les parties de la [branche active](branches-and-story-tree.md). |
| **Paternité de la branche** | Appuyez sur **Calculer** : le pourcentage des mots de la branche active que vous avez écrits et celui généré par un modèle, ainsi que le nombre de mots. Il est calculé à partir de l'[historique des versions](version-history.md) de chaque partie, il faut donc un certain temps sur un long livre ; une partie dont l'historique est désactivé est comparée au texte généré par le modèle en premier. Voir [Mise en évidence du texte généré](story-editor.md#highlighting-generated-text). |

Un livre a besoin d'un modèle et d'un protocole pour générer du texte. Sans eux, la génération s'arrête avec *Le livre n'a pas de modèle.* ou
*Le livre n'a pas de protocole.*

<a id="deleting-a-book"></a>

## Supprimer un livre

Cliquez sur le bouton de la corbeille dans la liste des livres, puis confirmez. Le livre disparaît de votre liste.

Un livre n'est pas supprimé tant que quelque chose travaille dessus : une partie en cours de génération, un lot de résumés, une traduction, une fenêtre de réécriture ou d'annotation, ou le livre ouvert dans une autre fenêtre. Marginalia indique alors ce qui fait obstacle (*Ceci est en cours d'utilisation : ...*) ; attendez la fin ou arrêtez-le, puis supprimez à nouveau. Il en va de même pour la restauration d'une [sauvegarde](backups.md) dans le livre pendant qu'une autre fenêtre ou une exécution travaille dessus.

Les livres supprimés ne sont pas retirés de la base de données immédiatement. Tant qu'un administrateur ne les supprime pas, avec toutes leurs parties, à l'aide de [Nettoyage](../administration/cleanup.md), vous pouvez ramener le livre depuis l'onglet [Corbeille](../trash.md). D'ici là, les données se trouvent encore dans les [sauvegardes de la base de données](../administration/database-backups.md). Les [sauvegardes](backups.md) propres au livre sont des fichiers et restent à leur emplacement.
