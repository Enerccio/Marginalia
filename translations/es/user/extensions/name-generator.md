---
label: Generador de nombres
order: 7
---

# Generador de nombres

Ofrece al modelo una forma de pedir nombres en lugar de inventarlos. Los nombres proceden de listas de nombres reales (nombres de pila y apellidos de muchos países, lugares, deidades, monstruos, fármacos, empresas, alimentos y más) y se generan de nuevo mediante una cadena de Markov, de modo que suenan a su categoría sin ser copias de ella.

La extensión no tiene pantalla propia ni nada que configurar. Otras extensiones ofrecen sus funciones al modelo; por ahora son el [Generador de lore](lore-generator.md#tools) y el [Esquema de arco](arc-outline.md), donde eliges qué categorías puede usar el modelo (véase [Herramientas para el modelo](tools.md)). Su suite es *Generador de nombres*, junto al *Proveedor de nombres* del [Proveedor de nombres](name-provider.md), que entrega nombres reales en lugar de inventados.

![Elegir las categorías de nombres en la ventana de Herramientas del Generador de lore](../../images/tools-dialog.png)

<a id="what-the-model-can-do"></a>

## Lo que puede hacer el modelo

| Función | Qué hace |
|---|---|
| `list_name_categories` | Lista las categorías de nombres; con un `filter`, solo las que tienen sus palabras en el nombre. |
| `generate_names` | Genera hasta 100 nombres nuevos de una categoría. Opcionalmente, con una longitud mínima y máxima. |

Sin longitud, los nombres tienen la longitud que suelen tener los de esa categoría (una categoría de nombres de empresa largos da nombres largos; una de nombres de pila da nombres cortos). El modelo puede pedir otras longitudes entre 2 y 30 letras. Una categoría pequeña puede devolver menos nombres de los pedidos, porque la cadena se queda sin nombres distintos. El modelo puede elegir uno de los nombres, modificarlo o combinar dos de ellos; véase la sugerencia de la suite *Generador de nombres* en la [ventana de herramientas](tools.md#choosing-the-tools).

<a id="commands"></a>

## Comandos

Los mismos nombres están disponibles como [comandos](../commands/index.md), para la línea de comandos y para los [agentes](../agents.md), que pueden usarlos en sus propios prompts (por ejemplo, para `/generate-lore-entry` del [Generador de lore](lore-generator.md)):

| Comando | Qué hace |
|---|---|
| `/list-name-categories [<filter>]` | Lista las categorías como un array JSON; con un filtro, solo las cuyo nombre contiene todas sus palabras (cada una al inicio de una palabra, en cualquier orden y sin distinguir mayúsculas: `chad male` encuentra *Africa - Central Africa - Chad - Male* pero no *…Female*). |
| `/generate-names category count [min_length [max_length]]` | Genera hasta 100 nombres de la categoría y los devuelve como un array JSON. Distintos nombres cada vez. |

Ambos solo leen.

<a id="categories"></a>

## Categorías

Hay unas 1270 categorías. Sus nombres dicen qué son:

- **Nombres de pila y apellidos del mundo**, del Onomastikon de Kate Monk: `Europe Western - France - Surnames`,
  `Ancient World - Egypt - Male`, `India - Hindu Gods - Goddesses`. Son varios cientos.
- **Apellidos de 75 países**, los apellidos romanizados del [conjunto de datos de nombres populares por país](https://github.com/sigpwned/popular-names-by-country-dataset)
  (CC0): `Armenia - Surnames`, `Japan - Surnames`, `Kosovo - Surnames`. Son listas pequeñas (de 6 a unos 150 nombres), así que las
  cadenas son cortas y los nombres nuevos se parecen a los reales; para los reales en sí, usa el
  [Proveedor de nombres](name-provider.md).
- **Listas de otras colecciones**: dioses, ángeles, demonios, ciudades y estados antiguos, emperadores, planetas (fantasygen),
  Corpora (animales, tarot, nombres de Tolkien, casas de Juego de Tronos, fármacos, hospitales, tejidos, piedras preciosas, lenguajes de
  programación...) y markov-namegen-lib (Pokémon, dragones, cócteles, bancos, nombres de lugares romanos...).

<a id="groups"></a>

### Grupos

Unas 170 entradas son **grupos** de categorías. Reúnen las muchas listas pequeñas de una lengua, de modo que pides
`German - Male` en lugar de elegir entre *Germany - Germanic - Male*, *Germany - Biblical - Male*, *Austria - First names - Male*
y las demás. Aparecen en la lista como cualquier categoría, y un grupo es un elemento más en la [ventana de herramientas](tools.md). Los grupos:

- **Por lengua**, cada uno como `- Male`, `- Female` y `- Surnames` cuando hay tales listas: alemán, francés, español (los
  apellidos incluyen Latinoamérica), italiano, portugués, neerlandés, escandinavo, finés, báltico, irlandés, escocés, galés, celta (todos
  ellos), eslavo occidental, oriental y meridional, griego, húngaro, rumano, albanés, caucásico, turco, persa, árabe, judío, hindú
  (las cuatro listas alfabéticas de nombres masculinos como una sola), sur de Asia, sudeste asiático, malayo-indonesio, polinesio y las
  regiones africanas (occidental, oriental, meridional, septentrional, central). `Chinese`, `Japanese`, `Korean` y `Vietnamese` tienen `- Surnames`.
- **Cualquier género**: `<Language> - Forenames` (`German - Forenames`, `Spanish - Forenames`, `Celtic - Forenames`...) toma las
  listas masculinas y femeninas de la lengua y sus listas de nombres para ambos géneros, de modo que una llamada devuelve nombres de hombres
  y de mujeres mezclados (las proporciones siguen el tamaño de las listas, en su mayoría entre la mitad y tres cuartos de hombres). Chinese, Japanese, Korean y Vietnamese
  también lo tienen. Las lenguas en las que la mezcla resultaría desequilibrada (turco, sur de Asia, África occidental y meridional, malayo-indonesio) solo tienen
  los grupos masculino y femenino.
- **Del pasado**: anglosajón, germánico medieval, inglés medieval, Oriente Próximo antiguo.
- **Nativos americanos**: `Native American - Male`, `- Female` y `- Surnames` (las categorías norteamericanas del Onomastikon:
  apache, sioux, iroqués, cherokee, inuit...) y lo mismo para Centroamérica y Sudamérica (azteca, maya, inca, amazónico).
- **Otros**: `English - Surnames` (patronímicos, oficios, lugares...), `Saints`, `Deities`, `Fantasy Creatures`, `Game of Thrones Houses`.

Para cada nombre, la extensión primero elige al azar una de las categorías del grupo y luego genera el nombre con la
cadena de esa categoría (con sus longitudes habituales). Un nombre suena a un pueblo y una lengua concretos en lugar de a una mezcla de todos ellos,
y una llamada devuelve nombres de varios. Las listas de los países, que se separan por origen o por letra, se eligen
según su tamaño (una lista de 600 nombres se elige más a menudo que una de 10); en los grupos de lenguas y países completos (turco,
caucásico, báltico, polinesio, regiones africanas, nativos americanos...) cada miembro tiene la misma probabilidad. Las fuentes de algunos
grupos son breves y en parte traducciones al inglés (*Calf-Woman*), así que los nombres son toscos.
Un grupo es una línea en `name-groups.json` (sus miembros y cómo se eligen); véase el [README del plugin](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-generator/README.md#groups).

Cada categoría tiene una cadena con la longitud que mejor le fue (las listas cortas tienen cadenas cortas, que repiten menos los originales)
y las longitudes que suelen tener sus nombres. Las listas en sí no forman parte de Marginalia: solo lo son las probabilidades,
así que los nombres originales no pueden leerse desde la extensión. Algunas de las fuentes tienen licencias propias; véase el
[README del plugin](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-generator/README.md).

<a id="usage"></a>

## Uso

Carga la extensión en *Admin → Extensiones*. Se aplica a la siguiente generación; las extensiones que la usan la encuentran al
cargarse, también si se instala después de ellas o si se desinstala y se vuelve a instalar: no hace falta reiniciar el Generador de lore. Sin ella y sin las demás suites, el Generador de lore no tiene el botón *Herramientas*.
