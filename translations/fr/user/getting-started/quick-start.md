---
label: Démarrage rapide
order: 70
---

# Démarrage rapide

De l'installation à la première partie générée d'une histoire. Ce guide suppose que Marginalia est
[installé](index.md) et que vous êtes connecté en tant qu'[administrateur](first-start.md).

<a id="1-connect-a-model"></a>

## 1. Connecter un modèle

1. Ouvrez l'onglet **Fournisseurs d'inférence** et cliquez sur **Ajouter un fournisseur d'inférence**.
2. Saisissez un **Nom**, par exemple `OpenRouter - Claude`.
3. Dans **Paramètres généraux**, réglez **Taille maximale du contexte** sur la taille de contexte du modèle, par exemple `32000`, et
   **Nombre maximal de jetons de réponse** sur la plus longue partie que vous souhaitez, par exemple `2000`.
4. Dans **Paramètres par type**, saisissez l'**OpenAI URL** (elle se termine par `/v1`) et la **Clé d'API** (laissez-la vide pour un serveur local sans clé), cliquez sur **Actualiser** et
   choisissez le **Modèle**.
5. Cliquez sur **OK**.

Les champs sont décrits dans [Fournisseurs d'inférence](../inference-providers.md), avec les URL des services courants.

<a id="2-create-a-protocol"></a>

## 2. Créer un protocole

1. Ouvrez l'onglet **Protocoles** et cliquez sur **Ajouter un protocole**.
2. Saisissez un **Nom**, par exemple `Default`.
3. Laissez **Nombre maximal de jetons de contexte** et **Nombre maximal de jetons de réponse** vides : ce sont les limites du fournisseur d'inférence qui sont utilisées.
4. Cliquez sur **OK**.

Voir [Protocoles](../protocols.md).

<a id="3-set-the-defaults"></a>

## 3. Définir les valeurs par défaut

Ouvrez l'onglet **Paramètres** et choisissez le nouveau fournisseur comme **Modèle par défaut** et le nouveau protocole comme
**Protocole par défaut**. Cliquez sur **Enregistrer**. Les nouveaux livres démarrent alors avec les deux, pour que vous n'ayez pas à les sélectionner à chaque fois.

![Modèle et protocole par défaut dans Paramètres](../../images/settings-defaults.png)

<a id="4-create-a-book"></a>

## 4. Créer un livre

1. Ouvrez l'onglet **Livres** et cliquez sur **Ajouter un livre**.
2. Saisissez le nom du livre. Le livre s'ouvre.
3. Dans l'onglet **À propos**, vérifiez que **Modèle** et **Protocoles** sont renseignés (ils proviennent de vos valeurs par défaut), et ajoutez une
   **Description** de l'histoire si vous le souhaitez.

<a id="5-write-the-first-part"></a>

## 5. Écrire la première partie

1. Ouvrez l'onglet **Histoire**.
2. Cliquez sur **+** dans le coin inférieur droit. Le panneau *Instructions du nouveau tour* s'ouvre.
3. Décrivez ce qui doit se passer dans **Instructions**, par exemple :
   > Un gardien de phare sur une île isolée trouve un message dans une bouteille, qui lui est adressé et daté de cinquante ans dans le futur. Présentez-la ainsi que l'île. Environ 800 mots.

   *Cadre de la scène*, *Personnage du point de vue* et *Personnages présents* sont facultatifs ; remplissez-les pour donner davantage de directives au modèle.
4. Cliquez sur **Générer**. Le texte apparaît pendant que le modèle l'écrit. Le bouton rouge **Arrêter** (à la place de **+**)
   interrompt la génération.

![La première partie générée](../../images/quick-start-first-part.png)

Cliquez à nouveau sur **+** pour la partie suivante. Chaque partie peut être modifiée, régénérée ou remplacée par une autre version depuis son menu (☰) ; voir [Éditeur de l'histoire](../books/story-editor.md) et
[Branches et arbre de l'histoire](../books/branches-and-story-tree.md).

<a id="next-steps"></a>

## Étapes suivantes

- Décrivez votre monde et vos personnages dans un [recueil de connaissances](../lorebooks/index.md).
- Ajustez le style, le point de vue et le temps verbal dans les [prompts](../books/prompts.md) du livre.
- [Résumez](../books/summaries.md) les parties anciennes à mesure que le livre grandit.
- Mettez en place des [sauvegardes du livre](../books/backups.md).
