package compuertas.modelo;

/** Compuerta OR: verdadera cuando al menos una entrada es verdadera. */
public class CompuertaOR extends CompuertaBinaria {

    public CompuertaOR(String nombre) {
        super(nombre);
    }

    public CompuertaOR(String nombre, FuenteLogica a, FuenteLogica b) {
        super(nombre, a, b);
    }

    @Override
    public String getTipo() {
        return "OR";
    }

    @Override
    public boolean evaluar() {
        // Se leen ambas entradas siempre, para detectar una entrada sin conectar.
        boolean a = valorA();
        boolean b = valorB();
        return a || b;
    }
}
