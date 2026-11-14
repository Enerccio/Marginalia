---
label: Dépannage et FAQ
order: 900
icon: question
---

# Dépannage et FAQ

<a id="where-are-the-logs"></a>

## Où se trouvent les journaux ?

La plupart des problèmes laissent une trace dans le journal :

| Installation | Journal |
|---|---|
| Application de bureau | `~/.marginalia/desktop/logs/marginalia.log` (`%USERPROFILE%\.marginalia\desktop\logs` sous Windows) ; l'exécution précédente se trouve dans `marginalia.log.1` |
| Docker | `docker compose logs -f marginalia` |

Les boîtes de dialogue d'erreur de l'application (*Erreur interne du serveur*) affichent également l'erreur ; le journal contient tous les détails.

Lorsque vous signalez un problème, indiquez le texte de version affiché dans le coin inférieur gauche de la page (par exemple
`Marginalia 1.5.0 (93ec118)` : la version et le commit Git de la version).

<a id="installing-and-starting"></a>

## Installation et démarrage

**macOS indique que l'application « est endommagée » ou « ne peut pas être ouverte ».**
L'application n'est pas encore signée. Faites un clic droit dessus et choisissez *Ouvrir*, ou exécutez
`xattr -dr com.apple.quarantine /Applications/Marginalia.app`. Voir [Application de bureau](getting-started/desktop-app.md).

**Windows SmartScreen bloque l'application.**
Cliquez sur *Informations complémentaires* → *Exécuter quand même*.

**Le navigateur ouvre un port différent de 8765.**
Le port 8765 était déjà utilisé, l'application de bureau a donc choisi un port libre. Utilisez **Ouvrir Marginalia** dans le menu de la barre d'état système, ou démarrez-la avec
`--port` pour fixer le port.

**« Marginalia did not start in time ».**
L'application de bureau n'a pas pu démarrer son serveur. Le journal indique la cause : souvent un autre programme qui occupe les fichiers, ou une extension défectueuse (voir [ci-dessous](#extensions)).

**Docker : le port 8080 est déjà utilisé.**
Modifiez la partie gauche de la correspondance de ports dans `docker-compose.yml`, par exemple `"8081:8080"`, puis ouvrez le port 8081.

**Marginalia est lent ou s'arrête avec « OutOfMemoryError ».**
Augmentez la mémoire : `-Xmx2g` (ou plus) dans `JAVA_TOOL_OPTIONS` dans `docker-compose.yml`.

<a id="logging-in"></a>

## Connexion

**« Se souvenir de moi » ne me garde pas connecté.**
Les connexions mémorisées nécessitent HTTPS sur un serveur. Placez Marginalia derrière un proxy inverse avec HTTPS, voir
[Serveur (Docker)](getting-started/docker-server.md#https-and-reverse-proxy).

**J'ai oublié mon mot de passe.**
Un administrateur peut en définir un nouveau, ou le supprimer afin que vous puissiez vous connecter avec un mot de passe vide et en définir un vous-même,
dans *Admin → Utilisateurs* (voir [Réinitialiser un mot de passe oublié](administration/users.md#resetting-a-forgotten-password)).
Il n'existe pas de réinitialisation du mot de passe par e-mail.

**Plus aucun administrateur ne peut se connecter.**
Sans administrateur, le mot de passe ne peut être réinitialisé que dans la base de données :

1. Arrêtez Marginalia et copiez `marginalia.sqlite` depuis le [dossier de données](administration/index.md#the-data-folder) comme sauvegarde.
2. Supprimez le mot de passe de l'administrateur avec l'outil `sqlite3` :
   ```sh
   sqlite3 marginalia.sqlite "UPDATE users SET passwordHash = NULL WHERE login = 'admin';"
   ```
   (utilisez le bon nom d'utilisateur).
3. Démarrez Marginalia, connectez-vous avec ce nom d'utilisateur et un mot de passe **vide**, puis définissez immédiatement un nouveau mot de passe avec
   **Changer le mot de passe**.

**« Connexion au serveur perdue » ou la page n'arrête pas de se recharger.**
Derrière un proxy inverse, le proxy doit transmettre les WebSockets et autoriser les requêtes longues : voir l'exemple nginx dans
[Serveur (Docker)](getting-started/docker-server.md#https-and-reverse-proxy). Avec l'application de bureau, l'application a probablement été fermée ; relancez-la.

**L'espace de travail fait geler le navigateur à l'ouverture.**
Marginalia rouvre ce que vous aviez ouvert à votre départ, donc un livre très long (par exemple un livre importé depuis une archive) peut figer la page avant que vous puissiez modifier quoi que ce soit. Ouvrez `/reset` à l'adresse de votre Marginalia (par exemple
`https://marginalia.example.com/reset`) et connectez-vous : l'état sauvegardé de l'espace de travail (livres ouverts, onglets sélectionnés, filtres) est oublié et vous arrivez dans un espace de travail vide. Puis définissez **Parties chargées à la fois dans l'histoire** dans votre [compte](account.md) avant d'ouvrir à nouveau le livre.

<a id="models-and-generation"></a>

## Modèles et génération

**« Failed to download model list ».**
Vérifiez que l'URL se termine par `/v1`, que la clé API est correcte et que le serveur de modèles est en marche. Certains services ne listent pas leurs modèles : saisissez alors l'identifiant du modèle. Voir [Fournisseurs d'inférence](inference-providers.md#per-type-settings).

**Docker : un serveur de modèles sur la même machine est inaccessible.**
Dans le conteneur, `localhost` désigne le conteneur lui-même. Utilisez `http://host.docker.internal:<port>/v1` et faites écouter le serveur de modèles sur toutes les adresses, voir [Modèles sur la même machine](getting-started/docker-server.md#models-on-the-same-machine).

**« Book is missing model. » / « Book is missing protocol. »**
Sélectionnez-les dans l'onglet *À propos* du livre. Définissez *Modèle par défaut* et *Protocole par défaut* dans *Paramètres* pour que les nouveaux livres les reçoivent.

**« Contextual limit not sufficient. »**
Le prompt sans aucune histoire dépasse déjà la place que laisse le contexte après la réponse. Augmentez *Taille maximale du contexte* du fournisseur (ou *Nombre maximal de jetons de contexte* du protocole), diminuez *Nombre maximal de jetons de réponse*, ou raccourcissez les modèles, les entrées du lorebook et les résumés. Voir [Limites](protocols.md#limits).

**Le modèle signale que le contexte est trop long.**
Le comptage de jetons de Marginalia est légèrement imprécis pour certains modèles. Diminuez *Taille maximale du contexte* de quelques centaines de jetons. Un avertissement avant la requête indique les tailles estimées lorsque le prompt semble déjà trop volumineux.

**« The provider did not answer in time », « The provider is limiting requests », « The provider rejected the API key »...**
Marginalia explique en termes simples les échecs de l'API du modèle, voir
[Messages d'erreur](inference-providers.md#error-messages). *Tester la connexion* dans la boîte de dialogue du fournisseur vérifie l'URL, la clé et le modèle sans livre. Le délai d'attente et les nouvelles tentatives se règlent par fournisseur.

**Le texte s'arrête au milieu d'une phrase.**
La limite de réponse a été atteinte. Augmentez *Nombre maximal de jetons de réponse* (ou *Nombre maximal de jetons de réponse* du protocole) : les modèles de raisonnement en ont besoin de beaucoup plus, car le raisonnement compte aussi.

**Les requêtes échouent lorsque le raisonnement est activé.**
L'API ne prend pas en charge `reasoning_effort`. Désactivez *Activer le raisonnement* sur le fournisseur.

**« The summaries of the messages with IDs ... are outdated ... Continue generation and delete them? »**
Une partie couverte par un résumé a été modifiée ou supprimée. Répondez *Oui* pour supprimer les résumés périmés et poursuivre, puis en générer de nouveaux. Répondez *Non* pour arrêter et les conserver : ils sont signalés dans la vue d'ensemble des résumés, où vous pouvez revalider ceux qui sont toujours justes. Voir [Quand les résumés deviennent périmés](books/summaries.md#when-summaries-become-outdated).

**Le prompt contient le mot « Error ».**
Un modèle utilise une variable ou une macro inexistante, souvent une coquille comme `instruction` au lieu de `instructions`. Le champ du modèle liste ces noms sous lui. Voir [Coquilles](templates/templates.md#typos).

## Lorebooks

**Une entrée n'est pas utilisée.** Parcourez les conditions de [Activation](lorebooks/entries.md#activation) :

- L'entrée est-elle activée ? Son lorebook l'est-il, ainsi que chaque sous-lorebook sur le chemin qui y mène ?
- Possède-t-elle des mots-clés ? Alors le livre doit en avoir un (les mots-clés du livre se trouvent dans l'onglet *À propos*). Les mots-clés négatifs l'excluent.
- Le lorebook lui-même possède-t-il des **Mots-clés** ? Ils s'appliquent à toutes ses entrées.
- Possède-t-il un filtre ? Les filtres ne voient que **les instructions de la partie en cours d'écriture**, pas l'histoire précédente : mentionnez le personnage dans *Personnages présents* ou dans les instructions.
- Le filtre est-il une expression régulière ? Une expression invalide ne correspond jamais.

**Afficher le prompt** sur une partie générée montre quel lore a été envoyé.

<a id="prompts-and-settings"></a>

## Prompts et paramètres

**Je ne peux pas passer à un autre onglet.**
L'onglet *Paramètres* contient des modifications non enregistrées. Cliquez sur **Enregistrer** ou **Abandonner les modifications**.

**Mon prompt de résumé n'est pas utilisé.**
Le prompt de résumé propre à un livre (onglet *Prompts*) l'emporte sur le *Prompt de résumé par défaut* des *Paramètres* ; videz le champ du livre pour utiliser la valeur par défaut. Les méta-résumés ont leur propre prompt, le *Prompt de méta-résumé par défaut*.

**Les macros dans Style, Point de vue ou Temps ne fonctionnent pas.**
Ces champs sont du texte brut ; seuls les modèles traitent les macros. Placez la macro dans le modèle principal ou dans le prompt utilisateur.

<a id="backups"></a>

## Sauvegardes

**J'ai restauré une sauvegarde de base de données, mais rien n'a changé.**
Une restauration est appliquée au prochain démarrage. Redémarrez Marginalia. Voir [Restauration](administration/database-backups.md#restoring).

**Les sauvegardes de livres manquent après un changement d'ordinateur.**
Les sauvegardes de livres sont des fichiers, et non une partie de la base de données. Copiez aussi le dossier `data`, voir
[Le dossier de données](administration/index.md#the-data-folder).

## Extensions

**Une extension installée n'apparaît pas.**
Fermez puis rouvrez le livre, ou rechargez la page.

**Une extension ne se charge pas et n'apparaît pas dans Admin → Extensions.**
Arrêtez Marginalia, supprimez son JAR du dossier `extensions`, puis redémarrez. Voir
[Quand une extension échoue](administration/extensions.md#when-an-extension-fails).

**Une extension a été refusée ou a cessé de fonctionner après la mise à jour de Marginalia.**
Les extensions sont compilées pour une version précise de Marginalia et sont vérifiées au chargement. Ouvrez le rapport (Admin → Extensions → *Vérification*) et chargez la version de l'extension correspondant à votre Marginalia. Voir
[Quand une extension échoue](administration/extensions.md#when-an-extension-fails).

**Après la mise à jour d'une extension, l'ancienne version tourne encore.**
Déchargez d'abord l'ancienne version, puis chargez la nouvelle, ou redémarrez Marginalia.
## FAQ

**Mon texte est-il envoyé quelque part ?**
Uniquement vers les fournisseurs d'inférence que vous configurez : le prompt de chaque partie (et les requêtes de comptage de tokens) est envoyé à l'API de ce fournisseur. Avec un modèle qui tourne sur votre propre ordinateur, rien ne quitte la machine. Marginalia n'envoie aucune télémétrie.

**Quel modèle dois-je utiliser ?**
Tout modèle qui écrit bien la fiction et dispose d'un contexte suffisamment grand ; un contexte de 32 000 tokens ou plus est confortable pour les longs livres. Essayez-en plusieurs avec le même livre : changer de fournisseur ne prend qu'un clic dans l'onglet **À propos**.

**Plusieurs personnes peuvent-elles travailler sur un même livre ?**
Non. Chaque livre appartient à un seul utilisateur. Les autres utilisateurs peuvent le lire une fois qu'il est [publié](books/publishing.md).

**Puis-je utiliser Marginalia hors ligne ?**
Oui, avec un modèle qui tourne en local (llama.cpp, LM Studio, Ollama...). Marginalia lui-même n'a pas besoin de connexion internet.

**Comment sortir mon livre de Marginalia ?**
[Exportez l'histoire](books/export.md) en texte, HTML, Word, PDF ou EPUB depuis le menu des réglages (⚙) de l'onglet **Histoire**.
Pour tout conserver, y compris toutes les branches, [exportez une sauvegarde du livre](books/backups.md#exporting-and-importing) (JSON).
Vous pouvez aussi le lire dans la [visionneuse](books/publishing.md).

**Cela fonctionne-t-il sur un téléphone ?**
La [visionneuse](books/publishing.md) est conçue pour les téléphones, et Marginalia peut être ajouté à l'écran d'accueil comme une application (*Install app* / *Add to Home Screen* dans le menu du navigateur). L'éditeur dispose d'une [mise en page pour téléphone](phone.md) lorsque la fenêtre du navigateur est plus étroite que 768 pixels. Il n'a pas de page d'administration, un éditeur de lorebook réduit et pas toutes les extensions : il convient donc mieux à l'écriture et à la lecture ; la configuration et l'administration sont plus faciles sur un grand écran.

**Existe-t-il un thème clair ou une autre langue ?**
Oui aux deux : le bouton soleil / lune à côté du sélecteur de langue change le thème (sombre par défaut), et le sélecteur de langue propose le japonais ainsi que les langues apportées par les extensions (voir [Compte](account.md#theme)).
