package compuertas.modelo;

import compuertas.modelo.excepciones.ConexionInvalidaException;

/** Compuerta NOT: invierte su única entrada. */
public class CompuertaNOT extends Compuerta {

    private FuenteLogica entrada;

    public CompuertaNOT(String nombre) {
        super(nombre);
    }

    public CompuertaNOT(String nombre, FuenteLogica entrada) {
        super(nombre);
        if (entrada == null) {
            throw new IllegalArgumentException("La entrada de " + nombre + " no puede ser null");
        }
        this.entrada = entrada;
    }

    @Override
    public String getTipo() {
        return "NOT";
    }

    @Override
    public int getNumeroEntradas() {
        return 1;
    }

    @Override
    public FuenteLogica getEntrada(int indice) {
        if (indice != 0) {
            throw new ConexionInvalidaException("Índice de entrada inválido: " + indice);
        }
        return entrada;
    }

    @Override
    protected void asignarEntrada(int indice, FuenteLogica fuente) {
        entrada = fuente;
    }

    @Override
    public boolean evaluar() {
        return !exigirEntrada(0).getValor();
    }
}
