---
label: Estadísticas
order: 87
---

# Estadísticas

Las [estadísticas de escritura](../books/statistics.md) de cada usuario (las palabras, el tiempo dedicado, el tamaño de cada libro, día a día)
se guardan en la base de datos. Registran cuándo trabajaron los usuarios en sus libros y los nombres y carpetas de los libros, también
los de los libros que se han borrado. La pestaña **Estadísticas** de la página de administración te permite limitarlo. Los usuarios
pueden hacer lo mismo para sí mismos en sus [ajustes](../account.md) y borrar sus propias estadísticas en las ventanas de estadísticas.

![La pestaña Estadísticas de la página de administración](../../images/admin-statistics.png)

| | |
|---|---|
| **Registrar las estadísticas de escritura** | Desactivado detiene el registro para todos los usuarios, sea cual sea su elección. Lo que ya se había registrado no se borra. Las ventanas de estadísticas indican que no se cuenta nada nuevo. |
| **Conservar estadísticas durante (meses)** | Los días más antiguos que ese plazo se borran de forma definitiva de las estadísticas de todos los usuarios, una vez al día (a eso de las 4:20, y poco después del arranque) y al guardar. Vacío o 0 conserva todo. Un usuario puede elegir un plazo más corto para sí mismo, nunca más largo. |
| **Borrar las estadísticas del usuario** | Borra todas las estadísticas del usuario elegido, previa confirmación. Sus libros no se modifican. |
| **Borrar las estadísticas de todos** | Lo mismo, para todos los usuarios. |

Las estadísticas borradas no pueden recuperarse, salvo desde una [copia de seguridad de la base de datos](database-backups.md): las estadísticas
forman parte de la base de datos (la copia de un único libro no las incluye).

La [limpieza](cleanup.md) no toca las estadísticas, porque no son objetos borrados: guardan el libro por su id y lo sobreviven a propósito.
