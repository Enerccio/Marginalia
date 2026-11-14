---
label: Livre d'exemple
order: 80
---

# Livre d'exemple

*Unregistered* est un livre achevé, à ouvrir, lire et démonter : environ 100 parties (près de 99 000 mots) d'une même histoire, avec tout ce que les fonctionnalités de Marginalia laissent derrière elles. C'est une bonne façon de voir à quoi ressemble un long livre dans l'[éditeur d'histoire](../books/story-editor.md), les [résumés](../books/summaries.md), les [annotations](../books/annotations.md) et les [statistiques](../books/statistics.md), ou d'essayer les extensions sur un contenu déjà présent.

> Un guide de cave tchèque qui a appris le japonais dans des romans et une infirmière japonaise en vacances franchissent une arche murée dans une cave de Prague et se réveillent dans une vallée fluviale tenue par un empire parlant le vrakht.
> Aucun des deux ne connaît la langue, et un percepteur de l'impôt sur la vie a trouvé deux personnes qui ne figurent pas dans son registre.

<a id="what-is-in-it"></a>

## Ce qu'il contient

| | |
|---|---|
| **L'histoire** | 100 parties sur la ligne principale et une branche latérale de 3 parties, réparties en 32 chapitres (les titres figurent dans le texte). |
| **Résumés** | Un pour chaque chapitre, de longueur quelconque (le dernier chapitre de chaque tranche de 25 parties est laissé entier, car le modèle le lit tel quel), et les méta-résumés qui fusionnent les plus anciens, au format compact du [prompt de résumé](../books/summaries.md) par défaut. |
| **Annotations** | Environ 450 notes de relecture (cohérence, style, lore, point de vue, rythme) proposées par **Annoter avec l'IA** du livre lui-même, et suivies par la réécriture ; chaque partie a aussi ses versions (360 au total, voir [historique des versions](../books/version-history.md)). |
| **Lorebook** | 31 entrées : le monde, les lieux, les personnages et les langues. |
| **Prompts** | Chaque partie conserve le prompt à partir duquel elle a été écrite et le lore qui a été activé : **Afficher le prompt**, **Comparer le prompt** et **Utilisation du lore** dans le menu de la partie ([Prompts du livre](../books/prompts.md)). |
| **Knowledge Tracker** | Sept personnages suivis et ce que chacun sait et ignore, partie par partie ([Knowledge Tracker](../extensions/knowledge-tracker.md)). |
| **Speech traduit** | Environ 1 250 passages traduits : les paroles de la Dominion en **vrakht** et de la vallée en **aethel-vael** ([Conlang Translator](../extensions/conlang-translator.md)), ainsi que le **japonais** et le **tchèque** ([Real Language Translator](../extensions/real-language-translator.md)). |
| **Chapitres** | Le plan de l'histoire, établi par [Arc Outline](../extensions/arc-outline.md) à partir du texte. |
| **Une image** | Une illustration, dans la partie 31. |

Les données des quatre extensions se trouvent dans le livre, mais vous ne les voyez qu'avec les extensions chargées ; sans elles, elles sont conservées et ignorées. Le livre ne fixe ni modèle ni protocole : après l'importation, choisissez le vôtre dans les paramètres du livre.

<a id="download"></a>

## Téléchargement

Les fichiers sont joints à la version [example-book-1](https://github.com/Enerccio/Marginalia/releases/tag/example-book-1) :

| Fichier | |
|---|---|
| [`unregistered-book.zip`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-book.zip) (5,7 Mo) | Le livre : la sauvegarde avec son image. |
| [`unregistered-lorebook.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-lorebook.json) | Le lorebook du livre, séparément. Le fichier du livre le contient aussi ; ce fichier-ci ne sert qu'à consulter le lorebook sans le livre. |
| [`vrakht.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/vrakht.json) | La langue construite de la Dominion, pour le Conlang Translator. |
| [`aethel-vael.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/aethel-vael.json) | La langue construite de la vallée, pour le Conlang Translator. |

<a id="importing-it"></a>

## L'importer

1. **Les langues** (si vous avez le Conlang Translator) : dans **Paramètres → Paramètres des extensions → Conlang Translator**, cliquez sur **Importer** et choisissez `vrakht.json`, puis recommencez avec `aethel-vael.json`
   ([Conlang Translator](../extensions/conlang-translator.md#languages)). Faites-le avant le livre, pour que les réglages de langue du livre aient où pointer.
2. **Le livre :** dans l'onglet *Paramètres* de la fenêtre du livre, cliquez sur **Importer la sauvegarde comme nouveau livre**, saisissez un nom et envoyez `unregistered-book.zip`
   ([comment une sauvegarde est importée](../books/backups.md#importing-a-backup-as-a-new-book)). Lorsque Marginalia demande ce qu'il faut faire du lorebook, choisissez **Importer** : il se trouve dans le fichier.
3. Ouvrez le nouveau livre et, dans ses paramètres, choisissez votre modèle et votre protocole.

Les passages traduits sont stockés dans le livre, donc les paroles en vrakht, en aethel-vael, en japonais et en tchèque s'affichent telles qu'elles ont été traduites. Pour traduire davantage avec le Real Language Translator, créez-y les langues *Japanese* et *Czech* (le plugin les propose en préréglages), car le plugin n'exporte pas ses langues avec un livre.

<a id="where-it-comes-from-and-the-licence"></a>

## D'où il vient et sa licence

L'histoire, le lorebook et les deux langues sont originaux. Le plan, le lorebook, les langues et le découpage des parties ont été écrits par l'auteur de Marginalia ; le texte des parties a été **écrit par un modèle d'IA** (Claude Haiku) à travers la boucle de génération propre à Marginalia : générer, annoter, réécrire, suivre les connaissances, traduire, résumer. C'est une démonstration du logiciel, non un roman publié, et il présente les tics d'écriture d'un modèle.

- Le texte, le lorebook et les deux langues sont placés dans le domaine public sous [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/) : vous pouvez les utiliser librement.
- L'image est *Interior of a Cottage in Scheveningen* (1887) de Willem Adriaan Alexander Liernur, dans le domaine public, provenant de [Rawpixel](https://www.rawpixel.com/image/13744617/image-face-person-art).
