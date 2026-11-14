---
label: Branches et arbre de l'histoire
order: 80
---

# Branches et arbre de l'histoire

Marginalia ne vous oblige jamais à jeter une version. Chaque variante que vous essayez est conservée comme une
**branche** de l'histoire, et vous pouvez revenir à n'importe laquelle.

<a id="how-the-story-tree-works"></a>

## Fonctionnement de l'arbre de l'histoire

Chaque partie fait suite à la partie qui la précède. Lorsqu'une partie a plusieurs suites, l'histoire se divise : chaque
chemin allant de la première partie à une fin constitue une branche.

```
#1 ─ #2 ─ #3 ─ #4 ─ #5          ← branch A
           └─ #4' ─ #5' ─ #6'   ← branch B (active)
```

La **branche active** est celle que l'[éditeur de l'histoire](story-editor.md) affiche et poursuit. Seules ses parties sont
envoyées au modèle ; les autres branches n'influencent pas l'histoire.

Il existe trois façons d'obtenir une nouvelle branche :

| | Ce qui se passe | L'ancienne version |
|---|---|---|
| **Variante** (dernière partie) | Le modèle écrit une autre version de la dernière partie. | Reste une branche séparée. |
| **Créer une branche** (n'importe quelle partie) | L'histoire se poursuit à partir de cette partie sur une nouvelle branche. | Les parties suivantes restent sur l'ancienne branche. |
| **Régénérer** (dernière partie) | Le modèle réécrit la dernière partie, à sa place. | Remplacée : aucune branche n'est créée. |

*Variante* et *Régénérer* ne figurent que dans le menu de la dernière partie ; *Créer une branche* est présent dans le
menu de chaque partie.

<a id="trying-another-version-of-the-last-part"></a>

## Essayer une autre version de la dernière partie

Lorsque la dernière partie ne vous plaît pas, choisissez **Variante** dans son menu. Le modèle réécrit la partie à partir
des mêmes [détails du tour](story-editor.md#turn-details), et la nouvelle version devient la fin de la branche active. La version précédente
est conservée comme branche à part ; vous pouvez y revenir dans la [Vue des branches](#the-branch-view).

Pour modifier ce qui doit se passer avant une nouvelle tentative, ouvrez **Détails du tour** de la dernière partie et
modifiez les instructions.

Utilisez **Régénérer** seul lorsque vous n'avez pas besoin de l'ancienne version : elle remplace la partie.

<a id="branching-from-an-earlier-part"></a>

## Créer une branche à partir d'une partie antérieure

Pour orienter l'histoire dans une autre direction à partir d'un point antérieur, ouvrez le menu de cette partie et
choisissez **Créer une branche**. Marginalia copie la partie sur une nouvelle branche et en fait la fin de la branche
active ; l'éditeur de l'histoire se termine donc désormais par cette partie. Poursuivez avec **+** comme d'habitude.

Les parties qui venaient après ne sont pas perdues : elles restent sur l'ancienne branche.

Les [résumés](summaries.md) de la partie copiée sont copiés avec elle.

<a id="the-branch-view"></a>

## La Vue des branches

L'onglet **Vue des branches** affiche l'ensemble de l'arbre de l'histoire. Chaque partie est une carte avec sa date, le
cadre de la scène, les personnages présents et les instructions ; survolez une carte pour voir tous ses détails.

![L'arbre de l'histoire](../../images/story-tree.png)

- Les parties de la **branche active** sont mises en évidence, et sa dernière partie est marquée.
- **Cliquez sur la dernière partie d'une autre branche** pour activer cette branche. Le livre passe à l'onglet *Histoire*
  et l'affiche. Seules les fins de branches sont cliquables ; pour poursuivre à partir d'une partie située au milieu,
  utilisez [Créer une branche](#branching-from-an-earlier-part).
- Les longues séquences sans aucun embranchement (plus de 20 parties) sont réduites en un bloc affichant les dix
  premières et les dix dernières parties. Cliquez sur le bloc pour afficher toutes les parties.

<a id="searching-all-branches"></a>

### Rechercher dans toutes les branches

La zone située au-dessus de l'arbre recherche dans le texte de **toutes les branches** du livre, et non seulement dans la
branche active. Saisissez ce que vous cherchez et appuyez sur **Entrée** ou cliquez sur **Rechercher** ; la zone mémorise
vos dernières recherches, choisissez-en une dans la liste pour la relancer. Le **x** de la zone efface la recherche.

- Une partie correspond lorsque son texte contient **chacun des mots** saisis, dans n'importe quel ordre : `lighthouse key` trouve
  les parties qui contiennent les deux mots. Mettez les mots entre guillemets pour chercher une expression exacte :
  `"lighthouse key"`.
- Utilisez `*` pour n'importe quels caractères : `light*` trouve *lighthouse* et *lightning*. Les autres caractères, y
  compris `%`, `_` et `\`, sont recherchés tels quels.
- Les majuscules et les minuscules n'ont pas d'importance. Pour les lettres accentuées, un mot saisi en minuscules trouve
  aussi le même mot en MAJUSCULES ou en initiale majuscule (`žluťoučký` trouve *Žluťoučký* et *ŽLUŤOUČKÝ*).
- Seul le texte des parties est recherché, et non les instructions, le cadre de la scène ou le lorebook. Les parties que
  vous avez supprimées ne sont pas recherchées.

![La vue des branches avec une recherche](../../images/story-tree-search.png)

La case **Inclure les versions**, à côté de la zone, examine également les versions antérieures du [texte](version-history.md#searching-the-old-text) des
parties ; une partie qui ne contient les mots que dans une ancienne version est mise en évidence elle aussi, indique
de quelle version il s'agit, et possède **Afficher dans l'historique** à côté de **Afficher dans l'histoire**.

Chaque partie correspondante est **mise en évidence** en orange et affiche le texte autour de la correspondance ; la
ligne au-dessus de l'arbre indique le nombre de parties trouvées. Les longues séquences qui seraient réduites sont
déployées lorsqu'elles contiennent une correspondance. La recherche reste active jusqu'à ce que vous l'effaciez ou fermiez
la fenêtre du livre, et la mise en évidence est mise à jour lorsque vous revenez à l'onglet.

Cliquez sur **Afficher dans l'histoire** dans une partie mise en évidence pour la lire :

- Lorsque la partie se trouve sur la branche active, le livre passe à l'onglet *Histoire* et fait défiler jusqu'à elle.
- Lorsqu'elle se trouve sur une autre branche, cette branche devient d'abord la branche active. Elle se termine à la fin
  de branche la plus proche située après la partie trouvée (la plus récente lorsqu'il y en a deux), de sorte que
  l'éditeur de l'histoire affiche l'histoire jusqu'à la partie trouvée et ce qui l'a suivie. Le livre passe ensuite à
  l'onglet *Histoire* et fait défiler jusqu'à la partie, qui clignote brièvement.

Changer de branche revient à cliquer sur la fin d'une branche dans l'arbre : rien n'est perdu, l'ancienne branche reste
telle quelle et vous pouvez y revenir depuis l'arbre.

<a id="deleting-parts-and-branches"></a>

## Supprimer des parties et des branches

**Supprimer** dans le menu d'une partie n'enlève que cette partie. Les parties qui la suivent remontent et se rattachent à
la partie qui la précède. Pour supprimer une branche entière, supprimez ses parties à partir de la fin.

Les compteurs de mots et de jetons de l'onglet *À propos* indiquent à la fois l'ensemble du livre (toutes les branches) et
la branche active.
