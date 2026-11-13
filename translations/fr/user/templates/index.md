---
label: Modèles et macros
order: 940
icon: code
---

# Modèles et macros

Tout ce que Marginalia envoie au modèle est construit à partir de **modèles** : des textes contenant des emplacements réservés, remplis pour chaque partie générée. Vous pouvez tous les modifier, du cadre du prompt jusqu'à une seule entrée de livre de lore.

{%{
```handlebars
Write the next section of the story.
{{#povCharacter}}Point of View Character: {{.}}{{/povCharacter}}
{{instructions}}
```
}%}

<a id="where-templates-are-used"></a>

## Où les modèles sont utilisés

| Modèle | Défini dans | Devient |
|---|---|---|
| **Modèle principal** | livre [Prompts](../books/prompts.md), *Paramètres → Modèles* | le message système : rôle, lore, style, résumés |
| **Prompt utilisateur** | livre Prompts, *Paramètres → Modèles* | la demande pour la partie en cours de rédaction |
| **Prompt de résumé** | livre Prompts, *Paramètres → Modèles* | la demande pour un [résumé](../books/summaries.md) |
| **Prompt de méta-résumé** | livre Prompts, *Paramètres → Modèles* | la demande pour un [méta-résumé](../books/summaries.md#meta-summaries) |
| **Contenu d'une entrée de lore** | [entrées de livre de lore](../lorebooks/entries.md) | le lore inséré dans le prompt |

*Point of View*, *Temps* et *Style* sont des textes simples, et non des modèles.

<a id="two-kinds-of-placeholders"></a>

## Deux types d'emplacements réservés

Les modèles combinent deux syntaxes :

- **Handlebars** - des variables comme {%{`{{instructions}}`}%} et des sections comme
  {%{`{{#sceneSetting}}...{{/sceneSetting}}`}%}, qui ne sont affichées que si la valeur n'est pas vide. Voir
  [Modèles de prompt](templates.md).
- **Macros SillyTavern** - {%{`{{user}}`, `{{random::a::b}}`, `{{setvar::name::value}}`, `{{if ...}}`}%} et le reste des macros
  connues des utilisateurs de SillyTavern, afin que les livres de lore et les prompts de SillyTavern continuent de
  fonctionner. Voir [Référence des macros](macros.md).

Les deux syntaxes fonctionnent dans tous les modèles et peuvent être combinées librement.

<a id="help-in-the-editor"></a>

## Aide dans l'éditeur

Chaque champ de modèle dispose d'une fenêtre d'aide :

- **Insérer le modèle par défaut** copie le modèle par défaut dans le champ ;
- **Variables de modèle disponibles** liste les variables de ce modèle, y compris celles disponibles partout ;
- **Macros disponibles** liste toutes les macros avec une courte description.

![La fenêtre d'aide d'un champ de modèle](../../images/template-hints.png)

Un modèle contenant une erreur de syntaxe (une section non fermée, par exemple) ne peut pas être enregistré dans les prompts
du livre et dans les Paramètres ; dans les entrées de lore, il est enregistré, marqué comme invalide, et utilisé comme
texte simple jusqu'à sa correction.

Les variables inconnues (généralement des fautes de frappe) n'empêchent pas l'enregistrement ; le champ les liste sous
celui-ci sous forme d'avertissement, voir [Fautes de frappe](templates.md#typos).

**Afficher le prompt** dans le menu d'une partie générée affiche le prompt tel qu'il a été envoyé - le meilleur moyen de
vérifier ce qu'a produit un modèle.

<a id="in-this-section"></a>

## Dans cette section

- [Modèles de prompt](templates.md) - variables de chaque modèle, sections, conditions, ordre d'évaluation
- [Référence des macros](macros.md) - toutes les macros, variables, conditions et compatibilité avec SillyTavern
