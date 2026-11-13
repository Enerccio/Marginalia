---
label: Fournisseur de lieux
order: 5
---

# Fournisseur de lieux

Donne au modèle de vrais lieux de la Terre à consulter au lieu de les inventer : villes, pays, mers et baies, fleuves, lacs, îles, montagnes, déserts, ports et aéroports, avec leurs coordonnées et les pays où ils se trouvent. Il peut aussi dire ce qui se trouve autour d'un lieu et quelle distance sépare deux lieux, afin qu'une histoire située sur la vraie carte ait des distances et des voisinages justes.

L'extension n'a pas d'écran propre et rien à configurer. Le [Générateur de lore](lore-generator.md#tools) et le [Plan d'arc](arc-outline.md) la proposent au modèle comme la suite *Place provider* de leur bouton **Outils** ([Outils pour le modèle](tools.md)), activée ou non ; elle existe aussi sous forme de commandes pour les [agents](../agents.md) et la ligne de commande.

<a id="what-it-knows"></a>

## Ce qu'elle connaît

Les données viennent de [Natural Earth](https://www.naturalearthdata.com/) (domaine public), une carte des lieux **notables** : environ 18 000, dont 4 900 villes, 250 pays, 4 500 provinces et États, 1 100 fleuves, 500 lacs, 400 îles, 350 montagnes et chaînes, 1 000 ports et 900 aéroports. Ce n'est pas un répertoire géographique : un petit ruisseau ou un village n'y figurent pas. Natural Earth représente aussi la plupart des anses comme des baies ; demander les anses du Canada donne donc les quatre qu'elle contient (Hamilton Inlet, Bathurst Inlet, Minto Inlet et Richard Collinson Inlet), et non les centaines de la côte de la Colombie-Britannique.

<a id="commands"></a>

## Commandes

Toutes se contentent de lire.

| Commande | Ce qu'il fait |
|---|---|
| `/list-place-types` | Liste les types de lieux (`city`, `capital`, `bay`, `inlet`, `river`, `mountain range`, `island`...) avec leur nombre. |
| `/search-places query [type [country [limit]]]` | Recherche par nom, type et pays, la meilleure correspondance en premier. `*` comme requête désigne n'importe quel nom : `/search-places * inlets Canada` liste les anses du Canada. |
| `/places-near place radius_km [type [limit]]` | Les lieux situés dans le rayon donné autour d'un lieu ou d'un point, les plus proches en premier, avec la distance en kilomètres et la direction. |
| `/distance-between from to` | La distance à vol d'oiseau sur la Terre (et non par la route) en kilomètres et en miles, avec la direction. |

Un *lieu* est un nom (`Ottawa` ; `Paris, France` lorsque plusieurs portent ce nom : la virgule et le pays ou la province permettent de choisir) ou des coordonnées (`45.42, -75.69`). Un *type* peut s'écrire au pluriel ou comme on le dit couramment : `inlets`, `mountains`, `towns`, `states`. Un *pays* est un nom ou un code : `Canada`, `Czechia`, `CA`, `CAN`. Les noms sont comparés sans tenir compte de la casse ni des accents. Au plus 100 lieux sont renvoyés (25 sans limite), et la réponse indique combien il y en a au total.

Une recherche par type trouve aussi les lieux d'un type d'eau voisin qui ont ce mot dans leur nom : `inlet` trouve Hamilton Inlet, que Natural Earth appelle une baie.

<a id="using-it-from-an-agent"></a>

## Utilisation depuis un agent

Un agent autorisé à utiliser les commandes (elles figurent dans la sélection *Toutes les non destructrices* et dans le rôle *Auditor*) peut les appeler comme des fonctions. Elles sont conçues pour une consigne du type « place le premier chapitre dans un village de pêcheurs sur un fjord de Norvège, à deux jours de mer de la capitale » : l'agent cherche `fjords of Norway`, en choisit un et demande la distance jusqu'à Oslo.

<a id="usage"></a>

## Utilisation

Chargez l'extension dans *Admin → Extensions*. Dans le Générateur de lore et le Plan d'arc, cochez *Utiliser cette suite* de Place provider dans **Outils** ; son indication demande au modèle de chercher un lieu réel au lieu de deviner où il se trouve ou à quelle distance il est.
