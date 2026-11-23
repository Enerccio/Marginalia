---
label: Seguimiento de estado
order: 29
---

# Seguimiento de estado

Lleva el registro del **estado** actual de tus personajes: qué llevan puesto, qué cargan, el arma que empuñan, sus heridas, su ánimo o cualquier otra cosa que definas. Incluye en el prompt el estado de los personajes de la escena. Un modelo olvida con facilidad que la espada se perdió dos capítulos atrás o que la capa está empapada. Con el State Tracker, el prompt indica, para cada personaje seguido en la escena, qué es cierto de él ahora mismo.

!!!
Tú eliges quién se sigue y qué se registra de cada uno. Solo se describe al modelo a los personajes que añadas a la pestaña, y solo mientras estén entre los *personajes presentes* de la parte que se escribe. En esta versión tú mismo llevas el estado (o lo hace un agente con los comandos de abajo); seguir la historia con el modelo llegará más adelante.
!!!

<a id="categories"></a>

## Categorías

Una **categoría** es una cosa que puede registrarse de un personaje. Cada categoría tiene:

- un **nombre**, que es también lo que el prompt llama a la categoría (*Wearing*, *Weapon at hand*, *Carrying*);
- un **tipo**:
  - **Single value**: un valor breve, y un valor nuevo sustituye al anterior (el arma en la mano, la ubicación).
  - **List**: entradas que se añaden y se quitan una a una (lo que lleva encima, las heridas).
  - **Descripción**: un texto que se reescribe entero (la ropa, el ánimo).
- una **instrucción para el modelo**: qué entra en la categoría y qué no. El modelo la leerá cuando siga la historia.
- **Solo lo que dice el texto**: si está activado, el valor cambia solo cuando el texto lo dice; si está desactivado, también con las consecuencias evidentes (mojado tras un baño). El modelo usará esto cuando siga la historia.
- **Decir "none" si está vacío**: el prompt dice *none* cuando el personaje no tiene valor (un arma perdida), en lugar de omitir la categoría.

<a id="my-categories"></a>

### Mis categorías

Tus propias categorías están en **Settings → Extension Settings → State Tracker**. Los libros copian sus categorías de aquí. Hasta que las cambies, tienes cinco integradas: *Wearing*, *Weapon at hand*, *Carrying*, *Injuries* y *Mood*. Añade, edita y elimina categorías ahí y pulsa **Guardar** en la pantalla de ajustes. **Reset to the built-in categories** restaura las cinco integradas.

<a id="the-categories-of-a-book"></a>

### Las categorías de un libro

Un libro guarda **su propia copia** de cada categoría que usa, de modo que una copia de seguridad o una exportación del libro lo contiene todo, y borrar una de tus categorías nunca rompe un libro. El botón **Categories of this book** de la pestaña **Estado** lista estas categorías:

- **Add from my categories** copia una de tus categorías al libro.
- **New category for this book** crea una categoría que existe solo en este libro (la carga de un barco, una afinidad mágica). Una categoría así aparece marcada como *Only in this book*. **Save to my categories** la añade a tus categorías, para que otros libros también puedan usarla.
- Una copia que ya no es igual a tu categoría aparece marcada como *Differs from your category*. **Update from my categories** toma tu versión. No se actualiza nada sin que tú lo decidas, porque un cambio en la instrucción cambia lo que hace el modelo.
- Editar una categoría la cambia solo en este libro. Su tipo no puede cambiar mientras haya personajes con valores en ella.
- Borrar una categoría del libro borra los valores de todos los personajes en ella.

<a id="the-state-tab"></a>

## La pestaña Estado

La barra lateral izquierda del [editor de historias](../books/story-editor.md) tiene una pestaña **Estado**. Todo se guarda en el libro a medida que lo cambias.

**Track a character** pide un **nombre**, opcionalmente **aliases** (los otros nombres con los que aparece el personaje en el campo *Personajes presentes* de una parte, separados por comas), y las **tracked categories**. La lista ofrece las categorías del libro y las tuyas; si eliges una de las tuyas, se copia al libro. Un caballo puede seguir solo *Injuries*, mientras que la heroína sigue todo.

Cada personaje seguido muestra sus categorías, cada una con su valor o valores actuales:

- **+** fija el valor de una categoría de tipo single o descripción (el valor actual pasa al historial) o añade una entrada a una lista.
- El lápiz edita un valor; la cruz lo **elimina**: deja de ser cierto, así que pasa al historial y no se envía al modelo.
- El candado marca un valor como **bloqueado** (una cicatriz, la espada familiar). La función de ordenar, que llegará con el modelo, deja intactos los valores bloqueados.
- **History** muestra los valores pasados de la categoría y el motivo por el que terminaron (*replaced* o *removed*). **Restaurar** vuelve a hacer actual un valor pasado (en una categoría de tipo single o descripción, el valor actual pasa al historial en su lugar). Delete borra un valor de forma definitiva.

El lápiz del personaje edita su nombre, sus aliases y sus categorías. Desmarcar una categoría la oculta, pero conserva sus valores; volver a marcarla los recupera. La papelera deja de seguir al personaje y borra todo su estado.

<a id="in-the-prompt"></a>

## En el prompt

El botón del engranaje guarda los ajustes del libro:

- **In prompt** incluye el estado de los personajes seguidos de la escena en el prompt de cada generación (el engranaje se pone verde cuando está activado).
- **Depth** y **Rol** indican dónde va, igual que en la Nota del autor: *Depth* es el número de mensajes después de él (0 tras las instrucciones del turno, 1 justo antes de ellas).
- **Name separators** indican cómo se divide el campo *Personajes presentes* (por defecto `,` `;` `/` `|` y saltos de línea). Las notas entre corchetes se ignoran, así que `Mara (asleep), Fiora` encuentra ambas.

El prompt incluye los personajes de la escena que tengan algo que decir:

```
The current state of the characters of this scene. Keep to it; it changes only when the story changes it:

Fiora:
- Wearing: torn green cloak, leather boots
- Weapon at hand: none
- Carrying: rope; lantern
```

Puedes poner el mismo texto en cualquier lugar de una plantilla con la macro {%{`{{command::state-tracker-block::{{presentCharacters}}}}`}%} (cualquier lista de nombres sirve como argumento). La macro muestra el estado tanto si **In prompt** está activado como si no, así que desactívalo cuando uses la macro, o el modelo recibirá el estado dos veces.

<a id="commands"></a>

## Comandos

Para la barra de comandos y para los agentes:

| Comando | Qué hace |
|---|---|
| `state-status` | Los ajustes, las categorías del libro y las tuyas de las que no tiene copia. |
| `list-state-characters` | Los personajes seguidos con sus categorías. |
| `track-state-character` | Empieza a seguir a un personaje: nombre, aliases y categorías por nombre. |
| `stop-tracking-state-character` | Deja de seguir a un personaje y borra su estado (pide confirmación). |
| `list-character-state` | Los valores de un personaje, actuales y pasados, numerados. |
| `set-character-state` | Fija un valor: personaje, categoría, texto. |
| `remove-character-state` | Elimina un valor por su número: pasa al historial. |
| `state-tracker-block` | El texto del prompt para una lista de personajes (la macro). |
