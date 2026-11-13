---
label: Import et export
order: 80
---

# Import et export

<a id="exporting"></a>

## Exporter

Sélectionnez le lorebook dans l'éditeur et cliquez sur le bouton **export** (icône de fichier, *Exporter le lorebook*). Le navigateur télécharge `<nom du lorebook>.json`, qui contient :

- le lorebook et **tous ses sous-lorebooks** (à chaque niveau), avec leurs noms, description, état activé et étiquettes ;
- chaque entrée avec son contenu, sa note, son ordre, son filtre, son mode d'insertion, ses étiquettes et ses étiquettes négatives.

Utilisez-le pour garder une copie, partager un univers avec d'autres utilisateurs ou le transférer vers une autre installation. Les données que les [extensions](../extensions/index.md) conservent dans un lorebook ou une entrée figurent aussi dans le fichier et sont restaurées lors de l'import. Les révisions d'entrées de l'extension [Lorebook VCS](../extensions/lorebook-vcs.md) appartiennent aux anciennes entrées et n'apparaissent pas sur les entrées créées par l'import ; l'extension dispose de son propre export pour elles.

<a id="exporting-for-sillytavern"></a>

### Exporter pour SillyTavern

Le bouton **export** ouvre un menu avec deux éléments : **Exporter le lorebook** (le fichier ci-dessus) et **Exporter vers SillyTavern**, qui télécharge le lorebook sous forme de fichier world info de SillyTavern, importable dans SillyTavern. C'est l'inverse de l'[import](#importing-sillytavern-world-info) :

| Marginalia | SillyTavern |
|---|---|
| *Nom de l'entrée* | Titre / mémo (`comment`) |
| *Contenu*, *Ordre*, *Activé* | Contenu, ordre, désactivé |
| Aucun filtre | Entrée constante (🔵) |
| Filtre *Texte* | Une clé primaire |
| *Mots-clés (n'importe lequel)* | Une clé primaire pour chaque mot-clé |
| *Mots-clés (tous)* | Le premier mot-clé est la clé primaire, les autres sont des clés secondaires avec la logique *AND ALL* |
| *Expression régulière* | Une clé écrite `/motif/indicateurs` |
| *Dans le bloc de lore* | Position *avant les définitions de personnages* |
| *Avant le prompt utilisateur* | Position *à la profondeur* 1 |
| Étiquettes | Filtre de personnage (par nom) ; étiquettes négatives seulement si l'entrée n'a pas d'étiquettes (*exclure*) |

Les **sous-lorebooks sont fusionnés** : le fichier contient d'abord les entrées du lorebook, puis celles de ses sous-lorebooks, car SillyTavern n'a pas de lorebooks à l'intérieur d'autres lorebooks. Ce qui n'a pas d'équivalent dans SillyTavern est omis : la *Note* d'une entrée, les étiquettes négatives combinées à des étiquettes, les données des extensions. SillyTavern recherche les clés dans le chat récent ; pour l'autre sens, voir [ce qui fonctionne différemment](#what-works-differently).

!!! warning
Une expression régulière est écrite avec la syntaxe Java. SillyTavern utilise JavaScript : les expressions simples fonctionnent, mais une construction propre à Java (quantificateurs possessifs, classes `\p{...}`) ne correspond pas là-bas.
!!!

<a id="importing-a-marginalia-lorebook"></a>

## Importer un lorebook Marginalia

1. Cliquez sur le bouton **import** (icône de fichier) et choisissez **Importer un lorebook Marginalia**.
2. Envoyez un fichier exporté depuis Marginalia.

L'import crée toujours des lorebooks **nouveaux** — le lorebook et ses sous-lorebooks, avec la même structure — même si vous avez déjà des lorebooks portant les mêmes noms. Le lorebook importé est sélectionné dans l'éditeur. Les étiquettes sont associées à vos étiquettes existantes par leur nom, ou créées.

Les fichiers des anciennes versions de Marginalia (un seul lorebook sans sous-lorebooks) peuvent aussi être importés.

!!!
Les lorebooks voyagent aussi avec les [sauvegardes de livres](../books/backups.md). La restauration ou la duplication d'une sauvegarde peut relier des lorebooks que vous possédez déjà au lieu de créer des copies ; voir [Restaurer une sauvegarde](../books/backups.md#restoring-a-backup).
!!!

<a id="importing-a-lorebook-another-user-shares"></a>

## Importer un lorebook partagé par un autre utilisateur

**Importer d'un autre utilisateur** dans le menu d'import liste les lorebooks que d'autres utilisateurs [partagent](lorebooks.md#sharing-a-lorebook) avec leur nom, leur auteur et leur description ; ouvrez une ligne pour lire les entrées, cliquez sur **Importer** pour obtenir une copie privée. Seuls les sous-lorebooks que l'auteur partage sont inclus. Contrairement à un import de fichier, rien n'a besoin d'être exporté et envoyé : l'auteur coche simplement **Partager avec d'autres utilisateurs**.

<a id="importing-an-archive-of-text-files"></a>

## Importer une archive de fichiers texte

1. Cliquez sur le bouton **import** et choisissez **Importer depuis une archive**.
2. Envoyez un fichier `.zip` contenant des fichiers `.md` (Markdown).
3. Vérifiez les **noms des entrées** dans le tableau et modifiez-les si vous le souhaitez, puis cliquez sur **Importer**.

![La fenêtre d'envoi de l'archive](../../images/archive-lorebook-upload.png)

![Les noms des entrées avant l'import](../../images/archive-entry-names.png)

Marginalia crée un nouveau lorebook nommé d'après le fichier (sans `.zip`), avec **une entrée par fichier `.md`**. Les fichiers sont pris dans l'ordre de leurs noms, les nombres à l'intérieur des noms étant comparés comme des nombres (`2.md` avant `10.md`), y compris dans les dossiers de l'archive ; les entrées reçoivent l'*Ordre* 1, 2, 3... dans cette séquence. Le fichier entier constitue le *Contenu*. Les autres fichiers, les dossiers, les fichiers cachés (`.nom`) et le dossier `__MACOSX` des archives créées sur Mac sont ignorés.

Les **noms des entrées** sont tirés, dans cet ordre :

1. d'un fichier `names.txt` à la racine de l'archive : un nom par ligne, la première ligne pour le premier fichier `.md` dans l'ordre ci-dessus, et ainsi de suite (une ligne vide laisse ce fichier à la règle suivante ; les lignes en trop sont ignorées). Utilisez-le pour nommer beaucoup de fichiers d'un coup, par exemple depuis un script ;
2. le premier titre du fichier (`# Chapitre 1` donne *Chapitre 1*) ;
3. le nom du fichier sans `.md`.

Le tableau de la boîte de dialogue affiche le résultat, et vous pouvez remplacer n'importe quel nom.

L'import affiche ensuite sa progression. **Arrêter** met fin à l'import après l'entrée en cours d'enregistrement : le lorebook conserve les entrées déjà importées (l'import n'est pas annulé), et un message indique combien de fichiers ont été importés.

Les entrées sont importées **désactivées** : elles n'ont pas de filtre, donc activées, elles seraient envoyées avec chaque requête, ce qui n'est guère souhaitable pour un livre entier de chapitres. Activez celles dont vous avez besoin, avec un filtre ou des [étiquettes](entries.md#tags) — **Modification en masse** est le moyen rapide de le faire pour de nombreuses entrées.

Pour placer les mêmes fichiers dans une histoire sous forme de parties, voir [Importer dans le livre](../books/story-editor.md#importing-into-the-book).

<a id="importing-sillytavern-world-info"></a>

## Importer un fichier world info de SillyTavern

1. Cliquez sur le bouton **import** et choisissez **Importer depuis SillyTavern**.
2. Envoyez le fichier world info (`.json`) exporté depuis SillyTavern.

Marginalia crée un nouveau lorebook nommé d'après le world info (ou d'après le fichier) et convertit chaque entrée :

| SillyTavern | Marginalia |
|---|---|
| Titre / mémo (`comment`) | *Nom de l'entrée* (la première clé s'il n'y a pas de titre) |
| Contenu | *Contenu* — les macros comme `{%{{{user}}}%}` et `{%{{{char}}}%}` continuent de fonctionner, voir [macros](../templates/macros.md) |
| Ordre | *Ordre* |
| Désactivé | *Activé* décoché |
| Constante (🔵), ou aucune clé | toujours active (aucun filtre) |
| Clés primaires, clés secondaires optionnelles avec leur logique | un filtre, voir ci-dessous |
| Sensible à la casse, mots entiers | intégré au filtre |
| Position *avant / après les définitions de personnages*, *exemples de messages* | *Dans le bloc de lore* |
| Position *note de l'auteur* (haut / bas), *à la profondeur* | *Avant le prompt utilisateur* |
| Filtre de personnage | étiquettes (ou étiquettes négatives pour *exclure*), par nom de personnage et étiquette |

**Les clés deviennent des filtres.** Une seule clé simple devient un filtre *Texte* (`dragon`). Tout le reste — plusieurs clés, clés secondaires (*AND ANY*, *AND ALL*, *NOT ANY*, *NOT ALL*), clés écrites `/regex/`, correspondance sensible à la casse ou aux mots entiers — devient un seul filtre *Expression régulière* qui fait la même chose. Par exemple, les clés `dragon`, `wyrm` avec la clé secondaire `fire` (*AND ANY*) deviennent :

```
(?s)^(?=.*(?:\Qdragon\E|\Qwyrm\E))(?=.*(?:\Qfire\E))
```

Vous n'avez pas besoin de lire ces expressions ; modifiez-les seulement si vous connaissez les expressions régulières.

<a id="what-works-differently"></a>

### Ce qui fonctionne différemment

Les réglages de SillyTavern sans équivalent dans Marginalia sont listés dans la **Note** de l'entrée, par exemple *Importé depuis SillyTavern, non pris en charge : probabilité 50 %, groupe d'inclusion « météo ».* Il s'agit de :

- la probabilité, les groupes d'inclusion, le sticky, le cooldown et le délai — l'entrée est simplement active dès qu'elle correspond ;
- l'insertion *à la profondeur* — l'entrée est placée avant le prompt utilisateur à la place.

La différence la plus importante concerne **ce contre quoi les clés sont comparées**. SillyTavern parcourt le chat récent. Marginalia compare les clés à la [requête de la partie en cours d'écriture](entries.md#filters) — sa scène, ses personnages et ses instructions — et une entrée importée part exactement de cela (son **Contexte d'activation** est *Instructions*). Ainsi, une entrée que SillyTavern activait parce qu'un nom apparaissait dans les derniers messages n'est activée dans Marginalia que si vous mentionnez le nom dans le tour. Pour obtenir le comportement de SillyTavern, réglez le **Contexte d'activation** de l'entrée sur *Instructions et parties de l'histoire* et **Parties** sur le nombre des parties les plus récentes à examiner, voir [Examiner l'histoire](entries.md#looking-into-the-story). L'activation récursive (des entrées qui en activent d'autres) n'est pas non plus réglée par l'import : activez-la avec la **profondeur de scan** du lorebook, voir [Entrées apportées par d'autres entrées](entries.md#entries-brought-in-by-other-entries).

Après l'import, parcourez les entrées et décidez lesquelles doivent être toujours actives, lesquelles gardent leurs filtres, et lesquelles sont mieux gérées avec des [étiquettes](entries.md#tags).
