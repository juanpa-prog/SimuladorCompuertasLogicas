package compuertas.modelo;

/** Compuerta NOR: negación de OR. */
public class CompuertaNOR extends CompuertaBinaria {

    public CompuertaNOR(String nombre) {
        super(nombre);
    }

    public CompuertaNOR(String nombre, FuenteLogica a, FuenteLogica b) {
        super(nombre, a, b);
    }

    @Override
    public String getTipo() {
        return "NOR";
    }

    @Override
    public boolean evaluar() {
        // Se leen ambas entradas siempre, para detectar una entrada sin conectar.
        boolean a = valorA();
        boolean b = valorB();
        return !(a || b);
    }
}
