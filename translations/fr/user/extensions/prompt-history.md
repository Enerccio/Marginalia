---
label: Historique des prompts
order: 5
---

# Historique des prompts

Affiche ce qui a réellement été envoyé au modèle et ce qu'il a répondu, pour déboguer une extension ou vérifier le contenu du prompt de toute requête autre qu'une génération classique.

<a id="how-it-works"></a>

## Fonctionnement

L'extension conserve les 20 dernières requêtes envoyées aux modèles dans votre session : les messages envoyés, la réponse et
le raisonnement du modèle (lorsque le fournisseur le transmet). Toutes les sortes de requêtes sont conservées, les générations comprises. Elle ajoute le panneau **Historique des prompts** à *Paramètres → Paramètres des extensions* ; le bouton **Afficher l'historique des prompts** ouvre la liste, la requête la plus récente en premier, avec :

- la **date** de la requête,
- le **modèle** : le nom de l'IA et l'identifiant du modèle,
- le **type de requête** : `generation` (une nouvelle partie, un swipe, une régénération), `annotate`, `summary`, `impersonate`,
  `redraft`, `agent`, `help`, ou le nom du type de travail propre à une extension,
- le statut (en cours, terminée, annulée, échouée) et la durée de la requête.

Choisissez une requête pour la lire dans les onglets sous la liste : **Requête** (chaque message sous son rôle), **Raisonnement**,
et **Réponse** (la réponse, les fonctions appelées par le modèle, et l'erreur d'une requête échouée). **Actualiser** recharge
la liste et **Effacer** la vide. Une requête apparaît dans la liste dès son envoi : pendant son exécution, sa réponse et son raisonnement s'allongent
dans la boîte de dialogue au fur et à mesure que le modèle les écrit.

![L'historique des requêtes](../../images/plugin-prompt-history.png)

Pour un [agent](../agents.md), le pied de page de sa console comporte le bouton **Historique des prompts** : il ouvre la même liste, avec
les requêtes de cette exécution.

<a id="notes"></a>

## Remarques

- L'historique est conservé uniquement en mémoire, compacté pour occuper peu de place. Il disparaît à la déconnexion ou au redémarrage du serveur, et il ne fait pas partie d'une sauvegarde.
- Chaque session de navigateur a sa propre liste. Les requêtes d'un agent n'y figurent pas : elles sont conservées par exécution (20 par exécution, pour les 10 dernières exécutions) et affichées depuis la console de l'exécution.
- Les messages sont affichés en entier, texte de l'histoire compris.
