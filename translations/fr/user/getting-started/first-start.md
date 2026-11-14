---
label: Premier démarrage
order: 80
---

# Premier démarrage

<a id="create-the-administrator"></a>

## Créer l'administrateur

Lorsque Marginalia démarre avec une base de données vide, il demande le premier compte au lieu d'afficher l'écran de connexion.
Ce compte est l'**administrateur** : il peut gérer les utilisateurs, les sauvegardes de la base de données, le nettoyage et les extensions.

![Création du compte administrateur](../../images/first-start.png)

Renseignez :

| Champ | |
|---|---|
| **Nom d'utilisateur** | Le nom de connexion. |
| **Nom complet** | Le nom affiché dans l'application. |
| **Mot de passe**, **Répéter le mot de passe** | Les deux doivent être identiques. Marginalia accepte aussi un mot de passe vide - ne le laissez jamais vide sur un serveur, n'importe qui pourrait alors se connecter en administrateur avec seulement le nom d'utilisateur. |

**Administrateur** est coché et ne peut pas être modifié pour le premier compte. Cliquez sur **OK**. La page se recharge et affiche l'écran de
connexion.

!!!
Notez le mot de passe. Il n'existe pas de réinitialisation par e-mail. Les autres administrateurs peuvent définir un nouveau mot de passe pour un
utilisateur dans *Administration → Utilisateurs*.
!!!

<a id="log-in"></a>

## Se connecter

Connectez-vous avec le nouveau compte. Avec **Rester connecté** coché, le navigateur reste connecté pendant 30 jours. Cela ne fonctionne qu'en HTTPS ou sur `127.0.0.1` avec l'application de bureau (voir [Serveur](docker-server.md#https-and-reverse-proxy)).

![L'écran de connexion](../../images/login.png)

La version de Marginalia et l'identifiant court du commit Git à partir duquel elle a été compilée (par exemple `Marginalia 1.5.0 (93ec118)`) s'affichent sous le formulaire de connexion.

<a id="the-workspace"></a>

## L'espace de travail

Après la connexion, vous voyez l'espace de travail. Les onglets à gauche sont :

| Onglet | |
|---|---|
| **Livres** | Vos livres. En ouvrir un affiche son éditeur de récit. Voir [Livres](../books/index.md). |
| **Lorebooks** | Notes sur le monde et les personnages pour le prompt. Voir [Lorebooks](../lorebooks/index.md). |
| **Paramètres** | Vos paramètres personnels : modèle et protocole par défaut pour les nouveaux livres, modèles de prompt, sauvegardes. Voir [Compte et paramètres](../account.md). |
| **Protocoles** | Paramètres de génération. Voir [Protocoles](../protocols.md). |
| **Fournisseurs d'inférence** | Les modèles auxquels Marginalia se connecte. Voir [Fournisseurs d'inférence](../inference-providers.md). |
| **Ressources** | Les fichiers que vous avez envoyés. Voir [Ressources](../resources.md). |
| **Corbeille** | Ce que vous avez supprimé, jusqu'au nettoyage par un administrateur. Voir [Corbeille](../trash.md). |

En bas se trouvent **Administration** (administrateurs uniquement), le sélecteur de **Langue**, **Changer le mot de passe** et **Déconnexion**
(voir [Administration](../administration/index.md) pour les pages *Administration*).

![L'espace de travail après la première connexion](../../images/workspace.png)

Le même texte de version, en petits caractères, se trouve dans le coin inférieur gauche de la page ; survolez-le pour afficher l'identifiant complet du commit et
l'heure du commit et de la compilation. Indiquez-le lorsque vous signalez un problème. Une version compilée depuis un dépôt de développement
(`mvn jetty:run`) affiche `dev` à la place de la version.

Marginalia se souvient de l'endroit où vous étiez. La prochaine fois que vous vous connecterez (sur n'importe quel appareil), il ouvrira le même onglet, avec les mêmes
filtres de la liste des livres, le même lorebook et les mêmes entrées de lorebook ouvertes et, si vous aviez laissé un livre ouvert, ce livre sur le
même onglet - avec la scène, le personnage POV, les personnages présents et les instructions que vous aviez saisies pour la prochaine partie. Ce qui
n'existe plus (un livre, un lorebook ou une étiquette supprimé) est ignoré ; un livre qui a été supprimé n'est pas ouvert.

Poursuivez avec le [démarrage rapide](quick-start.md).
