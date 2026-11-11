package compuertas.modelo;

import compuertas.modelo.excepciones.ConexionInvalidaException;

/**
 * Compuerta con dos entradas (AND, OR, XOR, NAND, NOR). Agrupa lo que
 * esas cinco comparten para no duplicarlo; NOT, con una sola entrada, no
 * hereda de aquí (resuelve el reto "¿qué pasa con NOT?" de la clase 05).
 */
public abstract class CompuertaBinaria extends Compuerta {

    private FuenteLogica a;
    private FuenteLogica b;

    /** Compuerta sin conectar (la GUI conecta después con cables). */
    protected CompuertaBinaria(String nombre) {
        super(nombre);
    }

    /** Compuerta ya conectada, como en la red de objetos de la clase. */
    protected CompuertaBinaria(String nombre, FuenteLogica a, FuenteLogica b) {
        super(nombre);
        if (a == null || b == null) {
            throw new IllegalArgumentException("Las entradas de " + nombre + " no pueden ser null");
        }
        this.a = a;
        this.b = b;
    }

    @Override
    public final int getNumeroEntradas() {
        return 2;
    }

    @Override
    public FuenteLogica getEntrada(int indice) {
        switch (indice) {
            case 0:
                return a;
            case 1:
                return b;
            default:
                throw new ConexionInvalidaException("Índice de entrada inválido: " + indice);
        }
    }

    @Override
    protected void asignarEntrada(int indice, FuenteLogica fuente) {
        if (indice == 0) {
            a = fuente;
        } else {
            b = fuente;
        }
    }

    protected final boolean valorA() {
        return exigirEntrada(0).getValor();
    }

    protected final boolean valorB() {
        return exigirEntrada(1).getValor();
    }
}
