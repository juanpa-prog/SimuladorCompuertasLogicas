# Diagramas de clases (UML)

Visibilidad: `+` público, `-` privado, `#` protegido. Triángulo hueco = herencia o implementación,
rombo negro = composición, flecha simple = asociación.

## 1. Modelo

```mermaid
classDiagram
    direction TB

    class FuenteLogica {
        <<interface>>
        +getValor() boolean
        +dependeDe(otra) boolean
    }
    class EntradaLogica {
        -String nombre
        -boolean valor
        +EntradaLogica(boolean)
        +EntradaLogica(String)
        +EntradaLogica(String, boolean)
        +getValor() boolean
        +setValor(boolean)
        +alternar()
    }
    class Compuerta {
        <<abstract>>
        -String nombre
        +getTipo()* String
        +getNumeroEntradas()* int
        +getEntrada(int)* FuenteLogica
        #asignarEntrada(int, FuenteLogica)*
        +evaluar()* boolean
        +getValor() boolean
        +conectar(int, FuenteLogica)
        +desconectar(int)
        +desconectarFuente(FuenteLogica)
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
        +conectar(FuenteLogica)
        +getValor() boolean
    }
    class Circuito {
        -String nombre
        -List~EntradaLogica~ entradas
        -List~Compuerta~ compuertas
        -List~SalidaLogica~ salidas
        +agregarEntrada() EntradaLogica
        +agregarCompuerta(CreadorCompuerta) Compuerta
        +agregarSalida() SalidaLogica
        +conectar(Compuerta, int, FuenteLogica)
        +conectarSalida(SalidaLogica, FuenteLogica)
        +eliminarCompuerta(Compuerta) boolean
        +evaluarSalidas() Map
        +generarTablaVerdad() TablaVerdad
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

## 2. Fábrica (Factory Method) y configuración (Singleton)

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

## 3. Vista y controlador

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
    }
    class EscuchadorVentana {
        <<interface>>
    }
    class VistaCircuito {
        <<interface>>
    }
    class VentanaPrincipal
    class ControladorCircuito

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
```
