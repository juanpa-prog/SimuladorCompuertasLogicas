package compuertas.modelo;

/** Compuerta AND: verdadera solo cuando A y B son verdaderas. */
public class CompuertaAND extends CompuertaBinaria {

    public CompuertaAND(String nombre) {
        super(nombre);
    }

    public CompuertaAND(String nombre, FuenteLogica a, FuenteLogica b) {
        super(nombre, a, b);
    }

    @Override
    public String getTipo() {
        return "AND";
    }

    @Override
    public boolean evaluar() {
        // Se leen ambas entradas siempre, para detectar una entrada sin conectar.
        boolean a = valorA();
        boolean b = valorB();
        return a && b;
    }
}
