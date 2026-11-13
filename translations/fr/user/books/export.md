---
label: Exporter un livre
order: 55
---

# Exporter un livre

L'export transforme l'histoire en un fichier que vous pouvez lire, imprimer, envoyer ou publier ailleurs. Contrairement à une [sauvegarde](backups.md), il ne contient que le texte, sous forme de livre fini.

Seule la [branche active](branches-and-story-tree.md) est exportée, soit les mêmes parties que l'éditeur d'histoire affiche. Pour exporter une autre version de l'histoire, passez d'abord à cette branche.

<a id="exporting"></a>

## Exporter

1. Ouvrez le livre et allez dans l'onglet **Histoire**.
2. Ouvrez le menu des réglages (⚙) dans la barre du bas et choisissez **Exporter l'histoire**.
3. Remplissez la boîte de dialogue et choisissez **Exporter**.
4. Marginalia rassemble les parties de la branche et écrit le fichier, ce qui prend un moment pour un long livre. Une barre de progression indique l'avancement.
5. Lorsque le fichier est prêt, une boîte de dialogue propose de le télécharger. Choisissez le bouton **Télécharger** pour l'enregistrer.

![La boîte de dialogue Exporter l'histoire](../../images/export-dialog.png)

| Champ | |
|---|---|
| **Format** | Le type de fichier, voir ci-dessous. |
| **Style d'impression** | Uniquement pour les formats qui ont des polices et des couleurs (HTML, Word, PDF, EPUB). L'apparence du texte, choisie parmi les styles *pour les exports* : l'application en fournit un pour chacun de ces formats, **Page HTML**, **Livre électronique (EPUB)**, **Livre PDF** et **Document Word**, qui sont les apparences que les exports ont toujours eues et qui sont choisies automatiquement lorsque vous sélectionnez le format. Vous pouvez aussi créer le vôtre, voir [Styles d'impression](#print-styles). Le style que vous avez choisi pour le format dans [Réglages → Exports](../account.md#exports) est celui présélectionné. |
| **Nom du fichier** | Nom du fichier téléchargé sans l'extension, que Marginalia ajoute. Prend au départ le nom du livre. |
| **Titre du livre** | Le titre dans le fichier (page de titre et propriétés du fichier). Prend au départ le nom du livre ; le modifier ne renomme pas le livre. |
| **Auteur** | Prend au départ votre nom complet depuis votre [compte](../account.md). Vide signifie aucun auteur. |
| **Depuis le message**, **Jusqu'au message** | Les parties à exporter. Les parties sont numérotées à partir de 1 dans l'ordre de l'histoire ; les parties sans texte ne sont pas comptées. Laissez **Jusqu'au message** vide pour exporter jusqu'à la fin. |
| **Inclure une page de titre** | Commence le fichier par une première page contenant le titre et l'auteur. |
| **Inclure une déclaration d'auteur** | Termine le fichier par une courte déclaration indiquant qui a écrit les mots : combien de mots vous avez écrits et combien ont été générés par un modèle, en mots et en pourcentage, avec les modèles. Certains éditeurs et concours la demandent. Elle est calculée à partir de l'[historique des versions](version-history.md) des parties exportées (voir [Mise en évidence du texte généré](story-editor.md#highlighting-generated-text)). Non proposée pour le chat SillyTavern. Le texte est un modèle dans [Réglages → Exports](../account.md#exports). |

![L'export terminé, prêt à être téléchargé](../../images/export-download.png)

## Formats

| Format | |
|---|---|
| **Texte brut** (`.txt`) | Seulement le texte en UTF-8. La mise en forme comme *l'italique* est supprimée, les paragraphes sont séparés par une ligne vide. |
| **Markdown** (`.md`) | Le texte tel qu'il est écrit dans l'histoire, pour des éditeurs comme Obsidian ou Typora, un site statique ou un dépôt. L'emphase, les titres, les citations, les listes, les blocs de code et les séparations de scènes restent en Markdown. Le HTML brut présent dans le texte est conservé tel quel ; le lecteur Markdown décide de l'afficher ou non. |
| **HTML** (`.html`) | Une page unique avec un style de livre intégré, qui s'affiche de la même façon dans tous les navigateurs. Elle peut être imprimée et ne comporte aucun fichier externe. |
| **Document Word** (`.docx`) | S'ouvre dans Word, LibreOffice et des programmes similaires. Texte justifié, avec le titre et l'auteur définis dans les propriétés du fichier. |
| **PDF** (`.pdf`) | Pages au format A5 avec des polices intégrées couvrant les textes latins, grecs et cyrilliques, et une police japonaise fournie (Noto Sans JP) pour les kana, les kanji et la ponctuation japonaise. Le texte d'autres écritures (caractères chinois uniquement, coréen, arabe) n'est pas affiché : le PDF n'a pas de police pour ces caractères. |
| **EPUB** (`.epub`) | Un livre électronique pour liseuses et applications de lecture. Les longs livres sont répartis en plusieurs fichiers à l'intérieur de l'EPUB afin que les liseuses restent rapides. |
| **HTML pour Archive of Our Own** (`.html`) | Pour publier sur AO3, voir [ci-dessous](#for-other-tools). |
| **Markdown avec front matter** (`.md`) | Pour les générateurs de sites statiques, voir [ci-dessous](#for-other-tools). |
| **Chat SillyTavern** (`.jsonl`) | L'histoire sous forme de chat, voir [ci-dessous](#for-other-tools). |
| **Coffre Obsidian** (`.zip`) | Les chapitres et le manuel de lore sous forme de notes liées, voir [ci-dessous](#for-other-tools). |
| **LaTeX** (`.zip`) | Un projet LaTeX du livre (`main.tex` et les images), voir [ci-dessous](#for-other-tools). |

Le texte des parties est en Markdown. L'emphase, les titres, les citations, les listes, les blocs de code et les séparations de scènes (`---`) sont conservés dans chaque format capable de les afficher. Le HTML brut présent dans le texte est affiché comme du texte, jamais exécuté.

<a id="footnotes"></a>

## Notes de bas de page

Une note de bas de page s'écrit dans le texte d'une partie sous la forme `[^1]` à l'endroit où la marque doit apparaître, et `[^1]: la note` sur une ligne à part (l'étiquette peut être n'importe quel mot). Tous les formats les prennent en charge : **Markdown** conserve la syntaxe ; **HTML**, **EPUB** et **HTML pour Archive of Our Own** placent un petit numéro en exposant, relié aux notes situées sous le texte de la partie (AO3 reçoit le balisage simple que son éditeur conserve) ; **texte brut** écrit la marque sous la forme `[1]` et les notes sous forme de lignes commençant par `[1]` après le texte de la partie, ce qui correspond à l'aspect d'une note de fin. **Word** et **PDF** disposent de véritables notes de bas de page : la marque est un petit numéro en exposant, et la note se trouve en bas de la même page, dans une taille plus petite (dans le PDF, sous un court filet, et les pages sont mises en page jusqu'à ce que chacune ait la place nécessaire à ses notes ; Word numérote et place lui-même les notes, elles suivent donc si vous modifiez le document). Les extensions peuvent aussi les utiliser : l'extension Conlang Translator écrit la traduction anglaise de chaque réplique sous forme de note de bas de page.

## Images

Les [images](story-editor.md#images) des parties sont placées là où leur position l'indique (après le texte, sauf si vous avez choisi un autre emplacement), avec leurs légendes, dans la largeur que vous leur avez donnée (un pourcentage de la largeur du texte ; sans largeur, l'image conserve sa taille, réduite pour tenir sur la page). Tous les formats ne permettent pas de placer du texte à côté d'une image :

| Format | |
|---|---|
| **HTML**, **EPUB** | Intégrées (dans la page, ou sous forme de fichiers du livre). Le texte s'écoule autour des images, à gauche ou à droite ; une image au milieu se situe entre deux colonnes de texte. Sur un écran étroit, elles passent au-dessus du texte, et une liseuse sans colonnes (certaines liseuses) affiche le premier texte, puis l'image, puis le reste les uns sous les autres. Les images placées côte à côte à un même endroit forment une rangée. |
| **Document Word** | L'image autour de laquelle le texte s'écoule est placée dans un petit tableau que Word positionne sur le côté ou au milieu ; Word enveloppe le texte autour (des deux côtés pour l'image du milieu, ligne par ligne) ; la légende se trouve sous l'image. Les images d'une largeur suffisante pour tenir côte à côte forment une ligne de tableau. |
| **PDF** | Le texte s'écoule autour des images sur le côté, la légende est dessinée sous l'image. Une image au milieu se situe entre deux colonnes de texte aligné à gauche (un bloc qui ne tient pas en fin de page passe à la page suivante). Les images d'une largeur suffisante pour tenir côte à côte forment une rangée. Une image trop haute pour le reste de la page passe à la page suivante. |
| **Markdown** | Intégrées au fichier sous forme d'images `![légende](data:...)`, avec la légende en italique dessous, le fichier reste donc unique. La position parmi le texte est conservée ; la largeur et l'écoulement du texte ne le sont pas. Tous les lecteurs n'affichent pas les images intégrées (GitHub ne les affiche pas), et un livre avec beaucoup d'images produit des lignes très longues : n'attachez des images aux parties que si vous les voulez dans un export Markdown. |
| **Texte brut** | Marquées `[Image]` ou `[Image : légende]` à leur place dans le texte. |
| **HTML pour Archive of Our Own** | Marquées `[Image : légende]` : AO3 ne prend les images que depuis des liens. |
| **Coffre Obsidian** | Enregistrées dans le dossier `Attachments` du coffre et intégrées à la note de leur chapitre, à leur place, avec la légende en italique. |
| **LaTeX** | Enregistrées dans le dossier `images` du projet et incluses dans le texte à leur place (une figure à la largeur de l'image, jamais plus large que le texte), avec la légende dessous. Le texte ne s'écoule pas autour d'elles. |
| **Chat SillyTavern** | Omises, un chat ne contient que du texte. |
<a id="for-other-tools"></a>

## Pour d'autres outils

Ces formats sont conçus pour être lus par un outil, et non pour la lecture. Ils n'ont pas le choix *Style d'impression*.

**HTML pour Archive of Our Own.** AO3 demande le titre, l'auteur et les titres des chapitres dans ses propres champs, et supprime ce qu'il ne connaît pas (blocs de styles, la page autour du texte, les images qui ne sont pas des liens). Le fichier n'a donc pas de page de titre. Ouvrez-le dans un navigateur pour voir l'œuvre telle qu'elle apparaîtra après le collage. Chaque chapitre (une partie qui commence par un titre, et les parties qui suivent jusqu'au titre suivant) a son titre au-dessus de deux commentaires, `BEGIN chapter N` et `END chapter N` : copiez ce qui se trouve entre eux dans l'éditeur HTML du chapitre sur AO3, et le titre dans son champ titre. Le texte est du HTML propre, sans retours à la ligne entre les balises, car AO3 transforme chaque retour à la ligne en `<br>`.

**Markdown avec en-tête front matter.** Le même Markdown que le format *Markdown*, mais le fichier commence par un bloc YAML (`title`, `author`, `lang`, `date` de l'export) que lisent Jekyll, Hugo, Eleventy et les générateurs similaires, à la place d'une page de titre ; **Inclure une page de titre** n'a aucun effet sur ce format. Le bloc est un modèle que vous pouvez modifier dans [Paramètres → Exports](../account.md#exports) (`{{title}}`, `{{author}}`, `{{language}}`, `{{exportDate}}` ; le titre et l'auteur sont déjà échappés pour un usage entre guillemets doubles).

**Discussion SillyTavern.** Un fichier JSON Lines que lit l'import de discussions de SillyTavern : le personnage porte le nom du livre, et vous êtes l'auteur. Chaque partie de l'histoire est un message du personnage (le texte), et lorsque la partie a été écrite avec des instructions, celles-ci viennent avant elle sous forme de votre message. Le raisonnement du modèle, si la partie en a un, est conservé comme raisonnement du message. La page de titre ne s'applique pas.

**Coffre Obsidian.** Un ZIP avec un dossier portant le nom du livre ; décompressez-le et ouvrez le dossier comme coffre. Il contient une note portant le nom du livre (l'en-tête front matter, la page de titre et les listes des chapitres et du savoir), une note pour chaque chapitre dans `Chapters` (avec des liens vers le chapitre précédent et le suivant), une note pour chaque entrée activée du [lorebook](../lorebooks/index.md) du livre et de ses sous-lorebooks dans `Lore/<lorebook>` (les étiquettes de l'entrée deviennent les étiquettes de la note, et son commentaire est un callout), ainsi que les images dans `Attachments`. Dans chaque note, la première mention du nom d'une entrée (un nom d'au moins trois caractères, mots entiers, tel qu'écrit) est un lien vers sa note, de sorte que la vue graphe montre quelle entrée se trouve dans quel chapitre. Une partie avec un titre commence un chapitre ; les parties placées avant le premier titre forment la note *Début* (ou *Histoire* lorsque le livre n'a pas de titres). L'en-tête front matter et la page de titre sont des modèles que vous pouvez modifier dans [Paramètres → Exports](../account.md#exports).

**LaTeX.** Un ZIP avec un dossier portant le nom du livre. `main.tex` est une `book` qui se compile avec `pdflatex`, `xelatex` ou `lualatex`, et les images des parties se trouvent dans `images`. La page de titre est une `titlepage` construite à partir du [modèle de page de titre](../account.md#exports). Le titre de niveau le plus élevé de l'histoire devient un chapitre, le niveau suivant une section, et ainsi de suite ; ils ne sont pas numérotés (une histoire nomme elle-même ses chapitres), mais ils figurent dans une table des matières. L'accentuation, les citations, les listes, les blocs de code et les séparateurs de scène deviennent leur équivalent en LaTeX ; les notes de bas de page sont de véritables notes de bas de page ; les caractères qui ont un sens pour LaTeX sont écrits comme du texte ; et la langue du livre sélectionne la césure (`babel`) lorsqu'elle fait partie des plus courantes. Un livre contenant du japonais, du chinois ou du coréen nécessite `xelatex` ou `lualatex` (`pdflatex` s'arrête avec un message) : le fichier charge alors `xeCJK` ou `luatexja`, et avec `xelatex` vous définissez une police qui contient vos caractères (la ligne à modifier est indiquée dans le commentaire au-dessus, par exemple `\setCJKmainfont{Noto Serif CJK JP}`), sinon seule la police chinoise fournie avec `xeCJK` est utilisée. Téléversez le ZIP sur [Overleaf](https://www.overleaf.com) ou décompressez-le et exécutez `pdflatex main.tex` (deux fois, pour la table des matières). Le style d'impression ne s'applique pas : l'apparence est celle de la classe, à modifier dans le préambule de `main.tex`.

<a id="print-styles"></a>

## Styles d'impression

L'export utilise les styles de texte de [Paramètres → Styles](../account.md#styles) qui sont marqués **Pour les exports**. Les styles sont soit destinés aux livres (l'histoire et le lecteur), soit aux exports : le menu *Changer les styles du texte* et le lecteur n'affichent que le premier type, la boîte de dialogue d'export n'affiche que le second. Les quatre styles intégrés sont les apparences des formats ; **Dupliquer** l'un d'eux (ou un style de livre, en cochant **Pour les exports**) pour créer le vôtre, par exemple le style Livre pour le papier. Le style est calculé pour le papier : les couleurs qui dépendent du thème de l'application (le texte clair du thème sombre) deviennent les couleurs sombres pour une page blanche.

| Format | Ce que donne le style |
|---|---|
| **HTML** | La feuille de style complète, y compris le **CSS personnalisé**, les **lettrines**, la largeur du texte et l'arrière-plan ; la page de titre et la table des matières conservent l'apparence du format. |
| **EPUB** | La feuille de style sans le CSS personnalisé (les liseuses ne le gèrent pas toutes ; certaines imposent aussi leurs propres polices et tailles). |
| **PDF** | La famille de polices (serif, sans ou monospace parmi les polices intégrées, choisie dans la pile de polices), la taille, la hauteur de ligne, l'alignement, l'espace entre les paragraphes, le retrait de la première ligne, la **couleur de fond** de la page (la page entière, page de titre comprise), les couleurs du texte, de l'italique, du gras, des citations, des titres et du séparateur de scène, l'alignement et la taille des titres, ainsi que le symbole du séparateur de scène. |
| **Document Word** | Identique au PDF, avec la police indiquée dans le style (Word affiche sa propre police lorsque l'ordinateur ne la possède pas) au lieu des polices intégrées. L'arrière-plan est la couleur de la page : Word l'affiche à l'écran, mais ne l'imprime pas sauf si *Imprimer les couleurs et images d'arrière-plan* est activé dans ses options d'impression. |

Les lettrines, le CSS personnalisé (PDF, Word, EPUB), les petites capitales des titres et la largeur du texte ne sont pas produits dans les fichiers PDF et Word.

<a id="title-page-template"></a>

## Modèle de page de titre

La page de titre est écrite à partir d'un modèle, un pour chaque format. Modifiez-le dans [Paramètres → Exports](../account.md#exports) : le modèle est du Markdown avec `{{title}}` (le titre du livre dans la boîte de dialogue) et `{{author}}`, par exemple `## {{title}}` et une ligne `écrit par {{author}}`. Par défaut, le titre est un titre et l'auteur est en italique.

Les mêmes modèles (la page de titre, l'en-tête front matter, la déclaration de paternité) peuvent être modifiés **pour un seul livre ou pour un dossier** : dans l'onglet *Prompts* du livre et dans la fenêtre du dossier, le **Paramètres des extensions** comporte un panneau pour chaque format d'export, avec un champ pour chacun de ses modèles. La version propre au livre est utilisée en premier, sinon celle du dossier le plus proche au-dessus, sinon la vôtre dans *Paramètres → Exports*, sinon le modèle par défaut, si bien qu'une série peut avoir sa propre page de titre. Un champ vide est hérité, et indique d'où il vient.
<a id="table-of-contents"></a>

## Table des matières

Lorsque le texte contient des chapitres, le fichier reçoit une table des matières après la page de titre. Un chapitre est une partie de l'histoire qui contient un titre Markdown (une ligne commençant par `#`, par exemple `# Chapter 3: The Harbour`), la même règle que celle utilisée par le plugin [Chapter Marker](../extensions/chapter-marker.md) dans la barre latérale de l'histoire. Le premier titre d'une partie est son titre ; les parties sans titre appartiennent au chapitre qui les précède. Une histoire sans aucun titre n'a pas de table des matières.

![La page de titre et la table des matières d'un PDF exporté](../../images/export-result.png)

En PDF, Document Word et EPUB, chaque chapitre commence sur une nouvelle page. La page de titre et la table des matières occupent elles aussi leurs propres pages.

Les entrées sont des liens vers les chapitres :

| Format | |
|---|---|
| **HTML** | Liens vers les chapitres de la page. |
| **PDF** | Entrées cliquables avec la page du chapitre ; les chapitres figurent aussi dans le plan (signets) que votre lecteur PDF affiche à côté des pages. |
| **EPUB** | La table des matières du lecteur, qui est aussi une page du livre après la page de titre. |
| **Document Word** (`.docx`) | Entrées cliquables avec la page du chapitre. Word demande de mettre à jour les champs à l'ouverture du fichier ; répondez **Oui** pour remplir les numéros de page (LibreOffice les remplit automatiquement). Il s'agit d'une liste de liens et non d'un champ de table des matières Word. |
| **Markdown** | Pas de table des matières. Les titres de chapitres restent dans le texte, et la plupart des éditeurs Markdown construisent un plan à partir de ceux-ci. |
| **Texte brut** | Pas de table des matières. |

<a id="page-numbers"></a>

## Numéros de page

Les fichiers PDF et Word affichent le numéro de page dans le pied de page de chaque page, sauf sur la page de titre. Le numéro correspond à la position de la page dans le fichier : la page de titre est la page 1, et la première page qui suit est la page 2. Les autres formats n'ont pas de pages propres.

<a id="the-title-page"></a>

## La page de titre

Avec **Inclure une page de titre**, le fichier commence par le titre du livre et le nom de l'auteur, sur une page distincte. La page est générée à partir d'un modèle intégré (`DEFAULT_EXPORT_HEADER_TEMPLATE`) qui n'est pas encore modifiable dans l'application.

<a id="good-to-know"></a>

## Bon à savoir

- L'export est un instantané de la branche au moment où vous choisissez **Exporter**. Les parties générées ou modifiées ensuite ne figurent pas dans le fichier.
- Le fichier exporté n'est conservé que jusqu'à la fermeture de la boîte de dialogue de téléchargement. Exportez à nouveau si vous en avez besoin plus tard.
- Les résumés, les lorebooks, les instructions et le raisonnement du modèle ne sont pas exportés : seuls le texte et les images des parties le sont. Pour tout sauvegarder, faites une [sauvegarde](backups.md).
