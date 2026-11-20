# Simulador de Compuertas Lógicas (Java Swing · NetBeans)

Proyecto de **Programación Orientada a Objetos** que simula circuitos con las compuertas
**AND, OR, NOT, XOR, NAND y NOR**: las eliges en una paleta, las arrastras por el lienzo, las
conectas con cables, cambias los valores de las entradas y ves el resultado en tiempo real, con
su tabla de verdad.

## Cómo abrirlo en NetBeans

1. Descomprime el `.zip`.
2. En NetBeans: **File → Open Project…** y selecciona la carpeta `SimuladorCompuertasLogicas`.
3. Clic derecho sobre el proyecto → **Run** (clase principal: `compuertas.Main`).

Requiere **JDK 11 o superior**. Si NetBeans pregunta por generar archivos de compilación
(`build-impl.xml`), acepta: los crea él mismo.

Sin NetBeans, desde la carpeta del proyecto:

```
javac -encoding UTF-8 -d build/classes $(find src -name "*.java")
java -cp build/classes compuertas.Main
java -cp build/classes compuertas.pruebas.PruebasTablasVerdad
java -cp build/classes compuertas.pruebas.PruebasPersistencia
java -Djava.awt.headless=true -cp build/classes compuertas.pruebas.PruebasControlador
```

## Cómo se usa

| Acción | Cómo |
|---|---|
| Agregar un elemento | Clic en su botón de la paleta (izquierda). Aparece en un lugar libre del lienzo. |
| Mover | Clic sobre el **cuerpo** de la compuerta y arrastrar. |
| Cambiar una entrada | Clic (sin arrastrar) sobre el **punto** de una entrada (A, B, C…): alterna 0 ↔ 1. |
| Conectar | Arrastrar desde el punto de una **salida** hasta el punto de una **entrada** (también sirve en sentido contrario). Un cable nuevo reemplaza al anterior de esa entrada. |
| Eliminar | Seleccionar con un clic (compuerta o cable) y pulsar **Supr** o «Eliminar selección». |
| Evaluar | «Evaluar» muestra `F = 1   G = 0` en la barra inferior. |
| Tabla de verdad | «Tabla de verdad» genera todas las combinaciones de las entradas. |
| Ver un ejemplo | «Cargar ejemplo F y G»: `F = (A AND B) OR NOT(C)` y `G = XOR(F, A)`. |
| Guardar | «Guardar…» escribe el circuito (con la posición de cada componente) en un archivo de texto `.txt`. Si el archivo existe, pide confirmación. |
| Abrir | «Abrir…» lee un `.txt`. Si el archivo no existe o no es válido, muestra el motivo (con el número de línea) y **no cambia** el circuito que estás viendo. |

**Indicadores de valor:** cada extremo de cable tiene un círculo. **Vacío = 0**, **relleno (verde) = 1**.
Los cables que llevan un 1 también se ven en verde. Todo se actualiza solo al cambiar algo.

## Arquitectura (MVC)

```
src/compuertas/
├── Main.java                  arranca en el hilo de eventos (invokeLater)
├── modelo/                    lógica pura, SIN Swing
│   ├── FuenteLogica           interfaz: todo lo que entrega un valor
│   ├── EntradaLogica, SalidaLogica
│   ├── Compuerta              abstracta (implementa FuenteLogica)
│   │   ├── CompuertaNOT
│   │   └── CompuertaBinaria   abstracta → AND, OR, XOR, NAND, NOR
│   ├── Circuito               compone entradas, compuertas y salidas
│   ├── TablaVerdad
│   ├── fabrica/               Factory Method: CreadorCompuerta + un creador por compuerta,
│   │                          y CatalogoCompuertas (tipo en texto → creador)
│   └── excepciones/           CircuitoException (y sus hijas) y FormatoCircuitoException
├── persistencia/              guardar/abrir en archivos de texto (sin serialización)
│   ├── RepositorioCircuitos   interfaz; RepositorioTexto la implementa
│   └── PosicionesCircuito, CircuitoCargado
├── config/                    Singleton (enum Configuracion) + interfaz Preferencias
├── vista/                     dibujo y captura de eventos
│   ├── VentanaPrincipal (JFrame), PanelPaleta, PanelCircuito, PanelTablaVerdad
│   ├── contratos/             Dibujable, Arrastrable
│   └── componentes/           ComponenteVisual → VisualAND, VisualOR…, Cable, PuntoConexion,
│                              CatalogoVisuales
├── controlador/               ControladorCircuito: atiende los eventos y usa el repositorio
└── pruebas/                   PruebasTablasVerdad, PruebasPersistencia, PruebasControlador (consola)
```

## Conceptos de POO y dónde verlos

| Concepto | Dónde |
|---|---|
| Clases y encapsulación | Atributos privados, constructores que validan, nombres `final`, sin setters innecesarios. |
| Herencia | `Compuerta → CompuertaBinaria → AND/OR/XOR/NAND/NOR`; `ComponenteVisual → VisualCompuerta → VisualAND…` |
| Clases abstractas e interfaces | `Compuerta`, `CompuertaBinaria`, `ComponenteVisual`, `CreadorCompuerta`; `FuenteLogica`, `Dibujable`, `Arrastrable`, `Preferencias`. |
| Polimorfismo dinámico | Un solo bucle `c.evaluar()` sobre `List<Compuerta>`; el lienzo llama `dibujar(g)` a cada componente. Sin `instanceof` ni `if/else` por tipo. |
| Polimorfismo estático (sobrecarga) | Constructores de `EntradaLogica`, de las compuertas y de los componentes visuales; `Circuito.agregarEntrada()` / `agregarEntrada(String)`. |
| Colecciones de objetos | `ArrayList` encapsulados en `Circuito` y `PanelCircuito`; solo se exponen vistas de solo lectura. |
| Asociación / composición | `Circuito ◆ Compuerta`; `Compuerta → FuenteLogica`; `ComponenteVisual → Compuerta`. |
| Patrones de diseño | **Factory Method** (`CreadorCompuerta`), **Singleton** (`Configuracion`) con inyección por constructor. Justificación en `docs/Decisiones.md`. |
| Diseño y SOLID | Modelo separado de la vista; el controlador depende de las interfaces `VistaCircuito` y `RepositorioCircuitos`. Detalle por principio en `docs/Decisiones.md`. |
| Excepciones | `CircuitoException` (no verificada) con `ConexionInvalidaException` y `EntradaSinConectarException`; `FormatoCircuitoException` (verificada, con número de línea) para archivos inválidos. Se capturan en el controlador y se muestran con mensajes claros. |
| Archivos | `RepositorioTexto`: `Path`/`Files`, `BufferedReader`/`BufferedWriter` y `try-with-resources`; formato de texto versionado y validado (`docs/FormatoArchivo.md`). |
| GUI y eventos | `MouseAdapter` en el lienzo, `ActionListener` en botones, escuchadores propios hacia el controlador. |

Extensibilidad: agregar una compuerta (por ejemplo XNOR) = una clase de modelo, su creador, su
clase visual y **registrarla** en tres tablas (`CatalogoCompuertas`, `CatalogoVisuales` y una línea en
`PanelPaleta`). No se toca ningún `switch`, ni `Circuito`, ni el repositorio, ni el código de evaluación.

## Decisiones de diseño

- **La posición (x, y) vive en la vista**, no en el modelo: la lógica no sabe dónde se dibuja.
- **Sin ciclos:** conectar una compuerta consigo misma (directa o indirectamente) lanza `ConexionInvalidaException`.
- **Entradas sin conectar** no se confunden con 0: evaluar lanza `EntradaSinConectarException` con un mensaje claro.
  Al dibujar, un pin sin conectar simplemente se ve vacío.
- **Guardado en texto, no en objetos serializados:** el archivo es legible, versionado y se valida al leer
  (cada error indica la línea). El modelo no conoce los archivos.
- **Abrir no destruye:** se lee a un circuito nuevo y solo se reemplaza el actual si el archivo es válido por completo.
- **Dos clases de error:** `IOException` (no pude acceder al archivo) y `FormatoCircuitoException` (lo que leí no es un circuito).
- **La interfaz está escrita a mano** (no con el diseñador de NetBeans) para mantener separadas las capas.
- Dibujo de las compuertas basado en el código del profesor (`GeneralPath`, relleno `(220, 240, 255)`,
  contorno negro de 3 px, antialiasing, `g.create()` + `translate`) y arrastre con `offset` para evitar saltos.

## Pruebas

Tres programas de consola (142 verificaciones): `PruebasTablasVerdad` (modelo), `PruebasPersistencia`
(archivos válidos e inválidos) y `PruebasControlador` (guardar/abrir con una vista falsa, sin ventana).
Cómo ejecutarlas y la lista de verificación manual de la interfaz: `docs/Pruebas.md`.

## Documentación

| Documento | Contenido |
|---|---|
| `docs/CasosDeUso.md` | Diagrama de casos de uso y especificación de conectar, evaluar, guardar y abrir. |
| `docs/DiagramaClases.md` | Diagramas de clases: modelo, excepciones, fábrica y Singleton, persistencia, vista y controlador. |
| `docs/DiagramaObjetos.md` | Red de objetos del ejemplo F y G y su equivalente en archivo. |
| `docs/ContratosGUI.md` | Interfaces y clases abstractas, y quién las implementa. |
| `docs/FormatoArchivo.md` | Formato del archivo de texto, cómo se lee y qué errores detecta. |
| `docs/Decisiones.md` | Justificación de patrones, SOLID, excepciones y persistencia; limitaciones conocidas. |
| `docs/Pruebas.md` | Qué cubre cada prueba y lista de verificación manual. |

Los diagramas están en Mermaid; se visualizan en GitHub, VS Code o <https://mermaid.live>.
