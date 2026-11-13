---
label: Proveedor de nombres
order: 6
---

# Proveedor de nombres

Ofrece al modelo nombres reales entre los que elegir, en lugar de inventarlos: los nombres de pila y apellidos más comunes de muchos
países. El [Generador de nombres](name-generator.md) inventa nombres nuevos que solo suenan a una categoría; esta
extensión entrega nombres que existen, cada uno con la grafía original junto a la romanizada: `Satō` y `佐藤`.

La extensión no tiene pantalla propia ni nada que configurar. Otras extensiones ofrecen sus funciones al modelo: el
[Generador de lore](lore-generator.md#tools) y el [Esquema de arco](arc-outline.md), donde eliges qué listas
puede usar el modelo. Su conjunto es *Proveedor de nombres* en la [ventana de herramientas](tools.md), junto al de *Generador de nombres*, y puedes marcar listas de ambos, de uno o de ninguno.

<a id="what-the-model-can-do"></a>

## Lo que puede hacer el modelo

| Función | Qué hace |
|---|---|
| `list_real_name_lists` | Lista las listas de nombres; con un `filter`, solo las que tienen sus palabras en el nombre. |
| `pick_real_names` | Elige hasta 100 nombres distintos de una lista, al azar. Cada nombre es un par: el nombre romanizado y el original. |

Una lista con menos nombres de los pedidos devuelve todos ellos. Al modelo se le indica (véase la sugerencia del conjunto *Proveedor de nombres* en la [ventana de herramientas](tools.md#choosing-the-tools)) que elija un nombre de los que recibe y que lo escriba tal como se devolvió, con el original junto a él cuando encaje con el escenario; puede combinar un nombre de pila con un apellido del mismo país.

<a id="commands"></a>

## Comandos

Los mismos nombres están disponibles como [comandos](../commands/index.md), para la línea de comandos y para los [agentes](../agents.md),
que pueden usarlos en sus propios prompts (por ejemplo, para `/generate-lore-entry` del [Generador de lore](lore-generator.md)):

| Comando | Qué hace |
|---|---|
| `/list-real-name-lists [<filter>]` | Lista las listas como un array JSON; con un filtro, solo las cuyo nombre contiene todas sus palabras (cada una al inicio de una palabra, en cualquier orden, sin distinguir mayúsculas). |
| `/pick-real-names list count` | Elige hasta 100 nombres de la lista al azar y los devuelve como un array JSON de pares: el nombre romanizado y el original. |

Ambos solo leen.

<a id="lists"></a>

## Listas

Hay unas 180 listas, con nombres `<País> - Surnames` y `<País> - Forenames`: apellidos de 75 países y nombres de pila de
106 (`Japan - Surnames`, `Czechia - Forenames`, `Kosovo - Surnames`). Los nombres de ambos géneros están en una misma lista. Una lista tiene
entre 5 y unos 150 nombres, los más comunes del país. Los nombres no se inventan: un nombre que recibes es un nombre que
usa la gente.

Unas pocas docenas de entradas son **grupos** de listas por idioma: `<Idioma> - Forenames` y `<Idioma> - Surnames` para el español
(18 países), portugués, francés, alemán, inglés, árabe, eslavo occidental, oriental y meridional, nórdico germánico, nórdico,
urálico, báltico, turco, persa, chino, sur de Asia, sudeste asiático, Cáucaso, albanés y rumano (donde hay tales listas). Se listan
como cualquier lista y son un elemento en la [ventana de herramientas](tools.md). Cada nombre de un grupo se elige de una de sus listas,
escogida al azar, con la misma probabilidad para cada país; así, una petición de 20 nombres obtiene nombres de varios países, nunca el mismo
nombre dos veces. Los países donde se hablan varios idiomas (Bélgica, Luxemburgo, Malta, Andorra) no están en ningún grupo. Un grupo es una
línea en `name-groups.json`; véase el [README del plugin](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-provider/README.md#groups).

Los datos proceden del [popular names by country dataset](https://github.com/sigpwned/popular-names-by-country-dataset) (CC0), que
los toma de las listas de Wikipedia de los apellidos más comunes y de los nombres de pila más populares. Los nombres escritos en
alfabetos no latinos (chino, japonés, coreano, árabe, cirílico...) tienen una romanización, bien la de la fuente o bien una
hecha por traducción automática, así que conviene revisar la ortografía cuando importe.

<a id="usage"></a>

## Uso

Carga la extensión en *Admin → Extensions*. Se aplica a la siguiente generación, también cuando se instala después de las
extensiones que la usan o cuando se descarga y se vuelve a instalar. Sin ella y sin los demás conjuntos no aparece el botón
*Herramientas*.
