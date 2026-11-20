# Formato del archivo de circuito (texto, versión 1)

Los circuitos se guardan en **archivos de texto UTF-8**, una línea por elemento. No se usa serialización de
objetos: el archivo se puede leer y corregir con cualquier editor, y cada campo se valida al cargarlo.

## Estructura

```
CIRCUITO;1;Circuito 1
ENTRADA;A;true;30;60
ENTRADA;B;false;30;170
ENTRADA;C;false;30;300
COMPUERTA;G1;AND;A;B;230;100
COMPUERTA;G2;NOT;C;230;300
COMPUERTA;G3;OR;G1;G2;440;190
COMPUERTA;G4;XOR;G3;A;650;330
SALIDA;F;G3;700;60
SALIDA;G;G4;890;350
```

Es exactamente el archivo que genera el botón «Guardar…» tras «Cargar ejemplo F y G»: `F = (A AND B) OR NOT(C)` y `G = XOR(F, A)`.

| Línea | Campos | Significado |
|---|---|---|
| Cabecera | `CIRCUITO;1;nombre` | Siempre es la primera línea. `1` es la versión del formato; el nombre es opcional al leer. |
| Entrada | `ENTRADA;nombre;true\|false[;x;y]` | Entrada lógica y su valor actual. |
| Compuerta | `COMPUERTA;nombre;TIPO;fuente1[;fuente2][;x;y]` | `TIPO` ∈ AND, OR, NOT, XOR, NAND, NOR. Una fuente por cada entrada de la compuerta (NOT tiene una, las demás dos). |
| Salida | `SALIDA;nombre;fuente[;x;y]` | Salida y la fuente que la alimenta. |

* **Fuente:** el nombre de una entrada o de una compuerta. Un guion (`-`) indica «entrada sin conectar», porque el
  simulador permite circuitos incompletos.
* **Posición `x;y`:** opcional, dos enteros al final de la línea. Es lo que el usuario movió en el lienzo; si falta, el
  programa acomoda el elemento en su columna.
* Las líneas en blanco se ignoran. Los espacios alrededor de cada campo se ignoran.
* Los nombres no pueden contener `;` ni saltos de línea, no pueden ser `-` y deben ser **únicos entre entradas,
  compuertas y salidas** (así una referencia nunca es ambigua).

## Cómo se lee: dos pasadas

1. **Primera pasada** (línea a línea, con `BufferedReader`): se valida cada línea y se crean entradas, compuertas y
   salidas **sin conectar**. Las conexiones quedan anotadas con su número de línea.
2. **Segunda pasada:** se enlazan las fuentes. Así una compuerta puede referirse a otra declarada más abajo en el
   archivo, y el orden de las líneas no importa. El propio modelo rechaza lo inválido (por ejemplo un ciclo).

## Errores y qué mensaje produce cada uno

Todos salen como `FormatoCircuitoException` con el número de línea (`Línea N: …`), salvo los de acceso al archivo.

| Problema en el archivo | Mensaje (resumen) |
|---|---|
| Archivo vacío | El archivo está vacío: falta la cabecera CIRCUITO;1. |
| Primera línea distinta de la cabecera | La primera línea debe ser la cabecera CIRCUITO;1. |
| Versión distinta de 1 | Versión de formato no soportada. |
| Cabecera repetida | La cabecera CIRCUITO solo puede aparecer una vez. |
| Palabra inicial desconocida | Tipo de línea desconocido. |
| Compuerta de un tipo que no existe | Tipo de compuerta desconocido. |
| Número de fuentes incorrecto | La compuerta AND necesita exactamente 2 entrada(s). |
| Fuente que no existe | La fuente «Z» no existe. |
| Nombre repetido o reservado | El nombre «A» está repetido / El nombre «-» está reservado. |
| Valor lógico distinto de true/false | Valor lógico inválido. |
| Posición no numérica o incompleta | La posición debe ser dos números enteros (x;y). |
| Ciclo o autoconexión | Conexión inválida: «G1» quedaría dependiendo de sí misma (ciclo). |
| Contenido que no es texto UTF-8 | El archivo no es un texto UTF-8 válido (¿está dañado?). |

Los problemas de acceso (el archivo no existe, no hay permisos, es una carpeta) salen como `IOException`
(`NoSuchFileException`, `AccessDeniedException`...), no como `FormatoCircuitoException`: «no pude abrirlo» es
distinto de «lo que abrí no es un circuito».

## Lo que garantiza

* **Abrir nunca modifica el circuito en pantalla** hasta que el archivo se haya leído y validado completo.
* **Guardar valida los nombres antes de escribir**, así que un intento fallido no deja un archivo a medias.
* Abrir y volver a guardar conserva el circuito: mismas entradas con sus valores, mismas compuertas y conexiones
  (incluidas las referencias compartidas, como `A` alimentando a `G1` y a `G4`) y mismas posiciones.
