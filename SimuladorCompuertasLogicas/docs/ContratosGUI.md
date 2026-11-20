# Contratos (interfaces) y quién los implementa

| Contrato | Paquete | Qué exige | Implementado por |
|---|---|---|---|
| `FuenteLogica` | `modelo` | `getNombre()`, `getValor()` (y `dependeDe`, con valor por defecto) | `EntradaLogica`, `Compuerta` y por herencia las seis compuertas |
| `Dibujable` | `vista.contratos` | `dibujar(Graphics2D)` | `ComponenteVisual` (y por herencia `VisualAND`, `VisualOR`, `VisualNOT`, `VisualXOR`, `VisualNAND`, `VisualNOR`, `VisualEntrada`, `VisualSalida`) y `Cable` |
| `Arrastrable` | `vista.contratos` | `contiene`, `moverA`, `getX`, `getY` | `ComponenteVisual` y todas sus subclases (los cables **no** se arrastran) |
| `Preferencias` | `config` | `isMostrarCuadricula`, `setMostrarCuadricula` | `Configuracion` (enum Singleton) |
| `EscuchadorPanel` | `vista` | `alConectar`, `alHacerClicEnPin`, `alEliminarSeleccion` | `ControladorCircuito` |
| `EscuchadorVentana` | `vista` | eventos de la paleta y la barra (incluye `alGuardar` y `alAbrir`) | `ControladorCircuito` |
| `VistaCircuito` | `vista` | lo que el controlador necesita de la ventana (mensajes, confirmar, elegir archivo para guardar y abrir) | `VentanaPrincipal` (y una vista falsa en `PruebasControlador`) |
| `RepositorioCircuitos` | `persistencia` | `guardar(circuito, posiciones, ruta)` y `cargar(ruta)` | `RepositorioTexto` |

## Clases abstractas

| Clase | Por qué es abstracta |
|---|---|
| `Compuerta` | No existe una "compuerta genérica"; define lo común y obliga a cada subclase a dar su regla en `evaluar()`. |
| `CompuertaBinaria` | Agrupa las cinco compuertas de dos entradas para no repetir código. `NOT` queda fuera porque solo tiene una. |
| `CreadorCompuerta` | Es el *Creator* del Factory Method: delega en cada creador concreto qué compuerta instanciar. |
| `ComponenteVisual` | Fija cómo se dibuja y se arrastra; cada subclase aporta solo su forma. |
| `VisualCompuerta` | Construye los pines según cuántas entradas tiene su compuerta. |

## Dónde se evita el `if/else` por tipo

- `Circuito.evaluarTodas()` y `evaluarSalidas()` llaman a `evaluar()` / `getValor()` sin preguntar el tipo.
- `PanelCircuito.paintComponent` llama a `dibujar(g)` de cada componente sin preguntar el tipo.
- `PanelPaleta` crea cada elemento con su propio `Constructor` (lambda), no con un `switch`.
- `CatalogoCompuertas` y `CatalogoVisuales` pasan de un tipo en texto (`"AND"`) al creador o al dibujo correcto con una tabla, no con un `switch`; así abrir un archivo no pregunta el tipo con `if`.
- `RepositorioTexto` guarda las fuentes de una compuerta con `FuenteLogica.getNombre()`, sin preguntar si son entradas o compuertas.
- `ComponenteVisual.getTipoElemento`, `getNombreElemento` y `getFuenteConectada` permiten guardar posiciones y reconstruir cables por polimorfismo.
- `ComponenteVisual.eliminarDelModelo`, `conectarEntrada` y `alActivarPin` se resuelven por polimorfismo.
