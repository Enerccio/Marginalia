---
label: Utilisateurs
order: 100
---

# Utilisateurs

L'onglet **Utilisateurs** de la page d'administration liste tous les comptes avec leur **Nom d'utilisateur**, leur **Nom complet** et l'indication de savoir s'ils sont **Administrateur**.

![L'onglet Utilisateurs](../../images/admin-users.png)

<a id="adding-a-user"></a>

## Ajouter un utilisateur

1. Cliquez sur **Ajouter un utilisateur**.
2. Renseignez **Nom d'utilisateur**, **Nom complet**, **Mot de passe** et **Répéter le mot de passe**.
3. Cochez **Administrateur** si l'utilisateur doit gérer l'installation.
4. Cliquez sur **OK**.

Communiquez le nom d'utilisateur et le mot de passe à la personne concernée ; elle pourra ensuite changer le mot de passe elle-même (voir [Compte et paramètres](../account.md#changing-the-password)).

!!!warning Toujours définir un mot de passe
Marginalia accepte un compte sans mot de passe : n'importe qui peut alors se connecter avec le seul nom d'utilisateur. Ne faites cela que dans l'application de bureau, sur votre propre ordinateur. Créer un utilisateur sans mot de passe demande d'abord une confirmation.
!!!

Le nom d'utilisateur doit être unique.

<a id="editing-a-user"></a>

## Modifier un utilisateur

Le bouton en forme de crayon ouvre la même boîte de dialogue pour un utilisateur existant :

- modifier le nom d'utilisateur, le nom complet et l'indicateur d'administrateur ;
- laisser les deux champs de mot de passe vides pour conserver le mot de passe actuel de l'utilisateur ;
- pour définir un **nouveau mot de passe**, saisissez-le dans les deux champs de mot de passe ;
- pour **effacer le mot de passe**, cochez **Effacer le mot de passe** (les champs de mot de passe sont alors désactivés) puis confirmez. Le compte n'a alors plus de mot de passe.

- pour obliger les navigateurs de l'utilisateur à se reconnecter, cochez **Effacer les connexions enregistrées (se souvenir de moi)** ; cette option n'apparaît que lorsque l'utilisateur a des connexions enregistrées. Une session déjà ouverte reste ouverte, mais les cookies « se souvenir de moi » de l'utilisateur cessent de fonctionner.

Vous n'avez pas besoin du mot de passe actuel de l'utilisateur pour toutes ces opérations. Un nouveau mot de passe, ou un mot de passe effacé, met fin à toutes les connexions enregistrées de l'utilisateur.

<a id="resetting-a-forgotten-password"></a>

### Réinitialiser un mot de passe oublié

Marginalia ne propose pas de réinitialisation du mot de passe par e-mail. Vous pouvez soit :

- définir ici un nouveau mot de passe et le communiquer à l'utilisateur, qui pourra le changer ensuite avec **Changer le mot de passe** ; soit
- cocher **Effacer le mot de passe**, demander à l'utilisateur de se connecter avec son nom d'utilisateur et un mot de passe **vide**, puis de définir immédiatement un nouveau mot de passe avec **Changer le mot de passe** (laisser **Mot de passe actuel** vide). De cette façon, l'administrateur ne connaît jamais le nouveau mot de passe.

!!!warning
Tant que l'utilisateur n'a pas défini de nouveau mot de passe, toute personne connaissant le nom d'utilisateur peut se connecter à un compte dont le mot de passe a été effacé. Sur un serveur, n'effacez le mot de passe que si l'utilisateur peut se connecter immédiatement.
!!!

Le dernier administrateur ne peut pas perdre l'indicateur d'administrateur : *Le dernier administrateur ne peut être ni supprimé ni rétrogradé.*

<a id="users-and-providers"></a>

## Utilisateurs et fournisseurs

Ajouter un [fournisseur d'inférence](../inference-providers.md) nécessite la permission **Créer ses propres modèles** ; les administrateurs l'ont toujours. Un utilisateur qui ne l'a pas ne peut utiliser que les fournisseurs que les administrateurs [partagent](../inference-providers.md#shared-providers). Les fournisseurs déjà rattachés à un utilisateur continuent de fonctionner et restent modifiables. Voir [Permissions](permissions.md).

## Permissions

Le bouton en forme de clé, sur la ligne d'un utilisateur, ouvre ses [permissions](permissions.md) : ce que l'utilisateur peut faire au-delà de l'ordinaire, comme ajouter ses propres modèles ou solliciter l'assistant d'aide. Un nouvel utilisateur n'en a aucune ; attribuez-lui ce dont il a besoin après l'avoir ajouté.

<a id="deleting-a-user"></a>

## Supprimer un utilisateur

Le bouton corbeille supprime le compte après confirmation. Vous ne pouvez pas supprimer votre propre compte ni le dernier administrateur.

L'utilisateur ne peut plus se connecter, les connexions enregistrées sur ses appareils cessent de fonctionner, et le nom d'utilisateur redevient disponible pour un nouveau compte.

Ses livres, ses lorebooks, ses fournisseurs d'inférence (avec leurs clés API), ses protocoles, ses étiquettes et ses paramètres disparaissent avec le compte : personne ne peut plus les voir, et le prochain [Nettoyage](cleanup.md) les supprime de la base de données avec le compte.

Les fichiers de l'utilisateur (images et ressources importées) sont conservés : son dossier dans le répertoire de données est renommé en `<login>-deleted` (`<login>-deleted-2`… si ce nom est déjà pris), ainsi un nouveau compte portant le même nom d'utilisateur commence avec un dossier vide. Supprimez le dossier renommé à la main lorsque vous n'en avez plus besoin.
