# Diagrama de objetos — F = (A AND B) OR NOT(C) y G = XOR(F, A)

Es la red de objetos que construye el botón **«Cargar ejemplo F y G»** (y la prueba de consola).
Cada flecha es una referencia: la compuerta guarda la fuente que alimenta cada una de sus entradas.

```mermaid
flowchart LR
    A["A : EntradaLogica<br/>valor = 1"]
    B["B : EntradaLogica<br/>valor = 0"]
    C["C : EntradaLogica<br/>valor = 0"]
    G1["G1 : CompuertaAND"]
    G2["G2 : CompuertaNOT"]
    G3["G3 : CompuertaOR"]
    G4["G4 : CompuertaXOR"]
    F["F : SalidaLogica"]
    G["G : SalidaLogica"]

    A -- "entrada 1" --> G1
    B -- "entrada 2" --> G1
    C -- "entrada 1" --> G2
    G1 -- "entrada 1" --> G3
    G2 -- "entrada 2" --> G3
    G3 -- "entrada 1" --> G4
    A -- "entrada 2" --> G4
    G3 --> F
    G4 --> G
```

Con A=1, B=0, C=0: G1 = 0, G2 = 1, **G3 = F = 1**, **G4 = G = 0**.

## Tabla de verdad (la que genera el simulador)

| A | B | C | F | G |
|---|---|---|---|---|
| 0 | 0 | 0 | 1 | 1 |
| 0 | 0 | 1 | 0 | 0 |
| 0 | 1 | 0 | 1 | 1 |
| 0 | 1 | 1 | 0 | 0 |
| 1 | 0 | 0 | 1 | 0 |
| 1 | 0 | 1 | 0 | 1 |
| 1 | 1 | 0 | 1 | 0 |
| 1 | 1 | 1 | 1 | 0 |

## Equivalente en archivo

Este mismo circuito, guardado en texto, tiene una línea por objeto (ver `FormatoArchivo.md`):

```
CIRCUITO;1;Tarea
ENTRADA;A;true
ENTRADA;B;false
ENTRADA;C;false
COMPUERTA;G1;AND;A;B
COMPUERTA;G2;NOT;C
COMPUERTA;G3;OR;G1;G2
COMPUERTA;G4;XOR;G3;A
SALIDA;F;G3
SALIDA;G;G4
```

Cada nombre de una línea (`A`, `G1`...) es una referencia a otro objeto: al abrir el archivo se vuelven a crear los
objetos y se enlazan por nombre, de modo que `A` sigue siendo **un único objeto** compartido por `G1` y `G4`.
