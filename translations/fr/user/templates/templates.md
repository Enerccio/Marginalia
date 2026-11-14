---
label: Modèles de prompts
order: 100
---

# Modèles de prompts

Les modèles utilisent la syntaxe [Handlebars](https://handlebarsjs.com/guide/), étendue avec les
[macros](macros.md) de SillyTavern. Cette page présente les variables que reçoit chaque modèle et la partie Handlebars de la syntaxe.

{%{

## Variables

Une variable entre doubles accolades est remplacée par sa valeur : `{{instructions}}` devient les instructions que vous avez écrites pour
la partie. Une valeur vide n'affiche rien.

<a id="variables-of-each-template"></a>

### Variables de chaque modèle

| Modèle | Variables |
|---|---|
| **Modèle principal** | `{{backgroundLore}}` entrées de lorebook activées (*Dans le bloc de lore*), `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}`, `{{summaries}}` tous les résumés de la branche, `{{annotations}}` les [notes](../books/annotations.md#giving-a-note-to-the-model) que vous avez marquées pour les donner au modèle, pour les parties du prompt |
| **User Prompt** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}` - les instructions du tour |
| **Summary Prompt** | `{{text}}` les parties à résumer, `{{backgroundLore}}` lore actif au moment où la partie résumée a été écrite |
| **Meta Summary Prompt** | `{{summaryBlocks}}` les résumés à fusionner, du plus ancien au plus récent, `{{backgroundLore}}` lore actif au moment où la partie du méta-résumé a été écrite |
| **Lorebook entry** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}`, `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` |

<a id="variables-available-everywhere"></a>

### Variables disponibles partout

En plus de ses propres variables, chaque modèle peut utiliser :

| Variable | |
|---|---|
| `{{povCharacter}}`, `{{sceneSetting}}`, `{{presentCharacters}}`, `{{instructions}}` | Les instructions du tour de la partie en cours d'écriture (pour un résumé : de la partie résumée). |
| `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` | Les paramètres narratifs du livre. |
| `{{manuscriptName}}`, `{{manuscriptDescription}}` | Le nom et la description du livre. |

La bulle d'aide (ⓘ) de chaque champ de modèle les liste aussi.

<a id="typos"></a>

### Fautes de frappe

Une variable qui n'existe pas s'affiche sous la forme du mot **`Error`** : `{{instruction}}` (sans *s*) met *Error* dans
le prompt. Les champs de modèle (*Prompts* du livre, *Paramètres*, entrées de lorebook) listent ces noms sous le champ, sous la forme
*Noms inconnus (envoyés au modèle comme « Erreur »)*. Ce n'est qu'un avertissement : le modèle est tout de même enregistré, donc
corrigez les noms qui y sont listés. Les variables et les sections sont vérifiées partout, y compris dans les branches `{{#if}}` qui ne sont pas affichées. Les noms
utilisés uniquement comme condition ne sont pas signalés, car ils n'affichent pas *Error* : dans `{{#if name}}`, un nom inconnu vaut
faux ; dans la forme SillyTavern `{{if name}}`, il est pris pour du texte brut, donc vrai.

## Sections

Une section n'affiche son contenu que si la variable a une valeur. À l'intérieur, `{{.}}` est la valeur :

```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```

Si *Cadre de la scène* est vide, toute la ligne disparaît : les modèles par défaut utilisent cela partout, pour que le prompt
ne contienne pas de titres vides.

Une **section inversée** n'affiche son contenu que si la variable est vide :

```handlebars
{{^povCharacter}}Write from an omniscient narrator's point of view.{{/povCharacter}}
```

## Conditions

`{{#if}}` avec un `{{else}}` optionnel :

```handlebars
{{#if presentCharacters}}
Characters present: {{presentCharacters}}
{{else}}
The POV character is alone.
{{/if}}
```

`{{#unless x}}...{{/unless}}` est l'inverse de `{{#if x}}`. La forme SillyTavern `{{if x}}...{{/if}}` fonctionne aussi et
offre davantage d'options (négation, variables, comparaisons), voir [Conditionnelles](macros.md#conditionals).

Les macros sans arguments peuvent être utilisées comme des variables dans les sections et les conditions :

```handlebars
{{#lastMessage}}The story so far ends with: {{.}}{{/lastMessage}}
{{#if summary}}...{{/if}}
```

<a id="comments-and-escaping"></a>

## Commentaires et échappement

| Syntaxe | Résultat |
|---|---|
| `{{!-- a comment --}}`, `{{! short comment }}` | Supprimés de la sortie. Utilisez-les pour vos notes dans les modèles. |
| `\{{instructions}}` | Affiche `{{instructions}}` tel quel, au lieu de la valeur. |
| `{{{instructions}}}` | Identique à `{{instructions}}`. Rien n'est échappé en HTML dans Marginalia : `<`, `&` et les guillemets sont affichés tels quels. |

Le commentaire SillyTavern `{{// ...}}` et l'échappement `\{\{...\}\}` fonctionnent aussi, voir [Utilitaires](macros.md#utility).

<a id="order-of-evaluation"></a>

## Ordre d'évaluation

Pour une partie générée, les modèles sont traités dans cet ordre :

1. le **user prompt** ;
2. les **entrées de lorebook** activées, dans leur ordre ;
3. le **modèle principal**.

Tous partagent un même ensemble de [variables](macros.md#variables) : une valeur définie avec `{{setvar}}` dans le user prompt peut
être lue dans les entrées de lorebook et dans le modèle principal ; une valeur définie dans une entrée de lorebook est visible par les entrées suivantes
et par le modèle principal, mais pas par le user prompt, qui a déjà été traité.

Les filtres de lorebook voient le user prompt déjà traité : une macro du user prompt peut donc produire un texte qui active
des entrées.

<a id="what-is-kept-between-parts"></a>

## Ce qui est conservé entre les parties

| | |
|---|---|
| **Variables locales** (`{{setvar}}`, `{{.name}}`) | Enregistrées avec la partie générée. La partie suivante part des valeurs de la dernière partie de la branche active, donc chaque branche a ses propres valeurs. *Régénérer* repart des valeurs de la partie précédente. |
| **Variables globales** (`{{setglobalvar}}`, `{{$name}}`) | Enregistrées avec le livre, partagées par toutes les branches. Elles ne sont pas annulées par *Régénérer* ni par la suppression d'une partie. |
| **Résumés** | Peuvent lire les variables, mais leurs modifications sont ignorées. |

`{{pick}}` donne le même résultat à chaque fois pour un même livre, `{{random}}` un nouveau à chaque fois.

<a id="writing-your-own-templates"></a>

## Écrire vos propres modèles

Partez du modèle par défaut : ouvrez la bulle d'aide et cliquez sur **Insérer le modèle par défaut**, puis modifiez-le. Points à conserver :

- le bloc `{{#backgroundLore}}` du modèle principal - sans lui, le lorebook n'est pas utilisé (seules les entrées *Avant le prompt utilisateur* atteignent encore le modèle) ;
- le bloc `{{#summaries}}` du modèle principal - sans lui, les résumés sont perdus ;
- `{{instructions}}` dans le user prompt - sans lui, le modèle ne voit pas ce qui doit se passer ;
- `{{text}}` dans le prompt de résumé - sans lui, il n'y a rien à résumer.

}%}
