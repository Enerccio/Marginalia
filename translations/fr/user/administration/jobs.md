---
label: Tâches
order: 86
---

# Tâches

Tout ce que l'application demande à une **IA externe** est une *tâche* : une génération, un résumé, une annotation, une exécution d'agent, un chapitre converti en audio, une image. Chaque tâche est consignée avec l'utilisateur pour qui elle s'exécute, l'heure de début et de fin, la façon dont elle s'est terminée, et chaque *appel* qu'elle a fait à un modèle ou à un serveur de synthèse vocale ou d'images, avec son propre début et sa propre fin. L'onglet **Jobs** de la page d'administration affiche le journal ; la page `/jobs` montre ce qui tourne en ce moment.

Seules les métadonnées sont conservées : qui, quand, combien de temps, quelle IA ou quel serveur (le nom de l'IA, ou l'hôte du serveur de synthèse vocale ou d'images ; jamais le chemin de l'adresse ni la requête), le modèle, la façon dont la tâche s'est terminée et le court message d'erreur en cas d'échec. **Aucune invite, aucune réponse, aucun texte** n'est jamais enregistré.

<a id="what-is-one-job"></a>

## Qu'est-ce qu'une tâche

- Une **demande que rien ne regroupe** (une génération, une annotation, une réécriture, une impersonation, un résumé d'une partie, une question à l'assistant d'aide, une requête d'un plugin comme la traduction d'une partie ou d'une entrée de lore) est une tâche à part, avec un seul appel. Son genre est le type de travail (`generation`, `annotate`, `summary`...).
- Une **réécriture des résumés** est une tâche (`summary`) avec un appel par résumé rédigé.
- Une **session de travail d'une exécution d'agent** est une tâche (`agent`) avec un appel par tour de l'agent ; une exécution mise en pause puis reprise a une tâche par session, et les requêtes d'un sous-agent sont des tâches à part.
- Une **conversion en audio** est une tâche (`audiobook`) par lot de parties, avec les requêtes du modèle d'étiquetage et chaque passage prononcé comme appels.
- Une image produite par le **Image Generator** est une tâche à part (`image-generator`) avec un appel d'image.

<a id="what-runs-now-jobs"></a>

## Ce qui tourne maintenant : `/jobs`

![Page /jobs avec une génération en cours](../../images/jobs-live.png)

Ouvrez `/jobs` à l'adresse de votre Marginalia (par exemple `http://localhost:8080/jobs` ; le lien **Open the live view** de l'onglet le fait). Elle demande l'identifiant comme toutes les autres pages et n'affiche rien aux utilisateurs qui ne sont pas administrateurs. La liste se rafraîchit toutes les deux secondes et affiche, pour chaque tâche en cours, l'utilisateur, le genre, la durée d'exécution, le nombre d'appels terminés et ce qui est en attente maintenant (l'IA ou le serveur, et depuis combien de temps l'appel attend), ainsi que le livre de l'exécution pour laquelle la tâche travaille, avec son bouton **Stop**. C'est le coup d'œil rapide quand le serveur semble occupé : une longue liste d'appels en attente sur un même modèle explique pourquoi tout est lent.

Sous les tâches, **Runs on the books** liste ce qui travaille sur un livre maintenant et empêche que des parties de celui-ci soient supprimées ou modifiées sous lui (voir le [panneau des exécutions](../books/story-editor.md#the-panel-of-the-runs), qui montre la même chose à chaque utilisateur pour ses propres livres) : l'utilisateur, le livre, ce qu'il fait, où il en est (ce qu'affiche le panneau : *Waiting for the model*, *The model is thinking/writing...*, *Working*, *Waits for you*, et pour une génération son étape, comme *INFERENCE* pendant qu'elle écrit sa partie ; la fenêtre d'un livre ouvert n'affiche rien à cet endroit), depuis quand, et combien d'objets elle détient (le livre, les parties). La fenêtre d'un livre ouvert est aussi une exécution. **Stop** arrête l'exécution comme le ferait son utilisateur. **End**, après une question, termine une exécution sans arrêter son travail : ce qu'elle détient est rendu tout de suite. Ne l'utilisez que pour une exécution qui ne se termine jamais ; son travail peut encore écrire quand il se réveille. La plupart de ces exécutions se terminent d'elles-mêmes : une exécution sur laquelle rien n'a travaillé pendant une heure, et qui n'attend ni vous ni son modèle, est terminée par le serveur, et il en va de même pour la fenêtre d'une page qui a été fermée.

<a id="the-log-the-jobs-tab"></a>

## Le journal : l'onglet Jobs

![L'onglet Jobs de la page d'administration](../../images/admin-jobs.png)

| | |
|---|---|
| **User, Kind, Status** | Filtres. Le *kind* est le type de tâche ; c'est `generation`, `summary`, `annotate`, `agent`, `audiobook`, `image-generator`... ; une requête faite par du code qui n'a pas nommé son travail est `untagged`. |
| **Older than (days)** | Seulement les tâches commencées il y a ce nombre de jours ou plus. |
| **Refresh** | Relit le journal. |
| **Delete these jobs** | Supprime définitivement les tâches affichées par le filtre et leurs appels, après une question qui indique combien. Sans filtre, supprime tout le journal. |
| **Open the live view** | Ouvre `/jobs` dans un nouvel onglet. |

Cliquez sur une ligne pour voir les appels de la tâche : le type (modèle de langage, audio, image), l'IA ou le serveur, le modèle, le début, la fin, la durée, le statut et l'erreur d'un appel en échec. Une tâche affichée en échec a fait un appel qui a échoué, ou a été terminée en échec par le code qui l'exécutait ; un appel annulé est un appel que vous avez arrêté (ou qui était encore en cours quand sa tâche s'est terminée).

![Les appels d'une conversion d'un livre audio](../../images/admin-jobs-calls.png)

Une tâche en échec indique quel appel a échoué et pourquoi :

![Une conversion en échec avec l'erreur de son appel de synthèse vocale](../../images/admin-jobs-failed.png)

**Rien n'est supprimé automatiquement.** Le journal grandit à chaque requête ; videz-le de temps en temps : réglez *Older than (days)* (par exemple 30), regardez ce qui est listé, puis supprimez. Le [nettoyage](cleanup.md) supprime les tâches d'un utilisateur en même temps que l'utilisateur.

![Les tâches de plus de 30 jours, et la question posée avant leur suppression](../../images/admin-jobs-old.png)

![La question posée avant la suppression des tâches](../../images/admin-jobs-delete.png)

Une tâche est écrite dans le journal quand elle se termine ; une tâche encore en cours n'est visible que sur `/jobs`. Si l'application s'arrête, les tâches en cours sont perdues (elles n'ont jamais été terminées). Une tâche que personne n'a terminée et qui ne fait aucun appel pendant une demi-heure est terminée comme réussie.
