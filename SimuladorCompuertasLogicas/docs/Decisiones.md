# Decisiones de diseño y justificación

Este documento reúne el *por qué* de las decisiones principales, para poder explicarlas en la sustentación.
Al final se listan, con honestidad, las limitaciones conocidas.

## 1. Persistencia: archivos de texto, sin serialización de objetos

**Decisión.** El circuito se guarda en un archivo `.txt` con un formato propio y versionado
(ver `FormatoArchivo.md`). Ninguna clase del modelo implementa `Serializable` ni se usan
`ObjectOutputStream` / `ObjectInputStream`.

**Por qué.**
* El archivo es **legible y verificable a mano**: se puede abrir en un editor, corregir y comparar.
* Cada campo se **valida al leer**, así que un archivo dañado produce un mensaje con el número de línea en
  lugar de una falla genérica.
* El formato lleva **versión** (`CIRCUITO;1`), de modo que un cambio futuro se detecta y no se interpreta mal.
* El modelo queda libre de preocupaciones de persistencia (no hay `transient`, ni compatibilidad de clases):
  `Circuito` no sabe que existen archivos (**DIP**: depende de la interfaz `RepositorioCircuitos`).

**Costo asumido.** Hay que escribir y mantener el lector (`RepositorioTexto`). A cambio, se controla exactamente
qué se acepta.

> Las `serialVersionUID` que aparecen en las excepciones no tienen relación con la persistencia: el compilador las
> sugiere porque `Throwable` es `Serializable`.

## 2. Excepciones: cuál es verificada y por qué

| Excepción | Tipo | Razón |
|---|---|---|
| `CircuitoException` y sus hijas (`ConexionInvalidaException`, `EntradaSinConectarException`) | **No verificadas** (`RuntimeException`) | Representan una **precondición violada** por quien usa el modelo (conectar en ciclo, evaluar con una entrada libre). Obligar a capturarlas en cada llamada ensuciaría todo el código sin que el llamador pueda corregir nada en ese punto. El controlador las captura **una vez**, en el borde, y muestra el mensaje. |
| `FormatoCircuitoException` | **Verificada** (`Exception`) | Un archivo corrupto es una falla **esperable y recuperable**: el usuario puede elegir otro. El compilador obliga a decidir qué hacer. |
| `IOException` (y `NoSuchFileException`, `AccessDeniedException`) | Verificada (estándar) | Problemas de acceso. Se distinguen de `FormatoCircuitoException` porque «no pude abrirlo» no es lo mismo que «lo que abrí es inválido». |

Otras decisiones:
* **Mensajes pensados para el usuario**, no para el programador (por ejemplo, «Línea 3: Tipo de compuerta desconocido «XNOR»»).
* **Se conserva la causa** (`CircuitoException(String, Throwable)` y `FormatoCircuitoException(String, int, Throwable)`),
  de modo que al traducir una excepción interna no se pierde su origen.
* **Un solo lugar atrapa y muestra**: `ControladorCircuito`. El modelo y el repositorio lanzan; la vista no decide qué hacer.
* **Fallar sin dañar:** abrir carga en un circuito nuevo y solo reemplaza el actual si todo salió bien; guardar valida
  los nombres antes de abrir el archivo.

## 3. Patrones de diseño

### Factory Method — `CreadorCompuerta`
* **Dónde:** `CreadorCompuerta` (abstracta) y un creador concreto por compuerta (`CreadorAND`, `CreadorOR`...).
  `Circuito.agregarCompuerta(creador)` recibe un creador y obtiene la compuerta sin conocer su clase concreta.
* **Qué problema resuelve:** el circuito, la paleta y el lector de archivos necesitan *crear* compuertas sin llenarse
  de `if (tipo.equals("AND")) new CompuertaAND(...)`. El método de fábrica mueve esa decisión a una jerarquía.
* **Beneficio concreto:** agregar una compuerta nueva no obliga a tocar `Circuito`.
* **Cuándo no conviene:** si hubiera una sola clase de compuerta, o si crearla fuera trivial y nunca cambiara, un
  `new` directo sería más simple. La fábrica se justifica porque hay seis tipos y crecen.
* **Complemento:** `CatalogoCompuertas` es una **tabla** tipo → creador (no un patrón GoF): permite pasar de un
  nombre de texto («AND», leído de un archivo) al creador correcto sin `switch`.

### Singleton — `Configuracion` (enum)
* **Dónde:** `enum Configuracion { INSTANCIA }`, que guarda las preferencias de la interfaz (mostrar la cuadrícula).
* **Por qué un enum:** es la forma más simple y segura de garantizar una única instancia en Java (el lenguaje evita
  que se cree otra y es seguro entre hilos).
* **Cuándo conviene:** hay un único conjunto de preferencias para toda la aplicación y todos los que lo consultan
  deben ver el mismo valor.
* **Cuándo NO conviene:**
  * **Para el circuito, las compuertas o las entradas**: pueden existir varios circuitos y muchas compuertas; un
    Singleton ahí impediría tener dos circuitos o escribir pruebas independientes.
  * Cuando se usa solo por comodidad para evitar pasar parámetros: esconde dependencias.
* **Riesgo y cómo se reduce:** el estado global dificulta las pruebas. Por eso `PanelCircuito` **recibe un
  `Preferencias` por el constructor** (inyección) en lugar de llamar directamente a `Configuracion.INSTANCIA`, y
  `PruebasTablasVerdad` comprueba que funciona con otra implementación.

### Patrones que no se usaron
**Prototype** no se aplicó: no hay una necesidad real de duplicar compuertas, y meterlo solo para tener más
patrones habría sido forzado. Se prefirió aplicar bien dos patrones que acumular tres.

### Otras estructuras (no son patrones GoF completos)
* **MVC:** `modelo` / `vista` / `controlador` separados; el modelo no importa Swing.
* **Escuchadores propios** (`EscuchadorPanel`, `EscuchadorVentana`): se parecen a Observer, pero con un único
  escuchador, y desacoplan la vista del controlador.

## 4. Principios SOLID: dónde se ven

| Principio | Evidencia en el proyecto |
|---|---|
| **S** · Responsabilidad única | `Circuito` gestiona elementos y conexiones; `TablaVerdad` solo calcula la tabla; `RepositorioTexto` solo conoce el formato del archivo; `PanelCircuito` dibuja y captura el mouse; `ControladorCircuito` atiende eventos. |
| **O** · Abierto/cerrado | Evaluar, dibujar y guardar un circuito funcionan sin tocar código al agregar una compuerta: se resuelven por polimorfismo (`evaluar()`, `dibujar(g)`) o por tablas de registro (`CatalogoCompuertas`, `CatalogoVisuales`). |
| **L** · Sustitución de Liskov | Cualquier `Compuerta` se usa donde se espera una `Compuerta` o una `FuenteLogica`: `Circuito` las evalúa sin saber cuál es. Las seis cumplen el mismo contrato (`evaluar()` según su tabla de verdad). |
| **I** · Segregación de interfaces | Interfaces pequeñas y específicas: `FuenteLogica` (3 métodos), `Dibujable`, `Arrastrable`, `Preferencias` (2), `RepositorioCircuitos` (2), y dos escuchadores separados en lugar de uno enorme. |
| **D** · Inversión de dependencias | `ControladorCircuito` depende de las **interfaces** `VistaCircuito` y `RepositorioCircuitos`, no de `VentanaPrincipal` ni de `RepositorioTexto`; `PanelCircuito` depende de `Preferencias`; `Compuerta` depende de `FuenteLogica`. Por eso `PruebasControlador` prueba el controlador con una vista falsa, sin ventana. |

**Añadir una compuerta nueva (ejemplo XNOR)** requiere: la clase de modelo, su creador, su clase visual, y **registrarla**
en tres tablas (`CatalogoCompuertas`, `CatalogoVisuales` y la paleta en `PanelPaleta`). No se modifican `Circuito`,
`TablaVerdad`, el repositorio ni el código de evaluación.

## 5. Limitaciones conocidas y mejoras posibles

Se anotan para ser transparentes y poder defender cada punto:

1. **La paleta toca el modelo.** Las lambdas de `PanelPaleta` crean la compuerta en el modelo y su dibujo a la vez,
   así que el controlador no es el *único* puente entre vista y modelo (por eso el README dice «principal»).
   Mejora posible: que la paleta solo emita un evento y el controlador cree todo.
2. **`ComponenteVisual` ofrece métodos que no todas las subclases soportan** (`conectarEntrada` lanza una excepción
   en `VisualEntrada`, que no tiene entradas). Es un compromiso leve frente a Liskov; la alternativa sería una
   interfaz aparte para componentes con entradas.
3. **`ControladorCircuito` concentra bastante:** además de los eventos, construye el ejemplo y reconstruye la vista
   al abrir un archivo. Podría dividirse si creciera.
4. **Tres registros por compuerta nueva** (sección 4): es explícito y simple, pero no es automático.
5. **El guardado escribe directamente sobre el archivo.** Los nombres se validan antes de abrirlo, pero una falla
   del disco *durante* la escritura podría dejar el archivo incompleto (no se usa archivo temporal).
6. **La interfaz Swing se probó mediante una vista falsa**, no con pruebas automáticas de la ventana real; por eso
   `Pruebas.md` incluye una lista de verificación manual.
7. **`Configuracion`** guarda un único dato (mostrar cuadrícula): el Singleton está aplicado de forma correcta, pero
   su alcance es pequeño.
