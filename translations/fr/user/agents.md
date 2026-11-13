---
label: Agents (expérimental)
order: 958
icon: cpu
---

# Agents (expérimental)

!!!warning Expérimental
Les agents sont nouveaux dans la 1.4.0 et ont été très peu utilisés avec de vrais modèles. Attendez-vous à des aspérités : des appels d'outils qu'un modèle faible fait mal, des exécutions qui coûtent plus de jetons que prévu, un écran de revue plus laborieux qu'il ne devrait l'être, et un comportement qui change d'une version à l'autre. Votre livre est protégé (l'agent travaille sur une copie, le résultat est relu et une sauvegarde est faite avant d'être appliqué), mais **gardez votre propre sauvegarde d'un livre avant de reprendre le résultat d'un agent**, fixez un budget de jetons, et surveillez les premières exécutions dans la [console](#the-console). Les signalements de ce qui a mal tourné sont les bienvenus.
!!!

Un **agent** est un modèle qui travaille sur l'un de vos livres de manière autonome, sur une longue durée, en appelant les [commandes](commands/index.md) que vous lui autorisez comme outils : il peut lire l'histoire, la vérifier par rapport au lorebook et vous faire un rapport. L'onglet **Agents** sert à les créer, à définir ce que chacun peut faire, à démarrer et arrêter leurs exécutions, à leur parler dans la [console](#the-console) et à les cloner.

**Démarrer** lance l'agent : il travaille en arrière-plan, un tour à la fois, en appelant les commandes que vous avez autorisées et ses propres outils de fichiers, jusqu'à ce qu'il termine, vous pose une question, atteigne une limite, ou que vous le mettiez en pause ou que vous l'arrêtiez (voir [Une exécution](#a-run)). Il travaille sur une [copie](#the-working-copy) cachée du livre, de sorte que votre livre n'est jamais touché pendant ce temps (le livre lui-même est [verrouillé](#the-book-is-locked)) ; une fois terminé, vous [examinez son résultat](#reviewing-the-result) et reprenez ce que vous voulez.

L'onglet n'apparaît que si un administrateur vous a accordé la [permission](administration/permissions.md) **Agents** (les administrateurs l'ont toujours, mais les limites sont les mêmes pour tous : les valeurs par défaut, sauf si l'administrateur remplit les réglages de sa propre permission ; un administrateur qui a besoin de plus d'agents, d'exécutions ou de place dans l'espace de travail les règle là. Une limite modifiée atteint une exécution en cours lorsqu'elle est mise en pause puis reprise.) Sur un téléphone, chaque agent est une carte avec les mêmes boutons, voir [Sur un téléphone](#on-a-phone).

<a id="the-table"></a>

## Le tableau

| Colonne | |
|---|---|
| **ID de l'agent** | L'identifiant de l'agent. |
| **Nom** | Le nom que vous lui avez donné. |
| **Livre** | Le livre sur lequel travaille l'agent. |
| **Statut** | Ce que fait son exécution : *En cours*, *Vous attend*, *En attente d'approbation*, *Mise en pause*, *En pause*, *Arrêt en cours*, *Arrêtée, résultat gardé*, *Terminée, attend l'examen*, *Appliquée*. Un agent sans exécution indique *À l'arrêt*, ou *Désactivé* lorsqu'il est éteint. |
| **Dernière exécution** | Le moment où la plus récente exécution de l'agent a démarré, *jamais* pour un agent sans exécution. |

![L'onglet Agents](../images/agents-tab.png)

Au-dessus du tableau, deux champs restreignent la liste : **Nom** et **Livre** n'affichent que les agents dont le nom, ou le nom de leur livre, contient tous les mots que vous tapez, dans n'importe quel ordre et quelle que soit la casse. Les en-têtes **Nom**, **Livre**, **Statut** et **Dernière exécution** trient le tableau (cliquez à nouveau pour inverser ; les agents qui n'ont jamais tourné viennent en premier lorsque la dernière exécution est croissante). Sur un téléphone, un sélecteur **Trier par** au-dessus des cartes les ordonne par nom, par statut, ou par exécution la plus récente en premier.

Les boutons en fin de ligne dépendent de l'exécution :

| Bouton | |
|---|---|
| **Démarrer** | Ouvre un menu. **Démarrer immédiatement** : l'agent travaille seul sur sa tâche (ses instructions) et vous le savez lorsqu'il vous pose une question ou termine. **Démarrer dans la console** : ouvre la [console](#the-console) et demande à l'agent de ne pas encore travailler, mais de vous demander immédiatement ce que vous voulez qu'il fasse ; l'exécution devient alors une [conversation](#a-run) qui se poursuit jusqu'à ce que vous appuyiez sur **Terminer**. |
| **Reprendre** | Poursuit une exécution mise en pause ou arrêtée (affiché à la place de **Démarrer**). Il demande une note facultative à l'agent, voir [Reprendre une exécution](#resuming-a-run). |
| **Mettre en pause** | Met en pause une exécution qui travaille ou qui attend votre réponse. |
| **Mettre en pause maintenant** | Affiché pendant que l'agent travaille : le met en pause sans attendre la fin du tour, voir [Pause et arrêt](#a-run). |
| **Console** | Ouvre la [console](#the-console) de l'agent. |
| **Arrêter** | Demande s'il faut **garder le résultat** (l'exécution pourra être reprise plus tard) ou le **supprimer**. |
| **Modifier** | Ouvre l'agent. |
| **Cloner** | Copie l'agent, voir [Clonage](#cloning). |
| **Supprimer** | Déplace l'agent, avec ses exécutions, vers la [corbeille](trash.md). |

Le démarrage est refusé, avec la raison, lorsque l'agent est désactivé, n'a pas de modèle, ou que vous avez atteint le nombre d'exécutions que vous pouvez mener en même temps.

![Le menu du bouton Démarrer](../images/agent-start-menu.png)

Un agent a au plus une exécution non terminée, et un livre a au plus un agent.
<a id="a-run"></a>

## Une exécution

Une exécution est le travail de l'agent. Elle se déroule par tours : le modèle est sollicité une fois, il répond par du texte et/ou des appels à ses outils, les résultats lui sont renvoyés, et ainsi de suite.
Chaque tour est sauvegardé, si bien qu'une exécution peut être mise en pause puis reprise plus tard, y compris après un redémarrage du serveur. L'agent dispose des commandes que vous avez autorisées ainsi que des siennes :

| Outil | |
|---|---|
| Fichiers | `list_files`, `read_file`, `write_file`, `append_file`, `replace_in_file`, `regex_replace_in_file`, `grep_files`, `move_file`, `copy_file`, `merge_files`, `unzip_file`, `delete_file` dans son propre espace de travail (notes qu'il conserve entre les exécutions). À côté de l'espace de travail, il voit le fichier en lecture seule `/AGENTS.md`, vos instructions telles qu'elles étaient au démarrage de l'exécution, qu'il peut relire quand il le souhaite et qu'il ne peut jamais modifier. `list_files` et `grep_files` acceptent un motif de nom (`glob`, par exemple `*.md` ou `chapters/*.md`), `copy_file` copie un dossier avec tout ce qu'il contient (un moyen rapide de conserver une version des notes), `merge_files` assemble plusieurs fichiers en un seul sans que le texte ne transite par le modèle (le dernier nom d'un chemin peut être un motif tel que `/chapters/*.md`), `unzip_file` décompresse dans un nouveau dossier une [archive zip](#unpacking-a-zip-file) de l'espace de travail. `replace_in_file` et `regex_replace_in_file` acceptent aussi un **dossier** comme chemin : ils modifient chaque occurrence dans tous les fichiers texte qu'il contient (seulement ceux dont le nom correspond à `glob`, lorsqu'il est donné) et indiquent `chemin : n remplacements` pour chaque fichier. Il s'agit d'un **essai à blanc** (rien n'est écrit, le rapport indique ce qui serait modifié) sauf si l'agent passe `dry_run` à false ; l'opération est tout ou rien (si un fichier ne peut pas être écrit, aucun n'est modifié) et refusée, sans rien modifier, lorsque plus de `max_files` fichiers (100 par défaut, sauf si l'agent en demande davantage) seraient modifiés. Un fichier isolé peut lui aussi faire l'objet d'un essai à blanc. `list_command_results` et `save_command_result` conservent le long résultat d'une commande sans que l'agent ait à le réécrire : les dix derniers résultats sont conservés en entier (l'agent voit un résultat long tronqué), et l'un d'eux peut être enregistré dans un fichier par son numéro, le plus récent si aucun n'est donné ; il transmet ensuite le fichier sous la forme `@file:/chemin`. |
| `@file:/chemin` | Ce n'est pas un outil mais une manière d'en appeler un : un argument texte d'une commande qui vaut exactement `@file:/notes/chapitre.md` est remplacé par le contenu de ce fichier de l'espace de travail de l'agent. Ainsi, l'agent peut écrire un long texte dans un fichier une seule fois, le vérifier, puis le transmettre à une commande (le résultat affiche la référence courte dans l'appel, la commande reçoit le texte). `write_file` et `append_file` l'acceptent aussi comme contenu, ce qui permet de copier un fichier ou de l'ajouter à un autre : un long fichier peut être écrit par parties, chacune tenant dans la réponse la plus longue du modèle, puis assemblé de cette façon. |
| `@result` | Comme `@file:`, pour le résultat d'une commande : un argument texte qui vaut exactement `@result` est remplacé par le résultat complet de la dernière commande appelée par l'agent, `@result:3` par le résultat portant ce numéro (`list_command_results` affiche les numéros). Un texte généré passe d'une commande à la suivante, ou vers `write_file` et `append_file`, sans que l'agent ait à le réécrire. |
| `search_manual`, `read_manual_page` | Rechercher une information dans le manuel utilisateur de Marginalia (les mêmes pages que lit l'assistant d'aide) : le fonctionnement d'un modèle de prompt ou d'une macro, l'effet d'un réglage de lorebook. Lecture seule ; chaque agent les possède. |
| `view_image` | Regarde un fichier image de son espace de travail (PNG, JPEG, GIF ou WebP) : l'image est montrée au modèle dans le message suivant. Seul un agent dont le modèle est marqué comme voyant les images (**Le modèle voit les images** dans l'agent) le possède. Une grande image est réduite avant d'être envoyée, un GIF montre sa première image, et seules les quatre images les plus récentes restent devant le modèle (une image plus ancienne devient une note, et l'agent peut la regarder à nouveau). Les images viennent de vous (**Téléverser un fichier**, ou [jointes à un message](#the-console)) ; l'agent ne peut pas en produire. |
| Images pour les commandes | Une commande qui envoie des images à un modèle qui lui est propre, comme [`generate-lore-entry`](extensions/lore-generator.md#commands), accepte un paramètre `images` : une liste de chemins de fichiers image de l'espace de travail (8 au maximum). C'est le modèle de la commande, et non celui de l'agent, qui doit les voir ; l'agent peut donc transmettre des images même lorsque son propre modèle ne voit pas les images. |
| `update_plan` | Sa liste de tâches. |
| `report` | Une constatation pour vous (ce qu'il a trouvé, où, et sa gravité : *Note*, *Avertissement* ou *Problème*). L'agent poursuit après l'avoir émise ; vous lisez les constatations dans la [console](#the-console) et dans la [relecture](#reviewing-the-result), en dehors de son rapport final. Une constatation par appel, 200 au maximum par exécution. |
| `get_run_status` | Le nombre de tours et de jetons utilisés, le contexte rempli, et la réponse la plus longue qu'il peut écrire ; son plan ; et ses sous-agents : une ligne pour chacun avec son état, son tour, ses jetons, ce qu'il a fait en dernier et sa tâche. L'agent connaît aussi ces limites de son modèle par son prompt, afin d'écrire un long texte par parties. |
| `checkpoint`, `checkpoint_list`, `checkpoint_diff`, `checkpoint_restore`, `checkpoint_delete` | Ses [points de contrôle](#checkpoints) : enregistre un point de restauration de la copie de travail et de son espace de travail (avec un libellé), les liste, affiche ce qui a changé depuis l'un d'eux (les réglages, parties, lorebooks et entrées du livre ajoutés, modifiés ou supprimés, et les fichiers de l'espace de travail ; avec un chemin, seulement ce fichier ou ce dossier, et pour un fichier texte les phrases qui diffèrent), revient à l'un d'eux (à la fin du tour) et supprime celui dont il n'a plus besoin. |
| `get_sync_report` | Uniquement après une reprise qui a repris des modifications de votre part : liste à nouveau ce que vous avez modifié dans le livre pendant la pause et ce qui en est passé dans la copie de l'agent, voir [Reprendre une exécution](#resuming-a-run). |
| `request_compaction` | Compacte son propre contexte : à la fin du tour, il écrit le résumé de son travail précédent, comme avec `/compact`. Il lui est demandé d'enregistrer d'abord ses notes dans un fichier. Voir [Contexte qui se remplit](#running-a-command-yourself). |
| `ask_user` | Une question pour vous. L'exécution attend (*Vous attend*). |
| `finish` | Son rapport : l'exécution passe à *Terminer*. Le rapport est affiché comme motif du statut. |

Voici comment une exécution se termine :

- **Vous attend :** l'agent a répondu sans appel d'outil, ou il vous a posé une question. Répondez-lui dans la [console](#the-console).
- **Fait :** l'agent a appelé `finish` (dans une exécution lancée *immédiatement*). Une exécution lancée *dans la console* est une conversation : le rapport de l'agent vous parvient comme une question, l'exécution attend votre message suivant, et seul **Terminer** dans la console (ou **Arrêter**) la termine. Le résultat d'une exécution *Fait* attend votre [relecture](#reviewing-the-result).
- **En pause**, avec le motif : la limite de tours, le budget de jetons ou la limite de contexte est atteinte, le modèle n'a pas pu être sollicité (une erreur du fournisseur, votre limite sur l'IA), ou le serveur a été redémarré. **Reprendre** continue depuis le dernier tour complet, voir [Lorsque le serveur s'arrête](#when-the-server-stops).
- **Échec :** le moteur d'exécution lui-même a cassé ; le motif indique pourquoi.

**Mettre en pause** prend effet entre deux tours : la requête au modèle et les appels en cours se terminent d'abord. Cela peut prendre
longtemps (un modèle lent, une longue génération), c'est pourquoi il existe **Mettre en pause maintenant** : elle annule la requête au modèle, dont la réponse est jetée, et l'appel
en cours, répond aux appels du tour qui n'ont pas été faits comme tels, et l'exécution passe à *En pause* dans un instant. Ce que la requête jetée aurait
coûté est perdu ; reprenez normalement. **Arrêter** fait de même : elle n'attend ni la requête ni l'appel en cours, ils sont annulés et l'exécution se termine aussitôt
(conservée ou abandonnée selon votre choix).

![La question lorsqu'une exécution est arrêtée](../images/agent-stop.png)

Une commande en cours reçoit l'ordre de s'arrêter, et une commande conçue pour être arrêtée (celles qui écrivent avec un modèle, chercher et remplacer) se termine aussitôt et laisse ce qu'elle avait
déjà fait. Une commande qui ne peut pas être arrêtée peut tout de même se terminer ; dans les deux cas, l'agent est informé qu'on ne sait pas si l'appel a été effectué et vérifie l'état avec une commande de lecture lorsqu'il reprend. Les commandes des sous-agents sont annulées de la même façon lorsque l'exécution est mise en pause, arrêtée ou qu'un message lui est envoyé immédiatement.

Pendant que l'agent attend une place libre auprès de son modèle (voir [Un modèle, plusieurs agents](#one-model-several-agents)), il est *En cours* ; un **Mettre en pause** ou un **Arrêter** met aussitôt fin à cette attente.
<a id="the-console"></a>

## La console

**Console** ouvre une fenêtre pour la dernière exécution de l'agent, avec huit onglets : **Run**, **Runs**, **Workspace**, **Checkpoints**, **Context**, **Book copy**, **Sub-agents** et **Diagnostics**.

![Une exécution lancée dans la console : l'agent demande quoi faire](../images/agent-console-start.png)

**Run** affiche la *transcription* : ce que vous avez demandé à l'agent, ce qu'il a répondu (en [Markdown](https://commonmark.org/help/), de sorte que les listes, les titres et l'emphase s'affichent comme tels), et chaque commande ou outil de fichier qu'il a appelé, replié sur une seule ligne avec les
arguments (cliquez dessus pour voir les arguments et le résultat, affiché en texte brut). Lorsque le modèle envoie son **raisonnement** (ce qu'il a réfléchi avant de répondre), un bloc replié *Reasoning* le montre avant la réponse, et le téléchargement le contient. Vous en obtenez ou non selon le modèle et le serveur : il faut qu'il le transmette, et que votre IA ait le raisonnement activé. Pendant qu'une requête est en cours, la console affiche ce que le modèle a écrit jusqu'ici (son raisonnement, ouvert pour que vous puissiez suivre, et son texte) dans un bloc marqué *Au travail...* à la fin de la transcription, au-dessus des messages qui attendent ; il est remplacé par les blocs définitifs une fois le tour terminé. Tant qu'il est affiché, la console relit l'exécution deux fois par seconde, sinon toutes les deux secondes. Seul l'agent lui-même est affiché de cette façon, pas ses sous-agents, et ce que le modèle écrit à l'intérieur d'un appel (un long fichier, par exemple) n'apparaît qu'une fois le tour terminé. Chaque message porte à droite de sa première ligne l'heure à laquelle il a été écrit (l'heure du jour, avec la date si c'était un autre jour ; survolez pour voir l'heure complète), afin que vous puissiez savoir combien de temps l'agent a travaillé et quand il s'est arrêté. L'état de l'exécution, le **contexte** (combien de jetons la dernière requête a utilisés sur la limite du modèle, pour que vous voyiez à quel point l'exécution est proche d'une pause), les tours et les jetons utilisés (ainsi que le budget de jetons, s'il y en a un) restent au-dessus de la transcription, ainsi que la zone de votre message en dessous, tandis que la transcription défile. Ce que l'agent attend figure dans la transcription à l'endroit où il a été écrit, sous forme de bulle distincte qui reste en place lorsque l'exécution se poursuit :

![La console pendant que l'agent travaille : un message de l'auteur attend dans la file](../images/agent-console-run.png)

- une **Question de l'agent**, lorsque l'exécution est *Vous attend* ; écrivez la réponse dans la zone de texte et appuyez sur **Envoyer** (ou **Ctrl+Enter**), et l'agent poursuit ;
- un **Rapport de l'agent**, lorsqu'il a appelé `finish`.

![La question d'un agent, avec les réponses qu'il propose](../images/agent-console-question.png)

![Le rapport de l'agent à la fin d'une conversation](../images/agent-console-report.png)

![La question d'un agent, avec les réponses qu'il propose](../images/agent-console-question.png)

![Le rapport de l'agent à la fin d'une conversation](../images/agent-console-report.png)

La ligne au-dessus de la transcription affiche l'état de l'exécution et ses chiffres : les tours, la taille du contexte par rapport à sa limite, la **Limite de réponse** (le plus grand nombre de jetons que le modèle de l'agent peut écrire dans une seule réponse ; un appel qui en demande davantage est coupé, si bien que l'agent écrit les longs textes en plusieurs parties) et le total des jetons. Lorsque l'agent a établi un plan (`update_plan`), un bouton **Plan** avec le nombre d'étapes faites sur le total (**Plan 2/5**) apparaît à cet endroit ; il ouvre les étapes dans une fenêtre flottante, chacune avec un repère *Fait*, *En cours* ou *À faire*. Lorsqu'il a signalé des constats (`report`), un bouton **Constats** avec leur nombre (**Constats 3**) les ouvre de la même façon, chacun avec sa gravité. Les constats sont conservés avec l'exécution : ils restent là après une pause, un redémarrage, et lorsque vous consultez une exécution plus ancienne dans l'historique ; le fichier Markdown d'une exécution (**Télécharger**) les contient dans une section **Constats**.

![Le plan de l'agent](../images/agent-console-plan.png)

![Les constats de l'agent](../images/agent-console-findings.png)

Dans une conversation, vous pouvez aussi écrire pendant que l'agent **travaille**. Le message est **mis en file d'attente** : il apparaît sous forme de bulle en pointillés à la fin de la transcription, et l'agent
le lit dès que le tour en cours est terminé : la requête au modèle qui est en route et les appels qu'il a faits s'achèvent d'abord, et la requête suivante contient déjà
votre message (plusieurs messages en file sont envoyés ensemble). Lorsqu'il ne faut pas attendre même cela, appuyez sur le petit bouton **Envoyer maintenant** (éclair) de la bulle : l'agent abandonne le tour en cours (la requête au modèle est annulée, une commande qui attendait votre approbation
est refusée, les appels non effectués sont marqués comme ignorés) et lit votre message ensuite. Le bouton **corbeille** à côté permet de reprendre le message tant que l'agent ne l'a pas lu (de même pour une commande en file). Les commandes déjà exécutées restent faites. Les messages et commandes en file sont conservés lors d'un redémarrage du serveur (ils sont lus à la reprise), et
une exécution qui n'est pas une conversation (lancée *immédiatement*) n'accepte pas de messages pendant qu'elle travaille.
<a id="running-a-command-yourself"></a>

### Exécuter une commande vous-même

Sous la transcription se trouve la ligne de commande de l'application (celle de l'onglet de l'histoire : tapez `/`, la liste et l'aide des commandes apparaissent au-dessus) avec ce que vous pouvez lancer sur l'agent : `/compact`, `/clear-workspace` et les
[commandes que l'agent peut appeler](#allowed-commands) qui ne font que lire ou ajouter des données (une commande qui modifie ou supprime des données n'est pas proposée, elle demanderait une confirmation
dans une fenêtre du livre, ce qu'une exécution ne permet pas). C'est vous qui lancez la commande, pas l'agent : il est informé, dans un message de votre part, que vous l'avez lancée et de sa réponse
(et que ce qu'il a lu auparavant peut être périmé si la commande a modifié l'espace de travail), de sorte qu'il ne croit jamais avoir fait un appel qu'il n'a pas fait. Elle fonctionne comme un message :
lorsque l'exécution attend (*Vous attend*) ou est *En pause*, la commande s'exécute immédiatement et l'exécution reste en l'état ; lorsqu'une [conversation](#the-console) est en cours, elle est mise en file d'attente, sous la forme d'une bulle en pointillés marquée *Commande*, et s'exécute à la fin du tour de l'agent, là où l'agent aurait attendu (l'agent ne continue pas à cause d'elle).
Le petit bouton **Exécuter maintenant** de la bulle abandonne le tour en cours de l'agent, exécute la commande et laisse l'agent continuer à partir de là. Une commande longue
bloque la zone de saisie : attendez qu'elle se termine avant d'écrire à l'agent.

![Une commande de l'auteur dans la console : /compact](../images/agent-console-command.png)

**`/compact [ce sur quoi insister]`** libère le contexte. L'agent est invité à relire tout ce qui precede et à rédiger un résumé (ce que vous avez demandé, ce qui est fait, ce qu'il a trouvé,
ses notes et son plan, ce qu'il allait faire) ; ce résumé remplace tout ce qui précède dans ce que lit le modèle. Dans la transcription, vous voyez toujours tout, ainsi que le résumé sous forme de bloc replié, **Résumé du travail précédent** ; l'export le conserve aussi. Le texte qui suit la commande, s'il y en a un, est ce sur quoi le résumé doit insister.
La requête est celle de l'agent et ses jetons sont comptés dans l'exécution. La question posée à l'agent est son **Prompt de compactage** : celui de l'agent, sinon votre valeur par défaut dans les [Réglages](account.md#templates), sinon celui de l'administrateur, sinon celui intégré. Si le contexte est déjà si plein que la requête ne tient pas, les messages les plus anciens en sont retirés (le résumé indique combien). Faites-le avant que le contexte ne soit plein, la ligne d'utilisation indique à quel point il s'en approche. Juste après le compactage, le contexte affiché est une estimation de ce que la requête suivante contiendra (le résumé, le prompt système et les définitions des outils) ; la première réponse du modèle après cela remplace ce chiffre par le nombre réel.

**Contexte qui se remplit.** Dès que le contexte du modèle dépasse 75 % de sa limite, l'agent reçoit un avertissement après chaque tour (un message de l'exécution, que vous voyez dans la transcription) : son niveau de remplissage, et qu'il doit enregistrer ce qu'il ne doit pas perdre dans un fichier de son espace de travail, mettre son plan à jour et appeler `request_compaction`. Au-delà de 90 %, l'avertissement indique que l'exécution s'arrête lorsque le contexte sera plein. Si l'agent ne compacte pas avant que le contexte ne dépasse la part **Compacter à** de sa définition (90 % sauf indication contraire de votre part), l'exécution le fait d'elle-même : le même compactage, le résumé commence par « compacté automatiquement », et l'agent n'est pas averti en plus. Un compactage qui échoue ne change rien, l'agent en est informé, et l'exécution réessaie seulement quelques tours plus tard. Le compactage demandé par l'agent est le même que le vôtre (son **Prompt de compactage**, voir [Créer et modifier un agent](#creating-and-editing-an-agent), ses jetons sont comptés dans l'exécution) ; s'il échoue, rien n'est modifié et l'agent en est informé.
Les avertissements nécessitent une limite de contexte : la plus basse entre la limite de l'agent et les limites de son modèle.

**Un appel que le serveur du modèle n'a pas pu lire.** Lorsqu'un modèle écrit un long texte dans un appel, sa réponse peut s'arrêter au milieu du texte (la limite de *une réponse*, les **reply tokens** du protocole ou les **max completion tokens** de l'IA, est atteinte, une valeur par défaut du serveur s'applique lorsqu'aucune n'est définie, ou le modèle s'arrête simplement ; le contexte, même plein, est une limite différente), et le serveur refuse l'appel incomplet (« Failed to parse tool call arguments as JSON »). Redemander avec la même conversation produirait la même erreur ; l'exécution ne se contente donc pas de se mettre en pause : l'agent est informé dans la transcription que sa dernière réponse n'a pas pu être utilisée et que rien n'a été fait, et qu'il doit écrire les longs textes dans un fichier de son espace de travail en plusieurs étapes plus petites ; puis il est interrogé à nouveau, jusqu'à trois fois de suite. Si l'échec persiste, l'exécution se met en pause, et ce qui lui a été dit reste dans la transcription : un **Reprendre** pose donc une question différente de celle qui a échoué. Il en va de même pour un sous-agent.

Un appel coupé qui a tout de même atteint l'application (le résultat de l'appel indique « not valid JSON ... cut off in the middle ») reste dans la transcription tel que le modèle l'a écrit, pour que vous puissiez le lire, mais il est renvoyé au modèle avec des arguments vides : un serveur qui relit les appels précédents refuserait sinon toutes les requêtes suivantes, et l'exécution ne pourrait pas continuer sans un retour en arrière.

**Appels incontrôlés.** Un modèle peut tomber dans une boucle et écrire le même appel des centaines de fois dans une seule réponse ; les résultats rempliraient le contexte avant la requête suivante. L'exécution effectue au plus 32 appels par réponse, et une répétition d'un appel qui ne fait que lire (le même outil avec les mêmes arguments, dans la même réponse) n'est effectuée qu'une fois. Les appels écartés ne figurent pas dans la transcription, et l'agent reçoit un avertissement qui le signale.

**Tourner en rond.** Un modèle peut aussi se répéter sur de nombreuses réponses : la même consultation (lister les fichiers, lister les lorebooks...) avec le même résultat, tour après tour, sans avancer. L'exécution surveille cela. Lorsque l'agent enchaîne des tours qui ne font que consulter, ne changent rien et sont identiques aux précédents (mêmes appels, mêmes résultats), il est informé au troisième que rien n'a changé. S'il continue, au cinquième ses outils lui sont retirés pour une requête : il doit répondre en texte clair à ce qu'il faisait et à ce qui le bloque, et l'exécution se **met en pause** avec cette réponse comme dernier message, afin que vous voyiez pourquoi il était bloqué au lieu de payer une centaine de tours supplémentaires. **Reprendre** (ou lui écrire) rend les outils et recommence le décompte à zéro. Un tour au cours duquel quelque chose a changé, ou pendant que des sous-agents travaillent, n'est pas compté. Un sous-agent qui se répète est arrêté de la même façon, et ce qu'il a dit constitue sa réponse à l'agent qui l'a lancé.

**`/clear-workspace [ce à conserver]`** supprime les fichiers et tous les dossiers de l'[espace de travail](#the-working-copy) de l'exécution (après confirmation, comme avec le bouton de l'espace de travail). Indiquez après la commande ce qu'il faut conserver, séparé par des espaces : des fichiers, des dossiers (avec tout ce qu'ils contiennent) ou des motifs tels que `*.md` ou `chapters/*.md`, un chemin contenant des espaces se met entre guillemets doubles
(`/clear-workspace /notes.md "/mes brouillons" *.png`). Les dossiers au-dessus d'un fichier conservé restent. Si un élément nommé n'existe pas, rien n'est supprimé. `/AGENTS.md` et les points de contrôle ne font pas partie de l'espace de travail et ne sont jamais touchés. L'agent est informé de ce qui a été supprimé et de ce qui a été conservé. Dans la transcription, une commande que vous avez lancée apparaît sous forme de bulle avec la commande et sa réponse.

Lorsque le modèle de l'agent voit les images (voir [Créer et modifier un agent](#creating-and-editing-an-agent)), **Joindre une image** se trouve à côté de **Envoyer** : choisissez un fichier PNG, JPEG, GIF ou WebP, ou collez une image depuis le presse-papiers
dans la zone de message (**Ctrl+V**) ; elle attend au-dessus de la zone (la croix la retire) jusqu'à votre envoi. Jusqu'à huit images accompagnent un message ; le texte peut être vide.
Les images sont enregistrées dans le dossier **uploads** de l'espace de travail (un nom déjà pris reçoit un numéro : `map-2.png`), dans les limites et règles de l'espace de travail,
et le modèle les voit avec votre message. Supprimez-les dans l'onglet **Espace de travail** lorsque vous n'en avez plus besoin.

Les boutons en bas sont **Pause**, **Reprendre** et **Arrêter**, qui font ce que décrit le tableau, et **Terminer**, qui met fin à une exécution qui vous attend (son travail est terminé, *Fait*).

La fenêtre consulte l'exécution toutes les deux secondes tant qu'elle est ouverte ; **Actualiser** le fait immédiatement. L'onglet **Exécution** affiche la dernière exécution non abandonnée, de sorte qu'une exécution terminée ou échouée reste consultable là jusqu'à ce que l'agent démarre une nouvelle exécution ; les plus anciennes se trouvent dans **Exécutions**.

**Exécutions** est l'historique de l'agent : chaque exécution qu'il a eue, la plus récente en premier, avec sa date, son statut, ses tours, ses jetons et son résultat. Utilisez l'œil pour **ouvrir** une exécution dans l'onglet **Exécution** et relire ses étapes et son rapport (l'exécution ouverte y reste jusqu'à ce que vous en ouvriez une autre), la flèche pour **télécharger** sa transcription sous forme de fichier Markdown (ce qui a été dit et quand, chaque commande avec ses arguments et son résultat, le rapport, les conversations des sous-agents), et la corbeille pour la **supprimer**.
Une exécution supprimée passe dans la [corbeille](trash.md). Une exécution en cours ne peut pas être supprimée : arrêtez-la d'abord. Les exécutions que vous avez arrêtées et **abandonnées** figurent aussi dans la liste, afin que vous puissiez lire ce qu'elles ont fait ou les supprimer ; l'onglet **Exécution** ne les affiche que lorsque vous les ouvrez.

![L'onglet Exécutions](../images/agent-runs.png)

**Espace de travail** affiche les fichiers de l'agent : ses notes et ses rapports, qu'il conserve d'une exécution à l'autre. Cliquez sur un dossier pour y entrer (**Haut** permet de revenir), cliquez sur un fichier texte pour le lire, utilisez les flèches pour télécharger un fichier ou la corbeille pour le supprimer, et **Effacer** supprime tout (des notes périmées induisent en erreur une nouvelle exécution). **Nouveau dossier** crée un dossier dans celui qui est ouvert, et **Téléverser un fichier** y place un fichier de votre ordinateur, afin que vous puissiez remettre à l'agent
un cahier des charges, un guide de style ou un brouillon. Les mêmes règles valent que pour les fichiers propres à l'agent : les noms ne peuvent pas contenir `\ < > : " | ? *`, ni un point ou un espace final, ni être trop longs ou trop profonds ; les limites de taille de fichier et de nombre de fichiers s'appliquent ; un fichier téléversé doit être un texte (UTF-8), car l'agent ne peut lire que du texte, une image (PNG, JPEG, GIF ou WebP, reconnue à son contenu et non à son nom ;
jusqu'à 5 Mo, et elle compte dans le total et le nombre de fichiers comme tout autre fichier) ou une archive zip (jusqu'à 10 Mo, conservée telle quelle ; l'agent la décompresse, voir [plus bas](#unpacking-a-zip-file)). Un fichier n'est jamais remplacé par un téléversement : supprimez d'abord l'ancien.
Les fichiers ne peuvent pas être modifiés pendant que l'agent travaille ; mettez-le d'abord en pause. L'espace de travail a une limite de taille, de taille de fichier et de nombre de fichiers, que votre administrateur peut définir dans votre [permission](administration/permissions.md#settings-of-a-permission) ; la ligne sous le tableau indique la part utilisée. Le nom `AGENTS.md` en haut est réservé à vos instructions, que l'agent y voit (le fichier ne peut être ni écrit ni supprimé).

![L'onglet Espace de travail](../images/agent-console-workspace.png)
<a id="unpacking-a-zip-file"></a>

### Dézipper une archive

Téléversez une archive zip dans l'onglet **Espace de travail** (**Téléverser un fichier**) lorsque l'agent doit travailler sur de nombreux fichiers à la fois, par exemple les chapitres d'un brouillon, puis demandez-lui de la dézipper : l'agent dispose de l'outil `unzip_file`, qui décompresse `/uploads/draft.zip` dans le nouveau dossier `/uploads/draft` (ou dans un dossier qu'on lui indique ; le dossier ne doit pas encore exister). La décompression se fait tout ou rien, et une archive n'est jamais considérée comme fiable :

- Seuls les fichiers texte (UTF-8), les images (PNG, JPEG, GIF, WebP) et les archives zip sont conservés. Les autres entrées sont laissées de côté et nommées dans la réponse donnée à l'agent. Une archive zip contenue dans l'archive est enregistrée telle quelle, jamais dézippée à son tour.
- Les fichiers doivent tenir dans la place disponible de l'espace de travail (taille, taille par fichier, nombre de fichiers), sinon rien n'est décompressé.
- Une archive contenant une entrée qui remonte au-dessus du dossier (`../x`) ou qui commence par `/` est refusée en entier, de même qu'une archive contenant deux fois le même nom (même en ne différant que par la casse), un nom qui est à la fois un fichier et un dossier, plus de 5 000 entrées, ou une archive chiffrée ou endommagée.
- Une bombe zip est refusée : une archive qui se décompresse à plus que ce que l'espace de travail peut contenir, ou à plus de 100 fois sa propre taille.

<a id="sub-agents"></a>

## Sous-agents

Un agent peut confier une tâche à un **sous-agent** : un assistant doté d'une conversation propre, qui accomplit une seule tâche et répond par un texte. Il dispose des commandes et des fichiers de l'agent (ou de ceux du modèle sur lequel il tourne, voir [plus bas](#models-for-sub-agents)) et travaille dans la même copie de travail et le même espace de travail (ce qu'il modifie dans le livre reste), mais il ne connaît pas la conversation de l'agent : l'agent doit donc tout dire dans la tâche. L'intérêt est que ce que le sous-agent lit et essaie reste hors du contexte de l'agent : relire un chapitre, vérifier les noms, rédiger une partie.

Réglez **Nombre maximal de sous-agents** dans l'[agent](#creating-and-editing-an-agent) sur le nombre de sous-agents qui peuvent travailler en même temps. Zéro (la valeur par défaut) signifie aucun : l'agent n'est même pas informé de leur existence.

- Avec **un**, l'agent lance un sous-agent et attend (il est *mis en attente*) jusqu'à ce qu'il ait fini ; l'appel renvoie sa réponse.
- Avec **plusieurs**, lancer un sous-agent rend la main aussitôt, et l'agent peut poursuivre, en lancer d'autres, les consulter (`agent_status`), attendre une réponse (`await_agent`) ou en arrêter un (`cancel_agent`). L'appel qui atteint la limite attend qu'un des sous-agents se termine, et renvoie ceux qui se sont terminés.

Règles valables dans les deux cas :

- Un sous-agent **lit l'espace de travail en entier mais n'écrit que dans un dossier à lui**, `/tmp/sub1`, `/tmp/sub2`… (il est créé au lancement du sous-agent, et ce qu'une exécution précédente a laissé dans un dossier de même nom est supprimé). Sa réponse y est enregistrée sous `result.md` : lorsqu'une longue réponse est affichée tronquée, l'agent lit l'intégralité dans ce fichier. Vous trouvez ces fichiers dans l'onglet **Espace de travail** ; ils comptent dans le quota de l'espace de travail et restent jusqu'à ce que vous le vidiez.
- Un sous-agent tourne sur le modèle principal de l'agent, sauf si l'agent en demande un autre, voir [Modèles des sous-agents](#models-for-sub-agents).
- Une commande d'un sous-agent qui modifie des données suit le réglage **Commandes destructrices** de l'agent : avec *Demander d'abord*, la même [fenêtre](#asking-you) vous interroge, et ce sous-agent attend votre réponse (la mise en pause de l'exécution la refuse).
- Un sous-agent ne peut pas **lancer de sous-agents**, terminer l'exécution, vous poser une question ni créer de points de contrôle ; il se termine en répondant par un texte.
- Tout compte dans le **budget unique de l'exécution** : tours, jetons, limites du modèle. Un sous-agent qui atteint une limite échoue et le signale ; l'agent atteint alors la même limite.
- Le nombre de requêtes envoyées au modèle en même temps est celui de **Requêtes au modèle en même temps** que votre administrateur vous a donné (un si rien n'est défini) : avec un seul, l'agent et son sous-agent se relaient.
- Les appels qui modifient le livre ou l'espace de travail se font un par un, quel que soit celui qui les fait, de sorte que deux sous-agents n'écrivent jamais au même instant. Évitez toutefois de demander à deux d'entre eux de modifier la même partie.
- **Mettre l'exécution en pause met aussi les sous-agents en pause.** Ils s'arrêtent à leur prochain point stable (entre deux requêtes ou deux appels, aussitôt si vous mettez en pause aussitôt) et ce qu'ils ont dit et fait est conservé avec l'exécution : une **Reprendre** repart donc de l'état où tout se trouvait, les sous-agents poursuivent leur conversation, et un appel de l'agent qui attendait l'un d'eux (`await_agent`, ou `spawn_agent` avec une limite d'un) attend à nouveau, comme si de rien n'était. Un sous-agent déjà terminé avant la pause reste accessible pour que l'agent puisse l'interroger. Arrêter une exécution en la conservant fait de même ; l'abandonner, restaurer un point de contrôle (le livre dans lequel travaillaient les sous-agents est remplacé), ou la fin de l'agent ou sa question à votre intention les achèvent définitivement. Un agent qui termine son tour alors que des sous-agents travaillent encore en est averti une fois ; s'il recommence, ils sont arrêtés.
- Leurs conversations ne figurent pas dans l'onglet **Exécution**. L'onglet **Sous-agents** de la console les affiche : choisissez-en un ci-dessus pour lire ce que l'agent lui a dit, ce qu'il a répondu, les commandes appelées avec leurs résultats et la façon dont il s'est terminé (la réponse donnée, ou la raison de l'échec ou de l'annulation), dans les mêmes blocs que la transcription. Il suit l'exécution pendant qu'elle travaille, et il affiche aussi les sous-agents d'une exécution plus ancienne (sélectionnez d'abord cette exécution dans l'onglet **Exécutions**). Un appel `spawn_agent` dans l'onglet **Exécution** possède un bouton **Sous-agent** qui ouvre sa conversation. Une conversation dont l'agent est revenu en arrière (un retour) est grisée et marquée *oublié par l'agent*. Le **téléchargement** de l'exécution (dans l'onglet **Exécutions**) comporte une section pour chaque sous-agent, avec sa tâche, son état et chaque message et appel.

![L'onglet Sous-agents](../images/agent-subagents.png)

<a id="models-for-sub-agents"></a>

### Modèles des sous-agents

L'agent a un **modèle principal**, choisi en haut de la boîte de dialogue. Sous **Modèles des sous-agents**, vous pouvez en ajouter d'autres, par exemple un modèle économique qui ne fait que lire, ou un modèle fluide qui rédige. Chacun a un **rôle** (son nom), une **description** (quand l'utiliser : l'agent la lit pour choisir), son IA et son protocole, une **limite de contexte** facultative, et ce qu'il a le droit d'utiliser :

![Un modèle pour sous-agents](../images/agent-models.png)

- **Peut utiliser des commandes et des outils.** Désactivé, un sous-agent sur ce modèle ne répond que par du texte : il n'a ni commandes ni outils.
- **Limiter les commandes.** Désactivé, un sous-agent sur ce modèle dispose des commandes de l'agent. Activé, seules celles que vous cochez, et parmi celles-ci seulement celles que l'agent est autorisé à utiliser (les commandes autorisées de l'agent restent la limite supérieure). Les outils pour les fichiers ne sont pas des commandes : chaque sous-agent les possède. Le modèle principal a le même réglage, pour les sous-agents qui tournent sur lui (le bouton **Modifier** de sa ligne).

L'agent est informé des rôles, des descriptions et des commandes de chaque modèle, et lance un sous-agent sur l'un d'eux avec le `profile` de `spawn_agent` ; s'il l'omet, c'est son propre modèle qui est utilisé. La case **Les sous-agents peuvent tourner sur le modèle principal** (activée par défaut) désactive ce dernier choix : l'agent doit alors nommer un autre modèle, et s'il n'en existe aucun, il ne reçoit pas du tout d'outils pour les sous-agents. Un sous-agent a les limites de son propre modèle (la quantité qu'il peut lire et la durée de sa réponse), et l'onglet **Sous-agents** ainsi que les diagnostics indiquent sur quel modèle chacun a tourné.

<a id="one-model-several-agents"></a>

## Un modèle, plusieurs agents

Un serveur local dispose souvent d'un seul emplacement (un seul GPU) : lui demander deux réponses à la fois fait ralentir les deux, voire expirer. Les **Connexions** du [modèle](inference-providers.md#general-settings) (une seule, sauf si vous en réglez davantage) font passer chaque requête à son tour, dans l'ordre d'arrivée : celles de vos agents, celles des agents d'autres utilisateurs, et votre propre écriture dans la fenêtre de l'histoire. Une exécution qui attend reste *En cours*, et une **Pause** ou un **Arrêter** met fin à l'attente aussitôt. Entre deux réponses du modèle, un agent ne garde aucune connexion : votre génération peut donc s'intercaler.
<a id="when-you-are-not-there"></a>

## Quand vous n'êtes pas là

Un agent qui a besoin de vous, qui termine ou qui échoue l'indique aux écrans ouverts de l'application (le badge sur l'onglet, un avis). Quand aucun de vos écrans n'est ouvert, il peut aussi envoyer un court message à une adresse que vous donnez dans [Réglages](account.md), si un administrateur vous a accordé la [permission *Notifications*](administration/permissions.md) (les administrateurs l'ont) (« Message vers une URL quand un agent a besoin de vous ») : votre sujet **ntfy** (l'application ntfy du téléphone l'affiche aussitôt), une URL de message **Gotify** ou un webhook **Discord** ; choisissez le **Format** que demande le service. **Envoyer un message de test** l'essaie avec ce qui se trouve dans les champs. Le message nomme l'agent et dit ce qu'il attend (« attend votre réponse », « terminé », « demande l'approbation de la commande ... », « échoué », « mis en pause : raison ») dans votre langue. **Aussi quand l'application est ouverte** l'envoie dans tous les cas.

C'est le serveur qui appelle l'adresse ; un administrateur peut donc désactiver cette fonction pour tout le monde (Admin, *Agents*), et seules les adresses sur internet sont appelées, sauf si l'administrateur a aussi coché **Autoriser les adresses de cette machine et des réseaux privés** dans votre permission : pour un ntfy ou un Gotify sur la même machine ou sur votre propre réseau, demandez-le. Le message de test indique quand l'adresse est refusée. Traitez l'adresse comme un mot de passe lorsqu'elle contient un secret : quiconque la connaît peut lire ce qui est envoyé, le serveur ne l'écrit jamais dans son journal, les redirections ne sont pas suivies, et seuls les protocoles `http` et `https` sont appelés.

<a id="asking-you"></a>

## Vous demander une réponse

Quand l'agent a besoin de vous, vous en êtes informé où que vous soyez dans l'application :

- un **nombre sur l'onglet Agents** compte les agents qui attendent votre réponse, et le bouton **Console** d'un tel agent est mis en évidence ;
- un **avis** apparaît avec un bouton **Ouvrir** qui ouvre la console de cet agent.

L'agent a besoin de vous lorsqu'il *vous pose une question ou attend votre prochain message*, qu'il vous demande d'**approuver une commande**, lorsqu'il a *terminé*, lorsqu'il a *échoué*, ou lorsqu'il s'est *mis en pause de lui-même* (une limite est atteinte, le modèle n'a pas pu être sollicité, le serveur a redémarré). Une pause que vous avez demandée ne compte pas.

Les agents travaillent pendant votre absence : cela ne dépend donc pas de l'ouverture de l'application. Le nombre est toujours là lorsque vous vous reconnectez, et chaque fenêtre du navigateur ouverte reçoit l'avis. Ouvrir la console de l'agent efface l'avis (les autres fenêtres se mettent à jour).

Lorsque l'agent est réglé sur **Demander d'abord** et qu'il veut lancer une commande qui modifie ou supprime des données, l'exécution s'arrête en *En attente d'approbation* et une fenêtre s'affiche au-dessus de la console, avec la commande et ses arguments. **Approuver** la laisse s'exécuter ; **Refuser** indique à l'agent que vous avez refusé, et il poursuit. Mettre en pause ou arrêter l'exécution (ou arrêter le serveur) ne compte pas comme un refus : l'agent n'en est pas informé, et lorsque vous **Reprendre**, la fenêtre réapparaît pour la même commande. La commande fonctionne toujours sur la [copie](#the-working-copy) du livre. Si la console est fermée, la fenêtre est là dès que vous l'ouvrez.

![La fenêtre qui demande l'approbation d'une commande](../images/agent-approval.png)

<a id="when-the-server-stops"></a>

## Quand le serveur s'arrête

Arrêter le serveur met en pause les exécutions qui travaillent, comme le fait **Mettre en pause maintenant** (il laisse au serveur quelques secondes) : la requête et l'appel en cours sont abandonnés, l'exécution est *En pause* avec la raison « Le serveur a été arrêté », et un **Reprendre** permet de continuer. Si le serveur est tué ou plante, le prochain démarrage met en pause les exécutions qu'il trouve en cours de travail (« Interrompue par un redémarrage du serveur »). Tout ce que l'exécution a fait est conservé : les tours enregistrés, la copie, l'espace de travail, ainsi que les messages et commandes que vous aviez mis en file d'attente. Seul le tour non terminé est perdu. La reprise traite l'appel en cours de l'exécution :

- Un appel **qui n'avait pas commencé** (il attendait votre approbation, ou son tour après un autre appel) est refait, de sorte qu'une commande qui demande votre approbation vous la redemande. Un appel qui ne fait que lire est lui aussi refait.
- Un appel **qui avait commencé à modifier quelque chose** a pu être réalisé en partie : l'agent est informé que son résultat est inconnu et vérifie avec un outil de lecture avant de le répéter ; les appels qui suivent sont marqués comme non effectués.
- Une commande de votre part qui était en cours d'exécution lorsque le serveur s'est arrêté se poursuit avec l'exécution. `/compact` et `/clear-workspace` sont relancées ; toute autre commande déjà commencée n'est **pas** relancée (elle a pu être réalisée en partie) : l'agent en est informé et vérifie. Les messages et commandes qui arrivent ensuite sont transmis normalement.
- Le plan de l'agent et les résultats de ses commandes (`list_command_results`, `@result`) sont eux aussi conservés, de sorte que l'agent les retrouve après le redémarrage.
- Les appels d'un sous-agent qui étaient en vol sont traités comme ceux de l'agent : ceux qui n'avaient pas commencé sont refaits, celui qui avait commencé est signalé à celui-ci comme « résultat inconnu », et un sous-agent qui attendait votre approbation la redemande.
- Si le serveur est **tué** (`kill -9`, coupure de courant) pendant qu'une commande s'exécute, la commande meurt avec lui ; ce qu'elle a fait est décrit ci-dessus.

<a id="the-working-copy"></a>

## La copie de travail

Chaque exécution travaille sur une **copie de votre livre** et de ses lorebooks que l'application crée au démarrage de l'exécution : les messages, résumés, étiquettes et réglages tels qu'ils sont à ce moment-là, ainsi que le lorebook et ses sous-lorebooks. La copie est cachée : elle n'apparaît pas dans les listes de livres et de lorebooks. Quoi que fasse l'agent, il le fait sur la copie ; votre livre reste tel qu'il était.

Lorsque l'agent a **Écrire le livre avec le modèle de l'agent** activé (par défaut), la copie reçoit aussi le **modèle de l'agent** (l'IA et le protocole de son profil de modèle principal) à la place de l'IA et du protocole de votre livre, à chaque démarrage ou reprise de l'exécution. Ainsi, les commandes qui écrivent avec un modèle (`/next`, `/swipe`, `/regenerate`, `/redraft-part`, `/generate-annotation`, les résumés) et les commandes de modèle des extensions (le Lore Generator, le Arc Outline) utilisent le modèle de l'agent, même lorsque votre livre est réglé sur un autre modèle ou sur aucun ; un `profile` nommé dans l'appel reste prioritaire. Avec ce réglage désactivé, elles utilisent le modèle du livre. Il ne s'agit pas d'une modification de votre livre : la relecture ne la liste pas, et l'application du résultat laisse l'IA et le protocole de votre livre tels quels.

La copie est placée dans le [dossier](books/folders.md) de votre livre, de sorte que l'agent écrive avec les réglages que fixent les dossiers (langue, modèles, point de vue, temps, style et prompts), comme le fait le livre. La copie ne fait pas partie du contenu du dossier : elle n'y est pas comptée, et le travail réalisé dans cette copie ne modifie pas la *Dernière modification* du dossier. La copie suit le livre si vous le déplacez pendant que l'exécution est en pause. L'application du résultat ne déplace jamais votre livre.

Une exécution en pause conserve sa copie et retravaille dessus lorsque vous la reprenez. Une exécution *Fait*, ou que vous avez arrêtée et conservée, garde sa copie jusqu'à ce que vous [repreniez son résultat ou le rejetiez](#reviewing-the-result). Lorsque l'exécution est *Échouée*, que vous rejetez son résultat, ou que ses modifications sont appliquées, la copie est supprimée définitivement, et l'exécution suivante en crée une nouvelle à partir du livre tel qu'il est alors.
<a id="resuming-a-run"></a>

## Reprise d'une exécution

Une exécution en pause ou gardée ne verrouille pas votre livre : vous pouvez donc le modifier entre-temps. Quand vous **reprenez** l'exécution, sa copie de travail rattrape ce que vous avez fait :

- Ce que vous avez modifié et que l'agent n'a pas modifié (une partie, une entrée de livre de références, un réglage, une étiquette...) entre dans la copie. Une partie que vous avez ajoutée y entre aussi, et l'agent peut la poursuivre ; ce que vous avez supprimé est supprimé de la copie.
- Ce que vous et l'agent avez modifié différemment, avec des textes différents, <strong>reste tel que l'agent l'a</strong> : rien de ce que l'agent a fait n'est perdu. C'est un [conflit](#reviewing-the-result) que vous résolvez lors de l'examen du résultat, avec les deux textes sous les yeux.
- Ce que vous avez modifié et que l'agent a supprimé reste supprimé, et ce que vous avez supprimé et que l'agent a modifié reste dans la copie ; l'examen montre les deux.

Aucun clic n'est nécessaire : la synchronisation fait partie de la reprise et prend un moment (quelques secondes quand vous avez modifié des centaines d'éléments). L'état de la copie avant la synchronisation est enregistré dans un [point de contrôle](#checkpoints) automatique. L'agent est informé de ce qui s'est passé, avec les noms par lesquels il connaît les éléments, dans un message au moment de reprendre (« Pendant la pause, l'utilisateur a modifié le livre... »), et il peut redemander avec `get_sync_report`.

**Un conflit ne disparaît pas tout seul.** Chaque reprise compare de nouveau la copie avec le livre ; un conflit est donc signalé à nouveau, et l'agent en est informé à nouveau, à chaque reprise, jusqu'à ce que vous le résolviez : en modifiant votre côté du livre (pour mettre le texte de l'agent, ou revenir à ce qu'il était), ou lors de l'examen.

**Une note avec la reprise.** **Reprendre** ouvre une petite fenêtre avec un champ *Note pour l'agent (facultative)*. Ce que vous y écrivez est la première chose que l'agent lit en repartant, après le compte rendu des modifications que vous avez faites dans le livre. Elle sert à un changement de direction (« ne touche pas au livre de références », « commence par le chapitre 3 »). Contrairement à un message dans une conversation, elle fonctionne pour toute exécution, y compris une exécution qui n'attend pas vos messages. Laissez-la vide pour reprendre comme avant.

![Reprise d'une exécution avec une note](../images/agent-resume.png)

<a id="reviewing-the-result"></a>

## Examiner le résultat

Quand une exécution est *Fait* (ou que vous l'avez arrêtée avec **Arrêter et garder le résultat**), la ligne de l'agent et la console ont un bouton **Examiner les modifications**. Il ouvre une fenêtre avec tout ce que l'agent a modifié, comparé à votre livre **tel qu'il est maintenant**, une ligne par élément. Quand l'agent a signalé des constats, ils sont listés au-dessus des modifications (**Constats (3)**), de sorte qu'un audit qui n'a rien modifié a tout de même quelque chose à montrer :

![Examen du résultat d'une exécution](../images/agent-review.png)

- **Livre :** son nom, sa description, sa langue, son point de vue, son temps, son style, ses modèles, le nombre de versions conservées par partie, ses étiquettes, et les données qu'une extension conserve dans le livre (un plan d'arc, le suivi des personnages).
- **Partie :** une partie dont l'agent a modifié le texte, les consignes, le cadre de la scène, le personnage au point de vue, les personnages présents ou le résumé, une partie qu'il a écrite, une partie qu'il a supprimée. Les variables qu'une génération conserve avec une partie (et les données des extensions) sont reprises avec elle, dans le champ **Données**.
- **Livre de références** et **Entrée :** un livre de références ou une entrée que l'agent a créé, modifié (aussi déplacé, renommé, ou avec d'autres étiquettes, ou d'autres sous-livres) ou supprimé.
- **Annotation :** une [note](books/annotations.md#annotations-and-agents) sur un passage d'une partie que l'agent a créée, modifiée (la note, le type, l'état) ou supprimée.

Cochez les lignes que vous voulez, puis cliquez sur **Appliquer les modifications choisies**. Les boutons au-dessus de la liste cochent **toutes** les lignes, **aucune**, **seulement les nouvelles parties** (la nouvelle branche écrite par l'agent, rien d'autre), **seulement les annotations** ou tout **sans les conflits**. Une ligne qui en nécessite une autre (une nouvelle partie a besoin de la partie qui la suit, si celle-ci est nouvelle aussi) entraîne cette dernière quand vous la cochez, et perd ses dépendantes quand vous la décochez. Cliquez sur une ligne pour la voir à droite : le texte de votre livre actuellement et le texte qui sera écrit, **que vous pouvez modifier** avant d'appliquer. Si vous n'écrivez rien, le texte de l'agent est appliqué.

Votre livre a peut-être changé depuis le début de l'exécution (vous pouvez le modifier dès que l'exécution ne travaille plus). Ce que vous seul avez modifié ne figure pas dans la liste et reste en place. Ce que vous et l'agent avez modifié pour obtenir le même résultat ne constitue aucune modification. Ce que vous avez modifié chacun en un texte **différent** est un **conflit** : il commence décoché, et quand vous le cochez, le texte que vous voyez à droite remplace le texte du livre. **Utiliser celle du livre** met le texte du livre dans la zone d'édition (la ligne ne change alors rien pour ce champ), **Utiliser celle de l'agent** y remet celui de l'agent ; vous pouvez aussi écrire le résultat vous-même. Une ligne qui ne peut pas être reprise (la partie qui la suit, ou le livre de références où elle se trouve, a été supprimée de votre livre) l'indique et ne peut pas être cochée.

![Un conflit dans l'examen](../images/agent-review-conflict.png)

Sous les textes, **Différences** montre ce qui change **mot à mot** : ce qui n'est que dans le livre actuel est barré en rouge, ce qui n'est que dans le texte à appliquer est en vert (il suit ce que vous écrivez dans la zone). Pour un conflit, il montre aussi ce que l'agent a modifié et ce que vous avez modifié depuis le début de l'exécution, chacun à partir du livre tel qu'il était alors, pour voir qui a fait quoi. Une ligne que l'agent a modifiée sur plusieurs points (une partie dont le texte et le cadre de la scène ont changé, une entrée avec un nouveau contenu et une nouvelle note) a une case **Prendre cette modification** à côté de chacun d'eux : décochez-en une pour appliquer la ligne sans elle, le livre garde alors sa propre valeur de ce champ. La place d'une partie dans l'histoire suit la partie et n'a pas de case ; ce que l'agent a créé ou supprimé est pris ou laissé en bloc. **Afficher dans la copie** (sur une ligne créée ou modifiée par l'agent) ouvre la [copie du livre](#the-copy-of-the-book) à cette partie ou à cette entrée, pour la lire à sa place.

![Les différences d'un conflit, mot à mot](../images/agent-review-differences.png)
<a id="data-that-extensions-keep-json"></a>

### Données que les extensions conservent (JSON)

Les extensions conservent leurs propres données dans un livre, dans ses parties, ses livres de référence et ses entrées (un plan d'arc, le suivi des personnages, les variables d'une génération). Il s'agit de JSON, et la revue l'affiche **mis en forme** avec, à côté du texte qui sera appliqué, la ligne
**Fusionner les données JSON**. Les données ne sont pas reprises en bloc : ce que seul l'agent a modifié est repris de l'agent, ce que seul votre livre a modifié est conservé, et ce que les deux ont modifié à **des endroits différents** est fusionné endroit par endroit. Il n'y a conflit que si les deux ont modifié le **même** endroit avec des valeurs différentes (l'ordre des clés n'a pas d'importance) ; tant que vous n'avez pas tranché, c'est la version de l'agent qui est retenue à cet endroit.

**Fusionner les données JSON** ouvre une fenêtre qui présente les données des trois versions superposées sous forme d'arbre : *Au début de l'exécution*, *Dans le livre actuellement*, l'*Agent* et le *Résultat*, chacun dans une colonne, avec une marque indiquant ce qui a changé et où (l'agent, le livre, les deux, un conflit). Seuls les endroits modifiés sont affichés (décochez **Seulement ce qui a changé** pour tous les voir) ; un endroit ne s'ouvre que lorsque vous l'ouvrez, si bien qu'un gros document ne pose aucun problème.

![Les données d'une extension superposées](../images/agent-json-merge.png)

- Sélectionnez un endroit et prenez ses données d'une version (**Utiliser celle de l'agent**, **Utiliser celle du livre**, **Utiliser celle du début de l'exécution**), **Omettre** (la clé ou l'élément ne figure pas dans le résultat), ou **Fusionner à partir de l'intérieur** (les endroits contenus décident, chacun pour soi). Une clé ou un élément qu'une seule version possède est copié dans le résultat en le prenant de cette version. Prendre un objet ou une liste entière d'une version annule ce que vous avez choisi à l'intérieur ; ce n'est pas définitif : fusionnez à nouveau à partir de l'intérieur et vos choix reviennent.
- Les éléments d'une **liste** n'ont pas de noms, ils sont donc associés selon ce qu'ils sont : d'abord les données identiques, puis le même `uuid`, `id` ou `name`, puis ceux qui se ressemblent le plus ; ce qui reste, en même nombre et de même nature, est considéré comme modifié. Si la supposition est fausse, **Séparer** un élément en ceux de chaque version, ou **Associer à…** un autre élément avec lequel le premier n'a aucune version en commun.
- **Ouvrir** (ou un double-clic) ne travaille que sur un seul endroit : le chemin au-dessus de l'arbre vous ramène en arrière.
- L'onglet **Texte** montre les données de l'endroit ouvert sous forme de texte : celles de l'agent que vous pouvez **modifier**, les autres seulement à lire (le livre, le début de l'exécution, le résultat), et **Afficher la différence avec** marque ce qui diffère mot à mot. Ce que vous écrivez est vérifié comme JSON lorsque vous revenez à l'arbre, changez ce qui est affiché, ouvrez un autre endroit ou prenez le résultat ; s'il n'est pas du JSON valide, vous restez où vous êtes et l'on vous indique le problème, sans rien modifier. Un texte valide remplace les données de l'agent à cet endroit, et tout ce qui s'y trouve est comparé à nouveau.
- **Recommencer** oublie ce que vous avez décidé et écrit ; **Prendre le résultat** transmet le résultat à la revue, où il constitue le texte à appliquer (la ligne reste telle que vous l'avez laissée tant que la revue est ouverte).

La revue propose aussi **Utiliser celle de l'agent** et **Utiliser celle du livre** pour un tel champ, afin de reprendre telle quelle l'une des deux versions, et **Recommencer** pour revenir aux données fusionnées.

![Le plan d'arc dans la revue, avec Fusionner les données JSON](../images/agent-review-json.png)

Un livre de référence utilisé par d'autres livres est marqué **Partagé** : la modification va dans le livre de référence lui-même, si bien que les autres livres la reçoivent aussi. Elle n'est pas cochée au départ.

L'application se fait **sur place** dans le livre : les parties conservent leurs adresses, les autres livres gardent leurs liens vers un livre de référence, et ce que l'agent a supprimé part à la corbeille. Une **sauvegarde du livre tel qu'il était** est faite au préalable (elle figure parmi les sauvegardes du livre, avec ses livres de référence), et les modifications sont faites toutes ensemble ou pas du tout. **Afficher la branche où l'agent s'est arrêté** bascule aussi le livre sur la branche où se trouvait l'agent, lorsqu'elle fait partie de ce que vous avez appliqué. Les résumés des parties modifiées sont périmés, comme après une modification à la main ; les résumés écrits par l'agent lui-même sont repris tels quels.

Lorsque les modifications sont appliquées, l'exécution est *Appliquée* et sa copie ainsi que ses points de contrôle sont supprimés. **Abandonner le résultat** jette la copie et laisse le livre tel qu'il est. Tant que vous n'avez pas fait l'une de ces deux actions, l'agent a une exécution inachevée et ne peut pas en démarrer une autre.

<a id="the-copy-of-the-book"></a>

### La copie du livre

L'onglet **Copie du livre** montre le livre sur lequel travaille l'agent, en lecture seule : l'**histoire**, une branche à la fois (choisissez la branche au-dessus ; chaque partie avec son numéro, le swipe qu'elle représente parmi les autres après la même partie, ses mots, le cadre de la scène, le point de vue et, replié, ses instructions et son résumé), et les **livres de référence** avec leurs entrées (choisissez-en une pour voir son contenu, sa note, ses étiquettes et ses réglages). Ce n'est pas une fenêtre de votre livre : rien n'y peut être modifié, aucune sauvegarde n'est faite de la copie et aucune extension ne voit un livre ouvert. Elle est lue à l'ouverture de l'onglet et lorsque vous appuyez sur **Actualiser**, y compris pendant que l'agent travaille, si bien qu'elle montre ce que l'agent a écrit jusque-là. Elle est disponible tant que l'exécution dispose de sa copie (pas après que le résultat a été appliqué ou abandonné).

![L'onglet Copie du livre](../images/agent-book-copy.png)

### Diagnostics

L'onglet **Diagnostics** compte ce que l'exécution a fait d'après sa transcription, afin de savoir ce dont un agent a besoin de ses outils et de son prompt. Il fonctionne pour toute exécution, même ancienne (choisissez-la d'abord dans **Exécutions**), et il est établi à l'ouverture de l'onglet et lorsque vous appuyez sur **Actualiser**.

![L'onglet Diagnostics](../images/agent-diagnostics.png)

- **Vue d'ensemble** : tours et appels, nombre d'appels échoués, le temps du modèle, des appels et des pauses (un écart de 30 minutes ou plus entre deux étapes est une pause, non du travail), le contexte à son maximum et à la fin, les jetons, les compactages, vos messages et les avis que l'exécuteur a donnés à l'agent (un appel illisible, trop d'appels, un contexte plein).
- **Outils** : pour chaque outil, les appels, les erreurs, les résultats qui ont été coupés, la fréquence à laquelle il était le seul appel d'un tour, les caractères écrits par l'agent, le nombre d'appels qui ont passé un texte sous forme de `@file:` ou de `@result`, le nombre de longs textes (300 caractères ou plus) écrits et, parmi eux, ceux recopiés d'un résultat que l'agent venait de recevoir, les jetons de ses résultats (environ, d'après les caractères), la part de tous les résultats, le plus long résultat et le temps.
- **Ce que les requêtes ont signalé** : enregistré pendant que l'exécution travaille, donc seulement pour les exécutions faites après l'ajout de cette fonction (une exécution plus ancienne indique qu'elle n'en a pas) : le temps d'attente des requêtes avant le premier mot (le serveur lit le prompt, un temps court signifie donc que son cache est chaud), le temps d'écriture des réponses et leur nombre de jetons par seconde, la part du prompt que le serveur n'a pas relue (le cache ; llama.cpp et OpenAI la signalent), la part des réponses qui relevait du raisonnement, les raisons de fin des réponses (`length` signifie que la limite d'une réponse l'a coupée), les appels écrits par le modèle qui n'ont pas été faits (répétitions, limites, politiques de lots), le temps d'attente des requêtes pour une place, les résumés écrits et leur coût, et la même chose pour chaque bloc de 25 tours.
- **Ce que le modèle a écrit** : les caractères des arguments, du texte accompagnant les appels et du raisonnement, au total et pour chaque bloc de 25 tours avec le temps du modèle, pour voir où va sa production et s'il a cessé d'utiliser `@file` à un moment donné. **Arguments les plus longs** montre le début des plus gros appels, avec le chemin d'un outil de fichier.
- **Tours perdus à cause d'un schéma** : le même outil appelé tour après tour (trois fois ou plus), signalé lorsqu'une recherche le précède immédiatement ; et les appels refaits avec les mêmes arguments. Ceux-ci montrent où un outil qui accepte beaucoup d'éléments, ou plusieurs appels dans une seule réponse, ferait gagner des tours.
- **Erreurs** avec les arguments du premier appel ayant reçu chaque message (pour voir ce que le modèle a envoyé lorsqu'un appel a échoué), les **plus gros résultats**, les **tours les plus lents**, les **compactages** et les **sous-agents** avec les outils que chacun a appelés.

**Télécharger** donne le rapport en Markdown, **Télécharger le JSON** en JSON, **Copier** met le Markdown dans le presse-papiers. Les temps sont les écarts entre les lignes enregistrées, ils sont donc approximatifs : le temps d'une réponse inclut l'attente d'une place libre auprès du modèle.
<a id="the-context"></a>

### Le contexte

L'onglet **Context** montre ce que le modèle lit avec sa **prochaine requête**, décomposé, pour que vous puissiez voir où partent les tokens et déboguer un agent qui se comporte bizarrement. Il est construit à l'ouverture de l'onglet et lorsque vous appuyez sur **Actualiser** (pas pendant que l'exécution travaille : il compte les tokens de chaque partie).

- Trois graphiques : les **types de parties** par rapport à la limite du contexte (le prompt de l'agent, vos instructions, les notes du runner, les définitions des outils, le résumé d'une compaction, vos messages, les réponses de l'agent, les résultats de ses appels, et ce qui est libre), **chaque partie dans l'ordre où elle est envoyée** (une barre pour chacune, de la couleur de son type : un outil dont la description est énorme ou un résultat qui remplit le contexte ressortent), et le **contexte à chaque réponse du modèle** : une barre empilée de ce dont il était composé (les mêmes types, estimés), la taille que le modèle a **indiquée** sous forme de ligne et la limite sous forme de ligne en pointillés. Là où la ligne est au-dessus des barres, le modèle a compté plus que l'estimation (images, format du fournisseur) ; une barre qui descend correspond à une compaction ou à un retour en arrière.
- Un **tableau des parties** avec le type, le nom, les tokens et la part du contexte. Sélectionnez une ligne pour lire **exactement le texte que le modèle reçoit** pour cette partie : une section du message système, la description et les paramètres d'un outil, un message, les arguments d'un appel ou le résultat de celui-ci.
- **Télécharger** donne l'ensemble du contexte sous forme d'un seul fichier Markdown (le message système avec ses sections, chaque outil avec sa description et son schéma, la conversation message par message, chaque texte dans un bloc de code), **Copier** place le même texte dans le presse-papiers.

![L'onglet Context](../images/agent-context.png)

Les tailles des parties sont des estimations ; le nombre indiqué par le modèle est le vrai (la requête et sa réponse). Dans le graphique des réponses, le prompt, les instructions et les outils sont tels qu'ils sont maintenant (une réponse plus ancienne pouvait avoir eu d'autres valeurs si vous les avez modifiés), la conversation est telle qu'elle était alors : un résumé a remplacé ce qui le précédait, et ce qu'un retour en arrière a annulé ne figure plus dans les réponses qui suivent (une compaction elle-même annulée n'est pas rétablie, les barres qui suivent sont donc un peu trop petites). Le message système et les outils sont ceux que la prochaine requête utiliserait, donc un changement du prompt, des instructions ou des commandes autorisées apparaît immédiatement. Les messages sont la transcription telle qu'elle est stockée, sans ce qu'une reprise ajoute (un « continue », les réponses aux appels qu'une interruption a laissés ouverts). Le raisonnement ne fait pas partie du contexte (il n'est pas renvoyé au modèle) et les images apparaissent sous leur nom de fichier. Ce qu'une compaction a remplacé ne figure pas non plus dans le contexte ; l'onglet **Run** et le téléchargement des **Runs** le conservent encore.

<a id="checkpoints"></a>

## Points de contrôle

Un **point de contrôle** est un point de restauration de la copie de travail de l'exécution (les parties, résumés, lorebooks et données des extensions, avec les poignées que l'agent a reçues) et de l'espace de travail de l'agent, afin qu'une tâche qui a mal tourné puisse être annulée : l'agent revient en arrière avec `checkpoint_restore`, ou vous le faites dans l'onglet **Checkpoints** de la console. Qui en crée :

- **Début de l'exécution**, créé au lancement de l'exécution. Il n'est jamais supprimé automatiquement.
- **Automatique**, créé par l'exécution avant un tour dans lequel l'agent appelle quelque chose qui peut modifier le livre ou l'espace de travail (une commande qui n'est pas une simple lecture, ou un outil de fichier qui écrit), et seulement si quelque chose a changé depuis le dernier point de contrôle. Les cinq derniers sont conservés ; le plus ancien disparaît lorsqu'un nouveau est créé.
- **Fait exprès**, créé par l'agent avec `checkpoint` (on lui demande d'en créer un avant une étape risquée) ou par vous avec **Enregistrer un point de contrôle**. Ils restent jusqu'à leur suppression.

![L'onglet Checkpoints](../images/agent-checkpoints.png)

Tous les points de contrôle d'une exécution partagent un **quota** que l'administrateur fixe dans la [permission](administration/permissions.md#settings-of-a-permission) (100 Mo si vide). Une partie est réservée aux automatiques, pour que l'agent ne puisse pas tout consommer ; un point de contrôle fait exprès qui ne rentre pas est **refusé**, rien n'est supprimé à l'insu de celui qui l'a demandé. L'agent reçoit alors la liste et supprime ce dont il n'a pas besoin (pas le point de contrôle de début, ni le plus récent) ; vous pouvez en supprimer n'importe lequel dans l'onglet. Chacun est une archive (un ZIP du livre en JSON, des paires et des fichiers de l'espace de travail) que vous pouvez **télécharger**.

**Restaurer** remet la copie de travail dans l'état du point de contrôle : c'est une nouvelle copie, l'ancienne est supprimée, l'espace de travail est celui qui a été sauvegardé (les fichiers écrits après sont perdus) et les poignées de l'agent pour les éléments qui existaient encore fonctionnent toujours. L'état d'avant est d'abord sauvegardé comme point de contrôle automatique, s'il y a de la place, pour qu'une restauration puisse être annulée. Une restauration prend à peu près le même temps que le lancement d'une exécution (une demi-minute pour un livre de deux mille parties). Vous pouvez restaurer pendant que l'agent ne travaille pas ; l'agent se restaure lui-même entre deux tours et est informé de ce qui a été rétabli. Les points de contrôle disparaissent lorsque l'exécution lâche sa copie.

**Revenir en arrière** est une restauration qui reprend aussi la *conversation*. Une restauration seule laisse l'agent se souvenir d'un travail qui n'est plus dans le livre ; une exécution que vous voulez envoyer dans une autre direction a donc besoin d'un retour en arrière : la copie de travail et l'espace de travail reviennent au point de contrôle, les messages postérieurs au moment où le point de contrôle a été créé sont grisés dans la transcription (vous pouvez toujours les lire, et l'export les contient) et l'agent ne les lit plus, et une notification informe l'agent de ce qui s'est passé, pour qu'il refasse le travail si celui-ci est encore nécessaire. L'exécution doit être mise en pause ou arrêtée (mettez d'abord en pause un agent qui attend votre réponse) ; vous la **Reprenez** ensuite et elle continue à partir de la notification. On ne peut revenir en arrière que vers un point de contrôle dont la place dans la conversation fait encore partie de celle-ci : après un retour en arrière vers un point de contrôle antérieur, les suivants (ceux de la conversation qui a été annulée) ne peuvent être que restaurés. Un point de contrôle créé avant que le retour en arrière n'existe ne connaît pas sa place et ne peut être que restauré. Ce qu'une compaction a fait après le point de contrôle est annulé avec le retour en arrière ; l'agent relit les messages que la compaction avait remplacés.

**Revenir à un message.** Vous n'êtes pas obligé de choisir un point de contrôle : chaque message que vous avez écrit à l'agent a un bouton **Revenir en arrière** sous lui, dans la transcription de l'onglet **Run**. Il ramène la conversation à avant ce message : l'agent oublie le message et tout ce qui suit, et la copie de travail et l'espace de travail reviennent au point de contrôle automatique le plus récent créé avant lui (le runner en crée un avant chaque tour qui modifie quelque chose, donc c'est généralement juste au niveau du message). L'état d'une exécution n'est connu qu'à ses points de contrôle ; donc, lorsque le plus proche est plus ancien que le message, les messages situés entre ce point de contrôle et le vôtre sont oubliés aussi ; la confirmation indique de quel point de contrôle il s'agit et combien de messages disparaissent. Les messages oubliés restent dans la transcription, grisés. Le bouton fonctionne pendant que l'agent attend votre réponse (il est en pause pour cela), est en pause, ou a été arrêté et conservé ; pendant que l'agent travaille, il est désactivé. Vous **Reprenez** ensuite l'exécution, et elle continue à partir de la notification.
<a id="creating-and-editing-an-agent"></a>

## Création et modification d'un agent

**Nouvel agent** ouvre la même fenêtre que **Modifier** :

![La fenêtre d'un nouvel agent](../images/agent-dialog.png)

| Champ | |
|---|---|
| **Nom** | Obligatoire. |
| **Activé** | Un agent désactivé ne peut pas être lancé ; ses exécutions existantes ne sont pas touchées. |
| **Livre** | Le livre sur lequel l'agent travaille, obligatoire. Seuls les livres qui n'ont pas encore d'agent sont proposés, et le livre d'un agent ne peut plus être changé ensuite. |
| **Modèle**, **Protocoles** | Le modèle et le protocole avec lesquels l'agent communique. Les deux, ou aucun ; un agent sans eux ne peut pas être lancé. |
| **Écrire le livre avec le modèle de l'agent** | Activé (par défaut) : les commandes qui ont un modèle écrivent quelque chose - le tour suivant, les swipes, les résumés, et les commandes des extensions comme le Lore Generator et le Arc Outline - utilisent le modèle ci-dessus, quel que soit le modèle de votre livre. Désactivé : elles utilisent le modèle de votre livre (une commande d'extension revient à votre modèle par défaut des [Paramètres](account.md) quand le livre n'en a pas). C'est ainsi que vous mélangez les modèles : un modèle puissant qui réfléchit et un modèle moins cher ou plus fluide qui écrit. Une commande peut toujours nommer un autre profil de modèle de l'agent. |
| **Appels dans une réponse** | Combien des appels que l'agent écrit dans une réponse sont effectués. *Toutes ensemble, ignorer les règles de lot* : chaque appel (le plus rapide). *Ensemble, les commandes marquées Seule à part* (par défaut) : les appels sont effectués ensemble, mais une commande dont la colonne **Lot** indique *Seule*, comme une commande qui fait un appel à un modèle, est effectuée dans une réponse à elle seule, et l'agent en est informé. *Un appel par réponse* : seul le premier appel est effectué et l'agent écrit le suivant après avoir lu le résultat ; plus lent (chaque appel est un tour à part, avec la réflexion du modèle) mais il peut réagir à chaque résultat. Une **erreur termine toujours la réponse**, dans tous les modes : les appels qui suivent celui qui a échoué ne sont pas effectués (ils reçoivent la réponse « non effectué car un appel précédent a échoué ») et l'agent lit d'abord l'erreur. |
| **Le modèle voit les images** | Cochez-le quand le modèle accepte les images (l'API du modèle ne le dit pas, c'est donc à vous de le savoir). L'agent dispose alors de `view_image` et vous pouvez joindre des images à vos messages ; sans cela, il ne reçoit jamais d'image. |
| **Langue** | La langue de tout ce que l'agent lit : les descriptions de ses commandes, leurs résultats et leurs erreurs, et les textes intégrés de l'exécution (son prompt système par défaut, les avis de l'exécution, le prompt de compactage) lorsqu'ils existent dans cette langue (anglais, japonais, chinois) ; la [langue des prompts](account.md#prompt-language) de vos paramètres ne s'applique pas aux agents. L'anglais est la valeur par défaut et le choix le plus sûr, car les commandes et leurs paramètres sont en anglais quelle que soit la langue (les descriptions dans une autre langue mélangent les deux), et les modèles, surtout les petits, utilisent les outils plus fiablement ainsi. La liste contient les langues installées dans l'application (une extension peut en ajouter une, comme la localisation chinoise). Elle n'a **rien à voir avec la langue du livre** : l'agent reçoit pour consigne d'écrire le texte du livre dans la langue du livre, qu'il peut lire avec `get-book-info`. Une exécution garde la langue avec laquelle elle a été lancée ; un changement s'applique à la prochaine exécution. |
| **Commandes destructrices** | Ce qui se passe quand l'agent veut une commande qui modifie ou supprime des données : *Les exécuter*, *Demander d'abord* (une [fenêtre](#asking-you) vous demande de l'approuver) ou *Les refuser*. |
| **Nombre maximal de tours**, **Budget de jetons d'une exécution**, **Limite de contexte** | Limites facultatives d'une exécution. La limite donnée par votre administrateur s'applique dans tous les cas. |
| **Nombre maximal de sous-agents** | Combien de [sous-agents](#sub-agents) l'agent peut faire travailler en même temps, de 0 à 10. Vide ou zéro signifie aucun. |
| **Modèles des sous-agents** | Les [modèles](#models-for-sub-agents) sur lesquels un sous-agent peut tourner, et s'il peut tourner sur le modèle principal. |
| **Compacter à (% du contexte)** | Quand le contexte du modèle dépasse cette part de sa limite et que l'agent n'a pas [compacté son travail](#running-a-command-yourself) lui-même, l'exécution le fait : l'agent écrit le résumé de son travail antérieur, qui le remplace. Vide vaut 90, zéro ne compacte jamais (l'exécution s'arrête alors quand le contexte est plein), sinon de 50 à 99. |
| **Prompt système** | Ce que l'agent est censé être. Laissez-le vide et l'agent suit la valeur par défaut - la vôtre, dans les [Paramètres](account.md#templates), sinon celle de votre administrateur, sinon celle intégrée - qui s'affiche en gris. L'icône d'information propose **Insérer le modèle par défaut**, qui remplit le champ avec la valeur par défaut pour que vous puissiez modifier une copie ; videz de nouveau le champ pour suivre la valeur par défaut. |
| **Prompt de compactage** | Ce qui est demandé à l'agent quand vous le [`/compact`](#running-a-command-yourself) : rédiger le résumé qui remplace son travail antérieur. Vide suit la valeur par défaut, comme le prompt système, avec la même icône d'information. |
| **Instructions (agents.md)** | Vos instructions à l'agent, ajoutées à son prompt à chaque exécution. L'agent peut les lire, jamais les modifier. Vide suit votre valeur par défaut des [Paramètres](account.md#templates), si vous en avez une (elle s'affiche en gris), sinon il n'y en a aucune. L'icône d'information propose **Insérer le modèle par défaut**, qui remplit le champ avec votre valeur par défaut pour que vous puissiez modifier une copie. Sous ce champ, une ligne indique le poids en jetons, **à chaque requête**, du prompt, des instructions et des commandes cochées (une estimation, auxquels s'ajoutent les outils de fichiers et d'exécution et les notes de l'exécuteur). |
| **Commandes autorisées** | Voir ci-dessous. |

![Les prompts de l'agent et les indications du modèle par défaut](../images/agent-dialog-prompts.png)

<a id="allowed-commands"></a>

### Commandes autorisées

L'agent ne se voit proposer que les commandes que vous cochez. Le tableau liste chaque commande avec son effet (*Destructrice* modifie ou supprime des données, *Non destructrice* ne fait que lire ou ajouter). La case **Sélection** coche plusieurs commandes d'un coup : **Toutes**, **Aucune** ou **Toutes les non destructrices** (par défaut pour un nouvel agent), ou un rôle ; cocher une commande vous-même fait passer la sélection à *Personnalisée*. Le champ **Rechercher des commandes** au-dessus du tableau n'affiche que les commandes qui contiennent tous les mots que vous tapez (dans n'importe quel ordre, dans le nom ou la description) ; la sélection s'applique toujours à toutes les commandes, y compris celles qui sont masquées. Les commandes apportées par une extension restent autorisées lorsque l'extension n'est pas chargée, vous ne les voyez simplement pas. La colonne **Lot** indique si la commande est utilisée *Ensemble* avec d'autres appels dans une réponse ou *Seule* (une commande qui fait un appel à un modèle, comme `next`, `swipe`, `regenerate`, `redraft-part`, `generate-annotation` ou le compactage, est seule ; voir **Appels dans une réponse** ci-dessus). Toutes les colonnes peuvent être redimensionnées. La colonne **Jetons** indique ce que la définition de la commande (son nom, sa description et ses paramètres) ajoute à chaque requête de l'agent, et la ligne sous les instructions additionne celles qui sont cochées : l'ensemble pèse environ 20 000 jetons, donc une sélection plus petite (un rôle) laisse plus de place dans le contexte pour le travail.

![Les commandes autorisées, avec les rôles de la case Sélection](../images/agent-commands.png)

Les rôles sont des ensembles de commandes pour une tâche. Tous peuvent lire le livre et les lorebooks (chaque commande qui ne fait que lire, celles des extensions comprises), et aucun ne peut supprimer de données :

| Rôle | En plus de la lecture |
|---|---|
| **Auditeur** | Rien : vérifications de cohérence, audits de noms et de termes, rapports. |
| **Gardien du lore** | Crée et met à jour les entrées et les lorebooks, leurs étiquettes et leurs sous-lorebooks. |
| **Éditeur** | Modifie et ajoute à la suite des parties, définit leurs champs, tient à jour les résumés (et peut faire réécrire les résumés par le modèle) ainsi que les informations et les étiquettes du livre. |
| **Rédacteur** | Ajoute des parties et des branches, modifie et ajoute à la suite, fait écrire le tour suivant par le modèle (`/next`, `/swipe`), tient à jour les résumés, crée et met à jour les entrées de lorebook de ce qu'il invente. |

Chaque commande cochée ajoute sa définition à chaque requête ; un rôle est donc aussi le moyen le moins coûteux d'obtenir un prompt court pour un modèle faible.
<a id="on-a-phone"></a>

## Sur téléphone

La disposition pour téléphone comporte un onglet **Agents** dans le tiroir, pour les utilisateurs autorisés à utiliser des agents, avec à côté de son nom le nombre d'agents qui attendent votre intervention. Chaque agent est une carte avec son nom, son état et les boutons du tableau (ils passent à la ligne). Les fenêtres - la console, la révision, les paramètres de l'agent - occupent tout l'écran, et la révision affiche le tableau au-dessus du détail de l'élément. Tout fonctionne comme sur l'ordinateur, en plus petit. La console est conçue pour être utilisée avec le pouce :

![L'onglet Agents sur un téléphone](../images/phone-agents.png)

![La console sur un téléphone](../images/phone-agent-console.png)

![L'onglet Agents sur un téléphone](../images/phone-agents.png)

![La console sur un téléphone](../images/phone-agent-console.png)

- **Les onglets sont des icônes** (exécution, exécutions, espace de travail, points de contrôle, contexte, copie du livre, sous-agents, diagnostic) ; le titre de la fenêtre indique celui qui est ouvert.
- **Pause, Pause maintenant, Reprendre, Terminer et Arrêter sont des boutons icônes en bas, et seuls ceux utilisables maintenant sont affichés** ; **Vérifier les modifications** garde son texte, c'est ce qu'attend une exécution terminée. **Actualiser** et **Fermer** sont des icônes dans l'en-tête.
- **La zone de message tient sur une ligne**, avec les icônes **Envoyer** et **Joindre une image** à côté. La **ligne de commande** (l'icône du terminal) est masquée jusqu'à son ouverture, afin que le clavier ne recouvre pas la transcription inutilement. Les messages de la transcription occupent toute la largeur.
- **Les tableaux sont des listes de cartes** : une exécution, un point de contrôle, une commande des paramètres de l'agent, un élément de la révision affichent chacun leur nom et leur état, avec les détails dessous et leurs boutons en dessous, à la place des colonnes. Les fichiers de l'espace de travail affichent leur taille sous le nom, et la barre d'outils de l'espace de travail est faite d'icônes. Le tableau du contexte conserve le numéro, le nom et les jetons ; le type et la part d'un élément sont sous le texte de l'élément choisi.

- **La barre supérieure indique qui attend votre intervention.** Quand le tiroir est fermé, un nombre rouge dans la barre supérieure (à côté du titre de la partie ouverte) compte les agents qui attendent votre intervention, comme le badge de l'onglet **Agents** dans le tiroir ; touchez-le pour aller à l'onglet **Agents**. Il est masqué lorsque personne n'attend, quand le tiroir est ouvert et dans l'onglet Agents lui-même.
- **Glissez entre les parties.** Un balayage horizontal sur le contenu passe à la partie suivante du tiroir (glisser vers la gauche) ou à la précédente (vers la droite). Il ne fait rien lorsque le tiroir est ouvert ou lorsque l'application verrouille la navigation, et un toucher qui commence dans un champ de texte, un tableau ou tout élément défilant horizontalement lui reste réservé.

Pour un long travail sur la transcription ou la révision, un écran plus grand reste plus confortable.

<a id="cloning"></a>

## Clonage

**Cloner** demande un nom et un livre qui n'a pas d'agent, et copie la définition de l'agent et ses fichiers (pas ses exécutions) vers un nouvel agent de ce livre. Vous pouvez avoir autant d'agents, clones compris, que votre administrateur l'autorise (cinq, sauf s'il en a fixé un autre nombre) ; à la limite, **Nouvel agent** et **Cloner** sont désactivés et indiquent la raison.

![Clonage d'un agent](../images/agent-clone.png)

<a id="lore-notes-of-an-imported-book"></a>

## Notes de lore d'un livre importé

Après que vous avez [importé](books/story-editor.md#after-an-import) un manuscrit, l'application propose un agent qui reprend le lore du livre : **Prendre des notes de lore avec un agent...** (le même élément se trouve dans le menu des paramètres ⚙ d'une histoire, **Analyse**). Il demande d'abord confirmation :

![La question posée avant le démarrage de l'agent](../images/lore-notes-confirm.png)

Il crée ensuite un agent appelé **Notes de lore** pour le livre, avec le modèle et le protocole du livre, et le démarre (sa console s'ouvre).

- Il lit l'histoire de la première partie à la dernière, quelques parties à la fois, et conserve ses notes dans les fichiers de son espace de travail (personnages, lieux, objets, termes) afin de pouvoir reprendre après une pause. Les résumés l'aident à s'orienter, les parties sont sa source.
- Il écrit ensuite une entrée pour chaque personnage, lieu, objet, groupe ou terme dans le lorebook du livre (un lorebook est créé pour le livre s'il n'en a pas), avec les noms utilisés dans l'histoire comme filtre, dans la langue du livre. Il ajoute des faits à une entrée existante et n'écrit pas deux fois la même chose.
- Il peut lire l'histoire et les lorebooks et écrire des entrées ; il ne peut pas modifier l'histoire ni rien supprimer.

C'est un agent comme les autres : le livre est [verrouillé](#the-book-is-locked) pendant son travail, il travaille sur une [copie](#the-working-copy), et ce qu'il a écrit n'est à vous qu'après que vous [l'avez révisé](#reviewing-the-result) et repris. Lisez les entrées avant de le faire : il ne connaît que ce que dit le texte, et un long livre coûte beaucoup de requêtes. Les instructions sont celles de l'agent (**Modifier** les affiche, vous pouvez les changer avant de le relancer). Il est refusé lorsque vous n'êtes pas autorisé à utiliser des agents, lorsque le livre n'a pas de modèle, et lorsque le livre a déjà un agent (un livre en a un seul).

<a id="the-book-is-locked"></a>

## Le livre est verrouillé

Pendant qu'une exécution d'agent travaille ou attend votre intervention, **son livre est verrouillé** : vous pouvez le lire, mais rien ne le modifie, afin que l'agent ne travaille jamais sur un texte qui bouge sous lui. La liste des livres n'ouvre pas un livre verrouillé ; elle indique quel agent le détient, et la génération, la modification, la recherche et le remplacement, les résumés et la suppression du livre sont refusés. **Mettez en pause** ou **Arrêtez** l'exécution pour modifier de nouveau le livre ; une exécution en pause, arrêtée ou terminée ne verrouille pas le livre.

![Un livre sur lequel travaille un agent, dans la liste des livres](../images/agent-locked-book.png)

Les **lorebooks du livre** sont verrouillés avec lui : le lorebook utilisé par le livre et ses sous-lorebooks. Dans l'onglet Lorebooks, un tel lorebook s'affiche comme sur un téléphone, en texte brut et sans aucune modification, avec une note indiquant quel agent le détient ; **Actualiser** le rend de nouveau modifiable après que vous avez mis en pause ou arrêté l'exécution. Les autres lorebooks ne sont pas concernés.

<a id="agents-and-folders"></a>

### Agents et dossiers

Un livre placé dans un [dossier](books/folders.md) sur lequel travaille un agent conserve sa place et ses paramètres : déplacer ou supprimer un dossier au-dessus de lui, et modifier un paramètre que ce dossier transmet (langue, modèles, point de vue, temps, style, consignes), est refusé avec le nom du livre et de l'agent. Renommer un dossier, ou modifier sa description, n'est pas refusé. Une exécution en pause, arrêtée ou terminée libère le dossier.

![Le lorebook d'un livre sur lequel travaille un agent](../images/agent-locked-lorebook.png)
