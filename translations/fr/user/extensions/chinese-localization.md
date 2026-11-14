---
label: Localisation en chinois
order: 18
---

# Localisation en chinois

Ajoute le chinois simplifié (中文（简体）) aux langues de l'interface utilisateur.

<a id="using-it"></a>

## Utilisation

Une fois l'extension chargée par un administrateur ([Gestion des extensions](../administration/extensions.md)), *中文（简体）*
figure dans le sélecteur **Langue**, en bas de l'espace de travail (dans le menu sur un [téléphone](../phone.md)), à côté
de l'anglais et du japonais. Choisissez-la et la page se recharge en chinois ; le choix est enregistré dans votre compte,
il est donc utilisé sur chaque appareil sur lequel vous vous connectez. Chaque utilisateur choisit sa propre langue.

![L'éditeur de récit en chinois](../../images/plugin-chineselocalization.png)

L'interface est traduite : menus, boutons, messages, descriptions des variables de modèle et des macros. Un texte qui
n'a pas encore de traduction chinoise reste affiché en anglais.

L'extension ajoute aussi le chinois à la **Langue des prompts** des paramètres (*Paramètres → Modèles*, voir
[Langue des prompts](../account.md#prompt-language)) : les prompts intégrés de l'application et des extensions sont
alors envoyés en chinois. C'est un choix à part, indépendant de la langue de l'interface. Vos livres, vos livres de
référence et les prompts que vous avez écrits vous-même ne sont pas modifiés.

<a id="removing-it"></a>

## Suppression

Décharger l'extension retire la langue, de l'interface comme des prompts. Les utilisateurs qui l'avaient choisie voient
et envoient alors l'anglais jusqu'à ce qu'ils en choisissent une autre ; leur choix est conservé et s'applique de nouveau
lorsque l'extension est chargée.

<a id="for-developers"></a>

## Pour les développeurs

L'extension sert d'exemple pour ajouter une langue : voir
[Extension d'exemple → Localisation en chinois](../../developer/plugins/example.md#walkthrough-chinese-localization).
