---
label: Inspecteur de prompt
order: 4
---

# Inspecteur de prompt

Affiche le prompt d'une génération avant son envoi au modèle, et demande s'il faut l'envoyer.

<a id="how-it-works"></a>

## Fonctionnement

L'extension ajoute **Inspect prompt before sending** au groupe *Analyse* du menu des paramètres (⚙) de l'[éditeur d'histoire](../books/story-editor.md). C'est un élément à case à cocher : choisissez-le pour activer l'inspection, choisissez-le à nouveau pour la désactiver. Ce choix est un paramètre de votre utilisateur et s'applique à tous vos livres.

Lorsqu'elle est activée, une génération (une nouvelle partie, un swipe, une régénération) s'arrête juste avant la requête et ouvre une boîte de dialogue, **Send this prompt to the model?** Le texte est le prompt tel que le modèle le recevra, après que toutes les autres extensions l'ont modifié : chaque message sous son rôle - `[SYSTEM]` (le modèle, le lore et les résumés), les parties précédentes de l'histoire sous forme de messages `[USER]` et `[ASSISTANT]`, et les instructions du dernier tour. Les images envoyées avec le prompt sont comptées à la fin de leur message.

![Le prompt avant son envoi](../../images/plugin-prompt-inspector.png)

- **Envoyer** envoie la requête et la génération se poursuit normalement.
- **Annuler** arrête la génération. La partie créée pour elle est supprimée (une partie régénérée retrouve son ancien texte), comme lorsqu'une génération échoue avant l'arrivée du moindre texte.

<a id="notes"></a>

## Remarques

- Le prompt est construit en premier, comme pour toute génération, et la question vous est posée en dernière étape avant la requête. Si un résumé est devenu invalide, la question habituelle sur la poursuite sans lui survient avant l'affichage du prompt. Aucune requête n'est envoyée au modèle avant votre réponse.
- Seules les générations lancées depuis l'éditeur d'histoire sont retenues. Les [agents](../agents.md) et les commandes n'ont pas de page où afficher le prompt, et leurs générations se poursuivent sans demande.
- La boîte de dialogue peut être déplacée et redimensionnée, et le texte peut être sélectionné et copié. Elle doit obligatoirement recevoir une réponse : Échap et un clic à l'extérieur ne la ferment pas. Si vous fermez la page alors que la boîte de dialogue est ouverte, cela équivaut à *Annuler* : la génération est arrêtée et rien n'est envoyé, dès que le serveur constate que la page a disparu (cela peut prendre un moment).
- Les autres utilisateurs ne sont pas interrogés, et le réglage est conservé lorsque la page est rechargée.
