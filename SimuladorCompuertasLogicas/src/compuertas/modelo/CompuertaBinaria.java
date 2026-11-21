package compuertas.modelo;

import compuertas.modelo.excepciones.ConexionInvalidaException;

/**
 * Compuerta con dos entradas (AND, OR, XOR, NAND, NOR). 
 */
public abstract class CompuertaBinaria extends Compuerta {

    private FuenteLogica a;
    private FuenteLogica b;


    protected CompuertaBinaria(String nombre) {
        super(nombre);
    }

   
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
