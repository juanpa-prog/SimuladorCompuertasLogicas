# Pruebas

Las pruebas son programas de consola (sin librerías externas). Cada una imprime `[OK]` o `[FALLO]` por
verificación y termina con código 1 si algo falla.

## Cómo ejecutarlas

Desde la carpeta del proyecto:

```
javac -encoding UTF-8 -d build/classes $(find src -name "*.java")
java -cp build/classes compuertas.pruebas.PruebasTablasVerdad
java -cp build/classes compuertas.pruebas.PruebasPersistencia
java -Djava.awt.headless=true -cp build/classes compuertas.pruebas.PruebasControlador
```

En NetBeans: clic derecho sobre cada archivo de `src/compuertas/pruebas` → **Run File**.

## Qué cubre cada una

| Prueba | Verificaciones | Qué comprueba |
|---|---|---|
| `PruebasTablasVerdad` | 58 | Tabla de verdad de las seis compuertas; circuitos compuestos (`F = (A AND B) OR NOT(C)`, `G = XOR(F, A)`); polimorfismo; errores del modelo (ciclo, autoconexión, entrada inexistente, fuente ajena, duplicados, entrada libre); eliminar y desconectar; solo lectura de las colecciones; Singleton e inyección de preferencias; base para archivos (nombre dado, catálogo, `FormatoCircuitoException`). |
| `PruebasPersistencia` | 55 | **Ida y vuelta** del circuito F y G (misma tabla de verdad, referencias compartidas, valores); posiciones; entradas sin conectar; orden libre de las líneas; **22 archivos inválidos** con mensaje y línea; errores de acceso; nombres que no se pueden guardar y archivo anterior intacto. |
| `PruebasControlador` | 29 | Guardar → limpiar → abrir devuelve el mismo circuito y las mismas posiciones; abrir con formato inválido, ciclo, vacío o inexistente **conserva el circuito actual**; confirmaciones al reemplazar y al sobrescribir; archivo sin posiciones; error al guardar. Usa una vista falsa (sin ventana). |

**Total: 142 verificaciones.**

### Archivos inválidos probados (criterio «archivos corruptos»)
Vacío · solo líneas en blanco · cabecera incorrecta · sin versión · versión no soportada · nombre de circuito vacío ·
cabecera repetida · tipo de línea desconocido · tipo de compuerta desconocido · fuente inexistente (en compuerta y en
salida) · nombre duplicado · nombre repetido entre entrada y compuerta · NOT con dos entradas · AND con una · entrada
vacía · ENTRADA sin valor · valor lógico inválido · posición no numérica · posición incompleta · nombre reservado `-` ·
ciclo · autoconexión · contenido binario (no UTF-8).

## Lista de verificación manual de la interfaz (Swing)

La ventana real no tiene pruebas automáticas; antes de entregar, recorrer esta lista en NetBeans:

- [✓] La paleta agrega cada una de las 6 compuertas, una entrada y una salida.
- [✓] Una compuerta se arrastra por su cuerpo; el cable la sigue.
- [✓] Un clic sobre el punto de una entrada alterna 0 ↔ 1 y los indicadores se actualizan.
- [✓] Un cable se crea arrastrando de una salida a una entrada, y se rechaza un ciclo con un mensaje claro.
- [✓] «Evaluar» muestra `F = 1   G = 0` en el ejemplo.
- [✓] «Tabla de verdad» muestra 8 filas en el ejemplo.
- [✓] «Guardar…» crea el archivo (agrega `.txt` si no se escribió extensión) y avisa en la barra de estado.
- [✓] Guardar sobre un archivo existente pide confirmación.
- [✓] «Abrir…» del archivo guardado deja el circuito igual, con los componentes en el mismo lugar.
- [✓] Abrir un archivo con un error escrito a mano muestra «Línea N: …» y no cambia lo que había en pantalla.
- [✓] Abrir una imagen o un archivo que no es texto muestra el mensaje de contenido inválido.
- [✓] Tras abrir, agregar una compuerta nueva continúa la numeración (por ejemplo `G5`).
