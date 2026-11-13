---
label: Proveedor de lugares
order: 5
---

# Proveedor de lugares

Permite al modelo consultar lugares reales de la Tierra en lugar de inventarlos: ciudades, países, mares y bahías, ríos, lagos,
islas, montañas, desiertos, puertos y aeropuertos, con sus coordenadas y los países en los que se encuentran. También puede indicar
qué hay alrededor de un lugar y a qué distancia están dos lugares, de modo que una historia ambientada en el mapa real tenga
distancias y vecinos correctos.

La extensión no tiene pantalla propia y no requiere configuración. El [Generador de lore](lore-generator.md#tools) y el
[Esquema de arco](arc-outline.md) lo ofrecen al modelo como el conjunto *Place provider* del botón **Tools**
([Herramientas para el modelo](tools.md)), activado o desactivado; también está disponible como comandos para [agentes](../agents.md) y desde la línea de comandos.

<a id="what-it-knows"></a>

## Qué conoce

Los datos proceden de [Natural Earth](https://www.naturalearthdata.com/) (dominio público), un mapa de los lugares **notables**: unos
18 000, entre ellos 4 900 ciudades, 250 países, 4 500 provincias y estados, 1 100 ríos, 500 lagos, 400 islas, 350 montañas y
cordilleras, 1 000 puertos y 900 aeropuertos. No es un nomenclátor: un arroyo pequeño o una aldea no aparecen. Natural Earth también dibuja
la mayoría de las entradas del mar como bahías, de modo que pedir las entradas de Canadá da las cuatro que tiene (Hamilton Inlet, Bathurst Inlet, Minto Inlet y
Richard Collinson Inlet), no las cientos de la costa de la Columbia Británica.

<a id="commands"></a>

## Comandos

Todos solo leen.

| Comando | Qué hace |
|---|---|
| `/list-place-types` | Enumera los tipos de lugares (`city`, `capital`, `bay`, `inlet`, `river`, `mountain range`, `island`...) con cuántos hay de cada uno. |
| `/search-places query [type [country [limit]]]` | Busca por nombre, tipo y país, con la mejor coincidencia primero. `*` como consulta equivale a cualquier nombre: `/search-places * inlets Canada` enumera las entradas de Canadá. |
| `/places-near place radius_km [type [limit]]` | Los lugares dentro del radio indicado alrededor de un lugar o de un punto, los más cercanos primero, con la distancia en kilómetros y la dirección. |
| `/distance-between from to` | La distancia en línea recta sobre la Tierra (no por carretera) en kilómetros y millas, con la dirección. |

Un *lugar* es un nombre (`Ottawa`; `Paris, France` cuando varios tienen el mismo nombre: la coma y el país o la provincia eligen uno) o
coordenadas (`45.42, -75.69`). Un *tipo* puede escribirse en plural o como se dice en el habla corriente: `inlets`, `mountains`, `towns`, `states`. Un
*país* es un nombre o un código: `Canada`, `Czechia`, `CA`, `CAN`. Los nombres se comparan sin distinguir mayúsculas ni tildes. Se devuelven como máximo 100 lugares (25 sin límite), y la respuesta indica cuántos hay en total.

Una búsqueda por tipo también encuentra los lugares de un tipo de agua parecido que tienen la palabra en su nombre: `inlet` encuentra Hamilton Inlet,
que Natural Earth clasifica como bahía.

<a id="using-it-from-an-agent"></a>

## Uso desde un agente

Un agente al que se le permiten los comandos (están en la selección *All non-destructive* y en el rol *Auditor*) puede llamarlos como
funciones. Están pensados para una instrucción como "ambienta el primer capítulo en un pueblo pesquero de un fiordo en Noruega, a dos días en barco de
la capital": el agente busca `fjords of Norway`, elige uno y pregunta la distancia a Oslo.

<a id="usage"></a>

## Uso

Carga la extensión en *Admin → Extensions*. En el Generador de lore y en el Esquema de arco, marca *Usar este conjunto* de Place
provider en **Tools**; su sugerencia indica al modelo que busque un lugar real en lugar de adivinar dónde está o qué distancia hay.
