---
label: Marqueur de chapitre
order: 20
---

# Marqueur de chapitre

Transforme le **Plan** de l'[éditeur de récit](../books/story-editor.md) en table des matières.

![Chapitres dans le plan](../../images/plugin-chaptermarker.png)

<a id="how-it-works"></a>

## Fonctionnement

Lorsque le texte d'une partie contient un titre Markdown - une ligne commençant par `#` - l'entrée de la partie dans le plan affiche le titre à la place de l'étiquette habituelle `#n (DB ID: m)`, mis en évidence :

```
# Chapter 3: The Harbour

Rain had been falling on Ostra since dawn...
```

apparaît dans le plan sous la forme **5. Chapter 3: The Harbour** - le numéro correspond à la position de la partie dans la branche, suivi du titre. Les parties sans titre conservent leur étiquette normale.

- Le titre peut se trouver sur n'importe quelle ligne de la partie ; s'il y en a plusieurs, c'est le premier qui est utilisé.
- Tous les niveaux de titre fonctionnent (`#`, `##`, `###`...).
- Le plan se met à jour dès que vous enregistrez une [modification](../books/story-editor.md#editing-the-text) : ajoutez un titre pour commencer un chapitre, supprimez-le pour fusionner la partie avec la précédente.

<a id="usage"></a>

## Utilisation

Aucune configuration n'est nécessaire. Vous pouvez soit écrire vous-même le titre dans la partie, soit demander au modèle de le produire dans les instructions, par exemple *Commence un nouveau chapitre intitulé « The Harbour » avec un titre Markdown.*

!!!
Le [prompt utilisateur](../books/prompts.md#user-prompt) par défaut demande au modèle de ne pas écrire de titres ni de titres de chapitres. Si vous souhaitez qu'il en écrive, indiquez-le explicitement dans les instructions, ou modifiez cette règle dans le prompt utilisateur.
!!!

Les titres font partie du texte du récit : ils sont donc aussi envoyés au modèle et affichés dans la [visionneuse](../books/publishing.md) ainsi que dans les sauvegardes.

La même règle permet de retrouver les chapitres dans la table des matières des [livres exportés](../books/export.md#table-of-contents).
