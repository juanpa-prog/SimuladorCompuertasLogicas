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
│   ├── fabrica/               Factory Method: CreadorCompuerta + un creador por compuerta
│   └── excepciones/
├── config/                    Singleton (enum Configuracion) + interfaz Preferencias
├── vista/                     dibujo y captura de eventos
│   ├── VentanaPrincipal (JFrame), PanelPaleta, PanelCircuito, PanelTablaVerdad
│   ├── contratos/             Dibujable, Arrastrable
│   └── componentes/           ComponenteVisual → VisualAND, VisualOR…, Cable, PuntoConexion
├── controlador/               ControladorCircuito: único puente vista ↔ modelo
└── pruebas/                   PruebasTablasVerdad (consola)
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
| Patrones de diseño | **Factory Method** (`CreadorCompuerta`), **Singleton** (`Configuracion`) con inyección por constructor. |
| GUI y eventos | `MouseAdapter` en el lienzo, `ActionListener` en botones, escuchadores propios hacia el controlador. |

Extensibilidad: agregar una compuerta (por ejemplo XNOR) = una clase de modelo, su creador, su
clase visual y **una línea** en `PanelPaleta`. No se toca ningún `switch` ni el código de evaluación.

## Decisiones de diseño

- **La posición (x, y) vive en la vista**, no en el modelo: la lógica no sabe dónde se dibuja.
- **Sin ciclos:** conectar una compuerta consigo misma (directa o indirectamente) lanza `ConexionInvalidaException`.
- **Entradas sin conectar** no se confunden con 0: evaluar lanza `EntradaSinConectarException` con un mensaje claro.
  Al dibujar, un pin sin conectar simplemente se ve vacío.
- **La interfaz está escrita a mano** (no con el diseñador de NetBeans) para mantener separadas las capas.
- Dibujo de las compuertas basado en el código del profesor (`GeneralPath`, relleno `(220, 240, 255)`,
  contorno negro de 3 px, antialiasing, `g.create()` + `translate`) y arrastre con `offset` para evitar saltos.

## Documentación

`docs/CasosDeUso.md` · `docs/DiagramaClases.md` · `docs/DiagramaObjetos.md` · `docs/ContratosGUI.md`
(diagramas en Mermaid; se visualizan en GitHub, VS Code o <https://mermaid.live>).
