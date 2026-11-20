package compuertas.persistencia;

/** Resultado de cargar un archivo: el circuito reconstruido y las posiciones guardadas. */
public final class CircuitoCargado {

    private final compuertas.modelo.Circuito circuito;
    private final PosicionesCircuito posiciones;

    public CircuitoCargado(compuertas.modelo.Circuito circuito, PosicionesCircuito posiciones) {
        this.circuito = circuito;
        this.posiciones = posiciones;
    }

    public compuertas.modelo.Circuito getCircuito() {
        return circuito;
    }

    public PosicionesCircuito getPosiciones() {
        return posiciones;
    }
}
