---
label: Prompts de livre
order: 70
---

# Prompts de livre

L'onglet **Prompts** d'un livre contient les textes à partir desquels Marginalia construit le prompt : deux modèles, les paramètres narratifs, ainsi que les prompts de résumé et de réécriture. Ils déterminent la voix du livre bien plus que les paramètres du modèle.

![L'onglet Prompts](../../images/book-prompts.png)

<a id="defaults-and-book-values"></a>

## Valeurs par défaut et valeurs du livre

Chaque champ est facultatif. Un champ vide hérite sa valeur : du [dossier](folders.md) le plus proche au-dessus du livre qui la définit, sinon de la valeur par défaut de **Paramètres → Modèles** (voir [Compte & paramètres](../account.md)), et si celle-ci est vide aussi, de la valeur intégrée, dans la [langue des prompts](../account.md#prompt-language) de vos paramètres. La valeur héritée est affichée en gris dans le champ vide.

Réglez donc ce que tous vos livres ont en commun dans *Paramètres*, ce qu'une série partage dans son dossier, et seulement ce qui est propre à un livre dans son onglet *Prompts*. Les modifications sont enregistrées au fur et à mesure de la saisie.

<a id="inherited-settings"></a>

### Paramètres hérités

Sous un champ vide, une ligne indique d'où vient sa valeur, afin de comprendre pourquoi le livre écrit de cette façon :

- *Hérité du dossier « Science-fiction »* - un [dossier](folders.md#where-the-settings-of-a-book-come-from) définit la valeur (le plus proche au-dessus du livre est nommé),
- *Hérité de vos paramètres* - la valeur de *Paramètres → Modèles*,
- *Valeur intégrée par défaut* - rien ne la définit.

![L'onglet Prompts d'un livre dans un dossier](../../images/book-prompts-inherited.png)

Les mêmes champs figurent dans les paramètres d'un dossier, avec les mêmes lignes. Un texte vide est considéré comme non défini. Comme la recherche se fait au moment de la construction du prompt, déplacer le livre dans un autre dossier, ou changer de dossier, modifie la prochaine partie que vous générez.

<a id="settings-of-extensions"></a>

### Paramètres des extensions

Une [extension](../extensions/index.md) peut ajouter des paramètres dont les livres et les dossiers héritent de la même façon (par exemple le média que traite une discussion sur le livre). Ils se trouvent dans une section **Paramètres des extensions** sous les prompts, dans l'onglet *Prompts* d'un livre et dans les paramètres d'un dossier, avec un **panneau repliable par extension** (tous fermés, sauf le premier qui contient une valeur définie, afin qu'une valeur que vous avez saisie ne soit pas cachée). Ils fonctionnent comme les champs ci-dessus : un champ vide hérite des dossiers au-dessus du livre, puis des paramètres propres à l'extension dans *Paramètres → Paramètres des extensions*, puis de sa valeur par défaut, et la ligne sous le champ indique laquelle. Les modèles des [formats d'export](export.md#title-page-template) (page de titre, pages liminaires, mention d'auteur) se trouvent dans la même section, avec un panneau par format. Une valeur que vous avez définie pour une extension reste attachée au livre ou au dossier lorsque l'extension n'est pas chargée.

Chaque champ dispose d'une fenêtre d'aide avec **Insérer le modèle par défaut**, qui copie le modèle par défaut dans le champ comme point de départ pour votre propre version. Les champs de modèle listent aussi les **Variables de modèle disponibles** et les **Macros disponibles**.

![La fenêtre d'aide d'un champ de modèle](../../images/template-hints.png)

<a id="the-fields"></a>

## Les champs

| Champ | Utilisé dans | |
|---|---|---|
| **Modèle principal** | message système | Le cadre du prompt : le rôle du modèle, et l'endroit où vont le lore, les paramètres narratifs et les résumés. Un modèle. |
| **Point de vue (POV)** | modèle principal | La voix narrative, par exemple *Première personne*, *Troisième personne limitée*. Texte brut. |
| **Temps** | modèle principal | Par exemple *Passé*, *Présent*. Texte brut. |
| **Style** | modèle principal | Consignes de style et de ton pour la prose. Texte brut. |
| **Prompt utilisateur par défaut** | dernier message | La demande de la partie suivante, construite à partir des [instructions du tour](story-editor.md#writing-the-next-part). Un modèle. |
| **Prompt de résumé par défaut** | résumés | La demande d'un [résumé](summaries.md). Un modèle. |
| **Prompt de méta-résumé par défaut** | méta-résumés | La demande d'un [méta-résumé](summaries.md#meta-summaries), qui fusionne des résumés. Un modèle. |
| **Prompt système de réécriture** | réécritures | Le message système d'une [réécriture](story-editor.md#redrafting-a-part) : les directives, la narration et les résumés. Un modèle. |
| **Prompt de réécriture** | réécritures | Le dernier message d'une réécriture, la demande elle-même : le lore que les livres de lore donnent maintenant à la partie (ainsi, une modification d'une entrée atteint la réécriture ; le lore avec lequel la partie a été écrite seulement lorsque le livre n'a pas de livre de lore), l'histoire après la partie, la partie et les instructions. Un modèle. |
| **Prompt de réécriture plus courte**, **Prompt de réécriture plus longue** | réécritures | La phrase ajoutée après vos instructions lorsque la fenêtre de [réécriture](story-editor.md#redrafting-a-part) est réglée sur **Plus court** ou **Plus long**. Texte brut, pas un modèle. Vide correspond au texte intégré. |
| **Prompt système d'annotation** | annotation | Le message système d'une demande à un modèle pour [annoter](annotations.md#letting-a-model-annotate) une partie. Un modèle. |
| **Prompt d'annotation** | annotation | Le dernier message de la demande d'annotation. Un modèle. |
| **Ce qu'il faut chercher** (*Annotate Instructions*) | annotation | Ce que le modèle recherche lorsqu'il annote (*signaler les erreurs de continuité*, *dialogues faibles*), le texte de `{%{{{annotateInstructions}}}%}` du prompt d'annotation. Texte brut. Définissez-le ici, dans un dossier ou dans *Paramètres → Modèles*, et la fenêtre *Annoter avec l'IA* s'ouvre avec ce texte. |
| **Prompt de rédaction des instructions** | instructions de rédaction | La demande faite à un modèle de [rédiger les instructions](story-editor.md#drafting-the-instructions) du prochain tour à votre place. Un modèle. |

*Point de vue*, *Temps* et *Style* sont insérés tels quels dans le modèle principal ; les macros n'y fonctionnent pas.

Un modèle contenant une erreur de syntaxe n'est pas enregistré ; un avertissement affiche l'erreur.

<a id="templates"></a>

## Modèles

Les modèles sont des textes avec des emplacements réservés entre doubles accolades. Chaque modèle dispose de ses propres variables :

| Modèle | Variables |
|---|---|
| Modèle principal | `{%{{{backgroundLore}}}%}` (entrées de lore activées), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, `{%{{{summaries}}}%}`, `{%{{{annotations}}}%}` (les notes que vous avez données au modèle) |
| Prompt utilisateur | `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}` |
| Prompt de résumé | `{%{{{backgroundLore}}}%}` (le lore que les livres de lore donnent maintenant à la partie), `{%{{{text}}}%}` (les parties à résumer) |
| Prompt de méta-résumé | `{%{{{backgroundLore}}}%}` (comme ci-dessus), `{%{{{summaryBlocks}}}%}` (les résumés à fusionner) |
| Prompt système de réécriture, Prompt de réécriture | `{%{{{text}}}%}` (la partie), `{%{{{redraftInstructions}}}%}` (ce que vous voulez modifier), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{followingStory}}}%}`, `{%{{{backgroundLore}}}%}`, `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, et le tour à partir duquel la partie a été écrite : `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}`, ainsi que `{%{{{annotations}}}%}` (vos notes ouvertes sur la partie, vides sauf si vous les demandez dans la fenêtre de réécriture), et, pour la [réécriture d'une sélection](story-editor.md#redrafting-only-a-passage), `{%{{{selection}}}%}` (le texte sélectionné ; vide lorsque la partie entière est réécrite), `{%{{{selectionBefore}}}%}` et `{%{{{selectionAfter}}}%}` (le texte de la partie autour de la sélection) |
| Prompt de rédaction des instructions | `{%{{{impersonateInstructions}}}%}` (vos indications), `{%{{{outline}}}%}` (le plan de l'histoire fourni par un appelant, par exemple le chapitre courant de l'Arc Outline), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{backgroundLore}}}%}` (le lore de la dernière partie), `{%{{{instructionExamples}}}%}` (vos instructions des trois dernières parties, de la plus ancienne à la plus récente), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, et le tour auquel les instructions s'appliquent : `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}` |
| Prompt système d'annotation, Prompt d'annotation | Les variables des prompts de réécriture, avec `{%{{{annotateInstructions}}}%}` (ce que vous voulez faire examiner) à la place de `{%{{{redraftInstructions}}}%}`, `{%{{{annotations}}}%}` (les notes que la partie a déjà), `{%{{{facts}}}%}` (ce que les extensions savent de l'histoire à la partie, par exemple ce que connaissent les personnages ; vide sans une telle extension) et `{%{{{outputFormat}}}%}` (la manière dont le modèle est invité à répondre) |

Une section n'affiche son contenu que lorsque la variable n'est pas vide, de sorte que le prompt ne contient pas de titres vides :

{%{
```handlebars
{{#sceneSetting}}Lieu et moment actuels : {{.}}{{/sceneSetting}}
```
}%}

`{%{{{.}}}%}` à l'intérieur d'une section est la valeur de la variable. Avec un *Cadre de la scène* vide, toute la ligne est omise.

Tous les modèles peuvent aussi utiliser les **macros** compatibles SillyTavern, comme `{%{{{user}}}%}` (le personnage du point de vue), `{%{{{description}}}%}` (la description du livre), `{%{{{random::a::b}}}%}`, `{%{{{getvar::name}}}%}` et `{%{{{setvar::name::value}}}%}`. Voir [Modèles & macros](../templates/index.md) pour la syntaxe complète et la référence des macros.

<a id="master-template"></a>

## Modèle principal

Le modèle principal devient le **message système**, le premier message du prompt. La valeur par défaut intégrée comporte ces blocs :

1. **Rôle système** - le modèle est un auteur qui poursuit l'histoire.
2. **Contexte du monde et lore** - les entrées activées du [livre de lore](../lorebooks/index.md) (`backgroundLore`).
3. **Style d'écriture et mécanique** - point de vue, temps, style et règles générales de la prose.
4. **Résumé de l'histoire jusqu'ici** - les [résumés](summaries.md).
5. **Notes de l'auteur sur l'histoire** - seulement lorsque vous avez donné des [notes au modèle](annotations.md#giving-a-note-to-the-model).

Si le [fournisseur d'inférence](../inference-providers.md#general-settings) a un prompt de jailbreak activé, celui-ci est placé avant le modèle principal.

<a id="user-prompt"></a>

## Prompt utilisateur

Le prompt utilisateur est le **dernier message** du prompt : la demande de la partie à écrire. Il est rempli à partir des *Instructions du nouveau tour* (ou des *Détails du tour* pour *Régénérer* et *Variante*). La valeur intégrée demande la section suivante avec le personnage du point de vue, le lieu, les personnages présents et les instructions de l'intrigue, suivis de règles : pas de nouveaux personnages sauf demande, suivre les instructions, garder la continuité, et ne produire que le texte de l'histoire.

Les entrées de lore dont le mode d'insertion est *Avant le prompt utilisateur* sont placées juste avant celui-ci ; voir [Entrées & activation](../lorebooks/entries.md).
<a id="redraft-prompt"></a>

## Prompt de réécriture

La [réécriture](story-editor.md#redrafting-a-part) d'une partie est une seule requête composée de deux prompts : le **Prompt système de réécriture** est le message système, l'histoire qui précède la partie le suit sous forme de messages antérieurs de la conversation (les parties comme réponses du modèle, comme une génération les envoie), et le **Prompt de réécriture** est le dernier message de l'utilisateur. Le prompt par défaut intégré demande au modèle d'appliquer vos instructions, de conserver ce que vous n'avez pas demandé de modifier, de rester cohérent avec l'histoire avant et après la partie, de garder la voix, et de répondre uniquement par la partie réécrite. Les deux prompts ont les mêmes variables ; `{%{{{previousStory}}}%}` n'est qu'une note indiquant que l'histoire suit sous forme de messages. Gardez dans le prompt système ce qui ne change pas d'une réécriture à l'autre, et dans le dernier prompt ce qui change (le lore, qui n'est pas le même pour chaque partie, l'histoire après la partie, la partie, les instructions) : un serveur qui mémorise le prompt qu'il a lu peut alors réutiliser la plus grande partie. Votre propre version du dernier prompt doit conserver les variables dont elle a besoin : sans `{%{{{text}}}%}`, le modèle ne voit pas la partie. Chaque prompt suit la règle de tous les prompts : le champ du livre, sinon celui de *Paramètres → Modèles*, sinon celui par défaut intégré.

Lorsque vous ne réécrivez qu'un [passage sélectionné](story-editor.md#redrafting-only-a-passage), le même prompt est utilisé avec `{%{{{selection}}}%}` renseigné : le prompt par défaut intégré affiche alors la partie avec le passage entre `[[PASSAGE TO REDRAFT]]` et `[[/PASSAGE TO REDRAFT]]`, et demande au modèle de répondre uniquement par le texte qui remplace le passage. Un prompt de votre cru qui ne connaît pas `{%{{{selection}}}%}` fait répondre le modèle avec la partie entière, et la réponse complète remplace alors la sélection ; ajoutez un bloc `{%{{#selection}}%}...{%{{{/selection}}}%}` (et `{%{{{^selection}}}%}` pour l'autre cas) pour le gérer.

<a id="redraft-shorter-and-longer"></a>

## Réécrire plus court et plus long

Le choix **Longueur** de la fenêtre de [réécriture](story-editor.md#redrafting-a-part) (**Même longueur**, **Plus court**, **Plus long**) ne touche pas aux prompts de réécriture : pour **Plus court**, le texte de **Prompt de réécriture plus courte** (pour **Plus long**, celui de **Prompt de réécriture plus longue**) est ajouté après ce que vous avez écrit dans *Instructions pour la réécriture*, à la place de `{%{{{redraftInstructions}}}%}`, donc cela fonctionne aussi avec un prompt de réécriture de votre cru. Avec des instructions vides, c'est la seule consigne. Les deux textes suivent la règle de tous les prompts : le champ du livre, sinon le dossier le plus proche au-dessus qui en définit un, sinon celui de *Paramètres → Modèles*, sinon le texte intégré, qui demande environ un tiers à une moitié de moins (plus : encore la moitié, à deux fois plus long) sans changer ce qui se passe.

<a id="annotate-prompt"></a>

## Prompt d'annotation

Lorsqu'un modèle [annote une partie](annotations.md#letting-a-model-annotate), il reçoit le même type de requête qu'une réécriture : le **Prompt système d'annotation** comme message système, l'histoire qui précède la partie comme messages antérieurs, et le **Prompt d'annotation** comme dernier message. Le prompt par défaut intégré fait du modèle un éditeur : il repère les passages qui posent un problème au sens de vos instructions, cite chacun mot pour mot, et rédige pour vous une courte note, sans éloge ni remplissage et sans répéter les notes que la partie possède déjà. Lorsqu'une extension fournit des [faits sur l'histoire](annotations.md#letting-a-model-annotate) (le Knowledge Tracker : ce que savent les personnages), le prompt par défaut les présente sous *Faits établis* et l'éditeur signale ce qui les contredit. Le prompt contient la variable `{%{{{outputFormat}}}%}`, à l'endroit où est décrite la manière de répondre (JSON, blocs de texte ou relecture libre, selon le format de réponse que vous choisissez) ; conservez-la dans un prompt de votre cru, sinon le modèle n'est pas informé de la manière de répondre. Les deux prompts d'annotation suivent la règle de tous les prompts : le champ du livre, sinon celui de *Paramètres → Modèles*, sinon celui par défaut intégré.

**Ce qu'il faut chercher** (*Annotate Instructions*) suit la même règle, ce qui vous permet de le régler une seule fois : dans *Paramètres → Modèles* pour tous vos livres, dans un dossier pour les livres qu'il contient, ou dans l'onglet *Prompts* pour un seul livre. La fenêtre *Annoter avec l'IA* affiche ce qui s'applique sous forme de texte gris dans son champ et indique d'où il vient ; laissez le champ vide pour l'utiliser, saisissez quelque chose pour le remplacer pour cette exécution, ou cliquez sur l'icône d'information du champ puis sur **Fill default** pour copier ce qui s'applique dans le champ et le modifier pour cette exécution. Si rien n'est défini nulle part, c'est la tâche par défaut du prompt. Il en va de même pour l'annotation automatique et la commande `/generate-annotation`.

<a id="impersonate-prompt"></a>

## Prompt de rédaction des instructions

Lorsqu'un modèle [rédige les instructions](story-editor.md#drafting-the-instructions) du prochain tour, il reçoit une seule requête, envoyée comme unique message, comme la réécriture. Le prompt par défaut intégré demande au modèle d'écrire ce que vous taperiez dans *Instructions* : une spécification de l'intrigue de la prochaine partie (ce qui se passe, qui fait quoi, où la partie s'arrête) qui développe vos indications et suit le plan, poursuit l'histoire là où elle s'est arrêtée, est écrite comme vos propres instructions antérieures, et n'est pas la prose elle-même. Sans indication, il poursuit l'histoire là où elle mène. Vos indications sont placées par le prompt à l'endroit de `{%{{{impersonateInstructions}}}%}` ; conservez-la dans un prompt de votre cru. Le prompt de rédaction des instructions suit la règle de tous les prompts : le champ du livre, sinon le **Prompt de rédaction des instructions** de *Paramètres → Modèles*, sinon celui par défaut intégré.

<a id="how-the-prompt-is-put-together"></a>

## Comment le prompt est assemblé

```
system     jailbreak (si activé) + modèle maître
user       [ Generate story. ]
assistant  partie n° 1
user       [ Generate more story. ]
assistant  partie n° 2
...        (autant de parties antérieures que possible)
user       "Before User Prompt" du lore + prompt utilisateur
```

Les parties antérieures sont envoyées comme réponses propres du modèle, afin qu'il les poursuive dans la même voix. Lorsque l'histoire dépasse la [limite de contexte](../protocols.md#limits), les parties les plus anciennes sont laissées de côté en premier ; les [résumés](summaries.md) les conservent dans l'histoire sous forme condensée.

**Afficher le prompt** dans le menu de n'importe quelle partie affiche le prompt exact à partir duquel cette partie a été écrite. **Comparer le prompt** le place à côté du prompt d'une partie antérieure et indique où ils diffèrent pour la première fois, c'est-à-dire où s'arrête le [cache de prompts](../prompt-cache.md#compare-prompt) d'un serveur.

<a id="tips"></a>

## Conseils

- Placez les règles qui valent pour chaque partie (pas de nouveaux personnages, longueur, mise en forme) dans le prompt utilisateur : étant le dernier message, il a l'effet le plus fort.
- Placez les faits durables sur le monde et les personnages dans le [lorebook](../lorebooks/index.md), et non dans *Style*.
- Demandez une longueur dans les instructions (« environ 800 mots ») plutôt que de vous fier à la limite de réponse, qui coupe le texte.
- Après avoir modifié un modèle, générez une partie et vérifiez-la avec **Afficher le prompt**.
