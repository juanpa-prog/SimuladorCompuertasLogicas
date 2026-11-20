# Diagramas de clases (UML)

Visibilidad: `+` público, `-` privado, `#` protegido. Triángulo hueco = herencia o implementación,
rombo negro = composición, rombo hueco = agregación, flecha simple = asociación, flecha punteada =
dependencia. Un `*` junto al nombre de un método indica que es abstracto.

El diagrama se divide en cinco partes para que cada una se pueda leer completa:
1. Modelo · 2. Excepciones · 3. Fábrica, catálogo y configuración · 4. Persistencia en texto ·
5. Vista y controlador.

## 1. Modelo

```mermaid
classDiagram
    direction TB

    class FuenteLogica {
        <<interface>>
        +getNombre() String
        +getValor() boolean
        +dependeDe(otra) boolean
    }
    class EntradaLogica {
        -String nombre
        -boolean valor
        +EntradaLogica(boolean)
        +EntradaLogica(String)
        +EntradaLogica(String, boolean)
        +getNombre() String
        +getValor() boolean
        +setValor(boolean)
        +alternar()
    }
    class Compuerta {
        <<abstract>>
        -String nombre
        +getNombre() String
        +getTipo()* String
        +getNumeroEntradas()* int
        +getEntrada(int)* FuenteLogica
        #asignarEntrada(int, FuenteLogica)*
        +evaluar()* boolean
        +getValor() boolean
        +conectar(int, FuenteLogica)
        +desconectar(int)
        +desconectarFuente(FuenteLogica)
        +estaCompleta() boolean
        +dependeDe(FuenteLogica) boolean
    }
    class CompuertaBinaria {
        <<abstract>>
        -FuenteLogica a
        -FuenteLogica b
        #valorA() boolean
        #valorB() boolean
    }
    class CompuertaNOT {
        -FuenteLogica entrada
        +evaluar() boolean
    }
    class CompuertaAND {
        +evaluar() boolean
    }
    class CompuertaOR {
        +evaluar() boolean
    }
    class CompuertaXOR {
        +evaluar() boolean
    }
    class CompuertaNAND {
        +evaluar() boolean
    }
    class CompuertaNOR {
        +evaluar() boolean
    }
    class SalidaLogica {
        -String nombre
        -FuenteLogica fuente
        +getNombre() String
        +getFuente() FuenteLogica
        +conectar(FuenteLogica)
        +desconectar()
        +getValor() boolean
    }
    class Circuito {
        -String nombre
        -List~EntradaLogica~ entradas
        -List~Compuerta~ compuertas
        -List~SalidaLogica~ salidas
        +agregarEntrada() EntradaLogica
        +agregarEntrada(String) EntradaLogica
        +agregarSalida() SalidaLogica
        +agregarSalida(String) SalidaLogica
        +agregarCompuerta(CreadorCompuerta) Compuerta
        +agregarCompuerta(CreadorCompuerta, String) Compuerta
        +conectar(Compuerta, int, FuenteLogica)
        +conectarSalida(SalidaLogica, FuenteLogica)
        +eliminarEntrada(EntradaLogica) boolean
        +eliminarCompuerta(Compuerta) boolean
        +eliminarSalida(SalidaLogica) boolean
        +buscarEntrada(String) EntradaLogica
        +buscarCompuerta(String) Compuerta
        +buscarSalida(String) SalidaLogica
        +evaluarTodas() Map
        +evaluarSalidas() Map
        +generarTablaVerdad() TablaVerdad
        +limpiar()
        +getEntradas() List
        +getCompuertas() List
        +getSalidas() List
    }
    class TablaVerdad {
        -List~String~ encabezados
        -boolean[][] filas
        +getValor(int, int) boolean
    }

    FuenteLogica <|.. EntradaLogica
    FuenteLogica <|.. Compuerta
    Compuerta <|-- CompuertaBinaria
    Compuerta <|-- CompuertaNOT
    CompuertaBinaria <|-- CompuertaAND
    CompuertaBinaria <|-- CompuertaOR
    CompuertaBinaria <|-- CompuertaXOR
    CompuertaBinaria <|-- CompuertaNAND
    CompuertaBinaria <|-- CompuertaNOR

    Circuito "1" *-- "0..*" EntradaLogica
    Circuito "1" *-- "0..*" Compuerta
    Circuito "1" *-- "0..*" SalidaLogica
    Compuerta --> "0..2" FuenteLogica : entradas
    SalidaLogica --> "0..1" FuenteLogica : fuente
    Circuito ..> TablaVerdad : crea
```

## 2. Excepciones

```mermaid
classDiagram
    direction TB
    class RuntimeException
    class Exception
    class CircuitoException {
        +CircuitoException(String)
        +CircuitoException(String, Throwable)
    }
    class ConexionInvalidaException
    class EntradaSinConectarException
    class FormatoCircuitoException {
        -int linea
        +FormatoCircuitoException(String)
        +FormatoCircuitoException(String, int)
        +FormatoCircuitoException(String, int, Throwable)
        +getLinea() int
    }

    RuntimeException <|-- CircuitoException
    CircuitoException <|-- ConexionInvalidaException
    CircuitoException <|-- EntradaSinConectarException
    Exception <|-- FormatoCircuitoException
```

| Excepción | Tipo | Cuándo se lanza |
|---|---|---|
| `CircuitoException` | **no verificada** | Base: uso incorrecto del modelo (nombre duplicado, tabla sin entradas...). |
| `ConexionInvalidaException` | no verificada | Ciclo, autoconexión, entrada inexistente, fuente ajena al circuito. |
| `EntradaSinConectarException` | no verificada | Se evalúa algo con una entrada sin conectar. |
| `FormatoCircuitoException` | **verificada** | El archivo se abrió pero su contenido no es un circuito válido (indica la línea). |

Las fallas de acceso al archivo (no existe, sin permisos) **no** tienen clase propia: se propagan como
`java.io.IOException`, para no confundir «no se pudo leer» con «lo que se leyó es inválido».

## 3. Fábrica (Factory Method), catálogo y configuración (Singleton)

```mermaid
classDiagram
    direction LR
    class CreadorCompuerta {
        <<abstract>>
        +getTipo()* String
        #crearCompuerta(String)* Compuerta
        +crear(String, FuenteLogica...) Compuerta
    }
    class CreadorAND
    class CreadorOR
    class CreadorNOT
    class CreadorXOR
    class CreadorNAND
    class CreadorNOR
    class CatalogoCompuertas {
        <<final>>
        +existe(String) boolean
        +obtener(String) CreadorCompuerta
        +tipos() List
    }
    class Compuerta {
        <<abstract>>
    }

    CreadorCompuerta <|-- CreadorAND
    CreadorCompuerta <|-- CreadorOR
    CreadorCompuerta <|-- CreadorNOT
    CreadorCompuerta <|-- CreadorXOR
    CreadorCompuerta <|-- CreadorNAND
    CreadorCompuerta <|-- CreadorNOR
    CreadorCompuerta ..> Compuerta : crea
    CatalogoCompuertas o-- "6" CreadorCompuerta : por tipo

    class Preferencias {
        <<interface>>
        +isMostrarCuadricula() boolean
        +setMostrarCuadricula(boolean)
    }
    class Configuracion {
        <<enumeration>>
        INSTANCIA
        +isMostrarCuadricula() boolean
        +setMostrarCuadricula(boolean)
    }
    class PanelCircuito
    Preferencias <|.. Configuracion
    PanelCircuito --> Preferencias : recibe por constructor
```

## 4. Persistencia en texto

```mermaid
classDiagram
    direction TB
    class RepositorioCircuitos {
        <<interface>>
        +guardar(Circuito, PosicionesCircuito, Path)
        +cargar(Path) CircuitoCargado
    }
    class RepositorioTexto {
        <<final>>
        +CABECERA String
        +VERSION int
        +guardar(Circuito, PosicionesCircuito, Path)
        +cargar(Path) CircuitoCargado
    }
    class CircuitoCargado {
        -Circuito circuito
        -PosicionesCircuito posiciones
        +getCircuito() Circuito
        +getPosiciones() PosicionesCircuito
    }
    class PosicionesCircuito {
        +poner(Tipo, String, int, int)
        +obtener(Tipo, String) Posicion
        +cantidad() int
    }
    class Posicion {
        -int x
        -int y
    }
    class Tipo {
        <<enumeration>>
        ENTRADA
        COMPUERTA
        SALIDA
    }
    class Circuito
    class CatalogoCompuertas
    class FormatoCircuitoException

    RepositorioCircuitos <|.. RepositorioTexto
    RepositorioTexto ..> CircuitoCargado : crea
    RepositorioTexto ..> Circuito : lee y reconstruye
    RepositorioTexto ..> CatalogoCompuertas : tipo → creador
    RepositorioTexto ..> FormatoCircuitoException : lanza (contenido inválido)
    CircuitoCargado --> Circuito
    CircuitoCargado "1" *-- "1" PosicionesCircuito
    PosicionesCircuito "1" *-- "0..*" Posicion
    PosicionesCircuito ..> Tipo
```

El **modelo no conoce los archivos**: `Circuito` no depende de este paquete. La posición `(x, y)` pertenece a la
vista, por eso viaja en `PosicionesCircuito` (que no depende de Swing). Formato del archivo: ver `FormatoArchivo.md`.

## 5. Vista y controlador

```mermaid
classDiagram
    direction TB
    class Dibujable {
        <<interface>>
        +dibujar(Graphics2D)
    }
    class Arrastrable {
        <<interface>>
        +contiene(int, int) boolean
        +moverA(int, int)
        +getX() int
        +getY() int
    }
    class ComponenteVisual {
        <<abstract>>
        -int x
        -int y
        -List~PuntoConexion~ pines
        #crearForma()* Shape
        +eliminarDelModelo(Circuito)*
        +getTipoElemento()* Tipo
        +getNombreElemento()* String
        +getFuenteConectada(int) FuenteLogica
        +dibujar(Graphics2D)
        +contiene(int, int) boolean
    }
    class VisualCompuerta {
        <<abstract>>
        -Compuerta compuerta
    }
    class VisualAND
    class VisualOR
    class VisualNOT
    class VisualXOR
    class VisualNAND
    class VisualNOR
    class VisualEntrada {
        -EntradaLogica entrada
    }
    class VisualSalida {
        -SalidaLogica salida
    }
    class CatalogoVisuales {
        <<final>>
        +crear(Compuerta, int, int) VisualCompuerta
    }
    class PuntoConexion {
        -Sentido sentido
        -int indice
        +getValor() boolean
    }
    class Cable {
        +dibujar(Graphics2D)
        +contiene(int, int) boolean
    }
    class PanelCircuito {
        -List~ComponenteVisual~ componentes
        -List~Cable~ cables
    }
    class EscuchadorPanel {
        <<interface>>
        +alConectar(PuntoConexion, PuntoConexion)
        +alHacerClicEnPin(PuntoConexion)
        +alEliminarSeleccion()
    }
    class EscuchadorVentana {
        <<interface>>
        +alAgregarElemento(ElementoPaleta)
        +alEvaluar()
        +alGenerarTablaVerdad()
        +alEliminarSeleccion()
        +alLimpiar()
        +alCargarEjemplo()
        +alGuardar()
        +alAbrir()
    }
    class VistaCircuito {
        <<interface>>
        +getPanelCircuito() PanelCircuito
        +getPanelTabla() PanelTablaVerdad
        +mostrarEstado(String)
        +mostrarError(String)
        +confirmar(String) boolean
        +elegirArchivoParaGuardar() Path
        +elegirArchivoParaAbrir() Path
    }
    class VentanaPrincipal
    class ControladorCircuito {
        -Circuito circuito
        -RepositorioCircuitos repositorio
    }
    class RepositorioCircuitos {
        <<interface>>
    }

    Dibujable <|.. ComponenteVisual
    Arrastrable <|.. ComponenteVisual
    Dibujable <|.. Cable
    ComponenteVisual <|-- VisualCompuerta
    ComponenteVisual <|-- VisualEntrada
    ComponenteVisual <|-- VisualSalida
    VisualCompuerta <|-- VisualAND
    VisualCompuerta <|-- VisualOR
    VisualCompuerta <|-- VisualNOT
    VisualCompuerta <|-- VisualXOR
    VisualCompuerta <|-- VisualNAND
    VisualCompuerta <|-- VisualNOR
    CatalogoVisuales ..> VisualCompuerta : crea
    ComponenteVisual "1" *-- "1..*" PuntoConexion
    PanelCircuito "1" o-- "0..*" ComponenteVisual
    PanelCircuito "1" o-- "0..*" Cable
    Cable --> PuntoConexion : origen y destino
    VistaCircuito <|.. VentanaPrincipal
    VentanaPrincipal *-- PanelCircuito
    EscuchadorPanel <|.. ControladorCircuito
    EscuchadorVentana <|.. ControladorCircuito
    ControladorCircuito --> VistaCircuito
    ControladorCircuito --> Circuito
    ControladorCircuito --> RepositorioCircuitos
    ControladorCircuito ..> CatalogoVisuales : reconstruye la vista al abrir
```
