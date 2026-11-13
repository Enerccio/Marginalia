---
label: Tâches
order: 86
---

# Tâches

Tout ce que l'application demande à une **IA externe** est une *tâche* : une génération, un résumé, une annotation, une exécution d'agent, un chapitre
converti en audio, une image. Chaque tâche est consignée avec l'utilisateur pour qui elle s'est exécutée, son heure de début et de fin, la façon dont elle s'est terminée, et chaque *appel*
qu'elle a fait à un modèle ou à un serveur de synthèse vocale ou d'images, avec à nouveau son début et sa fin. L'onglet **Tâches** de la page d'administration affiche le journal ;
la page `/jobs` montre ce qui s'exécute en ce moment.

Seules les métadonnées sont conservées : qui, quand, combien de temps, quelle IA ou quel serveur (le nom de l'IA, ou l'hôte du serveur de synthèse vocale ou d'images ; jamais le chemin de l'adresse ni la requête), le modèle, la façon dont elle s'est terminée et le bref message d'erreur d'un échec. **Aucun prompt, aucune réponse, aucun texte** n'est jamais enregistré.

<a id="what-is-one-job"></a>

## Ce qu'est une tâche

- Une **requête que rien ne regroupe** (une génération, une annotation, une réécriture, une imitation, un résumé d'une partie, une question à l'assistant d'aide, une requête d'un plugin comme une traduction d'une partie ou d'une entrée du lore) est une tâche à part entière avec un seul appel. Son genre est le type de travail (`generation`, `annotate`, `summary`...).
- Une **réécriture des résumés** est une tâche (`summary`) avec un appel par résumé rédigé.
- Une **session de travail d'une exécution d'agent** est une tâche (`agent`) avec un appel par tour de l'agent ; une exécution mise en pause puis reprise a une tâche pour chaque session, et les requêtes d'un sous-agent sont des tâches à part entière.
- Une **conversion en audio** est une tâche (`audiobook`) par lot de parties, avec les requêtes du modèle d'étiquetage et chaque morceau lu comme appels.
- Une image produite par le **Générateur d'images** est une tâche à part entière (`image-generator`) avec un appel d'image.

<a id="what-runs-now-jobs"></a>

## Ce qui tourne maintenant : `/jobs`

![La page /jobs avec une génération en cours](../../images/jobs-live.png)

Ouvrez `/jobs` à l'adresse de votre Marginalia (par exemple `http://localhost:8080/jobs` ; le lien **Ouvrir la vue en direct** de l'onglet le fait). Elle demande l'identification comme toutes les autres pages et n'affiche rien aux utilisateurs qui ne sont pas administrateurs. La liste se rafraîchit toutes les deux secondes et montre, pour chaque tâche en cours, l'utilisateur, le genre, la durée d'exécution, le nombre d'appels terminés et ce qui est en cours à cet instant (l'IA ou le serveur et le temps d'attente de l'appel). C'est le coup d'œil rapide lorsque le serveur semble occupé : une longue liste d'appels en attente sur un même modèle explique pourquoi tout est lent.

Sous les tâches, **Exécutions sur les livres** liste ce qui travaille sur un livre en ce moment et empêche que des parties de celui-ci soient supprimées ou modifiées sous ses pieds
(voir le [panneau des exécutions](../books/story-editor.md#the-panel-of-the-runs), qui montre la même chose à chaque utilisateur pour ses propres
livres) : l'utilisateur, le livre, ce qu'il fait, où il en est (ce qu'affiche le panneau : *Waiting for the model*, *The model is thinking/writing...*, *Working*, *Waits
for you*, et pour une génération son étape, comme *INFERENCE* pendant qu'elle rédige sa partie ; la fenêtre d'un livre ouvert n'affiche rien à cet endroit), depuis quand, et combien d'objets elle retient (le livre, les parties). La fenêtre d'un livre ouvert est aussi une exécution. **Arrêter** arrête l'exécution comme le ferait son utilisateur. **Terminer**, après une question, met fin à une exécution sans arrêter son travail : ce qu'elle retient est restitué immédiatement. Ne l'utilisez que pour une exécution qui ne se termine jamais (son travail est bloqué et elle retient le livre jusqu'à un redémarrage) ; son travail peut encore écrire à son réveil.

<a id="the-log-the-jobs-tab"></a>

## Le journal : l'onglet Tâches

![L'onglet Tâches de la page d'administration](../../images/admin-jobs.png)

| | |
|---|---|
| **Utilisateur, Genre, Statut** | Filtres. Le *genre* est ce qu'est la tâche (`generation`, `summary`, `annotate`, `agent`, `audiobook`, `image-generator`...) ; une requête faite par du code qui n'a pas nommé son travail est `untagged`. |
| **Plus anciennes que (jours)** | Seulement les tâches commencées il y a au moins autant de jours. |
| **Actualiser** | Relit le journal. |
| **Supprimer ces tâches** | Supprime définitivement les tâches affichées par le filtre et leurs appels, après une question qui indique combien il y en a. Sans filtre, il supprime tout le journal. |
| **Ouvrir la vue en direct** | Ouvre `/jobs` dans un nouvel onglet. |

Cliquez sur une ligne pour voir les appels de la tâche : le type (modèle de langage, audio, image), l'IA ou le serveur, le modèle, le début, la fin, la durée, le statut et l'erreur d'un appel en échec. Une tâche affichée comme en échec a fait un appel qui a échoué, ou a été terminée en échec par le code qui l'exécutait ; un appel annulé est celui que vous avez arrêté (ou celui qui était encore en cours lorsque sa tâche s'est terminée).

![Les appels d'une conversion en livre audio](../../images/admin-jobs-calls.png)

Une tâche en échec montre quel appel a échoué et pourquoi :

![Une conversion en échec avec l'erreur de son appel de synthèse vocale](../../images/admin-jobs-failed.png)

**Rien n'est supprimé de lui-même.** Le journal grandit à chaque requête, il faut donc le vider de temps en temps : réglez *Plus anciennes que (jours)*
(par exemple 30), regardez ce qui est listé, puis supprimez. Le [nettoyage](cleanup.md) supprime les tâches d'un utilisateur avec l'utilisateur lui-même.

![Les tâches de plus de 30 jours, et la question posée avant leur suppression](../../images/admin-jobs-old.png)

![La question posée avant la suppression des tâches](../../images/admin-jobs-delete.png)

Une tâche est écrite dans le journal lorsqu'elle se termine ; une tâche encore en cours n'est visible que sur `/jobs`. Si l'application s'arrête, les tâches en cours sont perdues (elles n'ont jamais été terminées). Une tâche que personne n'a terminée et qui ne fait aucun appel pendant une demi-heure est terminée comme réussie.
