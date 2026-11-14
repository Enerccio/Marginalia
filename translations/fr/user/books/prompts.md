---
label: Prompts du livre
order: 70
---

# Prompts du livre

L'onglet **Prompts** d'un livre contient les textes à partir desquels Marginalia construit le prompt : deux modèles, les paramètres narratifs et les prompts de résumé et de réécriture. Ils déterminent la voix du livre bien plus que les paramètres du modèle.

![L'onglet Prompts](../../images/book-prompts.png)

<a id="defaults-and-book-values"></a>

## Valeurs par défaut et valeurs du livre

Chaque champ est facultatif. Un champ vide est hérité : du [dossier](folders.md) le plus proche au-dessus du livre qui le définit, sinon de la valeur par défaut dans **Paramètres → Templates** (voir [Compte et paramètres](../account.md)), et si celle-ci est vide aussi, de la valeur intégrée par défaut, dans la [langue des prompts](../account.md#prompt-language) de vos paramètres. La valeur héritée est affichée en gris dans le champ vide.

Définissez donc ce que tous vos livres ont en commun dans *Paramètres*, ce qu'une série partage dans son dossier, et seulement ce qui est propre à un livre dans son onglet *Prompts*. Les modifications sont enregistrées au fur et à mesure de la saisie.

<a id="inherited-settings"></a>

### Paramètres hérités

Sous un champ vide, une ligne indique d'où vient sa valeur, afin de comprendre pourquoi le livre écrit comme il le fait :

- *Hérité du dossier « Sci-fi »* : un [dossier](folders.md#where-the-settings-of-a-book-come-from) définit la valeur (on indique le dossier le plus proche au-dessus du livre),
- *Hérité de vos paramètres* : la valeur de *Paramètres → Templates*,
- *Valeur intégrée par défaut* : rien ne définit la valeur.

![L'onglet Prompts d'un livre placé dans un dossier](../../images/book-prompts-inherited.png)

Les mêmes champs figurent dans les paramètres d'un dossier, avec les mêmes lignes. Un texte vide est considéré comme non défini. Comme la recherche est effectuée au moment où le prompt est construit, déplacer le livre vers un autre dossier, ou modifier le dossier, change la prochaine partie que vous générez.

<a id="settings-of-extensions"></a>

### Paramètres des extensions

Une [extension](../extensions/index.md) peut ajouter des paramètres que les livres et les dossiers héritent de la même façon (par exemple le support que le chat sur le livre considère comme celui du récit). Ils se trouvent dans une section **Paramètres des extensions**, sous les prompts, dans l'onglet *Prompts* d'un livre et dans les paramètres d'un dossier, avec un **panneau repliable par extension** (tous fermés, sauf le premier qui contient une valeur définie, afin qu'une valeur que vous avez saisie ne soit pas masquée). Ils fonctionnent comme les champs ci-dessus : un champ vide hérite des dossiers situés au-dessus du livre, puis des paramètres propres à l'extension dans *Paramètres → Paramètres des extensions*, puis de sa valeur par défaut, et la ligne sous le champ indique laquelle. Les modèles des [formats d'export](export.md#title-page-template) (page de titre, pages liminaires, mention de l'auteur) se trouvent dans la même section, avec un panneau par format. Une valeur que vous définissez pour une extension reste attachée au livre ou au dossier lorsque l'extension n'est pas chargée.

Chaque champ possède une info-bulle d'aide avec **Insérer le modèle par défaut**, qui copie la valeur par défaut dans le champ comme point de départ de votre propre version. Les champs de modèle listent aussi les **Variables de modèle disponibles** et les **Macros disponibles**.

![L'info-bulle d'aide d'un champ de modèle](../../images/template-hints.png)

<a id="the-fields"></a>

## Les champs

| Champ | Utilisé dans | |
|---|---|---|
| **Modèle principal** | message système | Le cadre du prompt : le rôle du modèle, et l'endroit où s'insèrent le lore, les paramètres narratifs et les résumés. Un modèle. |
| **Point de vue (POV)** | modèle principal | La voix narrative, par exemple *First person*, *Third-Person Limited*. Texte brut. |
| **Temps** | modèle principal | Par exemple *Past Tense*, *Present Tense*. Texte brut. |
| **Style** | modèle principal | Consignes de style et de ton pour la prose. Texte brut. |
| **Prompt utilisateur par défaut** | dernier message | La demande de la partie suivante, construite à partir des [instructions du tour](story-editor.md#writing-the-next-part). Un modèle. |
| **Prompt de résumé par défaut** | résumés | La demande d'un [résumé](summaries.md). Un modèle. |
| **Prompt de méta-résumé par défaut** | méta-résumés | La demande d'un [méta-résumé](summaries.md#meta-summaries), qui fusionne des résumés. Un modèle. |
| **Prompt système de réécriture** | réécritures | Le message système d'une [réécriture](story-editor.md#redrafting-a-part) : les directives, la narration et les résumés. Un modèle. |
| **Prompt de réécriture** | réécritures | Le dernier message d'une réécriture, la demande elle-même : le lore que les lorebooks donnent maintenant à la partie (ainsi, une modification d'entrée atteint la réécriture ; le lore avec lequel la partie a été écrite seulement lorsque le livre n'a pas de lorebook), l'histoire après la partie, la partie et les instructions. Un modèle. |
| **Prompt de réécriture plus courte**, **Prompt de réécriture plus longue** | réécritures | La phrase ajoutée après vos instructions lorsque la fenêtre de [réécriture](story-editor.md#redrafting-a-part) est réglée sur **Plus court** ou **Plus long**. Texte brut, pas un modèle. Vide, c'est le texte intégré qui est utilisé. |
| **Prompt système d'annotation** | annotation | Le message système d'une demande adressée à un modèle pour [annoter](annotations.md#letting-a-model-annotate) une partie. Un modèle. |
| **Prompt d'annotation** | annotation | Le dernier message de la demande d'annotation. Un modèle. |
| **Annotate Instructions** (*Ce qu'il faut chercher*) | annotation | Ce que le modèle cherche lorsqu'il annote (*signaler les incohérences*, *dialogues faibles*), le texte de `{%{{{annotateInstructions}}}%}` du prompt d'annotation. Texte brut. Définissez-le ici, dans un dossier ou dans *Paramètres → Templates*, et la fenêtre *Annoter avec l'IA* s'ouvre avec ce texte. |
| **Prompt de rédaction des instructions** | instructions de rédaction | La demande adressée à un modèle pour [rédiger les instructions](story-editor.md#drafting-the-instructions) du tour suivant à votre place. Un modèle. |

*Point de vue*, *Temps* et *Style* sont insérés tels quels dans le modèle principal ; les macros ne fonctionnent pas dans ces champs.

Un modèle contenant une erreur de syntaxe n'est pas enregistré ; un avertissement affiche l'erreur.

<a id="templates"></a>

## Modèles

Les modèles sont du texte contenant des emplacements entre doubles accolades. Chaque modèle possède ses propres variables :

| Modèle | Variables |
|---|---|
| Modèle principal | `{%{{{backgroundLore}}}%}` (entrées de lorebook activées), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, `{%{{{summaries}}}%}`, `{%{{{annotations}}}%}` (les notes que vous avez données au modèle) |
| Prompt utilisateur | `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}` |
| Prompt de résumé | `{%{{{backgroundLore}}}%}` (le lore que les lorebooks donnent maintenant à la partie), `{%{{{text}}}%}` (les parties à résumer) |
| Prompt de méta-résumé | `{%{{{backgroundLore}}}%}` (idem), `{%{{{summaryBlocks}}}%}` (les résumés à fusionner) |
| Prompt système de réécriture, Prompt de réécriture | `{%{{{text}}}%}` (la partie), `{%{{{redraftInstructions}}}%}` (ce que vous voulez modifier), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{followingStory}}}%}`, `{%{{{backgroundLore}}}%}`, `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, et le tour à partir duquel la partie a été écrite : `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}`, et `{%{{{annotations}}}%}` (vos notes ouvertes sur la partie, vides sauf si vous les demandez dans la fenêtre de réécriture), et pour la [réécriture d'une sélection](story-editor.md#redrafting-only-a-passage) `{%{{{selection}}}%}` (le texte sélectionné ; vide lorsque la partie entière est réécrite), `{%{{{selectionBefore}}}%}` et `{%{{{selectionAfter}}}%}` (le texte de la partie qui l'entoure) |
| Prompt de rédaction des instructions | `{%{{{impersonateInstructions}}}%}` (vos indications), `{%{{{outline}}}%}` (le plan de l'histoire fourni par un appelant, par exemple le chapitre en cours de l'Arc Outline), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{backgroundLore}}}%}` (le lore de la dernière partie), `{%{{{instructionExamples}}}%}` (vos instructions des trois dernières parties, de la plus ancienne à la plus récente), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, et le tour auquel les instructions sont destinées : `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}` |
| Prompt système d'annotation, Prompt d'annotation | Les variables des prompts de réécriture, avec `{%{{{annotateInstructions}}}%}` (ce que vous voulez faire examiner) à la place de `{%{{{redraftInstructions}}}%}`, `{%{{{annotations}}}%}` (les notes que la partie possède déjà), `{%{{{facts}}}%}` (ce que les extensions savent de l'histoire à la partie, par exemple ce que connaissent les personnages ; vide sans une telle extension) et `{%{{{outputFormat}}}%}` (la manière dont on demande au modèle de répondre) |

Une section n'affiche son contenu que lorsque la variable n'est pas vide, de sorte que le prompt ne contient aucun titre vide :

{%{
```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```
}%}

`{%{{{.}}}%}` à l'intérieur d'une section est la valeur de la variable. Si le *Cadre de la scène* est vide, toute la ligne est omise.

Tous les modèles peuvent aussi utiliser les **macros** compatibles avec SillyTavern, comme `{%{{{user}}}%}` (le personnage du point de vue), `{%{{{description}}}%}` (la description du livre), `{%{{{random::a::b}}}%}`, `{%{{{getvar::name}}}%}` et `{%{{{setvar::name::value}}}%}`. Voir [Modèles et macros](../templates/index.md) pour la syntaxe complète et la référence des macros.

<a id="master-template"></a>

## Modèle principal

Le modèle principal devient le **message système**, le premier message du prompt. La valeur intégrée par défaut contient ces blocs :

1. **Rôle système** : le modèle est un auteur qui poursuit l'histoire.
2. **Contexte du monde et lore** : les entrées de [lorebook](../lorebooks/index.md) activées (`backgroundLore`).
3. **Style d'écriture et mécanique** : point de vue, temps, style et règles générales de la prose.
4. **Résumé de l'histoire jusqu'ici** : les [résumés](summaries.md).
5. **Notes de l'auteur sur l'histoire** : seulement lorsque vous avez donné des [notes au modèle](annotations.md#giving-a-note-to-the-model).

Si le [fournisseur d'inférence](../inference-providers.md#general-settings) a un prompt de contournement activé, il est placé avant le modèle principal.

<a id="user-prompt"></a>

## Prompt utilisateur

Le prompt utilisateur est le **dernier message** du prompt : la demande de la partie à écrire. Il est rempli à partir des *Instructions du nouveau tour* (ou des *Détails du tour* pour *Régénérer* et *Variante*). La valeur intégrée par défaut demande la section suivante avec le personnage du point de vue, le lieu, les personnages présents et les instructions de l'intrigue, suivis de règles : pas de nouveaux personnages sauf demande, suivre les instructions, garder la continuité, et ne produire que le texte de l'histoire.

Les entrées de lorebook dont le mode d'insertion est *Avant le prompt utilisateur* sont placées juste avant lui ; voir [Entrées et activation](../lorebooks/entries.md).
<a id="redraft-prompt"></a>

## Prompt de redraft

Le [redraft](story-editor.md#redrafting-a-part) d'une partie est une seule requête composée de deux prompts : le **Prompt système de réécriture** est le
message système, l'histoire avant la partie la suit sous forme de messages précédents de la conversation (les parties comme les réponses
du modèle, comme une génération les envoie), et le **Prompt de réécriture** est le dernier message de l'utilisateur. Le prompt par défaut intégré demande au
modèle d'appliquer vos instructions, de garder ce que vous n'avez pas demandé de changer, de rester cohérent avec l'histoire avant et après la partie, de
conserver la voix, et de répondre uniquement par la partie réécrite. Les deux prompts ont les mêmes variables ; `{%{{{previousStory}}}%}` n'est qu'une
note indiquant que l'histoire suit sous forme de messages. Mettez dans le prompt système ce qui reste identique d'un redraft à l'autre, et dans
le dernier prompt ce qui change (le lore, qui n'est pas le même pour chaque partie, l'histoire après la partie, la partie, les instructions) : un serveur qui
retient le prompt qu'il a lu peut alors réutiliser la plus grande partie. Votre propre version du dernier prompt doit conserver les variables dont elle a besoin : sans `{%{{{text}}}%}`, le modèle ne
voit pas la partie. Chaque prompt suit la règle de tous les prompts : le champ du livre, sinon celui de *Paramètres → Modèles*, sinon celui
intégré.

Lorsque vous ne réécrivez qu'un [passage sélectionné](story-editor.md#redrafting-only-a-passage), le même prompt est utilisé avec
`{%{{{selection}}}%}` renseigné : le prompt par défaut intégré montre alors la partie avec le passage entre `[[PASSAGE TO REDRAFT]]` et
`[[/PASSAGE TO REDRAFT]]`, et demande au modèle de répondre uniquement par le texte qui remplace le passage. Un prompt à vous
qui ne connaît pas `{%{{{selection}}}%}` amène le modèle à répondre avec la partie entière, et la réponse entière remplace alors la sélection ; ajoutez un bloc `{%{{#selection}}%}...{%{{{/selection}}}%}` (et `{%{{{^selection}}}%}` pour l'autre cas) pour le gérer.

<a id="redraft-shorter-and-longer"></a>

## Réécrire plus court et plus long

Le choix **Longueur** de la fenêtre de [redraft](story-editor.md#redrafting-a-part) (**Même longueur**, **Plus court**, **Plus long**) ne touche pas aux prompts de redraft :
pour **Plus court**, le texte de **Prompt de réécriture plus courte** (pour **Plus long**, celui de **Prompt de réécriture plus longue**) est ajouté après ce que vous avez écrit dans *Instructions pour la réécriture*, à la place de `{%{{{redraftInstructions}}}%}`, ce qui fonctionne donc aussi avec un prompt de redraft à vous. Avec des instructions vides, c'est la seule consigne. Les deux textes suivent la règle de tous les prompts : le champ du livre, sinon celui du dossier le plus proche au-dessus qui en définit un, sinon celui de *Paramètres → Modèles*, sinon le texte intégré, qui demande environ un tiers à une moitié de moins (plus : une moitié de plus à deux fois plus long) sans changer ce qui se passe.

<a id="annotate-prompt"></a>

## Prompt d'annotation

Lorsqu'un modèle [annote une partie](annotations.md#letting-a-model-annotate), il reçoit le même type de requête qu'un redraft : le
**Prompt système d'annotation** comme message système, l'histoire avant la partie comme messages précédents, et le **Prompt d'annotation** comme
dernier message. Le prompt par défaut intégré fait du modèle un éditeur : il repère les passages qui posent un problème au sens de vos instructions, cite chacun
mot pour mot, et rédige pour vous une courte note, sans éloge ni remplissage et sans répéter les notes que la partie a déjà. Lorsqu'une extension fournit des [faits sur l'histoire](annotations.md#letting-a-model-annotate) (le
Knowledge Tracker : ce que savent les personnages), le prompt par défaut les présente sous *Faits établis* et l'éditeur signale ce qui les contredit.
Le prompt contient la variable `{%{{{outputFormat}}}%}` à l'endroit où la façon de répondre est décrite (JSON, blocs de texte ou revue libre, selon le format de réponse que vous choisissez) ; conservez-la dans un prompt à vous, faute de quoi le
modèle ne sait pas comment répondre. Les deux prompts d'annotation suivent la règle de tous les prompts : le champ du livre, sinon celui de
*Paramètres → Modèles*, sinon celui intégré.

**Ce qu'il faut chercher** (*Annotate Instructions*) suit la même règle, ce qui permet de le régler une fois : dans *Paramètres → Modèles* pour tous vos livres, dans un dossier pour les livres qu'il contient, ou dans l'onglet *Prompts* pour un seul livre. La fenêtre *Annoter avec l'IA* affiche ce qui s'applique sous forme de texte gris dans son champ et indique d'où il vient ; laissez le champ vide pour l'utiliser, tapez quelque chose pour le remplacer pour cette exécution, ou cliquez sur l'icône d'information du champ puis sur **Fill default** pour copier ce qui s'applique dans le champ et le modifier pour cette exécution. Si rien n'est défini nulle part, c'est la tâche par défaut du prompt. Il en va de même pour l'annotation automatique et la commande `/generate-annotation`.

<a id="impersonate-prompt"></a>

## Prompt de rédaction des instructions

Lorsqu'un modèle [rédige les instructions](story-editor.md#drafting-the-instructions) de la prochaine étape, il reçoit une seule requête, envoyée comme unique message, comme le redraft. Le prompt par défaut intégré demande au modèle d'écrire ce que vous taperiez dans *Instructions* : une spécification d'intrigue de la partie suivante (ce qui arrive, qui fait quoi, où la partie s'arrête) qui développe vos consignes et suit le plan, poursuit l'histoire là où elle s'est arrêtée, est écrite comme vos propres instructions précédentes, et n'est pas la prose elle-même. Sans consigne, elle poursuit l'histoire là où elle mène. Vos consignes sont à l'endroit où le prompt place `{%{{{impersonateInstructions}}}%}` ; conservez-la dans un prompt à vous. Le prompt de rédaction des instructions suit la règle de tous les prompts : le champ du livre, sinon le *Prompt de rédaction des instructions* de *Paramètres → Modèles*, sinon celui intégré.

<a id="how-the-prompt-is-put-together"></a>

## Comment le prompt est assemblé

```
system     jailbreak (if enabled) + master template
user       [ Generate story. ]
assistant  part #1
user       [ Generate more story. ]
assistant  part #2
...        (as many earlier parts as fit)
user       lore "Before User Prompt" + user prompt
```

Les parties précédentes sont envoyées comme les propres réponses du modèle, afin qu'il les poursuive dans la même voix. Lorsque l'histoire est plus longue que la [limite de contexte](../protocols.md#limits), les parties les plus anciennes sont retirées en premier ; les [résumés](summaries.md)
gardent celles-ci dans l'histoire sous forme condensée.

**Afficher le prompt** dans le menu de n'importe quelle partie affiche le prompt exact à partir duquel cette partie a été écrite. **Comparer le prompt** le place à côté
du prompt d'une partie antérieure et montre où ils divergent pour la première fois, à l'endroit où le [cache de prompt](../prompt-cache.md#compare-prompt) d'un serveur s'arrête.

<a id="tips"></a>

## Astuces

- Mettez les règles qui s'appliquent à chaque partie (pas de nouveaux personnages, longueur, mise en forme) dans le prompt utilisateur : étant le dernier
  message, il a l'effet le plus fort.
- Mettez les faits durables sur le monde et les personnages dans le [lorebook](../lorebooks/index.md), et non dans *Style*.
- Demandez une longueur dans les instructions (« environ 800 mots ») plutôt que de compter sur la limite de réponse, qui coupe le texte.
- Après avoir modifié un modèle, générez une partie et vérifiez-la avec **Afficher le prompt**.
